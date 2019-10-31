package com.shirali.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.AddlistAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityAddPlaylistsBinding;
import com.shirali.model.playlist.PlayListRequest;
import com.shirali.model.playlist.PlaylistDetail;
import com.shirali.model.playlist.PlaylistResponse;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPlaylistsActivity extends BaseActivity {


    private AddlistAdapter mAdapter;
    private ActivityAddPlaylistsBinding binding;
    private ArrayList<Shirali> playlist;
    private EditText title;
    private String songid;
    private ArrayList<String> getSonglist;
    private ArrayList<String> existSong;
    private Context mContext;
    private MixpanelAPI mixpanelAPI;
    private CustomBottomTabView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_playlists);
        mContext = this;
        setBottomView(mContext);
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("Add to Playlist button");
        getplaylist();
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        playlist = new ArrayList<>();
        getSonglist = new ArrayList<>();
        existSong = new ArrayList<>();

        try {
            if (getIntent().hasExtra("id")) {
                songid = getIntent().getStringExtra("id");
                getSonglist.add(songid);
            } else {
                getSonglist = getIntent().getStringArrayListExtra("list_of_song");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.addSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(AddPlaylistsActivity.this).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    final Dialog dialog = new Dialog(AddPlaylistsActivity.this, R.style.CustomDialog);
                    dialog.setContentView(R.layout.create_playlist_dialog);
                    dialog.show();
                    title = (EditText) dialog.findViewById(R.id.title);
                    TextView cancelPlaylist = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                    cancelPlaylist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    TextView createPlaylist = (TextView) dialog.findViewById(R.id.createPlaylist);
                    TextView remove = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                    createPlaylist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (title.getText().toString().equalsIgnoreCase("")) {
                                Utility.showAlert(mContext, getString(R.string.valid_playlist_name));
                            } else {
                                createPlaylist(title.getText().toString());
                            }
                            dialog.dismiss();
                        }
                    });
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    Utility.openAlertForAddToPlaylist(mContext, getResources().getString(R.string.premium_user_only));
                }
            }
        });

        mAdapter = new AddlistAdapter(this, playlist, "add_in_playlist");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewSfu.setLayoutManager(mLayoutManager);
        binding.recycleviewSfu.hasFixedSize();
        binding.recycleviewSfu.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AddlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getPlaylistData(playlist.get(position).id);
                overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
                finish();
            }
        });


        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(AddPlaylistsActivity.this).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    final Dialog dialog = new Dialog(AddPlaylistsActivity.this, R.style.CustomDialog);
                    dialog.setContentView(R.layout.create_playlist_dialog);
                    if (!isFinishing()) {
                        dialog.show();
                    }
                    title = (EditText) dialog.findViewById(R.id.title);
                    TextView cancelPlaylist = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                    cancelPlaylist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isFinishing()) {
                                dialog.dismiss();
                            }
                        }
                    });
                    TextView createPlaylist = (TextView) dialog.findViewById(R.id.createPlaylist);
                    TextView remove = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                    createPlaylist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (title.getText().toString().equalsIgnoreCase("")) {
                                Utility.showAlert(mContext, getString(R.string.valid_playlist_name));
                            } else {
                                createPlaylist(title.getText().toString());
                            }
                            if (!isFinishing()) {
                                dialog.dismiss();
                            }
                        }
                    });
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isFinishing()) {
                                dialog.dismiss();
                            }
                        }
                    });
                } else {
                    Utility.openAlertForAddToPlaylist(mContext, getResources().getString(R.string.premium_user_only));
                }
            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    //Get All Playlist of users
    private void getplaylist() {
        Call<PlaylistResponse> call = Constants.service.getUserPlaylist(Utility.getUserInfo(AddPlaylistsActivity.this).id);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                playlist.clear();
                PlaylistResponse playlistResponse = response.body();
                try {
                    if (playlistResponse.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(AddPlaylistsActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            if (playlistResponse.songs.size() > 0) {
                                binding.lytPlaylist.setVisibility(View.VISIBLE);
                                binding.lytEmpty.setVisibility(View.GONE);
                                for (int i = 0; i < playlistResponse.songs.size(); i++) {
                                    if (Utility.getUserInfo(AddPlaylistsActivity.this).id.equalsIgnoreCase(playlistResponse.songs.get(i).createdBy)) {
                                        playlist.add(playlistResponse.songs.get(i));
                                    }
                                }
                                mAdapter.notifyDataSetChanged();
                            } else {
                                binding.lytPlaylist.setVisibility(View.GONE);
                                binding.lytEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //For create playlist
    private void createPlaylist(String title) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("createdBy", Utility.getUserInfo(AddPlaylistsActivity.this).id);
        Call<PlayListRequest> playlistResponseCall = Constants.service.CreatePlaylist(hashMap);
        playlistResponseCall.enqueue(new Callback<PlayListRequest>() {
            @Override
            public void onResponse(Call<PlayListRequest> call, Response<PlayListRequest> response) {
                PlayListRequest playlistResponse = response.body();
                try {
                    if (playlistResponse.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(AddPlaylistsActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            getplaylist();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlayListRequest> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Add a song into selected playlist if that song is not exist in the playlist
    private void getPlaylistData(final String id) {
        Call<PlaylistDetail> call = Constants.service.getSelectedPlaylist(id);
        call.enqueue(new Callback<PlaylistDetail>() {
            @Override
            public void onResponse(Call<PlaylistDetail> call, Response<PlaylistDetail> response) {
                PlaylistDetail shirali = response.body();
                if (shirali.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(mContext);
                } else {
                    if (shirali.songs != null) {
                        if (shirali.songs.songs.size() > 0) {
                            for (int i = 0; i < shirali.songs.songs.size(); i++) {
                                existSong.add(shirali.songs.songs.get(i).id);
                            }
                        }
                    }
                    if (getSonglist.size() > 0) {
                        for (int i = 0; i < getSonglist.size(); i++) {
                            if (!existSong.contains(getSonglist.get(i))) {
                                existSong.add(getSonglist.get(i));
                            }
                        }
                    }
                    UserModel.getInstance().addToPlaylist(AddPlaylistsActivity.this, existSong, id);
                }
            }

            @Override
            public void onFailure(Call<PlaylistDetail> call, Throwable t) {
                t.printStackTrace();
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

    //Open player view on click play full album from timer if lt is not show
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
