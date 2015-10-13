package com.inipage.productivitypulse;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.BufferedSink;
import retrofit.Converter;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;

public class ApiFactory {
    public static ApiInterface getApi(){
        Retrofit ra = new Builder()
                .baseUrl("https://www.rescuetime.com")
                .build();
        return ra.create(ApiInterface.class);
    }
}
