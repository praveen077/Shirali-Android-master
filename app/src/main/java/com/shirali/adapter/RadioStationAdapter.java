package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.databinding.ItemRadioBinding;
import com.shirali.model.stations.Stations;
import com.shirali.util.Utility;

import java.util.ArrayList;


/**
 * Created by Sagar on 11/8/17.
 */

public class RadioStationAdapter extends RecyclerView.Adapter<RadioStationAdapter.ViewHolder> {

    private ItemRadioBinding binding;
    private Context context;
    private ArrayList<Stations> listStation;
    private OnItemClickListener mItemClickListener;

    public RadioStationAdapter(Context context, ArrayList<Stations> listStation) {
        this.context = context;
        this.listStation = listStation;

    }

    @Override
    public RadioStationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_radio, parent, false);
        return new RadioStationAdapter.ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            try {
                if (listStation.get(position).titleHebrew != null && listStation.get(position).titleHebrew.equalsIgnoreCase("")) {
                    holder.binding.tvTitle.setText(listStation.get(position).title);
                } else {
                    holder.binding.tvTitle.setText(listStation.get(position).titleHebrew);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                holder.binding.tvTitle.setText(listStation.get(position).title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Glide.with(context).load(listStation.get(position).imageurl).placeholder(R.drawable.radio).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.radio).crossFade().into(holder.binding.imgStation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.binding.lytRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v, position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return listStation.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemRadioBinding binding;

        public ViewHolder(View itemView, final ItemRadioBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
