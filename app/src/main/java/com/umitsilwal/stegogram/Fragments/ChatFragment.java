package com.umitsilwal.stegogram.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umitsilwal.stegogram.Adapter.ChatListAdapter;
import com.umitsilwal.stegogram.ChatListData;
import com.umitsilwal.stegogram.Database.DBHelper;
import com.umitsilwal.stegogram.R;

import java.util.LinkedList;


public class ChatFragment extends Fragment {

    private LinkedList<ChatListData> mChatListData = new LinkedList<>();
    private DBHelper mMessagesDB;
    private String mUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("xmpp_username", null);

        mMessagesDB = new DBHelper(getContext());
        mChatListData = mMessagesDB.GetChatList(mUser);
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
