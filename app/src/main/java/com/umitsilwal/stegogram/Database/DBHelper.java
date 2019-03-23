package com.umitsilwal.stegogram.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.umitsilwal.stegogram.ChatListData;

import java.util.LinkedList;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stegogram.db";
    private static final String TAG = "DBHelper";

    public static class ContactsTableInfo {
        static final String TABLE_NAME = "contacts";
        public static final String COL_ID = "id";
        public static final String COL_USER = "user_jid";
        public static final String COL_CONTACT_NAME = "contact_name";
        public static final String COL_CONTACT = "contact_jid";
    }

    public static class MessagesTableInfo{
        static final String TABLE_NAME = "messages";
        public static final String COL_ID = "id";
        public static final String COL_SENDER = "sender_jid";
        public static final String COL_RECEIVER = "receiver_jid";
        public static final String COL_MESSAGE = "message";
        public static final String COL_IMAGE = "image";
        public static final String COL_DATE = "date";
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createContactsTable = "CREATE TABLE "+ContactsTableInfo.TABLE_NAME+"("+
                ContactsTableInfo.COL_ID + " integer primary key autoincrement, "+
                ContactsTableInfo.COL_USER + " TEXT, "+
                ContactsTableInfo.COL_CONTACT + " TEXT, "+
                ContactsTableInfo.COL_CONTACT_NAME + " TEXT)";

        String createMessagesTable = "CREATE TABLE "+MessagesTableInfo.TABLE_NAME+"("+
                MessagesTableInfo.COL_ID +" integer primary key autoincrement,"+
                MessagesTableInfo.COL_SENDER +" text,"+
                MessagesTableInfo.COL_RECEIVER +" text,"+
                MessagesTableInfo.COL_MESSAGE +" text,"+
                MessagesTableInfo.COL_IMAGE +" text,"+
                MessagesTableInfo.COL_DATE+" datetime default current_timestamp)";

        db.execSQL(createContactsTable);
        db.execSQL(createMessagesTable);
        //Log.d(TAG, "Database Created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Log.d(TAG, "Database Upgrade.");
        db.execSQL("DROP TABLE IF EXISTS "+ContactsTableInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+MessagesTableInfo.TABLE_NAME);
        onCreate(db);
    }

    public boolean AddContact(String user_jid, String contact_jid, String contact_name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactsTableInfo.COL_USER, user_jid);
        values.put(ContactsTableInfo.COL_CONTACT, contact_jid);
        values.put(ContactsTableInfo.COL_CONTACT_NAME, contact_name);

        //Log.d(TAG, "Adding: user_jid = "+user_jid+", contact_jid = "+contact_jid);

        long result = db.insert(ContactsTableInfo.TABLE_NAME, null, values);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean ContactExists(String user_jid, String contact_jid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.query(ContactsTableInfo.TABLE_NAME,
                new String[] {ContactsTableInfo.COL_ID},
                ContactsTableInfo.COL_USER +"= ? AND "+ContactsTableInfo.COL_CONTACT+"= ?",
                new String[] {user_jid, contact_jid}, null, null, null);
        return result.getCount() > 0;
    }

    public Cursor GetContact(String user_jid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.query(ContactsTableInfo.TABLE_NAME,
                new String[] {ContactsTableInfo.COL_CONTACT, ContactsTableInfo.COL_CONTACT_NAME},
                ContactsTableInfo.COL_USER +"= ?",
                new String[] {user_jid}, null, null, null);
        return result;
    }

    public boolean AddMessage(String sender, String receiver, String message, String image){
        //Log.d(TAG, "Adding message: "+sender+" => "+receiver+": "+message);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessagesTableInfo.COL_SENDER, sender);
        values.put(MessagesTableInfo.COL_RECEIVER, receiver);
        values.put(MessagesTableInfo.COL_MESSAGE, message);
        values.put(MessagesTableInfo.COL_IMAGE, image);
        long result = db.insert(MessagesTableInfo.TABLE_NAME, null, values);
        if(result == -1){
            return false;
        }
        return true;
    }

    public Cursor GetMessageList(String sender, String receiver){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MessagesTableInfo.TABLE_NAME,
                new String[]{MessagesTableInfo.COL_SENDER, MessagesTableInfo.COL_RECEIVER, MessagesTableInfo.COL_MESSAGE, MessagesTableInfo.COL_IMAGE, MessagesTableInfo.COL_DATE},
                "("+MessagesTableInfo.COL_SENDER+" = ? AND "+MessagesTableInfo.COL_RECEIVER+" = ? ) OR "+"("+MessagesTableInfo.COL_SENDER+" = ? AND "+MessagesTableInfo.COL_RECEIVER+" = ? )",
                new String[]{ sender, receiver, receiver, sender },
                null,
                null,
                MessagesTableInfo.COL_DATE+" ASC"
        );
        return cursor;
    }

    public LinkedList<ChatListData> GetChatList(String user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select "+MessagesTableInfo.COL_RECEIVER+" as contact from "+MessagesTableInfo.TABLE_NAME+" where "+MessagesTableInfo.COL_SENDER+" = ? group by "+MessagesTableInfo.COL_RECEIVER+" union " +
                "select "+MessagesTableInfo.COL_SENDER+" as contact from "+MessagesTableInfo.TABLE_NAME+" where "+MessagesTableInfo.COL_RECEIVER+" = ? group by "+MessagesTableInfo.COL_SENDER, new String[]{user, user});
        LinkedList<ChatListData> chatLists = new LinkedList<>();
        while (cursor.moveToNext()){
            String contact = cursor.getString(cursor.getColumnIndex("contact"));
            Cursor messages = db.rawQuery("select * from "+MessagesTableInfo.TABLE_NAME+" where (sender_jid = ? and receiver_jid=?) " +
                    "or (receiver_jid = ? and sender_jid=?) order by date desc limit 1", new String[] {user, contact, user, contact});
            while (messages.moveToNext()){
                String sender = messages.getString(messages.getColumnIndex(MessagesTableInfo.COL_SENDER));
                String receiver = messages.getString(messages.getColumnIndex(MessagesTableInfo.COL_RECEIVER));
                String message = messages.getString(messages.getColumnIndex(MessagesTableInfo.COL_MESSAGE));
                String image = messages.getString(messages.getColumnIndex(MessagesTableInfo.COL_IMAGE));
                String date = messages.getString(messages.getColumnIndex(MessagesTableInfo.COL_DATE));
                ChatListData chat = new ChatListData();
                if(sender.contentEquals(user)){
                    chat.setSender(receiver);
                }else{
                    chat.setSender(sender);
                }
                if(image != null) {
                    chat.setDetails("Image");
                }else {
                    chat.setDetails(message);
                }
                chat.setTime(date);
                chatLists.add(chat);
            }
        }
        return chatLists;
    }

}
