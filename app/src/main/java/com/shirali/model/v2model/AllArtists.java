package com.shirali.model.v2model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.RelatedArtist;

import java.util.ArrayList;

/**
 * Created by Sagar on 16/5/18.
 */

public class AllArtists {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("discover")
    @Expose
    public ArrayList<RelatedArtist> discover;
    @SerializedName("artists")
    @Expose
    public ArrayList<Artists> artists;
}
