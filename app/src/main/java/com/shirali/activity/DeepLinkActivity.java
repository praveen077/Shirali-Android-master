package com.shirali.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shirali.R;

public class DeepLinkActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);
        onNewIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        SplashActivity.isFromDeep = true;
        try {
            if (Intent.ACTION_VIEW.equals(action) && data != null) {
                data = data.replace("shirali://", "");
                if (data.contains("subscribe")) {
                    Intent open = new Intent(this, SplashActivity.class);
                    open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    open.putExtra("type","subscribe");
                    startActivity(open);
                    finish();
                }else if (data.contains("share")) {
                    String splitData[] = data.split("=");
                    String type = splitData[1].replace("&id", "");
                    String id = splitData[2];
                    if (type.equalsIgnoreCase("song")){
                        Intent open = new Intent(this, SplashActivity.class);
                        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        open.putExtra("song",id);
                        open.putExtra("type","song");
                        startActivity(open);
                        finish();
                    }else if (type.equalsIgnoreCase("album")){
                        Intent open = new Intent(this, SplashActivity.class);
                        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        open.putExtra("album",id);
                        open.putExtra("type","album");
                        startActivity(open);
                        finish();
                    }else if (type.equalsIgnoreCase("artists")){
                        Intent open = new Intent(this, SplashActivity.class);
                        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        open.putExtra("artist",id);
                        open.putExtra("type","artist");
                        startActivity(open);
                        finish();
                    }else if (type.equalsIgnoreCase("playlists")){
                        Intent open = new Intent(this, SplashActivity.class);
                        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        open.putExtra("playlist",id);
                        open.putExtra("type","playlist");
                        startActivity(open);
                        finish();
                    }
                }else if (data.contains("resetpassword")) {
                    String splitData[] = data.split("=");
                    String token = splitData[1];
                    Intent open = new Intent(this, PasswordRecoveryActivity.class);
                    open.putExtra("isFrom","Deep");
                    open.putExtra("token",token);
                    open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(open);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

