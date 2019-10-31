package com.shirali.model.share;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 19/9/17.
 */

public class ShareSong {

    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("url")
    @Expose
    public String url;
}
