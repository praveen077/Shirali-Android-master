package com.shirali.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.shirali.activity.NewCampiagnActivity;
import com.shirali.controls.Controls;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;

/**
 * Created by Sagar on 20/2/18.
 */

public class SongPlayInBackgroundBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equalsIgnoreCase("Current_Song_Play")) {
                int song_number = intent.getIntExtra("song_number", 0);
                NewCampiagnActivity.isFromCampaign = false;
                if (Constants.isSongPlay) {
                    if (UserModel.getInstance().isSongPlayed) {
                        UserModel.getInstance().isSongPlayed = false;
                        if (Constants.isChangeSong) {
                                Constants.isChangeSong = false;
                                Controls.nextControl(context);
                        } else {
                            if (Constants.isPlay) {
                                Controls.pauseControl(context);
                                Constants.seekTo = 0;
                                Controls.seekToControl(context);
                            }
                            Constants.SONG_NUMBER = song_number;
                            Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                    }
                } else {
                    if (UserModel.getInstance().isAdShowForRadioFirstTime) {
                        Constants.SONG_NUMBER = song_number;
                        Constants.song = "";
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    } else {
                        Controls.playControl(context);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
