package com.umitsilwal.stegogram.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.umitsilwal.stegogram.ContactData;
import com.umitsilwal.stegogram.Adapter.ContactListAdapter;
import com.umitsilwal.stegogram.Adapter.MessageListAdapter;
import com.umitsilwal.stegogram.Database.DBHelper;
import com.umitsilwal.stegogram.NetworkBroadcastReceiver;
import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;
import com.umitsilwal.stegogram.StegoMessage;
import com.umitsilwal.stegogram.Utils.HelperMethods;

import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.util.LinkedList;

public class MessageListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_GET_SINGLE_FILE = 1;
    private Uri imagePath = null;
    private MessageListAdapter messageAdapter;
    private EditText messageContainer;
    private ContactData receiver;
    private BroadcastReceiver mBroadcastReceiver;
    private String currentUser;
    private LinkedList<StegoMessage> messageList = new LinkedList<>();

    private ImageView imagePreview;
    private FloatingActionButton cancel_btn;

    private DBHelper mMessagesDB;
    private RecyclerView messageRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.currentUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("xmpp_username", null);

        setContentView(R.layout.activity_message_list);

        Intent starter = getIntent();
        receiver = (ContactData) starter.getSerializableExtra(ContactListAdapter.ContactListHolder.RECEIVER);

        mMessagesDB = new DBHelper(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue);
        toolbar.setTitle(receiver.getName());

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }catch (NullPointerException e){
            Log.d(StegoConnectionService.TAG, e.getMessage());
        }

        imagePreview = findViewById(R.id.image_preview);
        cancel_btn = findViewById(R.id.cancel_btn);
        imagePreview.setVisibility(View.GONE);
        cancel_btn.hide();

        messageRecycler = findViewById(R.id.reyclerview_message_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        messageRecycler.setLayoutManager(layoutManager);
        messageAdapter = new MessageListAdapter(getApplicationContext(), messageList);
        messageRecycler.setAdapter(messageAdapter);
        messageRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    messageRecycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            messageRecycler.smoothScrollToPosition(messageAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });

        ImageButton sendBtn = findViewById(R.id.button_chatbox_send);
        ImageButton selectImageBtn = findViewById(R.id.image_button);

        //set event handler for click event
        sendBtn.setOnClickListener(this);
        selectImageBtn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        messageContainer = findViewById(R.id.edittext_chatbox);

        Cursor messages = mMessagesDB.GetMessageList(currentUser, receiver.getJid().asUnescapedString());
        while (messages.moveToNext()){
            String sender = messages.getString(messages.getColumnIndex(DBHelper.MessagesTableInfo.COL_SENDER));
            String receiver = messages.getString(messages.getColumnIndex(DBHelper.MessagesTableInfo.COL_RECEIVER));
            String message = messages.getString(messages.getColumnIndex(DBHelper.MessagesTableInfo.COL_MESSAGE));
            String image = messages.getString(messages.getColumnIndex(DBHelper.MessagesTableInfo.COL_IMAGE));
            String date = messages.getString(messages.getColumnIndex(DBHelper.MessagesTableInfo.COL_DATE));
            StegoMessage msg = new StegoMessage(sender, message, image, date);
            addMessage(msg);
        }
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
                    case StegoConnectionService.ACTION_MESSAGE_RECEIVED:
                        StegoMessage message = (StegoMessage) intent.getSerializableExtra("message");
                        if(message.getSenderJID().contentEquals(receiver.getJid().asUnescapedString())) {
                            addMessage(message);
                        }else{
                            Toast.makeText(getApplicationContext(), message.getSenderJID()+" says "+message.getBody(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case StegoConnectionService.ACTION_IMAGE_RECEIVED:
                        StegoMessage imageMessage = (StegoMessage) intent.getSerializableExtra("message");
                        if(imageMessage.getSenderJID().contentEquals(receiver.getJid().asUnescapedString())) {
                            addMessage(imageMessage);
                        }else{
                            Toast.makeText(getApplicationContext(), imageMessage.getSenderJID()+" sent a image.", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_AUTH_FAILED);
        intentFilter.addAction(StegoConnectionService.ACTION_NO_CONN);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        intentFilter.addAction(StegoConnectionService.ACTION_MESSAGE_RECEIVED);
        intentFilter.addAction(StegoConnectionService.ACTION_IMAGE_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_chatbox_send:
                view.setEnabled(false);
                sendMessage();
                view.setEnabled(true);
                break;
            case R.id.image_button:
                Intent pickImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImage.setType("image/*");
                if(pickImage.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(Intent.createChooser(pickImage, "Select Image"), REQUEST_GET_SINGLE_FILE);
                break;
            case R.id.cancel_btn:
                cancelImage();
                break;
        }
    }

    private void cancelImage(){
        imagePath = null;
        imagePreview.setImageURI(null);
        imagePreview.setVisibility(View.GONE);
        cancel_btn.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("com.umit.debug", "File");
        try{
            if(resultCode == RESULT_OK){
                if(requestCode == REQUEST_GET_SINGLE_FILE){
                    imagePath = data.getData();
                    imagePreview.setImageURI(imagePath);
                    imagePreview.setVisibility(View.VISIBLE);
                    cancel_btn.show();
                }
            }
        }catch(Exception e){
            //Log.e("FileSelectorActivity", "File select error", e);
        }

    }

    public void addMessage(StegoMessage message) {
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size());
        messageRecycler.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }


    private void sendMessage() {
        messageContainer.setEnabled(false);
        String message = messageContainer.getText().toString().trim();
        if(message.length() > 0 && imagePath == null){
            //Log.d("com.umit.debug", "Sending message: "+message);
            Intent sendMessage = new Intent(getApplicationContext(), StegoConnectionService.class);
            sendMessage.setAction(StegoConnectionService.ACTION_SEND_MESSAGE);
            sendMessage.putExtra("message_body", message);
            sendMessage.putExtra("receiver", receiver.getJid().asUnescapedString());
            startService(sendMessage);

            Message temp = new Message();
            temp.setBody(message);
            addMessage(new StegoMessage(temp, currentUser, HelperMethods.NowString()));
            messageContainer.setText("");
        }else if(imagePath != null){
            Intent sendImage = new Intent(this, StegoConnectionService.class);
            sendImage.setAction(StegoConnectionService.ACTION_SEND_IMAGE);
            sendImage.putExtra("receiver", receiver.getJid().asUnescapedString());
            sendImage.putExtra("filePath", imagePath.toString());
            sendImage.putExtra("message_body", message);
            startService(sendImage);

            try {
                Message temp = new Message();
                temp.setBody(message);
                StegoMessage imageMessage = new StegoMessage(temp, currentUser,
                        MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath), HelperMethods.NowString());
                addMessage(imageMessage);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            cancelImage();
            messageContainer.setText("");
        }else{
            messageContainer.requestFocus();
        }
        messageContainer.setEnabled(true);
    }
}
