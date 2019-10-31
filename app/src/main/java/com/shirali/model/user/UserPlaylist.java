package com.shirali.model.user;

import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.Expose;
import com.shirali.model.playlist.Shirali;

import java.util.List;

public class UserPlaylist {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("playlist")
    @Expose
    public List<Shirali> playlists;

}
