package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.FilterGenresAdapter;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityNewReleasesBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.NewRelease;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.controls.StartSnap;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewReleasesActivity extends BaseActivity implements View.OnClickListener {

    private ActivityNewReleasesBinding binding;
    private RecentPlayedAdapter Adapter;
    private SongForUAdapter mAdapter;
    private StartSnap startSnapHelper;
    private ArrayList<Song> listPrefSong;
    private ArrayList<Album> listRecentAlbum;
    private ArrayList<Song> listPlayed;
    private ArrayList<RelatedArtist> relatedArtist;
    private CustomLoaderDialog dialog;
    private ArrayList<String> generes;
    private ArrayList<String> genresHebrew;
    private ArrayList<String> generesId;
    private boolean isFrom;
    private Context context;
    private MixpanelAPI mixpanelAPI;
    private FilterGenresAdapter fgAdapter;
    private boolean isFirstTime = true;
    private CustomBottomTabView playerView;
    private GenresList genresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_releases);
        context = this;
        setBottomView(context);
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        dialog = new CustomLoaderDialog(context);
        listPrefSong = new ArrayList<>();
        listRecentAlbum = new ArrayList<>();
        listPlayed = new ArrayList<>();
        relatedArtist = new ArrayList<>();
        genresHebrew = new ArrayList<>();
        generes = new ArrayList<>();
        generesId = new ArrayList<>();
        if (!((Activity) context).isFinishing()) {
            dialog.show();
        }
        genresList = Utility.getGenres(NewReleasesActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                if (genresList != null && genresList.genres != null) {
                    if (genresList.genres.size() > 0) {
                        for (int i = 0; i < genresList.genres.size(); i++) {
                            generes.add(genresList.genres.get(i).title);
                            generesId.add(genresList.genres.get(i).id);
                            genresHebrew.add(genresList.genres.get(i).titleHebrew);
                        }
                    }
                }
                isFrom = getIntent().getBooleanExtra("isPopular", false);
                if (isFrom) {
                    getPopularSong("all");
                    binding.lytNewlyAddedRecycle.setVisibility(View.GONE);
                    binding.lblNewSong.setVisibility(View.GONE);
                    binding.lblTopSong.setVisibility(View.VISIBLE);
                    binding.lblMostListnedSong.setVisibility(View.VISIBLE);
                    binding.titleText.setText(getResources().getString(R.string.popular));
                } else {
                    getNewReleaseSong("all");
                }
            }
        });

        startSnapHelper = new StartSnap();
        startSnapHelper.attachToRecyclerView(binding.recycleviewAlbums);
        Adapter = new RecentPlayedAdapter(this, listRecentAlbum, listPlayed, relatedArtist, false, "album");
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recycleviewAlbums.setLayoutManager(LayoutManager);
        binding.recycleviewAlbums.hasFixedSize();
        binding.recycleviewAlbums.setAdapter(Adapter);

        mAdapter = new SongForUAdapter(this, listPrefSong, "song", Utility.getUserInfo(context).myMusic);
        //AKM:NEXT_LOGIC
        mAdapter.isAlbum();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewSfu.setLayoutManager(mLayoutManager);
        binding.recycleviewSfu.hasFixedSize();
        binding.recycleviewSfu.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
                }
                Constants.SONGS_LIST = listPrefSong;
                Constants.SONG_NUMBER = position;
                playerView.setPlayerData(Constants.SONGS_LIST);
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                callAdsFirstBeforeMusicPlay();
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {

            }
        });

        binding.lblAlbumViewAll.setOnClickListener(this);
        binding.lblSongViewAll.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.tvDsimiss.setOnClickListener(this);
        binding.lytAllMoods.setOnClickListener(this);
        binding.lblAllMood.setOnClickListener(this);
        binding.btnHome.setOnClickListener(this);

        if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }


        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeLyt.setRefreshing(false);
                dialog.show();
                UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                    @Override
                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                        if (isAdded) {
                            mAdapter.updateMyMusicList(myMusic);
                        }
                    }
                });
                if (isFrom) {
                    getPopularSong("all");
                    binding.lblFilterName.setText(getResources().getString(R.string.all_genres));
                } else {
                    getNewReleaseSong("all");
                    binding.lblFilterName.setText(getResources().getString(R.string.all_genres));
                }
            }
        });
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

    //Set custom player and nav bar in bottom
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    //Get popular song according genres
    public void getPopularSong(String genre) {
        Call<NewRelease> call = Constants.service.popular(genre);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                listPrefSong.clear();
                listRecentAlbum.clear();
                NewRelease release = response.body();
                if (response.isSuccessful() && release != null) {
                    if (release.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(NewReleasesActivity.this);
                    } else {
                        if (release.success) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (release.songs.size() > 0) {
                                isFirstTime = false;
                                binding.lytAllMoods.setVisibility(View.VISIBLE);
                                binding.mainScroll.setVisibility(View.VISIBLE);
                                binding.lytEmpty.setVisibility(View.GONE);
                                if (release.songs.size() > 0) {
                                    binding.recycleviewSfu.setVisibility(View.VISIBLE);
                                    binding.lytSong.setVisibility(View.VISIBLE);
                                    listPrefSong.addAll(release.songs);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    binding.recycleviewSfu.setVisibility(View.GONE);
                                    binding.lytSong.setVisibility(View.GONE);
                                }

                                if (release.albums.size() > 0) {
                                    listRecentAlbum.addAll(release.albums);
                                    Adapter.notifyDataSetChanged();
                                } else {
                                    listRecentAlbum.clear();
                                    Adapter.notifyDataSetChanged();
                                }
                                if (release.songsCount > 10) {
                                    binding.lblSongViewAll.setVisibility(View.VISIBLE);
                                } else {
                                    binding.lblSongViewAll.setVisibility(View.GONE);
                                }
                                if (release.albumsCount > 10) {
                                    binding.lblAlbumViewAll.setVisibility(View.VISIBLE);
                                } else {
                                    binding.lblAlbumViewAll.setVisibility(View.GONE);
                                }
                            } else {
                                if (isFirstTime) {
                                    isFirstTime = false;
                                    binding.lytAllMoods.setVisibility(View.GONE);
                                    binding.mainScroll.setVisibility(View.GONE);
                                    binding.lytEmpty.setVisibility(View.VISIBLE);
                                } else {
                                    binding.mainScroll.setVisibility(View.GONE);
                                    Utility.showAlert(context, context.getResources().getString(R.string.no_data_found));
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<NewRelease> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    //Get new release song according genres
    public void getNewReleaseSong(String genre) {
        Call<NewRelease> call = Constants.service.newReleaseAlbum(genre);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                listPrefSong.clear();
                listRecentAlbum.clear();
                NewRelease release = response.body();
                try {
                    if (response.isSuccessful() && release != null) {
                        if (release.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(NewReleasesActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (release.success) {
                                if (release.songs.size() > 0 && release.albums.size() > 0) {
                                    isFirstTime = false;
                                    binding.lytAllMoods.setVisibility(View.VISIBLE);
                                    binding.mainScroll.setVisibility(View.VISIBLE);
                                    binding.lytEmpty.setVisibility(View.GONE);
                                    if (release.songs.size() > 0) {
                                        binding.recycleviewSfu.setVisibility(View.VISIBLE);
                                        binding.lytSong.setVisibility(View.VISIBLE);
                                        listPrefSong.addAll(release.songs);
                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        binding.recycleviewSfu.setVisibility(View.GONE);
                                        binding.lytSong.setVisibility(View.GONE);
                                    }
                                    if (release.albums.size() > 0) {
                                        binding.recycleviewAlbums.setVisibility(View.VISIBLE);
                                        binding.lytNewAlbum.setVisibility(View.VISIBLE);
                                        listRecentAlbum.addAll(release.albums);
                                        Adapter.notifyDataSetChanged();
                                    } else {
                                        binding.recycleviewAlbums.setVisibility(View.GONE);
                                        binding.lytNewAlbum.setVisibility(View.GONE);
                                    }
                                    if (release.songsCount > 10) {
                                        binding.lblSongViewAll.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.lblSongViewAll.setVisibility(View.GONE);
                                    }
                                    if (release.albumsCount > 10) {
                                        binding.lblAlbumViewAll.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.lblAlbumViewAll.setVisibility(View.GONE);
                                    }
                                } else {
                                    if (isFirstTime) {
                                        isFirstTime = false;
                                        binding.lytAllMoods.setVisibility(View.GONE);
                                        binding.mainScroll.setVisibility(View.GONE);
                                        binding.lytEmpty.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.mainScroll.setVisibility(View.GONE);
                                        Utility.showAlert(context, context.getResources().getString(R.string.no_data_found));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(Call<NewRelease> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
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
        LocalBroadcastManager.getInstance(context).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(context).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lblAlbumViewAll:
                Intent intentAlbum = new Intent(NewReleasesActivity.this, ViewAllActivity.class);
                intentAlbum.putExtra("view", "new_album");
                startActivity(intentAlbum);
                break;
            case R.id.lblSongViewAll:
                Intent intentSong = new Intent(NewReleasesActivity.this, ViewAllActivity.class);
                if (isFrom) {
                    intentSong.putExtra("view", "pop_songs");
                } else {
                    intentSong.putExtra("view", "new_songs");
                }
                startActivity(intentSong);
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.tv_dsimiss:
                closeSheet();
                break;
            case R.id.lytAllMoods:
                if (binding.filterSheet.getVisibility() == View.VISIBLE) {
                    closeSheet();
                } else {
                    if (isFrom) {
                        mixpanelAPI.track("Popular: Genre_Filter");
                    } else {
                        mixpanelAPI.track("New Releases: Genre_Filter");
                    }
                    showTable();
                    binding.filterSheet.setVisibility(View.VISIBLE);
                    binding.mainScroll.setNestedScrollingEnabled(false);
                    Animation slide_down = AnimationUtils.loadAnimation(NewReleasesActivity.this, R.anim.animate_slide_down);
                    binding.filterSheet.startAnimation(slide_down);
                }
                break;
            case R.id.lblAllMood:
                if (dialog != null) {
                    dialog.show();
                }
                if (isFrom) {
                    getPopularSong("all");
                } else {
                    getNewReleaseSong("all");
                }
                binding.lblAllMood.setText(getResources().getString(R.string.all_mood_amp_geners));
                closeSheet();
                break;
            case R.id.btnHome:
                finish();
                break;
        }
    }

    //Hide filter sheet
    private void closeSheet() {
        Animation slide_up = AnimationUtils.loadAnimation(NewReleasesActivity.this, R.anim.animate_slide_up);
        binding.filterSheet.startAnimation(slide_up);
        binding.lytAllMoods.setEnabled(true);
        binding.mainScroll.setNestedScrollingEnabled(true);
        binding.filterSheet.setVisibility(View.GONE);
        binding.table.removeAllViews();
    }

    //Show filter sheet
    private void showTable() {
        if (!generes.contains("All Genres")) {
            generes.add(0, "All Genres");
        }
        if (!genresHebrew.contains("כל הזאנרים")) {
            genresHebrew.add(0, "כל הזאנרים");
        }
        fgAdapter = new FilterGenresAdapter(context, generes, genresHebrew);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.listFilterGenres.setLayoutManager(manager);
        binding.listFilterGenres.hasFixedSize();
        binding.listFilterGenres.setAdapter(fgAdapter);
        fgAdapter.setOnItemClickListener(new FilterGenresAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final String value, final int position) {
                closeSheet();
                binding.filterSheet.setVisibility(View.GONE);
                mixpanelAPI.track("Playlist:" + value);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Runtime.getRuntime().gc();
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.show();
                            }
                        }
                        binding.lblFilterName.setText(value);
                        if (isFrom) {
                            mixpanelAPI.track("Popular:" + value);
                            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                                if (value.equalsIgnoreCase(context.getResources().getString(R.string.all_genres))) {
                                    getPopularSong("all");
                                } else {
                                    if (generesId.size() > 0) {
                                        if (generesId.size() > 0) {
                                            getPopularSong(generesId.get(position - 1));
                                        }
                                    }
                                }
                            } else {
                                if (value.equalsIgnoreCase(context.getResources().getString(R.string.all_genres))) {
                                    getPopularSong("all");
                                } else {
                                    if (generesId.size() > 0) {
                                        getPopularSong(generesId.get(position - 1));
                                    }
                                }
                            }
                        } else {
                            mixpanelAPI.track("New Releases:" + value);
                            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                                if (value.equalsIgnoreCase(context.getResources().getString(R.string.all_genres))) {
                                    getNewReleaseSong("all");
                                } else {
                                    if (generesId.size() > 0) {
                                        getNewReleaseSong(generesId.get(position - 1));
                                    }
                                }
                            } else {
                                if (value.equalsIgnoreCase(context.getResources().getString(R.string.all_genres))) {
                                    getNewReleaseSong("all");
                                } else {
                                    if (generesId.size() > 0) {
                                        getNewReleaseSong(generesId.get(position - 1));
                                    }
                                }
                            }
                        }
                    }
                }, 500);
            }
        });
    }

    public void updateIcon() {
        if (binding.recycleviewSfu != null && binding.recycleviewSfu.getAdapter() != null)
            binding.recycleviewSfu.getAdapter().notifyDataSetChanged();
    }


    BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                finish();
            }
        }
    };
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
}
