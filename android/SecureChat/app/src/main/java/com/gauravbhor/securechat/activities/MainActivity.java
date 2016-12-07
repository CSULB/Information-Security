package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.fragments.LoginFragment;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.PreferenceKeys;
import com.google.gson.Gson;

import org.libsodium.jni.Sodium;

public class MainActivity extends SuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceHelper.init(getApplicationContext());
        Sodium.sodium_init();

        String userJson = PreferenceHelper.getString(PreferenceKeys.USER);
        if (userJson != null) {
            SuperActivity.user = new Gson().fromJson(userJson, User.class);
            startActivity(new Intent(this, TabbedActivity.class));
        } else {
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
        }
    }

    // THIS IS VERY IMPORTANT. STUPID ANDROID STUDIO DOESN'T LOAD IT AUTOMATICALLY.
    static {
        try {
            System.loadLibrary("sodiumjni");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
