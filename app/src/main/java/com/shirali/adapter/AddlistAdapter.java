package com.shirali.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Looper;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.App;
import com.shirali.R;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.databinding.AddPlaylistBinding;
import com.shirali.model.playlist.Playlist;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;
import java.util.logging.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sagar on 11/8/17.
 */

public class AddlistAdapter extends RecyclerView.Adapter<AddlistAdapter.ViewHolder> {
    private final ArrayList<Shirali> list;
    private final Context mContext;
    private AddPlaylistBinding binding;
    private OnItemClickListener mItemClickListener;
    private App app;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int seconds = 0;
    private String isFrom;

    public AddlistAdapter(Context context, ArrayList<Shirali> list, String from) {
        this.mContext = context;
        this.list = list;
        this.isFrom = from;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.add_playlist, parent, false);
        app = (App) mContext.getApplicationContext();
        preferences = mContext.getSharedPreferences("playlist", 0);
        editor = preferences.edit();
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        seconds = 0;
        for (int i = 0; i < list.get(position).songs.size(); i++) {
            seconds = seconds + Math.round(list.get(position).songs.get(i).durationSeconds);

        }
        if (list.get(position).songs.size() > 1) {
            holder.binding.tvName.setText(Html.fromHtml(Utility.setTextToTextView(list.get(position).songs.size() + " " + mContext.getResources().getString(R.string.songs), Utility.formatSeconds(mContext, seconds))));
        } else if (list.get(position).songs.size() == 0 || list.get(position).songs.size() == 1) {
            holder.binding.tvName.setText(Html.fromHtml(Utility.setTextToTextView(list.get(position).songs.size() + " " + mContext.getResources().getString(R.string.song), Utility.formatSeconds(mContext, seconds))));
        }
        if (list.get(position).avatar != null) {
            if (list.get(position).avatar.equalsIgnoreCase("")) {
                try {
                    Glide.with(mContext).load(list.get(position).songs.get(0).artwork).placeholder(R.drawable.logo).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(holder.binding.imgAlbIcon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Glide.with(mContext).load(list.get(position).avatar).placeholder(R.drawable.logo).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(holder.binding.imgAlbIcon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (list.get(position).title_hebrew == null || list.get(position).title_hebrew.equalsIgnoreCase("")) {
                    holder.binding.tvTitle.setText(list.get(position).title);
                } else {
                    holder.binding.tvTitle.setText(list.get(position).title_hebrew);
                }
            } else {
                holder.binding.tvTitle.setText(list.get(position).title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isFrom.equalsIgnoreCase("add_in_playlist")) {
            holder.binding.imgMenu.setVisibility(View.GONE);
        } else {
            holder.binding.imgMenu.setVisibility(View.VISIBLE);
        }

        holder.binding.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.getUserInfo(mContext).id.equalsIgnoreCase(list.get(position).createdBy)) {
                    final Dialog d = new BottomSheetDialog(mContext);
                    d.setContentView(R.layout.user_action_onsong_cell);
                    final ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
                    final TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (list.get(position).title_hebrew == null || list.get(position).title_hebrew.equalsIgnoreCase("") ) {
                                    title.setText(list.get(position).title);
                                } else {
                                    title.setText(list.get(position).title_hebrew);
                                }
                            }
                        }).start();

                    } else {
                        title.setText(list.get(position).title);
                    }
                    try {
                        if (list.get(position).avatar.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(list.get(position).songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        } else {
                            Glide.with(mContext).load(list.get(position).avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    View view = d.findViewById(R.id.bs);
                    ((View) view.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                    TextView delete = (TextView) d.findViewById(R.id.lblRemove);
                    delete.setText(mContext.getResources().getString(R.string.delete));
                    d.findViewById(R.id.lytPlay).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytAddMore).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytEditPlaylist).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytSharePlaylist).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
                    d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.lytPlay).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editor.putString("current", list.get(position).id);
                            editor.commit();
                            mItemClickListener.onItemClick(view, position);
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytAddMore).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid")||UserModel.getInstance().isForRenew||UserModel.getInstance().isForTrial) {
                                mItemClickListener.onItemClick(v, position);
                            } else {
                                Utility.showSubscriptionAlert(mContext,mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.cancel();
                        }
                    });
                    d.findViewById(R.id.lytEditPlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mItemClickListener.onItemClick(view, position);
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytSharePlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Utility.shareIt(mContext, "playlist", list.get(position).title, "", list.get(position).shareUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (list.size() > 0) {
                                openAlbum(position);
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (list.size() > 0) {
                                if (list.get(position).songs.get(0).artist.isPremium) {
                                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                        intent.putExtra("artist_id", list.get(position).songs.get(0).artist.id);
                                        mContext.startActivity(intent);
                                    } else {
                                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                    }
                                } else {
                                    Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                    intent.putExtra("artist_id", list.get(position).songs.get(0).artist.id);
                                    mContext.startActivity(intent);
                                }
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog openDialog = new Dialog(mContext);
                            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            openDialog.setContentView(R.layout.custom_alert);
                            TextView titlee = (TextView) openDialog.findViewById(R.id.lblTitle);
                            titlee.setText(mContext.getResources().getString(R.string.are_you_really_want_to_delete));
                            TextView canclee = (TextView) openDialog.findViewById(R.id.lblCancel);
                            TextView actione = (TextView) openDialog.findViewById(R.id.lblOkay);
                            actione.setText(mContext.getResources().getString(R.string.yes));
                            canclee.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDialog.dismiss();
                                }
                            });
                            actione.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    removePlaylist(list.get(position).id, position);
                                    openDialog.dismiss();
                                }
                            });
                            openDialog.show();
                            d.dismiss();
                        }
                    });
                    d.setCancelable(true);
                    d.show();
                } else {
                    final Dialog d = new BottomSheetDialog(mContext);
                    d.setContentView(R.layout.user_action_onsong_cell);
                    ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
                    TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                    View view = d.findViewById(R.id.bs);
                    ((View) view.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                    if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        title.setText(
                                list.get(position).title_hebrew == null ||
                                        list.get(position).title_hebrew.equalsIgnoreCase("")
                                        ? list.get(position).title
                                        : list.get(position).title_hebrew);
                    } else {
                        title.setText(list.get(position).title);
                    }
                    try {
                        if (list.get(position).avatar.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(list.get(position).songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        } else {
                            Glide.with(mContext).load(list.get(position).avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                    d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                    d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
                    d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                    d.findViewById(R.id.lytSharePlaylist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Utility.shareIt(mContext, "playlist", list.get(position).title, "", list.get(position).shareUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.lytUnFollow).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            unfollowPlaylist(list.get(position).id, position);
                            d.dismiss();
                        }
                    });
                    d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.cancel();
                        }
                    });

                    d.setCancelable(true);
                    d.show();
                }
            }
        });

        holder.binding.lytSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size() > 0) {
                    editor.putString("current", list.get(position).id);
                    editor.commit();
                    mItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().album = list.get(position).songs.get(0).albums.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mContext, AlbumDetailActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AddPlaylistBinding binding;

        public ViewHolder(View itemView, AddPlaylistBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    //Un follow playlist
    private void unfollowPlaylist(String play_id, final int position) {
        Call<UserModel> call = Constants.service.unfollowShiraliPlaylist(Utility.getUserInfo(mContext).id, play_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (user.success) {
                            if (user.user != null) {
                                Utility.showPopup(mContext, mContext.getResources().getString(R.string.unfollow_playlist));
                                Utility.setUserInfo(mContext, user.user);
                                list.remove(position);
                                notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {

            }
        });
    }

    //remove playlist
    private void removePlaylist(String id, final int position) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        mixpanelAPI.track("Delete Playlist");
        Call<Playlist> call = Constants.service.deletePlaylist(id);
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                Playlist playlist = response.body();
                try {
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlist.success) {
                            list.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}