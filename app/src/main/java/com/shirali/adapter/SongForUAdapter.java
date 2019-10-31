package com.shirali.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.shirali.databinding.ItemSongforplayBinding;
import com.shirali.interfaces.AddToMyMusicCallback;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Sagar on 10/7/17.
 */

public class SongForUAdapter extends RecyclerView.Adapter<SongForUAdapter.ViewHolder> {

    private Context context;
    private ItemSongforplayBinding binding;
    private OnItemClickListener mItemClickListener;
    private OnItemClicRemove onItemClicRemove;
    private ArrayList<Song> listPrefSong;
    private String isFrom;
    private ArrayList<Song> listNewSongs;
    private boolean isForFirstTime = false;
    private ArrayList<String> data;
    private String artistString = "", albumString = "";
    private boolean isAlbum = true;

    public SongForUAdapter(Context context, ArrayList<Song> listPrefSong, String isFrom, ArrayList<String> myMusic) {
        this.context = context;
        this.listPrefSong = listPrefSong;
        this.isFrom = isFrom;
        listNewSongs = new ArrayList<>();
        isForFirstTime = true;
        this.data = myMusic;
    }

    public void isAlbum(){
        isAlbum = false ;
        Constants.isListHitFirstTime = true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_songforplay, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            if (Constants.song.equalsIgnoreCase(listPrefSong.get(position).id)) {
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
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            try {
                Glide.with(context).load(listPrefSong.get(position).artwork).placeholder(R.drawable.imglogo).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgAlbIcon);
                if (listPrefSong.get(position).title_hebrew != null) {
                    if (listPrefSong.get(position).title_hebrew.equalsIgnoreCase("")) {
                        holder.binding.tvTitle.setText(listPrefSong.get(position).title);
                    } else {
                        holder.binding.tvTitle.setText(listPrefSong.get(position).title_hebrew);
                    }
                } else {
                    holder.binding.tvTitle.setText(listPrefSong.get(position).title);
                }
                if (listPrefSong.get(position).artist.nameHebrew == null || listPrefSong.get(position).artist.nameHebrew.equalsIgnoreCase("")) {
                    artistString = listPrefSong.get(position).artist.name;
                } else {
                    artistString = listPrefSong.get(position).artist.nameHebrew;
                }
                if (listPrefSong.get(position).albums.size() > 0) {
                    if (listPrefSong.get(position).albums.get(0).titleHebrew == null || listPrefSong.get(position).albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                        albumString = listPrefSong.get(position).albums.get(0).title;
                    } else {
                        albumString = listPrefSong.get(position).albums.get(0).titleHebrew;
                    }
                }
                holder.binding.tvAlbumTitle.setText(Html.fromHtml(Utility.setTextToTextView(artistString, albumString)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (listPrefSong.get(position).isPremium) {
                holder.binding.imgTag.setVisibility(View.VISIBLE);
                holder.binding.imgTag.setImageResource(R.drawable.premium_tag_hw);
            } else {
                holder.binding.imgTag.setVisibility(View.GONE);
            }
        } else {
            if (listPrefSong.size() > 0) {
                try {
                    Glide.with(context).load(listPrefSong.get(position).artwork).placeholder(R.drawable.imglogo).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(holder.binding.imgAlbIcon);
                    holder.binding.tvTitle.setText(listPrefSong.get(position).title);
                    holder.binding.tvAlbumTitle.setText(Html.fromHtml(Utility.setTextToTextView(listPrefSong.get(position).artist.name, listPrefSong.get(position).albums.get(0).title)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listPrefSong.get(position).isPremium) {
                    holder.binding.imgTag.setVisibility(View.VISIBLE);
                    holder.binding.imgTag.setImageResource(R.drawable.premium_tag_en);
                } else {
                    holder.binding.imgTag.setVisibility(View.GONE);
                }
            }
        }

        try {
            if (data.contains(listPrefSong.get(position).id)) {
                holder.binding.imgPlus.setVisibility(View.GONE);
            } else {
                holder.binding.imgPlus.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.binding.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Dialog d = new BottomSheetDialog(context);
                    d.setContentView(R.layout.user_action_onsong_cell);
                    View view = d.findViewById(R.id.bs);
                    final TextView deleteit = (TextView) d.findViewById(R.id.lblRemove);
                    TextView artist_name = (TextView) d.findViewById(R.id.tvArtistName);
                    ImageView artistImage = (ImageView) d.findViewById(R.id.ivArtistImage);
                    ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (listPrefSong.get(position).title_hebrew.equalsIgnoreCase("") || listPrefSong.get(position).title_hebrew == null) {
                            artist_name.setText(listPrefSong.get(position).title);
                        } else {
                            artist_name.setText(listPrefSong.get(position).title_hebrew);
                        }
                    } else {
                        artist_name.setText(listPrefSong.get(position).title);
                    }
                    try {
                        Glide.with(context).load(listPrefSong.get(position).artwork).error(R.drawable.imglogo).crossFade().into(artistImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (isFrom.equalsIgnoreCase("home")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    } else if (isFrom.equalsIgnoreCase("recommended")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    } else if (isFrom.equalsIgnoreCase("player")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    } else if (isFrom.equalsIgnoreCase("AlbumDetail")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    } else if (isFrom.equalsIgnoreCase("playlist")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.VISIBLE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        deleteit.setText(context.getResources().getString(R.string.remove_from_playlist));
                    } else if (isFrom.equalsIgnoreCase("shirali_playlist")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (Utility.getUserInfo(context).id.equalsIgnoreCase(UserModel.getInstance().shirali.createdBy)) {
                            deleteit.setText(context.getResources().getString(R.string.remove_from_playlist));
                        } else {
                            if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                                d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                            } else {
                                d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (isFrom.equalsIgnoreCase("song")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    } else if (isFrom.equalsIgnoreCase("search")) {
                        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                        } else {
                            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                        }
                    }
                    d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.cancel();
                        }
                    });
                    d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                context.startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("id", listPrefSong.get(position).id));
                            } else {
                                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytShareSong).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Utility.shareIt(context, "song", listPrefSong.get(position).title, listPrefSong.get(position).artist.name, listPrefSong.get(position).shareUrl);
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (listPrefSong.get(position).albums !=null) {
                                if (listPrefSong.get(position).albums.size() > 0) {
                                    UserModel.getInstance().tempAlbum = listPrefSong.get(position).albums.get(0);
                                    if (listPrefSong.get(position).albums.get(0).isPremium) {
                                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                            openAlbum(position);
                                        } else {
                                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                                        }
                                    } else {
                                        openAlbum(position);
                                    }
                                } else {
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
                            if (listPrefSong.size() > 0) {
                                try {
                                    if (listPrefSong.get(position).artist.isPremium) {
                                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                            Intent intent = new Intent(context, ArtistDetailActivity.class);
                                            intent.putExtra("artist_id", listPrefSong.get(position).artist.id);
                                            context.startActivity(intent);
                                        } else {
                                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                        }
                                    } else {
                                        Intent intent = new Intent(context, ArtistDetailActivity.class);
                                        intent.putExtra("artist_id", listPrefSong.get(position).artist.id);
                                        context.startActivity(intent);
                                    }
                                } catch (Resources.NotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                UserModel.getInstance().removeSongIndex = position;
                                mItemClickListener.onItemVisible(v, false);
                                if (deleteit.getText().toString().equalsIgnoreCase(context.getResources().getString(R.string.remove_from_playlist))) {
                                    onItemClicRemove.onItemClick(v, listPrefSong, position);
                                } else {
                                    UserModel.getInstance().removeFromMusic(context, Utility.getUserInfo(context).id, listPrefSong.get(position).id);
                                    UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                                        @Override
                                        public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                                            if (isAdded) {
                                                updateMyMusicList(myMusic);
                                            }
                                        }
                                    });
                                    d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                                    final Dialog dialog = new Dialog(context);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setContentView(R.layout.add_song_layout);
                                    TextView title = (TextView) dialog.findViewById(R.id.lytAdd);
                                    title.setText(R.string.song_remove_from_my_music);
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            d.dismiss();
                        }
                    });
                    d.setCancelable(true);
                    d.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        holder.binding.lytSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Constants.isSongPlay = true;
                UserModel.getInstance().isSingleSongPlay = isAlbum;
                UserModel.getInstance().tempSongList.clear();
                UserModel.getInstance().tempSongList.addAll(listPrefSong);
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    playSong(v, position);
                } else {
                    Album tempAlbum = new Album();
                    if (listPrefSong.get(position).albums !=null){
                        if (listPrefSong.get(position).albums.size()>0){
                            tempAlbum = listPrefSong.get(position).albums.get(0);
                        }
                    }
                    if (listPrefSong.get(position).isPremium || listPrefSong.get(position).artist.isPremium || tempAlbum.isPremium) {
                        if (listPrefSong.get(position).isPremium) {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                        } else if(listPrefSong.get(position).artist.isPremium){
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    }else {
                        playSong(v, position);
                    }
                }
            }

        });

        holder.binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    holder.binding.imgPlus.setVisibility(View.GONE);
                    UserModel.getInstance().addToMusicWithCallback(context, Utility.getUserInfo(context).id, listPrefSong.get(position).id, new AddToMyMusicCallback() {
                        @Override
                        public void checkAdd(boolean isAdd) {
                            if (isAdd){
                                UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                                    @Override
                                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                                        if (isAdded) {
                                            updateMyMusicList(myMusic);
                                        }
                                    }
                                });
                                mItemClickListener.onItemVisible(v, true);
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
                            }
                        }
                    });
                } else {
                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
            }
        });
    }

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().artist_id = listPrefSong.get(position).artist;
            UserModel.getInstance().album = listPrefSong.get(position).albums.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(context, AlbumDetailActivity.class);
        context.startActivity(intent);
    }

    private void playSong(final View v, final int position) {
        Constants.SONGS_LIST = listPrefSong;
        Constants.SONG_NUMBER = position;
        UserModel.getInstance().getAppSetting(context);
        UserModel.getInstance().freePaidUser(context, listPrefSong, position, new FreePaidUserCallBack() {
            @Override
            public void freePaidUser(boolean ifPaid) {
                if (ifPaid) {
                    UserModel.getInstance().listOfShuffleSong.clear();
                    UserModel.getInstance().listOfShuffleSong.addAll(listPrefSong);
                    Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                    UserModel.getInstance().listOfActualSong.clear();
                    UserModel.getInstance().listOfActualSong.addAll(listPrefSong);
                    mItemClickListener.onItemClick(v, "", position);
                } else {
                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPrefSong != null ? listPrefSong.size() : 0;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setOnItemClickRemovr(final OnItemClicRemove onItemClickRemovr) {
        this.onItemClicRemove = onItemClickRemovr;
    }

    public void delete() {
        listPrefSong.clear();
    }

    //Local filter for song according genres
    public void filterList(String genres) {
        if (isForFirstTime) {
            isForFirstTime = false;
            listNewSongs.addAll(listPrefSong);
        }
        if (genres.equalsIgnoreCase("all")) {
            listPrefSong.clear();
            listPrefSong.addAll(listNewSongs);
        } else {
            listPrefSong.clear();
            for (int i = 0; i < listNewSongs.size(); i++) {
                for (int j = 0; j < listNewSongs.get(i).genres.size(); j++) {
                    if (listNewSongs.get(i).genres.get(j).title.equalsIgnoreCase(genres)) {
                        listPrefSong.add(listNewSongs.get(i));
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateMyMusicList(ArrayList<String> myMuisc) {
        this.data = myMuisc;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String type, int position);

        void onItemVisible(View view, boolean isVisible);
    }

    public interface OnItemClicRemove {
        void onItemClick(View view, ArrayList<Song> list, int postion);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSongforplayBinding binding;

        public ViewHolder(View itemView, ItemSongforplayBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
