package com.jearhub.android.myplaces;

import com.jearhub.android.myplaces.Remote.IGoogleAPIService;
import com.jearhub.android.myplaces.Remote.RetrofitClient;

public class Common {
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static IGoogleAPIService getGoogleAPIService()
    {
        return RetrofitClient.getClient ( GOOGLE_API_URL ).create ( IGoogleAPIService.class );
    }
}
