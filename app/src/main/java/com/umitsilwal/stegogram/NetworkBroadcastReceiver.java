package com.umitsilwal.stegogram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        try {
            switch (action) {
                case NetworkConnectionService.NO_CONNECTION:
                    Intent connIntent = new Intent(context, NetworkConnectionService.class);
                    connIntent.setAction(MainActivity.CONNECT_ACTION);
                    context.startService(connIntent);
                    break;
                case NetworkConnectionService.CONNECTED:
                    Intent loginIntent = new Intent(context, NetworkConnectionService.class);
                    loginIntent.setAction(MainActivity.LOGIN_ACTION);
                    context.startService(loginIntent);
                    break;
                case NetworkConnectionService.AUTHENTICATED:
                    context.startActivity(new Intent(context, HomeActivity.class));
                    break;
            }
        }catch (NullPointerException e){
            Log.d(NetworkConnectionService.TAG, e.getMessage());
        }
    }
}
