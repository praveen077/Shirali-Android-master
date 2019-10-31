package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.databinding.ActivityLoginBinding;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CustomLoaderDialog dialog;
    private Context mContext;
    private MixpanelAPI mixpanelAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mContext = this;
        mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        dialog = new CustomLoaderDialog(mContext);
        preferences = this.getSharedPreferences("login", 0);
        editor = preferences.edit();

        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
        binding.tvForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, PasswordRecoveryActivity.class);
                i.putExtra("isFrom", "Login");
                startActivity(i);

            }
        });
        binding.imgLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixpanelAPI.track("Login");
                if (binding.lblEmail.getText().toString().equalsIgnoreCase("") && !emailValidator(binding.lblEmail.getText().toString()) && binding.lblPassword.getText().toString().equalsIgnoreCase("")) {
                    Utility.showAlert(mContext, mContext.getResources().getString(R.string.enter_valid_email_password));

                } else {
                    if (!isFinishing()) {
                        dialog.show();
                    }
                    login(binding.lblEmail.getText().toString().trim(), binding.lblPassword.getText().toString().trim());
                }
            }
        });

    }

    //For user login
    public void login(String email, String password) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("email", email);
        hm.put("password", password);
        Constants.setLoggedUser(false, "", "");
        Call<UserModel> call = Constants.service.login(hm);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                UserModel userModel = response.body();
                if (userModel != null) {
                    if (userModel.success) {
                        Utility.setBooleanPreferences(LoginActivity.this, Utility.IS_LOGIN, true);
                        editor.putString("userid", userModel.user.id);
                        editor.putString("device_id", userModel.user.deviceID);
                        editor.commit();
                        if (userModel.user.isTrialTaken == null) {
                            userModel.user.isTrialTaken = 0;
                        }
                        Utility.setUserInfo(getApplicationContext(), userModel.user);
                        Constants.setLoggedUser(true, userModel.user.id, userModel.user.deviceID);
                        if (SplashActivity.isFromSubscribe) {
                            setResult(2);
                            finish();
                        } else {
                            if (Utility.getUserInfo(mContext).genresPrefrences.genres.size() <= 2) {
                                Intent i = new Intent(mContext, FilterMusicActivity.class);
                                i.putExtra("isFrom", "sign_up");
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(LoginActivity.this, LoadingActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        }
                    } else {
                        binding.imgLoginButton.setEnabled(true);
                        //Utility.showAlert(mContext, getString(R.string.failed_login));
                        Utility.showAlert(mContext, userModel.message);
                    }
                } else {
                    /* --- KIPL -> AKM: Manage Server Message if avaialble else hard coded for imvalid email & password---*/
                    UserModel model = new Gson().fromJson(response.errorBody().charStream(),UserModel.class);
                    if(model != null)
                        Utility.showAlert(mContext, model.message);
                    else
                        Utility.showAlert(mContext, mContext.getResources().getString(R.string.invalid_email_password));
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                binding.imgLoginButton.setEnabled(true);
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Utility.showAlert(mContext, getResources().getString(R.string.something_went_wrong));
                t.printStackTrace();
            }
        });
    }

    public boolean emailValidator(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!isFinishing()) {
            if (dialog != null) {
                dialog.show();
            }
        }
        binding.imgLoginButton.setEnabled(false);
        login(binding.lblEmail.getText().toString().trim(), binding.lblPassword.getText().toString().trim());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.isConnectingToInternet(LoginActivity.this);
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
