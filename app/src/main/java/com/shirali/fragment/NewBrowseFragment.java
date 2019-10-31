package com.shirali.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shirali.R;
import com.shirali.activity.AlbumDetailActivity;
import com.shirali.activity.AllArtistActivity;
import com.shirali.activity.AllPlaylistActivity;
import com.shirali.activity.ArtistDetailActivity;
import com.shirali.activity.BaseActivity;
import com.shirali.activity.MainActivity;
import com.shirali.activity.NewReleasesActivity;
import com.shirali.activity.PlaylistActivity;
import com.shirali.activity.RadioStationsActivity;
import com.shirali.activity.RecommendedActivity;
import com.shirali.activity.SettingActivity;
import com.shirali.activity.ViewAllActivity;
import com.shirali.activity.WebViewActivity;
import com.shirali.activity.YourSubscriptionActivity;
import com.shirali.adapter.BannerPageAdapter;
import com.shirali.adapter.HomeAdapter;
import com.shirali.controls.StartSnap;
import com.shirali.databinding.FragmentNewBrowseBinding;
import com.shirali.interfaces.FreePaidUserCallBack;
import com.shirali.model.browse.Banner;
import com.shirali.model.browse.HomeData;
import com.shirali.model.browse.NewHomeList;
import com.shirali.model.mymusic.Album;
import com.shirali.model.share.ShareAlbum;
import com.shirali.model.share.SharePlaylist;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.songs.SongDetail;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewBrowseFragment extends Fragment implements View.OnClickListener {

    private FragmentNewBrowseBinding binding;
    private Context context;
    private BannerPageAdapter adapter;
    private int currentPage;
    private HomeAdapter rpAdapter, adapterAlbum;
    private ArrayList<HomeData> listRecentlyPlayed, listRecommendation, listNewRelease;
    private CustomLoaderDialog dialog;
    private ArrayList<RelatedArtist> relatedArtist;
    private HomeAdapter rAdapter;
    private ArrayList<Banner> listOfBanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_browse, container, false);
        context = getActivity();

        listRecentlyPlayed = new ArrayList<>();
        relatedArtist = new ArrayList<>();
        listRecentlyPlayed = new ArrayList<>();
        listRecommendation = new ArrayList<>();
        listNewRelease = new ArrayList<>();
        listOfBanner = new ArrayList<>();
        dialog = new CustomLoaderDialog(context);

        if(Utility.isConnectingToInternet(getActivity())){
            if (!((Activity) context).isFinishing()) {
                dialog.show();
            }
            getData();
        }

        SnapHelper snapHelper = new StartSnap();
        binding.lblSongViewAll.setOnClickListener(this);

        //display recently play audio
        snapHelper.attachToRecyclerView(binding.recycleviewGeners);
        rpAdapter = new HomeAdapter(getActivity(), listRecommendation);
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recycleviewGeners.setLayoutManager(LayoutManager);
        binding.recycleviewGeners.hasFixedSize();
        binding.recycleviewGeners.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(binding.recycleviewGeners, false);
        binding.recycleviewGeners.setAdapter(rpAdapter);
        rpAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (type.equalsIgnoreCase("song")) {
                    getSongDetail(context, listRecommendation.get(position).id);
                } else if (type.equalsIgnoreCase("album")) {
                    getAlbumById(listRecommendation.get(position).id);
                } else if (type.equalsIgnoreCase("artist")) {
                    getArtistDetail(listRecommendation.get(position).id);
                } else {
                    getPlaylistDetail(listRecommendation.get(position).id);
                }
            }
        });

        //recently played album
        snapHelper.attachToRecyclerView(binding.recyclerviewAlbum);
        adapterAlbum = new HomeAdapter(getActivity(), listNewRelease);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerviewAlbum.setLayoutManager(layoutManager);
        binding.recyclerviewAlbum.hasFixedSize();
        binding.recyclerviewAlbum.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(binding.recyclerviewAlbum, false);
        binding.recyclerviewAlbum.setAdapter(adapterAlbum);
        adapterAlbum.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (type.equalsIgnoreCase("song")) {
                    getSongDetail(context, listNewRelease.get(position).id);
                } else if (type.equalsIgnoreCase("album")) {
                    getAlbumById(listNewRelease.get(position).id);
                } else if (type.equalsIgnoreCase("artist")) {
                    getArtistDetail(listNewRelease.get(position).id);
                } else {
                    getPlaylistDetail(listNewRelease.get(position).id);
                }
            }
        });

        //display recently play audio
        snapHelper.attachToRecyclerView(binding.recycleviewSfu);
        rAdapter = new HomeAdapter(context, listRecentlyPlayed);
        RecyclerView.LayoutManager LayoutManagerr = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recycleviewSfu.setLayoutManager(LayoutManagerr);
        binding.recycleviewSfu.hasFixedSize();
        binding.recycleviewSfu.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(binding.recycleviewSfu, false);
        binding.recycleviewSfu.setAdapter(rAdapter);
        rAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (type.equalsIgnoreCase("song")) {
                    getSongDetail(context, listRecentlyPlayed.get(position).id);
                } else if (type.equalsIgnoreCase("album")) {
                    getAlbumById(listRecentlyPlayed.get(position).id);
                } else if (type.equalsIgnoreCase("artist")) {
                    getArtistDetail(listRecentlyPlayed.get(position).id);
                } else {
                    getPlaylistDetail(listRecentlyPlayed.get(position).id);
                }
            }
        });


        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utility.isConnectingToInternet(context)) {
                    if (!((Activity) context).isFinishing()) {
                        dialog.show();
                    }
                    getData();
                } /*else { // AKM
                    binding.lytAllData.setVisibility(View.GONE);
                }*/
                binding.swipeLyt.setRefreshing(false);
            }
        });

        binding.lytNewRelease.setOnClickListener(this);
        binding.lytRadio.setOnClickListener(this);
        binding.lytGenres.setOnClickListener(this);
        binding.lytRecommended.setOnClickListener(this);
        binding.lytPlaylist.setOnClickListener(this);
        binding.lytSelectedGenres.setOnClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        //AKM: Remove call everytime when screen open
        //getData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lytNewRelease:
                Intent i = new Intent(context, NewReleasesActivity.class);
                startActivity(i);
                break;
            case R.id.lytRadio:
                startActivity(new Intent(context, RadioStationsActivity.class));
                break;
            case R.id.lytGenres:
                Intent intent4 = new Intent(getActivity(), ViewAllActivity.class);
                intent4.putExtra("view", "genres");
                startActivity(intent4);
                break;
            case R.id.lytRecommended:
                startActivity(new Intent(context, RecommendedActivity.class));
                break;
            case R.id.lytPlaylist:
                startActivity(new Intent(context, AllPlaylistActivity.class));
                break;
            case R.id.lytSelectedGenres:
                startActivity(new Intent(context, AllArtistActivity.class));
                break;
            case R.id.lblSongViewAll:
                Intent intent2 = new Intent(getActivity(), ViewAllActivity.class);
                intent2.putExtra("view", "recent");
                startActivity(intent2);
                break;
        }
    }

    //Get user home data
    public void getData() {
        String id = Utility.getUserInfo(getActivity()).id;
        Call<NewHomeList> call = Constants.service.getNewHomeData(id);
        call.enqueue(new Callback<NewHomeList>() {
            @Override
            public void onResponse(Call<NewHomeList> call, Response<NewHomeList> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    NewHomeList listHome = response.body();
                    if (listHome != null && listHome.message != null) {
                        if (listHome.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (listHome.success) {
                                listRecentlyPlayed.clear();
                                listNewRelease.clear();
                                listRecommendation.clear();
                                listOfBanner.clear();
                                if (listHome.banner.size() > 0) {
                                    binding.lytBanner.setVisibility(View.VISIBLE);
                                    listOfBanner.addAll(listHome.banner);
                                    if (Utility.getUserInfo(context).getTrialTokan() == 1) {
                                        for (int i = 0; i < listOfBanner.size(); i++) {
                                            Banner banner = listOfBanner.get(i);
                                            if (banner.type.equalsIgnoreCase("subscription")) {
                                                listOfBanner.remove(i);
                                                break;
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                } else {
                                    binding.lytBanner.setVisibility(View.GONE);
                                }
                                if (listHome.recentlyPlayed.size() > 0) {
                                    binding.lytRecent.setVisibility(View.VISIBLE);
                                    listRecentlyPlayed.addAll(listHome.recentlyPlayed);
                                } else {
                                    binding.lytRecent.setVisibility(View.GONE);
                                }
                                rAdapter.notifyDataSetChanged();
                                if (listHome.recommendations.size() > 0) {
                                    binding.lytrecommended.setVisibility(View.VISIBLE);
                                    listRecommendation.addAll(listHome.recommendations);
                                } else {
                                    binding.lytrecommended.setVisibility(View.GONE);
                                }
                                rpAdapter.notifyDataSetChanged();
                                if (listHome.newReleases.size() > 0) {
                                    binding.lytNewReleasePage.setVisibility(View.VISIBLE);
                                    listNewRelease.addAll(listHome.newReleases);
                                } else {
                                    binding.lytNewReleasePage.setVisibility(View.GONE);
                                }
                                adapterAlbum.notifyDataSetChanged();
                                if (listHome.recentlyPlayed.size()>=20){
                                    binding.lblSongViewAll.setVisibility(View.VISIBLE);
                                }else {
                                    binding.lblSongViewAll.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<NewHomeList> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Utility.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    //For play share song
    private void getSongDetail(final Context context, String id) {
        Call<SongDetail> call = Constants.service.getSongDetail(id);
        call.enqueue(new Callback<SongDetail>() {
            @Override
            public void onResponse(Call<SongDetail> call, Response<SongDetail> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    SongDetail songDetail = response.body();
                    if (songDetail != null && songDetail.message != null) {
                        if (songDetail.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (songDetail.success) {
                                Album tempAlbum = new Album();
                                if (songDetail.songs.albums != null) {
                                    if (songDetail.songs.albums.size() > 0){
                                        tempAlbum = songDetail.songs.albums.get(0);
                                    }
                                }
                                if (songDetail.songs.isPremium || songDetail.songs.artist.isPremium || tempAlbum.isPremium) {
                                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        Constants.isSongPlay = true;
                                        UserModel.getInstance().isSingleSongPlay = true;
                                        ArrayList<Song> list = new ArrayList<>();
                                        list.add(songDetail.songs);
                                        Constants.SONGS_LIST = list;
                                        Constants.SONG_NUMBER = 0;
                                        UserModel.getInstance().tempSongList = list;
                                        UserModel.getInstance().listOfShuffleSong.clear();
                                        UserModel.getInstance().listOfShuffleSong.addAll(list);
                                        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                                        UserModel.getInstance().listOfActualSong.clear();
                                        UserModel.getInstance().listOfActualSong.addAll(list);
                                        UserModel.getInstance().freePaidUser(context, list, 0, new FreePaidUserCallBack() {
                                            @Override
                                            public void freePaidUser(boolean ifPaid) {
                                                if (ifPaid) {
                                                    ((MainActivity) getActivity()).playerVisibleManually();
                                                    Constants.SONG_NUMBER = 0;
                                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // Need to visible player from here also
                                                            /*AKM*/
                                                            //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                                            ((BaseActivity) getActivity()).callAdsFirstBeforeMusicPlay();
                                                        }
                                                    }, 100);
                                                }else {
                                                    Constants.SONG_NUMBER = 0;
                                                }
                                            }
                                        });
                                    } else {
                                        if (songDetail.songs.isPremium) {
                                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                                        } else if (songDetail.songs.artist.isPremium) {
                                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                        } else {
                                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                                        }
                                    }
                                } else {
                                    Constants.isSongPlay = true;
                                    UserModel.getInstance().isSingleSongPlay = true;
                                    ArrayList<Song> list = new ArrayList<>();
                                    list.add(songDetail.songs);
                                    Constants.SONGS_LIST = list;
                                    Constants.SONG_NUMBER = 0;
                                    UserModel.getInstance().tempSongList = list;
                                    UserModel.getInstance().listOfShuffleSong.clear();
                                    UserModel.getInstance().listOfShuffleSong.addAll(list);
                                    Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
                                    UserModel.getInstance().listOfActualSong.clear();
                                    UserModel.getInstance().listOfActualSong.addAll(list);
                                    UserModel.getInstance().freePaidUser(context, list, 0, new FreePaidUserCallBack() {
                                        @Override
                                        public void freePaidUser(boolean ifPaid) {
                                            if (ifPaid) {
                                                Constants.SONG_NUMBER = 0;
                                                ((MainActivity) getActivity()).playerVisibleManually();
                                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        /*AKM*/
                                                        //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                                        ((BaseActivity) getActivity()).callAdsFirstBeforeMusicPlay();
                                                    }
                                                }, 100);
                                            }else {
                                                Constants.SONG_NUMBER = 0;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SongDetail> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Get album detail with song for share album
    private void getAlbumById(String id) {
        Call<ShareAlbum> call = Constants.service.getAlbumById(id);
        call.enqueue(new Callback<ShareAlbum>() {
            @Override
            public void onResponse(Call<ShareAlbum> call, Response<ShareAlbum> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    ShareAlbum shareAlbum = response.body();
                    if (shareAlbum != null) {
                        if (shareAlbum.message.equalsIgnoreCase("Invalid device login.")) {
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (shareAlbum.success) {
                                if (shareAlbum.album.isPremium) {
                                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        try {
                                            UserModel.getInstance().album = shareAlbum.album;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(new Intent(context, AlbumDetailActivity.class));
                                    } else {
                                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                                    }
                                } else {
                                    try {
                                        UserModel.getInstance().album = shareAlbum.album;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(context, AlbumDetailActivity.class));
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ShareAlbum> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Get playlist detail with song for share playlist
    private void getPlaylistDetail(String id) {
        Call<SharePlaylist> call = Constants.service.getSongByPlaylistId(id);
        call.enqueue(new Callback<SharePlaylist>() {
            @Override
            public void onResponse(Call<SharePlaylist> call, Response<SharePlaylist> response) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    SharePlaylist playlist = response.body();
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (playlist.success) {
                            if (playlist.playlists != null/* && playlist.playlists.songs != null*/) {
                                try {
                                    UserModel.getInstance().shirali = playlist.playlists;
                                    startActivity(new Intent(context, PlaylistActivity.class));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SharePlaylist> call, Throwable t) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    private void getImages() {
        adapter = new BannerPageAdapter(context, listOfBanner);
        binding.banner.setAdapter(adapter);
        adapter.setOnItemClickListener(new BannerPageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                if (!((Activity) context).isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (type.equalsIgnoreCase("song")) {
                    getSongDetail(context, listOfBanner.get(position).recid);
                } else if (type.equalsIgnoreCase("album")) {
                    getAlbumById(listOfBanner.get(position).recid);
                } else if (type.equalsIgnoreCase("artist")) {
                    getArtistDetail(listOfBanner.get(position).recid);
                } else if (type.equalsIgnoreCase("playlist")) {
                    getPlaylistDetail(listOfBanner.get(position).recid);
                } else if (type.equalsIgnoreCase("subscription")) {
                    startActivity(new Intent(context, YourSubscriptionActivity.class));
                    dismissDialog();
                } else if (type.equalsIgnoreCase("radiostation")) {
                    startActivity(new Intent(context, RadioStationsActivity.class));
                    dismissDialog();
                } else if (type.equalsIgnoreCase("newreleases")) {
                    startActivity(new Intent(context, NewReleasesActivity.class));
                    dismissDialog();
                } else if (type.equalsIgnoreCase("artistsearch")) {
                    startActivity(new Intent(context, AllArtistActivity.class));
                    dismissDialog();
                } else if (type.equalsIgnoreCase("playlistdisplay")) {
                    startActivity(new Intent(context, AllPlaylistActivity.class));
                    dismissDialog();
                } else if (type.equalsIgnoreCase("weburl")) {
                    startActivity(new Intent(context, WebViewActivity.class).putExtra("banner_url", listOfBanner.get(position).recid));
                    dismissDialog();
                }else if (type.equalsIgnoreCase("settings")) {
                    startActivity(new Intent(context, SettingActivity.class));
                    dismissDialog();
                }
            }
        });

        final Handler handler = new Handler();

        final Runnable Update = new Runnable() {
            public void run() {
                if (binding.banner.getCurrentItem() + 1 == listOfBanner.size()) {
                    binding.banner.setCurrentItem(0, true);
                }else
                    binding.banner.setCurrentItem(binding.banner.getCurrentItem() + 1, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 1000, 8000);
        final float density = getResources().getDisplayMetrics().density;
        binding.indicator.setViewPager(binding.banner);
        binding.indicator.setRadius(3 * density);
        binding.indicator.setPageColor(0xFF888888);
        binding.indicator.setFillColor(0xFFFFFFFF);
        binding.indicator.setStrokeColor(0xFF888888);
    }

    public void dismissDialog(){
        if (!((Activity) context).isFinishing()) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    //Get Artist Detail
    public void getArtistDetail(String artist_id) {
        Call<ArtistInfo> call = Constants.service.getArtistNew(artist_id);
        call.enqueue(new Callback<ArtistInfo>() {
            @Override
            public void onResponse(Call<ArtistInfo> call, Response<ArtistInfo> response) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (response.isSuccessful()) {
                    ArtistInfo info = response.body();
                    try {
                        if (info.message.equalsIgnoreCase("Invalid device login.")) {
                            try {
                                Utility.openSessionOutDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (info.success) {
                                if (info.artist.isPremium) {
                                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", info.artist.id));
                                    } else {
                                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                                    }
                                } else {
                                    context.startActivity(new Intent(context, ArtistDetailActivity.class).putExtra("artist_id", info.artist.id));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistInfo> call, Throwable t) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                t.printStackTrace();
            }
        });
    }

}
