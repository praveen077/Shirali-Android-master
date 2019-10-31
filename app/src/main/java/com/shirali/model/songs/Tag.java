
package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tag {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("tagName")
    @Expose
    public String tagName;

}
