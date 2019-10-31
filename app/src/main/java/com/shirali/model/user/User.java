package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shirali.model.songs.Song;

import java.util.ArrayList;
import java.util.List;

public class User {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("isTrialTaken")
    @Expose
    public Integer isTrialTaken = 0;
    @SerializedName("updatedDate")
    @Expose
    public String updatedDate;
    @SerializedName("createdDate")
    @Expose
    public String createdDate;
    @SerializedName("stripe")
    @Expose
    public Stripe stripe;
    @SerializedName("subscribePlan")
    @Expose
    public Subscription subscribePlan;
    @SerializedName("deleted")
    @Expose
    public Boolean deleted;
    @SerializedName("isBlock")
    @Expose
    public Boolean isBlock;
    @SerializedName("isVocalOnly")
    @Expose
    public String isVocalOnly;
    @SerializedName("songs")
    @Expose
    public List<Song> songs = null;
    @SerializedName("playlist")
    @Expose
    public List<Object> playlist = null;
    /*@SerializedName("payments")
    @Expose
    public List<PaymentInfo> payments;*/
    @SerializedName("role")
    @Expose
    public List<String> role = null;
    @SerializedName("resetPasswordToken")
    @Expose
    public String resetPasswordToken;
    @SerializedName("blockedGenres")
    @Expose
    public ArrayList<String> blockedGenres = null;
    @SerializedName("recentlyPlayed")
    @Expose
    public ArrayList<String> recentlyPlayed = null;
    @SerializedName("preferences")
    @Expose
    public GenresPrefrences genresPrefrences;
    @SerializedName("deviceType")
    @Expose
    public List<Object> deviceType;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("deviceToken")
    @Expose
    public List<String> deviceToken;
    @SerializedName("billingAddress")
    @Expose
    public String billingAddress;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("lastName")
    @Expose
    public String lastName;
    @SerializedName("firstName")
    @Expose
    public String firstName;
    @SerializedName("myMusic")
    @Expose
    public ArrayList<String> myMusic;

    public int getTrialTokan() {
        if (isTrialTaken == null) {
            return 0;
        } else {
            return isTrialTaken;
        }
    }
}
