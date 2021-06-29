package com.coasapp.coas.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.connectycube.messenger.ChatConnectionManager;

public class ChatConnectionService extends Service implements APPConstants {

    Context context;

    ChatConnectionListener chatConnectionListener = new ChatConnectionListener() {
        @Override
        public void onSuccess() {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("ChatLogin"));

        }

        @Override
        public void onFailure() {

        }
    };

    public ChatConnectionService() {
    }


    public ChatConnectionService(Context context) {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("loggedIn", false)) {
            ChatConnectionManager chatManager = ChatConnectionManager.Companion.getInstance();
            chatManager.setChatConnectionListener(chatConnectionListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
