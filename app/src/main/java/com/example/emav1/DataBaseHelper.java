package com.example.emav1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    Context context;

    public static final String MAIN_CONTACTS_TABLE = "MAIN_CONTACTS_TABLE";
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
        String createTableStatement = "CREATE TABLE " + MAIN_CONTACTS_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CONTACT_NAME + " VARCHAR(255), " +
                CONTACT_NUMBER + " VARCHAR(255), " + CONTACT_KEY + " VARCHAR(255));";

        db.execSQL(createTableStatement);
    }

    // this is called if the database version number changes. Prevents crash when db is updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MAIN_CONTACTS_TABLE);
        onCreate(db);
    }

    public void addOneContact(String contactName, String contactNum, String contactKey){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CONTACT_NAME, contactName);
        cv.put(CONTACT_NUMBER, contactNum);
        cv.put(CONTACT_KEY, contactKey);

        long insert = db.insert(MAIN_CONTACTS_TABLE, null, cv);
        if(insert == -1)
            Toast.makeText(context, "Failed to Add", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();

    }

    Cursor readAllData(){
        String query = "SELECT * FROM " + MAIN_CONTACTS_TABLE;
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

        long result = db.update(MAIN_CONTACTS_TABLE, cv, "ID=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Update", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteOneContact(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(MAIN_CONTACTS_TABLE, "ID=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
        }

    }





}
