package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 29/9/17.
 */

public class RawCharge {

    @SerializedName("source")
    @Expose
    public Source source;
}
