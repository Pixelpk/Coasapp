package com.coasapp.coas.utils;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils {

    public static void showYesNoAlert(Activity activity, final String requestCode, String message, final AlertYesNoListener yesNoListener) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        yesNoListener.onYesClick(requestCode);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        yesNoListener.onNoClick(requestCode);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public static void showOKAlert(Activity activity, final String requestCode, String message, boolean cancelable, final AlertYesNoListener yesNoListener) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        yesNoListener.onYesClick(requestCode);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        yesNoListener.onNoClick(requestCode);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setPositiveButton("OK", dialogClickListener).setCancelable(cancelable).show();
                //.setNegativeButton("No", dialogClickListener).show();
    }
}
