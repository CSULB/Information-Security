package com.gaurav.securechat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gaurav.securechat.R;
import com.gaurav.securechat.fragments.LoginFragment;
import com.gaurav.securechat.utils.PreferenceHelper;
import com.gaurav.securechat.utils.StaticMembers;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceHelper.init(getApplicationContext());

        if (savedInstanceState != null) {
            return;
        }

        if (PreferenceHelper.getBoolean(StaticMembers.LOGGED_IN)) {
            System.out.println("Logged IN");
        } else {
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, loginFragment).commit();
        }
    }

    static {
        // Load native library ECDH-Curve25519-Mobile implementing Diffie-Hellman key
        // exchange with elliptic curve 25519.
        try {
            System.loadLibrary("ecdhcurve25519");
            Log.i("TAG", "Loaded ecdhcurve25519 library.");
        } catch (UnsatisfiedLinkError e) {
            Log.e("TAG", "Error loading ecdhcurve25519 library: " + e.getMessage());
        }
    }
}