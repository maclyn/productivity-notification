package com.inipage.productivitypulse;

import com.squareup.okhttp.ResponseBody;

import org.json.JSONObject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.http.GET;
import retrofit.http.Query;

public interface ApiInterface {
    @GET("/anapi/data")
    Call<ResponseBody> getData(@Query("key") String authToken);
}
