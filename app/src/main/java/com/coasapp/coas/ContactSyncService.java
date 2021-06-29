package com.coasapp.coas;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class ContactSyncService extends Service {
    ArrayList<String> contacts = new ArrayList<>();
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHandler = new DatabaseHandler(getApplicationContext());
        sqLiteDatabase = databaseHandler.getWritableDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ContactScan", "Started");
        contacts.clear();
        /*String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cur = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                String phoneNo = cur.getString(cur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "").replace(" ", "");
                if (phoneNo.length() > 7) {
                    Log.i("ContactScan", phoneNo);

                   *//* if (phoneNo.startsWith("0"))
                        contacts.add(phoneNo.substring(1));
                    else*//*
                    contacts.add(phoneNo);
                }

                *//*if (cursor.getCount() == 0) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("contact_phone", phoneNo.replace(" ", "").replace("-", ""));
                    contentValues.put("contact_synced", 0);
                    contentValues.put("contact_deleted", 0);
                    sqLiteDatabase.insert("contacts", null, contentValues);
                }

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));


                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                }*//*


            }
        }


        cur.close();*/



        contacts.addAll(intent.getStringArrayListExtra("contacts"));
        Log.i("ContactScan", "Add");
        List<ContentValues> contentValuesList = new ArrayList<>();
        for (String phoneNo : contacts) {
            String query = "select * from contacts where contact_phone like '%" + phoneNo + "%'";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.getCount() == 0) {

                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_phone", phoneNo.replace(" ", "").replace("-", "").replaceFirst("^0+(?!$)", ""));
                contentValues.put("contact_synced", 0);
                contentValues.put("contact_deleted", 0);
                contentValuesList.add(contentValues);

            }

        }
        sqLiteDatabase.beginTransaction();
        for (ContentValues contentValues : contentValuesList) {
            sqLiteDatabase.insert("contacts", null, contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        APPHelper.exportDB();
        Intent intentContacts = new Intent("contacts");
        intentContacts.putStringArrayListExtra("contacts", contacts);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentContacts);
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
