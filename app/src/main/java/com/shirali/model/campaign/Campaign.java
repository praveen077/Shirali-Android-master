package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 1/9/17.
 */

public class Campaign {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("campaign")
    @Expose
    public Campaign_ campaign;

    public String getMessage() {
        if (message == null){
            return "";
        }
        return message;
    }
}
