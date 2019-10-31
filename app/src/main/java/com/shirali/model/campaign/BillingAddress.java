package com.shirali.model.campaign;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar on 1/9/17.
 */

public class BillingAddress {

    @SerializedName("zip")
    @Expose
    public String zip;
    @SerializedName("aptNumber")
    @Expose
    public String aptNumber;
    @SerializedName("streetAddress")
    @Expose
    public String streetAddress;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("state")
    @Expose
    public String state;

}
