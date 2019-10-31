package com.shirali.model.mymusic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Pankaj on 16/8/17.
 */
public class MyMusic implements Serializable{
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("myMusic")
    @Expose
    public MyMusicContain myMusicContain;
    @SerializedName("count")
    @Expose
    public int count;

    public String getMessage() {
        if (message == null){
            return "";
        }
        return message;
    }
}
