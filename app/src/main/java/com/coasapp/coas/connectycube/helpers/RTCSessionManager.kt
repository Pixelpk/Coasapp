package com.connectycube.messenger.helpers

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.WebRTCSignaling
import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.messenger.CallActivity
import com.connectycube.messenger.EXTRA_IS_INCOMING_CALL
import com.coasapp.coas.R
import com.coasapp.coas.connectycube.CallService
import com.coasapp.coas.utils.*
import com.coasapp.coas.utils.APPConstants.MAIN_URL
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.core.request.PagedRequestBuilder
import com.connectycube.messenger.ChatMessageActivity
import com.connectycube.messenger.LoginActivity
import com.connectycube.messenger.api.ConnectycubePushSender
import com.connectycube.messenger.data.AppDatabase
import com.connectycube.messenger.data.User
import com.connectycube.messenger.utilities.CUBE_USER_ID
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.pushnotifications.model.ConnectycubeEnvironment
import com.connectycube.pushnotifications.model.ConnectycubeEvent
import com.connectycube.pushnotifications.model.ConnectycubeNotificationType
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.connectycube.videochat.*
import com.connectycube.videochat.callbacks.RTCClientSessionCallbacks
import com.connectycube.videochat.callbacks.RTCClientSessionCallbacksImpl
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_incoming_call.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

const val MAX_OPPONENTS = 4

class RTCSessionManager {

    var activity: Context? = null

    public var oppoId: String = ""

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: RTCSessionManager? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: RTCSessionManager().also { instance = it }
                }

    }

    fun initActivity(activity: Context) {
        this.activity = activity
    }

    private var applicationContext: Context? = null
    private var sessionCallbackListener: RTCClientSessionCallbacks? = null
    var currentCall: RTCSession? = null

    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
        this.sessionCallbackListener = RTCSessionCallbackListenerSimple()

        RTCConfig.setMaxOpponentsCount(MAX_OPPONENTS)
        RTCConfig.setDebugEnabled(true)

        ConnectycubeChatService.getInstance()
                .videoChatWebRTCSignalingManager?.addSignalingManagerListener { signaling, createdLocally ->
                    if (!createdLocally) {
                        RTCClient.getInstance(applicationContext).addSignaling(signaling as WebRTCSignaling)
                    }
                }

        RTCClient.getInstance(applicationContext).addSessionCallbacksListener(
                sessionCallbackListener
        )
        RTCClient.getInstance(applicationContext).prepareToProcessCalls()
    }

    fun startCall(rtcSession: RTCSession) {
        //checkNotNull(applicationContext) { "RTCSessionManager should be initialized before start call" }

        currentCall = rtcSession

        initRTCMediaConfig()
        startCallActivity(false)

        sendCallPushNotification(rtcSession.opponents, rtcSession.sessionID, RTCConfig.getAnswerTimeInterval())
    }

    private fun initRTCMediaConfig() {
        currentCall?.let {
            if (it.opponents.size < 2) {
                RTCMediaConfig.setVideoWidth(RTCMediaConfig.VideoQuality.HD_VIDEO.width)
                RTCMediaConfig.setVideoHeight(RTCMediaConfig.VideoQuality.HD_VIDEO.height)
            } else {
                RTCMediaConfig.setVideoWidth(RTCMediaConfig.VideoQuality.QVGA_VIDEO.width)
                RTCMediaConfig.setVideoHeight(RTCMediaConfig.VideoQuality.QVGA_VIDEO.height)
            }
        }
    }

    private fun sendCallPushNotification(
            opponents: List<Int>,
            sessionId: String,
            answerTimeInterval: Long
    ) {

        Log.i("CallUsers", Gson().toJson(opponents))

        val event = ConnectycubeEvent()
        event.userIds = StringifyArrayList(opponents)
        event.environment = ConnectycubeEnvironment.DEVELOPMENT
        event.notificationType = ConnectycubeNotificationType.PUSH

        val json = JSONObject()
        try {
            json.put(
                    PARAM_MESSAGE,
                    SharedPreferencesManager.getInstance(applicationContext!!).getCurrentUser().fullName + " is calling"
            )
            // custom parameters
            json.put(PARAM_NOTIFICATION_TYPE, 2)
            json.put(PARAM_CALL_ID, sessionId)
            json.put(PARAM_ANSWER_TIMEOUT, answerTimeInterval)
            json.put("VOIPCall ", "1")
            json.put("ios_voip", "1");

        } catch (e: Exception) {
            e.printStackTrace()
        }

        event.message = json.toString()
        if (opponents.size == 1) {
            Log.i("CallUsers", "Private")

            val sendNotification: SendNotification = SendNotification();
            val map: HashMap<String, String> = HashMap();

            val jsonArray = json.names();

            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val key = jsonArray.getString(i);
                    val value = json.getString(key)
                    map[key] = value
                }
            }
            map["user_id"] = APPHelper.getUserIDFromCOAS(oppoId)
            map["session_id"] = "" + map[PARAM_CALL_ID]
            sendNotification.mapParams = map;
            sendNotification.execute("" + opponents[0])

        } else {
            Log.i("CallUsers", "Group")

            ConnectycubePushSender().sendCallPushEvent(event)
        }
        //ConnectycubePushSender().sendCallPushEvent(event)

    }

    inner class SendNotification : AsyncTask<String, Void, String>() {
        public var mapParams: HashMap<String, String> = HashMap()

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: String?): String {
            val requestHandler = RequestHandler();
            return requestHandler.sendPostRequest(MAIN_URL + "CallPush.php", mapParams);

        }


    }

    fun receiveCall(rtcSession: RTCSession) {
        Log.i("ChatConCall", "Incoming")

        if (currentCall != null) {
            Log.i("ChatConCall", "IncomingNotNull")

            if (currentCall!!.sessionID != rtcSession.sessionID) {
                Log.i("ChatConCall", "IncomingReject")

                rtcSession.rejectCall(hashMapOf())
            }
            Log.i("ChatConCall", "IncomingReject2")

            return
        }
        Log.i("ChatConCall", "IncomingLaunchInit")

        currentCall = rtcSession

        initRTCMediaConfig()

        val databaseHandler = DatabaseHandler(activity)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val calendar = Calendar.getInstance()
        val currentTime = APPConstants.sdfDatabaseDateTime.format(calendar.time)
        val opponents: MutableList<Int>?
        opponents = rtcSession.opponents

        Log.i("CallLog", Gson().toJson(opponents))

        val jsonObjectCallData = JSONObject();

        val jsonObjectUsers = JSONObject();

        jsonObjectUsers.put("" + SharedPreferencesManager.getInstance(activity!!).getCurrentUser().id, jsonObjectCallData)

        for (i in opponents.indices) {
            if (opponents.get(i) == SharedPreferencesManager.getInstance(activity!!).getCurrentUser().id)
                jsonObjectCallData.put("call_status", "Missed")
            else
                jsonObjectCallData.put("call_status", "")

            jsonObjectUsers.put("" + opponents.get(i), jsonObjectCallData)

        }
        val callType: Int;
        val conferenceType: RTCTypes.ConferenceType? = null
        val isVideoCall = conferenceType == RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO
        if (isVideoCall) callType = 1 else callType = 2
        val contentValues = ContentValues();
        contentValues.put("session_id", "" + rtcSession.sessionID)
        contentValues.put("call_user_id", SharedPreferencesManager.getInstance(activity!!).getCurrentUser().id)
        contentValues.put("call_time", "" + currentTime)
        contentValues.put("call_direction", "in")
        contentValues.put("call_incoming_status", "Missed")
        contentValues.put("call_type", callType)
        contentValues.put("call_users_data", "" + jsonObjectUsers)
        try {
            sqLiteDatabase.insert("call_history", null, contentValues);
        } catch (e: Exception) {
            Toast.makeText(activity, "" + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        sqLiteDatabase.close()

        APPHelper.writeToFile("CallLogMissedIn " + rtcSession.sessionID + " " + rtcSession.opponents + " " + MyPrefs(activity, APPConstants.APP_PREF).getInt(CUBE_USER_ID), rtcSession.sessionID);
        getUsers(opponents)


        // activity!!.startService(Intent(activity, CallService::class.java))
    }

    internal var userList: MutableList<User> = ArrayList()
    fun getUsers(listUserIds: List<Int>) {

        val pagedRequestBuilder = PagedRequestBuilder()
        pagedRequestBuilder.page = 1
        pagedRequestBuilder.perPage = listUserIds.size
        Log.i("DialogUsersList", Gson().toJson(listUserIds))
        val params = Bundle()
        ConnectycubeUsers.getUsersByIDs(listUserIds, pagedRequestBuilder, params).performAsync(object : EntityCallback<ArrayList<ConnectycubeUser>> {
            override fun onSuccess(users: ArrayList<ConnectycubeUser>, args: Bundle) {

                Log.i("DialogUsersList", Gson().toJson(users))
                for (i in users.indices) {
                    val connectycubeUser = users[i]
                    userList.add(User(connectycubeUser.id!!, connectycubeUser.login, connectycubeUser.fullName, connectycubeUser))
                }

                AddUser().execute();

            }

            override fun onError(error: ResponseException) {
                Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    internal inner class AddUser : AsyncTask<Void, Void, Boolean>() {


        override fun doInBackground(vararg params: Void?): Boolean {
            val database = AppDatabase.getInstance(activity!!)
            //database.userDao().delete();
            Log.i("ChatDialogInsUser", Gson().toJson(userList))
            database.userDao().insertAll(userList)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            startCallActivity(true)

        }

    }


    private fun showCallNotification(context: Context) {
        //if (ConnectycubeChatService.getInstance().isLoggedIn) return

        val intent = Intent(context, CallActivity::class.java);
        Log.i("ChatConCall", "IncomingLaunchNotification")

        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_NEW_DOCUMENT
        intent.putExtra(EXTRA_IS_INCOMING_CALL, true)

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
        getNotificationManager(context).notify(notificationId, notification)
        //startForeground(notificationId, notification);

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

    fun endCall() {
        currentCall = null
    }

    private fun startCallActivity(isIncoming: Boolean) {
        Timber.w("start call incoming - $isIncoming")
        Log.i("ChatConCall", "IncomingLaunch")
        val intent = Intent(activity, CallActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_NEW_DOCUMENT
        intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncoming)
        activity?.startActivity(intent)
    }

    fun destroy() {
        if (applicationContext != null) {
            RTCClient.getInstance(applicationContext)
                    .removeSessionsCallbacksListener(sessionCallbackListener)
            RTCClient.getInstance(applicationContext).destroy()

            applicationContext = null
            sessionCallbackListener = null
        }
    }

    private inner class RTCSessionCallbackListenerSimple : RTCClientSessionCallbacksImpl() {
        override fun onReceiveNewSession(session: RTCSession?) {
            super.onReceiveNewSession(session)
            session?.let { receiveCall(session) }
        }

        override fun onSessionClosed(session: RTCSession?) {
            super.onSessionClosed(session)
            if (session == null || currentCall == null) return

            if (currentCall!!.sessionID == session.sessionID) {
                endCall()
            }
        }
    }
}