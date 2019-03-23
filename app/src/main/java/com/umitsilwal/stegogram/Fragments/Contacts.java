package com.umitsilwal.stegogram.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.umitsilwal.stegogram.Database.DBHelper;
import com.umitsilwal.stegogram.R;
import com.umitsilwal.stegogram.StegoConnectionService;

import java.util.LinkedList;

public class Contacts extends Fragment {

    private LinkedList<ContactData> mContactList = new LinkedList<>();
    private ContactListAdapter mListAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private DBHelper mDBHelper;
    private String mUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("xmpp_username", null);
        mDBHelper = new DBHelper(getContext());
        Cursor result = mDBHelper.GetContact(mUser);
        while (result.moveToNext()){
            String contact_jid = result.getString(result.getColumnIndex(DBHelper.ContactsTableInfo.COL_CONTACT));
            String contact_name = result.getString(result.getColumnIndex(DBHelper.ContactsTableInfo.COL_CONTACT_NAME));
            mContactList.add(new ContactData(contact_jid, contact_name));
        }

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
                        mContactList.clear();
                        mListAdapter.notifyDataSetChanged();
                        Cursor result = mDBHelper.GetContact(mUser);
                        while (result.moveToNext()){
                            String contact_jid = result.getString(result.getColumnIndex(DBHelper.ContactsTableInfo.COL_CONTACT));
                            String contact_name = result.getString(result.getColumnIndex(DBHelper.ContactsTableInfo.COL_CONTACT_NAME));
                            mContactList.add(new ContactData(contact_jid, contact_name));
                            mListAdapter.notifyItemInserted(mContactList.size());
                        }
                    break;
                    case StegoConnectionService.ACTION_RELOAD_ROSTER:
                        Intent getContactsIt = new Intent(context, StegoConnectionService.class);
                        getContactsIt.setAction(StegoConnectionService.ACTION_GET_CONTACT);
                        context.startService(getContactsIt);
                    break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(StegoConnectionService.ACTION_AUTHENTICATED);
        intentFilter.addAction(StegoConnectionService.ACTION_AUTH_FAILED);
        intentFilter.addAction(StegoConnectionService.ACTION_NO_CONN);
        intentFilter.addAction(StegoConnectionService.ACTION_CONNECTED);
        intentFilter.addAction(StegoConnectionService.ACTION_ROSTER_LOADED);
        intentFilter.addAction(StegoConnectionService.ACTION_RELOAD_ROSTER);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
