package com.shirali.model.recent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 26/9/17.
 */

public class Recent {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("recentlyPlayed")
    @Expose
    public RecentlyPlay recentlyPlayed;
    @SerializedName("count")
    @Expose
    public Integer count;

}
