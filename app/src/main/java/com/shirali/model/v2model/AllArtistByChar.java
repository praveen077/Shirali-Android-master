package com.shirali.model.v2model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.RelatedArtist;

import java.util.ArrayList;

/**
 * Created by Sagar on 17/5/18.
 */

public class AllArtistByChar {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("artists")
    @Expose
    public ArrayList<Artist> artists;
}
