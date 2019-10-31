package com.shirali.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.activity.BaseActivity;
import com.shirali.adapter.RecentAllAdapter;
import com.shirali.adapter.RecentPlayedAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.FragmentRecentBinding;
import com.shirali.model.HomeCellModel;
import com.shirali.model.mymusic.Album;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.recent.Recent;
import com.shirali.model.songs.RelatedArtist;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.controls.StartSnap;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecentFragment extends Fragment {

    private FragmentRecentBinding binding;
    private Context mContext;
    private ArrayList<HomeCellModel> list;
    private RecentPlayedAdapter Adapter;
    private RecentAllAdapter mAdapter;
    private ArrayList<Album> listRecentAlbum;

    private ArrayList<RelatedArtist> relatedArtist;
    private ArrayList<Song> listRecentlyPlayed;
    private CustomLoaderDialog dialog;
    private int seconds;
    private MixpanelAPI mixpanelAPI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent, container, false);
        mContext = getActivity();
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        list = new ArrayList<>();
        listRecentAlbum = new ArrayList<>();
        relatedArtist = new ArrayList<>();
        listRecentlyPlayed = new ArrayList<>();
        dialog = new CustomLoaderDialog(mContext);
        UserModel.getInstance().openFragment = "RECENT";

        if (Utility.isConnectingToInternet(getActivity())) {
            getData();
            getMyMusicData();

            if (!((Activity) (mContext)).isFinishing()) {
                dialog.show();
            }

        }



        SnapHelper snapHelper = new StartSnap();

        snapHelper.attachToRecyclerView(binding.recycleviewRecent);
        Adapter = new RecentPlayedAdapter(getActivity(), listRecentAlbum, listRecentlyPlayed, relatedArtist, false, "played");
        //AKM:NEXT_LOGIC
        Adapter.isAlbum();
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.recycleviewRecent.setLayoutManager(LayoutManager);
        binding.recycleviewRecent.hasFixedSize();
        binding.recycleviewRecent.setAdapter(Adapter);
        Adapter.setOnItemClickListener(new RecentPlayedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (Constants.isPlay) {
                    Controls.pauseControl(mContext);
                }
                Constants.SONGS_LIST = listRecentlyPlayed;
                Constants.SONG_NUMBER = position;
                /*AKM*/
                //Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                ((BaseActivity) getActivity()).callAdsFirstBeforeMusicPlay();
            }
        });


        mAdapter = new RecentAllAdapter(list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.recycleviewWeekly.setLayoutManager(mLayoutManager);
        binding.recycleviewWeekly.hasFixedSize();
        binding.recycleviewWeekly.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecentAllAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
            }
        });
        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utility.isConnectingToInternet(getActivity())) {
                    if (!((Activity) (mContext)).isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    getData();
                    getMyMusicData();
                }
                binding.swipeLyt.setRefreshing(false);
            }
        });

        binding.layoutAutoPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixpanelAPI.track("Tap-on-Recently_Added");
                Intent intent = new Intent("open_my_music_tab");
                intent.putExtra("message", "open_my_music_tab");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        });

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("open_browse"));
            }
        });
        return binding.getRoot();

    }

    //Get user recent data
    public void getData() {
        Call<Recent> call = Constants.service.getRecentNew(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<Recent>() {
            @Override
            public void onResponse(Call<Recent> call, Response<Recent> response) {
                if (response.isSuccessful()) {
                    listRecentlyPlayed.clear();
                    if (!((Activity) (mContext)).isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    Recent home = response.body();
                    try {
                        if (home != null) {
                            if (home.message.equalsIgnoreCase("Invalid device login.")) {
                                try {
                                    Utility.openSessionOutDialog(getActivity());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (home.success) {
                                    if (home.recentlyPlayed.recentlyPlayed.size() > 0) {
                                        binding.lytRecent.setVisibility(View.VISIBLE);
                                        binding.lytEmpty.setVisibility(View.GONE);
                                        listRecentlyPlayed.addAll(home.recentlyPlayed.recentlyPlayed);
                                    } else {
                                        binding.lytRecent.setVisibility(View.GONE);
                                        binding.lytEmpty.setVisibility(View.VISIBLE);
                                    }
                                    Adapter.notifyDataSetChanged();

                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Recent> call, Throwable t) {
                if (!(((Activity) mContext)).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Utility.showAlert(mContext, getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    //Calculate my music song for "in my music"
    public void getMyMusicData() {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                if (response.isSuccessful()) {
                    MyMusic myMusic = response.body();
                    seconds = 0;
                    if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(getActivity());
                    } else {
                        if (myMusic.success) {
                            if (myMusic.myMusicContain != null) {
                                if (myMusic.myMusicContain.song.size() > 0) {
                                    binding.lblInMyMusic.setVisibility(View.VISIBLE);
                                    binding.layoutAutoPlaylist.setVisibility(View.VISIBLE);
                                    binding.view.setVisibility(View.VISIBLE);
                                    try {
                                        Glide.with(mContext).load(myMusic.myMusicContain.song.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.ivRectangularArtist);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    int i;
                                    for (i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                                        try {
                                            seconds = seconds + Math.round(myMusic.myMusicContain.song.get(i).durationSeconds);
                                            if (i > 0) {
                                                binding.noofsongs.setText((i + 1) + " " + getActivity().getResources().getString(R.string.songs));
                                            } else {
                                                binding.noofsongs.setText((i + 1) + " " + getActivity().getResources().getString(R.string.song));
                                            }
                                            binding.tvTime.setText(Utility.formatSeconds(mContext, seconds));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    binding.lblInMyMusic.setVisibility(View.GONE);
                                    binding.layoutAutoPlaylist.setVisibility(View.GONE);
                                    binding.view.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mixpanelAPI.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if(Utility.isConnectingToInternet(getActivity())) {
                getData();
                getMyMusicData();
            }
        }
    }
}
