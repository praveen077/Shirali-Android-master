package com.shirali.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.ItemSongforplayBinding;
import com.shirali.model.HomeCellModel;

import java.util.ArrayList;

/**
 * Created by Sagar on 13-07-2017.
 */

public class RecentAllAdapter extends RecyclerView.Adapter<RecentAllAdapter.ViewHolder> {
    private ArrayList<HomeCellModel> list;
    private ItemSongforplayBinding binding;
    private OnItemClickListener mItemClickListener;

    public RecentAllAdapter(ArrayList<HomeCellModel> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_songforplay, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        binding.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
