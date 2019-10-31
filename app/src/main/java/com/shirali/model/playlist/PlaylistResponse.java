package com.shirali.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Pankaj on 10/8/17.
 */

public class PlaylistResponse {
    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("playlist")
    @Expose
    public List<Shirali> songs = null;

    public String getMessage() {
        if (message == null) {
            return "";
        }
        return message;
    }

}



