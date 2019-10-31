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
import com.shirali.databinding.HomeItemLayoutBinding;
import com.shirali.model.browse.HomeData;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;

/**
 * Created by Sagar on 15/5/18.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private HomeItemLayoutBinding binding;
    private Context context;
    private ArrayList<HomeData> listHomeData;
    private HomeAdapter.OnItemClickListener mItemClickListener;

    public HomeAdapter(Context context, ArrayList<HomeData> listData) {
        this.context = context;
        this.listHomeData = listData;
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.home_item_layout, parent, false);
        return new HomeAdapter.ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final HomeAdapter.ViewHolder holder, int position) {
        if (listHomeData.size() > 0) {
            if (listHomeData.get(position).type.equalsIgnoreCase("artist")) {
                holder.binding.lytImgSquare.setVisibility(View.GONE);
                holder.binding.imgCircle.setVisibility(View.VISIBLE);
                Glide.with(context).load(listHomeData.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgCircle);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                    if (listHomeData.get(position).titleHebrew == null || listHomeData.get(position).titleHebrew.equalsIgnoreCase("")) {
                        holder.binding.title.setText(listHomeData.get(position).title);
                    } else {
                        holder.binding.title.setText(listHomeData.get(position).titleHebrew);
                    }
                    holder.binding.subTitle.setText(context.getResources().getString(R.string.artist_tag));
                } else {
                    holder.binding.title.setText(listHomeData.get(position).title);
                    holder.binding.subTitle.setText(context.getResources().getString(R.string.artist_tag));
                }
            } else {
                holder.binding.imgCircle.setVisibility(View.GONE);
                holder.binding.lytImgSquare.setVisibility(View.VISIBLE);
                try {
                    Glide.with(context).load(listHomeData.get(position).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgSquare);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                    if (listHomeData.get(position).titleHebrew == null || listHomeData.get(position).titleHebrew.equalsIgnoreCase("")) {
                        holder.binding.title.setText(listHomeData.get(position).title);
                    } else {
                        holder.binding.title.setText(listHomeData.get(position).titleHebrew);
                    }

                    if (listHomeData.get(position).type.equalsIgnoreCase("song")) {
                        if (listHomeData.get(position).artist != null) {
                            if (listHomeData.get(position).artist.nameHebrew == null || listHomeData.get(position).artist.nameHebrew.equalsIgnoreCase("")) {
                                holder.binding.subTitle.setText(context.getString(R.string.song_by) + " " + listHomeData.get(position).artist.name);
                            } else {
                                holder.binding.subTitle.setText(context.getString(R.string.song_by) + " " + listHomeData.get(position).artist.nameHebrew);
                            }
                        }
                    } else if (listHomeData.get(position).type.equalsIgnoreCase("album")) {
                        if (listHomeData.get(position).artist != null) {
                            if (listHomeData.get(position).artist.nameHebrew == null || listHomeData.get(position).artist.nameHebrew.equalsIgnoreCase("")) {
                                holder.binding.subTitle.setText(context.getString(R.string.album_by) + " " + listHomeData.get(position).artist.name);
                            } else {
                                holder.binding.subTitle.setText(context.getString(R.string.album_by) + " " + listHomeData.get(position).artist.nameHebrew);
                            }
                        }
                    } else {
                        holder.binding.subTitle.setText(R.string.playlist_by_shirali);
                    }

                    if (listHomeData.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                } else {
                    holder.binding.title.setText(listHomeData.get(position).title);
                    if (listHomeData.get(position).type.equalsIgnoreCase("song")) {
                        if (listHomeData.get(position).artist != null) {
                            holder.binding.subTitle.setText(context.getString(R.string.song_by) + " " + listHomeData.get(position).artist.nameHebrew);
                        }
                    } else if (listHomeData.get(position).type.equalsIgnoreCase("album")) {
                        if (listHomeData.get(position).artist != null) {
                            holder.binding.subTitle.setText(context.getString(R.string.album_by) + " " + listHomeData.get(position).artist.nameHebrew);
                        }
                    } else {
                        holder.binding.subTitle.setText(R.string.playlist_by_shirali);
                    }
                    if (listHomeData.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return listHomeData.size();
    }

    public void setOnItemClickListener(final HomeAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String type, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private HomeItemLayoutBinding binding;

        public ViewHolder(View itemView, final HomeItemLayoutBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listHomeData.get(getAdapterPosition()).type.equalsIgnoreCase("song")) {
                        Constants.isSongPlay = true;
                        UserModel.getInstance().isSingleSongPlay = true;
                        if (listHomeData.get(getAdapterPosition()).isPremium) {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                mItemClickListener.onItemClick(v, "song", getAdapterPosition());
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                            }
                        } else {
                            mItemClickListener.onItemClick(v, "song", getAdapterPosition());
                        }
                    } else if (listHomeData.get(getAdapterPosition()).type.equalsIgnoreCase("album")) {
                        if (listHomeData.get(getAdapterPosition()).isPremium) {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                mItemClickListener.onItemClick(v, "album", getAdapterPosition());
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                            }
                        } else {
                            mItemClickListener.onItemClick(v, "album", getAdapterPosition());
                        }
                    } else if (listHomeData.get(getAdapterPosition()).type.equalsIgnoreCase("artist")) {
                        mItemClickListener.onItemClick(v, "artist", getAdapterPosition());
                    } else {
                        mItemClickListener.onItemClick(v, "playlist", getAdapterPosition());
                    }
                }
            });
        }

    }
}
