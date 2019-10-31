package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.fragment.AdsDialogFragment;
import com.shirali.interfaces.AdsAvailabilityCallback;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        freeMemory();
    }


    private void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public void callAdsFirstBeforeMusicPlay(){
        /* --- KIPL -> AKM: Ad vary first time ---*/

        if (Utility.getUserInfo(this).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        }else{
            if (!UserModel.getInstance().isAdPlayedOnCountOne) {
                UserModel.getInstance().isAdPlayedOnCountOne = true;

                UserModel.getInstance().showAdsIfAvailableForFirstTime(this, Utility.getUserInfo(this).id, new AdsAvailabilityCallback() {
                    @Override
                    public void adsAvailable(boolean isAdAvailable) {
                        if (isAdAvailable) {
                        } else {
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                    }
                });
                //new AdsDialogFragment().show(getFragmentManager(),"Ads");


            } else {
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            }
        }
        /* --- KIPL -> AKM ---- */
    }

    /* --- KIPL -> AKM  open subscription pop up (In case of premium song in list) :::: premiumDialog action from songservice --- */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("premiumDialog"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utility.showSubscriptionAlert(BaseActivity.this, getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
        }
    };
}
