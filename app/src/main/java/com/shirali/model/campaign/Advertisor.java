package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 1/9/17.
 */

public class Advertisor {

    @SerializedName("customerId")
    @Expose
    public String customerId;
    @SerializedName("updatedDate")
    @Expose
    public String updatedDate;
    @SerializedName("isDifferentBillingAddress")
    @Expose
    public Boolean isDifferentBillingAddress;
    @SerializedName("billingAddress")
    @Expose
    public BillingAddress billingAddress;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("name")
    @Expose
    public String name;
}
