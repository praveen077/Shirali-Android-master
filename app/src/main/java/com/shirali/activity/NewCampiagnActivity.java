package com.shirali.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.App;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.model.campaign.Campaign_;
import com.shirali.model.user.UserModel;
import com.shirali.receiver.SongPlayInBackgroundBroadcast;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.io.IOException;

public class NewCampiagnActivity extends BaseActivity implements MediaPlayer.OnPreparedListener {

    public static boolean isFromCampaign = false;
    public boolean isAdPlayerStart = false;
    private SurfaceView videoView;
    private ImageView image;
    private Context context;
    private LinearLayout frame;
    private TextView skipText;
    private MediaPlayer mediaplayer;
    private Campaign_ ads;
    private int duration;
    private SurfaceHolder surfaceHolder;
    private boolean isMediaPlayerPrepared = true;
    private boolean isStartNow = false;
    private boolean isAlreadyPlayed = false;
    private boolean playerStart = false;
    private boolean showAd = false;
    private CountDownTimer counter;
    private int playerDuration;
    private SongPlayInBackgroundBroadcast myReceiver;
    private CountDownTimer timer;
    private boolean isAdPrepareToPlay = false;
    private boolean isUserGoesback = false;
    private RelativeLayout backgroundImage;
    private LinearLayout subscriptionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_campaign);
        context = this;
        if (!Utility.isAdAlreadyFound) {
            mediaplayer = new MediaPlayer();
            mediaplayer.setOnPreparedListener(this);
        } else {
            mediaplayer = null;
            mediaplayer = Utility.mediaplayer;
        }
        Constants.adShow = true;
        Utility.setBooleanPreferences(context,"ad_playing",true);
        ads = UserModel.getInstance().campaign;
        videoView = (SurfaceView) findViewById(R.id.vdAds);
        image = (ImageView) findViewById(R.id.imgAd);
        frame = (LinearLayout) findViewById(R.id.lytAds);
        skipText = (TextView) findViewById(R.id.lblCount);
        backgroundImage = (RelativeLayout) findViewById(R.id.lytAdvertisement);
        subscriptionBtn = (LinearLayout) findViewById(R.id.btnForSubscription);

        Drawable drawable = new BitmapDrawable(Utility.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ad_background)));
        backgroundImage.setBackgroundDrawable(drawable);

        if (App.isBackground) {
            moveTaskToBack(true);
        }

        timer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (isAdPrepareToPlay) {
                    isAdPrepareToPlay = false;
                    if (timer != null) {
                        timer.cancel();
                    }
                }
            }

            public void onFinish() {
                if (mediaplayer != null) {
                    if (!mediaplayer.isPlaying()) {
                        try {
                            mediaplayer.setOnPreparedListener(null);
                            mediaplayer.stop();
                            mediaplayer.reset();
                            mediaplayer.release();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        Utility.getAdForFreeUser(NewCampiagnActivity.this, Utility.getUserInfo(NewCampiagnActivity.this).id);
                        finish();
                        if (!Constants.isSongPlay) {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("play_station"));
                        }
                    }
                }
            }

        }.start();

        subscriptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerStart) {
                    Constants.openSubsWindowFromAds = true;
                    try {
                        playerDuration = mediaplayer.getCurrentPosition();
                        mediaplayer.pause();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    counter.cancel();
                    isMediaPlayerPrepared = false;
                    Utility.clickAds(context, ads.id);
                    showAd = true;
                    startActivity(new Intent(context, YourSubscriptionActivity.class));
                }
            }
        });

        //It's play video advertisement
        if (ads.ads.adFormat != null) {
            if (ads.ads.adFormat.equalsIgnoreCase("Video")) {
                isStartNow = true;
                isAdPlayerStart = true;
                isFromCampaign = true;
                isAlreadyPlayed = true;
                Controls.pauseControl(context);

                surfaceHolder = videoView.getHolder();
                surfaceHolder.setFixedSize(176, 144);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                if (Utility.isAdAlreadyFound) {
                    isAdPrepareToPlay = true;
                    playerStart = true;
                    surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder surfaceHolder) {
                            try {
                                Controls.pauseControl(context);
                                Utility.mediaplayer.setDisplay(surfaceHolder);
                                Utility.mediaplayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                            isAlreadyPlayed = false;
                            try {
                                if (Utility.mediaplayer != null) {
                                    Utility.mediaplayer.setDisplay(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Utility.mediaplayer.start();
                    duration = Utility.mediaplayer.getDuration();
                    displaySkip(Utility.mediaplayer.getDuration());
                } else {
                    if (isAlreadyPlayed) {
                        isAlreadyPlayed = false;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mediaplayer.setDataSource(ads.ads.adFileUrl);
                                    mediaplayer.prepare();
                                    isMediaPlayerPrepared = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                    surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder surfaceHolder) {
                            try {
                                if (mediaplayer != null) {
                                    mediaplayer.setDisplay(surfaceHolder);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                            isAlreadyPlayed = false;
                            try {
                                if (mediaplayer != null) {
                                    mediaplayer.setDisplay(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Utility.isAdAlreadyFound) {
                            if (playerStart) {
                                try {
                                    playerDuration = Utility.mediaplayer.getCurrentPosition();
                                    Utility.mediaplayer.pause();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                counter.cancel();
                                isMediaPlayerPrepared = false;
                                Utility.clickAds(context, ads.id);
                                showAd = true;
                                startActivity(new Intent(context, WebViewActivity.class).putExtra("url", ads.ads.redirectUrl));
                            }
                        } else {
                            if (playerStart) {
                                try {
                                    playerDuration = mediaplayer.getCurrentPosition();
                                    mediaplayer.pause();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                counter.cancel();
                                isMediaPlayerPrepared = false;
                                Utility.clickAds(context, ads.id);
                                showAd = true;
                                startActivity(new Intent(context, WebViewActivity.class).putExtra("url", ads.ads.redirectUrl));
                            }
                        }
                    }
                });
            } else { //It's play audio advertisement
                isStartNow = true;
                isAdPlayerStart = true;
                isFromCampaign = true;
                Controls.pauseControl(context);
                videoView.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                try {
                    Glide.with(context).load(ads.ads.adArtworkUrl).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Utility.isAdAlreadyFound) {
                    playerStart = true;
                    isAdPrepareToPlay = true;
                    try {
                        Controls.pauseControl(context);
                        Utility.mediaplayer.start();
                        duration = Utility.mediaplayer.getDuration();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    displaySkip(duration);
                } else {
                    try {
                        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaplayer.setDataSource(ads.ads.adFileUrl);
                        try {
                            mediaplayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isMediaPlayerPrepared = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Utility.isAdAlreadyFound) {
                            playerDuration = Utility.mediaplayer.getCurrentPosition();
                            if (playerStart) {
                                try {
                                    Utility.mediaplayer.pause();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                counter.cancel();
                                isMediaPlayerPrepared = false;
                                Utility.clickAds(context, ads.id);
                                showAd = true;
                                startActivity(new Intent(context, WebViewActivity.class).putExtra("url", ads.ads.redirectUrl));
                            }
                        } else {
                            playerDuration = mediaplayer.getCurrentPosition();
                            if (playerStart) {
                                try {
                                    mediaplayer.pause();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                counter.cancel();
                                isMediaPlayerPrepared = false;
                                Utility.clickAds(context, ads.id);
                                showAd = true;
                                startActivity(new Intent(context, WebViewActivity.class).putExtra("url", ads.ads.redirectUrl));
                            }
                        }
                    }
                });
            }
        }
    }

    private void displaySkip(long duration) {
        counter = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                if (isStartNow) {
                    if (isAdPlayerStart) {
                        Controls.pauseControl(NewCampiagnActivity.this);
                        mediaplayer.start();
                        isStartNow = false;
                    }
                }
                skipText.setVisibility(View.VISIBLE);
                skipText.setText(" " + +millisUntilFinished / 1000 + " " + getString(R.string.second));
            }

            public void onFinish() {
                Constants.adShow = false;
                try {
                    mediaplayer.stop();
                    mediaplayer.reset();
                    mediaplayer.release();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                UserModel.getInstance().isVideoPlay = false;
                if (!Constants.isActivityResume) {
                    UserModel.getInstance().isSongPlayed = true;
                    if (Constants.isSongPlay) {
                        Constants.songSelectionNumberAfterAd = Utility.getIntPreferences(context,"next_song_number");
                        sendBroadcast(new Intent("Current_Song_Play").putExtra("song_number", Utility.getIntPreferences(context,"next_song_number")));
                    } else {
                        sendBroadcast(new Intent("Current_Song_Play").putExtra("song_number", Constants.SONG_NUMBER));
                    }
                }
                Utility.setIntPreferences(context,"next_song_number",0);
                Utility.mediaplayer = null;
                Utility.getAdForFreeUser(NewCampiagnActivity.this, Utility.getUserInfo(NewCampiagnActivity.this).id);
                finish();
                if (!Constants.isSongPlay) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("play_station"));
                }
            }

        }.start();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Current_Song_Play");
        myReceiver = new SongPlayInBackgroundBroadcast();
        registerReceiver(myReceiver, filter);

        Constants.adShow = true;
        Utility.setBooleanPreferences(context,"ad_playing",true);
        Constants.isActivityResume = true;
        Controls.pauseControl(context);

        if (ads.ads.adFormat.equalsIgnoreCase("Video")) {
            if (duration > 0) {
                Controls.pauseControl(context);
                isStartNow = true;
                isAdPlayerStart = true;
                if (showAd) {
                    showAd = false;
                    displaySkip(duration - playerDuration);
                    mediaplayer.start();
                    mediaplayer.seekTo(playerDuration);
                }
                if (isUserGoesback) {
                    isUserGoesback = false;
                    counter.cancel();
                    displaySkip(mediaplayer.getDuration() - mediaplayer.getCurrentPosition());
                }
            }
        } else {
            try {
                Controls.pauseControl(context);
                if (duration > 0) {
                    isStartNow = true;
                    isAdPlayerStart = true;
                    if (showAd) {
                        showAd = false;
                        displaySkip(duration - playerDuration);
                        if (mediaplayer != null) {
                            mediaplayer.start();
                            mediaplayer.seekTo(playerDuration);
                        }
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_ad);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_ad, new IntentFilter("finish_ad"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isUserGoesback = true;
        Constants.isActivityResume = false;
        unregisterReceiver(myReceiver);
        Constants.adShow = false;
        Utility.isAdAlreadyFound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.isSongPlaying = false;
        Constants.adShow = false;
        Utility.setBooleanPreferences(context,"ad_playing",false);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        duration = mediaplayer.getDuration();
        if (isMediaPlayerPrepared) {
            Controls.pauseControl(NewCampiagnActivity.this);
            Constants.adShow = true;
            mediaplayer.start();
            UserModel.getInstance().isVideoPlay = true;
        }
        displaySkip(duration);
        playerStart = true;
        isAdPrepareToPlay = true;
    }

    BroadcastReceiver finish_ad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mediaplayer != null) {
                    mediaplayer.stop();
                    mediaplayer.reset();
                    mediaplayer.release();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            finish();
            Utility.getAdForFreeUser(NewCampiagnActivity.this, Utility.getUserInfo(NewCampiagnActivity.this).id);
        }
    };

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }
}