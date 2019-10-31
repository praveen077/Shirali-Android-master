package com.shirali.model.songs.artist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;
import com.shirali.model.songs.Artist;

import java.util.List;

/**
 * Created by Sagar on 18/9/17.
 */

public class ArtistNewAlbum {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("albums")
    @Expose
    public List<Album> artist;
}
