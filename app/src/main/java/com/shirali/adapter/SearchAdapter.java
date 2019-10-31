package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.ItemSearchBinding;

import java.util.ArrayList;

/**
 * Created by Sagar on 13-07-2017.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private ItemSearchBinding binding;
    private Context context;
    private ArrayList<String> listCelebs;
    private OnItemClickListener mItemClickListener;

    public SearchAdapter(Context context,  ArrayList<String> listCelebs) {
        this.context = context;
        this.listCelebs = listCelebs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_search, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (listCelebs.size() > 0) {
            try {
                holder.binding.tvTitle.setText(listCelebs.get(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (listCelebs.size() > 5) {
            return 5;
        } else {
            return listCelebs.size();
        }
    }

    public void delete() {
        listCelebs.clear();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemSearchBinding binding;

        public ViewHolder(View itemView, final ItemSearchBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v,binding.tvTitle.getText().toString().trim(),getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view,String value,int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
