package com.vinniesnp.nightpatrol.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTemporaryStatus() {
        return temporaryStatus;
    }

    public void setTemporaryStatus(String isTemporary) {
        this.temporaryStatus = isTemporary;
    }

    public User(String inputToken, String inputTemp) {
        this.token = inputToken;
        this.temporaryStatus = inputTemp;
    }

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("isTemporary")
    @Expose
    String temporaryStatus;
}
