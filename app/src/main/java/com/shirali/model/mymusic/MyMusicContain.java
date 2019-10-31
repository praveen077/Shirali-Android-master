package com.shirali.model.mymusic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pankaj on 16/8/17.
 */

public class MyMusicContain {
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("myMusic")
    @Expose
    public ArrayList<Song> song = null;
}
