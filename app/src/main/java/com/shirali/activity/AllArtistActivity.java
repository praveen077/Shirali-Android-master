package com.shirali.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.shirali.R;
import com.shirali.adapter.ArtistAdapter;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.databinding.ActivityAllArtistBinding;
import com.shirali.interfaces.AddArtistOrNot;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.model.v2model.AllArtists;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllArtistActivity extends BaseActivity implements View.OnClickListener {


    long delay = 1000;
    long last_text_edit = 0;
    Handler handler = new Handler();
    private ActivityAllArtistBinding binding;
    private List<Artist> mDataArray;
    private Context context;
    private CustomLoaderDialog dialog;
    private ArtistAdapter adapter;
    private RecentPlayedAdapter rAdapter;
    private ArrayList<Album> listRecentAlbum;
    private ArrayList<Song> listPlayed;
    private ArrayList<RelatedArtist> relatedSongs;
    private CustomBottomTabView playerView;
    private ArrayList<String> listSong;
    StickyRecyclerHeadersDecoration headersDecor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_artist);
        context = this;
        setBottomView(context);
        dialog = new CustomLoaderDialog(context);

        if(Utility.isConnectingToInternet(context)) {
            if (!isFinishing()) {
                dialog.show();
            }
        }
        mDataArray = new ArrayList<>();
        listRecentAlbum = new ArrayList<>();
        listPlayed = new ArrayList<>();
        relatedSongs = new ArrayList<>();
        listSong = new ArrayList<>();

        binding.allArtist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtistAdapter(context, mDataArray);
        binding.allArtist.setAdapter(adapter);

        headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        binding.allArtist.addItemDecoration(headersDecor);
        StickyRecyclerHeadersTouchListener touchListener =
                new StickyRecyclerHeadersTouchListener(binding.allArtist, headersDecor);
        touchListener.setOnHeaderClickListener(
                new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(View header, int position, long headerId) {
                        int count = (int) header.getTag(R.id.img);
                        if (count > 20) {
                            startActivity(new Intent(context, ArtistViewAllActivity.class).putExtra("section_title", header.getTag(R.id.tvSongTag).toString()).putExtra("term", binding.lblSearch.getText().toString()));
                        }
                    }
                });
        binding.allArtist.addOnItemTouchListener(touchListener);

        binding.allArtist.setIndexTextSize(10);
        binding.allArtist.setIndexBarColor("#33334c");
        binding.allArtist.setIndexBarCornerRadius(0);
        binding.allArtist.setIndexBarTransparentValue((float) 0.4);
        binding.allArtist.setIndexbarMargin(0);
        binding.allArtist.setIndexbarWidth(40);
        binding.allArtist.setPreviewPadding(0);
        binding.allArtist.setIndexBarTextColor("#FFFFFF");

        binding.allArtist.setPreviewTextSize(60);
        binding.allArtist.setPreviewColor("#33334c");
        binding.allArtist.setPreviewTextColor("#FFFFFF");
        binding.allArtist.setPreviewTransparentValue(0.6f);

        binding.allArtist.setIndexBarVisibility(true);
        binding.allArtist.setIndexbarHighLateTextColor("#33334c");
        binding.allArtist.setIndexBarHighLateTextVisibility(true);

        adapter.setOnItemClickListener(new ArtistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String operationType, AddArtistOrNot check) {
                if (operationType.equalsIgnoreCase("plus")) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    addedToMyMusic(check, Utility.getUserInfo(context).myMusic, mDataArray.get(position).songsId, false);
                } else {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.show();
                            }
                        }
                        addedToMyMusic(check, Utility.getUserInfo(context).myMusic, mDataArray.get(position).songsId, true);
                    } else {
                        Utility.showSubscriptionAlert(context, getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
                    }
                }
            }
        });

        binding.imgBack.setOnClickListener(this);

        binding.relatedArtist.setHasFixedSize(true);
        binding.relatedArtist.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rAdapter = new RecentPlayedAdapter(this, listRecentAlbum, listPlayed, relatedSongs, true, "Artist");
        //AKM:NEXT_LOGIC
        rAdapter.isAlbum();
        binding.relatedArtist.setAdapter(rAdapter);

        binding.lblSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                adapter.filterList(binding.lblSearch.getText().toString().toLowerCase());
                try {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        binding.lblSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 2) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(suggestion_search, delay);
                } else {
                    if (binding.lblSearch.getText().toString().equalsIgnoreCase("")) {
                        getAllArtist("");
                        handler.removeCallbacks(suggestion_search);
                    }
                }
            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.lblSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    try {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.lblSearch.getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllArtist("");
        playerView.updateBottomView(UserModel.getInstance().openFragment);
        if (Constants.isSongPlay) {
            if (Constants.SONGS_LIST.size() > 0) {
                if(Constants.isHomeScreenPlayerVisible)
                    playerView.setPlayerData(Constants.SONGS_LIST);
            }
            playerView.updateSeekBar();
        } else {
            if (Constants.StationList.size() > 0) {
                if(Constants.isHomeScreenPlayerVisible)
                    playerView.setStationData(Constants.StationList);
            }
        }
        playerView.changePlayToPause();


        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                if (Constants.isChangeSong) {
                    if (!Utility.getBooleaPreferences(context,"ad_in_background")) {
                        Constants.isChangeSong = false;
                        com.shirali.controls.Controls.nextControl(context);
                        NewCampiagnActivity.isFromCampaign = false;
                    }
                } else {
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    NewCampiagnActivity.isFromCampaign = false;
                }
            } else {
                if (UserModel.getInstance().tempSongList.size() > 0) {
                    UserModel.getInstance().isPlaySongAfterAd = true;
                    Constants.SONGS_LIST = UserModel.getInstance().tempSongList;
                    Constants.SONG_NUMBER = 0;
                    Constants.song = Constants.SONGS_LIST.get(0).id;
                    /*AKM*/
                    //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    callAdsFirstBeforeMusicPlay();
                }
            }
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
    }

    //Set bottom nav bar and player view
    private void setBottomView(Context context) {
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.imgBack) {
            finish();
        }

    }

    // Getting artist list according user input
    public void getAllArtist(String term) {
        Call<AllArtists> call = Constants.service.getAllArtist(term);
        call.enqueue(new Callback<AllArtists>() {
            @Override
            public void onResponse(Call<AllArtists> call, Response<AllArtists> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    AllArtists artists = response.body();
                    if (artists.success) {
                        mDataArray.clear();
                        if (artists.artists.size() > 0) {
                            for (int i = 0; i < artists.artists.size(); i++) {
                                for (int j = 0; j < artists.artists.get(i).artist.size(); j++) {
                                    Artist artist = artists.artists.get(i).artist.get(j);
                                    artist.count = artists.artists.get(i).count;
                                    mDataArray.add(artist);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                @Override
                                public void onChanged() {
                                    headersDecor.invalidateHeaders();
                                }
                            });
                        }
                        if (artists.discover.size() > 0) {
                            relatedSongs.addAll(artists.discover);
                            rAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AllArtists> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    // find album song list which is not in my music
    private void addedToMyMusic(AddArtistOrNot view, ArrayList<String> myMusic, ArrayList<String> song_list, boolean isRemove) {
        if (isRemove) {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (myMusic.contains(song_list.get(i))) {
                    listSong.remove(song_list.get(i));
                }
            }
            addSong(listSong, view);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.artists_removed));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong, view);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.artist_added));
        }
    }

    // add selected song list into my music
    private void addSong(final ArrayList<String> list, final AddArtistOrNot check) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", list);
        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    UserModel user = response.body();
                    try {
                        if (user.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (user.success) {
                                UserModel.getInstance().getdata(context);
                                check.checkAdd();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    private Runnable suggestion_search = new Runnable() {
        public void run() {
            Runtime.getRuntime().gc();
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                getAllArtist(binding.lblSearch.getText().toString());
            }
        }
    };

    BroadcastReceiver finish_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                finish();
            }
        }
    };
}
