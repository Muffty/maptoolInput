package com.bitfighters.maptool.maptoolinput;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Tobias on 18.02.2017.
 */

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(Connector.currentConnection == null){
            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            am.cancel(Connector.alarmIntent);
        }else{
            Connector.currentConnection.sendHeartbeat();
        }
    }


}
