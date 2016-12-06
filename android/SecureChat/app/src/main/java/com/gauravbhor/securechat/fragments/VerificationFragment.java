package com.gauravbhor.securechat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;

import java.io.IOException;


import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.activities.FriendListActivity;
import com.gauravbhor.securechat.activities.SuperActivity;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.PreferenceKeys;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VerificationFragment extends Fragment {

    private EditText code;
    private Button verifyButton;
    private CircularProgressView progressView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_verify, container, false);

        final String userId = getArguments().getString("user_id");

        progressView = (CircularProgressView) rootView.findViewById(R.id.progress_view);
        code = (EditText) rootView.findViewById(R.id.edittext_verification_code);
        verifyButton = (Button) rootView.findViewById(R.id.button_verify);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = code.getText().toString();

                // Send request to verify
                if (verificationCode.length() == 6) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("code", verificationCode);
                        json.put("user_id", userId);
                        System.out.println(code + "::" + userId);

                        updateUI(true);

                        RetroBuilder.buildOn(ChatServer.class).verify(json).enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                updateUI(false);
                                try {
                                    if (response.isSuccessful()) {
                                        JSONObject resp = new JSONObject(response.body().string());
                                        if (resp.has("error")) {
                                            Toast.makeText(getContext(), "Error: " + resp.getString("error"), Toast.LENGTH_LONG).show();
                                        } else {

                                            PreferenceHelper.save(PreferenceKeys.USER, resp.toString());
                                            PreferenceHelper.save(PreferenceKeys.JWT, resp.getString("token"));

                                            System.out.println("JWT: " + resp.getString("token"));

                                            SuperActivity.user = new Gson().fromJson(resp.toString(), User.class);
                                            PreferenceHelper.save(PreferenceKeys.USER_ID, SuperActivity.user.getId());
                                            Toast.makeText(getContext(), "Verified!", Toast.LENGTH_LONG).show();
                                            // Open friend's list
                                            byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
                                            KeyPair key = new KeyPair(seed);

                                            String publicKey = Base64.encodeToString(key.getPublicKey().toBytes(), StaticMembers.BASE64_SAFE_URL_FLAGS);
                                            String privateKey = Base64.encodeToString(key.getPrivateKey().toBytes(), StaticMembers.BASE64_SAFE_URL_FLAGS);
                                            PreferenceHelper.save(PreferenceKeys.PRIVATE_KEY, privateKey);
                                            PreferenceHelper.save(PreferenceKeys.PUBLIC_KEY, publicKey);
                                            Intent intent = new Intent(getActivity(), FriendListActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Please try again", Toast.LENGTH_LONG).show();
                                t.printStackTrace();
                                updateUI(false);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    code.setError("Please enter a valid verfication code.");
                    updateUI(false);
                }
            }
        });

        return rootView;
    }

    private void updateUI(boolean isLoading) {
        if (isLoading) {
            progressView.setVisibility(View.VISIBLE);
            progressView.startAnimation();
            verifyButton.setEnabled(false);
            code.setEnabled(false);
        } else {
            progressView.stopAnimation();
            progressView.setVisibility(View.INVISIBLE);
            verifyButton.setEnabled(true);
            code.setEnabled(true);
        }
    }
}
