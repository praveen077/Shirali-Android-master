package com.shirali.model.radio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Radio {

    @SerializedName("resultCount")
    @Expose
    public Integer resultCount;
    @SerializedName("results")
    @Expose
    public List<RadioArtwork> results = null;
}
