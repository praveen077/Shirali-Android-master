package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import com.shirali.R;
import com.shirali.adapter.GenresAdapter;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityFilterMusicBinding;
import com.shirali.model.songs.Genre;
import com.shirali.model.songs.GenresList;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterMusicActivity extends BaseActivity {

    private ActivityFilterMusicBinding binding;
    private ArrayList<Genre> genre;
    private GenresAdapter generAdapter;
    private ArrayList<String> listGener;
    private CustomLoaderDialog dialog;
    private int selectedGenres;
    private String isFrom;
    private boolean isVisible = true;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter_music);
        context = this;
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        genre = new ArrayList<>();
        listGener = new ArrayList<>();
        isFrom = getIntent().getStringExtra("isFrom");
        if(Utility.isConnectingToInternet(context)) {
            dialog = new CustomLoaderDialog(context);
            if (!isFinishing()) {
                dialog.show();
            }
            getGenreData();
        }
        selectedGenres = Utility.getUserInfo(FilterMusicActivity.this).genresPrefrences.genres.size();

        if (Utility.getUserInfo(this).blockedGenres.size() > 0) {
            listGener.addAll(Utility.getUserInfo(this).blockedGenres);
        }

        if (isFrom.equalsIgnoreCase("setting")) {
            if (selectedGenres > 0) {
                binding.tvDone.startAnimation(AnimationUtils.loadAnimation(FilterMusicActivity.this, R.anim.bottom_up));
                binding.tvDone.setVisibility(View.VISIBLE);
            } else {
                binding.tvDone.startAnimation(AnimationUtils.loadAnimation(FilterMusicActivity.this, R.anim.bottom_down));
                binding.tvDone.setVisibility(View.GONE);
            }
        } else {
            binding.lblHeaderTitle.setText(getResources().getString(R.string.select_your_favorite_genres));
            binding.lblNotes.setText(getResources().getString(R.string.customize_your_listening_experience_by_selecting_at_least_three_genres_of_your_favorite_music));
        }

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.show();
                    }
                }
                if (isFrom.equalsIgnoreCase("setting")) {
                    setBlockedGenre(listGener);
                } else {
                    putGenresRequest(listGener);
                }
            }
        });
    }

    //Get all genres
    private void getGenreData() {
        Call<GenresList> filterMusicModelCall = Constants.service.getGenres("true");
        filterMusicModelCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                GenresList listGenre = response.body();
                try {
                    if (listGenre.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(FilterMusicActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (listGenre.success) {
                            genre.addAll(response.body().genres);
                            binding.recycleFilter.setNestedScrollingEnabled(false);
                            binding.recycleFilter.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
                            generAdapter = new GenresAdapter(context, genre, false, listGener);
                            binding.recycleFilter.setAdapter(generAdapter);

                            generAdapter.setOnItemClickListener(new GenresAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    if (listGener.size() == 0) {
                                        listGener.add(genre.get(position).id);
                                    } else {
                                        if (listGener.contains(genre.get(position).id)) {
                                            listGener.remove(genre.get(position).id);
                                            selectedGenres = selectedGenres - 1;
                                        } else {
                                            listGener.add(genre.get(position).id);
                                            selectedGenres = selectedGenres + 1;
                                        }
                                    }

                                    //for display done button
                                    if (isFrom.equalsIgnoreCase("sign_up")) {
                                        if (listGener.size() > 2) {
                                            if (isVisible) {
                                                isVisible = false;
                                                binding.tvDone.startAnimation(AnimationUtils.loadAnimation(FilterMusicActivity.this, R.anim.bottom_up));
                                                binding.tvDone.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            isVisible = true;
                                            /* --- KIPL -> AKM : Animation issue of bottom view ---*/
                                            if(binding.tvDone.getVisibility() == View.VISIBLE)
                                                binding.tvDone.startAnimation(AnimationUtils.loadAnimation(FilterMusicActivity.this, R.anim.bottom_down));
                                            binding.tvDone.setVisibility(View.GONE);
                                        }
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
            public void onFailure(Call<GenresList> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();

            }
        });
    }

    //Put selected genres into user favourite preferences list
    private void putGenresRequest(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("genres", list);
        HashMap<String, Object> hm2 = new HashMap<>();
        hm2.put("preferences", hm);

        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm2);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                try {
                    if (user.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(FilterMusicActivity.this);
                    } else {
                        if (user.success) {
                            if (!isFinishing()) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                            Utility.setUserInfo(FilterMusicActivity.this, user.user);
                            if (isFrom.equalsIgnoreCase("sign_up")) {
                                startActivity(new Intent(FilterMusicActivity.this, LoadingActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            } else {
                                finish();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Put selected genres into user un favourite preferences list
    private void setBlockedGenre(ArrayList<String> list) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("blockedGenres", list);

        Call<UserModel> call = Constants.service.updateGenres(Utility.getUserInfo(this).id, hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel user = response.body();
                UserModel.getInstance().getGenreData(context, null);
                if (user.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(FilterMusicActivity.this);
                } else {
                    if (user.success) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        Utility.setUserInfo(FilterMusicActivity.this, user.user);
                        finish();
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
                t.printStackTrace();
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
        Utility.isConnectingToInternet(FilterMusicActivity.this);
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
}
