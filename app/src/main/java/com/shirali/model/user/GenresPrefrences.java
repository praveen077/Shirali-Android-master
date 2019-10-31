package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sagar on 2/8/17.
 */

public class GenresPrefrences {

    @SerializedName("genres")
    @Expose
    public List<String> genres = null;
}
