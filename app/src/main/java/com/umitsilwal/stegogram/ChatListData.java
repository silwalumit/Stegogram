package com.umitsilwal.stegogram;

public class ChatListData {

    private String mSender;
    private String mDetails;
    private String mTime;

    public ChatListData(){

    }

    public ChatListData(String mSender, String mDetails, String mTime) {
        this.mSender = mSender;
        this.mDetails = mDetails;
        this.mTime = mTime;
    }

    public String getSender() {
        return mSender;
    }

    public String getDetails() {
        return mDetails;
    }

    public String getTime() {
        return mTime;
    }

    public void setSender(String mSender) {
        this.mSender = mSender;
    }

    public void setDetails(String mDetails) {
        this.mDetails = mDetails;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }
}
