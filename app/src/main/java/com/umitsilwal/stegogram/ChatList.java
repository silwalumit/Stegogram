package com.umitsilwal.stegogram;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;


public class ChatList extends Fragment {

    private LinkedList<ChatListData> mChatListData = new LinkedList<>();

    public ChatList() {
        ChatListData mContact = new ChatListData("Sam",
                "Let's go fishing with John and others. We will do some barbecue and have soo much fun",
                "10:42am");
        mChatListData.add(mContact);
        mContact = new ChatListData("John",
                "Fishing next Sunday??",
                "16:04pm");
        mChatListData.add(mContact);
        mContact = new ChatListData("Adam",
                "No one invited me for fishing...",
                "18:44pm");
        mChatListData.add(mContact);
        mContact = new ChatListData("Josh",
                "Don't forget our lunch at 3PM in Pizza hut",
                "01:04am");
        mChatListData.add(mContact);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new ChatListAdapter(context, mChatListData));
            recyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
        }
        return view;
    }

}
