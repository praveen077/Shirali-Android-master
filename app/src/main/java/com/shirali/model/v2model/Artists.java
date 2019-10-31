package com.shirali.model.v2model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Artist;

import java.util.ArrayList;

/**
 * Created by Sagar on 16/5/18.
 */

public class Artists {

    @SerializedName("char")
    @Expose
    public String chara;
    @SerializedName("count")
    @Expose
    public int count;
    @SerializedName("artist")
    @Expose
    public ArrayList<Artist> artist;
}
