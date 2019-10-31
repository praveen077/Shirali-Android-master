package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sagar on 25/8/17.
 */

public class AllArtist {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("artists")
    @Expose
    public List<Artist> artist;
}
