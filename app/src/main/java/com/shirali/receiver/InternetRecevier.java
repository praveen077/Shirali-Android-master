package com.shirali.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import com.shirali.App;
import com.shirali.util.Constants;

/**
 * Created by Sagar on 5/4/18.
 */

public class InternetRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        App app = (App) context.getApplicationContext();
        if (isConnected(context)) {
            if (Constants.isPlay) {
                if (!app.isConnected) {
                    app.isConnected = true;
                    app.connectSocket();
                }
            }
        }else {
            app.isConnected = false;
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }
}
