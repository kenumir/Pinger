package com.wt.pinger.data.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Kenumir on 2016-08-30.
 *
 */
public interface ApiService {

    @POST("pinger")
    Call<JsonObject> saveUser(@Body NewUser user);

}
