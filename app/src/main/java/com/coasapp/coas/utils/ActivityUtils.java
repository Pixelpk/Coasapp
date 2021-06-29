package com.coasapp.coas.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;


public class ActivityUtils {

    private static final String TAG = "ActivityUtils";

    public static final int START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE = 1;

/*
    public static void startAuthenticateActivityAndFinish(@NonNull Activity activity) {
        Log.d(TAG, "startAuthenticateActivityAndFinish()");

        Intent intent = new Intent(activity, AuthenticateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
*/

  /*  public static void startSignInManuallyActivityForResult(@NonNull Activity activity) {
        Log.d(TAG, "startSignInManuallyActivityAndFinish()");

        Intent intent = new Intent(activity, SignInManuallyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE);
    }*/

    /*public static void startMainActivityAndFinish(@NonNull Activity activity) {
        Log.d(TAG, "startMainActivityAndFinish()");

        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }*/
/*
    public static void startApplicationInformationActivity(@NonNull Activity activity) {
        Log.d(TAG, "startApplicationInformationActivity()");

        Intent intent = new Intent(activity, ApplicationInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }*/
}