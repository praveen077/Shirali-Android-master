package com.shirali.model.share;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.playlist.Shirali;

import java.util.List;

/**
 * Created by Sagar on 19/9/17.
 */

public class SharePlaylist {

        @SerializedName("success")
        @Expose
        public Boolean success;
        @SerializedName("message")
        @Expose
        public String message;
        @SerializedName("songs")
        @Expose
        public Shirali playlists;
}
