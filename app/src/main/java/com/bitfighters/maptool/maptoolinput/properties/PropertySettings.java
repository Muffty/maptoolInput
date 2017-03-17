package com.bitfighters.maptool.maptoolinput.properties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bitfighters.maptool.maptoolinput.Connector;
import com.bitfighters.maptool.maptoolinput.MainTab;
import com.bitfighters.maptool.maptoolinput.MyData;

import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TokenProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 11.03.2017.
 */

public class PropertySettings {

    private static final String SAVED_PROPERTY_SETTINGS_STRING = "SP_s";
    private static final String SAVED_PROPERTY_FACTORIES_STRING = "SP_f_";
    private static final String SAVED_PROPERTY_ORDER_STRING = "SP_o_";
    private static final String SAVED_PROPERTY_HIDDEN_STRING = "SP_h_";
    private static final String SAVED_PROPERTY_NAME_END_STRING = "_*&#_";
    private static PropertySettings instance;

    public static PropertySettings getInstance(){
        if(instance == null)
            instance = new PropertySettings();
        return instance;
    }
    public static void reset(){
        instance = new PropertySettings();
    }


    private Map<String, String> propertyFactory;

    private List<String> propertyOrder;
    private Set<String> hiddenProperties;
    private Set<String> campaignProperties;
    private int currentSelectedSettingInDialog;
    private String lastLoadedSetting = "";
    private String draggingProperty;

    private PropertySettings(){
        propertyFactory = new HashMap<>();
        propertyOrder = new LinkedList<>();
        hiddenProperties = new HashSet<>();
        campaignProperties = new HashSet<>();
    }

    public PropertyViewFactory getFactoryFor(String property){
        return PropertyViewFactory.getFactoryByName(propertyFactory.get(property));
    }

    public void setFactoryFor(String property, PropertyViewFactory factory){
        propertyFactory.put(property, factory.getName());
    }

    public View getViewFor(MainTab parent, String property, String currentValue, boolean showAsHidden, boolean showHideButton, GUID token, GUID zone){
        return getFactoryFor(property).createViewFor(parent, property, currentValue, showAsHidden, showHideButton, token, zone);
    }

    public void addAllViewsIn(LinearLayout parentView, MainTab parent, AndroidToken token, boolean showHidden, GUID zone){

        CheckPropertyOrderCorrect();

        for (int i = 0; i < propertyOrder.size(); i++) {
            String property = propertyOrder.get(i);

            TokenProperty defaultProperty = getDefaultProperty(property);

            if(defaultProperty == null)
                continue;   //Is no property of this campaign!


            if(!isDisplayProperty(property))
                continue;

            boolean hidden = hiddenProperties.contains(property);
            if(hidden && !showHidden)
                continue;

            String value = getPropertyValue(token, property);
            parentView.addView(getViewFor(parent, property, value, hidden, showHidden, token.id, zone));
        }
    }

    private void CheckPropertyOrderCorrect() {
        List<String> unsortedProperties = new LinkedList<>();
        campaignProperties.clear();
        for(List<TokenProperty> properties: MyData.instance.campaign.campaignProperties.tokenTypeMap.values()){
            for (TokenProperty property:properties) {
                unsortedProperties.add(property.getName());
                campaignProperties.add(property.getName());
            }
        }

        unsortedProperties.removeAll(propertyOrder);

        propertyOrder.addAll(unsortedProperties);
    }

    /**
     *
     * @param property
     * @return true if property is not hidden
     */
    public boolean toggleIsHidden(String property) {
        if(hiddenProperties.remove(property)){
            return false;
        }else{
            hiddenProperties.add(property);
            return true;
        }
    }

    public void saveSettings(){
        SharedPreferences dataRead = PreferenceManager.getDefaultSharedPreferences(MainTab.instance);

        Set<String> savedDataNames = dataRead.getStringSet(SAVED_PROPERTY_SETTINGS_STRING, null);
        if(savedDataNames == null){
            saveSettingAsNew();
        }else{
            new AlertDialog.Builder(MainTab.instance)
                    .setTitle("Save Setting")
                    .setMessage("Save new or override an existing setting?")
                    .setPositiveButton("New", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveSettingAsNew();
                        }

                    })
                    .setNegativeButton("Override", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveSettingsOverride();
                        }
                    })
                    .show();
        }
    }

    private void saveSettingsOverride() {

        SharedPreferences dataRead = PreferenceManager.getDefaultSharedPreferences(MainTab.instance);

        Set<String> savedDataNames = dataRead.getStringSet(SAVED_PROPERTY_SETTINGS_STRING, null);

        final CharSequence[] items = new CharSequence[savedDataNames.size()];
        Iterator<String> iter = savedDataNames.iterator();
        int i = 0;
        currentSelectedSettingInDialog = -1;
        while(iter.hasNext()){
            String name = iter.next();
            items[i] = name;

            if(name.equals(lastLoadedSetting))
                currentSelectedSettingInDialog = i;
            i++;
        }

        new AlertDialog.Builder(MainTab.instance)
                .setTitle("Save Setting")
                .setSingleChoiceItems(items, currentSelectedSettingInDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCurrentSelectedSettingInDialog(which);
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selected = getCurrentSelectedSettingInDialog();
                        if(selected != -1)
                            saveSetting(items[selected].toString());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveSettingAsNew() {
        final EditText input = new EditText(MainTab.instance);
        new AlertDialog.Builder(MainTab.instance)
                .setIcon(android.R.drawable.ic_menu_save)
                .setTitle("Save New Setting")
                .setMessage("Name of Setting:")
                .setView(input)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().toString().trim().length() == 0){
                            showNotSavedError();
                        }else{
                            saveSetting(input.getText().toString());
                        }
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNotSavedError() {
        new AlertDialog.Builder(MainTab.instance)
                .setIcon(android.R.drawable.stat_notify_error)
                .setTitle("Settings not saved!")
                .setMessage("No correct name specified!")
                .setNeutralButton("Okay", null)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveSettingAsNew();
                    }
                })
                .show();
    }
    private void showNotSavedErrorSerialize() {
        new AlertDialog.Builder(MainTab.instance)
                .setIcon(android.R.drawable.stat_notify_error)
                .setTitle("Settings not saved!")
                .setMessage("Settings could not be serialized \n(-> blame Tobi..)")
                .setNeutralButton(":( Okay", null)
                .show();
    }
    private void showNotLoadedError() {
        new AlertDialog.Builder(MainTab.instance)
                .setIcon(android.R.drawable.stat_notify_error)
                .setTitle("Settings not loaded!")
                .setMessage("Settings could not be loaded! \n(-> blame Tobi..)")
                .setNeutralButton(":( Okay", null)
                .show();
    }

    private void saveSetting(String name) {

        SharedPreferences dataRead = PreferenceManager.getDefaultSharedPreferences(MainTab.instance);
        Set<String> savedDataNames = dataRead.getStringSet(SAVED_PROPERTY_SETTINGS_STRING, new HashSet<String>());


        SharedPreferences.Editor data = PreferenceManager.getDefaultSharedPreferences(MainTab.instance).edit();

        String factorySer, orderSer, hiddenSer;
        try {
            factorySer = objectToString(propertyFactory);
            orderSer = objectToString(propertyOrder);
            hiddenSer = objectToString(hiddenProperties);
        } catch (IOException e) {
            e.printStackTrace();
            showNotSavedErrorSerialize();
            return;
        }
        savedDataNames.add(name);
        data.putStringSet(SAVED_PROPERTY_SETTINGS_STRING, savedDataNames);
        data.putString(SAVED_PROPERTY_FACTORIES_STRING + name + SAVED_PROPERTY_NAME_END_STRING, factorySer);
        data.putString(SAVED_PROPERTY_HIDDEN_STRING + name + SAVED_PROPERTY_NAME_END_STRING, hiddenSer);
        data.putString(SAVED_PROPERTY_ORDER_STRING + name + SAVED_PROPERTY_NAME_END_STRING, orderSer);

        data.apply();

        System.out.println("Saved Settings '"+name+"'. factorySer="+factorySer);
    }

    private String objectToString(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oStream = new ObjectOutputStream(baos);

        oStream.writeObject(obj);
        String str = new String(baos.toByteArray(), "ISO-8859-1");
        oStream.close();
        return str;
    }

    private Object stringToObject(String string) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(string.getBytes("ISO-8859-1")));
        return ois.readObject();
    }


    public void loadSettings(){
        SharedPreferences dataRead = PreferenceManager.getDefaultSharedPreferences(MainTab.instance);

        Set<String> savedDataNames = dataRead.getStringSet(SAVED_PROPERTY_SETTINGS_STRING, null);
        if(savedDataNames == null){
            new AlertDialog.Builder(MainTab.instance)
                    .setTitle("Load Setting")
                    .setMessage("No Settings found!")
                    .setNegativeButton("Okay",null)
                    .show();
        }else{
            final CharSequence[] items = new CharSequence[savedDataNames.size()];
            Iterator<String> iter = savedDataNames.iterator();
            int i = 0;
            currentSelectedSettingInDialog = -1;
            while(iter.hasNext()){
                String name = iter.next();
                items[i] = name;

                if(name.equals(lastLoadedSetting))
                    currentSelectedSettingInDialog = i;
                i++;
            }

            new AlertDialog.Builder(MainTab.instance).setTitle("Load Setting")
                    .setSingleChoiceItems(items, currentSelectedSettingInDialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setCurrentSelectedSettingInDialog(which);
                        }
                    })
                    .setPositiveButton("Load", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int selected = getCurrentSelectedSettingInDialog();
                            if(selected != -1)
                                loadSettings(items[selected].toString());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void loadSettings(String name) {

        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(MainTab.instance);
        String factorySer = data.getString(SAVED_PROPERTY_FACTORIES_STRING + name + SAVED_PROPERTY_NAME_END_STRING, null);
        String hiddenSer = data.getString(SAVED_PROPERTY_HIDDEN_STRING + name + SAVED_PROPERTY_NAME_END_STRING, null);
        String orderSer = data.getString(SAVED_PROPERTY_ORDER_STRING + name + SAVED_PROPERTY_NAME_END_STRING, null);

        if(factorySer == null ||hiddenSer == null || orderSer == null){
            showNotLoadedError();
            return;
        }

        try {
            Map<String, String> factory = (Map<String, String>)stringToObject(factorySer);
            List<String> order = (List<String>)stringToObject(orderSer);
            Set<String> hidden = (Set<String>)stringToObject(hiddenSer);


            propertyFactory = factory;
            propertyOrder = order;
            hiddenProperties = hidden;
            System.out.println("Loaded Settings '"+name+"'!");
            lastLoadedSetting = name;
            MainTab.instance.sendUpdateView();
        } catch (IOException e) {
            e.printStackTrace();
            showNotLoadedError();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            showNotLoadedError();
            return;
        }
    }

    public void setCurrentSelectedSettingInDialog(int currentSelectedSettingInDialog) {
        this.currentSelectedSettingInDialog = currentSelectedSettingInDialog;
    }

    public int getCurrentSelectedSettingInDialog() {
        return currentSelectedSettingInDialog;
    }

    public void setDraggingProperty(String draggingProperty) {
        this.draggingProperty = draggingProperty;
    }

    public int setDraggingPropertyAt(String property) {
        int to = propertyOrder.indexOf(property);
        int from = propertyOrder.indexOf(draggingProperty);
       // if(from != -1 && from < to)
        //    to--;
        propertyOrder.remove(draggingProperty);
        propertyOrder.add(to, draggingProperty);
        return to;
    }

    public String getPropertyValue(GUID token, String property) {

        return getPropertyValue(MyData.instance.getToken(token), property);

    }
    public String getPropertyValue(AndroidToken token, String property) {

        String value = token.properties.get(property);

        if(value == null){
            for(List<TokenProperty> properties: MyData.instance.campaign.campaignProperties.tokenTypeMap.values()){
                for (TokenProperty p2:properties) {
                    if(p2.getName().equals(property))
                        return p2.getDefaultValue();
                }
            }
            return "";
        }
        return value;

    }
    public TokenProperty getDefaultProperty(String property) {

        for(List<TokenProperty> properties: MyData.instance.campaign.campaignProperties.tokenTypeMap.values()){
            for (TokenProperty p2:properties) {
                if(p2.getName().equals(property))
                    return p2;
            }
        }
        return null;
    }
    public boolean isDisplayProperty(String property) {

        for (Map.Entry<String, List<TokenProperty>> pair: MyData.instance.campaign.campaignProperties.tokenTypeMap.entrySet()) {
            if(pair.getKey().equals("Basic"))
            for (TokenProperty p2:pair.getValue()) {
                if(p2.getName().equals(property))
                    return true;
            }
        }

        return false;
    }

    public void setPropertyValue(GUID zone, GUID token, String property, String value, boolean sendUpdate) {

        AndroidToken aToken = MyData.instance.getToken(token);
        aToken.properties.put(property, value);
        Connector.currentConnection.sendTokenUpdate(zone, aToken);

    }
}
