package com.shirali.model.user;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("_id")
    @Expose
    public String id;
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
    @SerializedName("isBlock")
    @Expose
    public Boolean isBlock;
    @SerializedName("songs")
    @Expose
    public List<Object> songs = null;
    @SerializedName("playlist")
    @Expose
    public List<Object> playlist = null;
    @SerializedName("payments")
    @Expose
    public List<Object> payments = null;
    @SerializedName("role")
    @Expose
    public List<String> role = null;
    @SerializedName("resetPasswordToken")
    @Expose
    public String resetPasswordToken;
    @SerializedName("recentlyPlayed")
    @Expose
    public List<Object> recentlyPlayed = null;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("deviceToken")
    @Expose
    public String deviceToken;
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
}
