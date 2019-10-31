package com.shirali.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityLegalInfoBinding;
import com.shirali.service.SongPlayService;
import com.shirali.util.Utility;

public class WebViewActivity extends BaseActivity {

    private ActivityLegalInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_legal_info);
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        if (getIntent().hasExtra("url")) {
            if (getIntent().getStringExtra("url").contains("http")) {
                startWebView(getIntent().getStringExtra("url"));
            } else {
                if (getIntent().getStringExtra("url").contains("www")) {
                    startWebView("https://" + getIntent().getStringExtra("url"));
                } else {
                    startWebView("https://www." + getIntent().getStringExtra("url"));
                }
            }
            binding.lblTitle.setText(R.string.campaign);
        } else if (getIntent().hasExtra("urls")) {
            startWebView(getIntent().getStringExtra("urls"));
            binding.lblTitle.setText(getIntent().getStringExtra("urls_name"));
        }else if (getIntent().hasExtra("banner_url")) {
            if (getIntent().getStringExtra("banner_url").contains("http")) {
                startWebView(getIntent().getStringExtra("banner_url"));
            } else {
                if (getIntent().getStringExtra("banner_url").contains("www")) {
                    startWebView("https://" + getIntent().getStringExtra("banner_url"));
                } else {
                    startWebView("https://www." + getIntent().getStringExtra("banner_url"));
                }
            }
            binding.lblTitle.setText(R.string.shirali);
        }else {
            startWebView(Utility.getUserSetting(WebViewActivity.this).termsPolicyUrl);
        }
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(WebViewActivity.this);
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            Controls.pauseControl(WebViewActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    //Open web view by url
    private void startWebView(String url) {
        binding.webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onLoadResource(WebView view, String url) {
            }

            public void onPageFinished(WebView view, String url) {
                try {
                    binding.loader.setVisibility(View.INVISIBLE);
                    binding.webView.setVisibility(View.VISIBLE);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        });
        binding.webView.getSettings().setLoadWithOverviewMode(true);
        binding.webView.getSettings().setUseWideViewPort(true);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.loadUrl(url);
    }

}
