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

public class GenreData {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("artists")
    @Expose
    public List<Artist> artists = null;
    @SerializedName("albums")
    @Expose
    public List<Album> albums = null;
    @SerializedName("songs")
    @Expose
    public List<Song> songs = null;
    @SerializedName("songCount")
    @Expose
    public int songsCount;
    @SerializedName("artistCount")
    @Expose
    public int artistsCount;
    @SerializedName("albumCount")
    @Expose
    public int albumsCount;
}
