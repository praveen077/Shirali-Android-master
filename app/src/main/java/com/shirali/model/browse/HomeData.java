package com.shirali.model.browse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Artist;

/**
 * Created by Sagar on 15/5/18.
 */

public class HomeData {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("artwork")
    @Expose
    public String artwork;
    @SerializedName("titleHebrew")
    @Expose
    public String titleHebrew;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("isPremium")
    @Expose
    public boolean isPremium;
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("artist")
    @Expose
    public Artist artist;
}
