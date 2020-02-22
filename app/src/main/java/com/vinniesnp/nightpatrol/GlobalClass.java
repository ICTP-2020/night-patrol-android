package com.vinniesnp.nightpatrol;

import android.app.Application;

public class GlobalClass extends Application {


    public String getBaseURL() {

        String url = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";

        return url;
    }

}
