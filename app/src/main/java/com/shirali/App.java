package com.shirali;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.onesignal.OneSignal;
import com.shirali.model.user.UserModel;
import com.shirali.service.PlayService;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.FontsOverride;
import com.shirali.util.Utility;
import com.shirali.widget.AppVisibilityDetector;

import java.net.URISyntaxException;

/**
 * Created by Sagar on 3/7/17.
 */

public class App extends MultiDexApplication {

    public static boolean isBackground = false;
    public boolean isConnected = false;
    private Context mContext;
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        try {
            mSocket = IO.socket(Constants.socketUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        UserModel.initInstance(mContext);
        //Crashlytics.getInstance().crash();
        try {
            FontsOverride.setDefaultFont(this, "SERIF", "Poppins-Regular.ttf");
            FontsOverride.setDefaultFont(this, "MONOSPACE", "Poppins-Bold.ttf");
            FontsOverride.setDefaultFont(this, "SANS_SERIF", "Poppins-Light.ttf");
        } catch (Exception e) {
            e.printStackTrace();
        }


        AppVisibilityDetector.init(App.this, new AppVisibilityDetector.AppVisibilityCallback() {
            @Override
            public void onAppGotoForeground() {
                Runtime.getRuntime().gc();
                isBackground = false;
                Utility.setBooleanPreferences(mContext,"ad_in_background",false);
            }

            @Override
            public void onAppGotoBackground() {
                Runtime.getRuntime().gc();
                isBackground = true;
                Utility.setBooleanPreferences(mContext,"ad_in_background",true);
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                NotificationManager notifManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                assert notifManager != null;
                notifManager.cancelAll();
                stopService(new Intent(mContext, SongPlayService.class));
                stopService(new Intent(mContext, PlayService.class));
                UserModel.getInstance().openFragment = "BROWSE";
                UserModel.getInstance().comeFromDeep = true;
            }
        });

        //Initial setup for one signal
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            stopService(new Intent(mContext, SongPlayService.class));
        }
    }

    public void connectSocket() {
        isConnected = true;
        mSocket = getSocket();
        mSocket.emit("play:listeningMusic", Utility.getUserInfo(mContext).id);
        mSocket.connect();
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void disconnectSocket() {
        isConnected = false;
        mSocket = getSocket();
        mSocket.emit("pause:listeningMusic", Utility.getUserInfo(mContext).id);
        mSocket.disconnect();
    }

    public void checkLiveUserUpdate() {
        mSocket = getSocket();
        mSocket.emit("status:listeningMusic", Utility.getUserInfo(mContext).id);
    }

}
