package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Sagar on 22/8/17.
 */

public class Subscription {

    @SerializedName("plantype")
    @Expose
    public String plantype;
    @SerializedName("subscriptionDate")
    @Expose
    public String subscriptionDate;
    @SerializedName("subscriptionRenewDate")
    @Expose
    public String subscriptionRenewDate;
    @SerializedName("amount")
    @Expose
    public Double amount;
    @SerializedName("trailEndDate")
    @Expose
    public String trailEndDate;
    @SerializedName("subscriptionPaymentDate")
    @Expose
    public String subscriptionPaymentDate;
    @SerializedName("planId")
    @Expose
    public String planId;
    @SerializedName("subscriptionId")
    @Expose
    public String subscriptionId;
}
