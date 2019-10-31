package com.shirali.model.mymusic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Artist;
import com.shirali.model.songs.Song;

import java.util.ArrayList;

/**
 * Created by Sagar on 2/8/17.
 */
public class Album {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("artist")
    @Expose
    public Artist artist;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("updatedDate")
    @Expose
    public String updatedDate;
    @SerializedName("createdDate")
    @Expose
    public String createdDate;
    @SerializedName("deleted")
    @Expose
    public Boolean deleted;
    @SerializedName("isActive")
    @Expose
    public Boolean isActive;
    @SerializedName("songs")
    @Expose
    public ArrayList<Song> songs = null;
    @SerializedName("isNewRelease")
    @Expose
    public Boolean isNewRelease;
    @SerializedName("isExclusive")
    @Expose
    public Boolean isExclusive;
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium = false;
    @SerializedName("artwork")
    @Expose
    public String artwork;
    @SerializedName("creationDate")
    @Expose
    public String creationDate;
    @SerializedName("language")
    @Expose
    public String language;
    @SerializedName("titleHebrew")
    @Expose
    public String titleHebrew;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("totalDuration")
    @Expose
    public String totalDuration;
    @SerializedName("shareUrl")
    @Expose
    public String shareUrl;
    @SerializedName("songCount")
    @Expose
    public int songCount;
}
