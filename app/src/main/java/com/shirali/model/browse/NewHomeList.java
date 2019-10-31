package com.shirali.model.browse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 2/8/17.
 */

public class NewHomeList {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("recentlyPlayed")
    @Expose
    public List<HomeData> recentlyPlayed = null;
    @SerializedName("banners")
    @Expose
    public List<Banner> banner = null;
    @SerializedName("recommendations")
    @Expose
    public List<HomeData> recommendations = null;
    @SerializedName("newReleases")
    @Expose
    public List<HomeData> newReleases = null;
}
