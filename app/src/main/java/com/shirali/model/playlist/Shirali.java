package com.shirali.model.playlist;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Song;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Sagar on 4/8/17.
 */
public class Shirali  {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("createdBy")
    @Expose
    public String createdBy;
    @SerializedName("songs")
    @Expose
    public ArrayList<Song> songs = null;
    @SerializedName("title_hebrew")
    @Expose
    public String title_hebrew;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("description_hebrew")
    @Expose
    public String description_hebrew;
    @SerializedName("artwork")
    @Expose
    public String artwork = "";
    @SerializedName("avatar")
    @Expose
    public String avatar = "";
    @SerializedName("shareUrl")
    @Expose
    public String shareUrl;

}
