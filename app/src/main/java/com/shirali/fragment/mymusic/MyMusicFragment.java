package com.shirali.fragment.mymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.adapter.MyMusicPageAdapter;
import com.shirali.databinding.FragmentMyMusicBinding;
import com.shirali.util.Constants;

public class MyMusicFragment extends Fragment {

    private FragmentMyMusicBinding binding;
    private MyMusicPageAdapter adapter;
    private MixpanelAPI mixpanelAPI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_music, container, false);

        mixpanelAPI = MixpanelAPI.getInstance(getActivity(), Constants.PROJECT_TOKEN);
        TabLayout.Tab playlistsTab = binding.tabMyMusic.newTab();
        playlistsTab.setText(getActivity().getResources().getString(R.string.playlist));
        binding.tabMyMusic.addTab(playlistsTab);

        TabLayout.Tab artistsTab = binding.tabMyMusic.newTab();
        artistsTab.setText(getActivity().getResources().getString(R.string.artist));
        binding.tabMyMusic.addTab(artistsTab);

        TabLayout.Tab albumsTab = binding.tabMyMusic.newTab();
        albumsTab.setText(getActivity().getResources().getString(R.string.albums));
        binding.tabMyMusic.addTab(albumsTab);

        TabLayout.Tab songsTab = binding.tabMyMusic.newTab();
        songsTab.setText(getActivity().getResources().getString(R.string.songs));
        binding.tabMyMusic.addTab(songsTab);

        adapter = new MyMusicPageAdapter(getFragmentManager(), binding.tabMyMusic.getTabCount());
        binding.pagerMyMusic.setAdapter(adapter);
        binding.pagerMyMusic.setCurrentItem(0);
        binding.pagerMyMusic.setOffscreenPageLimit(3);
        binding.pagerMyMusic.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabMyMusic));

        binding.pagerMyMusic.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mixpanelAPI.track("My Music:Playlists");
                    MyPlaylistFragment frg = (MyPlaylistFragment) adapter.instantiateItem(binding.pagerMyMusic, position);
                    frg.reload();
                } else if (position == 1) {
                    mixpanelAPI.track("My Music:Artists");
                    MyArtistFragment frg = (MyArtistFragment) adapter.instantiateItem(binding.pagerMyMusic, position);
                    frg.reload();
                } else if (position == 2) {
                    mixpanelAPI.track("My Music:Albums");
                    MyAlbumFragment frg = (MyAlbumFragment) adapter.instantiateItem(binding.pagerMyMusic, position);
                    frg.reload();
                } else {
                    mixpanelAPI.track("My Music:Songs");
                    MySongFragment frg = (MySongFragment) adapter.instantiateItem(binding.pagerMyMusic, position);
                    //frg.reload();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(open_add_more);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(open_add_more, new IntentFilter("open_add_more"));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tabMyMusic.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.pagerMyMusic.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    mixpanelAPI.track("My Music:Playlists");
                } else if (tab.getPosition() == 2) {
                    mixpanelAPI.track("My Music:Artists");
                } else if (tab.getPosition() == 3) {
                    mixpanelAPI.track("My Music:Albums");
                } else {
                    mixpanelAPI.track("My Music:Songs");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    BroadcastReceiver open_add_more = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", false));
            MySongFragment fragment = new MySongFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectsongs", "songs");
            fragment.setArguments(bundle);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, fragment);
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                ft.commitNow();
            } else {
                ft.commit();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mixpanelAPI.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}







