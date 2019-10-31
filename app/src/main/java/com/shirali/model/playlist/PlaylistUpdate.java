package com.shirali.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistUpdate {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("playlist")
    @Expose
    public Playlist playlist;

    public class Playlist {

        @SerializedName("_id")
        @Expose
        public String id;
        @SerializedName("createdBy")
        @Expose
        public String createdBy;
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
        @SerializedName("avatar")
        @Expose
        public Object avatar;
        @SerializedName("songs")
        @Expose
        public List<String> songs = null;
        @SerializedName("language")
        @Expose
        public String language;
        @SerializedName("description_hebrew")
        @Expose
        public Object descriptionHebrew;
        @SerializedName("description")
        @Expose
        public Object description;
        @SerializedName("title_hebrew")
        @Expose
        public Object titleHebrew;
        @SerializedName("title")
        @Expose
        public String title;

    }

}