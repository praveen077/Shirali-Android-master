package com.shirali.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivitySettingsBinding;
import com.shirali.model.setting.AppSetting;
import com.shirali.model.songs.GenresList;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private ActivitySettingsBinding binding;
    private String priceMonthly, priceYearly;
    private Context context;
    private MixpanelAPI mixpanelAPI;
    private String selectedGenres;
    private CustomLoaderDialog dialog;
    private boolean isVocalChecked = false;
    private User userData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        context = this;
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        userData = Utility.getUserInfo(SettingActivity.this);
        getAppSetting();
        getGenreData();
        dialog = new CustomLoaderDialog(context);

        try {
            binding.lblVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Utility.getBooleaPreferences(context, "NotificationPref")) {
            binding.notificationBtn.setChecked(true);
        } else {
            binding.notificationBtn.setChecked(false);
        }
        if (userData.isVocalOnly.length() > 0) {
            binding.vocalOnlySwitch.setChecked(true);
        } else {
            binding.vocalOnlySwitch.setChecked(false);
        }
        binding.filter.setOnClickListener(this);
        binding.imgLegalInfo.setOnClickListener(this);
        binding.imgYoursubscription.setOnClickListener(this);
        binding.imgVersion.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.lytLanguage.setOnClickListener(this);
        binding.lblSignOut.setOnClickListener(this);
        binding.updateProfile.setOnClickListener(this);
        binding.contact.setOnClickListener(this);
        binding.notificationBtn.setOnClickListener(this);
        binding.vocalOnlySwitch.setOnClickListener(this);

        try {
            if (userData.subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                binding.lblChange.setText(R.string.view_membership);
            } else {
                binding.lblChange.setText(R.string.join_shira_li_premium);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_yoursubscription:
                mixpanelAPI.track("Settings: Join Shirali Premium");
                Intent nii = new Intent(SettingActivity.this, YourSubscriptionActivity.class);
                nii.putExtra("month_subscription", priceMonthly);
                nii.putExtra("year_subscription", priceYearly);
                startActivity(nii);
                break;
            case R.id.filter:
                mixpanelAPI.track("Settings: Filter your music");
                Intent ii = new Intent(SettingActivity.this, FilterMusicActivity.class);
                ii.putExtra("isFrom", "setting");
                startActivity(ii);
                break;
            case R.id.img_legal_info:
                Intent iin = new Intent(SettingActivity.this, WebViewActivity.class);
                startActivity(iin);
                break;
            case R.id.backBtn:
                if (isVocalChecked) {
                    startActivity(new Intent(context, MainActivity.class));
                }
                finish();
                break;
            case R.id.updateProfile:
                mixpanelAPI.track("Settings: Update Profile");
                Intent updateIntent = new Intent(SettingActivity.this, SubscriptionActivity.class);
                updateIntent.putExtra("profile", "update_profile");
                startActivity(updateIntent);
                break;
            case R.id.lytLanguage:
                startActivity(new Intent(SettingActivity.this, LanguageActivity.class));
                break;
            case R.id.contact:
                mixpanelAPI.track("Settings: Customer Service");
                sendEmail();
                break;
            case R.id.lblSignOut:
                mixpanelAPI.track("Settings: Sign Out");
                if(Utility.isConnectingToInternet(SettingActivity.this))
                    openAlert();
                break;
            case R.id.notificationBtn:
                sendNotification(Utility.getBooleaPreferences(context, "NotificationPref"));
                break;
            case R.id.vocalOnlySwitch:
                if (binding.vocalOnlySwitch.isChecked()) {
                    openPremiumAlert(context);
                } else {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.show();
                        }
                    }
                    updateVoculOnlyMode(userData.isVocalOnly);
                }
                break;
        }
    }

    private void updateVoculOnlyMode(String isVocalEnabled) {
        if (isVocalEnabled.length() > 0) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("isVocalOnly", "");
            updateProfile(hashMap);
            isVocalChecked = true;
        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("isVocalOnly", selectedGenres);
            updateProfile(hashMap);
            isVocalChecked = true;
        }
    }

    private void sendNotification(boolean isNotificatioEnabled) {
        if (isNotificatioEnabled) {
            Utility.setBooleanPreferences(context, "NotificationPref", false);
            binding.notificationBtn.setChecked(false);
        } else {
            Utility.setBooleanPreferences(context, "NotificationPref", true);
            binding.notificationBtn.setChecked(true);
        }
    }

    //send mail to customer care support
    private void sendEmail() {
        String[] TO = {"support@shiraliapp.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:" + TO));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        if (Utility.getStringPreferences(context, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_request));
        } else {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_request));
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    //Open alert for sign out
    private void openAlert() {
        final Dialog openDialog = new Dialog(SettingActivity.this);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.custom_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        final TextView cancle = (TextView) openDialog.findViewById(R.id.lblCancel);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(getResources().getString(R.string.are_you_realy_want_sing_out));
        action.setText(R.string.sign_out);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("cancel_notification"));
                Utility.setBooleanPreferences(context, Utility.IS_LOGIN, false);
                Utility.clearAllSharedPreferences(context);
                Utility.setBooleanPreferences(context, "appInstall", true);
                UserModel.getInstance().isAdPlayedOnCountOne = false;
                //AKM
                Constants.isHomeScreenPlayerVisible = false;
                startActivity(new Intent(SettingActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Controls.stopControl(context);
                            if (Utility.isServiceRunning(SongPlayService.class.getName(), context)) {
                                stopService(new Intent(context, SongPlayService.class));

                                NotificationManager nMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                nMgr.cancel(SongPlayService.NOTIFICATION_ID);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 500); //1000
                openDialog.dismiss();

            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isVocalChecked) {
            startActivity(new Intent(SettingActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        finish();
    }

    //Get setting detail
    public void getAppSetting() {
        Call<AppSetting> call = Constants.service.getAppSetting();
        call.enqueue(new Callback<AppSetting>() {
            @Override
            public void onResponse(Call<AppSetting> call, Response<AppSetting> response) {
                AppSetting appsetting = response.body();
                try {
                    if (appsetting.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(SettingActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (appsetting.setting != null) {
                            if (appsetting.success) {
                                try {
                                    if (appsetting.setting.androidLiveVersion.equalsIgnoreCase(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)) {
                                    } else {
                                        Utility.showUpdateDialog(context);
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                if (appsetting.success) {
                                    priceMonthly = Double.toString(appsetting.setting.monthlySubscriptionPrice);
                                    priceYearly = Double.toString(appsetting.setting.yearlySubscriptionPrice);
                                }

                                if (appsetting.setting.current_date == null || appsetting.setting.current_date.equalsIgnoreCase("")) {
                                    appsetting.setting.current_date = Utility.getCurrentTimeStamp();
                                }
                                Utility.setUserSetting(context, appsetting);
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<AppSetting> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        //Utility.isConnectingToInternet(SettingActivity.this);

        if(Utility.isConnectingToInternetStatus(SettingActivity.this))
            UserModel.getInstance().getdata(SettingActivity.this);

        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
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
                    Controls.playControl(context);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mixpanelAPI.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Update user profile
    public void updateProfile(HashMap<String, Object> hashMap) {
        Call<UserModel> call = Constants.service.updateProfile(userData.id, hashMap);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    final UserModel model = response.body();
                    if (model.success) {
                        if (binding.vocalOnlySwitch.isChecked()) {
                            binding.vocalOnlySwitch.setChecked(true);
                        } else {
                            binding.vocalOnlySwitch.setChecked(false);
                        }
                        Utility.setUserInfo(context, model.user);
                        UserModel.getInstance().getdata(context);
                    } else {
                        if (binding.vocalOnlySwitch.isChecked()) {
                            binding.vocalOnlySwitch.setChecked(false);
                        } else {
                            binding.vocalOnlySwitch.setChecked(true);
                        }
                        Utility.showAlert(context, model.message);
                    }
                } else {
                    if (binding.vocalOnlySwitch.isChecked()) {
                        binding.vocalOnlySwitch.setChecked(false);
                    } else {
                        binding.vocalOnlySwitch.setChecked(true);
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
                if (binding.vocalOnlySwitch.isChecked()) {
                    binding.vocalOnlySwitch.setChecked(false);
                } else {
                    binding.vocalOnlySwitch.setChecked(true);
                }
                Utility.showAlert(context, getResources().getString(R.string.something_went_wrong));
                t.printStackTrace();
            }
        });
    }

    //Get all genres
    private void getGenreData() {
        Call<GenresList> filterMusicModelCall = Constants.service.getGenres("true");
        filterMusicModelCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                GenresList listGenre = response.body();
                try {
                    if (listGenre.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(SettingActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (listGenre.success) {
                            for (int i = 0; i < listGenre.genres.size(); i++) {
                                if (listGenre.genres.get(i).title.equalsIgnoreCase("Acapella (Vocal Only)")) {
                                    selectedGenres = listGenre.genres.get(i).id;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GenresList> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void openPremiumAlert(final Context context) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.vocal_only_popup);
        TextView start = (TextView) openDialog.findViewById(R.id.btnStart);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblGotIt);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null) {
                    dialog.show();
                }
                updateVoculOnlyMode(userData.isVocalOnly);
                openDialog.dismiss();
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.vocalOnlySwitch.setChecked(false);
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
    }
}
