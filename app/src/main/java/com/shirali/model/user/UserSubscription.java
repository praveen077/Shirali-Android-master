package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 22/8/17.
 */

public class UserSubscription {

    @SerializedName("success")
    @Expose
    public boolean success;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("result")
    @Expose
    public User result;
}
