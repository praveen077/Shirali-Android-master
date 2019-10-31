package com.shirali.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.shirali.adapter.MySongsAdapter;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.adapter.WeeklylistAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityArtistDetailBinding;
import com.shirali.model.MySongsModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.songs.Genre;
import com.shirali.model.songs.RelatedArtist;
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

public class ArtistDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityArtistDetailBinding binding;
    private ArrayList<MySongsModel> songsModelArrayList;
    private RecentPlayedAdapter rpAdapter;
    private ArrayList<Album> listRecentAlbum;
    private ArrayList<Song> listPlayed;
    private ArrayList<Shirali> listShirali;
    private ArrayList<Song> listSongs, listCurrent;
    private ArrayList<Album> listRecentPlayed;
    private MySongsAdapter mySongsAdapter;
    private String artist_id;
    private ArrayList<RelatedArtist> relatedSongs;
    private CustomLoaderDialog dialog;
    private RecentPlayedAdapter rAdapter;
    private ArrayList<String> songsList;
    private ArrayList<Song> newReleasedlist;
    private ArrayList<MySongsModel> mysonglist;
    private MySongsAdapter myAdpter;
    private WeeklylistAdapter mAdapter;
    private ArrayList<String> song_list;
    private ArrayList<Album> listNewAlbum;
    private ArrayList<String> listPlaylist;
    private Artist currentArtist;
    private Context mContext;
    private SharedPreferences preferences;
    private MixpanelAPI mixpanelAPI;
    private ArrayList<Song> listPrefSong;
    private CustomBottomTabView playerView;
    private boolean isAllSongArePremium = false;
    private boolean isFromAlbum = false;
    private ArrayList<Genre> listOfArtistGenres;
    private ArrayList<String> listSong;
    private ArrayList<String> list_song;
    private ArrayList<Song> listOfArtistSong;
    private int count = 0;
    private boolean isPlayAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_artist_detail);
        mContext = this;
        setBottomView(mContext);
        overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
        preferences = getApplicationContext().getSharedPreferences("login", 0);
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("View Artist");
        dialog = new CustomLoaderDialog(mContext);


        listRecentAlbum = new ArrayList<>();
        listPlayed = new ArrayList<>();
        listShirali = new ArrayList<>();
        mysonglist = new ArrayList<>();
        listSongs = new ArrayList<>();
        listRecentPlayed = new ArrayList<>();
        relatedSongs = new ArrayList<>();
        songsList = new ArrayList<>();
        newReleasedlist = new ArrayList<>();
        song_list = new ArrayList<>();
        listNewAlbum = new ArrayList<>();
        songsModelArrayList = new ArrayList<>();
        listPlaylist = new ArrayList<>();
        listPrefSong = new ArrayList<>();
        listOfArtistGenres = new ArrayList<>();
        listSong = new ArrayList<>();
        list_song = new ArrayList<>();
        listCurrent = new ArrayList<>();
        listOfArtistSong = new ArrayList<>();

        listPlaylist = UserModel.getInstance().getplaylistList(ArtistDetailActivity.this);

        String artistId = getIntent().getStringExtra("artist_id");
        if (artistId != null) {
            if (!artistId.equalsIgnoreCase("")) {
                artist_id = getIntent().getStringExtra("artist_id");

                if(Utility.isConnectingToInternet(mContext)) {
                    if (!isFinishing()) {
                        dialog.show();
                    }
                    getArtistDetail();
                }
            }
        }
        try {
            binding.lytAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            binding.toolbar1.setBackground(mContext.getResources().getDrawable(R.drawable.white_black_shadow, mContext.getTheme()));
                        } else {
                            binding.toolbar1.setBackground(mContext.getResources().getDrawable(R.drawable.white_black_shadow));
                        }
                    } else if (verticalOffset == 0) {
                        binding.toolbar1.setBackground(null);
                    } else {
                        binding.toolbar1.setBackground(null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
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

        if (Utility.isServiceRunning(SongPlayService.class.getName(), mContext)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.recyclerviewNewReleased.setHasFixedSize(true);
        binding.recyclerviewNewReleased.setLayoutManager(new LinearLayoutManager(this));
        myAdpter = new MySongsAdapter(mContext, mysonglist, newReleasedlist, artist_id, songsList, "new_Released", listNewAlbum);
        binding.recyclerviewNewReleased.setAdapter(myAdpter);

        binding.recyclerviewPopularSongs.setHasFixedSize(true);
        binding.recyclerviewPopularSongs.setLayoutManager(new LinearLayoutManager(this));
        mySongsAdapter = new MySongsAdapter(mContext, songsModelArrayList, listSongs, artist_id, songsList, "myArtist", listNewAlbum);
        binding.recyclerviewPopularSongs.setAdapter(mySongsAdapter);
        mySongsAdapter.setOnItemClick(new MySongsAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, final int position, String type) {
                playerView.setPlayerData(listPrefSong);
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                }
                Constants.SONGS_LIST = listSongs;
                //AKM
                UserModel.getInstance().listOfActualSong.clear();
                isPlayAlbum = false;

                UserModel.getInstance().listOfActualSong.addAll(listSongs);
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                callAdsFirstBeforeMusicPlay();
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {
                getArtistDetail();
            }
        });

        binding.recyclerviewAlbum.setHasFixedSize(true);
        binding.recyclerviewAlbum.setLayoutManager(new GridLayoutManager(this, 2));
        rpAdapter = new RecentPlayedAdapter(this, listRecentPlayed, listPlayed, relatedSongs, false, "album");
        //AKM:NEXT_LOGIC
        rpAdapter.isAlbum();
        binding.recyclerviewAlbum.setAdapter(rpAdapter);

        mAdapter = new WeeklylistAdapter(ArtistDetailActivity.this, listShirali, listPlaylist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewPopularplaylist.setLayoutManager(mLayoutManager);
        binding.recycleviewPopularplaylist.hasFixedSize();
        binding.recycleviewPopularplaylist.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new WeeklylistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listShirali.size() > 0) {
                    UserModel.getInstance().shirali = listShirali.get(position);
                }
                startActivity(new Intent(ArtistDetailActivity.this, PlaylistActivity.class));
            }
        });

        binding.recyclerviewArtists.setHasFixedSize(true);
        binding.recyclerviewArtists.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rAdapter = new RecentPlayedAdapter(this, listRecentAlbum, listPlayed, relatedSongs, true, "Artist");
        binding.recyclerviewArtists.setAdapter(rAdapter);

        binding.imgPlaySong.setOnClickListener(this);
        binding.menu.setOnClickListener(this);
        binding.imgSuffle.setOnClickListener(this);
        binding.popularSongsview.setOnClickListener(this);
        binding.albumViewall.setOnClickListener(this);
        binding.poplistViewall.setOnClickListener(this);

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utility.isConnectingToInternet(mContext)) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    getArtistDetail();
                }
                binding.swipeLyt.setRefreshing(false);
            }
        });

        binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(Utility.getUserInfo(ArtistDetailActivity.this).myMusic, list_song, false);
                    binding.imgPlus.setVisibility(View.GONE);
                } else {
                    Utility.showSubscriptionAlert(mContext, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
            }
        });
    }

    //Get Artist Detail
    public void getArtistDetail() {
        Constants.setLoggedUser(true, preferences.getString("userid", ""), Utility.getUserInfo(ArtistDetailActivity.this).deviceID);
        Call<ArtistInfo> call = Constants.service.getArtistNew(artist_id);
        call.enqueue(new Callback<ArtistInfo>() {
            @Override
            public void onResponse(Call<ArtistInfo> call, Response<ArtistInfo> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    listRecentPlayed.clear();
                    newReleasedlist.clear();
                    listShirali.clear();
                    relatedSongs.clear();
                    listNewAlbum.clear();
                    listSongs.clear();
                    list_song.clear();
                    listOfArtistGenres.clear();
                    ArtistInfo info = response.body();
                    try {
                        if (info.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(ArtistDetailActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (info.success) {
                                currentArtist = info.artist;
                                UserModel.getInstance().artist_id = info.artist;
                                try {
                                    Glide.with(ArtistDetailActivity.this).load(info.artist.avatar).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgHead);
                                    if (Utility.getStringPreferences(ArtistDetailActivity.this, Utility.preferencesLanguage).matches("iw")) {
                                        if (currentArtist.nameHebrew.equalsIgnoreCase("") || currentArtist.nameHebrew == null) {
                                            binding.collapsingToolbar.setTitle(currentArtist.name);
                                            binding.lblArtistSong.setText(getResources().getString(R.string.with) + " " + currentArtist.name + " " + getResources().getString(R.string.songs));
                                        } else {
                                            binding.collapsingToolbar.setTitle(currentArtist.nameHebrew);
                                            binding.lblArtistSong.setText(getResources().getString(R.string.with) + " " + currentArtist.nameHebrew + " " + getResources().getString(R.string.songs));
                                        }
                                    } else {
                                        binding.collapsingToolbar.setTitle(currentArtist.name);
                                        binding.lblArtistSong.setText(getResources().getString(R.string.with) + " " + currentArtist.name + " " + getResources().getString(R.string.songs));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (info.songList.size() > 0) {
                                    count = 0;
                                    if (info.popularPlaylist.size() > 0) {
                                        binding.lytPlaylist.setVisibility(View.VISIBLE);
                                        listShirali.addAll(info.popularPlaylist);
                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        binding.lytPlaylist.setVisibility(View.GONE);
                                    }

                                    if (info.albums.size() > 0) {
                                        listRecentPlayed.addAll(info.albums);
                                        rpAdapter.notifyDataSetChanged();
                                        getAlbumSongs(info.albums.get(0).id);
                                    } else {
                                        binding.lytArtistAlbum.setVisibility(View.GONE);
                                    }

                                    if (info.popularSongs.size() > 0) {
                                        for (int i = 0; i < info.popularSongs.size(); i++) {
                                            if (info.popularSongs.get(i).artist.id.equalsIgnoreCase(artist_id)) {
                                                listSongs.add(info.popularSongs.get(i));
                                            }
                                        }
                                        listCurrent.addAll(info.popularSongs);
                                        mySongsAdapter.notifyDataSetChanged();
                                    } else {
                                        binding.lytPopularSong.setVisibility(View.GONE);
                                    }

                                    try {
                                        if (info.artist.relatedArtists.size() > 0) {
                                            binding.lytRelatedArtist.setVisibility(View.VISIBLE);
                                            relatedSongs.addAll(info.artist.relatedArtists);
                                            rAdapter.notifyDataSetChanged();
                                        } else {
                                            binding.lytRelatedArtist.setVisibility(View.GONE);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (info.NewReleasedAlbums.size() > 0) {
                                        listNewAlbum.addAll(info.NewReleasedAlbums);
                                        myAdpter.notifyDataSetChanged();
                                    }
                                    if (info.popularSongsCount > 5) {
                                        binding.popularSongsview.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.popularSongsview.setVisibility(View.GONE);
                                    }
                                    if (info.albumsCount > 4) {
                                        binding.albumViewall.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.albumViewall.setVisibility(View.GONE);
                                    }
                                    if (info.popularPlaylistCount > 5) {
                                        binding.poplistViewall.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.poplistViewall.setVisibility(View.GONE);
                                    }

                                    if (info.artist.genres.size() <= 0) {
                                        binding.lytGenre.setVisibility(View.GONE);
                                    } else {
                                        binding.lytGenre.setVisibility(View.VISIBLE);
                                        listOfArtistGenres.addAll(info.artist.genres);
                                        updateGenresList(listOfArtistGenres);
                                    }
                                    for (int i = 0; i < info.songList.size(); i++) {
                                        list_song.add(info.songList.get(i).id);
                                    }
                                    listOfArtistSong.addAll(info.songList);
                                    updatePlusIcon(Utility.getUserInfo(ArtistDetailActivity.this).myMusic, list_song);

                                    for (int i = 0; i < listOfArtistSong.size(); i++) {
                                        Album album = new Album();
                                        if (listOfArtistSong.get(i).albums !=null){
                                            if (listOfArtistSong.get(i).albums.size()>0){
                                                album = listOfArtistSong.get(i).albums.get(0);
                                            }
                                        }
                                        if (listOfArtistSong.get(i).artist.isPremium || listOfArtistSong.get(i).isPremium || album.isPremium) {
                                            count = count + 1;
                                        }else {
                                            break;
                                        }
                                    }
                                    if (count >= listOfArtistSong.size()){
                                        isAllSongArePremium = true;
                                    }else {
                                        isAllSongArePremium = false;
                                    }
                                } else {
                                    Utility.openPremiumAlert(mContext);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistInfo> call, Throwable t) {
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
    public void onClick(View v) {
        if (v.getId() == R.id.imgPlaySong) {
            isPlayAlbum = true;
            playSongs();
        } else if (v.getId() == R.id.menu) {
            openMenu();
        } else if (v.getId() == R.id.imgSuffle) {
            if (Constants.shuffel) {
                Utility.setBooleanPreferences(mContext, "suffle", false);
                Constants.shuffel = false;
                binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
            } else {
                Utility.setBooleanPreferences(mContext, "suffle", true);
                Constants.shuffel = true;
                binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));

                playSongs();
            }
        } else if (v.getId() == R.id.popular_songsview) {
            Intent intent4 = new Intent(ArtistDetailActivity.this, ViewAllActivity.class);
            intent4.putExtra("view", "popular_songs");
            intent4.putExtra("artist_id", artist_id);
            startActivity(intent4);
        } else if (v.getId() == R.id.album_viewall) {
            Intent intent4 = new Intent(ArtistDetailActivity.this, ViewAllActivity.class);
            intent4.putExtra("view", "art_album");
            intent4.putExtra("artist_id", artist_id);
            startActivity(intent4);
        } else if (v.getId() == R.id.poplist_viewall) {
            Intent intent4 = new Intent(ArtistDetailActivity.this, ViewAllActivity.class);
            intent4.putExtra("view", "playlist");
            intent4.putExtra("artist_id", artist_id);
            startActivity(intent4);
        }
    }

    private void playSongs() {
        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
            playFullArtistList();
        } else {
            if (isAllSongArePremium) {
                try {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Controls.pauseControl(mContext);
                playFullArtistList();
            }
        }
    }

    private void playFullArtistList() {
        UserModel.getInstance().listOfShuffleSong.clear();
        UserModel.getInstance().listOfShuffleSong.addAll(listOfArtistSong);
        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
        UserModel.getInstance().listOfActualSong.clear();
        UserModel.getInstance().listOfActualSong.addAll(listOfArtistSong);
        if (Constants.isPlay) {
            Controls.pauseControl(mContext);
        }
        Constants.isSongPlay = true;
        UserModel.getInstance().isSingleSongPlay = false;
        if (listOfArtistSong.size() > 0) {
            Constants.SONGS_LIST = listOfArtistSong;
        }
        playerView.setPlayerData(Constants.SONGS_LIST);
        Constants.SONG_NUMBER = 0;
        Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
        /*AKM*/
        //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        callAdsFirstBeforeMusicPlay();

        try {
            UserModel.getInstance().addInRecentWithType(mContext, Utility.getUserInfo(mContext).id, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.id, "artist");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Open Menu
    private void openMenu() {
        final Dialog d = new BottomSheetDialog(ArtistDetailActivity.this);
        d.setContentView(R.layout.user_action_onsong_cell);
        View view = d.findViewById(R.id.bs);
        ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
        TextView title = (TextView) d.findViewById(R.id.tvArtistName);
        ((View) view.getParent()).setBackgroundColor(getApplicationContext().getResources().getColor(android.R.color.transparent));

        if (currentArtist != null) {
            if (Utility.getStringPreferences(ArtistDetailActivity.this, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (currentArtist.nameHebrew == null || currentArtist.nameHebrew.equalsIgnoreCase("")) {
                    title.setText(currentArtist.name);
                } else {
                    title.setText(currentArtist.nameHebrew);
                }
            } else {
                title.setText(currentArtist.name);
            }
        }
        try {
            Glide.with(ArtistDetailActivity.this).load(currentArtist.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
        d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
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
                    startActivity(new Intent(ArtistDetailActivity.this, AddPlaylistsActivity.class).putExtra("list_of_song", list_song));
                } else {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytShareArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Utility.shareIt(ArtistDetailActivity.this, "artist", currentArtist.name, "", currentArtist.shareUrl);
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
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(Utility.getUserInfo(ArtistDetailActivity.this).myMusic, list_song, true);
                    binding.imgPlus.setVisibility(View.VISIBLE);
                } else {
                    Utility.showSubscriptionAlert(mContext, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
                d.dismiss();
            }
        });
        d.setCancelable(true);
        d.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        updatePlusIcon(Utility.getUserInfo(ArtistDetailActivity.this).myMusic, list_song);
        Utility.isConnectingToInternet(ArtistDetailActivity.this);
        if (Utility.getBooleaPreferences(mContext, "suffle")) {
            Constants.shuffel = true;
            binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle_selected));
        } else {
            Constants.shuffel = false;
            binding.imgSuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.suffle));
        }

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
                if (isFromAlbum) {
                    isFromAlbum = false;
                    if (Constants.isPlay) {
                        Controls.pauseControl(mContext);
                        Constants.seekTo = 0;
                        Controls.seekToControl(mContext);
                    }
                    if (listSongs.size() > 0) {
                        Constants.SONGS_LIST = listSongs;
                    } else {
                        Constants.SONGS_LIST = listPrefSong;
                    }
                    Constants.SONG_NUMBER = Constants.songSelectionNumberAfterAd;
                    Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
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

    public void updateIcon() {
        if(!isPlayAlbum)
        if (binding.recyclerviewPopularSongs != null && binding.recyclerviewPopularSongs.getAdapter() != null)
            binding.recyclerviewPopularSongs.getAdapter().notifyDataSetChanged();
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

    //get song from album
    public void getAlbumSongs(String id) {
        Call<SongsList> call = Constants.service.getAlbumSong(id);
        call.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                if (response.isSuccessful()) {
                    SongsList list = response.body();
                    listPrefSong.clear();
                    try {
                        if (list != null) {
                            if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                                try {
                                    Utility.openSessionOutDialog(ArtistDetailActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (list.success) {
                                    if (list.songs.size() > 0) {
                                        listPrefSong.addAll(list.songs);
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
            public void onFailure(Call<SongsList> call, Throwable t) {
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

    //Show all the genres list which is played by artist
    private void updateGenresList(ArrayList<Genre> listOfArtistGenres) {
        if (listOfArtistGenres.size() == 1) {
            binding.lblGenre.setText(mContext.getResources().getString(R.string.genre));
            if (Utility.getStringPreferences(ArtistDetailActivity.this, Utility.preferencesLanguage).matches("iw")) {
                String genreText;
                if (listOfArtistGenres.get(0).titleHebrew == null || listOfArtistGenres.get(0).titleHebrew.equalsIgnoreCase("")) {
                    genreText = listOfArtistGenres.get(0).title;
                } else {
                    genreText = listOfArtistGenres.get(0).titleHebrew;
                }
                binding.lblGenreList.setText(genreText);
            } else {
                String genreText = listOfArtistGenres.get(0).title;
                binding.lblGenreList.setText(genreText);
            }
        } else {
            binding.lblGenre.setText(mContext.getResources().getString(R.string.genres));
            StringBuilder genresText = new StringBuilder();
            if (Utility.getStringPreferences(ArtistDetailActivity.this, Utility.preferencesLanguage).matches("iw")) {

                for (int i = 0; i < listOfArtistGenres.size(); i++) {
                    if (listOfArtistGenres.get(i).titleHebrew == null || listOfArtistGenres.get(i).titleHebrew.equalsIgnoreCase("")) {
                        if (genresText.toString().length() <= 0) {
                            genresText.append(listOfArtistGenres.get(i).title);
                        } else {
                            genresText.append(", ").append(listOfArtistGenres.get(i).title);
                        }
                    } else {
                        if (genresText.toString().length() <= 0) {
                            genresText.append(listOfArtistGenres.get(i).titleHebrew);
                        } else {
                            genresText.append(", ").append(listOfArtistGenres.get(i).titleHebrew);
                        }
                    }
                }
                binding.lblGenreList.setText(genresText);
            } else {
                for (int i = 0; i < listOfArtistGenres.size(); i++) {
                    if (genresText.toString().length() <= 0) {
                        genresText.append(listOfArtistGenres.get(i).title);
                    } else {
                        genresText.append(", ").append(listOfArtistGenres.get(i).title);
                    }
                }
                binding.lblGenreList.setText(genresText.toString());
            }

        }
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
            Utility.showPopup(mContext, getString(R.string.artists_removed));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(mContext, getString(R.string.artist_added));
        }
    }

    // add selected song list into my music
    private void addSong(final ArrayList<String> list) {
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
                                getArtistDetail();
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

    public void updatePlusIcon(ArrayList<String> listOfMYMusic, ArrayList<String> listOfArtistSongs) {
        if (listOfMYMusic.containsAll(listOfArtistSongs)) {
            binding.imgPlus.setVisibility(View.GONE);
        } else {
            binding.imgPlus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utility.isServiceRunning(SongPlayService.class.getName(), ArtistDetailActivity.this)) {
            Intent playIntent = new Intent(this, SongPlayService.class);
            startService(playIntent);
        }
        if (!Utility.isServiceRunning(PlayService.class.getName(), ArtistDetailActivity.this)) {
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
}
