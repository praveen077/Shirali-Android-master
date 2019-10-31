package com.shirali.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.controls.StartSnap;
import com.shirali.databinding.ActivityAlbumDetailBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.mymusic.Album;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongsList;
import com.shirali.model.user.UserModel;
import com.shirali.service.PlayService;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityAlbumDetailBinding binding;
    private SongForUAdapter rpAdapter;
    private ArrayList<Song> listPrefSong;
    private Album album;
    private CustomLoaderDialog dialog;
    private ArrayList<String> song_list;
    private Context mContext;
    private String artist_name;
    private MixpanelAPI mixpanelAPI;
    private ArrayList<String> listSong;
    private int clickCount;
    private boolean isFromAlbum = false;
    private CustomBottomTabView playerView;
    private String[] artist = {};
    private int count = 0;
    private boolean isAllSongArePremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_album_detail);
        mContext = this;
        setBottomView(mContext);
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("View Album");
        dialog = new CustomLoaderDialog(mContext);
        if (!isFinishing()) {
            dialog.show();
        }
        song_list = new ArrayList<>();
        listPrefSong = new ArrayList<>();
        listSong = new ArrayList<>();
        artist = new String[2];

        try {
            album = UserModel.getInstance().album;
            if (getIntent().hasExtra("artist")) {
                artist_name = getIntent().getStringExtra("artist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getAlbumSongs(album.id);

        try {
            if (album.artwork != null) {
                if (album.isPremium) {
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        binding.imgTag.setVisibility(View.VISIBLE);
                        binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        binding.imgTag.setVisibility(View.VISIBLE);
                        binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                    }
                } else {
                    binding.imgTag.setVisibility(View.GONE);
                }
                Glide.with(AlbumDetailActivity.this).load(album.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
                    @Override
                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                        if (isAdded) {
                            rpAdapter.updateMyMusicList(myMusic);
                        }
                    }
                });
                getAlbumSongs(album.id);
                binding.swipeLyt.setRefreshing(false);
            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.imgMenu.setOnClickListener(this);
        binding.imgSuffle.setOnClickListener(this);
        binding.imgPlus.setOnClickListener(this);

        SnapHelper snapHelper = new StartSnap();
        snapHelper.attachToRecyclerView(binding.recyclerviewAlbumSongs);
        binding.recyclerviewAlbumSongs.setHasFixedSize(true);
        binding.recyclerviewAlbumSongs.setLayoutManager(new LinearLayoutManager(this));
        rpAdapter = new SongForUAdapter(this, listPrefSong, "AlbumDetail", Utility.getUserInfo(mContext).myMusic);
        rpAdapter.isAlbum();
        binding.recyclerviewAlbumSongs.setAdapter(rpAdapter);
        rpAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, final int position) {
                playerView.setPlayerData(listPrefSong);
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                    Constants.seekTo = 0;
                    Controls.seekToControl(mContext);
                }
                Constants.SONGS_LIST = listPrefSong;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                callAdsFirstBeforeMusicPlay();
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {
                if (isVisible) {
                    clickCount = clickCount + 1;
                    if (clickCount >= listPrefSong.size()) {
                        binding.imgPlus.setVisibility(View.GONE);
                    } else {
                        binding.imgPlus.setVisibility(View.VISIBLE);
                    }
                } else {
                    clickCount = clickCount - 1;
                    binding.imgPlus.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(mContext, MainActivity.class));
                    SplashActivity.isFromDeep = false;
                }
                finish();
                overridePendingTransition(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit);
            }
        });
        binding.imgPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong();
            }
        });

    }

    public void playSong(){
        try {
            UserModel.getInstance().isSingleSongPlay = false;
            Constants.isSongPlay = true;
            Artist artist = new Artist();
            if (UserModel.getInstance().artist_id != null) {
                artist = UserModel.getInstance().artist_id;
            }
            if (artist.isPremium) {
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    Controls.pauseControl(mContext);
                    playContinuousSong();
                } else {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                }
            } else {
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    Controls.pauseControl(mContext);
                    playContinuousSong();
                } else {
                    if (isAllSongArePremium) {
                        try {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Controls.pauseControl(mContext);
                        playContinuousSong();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            Intent playIntent = new Intent(this, SongPlayService.class);
            startService(playIntent);
        }
        if (!Utility.isServiceRunning(PlayService.class.getName(), mContext)) {
            Intent playerIntent = new Intent(this, PlayService.class);
            startService(playerIntent);
        }
    }

    BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                finish();
            }
        }
    };

    //Notified current playing song play pause icon and seekbar on play/pause
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
            playerView.updateSeekBar();
        }
    };

    //Open player view on click play full album from timer if lt is not show
    BroadcastReceiver change_song = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.isPageSelectedFromNextOfPrevious = false;
            playerView.setPlayerData(Constants.SONGS_LIST);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mixpanelAPI.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Set bottom nav bar and player view
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    //get song from album
    public void getAlbumSongs(String id) {
        Call<SongsList> call = Constants.service.getAlbumSong(id);
        call.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    SongsList list = response.body();
                    listPrefSong.clear();
                    try {
                        if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(AlbumDetailActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (list.success) {
                                if (list.songs.size() > 0) {
                                    count = 0;
                                    UserModel.getInstance().artist_id = list.songs.get(0).artist;
                                    for (int i = 0; i < list.songs.size(); i++) {
                                        if (list.songs.get(i).albums.size() > 0) {
                                            if (!getIntent().hasExtra("id")) {
                                                if (list.songs.get(i).albums.get(0).id.equalsIgnoreCase(album.id)) {
                                                    listPrefSong.add(list.songs.get(i));
                                                    song_list.add(list.songs.get(i).id);
                                                }
                                            } else {
                                                listPrefSong.add(list.songs.get(i));
                                            }
                                        }
                                        Album tempAlbum = new Album();
                                        if (list.songs.get(i).albums != null) {
                                            if (list.songs.get(i).albums.size() > 0) {
                                                tempAlbum = list.songs.get(i).albums.get(0);
                                            }
                                        }
                                        if (list.songs.get(i).artist.isPremium || list.songs.get(i).isPremium || tempAlbum.isPremium) {
                                            count = count + 1;
                                        }
                                    }
                                    rpAdapter.notifyDataSetChanged();
                                    for (int j = 0; j < song_list.size(); j++) {
                                        if (Utility.getUserInfo(mContext).myMusic.contains(song_list.get(j))) {
                                            clickCount = clickCount + 1;
                                        }
                                    }
                                    if (Utility.getUserInfo(mContext).myMusic.containsAll(song_list)) {
                                        binding.imgPlus.setVisibility(View.GONE);
                                    } else {
                                        binding.imgPlus.setVisibility(View.VISIBLE);
                                    }
                                    if (count >= list.songs.size()) {
                                        isAllSongArePremium = true;
                                    } else {
                                        isAllSongArePremium = false;
                                    }
                                    artist[0] = list.songs.get(0).artist.name;
                                    artist[1] = list.songs.get(0).artist.nameHebrew;
                                    updateTitle();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
                t.printStackTrace();
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    // For playing all song from list
    private void playContinuousSong() {
        Runtime.getRuntime().gc();
        UserModel.getInstance().listOfShuffleSong.clear();
        UserModel.getInstance().listOfShuffleSong.addAll(listPrefSong);
        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
        UserModel.getInstance().listOfActualSong.clear();
        UserModel.getInstance().listOfActualSong.addAll(listPrefSong);

        Constants.SONGS_LIST = listPrefSong;
        Constants.SONG_NUMBER = 0;
        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
        /*AKM*/
        //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        callAdsFirstBeforeMusicPlay();
        try {
            playerView.setPlayerData(Constants.SONGS_LIST);
            UserModel.getInstance().addInRecentWithType(mContext, Utility.getUserInfo(mContext).id, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).id, "album");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTitle() {
        if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).matches("iw")) {
            if (album.titleHebrew.equalsIgnoreCase("") || album.titleHebrew == null) {
                binding.albumTitle.setText(album.title);
            } else {
                binding.albumTitle.setText(album.titleHebrew);
            }
            if (UserModel.getInstance().artist_id != null) {
                if (UserModel.getInstance().artist_id.nameHebrew.equalsIgnoreCase("") || UserModel.getInstance().artist_id.nameHebrew == null) {
                    binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + UserModel.getInstance().artist_id.name);
                } else {
                    binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + UserModel.getInstance().artist_id.nameHebrew);
                }
            } else {
                if (artist[1].equalsIgnoreCase("") || artist[1] == null) {
                    binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + artist[0]);
                } else {
                    binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + artist[1]);
                }
            }
        } else {
            binding.albumTitle.setText(album.title);
            if (UserModel.getInstance().artist_id != null) {
                binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + UserModel.getInstance().artist_id.name);
            } else {
                binding.albumSubTitle.setText(mContext.getResources().getString(R.string.by) + " " + artist[0]);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (SplashActivity.isFromDeep) {
            UserModel.getInstance().openFragment = "BROWSE";
            startActivity(new Intent(mContext, MainActivity.class));
            SplashActivity.isFromDeep = false;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        playerView.updateBottomView(UserModel.getInstance().openFragment);
        if (Constants.isSongPlay) {
            if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0) {
                if(Constants.isHomeScreenPlayerVisible)
                    playerView.setPlayerData(Constants.SONGS_LIST);
            }
            playerView.updateSeekBar();
        } else {
            if (Constants.StationList.size() > 0) {
                if(Constants.isHomeScreenPlayerVisible)
                    playerView.setStationData(Constants.StationList);
            }
        }
        playerView.changePlayToPause();
        if (Utility.getBooleaPreferences(mContext, "suffle")) {
            Constants.shuffel = true;
            binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));
        } else {
            Constants.shuffel = false;
            binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
        }
        Utility.isConnectingToInternet(AlbumDetailActivity.this);
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                if (Constants.isChangeSong) {
                    if (!Utility.getBooleaPreferences(mContext,"ad_in_background")) {
                        Controls.nextControl(mContext);
                        Constants.isChangeSong = false;
                        NewCampiagnActivity.isFromCampaign = false;
                    }
                } else {
                    if (isFromAlbum) {
                        if (Constants.isPlay) {
                            Controls.pauseControl(mContext);
                            Constants.seekTo = 0;
                            Controls.seekToControl(mContext);
                        }
                        Constants.SONGS_LIST = listPrefSong;
                        Constants.SONG_NUMBER = Constants.songSelectionNumberAfterAd;
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                        isFromAlbum = false;
                    } else {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        NewCampiagnActivity.isFromCampaign = false;
                    }
                    NewCampiagnActivity.isFromCampaign = false;
                }
            } else {
                if (UserModel.getInstance().tempSongList.size() > 0) {
                    Constants.SONGS_LIST = UserModel.getInstance().tempSongList;
                    Constants.SONG_NUMBER = 0;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    Constants.song = Constants.SONGS_LIST.get(0).id;
                    UserModel.getInstance().isPlaySongAfterAd = true;
                }
            }
        }

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(finish_activity, new IntentFilter("finish_activity"));

        // for update the list
        UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
            @Override
            public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                if (rpAdapter != null) {
                    rpAdapter.updateMyMusicList(myMusic);
                    rpAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //For remove album song from my music if exist
    private void getSongAccordingArtist(final String id) {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(AlbumDetailActivity.this).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                MyMusic myMusic = response.body();
                try {
                    if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(AlbumDetailActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (myMusic.myMusicContain.song.size() > 0) {
                            for (int i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                                if (myMusic.myMusicContain.song.get(i).albums.size() > 0) {
                                    if (myMusic.myMusicContain.song.get(i).albums.get(0).id.equalsIgnoreCase(id))
                                        song_list.add(myMusic.myMusicContain.song.get(i).id);
                                }
                            }
                            addedToMyMusic(Utility.getUserInfo(mContext).myMusic, song_list, true);
                            binding.imgPlus.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    // find album song list which is not in my music
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
            Utility.showPopup(mContext, getString(R.string.album_removed));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(mContext, getString(R.string.album_added));
        }
    }

    // add selected song list into my music
    private void addSong(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", list);

        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    UserModel user = response.body();
                    try {
                        if (user.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (user.success) {
                                UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
                                    @Override
                                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                                        if (isAdded) {
                                            rpAdapter.updateMyMusicList(myMusic);
                                        }
                                    }
                                });
                                UserModel.getInstance().getdata(mContext);
                                listPrefSong.clear();
                                rpAdapter.notifyDataSetChanged();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Runtime.getRuntime().gc();
                                        getAlbumSongs(album.id);
                                    }
                                }, 2000);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    private void updateIcon() {
        if (binding.recyclerviewAlbumSongs != null && binding.recyclerviewAlbumSongs.getAdapter() != null)
            binding.recyclerviewAlbumSongs.getAdapter().notifyDataSetChanged();
    }

    //Bottom sheet menu
    private void openMenu() {
        final Dialog d = new BottomSheetDialog(AlbumDetailActivity.this);
        d.setContentView(R.layout.user_action_onsong_cell);
        ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
        TextView title = (TextView) d.findViewById(R.id.tvArtistName);
        View v = d.findViewById(R.id.bs);
        if (Utility.getStringPreferences(AlbumDetailActivity.this, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            if (album.titleHebrew == null || album.titleHebrew.equalsIgnoreCase("")) {
                title.setText(album.title);
            } else {
                title.setText(album.titleHebrew);
            }
        } else {
            title.setText(album.title);
        }
        try {
            Glide.with(AlbumDetailActivity.this).load(album.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((View) v.getParent()).setBackgroundColor(getApplicationContext().getResources().getColor(android.R.color.transparent));
        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareAlbum).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
        d.findViewById(R.id.lytViewArtist).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
        if (binding.imgPlus.getVisibility() == View.VISIBLE) {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
        } else {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
        }

        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    startActivity(new Intent(AlbumDetailActivity.this, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                } else {
                    Utility.showSubscriptionAlert(mContext, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (album.artist != null) {
                        if (album.artist.isPremium) {
                            if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                startActivity(new Intent(AlbumDetailActivity.this, ArtistDetailActivity.class).putExtra("artist_id", album.artist.id));
                            } else {
                                Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                            }
                        } else {
                            startActivity(new Intent(AlbumDetailActivity.this, ArtistDetailActivity.class).putExtra("artist_id", album.artist.id));
                        }
                    } else {
                        if (UserModel.getInstance().artist_id.isPremium) {
                            if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid")) {
                                startActivity(new Intent(AlbumDetailActivity.this, ArtistDetailActivity.class).putExtra("artist_id", UserModel.getInstance().artist_id.id));
                            } else {
                                Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                            }
                        } else {
                            startActivity(new Intent(AlbumDetailActivity.this, ArtistDetailActivity.class).putExtra("artist_id", UserModel.getInstance().artist_id.id));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytShareAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Utility.shareIt(AlbumDetailActivity.this, "album", album.title, UserModel.getInstance().artist_id.name, album.shareUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSongAccordingArtist(album.id);
                d.dismiss();
            }
        });
        d.setCancelable(true);
        d.show();
    }

    public void showSubscriptionAlert(final Context context, String msg) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.new_premium_popup_layout);
        TextView message = (TextView) openDialog.findViewById(R.id.lblMessage);
        TextView subscribe = (TextView) openDialog.findViewById(R.id.btnSubscribe);
        TextView not_now = (TextView) openDialog.findViewById(R.id.lblNotNow);
        message.setText(msg);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourSubscriptionActivity.class);
                intent.putExtra("month_subscription", Utility.getUserSetting(context).monthlySubscriptionPrice);
                intent.putExtra("year_subscription", Utility.getUserSetting(context).yearlySubscriptionPrice);
                context.startActivity(intent);
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        not_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    context.startActivity(new Intent(context, MainActivity.class));
                    SplashActivity.isFromDeep = false;
                }
                openDialog.dismiss();
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgSuffle:
                mixpanelAPI.track("");
                if (Constants.shuffel) {
                    Utility.setBooleanPreferences(mContext, "suffle", false);
                    Constants.shuffel = false;
                    binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
                } else {
                    Utility.setBooleanPreferences(mContext, "suffle", true);
                    Constants.shuffel = true;
                    Constants.repeat = false;
                    binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));

                    // AKM: Need to call Music player
                    playSong();
                }
                break;
            case R.id.imgMenu:
                openMenu();
                break;
            case R.id.imgPlus:
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(Utility.getUserInfo(AlbumDetailActivity.this).myMusic, song_list, false);
                    binding.imgPlus.setVisibility(View.GONE);
                } else {
                    Utility.showSubscriptionAlert(mContext, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
                break;
        }
    }
}
