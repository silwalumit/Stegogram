package com.umitsilwal.stegogram;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.net.InetAddress;

public class StegoConnection implements ConnectionListener {

    public enum CONNECTIONSTATE{
        CONNECTED, DISCONNECTED, DISCONNECTING, AUTHENTICATED,CONNECTING
    }

    public enum LOGINSTATUS{
        LOGGED_IN, LOGGED_OUT
    }

    public static final String TAG = "com.umit.debug";
    private Context mAppContext;
    private String mUsername;
    private String mPassword;
    private AbstractXMPPConnection mConnection;

    public StegoConnection(Context context) {
        mAppContext = context.getApplicationContext();

        getUsernameAndPassword();
    }

    private void getUsernameAndPassword(){
        mUsername = PreferenceManager.getDefaultSharedPreferences(mAppContext)
                .getString("xmpp_username", null);

        mPassword = PreferenceManager.getDefaultSharedPreferences(mAppContext)
                .getString("xmpp_password", null);
    }

    public void connect() throws IOException, InterruptedException, XMPPException, SmackException {
        Log.d(TAG, "Connecting");
        XMPPTCPConnectionConfiguration builder = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(mUsername, mPassword)
                .setHostAddress(InetAddress.getByName("192.168.100.16"))
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(5222)
                .setXmppDomain("chat")
                .setDebuggerEnabled(true)
                .setSendPresence(false)
                .setResource("Resource")
                .build();

        mConnection = new XMPPTCPConnection(builder);
        mConnection.addConnectionListener(this);
        mConnection.connect();

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting");
        if (mConnection != null) {
            mConnection.disconnect();
        }
        mConnection = null;
    }

    public void login() throws InterruptedException, XMPPException, SmackException, IOException {
        if (mConnection != null) {
            getUsernameAndPassword();
            if (mUsername != null && mPassword != null) {
                mConnection.login(mUsername, mPassword);
            } else {
                Log.d(TAG, "Username = "+mUsername+" Password = "+mPassword);
                Log.d(TAG, "Either Username or Password empty.");
                Intent i = new Intent(NetworkConnectionService.AUTHENTICATION_FAILED);
                i.setPackage(mAppContext.getPackageName());
                mAppContext.sendBroadcast(i);
            }
        }else{
            Intent i = new Intent(NetworkConnectionService.NO_CONNECTION);
            i.setPackage(mAppContext.getPackageName());
            mAppContext.sendBroadcast(i);
        }
    }

    @Override
    public void reconnectingIn(int milisec) {
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.CONNECTING;
    }
    @Override
    public void reconnectionSuccessful() {
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.CONNECTED;
    }
    @Override
    public void reconnectionFailed(Exception e) {
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.DISCONNECTED;
    }

    /**
     * Notification that the connection has been successfully connected to the remote endpoint (e.g. the XMPP server).
     * <p>
     * Note that the connection is likely not yet authenticated and therefore only limited operations like registering
     * an account may be possible.
     * </p>
     *
     * @param connection the XMPPConnection which successfully connected to its endpoint.
     */
    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TAG, "Connected");
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.CONNECTED;

        Intent i = new Intent(NetworkConnectionService.CONNECTED);
        i.setPackage(mAppContext.getPackageName());
        mAppContext.sendBroadcast(i);
    }

    /**
     * Notification that the connection has been authenticated.
     *
     * @param connection the XMPPConnection which successfully authenticated.
     * @param resumed    true if a previous XMPP session's stream was resumed.
     */
    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "Authenticated");
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.AUTHENTICATED;

        Intent i = new Intent(NetworkConnectionService.AUTHENTICATED);
        i.setPackage(mAppContext.getPackageName());
        mAppContext.sendBroadcast(i);
    }

    /**
     * Notification that the connection was closed normally.
     */
    @Override
    public void connectionClosed() {
        Log.d(TAG, "Closed");
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.DISCONNECTED;
    }

    /**
     * Notification that the connection was closed due to an exception. When
     * abruptly disconnected it is possible for the connection to try reconnecting
     * to the server.
     *
     * @param e the exception.
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TAG, "Closed with error.");
        NetworkConnectionService.sConnectionState = CONNECTIONSTATE.DISCONNECTED;
        e.printStackTrace();
    }
}
