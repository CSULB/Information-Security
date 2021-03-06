package com.gauravbhor.securechat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.RetroBuilder;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* Using https://developer.android.com/training/permissions/requesting.html for runtime app permissions.
* Using https://github.com/ragunathjawahar/android-saripaar for form validation.
* */
public class RegisterFragment extends Fragment implements Validator.ValidationListener, View.OnClickListener {


    @NotEmpty
    private EditText firstName;

    @NotEmpty
    private EditText lastName;

    @NotEmpty
    @DecimalMin(12)
    private EditText phone;

    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS)
    private EditText password;

    @ConfirmPassword
    private EditText confirmPassword;

    private Button register;
    private Validator validator;
    private CircularProgressView progressView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        progressView = (CircularProgressView) rootView.findViewById(R.id.progress_view);
        firstName = (EditText) rootView.findViewById(R.id.edittext_first_name);
        lastName = (EditText) rootView.findViewById(R.id.edittext_last_name);
        phone = (EditText) rootView.findViewById(R.id.edittext_phone);
        password = (EditText) rootView.findViewById(R.id.edittext_password);
        confirmPassword = (EditText) rootView.findViewById(R.id.edittext_confirm_password);

        register = (Button) rootView.findViewById(R.id.button_register);
        register.setOnClickListener(this);

        validator = new Validator(this);
        validator.setValidationListener(this);

        return rootView;
    }

    @Override
    public void onValidationSucceeded() {

//        // Create Alice's secret key from a big random number.
//        SecureRandom random = new SecureRandom();
//        final byte[] privateKey = ECDHCurve25519.generate_secret_key(random);
//        // Create Alice's public key.
//        byte[] publicKey = ECDHCurve25519.generate_public_key(privateKey);
//
//        String s = bytesToHex(publicKey);
//
//        System.out.println("AA: " + s);
//
//        JSONObject json = new JSONObject();
//        try {
//            json.put("public_key", s);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        RetroBuilder.buildOn(ChatServer.class).dhExchange(json).enqueue(new Callback<ResponseBody>() {

        updateUI(true);
        User user = new User();
        user.setFirst_name(firstName.getText().toString());
        user.setLast_name(lastName.getText().toString());
        user.setPhone(phone.getText().toString());
        user.setPassword(password.getText().toString());
        user.setConfirm_password(confirmPassword.getText().toString());

        RetroBuilder.buildOn(ChatServer.class).register(user).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                updateUI(false);
                if (response.isSuccessful()) {
                    try {
//                        Attempt at diffie hellman
//                        JSONObject serverKeys = new JSONObject(response.body().string());
//                        byte[] serverPublicKey = hexStringToByteArray(serverKeys.getString("public_key"));
//                        byte[] sharedSecret = ECDHCurve25519.generate_shared_secret(privateKey, serverPublicKey);
                        JSONObject resp = new JSONObject(response.body().string());
                        if (resp.has("error")) {
                            /*
                             * Validation failed
                             * Codes:
                             * 0 = Missing fields
                             * 1 = Invalid fieds
                             * 2 = Not unique
                             * 3 = Passwords don't match
                            */
                            int error_code = resp.getInt("code");
                            switch (error_code) {
                                case 0:
                                    // Should never occur
                                    break;
                                case 1:
                                    // Should never occur
                                    break;
                                case 2:
                                    Bundle bundle = new Bundle();
                                    bundle.putString("phone", resp.getString("phone"));
                                    bundle.putString("id", resp.getString("id"));

                                    LoginFragment loginFragment = new LoginFragment();
                                    loginFragment.setArguments(bundle);

                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                    manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    manager.beginTransaction().replace(R.id.fragment_container, loginFragment).commit();

                                    Toast.makeText(getContext(), "We located your account. Please login.", Toast.LENGTH_LONG).show();
                                    break;
                                case 3:
                                    // Should never occur
                                    break;
                            }
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("user_id", resp.getString("id"));

                            VerificationFragment verificationFragment = new VerificationFragment();
                            verificationFragment.setArguments(bundle);

                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            manager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, verificationFragment).commit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Toast.makeText(getContext(), "Error: " + response.errorBody().string() + ". Please try again", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateUI(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Please try again", Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            if (error.getView() != password) {
                ((EditText) error.getView()).setError(error.getCollatedErrorMessage(getContext()));
            } else {
                password.setError("Please select a stronger password");
                password.getText().clear();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                validator.validate();
                break;
            default:
                break;
        }
    }

    private void updateUI(boolean isLoading) {
        if (isLoading) {
            progressView.setVisibility(View.VISIBLE);
            progressView.startAnimation();
            register.setEnabled(false);
            firstName.setEnabled(false);
            lastName.setEnabled(false);
            password.setEnabled(false);
            confirmPassword.setEnabled(false);
        } else {
            progressView.stopAnimation();
            progressView.setVisibility(View.INVISIBLE);
            register.setEnabled(true);
            firstName.setEnabled(true);
            lastName.setEnabled(true);
            password.setEnabled(true);
            confirmPassword.setEnabled(true);
        }
    }
}
