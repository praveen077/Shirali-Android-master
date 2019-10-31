package com.shirali.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.activity.AddPlaylistsActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.databinding.ItemRecyclerViewLayoutBinding;
import com.shirali.databinding.RecyclerSectionHeaderBinding;
import com.shirali.interfaces.AddArtistOrNot;
import com.shirali.model.songs.Artist;
import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sagar on 16/5/18.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder>
        implements SectionIndexer, StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private List<Artist> mDataArray;
    private ArrayList<Integer> mSectionPositions;
    private Context context;
    private ItemRecyclerViewLayoutBinding binding;
    private List<Artist> tempList = new ArrayList<>();
    private boolean isForFirstTime = false;
    private String typeSong, typeAlbum;
    private OnItemClickListener mItemClickListener;

    public ArtistAdapter(Context context, List<Artist> dataset) {
        this.context = context;
        this.mDataArray = dataset;
        isForFirstTime = true;
    }

    @Override
    public long getHeaderId(int position) {
        return mDataArray.get(position).name.charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        RecyclerSectionHeaderBinding mbinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.recycler_section_header, parent, false);
        return new RecyclerView.ViewHolder(mbinding.getRoot()) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, final int position) {
        TextView textView = (TextView) holder.itemView.findViewById(R.id.list_item_section_text);
        TextView viewAll = (TextView) holder.itemView.findViewById(R.id.tvViewAll);
        String section = String.valueOf(mDataArray.get(position).name.charAt(0)).toUpperCase();
        textView.setText(section);
        if (mDataArray.get(position).count > 20) {
            viewAll.setVisibility(View.VISIBLE);
        } else {
            viewAll.setVisibility(View.GONE);
        }
        holder.itemView.setTag(R.id.tvSongTag,section);
        holder.itemView.setTag(R.id.img,mDataArray.get(position).count);
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_recycler_view_layout, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(context).load(mDataArray.get(position).avatar).error(R.drawable.imglogo).into(holder.binding.ivCircularArtist);
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            if (mDataArray.get(position).nameHebrew != null && mDataArray.get(position).nameHebrew.equalsIgnoreCase("")) {
                holder.binding.tvAlphabet.setText(mDataArray.get(position).name);
            } else {
                holder.binding.tvAlphabet.setText(mDataArray.get(position).nameHebrew);
            }
        } else {
            holder.binding.tvAlphabet.setText(mDataArray.get(position).name);
        }
        if (mDataArray.get(position).albumCount > 1) {
            typeAlbum = "Albums";
        } else {
            typeAlbum = "Album";
        }
        if (mDataArray.get(position).songCount > 1) {
            typeSong = "Songs";
        } else {
            typeSong = "Song";
        }
        holder.binding.lblArtistInfo.setText(Html.fromHtml(Utility.setTextToTextView(mDataArray.get(position).albumCount + " " + typeAlbum, mDataArray.get(position).songCount + " " + typeSong)));

        if (mDataArray.get(position).songCount > 0) {
            if (Utility.getUserInfo(context).myMusic.containsAll(mDataArray.get(position).songsId)) {
                holder.binding.imgPlus.setVisibility(View.GONE);
            } else {
                holder.binding.imgPlus.setVisibility(View.VISIBLE);
            }
        }

        holder.binding.lytArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDataArray.get(position).isPremium) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", mDataArray.get(position).id));
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    }
                } else {
                    context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", mDataArray.get(position).id));
                }
            }
        });

        holder.binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (mDataArray.get(position).songsId.size() > 0) {
                        mItemClickListener.onItemClick(v, position, "plus", new AddArtistOrNot() {
                            @Override
                            public void checkAdd() {
                                holder.binding.imgPlus.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Utility.showArtistInProgress(context);
                    }
                } else {
                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                }
            }
        });

        holder.binding.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(holder, context, position);
            }
        });
    }

    @Override
    public int getSectionForPosition(int position) {
        return position;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = mDataArray.size(); i < size; i++) {
            String section = String.valueOf(mDataArray.get(i).name.charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerViewLayoutBinding binding;

        ViewHolder(View itemView, ItemRecyclerViewLayoutBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    //Local filter for song according genres
    public void filterList(String genres) {
        if (isForFirstTime) {
            isForFirstTime = false;
            tempList.addAll(mDataArray);
        }

        if (genres.equalsIgnoreCase("")) {
            mDataArray.clear();
            mDataArray.addAll(tempList);
        } else {
            mDataArray.clear();
            for (int i = 0; i < tempList.size(); i++) {
                if (tempList.get(i).name.toLowerCase().contains(genres)) {
                    mDataArray.add(tempList.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String operationType, AddArtistOrNot check);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    //open bottom sheet dialog
    private void openMenu(final ViewHolder holder, final Context context, final int position) {
        final Dialog d = new BottomSheetDialog(context);
        d.setContentView(R.layout.user_action_onsong_cell);
        final View view = d.findViewById(R.id.bs);
        ImageView image = (ImageView) d.findViewById(R.id.ivArtistImage);
        TextView title = (TextView) d.findViewById(R.id.tvArtistName);
        ((View) view.getParent()).setBackgroundColor(context.getApplicationContext().getResources().getColor(android.R.color.transparent));

        if (mDataArray.get(position) != null) {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (mDataArray.get(position).nameHebrew == null || mDataArray.get(position).nameHebrew.equalsIgnoreCase("")) {
                    title.setText(mDataArray.get(position).name);
                } else {
                    title.setText(mDataArray.get(position).nameHebrew);
                }
            } else {
                title.setText(mDataArray.get(position).name);
            }
        }
        try {
            Glide.with(context).load(mDataArray.get(position).avatar).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(image);
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
        if (holder.binding.imgPlus.getVisibility() == View.VISIBLE) {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
        } else {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
        }
        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    if (mDataArray.get(position).songsId.size() > 0) {
                        context.startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("list_of_song", mDataArray.get(position).songsId));
                    } else {
                        Utility.showArtistInProgress(context);
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
                    Utility.shareIt(context, "artist", mDataArray.get(position).name, "", mDataArray.get(position).shareUrl);
                } catch (Exception e) {
                    e.printStackTrace();
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
        d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v, position, "remove", new AddArtistOrNot() {
                    @Override
                    public void checkAdd() {
                        holder.binding.imgPlus.setVisibility(View.VISIBLE);
                    }
                });
                d.dismiss();
            }
        });
        d.setCancelable(true);
        d.show();
    }

}
