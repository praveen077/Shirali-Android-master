package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 1/9/17.
 */

public class Campaign_ {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("ads")
    @Expose
    public Ads ads;
    @SerializedName("advertisor")
    @Expose
    public Advertisor advertisor;

}
