package com.shirali.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.shirali.util.Utility;

/**
 * Created by Sagar on 18/4/18.
 */

public class PlayService extends Service {
    private Context context;
    private PowerManager.WakeLock wl;
    private WifiManager.WifiLock wifiLock;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");
            wl.acquire();
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "TAG");
            wifiLock.acquire();

            if (!Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
                Intent playIntent = new Intent(this, SongPlayService.class);
                startService(playIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (wl != null) {
            wl.release();
        }
        if (wifiLock != null) {
            wifiLock.release();
        }
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notifManager != null;
        notifManager.cancelAll();
        stopService(new Intent(context, SongPlayService.class));
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        if (wl != null) {
            wl.release();
        }
        if (wifiLock != null) {
            wifiLock.release();
        }
        stopService(new Intent(context, SongPlayService.class));
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
