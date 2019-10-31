package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 1/9/17.
 */

public class RelatedArtist {
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("avatar")
    @Expose
    public String avatar;
    @SerializedName("nameHebrew")
    @Expose
    public String nameHebrew;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium;
}

