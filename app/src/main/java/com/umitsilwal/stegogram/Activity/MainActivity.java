package com.umitsilwal.stegogram.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;

public class MainActivity extends AppCompatActivity {

    private final Context mContext;
    private TextView logTextView;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean logged_in;

    public MainActivity(){
        mContext = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logTextView = findViewById(R.id.log_text);
        logTextView.setText(R.string.app_connecting);

        //If user credentials has been saved, use that credentials to log in.
        logged_in = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("xmpp_logged_in", false);
        if(logged_in){
            Intent intent = new Intent(this, StegoConnectionService.class);
            startService(intent);
        }else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action){
                    case StegoConnectionService.ACTION_CONNECTED:
                        Intent intent1;
                        if(logged_in){
                            intent1 = new Intent(context, StegoConnectionService.class);
                            intent1.setAction(StegoConnectionService.ACTION_LOGIN);
                            startService(intent1);
                        }else{
                            intent1 = new Intent(context, LoginActivity.class);
                            startActivity(intent1);
                            finish();
                        }
                    case StegoConnectionService.ACTION_AUTHENTICATED:
                        startActivity(new Intent(context, HomeActivity.class));
                        finish();
                        break;
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }


}
