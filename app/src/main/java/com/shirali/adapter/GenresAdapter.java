package com.shirali.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.shirali.activity.GenreDetailActivity;
import com.shirali.databinding.ItemGenersBinding;
import com.shirali.model.songs.Genre;
import com.shirali.util.Utility;

import java.util.ArrayList;
/**
 * Created by Sagar on 10/7/17.
 */
public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.ViewHolder> {

    private Context context;
    private ItemGenersBinding binding;
    private ArrayList<Genre> list;
    private boolean isFrom;
    private OnItemClickListener mItemClickListener;
    private ArrayList<String> listOfGenre;

    public GenresAdapter(Context context, ArrayList<Genre> alist, boolean isFrom, ArrayList<String> listOfGenre) {
        this.list = alist;
        this.context = context;
        this.isFrom = isFrom;
        this.listOfGenre = listOfGenre;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_geners, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            if (listOfGenre.contains(list.get(position).id)) {
                holder.binding.check.setVisibility(View.VISIBLE);
            }
            if (isFrom) {
                if (list.size() > 0) {
                    holder.binding.lytFilterGenre.setVisibility(View.GONE);
                    holder.binding.lytGenres.setVisibility(View.VISIBLE);
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (list.get(position).titleHebrew.equalsIgnoreCase("") || list.get(position).titleHebrew == null) {
                            holder.binding.lblTitle.setText(list.get(position).title);
                        } else {
                            holder.binding.lblTitle.setText(list.get(position).titleHebrew);
                        }
                    } else {
                        holder.binding.lblTitle.setText(list.get(position).title);
                    }
                    Glide.with(context).load(list.get(position).icon).placeholder(R.drawable.logo).listener(new RequestListener<String, GlideDrawable>() {
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
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgGenresIcon);
                }
                holder.binding.lytGenres.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(context, GenreDetailActivity.class).putExtra("genre", list.get(position).id).putExtra("genresName", list.get(position).title).putExtra("genresNameHebrew", list.get(position).titleHebrew));
                    }
                });
            } else {
                holder.binding.lytFilterGenre.setVisibility(View.VISIBLE);
                holder.binding.lytGenres.setVisibility(View.GONE);
                try {
                    Glide.with(context).load(list.get(position).icon).listener(new RequestListener<String, GlideDrawable>() {
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
                    }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().error(R.drawable.imglogo).into(holder.binding.imgCircle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                    holder.binding.title.setText(list.get(position).titleHebrew);
                } else {
                    holder.binding.title.setText(list.get(position).title);
                }
                holder.binding.imgCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.binding.check.getVisibility() == view.INVISIBLE) {
                            holder.binding.check.setVisibility(View.VISIBLE);
                            mItemClickListener.onItemClick(position);
                        } else {
                            holder.binding.check.setVisibility(View.INVISIBLE);
                            mItemClickListener.onItemClick(position);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemGenersBinding binding;

        public ViewHolder(View itemView, ItemGenersBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
