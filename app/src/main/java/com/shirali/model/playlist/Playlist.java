package com.shirali.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sagar on 4/8/17.
 */

public class Playlist {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("playlists")
    @Expose
    public List<Shirali> playlists;

}
