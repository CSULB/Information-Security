package com.gauravbhor.securechat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.fragments.LoginFragment;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.SodiumJNI;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;
import org.libsodium.jni.keys.SigningKey;
import org.libsodium.jni.keys.VerifyKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final int BASE64_SAFE_URL_FLAGS = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;

    private TextView seedView, publicKeyView, privateKeyView, signKeyView, verifyKeyView;
    private Button generateKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceHelper.init(getApplicationContext());

        if (PreferenceHelper.getBoolean(StaticMembers.LOGGED_IN)) {
            System.out.println("Logged IN");
        } else {
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
        }
    }
}
