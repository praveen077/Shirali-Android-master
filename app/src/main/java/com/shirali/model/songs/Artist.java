
package com.shirali.model.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.mymusic.BankAccount;

import java.util.ArrayList;
import java.util.List;

public class Artist {
    @SerializedName("albumCount")
    @Expose
    public int albumCount;
    @SerializedName("songCount")
    @Expose
    public int songCount;
    @SerializedName("stripe_account_id")
    @Expose
    public String stripe_account_id;
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("language")
    @Expose
    public String language;
    @SerializedName("updatedDate")
    @Expose
    public String updatedDate;
    @SerializedName("createdDate")
    @Expose
    public String createdDate;
    @SerializedName("deleted")
    @Expose
    public Boolean deleted;
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium = false;
    @SerializedName("isActive")
    @Expose
    public Boolean isActive;
    @SerializedName("bankAccount")
    @Expose
    public BankAccount bankAccount;
    @SerializedName("relatedArtists")
    @Expose
    public List<RelatedArtist> relatedArtists = null;
    @SerializedName("relatedSongs")
    @Expose
    public List<String> relatedSongs = null;
    @SerializedName("tags")
    @Expose
    public List<String> tags = null;
    @SerializedName("genres")
    @Expose
    public List<Genre> genres = null;
    @SerializedName("avatar")
    @Expose
    public String avatar;
    @SerializedName("royaltyCost")
    @Expose
    public Float royaltyCost;
    @SerializedName("phoneNumber")
    @Expose
    public String phoneNumber;
    @SerializedName("emailAddress")
    @Expose
    public String emailAddress;
    @SerializedName("labels")
    @Expose
    public List<String> labels = null;
    @SerializedName("nameHebrew")
    @Expose
    public String nameHebrew;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("shareUrl")
    @Expose
    public String shareUrl;
    @SerializedName("songs")
    @Expose
    public ArrayList<String> songsId;

    public int count = 0;
}
