package com.shirali.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.util.Constants;

import java.util.Date;


public class CallReceiver extends PhonecallReceiver {

    static boolean isAlreadyPause;

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.e("onIncomingCallReceived", number + " : " + start); // incoming
        playPauseMusic(ctx, 1);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.e("onOutgoingCallStarted", number + " : " + start); // outgoing
        playPauseMusic(ctx, 1);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.e("onIncomingCallEnded", number + " : " + start); //phone cut
        playPauseMusic(ctx, 2);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.e("onOutgoingCallEnded", number + " : " + start); // ended
        playPauseMusic(ctx, 2);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.e("onMissedCall", number + " : " + start); // disconnected
        playPauseMusic(ctx, 2);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.e("onIncomingCallAnswered", number + " : " + start);
        playPauseMusic(ctx, 3);

    }

    public void playPauseMusic(final Context context, int call) {
        //Toast.makeText(context, "call : " + call, Toast.LENGTH_SHORT).show();

        if(call == 1){
            if (Constants.isSongPlay) {
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
                    //binding.imgPlay.setImageResource(R.drawable.play);
                }
            }
        }else if(call == 2){
            if (Constants.isSongPlay) {
                if (!Constants.isPlay) {
                    Controls.playControl(context);
                    //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                }
            }
        }

        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
    }

}
