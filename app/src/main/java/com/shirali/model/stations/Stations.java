package com.shirali.model.stations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 8/5/18.
 */

public class Stations {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("link")
    @Expose
    public String link;
    @SerializedName("titleHebrew")
    @Expose
    public String titleHebrew;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("imageurl")
    @Expose
    public String imageurl = "";

}
