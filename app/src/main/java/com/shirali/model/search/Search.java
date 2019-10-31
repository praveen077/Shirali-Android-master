package com.shirali.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 9/8/17.
 */

public class Search {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public List<Song> songs = null;
    @SerializedName("tranding")
    @Expose
    public List<Tranding> tranding = null;

}