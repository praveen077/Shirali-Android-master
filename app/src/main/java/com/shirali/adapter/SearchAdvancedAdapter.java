package com.shirali.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.activity.MainActivity;
import com.shirali.databinding.SearchLayoutBinding;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Artist;
import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;

import java.util.ArrayList;


/**
 * Created by sagar on 10/7/17.
 */

public class SearchAdvancedAdapter extends RecyclerView.Adapter<SearchAdvancedAdapter.ViewHolder> {

    private final ArrayList<Artist> artistlist;
    private final ArrayList<Album> albumlist;
    private Context context;
    private String musicType;
    private OnItemClickListener mItemClickListener;
    private OnClickListner onClickListner;
    private String artistString = "";

    public SearchAdvancedAdapter(Context context, ArrayList<Artist> artistModelArrayList, ArrayList<Album> albumArrayList, String musicType) {
        this.context = context;
        this.musicType = musicType;
        this.artistlist = artistModelArrayList;
        this.albumlist = albumArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final SearchLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_layout, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (musicType.equalsIgnoreCase("MyAlbum")) {
            if (albumlist.size() > 0) {
                try {
                    holder.binding.ivRectangularArtist.setVisibility(View.VISIBLE);
                    try {
                        Glide.with(context).load(albumlist.get(position).artwork).placeholder(R.drawable.imglogo).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                holder.binding.loaderIconSec.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.binding.loaderIconSec.setVisibility(View.GONE);
                                return false;
                            }
                        }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).into(holder.binding.imgRecTwo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (albumlist.get(position).titleHebrew.equalsIgnoreCase("") || albumlist.get(position).titleHebrew == null) {
                            holder.binding.tvMusicName.setText(albumlist.get(position).title);
                        } else {
                            holder.binding.tvMusicName.setText(albumlist.get(position).titleHebrew);
                        }
                        if (artistlist.get(position).nameHebrew.equalsIgnoreCase("") || artistlist.get(position).nameHebrew == null) {
                            artistString = artistlist.get(position).name;
                        } else {
                            artistString = artistlist.get(position).nameHebrew;
                        }
                        holder.binding.tvMusicAlbumNo.setText(albumlist.get(position).totalDuration);
                        if (albumlist.get(position).isPremium) {
                            holder.binding.imgTag.setVisibility(View.VISIBLE);
                            holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                        } else {
                            holder.binding.imgTag.setVisibility(View.GONE);
                        }
                    } else {
                        holder.binding.tvMusicName.setText(albumlist.get(position).title);
                        holder.binding.tvMusicAlbumNo.setText(albumlist.get(position).totalDuration);
                        if (albumlist.get(position).isPremium) {
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
        } else if (musicType.equalsIgnoreCase("MyArtist")) {
            if (artistlist.size() > 0) {
                holder.binding.imgTag.setVisibility(View.GONE);
                holder.binding.ivRectangularArtist.setVisibility(View.GONE);
                try {
                    Glide.with(context).load(artistlist.get(position).avatar).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).into(holder.binding.ivCircularArtist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (artistlist.get(position).nameHebrew.equalsIgnoreCase("") || artistlist.get(position).nameHebrew == null) {
                            holder.binding.tvMusicName.setText(artistlist.get(position).name);
                        } else {
                            holder.binding.tvMusicName.setText(artistlist.get(position).nameHebrew);
                        }
                    } else {
                        holder.binding.tvMusicName.setText(artistlist.get(position).name);
                    }
                    holder.binding.tvMusicAlbumNo.setText(Html.fromHtml(Utility.setTextToTextView(artistlist.get(position).albumCount + " " + context.getResources().getString(R.string.albums), artistlist.get(position).songCount + " " + context.getResources().getString(R.string.songs))));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        holder.binding.ivMusicFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        holder.binding.lytCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicType.equalsIgnoreCase("MyArtist")) {
                    if (artistlist.get(position).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", artistlist.get(position).id));
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }
                    } else {
                        context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", artistlist.get(position).id));
                    }
                } else {
                    UserModel.getInstance().tempAlbum = albumlist.get(position);
                    if (albumlist.get(position).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            mItemClickListener.onItemClick(v, position);
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (musicType.equalsIgnoreCase("MyArtist")) {
            return artistlist.size();
        } else {
            return albumlist.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final SearchLayoutBinding binding;

        public ViewHolder(final View view, final SearchLayoutBinding binding) {
            super(view);
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnClickListner {
        void onItemClick(View view, int position, String share);
    }

    public void setClickListner(final OnClickListner clickListner) {
        this.onClickListner = clickListner;
    }

}
