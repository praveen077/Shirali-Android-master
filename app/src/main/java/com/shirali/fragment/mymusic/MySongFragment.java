package com.shirali.fragment.mymusic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.adapter.MySongsAdapter;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.databinding.FragmentMySongsBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.MySongsModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.playlist.PlaylistUpdate;
import com.shirali.model.recent.Recent;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.model.user.UserPlaylist;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomScrollListner;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by user on 10/7/17.
 */

public class MySongFragment extends Fragment implements View.OnClickListener {

    private FragmentMySongsBinding binding;
    private Context mContext;
    private ArrayList<MySongsModel> songsModelArrayList;
    private ArrayList<Song> listSongs;
    private String listRecentPlayed = "mySong";
    private MySongsAdapter mySongsAdapter;
    private RecentPlayedAdapter myRecentSongAdapter;
    private ArrayList<Song> artistModelArrayList;
    private CustomLoaderDialog dialog;
    private ArrayList<String> songList;
    private SharedPreferences preferences;
    private ArrayList<String> songsList;
    private ArrayList<Album> listNewAlbum;
    private ArrayList<Album> recentAlbam;
    private ArrayList<Song> played;
    private ArrayList<RelatedArtist> relatedArtist;
    private boolean isLoading = false;
    private int page_no = 1;
    private int count;
    private LinearLayoutManager mLayoutManager;
    private boolean isLastPage = false;
    private int noOfSong = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_songs, container, false);
        mContext = getActivity();
        listSongs = new ArrayList<>();
        songList = new ArrayList<>();
        songsList = new ArrayList<>();
        listNewAlbum = new ArrayList<>();
        recentAlbam = new ArrayList<>();
        played = new ArrayList<>();
        relatedArtist = new ArrayList<>();

        preferences = getActivity().getSharedPreferences("playlist", 0);
        songsModelArrayList = new ArrayList<>();
        artistModelArrayList = new ArrayList<>();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));

        if (getArguments() != null) {
            if (getArguments().get("selectsongs").equals("songs")) {
                dialog = new CustomLoaderDialog(mContext);

                /* --- KIPL -> AKM : Internet Check ---*/
                if(Utility.isConnectingToInternetWithoutAlert(getActivity())) {
                    dialog.show();
                    //getData();
                    getMyMusicData();
                    getPlaylistSong();
                }

                binding.header.setVisibility(View.VISIBLE);
                binding.recyclerviewSongs.setVisibility(View.VISIBLE);
                binding.lblDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (songList.size() > 0) {
                            addToPlaylist(songList);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("back_to_home"));
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", true));
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("update_data"));
                        } else {
                        Utility.showAlert(mContext, getString(R.string.Please_select_at_least_one_song_to_add));
                        }
                    }
                });
                binding.imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("back_to_home"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", true));
                    }
                });
                binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (Utility.isConnectingToInternet(getActivity())) {
                            if (!((Activity) mContext).isFinishing()) {
                                if (dialog != null) {
                                    dialog.show();
                                }
                            }
                            getMyMusicData();
                        }
                        binding.swipeLyt.setRefreshing(false);
                    }
                });


                binding.rvSongsFragment.setHasFixedSize(true);
                binding.rvSongsFragment.setLayoutManager(new LinearLayoutManager(mContext));
                mySongsAdapter = new MySongsAdapter(mContext, songsModelArrayList, listSongs, listRecentPlayed, songsList, "add_song", listNewAlbum);
                binding.rvSongsFragment.setAdapter(mySongsAdapter);
                mySongsAdapter.setOnItemClickListener(new MySongsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, ArrayList<String> list, String type) {
                        addToList(view, list, position);
                    }
                });
            }
        } else {
            dialog = new CustomLoaderDialog(mContext);
            getMyMusicDataWithPagination(getActivity(), page_no);
            binding.header.setVisibility(View.GONE);
            binding.rvSongsFragment.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(mContext);
            binding.rvSongsFragment.setLayoutManager(mLayoutManager);
            mySongsAdapter = new MySongsAdapter(mContext, songsModelArrayList, artistModelArrayList, listRecentPlayed, songsList, "mySong", listNewAlbum);
            binding.rvSongsFragment.setAdapter(mySongsAdapter);
            mySongsAdapter.setOnItemClick(new MySongsAdapter.OnItemClick() {
                @Override
                public void onItemClick(View view, int position, String type) {
                    if (type.equalsIgnoreCase("mySong")) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            playSong(view, position);
                        } else {
                            if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                playSong(view, position);
                            } else {
                                Album tempAlbum = new Album();
                                if (artistModelArrayList.get(position).albums != null) {
                                    if (artistModelArrayList.get(position).albums.size() > 0) {
                                        tempAlbum = artistModelArrayList.get(position).albums.get(0);
                                    }
                                }
                                if (artistModelArrayList.get(position).isPremium || artistModelArrayList.get(position).artist.isPremium || tempAlbum.isPremium) {
                                    if (artistModelArrayList.get(position).isPremium) {
                                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                                    } else if (artistModelArrayList.get(position).artist.isPremium) {
                                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                    } else {
                                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                                    }
                                } else {
                                    playSong(view, position);
                                }
                            }
                        }
                    } else {
                        try {
                            UserModel.getInstance().removeFromMusic(mContext, Utility.getUserInfo(mContext).id, artistModelArrayList.get(position).id);
                            artistModelArrayList.clear();
                            page_no = 1;
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getMyMusicDataWithPagination(mContext, page_no);
                                }
                            }, 300); //1000
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onItemVisible(View view, boolean isVisible) {

                }
            });

            binding.rvSongsFragment.addOnScrollListener(new CustomScrollListner(mLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    binding.progress.setVisibility(View.VISIBLE);
                    getMyMusicDataWithPagination(mContext, page_no);
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

            if(Utility.isConnectingToInternetWithoutAlert(getActivity())) {
                getRecentSong();
            }

            binding.recyclerviewSongs.setHasFixedSize(true);
            binding.recyclerviewSongs.setLayoutManager(new LinearLayoutManager(mContext));
            myRecentSongAdapter = new RecentPlayedAdapter(getActivity(), recentAlbam, played, relatedArtist, false, "recent_song");
            myRecentSongAdapter.menuAlpha();
            //AKM:NEXT_LOGIC
            myRecentSongAdapter.isAlbum();
            binding.recyclerviewSongs.setAdapter(myRecentSongAdapter);
            myRecentSongAdapter.setOnItemClickListener(new RecentPlayedAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    binding.emptyLyt.setVisibility(View.GONE);
                    binding.lytDataState.setVisibility(View.VISIBLE);
                    UserModel.getInstance().addToMusic(mContext, Utility.getUserInfo(mContext).id, played.get(position).id);
                    listSongs.clear();
                    artistModelArrayList.clear();
                    getMyMusicDataWithPagination(getActivity(), 1);
                }
            });

            binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (dialog != null) {
                        dialog.show();
                    }
                    page_no = 1;
                    isLoading = true;
                    artistModelArrayList.clear();
                    mySongsAdapter.notifyDataSetChanged();
                    getMyMusicDataWithPagination(getActivity(), 1);
                    binding.swipeLyt.setRefreshing(false);
                }
            });
        }


        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.imgSongAdded) {
            binding.emptyLyt.setVisibility(View.GONE);
            binding.rvSongsFragment.setVisibility(View.VISIBLE);
        }
    }

    private void playSong(final View v, final int position) {
        Constants.SONGS_LIST = artistModelArrayList;
        Constants.SONG_NUMBER = position;
        Constants.isSongPlay = true;
        UserModel.getInstance().getAppSetting(mContext);
        UserModel.getInstance().freePaidUser(mContext, artistModelArrayList, position, new FreePaidUserCallBack() {
            @Override
            public void freePaidUser(boolean ifPaid) {
                if (ifPaid) {
                    Constants.SONGS_LIST = artistModelArrayList;
                    Constants.SONG_NUMBER = position;
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                } else {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                }
            }
        });
    }

    private void addToPlaylist(ArrayList<String> song) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("id", preferences.getString("current", ""));
        hm.put("songs", song);
        hm.put("isActive", true);
        Call<PlaylistUpdate> call = Constants.service.addToPlaylist(preferences.getString("current", ""), hm);
        call.enqueue(new Callback<PlaylistUpdate>() {
            @Override
            public void onResponse(Call<PlaylistUpdate> call, Response<PlaylistUpdate> response) {
                PlaylistUpdate playlistUpdate = response.body();
                try {
                    if (playlistUpdate.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistUpdate.playlist != null) {
                            UserModel.getInstance().getdata(getActivity());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlaylistUpdate> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void getMyMusicData() {
        Call<MyMusic> songs = Constants.service.getMyMusic(Utility.getUserInfo(mContext).id);
        songs.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                MyMusic list = response.body();
                if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(getActivity());
                } else {
                    if (list.myMusicContain.song.size() > 0) {
                        listSongs.clear();
                        listSongs.addAll(list.myMusicContain.song);
                        mySongsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        Utility.showAlert(mContext, mContext.getResources().getString(R.string.something_went_wrong));
                    }
                }
                //
            }
        });
    }

    //Get song from my music
    public void getMyMusicDataWithPagination(final Context context, final int page) {
        Call<MyMusic> call = Constants.service.getMyMusicWithPage(Utility.getUserInfo(context).id, Integer.toString(page));
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                binding.swipeLyt.setRefreshing(false);
                if (page == 1) {
                    artistModelArrayList.clear();
                }
                MyMusic myMusic = response.body();
                if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                    try {
                        Utility.openSessionOutDialog(getActivity());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (myMusic.success) {
                        binding.progress.setVisibility(View.GONE);
                        if (myMusic.myMusicContain.song.size() > 0) {
                            page_no = page_no + 1;
                            binding.emptyLyt.setVisibility(View.GONE);
                            binding.rvSongsFragment.setVisibility(View.VISIBLE);
                            binding.lytDataState.setVisibility(View.VISIBLE);
                            artistModelArrayList.addAll(myMusic.myMusicContain.song);
                            isLoading = false;
                            isLastPage = false;
                            count = myMusic.count;
                            mySongsAdapter.notifyDataSetChanged();
                        } else {
                            count = myMusic.count;
                            if (count <= 0) {
                                binding.lytDataState.setVisibility(View.GONE);
                                binding.emptyLyt.setVisibility(View.VISIBLE);
                            }
                            isLoading = false;
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (dialog != null)
                    dialog.dismiss();
                binding.emptyLyt.setVisibility(View.VISIBLE);
                binding.rvSongsFragment.setVisibility(View.GONE);
            }
        });
    }

    //Get playlist song
    private void getPlaylistSong() {
        Call<UserPlaylist> call = Constants.service.getPlaylistSong(Utility.getUserInfo(mContext).id);
        call.enqueue(new Callback<UserPlaylist>() {
            @Override
            public void onResponse(Call<UserPlaylist> call, Response<UserPlaylist> response) {
                UserPlaylist playlist = response.body();
                try {
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(getActivity());
                    } else {
                        if (playlist.success) {
                            for (int i = 0; i < playlist.playlists.size(); i++) {
                                if (playlist.playlists.get(i).id.equalsIgnoreCase(preferences.getString("current", ""))) {
                                    for (int j = 0; j < playlist.playlists.get(i).songs.size(); j++) {
                                        songsList.add(playlist.playlists.get(i).songs.get(j).id);
                                    }

                                }
                            }
                            noOfSong = songsList.size();
                            if (noOfSong != 0) {
                                binding.lblNoOfSongs.setText(Integer.toString(noOfSong));
                            }
                            mySongsAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserPlaylist> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //Get last 10 recent played song list
    public void getRecentSong() {
        Call<Recent> call = Constants.service.getRecentNew(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<Recent>() {
            @Override
            public void onResponse(Call<Recent> call, Response<Recent> response) {
                /*if (dialog != null)
                    dialog.dismiss();*/
                Recent home = response.body();
                if (home.message.equalsIgnoreCase("Invalid device login.")) {
                    try {
                        Utility.openSessionOutDialog(getActivity());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (home.success) {
                        if (home.recentlyPlayed.recentlyPlayed.size() >= 0) {
                            played.addAll(home.recentlyPlayed.recentlyPlayed);
                        }
                        myRecentSongAdapter.notifyDataSetChanged();

                    }
                }
            }

            @Override
            public void onFailure(Call<Recent> call, Throwable t) {
                //AKM
                //Utility.showAlert(mContext, getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    public void updateIcon() {
        if (binding.rvSongsFragment != null && binding.rvSongsFragment.getAdapter() != null)
            binding.rvSongsFragment.getAdapter().notifyDataSetChanged();
    }

    private void addToList(View view, ArrayList<String> list, int position) {
        songList = list;
        songList.add(listSongs.get(position).id);
        view.findViewById(R.id.img_plus).setVisibility(View.GONE);
        noOfSong++;
        binding.lblNoOfSongs.setText(Integer.toString(noOfSong));
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
        }
    };

}
