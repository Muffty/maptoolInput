package com.bitfighters.maptool.maptoolinput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.AndroidTokenState;
import net.rptools.maptool.model.AndroidZone;
import net.rptools.maptool.model.GUID;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class CharacterDetail extends AppCompatActivity {

    public static GUID characterToDisplay;
    public static GUID zone;

    public static LinkedList<String> lastEdited = new LinkedList<>();

    private AndroidToken token;
    private Map<String, Boolean> modifiedStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail2);

        token = MyData.instance.getToken(characterToDisplay);
        if(token == null){
            finish();
            return;
        }


        modifiedStates = new HashMap<>(MyData.instance.campaign.campaignProperties.tokenStates.size());

        LinearLayout v = (LinearLayout)findViewById(R.id.statsList);

        HashMap<String, Boolean> leftout = new HashMap<>();

        for(Map.Entry<String, Boolean> entry: token.state.entrySet()){

            if(lastEdited.contains(entry.getKey())){
                if(entry.getValue() == null) {
                    leftout.put(entry.getKey(), false);
                }else{
                    leftout.put(entry.getKey(), entry.getValue());
                }
                continue;
            }


            if(entry.getValue() == null) {
                modifiedStates.put(entry.getKey(), new Boolean(false));
                addCheckBox(entry.getKey(), false, v);
            }else {
                modifiedStates.put(entry.getKey(), new Boolean(entry.getValue()));

                addCheckBox(entry.getKey(), entry.getValue().booleanValue(), v);
            }
        }
        for (Map.Entry<String, AndroidTokenState> tokenState: MyData.instance.campaign.campaignProperties.tokenStates.entrySet()){
            if(!modifiedStates.containsKey(tokenState.getKey()) && !leftout.keySet().contains(tokenState.getKey())){



                if(lastEdited.contains(tokenState.getKey())){
                    leftout.put(tokenState.getKey(), false);
                    continue;
                }

                modifiedStates.put(tokenState.getKey(), false);

                addCheckBox(tokenState.getKey(), false, v);
            }
        }

        TextView notUsed = new TextView(this);
        notUsed.setText("Not Used:");
        v.addView(notUsed,0);
        for (int i = lastEdited.size()-1; i >= 0; i--) {
            if(leftout.keySet().contains(lastEdited.get(i))){
                boolean value = leftout.get(lastEdited.get(i));
                modifiedStates.put(lastEdited.get(i), value);
                addCheckBoxAtStart(lastEdited.get(i), value, v);
            }
        }
        TextView lastUsed = new TextView(this);
        lastUsed.setText("Used:");
        v.addView(lastUsed,0);

        TextView cName = (TextView) findViewById(R.id.cName);
        cName.setText(token.name);

        ImageView cImage = (ImageView) findViewById(R.id.cImage);
        MD5Key bitmapHash = token.imageAssetMap.get(null);
        Bitmap bitmap = MyData.instance.getBitmap(bitmapHash);

        if(bitmap != null){
            cImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));
        }else{
            cImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void addCheckBox(String name, boolean checked, LinearLayout view) {
        CheckBox child = (CheckBox)getLayoutInflater().inflate(R.layout.char_state, null);
        child.setText(name);
        child.setChecked(checked);
        view.addView(child);
        final String key = name;
        child.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(lastEdited.contains(key))
                    lastEdited.remove(key);
                lastEdited.addFirst(key);
                modifiedStates.put(key, checked);
            }
        });
    }

    private void addCheckBoxAtStart(String name, boolean checked, LinearLayout view) {
        CheckBox child = (CheckBox)getLayoutInflater().inflate(R.layout.char_state, null);
        child.setText(name);
        child.setChecked(checked);
        view.addView(child, 0);
        final String key = name;
        child.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(lastEdited.contains(key))
                    lastEdited.remove(key);
                lastEdited.addFirst(key);
                modifiedStates.put(key, checked);
            }
        });
    }


    @Override
    public void onBackPressed() {
        disconnect();
    }
    private void disconnect(){

        if(haveChanges()){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Save Changes")
                    .setMessage("Do you want to save the changes?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            save();
                            finish();
                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .show();
        }else{
            finish();
        }


    }

    private boolean haveChanges() {
        for(Map.Entry<String, Boolean> entry: token.state.entrySet()){
            if(entry.getValue() == null && modifiedStates.get(entry.getKey()).booleanValue() == true)
                return true;
            else if(entry.getValue().booleanValue() != modifiedStates.get(entry.getKey()).booleanValue())
                return true;
        }
        for(Map.Entry<String, Boolean> entry: modifiedStates.entrySet()){
            if(!token.state.containsKey(entry.getKey()) && entry.getValue().booleanValue() == true)
                return true;
        }
        return false;
    }

    private void save() {
        token.state = modifiedStates;
        Connector.currentConnection.sendTokenUpdate(zone, token);
    }

    public void save(View view){
        save();
    }

    public void cancel(View view){
        finish();
    }

}
