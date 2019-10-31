package com.shirali.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.activity.AddPlaylistsActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.databinding.MyMusicCellBinding;
import com.shirali.model.MyMusicModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongsList;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by sagar on 10/7/17.
 */

public class MyMusicAdapter extends RecyclerView.Adapter<MyMusicAdapter.ViewHolder> {

    private final ArrayList<Song> songslist;
    private final ArrayList<Album> albumlist;
    private Context context;
    private ArrayList<MyMusicModel> artistModelArrayList;
    private String musicType;
    private OnItemClickListener mItemClickListener;
    private OnClickListner onClickListner;
    private ArrayList<String> song_list;
    private String artistString = "";

    public MyMusicAdapter(Context context, ArrayList<Song> modelArrayList, ArrayList<MyMusicModel> artistModelArrayList, ArrayList<Album> albumArrayList, String musicType) {
        this.context = context;
        this.artistModelArrayList = artistModelArrayList;
        this.musicType = musicType;
        this.songslist = modelArrayList;
        this.albumlist = albumArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final MyMusicCellBinding binding = DataBindingUtil.inflate(inflater, R.layout.my_music_cell, parent, false);
        song_list = new ArrayList<>();
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (musicType.equalsIgnoreCase("MyAlbum")) {
            if (albumlist.size() > 0) {
                try {
                    holder.binding.ivRectangularArtist.setVisibility(View.VISIBLE);
                    try {
                        Glide.with(context).load(albumlist.get(position).artwork).listener(new RequestListener<String, GlideDrawable>() {
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
                        if (songslist.get(position).artist.nameHebrew.equalsIgnoreCase("") || songslist.get(position).artist.nameHebrew == null) {
                            artistString = songslist.get(position).artist.name;
                        } else {
                            artistString = songslist.get(position).artist.nameHebrew;
                        }
                        holder.binding.tvMusicAlbumNo.setText(Html.fromHtml(Utility.setTextToTextView(artistString, albumlist.get(position).totalDuration)));
                        if (albumlist.get(position).isPremium) {
                            holder.binding.imgTag.setVisibility(View.VISIBLE);
                            holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                        } else {
                            holder.binding.imgTag.setVisibility(View.GONE);
                        }
                    } else {
                        holder.binding.tvMusicName.setText(albumlist.get(position).title);
                        holder.binding.tvMusicAlbumNo.setText(Html.fromHtml(Utility.setTextToTextView(songslist.get(position).artist.name, albumlist.get(position).totalDuration)));
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
            if (songslist.size() > 0) {
                holder.binding.ivRectangularArtist.setVisibility(View.GONE);
                holder.binding.imgTag.setVisibility(View.GONE);
                try {
                    Glide.with(context).load(songslist.get(position).artist.avatar).listener(new RequestListener<String, GlideDrawable>() {
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
                        if (songslist.get(position).artist.nameHebrew.equalsIgnoreCase("") || songslist.get(position).artist.nameHebrew == null) {
                            holder.binding.tvMusicName.setText(songslist.get(position).artist.name);
                        } else {
                            holder.binding.tvMusicName.setText(songslist.get(position).artist.nameHebrew);
                        }
                    } else {
                        holder.binding.tvMusicName.setText(songslist.get(position).artist.name);
                    }
                    holder.binding.tvMusicAlbumNo.setText(Html.fromHtml(Utility.setTextToTextView(songslist.get(position).artist.albumCount + " " + context.getResources().getString(R.string.albums), songslist.get(position).artist.songCount + " " + context.getResources().getString(R.string.songs))));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        holder.binding.ivMusicFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new BottomSheetDialog(context);
                d.setContentView(R.layout.user_action_onsong_cell);
                final View view = d.findViewById(R.id.bs);
                ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
                TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                if (musicType.equalsIgnoreCase("MyArtist")) {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (songslist.get(position).artist.nameHebrew.equalsIgnoreCase("") || songslist.get(position).artist.nameHebrew == null) {
                            title.setText(songslist.get(position).artist.name);
                        } else {
                            title.setText(songslist.get(position).artist.nameHebrew);
                        }
                    } else {
                        title.setText(songslist.get(position).artist.name);
                    }
                    try {
                        if (songslist.get(position).artist.avatar.equalsIgnoreCase("")) {
                            Glide.with(context).load(songslist.get(position).artist.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        } else {
                            Glide.with(context).load(songslist.get(position).artist.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                    d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                } else if (musicType.equalsIgnoreCase("MyAlbum")) {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (albumlist.get(position).titleHebrew.equalsIgnoreCase("") || albumlist.get(position).titleHebrew == null) {
                            title.setText(albumlist.get(position).title);
                        } else {
                            title.setText(albumlist.get(position).titleHebrew);
                        }
                    } else {
                        title.setText(albumlist.get(position).title);
                    }
                    try {
                        if (albumlist.get(position).artwork.equalsIgnoreCase("")) {
                        } else {
                            Glide.with(context).load(albumlist.get(position).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                    d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (songslist.get(position).artist.isPremium) {
                                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", songslist.get(position).artist.id));
                                } else {
                                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                }
                            } else {
                                context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", songslist.get(position).artist.id));
                            }
                            d.dismiss();
                        }
                    });
                }
                d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            if (musicType.equalsIgnoreCase("MyArtist")) {
                                getArtistDetail(songslist.get(position).artist.id);
                            } else {
                                getAlbumSongs(albumlist.get(position).id);
                            }
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                        }
                        d.dismiss();
                    }
                });
                d.findViewById(R.id.lytShareArtist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (musicType.equalsIgnoreCase("MyArtist")) {
                                Utility.shareIt(context, "artist", songslist.get(position).artist.name, "", songslist.get(position).artist.shareUrl);
                            } else if (musicType.equalsIgnoreCase("MyAlbum")) {
                                Utility.shareIt(context, "album", albumlist.get(position).title, albumlist.get(position).artist.name, albumlist.get(position).shareUrl);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        d.dismiss();
                    }
                });
                d.findViewById(R.id.lytShareAlbum).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListner.onItemClick(v, position, "share");
                        d.dismiss();
                    }
                });

                d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.cancel();
                    }
                });
                d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mItemClickListener.onItemClick(view, position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        d.dismiss();
                    }
                });
                d.setCancelable(true);
                d.show();
            }
        });

        holder.binding.lytCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicType.equalsIgnoreCase("MyArtist")) {
                    if (songslist.get(position).artist.isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", songslist.get(position).artist.id));
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }
                    } else {
                        context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", songslist.get(position).artist.id));
                    }
                } else {
                    if (albumlist.get(position).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            onClickListner.onItemClick(v, position, "open");
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        onClickListner.onItemClick(v, position, "open");
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (songslist.size() > 0) {
            return songslist.size();
        } else if (albumlist.size() > 0) {
            return albumlist.size();
        } else {
            return artistModelArrayList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setClickListner(final OnClickListner clickListner) {
        this.onClickListner = clickListner;
    }

    //Get All album song
    private void getAlbumSongs(final String id) {
        Call<SongsList> call = Constants.service.getAlbumSong(id);
        call.enqueue(new Callback<SongsList>() {
            @Override
            public void onResponse(Call<SongsList> call, Response<SongsList> response) {
                SongsList list = response.body();
                try {
                    if (list.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
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
                                context.startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SongsList> call, Throwable t) {
            }
        });
    }

    //Get artist all song
    private void getArtistDetail(final String artist_id) {
        Call<ArtistInfo> call = Constants.service.getArtistDetail(artist_id);
        call.enqueue(new Callback<ArtistInfo>() {
            @Override
            public void onResponse(Call<ArtistInfo> call, Response<ArtistInfo> response) {
                ArtistInfo info = response.body();
                if (info.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(context);
                } else {
                    if (info.success) {
                        if (info.popularSongs.size() > 0) {
                            for (int i = 0; i < info.popularSongs.size(); i++) {
                                if (info.artist.id.equalsIgnoreCase(artist_id)) {
                                    song_list.add(info.popularSongs.get(i).id);
                                }
                            }
                            context.startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistInfo> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnClickListner {
        void onItemClick(View view, int position, String share);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MyMusicCellBinding binding;

        public ViewHolder(final View view, final MyMusicCellBinding binding) {
            super(view);
            this.binding = binding;
        }
    }

}
