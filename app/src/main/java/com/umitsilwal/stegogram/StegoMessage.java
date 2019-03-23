package com.umitsilwal.stegogram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jivesoftware.smack.packet.Message;

import java.io.Serializable;

public class StegoMessage implements Serializable {

    public enum TYPE { TEXT, IMAGE }

    protected Message message;
    protected String senderJID;
    protected Bitmap image;
    protected TYPE messageType;
    protected String date;

    public StegoMessage(String sender, String message, String image, String date){
        this.message = new Message();
        this.message.setBody(message);
        this.senderJID = sender;
        if(image != null) {
            this.image = BitmapFactory.decodeFile(image);
            messageType = TYPE.IMAGE;
        }else{
            this.image = null;
            messageType = TYPE.TEXT;
        }
        this.date = date;
    }

    public StegoMessage(Message message, String sender, String date){
        this.message = message;
        senderJID = sender;
        image = null;
        messageType = TYPE.TEXT;
        this.date = date;
    }

    public StegoMessage(Message message, String sender, Bitmap image, String date){
        this.message = message;
        this.senderJID = sender;
        this.image = image;
        messageType = TYPE.IMAGE;
        this.date = date;
    }

    public String getDate(){
        return date;
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
