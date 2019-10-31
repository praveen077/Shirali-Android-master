package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.shirali.R;
import com.shirali.databinding.ActivitySplashBinding;
import com.shirali.model.comman.AppCount;
import com.shirali.model.share.ShareAlbum;
import com.shirali.model.share.SharePlaylist;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.service.PlayService;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SplashActivity extends AppCompatActivity {

    public static boolean isFromDeep = false;
    public static boolean isFromDeepLink = false;
    public static boolean isFromSubscribe = false;
    public static boolean isFromSong = false;
    public static boolean isFromArtist = false;
    public static boolean isFromPlaylist = false;
    private ActivitySplashBinding binding;
    private Handler handler;
    private SharedPreferences preferences;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        preferences = getApplicationContext().getSharedPreferences("login", 0);
        //Fabric.with(this, new Crashlytics());
        Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)  // Enables Crashlytics debugger
                .build();
        Fabric.with(fabric);
        //int x = Integer.parseInt("df");
        context = this;
        Utility.setAppLanguage(context);
        Utility.setBooleanPreferences(context, "checkUpdateAvailability", true);
        UserModel.getInstance().getAppSetting(SplashActivity.this);

        if (!Utility.getBooleaPreferences(context, "appInstall")) {
            appDownloadCount();
        }
        if (isFromDeep) {
            UserModel.getInstance().comeFromDeep = false;
            if (getIntent().getStringExtra("type") != null) {
                if (getIntent().getStringExtra("type").equalsIgnoreCase("subscribe")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        startActivity(new Intent(context, YourSubscriptionActivity.class));
                        finish();
                    } else {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivityForResult(intent, 2);
                        isFromSubscribe = true;
                    }
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("song")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        startActivity(new Intent(context, MainActivity.class).putExtra("id", getIntent().getStringExtra("song")));
                        finish();
                    } else {
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("song");
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        isFromSong = true;
                    }
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("album")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        getAlbumById(getIntent().getStringExtra("album"));
                    } else {
                        UserModel.getInstance().isAlbumSharing = true;
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("album");
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                        isFromDeepLink = true;
                    }
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("artist")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        UserModel.getInstance().openFragment = "BROWSE";
                        getArtistDetail(getIntent().getStringExtra("artist"));
                    } else {
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("artist");
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                        isFromArtist = true;
                    }
                } else {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        getPlaylistDetail(getIntent().getStringExtra("playlist"));
                    } else {
                        UserModel.getInstance().isAlbumSharing = false;
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("playlist");
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                        isFromPlaylist = true;
                    }
                }
            } else {
                if (UserModel.getInstance().notificationType.equalsIgnoreCase("song")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        startActivity(new Intent(context, MainActivity.class).putExtra("id", UserModel.getInstance().notificationId));
                        finish();
                    } else {
                        UserModel.getInstance().albumOrPlaylistId = UserModel.getInstance().notificationId;
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        isFromSong = true;
                    }
                } else if (UserModel.getInstance().notificationType.equalsIgnoreCase("album")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        getAlbumById(UserModel.getInstance().notificationId);
                    } else {
                        UserModel.getInstance().isAlbumSharing = true;
                        UserModel.getInstance().albumOrPlaylistId = UserModel.getInstance().notificationId;
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        isFromDeepLink = true;
                    }
                } else if (UserModel.getInstance().notificationType.equalsIgnoreCase("artist")) {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        UserModel.getInstance().openFragment = "BROWSE";
                        getArtistDetail(UserModel.getInstance().notificationId);
                        finish();
                    } else {
                        UserModel.getInstance().albumOrPlaylistId = UserModel.getInstance().notificationId;
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                        isFromArtist = true;
                    }
                } else {
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        UserModel.getInstance().checkUser(context);
                        getPlaylistDetail(UserModel.getInstance().notificationId);
                    } else {
                        UserModel.getInstance().isAlbumSharing = false;
                        UserModel.getInstance().albumOrPlaylistId = UserModel.getInstance().notificationId;
                        UserModel.getInstance().isForSharing = true;
                        Intent intent = new Intent(context, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                        isFromPlaylist = true;
                    }
                }
            }
        } else {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().gc();
                    if (Utility.getBooleaPreferences(SplashActivity.this, Utility.IS_LOGIN)) {
                        Constants.setLoggedUser(true, preferences.getString("userid", ""), Utility.getUserInfo(SplashActivity.this).deviceID);
                        if (Utility.getUserInfo(context).genresPrefrences.genres.size() <= 2) {
                            Intent i = new Intent(context, FilterMusicActivity.class);
                            i.putExtra("isFrom", "sign_up");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            UserModel.getInstance().openFragment = "BROWSE";
                            startActivity(new Intent(SplashActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
                        }
                        getdata();
                    } else {
                        //AKM
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                }
            }, 3000);
        }
    }

    //Get User detail
    private void getdata() {
        Call<UserModel> call = Constants.service.getUserInfo(preferences.getString("userid", ""));
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel userModel = response.body();
                try {
                    if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(SplashActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (userModel.user != null) {
                            if (userModel.success) {
                                if (userModel.user.isTrialTaken == null){
                                    userModel.user.isTrialTaken = 0;
                                }
                                Utility.setUserInfo(getApplicationContext(), userModel.user);
                            }
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

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }


    //put app download count for unique user
    private void appDownloadCount() {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("deviceType", "android");
        Call<AppCount> call = Constants.service.appInstall(hm);
        call.enqueue(new Callback<AppCount>() {
            @Override
            public void onResponse(Call<AppCount> call, Response<AppCount> response) {
                Utility.setBooleanPreferences(context, "appInstall", true);
            }

            @Override
            public void onFailure(Call<AppCount> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Get album detail with song for share album
    private void getAlbumById(String id) {
        Constants.setLoggedUser(true, preferences.getString("userid", ""), Utility.getUserInfo(SplashActivity.this).deviceID);
        Call<ShareAlbum> call = Constants.service.getAlbumById(id);
        call.enqueue(new Callback<ShareAlbum>() {
            @Override
            public void onResponse(Call<ShareAlbum> call, Response<ShareAlbum> response) {
                if (response.isSuccessful()) {
                    ShareAlbum shareAlbum = response.body();
                    if (shareAlbum != null) {
                        if (shareAlbum.message.equalsIgnoreCase("Invalid device login.")) {
                            UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("album");
                            UserModel.getInstance().isForSharing = true;
                            Utility.openSessionOutDialog(context);
                        } else {
                            if (shareAlbum.success) {
                                UserModel.getInstance().openFragment = "BROWSE";
                                if(shareAlbum.album.isPremium){
                                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid")||UserModel.getInstance().isForRenew||UserModel.getInstance().isForTrial) {
                                        try {
                                            UserModel.getInstance().album = shareAlbum.album;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(new Intent(context, AlbumDetailActivity.class));
                                        finish();
                                    }else {
                                        startActivity(new Intent(context, MainActivity.class).putExtra("album", true));
                                        finish();
                                    }
                                }else{
                                    try {
                                        UserModel.getInstance().album = shareAlbum.album;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(context, AlbumDetailActivity.class));
                                    finish();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ShareAlbum> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Get Artist Detail
    public void getArtistDetail(String artist_id) {
        Call<ArtistInfo> call = Constants.service.getArtistNew(artist_id);
        call.enqueue(new Callback<ArtistInfo>() {
            @Override
            public void onResponse(Call<ArtistInfo> call, Response<ArtistInfo> response) {
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
                                        startActivity(new Intent(context, MainActivity.class).putExtra("artist", true));
                                        finish();
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
                t.printStackTrace();
            }
        });
    }

    //Get playlist detail with song for share playlist
    private void getPlaylistDetail(String id) {
        Constants.setLoggedUser(true, preferences.getString("userid", ""), Utility.getUserInfo(SplashActivity.this).deviceID);
        Call<SharePlaylist> call = Constants.service.getSongByPlaylistId(id);
        call.enqueue(new Callback<SharePlaylist>() {
            @Override
            public void onResponse(Call<SharePlaylist> call, Response<SharePlaylist> response) {
                if (response.isSuccessful()) {
                    SharePlaylist playlist = response.body();
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("playlist");
                        UserModel.getInstance().isForSharing = true;
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (playlist.success) {
                            UserModel.getInstance().openFragment = "BROWSE";
                            if (playlist.playlists.songs.size() > 0) {
                                UserModel.getInstance().shirali = playlist.playlists;
                            }
                            startActivity(new Intent(context, PlaylistActivity.class));
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SharePlaylist> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == resultCode) {
            startActivity(new Intent(context, YourSubscriptionActivity.class));
            finish();
            isFromSubscribe = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utility.isServiceRunning(SongPlayService.class.getName(), SplashActivity.this)) {
            Intent playIntent = new Intent(this, SongPlayService.class);
            startService(playIntent);
            Constants.isHomeScreenPlayerVisible = false;
        }else{
            Constants.isHomeScreenPlayerVisible = true;
        }
        if (!Utility.isServiceRunning(PlayService.class.getName(), SplashActivity.this)) {
            Intent playerIntent = new Intent(this, PlayService.class);
            startService(playerIntent);
        }
    }
}
