package com.shirali.model.recent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.RecentlyPlayed;
import com.shirali.model.songs.Song;

import java.util.ArrayList;

/**
 * Created by Sagar on 26/9/17.
 */

public class RecentlyPlay {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("recentlyPlayed")
    @Expose
    public ArrayList<Song> recentlyPlayed = null;
}
