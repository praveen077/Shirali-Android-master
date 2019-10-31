package com.shirali.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.App;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityMainBinding;
import com.shirali.fragment.NewBrowseFragment;
import com.shirali.fragment.PlayFragment;
import com.shirali.fragment.RecentFragment;
import com.shirali.fragment.SearchFragment;
import com.shirali.fragment.mymusic.MyMusicFragment;
import com.shirali.fragment.mymusic.MySongFragment;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongDetail;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.service.PlayService;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static boolean isForRenew, isForTrial;
    public static boolean isFirstTimeBrowse = false;
    private ActivityMainBinding binding;
    private Fragment fragment = null;
    private long onRecentBackPressedTime;
    private String currentTab = "";
    private App app;
    private Context baseContext;
    private MixpanelAPI mixpanel;
    private String current_fragment;
    private SharedPreferences preference;
    private String albumString = "", songString = "";
    private ArrayList<Fragment> listOfFragment;
    private ArrayList<String> listOfFragmentList;
    private boolean isFirstTimeRecent = false, isFirstTimeSearch = false, isFirstTimeMusic = false;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        baseContext = this;
        binding.lblSongName.setSelected(true);
        preference = getSharedPreferences("login", 0);
        listOfFragment = new ArrayList<>(4);
        listOfFragmentList = new ArrayList<>(4);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utility.getAdForFreeUser(baseContext, Utility.getUserInfo(baseContext).id);
                for (int i = 0; i < 5; i++) {
                    listOfFragment.add(i, null);
                    listOfFragmentList.add(i, null);
                }
            }
        });

        preferences = getApplicationContext().getSharedPreferences("login", 0);

        Utility.isConnectingToInternet(baseContext);
        UserModel.getInstance().getGenreData(MainActivity.this, null);

        String projectToken = Constants.PROJECT_TOKEN;
        Constants.repeat = Utility.getBooleaPreferences(MainActivity.this, "repeat");
        Constants.shuffel = Utility.getBooleaPreferences(MainActivity.this, "suffle");

        mixpanel = MixpanelAPI.getInstance(this, projectToken);
        mixpanel.identify(Utility.getUserInfo(MainActivity.this).id);
        mixpanel.getPeople().identify(Utility.getUserInfo(MainActivity.this).id);
        mixpanel.getPeople().set("$name", Utility.getUserInfo(MainActivity.this).firstName);
        mixpanel.getPeople().set("$email", Utility.getUserInfo(MainActivity.this).email);
        mixpanel.getPeople().set("OS", "Android");
        mixpanel.getPeople().initPushHandling(Constants.GOOGLE_SENDER_ID);

        app = (App) getApplicationContext();

        setSupportActionBar(binding.toolbar);
        binding.Header.setText(getResources().getString(R.string.browse));

        if (!SplashActivity.isFromDeep) {
            UserModel.getInstance().openFragment = CustomTab.BROWSE.toString();
            SplashActivity.isFromDeep = false;
        }

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                try {
                    if (Utility.getUserInfo(baseContext).isVocalOnly != null) {
                        if (Utility.getUserInfo(baseContext).isVocalOnly.length() > 0) {
                            if (Build.VERSION.SDK_INT <= 22) {
                                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.seekBar.getLayoutParams();
                                params.setMargins(0, 0, 0, -30);
                                binding.seekBar.setLayoutParams(params);
                            }
                            binding.lytPlay.setBackgroundColor(getResources().getColor(R.color.purple_player));
                            binding.seek.getThumb().setColorFilter(getResources().getColor(R.color.purple_seek), PorterDuff.Mode.SRC_IN);
                            binding.seek.setProgressDrawable(baseContext.getResources().getDrawable(R.drawable.purple_seekbar_drawable));
                            binding.seekRadio.getThumb().setColorFilter(getResources().getColor(R.color.purple_seek), PorterDuff.Mode.SRC_IN);
                            binding.seekRadio.setProgressDrawable(baseContext.getResources().getDrawable(R.drawable.purple_seekbar_drawable));
                        } else {
                            if (Build.VERSION.SDK_INT <= 22) {
                                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.seekBar.getLayoutParams();
                                params.setMargins(0, 0, 0, -30);
                                binding.seekBar.setLayoutParams(params);
                            }
                            binding.lytPlay.setBackgroundColor(getResources().getColor(R.color.bg_color));
                            binding.seek.getThumb().setColorFilter(getResources().getColor(R.color.back_color), PorterDuff.Mode.SRC_IN);
                            binding.seek.setProgressDrawable(baseContext.getResources().getDrawable(R.drawable.seekbar_drawable));
                            binding.seekRadio.getThumb().setColorFilter(getResources().getColor(R.color.back_color), PorterDuff.Mode.SRC_IN);
                            binding.seekRadio.setProgressDrawable(baseContext.getResources().getDrawable(R.drawable.seekbar_drawable));
                        }
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!UserModel.getInstance().isSongLoading) {
                    if (fromUser) {
                        Constants.seekTo = progress;
                        Controls.seekToControl(baseContext);
                    }
                } else {
                    binding.seek.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.imgSetting.setOnClickListener(this);
        binding.first.setOnClickListener(this);
        binding.second.setOnClickListener(this);
        binding.third.setOnClickListener(this);
        binding.fourth.setOnClickListener(this);
        binding.fifth.setOnClickListener(this);
        binding.openMusic.setOnClickListener(this);
        binding.lytPlay.setOnClickListener(this);
        binding.imgPlay.setOnClickListener(this);

        //It is for share functionality
        if (getIntent().hasExtra("id")) {
            getSongDetail(baseContext, getIntent().getStringExtra("id"));
        } else if (getIntent().hasExtra("album")) {
            UserModel.getInstance().openFragment = "Browse";
            Utility.showSubscriptionAlert(baseContext, getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
        } else if (getIntent().hasExtra("artist")) {
            UserModel.getInstance().openFragment = "Browse";
            Utility.showSubscriptionAlert(baseContext, getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
        }

        if (Utility.isServiceRunning(SongPlayService.class.getName(), MainActivity.this)) {
            if (Constants.isPlay) {
                Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Integer i[] = (Integer[]) msg.obj;
                        if (i[2] != 0) {
                            binding.seek.setProgress(i[2]);
                        }
                        if (binding.seek.getProgress() == 1) {
                            binding.loadingMusic.setVisibility(GONE);
                            binding.imgPlay.setVisibility(View.VISIBLE);
                        }
                    }
                };
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserModel.getInstance().getAppSetting(MainActivity.this);
        try {
            String current_date = "";
            if (Utility.getUserSetting(baseContext) != null){
                current_date = Utility.getUserSetting(baseContext).getCurrentTime();
            }else {
                current_date = Utility.getCurrentTimeStamp();
            }
            if (Utility.getUserInfo(baseContext).subscribePlan.trailEndDate != null) {
                if (Utility.getUserInfo(baseContext).isTrialTaken != null) {
                    if (Utility.getUserInfo(baseContext).getTrialTokan() == 1) {
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

        Runtime.getRuntime().gc();
        UserModel.getInstance().getGenreData(MainActivity.this, null);
        openReloadFragment();

        //AKM
        if(Utility.isConnectingToInternetWithoutAlert(MainActivity.this))
            UserModel.getInstance().getdata(baseContext);

        Constants.setLoggedUser(true, preference.getString("userid", ""), Utility.getUserInfo(baseContext).deviceID);
        updateUI();
        if (Utility.isServiceRunning(SongPlayService.class.getName(), MainActivity.this)) {
            try {

                if (Constants.isSongPlay) {
                    binding.seek.setVisibility(View.VISIBLE);
                    binding.seekRadio.setVisibility(GONE);
                    Song data = UserModel.getInstance().currentPlaySong;
                    if (data != null) {
                        if (Utility.getStringPreferences(baseContext, Utility.preferencesLanguage).matches("iw")) {
                            if (data.title_hebrew == null || data.title_hebrew.equalsIgnoreCase("")) {
                                songString = data.title;
                            } else {
                                songString = data.title_hebrew;
                            }
                            if (data.artist != null) {
                                if (data.artist.nameHebrew == null || data.artist.nameHebrew.equalsIgnoreCase("")) {
                                    albumString = data.artist.name;
                                } else {
                                    albumString = data.artist.nameHebrew;
                                }
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        } else {
                            songString = data.title;
                            if (data.artist != null) {
                                albumString = data.artist.name;
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        }
                    }
                } else {//AKM: if in buffer state user leave the application then
                    /*if (binding.player.getVisibility() != View.VISIBLE) {
                        Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                        binding.player.startAnimation(bottomUp);
                        binding.player.setVisibility(View.VISIBLE);
                        binding.seekBar.startAnimation(bottomUp);
                        binding.seekBar.setVisibility(View.VISIBLE);

                        if(UserModel.getInstance().isSongLoading){
                            binding.imgPlay.setVisibility(GONE);
                            binding.loadingMusic.setVisibility(View.VISIBLE);
                        }
                    }*/
                    binding.seek.setVisibility(GONE);
                    binding.seekRadio.setVisibility(View.VISIBLE);
                    binding.seekRadio.setEnabled(false);
                    if (Constants.StationList.size() > 0) {
                        if (Utility.getStringPreferences(baseContext, Utility.preferencesLanguage).matches("iw")) {
                            if (Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew == null && Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew.equalsIgnoreCase("")) {
                                binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                            } else {
                                binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                            }
                        } else {
                            binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                        }
                    }
                }
                if (Constants.isPlay) {
                    binding.imgPlay.setImageResource(R.drawable.icon_pause);
                } else {
                    binding.imgPlay.setImageResource(R.drawable.play);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String langString = Utility.getStringPreferences(MainActivity.this, Utility.preferencesLanguage);
        if (langString.equalsIgnoreCase("en")) {
            Utility.setStringPreferences(MainActivity.this, Utility.preferencesLanguage, "en");
        }

        if (Constants.isSongPlay) {
            if (Constants.SONGS_LIST != null) {
                if (Constants.SONGS_LIST.size() > 0) {
                    if (Constants.isPlay) {
                        if (binding.player.getVisibility() != View.VISIBLE) {
                            Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                            binding.player.startAnimation(bottomUp);
                            binding.player.setVisibility(View.VISIBLE);
                            binding.seekBar.startAnimation(bottomUp);
                            binding.seekBar.setVisibility(View.VISIBLE);
                        } else {
                            binding.loadingMusic.setVisibility(GONE);
                            binding.imgPlay.setVisibility(View.VISIBLE);
                            binding.imgPlay.setImageResource(R.drawable.icon_pause);
                        }
                    } else {
                        if (UserModel.getInstance().songPlayerDuration > 0){
                            binding.seek.setProgress(UserModel.getInstance().songPlayerDuration);
                        }
                        binding.loadingMusic.setVisibility(GONE);
                        binding.imgPlay.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            if (Constants.StationList != null) {
                if (Constants.StationList.size() > 0) {
                    if (Constants.isPlay) {
                        if (binding.player.getVisibility() != View.VISIBLE) {
                            Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                            binding.player.startAnimation(bottomUp);
                            binding.player.setVisibility(View.VISIBLE);
                        }
                    }
                    if (Constants.isPlay) {
                        binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    } else {
                        binding.imgPlay.setImageResource(R.drawable.play);
                    }


                }
            }
        }

        // AKM
        if(Constants.isHomeScreenPlayerVisible){
            if (binding.player.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                binding.player.startAnimation(bottomUp);
                binding.player.setVisibility(View.VISIBLE);
            }
        }

        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                UserModel.getInstance().isPlaySongAfterAd = false;
                if (Constants.isChangeSong) {
                    if (!Utility.getBooleaPreferences(baseContext,"ad_in_background")) {
                        Constants.isChangeSong = false;
                        Controls.nextControl(baseContext);
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
        }


        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(new_playlist);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(new_playlist, new IntentFilter("new_playlist"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(replace_fragment);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(replace_fragment, new IntentFilter("replace_fragment"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(back_to_home);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(back_to_home, new IntentFilter("back_to_home"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(music_palyer);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(music_palyer, new IntentFilter("music_player"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(open_my_music_tab);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(open_my_music_tab, new IntentFilter("open_my_music_tab"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(update_playerLayout);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(update_playerLayout, new IntentFilter("update_playerLayout"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(play_pause_control);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(play_pause_control, new IntentFilter("play_pause_control"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(open_browse);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(open_browse, new IntentFilter("open_browse"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(show_loader);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(show_loader, new IntentFilter("show_loader"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(update_player_purple);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(update_player_purple, new IntentFilter("update_player_purple"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(change_fragment);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(change_fragment, new IntentFilter("change_fragment"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(play_station);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(play_station, new IntentFilter("play_station"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(switch_off_player);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(switch_off_player, new IntentFilter("switch_off_player"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(stop_player);
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(stop_player, new IntentFilter("stop_player"));

    }

    //Notified current playing song play pause icon and seekbar on play/pause
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekBar();
        }
    };

    //Update seek bar
    private void updateSeekBar() {
        Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Integer i[] = (Integer[]) msg.obj;
                if (i[2] != 0) {
                    binding.seek.setProgress(i[2]);
                }
            }
        };
    }

    //Update UI
    private void updateUI() {
        try {
            if (Constants.isPlay) {
                binding.seek.setVisibility(View.VISIBLE);
                binding.seekRadio.setVisibility(GONE);
                Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Integer i[] = (Integer[]) msg.obj;
                        if (i[2] != 0) {
                            binding.seek.setProgress(i[2]);
                        }
                    }
                };
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
            } else {
                binding.imgPlay.setImageResource(R.drawable.play);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.imgSetting) {
            mixpanel.track(current_fragment + ":Settings");
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        } else if (v == binding.first) {
            current_fragment = getResources().getString(R.string.browsee);
            if (currentTab.equalsIgnoreCase(CustomTab.BROWSE.toString())) {
            } else {
                if (!isFirstTimeBrowse) {
                    openFragment(CustomTab.BROWSE.toString());
                    isFirstTimeBrowse = true;
                } else {
                    UserModel.getInstance().openFragment = CustomTab.BROWSE.toString();
                    if (listOfFragmentList.get(0).equalsIgnoreCase(CustomTab.BROWSE.toString())) {
                        changeBottom(CustomTab.BROWSE.toString());
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.show(listOfFragment.get(0));
                        if (listOfFragment.get(1) != null) {
                            ft.hide(listOfFragment.get(1));
                        }
                        if (listOfFragment.get(2) != null) {
                            ft.hide(listOfFragment.get(2));
                        }
                        if (listOfFragment.get(3) != null) {
                            ft.hide(listOfFragment.get(3));
                        }
                        if (listOfFragment.get(4) != null) {
                            ft.hide(listOfFragment.get(4));
                        }
                        int currentAPIVersion = Build.VERSION.SDK_INT;
                        try {
                            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                                ft.commitNow();
                            } else {
                                ft.commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        openFragment(CustomTab.BROWSE.toString());
                    }
                }
            }
        } else if (v == binding.second) {
            current_fragment = getResources().getString(R.string.Recent);
            mixpanel.track("Tap-on-Recents");
            if (currentTab.equalsIgnoreCase(CustomTab.RECENT.toString())) {
            } else {
                if (!isFirstTimeRecent) {
                    openFragment(CustomTab.RECENT.toString());
                    isFirstTimeRecent = true;
                } else {
                    UserModel.getInstance().openFragment = CustomTab.RECENT.toString();
                    if (listOfFragmentList.get(1).equalsIgnoreCase(CustomTab.RECENT.toString())) {
                        changeBottom(CustomTab.RECENT.toString());
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.show(listOfFragment.get(1));
                        if (listOfFragment.get(0) != null) {
                            ft.hide(listOfFragment.get(0));
                        }
                        if (listOfFragment.get(2) != null) {
                            ft.hide(listOfFragment.get(2));
                        }
                        if (listOfFragment.get(3) != null) {
                            ft.hide(listOfFragment.get(3));
                        }
                        if (listOfFragment.get(4) != null) {
                            ft.hide(listOfFragment.get(4));
                        }
                        int currentAPIVersion = Build.VERSION.SDK_INT;
                        try {
                            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                                ft.commitNow();
                            } else {
                                ft.commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        openFragment(CustomTab.RECENT.toString());
                    }
                }
            }
        } else if (v == binding.third) {
            fragment = new PlayFragment();
            binding.second.setAlpha(0.4f);
            binding.first.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v == binding.fourth) {
            UserModel.getInstance().openFragment = CustomTab.SEARCH.toString();
            if (!isFirstTimeSearch) {
                openFragment(CustomTab.SEARCH.toString());
                isFirstTimeSearch = true;
            } else {
                if (listOfFragmentList.get(2).equalsIgnoreCase(CustomTab.SEARCH.toString())) {
                    changeBottom(CustomTab.SEARCH.toString());
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.show(listOfFragment.get(2));
                    if (listOfFragment.get(0) != null) {
                        ft.hide(listOfFragment.get(0));
                    }
                    if (listOfFragment.get(1) != null) {
                        ft.hide(listOfFragment.get(1));
                    }
                    if (listOfFragment.get(3) != null) {
                        ft.hide(listOfFragment.get(3));
                    }
                    if (listOfFragment.get(4) != null) {
                        ft.hide(listOfFragment.get(4));
                    }
                    int currentAPIVersion = Build.VERSION.SDK_INT;
                    try {
                        if (currentAPIVersion >= Build.VERSION_CODES.M) {
                            ft.commitNow();
                        } else {
                            ft.commit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    openFragment(CustomTab.SEARCH.toString());
                }
            }
        } else if (v == binding.fifth) {
            current_fragment = getResources().getString(R.string.Mymusic);
            if (currentTab.equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
            } else {
                UserModel.getInstance().openFragment = CustomTab.MYMUSIC.toString();
                if (!isFirstTimeMusic) {
                    openFragment(CustomTab.MYMUSIC.toString());
                    isFirstTimeMusic = true;
                } else {
                    if (listOfFragmentList.get(3).equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
                        changeBottom(CustomTab.MYMUSIC.toString());
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.show(listOfFragment.get(3));
                        if (listOfFragment.get(0) != null) {
                            ft.hide(listOfFragment.get(0));
                        }
                        if (listOfFragment.get(2) != null) {
                            ft.hide(listOfFragment.get(2));
                        }
                        if (listOfFragment.get(1) != null) {
                            ft.hide(listOfFragment.get(1));
                        }
                        if (listOfFragment.get(4) != null) {
                            ft.hide(listOfFragment.get(4));
                        }
                        int currentAPIVersion = Build.VERSION.SDK_INT;
                        try {
                            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                                ft.commitNow();
                            } else {
                                ft.commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        openFragment(CustomTab.SEARCH.toString());
                    }
                }
            }
        } else if (v == binding.openMusic) {
            Song data;
            currentTab = CustomTab.MUSIC.toString();
            if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size() && Constants.SONGS_LIST.size() > 0) {
                data = Constants.SONGS_LIST.get(0);
            } else {
                if (Constants.SONGS_LIST.size() > 0) {
                    data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                } else {
                    return;
                }
            }
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            UserModel.getInstance().currentPlaySong = data;
            startActivity(intent);
        } else if (v == binding.lytPlay) {
            if (Constants.isSongPlay) {
                Song data;
                currentTab = CustomTab.MUSIC.toString();
                if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size() && Constants.SONGS_LIST.size() > 0) {
                    data = Constants.SONGS_LIST.get(0);
                } else {
                    if (Constants.SONGS_LIST.size() > 0) {
                        data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                    } else {
                        return;
                    }
                }
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                UserModel.getInstance().currentPlaySong = data;
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        } else if (v == binding.imgPlay) {
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(new Intent("refreshList"));
            if (Constants.isSongPlay) {
                if (Constants.SONGS_LIST.size() > 0) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(baseContext);
                        //binding.imgPlay.setImageResource(R.drawable.play);
                    } else {
                        Controls.playControl(baseContext);
                        //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    }
                }
            } else {
                if (Constants.isPlay) {
                    Controls.pauseControl(baseContext);
                    //binding.imgPlay.setImageResource(R.drawable.play);
                } else {
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utility.isServiceRunning(SongPlayService.class.getName(), MainActivity.this)) {
            Intent playIntent = new Intent(this, SongPlayService.class);
            startService(playIntent);
        }
        if (!Utility.isServiceRunning(PlayService.class.getName(), MainActivity.this)) {
            Intent playerIntent = new Intent(this, PlayService.class);
            startService(playerIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentTab.equalsIgnoreCase(CustomTab.RECENT.toString()) || currentTab.equalsIgnoreCase(CustomTab.SEARCH.toString()) || currentTab.equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
            UserModel.getInstance().openFragment = CustomTab.BROWSE.toString();
            openReloadFragment();
        } else {
            if (System.currentTimeMillis() - onRecentBackPressedTime > 2000) {
                onRecentBackPressedTime = System.currentTimeMillis();
                Snackbar.make(binding.lyt, getResources().getString(R.string.press_again_to_exit), Snackbar.LENGTH_SHORT).show();
                return;
            }
            Utility.setBooleanPreferences(baseContext, "finish_main", true);
            // AKM Radio failed > remove app > start app > player display (It should not be )
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* --- KIPL -> AKM : Bottom player visibility manage ---*/
        Constants.isHomeScreenPlayerVisible = binding.player.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //Constants.isHomeScreenPlayerVisible = false;
            mixpanel.flush();
            if (UserModel.getInstance().isSocketOpen) {
                if (app.isConnected) {
                    app.disconnectSocket();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //For play share song
    private void getSongDetail(final Context context, String id) {
        /*Constants.setLoggedUser(true, preferences.getString("userid", ""), Utility.getUserInfo(MainActivity.this).deviceID);*/
        Call<SongDetail> call = Constants.service.getSongDetail(id);
        call.enqueue(new Callback<SongDetail>() {
            @Override
            public void onResponse(Call<SongDetail> call, Response<SongDetail> response) {
                if (response.isSuccessful()) {
                    SongDetail songDetail = response.body();
                    if (songDetail != null && songDetail.message != null) {
                        if (songDetail.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (songDetail.success) {
                                Album tempAlbum = new Album();
                                if (songDetail.songs.albums != null) {
                                    if (songDetail.songs.albums.size() > 0) {
                                        tempAlbum = songDetail.songs.albums.get(0);
                                    }
                                }
                                if (songDetail.songs.isPremium || songDetail.songs.artist.isPremium || tempAlbum.isPremium) {
                                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        Constants.isSongPlay = true;
                                        UserModel.getInstance().isSingleSongPlay = true;
                                        openFragment(CustomTab.BROWSE.toString());
                                        ArrayList<Song> list = new ArrayList<>();
                                        list.add(songDetail.songs);
                                        UserModel.getInstance().listOfShuffleSong.clear();
                                        UserModel.getInstance().listOfShuffleSong.addAll(list);
                                        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                                        UserModel.getInstance().listOfActualSong.clear();
                                        UserModel.getInstance().listOfActualSong.addAll(list);
                                        Constants.SONGS_LIST = list;
                                        Constants.SONG_NUMBER = 0;
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                            }
                                        }, 00);
                                        Intent pintent = new Intent("update_playerLayout");
                                        pintent.putExtra("message", "update_playerLayout");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(pintent);
                                        Intent intent = new Intent("music_player");
                                        intent.putExtra("message", "music_player");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    } else {
                                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                        UserModel.getInstance().openFragment = "Browse";
                                        openReloadFragment();
                                    }
                                } else {
                                    Constants.isSongPlay = true;
                                    UserModel.getInstance().isSingleSongPlay = true;
                                    openFragment(CustomTab.BROWSE.toString());
                                    ArrayList<Song> list = new ArrayList<>();
                                    list.add(songDetail.songs);
                                    UserModel.getInstance().listOfShuffleSong.clear();
                                    UserModel.getInstance().listOfShuffleSong.addAll(list);
                                    Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                                    UserModel.getInstance().listOfActualSong.clear();
                                    UserModel.getInstance().listOfActualSong.addAll(list);
                                    Constants.SONGS_LIST = list;
                                    Constants.SONG_NUMBER = 0;
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                        }
                                    }, 00);
                                    Intent pintent = new Intent("update_playerLayout");
                                    pintent.putExtra("message", "update_playerLayout");
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(pintent);
                                    Intent intent = new Intent("music_player");
                                    intent.putExtra("message", "music_player");
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SongDetail> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void openFragment(String tab) {
        if (tab.equalsIgnoreCase(CustomTab.BROWSE.toString())) {
            currentTab = CustomTab.BROWSE.toString();
            binding.Header.setText(getResources().getString(R.string.browse));
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.first.setAlpha(1);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (UserModel.getInstance().comeFromDeep) {
                if (fm.findFragmentByTag("Browse") != null) {
                    ft.remove(fm.findFragmentByTag("Browse"));
                }
                if (fm.findFragmentByTag("Recent") != null) {
                    ft.remove(fm.findFragmentByTag("Recent"));
                }
                if (fm.findFragmentByTag("Search") != null) {
                    ft.remove(fm.findFragmentByTag("Search"));
                }
                if (fm.findFragmentByTag("MyMusic") != null) {
                    ft.remove(fm.findFragmentByTag("Search"));
                }
            } else {
                UserModel.getInstance().comeFromDeep = true;
            }
            fragment = new NewBrowseFragment();
            ft.add(R.id.container, fragment, "Browse");
            if (listOfFragment.get(1) != null) {
                ft.hide(listOfFragment.get(1));
            }
            if (listOfFragment.get(2) != null) {
                ft.hide(listOfFragment.get(2));
            }
            if (listOfFragment.get(3) != null) {
                ft.hide(listOfFragment.get(3));
            }
            if (listOfFragment.get(4) != null) {
                ft.hide(listOfFragment.get(4));
            }
            listOfFragment.set(0, fragment);
            listOfFragmentList.set(0, CustomTab.BROWSE.toString());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tab.equalsIgnoreCase(CustomTab.RECENT.toString())) {
            currentTab = CustomTab.RECENT.toString();
            binding.Header.setText(R.string.RECENT);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.first.setAlpha(0.4f);
            binding.second.setAlpha(1);
            binding.third.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Recent") != null) {
                ft.remove(fm.findFragmentByTag("Recent"));
            }
            fragment = new RecentFragment();
            ft.add(R.id.container, fragment, "Recent");
            if (listOfFragment.get(0) != null) {
                ft.hide(listOfFragment.get(0));
            }
            if (listOfFragment.get(2) != null) {
                ft.hide(listOfFragment.get(2));
            }
            if (listOfFragment.get(3) != null) {
                ft.hide(listOfFragment.get(3));
            }
            if (listOfFragment.get(4) != null) {
                ft.hide(listOfFragment.get(4));
            }
            listOfFragment.set(1, fragment);
            listOfFragmentList.set(1, CustomTab.RECENT.toString());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tab.equalsIgnoreCase(CustomTab.SEARCH.toString())) {
            current_fragment = getResources().getString(R.string.Search);
            mixpanel.track("Tap on Search button");
            currentTab = CustomTab.SEARCH.toString();
            binding.Header.setText(R.string.SEARCH);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(GONE);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.first.setAlpha(0.4f);
            binding.fourth.setAlpha(1);
            binding.fifth.setAlpha(0.4f);
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Search") != null) {
                ft.remove(fm.findFragmentByTag("Search"));
            }
            fragment = new SearchFragment();
            ft.add(R.id.container, fragment, "Search");
            if (listOfFragment.get(0) != null) {
                ft.hide(listOfFragment.get(0));
            }
            if (listOfFragment.get(1) != null) {
                ft.hide(listOfFragment.get(1));
            }
            if (listOfFragment.get(3) != null) {
                ft.hide(listOfFragment.get(3));
            }
            if (listOfFragment.get(4) != null) {
                ft.hide(listOfFragment.get(4));
            }
            listOfFragment.set(2, fragment);
            listOfFragmentList.set(2, CustomTab.SEARCH.toString());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            currentTab = CustomTab.MYMUSIC.toString();
            fragment = new MyMusicFragment();
            binding.Header.setText(R.string.MYMUSIC);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(1);
            binding.first.setAlpha(0.4f);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("MyMusic") != null) {
                ft.remove(fm.findFragmentByTag("MyMusic"));
            }
            fragment = new MyMusicFragment();
            ft.add(R.id.container, fragment, "MyMusic");
            if (listOfFragment.get(0) != null) {
                ft.hide(listOfFragment.get(0));
            }
            if (listOfFragment.get(1) != null) {
                ft.hide(listOfFragment.get(1));
            }
            if (listOfFragment.get(2) != null) {
                ft.hide(listOfFragment.get(2));
            }
            if (listOfFragment.get(4) != null) {
                ft.hide(listOfFragment.get(4));
            }
            listOfFragment.set(3, fragment);
            listOfFragmentList.set(3, CustomTab.MYMUSIC.toString());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void changeBottom(String tab) {
        if (tab.equalsIgnoreCase(CustomTab.BROWSE.toString())) {
            currentTab = CustomTab.BROWSE.toString();
            binding.Header.setText(getResources().getString(R.string.browse));
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.first.setAlpha(1);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
        } else if (tab.equalsIgnoreCase(CustomTab.RECENT.toString())) {
            currentTab = CustomTab.RECENT.toString();
            binding.Header.setText(R.string.RECENT);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.first.setAlpha(0.4f);
            binding.second.setAlpha(1);
            binding.third.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
        } else if (tab.equalsIgnoreCase(CustomTab.SEARCH.toString())) {
            current_fragment = getResources().getString(R.string.Search);
            mixpanel.track("Tap on Search button");
            currentTab = CustomTab.SEARCH.toString();
            binding.Header.setText(R.string.SEARCH);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(GONE);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.first.setAlpha(0.4f);
            binding.fourth.setAlpha(1);
            binding.fifth.setAlpha(0.4f);
        } else {
            currentTab = CustomTab.MYMUSIC.toString();
            fragment = new MyMusicFragment();
            binding.Header.setText(R.string.MYMUSIC);
            binding.second.setAlpha(0.4f);
            binding.third.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(1);
            binding.first.setAlpha(0.4f);
            binding.imgSetting.setVisibility(View.VISIBLE);
            binding.toolbar.setVisibility(View.VISIBLE);
        }
    }

    public void openReloadFragment() {
        if (UserModel.getInstance().openFragment.equalsIgnoreCase(CustomTab.BROWSE.toString())) {
            if (listOfFragment.get(0) == null) {
                openFragment(CustomTab.BROWSE.toString());
                isFirstTimeBrowse = true;
            } else {
                changeBottom(CustomTab.BROWSE.toString());
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(listOfFragment.get(0));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if (listOfFragment.get(1) != null) {
                    ft.hide(listOfFragment.get(1));
                }
                if (listOfFragment.get(2) != null) {
                    ft.hide(listOfFragment.get(2));
                }
                if (listOfFragment.get(3) != null) {
                    ft.hide(listOfFragment.get(3));
                }
                if (listOfFragment.get(4) != null) {
                    ft.hide(listOfFragment.get(4));
                }
                int currentAPIVersion = Build.VERSION.SDK_INT;
                try {
                    if (currentAPIVersion >= Build.VERSION_CODES.M) {
                        ft.commitNow();
                    } else {
                        ft.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (UserModel.getInstance().openFragment.equalsIgnoreCase(CustomTab.RECENT.toString())) {
            if (listOfFragment.get(1) == null) {
                openFragment(CustomTab.RECENT.toString());
                isFirstTimeRecent = true;
            } else {
                changeBottom(CustomTab.RECENT.toString());
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(listOfFragment.get(1));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if (listOfFragmentList.get(0) != null) {
                    ft.hide(listOfFragment.get(0));
                }
                if (listOfFragmentList.get(2) != null) {
                    ft.hide(listOfFragment.get(2));
                }
                if (listOfFragmentList.get(3) != null) {
                    ft.hide(listOfFragment.get(3));
                }
                if (listOfFragment.get(4) != null) {
                    ft.hide(listOfFragment.get(4));
                }
                int currentAPIVersion = Build.VERSION.SDK_INT;
                try {
                    if (currentAPIVersion >= Build.VERSION_CODES.M) {
                        ft.commitNow();
                    } else {
                        ft.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (UserModel.getInstance().openFragment.equalsIgnoreCase(CustomTab.SEARCH.toString())) {
            if (listOfFragment.get(2) == null) {
                openFragment(CustomTab.SEARCH.toString());
                isFirstTimeSearch = true;
            } else {
                changeBottom(CustomTab.SEARCH.toString());
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(listOfFragment.get(2));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if (listOfFragment.get(0) != null) {
                    ft.hide(listOfFragment.get(0));
                }
                if (listOfFragment.get(1) != null) {
                    ft.hide(listOfFragment.get(1));
                }
                if (listOfFragment.get(3) != null) {
                    ft.hide(listOfFragment.get(3));
                }
                if (listOfFragment.get(4) != null) {
                    ft.hide(listOfFragment.get(4));
                }
                int currentAPIVersion = Build.VERSION.SDK_INT;
                try {
                    if (currentAPIVersion >= Build.VERSION_CODES.M) {
                        ft.commitNow();
                    } else {
                        ft.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (UserModel.getInstance().openFragment.equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
            if (listOfFragment.get(3) == null) {
                openFragment(CustomTab.MYMUSIC.toString());
                isFirstTimeMusic = true;
            } else {
                changeBottom(CustomTab.MYMUSIC.toString());
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(listOfFragment.get(3));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if (listOfFragment.get(0) != null) {
                    ft.hide(listOfFragment.get(0));
                }
                if (listOfFragment.get(2) != null) {
                    ft.hide(listOfFragment.get(2));
                }
                if (listOfFragment.get(1) != null) {
                    ft.hide(listOfFragment.get(1));
                }
                if (listOfFragment.get(4) != null) {
                    ft.hide(listOfFragment.get(4));
                }
                int currentAPIVersion = Build.VERSION.SDK_INT;
                try {
                    if (currentAPIVersion >= Build.VERSION_CODES.M) {
                        ft.commitNow();
                    } else {
                        ft.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private enum CustomTab {
        BROWSE, RECENT, SEARCH, MYMUSIC, MUSIC
    }

    BroadcastReceiver update_player_purple = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Runtime.getRuntime().gc();
                        UserModel.getInstance().getGenreData(MainActivity.this, null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    BroadcastReceiver play_station = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        }
    };
    BroadcastReceiver new_playlist = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("isVisible", false)) {
                binding.toolbar.setVisibility(View.VISIBLE);
            } else {
                binding.toolbar.setVisibility(GONE);
            }
        }
    };
    BroadcastReceiver play_pause_control = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                if(intent.getStringExtra("action").equalsIgnoreCase("play")){
                    binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    binding.imgPlay.setVisibility(View.VISIBLE);
                    binding.loadingMusic.setVisibility(View.GONE);
                }
                else if(intent.getStringExtra("action").equalsIgnoreCase("pause")){
                    binding.imgPlay.setImageResource(R.drawable.play);
                    binding.imgPlay.setVisibility(View.VISIBLE);
                    binding.loadingMusic.setVisibility(View.GONE);
                }
            }
            /*if (Constants.isPlay) {
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
            } else {
                binding.imgPlay.setImageResource(R.drawable.play);
            }*/
        }
    };
    BroadcastReceiver show_loader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UserModel.getInstance().isSongLoading = true;
            binding.imgPlay.setVisibility(GONE);
            binding.loadingMusic.setVisibility(View.VISIBLE);
            binding.seekBar.setVisibility(View.VISIBLE);

            if(!intent.hasExtra("seek_percentage"))
                binding.seek.setProgress(0);

            if (Constants.isSongPlay) {
                binding.seek.setVisibility(View.VISIBLE);
                binding.seekRadio.setVisibility(GONE);
                //binding.seek.setProgress(0);
            } else {
                binding.seekRadio.setVisibility(View.VISIBLE);
                binding.seekRadio.setEnabled(false);
                binding.seek.setVisibility(GONE);
            }
        }
    };
    // Display player
    BroadcastReceiver music_palyer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("message").equalsIgnoreCase("music_player")) {
                if (binding.player.getVisibility() != View.VISIBLE) {
                    Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                    binding.player.startAnimation(bottomUp);
                    binding.player.setVisibility(View.VISIBLE);
                    binding.seekBar.startAnimation(bottomUp);
                    binding.seekBar.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    BroadcastReceiver update_playerLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UserModel.getInstance().isSongLoading = false;
            if (binding.player.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
                binding.player.startAnimation(bottomUp);
                binding.player.setVisibility(View.VISIBLE);
                binding.seekBar.startAnimation(bottomUp);
                binding.seekBar.setVisibility(View.VISIBLE);
            }

            if (Constants.isSongPlay) {
                binding.seek.setVisibility(View.VISIBLE);
                binding.seekRadio.setVisibility(GONE);
                Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Integer i[] = (Integer[]) msg.obj;
                        if (i[2] != 0) {
                            binding.seek.setProgress(i[2]);
                        }
                    }
                };
                if (Constants.SONGS_LIST.size() > 0) {
                    Song data;
                    if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size()) {
                        data = Constants.SONGS_LIST.get(0);
                    } else {
                        data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                    }
                    if (data != null) {
                        if (Utility.getStringPreferences(baseContext, Utility.preferencesLanguage).matches("iw")) {
                            if (data.title_hebrew == null || data.title_hebrew.equalsIgnoreCase("")) {
                                songString = data.title;
                            } else {
                                songString = data.title_hebrew;
                            }
                            if (data.artist != null /*&& data.albums.size() > 0*/) {
                                if (data.artist.nameHebrew == null || data.artist.nameHebrew.equalsIgnoreCase("")) {
                                    albumString = data.artist.name;
                                } else {
                                    albumString = data.artist.nameHebrew;
                                }
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        } else {
                            songString = data.title;
                            if (data.artist != null) {
                                albumString = data.artist.name;
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        }
                    }
                }
            } else {
                binding.loadingMusic.setVisibility(GONE);
                binding.seek.setVisibility(GONE);
                binding.seekRadio.setVisibility(View.VISIBLE);
                binding.seekRadio.setEnabled(false);
                if (Constants.StationList.size() > 0) {
                    if (Utility.getStringPreferences(baseContext, Utility.preferencesLanguage).matches("iw")) {
                        if (Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew == null && Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew.equalsIgnoreCase("")) {
                            binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                        } else {
                            binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                        }
                    } else {
                        binding.lblSongName.setText(Constants.StationList.get(Constants.SONG_NUMBER).title);
                    }
                }
            }
            binding.imgPlay.setVisibility(View.VISIBLE);
            binding.imgPlay.setImageResource(R.drawable.icon_pause);
            binding.loadingMusic.setVisibility(GONE);
        }
    };
    BroadcastReceiver open_my_music_tab = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!isFirstTimeMusic) {
                    openFragment(CustomTab.MYMUSIC.toString());
                    isFirstTimeMusic = true;
                } else {
                    if (listOfFragmentList.get(3).equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
                        changeBottom(CustomTab.MYMUSIC.toString());
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.show(listOfFragment.get(3));
                        if (listOfFragment.get(0) != null) {
                            ft.hide(listOfFragment.get(0));
                        }
                        if (listOfFragment.get(2) != null) {
                            ft.hide(listOfFragment.get(2));
                        }
                        if (listOfFragment.get(1) != null) {
                            ft.hide(listOfFragment.get(1));
                        }
                        if (listOfFragment.get(4) != null) {
                            ft.hide(listOfFragment.get(4));
                        }
                        int currentAPIVersion = Build.VERSION.SDK_INT;
                        try {
                            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                                ft.commitNow();
                            } else {
                                ft.commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        openFragment(CustomTab.MYMUSIC.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    BroadcastReceiver replace_fragment = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MySongFragment fragment = new MySongFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectsongs", "songs");
            fragment.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, fragment, "MyMusic");
            if (listOfFragment.get(3) != null) {
                ft.hide(listOfFragment.get(3));
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            listOfFragment.set(4, fragment);
            listOfFragmentList.set(4, "MySong");
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    BroadcastReceiver back_to_home = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFirstTimeMusic) {
                openFragment(CustomTab.MYMUSIC.toString());
                isFirstTimeMusic = true;
            } else {
                if (listOfFragmentList.get(3).equalsIgnoreCase(CustomTab.MYMUSIC.toString())) {
                    changeBottom(CustomTab.MYMUSIC.toString());
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.show(listOfFragment.get(3));
                    if (listOfFragment.get(0) != null) {
                        ft.hide(listOfFragment.get(0));
                    }
                    if (listOfFragment.get(2) != null) {
                        ft.hide(listOfFragment.get(2));
                    }
                    if (listOfFragment.get(1) != null) {
                        ft.hide(listOfFragment.get(1));
                    }
                    if (listOfFragment.get(4) != null) {
                        ft.hide(listOfFragment.get(4));
                    }
                    int currentAPIVersion = Build.VERSION.SDK_INT;
                    try {
                        if (currentAPIVersion >= Build.VERSION_CODES.M) {
                            ft.commitNow();
                        } else {
                            ft.commit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    openFragment(CustomTab.MYMUSIC.toString());
                }
            }
        }
    };
    BroadcastReceiver open_browse = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!isFirstTimeBrowse) {
                    openFragment(CustomTab.BROWSE.toString());
                    isFirstTimeBrowse = true;
                } else {
                    UserModel.getInstance().openFragment = CustomTab.BROWSE.toString();
                    if (listOfFragmentList.get(0).equalsIgnoreCase(CustomTab.BROWSE.toString())) {
                        changeBottom(CustomTab.BROWSE.toString());
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.show(listOfFragment.get(0));
                        if (listOfFragment.get(1) != null) {
                            ft.hide(listOfFragment.get(1));
                        }
                        if (listOfFragment.get(2) != null) {
                            ft.hide(listOfFragment.get(2));
                        }
                        if (listOfFragment.get(3) != null) {
                            ft.hide(listOfFragment.get(3));
                        }
                        int currentAPIVersion = Build.VERSION.SDK_INT;
                        try {
                            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                                ft.commitNow();
                            } else {
                                ft.commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        openFragment(CustomTab.BROWSE.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    BroadcastReceiver change_fragment = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            openReloadFragment();
        }
    };
    private BroadcastReceiver switch_off_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.player.setVisibility(GONE);
            binding.seek.setVisibility(GONE);
        }
    };

    private BroadcastReceiver stop_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.seek.setProgress(0);
            binding.imgPlay.setImageResource(R.drawable.play);
        }
    };

    public void playerVisibleManually(){
        if (binding.player.getVisibility() != View.VISIBLE) {
            Animation bottomUp = AnimationUtils.loadAnimation(baseContext, R.anim.show_from_bottom);
            binding.player.startAnimation(bottomUp);
            binding.player.setVisibility(View.VISIBLE);
            binding.seekBar.startAnimation(bottomUp);
            binding.seekBar.setVisibility(View.VISIBLE);


            if (Constants.isSongPlay) {
                binding.seek.setVisibility(View.VISIBLE);
                binding.seekRadio.setVisibility(GONE);
                if (Constants.SONGS_LIST.size() > 0) {
                    Song data;
                    if (Constants.SONG_NUMBER >= Constants.SONGS_LIST.size()) {
                        data = Constants.SONGS_LIST.get(0);
                    } else {
                        data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                    }
                    if (data != null) {
                        if (Utility.getStringPreferences(baseContext, Utility.preferencesLanguage).matches("iw")) {
                            if (data.title_hebrew == null || data.title_hebrew.equalsIgnoreCase("")) {
                                songString = data.title;
                            } else {
                                songString = data.title_hebrew;
                            }
                            if (data.artist != null) {
                                if (data.artist.nameHebrew == null || data.artist.nameHebrew.equalsIgnoreCase("")) {
                                    albumString = data.artist.name;
                                } else {
                                    albumString = data.artist.nameHebrew;
                                }
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        } else {
                            songString = data.title;
                            if (data.artist != null) {
                                albumString = data.artist.name;
                            }
                            binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                        }
                    }
                }
            }

        }

    }
}