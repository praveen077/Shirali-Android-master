package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;

import java.util.List;

public class RecentlyPlayed {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("artist")
    @Expose
    public Artist artist;
    @SerializedName("albums")
    @Expose
    public List<Album> albums = null;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("titleHebrew")
    @Expose
    public String titleHebrew;
    @SerializedName("duration_seconds")
    @Expose
    public float duration;
    @SerializedName("artwork")
    @Expose
    public String artwork;
    @SerializedName("song_original_fileurl")
    @Expose
    public String song_original_fileurl;

}