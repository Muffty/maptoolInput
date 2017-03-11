package com.bitfighters.maptool.maptoolinput.properties;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitfighters.maptool.maptoolinput.MainTab;
import com.bitfighters.maptool.maptoolinput.MyData;
import com.bitfighters.maptool.maptoolinput.R;

import net.rptools.maptool.model.GUID;

/**
 * Created by admin on 11.03.2017.
 */

public class DefaultPropertyFactory extends PropertyViewFactory {

    @Override
    View createViewForInternal(final MainTab parent, final  String property, final String currentValue, boolean showAsHidden, boolean showHideButton, final GUID token, final GUID zone) {
        View child = parent.getLayoutInflater().inflate(R.layout.property_simple, null);

        TextView pTitle = (TextView)child.findViewById(R.id.pTitle);
        final TextView pValue = (TextView)child.findViewById(R.id.pValue);

        ImageButton pEdit = (ImageButton)child.findViewById(R.id.pEdit);
        final ImageButton pHide = (ImageButton)child.findViewById(R.id.pHide);

        pTitle.setText(property);
        pValue.setText(currentValue);

        pEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oldValue = PropertySettings.getInstance().getPropertyValue(token, property);
                final EditText input = new EditText(MainTab.instance);
                input.setText(oldValue);
                new AlertDialog.Builder(MainTab.instance)
                        .setIcon(android.R.drawable.ic_menu_save)
                        .setTitle("Change Property")
                        .setMessage(property +" was " + oldValue)
                        .setView(input)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newValue = input.getText().toString();
                                if(oldValue == null || !oldValue.trim().equals(newValue.trim())){
                                    pValue.setText(newValue);
                                    PropertySettings.getInstance().setPropertyValue(zone, token, property, newValue, true);
                                }
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                parent.HandlePropertyEdit(property);
            }
        });


        if(showHideButton){

            if(showAsHidden){
                pHide.setImageResource(R.drawable.hidden);
            }else{
                pHide.setImageResource(R.drawable.visible);
            }
            pHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean hidden = PropertySettings.getInstance().toggleIsHidden(property);
                    if(hidden){
                        pHide.setImageResource(R.drawable.hidden);
                    }else{
                        pHide.setImageResource(R.drawable.visible);
                    }
                }
            });
        }else{
            pHide.setVisibility(View.INVISIBLE);
        }


        return child;
    }

    @Override
    public String getName() {
        return "simple";
    }
}