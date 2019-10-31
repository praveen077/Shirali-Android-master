package com.shirali.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.shirali.fragment.mymusic.MyAlbumFragment;
import com.shirali.fragment.mymusic.MyArtistFragment;
import com.shirali.fragment.mymusic.MyPlaylistFragment;
import com.shirali.fragment.mymusic.MySongFragment;

/**
 * Created by Sagar on 12/7/17.
 */

public class MyMusicPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public MyMusicPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                MyPlaylistFragment tab1 = new MyPlaylistFragment();
                return tab1;
            case 1:
                MyArtistFragment tab2 = new MyArtistFragment();
                return tab2;
            case 2:
                MyAlbumFragment tab3 = new MyAlbumFragment();
                return tab3;
            case 3:
                MySongFragment tab4 = new MySongFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
























