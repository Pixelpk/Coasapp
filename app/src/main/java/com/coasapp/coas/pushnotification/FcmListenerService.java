package com.coasapp.coas.pushnotification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.coasapp.coas.general.NotificationsActivity;

import com.coasapp.coas.R;
import com.coasapp.coas.general.COASHomeActivity;
import com.coasapp.coas.utils.APPConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.handler.CompletionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;


public class FcmListenerService extends FirebaseMessagingService implements APPConstants {

    private static final String TAG = "ApplozicGcmListener";
    String user_id="";
    SharedPreferences.Editor editor;


    @Override
    public void onNewToken(String s) {

        super.onNewToken(s);
        Log.i("Token", "Found Registration Id:" + s);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        sharedPreferences.edit().putString("token", s).apply();

        SendBirdCall.registerPushToken(s, true, new CompletionHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e == null) {
                    // The push token has been registered successfully.
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(TAG, "Message data:" + remoteMessage.getData());
        Map<String, String> receivedMap = remoteMessage.getData();

        if (remoteMessage.getData().size() > 0) {

                String title = receivedMap.get("title");
                String body = receivedMap.get("body");
                String icon = receivedMap.get("icon");
          /*  user_id = receivedMap.get("value");
            editor.putString(APPConstants.incoming_user_id,user_id).apply();*/
               /* if(receivedMap.get("value")!=null)
                {

                }*/
                if (getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getBoolean("loggedIn", false)) {
                    sendNotification(title, body, icon);
                }

        }

    }

    //It is same as we did in earlier posts
    private void sendNotification(String title, String messageBody, String image) {
        Bitmap bitmap = null;

        try {
            URL url = new URL(image);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        }
        byte[] byteArray = bStream.toByteArray();
        Intent intent = new Intent(FcmListenerService.this, NotificationsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("body", messageBody);
        intent.putExtra("image", byteArray);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(FcmListenerService.this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(COASHomeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.coasicon192);
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle().bigPicture(bitmap);
        s.setSummaryText(messageBody);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"1")
                .setSmallIcon(R.mipmap.coasicon40)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        if (!image.equalsIgnoreCase("")) {
            notificationBuilder.setStyle(s);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("1", title, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

}
