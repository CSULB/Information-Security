package com.gauravbhor.securechat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.Functions;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONObject;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* Using https://github.com/ragunathjawahar/android-saripaar for form validation.
*/
public class LoginFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;

    @NotEmpty
    @DecimalMin(12)
    private EditText editTextPhone;

    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS)
    private EditText editTextPassword;

    private Button loginButton, registerButton;

    private String userId;

    private Validator validator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        editTextPhone = (EditText) rootView.findViewById(R.id.edittext_phone);
        editTextPassword = (EditText) rootView.findViewById(R.id.edittext_password);
        loginButton = (Button) rootView.findViewById(R.id.button_login);
        registerButton = (Button) rootView.findViewById(R.id.button_register);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
        KeyPair pair = new KeyPair(seed);

        System.out.println("PR: " + pair.getPrivateKey());

        Bundle bundle = getArguments();
        if (bundle != null) {
            editTextPhone.setText(bundle.getString("phone"));
            userId = bundle.getString("id");
        }

        validator = new Validator(this);
        validator.setValidationListener(this);


        /*
        * Credits to https://developer.android.com/training/permissions/requesting.html
        * */
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_PHONE_STATE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                validator.validate();
                break;
            case R.id.button_register:
                RegisterFragment fragment = new RegisterFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            default:
                break;
        }
    }

    private void replyToChallenge(String phone, String challengeResponse) {

        User user = new User();
        user.setPhone(phone);
        user.setChallenge_response(challengeResponse);

        Call<ResponseBody> call = RetroBuilder.buildOn(ChatServer.class).remoteLogin(user, 2);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.has("error")) {
                            Toast.makeText(getContext(), json.getString("error"), Toast.LENGTH_LONG).show();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("user_id", json.getString("id"));

                            VerificationFragment verificationFragment = new VerificationFragment();
                            verificationFragment.setArguments(bundle);

                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            manager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, verificationFragment).commit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Error. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
    * Credits to https://developer.android.com/training/permissions/requesting.html
    * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    TelephonyManager t = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                    String mPhoneNumber = t.getLine1Number();

                    if (mPhoneNumber != null) {
                        // Strip all non-numeric characters
                        mPhoneNumber = mPhoneNumber.replaceAll("[^\\d]", "");
                        if (mPhoneNumber.length() == 10) {
                            editTextPhone.setText("01" + mPhoneNumber);
                        } else if (mPhoneNumber.length() == 11) {
                            editTextPhone.setText("0" + mPhoneNumber);
                        } else if (mPhoneNumber.length() == 12) {
                            editTextPhone.setText(mPhoneNumber);
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onValidationSucceeded() {
        final String phone = editTextPhone.getText().toString();
        final String password = editTextPassword.getText().toString();

        User user = new User();
        user.setPhone(phone);

        Call<ResponseBody> call = RetroBuilder.buildOn(ChatServer.class).remoteLogin(user, 1);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.has("error")) {
                            Toast.makeText(getContext(), json.getString("error"), Toast.LENGTH_LONG).show();
                        } else {
                            String saltedPasswordHash = Functions.hash("SHA-512", password, json.getString("salt"));
                            String challengeResponse = Functions.HMAC("HmacSHA512", saltedPasswordHash, json.getString("challenge"));
                            System.out.println("Server ata: " + json);
                            System.out.println("chall: " + challengeResponse);
                            replyToChallenge(phone, challengeResponse);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            if (error.getView() == editTextPassword) {
                editTextPassword.setError("Please select a stronger password");
                editTextPassword.getText().clear();
            } else if (error.getView() == editTextPhone) {
                editTextPhone.setError("Missing or invalid phone number");
            }
        }
    }
}
