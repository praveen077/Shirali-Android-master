package com.shirali.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Song;

import java.util.List;

public class NewRelease {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public List<Song> songs = null;
    @SerializedName("albums")
    @Expose
    public List<Album> albums = null;
    @SerializedName("songsCount")
    @Expose
    public int songsCount;
    @SerializedName("albumsCount")
    @Expose
    public int albumsCount;

}