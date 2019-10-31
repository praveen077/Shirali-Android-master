package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 29/9/17.
 */

public class Source {

    @SerializedName("brand")
    @Expose
    public String brand;
    @SerializedName("last4")
    @Expose
    public String last4;

}
