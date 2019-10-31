package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityLanguageBinding;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

public class LanguageActivity extends BaseActivity implements View.OnClickListener {

    private ActivityLanguageBinding binding;
    private Context context;
    private MixpanelAPI mixpanelAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language);
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        context = this;
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);

        if (Utility.getStringPreferences(LanguageActivity.this, Utility.preferencesLanguage).equalsIgnoreCase("iw")) {
            binding.imgEnglish.setVisibility(View.GONE);
            binding.imgSpanish.setVisibility(View.VISIBLE);
        } else {
            binding.imgEnglish.setVisibility(View.VISIBLE);
            binding.imgSpanish.setVisibility(View.GONE);
        }

        binding.english.setOnClickListener(this);
        binding.spanish.setOnClickListener(this);
        binding.other.setOnClickListener(this);
        binding.imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.english:
                mixpanelAPI.track("Settings: Language: English");
                binding.imgEnglish.setVisibility(View.VISIBLE);
                binding.imgSpanish.setVisibility(View.INVISIBLE);
                binding.imgOther.setVisibility(View.INVISIBLE);
                Utility.setStringPreferences(LanguageActivity.this, Utility.preferencesLanguage, "en");
                Utility.setAppLanguage(LanguageActivity.this);
                startActivity(new Intent(LanguageActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.spanish:
                mixpanelAPI.track("Settings: Language: Hebrew");
                binding.imgEnglish.setVisibility(View.INVISIBLE);
                binding.imgSpanish.setVisibility(View.VISIBLE);
                binding.imgOther.setVisibility(View.INVISIBLE);
                Utility.setStringPreferences(LanguageActivity.this, Utility.preferencesLanguage, "iw");
                Utility.setAppLanguage(LanguageActivity.this);
                startActivity(new Intent(LanguageActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.other:
                binding.imgEnglish.setVisibility(View.INVISIBLE);
                binding.imgSpanish.setVisibility(View.INVISIBLE);
                binding.imgOther.setVisibility(View.VISIBLE);
                break;
            case R.id.img_back:
                finish();
                break;
            default:
                binding.english.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LanguageActivity.this, SettingActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(LanguageActivity.this);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
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

}
