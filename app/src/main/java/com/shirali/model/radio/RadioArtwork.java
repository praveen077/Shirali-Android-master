package com.shirali.model.radio;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RadioArtwork implements Serializable {

    @SerializedName("wrapperType")
    @Expose
    public String wrapperType;
    @SerializedName("kind")
    @Expose
    public String kind;
    @SerializedName("artistId")
    @Expose
    public Integer artistId;
    @SerializedName("collectionId")
    @Expose
    public Integer collectionId;
    @SerializedName("trackId")
    @Expose
    public Integer trackId;
    @SerializedName("artistName")
    @Expose
    public String artistName;
    @SerializedName("collectionName")
    @Expose
    public String collectionName;
    @SerializedName("trackName")
    @Expose
    public String trackName;
    @SerializedName("collectionCensoredName")
    @Expose
    public String collectionCensoredName;
    @SerializedName("trackCensoredName")
    @Expose
    public String trackCensoredName;
    @SerializedName("artistViewUrl")
    @Expose
    public String artistViewUrl;
    @SerializedName("collectionViewUrl")
    @Expose
    public String collectionViewUrl;
    @SerializedName("trackViewUrl")
    @Expose
    public String trackViewUrl;
    @SerializedName("previewUrl")
    @Expose
    public String previewUrl;
    @SerializedName("artworkUrl30")
    @Expose
    public String artworkUrl30;
    @SerializedName("artworkUrl60")
    @Expose
    public String artworkUrl60;
    @SerializedName("artworkUrl100")
    @Expose
    public String artworkUrl100;
    @SerializedName("collectionPrice")
    @Expose
    public Float collectionPrice;
    @SerializedName("trackPrice")
    @Expose
    public Float trackPrice;
    @SerializedName("releaseDate")
    @Expose
    public String releaseDate;
    @SerializedName("collectionExplicitness")
    @Expose
    public String collectionExplicitness;
    @SerializedName("trackExplicitness")
    @Expose
    public String trackExplicitness;
    @SerializedName("discCount")
    @Expose
    public Integer discCount;
    @SerializedName("discNumber")
    @Expose
    public Integer discNumber;
    @SerializedName("trackCount")
    @Expose
    public Integer trackCount;
    @SerializedName("trackNumber")
    @Expose
    public Integer trackNumber;
    @SerializedName("trackTimeMillis")
    @Expose
    public Integer trackTimeMillis;
    @SerializedName("country")
    @Expose
    public String country;
    @SerializedName("currency")
    @Expose
    public String currency;
    @SerializedName("primaryGenreName")
    @Expose
    public String primaryGenreName;
    @SerializedName("isStreamable")
    @Expose
    public Boolean isStreamable;

}