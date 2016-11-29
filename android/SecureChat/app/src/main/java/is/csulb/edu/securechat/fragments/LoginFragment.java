package is.csulb.edu.securechat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import is.csulb.edu.securechat.R;
import is.csulb.edu.securechat.pojos.User;
import is.csulb.edu.securechat.rest.ChatServer;
import is.csulb.edu.securechat.utils.RetroBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;

    private EditText editTextPhone, editTextPassword;
    private Button loginButton, registerButton;

    private String userId;

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

        Bundle bundle = getArguments();
        if (bundle != null) {
            editTextPhone.setText(bundle.getString("phone"));
            userId = bundle.getString("id");
        }

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
                String email = editTextPhone.getText().toString();
                String password = editTextPassword.getText().toString();

                User user = new User();
                user.phone = email;
                user.password = password;

                Call<User> call = RetroBuilder.buildOn(ChatServer.class).remoteLogin(user);
                call.enqueue(new Callback<User>() {

                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            System.out.println("Working");
                        } else {
                            System.out.println("Minor lafda");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Please try again", Toast.LENGTH_LONG).show();
                    }
                });
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
}
