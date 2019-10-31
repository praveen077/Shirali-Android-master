package com.shirali.activity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivitySubscriptionBinding;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.model.user.UserSubscription;
import com.shirali.receiver.TimerBroadcast;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends BaseActivity {

    private ActivitySubscriptionBinding binding;
    private String ammount;
    private String plan;
    private com.shirali.model.user.Card card;
    private Context context;
    private CustomLoaderDialog dialog;
    private MixpanelAPI mixpanelAPI;
    private String data;
    private boolean isSubscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription);
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        context = this;
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        ammount = getIntent().getStringExtra("amount");
        plan = getIntent().getStringExtra("plan");

        if (getIntent().hasExtra("profile")) {
            data = getIntent().getStringExtra("profile");
        } else {
            return;
        }

        if (data.equalsIgnoreCase("subscribe")) {
            if (plan.equalsIgnoreCase("monthly")) {
                binding.lblHeader.setText(R.string.individual_monthly);
            } else {
                binding.lblHeader.setText(R.string.individual_yearly);
            }
        } else if (data.equalsIgnoreCase("update_profile")) {
            binding.lytPayment.setVisibility(View.GONE);
            binding.lytProfile.setVisibility(View.VISIBLE);
            binding.lytNote.setVisibility(View.GONE);
            binding.lblHeader.setText(getResources().getString(R.string.update_profile));
            binding.btnSubscribe.setText(getResources().getString(R.string.update_profile));
            User user = Utility.getUserInfo(SubscriptionActivity.this);
            binding.lblUserFirstName.setText(user.firstName);
            binding.lblUserLastName.setText(user.lastName);
            binding.lblUserEmail.setText(user.email);
            binding.lblUserPassword.setHint("********");
        } else if (data.equalsIgnoreCase("update_card")) {
            binding.lytPayment.setVisibility(View.VISIBLE);
            binding.lytProfile.setVisibility(View.GONE);
            binding.btnSubscribe.setText(getResources().getString(R.string.update_card));
            binding.lblHeader.setText(getResources().getString(R.string.update_card));
            try {
                card = UserModel.getInstance().card;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String[] splited = card.name.split("\\s+");
                binding.lblFirstName.setText(splited[0]);
                binding.lblLastName.setText(splited[1]);
                binding.lblStreet.setText("" + card.addressLine1);
                binding.lblCity.setText("" + card.addressCity);
                binding.lblState.setText("" + card.addressState);
                binding.lblZip.setText("" + card.addressZip);
                binding.lblPhone.setText("");
                binding.lblCreditCardName.setText("" + card.name);
                binding.lblCardNumber.setText("" + card.last4);
                binding.lblExpire.setText("" + card.expMonth + "/" + "" + card.expYear);
                binding.lblCvv.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            binding.lytPayment.setVisibility(View.VISIBLE);
            binding.lytProfile.setVisibility(View.GONE);
            if (plan.equalsIgnoreCase("monthly")) {
                binding.lblHeader.setText(R.string.individual_monthly);
            } else {
                binding.lblHeader.setText(R.string.individual_yearly);
            }
        }
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utility.isConnectingToInternet(context)) {
                    if (!isSubscribed) {
                        isSubscribed = true;
                        dialog = new CustomLoaderDialog(context);
                        if (!isFinishing()) {
                            dialog.show();
                        }
                        if (getIntent().getStringExtra("profile").equalsIgnoreCase("update_card")) {
                            if (binding.lblCreditCardName.getText().toString().equalsIgnoreCase("") || binding.lblCardNumber.getText().toString().equalsIgnoreCase("") || binding.lblExpire.getText().toString().equalsIgnoreCase("") || binding.lblCvv.getText().toString().equalsIgnoreCase("")) {
                                Utility.showAlert(context, getString(R.string.all_field_are_needed));
                                if (!isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                isSubscribed = false;
                            } else if (binding.lblCreditCardName.getText().toString().equalsIgnoreCase("")) {
                                if (!isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                binding.lblCreditCardName.setError(getString(R.string.enter_valid_name));
                                isSubscribed = false;
                            } else if (binding.lblCardNumber.getText().toString().length() < 19) {
                                if (!isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                binding.lblCardNumber.setError(getString(R.string.enter_valid_card));
                                isSubscribed = false;
                            } else if (binding.lblExpire.getText().toString().indexOf("/") <= -1) {
                                if (!isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                binding.lblExpire.setError(getString(R.string.enter_valid_expiry));
                                isSubscribed = false;
                            } else if (binding.lblCvv.getText().toString().length() < 3) {
                                if (!isFinishing()) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                binding.lblCvv.setError(getString(R.string.enter_valid_cvv));
                                isSubscribed = false;
                            } else {
                                generateToken(binding.lblCardNumber.getText().toString(), binding.lblExpire.getText().toString(), binding.lblCvv.getText().toString());
                            }
                        } else {
                            if (binding.btnSubscribe.getText().toString().equalsIgnoreCase(getResources().getString(R.string.update_profile))) {
                                if (binding.lblUserFirstName.getText().toString().trim().equalsIgnoreCase("")
                                        || binding.lblUserLastName.getText().toString().trim().equalsIgnoreCase("")
                                        || binding.lblUserEmail.getText().toString().trim().equalsIgnoreCase("")) {
                                    Utility.showToast(SubscriptionActivity.this, getString(R.string.enter_valid_detail));
                                    isSubscribed = false;
                                } else if (binding.lblUserFirstName.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblUserFirstName.setError(getString(R.string.first_name_not_empty));
                                    isSubscribed = false;
                                    if (!isFinishing()) {
                                        dialog.dismiss();
                                    }
                                } else if (binding.lblUserLastName.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblUserLastName.setError(getString(R.string.last_name_not_empty));
                                    isSubscribed = false;
                                    if (!isFinishing()) {
                                        dialog.dismiss();
                                    }
                                } else if (!emailValidator(binding.lblUserEmail.getText().toString()) || binding.lblUserEmail.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblUserEmail.setError(getString(R.string.please_enter_valid_email));
                                    isSubscribed = false;
                                    if (!isFinishing()) {
                                        dialog.dismiss();
                                    }
                                } else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("firstName", binding.lblUserFirstName.getText().toString().trim());
                                    hashMap.put("lastName", binding.lblUserLastName.getText().toString().trim());
                                    hashMap.put("email", binding.lblUserEmail.getText().toString().trim());
                                    if (binding.lblUserPassword.getText().toString().trim().equalsIgnoreCase("")) {
                                        hashMap.put("password", "");
                                    } else {
                                        hashMap.put("password", binding.lblUserPassword.getText().toString().trim());
                                    }
                                    updateProfile(hashMap);
                                }
                            } else {
                                if (binding.lblFirstName.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblFirstName.setError("Enter your name");
                                } else if (binding.lblLastName.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblLastName.setError("please enter your last name");
                                } else if (binding.lblStreet.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblStreet.setError("please enter your street");
                                } else if (binding.lblCity.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblCity.setError("please enter your city");
                                } else if (binding.lblState.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblState.setError("please enter your state");
                                } else if (binding.lblZip.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblZip.setError("please enter your zip");
                                } else if (binding.lblPhone.getText().toString().trim().equalsIgnoreCase("")) {
                                    binding.lblPhone.setError("please enter your phone number");
                                }
                                if (binding.lblCreditCardName.getText().toString().trim().equalsIgnoreCase("") || binding.lblCardNumber.getText().toString().trim().equalsIgnoreCase("") || binding.lblExpire.getText().toString().trim().equalsIgnoreCase("") || binding.lblCvv.getText().toString().trim().equalsIgnoreCase("")) {
                                    Utility.showAlert(context, getString(R.string.all_field_are_needed));
                                    if (!isFinishing()) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                    isSubscribed = false;
                                } else if (binding.lblCreditCardName.getText().toString().trim().equalsIgnoreCase("")) {
                                    if (!isFinishing()) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                    binding.lblCreditCardName.setError(getString(R.string.enter_valid_name));
                                    isSubscribed = false;
                                } else if (binding.lblCardNumber.getText().toString().trim().length() < 19) {
                                    if (!isFinishing()) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                    binding.lblCardNumber.setError(getString(R.string.enter_valid_card));
                                    isSubscribed = false;
                                } else if (binding.lblExpire.getText().toString().indexOf("/") <= -1) {
                                    if (!isFinishing()) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                    binding.lblExpire.setError(getString(R.string.enter_valid_expiry));
                                    isSubscribed = false;
                                } else if (binding.lblCvv.getText().toString().length() < 3) {
                                    if (!isFinishing()) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                    binding.lblCvv.setError(getString(R.string.enter_valid_cvv));
                                    isSubscribed = false;
                                } else {
                                    generateToken(binding.lblCardNumber.getText().toString(), binding.lblExpire.getText().toString(), binding.lblCvv.getText().toString());
                                }
                            }
                        }
                    }
                }
            }
        });

        binding.lblCardNumber.addTextChangedListener(new TextWatcher() {
            int count = binding.lblCardNumber.getText().toString().length();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (count <= binding.lblCardNumber.getText().toString().length()
                        && (binding.lblCardNumber.getText().toString().length() == 4
                        || binding.lblCardNumber.getText().toString().length() == 9
                        || binding.lblCardNumber.getText().toString().length() == 14)) {
                    binding.lblCardNumber.setText(binding.lblCardNumber.getText().toString() + " ");
                    int pos = binding.lblCardNumber.getText().length();
                    binding.lblCardNumber.setSelection(pos);
                } else if (count >= binding.lblCardNumber.getText().toString().length()
                        && (binding.lblCardNumber.getText().toString().length() == 4
                        || binding.lblCardNumber.getText().toString().length() == 9
                        || binding.lblCardNumber.getText().toString().length() == 14)) {
                    binding.lblCardNumber.setText(binding.lblCardNumber.getText().toString().substring(0, binding.lblCardNumber.getText().toString().length() - 1));
                    int pos = binding.lblCardNumber.getText().length();
                    binding.lblCardNumber.setSelection(pos);
                }
                count = binding.lblCardNumber.getText().toString().length();

            }
        });

        binding.lblExpire.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String working = s.toString();
                boolean isValid = true;
                if (working.length() == 2 && before == 0) {
                    if (Integer.parseInt(working) < 1 || Integer.parseInt(working) > 12) {
                        isValid = false;
                    } else {
                        working += "/";
                        binding.lblExpire.setText(working);
                        binding.lblExpire.setSelection(working.length());
                    }
                } else if (working.length() == 5 && before == 0) {
                    String enteredYear = working.substring(3);
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100;
                    if (Integer.parseInt(enteredYear) < currentYear) {
                        isValid = false;
                    }
                } else if (working.length() == 7 && before == 0) {
                    String enteredYear = working.substring(3);
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    if (Integer.parseInt(enteredYear) < currentYear) {
                        isValid = false;
                    }
                } else if (working.length() != 7) {
                    isValid = false;
                }

                if (!isValid) {
                    binding.lblExpire.setError(getString(R.string.enter_valid_date));
                } else {
                    binding.lblExpire.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

        });

        binding.lblUserFirstName.addTextChangedListener(mTextWatcher);
        binding.lblUserLastName.addTextChangedListener(mTextWatcher);
        binding.lblUserEmail.addTextChangedListener(mTextWatcher);
        binding.lblUserPassword.addTextChangedListener(mTextWatcher);
        binding.lblCreditCardName.addTextChangedListener(mTextWatcher);
        binding.lblCardNumber.addTextChangedListener(mTextWatcher);
        binding.lblExpire.addTextChangedListener(mTextWatcher);
        binding.lblCvv.addTextChangedListener(mTextWatcher);
    }

    private void checkFieldEmptyOrNot() {
        if (getIntent().getStringExtra("profile").equalsIgnoreCase("update_profile")) {
            if (!binding.lblUserFirstName.getText().toString().equalsIgnoreCase("") &&
                    !binding.lblUserLastName.getText().toString().equalsIgnoreCase("") &&
                    !binding.lblUserEmail.getText().toString().equalsIgnoreCase("")) {
                binding.btnSubscribe.startAnimation(AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.bottom_up));
                binding.btnSubscribe.setVisibility(View.VISIBLE);
            } else {
                binding.btnSubscribe.startAnimation(AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.bottom_down));
                binding.btnSubscribe.setVisibility(View.GONE);
            }
        } else {
            if (!binding.lblCreditCardName.getText().toString().trim().equalsIgnoreCase("") &&
                    !binding.lblCardNumber.getText().toString().trim().equalsIgnoreCase("") &&
                    !binding.lblExpire.getText().toString().trim().equalsIgnoreCase("") &&
                    !binding.lblCvv.getText().toString().trim().equalsIgnoreCase("")) {
                binding.btnSubscribe.startAnimation(AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.bottom_up));
                binding.btnSubscribe.setVisibility(View.VISIBLE);
            } else {
                binding.btnSubscribe.startAnimation(AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.bottom_down));
                binding.btnSubscribe.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //Generate Stripe token for payment
    public void generateToken(String card, String date, String cvv) {
        Calendar calendar = Utility.setDateIntoCalenderAndGet(Utility.convertStringToDate(date, "MM/yyyy"));
        Card cardd = new Card(card, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR), cvv);
        if (!cardd.validateCard()) {
            Utility.showAlert(context, getResources().getString(R.string.bad_card));
            if (!isFinishing()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            isSubscribed = false;
            return;
        }
        if (!cardd.validateExpMonth()) {
            Utility.showAlert(context, getString(R.string.invalid_month));
            if (!isFinishing()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            isSubscribed = false;
            return;
        }
        if (!cardd.validateExpYear()) {
            Utility.showAlert(context, getString(R.string.invalid_year));
            if (!isFinishing()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            isSubscribed = false;
            return;
        }
        if (!cardd.validateCVC()) {
            Utility.showAlert(context, getString(R.string.invalid_cvv));
            if (!isFinishing()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            isSubscribed = false;
            return;
        }
        Stripe stripe = new Stripe(SubscriptionActivity.this);
        stripe.createToken(cardd, Constants.stripe_Publish_Live_Key, new TokenCallback() {
            @Override
            public void onError(Exception error) {
                isSubscribed = false;
                if (!isFinishing()) {
                    dialog.dismiss();
                }
                final Dialog openDialog = new Dialog(context);
                openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                openDialog.setContentView(R.layout.alert_layout);
                openDialog.setCancelable(false);
                openDialog.setCanceledOnTouchOutside(false);
                TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
                TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
                title.setText(R.string.problem_with_card);
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.lblCreditCardName.setText("");
                        binding.lblCardNumber.setText("");
                        binding.lblExpire.setText("");
                        binding.lblCvv.setText("");
                        openDialog.dismiss();
                    }
                });
                openDialog.show();
            }

            @Override
            public void onSuccess(Token token) {
                if (!isFinishing()) {
                    dialog.dismiss();
                }
                if (getIntent().getStringExtra("profile").equalsIgnoreCase("update_card")) {
                    updateCard(Utility.getUserInfo(context).id, token.getId());
                } else {
                    subscribePlan(Utility.getUserInfo(SubscriptionActivity.this).id, token.getId(), plan, ammount);
                }
            }
        });
    }

    //Subscribe user selected plan
    public void subscribePlan(String id, String token, String plan, String fee) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("token", token);
        hm.put("plantype", plan);
        hm.put("amount", fee);
        Call<UserSubscription> call = Constants.service.subscribeUser(id, hm);
        call.enqueue(new Callback<UserSubscription>() {
            @Override
            public void onResponse(Call<UserSubscription> call, Response<UserSubscription> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                UserSubscription subscription = response.body();
                try {
                    if (subscription.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(SubscriptionActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (subscription.success) {
                            if (subscription.result.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                                mixpanelAPI.track("Individual-Monthly-Done");
                            } else {
                                mixpanelAPI.track("Individual-Annual-Done");
                            }
                            Utility.setUserInfo(SubscriptionActivity.this, subscription.result);
                            UserModel.getInstance().getdata(context);
                            try {
                                final Dialog openDialog = new Dialog(context);
                                openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                openDialog.setContentView(R.layout.alert_layout);
                                openDialog.setCancelable(false);
                                openDialog.setCanceledOnTouchOutside(false);
                                TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
                                TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
                                title.setText(context.getResources().getString(R.string.you_are_officially_subscribed));
                                action.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!Constants.openSubsWindowFromAds) {
                                            context.startActivity(new Intent(context, MainActivity.class));
                                        } else {
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("finish_ad"));
                                        }
                                        finish();
                                        openDialog.dismiss();
                                    }
                                });
                                if (!isFinishing()) {
                                    openDialog.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Utility.setIntPreferences(context, "albumSkipCount", 0);
                            Utility.setIntPreferences(context, "timerPopupCount", 0);
                            Intent intent = new Intent(context, TimerBroadcast.class);
                            intent.setAction("playlist_action");
                            intent.setAction("album_action");
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            alarmManager.cancel(pendingIntent);
                        }else {
                            isSubscribed = false;
                            Utility.showAlert(context, subscription.message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserSubscription> call, Throwable t) {
                t.printStackTrace();
                Utility.showAlert(context, getResources().getString(R.string.something_went_wrong));
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                isSubscribed = false;
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(SubscriptionActivity.this);
        if (NewCampiagnActivity.isFromCampaign) {
            NewCampiagnActivity.isFromCampaign = false;
            if (UserModel.getInstance().isPlaySongAfterAd) {
                if (Constants.openSubsWindowFromAds) {
                    Controls.pauseControl(context);
                } else if (Constants.isChangeSong) {
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

    //Update user profile
    public void updateProfile(HashMap<String, Object> hashMap) {
        Call<UserModel> call = Constants.service.updateProfile(Utility.getUserInfo(SubscriptionActivity.this).id, hashMap);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                final UserModel model = response.body();
                if (model.message.equalsIgnoreCase("Invalid device login.")) {
                    Utility.openSessionOutDialog(context);
                } else {
                    if (model.success) {
                        if (model.user != null) {
                            final Dialog openDialog = new Dialog(context);
                            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            openDialog.setContentView(R.layout.alert_layout);
                            openDialog.setCancelable(false);
                            openDialog.setCanceledOnTouchOutside(false);
                            TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
                            TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
                            title.setText(model.message);
                            action.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Utility.setUserInfo(SubscriptionActivity.this, model.user);
                                    UserModel.getInstance().getdata(context);
                                    finish();
                                    openDialog.dismiss();
                                }
                            });
                            openDialog.show();
                            openDialog.setCancelable(false);
                            openDialog.setCanceledOnTouchOutside(false);
                        }
                    } else {
                        Utility.showAlert(context, model.message);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                isSubscribed = false;
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


    //Update user card info
    private void updateCard(String id, String tokenId) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("id", id);
        hm.put("token", tokenId);
        Call<UserSubscription> call = Constants.service.updatePayment(Utility.getUserInfo(context).id, hm);
        call.enqueue(new Callback<UserSubscription>() {
            @Override
            public void onResponse(Call<UserSubscription> call, Response<UserSubscription> response) {
                UserSubscription subscription = response.body();
                if (subscription != null) {
                    if (subscription.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (subscription.success) {
                            if (subscription.result != null) {
                                Utility.setUserInfo(SubscriptionActivity.this, subscription.result);
                                UserModel.getInstance().getdata(context);
                                final Dialog openDialog = new Dialog(context);
                                openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                openDialog.setContentView(R.layout.alert_layout);
                                openDialog.setCancelable(false);
                                openDialog.setCanceledOnTouchOutside(false);
                                TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
                                TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
                                title.setText(context.getResources().getString(R.string.update_card_text));
                                action.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(context, MainActivity.class));
                                        finish();
                                        openDialog.dismiss();
                                    }
                                });
                                if (!isFinishing()) {
                                    openDialog.show();
                                }
                            }
                        }else {
                            isSubscribed = false;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSubscription> call, Throwable t) {
                isSubscribed = false;
                Utility.showAlert(context, getResources().getString(R.string.something_went_wrong));
                t.printStackTrace();
            }
        });
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

    public boolean emailValidator(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkFieldEmptyOrNot();
        }
    };

}
