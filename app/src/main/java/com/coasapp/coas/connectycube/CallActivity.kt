package com.connectycube.messenger


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.coasapp.coas.R
import com.coasapp.coas.general.COASHomeActivity
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.APPConstants.APP_PREF
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.DatabaseHandler
import com.coasapp.coas.utils.MyPrefs
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.messenger.helpers.RTCSessionManager
import com.connectycube.messenger.helpers.RingtoneManager
import com.connectycube.messenger.helpers.showSnackbar
import com.connectycube.messenger.utilities.*
import com.connectycube.messenger.viewmodels.CallViewModel
import com.connectycube.pushnotifications.ConnectycubePushNotifications
import com.connectycube.pushnotifications.model.ConnectycubeEnvironment
import com.connectycube.pushnotifications.model.ConnectycubeEvent
import com.connectycube.pushnotifications.model.ConnectycubeNotificationType
import com.connectycube.videochat.*
import com.connectycube.videochat.callbacks.RTCClientSessionCallbacks
import com.connectycube.videochat.callbacks.RTCSessionEventsCallback
import com.connectycube.videochat.callbacks.RTCSessionStateCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_call.*
import org.jivesoftware.smack.AbstractConnectionListener
import org.json.JSONObject
import timber.log.Timber


const val EXTRA_IS_INCOMING_CALL = "conversation_type"

class CallActivity : AppCompatActivity(R.layout.activity_call), APPConstants, SensorEventListener, RTCClientSessionCallbacks,
        RTCSessionEventsCallback, RTCSessionStateCallback<RTCSession> {
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val distance = p0!!.values[0];
        Log.i("Sensor", "Distance " + distance);
        if (distance.toString() == "0.0") {
            wakeLock!!.acquire()
            viewOverlay.visibility = VISIBLE
        } else {
            viewOverlay.visibility = GONE

            if (wakeLock!!.isHeld()) {
                wakeLock!!.release();
            }
        }
    }

    var notificationManager: NotificationManager? = null;
    private val callViewModel: CallViewModel by viewModels {
        InjectorUtils.provideCallViewModelFactory(this.application)
    }
    var powerManager: PowerManager? = null;
    var wakeLock: PowerManager.WakeLock? = null;
    var field = 0x00000020;

    var mSensorManager: SensorManager? = null;
    var mProximity: Sensor? = null;
    private val permissionsHelper = PermissionsHelper(this)
    private lateinit var ringtoneManager: RingtoneManager
    private var currentSession: RTCSession? = null
    private var audioManager: AppRTCAudioManager? = null
    private val connectionListener = ConnectionListener()
    private var isInComingCall: Boolean = false

    var jsonObjectUsers: JSONObject = JSONObject();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSession()
        initFields()
        initToolbar()
        initCall()
        initAudioManager()
        initRingtoneManager()
        checkPermissionsAndProceed()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        powerManager = getSystemService(POWER_SERVICE) as PowerManager;
        wakeLock = powerManager!!.newWakeLock(field, getLocalClassName());

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        imageViewMinimize.setOnClickListener { v ->
            val intent = Intent(applicationContext, COASHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
            startActivity(intent);
        }
    }

    override fun onBackPressed() {

    }


    override fun onStart() {
        super.onStart()
        ConnectycubeChatService.getInstance().addConnectionListener(connectionListener)
    }

    override fun onStop() {
        super.onStop()
        ConnectycubeChatService.getInstance().removeConnectionListener(connectionListener)
    }

    private fun initSession() {
        currentSession = RTCSessionManager.getInstance().currentCall
        val users = currentSession!!.opponents;
        Log.i("CallSession", "" + users);
        for (i in 0 until users.size) {
            if (users[i] != MyPrefs(applicationContext, APP_PREF).getInt(CUBE_USER_ID)) {
                val jsonObjectStatus = JSONObject();
                jsonObjectStatus.put("accept", false);
                jsonObjectUsers.put("" + users[i], jsonObjectStatus)
            }
        }
        currentSession?.addSessionCallbacksListener(this@CallActivity)
    }

    private fun initFields() {
        isInComingCall = intent?.extras!!.getBoolean(EXTRA_IS_INCOMING_CALL, true)


        // Toast.makeText(applicationContext, "" + isInComingCall, Toast.LENGTH_LONG).show()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toggle_speaker.setOnClickListener { switchAudioDevice() }
        toggle_mute_mic.setOnClickListener { setMuteAudio(toggle_mute_mic.isChecked) }
        toggle_screen_sharing.setOnClickListener { screenSharing() }
        updateToolbar()
    }

    private fun checkPermissionsAndProceed() {
        if (permissionsHelper.areCallPermissionsGranted()) {
            startFragment()
        } else {
            permissionsHelper.requestCallPermissions()
        }
    }

    private fun updateToolbar(showFull: Boolean = false) {
        currentSession?.let {
            if (isInComingCall && !showFull) {
                /*  if (intent.hasExtra("ringtone")) {
                      val ringtoneManager: RingtoneManager = intent.getSerializableExtra("ringtone") as RingtoneManager
                      ringtoneManager.stop()
                  }*/
                val ringtoneManager = RingtoneManager(context = applicationContext!!);

                toggle_speaker.visibility = View.INVISIBLE
                toggle_mute_mic.visibility = View.INVISIBLE
                toggle_screen_sharing.visibility = View.INVISIBLE
            } else {
                if (it.isAudioCall) {
                    toggle_mute_mic.visibility = View.VISIBLE
                    toggle_speaker.visibility = View.VISIBLE
                    toggle_screen_sharing.visibility = View.GONE
                } else {
                    toggle_screen_sharing.visibility = View.VISIBLE
                    toggle_mute_mic.visibility = View.GONE
                    toggle_speaker.visibility = View.GONE
                }
            }
        }
    }

    private fun switchAudioDevice() {
        audioManager?.apply {
            if (selectedAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
            } else {
                when {
                    audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH) -> selectAudioDevice(
                            AppRTCAudioManager.AudioDevice.BLUETOOTH
                    )
                    audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET) -> selectAudioDevice(
                            AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                    )
                    else -> selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
                }
            }
        }
    }

    private fun setMuteAudio(isEnabled: Boolean) {
        currentSession?.apply {
            mediaStreamManager?.localAudioTrack?.setEnabled(isEnabled)
        }
    }

    private fun screenSharing() {
        Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_LONG).show()
    }

    private fun initCall() {
        RTCClient.getInstance(this).addSessionCallbacksListener(this)
    }

    private fun initAudioManager() {
        if (audioManager == null) {
            audioManager = AppRTCAudioManager.create(this)
            audioManager?.apply {

                val isVideoCall =
                        RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO == currentSession?.conferenceType
                if (isVideoCall)
                    defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
                else {
                    when {
                        audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH) -> defaultAudioDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH
                        audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET) -> defaultAudioDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                        else -> defaultAudioDevice = AppRTCAudioManager.AudioDevice.EARPIECE
                    }
                }
                setOnWiredHeadsetStateListener { plugged, hasMicrophone ->
                    Timber.d("plugged= $plugged, hasMicrophone= $hasMicrophone")
                }
                setBluetoothAudioDeviceStateListener { connected ->
                    Timber.d("connected= $connected")
                }
            }
        }
    }

    private fun startAudioManager() {
        audioManager?.start { selectedAudioDevice, availableAudioDevices ->
            Timber.d("Audio device switched to  $selectedAudioDevice")

        }
    }

    private fun initRingtoneManager() {
        ringtoneManager = RingtoneManager(this, R.raw.ringtone_outgoing)
    }

    private fun startFragment() {
        if (isInComingCall) {

            startIncomingCallFragment()
            subscribeIncomingScreen()
        } else {
            ringtoneManager.startOut(looping = true, vibrate = true)
            startCall()
        }
    }

    private fun subscribeIncomingScreen() {
        callViewModel.incomingCallAction.observeOnce(this, Observer {
            it?.let {
                when (it) {
                    CallViewModel.CallUserAction.ACCEPT -> {
                        startCall()
                    }
                    CallViewModel.CallUserAction.REJECT -> rejectCurrentSession()
                    else -> Timber.d("subscribeIncomingScreen not defined action $it")
                }
            }
        })
    }

    private fun subscribeCallScreen() {
        callViewModel.callUserAction.observeOnce(this, Observer {
            it?.let {
                when (it) {
                    CallViewModel.CallUserAction.HANGUP -> hangUpCurrentSession()
                    else -> Timber.d("subscribeIncomingScreen not defined action $it")
                }
            }
        })
    }

    private fun startIncomingCallFragment() {
        currentSession?.let {
            val ringtoneManager = RingtoneManager(context = applicationContext);
            val fragment = IncomingCallFragment()

            supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    fragment,
                    fragment::class.java.simpleName
            ).commitAllowingStateLoss()
        }
    }

    private fun startCallFragment() {
        val isVideoCall =
                RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO == currentSession?.conferenceType
        val conversationFragment = BaseCallFragment.createInstance(
                if (isVideoCall) VideoCallFragment()
                else AudioCallFragment(),
                isInComingCall
        )
        supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                conversationFragment,
                conversationFragment::class.java.simpleName
        ).commitAllowingStateLoss()
    }

    private fun startCall() {
        updateToolbar(true)
        startAudioManager()
        startCallFragment()
        subscribeCallScreen()
    }

    private fun rejectCurrentSession() {
        currentSession?.rejectCall(HashMap<String, String>())
        dismissNotification()
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this);

    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CALL -> {
                if (permissionsHelper.areCallPermissionsGranted()) {
                    Timber.d("permission was granted")
                    startFragment()
                } else {
                    Timber.d("permission was denied")
                    Toast.makeText(
                        this,
                        getString(
                            R.string.denied_permission,
                            permissions.joinToString()
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: kotlin.IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CALL -> {
                if (permissionsHelper.areCallPermissionsGranted()) {
                    Timber.d("permission was granted")
                    startFragment()
                } else {
                    Timber.d("permission was denied")
                    Toast.makeText(
                            this,
                            getString(
                                    R.string.denied_permission,
                                    permissions.joinToString()
                            ),
                            Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onUserNotAnswer(session: RTCSession?, userId: Int?) {
        Log.i("CallSession", "UserNotAnswer " + userId);

        /* val userIds: StringifyArrayList<Int> = StringifyArrayList()
         userIds.clear()
         userIds.add(userId);
         if (session != null) {
             sendMissedNotification(userIds, session.sessionID)
         };*/

        if (session != currentSession) {
            return
        }
        val databaseHandler = DatabaseHandler(applicationContext)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();
        if (session != null) {
            val jsonObject = APPHelper.getCallUsers(applicationContext, session.sessionID)
            val jsonObjectData = jsonObject.getJSONObject("" + userId)
            jsonObjectData.put("call_status", "Not Answered")
            jsonObject.put("" + userId, jsonObjectData)
            contentValues.put("call_users_data", "" + jsonObject);
            sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
            APPHelper.writeToFile("CallLogNoAnswer " + session.sessionID + " " + session.opponents + " " + userId, session.sessionID);

        }
        APPHelper.exportDB();

        ringtoneManager.stop()
    }

    override fun onSessionStartClose(session: RTCSession) {
        Log.i("CallSession", "StartClose");

        if (session == currentSession) {
            currentSession?.removeSessionCallbacksListener(this@CallActivity)
            callViewModel.callSessionAction.value = CallViewModel.CallSessionAction.CALL_STOPPED
        }
    }

    override fun onReceiveHangUpFromUser(session: RTCSession,
                                         userId: Int,
                                         userInfo: MutableMap<String, String>?
    ) {
        Log.i("CallSession", "ReceiveHangUp " + userId);

        Timber.d("onReceiveHangUpFromUser userId= $userId")
        val databaseHandler = DatabaseHandler(applicationContext)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();

        val jsonObject = APPHelper.getCallUsers(applicationContext, session.sessionID)
        val jsonObjectData = jsonObject.getJSONObject("" + userId)
        jsonObjectData.put("call_status", "Hang Up")
        jsonObjectData.put("call_end", APPHelper.getCurrentTime())
        jsonObject.put("" + userId, jsonObjectData)
        contentValues.put("call_users_data", "" + jsonObject);
        sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
        APPHelper.writeToFile("CallLogHangUpOther " + session.sessionID + " " + session.opponents + " " + userId, session.sessionID);

        APPHelper.exportDB();

    }

    override fun onCallAcceptByUser(session: RTCSession?,
                                    userId: Int?,
                                    data: MutableMap<String, String>?
    ) {
        Log.i("CallSession", "Accept " + userId);
        if (jsonObjectUsers.has("" + userId)) {
            val jsonObjectStatus = jsonObjectUsers.getJSONObject("" + userId);
            jsonObjectStatus.put("accept", true);
            jsonObjectUsers.put("" + userId, jsonObjectStatus)
        }

        if (session != currentSession) {
            return
        }
        val databaseHandler = DatabaseHandler(applicationContext)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();
        if (session != null) {
            val cursor = APPHelper.getCallCursor(applicationContext, session.sessionID, sqLiteDatabase);
            if (cursor.count > 0) {
                cursor.moveToNext();
                val acceptTime = cursor.getString(cursor.getColumnIndex("session_accept_time"))

                if (acceptTime == "") {
                    contentValues.put("session_accept_time", "" + APPHelper.getCurrentTime());
                    sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
                }

            }
            val jsonObject = APPHelper.getCallUsers(applicationContext, session.sessionID)
            val jsonObjectData = jsonObject.getJSONObject("" + userId)
            jsonObjectData.put("call_status", "Accepted")
            jsonObjectData.put("call_start", APPHelper.getCurrentTime())
            jsonObject.put("" + userId, jsonObjectData)
            contentValues.put("call_users_data", "" + jsonObject);
            sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
            APPHelper.writeToFile("CallLogAccept " + session.sessionID + " " + session.opponents + " " + userId, session.sessionID);
        }
        ringtoneManager.stop()
        APPHelper.exportDB();

    }

    public fun dismissNotification() {
        if (notificationManager != null)
            notificationManager!!.cancel(1)

    }

    override fun onReceiveNewSession(session: RTCSession) {
        Log.i("CallSession", "NewReceive");

        Timber.d("onReceiveNewSession")
        if (currentSession != null) {
            Timber.d("reject new session, device is busy")
            session.rejectCall(null)
        }
    }

    override fun onUserNoActions(session: RTCSession?, userId: Int?) {
        Log.i("CallSession", "NoAction " + userId);

    }

    override fun onSessionClosed(session: RTCSession) {
        Log.i("CallSession", "Close " + jsonObjectUsers);
        Timber.d("onSessionClosed session= $session")
        val userIds: StringifyArrayList<Int> = StringifyArrayList()
        userIds.clear()
        Log.i("CallSession", "Notify " + isInComingCall);

        if (session == currentSession) {
            if (jsonObjectUsers.length() > 0) {
                val jsonArrayUsers = jsonObjectUsers.names();
                for (i in 0 until jsonArrayUsers.length()) {
                    val user: String = jsonArrayUsers.getString(i)
                    if (jsonObjectUsers.has(user)) {
                        Log.i("CallSession", "Notify1 " + jsonObjectUsers);

                        val jsonObjectStatus = jsonObjectUsers.getJSONObject(user);
                        Log.i("CallSession", "Notify2 " + jsonObjectStatus);
                        if (!jsonObjectStatus.getBoolean("accept")) {
                            userIds.add(Integer.parseInt(user));
                        }
                    }
                }

                Log.i("CallSession", "Notify3 " + userIds);

                if (!isInComingCall && userIds.size > 0)
                    sendMissedNotification(userIds, session.sessionID);
            }
            Timber.d("release currentSession")
            releaseCurrentCall()
            ringtoneManager.stop()
            finish()
            val databaseHandler = DatabaseHandler(applicationContext)
            val sqLiteDatabase = databaseHandler.writableDatabase
            val cursor = APPHelper.getCallCursor(applicationContext, session.sessionID, sqLiteDatabase);
            if (cursor.count > 0) {
                cursor.moveToNext();

                val contentValues = ContentValues();
                contentValues.put("session_end_time", "" + APPHelper.getCurrentTime());
                sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
                sqLiteDatabase.close()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCallRejectByUser(session: RTCSession?,
                                    userId: Int?,
                                    data: MutableMap<String, String>?
    ) {
        Log.i("CallSession", "Reject " + userId);
        if (jsonObjectUsers.has("" + userId)) {
            val jsonObjectStatus = jsonObjectUsers.getJSONObject("" + userId);
            jsonObjectStatus.put("accept", true);
            jsonObjectUsers.put("" + userId, jsonObjectStatus)
        }
        val databaseHandler = DatabaseHandler(applicationContext)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();
        if (session != null) {
            val jsonObject = APPHelper.getCallUsers(applicationContext, session.sessionID)
            val jsonObjectData = jsonObject.getJSONObject("" + userId)
            jsonObjectData.put("call_status", "Rejected")
            jsonObject.put("" + userId, jsonObjectData)
            contentValues.put("call_users_data", "" + jsonObject);
            sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(session.sessionID));
            APPHelper.writeToFile("CallLogReject " + session.sessionID + " " + session.opponents + " " + userId, session.sessionID);
        }
        APPHelper.exportDB();
    }

    private fun hangUpCurrentSession() {
        ringtoneManager.stop()
        currentSession?.hangUp(HashMap<String, String>())
        dismissNotification()

    }

    private fun releaseCurrentCall() {
        audioManager?.stop()
        RTCClient.getInstance(this).removeSessionsCallbacksListener(this)
        currentSession?.removeSessionCallbacksListener(this)
        currentSession = null
        RTCSessionManager.getInstance().endCall()
        dismissNotification()

    }

    override fun onDisconnectedFromUser(session: RTCSession?, userID: Int?) {
        dismissNotification()

    }

    override fun onConnectedToUser(session: RTCSession?, userID: Int?) {
        showOnGoingNotification()
        imageViewMinimize.visibility = VISIBLE
        callViewModel.callSessionAction.value = CallViewModel.CallSessionAction.CALL_STARTED
    }

    override fun onConnectionClosedForUser(session: RTCSession?, userID: Int?) {
    }

    override fun onStateChanged(session: RTCSession?, state: BaseSession.RTCSessionState?) {
    }

    private inner class ConnectionListener : AbstractConnectionListener() {
        override fun connectionClosedOnError(e: Exception?) {
            showSnackbar(
                    this@CallActivity,
                    R.string.connection_is_disconnected,
                    Snackbar.LENGTH_INDEFINITE
            )
        }

        override fun reconnectionSuccessful() {
            showSnackbar(
                    this@CallActivity,
                    R.string.connection_is_reconnected,
                    Snackbar.LENGTH_SHORT
            )
        }

        override fun reconnectingIn(seconds: Int) {

        }
    }

    fun showOnGoingNotification() {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra("call", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        /*val stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatDialogActivity::class.java);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);*/
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.coasapp)
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        val notificationBuilder = NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.coas_icon192)
                .setLargeIcon(icon)
                .setContentTitle("Ongoing call")
                .setContentText("Tap to return")
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
        //        if (!image.equalsIgnoreCase("")) {
        //            notificationBuilder.setStyle(s);
        //        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_LOW
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("1", "Call", importance)
            notificationManager!!.createNotificationChannel(mChannel)
        }
        notificationManager!!.notify(1, notificationBuilder.build())
    }


    fun sendMissedNotification(userIds: StringifyArrayList<Int>, sessionId: String) {

        Log.i("CallSession", "Notify4 " + userIds);

        val event = ConnectycubeEvent()
        event.userIds = userIds
        event.environment = ConnectycubeEnvironment.DEVELOPMENT
        event.notificationType = ConnectycubeNotificationType.PUSH
        val json = JSONObject()
        json.put("notification_type", sessionId)
        json.put("push_notification_type", "2")
        json.put("caller_name", "" + MyPrefs(applicationContext, APP_PREF).getString("firstName"))
        json.put("caller_phone", "" + MyPrefs(applicationContext, APP_PREF).getString("std_code") + MyPrefs(applicationContext, APP_PREF).getString("phone"))
        json.put("message", "You have missed call");
        json.put("title", "Missed call");
        json.put("answer_timeout", RTCConfig.getAnswerTimeInterval())
        event.message = json.toString()

        ConnectycubePushNotifications.createEvent(event).performAsync(object : EntityCallback<ConnectycubeEvent> {
            override fun onSuccess(event: ConnectycubeEvent, args: Bundle) {
                Log.i("UserNotification", "success")
            }

            override fun onError(error: ResponseException) {
                Log.i("UserNotification", error.message)

            }
        });

    }
}