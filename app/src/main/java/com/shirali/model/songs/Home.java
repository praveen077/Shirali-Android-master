package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;

import java.util.List;

/**
 * Created by Sagar on 2/8/17.
 */

public class Home {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("recentlyPlayed")
    @Expose
    public List<Song> recentlyPlayed = null;
    @SerializedName("recentAlbums")
    @Expose
    public List<Album> recentAlbums = null;
    @SerializedName("preferredSongs")
    @Expose
    public List<Song> preferredSongs = null;
    @SerializedName("genres")
    @Expose
    public List<Genre> genres = null;

    @SerializedName("recentlyPlayedCount")
    @Expose
    public int recentlyPlayedCount;
    @SerializedName("recentAlbumsCount")
    @Expose
    public int recentAlbumsCount;
    @SerializedName("preferredSongsCount")
    @Expose
    public int preferredSongsCount;
    @SerializedName("GeneresCount")
    @Expose
    public int GeneresCount;

}
