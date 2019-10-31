package com.shirali.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.R;
import com.shirali.controls.Controls;
import com.shirali.databinding.ActivityYourSubscriptionBinding;
import com.shirali.model.comman.AppCount;
import com.shirali.model.user.PaymentInfo;
import com.shirali.model.user.UpdatePayment;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.model.user.UserSubscription;
import com.shirali.util.Constants;
import com.shirali.util.Utility;
import com.shirali.widget.progress.CustomLoaderDialog;

import java.text.DecimalFormat;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YourSubscriptionActivity extends BaseActivity implements View.OnClickListener {

    BroadcastReceiver finish_ad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private ActivityYourSubscriptionBinding binding;
    private String monthSubString, yearSubsString;
    private CustomLoaderDialog dialog;
    private Context context;
    private User userData;
    private boolean isChangeSubscription = false;
    private MixpanelAPI mixpanelAPI;
    private double monthPrice = 0;
    private double yearPrice = 0;
    private boolean isCancelSubscription = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_your_subscription);
        context = this;
        dialog = new CustomLoaderDialog(context);
        mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        overridePendingTransition(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
        userData = Utility.getUserInfo(context);

        try {
            monthSubString = String.valueOf(Utility.getUserSetting(YourSubscriptionActivity.this).monthlySubscriptionPrice);
            yearSubsString = String.valueOf(Utility.getUserSetting(YourSubscriptionActivity.this).yearlySubscriptionPrice);

            monthPrice = Double.parseDouble(monthSubString);
            yearPrice = Double.parseDouble(yearSubsString);

            binding.lblMonthly.setText("$" + new DecimalFormat("0.00").format(monthPrice));
            binding.lblYearly.setText("$" + new DecimalFormat("0.00").format(yearPrice));
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.changeMembership.setOnClickListener(this);
        binding.updateCard.setOnClickListener(this);
        binding.cancelSubscription.setOnClickListener(this);

        String freeTrial = getResources().getString(R.string.payments_free_text);
        freeTrial = freeTrial.replace("+priceMonthly+", "$" + new DecimalFormat("0.00").format(monthPrice));
        binding.lblFreeTrial.setText(freeTrial);
        String monthString = getResources().getString(R.string.payments_monthly_text);
        monthString = monthString.replace("+priceMonthly+", "$" + new DecimalFormat("0.00").format(monthPrice));
        binding.lblAllPaymentMonth.setText(monthString);
        String yearString = getResources().getString(R.string.payments_yearly_text);
        yearString = yearString.replace("+priceYearly+", "$" + new DecimalFormat("0.00").format(yearPrice));
        binding.lblAllPaymentYear.setText(yearString);

        binding.lblPrivacyUrl.setPaintFlags(binding.lblPrivacyUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.lblTermsUrl.setPaintFlags(binding.lblTermsUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        binding.lblPrivacyUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, WebViewActivity.class).putExtra("urls", "https://www.shiraliapp.com/privacy").putExtra("urls_name", "Privacy Policy"));
            }
        });
        binding.lblTermsUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, WebViewActivity.class).putExtra("urls", "https://www.shiraliapp.com/terms").putExtra("urls_name", "Terms of Use"));
            }
        });

        if (userData.getTrialTokan() == 0) { // || ( data.subscribePlan.plantype != null && data.subscribePlan.plantype.equalsIgnoreCase("Free"))) {
            binding.afterTrial.setVisibility(View.GONE);
            binding.beforeTrial.setVisibility(View.VISIBLE);
            binding.lblSelectPlan.setText(getResources().getString(R.string.choose_your_subscription));
            binding.lblHeader.setText(getResources().getString(R.string.subscription_options));
            binding.updateCard.setVisibility(View.GONE);
            binding.lytForPaid.setVisibility(View.GONE);
        } else if (userData.getTrialTokan() == 1 && (userData.subscribePlan.subscriptionId != null
                && userData.subscribePlan.subscriptionId.equalsIgnoreCase(""))){ // KIPL -> AKM: if user unsubscribe within trial period
            //t@yopmail >> planType = free >> Un subscription // tomorrow status need to check
            //u@yopmail.com > planType = Piad, with subsc id // tomorrow status need to check
            binding.afterTrial.setVisibility(View.GONE);
            binding.beforeTrial.setVisibility(View.VISIBLE);
            binding.lblSelectPlan.setText(getResources().getString(R.string.choose_your_subscription));
            binding.lblHeader.setText(getResources().getString(R.string.subscription_options));
            binding.updateCard.setVisibility(View.GONE);
            binding.lytForPaid.setVisibility(View.GONE);
        } else {
            if (UserModel.getInstance().isForTrial ) {
                binding.lytFreeTrial.setVisibility(View.GONE);
                binding.lytTrialRemember.setVisibility(View.VISIBLE);
                binding.updateCard.setVisibility(View.GONE);
                binding.lytForPaid.setVisibility(View.VISIBLE);
                binding.lblChangeSubscription.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.lytOffer.getLayoutParams();
                params.setMargins(0, 80, 0, 0);
                binding.lytOffer.setLayoutParams(params);
                int diff = 0;
                if (userData.subscribePlan.trailEndDate != null) {
                    diff = Utility.getDifferenceDays(Utility.convertDateFormat(Utility.getUserSetting(context).current_date), Utility.convertDateFormat(userData.subscribePlan.trailEndDate));
                }
                if (diff == 0) {
                    //binding.cancelSubscription.setVisibility(View.GONE);
                    if (userData.subscribePlan.plantype.equalsIgnoreCase("Free")) {
                        binding.lblDayRemainingText.setVisibility(View.GONE);
                        binding.lblDayRemaining.setText(getResources().getString(R.string.subscription_canceld));
                    } else {
                        binding.lblDayRemainingText.setVisibility(View.GONE);
                        binding.lblDayRemaining.setText(getResources().getString(R.string.subscription_start));
                    }
                } else {
                    binding.lblDayRemainingText.setVisibility(View.VISIBLE);
                    binding.lblDayRemaining.setText("" + diff);
                }
                if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                    binding.imgMonth.setVisibility(View.VISIBLE);
                    binding.imgIndAnual.setVisibility(View.GONE);
                } else {
                    binding.imgMonth.setVisibility(View.GONE);
                    binding.imgIndAnual.setVisibility(View.VISIBLE);
                }
                if (userData.subscribePlan.plantype.equalsIgnoreCase("Paid")) { // one day trial expire today condition "Free" came from server
                    if (diff == 0) {
                        binding.cancelSubscription.setVisibility(View.GONE);
                    } else {
                        binding.cancelSubscription.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.cancelSubscription.setVisibility(View.GONE);
                }
            } else {
                if (userData.subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew) {
                    binding.lblHeader.setText(getResources().getString(R.string.your_subscription));
                    binding.lytPaymentText.setVisibility(View.GONE);
                    binding.afterTrial.setVisibility(View.VISIBLE);
                    binding.beforeTrial.setVisibility(View.GONE);
                    if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                        binding.indYearLyt.setVisibility(View.GONE);
                    } else {
                        binding.lblChangeSubscription.setVisibility(View.INVISIBLE);
                        binding.indMonth.setVisibility(View.GONE);
                    }
                    if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                        binding.lblMembership.setText(R.string.monthly);
                    } else {
                        binding.lblMembership.setText(R.string.yearly);
                    }
                    if (!userData.subscribePlan.subscriptionRenewDate.equalsIgnoreCase("")) {
                        binding.lblExpireDate.setText(Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy"));
                    }
                    if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                        binding.imgMonth.setVisibility(View.VISIBLE);
                        binding.imgIndAnual.setVisibility(View.GONE);
                    } else {
                        binding.imgMonth.setVisibility(View.GONE);
                        binding.imgIndAnual.setVisibility(View.VISIBLE);
                    }
                    binding.lytForPaid.setVisibility(View.VISIBLE);
                    binding.lblFinal.setVisibility(View.GONE);
                    getPaymentInfo();
                    if (UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        binding.lytForPaid.setVisibility(View.VISIBLE);
                        binding.lblFinal.setVisibility(View.VISIBLE);
                        binding.lblChangeSubscription.setVisibility(View.GONE);
                        binding.cancelSubscription.setVisibility(View.GONE);
                        if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                            binding.lblFinal.setText(getString(R.string.your) + " " + getString(R.string.premium_subscription) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy"));
                        } else {
                            if (binding.indYearLyt.getVisibility() == View.VISIBLE) {
                                binding.lblChangeSubscription.setVisibility(View.INVISIBLE);
                            } else {
                                binding.lblChangeSubscription.setVisibility(View.VISIBLE);
                            }
                            binding.lblFinal.setText(getString(R.string.your) + " " + getString(R.string.premium_subscription) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy"));
                        }
                    }
                } else {
                    binding.lblSelectPlan.setText(getResources().getString(R.string.choose_your_subscription));
                    binding.lblHeader.setText(getResources().getString(R.string.subscription_options));
                    binding.updateCard.setVisibility(View.GONE);
                    binding.lytForPaid.setVisibility(View.GONE);
                    binding.beforeTrial.setVisibility(View.GONE);
                    binding.afterTrial.setVisibility(View.VISIBLE);
                    binding.lytPaymentText.setVisibility(View.GONE);
                }
            }
        }

        binding.imgBack.setOnClickListener(this);
        binding.indMonth.setOnClickListener(this);
        binding.indYearLyt.setOnClickListener(this);
        binding.changeMembership.setOnClickListener(this);
        binding.lytUserProfile.setOnClickListener(this);
        binding.lblChangeSubscription.setOnClickListener(this);
        binding.cancelSubscription.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                if (SplashActivity.isFromDeep) {
                    SplashActivity.isFromDeep = false;
                    UserModel.getInstance().openFragment = "BROWSE";
                    startActivity(new Intent(context, MainActivity.class));
                }
                finish();
                break;
            case R.id.subscribe:
                break;
            case R.id.ind_month:
                if (userData.subscribePlan.plantype.equalsIgnoreCase("Free")) {
                    if (UserModel.getInstance().isForRenew ||
                            (UserModel.getInstance().isForTrial && (userData.subscribePlan.subscriptionId != null && !userData.subscribePlan.subscriptionId.equalsIgnoreCase("")))) {
                        //Utility.showAlert(context, getString(R.string.already_subscribed));
                    } else {
                        Intent intent = new Intent(context, SubscriptionActivity.class);
                        intent.putExtra("amount", monthSubString);
                        intent.putExtra("plan", "monthly");
                        intent.putExtra("profile", "charge");
                        startActivity(intent);
                        //showDialogForSubscrpition(context, getResources().getString(R.string.individual_monthly), getResources().getString(R.string.premium_user_advantages));
                    }
                } else {
                    if (binding.imgMonth.getVisibility() == View.VISIBLE) {
                        Utility.showAlert(context, getString(R.string.already_subscribed));
                    } else {
                        mixpanelAPI.track("Individual-Monthly");
                        if (UserModel.getInstance().isForTrial) {

                        } else {
                            try {
                                if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                                    showDialog(context, getString(R.string.change_subscription), getString(R.string.your_monthly_plan) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + getString(R.string.your_yearly_plan) + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.please_confirm_switch), getString(R.string.switch_plan), getString(R.string.never_mind));
                                } else {
                                    showDialog(context, getString(R.string.change_subscription), getString(R.string.your_yearly_plan_cuurent) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + getString(R.string.your_monthly_plan_start) + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.please_confirm_switch), getString(R.string.switch_plan), getString(R.string.never_mind));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case R.id.indYearLyt:
                if (userData.subscribePlan.plantype.equalsIgnoreCase("Free")) {
                    if (UserModel.getInstance().isForRenew ||
                            (UserModel.getInstance().isForTrial && (userData.subscribePlan.subscriptionId != null && !userData.subscribePlan.subscriptionId.equalsIgnoreCase("")))) {
                        //Utility.showAlert(context, getString(R.string.already_subscribed));
                    } else {
                        Intent intent = new Intent(context, SubscriptionActivity.class);
                        intent.putExtra("amount", yearSubsString);
                        intent.putExtra("plan", "yearly");
                        intent.putExtra("profile", "charge");
                        startActivity(intent);
                        //showDialogForSubscrpition(context, getResources().getString(R.string.individual_annual), getResources().getString(R.string.premium_user_advantages));
                    }
                } else {
                    if (binding.imgIndAnual.getVisibility() == View.VISIBLE) {
                        Utility.showAlert(context, getString(R.string.already_subscribed));
                    } else {
                        mixpanelAPI.track("Individual-Annual");
                        if (UserModel.getInstance().isForTrial) {

                        } else {
                            try {
                                if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                                    showDialog(context, getString(R.string.change_subscription), getString(R.string.your_monthly_plan) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + getString(R.string.your_yearly_plan) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.please_confirm_switch), getString(R.string.switch_plan), getString(R.string.never_mind));
                                } else {
                                    showDialog(context, getString(R.string.change_subscription), getString(R.string.your_yearly_plan_cuurent) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + getString(R.string.your_monthly_plan_start) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.please_confirm_switch), getString(R.string.switch_plan), getString(R.string.never_mind));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case R.id.change_membership:
                binding.primeUser.setVisibility(View.GONE);
                binding.nonPrimeUser.setVisibility(View.VISIBLE);
                if (userData.subscribePlan.plantype.equalsIgnoreCase("Paid")) {
                    if (userData.subscribePlan.planId != null) {
                        if (userData.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                            binding.imgMonth.setVisibility(View.VISIBLE);
                            binding.imgIndAnual.setVisibility(View.GONE);
                        } else {
                            binding.imgMonth.setVisibility(View.GONE);
                            binding.imgIndAnual.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            case R.id.lytUserProfile:
                break;
            case R.id.updateCard:
                openMenu();
                break;
            case R.id.cancelSubscription:
                try {
                    if (UserModel.getInstance().isForTrial) {
                        showDialog(context, getString(R.string.cancel_subscription_in_trail), getString(R.string.free_trial_body), context.getResources().getString(R.string.yes_end_free_trial), getString(R.string.no_keep_it_going));
                    } else {
                        showDialog(context, getString(R.string.are_you_sure_cancel_subscription), getString(R.string.your_subscription_till) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.will_no_longer_have_access) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.will_not_renew), context.getResources().getString(R.string.yes_end), getString(R.string.never_mind));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.lblChangeSubscription:
                if (!isChangeSubscription) {
                    isChangeSubscription = true;
                    try {
                        showDialog(context, getString(R.string.change_subscription), Html.fromHtml(getString(R.string.your_yearly_plan_cuurent)) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + Html.fromHtml(getString(R.string.your_monthly_plan_start)) + " " + Utility.getFormatedDate(userData.subscribePlan.subscriptionRenewDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "MMMM dd, yyyy") + " " + getString(R.string.please_confirm_switch), context.getResources().getString(R.string.yes) + ", " + context.getResources().getString(R.string.yes_switch_plan), getString(R.string.never_mind));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (SplashActivity.isFromDeep) {
            SplashActivity.isFromDeep = false;
            UserModel.getInstance().openFragment = "BROWSE";
            startActivity(new Intent(context, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runtime.getRuntime().gc();
        Utility.isConnectingToInternet(YourSubscriptionActivity.this);
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
        LocalBroadcastManager.getInstance(context).unregisterReceiver(finish_ad);
        LocalBroadcastManager.getInstance(context).registerReceiver(finish_ad, new IntentFilter("finish_ad"));
    }

    //Get user card info
    private void getCardInfo(final Context context) {
        Call<UpdatePayment> call = Constants.service.getPayment(userData.id);
        call.enqueue(new Callback<UpdatePayment>() {
            @Override
            public void onResponse(Call<UpdatePayment> call, Response<UpdatePayment> response) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                UpdatePayment userModel = response.body();
                try {
                    if (userModel.message.equalsIgnoreCase("Invalid device login.")) {
                        try {
                            Utility.openSessionOutDialog(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (userModel.success) {
                            if (userModel.paymentMethod != null) {
                                UserModel.getInstance().card = userModel.paymentMethod.card;
                                startActivity(new Intent(YourSubscriptionActivity.this, SubscriptionActivity.class).putExtra("profile", "update_card"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UpdatePayment> call, Throwable t) {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                t.printStackTrace();
            }
        });
    }

    //Cancel user selected subscription
    public void cancelSubs() {
        Call<AppCount> call = Constants.service.cancelSubscription(userData.id);
        call.enqueue(new Callback<AppCount>() {
            @Override
            public void onResponse(Call<AppCount> call, Response<AppCount> response) {
                AppCount data = response.body();
                if (data.success) {
                    isCancelSubscription = true;
                    UserModel.getInstance().getdata(context);
                    showAlert(context, getString(R.string.cancel_subscription_success));
                }
            }

            @Override
            public void onFailure(Call<AppCount> call, Throwable t) {
                isCancelSubscription = false;
                t.printStackTrace();
            }
        });
    }

    private void showDialog(Context mContext, String titletext, String msgText, final String actionText, String neverMind) {
        final Dialog openDialog = new Dialog(mContext);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.subscription_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView msg = (TextView) openDialog.findViewById(R.id.lblMessage);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblNeverMind);
        final TextView action = (TextView) openDialog.findViewById(R.id.lblSwitch);
        title.setText(titletext);
        msg.setText(msgText);
        action.setText(actionText);
        cancle.setText(neverMind);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionText.equalsIgnoreCase(context.getResources().getString(R.string.yes) + ", " + context.getResources().getString(R.string.yes_switch_plan))) {
                    //isChangeSubscription = true;
                    try {
                        subscribePlan(userData.id, "yearly", Utility.getUserSetting(context).yearlySubscriptionPrice.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isFinishing()) {
                        openDialog.dismiss();
                    }
                } else {
                    if (!isCancelSubscription) {
                        isCancelSubscription = true;
                        cancelSubs();
                        if (!isFinishing()) {
                            openDialog.dismiss();
                        }
                    }
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChangeSubscription = false;
                openDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            openDialog.show();
        }
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);
    }

    private void showDialogForSubscrpition(Context mContext, final String titletext, String msgText) {
        final Dialog openDialog = new Dialog(mContext);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.subscription_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView msg = (TextView) openDialog.findViewById(R.id.lblMessage);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblNeverMind);
        final TextView action = (TextView) openDialog.findViewById(R.id.lblSwitch);
        title.setText(titletext);
        msg.setText(msgText);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titletext.equalsIgnoreCase(context.getResources().getString(R.string.individual_monthly))) {
                    Intent intent = new Intent(context, SubscriptionActivity.class);
                    intent.putExtra("amount", monthSubString);
                    intent.putExtra("plan", "monthly");
                    intent.putExtra("profile", "charge");
                    startActivity(intent);
                    if (!isFinishing()) {
                        openDialog.dismiss();
                    }
                } else {
                    Intent intent = new Intent(context, SubscriptionActivity.class);
                    intent.putExtra("amount", yearSubsString);
                    intent.putExtra("plan", "yearly");
                    intent.putExtra("profile", "charge");
                    startActivity(intent);
                    if (!isFinishing()) {
                        openDialog.dismiss();
                    }
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            openDialog.show();
        }
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);
    }

    //Get payment card info
    private void getPaymentInfo() {
        Call<PaymentInfo> call = Constants.service.getPayoutMethod(userData.id);
        call.enqueue(new Callback<PaymentInfo>() {
            @Override
            public void onResponse(Call<PaymentInfo> call, Response<PaymentInfo> response) {
                if (response.isSuccessful()) {
                    PaymentInfo info = response.body();
                    if (info.message.equalsIgnoreCase("Invalid device login.")) {
                        Utility.openSessionOutDialog(context);
                    } else {
                        if (info.success) {
                            binding.lblSelectPlan.setText(getResources().getString(R.string.your_subscription));
                            String brand = info.paymentMethod.card.brand.toLowerCase();
                            int checkExistence = getResources().getIdentifier(brand, "drawable", getPackageName());
                            if (checkExistence != 0) {
                                binding.imgCard.setImageResource(getResources().getIdentifier(brand, "drawable", getPackageName()));
                            } else {
                                binding.imgCard.setImageResource(R.drawable.ic_changecard);
                            }
                            binding.lblCardInfo.setText("• • • • • • " + info.paymentMethod.card.last4);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PaymentInfo> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void openMenu() {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.custom_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblCancel);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(R.string.change_or_update_card);
        action.setText(R.string.yes);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    dialog.show();
                }
                getCardInfo(YourSubscriptionActivity.this);
                openDialog.dismiss();
            }
        });
        cancle.setText(R.string.no);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
    }

    //Switch from monthly to yearly plan
    private void subscribePlan(String id, String plan, String fee) {
        HashMap<String, Object> hm = new HashMap<>();
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
                if (subscription.message.equalsIgnoreCase("Invalid device login.")) {
                    try {
                        Utility.openSessionOutDialog(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (subscription.success) {
                        isChangeSubscription = true;
                        if (subscription.result.subscribePlan.planId.equalsIgnoreCase("Shirali_Monthly_Subscription")) {
                            mixpanelAPI.track("Individual-Monthly-Done");
                        } else {
                            mixpanelAPI.track("Individual-Annual-Done");
                        }
                        Utility.setUserInfo(context, subscription.result);
                        UserModel.getInstance().getdata(context);
                        try {
                            final Dialog openDialog = new Dialog(context);
                            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            openDialog.setContentView(R.layout.alert_layout);
                            openDialog.setCancelable(false);
                            openDialog.setCanceledOnTouchOutside(false);
                            TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
                            TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
                            title.setText(context.getResources().getString(R.string.update_subscription));
                            action.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    context.startActivity(new Intent(context, MainActivity.class));
                                    finish();
                                    openDialog.dismiss();
                                }
                            });
                            openDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        isChangeSubscription = false;
                        Utility.showAlert(context, subscription.message);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserSubscription> call, Throwable t) {
                t.printStackTrace();
                isChangeSubscription = false;
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.no_animation, R.anim.fragment_slide_left_exit);
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
                startActivity(new Intent(context, MainActivity.class));
                finish();
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);
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
