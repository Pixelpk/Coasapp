package com.coasapp.coas.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GetHash implements APPConstants {

    public static String getHash(Context context) throws UnsupportedEncodingException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String hash = new AppSignatureHelper(context).getAppSignatures().get(0);
        String message = "<#> ##OTP## is your verification code " + hash;
        //String encodedMsg = URLEncoder.encode(message, "UTF-8").replace("+", URLEncoder.encode("+", "UTF-8"));
        String encodedMsg = message.replace("+", URLEncoder.encode("+", "UTF-8"));
        APPHelper.showLog("msg", encodedMsg);
        return encodedMsg;
    }

    public static String getCode(String msg) {

        String[] msgArray = msg.split(" ");
        return msgArray[1];
    }


}
