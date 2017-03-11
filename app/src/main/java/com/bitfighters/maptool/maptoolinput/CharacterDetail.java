package com.bitfighters.maptool.maptoolinput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.AndroidTokenState;
import net.rptools.maptool.model.AndroidZone;
import net.rptools.maptool.model.GUID;

import java.util.HashMap;
import java.util.Map;

public class CharacterDetail extends AppCompatActivity {

    public static GUID characterToDisplay;
    public static GUID zone;

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

        for(Map.Entry<String, Boolean> entry: token.state.entrySet()){
            if(entry.getValue() == null) {
                modifiedStates.put(entry.getKey(), new Boolean(false));
                addCheckBox(entry.getKey(), false, v);
            }else {
                modifiedStates.put(entry.getKey(), new Boolean(entry.getValue()));

                addCheckBox(entry.getKey(), entry.getValue().booleanValue(), v);
            }
        }

        for (Map.Entry<String, AndroidTokenState> tokenState: MyData.instance.campaign.campaignProperties.tokenStates.entrySet()){
            if(!modifiedStates.containsKey(tokenState.getKey())){
                modifiedStates.put(tokenState.getKey(), false);

                addCheckBox(tokenState.getKey(), false, v);
            }
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
