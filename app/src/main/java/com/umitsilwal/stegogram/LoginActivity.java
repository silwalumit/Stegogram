package com.umitsilwal.stegogram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    // UI references.
    private EditText mPasswordView,mEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if(TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            saveCredentialsAndLogin(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
        // return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    private void saveCredentialsAndLogin(String email, String password){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit()
            .putString("xmpp_username", email)
            .putString("xmpp_password", password)
            .apply();

        //Start the service
        Intent intent = new Intent(getApplicationContext(), NetworkConnectionService.class);
        intent.setAction(MainActivity.LOGIN_ACTION);
        startService(intent);
    }

    @Override
    protected void onPause() {
        Log.d(NetworkConnectionService.TAG, "LoginActivity Paused.");
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        Log.d(NetworkConnectionService.TAG, "LoginActivity Resumed.");
        super.onResume();

        mBroadcastReceiver = new NetworkBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                String action = intent.getAction();
                switch (action){
                    case NetworkConnectionService.NO_CONNECTION:
                        startService(new Intent(getApplicationContext(), NetworkConnectionService.class));
                        break;
                    case NetworkConnectionService.AUTHENTICATED:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                        break;
                    case NetworkConnectionService.AUTHENTICATION_FAILED:
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(NetworkConnectionService.AUTHENTICATED);
        intentFilter.addAction(NetworkConnectionService.AUTHENTICATION_FAILED);
        intentFilter.addAction(NetworkConnectionService.NO_CONNECTION);
        intentFilter.addAction(NetworkConnectionService.CONNECTED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

}

