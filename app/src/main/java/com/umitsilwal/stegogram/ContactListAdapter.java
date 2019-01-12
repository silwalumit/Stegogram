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
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;


public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactListHolder> {

    private LinkedList<ContactsData> contactData;
    private Context context;
    private LayoutInflater mInflater;

    public ContactListAdapter(Context context, LinkedList<ContactsData> contactData){
        this.contactData = contactData;
        this.context = context;
        //this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ContactListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.contact_list,parent,false);
       // View view = mInflater.inflate(R.layout.contact_list, parent, false);
        return new ContactListHolder(view, this);
    }


    @Override
    public void onBindViewHolder(@NonNull ContactListHolder holder, int position) {
        holder.mSender.setText(contactData.get(position).getmSender());
        holder.mContactTitle.setText(contactData.get(position).getmTitle());
        holder.mContactDetail.setText(contactData.get(position).getmDetails());
        holder.mContactTime.setText(contactData.get(position).getmTime());
        holder.mIcon.setText(contactData.get(position).getmSender().substring(0, 1));
        Random mRandom = new Random();
        int color = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
        ((GradientDrawable) holder.mIcon.getBackground()).setColor(color);
    }

    @Override
    public int getItemCount() {
        return contactData.size();
    }

    public class ContactListHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        public final String EXTRA_TITLE = "com.umitsilwal.stegogram.TITLE";
        public final TextView mIcon;
        public final TextView mSender;
        public final TextView mContactTitle;
        public final TextView mContactDetail;
        public final TextView mContactTime;
        final ContactListAdapter mAdapter;

        public ContactListHolder(View itemView, ContactListAdapter mAdapter) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.textViewIcon);
            mSender = itemView.findViewById(R.id.textContactSender);
            mContactTitle = itemView.findViewById(R.id.textContactTitle);
            mContactDetail = itemView.findViewById(R.id.textContactDetails);
            mContactTime = itemView.findViewById(R.id.textContactTime);
            this.mAdapter = mAdapter;

            itemView.setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();
            Intent intent = new Intent(context, MessageListActivity.class);
            ContactsData contactInfo = contactData.get(pos);
            intent.putExtra(EXTRA_TITLE, contactInfo.getmTitle());
            context.startActivity(intent);
        }
    }
}
