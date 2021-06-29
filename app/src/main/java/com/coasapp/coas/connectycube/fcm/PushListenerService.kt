package com.connectycube.messenger.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coasapp.coas.ApplozicSampleApplication
import com.coasapp.coas.R
import com.coasapp.coas.call.CallActivity
import com.coasapp.coas.call.CallService
import com.coasapp.coas.general.COASHomeActivity
import com.coasapp.coas.general.NotificationsActivity
import com.coasapp.coas.utils.APPConstants
import com.connectycube.messenger.helpers.AppNotificationManager
import com.connectycube.pushnotifications.services.fcm.FcmPushListenerService
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.calls.DirectCall
import com.sendbird.calls.SendBirdCall
import com.sendbird.calls.handler.DirectCallListener
import com.sendbird.calls.handler.SendBirdCallListener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.*

class PushListenerService : FcmPushListenerService() {

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        Log.i("Token", "Found Registration Id:$refreshedToken")

        val sharedPreferences = getSharedPreferences(APPConstants.APP_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("token", refreshedToken).apply()


    }

    fun initSendBirdCall(appId: String): Boolean {
        var appId = appId
        Log.d(ApplozicSampleApplication.TAG, "initSendBirdCall(appId: $appId)")
        val context = applicationContext
        if (TextUtils.isEmpty(appId)) {
            appId = ApplozicSampleApplication.APP_ID1
        }
        if (SendBirdCall.init(context, appId)) {
            SendBirdCall.removeAllListeners()
            SendBirdCall.addListener(UUID.randomUUID().toString(), object : SendBirdCallListener() {
                override fun onRinging(call: DirectCall) {
                    Log.d(ApplozicSampleApplication.TAG, "onRinging() => callId: " + call.callId)
               //     Log.d(ApplozicSampleApplication.TAG, "onRinging() => callDetails: " + call.caller.userId + " " + call.callee.userId)
                    if (CallActivity.sIsRunning) {
                        call.end()
                        return
                    }
                    call.setListener(object : DirectCallListener() {
                        override fun onConnected(call: DirectCall) {
                            Log.d(ApplozicSampleApplication.TAG, "onConnected() => callId: " + call.callId)
                        }

                        override fun onEnded(call: DirectCall) {
                            if (!CallActivity.sIsRunning) {
                                Log.d(ApplozicSampleApplication.TAG, "onEnded() => callId: " + call.callId)
                                CallService.stopService(context)
                                val intent = Intent("call_end")
                                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                            }
                        }
                    })

                    //startActivity(intent)

                    CallService.startService(context, call, true);
                }
            })
            return true
        }
        return false
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i("Notification", remoteMessage.data.toString())
        val receivedMap = remoteMessage.data
        if (getSharedPreferences(APPConstants.APP_PREF, Context.MODE_PRIVATE).getBoolean("loggedIn", false)) {

            if (SendBirdCall.handleFirebaseMessageData(remoteMessage.data)) {
                Log.i("SendBirdNotification", "" + receivedMap + " " + remoteMessage.from);
                initSendBirdCall(ApplozicSampleApplication.APP_ID1)
            } else {
                if (receivedMap.containsKey("message")) {
                    val intent = Intent("chats");
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);

               //     println(newString)
                    AppNotificationManager.getInstance().processPushNotification(this, remoteMessage.data)
                } else {
                    val title = receivedMap["title"]
                    val body = receivedMap["body"]
                    val icon = receivedMap["icon"]

                    /*Log.d("title",title);
                    Log.d("body",body);
                    Log.d("icon",icon);*/

                    if(title!=null || body!=null || icon!=null) {
                        sendNotification(title, body, icon)
                    }
                }
            }
        }


    }

    private fun sendNotification(title: String?, messageBody: String?, image: String?) {
        var bitmap: Bitmap? = null

        try {
            val url = URL(image)
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

        } catch (e: IOException) {
            println(e.message)
        }

        val bStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bStream)
        val byteArray = bStream.toByteArray()
        val intent = Intent(this@PushListenerService, NotificationsActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("body", messageBody)
        intent.putExtra("image", byteArray)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val stackBuilder = TaskStackBuilder.create(this@PushListenerService)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(COASHomeActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.coasicon192)
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        val s = NotificationCompat.BigPictureStyle().bigPicture(bitmap)
        s.setSummaryText(messageBody)
        val notificationBuilder = NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.coasicon40)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
        if (!image.equals("", ignoreCase = true)) {
            notificationBuilder.setStyle(s)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("1", title, importance)
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}