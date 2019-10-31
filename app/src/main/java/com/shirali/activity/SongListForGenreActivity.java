package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivitySongListForGenreBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.NewRelease;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.CustomScrollListner;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongListForGenreActivity extends BaseActivity {

    private ActivitySongListForGenreBinding binding;
    private SongForUAdapter mAdapter;
    private ArrayList<Song> listPrefSong;
    private CustomLoaderDialog dialog;
    private Context mContext;
    private String title, titleHebrew;
    private boolean isLoading = false;
    private int page_number = 1;
    private boolean isLastPage = false;
    private LinearLayoutManager mLayoutManager;
    private CustomBottomTabView playerView;
    private ArrayList<Song> listAllSong;
    private boolean userPlaySong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_song_list_for_genre);
        mContext = this;
        setBottomView(mContext);
        listPrefSong = new ArrayList<>();
        listAllSong = new ArrayList<>();

        if (getIntent().hasExtra("genre")) {
            title = getIntent().getStringExtra("genresName");
            titleHebrew = getIntent().getStringExtra("genresNameHebrew");
            //getSongAccordingGenre(getIntent().getStringExtra("genre"), "1");
        }

        dialog = new CustomLoaderDialog(mContext);

        if(Utility.isConnectingToInternet(mContext)) {
            if (!isFinishing()) {
                dialog.show();
            }
            if (getIntent().hasExtra("genre")) {
                getSongAccordingGenre(getIntent().getStringExtra("genre"), "1");
            }
        }



        if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            if (titleHebrew.equalsIgnoreCase("") || titleHebrew == null) {
                binding.head.setText(title);
            } else {
                binding.head.setText(titleHebrew);
            }
        } else {
            binding.head.setText(title);
        }
        mAdapter = new SongForUAdapter(this, listPrefSong, "home", Utility.getUserInfo(mContext).myMusic);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewAll.setLayoutManager(mLayoutManager);
        binding.recycleviewAll.hasFixedSize();
        binding.recycleviewAll.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
                    @Override
                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                        if (isAdded) {
                            mAdapter.updateMyMusicList(myMusic);
                        }
                    }
                });
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                }
                Constants.SONGS_LIST = listPrefSong;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(position).id;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                playerView.setPlayerData(Constants.SONGS_LIST);
                userPlaySong = true;
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {

            }
        });

        binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                binding.progress.setVisibility(View.VISIBLE);
                getSongAccordingGenre(getIntent().getStringExtra("genre"), "" + page_number);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void showProgress() {

            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utility.isConnectingToInternet(mContext)) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getSongAccordingGenre(getIntent().getStringExtra("genre"), "" + page_number);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                }
                binding.swipeLyt.setRefreshing(false);
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Set bottom player and nav bar view
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    //Get song according genre with pagination
    public void getSongAccordingGenre(String id, String page) {
        Call<NewRelease> call = Constants.service.getSongByGenre(id, page);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                final NewRelease songList = response.body();
                if (songList.success) {
                    if (songList.songs.size() > 0) {
                        isLoading = false;
                        isLastPage = false;
                        page_number = page_number + 1;
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                if (listPrefSong.size() <= 0) {
                                    listPrefSong.addAll(songList.songs);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    listPrefSong.addAll(songList.songs);
                                    mAdapter.notifyItemRangeInserted(listPrefSong.size(), 20);
                                }
                            }
                        });
                        if (listAllSong.size() < listPrefSong.size()) {
                            listAllSong.addAll(songList.songs);
                        }
                    } else {
                        isLoading = false;
                        isLastPage = true;
                        if (listPrefSong.size() <= 0) {
                            Utility.showAlertWithCondition(mContext, mContext.getResources().getString(R.string.no_data_found));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<NewRelease> call, Throwable t) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
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
                    if (!Utility.getBooleaPreferences(mContext,"ad_in_background")) {
                        Constants.isChangeSong = false;
                        Controls.nextControl(mContext);
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));

        // for update the list
        UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
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
        if (binding.recycleviewAll != null && binding.recycleviewAll.getAdapter() != null)
            binding.recycleviewAll.getAdapter().notifyDataSetChanged();
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
}
