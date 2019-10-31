package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.databinding.ActivityLoginTransactionBinding;
import com.shirali.model.share.ShareAlbum;
import com.shirali.model.share.SharePlaylist;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends BaseActivity {

    private ActivityLoginTransactionBinding binding;
    private Handler handler;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_transaction);
        binding.spiralIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in));
        mContext = this;

        UserModel.getInstance().getdata(LoadingActivity.this);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                UserModel.getInstance().openFragment = "BROWSE";
                if (UserModel.getInstance().isForSharing) {
                    UserModel.getInstance().checkUser(mContext);
                    UserModel.getInstance().isForSharing = false;
                    if (SplashActivity.isFromDeepLink) {
                        SplashActivity.isFromDeepLink = false;
                        getAlbumById(UserModel.getInstance().albumOrPlaylistId);
                    } else if (SplashActivity.isFromSong) {
                        SplashActivity.isFromSong = false;
                        startActivity(new Intent(mContext, MainActivity.class).putExtra("id", UserModel.getInstance().albumOrPlaylistId));
                        UserModel.getInstance().albumOrPlaylistId = "";
                    } else if (SplashActivity.isFromArtist) {
                        SplashActivity.isFromArtist = false;
                        getArtistDetail(UserModel.getInstance().albumOrPlaylistId);
                        UserModel.getInstance().albumOrPlaylistId = "";
                    } else if (SplashActivity.isFromPlaylist) {
                        SplashActivity.isFromPlaylist = false;
                        getPlaylistDetail(UserModel.getInstance().albumOrPlaylistId);
                    }
                } else {
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                }
                finish();
            }
        }, 4000);
    }

    //Get playlist detail with song for share playlist
    private void getPlaylistDetail(String id) {
        Call<SharePlaylist> call = Constants.service.getSongByPlaylistId(id);
        call.enqueue(new Callback<SharePlaylist>() {
            @Override
            public void onResponse(Call<SharePlaylist> call, Response<SharePlaylist> response) {
                if (response.isSuccessful()) {
                    SharePlaylist playlist = response.body();
                    if (playlist.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(mContext);
                    } else {
                        if (playlist.success) {
                            UserModel.getInstance().albumOrPlaylistId = "";
                            UserModel.getInstance().openFragment = "BROWSE";
                            if (playlist.playlists.songs.size() > 0) {
                                UserModel.getInstance().shirali = playlist.playlists;
                            }
                            startActivity(new Intent(mContext, PlaylistActivity.class));
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

    //Get album detail with song for share album
    private void getAlbumById(String id) {
        Call<ShareAlbum> call = Constants.service.getAlbumById(id);
        call.enqueue(new Callback<ShareAlbum>() {
            @Override
            public void onResponse(Call<ShareAlbum> call, Response<ShareAlbum> response) {
                ShareAlbum shareAlbum = response.body();
                if (shareAlbum != null) {
                    if (shareAlbum.message.equalsIgnoreCase("Invalid device login.")) {
                        UserModel.getInstance().albumOrPlaylistId = getIntent().getStringExtra("album");
                        UserModel.getInstance().isForSharing = true;
                        Utility.openSessionOutDialog(mContext);
                    } else {
                        if (shareAlbum.success) {
                            UserModel.getInstance().openFragment = "BROWSE";
                            if (shareAlbum.album.isPremium) {
                                if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    try {
                                        UserModel.getInstance().album = shareAlbum.album;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(mContext, AlbumDetailActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(mContext, MainActivity.class).putExtra("album", true));
                                    finish();
                                }
                            } else {
                                try {
                                    UserModel.getInstance().album = shareAlbum.album;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                startActivity(new Intent(mContext, AlbumDetailActivity.class));
                                finish();
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
                                Utility.openSessionOutDialog(mContext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (info.success) {
                                if (info.artist.isPremium) {
                                    if (Utility.getUserInfo(mContext).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                        mContext.startActivity(new Intent(mContext, ArtistDetailActivity.class).putExtra("artist_id", info.artist.id));
                                    } else {
                                        startActivity(new Intent(mContext, MainActivity.class).putExtra("artist", true));
                                    finish();
                                    }
                                } else {
                                    mContext.startActivity(new Intent(mContext, ArtistDetailActivity.class).putExtra("artist_id", info.artist.id));
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
}
