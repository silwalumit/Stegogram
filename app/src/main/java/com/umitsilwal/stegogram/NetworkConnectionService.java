package com.umitsilwal.stegogram;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class NetworkConnectionService extends Service {
    public static final String TAG = "com.umit.debug";

    public static final String AUTHENTICATED = "com.umitsilwal.stegogram.authenticated";
    public static final String CONNECTED = "com.umitsilwal.stegogram.connected";
    public static final String AUTHENTICATION_FAILED = "com.umitsilwal.stegogram.auth_error";
    public static final String NO_CONNECTION = "com.umitsilwal.stegogram.no_connection";

    public static StegoConnection.CONNECTIONSTATE sConnectionState;
    public static StegoConnection.LOGINSTATUS sLogInStatus;
    private StegoConnection mConnection;
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;

    public NetworkConnectionService()
    {
        Log.d(TAG, "Service Instantiated");
        mConnection = null;
    }

    public static StegoConnection.CONNECTIONSTATE getState()
    {
        if (sConnectionState == null)
        {
            return StegoConnection.CONNECTIONSTATE.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static StegoConnection.LOGINSTATUS getLoggedInState()
    {
        if (sLogInStatus == null)
        {
            return StegoConnection.LOGINSTATUS.LOGGED_OUT;
        }
        return sLogInStatus;
    }

    private void initConnection()
    {
        Log.d(TAG, "Initializing Connection.");
        if( mConnection == null)
        {
            mConnection = new StegoConnection(this);
        }
        try
        {
            mConnection.connect();
        }catch (IOException |InterruptedException |SmackException |XMPPException e)
        {
            Intent i1 = new Intent(NO_CONNECTION);
            i1.setPackage(getPackageName());
            this.sendBroadcast(i1);
            Log.d(TAG,"Could not connect.");
            e.printStackTrace();
            stopSelf();
        }
    }

    public void start()
    {
        Log.d(TAG, "Starting Service");
        if(!mActive)
        {
            mActive = true;
            if( mThread == null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }

    public void authenticate()
    {
        Log.d(TAG, "Authenticating");
        if(mActive)
        {
            if(mConnection != null){
                try{
                    mConnection.login();
                }catch (IOException |InterruptedException |SmackException |XMPPException e)
                {
                    Intent i1 = new Intent(AUTHENTICATION_FAILED);
                    i1.setPackage(getPackageName());
                    this.sendBroadcast(i1);

                    Log.d(TAG,"Could not login.");
                    e.printStackTrace();
                }
            }
        }else{
            start();
        }
    }

    public void stop()
    {
        Log.d(TAG, "Stopping Service");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });
        mThread = null;
        mTHandler = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();
            Log.d(TAG, "Starting Service for: "+action);
            switch (action) {
                case MainActivity.LOGIN_ACTION:
                    authenticate();
                    break;
                case HomeActivity.LOGOUT_ACTION:
                    stop();
                    break;
                default:
                    start();
            }
        } catch (NullPointerException e){
            start();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying Service.");
        super.onDestroy();
        stop();
    }
}
