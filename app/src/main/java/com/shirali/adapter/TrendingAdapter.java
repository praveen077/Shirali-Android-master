package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.ItemTrendingBinding;

import java.util.ArrayList;

/**
 * Created by Sagar on 11/8/17.
 */

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.ViewHolder> {
    private ItemTrendingBinding binding;
    private Context context;
    private ArrayList<String> listTrending;
    private OnItemClickListener mItemClickListener;

    public TrendingAdapter(Context context, ArrayList<String> listTrending) {
        this.context = context;
        this.listTrending = listTrending;
    }

    @Override
    public TrendingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_trending, parent, false);
        return new TrendingAdapter.ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (listTrending.size() > 0) {
            try {
                holder.binding.tvTitle.setText(listTrending.get(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return listTrending.size();
    }

    public void delete() {
        listTrending.clear();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemTrendingBinding binding;

        public ViewHolder(View itemView, final ItemTrendingBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v, binding.tvTitle.getText().toString().trim(),getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {}
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String value,int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
