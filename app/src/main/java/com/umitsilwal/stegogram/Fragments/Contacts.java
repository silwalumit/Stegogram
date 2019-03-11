package com.umitsilwal.stegogram.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umitsilwal.stegogram.ContactData;
import com.umitsilwal.stegogram.Adapter.ContactListAdapter;
import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.Collection;
import java.util.LinkedList;

public class Contacts extends Fragment {

    private LinkedList<ContactData> mContactList = new LinkedList<>();
    private ContactListAdapter mListAdapter;
    private BroadcastReceiver mBroadcastReceiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ContactListAdapter(getContext(), mContactList);
        Intent getContactsIt = new Intent(getContext(), StegoConnectionService.class);
        getContactsIt.setAction(StegoConnectionService.ACTION_GET_CONTACT);
        getContext().startService(getContactsIt);

        mBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case StegoConnectionService.ACTION_ROSTER_LOADED:
                        Collection<RosterEntry> contacts = (Collection<RosterEntry>) intent.getSerializableExtra("contacts");
                        for (RosterEntry contact: contacts){
                            mContactList.add(new ContactData(contact));
                            mListAdapter.notifyItemInserted(mContactList.size());
                        }
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_AUTH_FAILED);
        intentFilter.addAction(StegoConnectionService.ACTION_NO_CONN);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        intentFilter.addAction(StegoConnectionService.ACTION_ROSTER_LOADED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            //recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        return view;
    }
}
