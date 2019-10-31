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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.activity.AddPlaylistsActivity;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.databinding.MySongsCellBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.MySongsModel;
import com.shirali.model.mymusic.Album;
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
 * Created by user on 10/7/17.
 */

public class MySongsAdapter extends RecyclerView.Adapter<MySongsAdapter.ViewHolder> {

    private final String listRecently;
    private ArrayList<Song> listSongs;
    private Context context;
    private ArrayList<MySongsModel> songsModelArrayList;
    private MySongsCellBinding binding;
    private String isFrom;
    private ArrayList<String> songList;
    private OnItemClickListener mItemClickListener;
    private ArrayList<Album> listNewRelease;
    private OnItemClick mItemClick;
    private ArrayList<String> song_list;
    private String artistString = "", albumString = "";

    public MySongsAdapter(Context context, ArrayList<MySongsModel> songsModelArrayList, ArrayList<Song> listSongs, String listRecentPlayed, ArrayList<String> songsList, String isFrom, ArrayList<Album> listNewRelease) {
        this.context = context;
        this.songsModelArrayList = songsModelArrayList;
        this.songList = songsList;
        this.listSongs = listSongs;
        this.listRecently = listRecentPlayed;
        this.isFrom = isFrom;
        this.listNewRelease = listNewRelease;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.my_songs_cell, parent, false);
        song_list = new ArrayList<>();
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            if (Constants.song.equalsIgnoreCase(listSongs.get(position).id)) {
                if (Constants.isPlay) {
                    holder.binding.imgSongPlay.setVisibility(View.GONE);
                    holder.binding.imgCurrentPlay.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.imgCurrentPlay.setVisibility(View.GONE);
                    holder.binding.imgSongPlay.setVisibility(View.VISIBLE);
                }
                holder.binding.lytCoverImage.setVisibility(View.VISIBLE);
            } else {
                holder.binding.lytCoverImage.setVisibility(View.GONE);
                holder.binding.imgCurrentPlay.setVisibility(View.GONE);
                holder.binding.imgSongPlay.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (isFrom.equalsIgnoreCase("player")) {
                holder.binding.imgPlus.setVisibility(View.GONE);
                holder.binding.menu.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isFrom.equalsIgnoreCase("new_Released")) {
            try {
                binding.newRelases.setVisibility(View.VISIBLE);
                binding.lytMain.setVisibility(View.GONE);
                Glide.with(context).load(listNewRelease.get(position).artwork).placeholder(R.drawable.imglogo).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgIcon);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listNewRelease.get(position).titleHebrew.equalsIgnoreCase("") || listNewRelease.get(position).titleHebrew == null) {
                        holder.binding.tvName.setText(listNewRelease.get(position).title);
                    } else {
                        holder.binding.tvName.setText(listNewRelease.get(position).titleHebrew);
                    }
                    if (listNewRelease.get(position).isPremium) {
                        holder.binding.imgTagAlbum.setVisibility(View.VISIBLE);
                        holder.binding.imgTagAlbum.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        holder.binding.imgTagAlbum.setVisibility(View.GONE);
                    }
                } else {
                    holder.binding.tvName.setText(listNewRelease.get(position).title);
                    if (listNewRelease.get(position).isPremium) {
                        holder.binding.imgTagAlbum.setVisibility(View.VISIBLE);
                        holder.binding.imgTagAlbum.setImageResource(R.drawable.premium_tag_en);
                    } else {
                        holder.binding.imgTagAlbum.setVisibility(View.GONE);
                    }
                }
                if (listNewRelease.get(position).creationDate != null && !listNewRelease.get(position).creationDate.equalsIgnoreCase("")) {
                    String timeString = Utility.getFormatedDate(listNewRelease.get(position).creationDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd MMMM, yyyy");
                    if (!timeString.equalsIgnoreCase("01 January, 1900")) {
                        holder.binding.tvDate.setText(timeString);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            holder.binding.newRelases.setVisibility(View.GONE);
            holder.binding.lytMain.setVisibility(View.VISIBLE);
            try {
                Glide.with(context).load(listSongs.get(position).artwork).placeholder(R.drawable.imglogo).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.ivRectangularArtist);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listSongs.get(position).title_hebrew.equalsIgnoreCase("") || listSongs.get(position).title_hebrew == null) {
                        holder.binding.tvSongName.setText(listSongs.get(position).title);
                    } else {
                        holder.binding.tvSongName.setText(listSongs.get(position).title_hebrew);
                    }
                    if (listSongs.get(position).artist.nameHebrew == null || listSongs.get(position).artist.nameHebrew.equalsIgnoreCase("")) {
                        artistString = listSongs.get(position).artist.name;
                    } else {
                        artistString = listSongs.get(position).artist.nameHebrew;
                    }
                    if (listSongs.get(position).albums.size() > 0) {
                        if (listSongs.get(position).albums.get(0).titleHebrew == null || listSongs.get(position).albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                            albumString = listSongs.get(position).albums.get(0).title;
                        } else {
                            albumString = listSongs.get(position).albums.get(0).titleHebrew;
                        }
                    }
                    holder.binding.tvAlbumName.setText(Html.fromHtml(Utility.setTextToTextView(artistString, albumString)));
                    if (listSongs.get(position).tags.size() > 0) {
                        holder.binding.tvSongTag.setVisibility(View.VISIBLE);
                        holder.binding.tvSongTag.setText(listSongs.get(position).tags.get(0).tagName);
                    } else {
                        holder.binding.tvSongTag.setBackgroundResource(android.R.color.transparent);
                        holder.binding.tvSongTag.setVisibility(View.GONE);
                    }
                    if (listSongs.get(position).isPremium) {
                        binding.imgTag.setVisibility(View.VISIBLE);
                        binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
                    } else {
                        binding.imgTag.setVisibility(View.GONE);
                    }
                } else {
                    holder.binding.tvSongName.setText(listSongs.get(position).title);
                    holder.binding.tvAlbumName.setText(Html.fromHtml(Utility.setTextToTextView(listSongs.get(position).artist.name, listSongs.get(position).albums.get(0).title)));
                    if (listSongs.get(position).tags.size() > 0) {
                        holder.binding.tvSongTag.setVisibility(View.VISIBLE);
                        holder.binding.tvSongTag.setText(listSongs.get(position).tags.get(0).tagName);
                    } else {
                        holder.binding.tvSongTag.setBackgroundResource(android.R.color.transparent);
                        holder.binding.tvSongTag.setVisibility(View.GONE);
                    }
                    if (listSongs.get(position).isPremium) {
                        holder.binding.imgTag.setVisibility(View.VISIBLE);
                        holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                    } else {
                        holder.binding.imgTag.setVisibility(View.GONE);
                    }
                }
                holder.binding.tvSongTag.setVisibility(View.VISIBLE);
                if (isFrom.equalsIgnoreCase("myArtist")) {
                    ArrayList<String> data = (ArrayList<String>) Utility.getUserInfo(context).myMusic;
                    try {
                        if (data.contains(listSongs.get(position).id)) {
                            holder.binding.imgPlus.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (isFrom.equalsIgnoreCase("song")) {
            if (songList.contains(listSongs.get(position).id)) {
                holder.binding.menuIcon.setVisibility(View.GONE);
            } else {
                holder.binding.menuIcon.setVisibility(View.VISIBLE);
            }
            holder.binding.menu.setVisibility(View.GONE);

        } else if (isFrom.equalsIgnoreCase("add_song")) {
            if (songList.contains(listSongs.get(position).id)) {
                holder.binding.imgPlus.setVisibility(View.GONE);
            } else {
                holder.binding.imgPlus.setVisibility(View.VISIBLE);
            }
            holder.binding.menu.setVisibility(View.GONE);
            try {
                if (holder.binding.menu.getVisibility() == View.GONE) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.binding.imgPlus.getLayoutParams();
                    params.setMargins(30, 0, 0, 0);
                    holder.binding.imgPlus.setLayoutParams(params);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isFrom.equalsIgnoreCase("myArtist")) {
            ArrayList<String> data = Utility.getUserInfo(context).myMusic;
            if (data.contains(listSongs.get(position).id)) {
                holder.binding.imgPlus.setVisibility(View.INVISIBLE);
            } else {
                holder.binding.imgPlus.setVisibility(View.VISIBLE);
            }
        } else {
            holder.binding.imgPlus.setVisibility(View.INVISIBLE);
            holder.binding.menu.setVisibility(View.VISIBLE);
        }

        holder.binding.newRelases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrom.equalsIgnoreCase("new_released")) {
                    UserModel.getInstance().tempAlbum = listNewRelease.get(position);
                    if (listNewRelease.get(position).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            try {
                                UserModel.getInstance().album = listNewRelease.get(position);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, AlbumDetailActivity.class);
                            context.startActivity(intent);
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        try {
                            UserModel.getInstance().album = listNewRelease.get(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(context, AlbumDetailActivity.class);
                        context.startActivity(intent);
                    }
                }
            }
        });

        holder.binding.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(holder, position, view);
            }
        });

        holder.binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrom.equalsIgnoreCase("song")) {
                    mItemClickListener.onItemClick(v, position, songList, "song");
                } else if (isFrom.equalsIgnoreCase("add_song")) {
                    mItemClickListener.onItemClick(v, position, songList, "add_song");
                } else {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        try {
                            UserModel.getInstance().addToMusic(context, Utility.getUserInfo(context).id, listSongs.get(position).id);
                            holder.binding.imgPlus.setVisibility(View.INVISIBLE);
                            mItemClick.onItemVisible(v, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final Dialog dialog = new Dialog(context);
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
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                    }
                }
            }
        });

        holder.binding.lytMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                UserModel.getInstance().getAppSetting(context);
                if (isFrom.equalsIgnoreCase("new_released")) {
                    mItemClick.onItemClick(v, position, "new_released");
                    notifyDataSetChanged();
                } else if (isFrom.equalsIgnoreCase("mySong")) {
                    UserModel.getInstance().isSingleSongPlay = true;
                    UserModel.getInstance().listOfShuffleSong.clear();
                    UserModel.getInstance().listOfShuffleSong.addAll(listSongs);
                    Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                    UserModel.getInstance().listOfActualSong.clear();
                    UserModel.getInstance().listOfActualSong.addAll(listSongs);
                    mItemClick.onItemClick(v, position, "mySong");
                } else if (isFrom.equalsIgnoreCase("add_song")) {
                } else {
                    UserModel.getInstance().tempSongList.clear();
                    UserModel.getInstance().tempSongList.addAll(listSongs);
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        playSong(v, position);
                    } else {
                        Album tempAlbum = new Album();
                        if (listSongs.get(position).albums != null) {
                            if (listSongs.get(position).albums.size() > 0) {
                                tempAlbum = listSongs.get(position).albums.get(0);
                            }
                        }
                        if (listSongs.get(position).isPremium || listSongs.get(position).artist.isPremium || tempAlbum.isPremium) {
                            if (listSongs.get(position).isPremium) {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                            } else if (listSongs.get(position).artist.isPremium) {
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
        Constants.SONGS_LIST = listSongs;
        Constants.SONG_NUMBER = position;
        Constants.isSongPlay = true;
        UserModel.getInstance().freePaidUser(context, listSongs, position, new FreePaidUserCallBack() {
            @Override
            public void freePaidUser(boolean ifPaid) {
                if (ifPaid) {
                    mItemClick.onItemClick(v, position, "song");
                } else {
                    if (listSongs.get(position).isPremium) {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    } else {
                        mItemClick.onItemClick(v, position, "song");
                    }
                }
            }
        });
    }

    private void openMenu(final ViewHolder holder, final int position, View v) {
        final Dialog d = new BottomSheetDialog(context);
        d.setContentView(R.layout.user_action_onsong_cell);
        View view = d.findViewById(R.id.bs);
        ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        TextView artist_name = (TextView) d.findViewById(R.id.tvArtistName);
        ImageView artistImage = (ImageView) d.findViewById(R.id.ivArtistImage);
        try {
            if (isFrom.equalsIgnoreCase("new_Released")) {
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listNewRelease.get(position).titleHebrew.equalsIgnoreCase("") || listNewRelease.get(position).titleHebrew == null) {
                        artist_name.setText(listNewRelease.get(position).title);
                    } else {
                        artist_name.setText(listNewRelease.get(position).titleHebrew);
                    }
                } else {
                    artist_name.setText(listNewRelease.get(position).title);
                }
                try {
                    Glide.with(context).load(listNewRelease.get(position).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(artistImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listSongs.get(position).title_hebrew.equalsIgnoreCase("") || listSongs.get(position).title_hebrew == null) {
                        artist_name.setText(listSongs.get(position).title);
                    } else {
                        artist_name.setText(listSongs.get(position).title_hebrew);
                    }
                } else {
                    artist_name.setText(listSongs.get(position).title);
                }
                try {
                    Glide.with(context).load(listSongs.get(position).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(artistImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFrom.equalsIgnoreCase("myArtist")) {
            d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
            d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
            d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytShareSong).setVisibility(View.VISIBLE);
            d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
            d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
            d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
            d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
            if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
            } else {
                d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
            }
        } else if (isFrom.equalsIgnoreCase("new_released")) {
            d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
            d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
            d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
            d.findViewById(R.id.lytShareArtist).setVisibility(View.VISIBLE);
            d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
            d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
            d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
            d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
            d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
        } else {
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
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
        }
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
                    if (listSongs.size() > 0)
                        Utility.shareIt(context, "song", listSongs.get(position).title, listSongs.get(position).artist.name, listSongs.get(position).shareUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFrom.equalsIgnoreCase("new_Released")) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        getAlbumSongs(listNewRelease.get(position).id);
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                    }
                } else {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        context.startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("id", listSongs.get(position).id));
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                    }
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytShareAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (listNewRelease.size() > 0)
                        Utility.shareIt(context, "album", listNewRelease.get(position).title, listNewRelease.get(position).artist.name, listNewRelease.get(position).shareUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listSongs.get(position).albums !=null) {
                    if (listSongs.get(position).albums.size() > 0) {
                        UserModel.getInstance().tempAlbum = listSongs.get(position).albums.get(0);
                        if (listSongs.get(position).albums.get(0).isPremium) {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                try {
                                    UserModel.getInstance().album = listSongs.get(position).albums.get(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, AlbumDetailActivity.class);
                                context.startActivity(intent);
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                            }
                        } else {
                            try {
                                UserModel.getInstance().album = listSongs.get(position).albums.get(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, AlbumDetailActivity.class);
                            context.startActivity(intent);
                        }
                    }else {
                        Utility.showNoDataFound(context);
                    }
                } else {
                    Utility.showNoDataFound(context);
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listSongs.size() > 0) {
                    if (listSongs.get(position).artist.isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            Intent intent = new Intent(context, ArtistDetailActivity.class);
                            intent.putExtra("artist_id", listSongs.get(position).artist.id);
                            context.startActivity(intent);
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }
                    } else {
                        Intent intent = new Intent(context, ArtistDetailActivity.class);
                        intent.putExtra("artist_id", listSongs.get(position).artist.id);
                        context.startActivity(intent);
                    }
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrom.equalsIgnoreCase("new_Released") || isFrom.equalsIgnoreCase("add_song")) {
                } else if (isFrom.equalsIgnoreCase("myArtist")) {
                    UserModel.getInstance().removeFromMusic(context, Utility.getUserInfo(context).id, listSongs.get(position).id);
                    mItemClick.onItemVisible(v, false);
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.add_song_layout);
                    TextView title = (TextView) dialog.findViewById(R.id.lytAdd);
                    title.setText(R.string.remove_from_music);
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
                    holder.binding.imgPlus.setVisibility(View.VISIBLE);
                } else {
                    mItemClick.onItemClick(v, position, "remove");
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(R.layout.add_song_layout);
                    TextView title = (TextView) dialog.findViewById(R.id.lytAdd);
                    title.setText(R.string.remove_from_music);
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
                    holder.binding.imgPlus.setVisibility(View.VISIBLE);
                }
                d.dismiss();
            }
        });
        d.setCancelable(true);
        d.show();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setOnItemClick(final OnItemClick mItemClick) {
        this.mItemClick = mItemClick;
    }

    @Override
    public int getItemCount() {
        if (isFrom.equalsIgnoreCase("new_Released")) {
            return listNewRelease.size();
        } else {
            if (listSongs.size() <= 0) {
                return songsModelArrayList.size();
            } else {
                return listSongs.size();
            }
        }
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
                t.printStackTrace();
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, ArrayList<String> songList, String type);
    }

    public interface OnItemClick {
        void onItemClick(View view, int position, String type);

        void onItemVisible(View view, boolean isVisible);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MySongsCellBinding binding;

        public ViewHolder(final View view, final MySongsCellBinding binding) {
            super(view);
            this.binding = binding;
        }
    }
}