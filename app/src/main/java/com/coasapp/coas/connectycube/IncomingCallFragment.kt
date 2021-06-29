package com.connectycube.messenger

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coasapp.coas.R
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.APPConstants.APP_PREF
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.DatabaseHandler
import com.coasapp.coas.utils.MyPrefs
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.messenger.helpers.RTCSessionManager
import com.connectycube.messenger.helpers.RingtoneManager
import com.connectycube.messenger.utilities.CUBE_USER_ID
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.messenger.utilities.loadUserAvatar
import com.connectycube.messenger.viewmodels.CallViewModel
import com.connectycube.messenger.vo.Status
import com.connectycube.users.model.ConnectycubeUser
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_incoming_call.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class IncomingCallFragment : Fragment(R.layout.fragment_incoming_call) {
    private var currentSession: RTCSession? = null
    public lateinit var ringtoneManager: RingtoneManager
    private var opponentsIds: List<Int>? = null

    private var conferenceType: RTCTypes.ConferenceType? = null

    private lateinit var callViewModel: CallViewModel

    var sessionId = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setTitle(R.string.title_incoming_call)
        ringtoneManager = RingtoneManager(context!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            currentSession = RTCSessionManager.getInstance().currentCall
            callViewModel = ViewModelProviders.of(it).get(CallViewModel::class.java)
            sessionId = currentSession!!.sessionID
        }
        initArguments()
        initFields()
    }

    override fun onResume() {
        super.onResume()
        startRingtone()
    }

    override fun onPause() {
        super.onPause()
        stopRingtone()
    }

    private fun startRingtone() {
        Timber.d("startRingtone")
        ringtoneManager.start(looping = false, vibrate = true)
    }

    private fun stopRingtone() {
        Timber.d("stopRingtone()")
        try {
            ringtoneManager.stop()
        } catch (e: Exception) {
        }
    }

    private fun initArguments() {
        currentSession?.let {
            opponentsIds = it.opponents
            conferenceType = it.conferenceType
        }
    }

    private fun initFields() {
        currentSession?.let { session ->
            val ids = ArrayList<Int>(session.opponents.apply { add(session.callerID) }).toIntArray()
            callViewModel.getOpponents(*ids).observe(this, Observer { result ->
                if (result.status == Status.SUCCESS) {
                    val callerUser: ConnectycubeUser =
                            result.data!!.first { it.id == session.callerID }
                    loadUserAvatar(context!!, callerUser, image_avatar)
                    text_name.text = callerUser.fullName ?: callerUser.login
                    text_name.text = APPHelper.getContactName(context, callerUser.phone, callerUser.fullName)
                    val opponentsFiltered =
                            result.data.filterNot { it.id != session.callerID || it.id != ConnectycubeChatService.getInstance().user.id }
                    val names = opponentsFiltered.joinToString { it.fullName ?: it.login }
                    // val names = opponentsFiltered.joinToString {  APPHelper.getContactName(context,it.phone, it.fullName) ?: it.login }
                    Log.i("IncomingOpponents", "Names " + names);

                    if (names.isNotEmpty()) {
                        text_on_call.visibility = View.VISIBLE
                        text_other_name.text = names
                    }
                }
            })

            Log.i("IncomingOpponents", Gson().toJson(opponentsIds));

            setCallType()
            initButtons()
        }
    }

    var callType = 0;

    private fun setCallType() {
        val isVideoCall = conferenceType == RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO
        text_call_type.text =
                if (isVideoCall) getString(R.string.incoming_video_call_title) else getString(R.string.incoming_audio_call_title)

        if (isVideoCall) callType = 1 else callType = 2
    }

    private fun initButtons() {
        button_reject_call.setOnClickListener { reject() }
        button_accept_call.setOnClickListener { accept() }
    }

    private fun reject() {
        callViewModel.incomingCallAction.value = CallViewModel.CallUserAction.REJECT
        stopRingtone()
        val databaseHandler = DatabaseHandler(context)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();
        if (currentSession != null) {
            val jsonObject = APPHelper.getCallUsers(context, sessionId)
            val jsonObjectData = jsonObject.getJSONObject("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id)
            jsonObjectData.put("call_status", "Rejected")
            jsonObject.put("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id, jsonObjectData)
            contentValues.put("call_users_data", "" + jsonObject);
            contentValues.put("call_incoming_status", "Rejected")
            sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(sessionId));
            APPHelper.writeToFile("CallLogRejectIn " + sessionId + " " + opponentsIds + " " + MyPrefs(context, APP_PREF).getInt(CUBE_USER_ID), sessionId);
        }

        sqLiteDatabase.close()
        APPHelper.exportDB()

        activity!!.finish()
    }

    private fun accept() {
        val databaseHandler = DatabaseHandler(context)
        val sqLiteDatabase = databaseHandler.writableDatabase
        val contentValues = ContentValues();
        if (currentSession != null) {
            val jsonObject = APPHelper.getCallUsers(context, sessionId)
            val jsonObjectData = jsonObject.getJSONObject("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id)
            jsonObjectData.put("call_status", "Accepted")
            jsonObjectData.put("call_start", APPHelper.getCurrentTime())
            jsonObject.put("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id, jsonObjectData)
            contentValues.put("call_users_data", "" + jsonObject);
            contentValues.put("call_incoming_status", "Accepted")
            sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(sessionId));
        }
        sqLiteDatabase.close()
        APPHelper.exportDB()
        APPHelper.writeToFile("CallLogAcceptIn " + sessionId + " " + opponentsIds + " " + MyPrefs(context, APP_PREF).getInt(CUBE_USER_ID), sessionId);

        callViewModel.incomingCallAction.value = CallViewModel.CallUserAction.ACCEPT
        stopRingtone()
    }

}