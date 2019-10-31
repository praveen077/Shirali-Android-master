
package com.shirali.model.setting;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppSetting {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("setting")
    @Expose
    public Setting setting;

}
