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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.ArtistAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityArtistViewAllBinding;
import com.shirali.interfaces.AddArtistOrNot;
import com.shirali.model.songs.Artist;
import com.shirali.model.user.UserModel;
import com.shirali.model.v2model.AllArtistByChar;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.CustomScrollListner;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistViewAllActivity extends BaseActivity {

    private ActivityArtistViewAllBinding binding;
    private Context mContext;
    private CustomLoaderDialog dialog;
    private LinearLayoutManager mLayoutManager;
    private ArtistAdapter adapter;
    private List<Artist> mDataArray;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int page_number = 1;
    private CustomBottomTabView playerView;
    private ArrayList<Artist> listAllSong;
    private String section_title = "";
    private String term = "";
    private ArrayList<String> listSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_artist_view_all);

        mContext = this;
        mDataArray = new ArrayList<>();
        listAllSong = new ArrayList<>();
        listSong = new ArrayList<>();
        setBottomView(mContext);
        dialog = new CustomLoaderDialog(mContext);
        if (!isFinishing()) {
            dialog.show();
        }

        if (getIntent().hasExtra("section_title")) {
            section_title = getIntent().getStringExtra("section_title");
            term = getIntent().getStringExtra("term");
            getSongAccordingGenre(getIntent().getStringExtra("section_title"), getIntent().getStringExtra("term"), 1);
        }

        mLayoutManager = new LinearLayoutManager(this);
        binding.recycleviewAll.setLayoutManager(mLayoutManager);
        adapter = new ArtistAdapter(mContext, mDataArray);
        binding.recycleviewAll.setAdapter(adapter);

        adapter.setOnItemClickListener(new ArtistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String operationType, AddArtistOrNot check) {
                if (operationType.equalsIgnoreCase("plus")) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(check, Utility.getUserInfo(mContext).myMusic, mDataArray.get(position).songsId, false);
                } else {
                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.show();
                            }
                        }
                        addedToMyMusic(check, Utility.getUserInfo(mContext).myMusic, mDataArray.get(position).songsId, true);
                    } else {
                        Utility.showSubscriptionAlert(mContext, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                    }
                }
            }
        });

        binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                binding.progress.setVisibility(View.VISIBLE);
                getSongAccordingGenre(section_title,term, page_number);
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
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                mDataArray.clear();
                adapter.notifyDataSetChanged();
                page_number = 1;
                getSongAccordingGenre(section_title,term, page_number);
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    //Get all artist list according user input
    public void getSongAccordingGenre(String id, String term, int page) {
        Call<AllArtistByChar> call = Constants.service.getAllArtistByChar(id, term, String.valueOf(page));
        call.enqueue(new Callback<AllArtistByChar>() {
            @Override
            public void onResponse(Call<AllArtistByChar> call, Response<AllArtistByChar> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                final AllArtistByChar songList = response.body();
                if (songList.success) {
                    if (songList.artists.size() > 0) {
                        isLoading = false;
                        isLastPage = false;
                        page_number = page_number + 1;
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                if (mDataArray.size() <= 0) {
                                    mDataArray.addAll(songList.artists);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    mDataArray.addAll(songList.artists);
                                    adapter.notifyItemRangeInserted(mDataArray.size(), 20);
                                }
                            }
                        });
                        if (listAllSong.size() < mDataArray.size()) {
                            listAllSong.addAll(songList.artists);
                        }
                    } else {
                        isLoading = false;
                        isLastPage = true;
                        if (mDataArray.size() <= 0) {
                            Utility.showAlertWithCondition(mContext, mContext.getResources().getString(R.string.no_data_found));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AllArtistByChar> call, Throwable t) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Utility.showAlert(mContext, getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    public void updateIcon() {
        if (binding.recycleviewAll != null && binding.recycleviewAll.getAdapter() != null)
            binding.recycleviewAll.getAdapter().notifyDataSetChanged();
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

    // find album song list which is not in my music
    private void addedToMyMusic(AddArtistOrNot view, ArrayList<String> myMusic, ArrayList<String> song_list, boolean isRemove) {
        if (isRemove) {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (myMusic.contains(song_list.get(i))) {
                    listSong.remove(song_list.get(i));
                }
            }
            addSong(listSong, view);
            listSong.clear();
            Utility.showPopup(mContext, getString(R.string.artists_removed));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong, view);
            listSong.clear();
            Utility.showPopup(mContext, getString(R.string.artist_added));
        }
    }

    // add selected song list into my music
    private void addSong(final ArrayList<String> list, final AddArtistOrNot check) {
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
                                UserModel.getInstance().getdata(mContext);
                                check.checkAdd();
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
