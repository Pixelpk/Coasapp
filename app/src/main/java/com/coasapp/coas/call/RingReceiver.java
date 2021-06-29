package com.coasapp.coas.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.connectycube.messenger.helpers.RingtoneManager;

public class RingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*RingtoneManager ringtoneManager = new RingtoneManager(context);
        boolean playRing = intent.getBooleanExtra("play", false);
        Log.i("RingtoneReceiver", String.valueOf(playRing));
        try {
            if (playRing) {
                ringtoneManager.start(true, true);
            } else {
                ringtoneManager.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
