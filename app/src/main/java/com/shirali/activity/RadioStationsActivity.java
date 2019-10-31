package com.shirali.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.RadioStationAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityRadioStationsBinding;
import com.shirali.interfaces.AdsAvailabilityCallback;
import com.shirali.model.stations.StationList;
import com.shirali.model.stations.Stations;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.CustomBottomTabView;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadioStationsActivity extends BaseActivity {

    private ActivityRadioStationsBinding binding;
    private CustomBottomTabView playerView;
    private Context mContext;
    private CustomLoaderDialog dialog;
    private RadioStationAdapter adapter;
    private ArrayList<Stations> listRadio;
    private LinearLayoutManager mLayoutManager;
    private boolean isSingleTap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_radio_stations);
        mContext = this;
        listRadio = new ArrayList<>();

        setBottomView(mContext);
        dialog = new CustomLoaderDialog(mContext);
        if(Utility.isConnectingToInternet(mContext)) {
            if (!isFinishing()) {
                if (dialog != null) {
                    dialog.show();
                }
            }
            getAllRadioStation();
        }

        //startTimer();

        adapter = new RadioStationAdapter(mContext, listRadio);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recycleviewAll.setLayoutManager(mLayoutManager);
        binding.recycleviewAll.hasFixedSize();
        binding.recycleviewAll.setAdapter(adapter);
        adapter.setOnItemClickListener(new RadioStationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                if (!isSingleTap) {

                    isSingleTap = true;
                    UserModel.getInstance().isAdShowForRadioFirstTime = true; /* --- KIPL -> AKM: Handling ads on Radio Screen---*/
                    Constants.isSongPlay = false;
                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        if (Constants.isPlay) {
                            Controls.pauseControl(mContext);
                        }
                        Constants.StationList = listRadio;
                        Constants.SONG_NUMBER = position;
                        Constants.song = "";
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isSingleTap = false;
                                Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                            }
                        }, 100);
                    } else {
                        Constants.StationList = listRadio;
                        UserModel.getInstance().showAdsIfAvailable(mContext, Utility.getUserInfo(mContext).id, new AdsAvailabilityCallback() {
                            @Override
                            public void adsAvailable(boolean isAdAvailable) {
                                if (isAdAvailable) {
                                    Controls.pauseControl(mContext);
                                    Constants.StationList = listRadio;
                                    Constants.SONG_NUMBER = position;
                                    Constants.song = "";
                                } else {
                                    Constants.StationList = listRadio;
                                    Constants.SONG_NUMBER = position;
                                    Constants.song = "";
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            isSingleTap = false;
                                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                        }
                                    }, 100);
                                }
                            }
                        });
                    }
                    //AKm: Patch
                    Constants.isHomeScreenPlayerVisible = true;
                    playerView.setStationData(listRadio);



                }
            }
        });


        binding.swipeLyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utility.isConnectingToInternet(mContext)) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    getAllRadioStation();
                }
                binding.swipeLyt.setRefreshing(false);
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (SplashActivity.isFromDeep) {
                            UserModel.getInstance().openFragment = "BROWSE";
                            startActivity(new Intent(mContext, MainActivity.class));
                            SplashActivity.isFromDeep = false;
                        }
                        finish();
                    }
                }, 200); // 2000
            }
        });

    }

    //Set bottom player and nav bar view
    private void setBottomView(Context context) {

        playerView = new CustomBottomTabView(context);
        binding.lytCustomBottom.addView(playerView);
        if(Constants.isHomeScreenPlayerVisible) {
            Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom);
            binding.lytCustomBottom.startAnimation(bottomUp);
            binding.lytCustomBottom.setVisibility(View.VISIBLE);
        }



    }

    //Get list of all radio station
    public void getAllRadioStation() {
        Call<StationList> call = Constants.service.getAllRadioStation();
        call.enqueue(new Callback<StationList>() {
            @Override
            public void onResponse(Call<StationList> call, Response<StationList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (response.isSuccessful()) {
                    StationList stationList = response.body();
                    if (stationList.success) {
                        listRadio.clear();
                        if (stationList.stations.size() > 0) {
                            listRadio.addAll(stationList.stations);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StationList> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Utility.showAlert(mContext, getResources().getString(R.string.something_went_wrong));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        isSingleTap = false;
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
            if (UserModel.getInstance().currentPosition != 0) {
                Controls.playControl(mContext);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                    }
                }, 500); // 1000
            }
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(update_playerLayout);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(update_playerLayout, new IntentFilter("update_playerLayout"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(finish_activity);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(finish_activity, new IntentFilter("finish_activity"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(update_playerLayout);
    }

    BroadcastReceiver update_playerLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.isSongPlay) {
            } else {
                if (Constants.StationList.size() > 0) {
                    playerView.setStationData(Constants.StationList);
                }
            }
            if (Constants.isPlay) {
                playerView.changePlayToPause();
            } else {
                playerView.changePlayToPause();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        playerView.playerVisible();
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SplashActivity.isFromDeep) {
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(mContext, MainActivity.class));
                    SplashActivity.isFromDeep = false;
                }
                finish();
            }
        }, 200); // 2000
    }
}
