package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 12/9/17.
 */

public class CampaignResponse {

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

