package com.shirali.fragment.mymusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.adapter.MyMusicAdapter;
import com.shirali.databinding.FragmentMyAlbumBinding;
import com.shirali.model.MyMusicModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sagar on 10/7/17.
 */

public class MyAlbumFragment extends Fragment {

    private FragmentMyAlbumBinding binding;
    private Context mContext;
    private ArrayList<MyMusicModel> myArtistModelArrayList;
    private ArrayList<Song> artistModelArrayList;
    private ArrayList<Album> albumArrayList;
    private MyMusicAdapter myMusicAdapter;
    private CustomLoaderDialog dialog;
    private ArrayList<String> artist_id;
    private ArrayList<String> listArtist;
    private ArrayList<String> song_list;
    private String artist_name;
    private ArrayList<String> listSong;
    private String artist;
    private boolean isFirstLoad = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_album, container, false);
        mContext = getActivity();
        dialog = new CustomLoaderDialog(mContext);
        artist_id = new ArrayList<>();
        song_list = new ArrayList<>();
        listArtist = new ArrayList<>();
        listSong = new ArrayList<>();

        if(Utility.isConnectingToInternet(mContext)) {
            getMyMusicData(false);
        }
        isFirstLoad = true;

        myArtistModelArrayList = new ArrayList<>();
        artistModelArrayList = new ArrayList<>();
        albumArrayList = new ArrayList<>();

        binding.rvAlbumFragment.setHasFixedSize(true);
        binding.rvAlbumFragment.setLayoutManager(new LinearLayoutManager(mContext));
        myMusicAdapter = new MyMusicAdapter(mContext, artistModelArrayList, myArtistModelArrayList, albumArrayList, "MyAlbum");
        binding.rvAlbumFragment.setAdapter(myMusicAdapter);
        myMusicAdapter.setClickListner(new MyMusicAdapter.OnClickListner() {
            @Override
            public void onItemClick(View view, int position, String share) {
                if (share.equalsIgnoreCase("open")) {
                    UserModel.getInstance().tempAlbum = albumArrayList.get(position);
                    if (albumArrayList.get(position).isPremium) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            openAlbum(position);
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        openAlbum(position);
                    }
                } else {
                    try {
                        Utility.shareIt(getActivity(), "album", albumArrayList.get(position).title, artist_name, albumArrayList.get(position).shareUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        myMusicAdapter.setOnItemClickListener(new MyMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getSongAccordingArtist(artist_id.get(position));
                artist = artist_id.get(position);
            }
        });

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* --- KIPL -> AKM : Internet Check ---*/
                if (Utility.isConnectingToInternet(getActivity())) {
                    if (!((Activity) mContext).isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    getMyMusicData(true);
                }
                binding.swipeLyt.setRefreshing(false);

            }
        });
        return binding.getRoot();
    }

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().album = albumArrayList.get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mContext, AlbumDetailActivity.class);
        intent.putExtra("artist", artist_name);
        mContext.startActivity(intent);
    }

    //Get album of my music song
    public void getMyMusicData(final boolean isPullToRefresh) {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                MyMusic myMusic = response.body();
                try {
                    if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (myMusic.success) {
                            artist_id.clear();
                            artistModelArrayList.clear();
                            albumArrayList.clear();
                            if (myMusic.myMusicContain.song.size() == 0) {
                                binding.layoutDemo.setVisibility(View.VISIBLE);
                                binding.rvAlbumFragment.setVisibility(View.GONE);
                            } else {
                                binding.layoutDemo.setVisibility(View.GONE);
                                binding.rvAlbumFragment.setVisibility(View.VISIBLE);
                                for (int i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                                    if (artist_id.size() <= 0) {
                                        artist_name = myMusic.myMusicContain.song.get(i).artist.name;
                                        for (int j = 0; j < myMusic.myMusicContain.song.get(i).albums.size(); j++) {
                                            if (!artist_id.contains(myMusic.myMusicContain.song.get(i).albums.get(j).id)) {
                                                artist_id.add(myMusic.myMusicContain.song.get(i).albums.get(j).id);
                                                albumArrayList.add(myMusic.myMusicContain.song.get(i).albums.get(j));
                                                artistModelArrayList.add(myMusic.myMusicContain.song.get(i));
                                            }
                                        }
                                    } else {
                                        for (int j = 0; j < myMusic.myMusicContain.song.get(i).albums.size(); j++) {
                                            if (!artist_id.contains(myMusic.myMusicContain.song.get(i).albums.get(j).id)) {
                                                artist_id.add(myMusic.myMusicContain.song.get(i).albums.get(j).id);
                                                albumArrayList.add(myMusic.myMusicContain.song.get(i).albums.get(j));
                                                artistModelArrayList.add(myMusic.myMusicContain.song.get(i));
                                            }
                                        }
                                    }
                                }
                                myMusicAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isPullToRefresh) {
                    if (!((Activity) mContext).isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                binding.layoutDemo.setVisibility(View.VISIBLE);
                binding.rvAlbumFragment.setVisibility(View.GONE);

            }
        });
    }

    // remove all song of particular album song
    private void getSongAccordingArtist(final String id) {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                MyMusic myMusic = response.body();
                if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(getActivity());
                } else {
                    if (myMusic.myMusicContain.song.size() > 0) {
                        for (int i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                            if (myMusic.myMusicContain.song.get(i).albums.size() > 0) {
                                if (myMusic.myMusicContain.song.get(i).albums.get(0).id.equalsIgnoreCase(id))
                                    song_list.add(myMusic.myMusicContain.song.get(i).id);
                            }
                        }
                        addedToMyMusic(Utility.getUserInfo(mContext).myMusic, song_list, true);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //Add and remove song from my music
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
            song_list.clear();
            for (int i = 0; i < albumArrayList.size(); i++) {
                if (albumArrayList.get(i).id.equalsIgnoreCase(artist)) {
                    albumArrayList.remove(i);
                }
            }
            myMusicAdapter.notifyDataSetChanged();
            if (Utility.getStringPreferences(getActivity(), Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                Utility.showPopup(mContext, getString(R.string.album_removed));
            } else {
                Utility.showPopup(mContext, getString(R.string.album_removed));
            }
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong);
            Utility.showPopup(mContext, getString(R.string.album_added));
        }
    }

    //Add Song into my music
    private void addSong(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", list);

        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(mContext).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    UserModel user = response.body();
                    try {
                        if (user != null) {
                            if (user.message.equalsIgnoreCase("Invalid device login.")) {
                                Utility.openSessionOutDialog(mContext);
                            } else {
                                if (user.success) {
                                    UserModel.getInstance().getdata(mContext);
                                    getMyMusicData(false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    public void reload() {
        getMyMusicData(false);
    }
}
