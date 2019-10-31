package com.shirali.model.mymusic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 16/8/17.
 */

public class BankAccount {
    @SerializedName("accountNumber")
    @Expose
    public String accountNumber;
    @SerializedName("routingNumber")
    @Expose
    public String routingNumber;
    @SerializedName("bankName")
    @Expose
    public String bankName;
    @SerializedName("zipcode")
    @Expose
    public String zipcode;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("aptNumber")
    @Expose
    public String aptNumber;
    @SerializedName("streetAddress")
    @Expose
    public String streetAddress;

}
