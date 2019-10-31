package com.shirali.model.user;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.activity.NewCampiagnActivity;
import com.shirali.controls.Controls;
import com.shirali.fragment.AdsDialogFragment;
import com.shirali.interfaces.AddToMyMusicCallback;
import com.shirali.interfaces.AdsAvailabilityCallback;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.interfaces.LikeAndUnlikeCallBack;
import com.shirali.model.StreamCount;
import com.shirali.model.campaign.Campaign;
import com.shirali.model.campaign.CampaignResponse;
import com.shirali.model.campaign.Campaign_;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.PlaylistResponse;
import com.shirali.model.playlist.PlaylistUpdate;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.radio.RadioTempData;
import com.shirali.model.setting.AppSetting;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.RecentlyPlayed;
import com.shirali.model.songs.Song;
import com.shirali.receiver.TimerBroadcast;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ALARM_SERVICE;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UserModel {
    private static UserModel instance;
    public boolean isSocketOpen = true;
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("user")
    @Expose
    public User user;
    @SerializedName("token")
    @Expose
    public String token;
    public int songStatus;
    public int stationStatus;

    public Shirali shirali = new Shirali();
    public Album album = new Album();
    public RecentlyPlayed artist = new RecentlyPlayed();
    public Card card = new Card();
    public Campaign_ campaign = new Campaign_();
    public Song currentPlaySong = new Song();
    public boolean isVideoPlay = false;
    public boolean isSongPlayed = false;
    public Artist artist_id = new Artist();
    public ArrayList<Song> tempSongList = new ArrayList<>();
    public Album tempAlbum = new Album();
    public String openFragment = "";
    public boolean isPlaySongAfterAd = true;
    private String selectedGenres;
    public String suggestionLetter = "";
    public boolean checkSessionOutDialogVisibility = true;
    public boolean isForSharing = false;
    public boolean isAlbumSharing = false;
    public String albumOrPlaylistId = "";
    public String notificationType = "";
    public String notificationId = "";
    public ArrayList<Song> listOfShuffleSong = new ArrayList<>();
    public ArrayList<Song> listOfActualSong = new ArrayList<>();
    public boolean isForRenew = false, isForTrial = false;
    public int currentPosition = 0;
    public boolean isAdShowForRadioFirstTime = false;
    public Campaign_ ad;
    public boolean isSingleSongPlay = false;
    public boolean isAdPlayedOnCountOne = false;
    public RadioTempData tempData = new RadioTempData();
    public String title = "", artistName = "", artwork = "";
    public boolean isSongLoading = true;
    public long playerDuration = 0;
    public boolean isPhoneDischarged = false;
    public boolean isStationPlaying = false;
    public boolean comeFromDeep = true;
    public boolean isSongComplete = false;
    private boolean isDialogShowOneTime = false;
    public int removeSongIndex = 0;
    public int songPlayerDuration = 0;

    public static void initInstance(Context _applicationContext) {
        if (instance == null) {
            instance = new UserModel();
        }
    }

    public static UserModel getInstance() {
        return instance;
    }

    public void checkUser(Context baseContext) {
        try {
            String current_date = "";
            if (Utility.getUserInfo(baseContext).subscribePlan.trailEndDate != null) {
                if (Utility.getUserInfo(baseContext).getTrialTokan() == 1) {
                    if (Utility.getUserSetting(baseContext) != null){
                        current_date = Utility.getUserSetting(baseContext).getCurrentTime();
                    }else {
                        current_date = Utility.getCurrentTimeStamp();
                    }
                    if (Utility.compareDates(Utility.getUserInfo(baseContext).subscribePlan.trailEndDate, current_date)) {
                        UserModel.getInstance().isForTrial = true;
                    } else {
                        UserModel.getInstance().isForTrial = false;
                    }
                } else {
                    UserModel.getInstance().isForTrial = false;
                }
            } else {
                UserModel.getInstance().isForTrial = false;
            }
            if (Utility.getUserInfo(baseContext).subscribePlan.plantype.equalsIgnoreCase("Free")) {
                if (Utility.getUserInfo(baseContext).subscribePlan.subscriptionRenewDate != null && Utility.getUserInfo(baseContext).subscribePlan.subscriptionPaymentDate != null) {
                    if (Utility.dateIsExist(Utility.getUserInfo(baseContext).subscribePlan.subscriptionRenewDate, Utility.getUserInfo(baseContext).subscribePlan.subscriptionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", current_date)) {
                        UserModel.getInstance().isForRenew = true;
                    } else {
                        UserModel.getInstance().isForRenew = false;
                    }
                } else {
                    UserModel.getInstance().isForRenew = false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Add Song into My Music
    public void addToMusic(final Context context, final String user_id, String song_id) {
        Call<UserModel> call = Constants.service.addToPlaylist(user_id, song_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                if (response.isSuccessful()) {
                    try {
                        if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                if (userModel.success) {
                                    Utility.setUserInfo(context, userModel.user);
                                    UserModel.getInstance().getdata(context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Utility.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    // Remove Song into My Music
    public void removeFromMusic(final Context context, final String user_id, String song_id) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("Remove from My Music");
        mixpanelAPI.flush();
        Call<UserModel> call = Constants.service.removeFromPlaylist(user_id, song_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(context);
                } else {
                    if (userModel.success) {
                        Utility.setUserInfo(context, userModel.user);
                        UserModel.getInstance().getdata(context);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Utility.showToast(context, "Failed to remove");
            }
        });
    }

    // Get App Setting
    public void getAppSetting(final Context context) {
        Call<AppSetting> call = Constants.service.getAppSetting();
        call.enqueue(new Callback<AppSetting>() {
            @Override
            public void onResponse(Call<AppSetting> call, Response<AppSetting> response) {
                if (response.isSuccessful()) {
                    AppSetting appSetting = response.body();
                    try {
                        if (appSetting.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (appSetting.setting != null) {
                                if (appSetting.success) {
                                    try {
                                        if (appSetting.setting.androidLiveVersion.equalsIgnoreCase(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)) {
                                        } else {
                                            Utility.showUpdateDialog(context);
                                        }
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    if (appSetting.setting.current_date == null || appSetting.setting.current_date.equalsIgnoreCase("")) {
                                        appSetting.setting.current_date = Utility.getCurrentTimeStamp();
                                    }
                                    Utility.setUserSetting(context, appSetting);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AppSetting> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Add song list into playlist
    public void addToPlaylist(final Context context, ArrayList<String> song, String current_playlist_id) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("id", current_playlist_id);
        hm.put("songs", song);
        hm.put("isActive", true);
        Call<PlaylistUpdate> call = Constants.service.addToPlaylist(current_playlist_id, hm);
        call.enqueue(new Callback<PlaylistUpdate>() {
            @Override
            public void onResponse(Call<PlaylistUpdate> call, Response<PlaylistUpdate> response) {
                PlaylistUpdate playlistUpdate = response.body();
                try {
                    if (playlistUpdate.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistUpdate.playlist != null) {
                            if (playlistUpdate.success) {
                                Utility.showToast(context, playlistUpdate.message);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlaylistUpdate> call, Throwable t) {
                Utility.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    // Get All genres list
    public void getGenreData(final Context context, String type) {
        Call<GenresList> filterMusicModelCall = Constants.service.getGenres(type);
        filterMusicModelCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                if (response.isSuccessful()) {
                    try {
                        GenresList listGenre = response.body();
                        if (listGenre != null) {
                            if (listGenre.message.equalsIgnoreCase("Invalid device login.")) {
                                Utility.openSessionOutDialog(context);
                            } else {
                                if (listGenre.success) {
                                    if (listGenre.genres != null && listGenre.genres.size() > 0) {
                                        Utility.setGenres(context, listGenre);
                                        for (int i = 0; i < response.body().genres.size(); i++) {
                                            if (listGenre.genres.get(i).title.equalsIgnoreCase("Acapella (Vocal Only)")) {
                                                selectedGenres = listGenre.genres.get(i).id;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GenresList> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void clickOnAd(String ad_id) {
        Call<CampaignResponse> call = Constants.service.clickCountOnAd(ad_id);
        call.enqueue(new Callback<CampaignResponse>() {
            @Override
            public void onResponse(Call<CampaignResponse> call, Response<CampaignResponse> response) {
                if (response.isSuccessful()) {
                    CampaignResponse response1 = response.body();
                }
            }

            @Override
            public void onFailure(Call<CampaignResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Get User Playlist
    public ArrayList<String> getplaylistList(final Context context) {
        final ArrayList<String> listPlaylist = new ArrayList<>();
        Call<PlaylistResponse> call = Constants.service.getUserPlaylist(Utility.getUserInfo(context).id);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                PlaylistResponse playlistResponse = response.body();
                try {
                    if (playlistResponse.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            if (playlistResponse.songs.size() > 0) {
                                for (int i = 0; i < playlistResponse.songs.size(); i++) {
                                    if (!playlistResponse.songs.get(i).createdBy.equalsIgnoreCase(Utility.getUserInfo(context).id))
                                        listPlaylist.add(playlistResponse.songs.get(i).id);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
        return listPlaylist;
    }

    // Song Stream count according free and paid user.
    public void songsStream(final Context context, final String user_id, String song_id, String plan) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("songId", song_id);
        hm.put("plantype", plan);
        Call<StreamCount> call = Constants.service.songStreamCount(song_id, hm);
        call.enqueue(new Callback<StreamCount>() {
            @Override
            public void onResponse(Call<StreamCount> call, Response<StreamCount> response) {
                if (response.isSuccessful()) {
                    StreamCount count = response.body();
                    if (count.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (count.success) {
                            UserModel.getInstance().userStat(user_id);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamCount> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void userStat(String user_id) {
        Call<StreamCount> call = Constants.service.userStats(user_id);
        call.enqueue(new Callback<StreamCount>() {
            @Override
            public void onResponse(Call<StreamCount> call, Response<StreamCount> response) {
                StreamCount count = response.body();
                try {
                    if (count.message.equalsIgnoreCase("Invalid device login.")) {
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<StreamCount> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Update User data as requirement
    public void getdata(final Context context) {
        Call<UserModel> call = Constants.service.getUserInfo(Utility.getUserInfo(context).id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                try {
                    if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (userModel.user != null) {
                            if (userModel.success) {
                                Utility.setUserInfo(context, userModel.user);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                //Utility.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    // Get Song Status by Song ID
    public void getSongStatusByID(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = "";
        if (Constants.SONGS_LIST != null) {
            if (Constants.SONGS_LIST.size() <= Constants.SONG_NUMBER && Constants.SONGS_LIST.size() > 0) {
                s = Constants.SONGS_LIST.get(0).id;
            } else {
                if (Constants.SONGS_LIST.size() > 0) {
                    s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                } else {
                    return;
                }
            }
        }
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.getLikes(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                if (response.isSuccessful()) {
                    UserSongStatus model = response.body();
                    if (model.success) {
                        if (model.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            songStatus = model.status;
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Like a song.
    public void likeapi(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.setlike(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                UserSongStatus model = response.body();
                if (model.success) {
                    try {
                        if (model.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(mContext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            songStatus = 1;
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                            Controls.sendMessage("Like");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Unlike a song
    public void unlikeApi(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.setunlike(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                UserSongStatus model = response.body();
                if (model != null) {
                    if (!model.message.equalsIgnoreCase("Invalid device login.")) {
                        if (model.success) {
                            songStatus = 0;
                            Controls.sendMessage("Dislike");
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                        }
                    } else {
                        Utility.openSessionOutDialog(mContext);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Next song for free and paid user
    public void nextPlayMethod(final Context mContext) {
        UserModel.getInstance().getAppSetting(mContext);
        int count = Utility.getIntPreferences(mContext, "albumSkipCount");
        count = count + 1;
        if (count < 4) {
            Controls.nextControl(mContext);
        } else {
            if (count != 4) {
                String time = Utility.getLongPreferences(mContext, "skip_time");
                if (time == null || time.equalsIgnoreCase("")){
                    time = Utility.getCurrentTimeEvent();
                }
                long a = Utility.timeDiffrence(time);
                long b = Long.parseLong(Utility.getLongPreferences(mContext, "timer_time")) * 60 * 1000;
                final long dif = b - a;
                if (dif <= 0) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(mContext);
                    }
                    Controls.nextControl(mContext);
                    Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                } else {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            Runtime.getRuntime().gc();
                            UserModel.getInstance().showAdsIfAvailable(mContext, Utility.getUserInfo(mContext).id, new AdsAvailabilityCallback() {
                                @Override
                                public void adsAvailable(boolean isAdAvailable) {
                                    if (isAdAvailable) {
                                        if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                        } else {
                                            Constants.SONG_NUMBER = 0;
                                        }
                                        int number = Constants.SONG_NUMBER;
                                        if (number >= Constants.SONGS_LIST.size()){
                                            number = 0;
                                        }
                                        Utility.setIntPreferences(mContext,"next_song_number",number);
                                    } else {
                                        Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                                        Utility.setIntPreferences(mContext, "count", 0);
                                        if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                        } else {
                                            Constants.SONG_NUMBER = 0;
                                        }
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                    }
                                }
                            });
                        }
                    };
                    runnable.run();
                }
            } else {
                setAlarm(mContext);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String skip = simpleDateFormat.format(new Date());
                Utility.setLongPreferences(mContext, "skip_time", skip);
                Utility.setLongPreferences(mContext, "timer_time", String.valueOf(Utility.getUserSetting(mContext).skipping_timer));
                String time = Utility.getLongPreferences(mContext, "skip_time");
                long a = Utility.timeDiffrence(time);
                long b = Long.parseLong(Utility.getLongPreferences(mContext, "timer_time")) * 60 * 1000;
                final long dif = b - a;
                Runnable runnable = new Runnable() {
                    public void run() {
                        Runtime.getRuntime().gc();
                        UserModel.getInstance().showAdsIfAvailable(mContext, Utility.getUserInfo(mContext).id, new AdsAvailabilityCallback() {
                            @Override
                            public void adsAvailable(boolean isAdAvailable) {
                                if (isAdAvailable) {
                                    if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                        Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                    } else {
                                        Constants.SONG_NUMBER = 0;
                                    }
                                    int number = Constants.SONG_NUMBER;
                                    if (number >= Constants.SONGS_LIST.size()){
                                        number = 0;
                                    }
                                    Utility.setIntPreferences(mContext,"next_song_number",number);
                                } else {
                                    Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                                    Utility.setIntPreferences(mContext, "count", 0);
                                    if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                        Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                    } else {
                                        Constants.SONG_NUMBER = 0;
                                    }
                                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                }
                            }
                        });
                    }
                };
                runnable.run();
            }
        }
    }

    //Previous song for free and paid user
    public void previousPlayMethod(final Context mContext) {
        UserModel.getInstance().getAppSetting(mContext);
        int count = Utility.getIntPreferences(mContext, "albumSkipCount");
        count = count + 1;
        if (count < 4) {
            Controls.previousControl(mContext);
        } else {
            if (count != 4) {
                String time = Utility.getLongPreferences(mContext, "skip_time");
                if (time == null || time.equalsIgnoreCase("")){
                    time = Utility.getCurrentTimeEvent();
                }
                long a = Utility.timeDiffrence(time);
                long b = Long.parseLong(Utility.getLongPreferences(mContext, "timer_time")) * 60 * 1000;
                final long dif = b - a;
                if (dif <= 0) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(mContext);
                    }
                    Controls.previousControl(mContext);
                    Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                } else {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            UserModel.getInstance().showAdsIfAvailable(mContext, Utility.getUserInfo(mContext).id, new AdsAvailabilityCallback() {
                                @Override
                                public void adsAvailable(boolean isAdAvailable) {
                                    if (isAdAvailable) {
                                        if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                        } else {
                                            Constants.SONG_NUMBER = 0;
                                        }
                                        int number = Constants.SONG_NUMBER;
                                        if (number >= Constants.SONGS_LIST.size()){
                                            number = 0;
                                        }
                                        Utility.setIntPreferences(mContext,"next_song_number",number);
                                    } else {
                                        Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                                        Utility.setIntPreferences(mContext, "count", 0);
                                        if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                        } else {
                                            Constants.SONG_NUMBER = 0;
                                        }
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                    }
                                }
                            });
                        }
                    };
                    runnable.run();
                }
            } else {
                setAlarm(mContext);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String skip = simpleDateFormat.format(new Date());
                Utility.setLongPreferences(mContext, "skip_time", skip);
                Utility.setLongPreferences(mContext, "timer_time", String.valueOf(Utility.getUserSetting(mContext).skipping_timer));
                String time = Utility.getLongPreferences(mContext, "skip_time");
                long a = Utility.timeDiffrence(time);
                long b = Long.parseLong(Utility.getLongPreferences(mContext, "timer_time")) * 60 * 1000;
                final long dif = b - a;
                Runnable runnable = new Runnable() {
                    public void run() {
                        UserModel.getInstance().showAdsIfAvailable(mContext, Utility.getUserInfo(mContext).id, new AdsAvailabilityCallback() {
                            @Override
                            public void adsAvailable(boolean isAdAvailable) {
                                if (isAdAvailable) {
                                    if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                        Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                    } else {
                                        Constants.SONG_NUMBER = 0;
                                    }
                                    int number = Constants.SONG_NUMBER;
                                    if (number >= Constants.SONGS_LIST.size()){
                                        number = 0;
                                    }
                                    Utility.setIntPreferences(mContext,"next_song_number",number);
                                } else {
                                    Utility.setIntPreferences(mContext, "albumSkipCount", 0);
                                    Utility.setIntPreferences(mContext, "count", 0);
                                    if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                        Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                    } else {
                                        Constants.SONG_NUMBER = 0;
                                    }
                                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                }
                            }
                        });
                    }
                };
                runnable.run();
            }
        }
    }


    //Show timer counter for free user after three skip
    public void freePaidUser(final Context context, ArrayList<Song> listPrefSong, int position, FreePaidUserCallBack freePaidUserCallBack) {
        UserModel.getInstance().getAppSetting(context);
        try {
            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                freePaidUserCallBack.freePaidUser(true);
                Utility.setIntPreferences(context, "albumSkipCount", 0);
            } else {
                if (listPrefSong.get(position).artist.isPremium) {
                    freePaidUserCallBack.freePaidUser(false);
                } else {
                    int count = Utility.getIntPreferences(context, "albumSkipCount");

                    /* --- AKM: Ad vary first time ---*/
                    /*if(count == 0 && !isAdPlayedOnCountOne){
                        isAdPlayedOnCountOne = true;
                        freePaidUserCallBack.freePaidUser(true);
                    }*/
                    /* --- AKM ---- */
                    count = count + 1;
                    if (count < 4) {
                        freePaidUserCallBack.freePaidUser(true);
                    } else {
                        if (count != 4) {
                            String time = Utility.getLongPreferences(context, "skip_time");
                            if (time == null || time.equalsIgnoreCase("")){
                                time = Utility.getCurrentTimeEvent();
                            }
                            long a = Utility.timeDiffrence(time);
                            long b = Long.parseLong(Utility.getLongPreferences(context, "timer_time")) * 60 * 1000;
                            final long dif = b - a;
                            if (dif <= 0) {
                                if (Constants.isPlay) {
                                    Controls.pauseControl(context);
                                }
                                freePaidUserCallBack.freePaidUser(true);
                                Utility.setIntPreferences(context, "albumSkipCount", 0);
                            } else {
                                Utility.setIntPreferences(context, "albumSkipCount", count);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("open_alert"));
                                Runnable runnable = new Runnable() {
                                    public void run() {
                                        Runtime.getRuntime().gc();
                                        UserModel.getInstance().showAdsIfAvailable(context, Utility.getUserInfo(context).id, new AdsAvailabilityCallback() {
                                            @Override
                                            public void adsAvailable(boolean isAdAvailable) {
                                                if (!isAdAvailable) {
                                                    Utility.setIntPreferences(context, "albumSkipCount", 0);
                                                    Utility.setIntPreferences(context, "count", 0);
                                                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                                }
                                            }
                                        });
                                    }
                                };
                                runnable.run();
                            }
                        } else {
                            setAlarm(context);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            String skip = simpleDateFormat.format(new Date());
                            Utility.setLongPreferences(context, "skip_time", skip);
                            Utility.setLongPreferences(context, "timer_time", String.valueOf(Utility.getUserSetting(context).skipping_timer));
                            String time = Utility.getLongPreferences(context, "skip_time");
                            long a = Utility.timeDiffrence(time);
                            long b = Long.parseLong(Utility.getLongPreferences(context, "timer_time")) * 60 * 1000;
                            final long dif = b - a;
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("open_alert"));
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    UserModel.getInstance().showAdsIfAvailable(context, Utility.getUserInfo(context).id, new AdsAvailabilityCallback() {
                                        @Override
                                        public void adsAvailable(boolean isAdAvailable) {
                                            if (!isAdAvailable) {
                                                Utility.setIntPreferences(context, "albumSkipCount", 0);
                                                Utility.setIntPreferences(context, "count", 0);
                                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                            }else {
                                                if (Constants.isSongPlay){
                                                    int number = Constants.SONG_NUMBER;
                                                    if (number >= Constants.SONGS_LIST.size()){
                                                        number = 0;
                                                    }
                                                    Utility.setIntPreferences(context,"next_song_number",number);
                                                }
                                            }
                                        }
                                    });
                                }
                            };
                            runnable.run();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAlarm(Context context) {
        Intent intent = new Intent(context, TimerBroadcast.class);
        intent.setAction("album_action");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Utility.getUserSetting(context).skipping_timer * 60 * 1000, pendingIntent);
    }

    //For generate random number for shuffle
    public void playShuffleSong(String songId, boolean isNext) {
        if (Constants.SONGS_LIST.size() != 1) {
            int currentSongId = 0;
            for (int i = 0; i < UserModel.getInstance().listOfShuffleSong.size(); i++) {
                if (UserModel.getInstance().listOfShuffleSong.get(i).id.equalsIgnoreCase(songId)) {
                    currentSongId = i;
                }
            }
            if (isNext) {
                currentSongId = currentSongId + 1;
            } else {
                currentSongId = currentSongId - 1;
            }
            if (currentSongId == -1) {
                currentSongId = UserModel.getInstance().listOfShuffleSong.size() - 1;
            }
            if (currentSongId == Constants.SONGS_LIST.size()){
                currentSongId = 0;
            }
            try {
                Constants.SONGS_LIST = UserModel.getInstance().listOfShuffleSong;
                Constants.SONG_NUMBER = currentSongId;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        } else {
            try {
                Constants.SONGS_LIST = UserModel.getInstance().listOfShuffleSong;
                Constants.SONG_NUMBER = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        }
    }

    //Ad logic for free User
    public void showAdsIfAvailable(final Context context, String user_id, final AdsAvailabilityCallback adsAvailabilityCallback) {
        Utility.setIntPreferences(context, "albumSkipCount", 0);
        Utility.setIntPreferences(context, "count", 0);
        if (Utility.isAdAlreadyFound) {
            try {
                adsAvailabilityCallback.adsAvailable(true);
                UserModel.getInstance().campaign = UserModel.getInstance().ad;
                Controls.pauseControl(context);
                NewCampiagnActivity.isFromCampaign = true;
                context.startActivity(new Intent(context, NewCampiagnActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
                //new AdsDialogFragment().show(((Activity) context).getFragmentManager(),"Ads");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Call<Campaign> call = Constants.service.getAdsAccordingUser(user_id);
            call.enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                    if (response.isSuccessful()) {
                        Campaign campaign = response.body();
                        if (campaign != null && campaign.campaign != null && campaign.campaign.ads != null) {
                            adsAvailabilityCallback.adsAvailable(true);
                            try {
                                UserModel.getInstance().campaign = campaign.campaign;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Controls.pauseControl(context);
                            NewCampiagnActivity.isFromCampaign = true;
                            context.startActivity(new Intent(context, NewCampiagnActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
                            //new AdsDialogFragment().show(((Activity) context).getFragmentManager(),"Ads");
                        } else {
                            adsAvailabilityCallback.adsAvailable(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Campaign> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }


    public void showAdsIfAvailableForFirstTime(final Context context, String user_id, final AdsAvailabilityCallback adsAvailabilityCallback) {
        if (Utility.isAdAlreadyFound) {
            try {
                adsAvailabilityCallback.adsAvailable(true);
                UserModel.getInstance().campaign = UserModel.getInstance().ad;
                Controls.pauseControl(context);
                NewCampiagnActivity.isFromCampaign = true;
                context.startActivity(new Intent(context, NewCampiagnActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
                //new AdsDialogFragment().show(((Activity) context).getFragmentManager(),"Ads");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Call<Campaign> call = Constants.service.getAdsAccordingUser(user_id);
            call.enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                    if (response.isSuccessful()) {
                        Campaign campaign = response.body();
                        if (campaign != null && campaign.campaign != null && campaign.campaign.ads != null) {
                            adsAvailabilityCallback.adsAvailable(true);
                            try {
                                UserModel.getInstance().campaign = campaign.campaign;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Controls.pauseControl(context);
                            NewCampiagnActivity.isFromCampaign = true;
                            context.startActivity(new Intent(context, NewCampiagnActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
                            //new AdsDialogFragment().show(((Activity) context).getFragmentManager(),"Ads");
                        } else {
                            adsAvailabilityCallback.adsAvailable(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Campaign> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }


    // Increase skip count if user skip the song within skipping interval
    public void skipSong(final Context context, String songId) {
        HashMap<String, Object> songHashMap = new HashMap<>();
        songHashMap.put("songid", songId);
        Call<ResponseBody> call = Constants.service.skipSong(songHashMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jObjError = new JSONObject(response.body().string());
                        if (jObjError.getBoolean("success")) {
                            UserModel.getInstance().getdata(context);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Get User MyMusic Song List
    public void getMyMusic(final Context context, final GetMyMusicListCallback callback) {
        final ArrayList<String> listPlaylist = new ArrayList<>();
        Call<UserModel> call = Constants.service.getUserInfo(Utility.getUserInfo(context).id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel playlistResponse = response.body();
                try {
                    if (playlistResponse.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            listPlaylist.addAll(playlistResponse.user.myMusic);
                            callback.addedToMusic(true, listPlaylist);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Add Song into My Music
    public void addToMusicWithCallback(final Context context, final String user_id, String song_id, final AddToMyMusicCallback callback) {
        Call<UserModel> call = Constants.service.addToPlaylist(user_id, song_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                if (response.isSuccessful()) {
                    try {
                        if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                if (userModel.success) {
                                    Utility.setUserInfo(context, userModel.user);
                                    UserModel.getInstance().getdata(context);
                                    callback.checkAdd(true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Utility.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    //Add Song, album, artist and playlist into Recent
    public void addInRecentWithType(final Context context, String user_id, String id, String type) {
        Call<UserModel> call = Constants.service.updateRecentWithType(user_id, id, type);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                try {
                    if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (userModel.success) {
                                Utility.setUserInfo(context, userModel.user);
                                UserModel.getInstance().getdata(context);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    // Get Station Status by Station ID
    public void getStationStatusByID(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = "";
        if (Constants.StationList != null) {
            if (Constants.StationList.size() <= Constants.SONG_NUMBER && Constants.StationList.size() > 0) {
                s = Constants.StationList.get(0).id;
            } else {
                if (Constants.StationList.size() > 0) {
                    s = Constants.StationList.get(Constants.SONG_NUMBER).id;
                } else {
                    return;
                }
            }
        }
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.getStationLikes(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                if (response.isSuccessful()) {
                    UserSongStatus model = response.body();
                    if (model.success) {
                        if (model.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            songStatus = model.status;
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Like a station.
    public void likeStationApi(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = Constants.StationList.get(Constants.SONG_NUMBER).id;
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.setStationLike(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                UserSongStatus model = response.body();
                if (model.success) {
                    try {
                        if (model.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(mContext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            songStatus = 1;
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                            Controls.sendMessage("Like");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Unlike a station
    public void unlikeStationApi(final Context mContext, final LikeAndUnlikeCallBack likeAndUnlikeCallBack) {
        String s = Constants.StationList.get(Constants.SONG_NUMBER).id;
        String user_id = Utility.getUserInfo(mContext).id;
        Call<UserSongStatus> modelCall = Constants.service.setStationUnlike(user_id, s);
        modelCall.enqueue(new Callback<UserSongStatus>() {
            @Override
            public void onResponse(Call<UserSongStatus> call, Response<UserSongStatus> response) {
                UserSongStatus model = response.body();
                if (model != null) {
                    if (!model.message.equalsIgnoreCase("Invalid device login.")) {
                        if (model.success) {
                            songStatus = 0;
                            Controls.sendMessage("Dislike");
                            likeAndUnlikeCallBack.statusLikeUnlike(songStatus);
                        }
                    } else {
                        Utility.openSessionOutDialog(mContext);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSongStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
