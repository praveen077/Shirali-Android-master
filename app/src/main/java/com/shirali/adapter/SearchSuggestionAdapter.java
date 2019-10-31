package com.shirali.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shirali.R;
import com.shirali.databinding.ItemSearchSuggestionBinding;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.Artist;
import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;

import java.util.ArrayList;

/**
 * Created by Sagar on 25/1/18.
 */

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {
    private ItemSearchSuggestionBinding binding;
    private Context context;
    private ArrayList<Album> listAlbum;
    private ArrayList<Artist> listArtist;
    private ArrayList<Shirali> listPlaylist;
    private String isFrom, letter;
    private SearchSuggestionAdapter.OnItemClickListener mItemClickListener;

    public SearchSuggestionAdapter(Context context, ArrayList<Album> listTrending, ArrayList<Artist> listArtist, ArrayList<Shirali> listPlaylist, String type, String letter) {
        this.context = context;
        this.listAlbum = listTrending;
        this.listArtist = listArtist;
        this.listPlaylist = listPlaylist;
        this.isFrom = type;
        this.letter = letter;
    }

    @Override
    public SearchSuggestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_search_suggestion, parent, false);
        return new SearchSuggestionAdapter.ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isFrom.equalsIgnoreCase("Album")) {
            SpannableString spanText = null;
            String originalString = "";
            int start = 0, end = 0;
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (listAlbum.get(position).titleHebrew == null || listAlbum.get(position).titleHebrew.equalsIgnoreCase("")) {
                    originalString = listAlbum.get(position).title.toLowerCase();
                    spanText = new SpannableString(listAlbum.get(position).title);
                } else {
                    originalString = listAlbum.get(position).title.toLowerCase();
                    spanText = new SpannableString(listAlbum.get(position).titleHebrew);
                }
            } else {
                originalString = listAlbum.get(position).title.toLowerCase();
                spanText = new SpannableString(listAlbum.get(position).title);
            }
            start = originalString.indexOf(UserModel.getInstance().suggestionLetter.toLowerCase());
            if (start < 0) start = 0;
            end = start + UserModel.getInstance().suggestionLetter.length();
            if (start >= 0 && end > 0) {
                if ((end - start) <= spanText.length()) {
                    spanText.setSpan(new RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.binding.tvTitle.setText(spanText);
                }
            }
        } else if (isFrom.equalsIgnoreCase("Artist")) {
            SpannableString spanText = null;
            String originalString = "";
            int start = 0, end = 0;
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (listArtist.get(position).nameHebrew == null || listArtist.get(position).nameHebrew.equalsIgnoreCase("")) {
                    originalString = listArtist.get(position).name.toLowerCase();
                    spanText = new SpannableString(listArtist.get(position).name);
                } else {
                    originalString = listArtist.get(position).nameHebrew.toLowerCase();
                    spanText = new SpannableString(listArtist.get(position).nameHebrew);
                }
            } else {
                originalString = listArtist.get(position).name.toLowerCase();
                spanText = new SpannableString(listArtist.get(position).name);
            }
            start = originalString.indexOf(UserModel.getInstance().suggestionLetter.toLowerCase());
            if (start < 0) start = 0;
            end = start + UserModel.getInstance().suggestionLetter.length();
            if (start >= 0 && end > 0) {
                if ((end - start) <= spanText.length()) {
                    spanText.setSpan(new RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.binding.tvTitle.setText(spanText);
                }
            }
        } else {
            SpannableString spanText = null;
            String originalString = "";
            int start = 0, end = 0;
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (listPlaylist.get(position).title_hebrew == null || listPlaylist.get(position).title_hebrew.equalsIgnoreCase("")) {
                    originalString = listPlaylist.get(position).title.toLowerCase();
                    spanText = new SpannableString(listPlaylist.get(position).title);
                } else {
                    originalString = listPlaylist.get(position).title_hebrew.toLowerCase();
                    spanText = new SpannableString(listPlaylist.get(position).title_hebrew);
                }
            } else {
                originalString = listPlaylist.get(position).title.toLowerCase();
                spanText = new SpannableString(listPlaylist.get(position).title);
            }
            start = originalString.indexOf(UserModel.getInstance().suggestionLetter.toLowerCase());
            if (start < 0) start = 0;
            end = start + UserModel.getInstance().suggestionLetter.length();
            if (start >= 0 && end > 0) {
                if ((end - start) <= spanText.length()) {
                    spanText.setSpan(new RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.binding.tvTitle.setText(spanText);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isFrom.equalsIgnoreCase("Album")) {
            if (listAlbum.size() < 4) {
                return listAlbum.size();
            } else {
                return 3;
            }
        } else if (isFrom.equalsIgnoreCase("Artist")) {
            if (listArtist.size() < 4) {
                return listArtist.size();
            } else {
                return 3;
            }
        } else {
            if (listPlaylist.size() < 4) {
                return listPlaylist.size();
            } else {
                return 3;
            }
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemSearchSuggestionBinding binding;

        public ViewHolder(View itemView, final ItemSearchSuggestionBinding binding) {
            super(itemView);
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final SearchSuggestionAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}

