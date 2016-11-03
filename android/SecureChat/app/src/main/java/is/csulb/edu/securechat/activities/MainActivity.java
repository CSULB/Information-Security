package is.csulb.edu.securechat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import is.csulb.edu.securechat.R;
import is.csulb.edu.securechat.pojos.User;
import is.csulb.edu.securechat.rest.ChatServer;
import is.csulb.edu.securechat.utils.RetroBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = (EditText) findViewById(R.id.edittext_email);
        editTextPassword = (EditText) findViewById(R.id.edittext_password);
        loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                System.out.println("1");
                User user = new User();
                user.email = email;
                user.password = password;

                Call<User> call = RetroBuilder.buildOn(ChatServer.class).remoteLogin(user);
                System.out.println("2");
                call.enqueue(new Callback<User>() {

                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        System.out.println("4");
                        if (response.isSuccessful()) {
                            System.out.println("Working");
                        } else {
                            System.out.println("Minor lafda");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        System.out.println("5");
                        System.out.println("Jyada vaat laga");
                    }
                });
                System.out.println("3");
                break;
            default:
                break;
        }
    }
}