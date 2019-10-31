package com.shirali.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 16/9/17.
 */

public class PlaylistDetail {

    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public Shirali songs = null;
}
