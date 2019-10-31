package com.shirali.adapter;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.databinding.ItemWeeklylistBinding;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sagar on 10/7/17.
 */

public class WeeklylistAdapter extends RecyclerView.Adapter<WeeklylistAdapter.ViewHolder> {
    private ItemWeeklylistBinding binding;
    private OnItemClickListener mItemClickListener;
    private ArrayList<Shirali> listShirali;
    private ArrayList<String> listFollow;
    private Context context;
    private boolean isFollow = false;

    public WeeklylistAdapter(Context context, ArrayList<Shirali> listPlay, ArrayList<String> listFollow) {
        this.context = context;
        this.listShirali = listPlay;
        this.listFollow = listFollow;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_weeklylist, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            if (listShirali.get(position).title_hebrew.equalsIgnoreCase("") || listShirali.get(position).title_hebrew == null) {
                holder.binding.tvTitle.setText(listShirali.get(position).title);
            } else {
                holder.binding.tvTitle.setText(listShirali.get(position).title_hebrew);
            }
            if (listShirali.get(position).description_hebrew.equalsIgnoreCase("") || listShirali.get(position).description_hebrew == null) {
                holder.binding.tvName.setText(listShirali.get(position).description);
            } else {
                holder.binding.tvName.setText(listShirali.get(position).description_hebrew);
            }
        } else {
            holder.binding.tvTitle.setText(listShirali.get(position).title);
            holder.binding.tvName.setText(listShirali.get(position).description);
        }
        try {
            if (!listShirali.get(position).avatar.equalsIgnoreCase("")) {
                Utility.loadMailPlayImage(listShirali.get(position).avatar, context).error(R.drawable.imglogo).into(holder.binding.imgAlbIcon);
            } else {
                Glide.with(context).load(listShirali.get(position).songs.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(holder.binding.imgAlbIcon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.binding.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new BottomSheetDialog(context);
                d.setContentView(R.layout.user_action_onsong_cell);
                CircleImageView image = (CircleImageView) d.findViewById(R.id.ivArtistImage);
                TextView title = (TextView) d.findViewById(R.id.tvArtistName);
                final LinearLayout follow = (LinearLayout) d.findViewById(R.id.lytFollowPlaylist);
                final LinearLayout unFollow = (LinearLayout) d.findViewById(R.id.lytUnFollow);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    title.setText(listShirali.get(position).title_hebrew);
                } else {
                    title.setText(listShirali.get(position).title);
                }
                try {
                    Glide.with(context).load(listShirali.get(position).avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                View view = d.findViewById(R.id.bs);
                ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
                d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
                d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
                d.findViewById(R.id.lytAddToPlaylist).setVisibility(View.GONE);
                d.findViewById(R.id.lytShareSong).setVisibility(View.GONE);
                d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
                d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
                d.findViewById(R.id.lytViewArtist).setVisibility(View.GONE);
                d.findViewById(R.id.lytViewAlbum).setVisibility(View.GONE);
                d.findViewById(R.id.lytSharePlaylist).setVisibility(View.VISIBLE);
                if (Utility.getUserInfo(context).playlist.contains(listShirali.get(position).id)/* || isFollow*/) {
                    follow.setVisibility(View.GONE);
                    unFollow.setVisibility(View.VISIBLE);
                } else {
                    follow.setVisibility(View.VISIBLE);
                    unFollow.setVisibility(View.GONE);
                }
                d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
                d.findViewById(R.id.lytSharePlaylist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Utility.shareIt(context, "playlist", listShirali.get(position).title, "", listShirali.get(position).shareUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        d.dismiss();
                    }
                });
                d.findViewById(R.id.lytFollowPlaylist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (follow.getVisibility() == View.VISIBLE) {
                            isFollow = true;
                            follow.setVisibility(View.GONE);
                            unFollow.setVisibility(View.VISIBLE);
                            followPlaylist(listShirali.get(position).id);
                            notifyDataSetChanged();
                            Utility.showPopup(context, context.getResources().getString(R.string.follow_playlist_text));
                        }
                        d.dismiss();
                    }
                });
                d.findViewById(R.id.lytUnFollow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (unFollow.getVisibility() == View.VISIBLE) {
                            isFollow = false;
                            follow.setVisibility(View.VISIBLE);
                            unFollow.setVisibility(View.GONE);
                            unfollowPlaylist(listShirali.get(position).id, position);
                            notifyDataSetChanged();
                            Utility.showPopup(context, context.getResources().getString(R.string.unfollow_playlist_text));
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
                d.setCancelable(true);
                d.show();
            }
        });

        holder.binding.lytPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listShirali != null ? listShirali.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemWeeklylistBinding binding;

        public ViewHolder(View itemView, ItemWeeklylistBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    //un follow playlist
    private void unfollowPlaylist(String play_id, final int position) {
        Call<UserModel> call = Constants.service.unfollowShiraliPlaylist(Utility.getUserInfo(context).id, play_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (user.user != null) {
                            if (user.success) {
                                UserModel.getInstance().getdata(context);
                                Utility.setUserInfo(context, user.user);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    //follow playlist
    private void followPlaylist(String play_id) {
        Call<UserModel> call = Constants.service.followShiraliPlaylist(Utility.getUserInfo(context).id, play_id);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (user.user != null) {
                            if (user.success) {
                                UserModel.getInstance().getdata(context);
                                Utility.setUserInfo(context, user.user);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
