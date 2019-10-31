package com.shirali.fragment.mymusic;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.adapter.MyMusicAdapter;
import com.shirali.databinding.FragmentMyGenresBinding;
import com.shirali.model.mymusic.Album;
import com.shirali.model.MyMusicModel;
import com.shirali.model.songs.Song;

import java.util.ArrayList;

/**
 * Created by Sagar on 10/7/17.
 */

public class MyGenreFragment extends Fragment {
    private FragmentMyGenresBinding binding;
    private Context mContext;
    private ArrayList<MyMusicModel> genreList;
    private ArrayList<Song> songsList;
    private ArrayList<Album> albumArrayList;
    private ArrayList<String> listArtist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_genres, container, false);
        mContext = getActivity();

        genreList = new ArrayList<>();
        songsList = new ArrayList<>();
        albumArrayList = new ArrayList<>();
        listArtist = new ArrayList<>();

        for (int i = 0; i <5; i++) {
            genreList.add(new MyMusicModel("Classical Music", "2", "21"));  }

        binding.rvGenresFragment.setHasFixedSize(true);
        binding.rvGenresFragment.setLayoutManager(new LinearLayoutManager(mContext));
        MyMusicAdapter myMusicAdapter = new MyMusicAdapter(mContext, songsList, genreList, albumArrayList, "MyGenre");
        binding.rvGenresFragment.setAdapter(myMusicAdapter);
        return binding.getRoot();
    }
}
