package com.shirali.fragment.mymusic;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.App;
import com.shirali.R;
import com.shirali.activity.PlaylistActivity;
import com.shirali.adapter.AddlistAdapter;
import com.shirali.databinding.FragmentMyPlaylistBinding;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.playlist.PlayListRequest;
import com.shirali.model.playlist.PlaylistResponse;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by user on 11/7/17.
 */

public class MyPlaylistFragment extends Fragment implements View.OnClickListener {

    public static boolean isFromPlaylist = false;
    public App app;
    private FragmentMyPlaylistBinding binding;
    private Context mContext;
    private EditText title;
    private ArrayList<Shirali> lists;
    private AddlistAdapter adapter;
    private CustomLoaderDialog dialog;
    private int seconds;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ArrayList<Song> listSong;
    private MixpanelAPI mixpanelAPI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_playlist, container, false);
        mContext = getActivity();
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        listSong = new ArrayList<>();
        dialog = new CustomLoaderDialog(mContext);
        preferences = getActivity().getSharedPreferences("playlist", 0);
        editor = preferences.edit();
        app = (App) mContext.getApplicationContext();

        /* --- KIPL -> AKM : Internet Check ---*/
        if(Utility.isConnectingToInternet(getActivity())) {
            if (!((Activity) mContext).isFinishing()) {
                dialog.show();
            }
            getplaylist();
        }
        //getMyMusicData();

        lists = new ArrayList<>();
        binding.tvCreatePlaylist.setOnClickListener(this);
        binding.createPlaylist.setOnClickListener(this);
        binding.layoutAutoPlaylist.setOnClickListener(this);

        adapter = new AddlistAdapter(getActivity(), lists, "");
        binding.recycleAddlist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.recycleAddlist.setHasFixedSize(true);
        binding.recycleAddlist.setAdapter(adapter);
        adapter.setOnItemClickListener(new AddlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                switch (view.getId()) {
                    case R.id.lytEditPlaylist:
                        final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
                        dialog.setContentView(R.layout.create_playlist_dialog);
                        dialog.show();
                        TextView textView = (TextView) dialog.findViewById(R.id.head);
                        textView.setText(getResources().getString(R.string.please_edit_your_playlist_name));

                        title = (EditText) dialog.findViewById(R.id.title);
                        title.setText(lists.get(position).title);
                        title.setSelection(lists.get(position).title.length());
                        TextView cancelPlaylist = (TextView) dialog.findViewById(R.id.cancelPlaylist);
                        cancelPlaylist.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        TextView createPlaylist = (TextView) dialog.findViewById(R.id.createPlaylist);
                        createPlaylist.setText("Edit");
                        createPlaylist.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("update_data"));
                                editPlaylist(position);
                                dialog.dismiss();
                            }
                        });

                        break;
                    case R.id.lytSong:
                        if (lists.size() > 0) {
                            if (lists.get(position).songs.size() > 0) {
                                openActivity(position);
                            } else {
                                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", false));
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("replace_fragment"));
                                } else {
                                    Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                                }
                            }
                        }
                        break;
                    case R.id.lytPlay:
                        openActivity(position);
                        break;
                    case R.id.lytAddMore:
                        editor.putString("current", lists.get(position).id);
                        editor.apply();
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", false));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("replace_fragment"));
                        break;
                }

            }
        });

        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utility.isConnectingToInternet(getActivity())) {
                    if (dialog != null) {
                        dialog.show();
                    }
                    getplaylist();
                    getMyMusicData();
                }
                binding.swipeLyt.setRefreshing(false);
            }
        });
        return binding.getRoot();
    }

    private void openActivity(int position) {
        if (lists.size() > 0) {
            if (lists.size() > 0) {
                UserModel.getInstance().shirali = lists.get(position);
            }
            mContext.startActivity(new Intent(mContext, PlaylistActivity.class).putExtra("playlist", lists.get(position).id));
        } else {
            Utility.showAlert(mContext, mContext.getResources().getString(R.string.no_data_found));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.tvCreatePlaylist) {
            mixpanelAPI.track("My Music: Create Playlist");
            if (Utility.getUserInfo(getActivity()).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                openPlaylistDialog();
            } else {
                mixpanelAPI.track("My Music: Create Playlist: Go Premium");
                Utility.showSubscriptionAlert(mContext, mContext.getResources().getString(R.string.with_shiraLi_premium_you_can_create_as_many_playlists_as_you_want));
            }
        } else if (v == binding.createPlaylist) {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.playlistLyt.setVisibility(View.VISIBLE);
        } else if (v == binding.layoutAutoPlaylist) {
            mContext.startActivity(new Intent(mContext, PlaylistActivity.class).putExtra("list_song", "list_song"));
        }
    }

    //Dialog for enter playlist name
    private void openPlaylistDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
        dialog.setContentView(R.layout.create_playlist_dialog);
        dialog.show();
        title = (EditText) dialog.findViewById(R.id.title);
        TextView cancelPlaylist = (TextView) dialog.findViewById(R.id.cancelPlaylist);
        cancelPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView createPlaylist = (TextView) dialog.findViewById(R.id.createPlaylist);
        createPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (title.getText().toString().equalsIgnoreCase("")) {
                    Utility.showAlert(mContext, getString(R.string.valid_playlist_name));
                } else {
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", false));
                    createPlaylist();
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("replace_fragment"));
                    getplaylist();
                }
                dialog.dismiss();
            }
        });
    }

    private void editPlaylist(int position) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", lists.get(position).id);
        hashMap.put("title", title.getText().toString().trim());
        Call<PlayListRequest> call = Constants.service.editPlaylist(Utility.getUserInfo(mContext).id, hashMap);
        call.enqueue(new Callback<PlayListRequest>() {
            @Override
            public void onResponse(Call<PlayListRequest> call, Response<PlayListRequest> response) {
                PlayListRequest playlistResponse = response.body();
                try {
                    if (playlistResponse.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            getplaylist();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlayListRequest> call, Throwable t) {

            }
        });
    }

    //create playlist
    private void createPlaylist() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title.getText().toString().trim());
        hashMap.put("createdBy", Utility.getUserInfo(mContext).id);
        Call<PlayListRequest> playlistResponseCall = Constants.service.CreatePlaylist(hashMap);
        playlistResponseCall.enqueue(new Callback<PlayListRequest>() {
            @Override
            public void onResponse(Call<PlayListRequest> call, Response<PlayListRequest> response) {
                PlayListRequest playlistResponse = response.body();
                try {
                    if (playlistResponse.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (playlistResponse.success) {
                            editor.putString("current", playlistResponse.songs.id);
                            editor.commit();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlayListRequest> call, Throwable t) {
                Utility.showAlert(mContext, t.toString());
            }
        });
    }

    //Get playlist
    private void getplaylist() {
        Call<PlaylistResponse> call = Constants.service.getUserPlaylist(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (!((Activity) mContext).isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    PlaylistResponse playlistResponse = response.body();
                    if (playlistResponse.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(getActivity());
                    } else {
                        if (playlistResponse.success) {
                            lists.clear();
                            if (playlistResponse.songs.size() <= 0) {
                                binding.recycleAddlist.setVisibility(View.GONE);
                            } else {
                                binding.emptyLayout.setVisibility(View.GONE);
                                binding.playlistLyt.setVisibility(View.VISIBLE);
                                binding.recycleAddlist.setVisibility(View.VISIBLE);
                                for (int i = 0; i < playlistResponse.songs.size(); i++) {
                                    lists.add(playlistResponse.songs.get(i));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    //Calculate all my music data for auto playlist
    public void getMyMusicData() {
        Call<MyMusic> call = Constants.service.getMusic(Utility.getUserInfo(getActivity()).id);
        call.enqueue(new Callback<MyMusic>() {
            @Override
            public void onResponse(Call<MyMusic> call, Response<MyMusic> response) {
                listSong.clear();
                if (dialog != null) {
                    dialog.dismiss();
                }
                MyMusic myMusic = response.body();
                try {
                    if (myMusic.getMessage().equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (myMusic.success) {
                            if (myMusic.myMusicContain.song.size() <= 0) {
                                binding.lytAuto.setVisibility(View.GONE);
                            } else {
                                binding.lytAuto.setVisibility(View.VISIBLE);
                                if (myMusic.myMusicContain.song.size() > 0) {
                                    listSong.addAll(myMusic.myMusicContain.song);
                                } else {
                                    binding.layoutAutoPlaylist.setVisibility(View.GONE);
                                }
                                binding.emptyLayout.setVisibility(View.GONE);
                                binding.playlistLyt.setVisibility(View.VISIBLE);
                                if (myMusic.myMusicContain.song.size() > 0) {
                                    try {
                                        Glide.with(getActivity()).load(myMusic.myMusicContain.song.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.ivRectangularArtist);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                seconds = 0;
                                try {
                                    int i;
                                    for (i = 0; i < myMusic.myMusicContain.song.size(); i++) {
                                        try {
                                            seconds = seconds + Math.round(myMusic.myMusicContain.song.get(i).durationSeconds);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (i > 1) {
                                        binding.noSongs.setText(i + " " + mContext.getResources().getString(R.string.songs));
                                        binding.time.setText(Utility.formatSeconds(mContext, seconds));
                                    } else if (i == 1) {
                                        binding.noSongs.setText(i + " " + mContext.getResources().getString(R.string.song));
                                        binding.time.setText(Utility.formatSeconds(mContext, seconds));
                                    } else if (i == 0) {
                                        binding.noSongs.setText(i + " " + mContext.getResources().getString(R.string.song));
                                        binding.time.setVisibility(View.GONE);
                                        binding.imgDot.setVisibility(View.GONE);
                                    }
                                } catch (Resources.NotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MyMusic> call, Throwable t) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        lists.clear();
        getplaylist();
        if (isFromPlaylist) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("new_playlist").putExtra("isVisible", false));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("replace_fragment"));
            isFromPlaylist = false;
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(update_data);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(update_data, new IntentFilter("update_data"));
        getMyMusicData();
    }

    public void reload() {
        getMyMusicData();
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

    BroadcastReceiver update_data = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!((Activity) mContext).isFinishing()) {
                lists.clear();
                getMyMusicData();
                getplaylist();
            }
        }
    };
}

