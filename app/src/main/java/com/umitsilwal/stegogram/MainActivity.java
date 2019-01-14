package com.umitsilwal.stegogram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String LOGIN_ACTION = "com.umitsilwal.stegogram.LOGIN";
    public static final String CONNECT_ACTION = "com.umitsilwal.stegogram.CONNECT";

    private TextView logTextView;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logTextView = findViewById(R.id.log_text);
        logTextView.setText(R.string.app_connecting);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent connIntent = new Intent(getApplicationContext(), NetworkConnectionService.class);
        connIntent.setAction(CONNECT_ACTION);
        startService(connIntent);

        mBroadcastReceiver = new NetworkBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                String action = intent.getAction();
                switch (action){
                    case NetworkConnectionService.CONNECTED:
                        logTextView.setText(R.string.app_login);
                        break;
                    case NetworkConnectionService.AUTHENTICATED:
                        finish();
                        break;
                    case NetworkConnectionService.AUTHENTICATION_FAILED:
                        context.startActivity(new Intent(context, LoginActivity.class));
                        finish();
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
