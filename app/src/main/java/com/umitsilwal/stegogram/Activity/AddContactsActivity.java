package com.umitsilwal.stegogram.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.umitsilwal.stegogram.NetworkBroadcastReceiver;
import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;
import com.umitsilwal.stegogram.StegoMessage;

public class AddContactsActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private EditText mUserJidEditText;
    private EditText mUserNicknameEditText;
    private Button add_contact_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue);
        toolbar.setTitle("Add Contact");

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }catch (NullPointerException e){
            Log.d(StegoConnectionService.TAG, e.getMessage());
        }

        mUserJidEditText = findViewById(R.id.user_jid_editText);
        mUserNicknameEditText = findViewById(R.id.user_nickname_jid);

        add_contact_btn = findViewById(R.id.add_contact_btn);
        add_contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userJid = mUserJidEditText.getText().toString().trim();
                String userNickname = mUserNicknameEditText.getText().toString().trim();
                if(validate(userJid, userNickname)) {
                    add_contact_btn.setEnabled(false);
                    Intent intent = new Intent(getApplicationContext(), StegoConnectionService.class);
                    intent.setAction(StegoConnectionService.ACTION_ADD_CONTACT);
                    intent.putExtra("user_jid", userJid);
                    intent.putExtra("user_nickname", userNickname);
                    startService(intent);
                }
            }
        });
    }

    private boolean isJidValid(String jid) {
        return jid.contains("@");
    }

    protected boolean validate(String jid, String nickname){
        if(jid.length() <= 0){
            mUserJidEditText.setError(getString(R.string.error_field_required));
            return false;
        }else if(!isJidValid(jid)){
            mUserJidEditText.setError(getString(R.string.error_invalid_email));
            return false;
        }else if(nickname.length() <= 0){
            mUserNicknameEditText.setError(getString(R.string.error_field_required));
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new NetworkBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                String action = intent.getAction();
                switch (action){
                    case StegoConnectionService.ACTION_CONTACT_ADD_FAILED:
                        Toast.makeText(getApplicationContext(), "Could not add contact. Try Again.", Toast.LENGTH_LONG).show();
                        add_contact_btn.setEnabled(true);
                        break;
                    case StegoConnectionService.ACTION_CONTACT_ADDED:
                        Toast.makeText(getApplicationContext(), "Contact added.", Toast.LENGTH_LONG).show();
                        mUserJidEditText.setText("");
                        mUserNicknameEditText.setText("");
                        add_contact_btn.setEnabled(true);
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_AUTH_FAILED);
        intentFilter.addAction(StegoConnectionService.ACTION_NO_CONN);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        intentFilter.addAction(StegoConnectionService.ACTION_CONTACT_ADDED);
        intentFilter.addAction(StegoConnectionService.ACTION_CONTACT_ADD_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
