package com.example.emav1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String MESSAGE = "MESSAGE";
    public static final String MESSAGES_TABLE = MESSAGE + "S_TABLE";
    public static final String MESSAGES_IN_CACHE = MESSAGE + "S_IN_CACHE";
    public static final String MESSAGES_OUT_CACHE = MESSAGE + "S_OUT_CACHE";
    public static final String SENDER_ID = "SENDER_ID";
    public static final String TEMP_SENDER_ID = "TEMP_SENDER_ID";
    public static final String TEMP_RECEIVER_ID = "TEMP_RECEIVER_ID";
    public static final String TEMP_SMESSAGE = "TEMP_SMESSAGE";
    public static final String TEMP_RMESSAGE = "TEMP_RMESSAGE";
    public static final String RECEIVED = "RECEIVED";
    public static final String SENT = "SENT";
    Context context;

    public static final String CONTACTS_TABLE = "CONTACTS_TABLE";
    public static final String CONTACT_NAME = "CONTACT_NAME";
    public static final String CONTACT_NUMBER = "CONTACT_NUMBER";
    public static final String CONTACT_KEY = "CONTACT_KEY";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "t7ta.db", null, 1);
        this.context = context;
    }

    //called the first time a database is accessed. (CREATE DATABASE)
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createContactsTableStatement = "CREATE TABLE " + CONTACTS_TABLE + "(CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CONTACT_NAME + " VARCHAR(255), " +
                CONTACT_NUMBER + " VARCHAR(4) UNIQUE, " + CONTACT_KEY + " VARCHAR(255));";
        String createMessageTableStatement = "CREATE TABLE " + MESSAGES_TABLE + "(MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + SENDER_ID + " VARCHAR(4), " +
                MESSAGE + " TEXT, " + RECEIVED + " VARCHAR(255), " + SENT + " VARCHAR(255), " +
                "FOREIGN KEY(" + SENDER_ID + ") REFERENCES " + CONTACTS_TABLE + "(CONTACT_ID) ON UPDATE CASCADE ON DELETE CASCADE);";
        String createMessageInCache = "CREATE TABLE " + MESSAGES_IN_CACHE + "(TEMP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + TEMP_SENDER_ID + " VARCHAR(4), " +
                TEMP_SMESSAGE + " TEXT);";
        String createMessageOutCache = "CREATE TABLE " + MESSAGES_OUT_CACHE + "(TEMP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + TEMP_RECEIVER_ID + " VARCHAR(4), " +
                TEMP_RMESSAGE + " TEXT);";
        String enableForeignKey = "PRAGMA foreign_keys = ON;";

        db.execSQL(enableForeignKey);
        db.execSQL(createContactsTableStatement);
        db.execSQL(createMessageTableStatement);
        db.execSQL(createMessageInCache);
        db.execSQL(createMessageOutCache);
    }

    // this is called if the database version number changes. Prevents crash when db is updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_IN_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_OUT_CACHE);
        onCreate(db);
    }

    public void addOneContact(String contactName, String contactNum, String contactKey){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CONTACT_NAME, contactName);
        cv.put(CONTACT_NUMBER, contactNum);
        cv.put(CONTACT_KEY, contactKey);

        long insert = db.insert(CONTACTS_TABLE, null, cv);
        if(insert == -1)
            Toast.makeText(context, "Failed to Add", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
    }

    public void addOneMessage(String messageSenderID, String messageText, String messageReceived, String messageSent){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SENDER_ID, messageSenderID);
        cv.put(MESSAGE, messageText);
        cv.put(RECEIVED, messageReceived);
        cv.put(SENT, messageSent);

        long insert = db.insert(MESSAGES_TABLE, null, cv);
        if(insert == -1)
            Toast.makeText(context, "Failed to Add", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
    }

    Cursor readAllDataContactsTable(){
        String query = "SELECT * FROM " + CONTACTS_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readAllDataMessagesTable(){
        String query = "SELECT * FROM " + MESSAGES_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readContactName(String senderID){
        String query = "SELECT CONTACT_NAME FROM " + CONTACTS_TABLE + " WHERE " + CONTACT_NUMBER + " = " + senderID;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    Cursor readContactNumber(String name){
        String query = "SELECT CONTACT_NUMBER FROM " + CONTACTS_TABLE + " WHERE " + CONTACT_NAME + " = \"" + name + "\"";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readUserSID(){
        String query = "SELECT CONTACT_NUMBER FROM " + CONTACTS_TABLE + " WHERE CONTACT_ID = 1";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    void updateContact(String row_id, String name, String number, String key){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CONTACT_NAME, name);
        cv.put(CONTACT_NUMBER, number);
        cv.put(CONTACT_KEY, key);

        long result = db.update(CONTACTS_TABLE, cv, "CONTACT_ID=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Update", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteOneContact(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(CONTACTS_TABLE, "CONTACT_ID=?", new String[]{row_id});


        if(result == -1){
            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
        }

    }







}
