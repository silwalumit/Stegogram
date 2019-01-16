package com.umitsilwal.stegogram;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListHolder> {

    private Context context;
    private LinkedList<ChatListData> chatListData;

    public ChatListAdapter(Context context, LinkedList<ChatListData> data) {
        this.context = context;
        this.chatListData = data;
    }

    @NonNull
    @Override
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.chat_list,parent,false);
        return new ChatListAdapter.ChatListHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder holder, int position) {
        holder.mSender.setText(chatListData.get(position).getmSender());
        holder.mContactDetail.setText(chatListData.get(position).getmDetails());
        holder.mContactTime.setText(chatListData.get(position).getmTime());
        holder.mIcon.setText(chatListData.get(position).getmSender().substring(0, 1));
        Random mRandom = new Random();
        int color = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
        ((GradientDrawable) holder.mIcon.getBackground()).setColor(color);
    }

    @Override
    public int getItemCount() {
        return chatListData.size();
    }

    public class ChatListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mIcon;
        public final TextView mSender;
        public final TextView mContactDetail;
        public final TextView mContactTime;
        private final ChatListAdapter mAdapter;

        public ChatListHolder(View view, ChatListAdapter chatListAdapter) {
            super(view);
            mIcon = itemView.findViewById(R.id.textViewIcon);
            mSender = itemView.findViewById(R.id.textContactSender);
            mContactDetail = itemView.findViewById(R.id.textContactDetails);
            mContactTime = itemView.findViewById(R.id.textContactTime);
            mAdapter = chatListAdapter;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();
            Intent intent = new Intent(context, MessageListActivity.class);
            ChatListData chatInfo = chatListData.get(pos);
            intent.putExtra(ContactListAdapter.ContactListHolder.EXTRA_TITLE, chatInfo.getmSender());
            context.startActivity(intent);
        }
    }
}
