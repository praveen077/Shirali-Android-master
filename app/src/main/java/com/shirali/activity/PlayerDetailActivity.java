package com.shirali.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.adapter.SongForUAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityPlayerDetailBinding;
import com.shirali.interfaces.GetMyMusicListCallback;
import com.shirali.model.songs.Song;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.AutoScrollableLinearLayoutManager;
import com.shirali.widget.CustomBottomTabView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityPlayerDetailBinding binding;
    private SongForUAdapter rpAdapter;
    private ArrayList<Song> listPrefSong;
    private Context context;
    private ArrayList<String> listSong;
    private ArrayList<String> song_list;
    private CustomBottomTabView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail);
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        context = this;
        listPrefSong = new ArrayList<>();
        listPrefSong.addAll(UserModel.getInstance().listOfActualSong);
        listSong = new ArrayList<>();
        song_list = new ArrayList<>();
        setBottomView(context);
        updateAUI();

        binding.recyclerviewAlbumSongs.setHasFixedSize(true);
        binding.recyclerviewAlbumSongs.setLayoutManager(new AutoScrollableLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rpAdapter = new SongForUAdapter(this, listPrefSong, "AlbumDetail", Utility.getUserInfo(context).myMusic);
        //AKM:NEXT_LOGIC
        rpAdapter.isAlbum();
        binding.recyclerviewAlbumSongs.setAdapter(rpAdapter);
        binding.recyclerviewAlbumSongs.smoothScrollToPosition(Constants.SONG_NUMBER);
        rpAdapter.setOnItemClickListener(new SongForUAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String type, int position) {
                song_list.clear();
                song_list.add(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                if (Utility.getUserInfo(context).myMusic.containsAll(song_list)) {
                    binding.imgAddSong.setVisibility(View.INVISIBLE);
                } else {
                    binding.imgAddSong.setVisibility(View.VISIBLE);
                }
                Controls.pauseControl(context);
                Constants.SONGS_LIST = listPrefSong;
                Constants.SONG_NUMBER = position;
                Constants.song = Constants.SONGS_LIST.get(position).id;
                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                playerView.setPlayerData(Constants.SONGS_LIST);
                binding.recyclerviewAlbumSongs.smoothScrollToPosition(Constants.SONG_NUMBER);
            }

            @Override
            public void onItemVisible(View view, boolean isVisible) {
                if (isVisible) {
                    song_list.clear();
                    song_list.add(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                    if (Utility.getUserInfo(context).myMusic.containsAll(song_list)) {
                        binding.imgAddSong.setVisibility(View.INVISIBLE);
                    } else {
                        binding.imgAddSong.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (Constants.SONGS_LIST.get(UserModel.getInstance().removeSongIndex).id.equalsIgnoreCase(Constants.song)) {
                        binding.imgAddSong.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
            if (Constants.isPlay) {
                playerView.updateSeekBar();
            }
        }

        binding.imgBackAgain.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.imgAddSong.setOnClickListener(this);
        binding.imgManu.setOnClickListener(this);
    }

    //Set bottom sheet player and nav bar
    private void setBottomView(Context context) {
        if (Constants.isHomeScreenPlayerVisible){
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }
        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
    }

    private void updateAUI() {
        try {
            Glide.with(context).load(listPrefSong.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbum);
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (listPrefSong.get(0).title_hebrew.equalsIgnoreCase("") || listPrefSong.get(0).title_hebrew == null) {
                    binding.lblTitle.setText(listPrefSong.get(0).title);
                } else {
                    binding.lblTitle.setText(listPrefSong.get(0).title_hebrew);
                }
            } else {
                binding.lblTitle.setText(listPrefSong.get(0).title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateIcon() {
        if (binding.recyclerviewAlbumSongs != null && binding.recyclerviewAlbumSongs.getAdapter() != null)
            binding.recyclerviewAlbumSongs.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backBtn) {
            finishActivity();
        } else if (v.getId() == R.id.imgBackAgain) {
            finishActivity();
        } else if (v.getId() == R.id.imgAddSong) {
            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                song_list.clear();
                song_list.add(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).id);
                addedToMyMusic(Utility.getUserInfo(context).myMusic, song_list, false);
                binding.imgAddSong.setVisibility(View.INVISIBLE);
            } else {
                Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_mymusic_as_many_songs_as_you_want));
            }
        } else if (v.getId() == R.id.imgManu) {
            openMenu();
        }
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
    }

    //Add and remove song list into my music
    private void addedToMyMusic(ArrayList<String> myMusic, ArrayList<String> song_list, boolean isRemove) {
        if (isRemove) {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (myMusic.contains(song_list.get(i))) {
                    listSong.remove(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.album_removed));
        } else {
            listSong.addAll(myMusic);
            for (int i = 0; i < song_list.size(); i++) {
                if (!myMusic.contains(song_list.get(i))) {
                    listSong.add(song_list.get(i));
                }
            }
            addSong(listSong);
            listSong.clear();
            Utility.showPopup(context, getString(R.string.album_added));
        }
    }

    //Bottom sheet menu
    private void openMenu() {
        final Dialog d = new BottomSheetDialog(context);
        d.setContentView(R.layout.user_action_onsong_cell);
        View view = d.findViewById(R.id.bs);
        ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        TextView artist_name = (TextView) d.findViewById(R.id.tvArtistName);
        ImageView artistImage = (ImageView) d.findViewById(R.id.ivArtistImage);
        try {
            if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).titleHebrew == null || Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                    artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).title);
                } else {
                    artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).titleHebrew);
                }
            } else {
                artist_name.setText(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).title);
            }
            Glide.with(context).load(Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(artistImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        d.findViewById(R.id.lytPlay).setVisibility(View.GONE);
        d.findViewById(R.id.lytAddMore).setVisibility(View.GONE);
        d.findViewById(R.id.lytEditPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareAlbum).setVisibility(View.GONE);
        d.findViewById(R.id.lytShareArtist).setVisibility(View.GONE);
        d.findViewById(R.id.lytSharePlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytFollowPlaylist).setVisibility(View.GONE);
        d.findViewById(R.id.lytUnFollow).setVisibility(View.GONE);
        if (binding.imgAddSong.getVisibility() == View.VISIBLE) {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.GONE);
        } else {
            d.findViewById(R.id.lytRemoveSong).setVisibility(View.VISIBLE);
        }
        d.setCancelable(true);
        d.findViewById(R.id.closeArtistSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.findViewById(R.id.lytViewArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.isPremium) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        openArtist();
                    } else {
                        Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                    }
                } else {
                    openArtist();
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytViewAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.size() > 0) {
                    UserModel.getInstance().tempAlbum = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0);
                    if (Constants.SONGS_LIST.get(Constants.SONG_NUMBER).albums.get(0).isPremium) {
                        if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                            openAlbum(Constants.SONG_NUMBER);
                        } else {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        }
                    } else {
                        openAlbum(Constants.SONG_NUMBER);
                    }
                } else {
                    Utility.showNoDataFound(context);
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytShareSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.shareIt(context, "song", Constants.SONGS_LIST.get(Constants.SONG_NUMBER).title, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.name, Constants.SONGS_LIST.get(Constants.SONG_NUMBER).shareUrl);
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytAddToPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                    startActivity(new Intent(context, AddPlaylistsActivity.class).putExtra("list_of_song", song_list));
                } else {
                    Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_add_into_playlist_as_many_songs_as_you_want));
                }
                d.dismiss();
            }
        });
        d.findViewById(R.id.lytRemoveSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imgAddSong.setVisibility(View.VISIBLE);
                addedToMyMusic(Utility.getUserInfo(context).myMusic, song_list, true);
                d.dismiss();
            }
        });
        d.show();
    }

    //Add song list into my music
    private void addSong(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("myMusic", list);
        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (user.success) {
                            Utility.setUserInfo(context, user.user);
                            UserModel.getInstance().getdata(context);
                            UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                                @Override
                                public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                                    if (isAdded) {
                                        rpAdapter.updateMyMusicList(myMusic);
                                        rpAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void openArtist() {
        Intent intent = new Intent(context, ArtistDetailActivity.class);
        intent.putExtra("artist_id", Constants.SONGS_LIST.get(Constants.SONG_NUMBER).artist.id);
        startActivity(intent);
    }

    private void openAlbum(int position) {
        try {
            UserModel.getInstance().artist_id = Constants.SONGS_LIST.get(position).artist;
            UserModel.getInstance().album = Constants.SONGS_LIST.get(position).albums.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(context, AlbumDetailActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(context);
        playerView.updateBottomView(UserModel.getInstance().openFragment);
        try {
            if (Constants.SONGS_LIST != null && Constants.SONGS_LIST.size() > 0) {
                playerView.setPlayerData(Constants.SONGS_LIST);
                Song data = Constants.SONGS_LIST.get(Constants.SONG_NUMBER);
                Glide.with(PlayerDetailActivity.this).load(data.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbum);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
                    if (data.title_hebrew.equalsIgnoreCase("") || data.title_hebrew == null) {
                        binding.lblTitle.setText(data.title);
                    } else {
                        binding.lblTitle.setText(data.title_hebrew);
                    }
                    if (data.artist.nameHebrew.equalsIgnoreCase("") || data.artist.nameHebrew == null) {
                        binding.lblArtist.setText(data.artist.name);
                    } else {
                        binding.lblArtist.setText(data.artist.nameHebrew);
                    }
                } else {
                    binding.lblTitle.setText(data.title);
                    binding.lblArtist.setText(data.artist.name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // clear list and add current playing song id
                song_list.clear();
                if (Utility.getBooleaPreferences(context, "suffle")) {
                    song_list.add(UserModel.getInstance().listOfShuffleSong.get(Constants.SONG_NUMBER).id);
                } else {
                    song_list.add(UserModel.getInstance().listOfActualSong.get(Constants.SONG_NUMBER).id);
                }

                //check list is exist or not into user my music
                if (Utility.getUserInfo(context).myMusic.containsAll(song_list)) {
                    binding.imgAddSong.setVisibility(View.INVISIBLE);
                } else {
                    binding.imgAddSong.setVisibility(View.VISIBLE);
                }

                // for update the list
                UserModel.getInstance().getMyMusic(context, new GetMyMusicListCallback() {
                    @Override
                    public void addedToMusic(boolean isAdded, ArrayList<String> myMusic) {
                        if (rpAdapter!=null) {
                            rpAdapter.updateMyMusicList(myMusic);
                            rpAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });

        playerView.changePlayToPause();

        playerView.updateSeekBar();

        // it is for campaign
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                if (Constants.isChangeSong) {
                    if (!Utility.getBooleaPreferences(context,"ad_in_background")) {
                        Constants.isChangeSong = false;
                        Controls.nextControl(context);
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
                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                }
            }
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(update_playerLayout);
        LocalBroadcastManager.getInstance(context).registerReceiver(update_playerLayout, new IntentFilter("update_playerLayout"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(change_song);
        LocalBroadcastManager.getInstance(context).registerReceiver(change_song, new IntentFilter("change_song"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
    }

    //Update background
    private void updateSongData(Song song) {
        binding.recyclerviewAlbumSongs.smoothScrollToPosition(Constants.SONG_NUMBER);
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
            if (song.title_hebrew.equalsIgnoreCase("") || song.title_hebrew == null) {
                binding.lblTitle.setText(song.title);
            } else {
                binding.lblTitle.setText(song.title_hebrew);
            }
            if (song.artist.nameHebrew.equalsIgnoreCase("") || song.artist.nameHebrew == null) {
                binding.lblArtist.setText(song.artist.name);
            } else {
                binding.lblArtist.setText(song.artist.nameHebrew);
            }
        } else {
            binding.lblTitle.setText(song.title);
            binding.lblArtist.setText(song.artist.name);
        }
        try {
            Glide.with(PlayerDetailActivity.this).load(song.artwork).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.imglogo).crossFade().into(binding.imgAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateIcon();
            playerView.updateSeekBar();
        }
    };
    BroadcastReceiver change_song = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.isPageSelectedFromNextOfPrevious = false;
            playerView.setPlayerData(Constants.SONGS_LIST);
        }
    };
    BroadcastReceiver update_playerLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSongData(Constants.SONGS_LIST.get(Constants.SONG_NUMBER));
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
