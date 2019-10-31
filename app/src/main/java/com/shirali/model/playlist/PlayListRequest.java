package com.shirali.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Created by Sagar on 11/8/17.
 */

public class PlayListRequest {

    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("playlist")
    @Expose
    public Shirali songs = null;
}
