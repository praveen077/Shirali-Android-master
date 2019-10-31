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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.GenresAdapter;
import com.shirali.adapter.SearchAdvancedAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.adapter.ViewAlbumSongAdapter;
import com.shirali.adapter.WeeklylistAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityViewAllBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.GenreAlbum;
import com.shirali.model.NewRelease;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.search.AdvanceSearch;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Genre;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongsList;
import com.shirali.model.songs.artist.ArtistNewAlbum;
import com.shirali.model.songs.artist.ArtistPlaylist;
import com.shirali.model.songs.artist.ArtistPopularSong;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.CustomScrollListner;
import com.shirali.widget.GridPaginationScrollListner;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllActivity extends BaseActivity implements View.OnClickListener {

    private ActivityViewAllBinding binding;
    private GenresAdapter adapterGenres;
    private ArrayList<String> listG;
    private ArrayList<Genre> listGenres;
    private CustomLoaderDialog dialog;
    private ArrayList<Song> listPrefSong;
    private SongForUAdapter mAdapter;
    private ViewAlbumSongAdapter albumSongAdapter;
    private ArrayList<Album> listAlbum;
    private int page_number = 1;
    private Context context;
    private ArrayList<Shirali> listShirali;
    private ArrayList<String> listPlaylist;
    private WeeklylistAdapter weekAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private GridLayoutManager gridLayoutManager;
    private CustomBottomTabView playerView;
    private ArrayList<Song> listAllSong;
    private boolean userPlaySong = false;
    private SearchAdvancedAdapter artistAdvancedAdapter, albumAdvancedAdapter;
    private ArrayList<Artist> listSuggestionArtist;
    private ArrayList<Album> listSuggestionAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_all);
        context = this;
        setBottomView(context);
        listG = new ArrayList<>();
        listShirali = new ArrayList<>();
        listPlaylist = new ArrayList<>();
        listSuggestionAlbum = new ArrayList<>();
        listSuggestionArtist = new ArrayList<>();
        dialog = new CustomLoaderDialog(context);
        /*if (!isFinishing()) {
            dialog.show();
        }*/
        listGenres = new ArrayList<>();
        listPrefSong = new ArrayList<>();
        listAlbum = new ArrayList<>();
        listAllSong = new ArrayList<>();

        binding.imgBack.setOnClickListener(this);

        if (getIntent().getStringExtra("view").equalsIgnoreCase("genres")) {
            binding.head.setText(getResources().getString(R.string.geners));
            binding.recycleviewAll.setHasFixedSize(true);
            binding.recycleviewAll.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
            adapterGenres = new GenresAdapter(this, listGenres, true, listG);
            binding.recycleviewAll.setAdapter(adapterGenres);
            getGenreData();
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(Utility.isConnectingToInternet(context)) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.show();
                            }
                        }
                        getGenreData();
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("songs")) {
            binding.head.setText(getResources().getString(R.string.songs));
            mAdapter = new SongForUAdapter(this, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();
            binding.recycleviewAll.setAdapter(mAdapter);
            getSongs("" + page_number);
            mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String type, int position) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                    }
                    Constants.SONGS_LIST = listPrefSong;
                    Constants.SONG_NUMBER = position;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    playerView.setPlayerData(Constants.SONGS_LIST);
                    userPlaySong = true;
                }

                @Override
                public void onItemVisible(View view, boolean isVisible) {

                }
            });

            if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
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
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getSongs("" + 1);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST.addAll(listAllSong);
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getSongs("" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("recent")) {
            binding.head.setText(getResources().getString(R.string.recently_played));
            gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(gridLayoutManager);
            binding.recycleviewAll.setHasFixedSize(true);
            albumSongAdapter = new ViewAlbumSongAdapter(context, listPrefSong, listAlbum, "recent");
            binding.recycleviewAll.setAdapter(albumSongAdapter);
            albumSongAdapter.setOnItemClickListener(new ViewAlbumSongAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                    }
                    Constants.SONGS_LIST = listPrefSong;
                    Constants.SONG_NUMBER = position;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    playerView.setPlayerData(Constants.SONGS_LIST);
                    userPlaySong = true;
                }
            });
            getRecent("" + 1);

            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    listAlbum.clear();
                    albumSongAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getRecent("" + 1);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new GridPaginationScrollListner(gridLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    if (!binding.swipeLyt.isRefreshing()) {
                        isLoading = true;
                        binding.progress.setVisibility(View.VISIBLE);
                        getRecent(Integer.toString(page_number));
                    }
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("new_album")) {
            binding.head.setText(getResources().getString(R.string.albums));
            binding.recycleviewAll.setHasFixedSize(true);
            gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(gridLayoutManager);
            albumSongAdapter = new ViewAlbumSongAdapter(context, listPrefSong, listAlbum, "album");
            binding.recycleviewAll.setAdapter(albumSongAdapter);
            getNewReleaseAlbum("" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listAlbum.clear();
                    albumSongAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getNewReleaseAlbum("" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new GridPaginationScrollListner(gridLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    binding.progress.setVisibility(View.VISIBLE);
                    getNewReleaseAlbum("" + page_number);
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("new_songs")) {
            binding.head.setText(getResources().getString(R.string.songs));
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();
            getNewReleaseSong("" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    binding.swipeLyt.setRefreshing(false);
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getNewReleaseSong("" + page_number);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                }
            });

            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getNewReleaseSong("" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });
        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("pop_songs")) {
            binding.head.setText(getResources().getString(R.string.songs));
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();
            mAdapter = new SongForUAdapter(context, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
            binding.recycleviewAll.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String type, int position) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                    }
                    Constants.SONGS_LIST = listPrefSong;
                    Constants.SONG_NUMBER = position;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    playerView.setPlayerData(Constants.SONGS_LIST);
                    userPlaySong = true;
                }

                @Override
                public void onItemVisible(View view, boolean isVisible) {
                }
            });
            getPopularSong("" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getPopularSong("" + 1);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getPopularSong("" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("album")) {
            binding.head.setText(getResources().getString(R.string.albums));
            binding.recycleviewAll.setHasFixedSize(true);
            gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(gridLayoutManager);
            albumSongAdapter = new ViewAlbumSongAdapter(context, listPrefSong, listAlbum, "album");
            binding.recycleviewAll.setAdapter(albumSongAdapter);
            getRecentlyPlayedAlbum(1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listAlbum.clear();
                    albumSongAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getRecentlyPlayedAlbum(1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new GridPaginationScrollListner(gridLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    binding.progress.setVisibility(View.VISIBLE);
                    getRecentlyPlayedAlbum(page_number);
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });
        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("playlist")) {
            binding.head.setText(getResources().getString(R.string.playlist));
            binding.recycleviewAll.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            getArtistPlaylist(getIntent().getStringExtra("artist_id"), "" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listShirali.clear();
                    weekAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getArtistPlaylist(getIntent().getStringExtra("artist_id"), "" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getArtistPlaylist(getIntent().getStringExtra("artist_id"), Integer.toString(page_number));
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("popular_songs")) {
            binding.head.setText(getResources().getString(R.string.songs));
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();
            getArtistSong(getIntent().getStringExtra("artist_id"), "" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getArtistSong(getIntent().getStringExtra("artist_id"), "" + 1);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getArtistSong(getIntent().getStringExtra("artist_id"), "" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("art_album")) {
            binding.head.setText(getResources().getString(R.string.albums));
            binding.recycleviewAll.setHasFixedSize(true);
            gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(gridLayoutManager);
            albumSongAdapter = new ViewAlbumSongAdapter(context, listPrefSong, listAlbum, "album");
            binding.recycleviewAll.setAdapter(albumSongAdapter);
            getArtistAlbum(getIntent().getStringExtra("artist_id"), "" + 1);
            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listAlbum.clear();
                    albumSongAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getArtistAlbum(getIntent().getStringExtra("artist_id"), "" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new GridPaginationScrollListner(gridLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    binding.progress.setVisibility(View.VISIBLE);
                    getArtistAlbum(getIntent().getStringExtra("artist_id"), "" + page_number);
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("recomm_song")) {
            getRecommandedSong("" + page_number);
            binding.head.setText(getResources().getString(R.string.songs));
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();

            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getRecommandedSong("" + 1);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST = listAllSong;
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getRecommandedSong("" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("recommanded_album")) {
            binding.head.setText(getResources().getString(R.string.albums));
            binding.recycleviewAll.setHasFixedSize(true);
            gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(gridLayoutManager);
            albumSongAdapter = new ViewAlbumSongAdapter(context, listPrefSong, listAlbum, "album");
            binding.recycleviewAll.setAdapter(albumSongAdapter);
            getRecommandedAlbum("" + 1);

            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    listAlbum.clear();
                    albumSongAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getRecommandedAlbum("" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });
            binding.recycleviewAll.addOnScrollListener(new GridPaginationScrollListner(gridLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    binding.progress.setVisibility(View.VISIBLE);
                    getRecommandedAlbum("" + page_number);
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });
        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("genre_songs")) {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                if (getIntent().getStringExtra("view_song") == null || getIntent().getStringExtra("view_song").equalsIgnoreCase("")) {
                    binding.head.setText(getIntent().getStringExtra("view_song"));
                } else {
                    binding.head.setText(getIntent().getStringExtra("view_song_hebrew"));
                }
            } else {
                binding.head.setText(getIntent().getStringExtra("view_song"));
            }
            final String genreId = getIntent().getStringExtra("genre");
            mAdapter = new SongForUAdapter(this, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            binding.recycleviewAll.hasFixedSize();
            binding.recycleviewAll.setAdapter(mAdapter);
            getGenreSongs("" + page_number, genreId);
            mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String type, int position) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                    }
                    Constants.SONGS_LIST = listPrefSong;
                    Constants.SONG_NUMBER = position;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    playerView.setPlayerData(Constants.SONGS_LIST);
                    userPlaySong = true;
                }

                @Override
                public void onItemVisible(View view, boolean isVisible) {

                }
            });

            if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
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
                    listPrefSong.clear();
                    mAdapter.notifyDataSetChanged();
                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                        @Override
                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                            if (isAdded) {
                                mAdapter.updateMyMusicList(myMusic);
                            }
                        }
                    });
                    page_number = 1;
                    getGenreSongs("" + 1, genreId);
                    if (listAllSong.size() > 0) {
                        if (userPlaySong) {
                            Constants.SONGS_LIST.addAll(listAllSong);
                        }
                    }
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getGenreSongs("" + page_number, genreId);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("genre_artist")) {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                if (getIntent().getStringExtra("view_artist") == null || getIntent().getStringExtra("view_artist").equalsIgnoreCase("")) {
                    binding.head.setText(getIntent().getStringExtra("view_artist"));
                } else {
                    binding.head.setText(getIntent().getStringExtra("view_artist_hebrew"));
                }
            } else {
                binding.head.setText(getIntent().getStringExtra("view_artist"));
            }
            final String genreId = getIntent().getStringExtra("genre");
            mLayoutManager = new LinearLayoutManager(context);
            binding.recycleviewAll.setHasFixedSize(true);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            artistAdvancedAdapter = new SearchAdvancedAdapter(context, listSuggestionArtist, listSuggestionAlbum, "MyArtist");
            binding.recycleviewAll.setAdapter(artistAdvancedAdapter);
            artistAdvancedAdapter.setOnItemClickListener(new SearchAdvancedAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                }
            });
            getGenresArtist(genreId, "" + page_number);

            if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
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
                    listSuggestionArtist.clear();
                    artistAdvancedAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getGenresArtist(genreId, "" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getGenresArtist(genreId, "" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        } else if (getIntent().getStringExtra("view").equalsIgnoreCase("genre_album")) {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                if (getIntent().getStringExtra("view_album") == null || getIntent().getStringExtra("view_album").equalsIgnoreCase("")) {
                    binding.head.setText(getIntent().getStringExtra("view_album"));
                } else {
                    binding.head.setText(getIntent().getStringExtra("view_album_hebrew"));
                }
            } else {
                binding.head.setText(getIntent().getStringExtra("view_album"));
            }
            final String genreId = getIntent().getStringExtra("genre");
            mLayoutManager = new LinearLayoutManager(context);
            binding.recycleviewAll.setHasFixedSize(true);
            binding.recycleviewAll.setLayoutManager(mLayoutManager);
            albumAdvancedAdapter = new SearchAdvancedAdapter(context, listSuggestionArtist, listSuggestionAlbum, "MyAlbum");
            binding.recycleviewAll.setAdapter(albumAdvancedAdapter);
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
            getGenresAlbum(genreId, "" + page_number);

            if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
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
                    listSuggestionAlbum.clear();
                    albumAdvancedAdapter.notifyDataSetChanged();
                    page_number = 1;
                    getGenresAlbum(genreId, "" + 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });

            binding.recycleviewAll.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    getGenresAlbum(genreId, "" + page_number);
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
                    binding.progress.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(ViewAllActivity.this);
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
                if (mAdapter !=null) {
                    mAdapter.updateMyMusicList(myMusic);
                    mAdapter.notifyDataSetChanged();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }

    //For get all genres
    private void getGenreData() {
        Call<GenresList> filterMusicModelCall = Constants.service.getGenres("");
        filterMusicModelCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                listGenres.clear();
                GenresList genre = response.body();
                try {
                    if (genre.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (genre.genres.size() > 0) {
                            listGenres.addAll(genre.genres);
                            adapterGenres.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GenresList> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Get all song
    private void getSongs(final String page_numberr) {
        Call<SongsList> songsListCall = Constants.service.preferredSong(page_numberr);
        songsListCall.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page_numberr) == 1) {
                    listPrefSong.clear();
                }
                if (response.isSuccessful()) {
                    final SongsList songsList = response.body();
                    if (songsList.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(ViewAllActivity.this);
                    } else {
                        binding.progress.setVisibility(View.GONE);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                if (songsList.songs != null) {
                                    if (songsList.songs.size() > 0) {
                                        isLoading = false;
                                        isLastPage = false;
                                        page_number = page_number + 1;
                                        if (listPrefSong.size() <= 0) {
                                            listPrefSong.addAll(songsList.songs);
                                            mAdapter.notifyDataSetChanged();
                                        } else {
                                            listPrefSong.addAll(songsList.songs);
                                            mAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                                        }
                                        if (listAllSong.size() < listPrefSong.size()) {
                                            listAllSong.addAll(songsList.songs);
                                        }

                                    } else {
                                        isLoading = false;
                                        isLastPage = true;
                                    }
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
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

    //Get all recommendation song
    private void getRecommandedSong(final String page_numberr) {
        Call<SongsList> songsListCall = Constants.service.getAllRecommandedSong(page_numberr);
        songsListCall.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                binding.progress.setVisibility(View.GONE);
                SongsList songsList = response.body();
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page_numberr) == 1) {
                    listPrefSong.clear();
                }
                try {
                    if (songsList.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(ViewAllActivity.this);
                    } else {
                        if (songsList.songs.size() > 0) {
                            page_number = page_number + 1;
                            isLoading = false;
                            isLastPage = false;
                            if (listPrefSong.size() <= 0) {
                                listPrefSong.addAll(songsList.songs);
                                mAdapter = new SongForUAdapter(context, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
                                binding.recycleviewAll.setAdapter(mAdapter);
                                mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, String type, int position) {
                                        if (Constants.isPlay) {
                                            Controls.pauseControl(context);
                                        }
                                        Constants.SONGS_LIST = listPrefSong;
                                        Constants.SONG_NUMBER = position;
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                        playerView.setPlayerData(Constants.SONGS_LIST);
                                        userPlaySong = true;
                                    }

                                    @Override
                                    public void onItemVisible(View view, boolean isVisible) {

                                    }
                                });
                            } else {
                                listPrefSong.addAll(songsList.songs);
                                mAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                            }
                            if (listAllSong.size() < listPrefSong.size()) {
                                listAllSong.addAll(songsList.songs);
                            }
                        } else {
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
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

    //Get all recent played song
    private void getRecent(final String page_numberr) {
        Call<SongsList> songsListCall = Constants.service.recentPlayAllSong(page_numberr);
        songsListCall.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page_numberr) == 1) {
                    listPrefSong.clear();
                }
                SongsList songsList = response.body();
                try {
                    if (songsList.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (songsList.songs.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            if (listPrefSong.size() <= 0) {
                                listPrefSong.addAll(songsList.songs);
                                albumSongAdapter.notifyDataSetChanged();
                            } else {
                                listPrefSong.addAll(songsList.songs);
                                albumSongAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                            }
                            if (listAllSong.size() < listPrefSong.size()) {
                                listAllSong.addAll(songsList.songs);
                            }
                        } else {
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
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

    //Get all new release album
    private void getNewReleaseAlbum(String page) {
        Call<NewRelease> call = Constants.service.getAllNewReleaseAlbum(page);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                NewRelease release = response.body();
                try {
                    if (release.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (release.albums.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            listAlbum.addAll(release.albums);
                            albumSongAdapter.notifyDataSetChanged();

                        } else {
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    //Get all recommendation album
    private void getRecommandedAlbum(String page_no) {
        Call<NewRelease> call = Constants.service.getAllRecommandedAlbum(page_no);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                NewRelease release = response.body();
                if (release.message.equalsIgnoreCase("Invalid device login.")) {
                    try {
                        Utility.openSessionOutDialog(ViewAllActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (release.success) {
                        if (release.albums.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            listAlbum.addAll(release.albums);
                            albumSongAdapter.notifyDataSetChanged();
                        } else {
                            isLoading = false;
                            isLastPage = true;
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

    //Get all new release song
    private void getNewReleaseSong(final String page) {
        if (page_number > 0) {
            binding.progress.setVisibility(View.VISIBLE);
        }
        Call<NewRelease> call = Constants.service.getAllNewReleaseSong(page);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page) == 1) {
                    listPrefSong.clear();
                }
                NewRelease release = response.body();
                try {
                    if (response.isSuccessful()) {
                        if (release.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } else {
                            if (release.success) {
                                if (release.songs.size() > 0) {
                                    isLoading = false;
                                    isLastPage = false;
                                    page_number = page_number + 1;
                                    if (listPrefSong.size() <= 0) {
                                        listPrefSong.addAll(release.songs);
                                        mAdapter = new SongForUAdapter(context, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
                                        binding.recycleviewAll.setAdapter(mAdapter);
                                        mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(View view, String type, int position) {
                                                if (Constants.isPlay) {
                                                    Controls.pauseControl(context);
                                                }
                                                Constants.SONGS_LIST = listPrefSong;
                                                Constants.SONG_NUMBER = position;
                                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                                playerView.setPlayerData(Constants.SONGS_LIST);
                                                userPlaySong = true;
                                            }

                                            @Override
                                            public void onItemVisible(View view, boolean isVisible) {
                                            }
                                        });
                                    } else {
                                        listPrefSong.addAll(release.songs);
                                        mAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                                    }
                                    if (listAllSong.size() < listPrefSong.size()) {
                                        listAllSong.addAll(release.songs);
                                    }
                                } else {
                                    isLoading = false;
                                    isLastPage = true;
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
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                binding.progress.setVisibility(View.VISIBLE);
                t.printStackTrace();
            }
        });
    }

    //Get all new release song
    private void getPopularSong(final String page) {
        Call<NewRelease> call = Constants.service.getPopularSong(page);
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page) == 1) {
                    listPrefSong.clear();
                }
                final NewRelease release = response.body();
                try {
                    if (release.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(ViewAllActivity.this);
                    } else {
                        if (release.success) {
                            if (release.songs.size() > 0) {
                                isLoading = false;
                                isLastPage = false;
                                page_number = page_number + 1;
                                if (listPrefSong.size() <= 0) {
                                    listPrefSong.addAll(release.songs);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    listPrefSong.addAll(release.songs);
                                    mAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                                }
                                if (listAllSong.size() < listPrefSong.size()) {
                                    listAllSong.addAll(release.songs);
                                }
                            } else {
                                isLoading = false;
                                isLastPage = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    //Get all playlist
    private void getArtistPlaylist(String id, String page) {
        Call<ArtistPlaylist> call = Constants.service.getArtistPlaylist(id, page);
        call.enqueue(new Callback<ArtistPlaylist>() {
            @Override
            public void onResponse(Call<ArtistPlaylist> call, Response<ArtistPlaylist> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                ArtistPlaylist release = response.body();
                try {
                    if (release.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (release.success) {
                            if (release.playlist.size() > 0) {
                                isLoading = false;
                                isLastPage = false;
                                page_number = page_number + 1;
                                listShirali.addAll(release.playlist);
                                weekAdapter = new WeeklylistAdapter(context, listShirali, listPlaylist);
                                binding.recycleviewAll.setAdapter(weekAdapter);
                                weekAdapter.setOnItemClickListener(new WeeklylistAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        if (listShirali.size() > 0) {
                                            UserModel.getInstance().shirali = listShirali.get(position);
                                        }
                                        startActivity(new Intent(ViewAllActivity.this, PlaylistActivity.class));
                                    }
                                });
                            } else {
                                isLoading = false;
                                isLastPage = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ArtistPlaylist> call, Throwable t) {
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

    //Get artist all popular song
    private void getArtistSong(String id, final String page) {
        Call<ArtistPopularSong> call = Constants.service.getArtistPopularSong(id, page);
        call.enqueue(new Callback<ArtistPopularSong>() {
            @Override
            public void onResponse(Call<ArtistPopularSong> call, Response<ArtistPopularSong> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page) == 1) {
                    listPrefSong.clear();
                }
                ArtistPopularSong release = response.body();
                if (release.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(ViewAllActivity.this);
                } else {
                    if (release.success) {
                        if (release.songs.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            if (listPrefSong.size() <= 0) {
                                listPrefSong.addAll(release.songs);
                                mAdapter = new SongForUAdapter(context, listPrefSong, "home", Utility.getUserInfo(context).myMusic);
                                binding.recycleviewAll.setAdapter(mAdapter);
                                mAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, String type, int position) {
                                        //mAdapter.updateMyMusicList(UserModel.getInstance().getMyMusic(context));
                                        if (Constants.isPlay) {
                                            Controls.pauseControl(context);
                                        }
                                        Constants.SONGS_LIST = listPrefSong;
                                        Constants.SONG_NUMBER = position;
                                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                        playerView.setPlayerData(Constants.SONGS_LIST);
                                        userPlaySong = true;
                                    }

                                    @Override
                                    public void onItemVisible(View view, boolean isVisible) {

                                    }
                                });
                            } else {
                                listPrefSong.addAll(release.songs);
                                mAdapter.notifyItemRangeChanged(listPrefSong.size(), 20);
                            }
                            if (listAllSong.size() < listPrefSong.size()) {
                                listAllSong.addAll(release.songs);
                            }
                        } else {
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistPopularSong> call, Throwable t) {
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

    //Get artist all album
    private void getArtistAlbum(String id, String page) {
        Call<ArtistNewAlbum> songsListCall = Constants.service.getArtistAlbums(id, page);
        songsListCall.enqueue(new Callback<ArtistNewAlbum>() {
            @Override
            public void onResponse(Call<ArtistNewAlbum> call, Response<ArtistNewAlbum> response) {
                binding.progress.setVisibility(View.GONE);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                ArtistNewAlbum songsList = response.body();
                try {
                    if (songsList.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (songsList.artist.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            listAlbum.addAll(songsList.artist);
                            albumSongAdapter.notifyDataSetChanged();
                        } else {
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ArtistNewAlbum> call, Throwable t) {
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

    //Get all recent played album
    private void getRecentlyPlayedAlbum(int page_no) {
        Call<NewRelease> call = Constants.service.getRecentlyPlayedAlbum(Integer.toString(page_no));
        call.enqueue(new Callback<NewRelease>() {
            @Override
            public void onResponse(Call<NewRelease> call, Response<NewRelease> response) {
                try {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    binding.progress.setVisibility(View.GONE);
                    NewRelease album = response.body();
                    if (album.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(ViewAllActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (album.albums.size() > 0) {
                            isLoading = false;
                            isLastPage = false;
                            page_number = page_number + 1;
                            listAlbum.addAll(album.albums);
                            albumSongAdapter.notifyDataSetChanged();
                        } else {
                            isLoading = false;
                            isLastPage = true;
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
                binding.progress.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    public void updateIcon() {
        if (binding.recycleviewAll != null && binding.recycleviewAll.getAdapter() != null)
            binding.recycleviewAll.getAdapter().notifyDataSetChanged();
    }

    //Set bottom player view and nav bar view
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    //Get all song
    private void getGenreSongs(final String page_numberr, String genre) {
        Call<SongsList> songsListCall = Constants.service.getGenreSong(genre, page_numberr);
        songsListCall.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (Integer.parseInt(page_numberr) == 1) {
                    listPrefSong.clear();
                }
                if (response.isSuccessful()) {
                    final SongsList songsList = response.body();
                    if (songsList.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(ViewAllActivity.this);
                    } else {
                        binding.progress.setVisibility(View.GONE);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                if (songsList.songs != null) {
                                    if (songsList.songs.size() > 0) {
                                        isLoading = false;
                                        isLastPage = false;
                                        page_number = page_number + 1;
                                        if (listPrefSong.size() <= 0) {
                                            listPrefSong.addAll(songsList.songs);
                                            mAdapter.notifyDataSetChanged();
                                        } else {
                                            listPrefSong.addAll(songsList.songs);
                                            mAdapter.notifyItemRangeChanged(listPrefSong.size(), 30);
                                        }
                                        if (listAllSong.size() < listPrefSong.size()) {
                                            listAllSong.addAll(songsList.songs);
                                        }
                                    } else {
                                        isLoading = false;
                                        isLastPage = true;
                                    }
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
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

    //Get All albums on click view all artist
    private void getGenresArtist(String tag, String page) {
        Call<AdvanceSearch> call = Constants.service.getGenreArtist(tag, page);
        call.enqueue(new Callback<AdvanceSearch>() {
            @Override
            public void onResponse(Call<AdvanceSearch> call, Response<AdvanceSearch> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    AdvanceSearch search = response.body();
                    if (search != null) {
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (search.artists.size() > 0) {
                                isLoading = false;
                                isLastPage = false;
                                page_number = page_number + 1;
                                if (listSuggestionArtist.size() <= 0) {
                                    listSuggestionArtist.addAll(search.artists);
                                    artistAdvancedAdapter.notifyDataSetChanged();
                                } else {
                                    listSuggestionArtist.addAll(search.artists);
                                    artistAdvancedAdapter.notifyDataSetChanged();
                                }
                            } else {
                                isLoading = false;
                                isLastPage = true;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AdvanceSearch> call, Throwable t) {
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

    //Get All albums on click view all album
    private void getGenresAlbum(String tag, String page) {
        Call<GenreAlbum> call = Constants.service.getGenreAlbum(tag, page);
        call.enqueue(new Callback<GenreAlbum>() {
            @Override
            public void onResponse(Call<GenreAlbum> call, Response<GenreAlbum> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    GenreAlbum search = response.body();
                    if (search != null) {
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (search.albums.size() > 0) {
                                isLoading = false;
                                isLastPage = false;
                                page_number = page_number + 1;
                                if (listSuggestionAlbum.size() <= 0) {
                                    listSuggestionAlbum.addAll(search.albums);
                                    albumAdvancedAdapter.notifyDataSetChanged();
                                } else {
                                    listSuggestionAlbum.addAll(search.albums);
                                    albumAdvancedAdapter.notifyDataSetChanged();
                                }
                            } else {
                                isLoading = false;
                                isLastPage = true;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GenreAlbum> call, Throwable t) {
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
