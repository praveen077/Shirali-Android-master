package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


/**
 * Created by Pankaj on 2/8/17.
 */

public class GenresList {
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("genres")
    @Expose
    public ArrayList<Genre> genres;

}
