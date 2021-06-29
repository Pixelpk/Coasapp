package com.coasapp.coas.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/**
 * Created by AK INFOPARK on 20-11-2017.
 */

public class MySMSBroadcastReceiver extends BroadcastReceiver implements APPConstants {

    AppCompatActivity appCompatActivity;
/*

    public MySMSBroadcastReceiver(AppCompatActivity activity) {
        this.appCompatActivity = activity;
    }
*/

    @Override
    public void onReceive(Context context, Intent intent) {
        APPHelper.showLog("SMSR", "" + SmsRetriever.SMS_RETRIEVED_ACTION + " " + intent.getAction());
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            APPHelper.showLog("SMSR", "" + status.getStatusCode());

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:

                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    APPHelper.showLog("SMSR", message);

                   /* if (screen.equals("reg")) {
                        ((RegisterActivity) appCompatActivit ).setCode(message);
                    }
                   */
                    LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
                    Intent intentBroasCast = new Intent("OTP");
                    intentBroasCast.putExtra("msg", message);
                    broadcaster.sendBroadcast(intentBroasCast);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    break;
            }
        }

    }

}
