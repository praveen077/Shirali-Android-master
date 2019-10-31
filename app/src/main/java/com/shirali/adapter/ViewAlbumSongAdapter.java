package com.shirali.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.databinding.ViewAllCellBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Sagar on 3/9/17.
 */

public class ViewAlbumSongAdapter extends RecyclerView.Adapter<ViewAlbumSongAdapter.ViewHolder> {
    private ArrayList<Song> listSong;
    private ArrayList<Album> listAlbum;
    private String isFrom;
    private Context context;

    private OnItemClickListener mItemClickListener;
    private Album tempAlbum = new Album();

    public ViewAlbumSongAdapter(Context context, ArrayList<Song> song, ArrayList<Album> album, String isFrom) {
        this.context = context;
        this.listSong = song;
        this.listAlbum = album;
        this.isFrom = isFrom;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewAllCellBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_all_cell, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (isFrom.equalsIgnoreCase("album")) {
            try {
                Utility.loadMailPlayImage(listAlbum.get(position).artwork, context).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.binding.loaderIcon.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.binding.loaderIcon.setVisibility(View.GONE);
                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).into(holder.binding.imgItemcell);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listAlbum.get(position).titleHebrew.equalsIgnoreCase("") || listAlbum.get(position).titleHebrew == null) {
                        holder.binding.title.setText(listAlbum.get(position).title);
                    } else {
                        holder.binding.title.setText(listAlbum.get(position).titleHebrew);
                    }
                    if (listAlbum.get(position).artist.nameHebrew.equalsIgnoreCase("") || listAlbum.get(position).artist.nameHebrew == null) {
                        holder.binding.subTitle.setText(listAlbum.get(position).artist.name);
                    } else {
                        holder.binding.subTitle.setText(listAlbum.get(position).artist.nameHebrew);
                    }
                    if (listAlbum.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                } else {
                    holder.binding.title.setText(listAlbum.get(position).title);
                    holder.binding.subTitle.setText(listAlbum.get(position).artist.name);
                    if (listAlbum.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Utility.loadMailPlayImage(listSong.get(position).artwork, context).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.binding.loaderIcon.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.binding.loaderIcon.setVisibility(View.GONE);
                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).into(holder.binding.imgItemcell);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listSong.get(position).title_hebrew.equalsIgnoreCase("") || listSong.get(position).title_hebrew == null) {
                        holder.binding.title.setText(listSong.get(position).title);
                    } else {
                        holder.binding.title.setText(listSong.get(position).title_hebrew);
                    }
                    if (listSong.get(position).artist.nameHebrew.equalsIgnoreCase("") || listSong.get(position).artist.nameHebrew == null) {
                        holder.binding.subTitle.setText(listSong.get(position).artist.name);
                    } else {
                        holder.binding.subTitle.setText(listSong.get(position).artist.nameHebrew);
                    }
                    if (listSong.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                } else {
                    holder.binding.title.setText(listSong.get(position).title);
                    holder.binding.subTitle.setText(listSong.get(position).artist.name);
                    if (listSong.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.binding.lytAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrom.equalsIgnoreCase("album")) {
                    UserModel.getInstance().tempAlbum = listAlbum.get(position);
                    if (listAlbum.get(position).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            try {
                                UserModel.getInstance().album = listAlbum.get(position);
                                if (listAlbum.get(position).artist != null) {
                                    UserModel.getInstance().artist_id = listAlbum.get(position).artist;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            context.startActivity(new Intent(context, AlbumDetailActivity.class).putExtra("id", listAlbum.get(position).id));
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        try {
                            UserModel.getInstance().album = listAlbum.get(position);
                            if (listAlbum.get(position).artist != null) {
                                UserModel.getInstance().artist_id = listAlbum.get(position).artist;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        context.startActivity(new Intent(context, AlbumDetailActivity.class).putExtra("id", listAlbum.get(position).id));
                    }
                } else {
                    Constants.isSongPlay = true;
                    UserModel.getInstance().isSingleSongPlay = true;
                    UserModel.getInstance().listOfShuffleSong.clear();
                    UserModel.getInstance().listOfShuffleSong.addAll(listSong);
                    Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                    UserModel.getInstance().listOfActualSong.clear();
                    UserModel.getInstance().listOfActualSong.addAll(listSong);
                    UserModel.getInstance().tempSongList.clear();
                    UserModel.getInstance().tempSongList.addAll(listSong);
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        playSong(v, position);
                    } else {
                        if (listSong.get(position).albums != null) {
                            if (listSong.get(position).albums.size() > 0) {
                                tempAlbum = listSong.get(position).albums.get(0);
                            }
                        }
                        if (listSong.get(position).isPremium || listSong.get(position).artist.isPremium || tempAlbum.isPremium) {
                            if (listSong.get(position).isPremium) {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                            } else if (listSong.get(position).artist.isPremium) {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                            }
                        } else {
                            playSong(v, position);
                        }
                    }
                }
            }
        });
    }

    private void playSong(final View v, final int position) {
        Constants.SONGS_LIST = listSong;
        Constants.SONG_NUMBER = position;
        UserModel.getInstance().getAppSetting(context);
        UserModel.getInstance().freePaidUser(context, listSong, position, new FreePaidUserCallBack() {
            @Override
            public void freePaidUser(boolean ifPaid) {
                if (ifPaid) {
                    mItemClickListener.onItemClick(v, position);
                } else {
                    if (listSong.get(position).isPremium) {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    } else {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isFrom.equalsIgnoreCase("album")) {
            return listAlbum.size();
        } else {
            return listSong.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewAllCellBinding binding;

        public ViewHolder(View itemView, ViewAllCellBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
