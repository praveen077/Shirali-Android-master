package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Sagar on 22/8/17.
 */

public class Stripe {

    @SerializedName("paymentMethods")
    @Expose
    public ArrayList<String> paymentMethods;
    @SerializedName("customerId")
    @Expose
    public String customerId;
}
