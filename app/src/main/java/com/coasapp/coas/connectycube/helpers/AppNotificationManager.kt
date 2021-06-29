package com.connectycube.messenger.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.*
import com.coasapp.coas.R
import com.coasapp.coas.general.COASHomeActivity
import com.coasapp.coas.utils.APPHelper
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.messenger.*
import com.connectycube.messenger.data.AppDatabase
import com.connectycube.messenger.data.Chat
import com.connectycube.messenger.data.ChatRepository
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.videochat.RTCConfig
import java.util.*


const val PARAM_NOTIFICATION_TYPE = "push_notification_type"
const val PARAM_MESSAGE = "message"
const val PARAM_USER_ID = "user_id"
const val PARAM_MESSAGE_ID = "message_id"
const val PARAM_DIALOG_ID = "dialog_id"
const val PARAM_CALL_ID = "notification_type"
const val PARAM_ANSWER_TIMEOUT = "answer_timeout"

const val CALLS_CHANNEL_ID = "notifications_channel_calls"
const val CALL_NOTIFICATION_ID = 222

const val CHATS_CHANNEL_ID = "notifications_channel_chats"
const val CHATS_NOTIFICATION_ID = 333

const val CHATS_NOTIFICATION_GROUP_ID = "chat_notifications_group_id"

const val EXTRA_REPLY_TEXT = "key_reply"
const val EXTRA_NOTIFICATION_ID = "notification_id"

const val NOTIFICATION_TYPE_CALL = 2

class AppNotificationManager {

    private val notificationsMessages = mutableMapOf<String, NotificationCompat.MessagingStyle>()

    companion object {


        // For Singleton instantiation
        @Volatile
        private var instance: AppNotificationManager? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: AppNotificationManager().also { instance = it }
                }
    }

    fun processPushNotification(context: Context, data: Map<String, String>) {
        ChatConnectionManager.getInstance().initWith(context)
        Log.i("ChatServiceLogin", ConnectycubeChatService.getInstance().isLoggedIn.toString())
        if (isChatMessage(data)) {
            showChatNotification(context, data)
            return
        }

        if (data.containsKey(PARAM_NOTIFICATION_TYPE)) {
            when (data[PARAM_NOTIFICATION_TYPE]?.toInt()) {
                NOTIFICATION_TYPE_CALL -> {
                    showCallNotification(context, data)
                }
            }
        }
    }

    private fun showChatNotification(context: Context, data: Map<String, String>) {
        val chtRepository = ChatRepository.getInstance(AppDatabase.getInstance(context.applicationContext).chatDao())
        val dialogId = data[PARAM_DIALOG_ID]
        val dialog: Chat? = chtRepository.getChatSync(dialogId!!)
        val notificationId = dialogId.hashCode()
        val startBundle = Bundle().apply {
            putString(EXTRA_CHAT_ID, dialogId)
            putInt(EXTRA_NOTIFICATION_ID, notificationId)
        }
        var callerName = "New Message"
        var callerPhone = "0"
        if (data.containsKey("caller_name")) {
            callerName = data["caller_name"].toString();
        }
        if (data.containsKey("caller_phone")) {
            callerPhone = data["caller_phone"].toString();
        }
        val dispName = APPHelper.getContactName(context, callerPhone, callerName);
        val dialogName = dispName

        val icon = BitmapFactory.decodeResource(context.resources, R.mipmap.coasicon192)

        //val intent = prepareStartIntent(context, ChatMessageActivity::class.java, startBundle)
        val intent = prepareStartIntent(context, COASHomeActivity::class.java, startBundle)

        val taskStackBuilder = TaskStackBuilder.create(context)
                //.addNextIntent(Intent(context, ChatDialogActivity::class.java))
                .addNextIntent(intent)

        val pendingIntent = taskStackBuilder.getPendingIntent(Random().nextInt(), FLAG_UPDATE_CURRENT)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChatNotificationsChannel(context)
        }

        val remoteInput = RemoteInput.Builder(EXTRA_REPLY_TEXT)
                .setLabel(context.getString(R.string.enter_your_reply_here))
                .build()

        val replyPendingIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PendingIntent.getService(
                            context,
                            Random().nextInt(),
                            Intent(context, SendFastReplyMessageService::class.java).apply {
                                putExtra(EXTRA_CHAT_ID, dialogId)
                            },
                            FLAG_ONE_SHOT
                    )
                } else {
                    pendingIntent
                }

        val replyAction =
                NotificationCompat.Action.Builder(
                        0,
                        context.getString(R.string.reply),
                        replyPendingIntent
                )
                        .addRemoteInput(remoteInput)
                        .setAllowGeneratedReplies(true)
                        .build()

        val builder = prepareSimpleNotificationBuilder(context, CHATS_CHANNEL_ID)
                .setContentTitle(dialogName)
                .setSmallIcon(R.mipmap.coasicon40)
                .setLargeIcon(icon)

                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText("You have a new Notification")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .setStyle(getMessageStyleForDialog(dialogId, dialogName, data.getValue(PARAM_MESSAGE)))
                .addAction(replyAction)

        displayNotification(context, notificationId, builder.build())
    }

    private fun getMessageStyleForDialog(dialogId: String, dialogName: String, newMessage: CharSequence): NotificationCompat.MessagingStyle {
        var messagingStyle = notificationsMessages[dialogId]
        if (messagingStyle == null) {
            messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName(dialogName).build())
            notificationsMessages[dialogId] = messagingStyle
        }

        if (newMessage.isNotEmpty()) {
            messagingStyle.addMessage(newMessage, Date().time, Person.Builder().setName(dialogName).build())
        }

        return messagingStyle
    }

    private fun isChatMessage(data: Map<String, String>): Boolean {
        return /*data.containsKey(PARAM_MESSAGE_ID) && */data.containsKey(PARAM_DIALOG_ID)
    }

    private fun showCallNotification(context: Context, data: Map<String, String>) {
        //if (ConnectycubeChatService.getInstance().isLoggedIn) return

        val intent = prepareStartIntent(context, COASHomeActivity::class.java, Bundle())

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallsNotificationsChannel(context)
        }
        var callerName = ""
        var callerPhone = ""
        if (data.containsKey("caller_name")) {
            callerName = data["caller_name"].toString();
        }
        if (data.containsKey("caller_phone")) {
            callerPhone = data["caller_phone"].toString();
        }
        val dispName = APPHelper.getContactName(context, callerPhone, callerName);
        val builder = prepareSimpleNotificationBuilder(context, CALLS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_incoming_call)
                .setContentTitle(context.getString(R.string.incoming_call))
                .setContentText(data[PARAM_MESSAGE] + " " + dispName)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_CALL)

        var cancelNotificationTimeoutSec = RTCConfig.getAnswerTimeInterval()
        val answerTimeout = data[PARAM_ANSWER_TIMEOUT]?.toLong()
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
    private fun createChatNotificationsChannel(context: Context) {
        val name = context.getString(R.string.chats_channel_name)
        val descriptionText = context.getString(R.string.chats_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHATS_CHANNEL_ID, name, importance)
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

    private fun getNotificationManager(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
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

    private fun displayNotification(context: Context,
                                    notificationId: Int,
                                    notification: Notification
    ) {
        getNotificationManager(context).notify(notificationId, notification)
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

    private fun getDefaultActivityIntent(context: Context): Intent {
        return Intent(context, LoginActivity::class.java).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
    }

    private fun userAlreadyRegistered(context: Context): Boolean {
        return SharedPreferencesManager.getInstance(context).currentUserExists()
    }

    fun notifyReplyResult(context: Context, dialogId: String, text: String, success: Boolean) {
        val params = mutableMapOf<String, String>()
        params[PARAM_DIALOG_ID] = dialogId

        if (success) {
            params[PARAM_MESSAGE] = context.getString(R.string.you_format, text)
        } else {
            params[PARAM_MESSAGE] = ""
        }

        showChatNotification(context, params)
    }

    fun clearNotificationData(context: Context, dialogId: String) {
        getNotificationManager(context).cancel(dialogId.hashCode())

        notificationsMessages.remove(dialogId)
    }

}