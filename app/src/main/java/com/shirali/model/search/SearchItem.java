package com.shirali.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Playlist;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Song;

import java.util.List;

/**
 * Created by Sagar on 25/1/18.
 */

public class SearchItem {

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
    @SerializedName("playlists")
    @Expose
    public List<Shirali> playlists = null;
}
