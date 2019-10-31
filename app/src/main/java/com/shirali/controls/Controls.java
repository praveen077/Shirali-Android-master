package com.shirali.controls;

import android.content.Context;

import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

public class Controls {
    static String LOG_CLASS = "Controls";

    public static void playControl(Context context) {
        sendMessage("Play");
    }

    public static void stopControl(Context context) {
        sendMessage("Stop");
    }

    public static void seekToControl(Context context) {
        sendMessage("Seek");
    }

    public static void pauseControl(Context context) {
        sendMessage("Pause");
    }

    /* --- KIPL -> AKM: Close Control ---*/
    public static void dismissControl(Context context) {
        sendMessage("Dismiss");
    }

    public static void nextControl(Context context) {
        UserModel.getInstance().getAppSetting(context);
        Constants.isPageSelectedFromNextOfPrevious = false;
        boolean isServiceRunning = Utility.isServiceRunning(SongPlayService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (Constants.SONGS_LIST.size() > 0) {
            if (Utility.getBooleaPreferences(context, "suffle")) {
                if (Constants.SONGS_LIST.size() > 0) {
                    Constants.isPrevious = false;
                    UserModel.getInstance().playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id, true);
                }
            } else {
                int currentSongId = 0;
                for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
                    if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id)) {
                        currentSongId = i;
                    }
                }
                Constants.SONG_NUMBER = currentSongId;

                Constants.SONGS_LIST = UserModel.getInstance().listOfActualSong;
                if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                    Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                    Constants.isPrevious = false;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                } else {
                    Constants.SONG_NUMBER = 0;
                    Constants.isPrevious = false;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                }
            }
        }
        Constants.isPlay = false;
    }

    public static void previousControl(Context context) {
        UserModel.getInstance().getAppSetting(context);
        Constants.isPageSelectedFromNextOfPrevious = false;
        boolean isServiceRunning = Utility.isServiceRunning(SongPlayService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (Constants.SONGS_LIST.size() > 0) {
            if (Utility.getBooleaPreferences(context, "suffle")) {
                if (Constants.SONGS_LIST.size() > 0) {
                    Constants.isPrevious = true;
                    UserModel.getInstance().playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id, false);
                }
            } else {
                int currentSongId = 0;
                for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
                    if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id)) {
                        currentSongId = i;
                    }
                }
                Constants.SONG_NUMBER = currentSongId;

                Constants.SONGS_LIST = UserModel.getInstance().listOfActualSong;
                if (Constants.SONG_NUMBER > 0) {
                    Constants.isPrevious = true;
                    Constants.SONG_NUMBER = Constants.SONG_NUMBER - 1;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                } else {
                    Constants.isPrevious = true;
                    Constants.SONG_NUMBER = Constants.SONGS_LIST.size() - 1;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                }
            }
        }
        Constants.isPlay = false;
    }

    public static void sendMessage(String message) {
        try {
            Constants.PLAY_PAUSE_HANDLER.sendMessage(Constants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void nextStation(Context context) {
        UserModel.getInstance().getAppSetting(context);
        boolean isServiceRunning = Utility.isServiceRunning(SongPlayService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (Constants.StationList.size() > 0) {
            if (Constants.SONG_NUMBER < (Constants.StationList.size() - 1)) {
                Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            } else {
                Constants.SONG_NUMBER = 0;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            }
            pauseControl(context);
        }
        Constants.isPlay = false;
    }

    public static void previousStation(Context context) {
        UserModel.getInstance().getAppSetting(context);
        boolean isServiceRunning = Utility.isServiceRunning(SongPlayService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (Constants.StationList.size() > 0) {
            if (Constants.SONG_NUMBER > 0) {
                Constants.SONG_NUMBER = Constants.SONG_NUMBER - 1;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            } else {
                Constants.SONG_NUMBER = Constants.StationList.size() - 1;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            }
            pauseControl(context);
        }
        Constants.isPlay = false;
    }
}
