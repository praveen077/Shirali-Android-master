package com.shirali.model.radio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RadioTempData {

    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("artist")
    @Expose
    public String artist = "";
    @SerializedName("artwork")
    @Expose
    public String artwork = "";
}
