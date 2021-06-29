package com.coasapp.coas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPrefs implements APPConstants {

    Context context;
    SharedPreferences sharedPreferences;
    String name;

    public MyPrefs(Context context, String name) {
        this.context = context;
        this.name=name;
        sharedPreferences = context.getSharedPreferences(name,0);
    }

    public  void putInt( String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public  void putString(String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public  void putBoolean(String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public  void saveDouble(String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public  int getInt(String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public  String getString(String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public  String getStringNum(String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "0");
    }

    public  boolean getBoolean(String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }
}
