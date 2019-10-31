package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.controls.StartSnap;
import com.shirali.databinding.ActivityRecommendedBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.NewRelease;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendedActivity extends BaseActivity {

    private ActivityRecommendedBinding binding;
    private Context context;
    private SongForUAdapter mAdapter;
    private RecentPlayedAdapter Adapter;
    private ArrayList<Song> listPrefSong;
    private ArrayList<Album> listRecentAlbum;
    private ArrayList<Song> listPlayed;
    private ArrayList<RelatedArtist> relatedArtist;
    private CustomLoaderDialog dialog;
    private CustomBottomTabView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recommended);

        context = this;
        setBottomView(context);
        dialog = new CustomLoaderDialog(context);


        if(Utility.isConnectingToInternet(context)) {
            if (!isFinishing()) {
                dialog.show();
            }
            getRecommandedSong();
        }
        listPrefSong = new ArrayList<>();
        listRecentAlbum = new ArrayList<>();
        listPlayed = new ArrayList<>();
        relatedArtist = new ArrayList<>();

        SnapHelper snapHelper = new StartSnap();

        snapHelper.attachToRecyclerView(binding.recycleviewAlbums);
        Adapter = new RecentPlayedAdapter(context, listRecentAlbum, listPlayed, relatedArtist, false, "album");
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        binding.recycleviewAlbums.setLayoutManager(LayoutManager);
        binding.recycleviewAlbums.hasFixedSize();
        binding.recycleviewAlbums.setAdapter(Adapter);

        mAdapter = new SongForUAdapter(context, listPrefSong, "recommended", Utility.getUserInfo(context).myMusic);
        //AKM:NEXT_LOGIC
        mAdapter.isAlbum();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewSfu.setLayoutManager(mLayoutManager);
        binding.recycleviewSfu.hasFixedSize();
        binding.recycleviewSfu.setNestedScrollingEnabled(false);
        binding.recycleviewSfu.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                playerView.setPlayerData(listPrefSong);
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
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

            }
        });

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeLyt.setRefreshing(false);
                UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                    @Override
                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                        if (isAdded) {
                            mAdapter.updateMyMusicList(myMusic);
                        }
                    }
                });
                if(Utility.isConnectingToInternet(context)) {
                    getRecommandedSong();
                    if (!((Activity) context).isFinishing()) {
                        dialog.show();
                    }
                }
            }
        });

        binding.lblSongViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(context, ViewAllActivity.class);
                intent3.putExtra("view", "recomm_song");
                startActivity(intent3);
            }
        });

        binding.lblAlbumViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(context, ViewAllActivity.class);
                intent1.putExtra("view", "recommanded_album");
                startActivity(intent1);
            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    //Get all recommended song and album
    public void getRecommandedSong() {
        Call<NewRelease> call = Constants.service.recommandation();
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    binding.lytRecomnded.setVisibility(View.VISIBLE);
                    binding.lytEmpty.setVisibility(View.GONE);
                    listPrefSong.clear();
                    listRecentAlbum.clear();
                    NewRelease release = response.body();
                    try {
                        if (release.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (release.success) {
                                if (release.songs.size() > 0 || release.albums.size() > 0) {
                                    binding.lytRecomnded.setVisibility(View.VISIBLE);
                                    binding.lytEmpty.setVisibility(View.GONE);
                                    if (release.songs.size() > 0) {
                                        listPrefSong.addAll(release.songs);
                                    } else {
                                        binding.lytSong.setVisibility(View.GONE);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    if (release.albums.size() > 0) {
                                        listRecentAlbum.addAll(release.albums);
                                    } else {
                                        binding.lytAlbum.setVisibility(View.GONE);
                                    }
                                    Adapter.notifyDataSetChanged();
                                    if (release.albumsCount > 10) {
                                        binding.lblAlbumViewAll.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.lblAlbumViewAll.setVisibility(View.GONE);
                                    }
                                    if (release.songsCount > 10) {
                                        binding.lblSongViewAll.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.lblSongViewAll.setVisibility(View.GONE);
                                    }
                                } else {
                                    binding.lytRecomnded.setVisibility(View.GONE);
                                    binding.lytEmpty.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<NewRelease> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        playerView.updateBottomView(UserModel.getInstance().openFragment);
        if (Constants.isSongPlay) {
            if (Constants.SONGS_LIST.size() > 0) {
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
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                if (Constants.isChangeSong) {
                    if (!Utility.getBooleaPreferences(context,"ad_in_background")) {
                        Constants.isChangeSong = false;
                        Controls.nextControl(context);
                        NewCampiagnActivity.isFromCampaign = false;
                    }
                } else {
                    //Controls.playControl(context);
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    NewCampiagnActivity.isFromCampaign = false;
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
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(context).registerReceiver(change_song, new IntentFilter("change_song"));

        // for update the list
        UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
            @Override
            public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                if (mAdapter != null) {
                    mAdapter.updateMyMusicList(myMusic);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void updateIcon() {
        if (binding.recycleviewSfu != null && binding.recycleviewSfu.getAdapter() != null)
            binding.recycleviewSfu.getAdapter().notifyDataSetChanged();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
            playerView.updateSeekBar();
        }
    };
    BroadcastReceiver change_song = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.isPageSelectedFromNextOfPrevious = false;
            playerView.setPlayerData(Constants.SONGS_LIST);
        }
    };
    BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                finish();
            }
        }
    };
}
