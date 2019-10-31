
package com.shirali.model.songs;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongsList {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public ArrayList<Song> songs = null;

    public String getMessage() {
        if (message == null) {
            return "";
        }
        return message;
    }

}
