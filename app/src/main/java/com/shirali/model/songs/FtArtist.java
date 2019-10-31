
package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FtArtist {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;

}
