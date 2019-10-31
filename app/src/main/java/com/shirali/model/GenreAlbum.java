package com.shirali.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;

import java.util.List;

/**
 * Created by Sagar on 16/4/18.
 */

public class GenreAlbum {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("count")
    @Expose
    public String count;
    @SerializedName("album")
    @Expose
    public List<Album> albums = null;
}
