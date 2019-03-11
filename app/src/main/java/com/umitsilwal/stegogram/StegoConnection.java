package com.umitsilwal.stegogram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.umitsilwal.stegogram.Utils.Constants;
import com.umitsilwal.stegogram.Utils.HelperMethods;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.si.packet.StreamInitiation;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


public class StegoConnection implements ConnectionListener, IncomingChatMessageListener {

    private static final String TAG = "StegoConnection";

    private final Context mAppContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private AbstractXMPPConnection mConnection;
    private Roster roster;

    public enum CONNECTION_STATE {
        CONNECTED, DISCONNECTED, DISCONNECTING, AUTHENTICATED,CONNECTING
    }

    public enum LOGIN_STATUS {
        LOGGED_IN, LOGGED_OUT
    }

    public StegoConnection(Context context) {
        mAppContext = context.getApplicationContext();
        //If user has logged in for the first time, fetch credentials from preference
        getUsernameAndPassword();
        roster = null;
    }

    private void getUsernameAndPassword(){
        String jid = PreferenceManager.getDefaultSharedPreferences(mAppContext)
                .getString("xmpp_username", null);

        mPassword = PreferenceManager.getDefaultSharedPreferences(mAppContext)
                .getString("xmpp_password", null);

        // When you have a jabber id, get the username
        if(jid != null){
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@")[1];
        }else{
            mUsername = "";
            mServiceName = "chat.im";
        }
    }

    public void connect() throws IOException, InterruptedException, XMPPException, SmackException {
        Log.d(TAG, "Connecting");
        XMPPTCPConnectionConfiguration builder = XMPPTCPConnectionConfiguration.builder()
                .setHostAddress(InetAddress.getByName("192.168.0.104"))
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(5222)
                .setSendPresence(true)
                .setXmppDomain(mServiceName)
                .setResource("chat.im")
                .build();

        Log.d(TAG, "Username : "+mUsername);
        Log.d(TAG, "Password : "+mPassword);
        Log.d(TAG, "Server : " + mServiceName);

        //Set up the ui thread broadcast message receiver.
       // setupUiThreadBroadCastMessageReceiver();

        mConnection = new XMPPTCPConnection(builder);
        mConnection.addConnectionListener(this);
        mConnection.connect();

        Log.d(TAG, "Connected");

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        ReconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from " + mServiceName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        prefs.edit().putBoolean("xmpp_logged_in",false).apply();
        if (mConnection != null) {
            mConnection.disconnect();
        }
        mConnection = null;
    }

    public void login() throws InterruptedException, XMPPException, SmackException, IOException {
        if (mConnection != null) {
            Log.d(TAG, "Logging In.");
            getUsernameAndPassword();
            if (mUsername != null && mPassword != null) {
                mConnection.login(mUsername, mPassword);
                Presence presence = new Presence(Presence.Type.available);
                presence.setStatus("online");
                mConnection.sendStanza(presence);
                ChatManager manager = ChatManager.getInstanceFor(mConnection);
                manager.addIncomingListener(this);
                listenForFiles(this.mAppContext);

            } else {
                Log.d(TAG, "Username = "+mUsername+" Password = "+mPassword);
                Log.d(TAG, "Either Username or Password empty.");
                Intent i = new Intent(StegoConnectionService.ACTION_AUTH_FAILED);
                i.setPackage(mAppContext.getPackageName());
                LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
            }
        }else{
            Intent i = new Intent(StegoConnectionService.ACTION_NO_CONN);
            i.setPackage(mAppContext.getPackageName());
            LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
        }
    }

    public void loadRoster(){
        if(roster == null)
            roster = Roster.getInstanceFor(mConnection);
        Log.d(TAG, "Retrieving roster");
        Collection<RosterEntry> entries = roster.getEntries();
        Intent contactsIntent = new Intent(StegoConnectionService.ACTION_ROSTER_LOADED);
        contactsIntent.putExtra("contacts", (Serializable) entries);
        Log.d(TAG, "Rooster successfully retrieved");
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(contactsIntent);
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        Log.d(TAG, "Incoming Message");
        Log.d(TAG, "Sender: "+from+"\n Message: "+message.getBody());
        StegoMessage msg = new StegoMessage(message, from.asUnescapedString());

        Intent messageIntent = new Intent(StegoConnectionService.ACTION_MESSAGE_RECEIVED);
        messageIntent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(messageIntent);
    }

    public void sendMessage(String body, String jid) {
        try {
            Log.d(TAG, "Sending to: "+jid);
            Chat chat = ChatManager.getInstanceFor(mConnection).chatWith(JidCreate.entityBareFrom(jid));
            Message newMessage = new Message();
            newMessage.setBody(body);
            chat.send(newMessage);
            Log.d(TAG, "Message sent");
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Message failed to send.");
        }
    }

    public void listenForFiles(final Context context) {
        FileTransferManager manager = FileTransferManager.getInstanceFor(this.mConnection);
        manager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(final FileTransferRequest request) {
               // SmackConfiguration.setDefaultReplyTimeout(100000);
                new Thread(){
                    @Override
                    public void run() {
                        Log.d("com.umit.debug", "Receiving");
                        IncomingFileTransfer transfer = request.accept();
                        File file = new File(context.getExternalFilesDir(null), System.currentTimeMillis() + transfer.getFileName());

                        try{
                            transfer.receiveFile(file);
                            while(!transfer.isDone()) {
                                Log.d("com.umit.debug",transfer.getProgress() * 100 + "%");
                                if(transfer.getStatus().equals(FileTransfer.Status.error))
                                    Log.d("com.umit.debug","ERROR!!! "+ transfer.getError() + "");
                                else if(transfer.getStatus().equals(FileTransfer.Status.cancelled ) || transfer.getStatus().equals(FileTransfer.Status.refused))
                                    Log.d("com.umit.debug","Canceled: " + transfer.getError());
                                if(transfer.getException() != null)
                                    transfer.getException().printStackTrace();
                            }

                            if(transfer.getException() != null)
                                transfer.getException().printStackTrace();
                            if(transfer.getStatus().equals(FileTransfer.Status.error)){
                                Log.d("com.umit.debug","ERROR!!! "+ transfer.getError() + "");
                            }else if(transfer.getStatus().equals(FileTransfer.Status.cancelled ) || transfer.getStatus().equals(FileTransfer.Status.refused)){
                                Log.d("com.umit.debug","Canceled: " + transfer.getError());
                            }else{
                                Log.d("com.umit.debug","File Received!!!");
                                Log.d(TAG, "Sender: "+transfer.getPeer());

                                Message message = new Message();
                                Bitmap image = BitmapFactory.decodeFile(file.getPath());
                                String decodedMessage = Steganography.DecodeMessage(image);
                                if(decodedMessage.length() > 0) {
                                    byte[] bytes = HelperMethods.bitsStreamToByteArray(decodedMessage);
                                    decodedMessage = new String(bytes);
                                    message.setBody(decodedMessage);
                                }
                                message.setFrom(transfer.getPeer());
                                StegoMessage msg = new StegoMessage(message, JidCreate.bareFrom(transfer.getPeer().asUnescapedString()).asUnescapedString(), image);
                                Intent messageIntent = new Intent(StegoConnectionService.ACTION_IMAGE_RECEIVED);
                                messageIntent.putExtra("message", msg);
                                LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(messageIntent);
                            }
                        }catch (Exception e) {
                            Log.d("com.umit.debug",e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    public void sendImage(final String jid, final File image){
        final FileTransferManager manager = FileTransferManager.getInstanceFor(this.mConnection);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Presence receiver_presence = roster.getPresence(JidCreate.bareFrom(jid));
                    Log.d(TAG, "Sending to: " + receiver_presence.getFrom());
                    Log.d(TAG,JidCreate.entityFullFrom(receiver_presence.getFrom()).toString());
                    OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom(receiver_presence.getFrom()));
                    transfer.sendFile(image, "Image JPEG");
                    while(!transfer.isDone()){
                        Log.d("com.umit.debug",transfer.getProgress() * 100 + "% done.");
                        if(transfer.getStatus().equals(FileTransfer.Status.error))
                            Log.d("com.umit.debug","Error: " + transfer.getError());
                        else if(transfer.getStatus().equals(FileTransfer.Status.cancelled ) || transfer.getStatus().equals(FileTransfer.Status.refused))
                            Log.d("com.umit.debug","Canceled: " + transfer.getError());
                    }
                    if(transfer.getStatus().equals(FileTransfer.Status.error))
                        Log.d("com.umit.debug","Error: " + transfer.getError());
                    else if(transfer.getStatus().equals(FileTransfer.Status.cancelled ) || transfer.getStatus().equals(FileTransfer.Status.refused))
                        Log.d("com.umit.debug","Canceled: " + transfer.getError());
                    else{
                        Log.d(TAG, "File sent.");
                        image.delete();
                    }
                }catch(XmppStringprepException | SmackException e){
                    e.printStackTrace();
                }
            }
        }).start();
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
        StegoConnectionService.sConnectionState = CONNECTION_STATE.CONNECTED;

        Intent i = new Intent(StegoConnectionService.ACTION_CONNECTED);
        i.setPackage(mAppContext.getPackageName());
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
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
        StegoConnectionService.sConnectionState = CONNECTION_STATE.AUTHENTICATED;
        Intent i = new Intent(StegoConnectionService.ACTION_AUTHENTICATED);
        i.setPackage(mAppContext.getPackageName());
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
        Log.d(TAG, "Sent the broadcast that we are authenticated!");
    }

    /**
     * Notification that the connection was closed normally.
     */
    @Override
    public void connectionClosed() {
        Log.d(TAG, "Closed");
        StegoConnectionService.sConnectionState = CONNECTION_STATE.DISCONNECTED;
    }

    /**
     * Notification that the connection was closed due to an exception. When
     * abruptly disconnected it is possible for the connection to try reconnecting
     * to the server.
     *ask
     * @param e the exception.
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        StegoConnectionService.sConnectionState = CONNECTION_STATE.DISCONNECTED;
        Log.d(TAG, "Connnection Closed with error." + e.toString());
    }
}
