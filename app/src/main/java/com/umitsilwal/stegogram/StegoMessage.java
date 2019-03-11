package com.umitsilwal.stegogram;

import android.graphics.Bitmap;

import org.jivesoftware.smack.packet.Message;

import java.io.Serializable;

public class StegoMessage implements Serializable {

    public enum TYPE { TEXT, IMAGE }

    protected Message message;
    protected String senderJID;
    protected Bitmap image;
    protected TYPE messageType;

    public StegoMessage(Message message, String sender){
        this.message = message;
        senderJID = sender;
        image = null;
        messageType = TYPE.TEXT;
    }

    public StegoMessage(Message message, String sender, Bitmap image){
        this.message = message;
        this.senderJID = sender;
        this.image = image;
        messageType = TYPE.IMAGE;
    }

    public String getBody(){
        return message.getBody();
    }

    public String getSenderJID(){
        return senderJID;
    }

    public Message getMessage(){
        return message;
    }

    public TYPE getMessageType() {
        return messageType;
    }

    public Bitmap getImage(){
        return image;
    }
}
