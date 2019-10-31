package com.shirali.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shirali.App;
import com.shirali.R;
import com.shirali.activity.NewCampiagnActivity;
import com.shirali.activity.PlayerActivity;
import com.shirali.controls.Controls;
import com.shirali.interfaces.AdsAvailabilityCallback;
import com.shirali.interfaces.LikeAndUnlikeCallBack;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;
import com.shirali.model.stations.Stations;
import com.shirali.model.user.UserModel;
import com.shirali.controls.RemoteControlPlayer;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.GetDataOnlineStreaming;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Sagar on 16/8/17.
 */
public class SongPlayService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener , MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener{

    public static final String NOTIFY_PREVIOUS = "com.shirali.previous";
    public static final String NOTIFY_DELETE = "com.shirali.delete";
    public static final String NOTIFY_PAUSE = "com.shirali.pause";
    public static final String NOTIFY_PLAY = "com.shirali.play";
    public static final String NOTIFY_NEXT = "com.shirali.next";
    public static final String NOTIFY_LiKE = "com.shirali.like";
    public static final String NOTIFY_DISLIKE = "com.shirali.dislike";
    public final int STOPPED = 0;
    public final int PAUSED = 1;
    public final int PLAYING = 2;
    private final IBinder musicBind = new MusicBinder();
    public int songstatus = -1;
    public PendingIntent contentIntent;
    public static final int NOTIFICATION_ID = 1111;
    AudioManager audioManager;
    int delay = 20000;
    private int stationStatus = -1;
    private int playerState = 0;
    private boolean currentVersionSupportBigNotification = false;
    private boolean currentVersionSupportLockScreenControls = false;
    private Timer timer;
    private MediaPlayer player;
    private ComponentName remoteComponentName;
    private RemoteControlClient remoteControlClient;
    private Context context;
    private App app;
    private boolean isPlayState;
    private Notification notification;
    private RemoteViews simpleContentView;
    private RemoteViews expandedView;
    private boolean isFirstTimeLoadImage = false;
    private boolean isFirstTimeUpdateMetadateImg = false;
    private NotificationManager notificationManager;
    private AudioManager.OnAudioFocusChangeListener audioFocus;
    private boolean isPlayerRunning = false;
    private String currentPlayingSongId = "";
    private int playerCurrentDuration = 0;
    private boolean isForOnlyOneTime = false;
    private int storeLastPlayTime = 0;
    private boolean isStation = false;
    String url;

    //Update seek bar for song and check ad availablity for radio
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (player != null) {
                    if (player.isPlaying()) {
                        if (Constants.isSongPlay) {
                            int progress = (player.getCurrentPosition() * 100) / player.getDuration();
                            Integer i[] = new Integer[3];
                            i[0] = player.getCurrentPosition();
                            i[1] = player.getDuration();
                            i[2] = progress;

                            if ((progress % 5) == 0 && progress > 0) {
                                if (progress != storeLastPlayTime) {
                                    isForOnlyOneTime = true;
                                }
                                if (isForOnlyOneTime) {
                                    isForOnlyOneTime = false;
                                    storeLastPlayTime = progress;
                                    app.checkLiveUserUpdate();
                                }
                            }
                            UserModel.getInstance().songPlayerDuration = i[2];
                            Constants.PROGRESSBAR_HANDLER.sendMessage(Constants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
                            try {
                                if (player.getCurrentPosition() >= Utility.getUserSetting(context).streamedTimeDelay * 1000) {
                                    if (isPlayState) {
                                        isPlayState = false;
                                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                            if (Constants.SONG_NUMBER < Constants.SONGS_LIST.size()) {
                                                UserModel.getInstance().songsStream(context, Utility.getUserInfo(context).id, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id, "paid");
                                            }
                                        } else {
                                            if (Constants.SONG_NUMBER < Constants.SONGS_LIST.size()) {
                                                UserModel.getInstance().songsStream(context, Utility.getUserInfo(context).id, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id, "free");
                                            }
                                        }
                                        int count = Utility.getIntPreferences(context, "count");
                                        Utility.setIntPreferences(context, "count", count + 1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            playerCurrentDuration = player.getCurrentPosition();
                        } else {
                            try {
                                if ((player.getCurrentPosition() / 1000) != 0) {
                                    if (UserModel.getInstance().currentPosition != (player.getCurrentPosition() / 1000)) {
                                        UserModel.getInstance().currentPosition = (player.getCurrentPosition() / 1000);
                                        if (0 == ((player.getCurrentPosition() / 1000) % 1800)) {
                                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                                Controls.playControl(context);
                                            } else {
                                                UserModel.getInstance().showAdsIfAvailable(context, Utility.getUserInfo(context).id, new AdsAvailabilityCallback() {
                                                    @Override
                                                    public void adsAvailable(boolean isAdAvailable) {
                                                        UserModel.getInstance().isAdShowForRadioFirstTime = false;
                                                        if (isAdAvailable) {
                                                            Controls.pauseControl(context);
                                                        } else {
                                                            Controls.playControl(context);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.fillInStackTrace();
                            }
                        }
                    } else {
                        if (Constants.resetSeekBar) {
                            Constants.resetSeekBar = false;
                            Integer i[] = new Integer[3];
                            i[2] = 0;
                            Constants.PROGRESSBAR_HANDLER.sendMessage(Constants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Handler metaHandler = new Handler(Looper.getMainLooper());
    Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                //Get online streaming data for radio
                new GetDataOnlineStreaming(context).execute(new URL(Constants.StationList.get(Constants.SONG_NUMBER).link));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            metaHandler.postDelayed(this, delay);
        }
    };
    private ArrayList<String> listOfPlayedSong;
    private CountDownTimer countTimer;
    BroadcastReceiver cancel_notification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (countTimer != null) {
                countTimer.cancel();
            }
            try {
                if (notification != null) {
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    startForeground(NOTIFICATION_ID, notification);
                    stopForeground(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopSelf();
        }
    };
    private String nameOfAlbum = "";
    BroadcastReceiver update_meta = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!nameOfAlbum.equalsIgnoreCase(UserModel.getInstance().title)) {
                nameOfAlbum = UserModel.getInstance().title;
                newNotification();
            }
        }
    };
    private int bufferPercentage;

    public void onCreate() {
        super.onCreate();
        context = this;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        RegisterRemoteClient();
        timer = new Timer();
        listOfPlayedSong = new ArrayList<>();
        app = (App) context.getApplicationContext();
        currentVersionSupportBigNotification = Utility.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = Utility.currentVersionSupportLockScreenControls();
        player = new MediaPlayer();
        initMusicPlayer();

        simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
        expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        LocalBroadcastManager.getInstance(context).unregisterReceiver(cancel_notification);
        LocalBroadcastManager.getInstance(context).registerReceiver(cancel_notification, new IntentFilter("cancel_notification"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(update_meta);
        LocalBroadcastManager.getInstance(context).registerReceiver(update_meta, new IntentFilter("update_metadata"));

        audioFocus = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                try {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        try {
                            //Controls.pauseControl(context);
                            if (UserModel.getInstance().isSocketOpen) {
                                if (app.isConnected) {
                                    app.disconnectSocket();
                                }
                            }
                            player.pause();

                            /* --- KIPL -> AKM : Notification Control Manage ---*/
                            Intent intent = new Intent("play_pause_control");
                            intent.putExtra("message", "play_pause_control");
                            intent.putExtra("action", "pause");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            /* --- KIPL -> AKM  ---*/

                            newNotification();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        try {
                            //Controls.pauseControl(context);
                            if (UserModel.getInstance().isSocketOpen) {
                                if (app.isConnected) {
                                    app.disconnectSocket();
                                }
                            }
                            player.pause();

                            /* --- KIPL -> AKM : Notification Control Manage ---*/
                            Intent intent = new Intent("play_pause_control");
                            intent.putExtra("message", "play_pause_control");
                            intent.putExtra("action", "pause");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            /* --- KIPL -> AKM ---*/

                            newNotification();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        if (Constants.isPlay) {

                            try {
                                //Controls.playControl(context);
                                if (UserModel.getInstance().isSocketOpen) {
                                    if (!app.isConnected) {
                                        app.connectSocket();
                                    }
                                }
                                player.start();

                                /* --- KIPL -> AKM : Notification Control Manage ---*/
                                Intent intent = new Intent("play_pause_control");
                                intent.putExtra("message", "play_pause_control");
                                intent.putExtra("action", "play");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                /* --- KIPL -> AKM ---*/

                                newNotification();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        try {
                            //Controls.pauseControl(context);
                            if (UserModel.getInstance().isSocketOpen) {
                                if (app.isConnected) {
                                    app.disconnectSocket();
                                }
                            }
                            try {
                                if (player != null) {
                                    player.pause();

                                    /* --- KIPL -> AKM : Notification Control Manage ---*/
                                    Intent intent = new Intent("play_pause_control");
                                    intent.putExtra("message", "play_pause_control");
                                    intent.putExtra("action", "pause");
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    /* --- KIPL -> AKM ---*/
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                            Constants.isPlay = false;
                            if (Constants.isSongPlay) {
                                if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                                    newNotification();
                                else cancelNotification();
                            } else {
                                if (Constants.StationList != null && Constants.StationList.size() > 0)
                                    newNotification();
                                else cancelNotification();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OwnThread thread = new OwnThread();
        thread.start();

        //new Async().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            metaHandler.removeCallbacks(run);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (countTimer != null) {
            countTimer.cancel();
            countTimer = null;
        }
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        try {
            player.stop();
            player.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        try {
            if (notification != null) {
                notification.flags = Notification.FLAG_ONGOING_EVENT;
                startForeground(NOTIFICATION_ID, notification);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Runtime.getRuntime().gc();
                        stopForeground(true);
                    }
                }, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //audioManager.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        try {
            metaHandler.removeCallbacks(run);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (countTimer != null) {
            countTimer.cancel();
            countTimer = null;
        }
        try {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(NOTIFICATION_ID, notification);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().gc();
                    stopForeground(true);
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
    private void RegisterRemoteClient() {
        remoteComponentName = new ComponentName(getApplicationContext(), new RemoteControlPlayer().ComponentName());
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        //player.setOnInfoListener(this);
    }

    /**
     * Notification
     * Custom Bignotification is available from API 16
     */
    @SuppressLint("NewApi")
    private void newNotification() {
        String CHANNEL_ID = "default";
        Runtime.getRuntime().gc();
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
        if (Constants.isPlay) {
            Intent intent = new Intent(context, PlayerActivity.class);
            if (Constants.adShow || Utility.getBooleaPreferences(context, "ad_playing")) {
                contentIntent = null;
            } else {
                if (Constants.isSongPlay) {
                    if (Constants.SONGS_LIST != null) {
                        if (Constants.SONGS_LIST.size() <= 0) {
                            contentIntent = null;
                        } else {
                            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }
                } else {
                    if (Constants.StationList != null) {
                        if (Constants.StationList.size() <= 0) {
                            contentIntent = null;
                        } else {
                            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }
                }
            }
            notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.noti_logo)
                    .setAutoCancel(false)
                    .setDefaults(0)
                    .setSound(null, 0)
                    .setPriority(Notification.DEFAULT_ALL)
                    .setOngoing(true)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(contentIntent).build();
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        } else {
            Intent intent = new Intent(context, PlayerActivity.class);
            if (Constants.adShow || Utility.getBooleaPreferences(context, "ad_playing")) {
                contentIntent = null;
            } else {
                if (Constants.isSongPlay) {
                    if (Constants.SONGS_LIST != null) {
                        if (Constants.SONGS_LIST.size() <= 0) {
                            contentIntent = null;
                        } else {
                            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }
                } else {
                    if (Constants.StationList != null) {
                        if (Constants.StationList.size() <= 0) {
                            contentIntent = null;
                        } else {
                            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }
                }
            }
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.noti_logo)
                    .setWhen(0)
                    .setDefaults(0)
                    .setSound(null, 0)
                    .setPriority(Notification.DEFAULT_ALL)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(createOnDismissedIntent(context, NOTIFICATION_ID))
                    .build();
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        setListeners(simpleContentView);
        if (currentVersionSupportBigNotification) {
            setListeners(expandedView);
        }

        notification.contentView = simpleContentView;
        if (currentVersionSupportBigNotification) {
            notification.bigContentView = expandedView;
        }

        try {
            if (Constants.isPlay) {
                notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                    notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
                }
            } else {
                notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
                notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
                    notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (isFirstTimeLoadImage) {
                isFirstTimeLoadImage = false;
                String albumId = "";
                if (Constants.isSongPlay) {
                    if (Constants.SONGS_LIST.size() > 0) {
                        albumId = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artwork;
                    }
                } else {
                    albumId = Constants.StationList.get(Constants.SONG_NUMBER).imageurl;
                }
                Bitmap albumArt = getBitmap(albumId);
                if (albumArt == null || albumId.equalsIgnoreCase("")) {
                    notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.ico_shirali_placeholder);
                    if (currentVersionSupportBigNotification) {
                        notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.ico_shirali_placeholder);
                    }
                } else {
                    notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                    if (currentVersionSupportBigNotification) {
                        notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String songName = "";
        String albumName = "";
        if (Constants.isSongPlay) {
            try {
                if (Constants.SONGS_LIST.size() > 0) {
                    if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size()) {
                        songName = Constants.SONGS_LIST.get(0).title;
                        if (Constants.SONGS_LIST.get(0).albums.size() > 0) {
                            albumName = Constants.SONGS_LIST.get(0).albums.get(0).title;
                        }
                    } else {
                        songName = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).title;
                        if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.size() > 0) {
                            albumName = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).title;
                        }
                    }
                }
                if (albumName == null) {
                    albumName = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                songName = Constants.StationList.get(Constants.SONG_NUMBER).title;
                albumName = nameOfAlbum;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            notification.contentView.setTextViewText(R.id.textSongName, songName);
            notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (currentVersionSupportBigNotification) {
            try {
                notification.bigContentView.setTextViewText(R.id.textSongName, songName);
                notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
                if (Constants.isSongPlay) {
                    notification.bigContentView.setImageViewResource(R.id.imgLike, songstatus == 1 ? R.drawable.thumb_up : R.drawable.thumb_up_white);
                    notification.bigContentView.setImageViewResource(R.id.imgDislike, songstatus == 0 ? R.drawable.thumb_down : R.drawable.thumb_down_white);
                } else {
                    try {
                        notification.bigContentView.setImageViewResource(R.id.imgLike, stationStatus == 1 ? R.drawable.thumb_up : R.drawable.thumb_up_white);
                        notification.bigContentView.setImageViewResource(R.id.imgDislike, stationStatus == 0 ? R.drawable.thumb_down : R.drawable.thumb_down_white);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("no sound");
            channel.setSound(null,null);
            channel.enableLights(false);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);*/

            NotificationChannel channel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        if (Constants.isPlay) {
            try {
                //notificationManager.notify(NOTIFICATION_ID, notification);
                startForeground(NOTIFICATION_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                //notificationManager.notify(NOTIFICATION_ID, notification);
                startForeground(NOTIFICATION_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Cancel notification player
    public void cancelNotification() {
        try {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(NOTIFICATION_ID, notification);
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, SongPlayService.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }

    /**
     * Notification click listeners
     */
    public void setListeners(RemoteViews view) {
        /* --- KIPL -> AKM: Handling control issue ---*/
        Intent previous = new Intent(this, RemoteControlPlayer.class);
        Intent delete = new Intent(this, RemoteControlPlayer.class);
        Intent pause = new Intent(this, RemoteControlPlayer.class);
        Intent next = new Intent(this, RemoteControlPlayer.class);
        Intent play = new Intent(this, RemoteControlPlayer.class);
        Intent like = new Intent(this, RemoteControlPlayer.class);
        Intent dislike = new Intent(this, RemoteControlPlayer.class);

        previous.setAction(NOTIFY_PREVIOUS);
        delete.setAction(NOTIFY_DELETE);
        pause.setAction(NOTIFY_PAUSE);
        next.setAction(NOTIFY_NEXT);
        play.setAction(NOTIFY_PLAY);
        like.setAction(NOTIFY_LiKE);
        dislike.setAction(NOTIFY_DISLIKE);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

        PendingIntent pLike = PendingIntent.getBroadcast(getApplicationContext(), 0, like, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imgLike, pLike);

        PendingIntent pDislike = PendingIntent.getBroadcast(getApplicationContext(), 0, dislike, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imgDislike, pDislike);

    }

    public Bitmap getBitmap(String data) throws IOException {
        Bitmap output = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        Bitmap bitmap = null;
        if (SDK_INT > 8) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                URL url = new URL(data);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                m.setScale((float) 120 / bitmap.getWidth(), (float) 120 / bitmap.getHeight());
                canvas.drawBitmap(bitmap, m, new Paint());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e1) {
                output = BitmapFactory.decodeResource(getResources(), R.drawable.ico_shirali_placeholder);
                e1.printStackTrace();
            }
        }
        return output;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        /*mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.e("show_loader>>>", "showing_loader" + what);
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("show_loader>>>", "show_loader");
                                Intent intent = new Intent("show_loader");
                                intent.putExtra("message", "show_loader");
                                intent.putExtra("seek_percentage", 1);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        });
                        t.start();

                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Thread t1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("show_loader_end>>>", "show_loader_end");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_playerLayout"));
                            }
                        });
                        t1.start();

                        break;
                }
                return true;
            }
        });*/
        if (Constants.isSongPlay) {
            player.start();
            isPlayerRunning = true;
            isPlayState = true;
            isForOnlyOneTime = true;
            Constants.isPlay = true;
            if (timer != null) {
                timer.scheduleAtFixedRate(new MainTask(), 0, 100);
            }
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().gc();
                    if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                        newNotification();
                    else cancelNotification();
                    if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0) {
                        if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size()) {
                            UpdateMetadata(Constants.SONGS_LIST.get(0));
                            Constants.SONG_NUMBER = 0;
                        } else {
                            if (Constants.SONG_NUMBER < Constants.SONGS_LIST.size()) {
                                UpdateMetadata(Constants.SONGS_LIST.get(Constants.SONG_NUMBER));
                            }
                        }
                    }
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                }
            });
            t.start();
            if (Constants.SONGS_LIST.size() > 0 && Constants.SONG_NUMBER < Constants.SONGS_LIST.size()) {
                currentPlayingSongId = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
            }
            listOfPlayedSong.add(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_playerLayout"));
        } else {

            Constants.isPlay = true;
            UserModel.getInstance().isStationPlaying = false;
            /*if (timer != null) {
                timer.scheduleAtFixedRate(new MainTask(), 0, 100);
            }*/
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().gc();
                    if (Constants.StationList != null && Constants.StationList.size() > 0) {
                        newNotification();
                        UpdateRadioMetadata(Constants.StationList.get(Constants.SONG_NUMBER));
                    } else cancelNotification();
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                    player.start();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_playerLayout"));
                }
            });
            t.start();

            //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_playerLayout"));
            //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // Progreess Dialog : : G
        Log.e("what >>>", "" + what);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("show_loader>>>", "show_loader");
                        Intent intent = new Intent("show_loader");
                        intent.putExtra("message", "show_loader");
                        intent.putExtra("seek_percentage", 1);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
                t.start();

                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("show_loader_end>>>", "show_loader_end");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_playerLayout"));
                    }
                });
                t1.start();

                break;
        }

        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.e("percent>>>", "" + percent);
        bufferPercentage = percent;
        /*Intent intent = new Intent("show_loader_new");
        intent.putExtra("message", "show_loader");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isPlayerRunning) {
            isPlayerRunning = false;
            Constants.isSongComplete = true;
            Constants.isFirstLoad = true;
            Constants.isPrevious = false;
            Constants.isPageSelectedFromNextOfPrevious = false;
            final int count = Utility.getIntPreferences(context, "count");

            if (UserModel.getInstance().isSingleSongPlay) {
                if (Constants.repeat) {
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                } else {
                    UserModel.getInstance().isSongComplete = true;
                    Constants.isPlay = false;
                    newNotification();
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                    UserModel.getInstance().songPlayerDuration = 0;
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("stop_player"));
                }
            } else {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (Constants.repeat) {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    } else if (Constants.shuffel) {
                        playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                    } else {
                        playRegularSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                    }
                } else {
                    if (count >= 3) {
                        if (Constants.SONG_NUMBER == (Constants.SONGS_LIST.size() - 1)) {
                            if (Constants.shuffel) {
                                playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                            } else {
                                playRegularSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                            }
                        } else {
                            UserModel.getInstance().showAdsIfAvailable(context, Utility.getUserInfo(context).id, new AdsAvailabilityCallback() {
                                @Override
                                public void adsAvailable(boolean isAdAvailable) {
                                    if (isAdAvailable) {
                                        Constants.adShow = true;
                                        Constants.isChangeSong = true;
                                        int number = Constants.SONG_NUMBER + 1;
                                        if (number >= Constants.SONGS_LIST.size()) {
                                            number = 0;
                                        }
                                        Utility.setIntPreferences(context, "next_song_number", number);
                                        player.pause();
                                        Controls.pauseControl(context);
                                    } else {
                                        if (Constants.shuffel) {
                                            playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                                        } else {
                                            playRegularSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        if (Constants.shuffel) {
                            playShuffleSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                        } else {
                            playRegularSong(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(what == 1){
            if (Constants.isPlay) {
                /* --- KIPL -> AKM: Controls.pauseControl having issue to call from Service---*/
                //Controls.pauseControl(context);
                pausePlayer();
                //Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }else if(UserModel.getInstance().isStationPlaying){
                /* --- KIPL -> AKM: Controls.pauseControl having issue to call from Service---*/
                //Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                //Controls.pauseControl(context);
                pausePlayer();
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void UpdateMetadata(Song data) {
        if (remoteControlClient == null)
            return;
        final RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        Bitmap imgUrl = null;
        try {
            if (isFirstTimeUpdateMetadateImg) {
                isFirstTimeUpdateMetadateImg = false;
                imgUrl = getBitmap(data.artwork);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (data.albums.size() > 0) {
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.albums.get(0).title);
            }
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.artist.name);
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.title);
            if (data.artwork.equalsIgnoreCase("") || imgUrl == null) {
                Bitmap tempBMP = BitmapFactory.decodeResource(getResources(), R.drawable.ico_shirali_placeholder);
                metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, tempBMP);
            } else {
                metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, imgUrl);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        metadataEditor.apply();
        audioManager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @SuppressLint("NewApi")
    private void UpdateRadioMetadata(Stations data) {
        if (remoteControlClient == null)
            return;
        final RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        Bitmap imgUrl = null;
        try {
            imgUrl = getBitmap(data.imageurl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.title);
            if (data.imageurl.equalsIgnoreCase("") || imgUrl == null) {
                Bitmap tempBMP = BitmapFactory.decodeResource(getResources(), R.drawable.ico_shirali_placeholder);
                metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, tempBMP);
            } else {
                metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, imgUrl);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        metadataEditor.apply();
        audioManager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    //Play Song
    private void playSong() {
        Constants.adShow = false;
        Constants.isSongComplete = false;
        Song playSong;
        if (Constants.SONGS_LIST.size() <= Constants.SONG_NUMBER && Constants.SONGS_LIST.size() > 0) {
            playSong = Constants.SONGS_LIST.get(0);
            UpdateMetadata(Constants.SONGS_LIST.get(0));
            Constants.SONG_NUMBER = 0;
        } else {
            if (Constants.SONGS_LIST.size() > 0) {
                playSong = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
            } else {
                return;
            }
        }
        try {
            // remove ! because it is not reset to player and not to change song
            try {
                player.stop();
                player.reset();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bufferPercentage = 0;
            }
            url = Constants.songUrl + "?id=" + playSong.id + "&secret=" + Constants.song_cloud_token;
            try {
                player.setDataSource(url);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            try {
                player.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
            UserModel.getInstance().currentPlaySong = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
            playerState = 2;
            UserModel.getInstance().getSongStatusByID(context, new LikeAndUnlikeCallBack() {
                @Override
                public void statusLikeUnlike(int i) {
                    songstatus = i;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Runtime.getRuntime().gc();
                            isFirstTimeLoadImage = true;
                            isFirstTimeUpdateMetadateImg = true;
                            if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                                newNotification();
                            else cancelNotification();
                        }
                    });
                    t.start();
                }
            });
            try {
                UserModel.getInstance().addInRecentWithType(context, Utility.getUserInfo(context).id, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id, "song");
            } catch (Exception e) {
                e.printStackTrace();
            }
            UserModel.getInstance().songPlayerDuration = 0;
            Intent intent = new Intent("show_loader");
            intent.putExtra("message", "show_loader");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Utility.setBooleanPreferences(context, "tempForSong", true);
            Utility.setIntPreferences(context, "tempSongNumber", Constants.SONG_NUMBER);
            Utility.setStringPreferences(context, "tempSongList", new Gson().toJson(Constants.SONGS_LIST));
            metaHandler.removeCallbacks(run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Play station
    private void playStation() {
        try {
            try {
                player.stop();
                player.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = Constants.StationList.get(Constants.SONG_NUMBER).link;
            try {
                player.setDataSource(url);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepareAsync();
                //player.start();
            } catch (Exception e) {
                e.printStackTrace();
                //AKM: Need to manage all Constant/preference/usermodel, if any failure happen
                if (UserModel.getInstance().isStationPlaying) {
                    UserModel.getInstance().isStationPlaying = false;
                    Controls.pauseControl(context);
                    Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    //binding.imgPlay.setImageResource(R.drawable.play);
                }
            }
            isFirstTimeLoadImage = true;
            UpdateRadioMetadata(Constants.StationList.get(Constants.SONG_NUMBER));
            Intent intent = new Intent("show_loader");
            intent.putExtra("message", "show_loader");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            if (Constants.StationList != null && Constants.StationList.size() > 0)
                newNotification();
            else cancelNotification();
            Utility.setBooleanPreferences(context, "tempForSong", false);
            Utility.setIntPreferences(context, "tempSongNumber", Constants.SONG_NUMBER);
            Utility.setStringPreferences(context, "tempStationList", new Gson().toJson(Constants.StationList));
            metaHandler.removeCallbacks(run);
            metaHandler.postDelayed(run, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlay() {
        switch (playerState) {
            case STOPPED:
                try {
                    String plan = Utility.getUserInfo(context).subscribePlan.plantype;
                    if (Constants.isSongPlay) {
                        Constants.isSongPlay = true;
                        if (plan.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    playSong();
                                }
                            });
                            t.start();
                        } else {
                            if (Constants.SONGS_LIST.size() <= Constants.SONG_NUMBER) {
                                return;
                            } else {
                                Album album = new Album();
                                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums != null) {
                                    if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.size() > 0) {
                                        album = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0);
                                    }
                                }
                                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.isPremium || Constants.SONGS_LIST.get(Constants.SONG_NUMBER).isPremium || album.isPremium) {
                                    if (Constants.isPrevious) {
                                        if (Constants.SONG_NUMBER > 0) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER - 1;
                                            togglePlay();
                                        } else {
                                            Constants.SONG_NUMBER = Constants.SONGS_LIST.size() - 1;
                                            togglePlay();
                                        }
                                    } else {
                                        if (Constants.SONG_NUMBER < (Constants.SONGS_LIST.size() - 1)) {
                                            Constants.SONG_NUMBER = Constants.SONG_NUMBER + 1;
                                            togglePlay();

                                            //AKM: open subscription pop up (In case of premium song in list)
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("premiumDialog"));

                                        } else {
                                            if (Constants.repeat) {
                                                Constants.SONG_NUMBER = 0;
                                                togglePlay();
                                            } else {
                                                try {
                                                    player.reset();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                UserModel.getInstance().isSongComplete = true;
                                                Constants.isPlay = false;
                                                newNotification();
                                                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                                                UserModel.getInstance().songPlayerDuration = 0;
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("stop_player"));
                                            }
                                        }
                                    }
                                } else {
                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Runtime.getRuntime().gc();
                                            playSong();
                                        }
                                    });
                                    t.start();
                                }
                            }
                        }
                    } else {
                        UserModel.getInstance().isStationPlaying = true;
                        Intent intent = new Intent("show_loader");
                        intent.putExtra("message", "show_loader");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                playStation();
                            }
                        });
                        t.start();
                        Constants.isSongPlay = false;
                        Constants.isPlay = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PAUSED:
                try {
                    if (UserModel.getInstance().isSocketOpen) {
                        if (!app.isConnected) {
                            app.connectSocket();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                player.start();
                playerState = PLAYING;
                break;
            case PLAYING:
                try {
                    if (UserModel.getInstance().isSocketOpen) {
                        if (app.isConnected) {
                            app.disconnectSocket();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                player.pause();
                playerState = PAUSED;
                break;
        }
    }

    //Play shuffle song
    public void playShuffleSong(String songId) {
        int currentSongId = 0;
        for (int i = 0; i < UserModel.getInstance().listOfShuffleSong.size(); i++) {
            if (UserModel.getInstance().listOfShuffleSong.get(i).id.equalsIgnoreCase(songId)) {
                currentSongId = i;
            }
        }
        currentSongId = currentSongId + 1;
        if (currentSongId >= UserModel.getInstance().listOfShuffleSong.size()) {
            if (Constants.repeat) {
                currentSongId = 0;
            } else {
                try {
                    player.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UserModel.getInstance().isSongComplete = true;
                Constants.isPlay = false;
                newNotification();
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                UserModel.getInstance().songPlayerDuration = 0;
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("stop_player"));
                return;
            }
        }
        try {
            Constants.SONGS_LIST = UserModel.getInstance().listOfShuffleSong;
            Constants.SONG_NUMBER = currentSongId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerState = STOPPED;
        togglePlay();
    }

    //Play regular song
    public void playRegularSong(String songId) {
        int currentSongId = 0;
        for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
            if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(songId)) {
                currentSongId = i;
            }
        }
        currentSongId = currentSongId + 1;
        if (currentSongId >= UserModel.getInstance().listOfActualSong.size()) {
            if (Constants.repeat) {
                currentSongId = 0;
            } else {
                try {
                    player.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UserModel.getInstance().isSongComplete = true;
                Constants.isPlay = false;
                newNotification();
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                UserModel.getInstance().songPlayerDuration = 0;
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("stop_player"));
                return;
            }
        }
        try {
            Constants.SONGS_LIST = UserModel.getInstance().listOfActualSong;
            Constants.SONG_NUMBER = currentSongId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerState = STOPPED;
        togglePlay();
    }

    //Dismiss notification player if pause media player for more than 20 mins
    public synchronized void setIntervalForCancelNotification() {
        countTimer = new CountDownTimer(1200000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                try {
                    if (Constants.isPlay) {
                        if (countTimer != null) {
                            countTimer.cancel();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                cancelNotification();
            }
        }.start();
    }

    private class MusicBinder extends Binder {
        public SongPlayService getService() {
            return SongPlayService.this;
        }
    }

    private class MainTask extends TimerTask {

        public void run() {
            Runtime.getRuntime().gc();
            handler.sendEmptyMessage(0);
        }
    }

    //Perform all operation like play pause stop etc.

    private class OwnThread extends Thread {
        @Override
        public void run() {
            Runtime.getRuntime().gc();
            Constants.SONG_CHANGE_HANDLER = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    Constants.isHomeScreenPlayerVisible = true;

                    if (Constants.isSongPlay) {
                        if (!UserModel.getInstance().isSingleSongPlay) {
                            /*if(Constants.SONGS_LIST.size() == UserModel.getInstance().listOfActualSong.size())
                                if(url.contains(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id))
                                    return true;*/
                            if (Utility.getBooleaPreferences(context, "suffle")) {
                                if(Constants.isListHitFirstTime = true){
                                    Constants.isListHitFirstTime = false;
                                    Constants.SONGS_LIST = UserModel.getInstance().listOfActualSong;
                                }else {
                                    Constants.SONGS_LIST = UserModel.getInstance().listOfShuffleSong;
                                }
                            } else {
                                Constants.SONGS_LIST = UserModel.getInstance().listOfActualSong;
                            }
                        }
                    }
                    listOfPlayedSong.clear();
                    playerState = STOPPED;

                    togglePlay();
                    Constants.resetSeekBar = true;
                    try {
                        if (UserModel.getInstance().isSocketOpen) {
                            if (!app.isConnected) {
                                app.connectSocket();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Constants.isSongPlay) {
                        int duration = playerCurrentDuration;
                        if (duration <= 60000) {
                            UserModel.getInstance().skipSong(context, currentPlayingSongId);
                        }
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Free") || !UserModel.getInstance().isForRenew || !UserModel.getInstance().isForTrial) {
                            if (duration >= Utility.getUserSetting(context).timeForCount * 1000) {
                                int count = Utility.getIntPreferences(context, "albumSkipCount");
                                count = count + 1;
                                Utility.setIntPreferences(context, "albumSkipCount", count);
                            }
                        }
                    }
                    return true;
                }
            });
            Constants.PLAY_PAUSE_HANDLER = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String) msg.obj;
                    if (player == null) {
                        return true;
                    }


                    /* --- AKM: Ad vary first time ---*/
                    /*int count = Utility.getIntPreferences(context, "albumSkipCount");
                    if(count == 0 && !UserModel.getInstance().isAdPlayedOnCountOne){
                        UserModel.getInstance().isAdPlayedOnCountOne = true;

                        UserModel.getInstance().campaign = UserModel.getInstance().ad;
                        Controls.pauseControl(context);
                        NewCampiagnActivity.isFromCampaign = true;
                        context.startActivity(new Intent(context, NewCampiagnActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
                        return true;
                    }*/
                    /* --- AKM ---- */


                    if (message.equalsIgnoreCase("Play")) {
                        if (!UserModel.getInstance().isSongComplete) {
                            audioManager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                            Constants.isPlay = true;
                            if (currentVersionSupportLockScreenControls) {
                                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                            }
                            try {
                                if (UserModel.getInstance().isSocketOpen) {
                                    if (!app.isConnected) {
                                        app.connectSocket();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            playerState = PLAYING;
                            try {
                                player.start();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent("play_pause_control");
                            intent.putExtra("message", "play_pause_control");
                            intent.putExtra("action", "play");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    if (Constants.isSongPlay) {
                                        if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    } else {
                                        if (Constants.StationList != null && Constants.StationList.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    }
                                }
                            });
                            t.start();
                        } else {
                            UserModel.getInstance().isSongComplete = false;
                            if (UserModel.getInstance().isSingleSongPlay) {
                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                            } else {
                                Constants.SONG_NUMBER = 0;
                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                            }
                        }
                    } else if (message.equalsIgnoreCase("Pause")) {
                        pausePlayer();
                    } else if (message.equalsIgnoreCase("Stop")) {
                        setIntervalForCancelNotification();
                        try {
                            if (UserModel.getInstance().isSocketOpen) {
                                if (app.isConnected) {
                                    app.disconnectSocket();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (player != null) {
                            Constants.isPlay = false;
                            Constants.song = "";
                            try {
                                player.stop();
                                player.reset();
                                player.release();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        playerState = STOPPED;
                    } else if (message.equalsIgnoreCase("Seek")) {
                        try {
                            int current = (player.getDuration() * Constants.seekTo) / 100;
                            player.seekTo(current);
                            //AKM
                            /*if (Constants.seekTo > bufferPercentage) {
                                Intent intent = new Intent("show_loader");
                                intent.putExtra("message", "show_loader");
                                intent.putExtra("seek_percentage", 1);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (message.equalsIgnoreCase("Like")) {
                        try {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    if (Constants.isSongPlay) {
                                        songstatus = 1;
                                        if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    } else {
                                        stationStatus = 1;
                                        if (Constants.StationList != null && Constants.StationList.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    }
                                }
                            });
                            t.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (message.equalsIgnoreCase("Dislike")) {
                        try {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    if (Constants.isSongPlay) {
                                        songstatus = 0;
                                        if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    } else {
                                        stationStatus = 0;
                                        if (Constants.StationList != null && Constants.StationList.size() > 0)
                                            newNotification();
                                        else cancelNotification();
                                    }
                                }
                            });
                            t.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (message.equalsIgnoreCase("Dismiss")) {
                        cancelNotification();
                        Constants.isPlay = false;
                        try {
                            if (UserModel.getInstance().isSocketOpen) {
                                if (app.isConnected) {
                                    app.disconnectSocket();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        playerState = PAUSED;
                        try {
                            player.pause();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent("play_pause_control");
                        intent.putExtra("message", "play_pause_control");
                        intent.putExtra("action", "pause");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                if (currentVersionSupportLockScreenControls) {
                                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                                }
                            }
                        });
                        t.start();
                    }
                    return false;
                }
            });
        }
    }

    private void pausePlayer() {
        setIntervalForCancelNotification();
        Constants.isPlay = false;
        try {
            if (UserModel.getInstance().isSocketOpen) {
                if (app.isConnected) {
                    app.disconnectSocket();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerState = PAUSED;
        try {
            player.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("play_pause_control");
        intent.putExtra("message", "play_pause_control");
        intent.putExtra("action", "pause");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                if (Constants.isSongPlay) {
                    if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0)
                        newNotification();
                    else cancelNotification();
                } else {
                    if (Constants.StationList != null && Constants.StationList.size() > 0)
                        newNotification();
                    else cancelNotification();
                }

                if (currentVersionSupportLockScreenControls) {
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                }
            }
        });
        t.start();
    }

    private class Async extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }



}