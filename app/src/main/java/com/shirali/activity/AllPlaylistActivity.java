package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.shirali.adapter.WeeklylistAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityAllPlaylistBinding;
import com.shirali.model.playlist.Playlist;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.GenresList;
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

public class AllPlaylistActivity extends BaseActivity {

    Context context;
    private ActivityAllPlaylistBinding binding;
    private WeeklylistAdapter mAdapter;
    private ArrayList<Shirali> listShirali;
    private CustomLoaderDialog dialog;
    private ArrayList<String> generes;
    private ArrayList<String> genresHebrew;
    private ArrayList<String> generesId;
    private ArrayList<String> listPlaylist;
    private MixpanelAPI mixpanelAPI;
    private FilterGenresAdapter fgAdapter;
    private boolean isFirstTime = true;
    private String currentGenres = "all";
    private GenresList genresList;
    private CustomBottomTabView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_playlist);

        context = this;
        setBottomView(context);
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        listPlaylist = new ArrayList<>();
        listShirali = new ArrayList<>();
        generes = new ArrayList<>();
        genresHebrew = new ArrayList<>();
        generesId = new ArrayList<>();

        listPlaylist = UserModel.getInstance().getplaylistList(context);

        dialog = new CustomLoaderDialog(context);
        if(Utility.isConnectingToInternet(context)) {
            if (!isFinishing()) {
                dialog.show();
            }
            getPlaylist(currentGenres);
        }



        genresList = Utility.getGenres(context);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                try {
                    if (genresList != null && genresList.genres != null) {
                        for (int i = 0; i < genresList.genres.size(); i++) {
                            generes.add(genresList.genres.get(i).title);
                            genresHebrew.add(genresList.genres.get(i).titleHebrew);
                            generesId.add(genresList.genres.get(i).id);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mAdapter = new WeeklylistAdapter(context, listShirali, listPlaylist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewWeekly.setLayoutManager(mLayoutManager);
        binding.recycleviewWeekly.hasFixedSize();
        binding.recycleviewWeekly.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new WeeklylistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listShirali.size() > 0) {
                    UserModel.getInstance().shirali = listShirali.get(position);
                }
                context.startActivity(new Intent(context, PlaylistActivity.class).putExtra("playlist", listShirali.get(position).id));
            }
        });

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeLyt.setRefreshing(false);
                if(Utility.isConnectingToInternet(context)) {
                    if (!((Activity) context).isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    getPlaylist(currentGenres);
                }
            }
        });


        binding.tvDsimiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSheet();
            }
        });


        binding.lytAllMoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.filterSheet.getVisibility() == View.VISIBLE) {
                    closeSheet();
                } else {
                    showTable();
                    binding.filterSheet.setVisibility(View.VISIBLE);
                    binding.mainScroll.setNestedScrollingEnabled(false);
                    Animation slide_down = AnimationUtils.loadAnimation(context, R.anim.animate_slide_down);
                    binding.filterSheet.startAnimation(slide_down);
                }
            }
        });

        binding.lblAllMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGenres = "all";
                getPlaylist(currentGenres);
                binding.lblTitlee.setText("All Mood & Genres");
                closeSheet();
            }
        });

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    //Open filter sheet
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
            public void onItemClick(View view, String value, int position) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                currentGenres = value;
                mixpanelAPI.track("Playlist:" + value);
                binding.lblTitlee.setText(value);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (value.equalsIgnoreCase(context.getResources().getString(R.string.all_genres))) {
                        getPlaylist("all");
                    } else {
                        if (generesId.size() > 0) {
                            getPlaylist(generesId.get(position - 1));
                        }
                    }
                } else {
                    if (value.equalsIgnoreCase("All Genres")) {
                        getPlaylist("all");
                    } else {
                        if (generesId.size() > 0) {
                            getPlaylist(generesId.get(position - 1));
                        }
                    }
                }
                closeSheet();
            }
        });
    }

    //close filter sheet
    private void closeSheet() {
        Animation slide_up = AnimationUtils.loadAnimation(context, R.anim.animate_slide_up);
        binding.filterSheet.startAnimation(slide_up);
        binding.lytAllMoods.setEnabled(true);
        binding.mainScroll.setNestedScrollingEnabled(true);
        binding.filterSheet.setVisibility(View.GONE);
        binding.table.removeAllViews();
    }

    // Get playlist according genres
    public void getPlaylist(final String genres) {
        Call<Playlist> call = Constants.service.getShiraliPlaylist(genres);
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                if (response.isSuccessful()) {
                    if (!((Activity) context).isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    listShirali.clear();
                    Playlist playlist = response.body();
                    try {
                        if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (playlist.success) {
                                if (playlist.playlists.size() > 0) {
                                    binding.lytAllMoods.setVisibility(View.VISIBLE);
                                    binding.mainScroll.setVisibility(View.VISIBLE);
                                    binding.lytEmpty.setVisibility(View.GONE);
                                    isFirstTime = false;
                                    binding.recycleviewWeekly.setVisibility(View.VISIBLE);
                                    if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                                        for (int i = 0; i < playlist.playlists.size(); i++) {
                                            if (playlist.playlists.get(i).songs.size() > 0) {
                                                listShirali.add(playlist.playlists.get(i));
                                            }
                                        }
                                        if (listShirali.size() > 0) {
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        listShirali.addAll(playlist.playlists);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    if (isFirstTime) {
                                        isFirstTime = false;
                                        binding.lytAllMoods.setVisibility(View.GONE);
                                        binding.mainScroll.setVisibility(View.GONE);
                                        binding.lytEmpty.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.recycleviewWeekly.setVisibility(View.GONE);
                                        Utility.showAlert(context, context.getResources().getString(R.string.there_are_no_song));
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
            public void onFailure(Call<Playlist> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (!((Activity) context).isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
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
        if (Constants.isPlay) {
            playerView.changePlayToPause();
        } else {
            playerView.changePlayToPause();
        }
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

        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
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

    private BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                finish();
            }
        }
    };
}
