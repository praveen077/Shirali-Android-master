package com.shirali.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.databinding.ActivitySignUpBinding;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private MixpanelAPI mixpanelAPI;
    private CustomLoaderDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        context = this;
        dialog = new CustomLoaderDialog(context);
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        preferences = this.getSharedPreferences("login", 0);
        editor = preferences.edit();
        binding.tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixpanelAPI.track("Signup");
                if (binding.lblFirstName.getText().toString().equalsIgnoreCase("") && binding.lblLastName.getText().toString().equalsIgnoreCase("") && binding.lblEmail.getText().toString().equalsIgnoreCase("") && binding.lblPassword.getText().toString().equalsIgnoreCase("")) {
                    Utility.showAlert(context, getString(R.string.please_enter_complete_address));
                } else {
                    if (binding.lblFirstName.getText().toString().equalsIgnoreCase("")) {
                        binding.lblFirstName.setError(getString(R.string.enter_valid_first_name));
                    } else if (binding.lblLastName.getText().toString().equalsIgnoreCase("")) {
                        binding.lblLastName.setError(getString(R.string.enter_valid_last_name));
                    } else if (!emailValidator(binding.lblEmail.getText().toString())) {
                        binding.lblEmail.setError(getString(R.string.invalid_email));
                    } else if (binding.lblPassword.length() < 1) {
                        binding.lblPassword.setError(getString(R.string.invalid_password));
                    } else {
                        if(Utility.isConnectingToInternet(context)) {
                            if (!isFinishing()) {
                                if (dialog != null) {
                                    dialog.show();
                                }
                            }
                            signUp(binding.lblFirstName.getText().toString().trim(), binding.lblLastName.getText().toString().trim(),
                                    binding.lblEmail.getText().toString().trim(), binding.lblPassword.getText().toString().trim());
                        }
                    }
                }
            }
        });
        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.lblLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });
    }

    //For sign up
    public void signUp(String firstName, String lastName, String email, String password) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("username", firstName + " " + lastName);
        hm.put("email", email);
        hm.put("password", password);
        if (firstName.equalsIgnoreCase("") && lastName.equalsIgnoreCase("") && email.equalsIgnoreCase("") && password.equalsIgnoreCase("")) {
            Utility.showAlert(context, getString(R.string.please_enter_complete_address));
        } else {
            if (firstName.equalsIgnoreCase("")) {
                binding.lblFirstName.setError(getString(R.string.enter_valid_first_name));
            } else if (lastName.equalsIgnoreCase("")) {
                binding.lblLastName.setError(getString(R.string.enter_valid_last_name));
            } else if (!emailValidator(email)) {
                binding.lblEmail.setError(getString(R.string.invalid_email));
            } else {
                Constants.setLoggedUser(false, "", "");
                Call<UserModel> call = Constants.service.signUp(hm);
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        if (response.isSuccessful()) {
                            UserModel userModel = response.body();
                            if (userModel != null) {
                                if (userModel.success) {
                                    Utility.setBooleanPreferences(SignUpActivity.this, Utility.IS_LOGIN, true);
                                    editor.putString("userid", userModel.user.id);
                                    editor.commit();
                                    Utility.setUserInfo(getApplicationContext(), userModel.user);
                                    Constants.setLoggedUser(true, userModel.user.id, userModel.user.deviceID);
                                    Intent i = new Intent(SignUpActivity.this, FilterMusicActivity.class);
                                    i.putExtra("isFrom", "sign_up");
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                } else {
                                    Utility.showAlert(context, userModel.message);
                                }
                            }else{
                                UserModel model = new Gson().fromJson(response.errorBody().charStream(),UserModel.class);
                                if(model != null)
                                    Utility.showAlert(context, model.message);
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
                        Utility.showAlert(context, getResources().getString(R.string.something_went_wrong));
                        t.printStackTrace();
                    }
                });
            }
        }
    }

    public boolean emailValidator(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.isConnectingToInternet(SignUpActivity.this);
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

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }
}
