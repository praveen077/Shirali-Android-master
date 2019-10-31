package com.shirali.model.browse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 15/5/18.
 */

public class Banner {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("recid")
    @Expose
    public String recid;
    @SerializedName("imageUrl")
    @Expose
    public String imageUrl;
    @SerializedName("isPremium")
    @Expose
    public boolean isPremium = false;
}
