package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.databinding.MusicAlbumCellBinding;
import com.shirali.model.songs.Song;
import com.shirali.model.stations.Stations;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;

public class MySongPagerAdapter extends PagerAdapter {
    ArrayList<Song> musicList;
    Context context;
    ArrayList<Stations> listOfStation;

    public MySongPagerAdapter(Context context, ArrayList<Song> musicList, ArrayList<Stations> listOfStation) {
        this.context = context;
        this.musicList = musicList;
        this.listOfStation = listOfStation;
    }

    @Override
    public int getCount() {
        if (Constants.isSongPlay) {
            return this.musicList.size();
        } else {
            return this.listOfStation.size();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final MusicAlbumCellBinding binding = DataBindingUtil.inflate(inflater, R.layout.music_album_cell, container, false);
        if (Constants.isSongPlay || Utility.getBooleaPreferences(context,"tempForSong")) {
            if (!musicList.get(position).artwork.equalsIgnoreCase("")) {
                try {
                    Glide.with(context).load(this.musicList.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            binding.loaderIcon.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            binding.loaderIcon.setVisibility(View.GONE);
                            return false;
                        }
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgVideoThumb2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!listOfStation.get(position).imageurl.equalsIgnoreCase("")) {
                try {
                    Glide.with(context).load(this.listOfStation.get(position).imageurl).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            binding.loaderIcon.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            binding.loaderIcon.setVisibility(View.GONE);
                            return false;
                        }
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgVideoThumb2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void updateList(ArrayList<Song> updatedList){
        this.musicList = updatedList;
        notifyDataSetChanged();
    }
}
