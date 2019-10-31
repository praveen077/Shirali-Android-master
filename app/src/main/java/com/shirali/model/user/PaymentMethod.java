
package com.shirali.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentMethod {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("updatedAt")
    @Expose
    public String updatedAt;
    @SerializedName("createdAt")
    @Expose
    public String createdAt;
    @SerializedName("cardId")
    @Expose
    public String cardId;
    @SerializedName("user")
    @Expose
    public String user;
    @SerializedName("__v")
    @Expose
    public Integer v;
    @SerializedName("card")
    @Expose
    public Card card;
    @SerializedName("used")
    @Expose
    public Boolean used;
    @SerializedName("object")
    @Expose
    public String object;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("livemode")
    @Expose
    public Boolean livemode;
    @SerializedName("created")
    @Expose
    public String created;
    @SerializedName("isDefault")
    @Expose
    public Boolean isDefault;

}
