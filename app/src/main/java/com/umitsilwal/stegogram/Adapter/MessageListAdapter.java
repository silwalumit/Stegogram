package com.umitsilwal.stegogram.Adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;
import com.umitsilwal.stegogram.StegoMessage;

import java.util.LinkedList;

public class MessageListAdapter extends RecyclerView.Adapter{
    private Context context;
    private String currentUser;
    private LinkedList<StegoMessage> messageList;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_RECEIVED = 4;

    public MessageListAdapter(Context context, LinkedList<StegoMessage> messages){
        this.context = context;
        this.messageList = messages;
        this.currentUser = PreferenceManager.getDefaultSharedPreferences(this.context)
                .getString("xmpp_username", null);
        Log.d(StegoConnectionService.TAG, String.valueOf(messageList.size()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        StegoMessage message = messageList.get(position);
        if (message.getSenderJID().contentEquals(currentUser)) {
            // If the current user is the sender of the message
            if(message.getMessageType() == StegoMessage.TYPE.IMAGE)
                return VIEW_TYPE_IMAGE_MESSAGE_SENT;
            else
                return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            if(message.getMessageType() == StegoMessage.TYPE.IMAGE)
                return VIEW_TYPE_IMAGE_MESSAGE_RECEIVED;
            else
                return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(context).
                    inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if(viewType == VIEW_TYPE_IMAGE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(context)
                    .inflate(R.layout.image_message_received, parent, false);
            return new ReceivedImageMessageHolder(view);
        } else if(viewType == VIEW_TYPE_IMAGE_MESSAGE_SENT){
            view = LayoutInflater.from(context)
                    .inflate(R.layout.image_message_sent, parent, false);
            return new SentImageMessageHolder(view);
        } else{
            view = LayoutInflater.from(context)
                    .inflate(R.layout.message_sent, parent, false);
            return new SentMessageHolder(view);
        }
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StegoMessage message =  messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_MESSAGE_SENT:
                ((SentImageMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_MESSAGE_RECEIVED:
                ((ReceivedImageMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView messageText, timeText, nameText;
        TextView profileImage;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            profileImage =  itemView.findViewById(R.id.image_message_profile);
        }

        void bind(StegoMessage message) {
            messageText.setText(message.getBody());
            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getDate());
            profileImage.setText(message.getSenderJID().substring(0,1).toUpperCase());
        }

    }

    public class ReceivedImageMessageHolder extends ReceivedMessageHolder{

        ImageView image;
        Button decodeMessageBtn;

        public ReceivedImageMessageHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.message_image);
            decodeMessageBtn = itemView.findViewById(R.id.show_msg_btn);
            messageText.setVisibility(View.GONE);

            decodeMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageText.setVisibility(View.VISIBLE);
                    decodeMessageBtn.setVisibility(View.GONE);
                }
            });
        }

        @Override
        void bind(StegoMessage message) {
            super.bind(message);
            image.setImageBitmap(message.getImage());
            if (message.getBody() == null || message.getBody().isEmpty()){
                decodeMessageBtn.setVisibility(View.GONE);
            }
        }
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }
        void bind(StegoMessage message) {
            messageText.setText(message.getBody());
            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getDate());
        }
    }

    public class SentImageMessageHolder extends SentMessageHolder{

        ImageView image;
        Button decodeMessageBtn;

        public SentImageMessageHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.message_image);
            decodeMessageBtn = itemView.findViewById(R.id.show_msg_btn);
            messageText.setVisibility(View.GONE);

            decodeMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageText.setVisibility(View.VISIBLE);
                    decodeMessageBtn.setVisibility(View.GONE);
                }
            });
        }

        @Override
        void bind(StegoMessage message) {
            super.bind(message);
            image.setImageBitmap(message.getImage());
            if (message.getBody().length() <= 0 ){
                decodeMessageBtn.setVisibility(View.GONE);
            }
        }
    }
}
