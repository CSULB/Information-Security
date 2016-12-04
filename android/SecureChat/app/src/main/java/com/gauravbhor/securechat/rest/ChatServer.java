package com.gauravbhor.securechat.rest;

import com.gauravbhor.securechat.pojos.User;

import org.json.JSONObject;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatServer {

    @POST("users/register")
    Call<ResponseBody> register(@Body User user);

    @POST("users/login/{step}")
    Call<ResponseBody> remoteLogin(@Body User user, @Path("step") int step);

    @POST("users/verify")
    Call<ResponseBody> verify(@Body JSONObject json);

    @POST("keys/dh")
    Call<ResponseBody> dhExchange(@Body JSONObject publicKey);
}