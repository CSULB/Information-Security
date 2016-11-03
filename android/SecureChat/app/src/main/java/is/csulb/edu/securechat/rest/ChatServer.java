package is.csulb.edu.securechat.rest;

import is.csulb.edu.securechat.pojos.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatServer {

    @POST("users/register")
    Call<User> register(User user);

    @POST("users/login")
    Call<User> remoteLogin(@Body User user);

}
