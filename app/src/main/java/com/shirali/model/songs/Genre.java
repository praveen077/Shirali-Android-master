
package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Genre {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("updatedDate")
    @Expose
    public Boolean updatedDate;
    @SerializedName("createdDate")
    @Expose
    public String createdDate;
    @SerializedName("deleted")
    @Expose
    public Boolean deleted;
    @SerializedName("isActive")
    @Expose
    public Boolean isActive;
    @SerializedName("icon")
    @Expose
    public String icon;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("titleHebrew")
    @Expose
    public String titleHebrew;
}
