package com.shirali.model.share;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;

/**
 * Created by Sagar on 19/9/17.
 */

public class ShareAlbum {

    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("album")
    @Expose
    public Album album;
}
