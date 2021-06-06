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
    public static final String SENDER_ID = "SENDER_ID";
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
                CONTACT_NUMBER + " VARCHAR(255) UNIQUE, " + CONTACT_KEY + " VARCHAR(255));";
        String createMessageTableStatement = "CREATE TABLE " + MESSAGES_TABLE + "(MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + SENDER_ID + " VARCHAR(255), " +
                MESSAGE + " TEXT, " + RECEIVED + " VARCHAR(255), " + SENT + " VARCHAR(255), " +
                "FOREIGN KEY(" + SENDER_ID + ") REFERENCES " + CONTACTS_TABLE + "(CONTACT_ID));";
        String enableForeignKey = "PRAGMA foreign_keys = ON;";

        db.execSQL(enableForeignKey);
        db.execSQL(createContactsTableStatement);
        db.execSQL(createMessageTableStatement);
    }

    // this is called if the database version number changes. Prevents crash when db is updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
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

    Cursor readAllData(){
        String query = "SELECT * FROM " + CONTACTS_TABLE;
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
