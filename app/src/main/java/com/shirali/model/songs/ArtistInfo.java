package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.playlist.Shirali;

import java.util.ArrayList;

/**
 * Created by Sagar on 25/8/17.
 */

public class ArtistInfo {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("artist")
    @Expose
    public Artist artist;
    @SerializedName("NewReleasedAlbums")
    @Expose
    public ArrayList<Album> NewReleasedAlbums;
    @SerializedName("albums")
    @Expose
    public ArrayList<Album> albums;
    @SerializedName("popularSongs")
    @Expose
    public ArrayList<Song> popularSongs;
    @SerializedName("popularPlaylist")
    @Expose
    public ArrayList<Shirali> popularPlaylist;
    @SerializedName("NewReleasedAlbumsCount")
    @Expose
    public int NewReleasedAlbumsCount;
    @SerializedName("albumsCount")
    @Expose
    public int albumsCount;
    @SerializedName("popularSongsCount")
    @Expose
    public int popularSongsCount;
    @SerializedName("popularPlaylistCount")
    @Expose
    public int popularPlaylistCount;
    @SerializedName("artistSongs")
    @Expose
    public ArrayList<Song> songList;
}
