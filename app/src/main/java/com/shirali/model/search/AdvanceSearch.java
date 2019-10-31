package com.shirali.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 25/1/18.
 */

public class AdvanceSearch {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("topArtist")
    @Expose
    public List<Artist> topArtist = null;
    @SerializedName("artists")
    @Expose
    public List<Artist> artists = null;
    @SerializedName("albums")
    @Expose
    public List<Album> albums = null;
    @SerializedName("songs")
    @Expose
    public List<Song> songs = null;
    @SerializedName("songsCount")
    @Expose
    public int songsCount;
    @SerializedName("artistsCount")
    @Expose
    public int artistsCount;
    @SerializedName("albumsCount")
    @Expose
    public int albumsCount;
}
