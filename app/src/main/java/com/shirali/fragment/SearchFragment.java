package com.shirali.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shirali.R;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.activity.PlaylistActivity;
import com.shirali.activity.SettingActivity;
import com.shirali.adapter.FilterGenresAdapter;
import com.shirali.adapter.SearchAdapter;
import com.shirali.adapter.SearchAdvancedAdapter;
import com.shirali.adapter.SearchSuggestionAdapter;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.adapter.TrendingAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.FragmentSearchBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.search.AdvanceSearch;
import com.shirali.model.search.Search;
import com.shirali.model.search.SearchItem;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchFragment extends Fragment {
    long delay = 1000;
    long last_text_edit = 0;
    Handler handler = new Handler();
    private FragmentSearchBinding binding;
    private Context mContext;
    private SearchAdapter adapter;
    private TrendingAdapter adapterTrending;
    private SongForUAdapter sAdapter;
    private ArrayList<String> listCelebs;
    private ArrayList<Song> listSong;
    private ArrayList<String> celebs;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ArrayList<String> listTrending;
    private int count;
    private CustomLoaderDialog dialog;
    private boolean isFirstTime = false;
    private boolean firstLoad = true;
    private int strSize;
    private ArrayList<Album> listSuggestionAlbum;
    private ArrayList<Artist> listSuggestionArtist;
    private ArrayList<Shirali> listSuggestionPlaylist;
    private SearchSuggestionAdapter albumAdapter, artistAdapter, playlistAdapter;
    private SearchAdvancedAdapter artistAdvancedAdapter, albumAdvancedAdapter;
    private String artist_id;
    private boolean isArtistPremium = false;
    private String current_search;
    private ArrayList<String> generes;
    private ArrayList<String> genresHebrew;
    private ArrayList<String> generesId;
    private FilterGenresAdapter fgAdapter;
    private GenresList genresList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        mContext = getActivity();
        Runtime.getRuntime().gc();
        listSong = new ArrayList<>();
        listCelebs = new ArrayList<>();
        celebs = new ArrayList<>();
        listTrending = new ArrayList<>();
        listSuggestionAlbum = new ArrayList<>();
        listSuggestionArtist = new ArrayList<>();
        listSuggestionPlaylist = new ArrayList<>();
        generes = new ArrayList<>();
        genresHebrew = new ArrayList<>();
        generesId = new ArrayList<>();

        dialog = new CustomLoaderDialog(mContext);
        if(Utility.isConnectingToInternet(getActivity())) {
            if (!((Activity) mContext).isFinishing()) {
                dialog.show();
            }
            getSearchData(" ");
        }
        isFirstTime = true;
        genresList = Utility.getGenres(getActivity());
        preferences = getActivity().getSharedPreferences("search", 0);
        editor = preferences.edit();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                if (genresList != null && genresList.genres != null) {
                    for (int i = 0; i < genresList.genres.size(); i++) {
                        generes.add(genresList.genres.get(i).title);
                        genresHebrew.add(genresList.genres.get(i).titleHebrew);
                        generesId.add(genresList.genres.get(i).id);
                    }
                }
            }
        });

        binding.settingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SettingActivity.class);
                startActivity(i);
            }
        });
        binding.lytClearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.lblRecentSearch.setVisibility(View.GONE);
                binding.lytClearList.setVisibility(View.GONE);
                binding.viewDivider.setVisibility(View.GONE);
                //editor.clear();
                celebs.clear();
                editor.putString("search_term", celebs.toString());
                editor.commit();
                adapter.delete();
                adapter.notifyDataSetChanged();
            }
        });

        binding.lblSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.lblCancel.setVisibility(View.VISIBLE);
                return false;
            }
        });


        binding.lblCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.lblCancel.setVisibility(View.GONE);
                binding.lblNoSearch.setVisibility(View.GONE);
                binding.lytSearchFilter.setVisibility(View.GONE);
                binding.lytSearchResult.setVisibility(View.GONE);
                binding.lytSearchTerm.setVisibility(View.VISIBLE);
                binding.lblSearch.setText("");
                try {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                funRecentSearch();
            }
        });

        adapter = new SearchAdapter(getActivity(), listCelebs);
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.recycleviewWeekly.setLayoutManager(LayoutManager);
        binding.recycleviewWeekly.hasFixedSize();
        binding.recycleviewWeekly.setAdapter(adapter);
        adapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String value, int position) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                handler.removeCallbacks(suggestion_search);
                getAdvancedResult(value);
                celebs.add(value.trim());
                editor.putString("search_term", celebs.toString());
                editor.commit();
                binding.lytSearchTerm.setVisibility(View.GONE);
                binding.lytSearchResult.setVisibility(View.VISIBLE);
                binding.lytSearchFilter.setVisibility(View.GONE);
            }
        });

        adapterTrending = new TrendingAdapter(getActivity(), listTrending);
        final RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.recycleTrending.setLayoutManager(manager);
        binding.recycleTrending.hasFixedSize();
        binding.recycleTrending.setAdapter(adapterTrending);
        adapterTrending.setOnItemClickListener(new TrendingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String value, int position) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (!listTrending.get(position).trim().equalsIgnoreCase("")) {
                    celebs.add(listTrending.get(position).replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
                handler.removeCallbacks(suggestion_search);
                getAdvancedResult(value);
                binding.lytSearchTerm.setVisibility(View.GONE);
                binding.lytSearchResult.setVisibility(View.VISIBLE);
                binding.lytSearchFilter.setVisibility(View.GONE);
            }
        });

        sAdapter = new SongForUAdapter(getActivity(), listSong, "search", Utility.getUserInfo(mContext).myMusic);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.recycleSearch.setLayoutManager(layoutManager);
        binding.recycleSearch.smoothScrollToPosition(listSong.size());
        binding.recycleSearch.hasFixedSize();
        binding.recycleSearch.setAdapter(sAdapter);
        sAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                binding.lytSearchFilter.setVisibility(View.GONE);
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                }
                Constants.SONGS_LIST = listSong;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());

                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {

            }
        });

        if (preferences.contains("search_term")) {
            ArrayList<String> celebList = new ArrayList<>(Arrays.asList(preferences.getString("search_term", "").split(",")));
            for (int i = 0; i < celebList.size(); i++) {
                celebs.add(celebList.get(i).trim().replace("[", "").replace("]", ""));
            }
            count = celebs.size();
        } else {
            count = 0;
            binding.lblRecentSearch.setVisibility(View.GONE);
            binding.lytClearList.setVisibility(View.GONE);
            binding.viewDivider.setVisibility(View.GONE);
        }


        //Adapter for suggestion
        albumAdapter = new SearchSuggestionAdapter(getActivity(), listSuggestionAlbum, listSuggestionArtist, listSuggestionPlaylist, "Album", UserModel.getInstance().suggestionLetter);
        binding.listAlbum.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.listAlbum.hasFixedSize();
        binding.listAlbum.setAdapter(albumAdapter);
        albumAdapter.setOnItemClickListener(new SearchSuggestionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int value) {
                if (listSuggestionAlbum.size() > 0 && listSuggestionAlbum != null) {
                    UserModel.getInstance().tempAlbum = listSuggestionAlbum.get(value);
                    if (listSuggestionAlbum.get(value).isPremium) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            try {
                                UserModel.getInstance().artist_id = listSuggestionAlbum.get(value).artist;
                                UserModel.getInstance().album = listSuggestionAlbum.get(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                            mContext.startActivity(intent);
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        try {
                            UserModel.getInstance().artist_id = listSuggestionAlbum.get(value).artist;
                            UserModel.getInstance().album = listSuggestionAlbum.get(value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                        mContext.startActivity(intent);
                    }
                }
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        artistAdapter = new SearchSuggestionAdapter(getActivity(), listSuggestionAlbum, listSuggestionArtist, listSuggestionPlaylist, "Artist", UserModel.getInstance().suggestionLetter);
        binding.listArtist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.listArtist.hasFixedSize();
        binding.listArtist.setAdapter(artistAdapter);
        artistAdapter.setOnItemClickListener(new SearchSuggestionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int value) {
                if (listSuggestionArtist != null && listSuggestionArtist.size() > 0) {
                    if (listSuggestionArtist.get(value).isPremium) {
                        if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                            intent.putExtra("artist_id", listSuggestionArtist.get(value).id);
                            mContext.startActivity(intent);
                        } else {
                            Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        }
                    } else {
                        Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                        intent.putExtra("artist_id", listSuggestionArtist.get(value).id);
                        mContext.startActivity(intent);
                    }
                }
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        playlistAdapter = new SearchSuggestionAdapter(getActivity(), listSuggestionAlbum, listSuggestionArtist, listSuggestionPlaylist, "Playlist", UserModel.getInstance().suggestionLetter);
        binding.listPlaylist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.listPlaylist.hasFixedSize();
        binding.listPlaylist.setAdapter(playlistAdapter);
        playlistAdapter.setOnItemClickListener(new SearchSuggestionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listSuggestionPlaylist.size() > 0) {
                    UserModel.getInstance().shirali = listSuggestionPlaylist.get(position);
                }
                startActivity(new Intent(mContext, PlaylistActivity.class));
            }
        });

        binding.lblSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Runtime.getRuntime().gc();
                if (strSize != binding.lblSearch.getText().toString().trim().length()) {
                    firstLoad = true;
                }
                binding.lytSearchFilter.setVisibility(View.GONE);
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
                sAdapter.delete();
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                        try {
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        handler.removeCallbacks(suggestion_search);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getAdvancedResult(binding.lblSearch.getText().toString());
                            }
                        });
                        binding.lytSearchTerm.setVisibility(View.GONE);
                        binding.lytSearchResult.setVisibility(View.GONE);
                        binding.lblNoSearch.setVisibility(View.GONE);
                        sAdapter.notifyDataSetChanged();
                        if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                            celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                            editor.putString("search_term", celebs.toString());
                            editor.commit();
                        }
                    }
                    return true;
                } else {
                    if (firstLoad) {
                        strSize = binding.lblSearch.getText().toString().trim().length();
                        firstLoad = false;
                        Utility.showAlert(mContext, getResources().getString(R.string.three_character));
                    }
                }
                return false;
            }
        });


        binding.artistSearch.setHasFixedSize(true);
        binding.artistSearch.setLayoutManager(new LinearLayoutManager(mContext));
        artistAdvancedAdapter = new SearchAdvancedAdapter(mContext, listSuggestionArtist, listSuggestionAlbum, "MyArtist");
        binding.artistSearch.setAdapter(artistAdvancedAdapter);
        artistAdvancedAdapter.setOnItemClickListener(new SearchAdvancedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                binding.lytSearchFilter.setVisibility(View.GONE);
            }
        });

        binding.albumSearch.setHasFixedSize(true);
        binding.albumSearch.setLayoutManager(new LinearLayoutManager(mContext));
        albumAdvancedAdapter = new SearchAdvancedAdapter(mContext, listSuggestionArtist, listSuggestionAlbum, "MyAlbum");
        binding.albumSearch.setAdapter(albumAdvancedAdapter);
        albumAdvancedAdapter.setOnItemClickListener(new SearchAdvancedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                binding.lytSearchFilter.setVisibility(View.GONE);
                try {
                    UserModel.getInstance().album = listSuggestionAlbum.get(position);
                    UserModel.getInstance().artist_id = listSuggestionAlbum.get(position).artist;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                mContext.startActivity(intent);
            }
        });

        binding.lblSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() <= 1) {
                    binding.lytSearchResult.setVisibility(View.GONE);
                    binding.lytSearchFilter.setVisibility(View.GONE);
                    binding.lytSearchTerm.setVisibility(View.VISIBLE);
                    UserModel.getInstance().suggestionLetter = "";
                    handler.removeCallbacks(suggestion_search);
                } else {
                    UserModel.getInstance().suggestionLetter = "";
                    UserModel.getInstance().suggestionLetter = charSequence.toString();
                    handler.removeCallbacks(suggestion_search);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.lblNoSearch.setVisibility(View.GONE);
                if (editable.length() >= 2) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(suggestion_search, delay);
                }
            }
        });

        funRecentSearch();

        if (preferences.contains("trending")) {
            ArrayList<String> celebList = new ArrayList<>(Arrays.asList(preferences.getString("trending", "").split(",")));
            for (int i = celebList.size(); i > 0; i--) {
                listTrending.add(celebList.get(i - 1).replace("[", "").replace("]", ""));
            }
            adapterTrending.notifyDataSetChanged();
        }

        binding.lblSeeAllResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                try {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.removeCallbacks(suggestion_search);
                getAdvancedResult(binding.lblSearch.getText().toString());
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        binding.lytArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    mContext.startActivity(new Intent(mContext, ArtistDetailActivity.class).putExtra("artist_id", artist_id));
                } else {
                    if (isArtistPremium) {
                        Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    } else {
                        mContext.startActivity(new Intent(mContext, ArtistDetailActivity.class).putExtra("artist_id", artist_id));
                    }
                }
            }
        });

        binding.lblAllSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllSongs(current_search);
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        binding.lblAllAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllAlbums(current_search);
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        binding.lblAllArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllArtist(current_search);
                if (!binding.lblSearch.getText().toString().trim().equalsIgnoreCase("")) {
                    celebs.add(binding.lblSearch.getText().toString().replace("[", "").replace("]", "").trim());
                    editor.putString("search_term", celebs.toString());
                    editor.commit();
                }
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                binding.lytHeader.setVisibility(View.GONE);
                binding.lytSearchHeader.setVisibility(View.VISIBLE);
                binding.lblSearch.setText(current_search);
                binding.lblSearch.setSelection(current_search.length());
                handler.removeCallbacks(suggestion_search);
                getAdvancedResult(current_search);
            }
        });

        binding.lytAllMoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.filterSheet.getVisibility() == View.VISIBLE) {
                    closeSheet();
                } else {
                    showTable();
                    binding.filterSheet.setVisibility(View.VISIBLE);
                    binding.lytSearchResult.setNestedScrollingEnabled(false);
                    Animation slide_down = AnimationUtils.loadAnimation(mContext, R.anim.animate_slide_down);
                    binding.filterSheet.startAnimation(slide_down);
                }
            }
        });

        binding.tvDsimiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSheet();
            }
        });

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        return binding.getRoot();
    }

    private void funRecentSearch() {
        listCelebs.clear();
        if (preferences.contains("search_term")) {
            ArrayList<String> celebList = new ArrayList<>(Arrays.asList(preferences.getString("search_term", "").split(",")));
            for (int i = celebList.size(); i > 0; i--) {
                String data = celebList.get(i-1).replace("[", "").replace("]", "").trim();
                if (!data.equalsIgnoreCase("")) {
                    if (!listCelebs.contains(data))
                        listCelebs.add(data);
                }
            }
            if (listCelebs.size() > 0) {
                binding.lblRecentSearch.setVisibility(View.VISIBLE);
                binding.lytClearList.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    public void updateIcon() {
        if (binding.recycleSearch != null && binding.recycleSearch.getAdapter() != null)
            binding.recycleSearch.getAdapter().notifyDataSetChanged();
    }

    //Get search by tag
    public void getSearchData(final String tag) {
        Call<Search> call = Constants.service.getSearchItem(tag);
        call.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(Call<Search> call, Response<Search> response) {
                if (response.isSuccessful()) {
                    if (!((Activity) mContext).isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    listTrending.clear();
                    Search search = response.body();
                    if (search != null) {
                        try {
                            if (search.message.equalsIgnoreCase("Invalid device login.")) {
                                try {
                                    Utility.openSessionOutDialog(getActivity());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (search.success) {
                                    for (int i = 0; i < search.tranding.size(); i++) {
                                        listTrending.add(search.tranding.get(i).id);
                                    }
                                    editor.putString("trending", listTrending.toString());
                                    editor.commit();
                                    adapterTrending.notifyDataSetChanged();
                                    if (!tag.equalsIgnoreCase(" ")) {
                                        if (search.songs.size() > 0) {
                                            listSong.addAll(search.songs);
                                            sAdapter.notifyDataSetChanged();
                                        } else {
                                            binding.lytSearchTerm.setVisibility(View.GONE);
                                            binding.lytSearchResult.setVisibility(View.GONE);
                                            binding.lblNoSearch.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        if (isFirstTime) {
                                            isFirstTime = false;
                                            binding.lytSearchTerm.setVisibility(View.VISIBLE);
                                            binding.lytSearchResult.setVisibility(View.GONE);
                                            binding.lblNoSearch.setVisibility(View.GONE);
                                        } else {
                                            binding.lytSearchTerm.setVisibility(View.GONE);
                                            binding.lytSearchResult.setVisibility(View.GONE);
                                            binding.lblNoSearch.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!((Activity) mContext).isFinishing()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        binding.lytSearchTerm.setVisibility(View.VISIBLE);
                        binding.lytSearchResult.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onFailure(Call<Search> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    //Get response instant on user type (Real time)
    private void getRealtimeSearchResult(final String data) {
        Call<SearchItem> call = Constants.service.searchResult(data);
        call.enqueue(new Callback<SearchItem>() {
            @Override
            public void onResponse(Call<SearchItem> call, Response<SearchItem> response) {
                if (response.isSuccessful()) {
                    SearchItem searchItem = response.body();
                    if (searchItem.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(mContext);
                    } else {
                        try {
                            listSuggestionAlbum.clear();
                            listSuggestionArtist.clear();
                            listSuggestionPlaylist.clear();
                            binding.lytSearchTerm.setVisibility(View.GONE);
                            binding.lytSearchResult.setVisibility(View.GONE);
                            binding.lytSearchFilter.setVisibility(View.VISIBLE);
                            binding.lblNoSearch.setVisibility(View.GONE);
                            if (searchItem.albums == null && searchItem.artists == null && searchItem.playlists == null) {
                                binding.lytSearchTerm.setVisibility(View.VISIBLE);
                                binding.lytSearchResult.setVisibility(View.GONE);
                                binding.lytSearchFilter.setVisibility(View.GONE);
                                binding.lblNoSearch.setVisibility(View.GONE);
                            } else {
                                if (searchItem.albums.size() > 0) {
                                    binding.lblAlbum.setVisibility(View.VISIBLE);
                                    binding.listAlbum.setVisibility(View.VISIBLE);
                                    binding.viewAlbum.setVisibility(View.VISIBLE);
                                    listSuggestionAlbum.addAll(searchItem.albums);
                                    albumAdapter.notifyDataSetChanged();
                                } else {
                                    binding.viewAlbum.setVisibility(View.GONE);
                                    binding.lblAlbum.setVisibility(View.GONE);
                                    binding.listAlbum.setVisibility(View.GONE);
                                }
                                if (searchItem.artists.size() > 0) {
                                    binding.lblArtist.setVisibility(View.VISIBLE);
                                    binding.listArtist.setVisibility(View.VISIBLE);
                                    binding.viewArtist.setVisibility(View.VISIBLE);
                                    listSuggestionArtist.addAll(searchItem.artists);

                                    artistAdapter.notifyDataSetChanged();
                                } else {
                                    binding.viewArtist.setVisibility(View.GONE);
                                    binding.lblArtist.setVisibility(View.GONE);
                                    binding.listArtist.setVisibility(View.GONE);
                                }
                                if (searchItem.playlists.size() > 0) {
                                    binding.viewPlaylist.setVisibility(View.VISIBLE);
                                    binding.lblPlaylist.setVisibility(View.VISIBLE);
                                    binding.listPlaylist.setVisibility(View.VISIBLE);
                                    listSuggestionPlaylist.addAll(searchItem.playlists);
                                    playlistAdapter.notifyDataSetChanged();
                                } else {
                                    binding.viewPlaylist.setVisibility(View.GONE);
                                    binding.lblPlaylist.setVisibility(View.GONE);
                                    binding.listPlaylist.setVisibility(View.GONE);
                                }
                                if (searchItem.playlists.size() <= 0 && searchItem.artists.size() <= 0 && searchItem.albums.size() <= 0) {
                                    binding.lytSearchFilter.setVisibility(View.GONE);
                                    binding.lytSearchResult.setVisibility(View.GONE);
                                    binding.lytSearchTerm.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SearchItem> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Get all Result on search
    private void getAdvancedResult(final String tag) {
        Call<AdvanceSearch> call = Constants.service.searchAdavnceResult(tag);
        call.enqueue(new Callback<AdvanceSearch>() {
            @Override
            public void onResponse(Call<AdvanceSearch> call, Response<AdvanceSearch> response) {
                if (response.isSuccessful()) {
                    try {
                        AdvanceSearch search = response.body();
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (search.success) {
                                current_search = tag;
                                if (!((Activity) mContext).isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                listSuggestionArtist.clear();
                                listSuggestionAlbum.clear();
                                listSong.clear();
                                binding.lytSearchFilter.setVisibility(View.GONE);
                                binding.lytSearchTerm.setVisibility(View.GONE);
                                binding.lytAllMoods.setVisibility(View.GONE);
                                binding.lytSearchResult.setVisibility(View.VISIBLE);
                                if (search.success) {
                                    if (search.topArtist.size() <= 0 && search.songs.size() <= 0 && search.artists.size() <= 0 && search.albums.size() <= 0) {
                                        binding.lytSearchResult.setVisibility(View.GONE);
                                        binding.lblNoSearch.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.lytSearchResult.setVisibility(View.VISIBLE);
                                        binding.lblNoSearch.setVisibility(View.GONE);
                                        if (search.topArtist.size() > 0) {
                                            binding.lblTopArtist.setVisibility(View.VISIBLE);
                                            binding.lytArtist.setVisibility(View.VISIBLE);
                                            artist_id = search.topArtist.get(0).id;
                                            isArtistPremium = search.topArtist.get(0).isPremium;
                                            Glide.with(mContext).load(search.topArtist.get(0).avatar).listener(new RequestListener<String, GlideDrawable>() {
                                                @Override
                                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                    binding.loaderIconSec.setVisibility(View.GONE);
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                    binding.loaderIconSec.setVisibility(View.GONE);
                                                    return false;
                                                }
                                            }).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).into(binding.ivCircularArtist);
                                            if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                                                if (search.topArtist.get(0).nameHebrew == null || search.topArtist.get(0).nameHebrew.equalsIgnoreCase("")) {
                                                    binding.tvMusicName.setText(search.topArtist.get(0).name);
                                                } else {
                                                    binding.tvMusicName.setText(search.topArtist.get(0).nameHebrew);
                                                }
                                            } else {
                                                binding.tvMusicName.setText(search.topArtist.get(0).name);
                                            }
                                        } else {
                                            binding.lblTopArtist.setVisibility(View.GONE);
                                            binding.lytArtist.setVisibility(View.GONE);
                                        }
                                        if (search.songs.size() > 0) {
                                            binding.lblSongName.setVisibility(View.VISIBLE);
                                            binding.recycleSearch.setVisibility(View.VISIBLE);
                                            listSong.addAll(search.songs);
                                            sAdapter.notifyDataSetChanged();
                                        } else {
                                            binding.lblSongName.setVisibility(View.GONE);
                                            binding.recycleSearch.setVisibility(View.GONE);
                                        }
                                        if (search.artists.size() > 0) {
                                            binding.lblArtistName.setVisibility(View.VISIBLE);
                                            binding.artistSearch.setVisibility(View.VISIBLE);
                                            listSuggestionArtist.addAll(search.artists);
                                            artistAdvancedAdapter.notifyDataSetChanged();
                                        } else {
                                            binding.lblArtistName.setVisibility(View.GONE);
                                            binding.artistSearch.setVisibility(View.GONE);
                                        }
                                        if (search.albums.size() > 0) {
                                            binding.lblAlbumName.setVisibility(View.VISIBLE);
                                            binding.albumSearch.setVisibility(View.VISIBLE);
                                            listSuggestionAlbum.addAll(search.albums);
                                            albumAdvancedAdapter.notifyDataSetChanged();
                                        } else {
                                            binding.lblAlbumName.setVisibility(View.GONE);
                                            binding.albumSearch.setVisibility(View.GONE);
                                        }
                                        if (search.songsCount > 4) {
                                            binding.lblAllSong.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.lblAllSong.setVisibility(View.GONE);
                                        }
                                        if (search.albumsCount > 3) {
                                            binding.lblAllAlbum.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.lblAllAlbum.setVisibility(View.GONE);
                                        }
                                        if (search.artistsCount > 3) {
                                            binding.lblAllArtist.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.lblAllArtist.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AdvanceSearch> call, Throwable t) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // for update the list
        UserModel.getInstance().getMyMusic(mContext, new GetMyMusicListCallback() {
            @Override
            public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                if (sAdapter !=null) {
                    sAdapter.updateMyMusicList(myMusic);
                    sAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //Get All songs on click view all song
    private void getAllSongs(final String tag) {
        Call<AdvanceSearch> call = Constants.service.searchAllSong(tag);
        call.enqueue(new Callback<AdvanceSearch>() {
            @Override
            public void onResponse(Call<AdvanceSearch> call, Response<AdvanceSearch> response) {
                if (response.isSuccessful()) {
                    try {
                        listSong.clear();
                        AdvanceSearch search = response.body();
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (search.success) {
                                binding.lytSearchHeader.setVisibility(View.GONE);
                                binding.lytHeader.setVisibility(View.VISIBLE);
                                binding.lblHeader.setText(tag + " " + mContext.getResources().getString(R.string.in_songs));
                                binding.lblTopArtist.setVisibility(View.GONE);
                                binding.lytArtist.setVisibility(View.GONE);
                                binding.lblArtistName.setVisibility(View.GONE);
                                binding.artistSearch.setVisibility(View.GONE);
                                binding.lblAllArtist.setVisibility(View.GONE);
                                binding.lblAlbumName.setVisibility(View.GONE);
                                binding.albumSearch.setVisibility(View.GONE);
                                binding.lblAllAlbum.setVisibility(View.GONE);
                                binding.lytAllMoods.setVisibility(View.VISIBLE);
                                binding.lblAllSong.setVisibility(View.GONE);
                                if (search.songs.size() > 0) {
                                    listSong.addAll(search.songs);
                                }
                                sAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AdvanceSearch> call, Throwable t) {

            }
        });
    }

    //Get All albums on click view all album
    private void getAllAlbums(final String tag) {
        Call<AdvanceSearch> call = Constants.service.searchAllAlbum(tag);
        call.enqueue(new Callback<AdvanceSearch>() {
            @Override
            public void onResponse(Call<AdvanceSearch> call, Response<AdvanceSearch> response) {
                if (response.isSuccessful()) {
                    try {
                        listSuggestionAlbum.clear();
                        AdvanceSearch search = response.body();
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (search.success) {
                                binding.lytSearchHeader.setVisibility(View.GONE);
                                binding.lytHeader.setVisibility(View.VISIBLE);
                                binding.lblHeader.setText(tag + " " + mContext.getResources().getString(R.string.in_albums));
                                binding.lblTopArtist.setVisibility(View.GONE);
                                binding.lytArtist.setVisibility(View.GONE);
                                binding.lblArtistName.setVisibility(View.GONE);
                                binding.artistSearch.setVisibility(View.GONE);
                                binding.lblSongName.setVisibility(View.GONE);
                                binding.lblAllArtist.setVisibility(View.GONE);
                                binding.lblAllAlbum.setVisibility(View.GONE);
                                binding.lytAllMoods.setVisibility(View.GONE);
                                binding.recycleSearch.setVisibility(View.GONE);
                                binding.lblAllSong.setVisibility(View.GONE);
                                if (search.albums.size() > 0) {
                                    listSuggestionAlbum.addAll(search.albums);
                                }
                                albumAdvancedAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AdvanceSearch> call, Throwable t) {

            }
        });
    }

    //Get all artists on click view all artist
    private void getAllArtist(final String tag) {
        Call<AdvanceSearch> call = Constants.service.searchAllArtists(tag);
        call.enqueue(new Callback<AdvanceSearch>() {
            @Override
            public void onResponse(Call<AdvanceSearch> call, Response<AdvanceSearch> response) {
                if (response.isSuccessful()) {
                    try {
                        listSuggestionArtist.clear();
                        AdvanceSearch search = response.body();
                        if (search.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(mContext);
                        } else {
                            if (search.success) {
                                binding.lytSearchHeader.setVisibility(View.GONE);
                                binding.lytHeader.setVisibility(View.VISIBLE);
                                binding.lblHeader.setText(tag + " " + mContext.getResources().getString(R.string.in_artists));
                                binding.lblSongName.setVisibility(View.GONE);
                                binding.lblTopArtist.setVisibility(View.GONE);
                                binding.lytArtist.setVisibility(View.GONE);
                                binding.lblAllArtist.setVisibility(View.GONE);
                                binding.lblAlbumName.setVisibility(View.GONE);
                                binding.albumSearch.setVisibility(View.GONE);
                                binding.recycleSearch.setVisibility(View.GONE);
                                binding.lblAllAlbum.setVisibility(View.GONE);
                                binding.lytAllMoods.setVisibility(View.GONE);
                                binding.lblAllSong.setVisibility(View.GONE);
                                if (search.artists.size() > 0) {
                                    listSuggestionArtist.addAll(search.artists);
                                }
                                artistAdvancedAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AdvanceSearch> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    //Show filter sheet
    private void showTable() {
        if (!generes.contains("All Genres")) {
            generes.add(0, "All Genres");
        }
        if (!genresHebrew.contains("כל הזאנרים")) {
            genresHebrew.add(0, "כל הזאנרים");
        }
        fgAdapter = new FilterGenresAdapter(getActivity(), generes, genresHebrew);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.listFilterGenres.setLayoutManager(manager);
        binding.listFilterGenres.hasFixedSize();
        binding.listFilterGenres.setAdapter(fgAdapter);
        fgAdapter.setOnItemClickListener(new FilterGenresAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String value, int position) {
                binding.lblTitlee.setText(value);
                if (Utility.getStringPreferences(mContext, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (value.equalsIgnoreCase(mContext.getResources().getString(R.string.all_genres))) {
                        sAdapter.filterList("all");
                    } else {
                        sAdapter.filterList(generes.get(position));
                    }
                } else {
                    if (value.equalsIgnoreCase("All Genres")) {
                        sAdapter.filterList("all");
                    } else {
                        sAdapter.filterList(generes.get(position));
                    }
                }
                closeSheet();
            }
        });
    }

    //Hide filter sheet
    private void closeSheet() {
        Animation slide_up = AnimationUtils.loadAnimation(mContext, R.anim.animate_slide_up);
        binding.filterSheet.startAnimation(slide_up);
        binding.lytAllMoods.setEnabled(true);
        binding.lytSearchResult.setNestedScrollingEnabled(true);
        binding.filterSheet.setVisibility(View.GONE);
        binding.table.removeAllViews();
    }

    private Runnable suggestion_search = new Runnable() {
        public void run() {
            Runtime.getRuntime().gc();
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                getRealtimeSearchResult(UserModel.getInstance().suggestionLetter);
            }
        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
        }
    };
}