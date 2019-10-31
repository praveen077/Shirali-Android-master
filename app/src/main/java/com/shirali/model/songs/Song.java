package com.shirali.model.songs;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.Album;

public class Song {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("artist")
    @Expose
    public Artist artist = new Artist();
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
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium;
    @SerializedName("albums")
    @Expose
    public List<Album> albums = null;
    @SerializedName("streamedCount")
    @Expose
    public int streamedCount;
    @SerializedName("unlikeCount")
    @Expose
    public int unlikeCount;
    @SerializedName("likeCount")
    @Expose
    public int likeCount;
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
    @SerializedName("avatar")
    @Expose
    public String avatar;
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
    @SerializedName("shareUrl")
    @Expose
    public String shareUrl;
    @SerializedName("shortid")
    @Expose
    public String shortid;
    @SerializedName("genres")
    @Expose
    public List<Genre> genres = null;
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("titleHebrew")
    @Expose
    public String title_hebrew = "";


}
