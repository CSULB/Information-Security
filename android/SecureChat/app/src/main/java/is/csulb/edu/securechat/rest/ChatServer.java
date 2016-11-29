package is.csulb.edu.securechat.rest;

import org.json.JSONObject;

import is.csulb.edu.securechat.pojos.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatServer {

    @POST("users/register")
    Call<ResponseBody> register(@Body User user);

    @POST("users/login")
    Call<User> remoteLogin(@Body User user);

    @POST("users/verify")
    Call<ResponseBody> verify(@Body JSONObject json);

    @POST("keys/dh")
    Call<ResponseBody> dhExchange(@Body JSONObject publicKey);
}