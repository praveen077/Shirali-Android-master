
package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Card {

    @SerializedName("tokenization_method")
    @Expose
    public Object tokenizationMethod;
    @SerializedName("funding")
    @Expose
    public String funding;
    @SerializedName("brand")
    @Expose
    public String brand;
    @SerializedName("object")
    @Expose
    public String object;
    @SerializedName("last4")
    @Expose
    public String last4;
    @SerializedName("exp_year")
    @Expose
    public Integer expYear;
    @SerializedName("exp_month")
    @Expose
    public Integer expMonth;
    @SerializedName("country")
    @Expose
    public String country;
    @SerializedName("address_country")
    @Expose
    public Object addressCountry;
    @SerializedName("address_zip")
    @Expose
    public String addressZip;
    @SerializedName("address_state")
    @Expose
    public String addressState;
    @SerializedName("address_city")
    @Expose
    public String addressCity;
    @SerializedName("address_line2")
    @Expose
    public String addressLine2;
    @SerializedName("address_line1")
    @Expose
    public String addressLine1;
    @SerializedName("name")
    @Expose
    public String name;

}
