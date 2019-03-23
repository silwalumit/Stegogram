package com.umitsilwal.stegogram;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.umitsilwal.stegogram.Database.DBHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StegoConnectionService extends Service {
    public static final String TAG = "ConnectionService";

    public static final String ACTION_AUTHENTICATED = BuildConfig.APPLICATION_ID + ".ACTION_AUTHENTICATED";
    public static final String ACTION_CONNECTED = BuildConfig.APPLICATION_ID + ".ACTION_CONNECTED";
    public static final String ACTION_AUTH_FAILED = BuildConfig.APPLICATION_ID + ".ACTION_AUTH_FAILED";
    public static final String ACTION_NO_CONN = BuildConfig.APPLICATION_ID + ".ACTION_NO_CONN";
    public static final String ACTION_ROSTER_LOADED = BuildConfig.APPLICATION_ID + ".ACTION_ROSTER_LOADED";
    public static final String ACTION_MESSAGE_RECEIVED = BuildConfig.APPLICATION_ID + ".ACTION_MESSAGE_RECEIVED";
    public static final String ACTION_IMAGE_RECEIVED =  BuildConfig.APPLICATION_ID + ".ACTION_IMAGE_RECEIVED";
    public static final String ACTION_CONTACT_ADDED = BuildConfig.APPLICATION_ID + ".ACTION_CONTACT_ADDED";
    public static final String ACTION_CONTACT_ADD_FAILED = BuildConfig.APPLICATION_ID + ".ACTION_CONTACT_ADD_FAILED";

    public static final String ACTION_SEND_MESSAGE = BuildConfig.APPLICATION_ID + ".ACTION_SEND_MESSAGE";
    public static final String ACTION_SEND_IMAGE = BuildConfig.APPLICATION_ID + ".ACTION_SEND_IMAGE";
    public static final String ACTION_GET_CONTACT = BuildConfig.APPLICATION_ID + ".ACTION_GET_CONTACT";
    public static final String ACTION_LOGOUT = BuildConfig.APPLICATION_ID + ".ACTION_LOGOUT";
    public static final String ACTION_LOGIN = BuildConfig.APPLICATION_ID + ".ACTION_LOGIN";
    public static final String ACTION_CONNECT = BuildConfig.APPLICATION_ID + ".ACTION_CONNECT";
    public static final String ACTION_ADD_CONTACT = BuildConfig.APPLICATION_ID + ".ACTION_ADD_CONTACT";

    public static final String ACTION_RELOAD_ROSTER = BuildConfig.APPLICATION_ID + ".ACTION_RELOAD_ROSTER";


    public static StegoConnection.CONNECTION_STATE sConnectionState;
    public static StegoConnection.LOGIN_STATUS sLogInStatus;
    private StegoConnection mConnection;
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;

    public StegoConnectionService()
    {
        Log.d(TAG, "Service Instantiated");
        mConnection = null;
    }

    public static StegoConnection.CONNECTION_STATE getState()
    {
        if (sConnectionState == null)
        {
            return StegoConnection.CONNECTION_STATE.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static StegoConnection.LOGIN_STATUS getLoggedInState()
    {
        if (sLogInStatus == null)
        {
            return StegoConnection.LOGIN_STATUS.LOGGED_OUT;
        }
        return sLogInStatus;
    }

    private void initConnection()
    {
        //Log.d(TAG, "Initializing Connection.");
        if( mConnection == null)
        {
            mConnection = new StegoConnection(this);
        }
        try
        {
            mConnection.connect();
        }catch (IOException |InterruptedException |SmackException |XMPPException e) {
            Intent i1 = new Intent(ACTION_NO_CONN);
            i1.setPackage(getPackageName());
            LocalBroadcastManager.getInstance(this).sendBroadcast(i1);
            //Log.d(TAG,"Something went wrong while connecting!");
            //e.printStackTrace();
            //stop the service all together
            stopSelf();
        }
    }

    private void start()
    {
        //Log.d(TAG, "Starting Service");
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
        }else{
            Intent i1 = new Intent(ACTION_CONNECTED);
            i1.setPackage(getPackageName());
            LocalBroadcastManager.getInstance(this).sendBroadcast(i1);
        }
    }

    private void authenticate()
    {
        //Log.d(TAG, "Authenticating");
        if(mActive)
        {
            if(mConnection != null){
                try{
                    mConnection.login();
                }catch (IOException |InterruptedException |SmackException |XMPPException e)
                {
                    Intent i1 = new Intent(ACTION_AUTH_FAILED);
                    i1.setPackage(getPackageName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i1);

                    //Log.d(TAG,"Could not login.");
                    //e.printStackTrace();
                    stopSelf();
                }
            }
        }else{
            start();
        }
    }

    private void stop()
    {
        //Log.d(TAG, "Stopping Service");
        mActive = false;
        if(mTHandler != null) {
            mTHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mConnection != null) {
                        mConnection.disconnect();
                    }
                }
            });
        }
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
            //Log.d(TAG, "Starting Service");
            switch (action) {
                case ACTION_LOGIN:
                    authenticate();
                    break;
                case ACTION_LOGOUT:
                    stop();
                    break;
                case ACTION_GET_CONTACT:
                    getRoster();
                    break;
                case ACTION_SEND_MESSAGE:
                    sendMessage(intent.getStringExtra("message_body"),
                            intent.getStringExtra("receiver"));
                    break;
                case ACTION_SEND_IMAGE:
                    sendImage(intent.getStringExtra("receiver"),
                            Uri.parse(intent.getStringExtra("filePath")),
                            intent.getStringExtra("message_body"));
                    break;
                case ACTION_ADD_CONTACT:
                    addContact(intent.getStringExtra("user_jid"),
                                intent.getStringExtra("user_nickname"));
                    break;
                default:
                    start();
            }
        } catch (NullPointerException e){
            start();
        }
        return Service.START_STICKY;
    }

    private void addContact(String userJid, String userNickname) {
        try {
            mConnection.addRosterEntry(userJid, userNickname);
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException | SmackException.NotLoggedInException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
            //e.printStackTrace();
            Intent i1 = new Intent(ACTION_CONTACT_ADD_FAILED);
            i1.setPackage(getPackageName());
            LocalBroadcastManager.getInstance(this).sendBroadcast(i1);
        }
    }

    private void sendImage(String jid, Uri imagePath, String messageToEncode){
        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            String fileName = "";
            Cursor cursor = getContentResolver().query(imagePath, filePathColumn, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            }
            cursor.close();

            File image = new File(getExternalFilesDir(null), fileName);
            FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(imagePath);
            FileOutputStream outputStream = new FileOutputStream(image);
            int b = 0;
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b);
            }
            outputStream.close();
            inputStream.close();

            if(messageToEncode.length() > 0) {
                image = Steganography.EncodeMessage(image, messageToEncode);
            }

            DBHelper messagesDB = new DBHelper(getApplicationContext());
            messagesDB.AddMessage(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getString("xmpp_username", null),
                    JidCreate.bareFrom(jid).asUnescapedString(),
                    messageToEncode, image.getPath());

            mConnection.sendImage(jid, image);
        } catch(IOException e){
            //e.printStackTrace();
            //Log.d(TAG, "Cannot read image.");
        }
    }

    private void sendMessage(String body, String jid) {
        //Log.d(TAG, "Sending Message");
        mConnection.sendMessage(body, jid);
    }

    private void getRoster() {
        //Log.d(TAG, "Getting Roster");
        mConnection.loadRoster();
    }

    @Override
    public void onDestroy() {
        //Log.d(TAG, "Destroying Service.");
        super.onDestroy();
        stop();
    }
}
