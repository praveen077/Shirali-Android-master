package com.shirali.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.databinding.ActivityRecoveryBinding;
import com.shirali.model.user.ForgotPassword;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordRecoveryActivity extends BaseActivity {

    private ActivityRecoveryBinding binding;
    private CustomLoaderDialog dialog;
    private String token;
    private Context context;
    private MixpanelAPI mixpanelAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recovery);
        context = this;
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        dialog = new CustomLoaderDialog(context);
        if (getIntent().getStringExtra("isFrom").equalsIgnoreCase("Login")) {
            binding.lytRecovery.setVisibility(View.VISIBLE);
            binding.lytConfirm.setVisibility(View.GONE);
            //Back button visible manage /* --- KIPL -> AKM ---*/
            binding.back.setVisibility(View.VISIBLE);
        } else {
            token = getIntent().getStringExtra("token");
            binding.lytRecovery.setVisibility(View.GONE);
            binding.lytConfirm.setVisibility(View.VISIBLE);
        }

        binding.imgGetLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixpanelAPI.track("Forgot My Password");
                if (!emailValidator(binding.lblEmail.getText().toString())) {
                    binding.lblEmail.setError(getString(R.string.please_enter_a_valid_email));
                } else {
                    passRecoveryMail(binding.lblEmail.getText().toString().trim());
                }
            }
        });
        binding.imgResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.lblNewPassword.getText().toString().trim().equalsIgnoreCase("")
                        && binding.lblConfirmPassword.getText().toString().trim().equalsIgnoreCase("")) {
                    Utility.showAlert(context, getString(R.string.match_the_password));
                } else {
                    if (binding.lblNewPassword.getText().toString().trim().equalsIgnoreCase(binding.lblConfirmPassword.getText().toString().trim())) {
                        /* --- KIPL -> AKM: Internet Check ---*/
                        if(Utility.isConnectingToInternet(context)) {
                            if (!isFinishing()) {
                                if (dialog != null) {
                                    dialog.show();
                                }
                            }
                            updatePassword(token, binding.lblNewPassword.getText().toString().trim());
                        }
                    } else {
                        Utility.showAlert(context, getString(R.string.match_the_password));
                    }
                }
            }
        });
        /* --- KIPL -> AKM : Activity finish handling ---*/
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void passRecoveryMail(String email) {
        if (!emailValidator(email)) {
            binding.lblEmail.setError(getString(R.string.please_enter_a_valid_email));
        } else {
            /* --- KIPL -> AKM: Internet check---*/
            if(Utility.isConnectingToInternet(context)) {
                if (!isFinishing()) {
                    dialog.show();
                }
                passwordRecover(email);
            }
        }
    }

    public boolean emailValidator(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Set password recovery email
    public void passwordRecover(String email) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("email", email);
        hm.put("source", "mobile");
        Call<ForgotPassword> call = Constants.service.forgotPassword(hm);
        call.enqueue(new Callback<ForgotPassword>() {
            @Override
            public void onResponse(Call<ForgotPassword> call, Response<ForgotPassword> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                ForgotPassword password = response.body();
                if (password.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(PasswordRecoveryActivity.this);
                } else {
                    if (password.success) {
                        Utility.showAlert(context, getResources().getString(R.string.reset_password_link));
                    } else {
                        Utility.showAlert(context, password.message);
                    }
                }
            }

            @Override
            public void onFailure(Call<ForgotPassword> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Update password
    private void updatePassword(String token, String password) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("token", token);
        hm.put("password", password);
        Call<ForgotPassword> call = Constants.service.updatePassword(hm);
        call.enqueue(new Callback<ForgotPassword>() {
            @Override
            public void onResponse(Call<ForgotPassword> call, Response<ForgotPassword> response) {
                ForgotPassword password = response.body();
                if (password != null) {
                    if (password.success) {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        showAlert(PasswordRecoveryActivity.this, getResources().getString(R.string.password_update));
                    } else {
                        if (!isFinishing()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        Utility.showAlert(PasswordRecoveryActivity.this, password.message);
                    }
                }else {
                    if (!isFinishing()) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    Utility.showAlert(context,getString(R.string.email_expire));
                }
            }

            @Override
            public void onFailure(Call<ForgotPassword> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.isConnectingToInternet(PasswordRecoveryActivity.this);
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Runtime.getRuntime().gc();
    }

    private void showAlert(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.alert_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(text);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PasswordRecoveryActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setCancelable(false);
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
