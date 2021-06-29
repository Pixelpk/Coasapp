package com.coasapp.coas.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.connectycube.messenger.ChatConnectionManager;

public class OnClearFromRecentServices extends Service implements APPConstants {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ClearFromRecentService", "Service Started");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("ClearFromRecentService", "END");
        //Code here
       ChatConnectionService chatConnectionService = new ChatConnectionService(getApplicationContext());
           Intent intent = APPHelper.getChatServiceIntent(getApplicationContext(),chatConnectionService.getClass());
           startService(intent);

    }
}
