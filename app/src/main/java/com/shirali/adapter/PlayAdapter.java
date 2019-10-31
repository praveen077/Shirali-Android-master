package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.databinding.ItemPlaylistBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Sagar on 1/8/17.
 */

public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.ViewHolder> {
    private final Context mContext;
    private ItemPlaylistBinding binding;
    private ArrayList<Song> listSong;
    private OnItemClickListener mItemClickListener;

    public PlayAdapter(Context context, ArrayList<Song> listSong) {
        this.mContext = context;
        this.listSong = listSong;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_playlist, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            Glide.with(mContext).load(listSong.get(position).artwork).placeholder(R.drawable.imglogo).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    return false;
                }
            }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(holder.binding.img);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).matches("iw")) {
            if (listSong.get(position).isPremium) {
                holder.binding.imgTag.setVisibility(View.VISIBLE);
                holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
            } else {
                holder.binding.imgTag.setVisibility(View.GONE);
            }
        } else {
            if (listSong.get(position).isPremium) {
                holder.binding.imgTag.setVisibility(View.VISIBLE);
                holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
            } else {
                holder.binding.imgTag.setVisibility(View.GONE);
            }
        }

        holder.binding.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Constants.isSongPlay = true;
                UserModel.getInstance().isSingleSongPlay = true;
                UserModel.getInstance().tempSongList.clear();
                UserModel.getInstance().tempSongList.addAll(listSong);
                UserModel.getInstance().listOfShuffleSong.clear();
                UserModel.getInstance().listOfShuffleSong.addAll(listSong);
                Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                UserModel.getInstance().listOfActualSong.clear();
                UserModel.getInstance().listOfActualSong.addAll(listSong);

                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    playSong(v, position);
                } else {
                    Album tempAlbum = new Album();
                    if (listSong.get(position).albums != null) {
                        if (listSong.get(position).albums.size() > 0){
                            tempAlbum = listSong.get(position).albums.get(0);
                        }
                    }
                    if (listSong.get(position).isPremium || listSong.get(position).artist.isPremium || tempAlbum.isPremium) {
                        if (listSong.get(position).isPremium) {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                        } else if(listSong.get(position).artist.isPremium){
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    }else {
                        playSong(v, position);
                    }
                }
            }
        });
    }

    private void playSong(final View v, final int position) {
        Constants.SONGS_LIST = listSong;
        Constants.SONG_NUMBER = position;
        UserModel.getInstance().getAppSetting(mContext);
        UserModel.getInstance().freePaidUser(mContext, listSong, position, new FreePaidUserCallBack() {
            @Override
            public void freePaidUser(boolean ifPaid) {
                if (ifPaid) {
                    mItemClickListener.onItemClick(v, position);
                } else {
                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public int getItemCount() {
        return listSong != null ? listSong.size() : 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemPlaylistBinding binding;

        public ViewHolder(View itemView, ItemPlaylistBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}

