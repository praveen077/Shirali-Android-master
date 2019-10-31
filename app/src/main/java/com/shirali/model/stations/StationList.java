package com.shirali.model.stations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sagar on 8/5/18.
 */

public class StationList {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("stations")
    @Expose
    public List<Stations> stations = null;

}