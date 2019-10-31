package com.shirali.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.MySongPagerAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityPlayerBinding;
import com.shirali.interfaces.AdsAvailabilityCallback;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.interfaces.LikeAndUnlikeCallBack;
import com.shirali.model.songs.Song;
import com.shirali.model.stations.Stations;
import com.shirali.model.user.UserModel;
import com.shirali.service.PlayService;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.GetDataOnlineStreaming;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerActivity extends BaseActivity {

    private MySongPagerAdapter adapter;
    private ActivityPlayerBinding binding;
    private Context context;
    private int margin;
    private int song_position = 0;
    private ArrayList<String> listSong;
    private ArrayList<String> song_list;
    private boolean isAvailable = false;
    private boolean isForFirstTimePageLoad = false;
    private boolean isActivityResumed = false;
    private MixpanelAPI mixpanelAPI;
    private ArrayList<Song> playerSong;
    private ArrayList<Stations> playStation;
    private boolean isPlayFromTemp = false;
    private boolean isFromNotification = false;
    private String isVocal = "";
    private boolean itemUpdate = true;
    private ArrayList<Song> listOfPagerSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        context = this;
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        listSong = new ArrayList<>();
        song_list = new ArrayList<>();
        playerSong = new ArrayList<>();
        playStation = new ArrayList<>();
        listOfPagerSong = new ArrayList<>();
        binding.lblTitle.setSelected(true);
        binding.lblArtist.setSelected(true);
        binding.lblAlbumName.setSelected(true);
        isVocal = Utility.getUserInfo(context).isVocalOnly;

        Constants.isSongPlay = Utility.getBooleaPreferences(context, "tempForSong");

        if (Constants.isSongPlay) {
            if (Constants.SONGS_LIST != null) {
                if (Constants.SONGS_LIST.size() > 0) {
                    playerSong = Constants.SONGS_LIST;
                    listOfPagerSong.addAll(Constants.SONGS_LIST);
                } else {
                    isFromNotification = true;
                    isPlayFromTemp = true;
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Song>>() {
                    }.getType();
                    ArrayList<Song> arrayList = gson.fromJson(Utility.getStringPreferences(context, "tempSongList"), type);
                    playerSong = arrayList;
                    Constants.SONGS_LIST = playerSong;
                    UserModel.getInstance().listOfActualSong = playerSong;
                    Constants.SONG_NUMBER = Utility.getIntPreferences(context, "tempSongNumber");
                    playRegularSong(Constants.SONG_NUMBER);
                    Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                    isActivityResumed = true;
                    listOfPagerSong.addAll(Constants.SONGS_LIST);
                }
            }
        } else {
            Constants.isSongPlay = false;
            if (Constants.StationList != null) {
                if (Constants.StationList.size() > 0) {
                    playStation = Constants.StationList;
                } else {
                    isFromNotification = true;
                    isPlayFromTemp = true;
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Stations>>() {
                    }.getType();
                    ArrayList<Stations> arrayList = gson.fromJson(Utility.getStringPreferences(context, "tempSongList"), type);
                    playStation = arrayList;
                    Constants.StationList = playStation;
                    Constants.SONG_NUMBER = Utility.getIntPreferences(context, "tempSongNumber");
                    playRegularSong(Constants.SONG_NUMBER);
                }
            }
        }

        if (isVocal != null) {
            if (isVocal.length() > 0) {
                if (Build.VERSION.SDK_INT <= 22) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.seekBar.getLayoutParams();
                    params.setMargins(0, 0, 0, -30);
                    binding.seekBar.setLayoutParams(params);
                }
                binding.lyt.setBackgroundColor(context.getResources().getColor(R.color.purple_player));
                binding.mseek.getThumb().setColorFilter(context.getResources().getColor(R.color.purple_seek), PorterDuff.Mode.SRC_IN);
                binding.mseek.setProgressDrawable(context.getResources().getDrawable(R.drawable.purple_seekbar_drawable));
            } else {
                if (Build.VERSION.SDK_INT <= 22) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.seekBar.getLayoutParams();
                    params.setMargins(0, 0, 0, -30);
                    binding.seekBar.setLayoutParams(params);
                }
                binding.lyt.setBackgroundColor(context.getResources().getColor(R.color.bg_color));
                binding.mseek.getThumb().setColorFilter(context.getResources().getColor(R.color.back_color), PorterDuff.Mode.SRC_IN);
                binding.mseek.setProgressDrawable(context.getResources().getDrawable(R.drawable.seekbar_drawable));
            }
        }

        margin = (getScreenWidth() * 18) / 100;

        binding.myMusicPager.setPageMargin(-margin);
        binding.myMusicPager.setOffscreenPageLimit(3);
        binding.myMusicPager.setClipChildren(false);
        adapter = new MySongPagerAdapter(context, listOfPagerSong, Constants.StationList);
        binding.myMusicPager.setPageTransformer(true, new ZoomOutPageTransformer(true));
        binding.myMusicPager.setAdapter(adapter);

        if (Constants.isSongPlay) {
            binding.myMusicPager.setVisibility(View.VISIBLE);
            binding.imgCurrentSong.setVisibility(View.GONE);
            binding.imgAddSong.setVisibility(View.VISIBLE);
            binding.imgManu.setVisibility(View.VISIBLE);
            binding.imgSwitchAlbum.setVisibility(View.VISIBLE);
            binding.repeat.setVisibility(View.VISIBLE);
            binding.shuffle.setVisibility(View.VISIBLE);
            binding.imgNext.setVisibility(View.VISIBLE);
            binding.imgPrevious.setVisibility(View.VISIBLE);
            binding.lblAlbum.setVisibility(View.VISIBLE);
            if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0) {
                for (int i = 0; i < Constants.SONGS_LIST.size(); i++) {
                    if (Constants.song.equalsIgnoreCase(Constants.SONGS_LIST.get(i).id)) {
                        binding.myMusicPager.setCurrentItem(i);
                        song_position = i;
                        Constants.SONG_NUMBER = song_position;
                        updateSongData(Constants.SONGS_LIST.get(song_position));
                        UserModel.getInstance().currentPlaySong = Constants.SONGS_LIST.get(song_position);
                    }
                }
            }
        } else {
            binding.myMusicPager.setVisibility(View.GONE);
            binding.imgCurrentSong.setVisibility(View.VISIBLE);
            binding.imgAddSong.setVisibility(View.INVISIBLE);
            binding.imgManu.setVisibility(View.GONE);
            binding.imgSwitchAlbum.setVisibility(View.INVISIBLE);
            binding.repeat.setVisibility(View.GONE);
            binding.shuffle.setVisibility(View.GONE);
            binding.imgNext.setVisibility(View.GONE);
            binding.imgPrevious.setVisibility(View.GONE);
            binding.lblAlbum.setVisibility(View.VISIBLE);
            if (Constants.StationList != null && Constants.StationList.size() > 0) {
                for (int i = 0; i < Constants.StationList.size() - 1; i++) {
                    if (Constants.StationList.get(Constants.SONG_NUMBER).id.equalsIgnoreCase(Constants.StationList.get(i).id)) {
                        binding.myMusicPager.setCurrentItem(i);
                        song_position = i;
                        Constants.SONG_NUMBER = song_position;
                        updateStation(Constants.StationList.get(song_position));
                    }
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Get online streaming data for radio
                        new GetDataOnlineStreaming(context).execute(new URL(Constants.StationList.get(Constants.SONG_NUMBER).link));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }, 20000);
        }

        binding.myMusicPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                if (itemUpdate) {
                    if (Constants.isPageSelectedFromNextOfPrevious) {
                        if (isForFirstTimePageLoad) {
                            isForFirstTimePageLoad = false;
                            if (Constants.SONGS_LIST.get(position).isPremium) {
                                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    playRegularSong(position);
                                } else {
                                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("open_alert"));
                                }
                            } else {
                                Constants.SONG_NUMBER = position;
                                playRegularSong(position);
                            }
                        }
                    } else {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("open_alert"));
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Constants.isPageSelectedFromNextOfPrevious = true;
                            }
                        }, 500);
                    }
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemUpdate = true;
                        }
                    }, 500);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (Utility.getBooleaPreferences(context, "suffle")) {
                    song_list.add(UserModel.getInstance().listOfShuffleSong.get(Constants.SONG_NUMBER).id);
                    checkCurrentSongInMyMusic(UserModel.getInstance().listOfShuffleSong.get(Constants.SONG_NUMBER), Utility.getUserInfo(context).myMusic);
                } else {
                    song_list.add(UserModel.getInstance().listOfActualSong.get(Constants.SONG_NUMBER).id);
                    checkCurrentSongInMyMusic(UserModel.getInstance().listOfActualSong.get(Constants.SONG_NUMBER), Utility.getUserInfo(context).myMusic);
                }
                isForFirstTimePageLoad = true;
            }
        });

        binding.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isSongPlay) {
                    mixpanelAPI.track("Number of taps on Thumbs UP");
                    if (Utility.getBooleaPreferences(context, "showLikeUnlikePopup")) {
                        likeUnlikeSong(true);
                    } else {
                        Utility.likeUnlikeAlert(context, getResources().getString(R.string.thumbs_up_down_logic));
                        likeUnlikeSong(true);
                    }
                } else {
                    likeUnlikeStation(true);
                }
            }
        });

        binding.imgDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isSongPlay) {
                    mixpanelAPI.track("Number of taps on Thumbs DOWN");
                    if (Utility.getBooleaPreferences(context, "showLikeUnlikePopup")) {
                        likeUnlikeSong(false);
                    } else {
                        Utility.likeUnlikeAlert(context, getResources().getString(R.string.thumbs_up_down_logic));
                        likeUnlikeSong(false);
                    }
                } else {
                    likeUnlikeStation(false);
                }
            }
        });

        binding.imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isSongPlay) {
                    if (Constants.SONGS_LIST.size() > 0) {
                        if (Constants.isPlay) {
                            Controls.pauseControl(context);
                            //binding.imgPlay.setImageResource(R.drawable.play);
                        } else {
                            //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                            if (isFromNotification) {
                                isFromNotification = false;
                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                            } else {
                                Controls.playControl(context);
                            }
                        }
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                } else {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                        //binding.imgPlay.setImageResource(R.drawable.play);
                    } else {
                        //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                        if (isFromNotification) {
                            isFromNotification = false;
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        } else {
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                    }
                }
            }
        });

        binding.shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.isSongPlay) {
                    mixpanelAPI.track("Number of taps on Shuffle");
                    if (Constants.shuffel) {
                        /*adapter.updateList(UserModel.getInstance().listOfActualSong);
                        for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
                            if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(Constants.song)) {
                                updateSongData(UserModel.getInstance().listOfActualSong.get(i));
                                itemUpdate = false;
                                binding.myMusicPager.setCurrentItem(i);
                                break;
                            }
                        }*/
                        Utility.setBooleanPreferences(context, "suffle", false);
                        Constants.shuffel = false;
                        binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.suffle));
                    } else {
                        /*adapter.updateList(UserModel.getInstance().listOfShuffleSong);
                        for (int i = 0; i < UserModel.getInstance().listOfShuffleSong.size(); i++) {
                            if (UserModel.getInstance().listOfShuffleSong.get(i).id.equalsIgnoreCase(Constants.song)) {
                                updateSongData(UserModel.getInstance().listOfShuffleSong.get(i));
                                itemUpdate = false;
                                binding.myMusicPager.setCurrentItem(i);
                                break;
                            }
                        }*/
                        Utility.setBooleanPreferences(context, "suffle", true);
                        Utility.setBooleanPreferences(context, "repeat", false);
                        Constants.shuffel = true;
                        Constants.repeat = false;
                        if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                            binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.random_purple));
                        } else {
                            binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.suffle_selected));
                        }
                        binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat));
                    }
                }
            }
        });

        binding.repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.isSongPlay) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        mixpanelAPI.track("Number of taps on Repeat");
                        if (Constants.repeat) {
                            Utility.setBooleanPreferences(context, "repeat", false);
                            Constants.repeat = false;
                            binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat));
                        } else {
                            if (Utility.getBooleaPreferences(context, "suffle")) {
                                adapter.updateList(UserModel.getInstance().listOfActualSong);
                                for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
                                    if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(Constants.song)) {
                                        updateSongData(UserModel.getInstance().listOfActualSong.get(i));
                                        itemUpdate = false;
                                        binding.myMusicPager.setCurrentItem(i);
                                        break;
                                    }
                                }
                            }
                            Utility.setBooleanPreferences(context, "suffle", false);
                            Utility.setBooleanPreferences(context, "repeat", true);
                            Constants.repeat = true;
                            Constants.shuffel = false;
                            binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.suffle));
                            if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                                binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.rename_purple));
                            } else {
                                binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat_selected));
                            }
                        }
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_repeat_as_many_songs_as_you_want));
                    }
                }
            }
        });

        binding.imgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.isSongPlay) {
                            mixpanelAPI.track("Number of taps on Previous Item");
                            Constants.isPageSelectedFromNextOfPrevious = false;
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                if (Constants.isPlay) {
                                    Controls.pauseControl(context);
                                }
                                binding.imgPlay.setVisibility(View.GONE);
                                binding.loadingMusic.setVisibility(View.VISIBLE);
                                Controls.previousControl(context);
                                Utility.setIntPreferences(context, "albumSkipCount", 0);
                            } else {
                                if (!Constants.isPlay) {
                                    binding.imgPlay.setImageResource(R.drawable.play);
                                }
                                UserModel.getInstance().previousPlayMethod(context);
                            }
                            Constants.repeat = false;
                            binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat));
                            getSongStatus();
                            song_list.clear();
                            song_list.add(Constants.SONGS_LIST.get(song_position).id);
                            isAvailable = false;
                            checkCurrentSongInMyMusic(Constants.SONGS_LIST.get(song_position), Utility.getUserInfo(context).myMusic);
                        }
                    }
                }, 100); //500
            }
        });

        binding.imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.isSongPlay) {
                            mixpanelAPI.track("Number of taps on Skip");
                            Constants.isPageSelectedFromNextOfPrevious = false;
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") ||
                                    UserModel.getInstance().isForRenew ||
                                    UserModel.getInstance().isForTrial) {
                                if (Constants.isPlay) {
                                    Controls.pauseControl(context);
                                }
                                binding.imgPlay.setVisibility(View.GONE);
                                binding.loadingMusic.setVisibility(View.VISIBLE);
                                Controls.nextControl(context);
                                Utility.setIntPreferences(context, "albumSkipCount", 0);
                            } else {
                                if (!Constants.isPlay) {
                                    binding.imgPlay.setImageResource(R.drawable.play);
                                }
                                UserModel.getInstance().nextPlayMethod(context);
                            }
                            Constants.repeat = false;
                            binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat));
                            getSongStatus();
                            song_list.clear();
                            song_list.add(Constants.SONGS_LIST.get(song_position).id);
                            isAvailable = false;
                            checkCurrentSongInMyMusic(Constants.SONGS_LIST.get(song_position), Utility.getUserInfo(context).myMusic);
                        }
                    }
                }, 100); //500
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(context, MainActivity.class));
                    SplashActivity.isFromDeep = false;
                }
                if (isPlayFromTemp) {
                    isPlayFromTemp = false;
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(context, MainActivity.class));
                }
                if (Utility.getBooleaPreferences(context, "finish_main")) {
                    Utility.setBooleanPreferences(context, "finish_main", false);
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(context, MainActivity.class));
                }
                finish();
                overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
            }
        });

        if (Constants.isSongPlay) {
            binding.mseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        Constants.seekTo = progress;
                        Controls.seekToControl(context);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else {
            binding.mseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekBar.setProgress(0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        binding.imgAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isSongPlay) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        song_list.clear();
                        song_list.add(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                        addedToMyMusic(Utility.getUserInfo(context).myMusic, song_list, false);
                        binding.imgAddSong.setVisibility(View.INVISIBLE);
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                    }
                }
            }
        });

        binding.imgManu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.isSongPlay) {
                    openMenu();
                }
            }
        });

        binding.imgSwitchAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.isSongPlay) {
                    startActivity(new Intent(context, PlayerDetailActivity.class));
                }
            }
        });

        updateUI();

        if(UserModel.getInstance().isSongLoading){
            binding.imgPlay.setVisibility(View.GONE);
            binding.imgPause.setVisibility(View.GONE);
            binding.loadingMusic.setVisibility(View.VISIBLE);
        }
    }

    //Play continuous song
    private void playRegularSong(final int position) {
        if (Constants.isSongPlay) {
            UserModel.getInstance().freePaidUser(context, Constants.SONGS_LIST, position, new FreePaidUserCallBack() {
                @Override
                public void freePaidUser(boolean ifPaid) {
                    if (ifPaid) {
                        playSong(position);
                    } else {
                        playSong(position);
                    }
                }
            });
        } else {
            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                playSong(position);
            } else {
                UserModel.getInstance().showAdsIfAvailable(context, Utility.getUserInfo(context).id, new AdsAvailabilityCallback() {
                    @Override
                    public void adsAvailable(boolean isAdAvailable) {
                        if (isAdAvailable) {
                            Constants.SONG_NUMBER = position;
                        } else {
                            Constants.SONG_NUMBER = position;
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                    }
                });
            }
        }
    }

    //Calculate screen width
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    //Update title and background
    private void updateSongData(Song song) {
        binding.lblSongTitle.setText(context.getResources().getString(R.string.playing_from_album));
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            if (song.albums.get(0).titleHebrew == null || song.albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                binding.lblAlbumName.setText(song.albums.get(0).title);
            } else {
                binding.lblAlbumName.setText(song.albums.get(0).titleHebrew);
            }
            if (song.title_hebrew.equalsIgnoreCase("") || song.title_hebrew == null) {
                binding.lblTitle.setText(song.title);
            } else {
                binding.lblTitle.setText(song.title_hebrew);
            }
            if (song.artist.nameHebrew.equalsIgnoreCase("") || song.artist.nameHebrew == null) {
                binding.lblArtist.setText(song.artist.name);
            } else {
                binding.lblArtist.setText(song.artist.nameHebrew);
            }
        } else {
            binding.lblAlbumName.setText(song.albums.get(0).title);
            binding.lblTitle.setText(song.title);
            binding.lblArtist.setText(song.artist.name);
        }
        if (isActivityResumed) {
            Glide.with(context).load(song.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgBlurSong);
        }
    }

    //Update title and background
    private void updateStation(Stations song) {
        binding.lblTitle.setTextSize(14);
        binding.lblArtist.setTextSize(16);
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            if (song.titleHebrew.equalsIgnoreCase("") || song.titleHebrew == null) {
                binding.lblArtist.setText(song.title);
                binding.lblSongTitle.setText(song.title);
                binding.lblAlbumName.setText(song.title);
            } else {
                binding.lblArtist.setText(song.titleHebrew);
                binding.lblSongTitle.setText(song.titleHebrew);
                binding.lblAlbumName.setText(song.titleHebrew);
            }
        } else {
            binding.lblArtist.setText(song.title);
            binding.lblSongTitle.setText(song.title);
            binding.lblAlbumName.setText(song.title);
        }
    }

    //Check current song is exist in user my music for display plus icon
    private void checkCurrentSongInMyMusic(Song song, ArrayList<String> songsId) {
        for (int i = 0; i < songsId.size(); i++) {
            if (songsId.get(i).equalsIgnoreCase(song.id)) {
                isAvailable = true;
                song_list.clear();
                song_list.add(song.id);
            }
        }
        if (isAvailable) {
            binding.imgAddSong.setVisibility(View.INVISIBLE);
        } else {
            binding.imgAddSong.setVisibility(View.VISIBLE);
        }
    }

    private void likeUnlikeSong(boolean isLike) {
        if (isLike) {
            UserModel.getInstance().likeapi(context, new LikeAndUnlikeCallBack() {
                @Override
                public void statusLikeUnlike(int i) {
                    UserModel.getInstance().songStatus = i;
                    if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                        binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.like_purple) : getResources().getDrawable(R.drawable.thumb_up_white));
                        binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.unlike_purple) : getResources().getDrawable(R.drawable.thumb_down_white));
                    } else {
                        binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.thumb_up) : getResources().getDrawable(R.drawable.thumb_up_white));
                        binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.thumb_down) : getResources().getDrawable(R.drawable.thumb_down_white));
                    }
                }
            });
        } else {
            UserModel.getInstance().unlikeApi(context, new LikeAndUnlikeCallBack() {
                @Override
                public void statusLikeUnlike(int i) {
                    UserModel.getInstance().songStatus = i;
                    if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                        binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.like_purple) : getResources().getDrawable(R.drawable.thumb_up_white));
                        binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.unlike_purple) : getResources().getDrawable(R.drawable.thumb_down_white));
                    } else {
                        binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.thumb_up) : getResources().getDrawable(R.drawable.thumb_up_white));
                        binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.thumb_down) : getResources().getDrawable(R.drawable.thumb_down_white));
                    }
                }
            });
        }

    }

    private void likeUnlikeStation(boolean isLike) {
        if (isLike) {
            UserModel.getInstance().likeStationApi(context, new LikeAndUnlikeCallBack() {
                @Override
                public void statusLikeUnlike(int i) {
                    UserModel.getInstance().stationStatus = i;
                    binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.thumb_up) : getResources().getDrawable(R.drawable.thumb_up_white));
                    binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.thumb_down) : getResources().getDrawable(R.drawable.thumb_down_white));
                }
            });
        } else {
            UserModel.getInstance().unlikeStationApi(context, new LikeAndUnlikeCallBack() {
                @Override
                public void statusLikeUnlike(int i) {
                    UserModel.getInstance().stationStatus = i;
                    binding.imgLike.setImageDrawable(i == 1 ? getResources().getDrawable(R.drawable.thumb_up) : getResources().getDrawable(R.drawable.thumb_up_white));
                    binding.imgDisLike.setImageDrawable(i == 0 ? getResources().getDrawable(R.drawable.thumb_down) : getResources().getDrawable(R.drawable.thumb_down_white));
                }
            });
        }

    }

    private void getSongStatus() {
        UserModel.getInstance().getSongStatusByID(context, new LikeAndUnlikeCallBack() {
            @Override
            public void statusLikeUnlike(int i) {
                if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                    binding.imgLike.setImageDrawable(i == 1 ? ContextCompat.getDrawable(context, R.drawable.like_purple) : ContextCompat.getDrawable(context, R.drawable.thumb_up_white));
                    binding.imgDisLike.setImageDrawable(i == 0 ? ContextCompat.getDrawable(context, R.drawable.unlike_purple) : ContextCompat.getDrawable(context, R.drawable.thumb_down_white));
                } else {
                    binding.imgLike.setImageDrawable(i == 1 ? ContextCompat.getDrawable(context, R.drawable.thumb_up) : ContextCompat.getDrawable(context, R.drawable.thumb_up_white));
                    binding.imgDisLike.setImageDrawable(i == 0 ? ContextCompat.getDrawable(context, R.drawable.thumb_down) : ContextCompat.getDrawable(context, R.drawable.thumb_down_white));
                }
            }
        });
    }

    //Add and remove song in/from my music
    private void addedToMyMusic(ArrayList<String> myMusic, ArrayList<String> song_list, boolean isRemove) {
        if (isRemove) {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (myMusic.contains(song_list.get(i))) {
                    listSong.remove(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.song_remove_from_my_music));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.song_added_to_your_music));
        }
    }

    //Display bottom sheet menu
    private void openMenu() {
        final Dialog d = new BottomSheetDialog(context);
        d.setContentView(R.layout.user_action_onsong_cell);
        View view = d.findViewById(R.id.bs);
        ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        TextView artist_name = (TextView) d.findViewById(R.id.tvArtistName);
        ImageView artistImage = (ImageView) d.findViewById(R.id.ivArtistImage);
        try {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).titleHebrew == null || Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                    artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).title);
                } else {
                    artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).title_hebrew);
                }
            } else {
                artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).title);
            }
            Glide.with(context).load(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(artistImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
        if (binding.imgAddSong.getVisibility() == View.VISIBLE) {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
        } else {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
        }
        d.setCancelable(true);
        d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums != null) {
                    if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.size() > 0) {
                        UserModel.getInstance().tempAlbum = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0);
                        if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).isPremium) {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                openAlbum(Constants.SONG_NUMBER);
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                            }
                        } else {
                            openAlbum(Constants.SONG_NUMBER);
                        }
                    } else {
                        Utility.showNoDataFound(context);
                    }
                } else {
                    Utility.showNoDataFound(context);
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.isPremium) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        openArtist();
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    }
                } else {
                    openArtist();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytShareSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.shareIt(context, "song", Constants.SONGS_LIST.get(song_position).title, Constants.SONGS_LIST.get(song_position).artist.name, Constants.SONGS_LIST.get(song_position).shareUrl);
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                } else {
                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imgAddSong.setVisibility(View.VISIBLE);
                addedToMyMusic(Utility.getUserInfo(context).myMusic, song_list, true);
                d.dismiss();
            }
        });
        d.show();
    }

    public void updateUI() {
        try {
            if (Utility.isServiceRunning(SongPlayService.class.getName(), PlayerActivity.this)) {
                Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Integer i[] = (Integer[]) msg.obj;
                        if (Constants.isSongPlay) {
                            if (i[2] != 0) {
                                binding.mseek.setProgress(i[2]);
                            }
                            try {
                                binding.lblCurrentTime.setText(Utility.milliSecondsToTimer(i[1]));
                                binding.lblRemainingTime.setText("-" + Utility.milliSecondsToTimer(i[1] - i[0]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                if (Constants.isSongPlay) {
                    binding.mseek.setVisibility(View.VISIBLE);
                }
            }
            if (Constants.isPlay) {
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
            } else {
                binding.imgPlay.setImageResource(R.drawable.play);
                if (Constants.isSongPlay) {
                    if (UserModel.getInstance().songPlayerDuration > 0){
                        binding.mseek.setProgress(UserModel.getInstance().songPlayerDuration);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Play song and station
    private void playSong(int position) {
        if (Constants.isSongPlay) {
            if (position > song_position) {
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
                }
                song_position = position;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(position).id;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                updateSongData(Constants.SONGS_LIST.get(position));
                UserModel.getInstance().currentPlaySong = Constants.SONGS_LIST.get(position);
            } else {
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
                }
                song_position = position;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(position).id;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                updateSongData(Constants.SONGS_LIST.get(position));
                UserModel.getInstance().currentPlaySong = Constants.SONGS_LIST.get(position);
            }
            song_list.clear();
            song_list.add(Constants.SONGS_LIST.get(song_position).id);
            isAvailable = false;
            checkCurrentSongInMyMusic(Constants.SONGS_LIST.get(song_position), Utility.getUserInfo(context).myMusic);
        } else {
            Constants.isSongPlay = false;
            if (Constants.isPlay) {
                Controls.pauseControl(context);
            }
            Constants.SONG_NUMBER = position;
            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
            updateStation(Constants.StationList.get(position));
        }
    }

    //Add a song in my music
    private void addSong(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", list);
        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (user.success) {
                            Utility.setUserInfo(context, user.user);
                            UserModel.getInstance().getdata(context);
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

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().artist_id = Constants.SONGS_LIST.get(position).artist;
            UserModel.getInstance().album = Constants.SONGS_LIST.get(position).albums.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(context, AlbumDetailActivity.class);
        context.startActivity(intent);
    }

    private void openArtist() {
        Intent intent = new Intent(context, ArtistDetailActivity.class);
        intent.putExtra("artist_id", Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.id);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
            Intent playIntent = new Intent(this, SongPlayService.class);
            startService(playIntent);
        }
        if (!Utility.isServiceRunning(PlayService.class.getName(), context)) {
            Intent playerIntent = new Intent(this, PlayService.class);
            startService(playerIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mixpanelAPI.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (SplashActivity.isFromDeep) {
            UserModel.getInstance().openFragment = "BROWSE";
            startActivity(new Intent(context, MainActivity.class));
            SplashActivity.isFromDeep = false;
        }
        if (isPlayFromTemp) {
            isPlayFromTemp = false;
            UserModel.getInstance().openFragment = "BROWSE";
            startActivity(new Intent(context, MainActivity.class));
        }
        if (Utility.getBooleaPreferences(context, "finish_main")) {
            Utility.setBooleanPreferences(context, "finish_main", false);
            UserModel.getInstance().openFragment = "BROWSE";
            startActivity(new Intent(context, MainActivity.class));
        }
        finish();
        overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        isActivityResumed = true;
        Utility.isConnectingToInternet(context);
        if (Constants.isSongPlay) {
            if (Utility.getBooleaPreferences(context, "suffle")) {
                Constants.shuffel = true;
                adapter.updateList(UserModel.getInstance().listOfShuffleSong);
                for (int i = 0; i < UserModel.getInstance().listOfShuffleSong.size(); i++) {
                    if (UserModel.getInstance().listOfShuffleSong.get(i).id.equalsIgnoreCase(Constants.song)) {
                        updateSongData(UserModel.getInstance().listOfShuffleSong.get(i));
                        //itemUpdate = false;
                        binding.myMusicPager.setCurrentItem(i);

                        song_list.clear();
                        song_list.add(UserModel.getInstance().listOfShuffleSong.get(Constants.SONG_NUMBER).id);
                        checkCurrentSongInMyMusic(UserModel.getInstance().listOfShuffleSong.get(Constants.SONG_NUMBER), Utility.getUserInfo(context).myMusic);
                        break;
                    }
                }
                binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.suffle_selected));
            } else {
                Constants.shuffel = false;
                adapter.updateList(UserModel.getInstance().listOfActualSong);
                for (int i = 0; i < UserModel.getInstance().listOfActualSong.size(); i++) {
                    if (UserModel.getInstance().listOfActualSong.get(i).id.equalsIgnoreCase(Constants.song)) {
                        updateSongData(UserModel.getInstance().listOfActualSong.get(i));
                        //itemUpdate = false;
                        binding.myMusicPager.setCurrentItem(i);

                        song_list.clear();
                        song_list.add(UserModel.getInstance().listOfActualSong.get(Constants.SONG_NUMBER).id);
                        checkCurrentSongInMyMusic(UserModel.getInstance().listOfActualSong.get(Constants.SONG_NUMBER), Utility.getUserInfo(context).myMusic);
                        break;
                    }
                }
                binding.shuffle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.suffle));
            }

            if (Utility.getBooleaPreferences(context, "repeat")) {
                binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat_selected));
            } else {
                binding.repeat.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.repeat));
            }
        }
        updateUI();
        try {
            if (Constants.isSongPlay) {
                getSongStatus();
                listOfPagerSong.clear();
                listOfPagerSong.addAll(Constants.SONGS_LIST);
                adapter.notifyDataSetChanged();
                Song data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (data.title_hebrew.equalsIgnoreCase("") || data.title_hebrew == null) {
                        binding.lblTitle.setText(data.title);
                    } else {
                        binding.lblTitle.setText(data.title_hebrew);
                    }
                    if (data.artist.nameHebrew.equalsIgnoreCase("") || data.artist.nameHebrew == null) {
                        binding.lblArtist.setText(data.artist.name);
                    } else {
                        binding.lblArtist.setText(data.artist.nameHebrew);
                    }
                } else {
                    binding.lblTitle.setText(data.title);
                    binding.lblArtist.setText(data.artist.name);
                }
                Glide.with(context).load(data.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgBlurSong);
                checkCurrentSongInMyMusic(Constants.SONGS_LIST.get(Constants.SONG_NUMBER), Utility.getUserInfo(context).myMusic);
            } else {
                binding.lblTitle.setTextSize(14);
                binding.lblArtist.setTextSize(16);
                getStationStatus();
                Stations data = Constants.StationList.get(Constants.SONG_NUMBER);
                binding.lblTitle.setText(context.getResources().getString(R.string.playing_live_from));
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (data.titleHebrew.equalsIgnoreCase("") || data.titleHebrew == null) {
                        binding.lblArtist.setText(data.title);
                    } else {
                        binding.lblArtist.setText(data.titleHebrew);
                    }
                } else {
                    binding.lblArtist.setText(data.title);
                }
                updateRadioDataForFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (Constants.isSongPlay) {
                if (UserModel.getInstance().isPlaySongAfterAd) {
                    if (Constants.isChangeSong) {
                        if (!Utility.getBooleaPreferences(context,"ad_in_background")) {
                            Constants.isChangeSong = false;
                            Controls.nextControl(context);
                            NewCampiagnActivity.isFromCampaign = false;
                            binding.imgPlay.setImageResource(R.drawable.icon_pause);
                        }
                    } else {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        NewCampiagnActivity.isFromCampaign = false;
                        binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    }
                } else {
                    if (UserModel.getInstance().tempSongList.size() > 0) {
                        UserModel.getInstance().isPlaySongAfterAd = true;
                        Constants.SONGS_LIST = UserModel.getInstance().tempSongList;
                        Constants.SONG_NUMBER = 0;
                        Constants.song = Constants.SONGS_LIST.get(0).id;
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    }
                }
            } else {
                if (UserModel.getInstance().currentPosition != 0) {
                    Controls.playControl(context);
                } else {
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                }
            }
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(play_pause_control);
        LocalBroadcastManager.getInstance(context).registerReceiver(play_pause_control, new IntentFilter("play_pause_control"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(show_loader);
        LocalBroadcastManager.getInstance(context).registerReceiver(show_loader, new IntentFilter("show_loader"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(update_playerLayout);
        LocalBroadcastManager.getInstance(context).registerReceiver(update_playerLayout, new IntentFilter("update_playerLayout"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(open_alert);
        LocalBroadcastManager.getInstance(context).registerReceiver(open_alert, new IntentFilter("open_alert"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(context).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(switch_off_player);
        LocalBroadcastManager.getInstance(context).registerReceiver(switch_off_player, new IntentFilter("switch_off_player"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(update_metadata);
        LocalBroadcastManager.getInstance(context).registerReceiver(update_metadata, new IntentFilter("update_metadata"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(stop_player);
        LocalBroadcastManager.getInstance(context).registerReceiver(stop_player, new IntentFilter("stop_player"));
    }

    private void getStationStatus() {
        UserModel.getInstance().getStationStatusByID(context, new LikeAndUnlikeCallBack() {
            @Override
            public void statusLikeUnlike(int i) {
                binding.imgLike.setImageDrawable(i == 1 ? ContextCompat.getDrawable(context, R.drawable.thumb_up) : ContextCompat.getDrawable(context, R.drawable.thumb_up_white));
                binding.imgDisLike.setImageDrawable(i == 0 ? ContextCompat.getDrawable(context, R.drawable.thumb_down) : ContextCompat.getDrawable(context, R.drawable.thumb_down_white));
            }
        });
    }

    //Update dynamic song data of radio
    private void updateRadioDataForFirst() {
        if (UserModel.getInstance().title != null && !UserModel.getInstance().title.equalsIgnoreCase("")) {
            binding.lblSongTitle.setText(UserModel.getInstance().title);
        } else {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                if (Constants.StationList.get(Constants.SONG_NUMBER).title == null && Constants.StationList.get(Constants.SONG_NUMBER).title.equalsIgnoreCase("")) {
                    binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                } else {
                    binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew);
                }
            } else {
                binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
            }
        }
        if (UserModel.getInstance().artistName != null && !UserModel.getInstance().artistName.equalsIgnoreCase("")) {
            binding.lblAlbumName.setText(UserModel.getInstance().artistName);
        } else {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                if (Constants.StationList.get(Constants.SONG_NUMBER).title == null && Constants.StationList.get(Constants.SONG_NUMBER).title.equalsIgnoreCase("")) {
                    binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                } else {
                    binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew);
                }
            } else {
                binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
            }
        }
        Glide.with(context).load(UserModel.getInstance().artwork).placeholder(R.drawable.radio_cover).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(binding.imgCurrentSong);
        Glide.with(context).load(UserModel.getInstance().artwork).placeholder(R.drawable.radio_cover).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.radio_cover).crossFade().into(binding.imgBlurSong);
        UserModel.getInstance().artwork = "";
    }

    //Set page transform for view pager
    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_ALPHA = 0.7f;
        private float MIN_SCALE = 1f;

        ZoomOutPageTransformer(boolean isZoomEnable) {
            if (isZoomEnable) {
                MIN_SCALE = 0.9f;
            } else {
                MIN_SCALE = 1f;
            }
        }

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            float vertMargin = pageHeight * (1 - MIN_SCALE) / 2;
            float horzMargin = pageWidth * (1 - MIN_SCALE) / 2;
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
            if (position < -1) {
                view.setAlpha(MIN_ALPHA);
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                vertMargin = pageHeight * (1 - scaleFactor) / 2;
                horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                view.setAlpha(MIN_ALPHA);
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }
        }
    }


    BroadcastReceiver play_pause_control = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                if(intent.getStringExtra("action").equalsIgnoreCase("play")){
                    binding.imgPlay.setImageResource(R.drawable.icon_pause);
                }
                else if(intent.getStringExtra("action").equalsIgnoreCase("pause")){
                    binding.imgPlay.setImageResource(R.drawable.play);
                }
            }
            /*if (Constants.isPlay) {
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
            } else {
                binding.imgPlay.setImageResource(R.drawable.play);
            }*/
        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.isPlay) {
                binding.imgPlay.setVisibility(View.VISIBLE);
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
                binding.loadingMusic.setVisibility(View.GONE);
                Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        final Integer i[] = (Integer[]) msg.obj;
                        if (Constants.isSongPlay) {
                            if (i[2] != 0) {
                                binding.mseek.setProgress(i[2]);
                            }
                            try {
                                binding.lblCurrentTime.setText(Utility.milliSecondsToTimer(i[1]));
                                binding.lblRemainingTime.setText("-" + Utility.milliSecondsToTimer(i[1] - i[0]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
            if (Constants.isSongPlay) {
                binding.mseek.setVisibility(View.VISIBLE);
                getSongStatus();
            } else {
                binding.mseek.setVisibility(View.GONE);
                getStationStatus();
            }
        }
    };
    BroadcastReceiver show_loader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.imgPlay.setVisibility(View.GONE);
            binding.loadingMusic.setVisibility(View.VISIBLE);

            if(!intent.hasExtra("seek_percentage"))
                binding.mseek.setProgress(0);

            if (Constants.isFirstLoad) {
                Constants.isFirstLoad = false;
                binding.myMusicPager.setCurrentItem(Constants.SONG_NUMBER);
            }
            if (Constants.isSongPlay) {
                try {
                    song_list.clear();
                    song_list.add(Constants.SONGS_LIST.get(song_position).id);
                    isAvailable = false;
                    checkCurrentSongInMyMusic(Constants.SONGS_LIST.get(song_position), Utility.getUserInfo(context).myMusic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    BroadcastReceiver open_alert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.SONGS_LIST.size() > 0 && Constants.SONGS_LIST.size() > Constants.SONG_NUMBER) {
                binding.myMusicPager.setCurrentItem(Constants.SONG_NUMBER);
                updateSongData(Constants.SONGS_LIST.get(Constants.SONG_NUMBER));
            }
        }
    };
    BroadcastReceiver change_song = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.isPageSelectedFromNextOfPrevious = false;
            updateSongData(Constants.SONGS_LIST.get(Constants.SONG_NUMBER));
        }
    };
    private BroadcastReceiver switch_off_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private BroadcastReceiver update_metadata = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mContext, Intent intent) {
            if (!isFinishing()) {
                if (UserModel.getInstance().title != null && !UserModel.getInstance().title.equalsIgnoreCase("")) {
                    binding.lblSongTitle.setText(UserModel.getInstance().title);
                } else {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (Constants.StationList.get(Constants.SONG_NUMBER).title == null && Constants.StationList.get(Constants.SONG_NUMBER).title.equalsIgnoreCase("")) {
                            binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                        } else {
                            binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew);
                        }
                    } else {
                        binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                    }
                }
                if (UserModel.getInstance().artistName != null && !UserModel.getInstance().artistName.equalsIgnoreCase("")) {
                    binding.lblAlbumName.setText(UserModel.getInstance().artistName);
                } else {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (Constants.StationList.get(Constants.SONG_NUMBER).title == null && Constants.StationList.get(Constants.SONG_NUMBER).title.equalsIgnoreCase("")) {
                            binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                        } else {
                            binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew);
                        }
                    } else {
                        binding.lblSongTitle.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                    }
                }
                Glide.with(context).load(UserModel.getInstance().artwork).placeholder(R.drawable.radio_cover).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(binding.imgCurrentSong);
                Glide.with(context).load(UserModel.getInstance().artwork).placeholder(R.drawable.radio_cover).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.radio_cover).crossFade().into(binding.imgBlurSong);
                UserModel.getInstance().artwork = "";
            }
        }
    };
    BroadcastReceiver update_playerLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UserModel.getInstance().isSongLoading = false;
            binding.loadingMusic.setVisibility(View.GONE);
            binding.imgPlay.setVisibility(View.VISIBLE);
            if (Constants.isSongPlay) {
                if (isActivityResumed) {
                    binding.mseek.setVisibility(View.VISIBLE);
                    listOfPagerSong.clear();
                    listOfPagerSong.addAll(Constants.SONGS_LIST);
                    adapter.notifyDataSetChanged();
                    if (Constants.SONGS_LIST.size() > Constants.SONG_NUMBER) {
                        binding.myMusicPager.setCurrentItem(Constants.SONG_NUMBER);
                    }
                    updateSongData(Constants.SONGS_LIST.get(Constants.SONG_NUMBER));
                }
            } else {
                binding.mseek.setVisibility(View.GONE);
                if (Constants.StationList.size() > Constants.SONG_NUMBER) {
                    binding.myMusicPager.setCurrentItem(Constants.SONG_NUMBER);
                }
                updateStation(Constants.StationList.get(Constants.SONG_NUMBER));
            }
        }
    };
    private BroadcastReceiver stop_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.mseek.setProgress(0);
            binding.imgPlay.setImageResource(R.drawable.play);
        }
    };

}
