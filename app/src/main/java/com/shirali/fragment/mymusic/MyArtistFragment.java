package com.shirali.fragment.mymusic;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.adapter.MyMusicAdapter;
import com.shirali.databinding.FragmentMyArtistBinding;
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

public class MyArtistFragment extends Fragment implements View.OnClickListener {
    private FragmentMyArtistBinding binding;
    private Context mContext;
    private ArrayList<MyMusicModel> myArtistModelArrayList;
    private ArrayList<Song> artistModelArrayList;
    private MyMusicAdapter myMusicAdapter;
    private ArrayList<Song> myMusic;
    private CustomLoaderDialog dialog;
    private ArrayList<Album> albumArrayList;
    private ArrayList<String> artist_id;
    private ArrayList<String> song_list;
    private ArrayList<String> listArtist;
    private ArrayList<String> listSong;
    private String artist;
    private boolean isFirstLoad = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_artist, container, false);
        mContext = getActivity();
        dialog = new CustomLoaderDialog(mContext);

        artist_id = new ArrayList<>();
        myArtistModelArrayList = new ArrayList<>();
        artistModelArrayList = new ArrayList<>();
        albumArrayList = new ArrayList<>();
        song_list = new ArrayList<>();
        listArtist = new ArrayList<>();
        listSong = new ArrayList<>();

        if(Utility.isConnectingToInternet(getActivity())) {
            if (!((Activity) mContext).isFinishing()) {
                if (dialog != null) {
                    dialog.show();
                }
            }
            getMyMusicData();
        }
        isFirstLoad = true;

        binding.rvArtistFragment.setHasFixedSize(true);
        binding.rvArtistFragment.setLayoutManager(new LinearLayoutManager(mContext));
        myMusicAdapter = new MyMusicAdapter(mContext, artistModelArrayList, myArtistModelArrayList, albumArrayList, "MyArtist");
        binding.rvArtistFragment.setAdapter(myMusicAdapter);
        myMusicAdapter.notifyDataSetChanged();
        myMusicAdapter.setOnItemClickListener(new MyMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                artist = artist_id.get(position);
                getSongAccordingArtist(artist_id.get(position));
            }
        });

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* --- KIPL -> AKM : Internet Check---*/
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
        binding.imgForAddArtist.setOnClickListener(this);
        return binding.getRoot();
    }

    //Get artist of my music songs
    public void getMyMusicData() {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    MyMusic myMusic = response.body();
                    try {
                        if (myMusic != null) {
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
                                    binding.emptyLyt.setVisibility(View.GONE);
                                    binding.rvArtistFragment.setVisibility(View.VISIBLE);
                                    if (myMusic.myMusicContain.song.size() <= 0) {
                                        binding.emptyLyt.setVisibility(View.VISIBLE);
                                        binding.rvArtistFragment.setVisibility(View.GONE);
                                    } else {
                                        binding.emptyLyt.setVisibility(View.GONE);
                                        binding.rvArtistFragment.setVisibility(View.VISIBLE);
                                        for (int i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                                            if (artist_id.size() <= 0) {
                                                if (!artist_id.contains(myMusic.myMusicContain.song.get(i).artist.id)) {
                                                    artist_id.add(myMusic.myMusicContain.song.get(i).artist.id);
                                                    artistModelArrayList.add(myMusic.myMusicContain.song.get(i));
                                                }
                                            } else {
                                                if (!artist_id.contains(myMusic.myMusicContain.song.get(i).artist.id)) {
                                                    artist_id.add(myMusic.myMusicContain.song.get(i).artist.id);
                                                    artistModelArrayList.add(myMusic.myMusicContain.song.get(i));
                                                }
                                            }
                                        }
                                        myMusicAdapter.notifyDataSetChanged();
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
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                binding.emptyLyt.setVisibility(View.VISIBLE);
                binding.rvArtistFragment.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
    }


    //Filter songs which is exist or not into my music
    private void getSongAccordingArtist(final String id) {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                MyMusic myMusic = response.body();
                if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(getActivity());
                } else {
                    for (int i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                        if (myMusic.myMusicContain.song.get(i).artist.id.equalsIgnoreCase(id))
                            song_list.add(myMusic.myMusicContain.song.get(i).id);
                    }
                    addedToMyMusic(Utility.getUserInfo(mContext).myMusic, song_list, true);
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


    //Perform add and remove song into my music
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
            for (int i = 0; i < artistModelArrayList.size(); i++) {
                if (artistModelArrayList.get(i).artist.id.equalsIgnoreCase(artist)) {
                    artistModelArrayList.remove(i);
                }
            }
            myMusicAdapter.notifyDataSetChanged();
            if (Utility.getStringPreferences(getActivity(), Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                Utility.showPopup(mContext, getString(R.string.artist_removed));
            } else {
                Utility.showPopup(mContext, getString(R.string.artist_removed));
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
                                    getMyMusicData();
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
        getMyMusicData();
    }
}
