package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 1/9/17.
 */

public class Ads {

    @SerializedName("redirectUrl")
    @Expose
    public String redirectUrl;
    @SerializedName("adDuration")
    @Expose
    public int adDuration;
    @SerializedName("createDate")
    @Expose
    public String createDate;
    @SerializedName("updatedDate")
    @Expose
    public String updatedDate;
    @SerializedName("deleted")
    @Expose
    public Boolean deleted;
    @SerializedName("isActive")
    @Expose
    public Boolean isActive;
    @SerializedName("adArtworkUrl")
    @Expose
    public String adArtworkUrl;
    @SerializedName("adFileUrl")
    @Expose
    public String adFileUrl;
    @SerializedName("adFormat")
    @Expose
    public String adFormat;

}
