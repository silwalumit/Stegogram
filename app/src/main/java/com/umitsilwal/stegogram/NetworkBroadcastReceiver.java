package com.umitsilwal.stegogram;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkBroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        try {
            switch (action) {
                case StegoConnectionService.ACTION_NO_CONN:
                    Intent connIntent = new Intent(context, StegoConnectionService.class);
                    connIntent.setAction(StegoConnectionService.ACTION_CONNECT);
                    context.startService(connIntent);
                    break;
            }
        }catch (NullPointerException e){
            Log.d(StegoConnectionService.TAG, e.getMessage());
        }
    }
}
