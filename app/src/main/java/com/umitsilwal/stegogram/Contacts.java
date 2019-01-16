package com.umitsilwal.stegogram;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

public class Contacts extends Fragment {

    private LinkedList<ContactsData> mContactList = new LinkedList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContactsData mContact = new ContactsData("Sam", "sam@gmail.com");
        mContactList.add(mContact);
        mContact = new ContactsData("John", "john@gmail.com");
        mContactList.add(mContact);
        mContact = new ContactsData("Adam", "adam23@gmail.com");
        mContactList.add(mContact);
        mContact = new ContactsData("Josh", "josh_12@yahoo.com");
        mContactList.add(mContact);
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
            recyclerView.setAdapter(new ContactListAdapter(context, mContactList));
            recyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            //recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        return view;
    }
}
