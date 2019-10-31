package com.shirali.fragment.mymusic;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.CreatePlaylistViewBinding;

/**
 * Created by user on 11/7/17.
 */

public class CreatePlaylistFragment extends Fragment implements View.OnClickListener {

    private CreatePlaylistViewBinding binding;
    private Context mContext;
    private Fragment fragment;
    public static boolean isFromPlaylist = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.create_playlist_view, container, false);
        mContext = getActivity();
        isFromPlaylist = true;
        fragment = new MyArtistFragment();

        TabLayout.Tab artistTab = binding.tabMyMusic.newTab();
        artistTab.setText("Artists");
        binding.tabMyMusic.addTab(artistTab);

        TabLayout.Tab albumsTab = binding.tabMyMusic.newTab();
        albumsTab.setText("Albums");
        binding.tabMyMusic.addTab(albumsTab);

        TabLayout.Tab songsTab = binding.tabMyMusic.newTab();
        songsTab.setText("Songs");
        binding.tabMyMusic.addTab(songsTab);

        TabLayout.Tab genresTab = binding.tabMyMusic.newTab();
        genresTab.setText("Genres");
        binding.tabMyMusic.addTab(genresTab);

        PlaylistPagerAdapter adapter = new PlaylistPagerAdapter(getFragmentManager(), binding.tabMyMusic.getTabCount());
        binding.pagerMyMusic.setAdapter(adapter);
        binding.pagerMyMusic.setCurrentItem(0);
        binding.pagerMyMusic.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabMyMusic));

        binding.tabMyMusic.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.pagerMyMusic.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.toolbarCancel.setOnClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.toolbarCancel) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible",true));
            isFromPlaylist = false;
            fragment = new MyMusicFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, fragment);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            try {
                if (currentAPIVersion >= Build.VERSION_CODES.M) {
                    ft.commitNow();
                } else {
                    ft.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class PlaylistPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PlaylistPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    MyArtistFragment artistFragment = new MyArtistFragment();
                    return artistFragment;
                case 1:
                    MyAlbumFragment albumFragment = new MyAlbumFragment();
                    return albumFragment;
                case 2:
                    MySongFragment songFragment = new MySongFragment();
                    return songFragment;
                case 3:
                    MyGenreFragment genreFragment = new MyGenreFragment();
                    return genreFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}
