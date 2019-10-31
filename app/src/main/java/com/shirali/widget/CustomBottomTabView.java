package com.shirali.widget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.shirali.R;
import com.shirali.activity.MainActivity;
import com.shirali.activity.PlayerActivity;
import com.shirali.activity.SplashActivity;
import com.shirali.controls.Controls;
import com.shirali.databinding.CustomPlayerBinding;
import com.shirali.model.songs.Song;
import com.shirali.model.stations.Stations;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.ArrayList;

/**
 * Created by Sagar on 15/2/18.
 */

public class CustomBottomTabView extends LinearLayout {
    private Context context;
    private CustomPlayerBinding binding;
    private String albumString = "", songString = "";

    public CustomBottomTabView(final Context context) {
        super(context);
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_player, null, false);
        addView(binding.getRoot());
        binding.lblSongName.setSelected(true);

        if (Utility.getUserInfo(context).isVocalOnly != null) {
            if (Utility.getUserInfo(context).isVocalOnly.length() > 0) {
                binding.player.setBackgroundColor(context.getResources().getColor(R.color.purple_player));
                binding.mseek.getThumb().setColorFilter(context.getResources().getColor(R.color.purple_seek), PorterDuff.Mode.SRC_IN);
                binding.mseek.setProgressDrawable(context.getResources().getDrawable(R.drawable.purple_seekbar_drawable));
                binding.seekRadio.getThumb().setColorFilter(context.getResources().getColor(R.color.purple_seek), PorterDuff.Mode.SRC_IN);
                binding.seekRadio.setProgressDrawable(context.getResources().getDrawable(R.drawable.purple_seekbar_drawable));
            } else {
                binding.player.setBackgroundColor(context.getResources().getColor(R.color.bg_color));
                binding.mseek.getThumb().setColorFilter(context.getResources().getColor(R.color.back_color), PorterDuff.Mode.SRC_IN);
                binding.mseek.setProgressDrawable(context.getResources().getDrawable(R.drawable.seekbar_drawable));
                binding.seekRadio.getThumb().setColorFilter(context.getResources().getColor(R.color.back_color), PorterDuff.Mode.SRC_IN);
                binding.seekRadio.setProgressDrawable(context.getResources().getDrawable(R.drawable.seekbar_drawable));
            }
        }

        /*if (Constants.isSongPlay)
            if (Constants.SONGS_LIST != null)
                if (Constants.SONGS_LIST.size() > 0)
                    if (Constants.isPlay)
                        if (binding.player.getVisibility() != View.VISIBLE) {
                            binding.player.setVisibility(VISIBLE);
                        }*/

        binding.first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel.getInstance().openFragment = "BROWSE";
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    context.startActivity(new Intent(context, MainActivity.class));
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("finish_activity"));
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });
        binding.second.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel.getInstance().openFragment = "RECENT";
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "RECENT";
                    context.startActivity(new Intent(context, MainActivity.class));
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("finish_activity"));
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });
        binding.fourth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel.getInstance().openFragment = "SEARCH";
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "SEARCH";
                    context.startActivity(new Intent(context, MainActivity.class));
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("finish_activity"));
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });
        binding.fifth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel.getInstance().openFragment = "MYMUSIC";
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "MYMUSIC";
                    context.startActivity(new Intent(context, MainActivity.class));
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("finish_activity"));
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });

        binding.player.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PlayerActivity.class));
            }
        });
        binding.imgPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("refreshList"));
                if (Constants.isSongPlay) {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                        //binding.imgPlay.setImageResource(R.drawable.play);
                    } else {
                        Controls.playControl(context);
                        //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    }
                } else {
                    if (Constants.isPlay) {
                        Controls.pauseControl(context);
                        //binding.imgPlay.setImageResource(R.drawable.play);
                    } else {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        //binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    }
                }
            }
        });

        if (binding.loadingMusic.getVisibility() != View.VISIBLE) {
            binding.mseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!UserModel.getInstance().isSongLoading) {
                        if (fromUser) {
                            Constants.seekTo = progress;
                            Controls.seekToControl(context);
                        }
                    } else {
                        binding.mseek.setProgress(0);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(play_pause_control);
        LocalBroadcastManager.getInstance(context).registerReceiver(play_pause_control, new IntentFilter("play_pause_control"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(show_loader);
        LocalBroadcastManager.getInstance(context).registerReceiver(show_loader, new IntentFilter("show_loader"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(update_playerLayout);
        LocalBroadcastManager.getInstance(context).registerReceiver(update_playerLayout, new IntentFilter("update_playerLayout"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(switch_off_player);
        LocalBroadcastManager.getInstance(context).registerReceiver(switch_off_player, new IntentFilter("switch_off_player"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(stop_player);
        LocalBroadcastManager.getInstance(context).registerReceiver(stop_player, new IntentFilter("stop_player"));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("refreshList"));
    }

    //Show player and set list of song in player
    public void setPlayerData(ArrayList<Song> data) {
        /*boolean isPlayerNeedToDisplay = Constants.isHomeScreenPlayerVisible;

        if(Constants.isPlay)
            isPlayerNeedToDisplay = true;*/


        if (data.size() > 0) {
                int song_number = 0;
                try {
                    song_number = 0;
                    if (binding.player.getVisibility() != View.VISIBLE) {
                        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
                        binding.player.startAnimation(bottomUp);
                        binding.seekBar.startAnimation(bottomUp);
                        binding.player.setVisibility(VISIBLE);
                        binding.seekBar.setVisibility(VISIBLE);
                    }
                    if (UserModel.getInstance().songPlayerDuration > 0) {
                        binding.mseek.setProgress(UserModel.getInstance().songPlayerDuration);
                    }

                    if (Constants.isPlay) {
                        binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    }
                    for (int i = 0; i < Constants.SONGS_LIST.size(); i++) {
                        if (Constants.SONGS_LIST.get(i).id.equalsIgnoreCase(Constants.song)) {
                            song_number = i;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (data.get(song_number).title_hebrew == null || data.get(song_number).title_hebrew.equalsIgnoreCase("")) {
                            songString = data.get(song_number).title;
                        } else {
                            songString = data.get(song_number).title_hebrew;
                        }
                        if (data.get(song_number).artist != null) {
                            if (data.get(song_number).artist.nameHebrew == null || data.get(song_number).artist.nameHebrew.equalsIgnoreCase("")) {
                                albumString = data.get(song_number).artist.name;
                            } else {
                                albumString = data.get(song_number).artist.nameHebrew;
                            }
                        }
                        binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                    } else {
                        songString = data.get(song_number).title;
                        if (data.get(Constants.SONG_NUMBER).artist != null) {
                            albumString = data.get(song_number).artist.name;
                        }
                        binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


        }

    }

    public void setStationData(ArrayList<Stations> data) {
        //Logic failed due to common mechanism
      /*  boolean isPlayerNeedToDisplay = Constants.isHomeScreenPlayerVisible;

        if(Constants.isPlay)
            isPlayerNeedToDisplay = true;*/

        if (data.size() > 0) {
                if (binding.player.getVisibility() != View.VISIBLE) {
                    Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
                    binding.player.startAnimation(bottomUp);
                    binding.seekBar.startAnimation(bottomUp);
                    binding.player.setVisibility(VISIBLE);
                    binding.seekBar.setVisibility(VISIBLE);
                }

                if (Constants.isPlay) {
                    binding.imgPlay.setImageResource(R.drawable.icon_pause);
                }
                try {
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (data.get(Constants.SONG_NUMBER).titleHebrew == null || data.get(Constants.SONG_NUMBER).titleHebrew.equalsIgnoreCase("")) {
                            songString = data.get(Constants.SONG_NUMBER).title;
                        } else {
                            songString = data.get(Constants.SONG_NUMBER).titleHebrew;
                        }
                        binding.lblSongName.setText(songString);
                    } else {
                        songString = data.get(Constants.SONG_NUMBER).title;
                        binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, "")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }

    //Update seek bar
    public void updateSeekBar() {
        if (Constants.isSongPlay) {
            binding.mseek.setVisibility(VISIBLE);
            binding.seekRadio.setVisibility(GONE);
            Constants.PROGRESSBAR_HANDLER = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    Integer i[] = (Integer[]) msg.obj;
                    try {
                        if (i[2] != 0) {
                            binding.mseek.setProgress(i[2]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        } else {
            binding.mseek.setVisibility(GONE);
            binding.seekRadio.setVisibility(VISIBLE);
            binding.seekRadio.setEnabled(false);
        }

    }

    //Update bottom selected view
    public void updateBottomView(String selectedTab) {
        if (selectedTab.equalsIgnoreCase("BROWSE")) {
            binding.second.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
            binding.first.setAlpha(1);
        } else if (selectedTab.equalsIgnoreCase("RECENT")) {
            binding.second.setAlpha(1);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(0.4f);
            binding.first.setAlpha(0.4f);
        } else if (selectedTab.equalsIgnoreCase("SEARCH")) {
            binding.second.setAlpha(0.4f);
            binding.fourth.setAlpha(1);
            binding.fifth.setAlpha(0.4f);
            binding.first.setAlpha(0.4f);
        } else {
            binding.second.setAlpha(0.4f);
            binding.fourth.setAlpha(0.4f);
            binding.fifth.setAlpha(1);
            binding.first.setAlpha(0.4f);
        }
    }

    public void changePlayToPause() {
        if (Constants.isPlay) {
            binding.imgPlay.setImageResource(R.drawable.icon_pause);
        } else {
            binding.imgPlay.setImageResource(R.drawable.play);
        }
    }

    BroadcastReceiver play_pause_control = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /* --- KIPL -> AKM: handle play pause control comes from service, added action "play" / "pause" in service  ---*/
            if(intent != null){
                if(intent.getStringExtra("action").equalsIgnoreCase("play")){
                    binding.imgPlay.setImageResource(R.drawable.icon_pause);
                    binding.imgPlay.setVisibility(VISIBLE);
                    binding.loadingMusic.setVisibility(GONE);
                }
                else if(intent.getStringExtra("action").equalsIgnoreCase("pause")){
                    binding.imgPlay.setImageResource(R.drawable.play);
                    binding.imgPlay.setVisibility(VISIBLE);
                    binding.loadingMusic.setVisibility(GONE);

                }
            }
            /* --- KIPL -> AKM: this won't be handling every case, so above added  ---*/
            /*if (Constants.isPlay) {
                binding.imgPlay.setImageResource(R.drawable.icon_pause);
            } else {
                binding.imgPlay.setImageResource(R.drawable.play);
            }*/
        }
    };
    BroadcastReceiver show_loader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UserModel.getInstance().isSongLoading = true;
            binding.loadingMusic.setVisibility(View.VISIBLE);
            binding.imgPlay.setVisibility(View.GONE);
            if(!intent.hasExtra("seek_percentage"))
                binding.mseek.setProgress(0);
        }
    };
    BroadcastReceiver update_playerLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            int song_number = Constants.SONG_NUMBER;
            UserModel.getInstance().isSongLoading = false;
            binding.loadingMusic.setVisibility(View.GONE);
            binding.imgPlay.setVisibility(View.VISIBLE);
            binding.imgPlay.setImageResource(R.drawable.icon_pause);
            if (Constants.isSongPlay) {
                binding.mseek.setVisibility(VISIBLE);
                binding.seekRadio.setVisibility(GONE);
                if (Constants.SONGS_LIST.size() > 0) {
                    Song data = null;
                    if (song_number >= Constants.SONGS_LIST.size()) {
                        data = Constants.SONGS_LIST.get(0);
                    } else {
                        data = Constants.SONGS_LIST.get(song_number);
                    }
                    if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                        if (data.title_hebrew == null || data.title_hebrew.equalsIgnoreCase("")) {
                            songString = data.title;
                        } else {
                            songString = data.title_hebrew;
                        }
                        if (data.albums != null && data.albums.size() > 0) {
                            if (data.albums.get(0).titleHebrew == null || data.albums.get(0).titleHebrew.equalsIgnoreCase("")) {
                                albumString = data.artist.name;
                            } else {
                                albumString = data.artist.nameHebrew;
                            }
                        }
                        binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                    } else {
                        songString = data.title;
                        if (data.albums != null && data.albums.size() > 0) {
                            //albumString = data.albums.get(0).title;
                            albumString = data.artist.name;
                        }
                        binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, albumString)));
                    }
                }
            } else {
                binding.loadingMusic.setVisibility(View.GONE);
                binding.mseek.setVisibility(GONE);
                binding.seekRadio.setVisibility(VISIBLE);
                binding.seekRadio.setEnabled(false);
                if (Utility.getStringPreferences(context, Utility.preferencesLanguage).matches("iw")) {
                    if (Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew == null || Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew.equalsIgnoreCase("")) {
                        songString = Constants.StationList.get(Constants.SONG_NUMBER).title;
                    } else {
                        songString = Constants.StationList.get(Constants.SONG_NUMBER).titleHebrew;
                    }
                    binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, "")));
                } else {
                    songString = Constants.StationList.get(Constants.SONG_NUMBER).title;
                    binding.lblSongName.setText(Html.fromHtml(Utility.setTextToTextView(songString, "")));
                }
            }

        }

    };
    private BroadcastReceiver switch_off_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binding.player.getVisibility() == View.VISIBLE) {
                binding.player.setVisibility(GONE);
                binding.seekBar.setVisibility(GONE);
            }
        }
    };
    private BroadcastReceiver stop_player = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.mseek.setProgress(0);
            binding.imgPlay.setImageResource(R.drawable.play);
        }
    };

    //Notified current playing song play pause icon and seekbar on play/pause
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekBar();
        }
    };

    public boolean playerVisible(){
        return Constants.isHomeScreenPlayerVisible = binding.player.getVisibility() == View.VISIBLE;
    }
}
