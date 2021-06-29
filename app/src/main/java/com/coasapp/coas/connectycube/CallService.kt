package com.coasapp.coas.connectycube

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coasapp.coas.R
import com.connectycube.messenger.CallActivity
import com.connectycube.messenger.EXTRA_IS_INCOMING_CALL
import com.connectycube.messenger.LoginActivity
import com.connectycube.messenger.helpers.CALLS_CHANNEL_ID
import com.connectycube.messenger.helpers.CALL_NOTIFICATION_ID
import com.connectycube.messenger.helpers.RingtoneManager
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.videochat.RTCConfig

class CallService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showCallNotification(applicationContext)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showCallNotification(context: Context) {
        //if (ConnectycubeChatService.getInstance().isLoggedIn) return
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.start(true, false)
        val intent = Intent(context, CallActivity::class.java);
        Log.i("ChatConCall", "IncomingLaunchNotification")

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        intent.putExtra(EXTRA_IS_INCOMING_CALL, true)
        intent.putExtra("ringtone", ringtoneManager)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallsNotificationsChannel(context)
        }

        val builder = prepareSimpleNotificationBuilder(context, CALLS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_incoming_call)
                .setContentTitle(context.getString(R.string.incoming_call))
                .setContentText("Incoming Call")
                .setFullScreenIntent(pendingIntent, true)
                .setCategory(NotificationCompat.CATEGORY_CALL)

        var cancelNotificationTimeoutSec = RTCConfig.getAnswerTimeInterval()
        val answerTimeout = RTCConfig.getAnswerTimeInterval();
        if (answerTimeout != null && answerTimeout > 0) {
            cancelNotificationTimeoutSec = answerTimeout
        }

        builder.setTimeoutAfter(cancelNotificationTimeoutSec * 1000)

        displayNotification(context, CALL_NOTIFICATION_ID, builder.build())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCallsNotificationsChannel(context: Context) {
        val name = context.getString(R.string.calls_channel_name)
        val descriptionText = context.getString(R.string.calls_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CALLS_CHANNEL_ID, name, importance)
        channel.description = descriptionText
        configureAndFireNotificationChannel(context, channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureAndFireNotificationChannel(context: Context,
                                                    channel: NotificationChannel
    ) {
        channel.vibrationPattern = longArrayOf(500)
        channel.enableVibration(true)
        channel.lightColor = context.resources.getColor(R.color.colorPrimary)
        channel.enableLights(true)
        channel.importance = NotificationManager.IMPORTANCE_DEFAULT
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        getNotificationManager(context).createNotificationChannel(channel)
    }


    private fun displayNotification(context: Context,
                                    notificationId: Int,
                                    notification: Notification
    ) {
        startForeground(notificationId, notification);
    }

    private fun getNotificationManager(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }


    private fun prepareStartIntent(context: Context,
                                   clazz: Class<out AppCompatActivity>,
                                   extras: Bundle
    ): Intent {
        return if (userAlreadyRegistered(context)) {
            Intent(context, clazz).apply { putExtras(extras) }
        } else {
            getDefaultActivityIntent(context)
        }
    }

    private fun userAlreadyRegistered(context: Context): Boolean {
        return SharedPreferencesManager.getInstance(context).currentUserExists()
    }

    private fun getDefaultActivityIntent(context: Context): Intent {
        return Intent(context, LoginActivity::class.java).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
    }

    private fun prepareSimpleNotificationBuilder(context: Context,
                                                 channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.coasicon40)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(longArrayOf(500))
                .setLights(context.resources.getColor(R.color.colorPrimary), 2000, 2000)
                .setColor(context.resources.getColor(R.color.colorPrimary))
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

}