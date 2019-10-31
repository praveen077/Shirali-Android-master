package com.shirali.activity;

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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.SearchAdvancedAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityGenreDetailBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.mymusic.Album;
import com.shirali.model.search.GenreData;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenreDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityGenreDetailBinding binding;
    private SearchAdvancedAdapter artistAdvancedAdapter, albumAdvancedAdapter;
    private Context context;
    private ArrayList<Artist> listSuggestionArtist;
    private ArrayList<Album> listSuggestionAlbum;
    private SongForUAdapter sAdapter;
    private ArrayList<Song> listSong;
    private String title = "";
    private String titleHebrew = "";
    private String genreId = "";
    private CustomLoaderDialog dialog;
    private CustomBottomTabView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_genre_detail);
        context = this;
        setBottomView(context);
        listSuggestionArtist = new ArrayList<>();
        listSuggestionAlbum = new ArrayList<>();
        listSong = new ArrayList<>();

        if (getIntent().hasExtra("genre")) {
            title = getIntent().getStringExtra("genresName");
            titleHebrew = getIntent().getStringExtra("genresNameHebrew");
            genreId = getIntent().getStringExtra("genre");
            //getGenreDetail(getIntent().getStringExtra("genre"));
        }

        if(Utility.isConnectingToInternet(context)) {
            dialog = new CustomLoaderDialog(context);
            if (!isFinishing()) {
                dialog.show();
            }

            if (getIntent().hasExtra("genre")) {
                getGenreDetail(getIntent().getStringExtra("genre"));
            }
        }


        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            if (titleHebrew.equalsIgnoreCase("") || titleHebrew == null) {
                binding.lblTitle.setText(title);
            } else {
                binding.lblTitle.setText(titleHebrew);
            }
        } else {
            binding.lblTitle.setText(title);
        }

        binding.listArtist.setHasFixedSize(true);
        binding.listArtist.setLayoutManager(new LinearLayoutManager(context));
        artistAdvancedAdapter = new SearchAdvancedAdapter(context, listSuggestionArtist, listSuggestionAlbum, "MyArtist");
        binding.listArtist.setAdapter(artistAdvancedAdapter);
        artistAdvancedAdapter.setOnItemClickListener(new SearchAdvancedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }
        });

        binding.listAlbum.setHasFixedSize(true);
        binding.listAlbum.setLayoutManager(new LinearLayoutManager(context));
        albumAdvancedAdapter = new SearchAdvancedAdapter(context, listSuggestionArtist, listSuggestionAlbum, "MyAlbum");
        binding.listAlbum.setAdapter(albumAdvancedAdapter);
        albumAdvancedAdapter.setOnItemClickListener(new SearchAdvancedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    UserModel.getInstance().album = listSuggestionAlbum.get(position);
                    UserModel.getInstance().artist_id = listSuggestionAlbum.get(position).artist;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, AlbumDetailActivity.class);
                startActivity(intent);
            }
        });

        sAdapter = new SongForUAdapter(context, listSong, "search", Utility.getUserInfo(context).myMusic);
        sAdapter.isAlbum();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.listSong.setLayoutManager(layoutManager);
        binding.listSong.smoothScrollToPosition(listSong.size());
        binding.listSong.hasFixedSize();
        binding.listSong.setAdapter(sAdapter);
        sAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (Constants.isPlay) {
                    Controls.pauseControl(context);
                }
                Constants.SONGS_LIST = listSong;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                callAdsFirstBeforeMusicPlay();
                playerView.setPlayerData(Constants.SONGS_LIST);
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {

            }
        });

        binding.lytSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isFinishing()) {
                    dialog.show();
                }
                if (getIntent().hasExtra("genre")) {
                    title = getIntent().getStringExtra("genresName");
                    titleHebrew = getIntent().getStringExtra("genresNameHebrew");
                    genreId = getIntent().getStringExtra("genre");
                    getGenreDetail(getIntent().getStringExtra("genre"));
                }
                binding.lytSwipe.setRefreshing(false);
            }
        });

        binding.imgBack.setOnClickListener(this);
        binding.lblArtistViewAll.setOnClickListener(this);
        binding.lblAlbumViewAll.setOnClickListener(this);
        binding.lblSongViewAll.setOnClickListener(this);
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
                    playerView.setPlayerData(Constants.SONGS_LIST);
                }
            }
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(context).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));

        // for update the list
        UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
            @Override
            public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                if (sAdapter !=null) {
                    sAdapter.updateMyMusicList(myMusic);
                    sAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    // Get song, album and artist of specific genres
    public void getGenreDetail(String genre_id) {
        Call<GenreData> call = Constants.service.getGenreDetail(genre_id);
        call.enqueue(new Callback<GenreData>() {
            @Override
            public void onResponse(Call<GenreData> call, Response<GenreData> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                listSong.clear();
                listSuggestionArtist.clear();
                listSuggestionAlbum.clear();
                if (response.isSuccessful()) {
                    GenreData search = response.body();
                    if (search != null) {
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (search.songs.size() > 0) {
                                binding.lytSong.setVisibility(View.VISIBLE);
                                listSong.addAll(search.songs);
                                sAdapter.notifyDataSetChanged();
                            } else {
                                binding.lytSong.setVisibility(View.GONE);
                            }
                            if (search.artists.size() > 0) {
                                binding.lytArtist.setVisibility(View.VISIBLE);
                                listSuggestionArtist.addAll(search.artists);
                                artistAdvancedAdapter.notifyDataSetChanged();
                            } else {
                                binding.lytArtist.setVisibility(View.GONE);
                            }
                            if (search.albums.size() > 0) {
                                binding.lytAlbum.setVisibility(View.VISIBLE);
                                listSuggestionAlbum.addAll(search.albums);
                                albumAdvancedAdapter.notifyDataSetChanged();
                            } else {
                                binding.lytAlbum.setVisibility(View.GONE);
                            }
                            if (search.artistsCount < 5) {
                                binding.lblArtistViewAll.setVisibility(View.GONE);
                            } else {
                                binding.lblArtistViewAll.setVisibility(View.VISIBLE);
                            }
                            if (search.songsCount < 5) {
                                binding.lblSongViewAll.setVisibility(View.GONE);
                            } else {
                                binding.lblSongViewAll.setVisibility(View.VISIBLE);
                            }
                            if (search.albumsCount < 5) {
                                binding.lblAlbumViewAll.setVisibility(View.GONE);
                            } else {
                                binding.lblAlbumViewAll.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GenreData> call, Throwable t) {
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
        if (v.getId() == R.id.img_back) {
            finish();
        }
        if (v.getId() == R.id.lblAlbumViewAll) {
            Intent intent4 = new Intent(context, ViewAllActivity.class);
            intent4.putExtra("view", "genre_album");
            intent4.putExtra("view_album", getResources().getString(R.string.albums) + " " + getResources().getString(R.string.in) + " " + title);
            intent4.putExtra("view_album_hebrew", getResources().getString(R.string.albums) + " " + getResources().getString(R.string.in) + " " + titleHebrew);
            intent4.putExtra("genre", genreId);
            startActivity(intent4);
        }
        if (v.getId() == R.id.lblSongViewAll) {
            Intent intent4 = new Intent(context, ViewAllActivity.class);
            intent4.putExtra("view", "genre_songs");
            intent4.putExtra("view_song", getResources().getString(R.string.songs) + " " + getResources().getString(R.string.in) + " " + title);
            intent4.putExtra("view_song_hebrew", getResources().getString(R.string.songs) + " " + getResources().getString(R.string.in) + " " + titleHebrew);
            intent4.putExtra("genre", genreId);
            startActivity(intent4);
        }
        if (v.getId() == R.id.lblArtistViewAll) {
            Intent intent4 = new Intent(context, ViewAllActivity.class);
            intent4.putExtra("view", "genre_artist");
            intent4.putExtra("view_artist", getResources().getString(R.string.artist) + " " + getResources().getString(R.string.in) + " " + title);
            intent4.putExtra("view_artist_hebrew", getResources().getString(R.string.artist) + " " + getResources().getString(R.string.in) + " " + titleHebrew);
            intent4.putExtra("genre", genreId);
            startActivity(intent4);
        }
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

    BroadcastReceiver change_song = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.isPageSelectedFromNextOfPrevious = false;
            playerView.setPlayerData(Constants.SONGS_LIST);
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
            playerView.updateSeekBar();
        }
    };

    public void updateIcon() {
        if (binding.listSong != null && binding.listSong.getAdapter() != null)
            binding.listSong.getAdapter().notifyDataSetChanged();
    }

    BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()){
                finish();
            }
        }
    };
}
