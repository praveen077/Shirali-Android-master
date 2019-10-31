package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.DropDownListItemBinding;
import com.shirali.util.Utility;

import java.util.ArrayList;

/**
 * Created by Sagar on 11/8/17.
 */

public class FilterGenresAdapter extends RecyclerView.Adapter<FilterGenresAdapter.ViewHolder> {

    private DropDownListItemBinding binding;
    private Context context;
    private ArrayList<String> listTrending;
    private ArrayList<String> listGenresHebrew;
    private OnItemClickListener mItemClickListener;

    public FilterGenresAdapter(Context context, ArrayList<String> listTrending,ArrayList<String> listGenresHebrew) {
        this.context = context;
        this.listTrending = listTrending;
        this.listGenresHebrew = listGenresHebrew;
    }

    @Override
    public FilterGenresAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.drop_down_list_item, parent, false);
        return new FilterGenresAdapter.ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (listTrending.size() > 0) {
            try {
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (listGenresHebrew.get(position) != null) {
                        if (listGenresHebrew.get(position).equalsIgnoreCase("")) {
                            holder.binding.lblSelectedGenres.setText(listTrending.get(position));
                        } else {
                            holder.binding.lblSelectedGenres.setText(listGenresHebrew.get(position));
                        }
                    } else {
                        holder.binding.lblSelectedGenres.setText(listTrending.get(position));
                    }
                } else {
                    holder.binding.lblSelectedGenres.setText(listTrending.get(position));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return listTrending.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private DropDownListItemBinding binding;

        public ViewHolder(View itemView, final DropDownListItemBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                        if (listGenresHebrew.get(getAdapterPosition()).equalsIgnoreCase("") || listGenresHebrew.get(getAdapterPosition()) == null) {
                            mItemClickListener.onItemClick(v, listTrending.get(getAdapterPosition()), getAdapterPosition());
                        } else {
                            mItemClickListener.onItemClick(v, listGenresHebrew.get(getAdapterPosition()), getAdapterPosition());
                        }
                    } else {
                        mItemClickListener.onItemClick(v, listTrending.get(getAdapterPosition()), getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String value,int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
