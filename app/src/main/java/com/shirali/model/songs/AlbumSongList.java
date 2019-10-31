package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AlbumSongList {
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("songs")
    @Expose
    public ArrayList<SongsAlbum> songs = null;

    public class SongsAlbum {

        @SerializedName("_id")
        @Expose
        public String id;
        @SerializedName("artist")
        @Expose
        public Artist artist;
        @SerializedName("ft_artist")
        @Expose
        public List<FtArtist> ftArtist;
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
        @SerializedName("albums")
        @Expose
        public List<String> albums = null;
        @SerializedName("streamedCount")
        @Expose
        public Integer streamedCount;
        @SerializedName("tags")
        @Expose
        public List<Tag> tags = null;
        @SerializedName("duration_seconds")
        @Expose
        public Float durationSeconds;
        @SerializedName("duration")
        @Expose
        public String duration;
        @SerializedName("isNewRelease")
        @Expose
        public Boolean isNewRelease;
        @SerializedName("isExclusive")
        @Expose
        public Boolean isExclusive;
        @SerializedName("artwork")
        @Expose
        public String artwork;
        @SerializedName("song_original_fileurl")
        @Expose
        public String songOriginalFileurl;
        @SerializedName("song_medium_fileurl")
        @Expose
        public String songMediumFileurl;
        @SerializedName("song_low_fileurl")
        @Expose
        public String songLowFileurl;
        @SerializedName("song_high_fileurl")
        @Expose
        public String songHighFileurl;
        @SerializedName("creationDate")
        @Expose
        public String creationDate;
        @SerializedName("genres")
        @Expose
        public List<Genre> genres = null;
        @SerializedName("title")
        @Expose
        public String title;
        @SerializedName("titleHebrew")
        @Expose
        public String title_hebrew;
    }
}