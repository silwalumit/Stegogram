package com.umitsilwal.stegogram.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;

import com.umitsilwal.stegogram.NetworkBroadcastReceiver;
import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Debug Tag
    private static final String TAG = "LoginActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
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
        mContext = this;
    }

    @Override
    protected void onPause() {
        Log.d(StegoConnectionService.TAG, "LoginActivity Paused.");
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        Log.d(StegoConnectionService.TAG, "LoginActivity Resumed.");
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
                    case StegoConnectionService.ACTION_CONNECTED:
                        Intent login = new Intent(getApplicationContext(), StegoConnectionService.class);
                        login.setAction(StegoConnectionService.ACTION_LOGIN);
                        startService(login);
                        break;
                    case StegoConnectionService.ACTION_AUTHENTICATED:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                        break;
                    case StegoConnectionService.ACTION_AUTH_FAILED:
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_AUTH_FAILED);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
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
        if(TextUtils.isEmpty(password) || !isPasswordValid(password)){
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
            saveCredentialsAndLogin();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private void saveCredentialsAndLogin(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_username", mEmailView.getText().toString())
                .putString("xmpp_password", mPasswordView.getText().toString())
                .putBoolean("xmpp_logged_in", true)
                .apply();
        //Start the service
        Intent intent = new Intent(getApplicationContext(), StegoConnectionService.class);
        intent.setAction(StegoConnectionService.ACTION_LOGIN);
        startService(intent);
    }


}

