package com.shirali.model.setting;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.util.Utility;

public class Setting {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("adDisplayInterval")
    @Expose
    public Integer adDisplayInterval;
    @SerializedName("skipping_timer")
    @Expose
    public Integer skipping_timer;
    @SerializedName("streamedTimeDelay")
    @Expose
    public Integer streamedTimeDelay;
    @SerializedName("fieldsPerPage")
    @Expose
    public Integer fieldsPerPage;
    @SerializedName("sendgridKey")
    @Expose
    public String sendgridKey;
    @SerializedName("sendgridSecretKey")
    @Expose
    public String sendgridSecretKey;
    @SerializedName("awsKey")
    @Expose
    public String awsKey;
    @SerializedName("awsSecretKey")
    @Expose
    public String awsSecretKey;
    @SerializedName("YearlySubscriptionPrice")
    @Expose
    public Double yearlySubscriptionPrice;
    @SerializedName("MonthlySubscriptionPrice")
    @Expose
    public Double monthlySubscriptionPrice;
    @SerializedName("AdRevenuePayoutPercentage")
    @Expose
    public Integer adRevenuePayoutPercentage;
    @SerializedName("SubscriptionPayoutPercentage")
    @Expose
    public Integer subscriptionPayoutPercentage;
    @SerializedName("underMaintananceMessage")
    @Expose
    public String underMaintananceMessage;
    @SerializedName("underMaintanance")
    @Expose
    public String underMaintanance;
    @SerializedName("terms_policy_url")
    @Expose
    public String termsPolicyUrl;
    @SerializedName("subscriptionPlanTypes")
    @Expose
    public List<String> subscriptionPlanTypes = null;
    @SerializedName("privacy_policy_url")
    @Expose
    public String privacyPolicyUrl;
    @SerializedName("playstorelink")
    @Expose
    public String playstorelink;
    @SerializedName("itunelink")
    @Expose
    public String itunelink;
    @SerializedName("iosLiveVersion")
    @Expose
    public String iosLiveVersion;
    @SerializedName("contactUs")
    @Expose
    public String contactUs;
    @SerializedName("androidLiveVersion")
    @Expose
    public String androidLiveVersion;
    @SerializedName("subsciption_trial_days")
    @Expose
    public Integer subsciption_trial_days;
    @SerializedName("current_date")
    @Expose
    public String current_date = "";
    @SerializedName("timeDeleyForRadioAdv")
    @Expose
    public int timeDeleyForRadioAdv = 1800;
    @SerializedName("timeForCount")
    @Expose
    public int timeForCount = 30;

    public String getCurrentTime() {
        if (current_date == null || current_date.equalsIgnoreCase("")) {
            String date = Utility.getCurrentTimeStamp();
            return date;
        } else {
            return current_date;
        }
    }

}
