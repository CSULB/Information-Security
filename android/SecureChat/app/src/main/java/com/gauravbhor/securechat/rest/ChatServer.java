package com.gauravbhor.securechat.rest;

import com.gauravbhor.securechat.adapters.GroupMessageAdapter;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.User;

import org.json.JSONObject;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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

    @POST("users/{id}")
    Call<User> getUser(@Body JSONObject json, @Path("id") String id);

    @POST("users/message/{id}")
    Call<ChatMessage> sendMessage(@Body JSONObject parent, @Path("id") long id);

    @POST("users/message/{id}/{mid}")
    Call<ResponseBody> getMessages(@Path("id") long id, @Path("mid") long mid);

    @POST("groups/create")
    Call<Group> createGroup(@Body Group group);

    @POST("groups/details")
    Call<Group> getGroupDetails(@Body JSONObject json);

    @POST("groups/messages/send/{id}")
    Call<ResponseBody> sendGroupMessage(@Body JSONObject parent, @Path("id") long groupID);

    @POST("groups/message/{id}")
    Call<ResponseBody> getGroupMessages(@Path("id") long id);
}