package com.umitsilwal.stegogram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umitsilwal.stegogram.Activity.MessageListActivity;
import com.umitsilwal.stegogram.BuildConfig;
import com.umitsilwal.stegogram.ContactData;
import com.umitsilwal.stegogram.R;

import java.util.LinkedList;
import java.util.Random;


public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactListHolder> {

    private LinkedList<ContactData> contactData;
    private Context context;

    public ContactListAdapter(Context context, LinkedList<ContactData> contactData){
        this.contactData = contactData;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.contact_list,parent,false);
        return new ContactListHolder(view, this);
    }


    @Override
    public void onBindViewHolder(@NonNull ContactListHolder holder, int position) {
        ContactData contact = contactData.get(position);
        holder.mSender.setText(contact.getName());
        holder.mSenderEmail.setText(contact.getJid());
        holder.mIcon.setText(contact.getName().substring(0, 1).toUpperCase());
        /*Random mRandom = new Random();
        int color = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
        ((GradientDrawable) holder.mIcon.getBackground()).setColor(color);*/
    }

    @Override
    public int getItemCount() {
        return contactData.size();
    }

    public class ContactListHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        public static final String RECEIVER = BuildConfig.APPLICATION_ID + ".MESSAGE_RECEIVER";
        public final TextView mIcon;
        public final TextView mSender;
        public final TextView mSenderEmail;
        final ContactListAdapter mAdapter;

        public ContactListHolder(View itemView, ContactListAdapter mAdapter) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.textViewIcon);
            mSender = itemView.findViewById(R.id.textContactSender);
            mSenderEmail = itemView.findViewById(R.id.textEmailSender);
            this.mAdapter = mAdapter;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();
            Intent intent = new Intent(context, MessageListActivity.class);
            ContactData contactInfo = contactData.get(pos);
            intent.putExtra(RECEIVER, contactInfo);
            context.startActivity(intent);
        }
    }
}
