package com.umitsilwal.stegogram;

class ChatListData {

    private String mSender;
    private String mDetails;
    private String mTime;

    public ChatListData(String mSender, String mDetails, String mTime) {
        this.mSender = mSender;
        this.mDetails = mDetails;
        this.mTime = mTime;
    }

    public String getmSender() {
        return mSender;
    }

    public String getmDetails() {
        return mDetails;
    }

    public String getmTime() {
        return mTime;
    }

}
