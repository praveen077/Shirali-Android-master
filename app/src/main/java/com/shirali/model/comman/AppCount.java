package com.shirali.model.comman;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 22/9/17.
 */

public class AppCount {
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;

    public String getMessage() {
        if (message == null){
            return "";
        }
        return message;
    }
}
