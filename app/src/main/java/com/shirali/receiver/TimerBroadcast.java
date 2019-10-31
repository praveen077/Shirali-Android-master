package com.shirali.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shirali.util.Utility;

/**
 * Created by Sagar on 10/20/2017.
 */

// For receive local notification after 1 hours
public class TimerBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context k1, Intent k2) {
        // TODO Auto-generated method stub
        Utility.setIntPreferences(k1, "albumSkipCount", 0);
        Intent intent = new Intent(k1, TimerBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(k1, 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) k1.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
