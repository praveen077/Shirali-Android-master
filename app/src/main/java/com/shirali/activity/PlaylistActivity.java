package com.shirali.activity;

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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.PlayAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.controls.StartSnap;
import com.shirali.databinding.ActivityPlaylistBinding;
import com.shirali.fragment.mymusic.MyPlaylistFragment;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.HomeCellModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.playlist.PlayListRequest;
import com.shirali.model.playlist.Playlist;
import com.shirali.model.playlist.PlaylistDetail;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
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

public class PlaylistActivity extends BaseActivity implements View.OnClickListener {

    private ActivityPlaylistBinding binding;
    private ArrayList<HomeCellModel> list;
    private SongForUAdapter rpAdapter;
    private ArrayList<String> song_list;
    private ArrayList<String> listOfMyMusic;
    private Shirali shirali;
    private Context mContext;
    private int songCount;
    private ArrayList<Song> getSonglist;
    private EditText titleText;
    private ArrayList<String> listPlaylist;
    private boolean isMediaPlayerPlay = false;
    private MixpanelAPI mixpanelAPI;
    private CustomLoaderDialog dialog;
    private ArrayList<String> listSong;
    private boolean isFollow;
    private ArrayList<Song> updatedList;
    private int clickCount;
    private boolean isFromAlbum = false;
    private PlayAdapter pAdapter;
    private CustomBottomTabView playerView;
    private boolean isAllSongArePremium = false;
    private ArrayList<Song> listSongs;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        mContext = this;
        setBottomView(mContext);
        Runtime.getRuntime().gc();

        //initialized all the list here
        list = new ArrayList<>();
        song_list = new ArrayList<>();
        getSonglist = new ArrayList<>();
        listPlaylist = new ArrayList<>();
        listSong = new ArrayList<>();
        updatedList = new ArrayList<>();
        listSongs = new ArrayList<>();
        listOfMyMusic = new ArrayList<>();

        //initialized mixpanel here
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);

        SnapHelper snapHelper = new StartSnap();
        Utility.setBooleanPreferences(mContext, "isPrime", true);
        listPlaylist = UserModel.getInstance().getplaylistList(PlaylistActivity.this);

        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }
        //for auto playlist
        try {
            if (getIntent().hasExtra("list_song")) {
                binding.imgMenuu.setVisibility(View.GONE);
                binding.imgPlus.setVisibility(View.GONE);
                binding.lblCreater.setVisibility(View.VISIBLE);
                dialog = new CustomLoaderDialog(mContext);
                if (!isFinishing()) {
                    dialog.show();
                }
                getMyMusicData();

            } else {
                /*For shirali and user playlist*/
                binding.lblCreater.setVisibility(View.VISIBLE);
                shirali = UserModel.getInstance().shirali;
                dialog = new CustomLoaderDialog(mContext);
                if (!isFinishing()) {
                    dialog.show();
                }
                getPlaylistData(shirali.id);

                listOfMyMusic = Utility.getUserInfo(mContext).myMusic;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < shirali.songs.size(); i++) {
                            songCount = songCount + 1;
                            song_list.add(shirali.songs.get(i).id);
                            if (listOfMyMusic.contains(song_list.get(i))) {
                                clickCount = clickCount + 1;
                            }
                        }
                    }
                });

                if (Utility.getUserInfo(mContext).myMusic.containsAll(song_list)) {
                    binding.imgPlus.setVisibility(View.GONE);
                } else {
                    binding.imgPlus.setVisibility(View.VISIBLE);
                }

                if (shirali.songs.size() <= 0) {
                    binding.lytPlaylist.setVisibility(View.GONE);
                    binding.lblNoData.setVisibility(View.VISIBLE);
                } else if (shirali.songs.size() > 0) {
                    if (shirali.avatar != null) {
                        if (shirali.avatar.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(shirali.songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                        } else {
                            Glide.with(mContext).load(shirali.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                        }
                    }
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (shirali.createdBy != null) {
                            if (Utility.getUserInfo(mContext).id.equalsIgnoreCase(shirali.createdBy)) {
                                if (shirali.title_hebrew.equalsIgnoreCase("") || shirali.title_hebrew == null) {
                                    binding.lblPlaylistName.setText(shirali.title);
                                } else {
                                    binding.lblPlaylistName.setText(shirali.title_hebrew);
                                }
                                binding.lblCreater.setText(getString(R.string.created_by) + " " + Utility.getUserInfo(mContext).firstName);
                            } else {
                                binding.lblCreater.setText(R.string.created_by_shirali);
                                if (shirali.title_hebrew.equalsIgnoreCase("") || shirali.title_hebrew == null) {
                                    binding.lblPlaylistName.setText(shirali.title);
                                } else {
                                    binding.lblPlaylistName.setText(shirali.title_hebrew);
                                }
                            }
                        } else {
                            binding.lblCreater.setText(R.string.created_by_shirali);
                            if (shirali.title_hebrew.equalsIgnoreCase("") || shirali.title_hebrew == null) {
                                binding.lblPlaylistName.setText(shirali.title);
                            } else {
                                binding.lblPlaylistName.setText(shirali.title_hebrew);
                            }
                        }
                    } else {
                        if (shirali.createdBy != null) {
                            if (Utility.getUserInfo(PlaylistActivity.this).id.equalsIgnoreCase(shirali.createdBy)) {
                                binding.lblPlaylistName.setText(shirali.title);
                                binding.lblCreater.setText(getString(R.string.created_by) + " " + Utility.getUserInfo(mContext).firstName);
                            } else {
                                binding.lblPlaylistName.setText(shirali.title);
                                binding.lblCreater.setVisibility(View.VISIBLE);
                                binding.lblCreater.setText(R.string.created_by_shirali);
                            }
                        } else {
                            binding.lblPlaylistName.setText(shirali.title);
                            binding.lblCreater.setVisibility(View.VISIBLE);
                            binding.lblCreater.setText(R.string.created_by_shirali);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeLyt.setRefreshing(false);
                if (getIntent().hasExtra("list_song")) {
                } else {
                    if (!isFinishing()) {
                        dialog.show();
                    }
                    UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                rpAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    getPlaylistData(shirali.id);
                }
            }
        });
        snapHelper.attachToRecyclerView(binding.recyclerviewAlbumSongs);


        binding.imgMenuu.setOnClickListener(this);
        binding.imgPlus.setOnClickListener(this);
        binding.imageView3.setOnClickListener(this);

        binding.recyclerviewPlaylistSongs.setHasFixedSize(true);
        binding.recyclerviewPlaylistSongs.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));
        if (getIntent().hasExtra("list_song")) {
            pAdapter = new PlayAdapter(mContext, getSonglist);
        } else {
            pAdapter = new PlayAdapter(mContext, updatedList);
        }
        binding.recyclerviewPlaylistSongs.setAdapter(pAdapter);
        pAdapter.setOnItemClickListener(new PlayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Constants.isSongPlay = true;
                playerView.setPlayerData(updatedList);

                //isMediaPlayerPlay = true;
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                }
                Constants.SONGS_LIST = shirali.songs;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(position).id;
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                callAdsFirstBeforeMusicPlay();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(mContext, MainActivity.class));
                    SplashActivity.isFromDeep = false;
                }
                finish();
            }
        });

        binding.imgPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playSongs();

            }
        });
    }

    private void playSongs() {
        Constants.isSongPlay = true;
        UserModel.getInstance().isSingleSongPlay = false;
        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
            playFullPlayList();
        } else {
            if (getIntent().hasExtra("list_song")) {
                if (isAllSongArePremium) {
                    try {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Controls.pauseControl(mContext);
                    playFullPlayList();
                }
            } else {
                if (isAllSongArePremium) {
                    try {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Controls.pauseControl(mContext);
                    playFullPlayList();
                }
            }
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

    //Set bottom sheet player and nav bar
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView3:
                mixpanelAPI.track("Number of taps on Shuffle");
                if (Constants.shuffel) {
                    Utility.setBooleanPreferences(mContext, "suffle", false);
                    Constants.shuffel = false;
                    binding.imageView3.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
                } else {
                    Utility.setBooleanPreferences(mContext, "suffle", true);
                    Constants.shuffel = true;
                    Constants.repeat = false;
                    binding.imageView3.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));

                    // AKM: Need to call Music player
                    //Utility.playFullPlayListShuffle(getIntent(), mContext, playerView, getSonglist, shirali);
                    playSongs();
                }
                break;
            case R.id.imgMenuu:
                if (Utility.getUserInfo(PlaylistActivity.this).id.equalsIgnoreCase(shirali.createdBy)) {
                    final Dialog d = new BottomSheetDialog(mContext);
                    d.setContentView(R.layout.user_action_onsong_cell);
                    ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
                    final TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                    TextView playPause = (TextView) d.findViewById(R.id.lblPlay);
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        title.setText(shirali.title_hebrew);
                    } else {
                        title.setText(shirali.title);
                    }
                    try {
                        if (shirali.avatar.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(shirali.songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        } else {
                            Glide.with(mContext).load(shirali.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (isMediaPlayerPlay) {
                        playPause.setText(getResources().getString(R.string.pause));
                    } else {
                        playPause.setText(R.string.play);
                    }
                    View vieww = d.findViewById(R.id.bs);
                    ((View) vieww.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                    TextView delete = (TextView) d.findViewById(R.id.lblRemove);
                    delete.setText(mContext.getResources().getString(R.string.delete));
                    d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                    d.findViewById(R.id.lytPlay).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Constants.isPlay) {
                                Controls.pauseControl(mContext);
                            }
                            if (getIntent().hasExtra("list_song")) {
                                Constants.SONGS_LIST = getSonglist;
                            } else {
                                Constants.SONGS_LIST = shirali.songs;
                            }
                            Constants.SONG_NUMBER = 0;
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytAddMore).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MyPlaylistFragment.isFromPlaylist = true;
                            finish();
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.cancel();
                        }
                    });
                    d.findViewById(R.id.lytEditPlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (getIntent().hasExtra("list_song")) {
                            } else {
                                final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
                                dialog.setContentView(R.layout.create_playlist_dialog);
                                dialog.show();
                                TextView textView = (TextView) dialog.findViewById(R.id.head);
                                textView.setText(getResources().getString(R.string.please_edit_your_playlist_name));

                                titleText = (EditText) dialog.findViewById(R.id.title);
                                if (Utility.getStringPreferences(PlaylistActivity.this, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                                    titleText.setText(shirali.title_hebrew);
                                } else {
                                    titleText.setText(shirali.title);
                                }
                                titleText.setSelection(shirali.title.length());
                                TextView cancelPlaylist = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                                cancelPlaylist.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                                TextView createPlaylist = (TextView) dialog.findViewById(R.id.createPlaylist);
                                createPlaylist.setText(R.string.edit);
                                createPlaylist.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        editPlaylist();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytSharePlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Utility.shareIt(mContext, "playlist", shirali.title, "", shirali.shareUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (list.size() > 0) {
                                try {
                                    UserModel.getInstance().album = shirali.songs.get(0).albums.get(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                                mContext.startActivity(intent);
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (list.size() > 0) {
                                if (shirali.songs.get(0).artist.isPremium) {
                                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                        intent.putExtra("artist_id", shirali.songs.get(0).artist.id);
                                        mContext.startActivity(intent);
                                    } else {
                                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                    }
                                } else {
                                    Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                    intent.putExtra("artist_id", shirali.songs.get(0).artist.id);
                                    mContext.startActivity(intent);
                                }
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removePlaylist(shirali.id);
                            d.dismiss();
                        }
                    });
                    d.setCancelable(true);
                    d.show();
                } else {
                    final Dialog d = new BottomSheetDialog(mContext);
                    d.setContentView(R.layout.user_action_onsong_cell);
                    ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
                    TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                    final LinearLayout follow = (LinearLayout) d.findViewById(R.id.lytFollowPlaylist);
                    final LinearLayout unFollow = (LinearLayout) d.findViewById(R.id.lytUnFollow);
                    View vie = d.findViewById(R.id.bs);
                    ((View) vie.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                    try {
                        if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                            title.setText(shirali.title_hebrew);
                        } else {
                            title.setText(shirali.title);
                        }
                        if (shirali.artwork.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(shirali.songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        } else {
                            Glide.with(mContext).load(shirali.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                    d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                    if (Utility.getUserInfo(mContext).playlist.contains(shirali.id) || isFollow) {
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.VISIBLE);
                    } else {
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.VISIBLE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                    }
                    d.findViewById(R.id.lytSharePlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Utility.shareIt(mContext, "playlist", shirali.title, "", shirali.shareUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytFollowPlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (follow.getVisibility() == View.VISIBLE) {
                                isFollow = true;
                                followPlaylist(shirali.id);
                                Utility.showPopup(mContext, mContext.getResources().getString(R.string.follow_playlist_text));
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytUnFollow).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (unFollow.getVisibility() == View.VISIBLE) {
                                isFollow = false;
                                unfollowPlaylist(shirali.id);
                                Utility.showPopup(mContext, mContext.getResources().getString(R.string.unfollow_playlist_text));
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

                    d.setCancelable(true);
                    d.show();
                }
                break;
            case R.id.imgPlus:
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(Utility.getUserInfo(mContext).myMusic, song_list, false);
                    binding.imgPlus.setVisibility(View.GONE);
                } else {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
                break;

        }
    }

    //Get user my music data for auto playlist
    public void getMyMusicData() {
        Call<MyMusic> songs = Constants.service.getMyMusic(Utility.getUserInfo(mContext).id);
        songs.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (response.isSuccessful()) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    try {
                        MyMusic list = response.body();
                        if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (list.myMusicContain.song.size() > 0) {
                                count = 0;
                                getSonglist = list.myMusicContain.song;
                                listSongs.clear();
                                listSongs.addAll(list.myMusicContain.song);
                                Glide.with(mContext).load(listSongs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                                binding.lblPlaylistName.setText(getResources().getString(R.string.recently_added_to_music));
                                binding.lblCreater.setText(getResources().getString(R.string.auto_playlist));
                                if (listSongs.size() > 0) {
                                    for (int i = 0; i < listSongs.size(); i++) {
                                        songCount = songCount + 1;
                                        song_list.add(listSongs.get(i).id);
                                    }
                                }
                                binding.recyclerviewAlbumSongs.setLayoutManager(new LinearLayoutManager(mContext));
                                if (getIntent().hasExtra("list_song")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Runtime.getRuntime().gc();
                                            rpAdapter = new SongForUAdapter(mContext, listSongs, "playlist", Utility.getUserInfo(mContext).myMusic);
                                            rpAdapter.isAlbum();
                                            binding.recyclerviewAlbumSongs.setAdapter(rpAdapter);
                                        }
                                    });
                                }
                                rpAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, String type, final int position) {
                                        playerView.setPlayerData(listSongs);

                                        if (Constants.isPlay) {
                                            Controls.pauseControl(mContext);
                                        }
                                        Constants.SONGS_LIST = listSongs;
                                        Constants.SONG_NUMBER = position;
                                        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                    }

                                    @Override
                                    public void onItemVisible(View view, boolean isVisible) {
                                        if (isVisible) {
                                            clickCount = clickCount + 1;
                                            if (clickCount >= song_list.size()) {
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

                                rpAdapter.setOnItemClickRemovr(new SongForUAdapter.OnItemClicRemove() {
                                    @Override
                                    public void onItemClick(View view, ArrayList<Song> list, int position) {
                                        UserModel.getInstance().removeFromMusic(PlaylistActivity.this, Utility.getUserInfo(mContext).id, list.get(position).id);
                                        list.remove(position);
                                        rpAdapter.notifyDataSetChanged();
                                        pAdapter.notifyDataSetChanged();
                                        if (list.size() <= 0) {
                                            finish();
                                        }
                                        binding.imgPlus.setVisibility(View.GONE);
                                    }
                                });

                                binding.recyclerviewPlaylistSongs.setHasFixedSize(true);
                                binding.recyclerviewPlaylistSongs.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.HORIZONTAL, false));
                                if (getIntent().hasExtra("list_song")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Runtime.getRuntime().gc();
                                            pAdapter = new PlayAdapter(mContext, listSongs);
                                            binding.recyclerviewPlaylistSongs.setAdapter(pAdapter);
                                        }
                                    });
                                }
                                pAdapter.setOnItemClickListener(new PlayAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, final int position) {
                                        playerView.setPlayerData(listSongs);
                                        if (Constants.isPlay) {
                                            Controls.pauseControl(mContext);
                                        }
                                        if (getIntent().hasExtra("list_song")) {
                                            Constants.SONGS_LIST = getSonglist;
                                        } else {
                                            Constants.SONGS_LIST = shirali.songs;
                                        }
                                        Constants.SONG_NUMBER = position;
                                        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                    }
                                });

                                for (int i = 0; i < list.myMusicContain.song.size(); i++) {
                                    Album album = new Album();
                                    if (list.myMusicContain.song.get(i).albums != null) {
                                        if (list.myMusicContain.song.get(i).albums.size() > 0) {
                                            album = list.myMusicContain.song.get(i).albums.get(0);
                                        }
                                    }
                                    if (list.myMusicContain.song.get(i).artist.isPremium || list.myMusicContain.song.get(i).isPremium || album.isPremium) {
                                        count = count + 1;
                                    } else {
                                        break;
                                    }
                                }
                                if (count >= list.myMusicContain.song.size()) {
                                    isAllSongArePremium = true;
                                } else {
                                    isAllSongArePremium = false;
                                }
                            } else {
                                binding.lytPlaylist.setVisibility(View.GONE);
                                binding.lblNoData.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Get playlist data
    private void getPlaylistData(String id) {
        Call<PlaylistDetail> call = Constants.service.getSelectedPlaylist(id);
        call.enqueue(new Callback<PlaylistDetail>() {
            @Override
            public void onResponse(Call<PlaylistDetail> call, Response<PlaylistDetail> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                final PlaylistDetail shiralip = response.body();
                if (shiralip != null) {
                    if (shiralip.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(mContext);
                    } else {
                        if (shiralip.songs.songs.size() > 0) {
                            count = 0;
                            shirali.songs = shiralip.songs.songs;
                            try {
                                if (Utility.getStringPreferences(PlaylistActivity.this, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                                    if (shiralip.songs.title_hebrew.equalsIgnoreCase("") || shiralip.songs.title_hebrew == null) {
                                        binding.lblPlaylistName.setText(shiralip.songs.title);
                                    } else {
                                        binding.lblPlaylistName.setText(shiralip.songs.title_hebrew);
                                    }
                                } else {
                                    binding.lblPlaylistName.setText(shiralip.songs.title);
                                }
                                if (shiralip.songs.avatar != null) {
                                    if (shiralip.songs.avatar.equalsIgnoreCase("")) {
                                        Glide.with(PlaylistActivity.this).load(shiralip.songs.songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                                    } else {
                                        Glide.with(PlaylistActivity.this).load(shiralip.songs.avatar).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                                    }
                                } else {
                                    Glide.with(PlaylistActivity.this).load(shiralip.songs.songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.imglogo).crossFade().into(binding.imgAlbumIcon);
                                }
                                updatedList = shiralip.songs.songs;
                                pAdapter.notifyDataSetChanged();
                                binding.recyclerviewAlbumSongs.setHasFixedSize(true);
                                binding.recyclerviewAlbumSongs.setNestedScrollingEnabled(false);
                                binding.recyclerviewAlbumSongs.setLayoutManager(new LinearLayoutManager(mContext));
                                if (shiralip.songs.createdBy != null) {
                                    if (Utility.getUserInfo(PlaylistActivity.this).id.equalsIgnoreCase(shiralip.songs.createdBy)) {
                                        rpAdapter = new SongForUAdapter(mContext, updatedList, "playlist", Utility.getUserInfo(mContext).myMusic);
                                    } else {
                                        rpAdapter = new SongForUAdapter(mContext, updatedList, "shirali_playlist", Utility.getUserInfo(mContext).myMusic);
                                    }
                                } else {
                                    rpAdapter = new SongForUAdapter(mContext, updatedList, "shirali_playlist", Utility.getUserInfo(mContext).myMusic);
                                }
                                //AKM:NEXT_LOGIC
                                rpAdapter.isAlbum();
                                binding.recyclerviewAlbumSongs.setAdapter(rpAdapter);
                                rpAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, String type, final int position) {
                                        Constants.isSongPlay = true;
                                        playerView.setPlayerData(updatedList);
                                        if (Constants.isPlay) {
                                            Controls.pauseControl(mContext);
                                        }
                                        if (getIntent().hasExtra("list_song")) {
                                            Constants.SONGS_LIST = getSonglist;
                                        } else {
                                            Constants.SONGS_LIST = shiralip.songs.songs;
                                        }
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
                                            if (clickCount >= song_list.size()) {
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

                                rpAdapter.setOnItemClickRemovr(new SongForUAdapter.OnItemClicRemove() {
                                    @Override
                                    public void onItemClick(View view, ArrayList<Song> list, int position) {
                                        for (int i = 0; i < song_list.size(); i++) {
                                            if (song_list.get(i).equalsIgnoreCase(shiralip.songs.songs.get(position).id)) {
                                                song_list.remove(i);
                                                list.remove(i);
                                            }
                                        }
                                        UserModel.getInstance().addToPlaylist(PlaylistActivity.this, song_list, shiralip.songs.id);
                                        rpAdapter.notifyDataSetChanged();
                                        pAdapter.notifyDataSetChanged();
                                        if (list.size() <= 0) {
                                            finish();
                                        }
                                    }
                                });

                                binding.recyclerviewPlaylistSongs.setHasFixedSize(true);
                                binding.recyclerviewPlaylistSongs.setNestedScrollingEnabled(false);
                                binding.recyclerviewPlaylistSongs.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.HORIZONTAL, false));
                                pAdapter = new PlayAdapter(mContext, updatedList);
                                binding.recyclerviewPlaylistSongs.setAdapter(pAdapter);

                                pAdapter.setOnItemClickListener(new PlayAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, final int position) {

                                        Constants.isSongPlay = true;
                                        playerView.setPlayerData(updatedList);
                                        if (Constants.isPlay) {
                                            Controls.pauseControl(mContext);
                                        }
                                        if (getIntent().hasExtra("list_song")) {
                                            Constants.SONGS_LIST = getSonglist;
                                        } else {
                                            Constants.SONGS_LIST = shiralip.songs.songs;
                                        }
                                        Constants.SONG_NUMBER = position;
                                        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                                        /*AKM*/
                                        //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                        callAdsFirstBeforeMusicPlay();
                                    }
                                });

                                for (int i = 0; i < shiralip.songs.songs.size(); i++) {
                                    Album tempAlbum = new Album();
                                    if (shiralip.songs.songs.get(i).albums != null) {
                                        if (shiralip.songs.songs.get(i).albums.size() > 0) {
                                            tempAlbum = shiralip.songs.songs.get(i).albums.get(0);
                                        }
                                    }
                                    if (shiralip.songs.songs.get(i).artist.isPremium || shiralip.songs.songs.get(i).isPremium || tempAlbum.isPremium) {
                                        count = count + 1;
                                    } else {
                                        break;
                                    }
                                }
                                if (count >= shiralip.songs.songs.size()) {
                                    isAllSongArePremium = true;
                                } else {
                                    isAllSongArePremium = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            showAlert(mContext, getString(R.string.no_record));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PlaylistDetail> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Play complete playlist
    private void playFullPlayList() {
        if (getIntent().hasExtra("list_song")) {
            Constants.SONGS_LIST = getSonglist;
        } else {
            Constants.SONGS_LIST = shirali.songs;
        }
        UserModel.getInstance().listOfShuffleSong.clear();
        UserModel.getInstance().listOfShuffleSong.addAll(Constants.SONGS_LIST);
        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
        UserModel.getInstance().listOfActualSong.clear();
        UserModel.getInstance().listOfActualSong.addAll(Constants.SONGS_LIST);
        playerView.setPlayerData(Constants.SONGS_LIST);
        Constants.SONG_NUMBER = 0;
        /*AKM*/
        //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        callAdsFirstBeforeMusicPlay();
        try {
            UserModel.getInstance().addInRecentWithType(mContext, Utility.getUserInfo(mContext).id, shirali.id, "playlist");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.alert_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(text);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);
    }

    // for edit playlist name
    private void editPlaylist() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", shirali.id);
        hashMap.put("title", titleText.getText().toString().trim());
        Call<PlayListRequest> call = Constants.service.editPlaylist(Utility.getUserInfo(mContext).id, hashMap);
        call.enqueue(new Callback<PlayListRequest>() {
            @Override
            public void onResponse(Call<PlayListRequest> call, Response<PlayListRequest> response) {
                PlayListRequest playlistResponse = response.body();
                try {
                    if (playlistResponse.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(PlaylistActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
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

    public void updateIcon() {
        if (binding.recyclerviewAlbumSongs != null && binding.recyclerviewAlbumSongs.getAdapter() != null)
            binding.recyclerviewAlbumSongs.getAdapter().notifyDataSetChanged();
    }

    //For un follow playlist
    private void unfollowPlaylist(String play_id) {
        Call<UserModel> call = Constants.service.unfollowShiraliPlaylist(Utility.getUserInfo(mContext).id, play_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                if (user.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(mContext);
                } else {
                    if (user.user != null) {
                        if (user.success) {
                            Utility.setUserInfo(mContext, user.user);
                            UserModel.getInstance().getdata(mContext);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
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

    // for remove playlist
    private void removePlaylist(String id) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("Delete Playlist");
        Call<Playlist> call = Constants.service.deletePlaylist(id);
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                Playlist playlist = response.body();
                try {
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlist.success) {
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Utility.showAlert(mContext, mContext.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    //find song list which is not added into my music
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

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(PlaylistActivity.this);
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
        if (Utility.getBooleaPreferences(mContext, "suffle")) {
            Constants.shuffel = true;
            binding.imageView3.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));
        } else {
            Constants.shuffel = false;
            binding.imageView3.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
        }
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
                    NewCampiagnActivity.isFromCampaign = false;
                    if (isFromAlbum) {
                        isFromAlbum = false;
                        if (Constants.isPlay) {
                            Controls.pauseControl(mContext);
                        }
                        if (getIntent().hasExtra("list_song")) {
                            Constants.SONGS_LIST = getSonglist;
                        } else {
                            Constants.SONGS_LIST = shirali.songs;
                        }
                        Constants.SONG_NUMBER = Constants.songSelectionNumberAfterAd;
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    } else {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        NewCampiagnActivity.isFromCampaign = false;
                    }
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


    //add and remove song list into my music
    private void addSong(ArrayList<String> listt) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", listt);
        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
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
                            updatedList.clear();
                            rpAdapter.notifyDataSetChanged();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    getPlaylistData(shirali.id);
                                }
                            }, 300); //2000
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

    //follow playlist
    private void followPlaylist(String play_id) {
        Call<UserModel> call = Constants.service.followShiraliPlaylist(Utility.getUserInfo(mContext).id, play_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (user.success) {
                            if (user.user != null) {
                                Utility.setUserInfo(mContext, user.user);
                                UserModel.getInstance().getdata(mContext);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {

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