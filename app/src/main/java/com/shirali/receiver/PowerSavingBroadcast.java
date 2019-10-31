package com.shirali.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;

public class PowerSavingBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (pm.isPowerSaveMode() || level <= 15) {
                if (UserModel.getInstance().isPhoneDischarged){
                    Utility.sendBatteryNotification(context);
                    UserModel.getInstance().isPhoneDischarged = false;
                }
            }else {
                UserModel.getInstance().isPhoneDischarged = true;
            }
        } else {
            if (level <= 15) {
                if (UserModel.getInstance().isPhoneDischarged){
                    Utility.sendBatteryNotification(context);
                    UserModel.getInstance().isPhoneDischarged = false;
                }
            }else {
                UserModel.getInstance().isPhoneDischarged = true;
            }
        }
    }
}
