package com.shirali.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 9/8/17.
 */

public class Tranding {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("totalSearchCount")
    @Expose
    public Integer totalSearchCount;
    @SerializedName("searchTerm")
    @Expose
    public String searchTerm;

}