package com.coasapp.coas.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String DB_NAME = "coasapp.db";
    public static final int DB_VERSION = 5;
    Context context;


    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "create table if not exists cart (pro_id INTEGER, pro_name text,pro_image text, qty INTEGER, points double, merchant text, merchant_name text)";
        String sql2 = "create table if not exists contacts (contact_phone text,contact_name text, contact_synced INTEGER, contact_chat_id INTEGER, contact_image TEXT, contact_deleted INTEGER)";
        String sql3 = "create table if not exists call_history (call_id INTEGER PRIMARY KEY, session_id text,session_accept_time TEXT, session_end_time TEXT, call_user_id INTEGER, call_users_data TEXT,  call_time TEXT, call_direction TEXT,call_incoming_status TEXT, call_type INTEGER)";


        db.execSQL(sql);
        db.execSQL(sql2);
        db.execSQL(sql3);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS call_history");

        onCreate(db);
    }
}
