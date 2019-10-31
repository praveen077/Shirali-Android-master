package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 2/8/17.
 */

public class RecentAlbum {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("artist")
    @Expose
    public Artist artist;
    @SerializedName("artwork")
    @Expose
    public String artwork;
    @SerializedName("title")
    @Expose
    public String title;
}