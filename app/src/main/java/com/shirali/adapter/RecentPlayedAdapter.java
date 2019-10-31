package com.shirali.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.activity.AddPlaylistsActivity;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.databinding.ItemRecentBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongsList;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sagar on 10/7/17.
 */

public class RecentPlayedAdapter extends RecyclerView.Adapter<RecentPlayedAdapter.ViewHolder> {
    private final Context mContext;
    private final boolean check;
    private ItemRecentBinding binding;
    private ArrayList<Album> listRecentAlbum;
    private String isFromClass;
    private ArrayList<Song> listPlayed;
    private ArrayList<RelatedArtist> relatedArtist;
    private OnItemClickListener mItemClickListener;
    private String artistString = "", albumString = "";
    private boolean isAlpha;
    private ArrayList<String> song_list;
    private boolean isAlbum = true;

    public RecentPlayedAdapter(Context context, ArrayList<Album> recentAlbam,
                               ArrayList<Song> played, ArrayList<RelatedArtist> relatedArtist, boolean artist, String isFromClass) {
        this.mContext = context;
        this.check = artist;
        this.listRecentAlbum = recentAlbam;
        this.isFromClass = isFromClass;
        this.listPlayed = played;
        this.relatedArtist = relatedArtist;
    }

    public void isAlbum(){
        isAlbum = false ;
        Constants.isListHitFirstTime = true;
    }

    public void menuAlpha(){
        this.isAlpha = true;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_recent, parent, false);
        song_list = new ArrayList<>();
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (isFromClass.equalsIgnoreCase("album")) {
            holder.binding.lytSong.setVisibility(View.GONE);
            holder.binding.lytAlbum.setVisibility(View.VISIBLE);
            holder.binding.ivCircularArtist.setVisibility(View.GONE);
            if (listRecentAlbum.size() > 0) {
                try {
                    Glide.with(mContext).load(listRecentAlbum.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgItemcell);

                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).matches("iw")) {
                        if (listRecentAlbum.get(position).titleHebrew.equalsIgnoreCase("") || listRecentAlbum.get(position).titleHebrew == null) {
                            holder.binding.title.setText(listRecentAlbum.get(position).title);
                        } else {
                            holder.binding.title.setText(listRecentAlbum.get(position).titleHebrew);
                        }
                        if (listRecentAlbum.get(position).artist != null) {
                            if (listRecentAlbum.get(position).artist.nameHebrew.equalsIgnoreCase("") || listRecentAlbum.get(position).artist.nameHebrew == null) {
                                holder.binding.subTitle.setText(listRecentAlbum.get(position).artist.name);
                            } else {
                                holder.binding.subTitle.setText(listRecentAlbum.get(position).artist.nameHebrew);
                            }
                        }
                        if (listRecentAlbum.get(position).isPremium) {
                            holder.binding.imgTag.setVisibility(View.VISIBLE);
                            holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                        } else {
                            holder.binding.imgTag.setVisibility(View.GONE);
                        }
                    } else {
                        holder.binding.title.setText(listRecentAlbum.get(position).title);
                        if (listRecentAlbum.get(position).artist != null) {
                            holder.binding.subTitle.setText(listRecentAlbum.get(position).artist.name);
                        }
                        if (listRecentAlbum.get(position).isPremium) {
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
        } else if (isFromClass.equalsIgnoreCase("recent_song")) {
            if(isAlpha)
                binding.imgMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openMenu(holder, position, view);
                    }
                });
            holder.binding.lytAlbum.setVisibility(View.GONE);
            holder.binding.lytSong.setVisibility(View.VISIBLE);
            holder.binding.ivCircularArtist.setVisibility(View.GONE);
            if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).matches("iw")) {
                try {
                    Glide.with(mContext).load(listPlayed.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgAlbIcon);
                    if (listPlayed.get(position).title_hebrew.equalsIgnoreCase("") || listPlayed.get(position).title_hebrew == null) {
                        holder.binding.tvTitle.setText(listPlayed.get(position).title);
                    } else {
                        holder.binding.tvTitle.setText(listPlayed.get(position).title_hebrew);
                    }
                    if (listPlayed.get(position).artist.nameHebrew.equalsIgnoreCase("") || listPlayed.get(position).artist.nameHebrew == null) {
                        artistString = listPlayed.get(position).artist.name;
                    } else {
                        artistString = listPlayed.get(position).artist.nameHebrew;
                    }
                    if (listPlayed.get(position).albums.size() > 0) {
                        if (listPlayed.get(position).albums.get(0).titleHebrew == null || listPlayed.get(position).albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                            albumString = listPlayed.get(position).albums.get(0).title;
                        } else {
                            albumString = listPlayed.get(position).albums.get(0).titleHebrew;
                        }
                        holder.binding.tvName.setText(Html.fromHtml(Utility.setTextToTextView(artistString, albumString)));
                    }
                    ArrayList<String> data = Utility.getUserInfo(mContext).myMusic;
                    if (data.contains(listPlayed.get(position).id)) {
                        holder.binding.imgPlus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listPlayed.get(position).isPremium) {
                    holder.binding.imgTagSong.setVisibility(View.VISIBLE);
                    holder.binding.imgTagSong.setImageResource(R.drawable.premium_tag_hw);
                } else {
                    holder.binding.imgTagSong.setVisibility(View.GONE);
                }
            } else {
                if (listPlayed.size() > 0) {
                    try {
                        Glide.with(mContext).load(listPlayed.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                        }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgAlbIcon);
                        holder.binding.tvTitle.setText(listPlayed.get(position).title);
                        if (listPlayed.get(position).albums.size() > 0) {
                            holder.binding.tvName.setText(Html.fromHtml(Utility.setTextToTextView(listPlayed.get(position).artist.name, listPlayed.get(position).albums.get(0).title)));
                        }
                        ArrayList<String> data = Utility.getUserInfo(mContext).myMusic;
                        if (data.contains(listPlayed.get(position).id)) {
                            holder.binding.imgPlus.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (listPlayed.get(position).isPremium) {
                    holder.binding.imgTagSong.setVisibility(View.VISIBLE);
                    holder.binding.imgTagSong.setImageResource(R.drawable.premium_tag_en);
                } else {
                    holder.binding.imgTagSong.setVisibility(View.GONE);
                }
            }

        } else if (isFromClass.equalsIgnoreCase("played")) {
            holder.binding.lytSong.setVisibility(View.GONE);
            holder.binding.lytAlbum.setVisibility(View.VISIBLE);
            holder.binding.ivCircularArtist.setVisibility(View.GONE);
            if (listPlayed.size() > 0) {
                try {
                    Glide.with(mContext).load(listPlayed.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgItemcell);
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).matches("iw")) {
                        if (listPlayed.get(position).title_hebrew.equalsIgnoreCase("") || listPlayed.get(position).title_hebrew == null) {
                            holder.binding.title.setText(listPlayed.get(position).title);
                        } else {
                            holder.binding.title.setText(listPlayed.get(position).title_hebrew);
                        }
                        if (listPlayed.get(position).artist.nameHebrew.equalsIgnoreCase("") || listPlayed.get(position).artist.nameHebrew == null) {
                            holder.binding.subTitle.setText(listPlayed.get(position).artist.name);
                        } else {
                            holder.binding.subTitle.setText(listPlayed.get(position).artist.nameHebrew);
                        }
                        if (listPlayed.get(position).isPremium) {
                            holder.binding.imgTag.setVisibility(View.VISIBLE);
                            holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                        } else {
                            holder.binding.imgTag.setVisibility(View.GONE);
                        }
                    } else {
                        holder.binding.title.setText(listPlayed.get(position).title);
                        holder.binding.subTitle.setText(listPlayed.get(position).artist.name);
                        if (listPlayed.get(position).isPremium) {
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
        } else if (isFromClass.equalsIgnoreCase("Artist")) {
            holder.binding.lytSong.setVisibility(View.GONE);
            holder.binding.lytAlbum.setVisibility(View.GONE);
            holder.binding.ivCircularArtist.setVisibility(View.VISIBLE);
            try {
                Glide.with(mContext).load(relatedArtist.get(position).avatar).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.binding.loaderIconThree.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.binding.loaderIconThree.setVisibility(View.GONE);
                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).fitCenter().into(holder.binding.imgArtist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                holder.binding.titleArtist.setText(relatedArtist.get(position).nameHebrew);
            } else {
                holder.binding.titleArtist.setText(relatedArtist.get(position).name);
            }
        }
        if (check) {
            holder.binding.ivCircularArtist.setVisibility(View.VISIBLE);
            holder.binding.recImg.setVisibility(View.GONE);
            holder.binding.subTitle.setVisibility(View.GONE);
        } else {
            holder.binding.ivCircularArtist.setVisibility(View.GONE);
            holder.binding.recImg.setVisibility(View.VISIBLE);
            holder.binding.title.setGravity(Gravity.NO_GRAVITY);
            holder.binding.subTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (isFromClass.equalsIgnoreCase("album") && listRecentAlbum.size() > 0) {
            return listRecentAlbum.size();
        } else if (isFromClass.equalsIgnoreCase("played")) {
            return listPlayed.size();
        } else if (isFromClass.equalsIgnoreCase("Artist")) {
            return relatedArtist.size();
        } else if (isFromClass.equalsIgnoreCase("recent_song")) {
            return listPlayed.size();
        } else {
            return 0;
        }
    }

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().album = listRecentAlbum.get(position);
            if (listRecentAlbum.get(position).artist != null) {
                UserModel.getInstance().artist_id = listRecentAlbum.get(position).artist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mContext, AlbumDetailActivity.class);
        mContext.startActivity(intent);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ItemRecentBinding binding;

        public ViewHolder(View itemView, final ItemRecentBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(this);


            binding.imgPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFromClass.equalsIgnoreCase("recent_song")) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            try {
                                mItemClickListener.onItemClick(v, getAdapterPosition());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            final Dialog dialog = new Dialog(mContext);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setContentView(R.layout.add_song_layout);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().gc();
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }
                            }, 2500);
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                        }
                    }
                }
            });

        }

        @Override
        public void onClick(final View v) {
            if (isFromClass.equalsIgnoreCase("album")) {
                UserModel.getInstance().tempAlbum = listRecentAlbum.get(getAdapterPosition());
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    openAlbum(getAdapterPosition());
                } else {
                    if (listRecentAlbum.get(getAdapterPosition()).isPremium) {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                    } else {
                        openAlbum(getAdapterPosition());
                    }
                }
            } else if (isFromClass.equalsIgnoreCase("played")) {
                Constants.isSongPlay = true;
                UserModel.getInstance().isSingleSongPlay = isAlbum;
                UserModel.getInstance().tempSongList.clear();
                UserModel.getInstance().tempSongList.addAll(listPlayed);
                UserModel.getInstance().listOfShuffleSong.clear();
                UserModel.getInstance().listOfShuffleSong.addAll(listPlayed);
                Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                UserModel.getInstance().listOfActualSong.clear();
                UserModel.getInstance().listOfActualSong.addAll(listPlayed);
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    playSong(v);
                } else {
                    Album tempAlbum = new Album();
                    if (listPlayed.get(getAdapterPosition()).albums != null) {
                        if (listPlayed.get(getAdapterPosition()).albums.size() > 0) {
                            tempAlbum = listPlayed.get(getAdapterPosition()).albums.get(0);
                        }
                    }
                    if (listPlayed.get(getAdapterPosition()).isPremium || listPlayed.get(getAdapterPosition()).artist.isPremium || tempAlbum.isPremium) {
                        if (listPlayed.get(getAdapterPosition()).isPremium) {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                        } else if (listPlayed.get(getAdapterPosition()).artist.isPremium) {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        playSong(v);
                    }
                }
            } else if (isFromClass.equalsIgnoreCase("Artist")) {
                if (relatedArtist.get(getAdapterPosition()).isPremium) {
                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                        intent.putExtra("artist_id", relatedArtist.get(getAdapterPosition()).id);
                        mContext.startActivity(intent);
                    } else {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                    }
                } else {
                    Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                    intent.putExtra("artist_id", relatedArtist.get(getAdapterPosition()).id);
                    mContext.startActivity(intent);
                }
            }
        }

        private void playSong(final View v) {
            Constants.SONGS_LIST = listPlayed;
            Constants.SONG_NUMBER = getAdapterPosition();
            UserModel.getInstance().getAppSetting(mContext);
            UserModel.getInstance().freePaidUser(mContext, listPlayed, getAdapterPosition(), new FreePaidUserCallBack() {
                @Override
                public void freePaidUser(boolean ifPaid) {
                    if (ifPaid) {
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                    } else {
                        if (listPlayed.get(getAdapterPosition()).isPremium) {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        } else {
                            mItemClickListener.onItemClick(v, getAdapterPosition());
                        }
                    }
                }
            });
        }
    }

    private void openMenu(final ViewHolder holder, final int position, View v) {
        final Dialog d = new BottomSheetDialog(mContext);
        d.setContentView(R.layout.user_action_onsong_cell);
        View view = d.findViewById(R.id.bs);
        ((View) view.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        TextView artist_name = (TextView) d.findViewById(R.id.tvArtistName);
        ImageView artistImage = (ImageView) d.findViewById(R.id.ivArtistImage);
        try {
            if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (listPlayed.get(position).title_hebrew.equalsIgnoreCase("") ||
                        listPlayed.get(position).title_hebrew == null) {
                    artist_name.setText(listPlayed.get(position).title);
                } else {
                    artist_name.setText(listPlayed.get(position).title_hebrew);
                }
            } else {
                artist_name.setText(listPlayed.get(position).title);
            }
            try {
                Glide.with(mContext).load(listPlayed.get(position).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(artistImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareSong).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
        d.findViewById(R.id.lytViewArtist).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytViewAlbum).setVisibility(View.VISIBLE);
        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
        d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);

        d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.findViewById(R.id.lytShareSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (listPlayed.size() > 0)
                        Utility.shareIt(mContext, "song", listPlayed.get(position).title, listPlayed.get(position).artist.name, listPlayed.get(position).shareUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFromClass.equalsIgnoreCase("new_Released")) {
                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        getAlbumSongs(listPlayed.get(position).id);
                    } else {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                    }
                } else {
                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        mContext.startActivity(new Intent(mContext, AddPlaylistsActivity.class).putExtra("id", listPlayed.get(position).id));
                    } else {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                    }
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listPlayed.get(position).albums !=null) {
                    if (listPlayed.get(position).albums.size() > 0) {
                        UserModel.getInstance().tempAlbum = listPlayed.get(position).albums.get(0);
                        if (listPlayed.get(position).albums.get(0).isPremium) {
                            if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                try {
                                    UserModel.getInstance().album = listPlayed.get(position).albums.get(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                                mContext.startActivity(intent);
                            } else {
                                Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                            }
                        } else {
                            try {
                                UserModel.getInstance().album = listPlayed.get(position).albums.get(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                            mContext.startActivity(intent);
                        }
                    }else {
                        Utility.showNoDataFound(mContext);
                    }
                } else {
                    Utility.showNoDataFound(mContext);
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listPlayed.size() > 0) {
                    if (listPlayed.get(position).artist.isPremium) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                            intent.putExtra("artist_id", listPlayed.get(position).artist.id);
                            mContext.startActivity(intent);
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }
                    } else {
                        Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                        intent.putExtra("artist_id", listPlayed.get(position).artist.id);
                        mContext.startActivity(intent);
                    }
                }
                d.dismiss();
            }
        });

        d.setCancelable(true);
        d.show();
    }

    //Get all album song
    private void getAlbumSongs(final String id) {
        Call<SongsList> call = Constants.service.getAlbumSong(id);
        call.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                SongsList list = response.body();
                try {
                    if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (list.success) {
                            if (list.songs.size() > 0) {
                                for (int i = 0; i < list.songs.size(); i++) {
                                    if (list.songs.get(i).albums.size() > 0) {
                                        if (list.songs.get(i).albums.get(0).id.equalsIgnoreCase(id)) {
                                            song_list.add(list.songs.get(i).id);
                                        }
                                    }
                                }
                                mContext.startActivity(new Intent(mContext, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
