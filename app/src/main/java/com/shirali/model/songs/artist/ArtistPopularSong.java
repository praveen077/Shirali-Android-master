package com.shirali.model.songs.artist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 20/9/17.
 */

public class ArtistPopularSong {
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public List<Song> songs;
}
