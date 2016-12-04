package com.gaurav.securechat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gaurav.securechat.R;
import com.gaurav.securechat.rest.ChatServer;
import com.gaurav.securechat.utils.RetroBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VerificationFragment extends Fragment {

    private EditText code;
    private Button verifyButton;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_verify, container, false);

        final String userId = getArguments().getString("user_id");

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
                        RetroBuilder.buildOn(ChatServer.class).verify(json).enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if (response.isSuccessful()) {
                                        JSONObject resp = new JSONObject(response.body().string());
                                        if (resp.has("error")) {
                                            Toast.makeText(getContext(), "Error: " + resp.getString("error"), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getContext(), "Verified!", Toast.LENGTH_LONG).show();
                                            // Open friend's list
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
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    code.setError("Please enter a valid verfication code.");
                }
            }
        });

        return rootView;
    }
}
