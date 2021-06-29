package com.connectycube.messenger.helpers

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.DatabaseHandler
import com.connectycube.messenger.EXTRA_USERS_TO_LOAD
import com.connectycube.messenger.SelectCallMembersActivity
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.videochat.RTCClient
import com.connectycube.videochat.RTCTypes
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_CALL_TYPE = "call_type"

const val CALL_TYPE_VIDEO = 1
const val CALL_TYPE_AUDIO = 2

fun startCall(context: Activity, occupants: ArrayList<Int>, callType: Int, coasLoginReceiver: String) {
    val rtcCallType =
            if (callType == CALL_TYPE_VIDEO) RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO
            else RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO

    val rtcClient = RTCClient.getInstance(context.applicationContext)
    val rtcSession = rtcClient.createNewSessionWithOpponents(occupants, rtcCallType)
    val rtcSessionManager = RTCSessionManager.getInstance();
    rtcSessionManager.oppoId = coasLoginReceiver
    rtcSessionManager.initActivity(context)
    rtcSessionManager.startCall(rtcSession)

    val databaseHandler = DatabaseHandler(context)
    val sqLiteDatabase = databaseHandler.writableDatabase
    val calendar = Calendar.getInstance()
    val currentTime = APPConstants.sdfDatabaseDateTime.format(calendar.time)

    var contentValues = ContentValues();

    Log.i("CallLog", occupants.toString())


    var jsonObjectCallData = JSONObject();
    val jsonObjectUsers = JSONObject();

    jsonObjectUsers.put("" + SharedPreferencesManager.getInstance(context).getCurrentUser().id, jsonObjectCallData)

    for (i in occupants.indices) {
        jsonObjectCallData = JSONObject();
        jsonObjectCallData.put("call_status", "Not Answered")
        jsonObjectUsers.put("" + occupants.get(i), jsonObjectCallData)
    }

    contentValues = ContentValues();
    contentValues.put("session_id", "" + rtcSession.sessionID)
    contentValues.put("call_user_id", SharedPreferencesManager.getInstance(context).getCurrentUser().id)
    contentValues.put("call_time", "" + currentTime)
    contentValues.put("call_direction", "out")
    contentValues.put("call_type", callType)
    contentValues.put("call_users_data", "" + jsonObjectUsers)


    try {
        sqLiteDatabase.insert("call_history", null, contentValues);
    } catch (e: Exception) {
        e.printStackTrace()
    }
    sqLiteDatabase.close()

    APPHelper.writeToFile("CallLogInit " + rtcSession.sessionID + " " + rtcSession.opponents, rtcSession.sessionID);

    APPHelper.exportDB();


    //Toast.makeText(context, "members = $occupants, callType = $callType", Toast.LENGTH_LONG).show()
}

fun startAudioCall(context: Activity, allOccupants: ArrayList<Int>, coasLoginReceiver: String) {
    if (allOccupants.size == 1) {
        startCall(context, allOccupants, CALL_TYPE_AUDIO, coasLoginReceiver)
    } else {
        startOpponentsChoosing(context, allOccupants, CALL_TYPE_AUDIO)
    }
}

fun startVideoCall(context: Activity, allOccupants: ArrayList<Int>, coasLoginReceiver: String) {
    if (allOccupants.size == 1) {
        startCall(context, allOccupants, CALL_TYPE_VIDEO, coasLoginReceiver)
    } else {
        startOpponentsChoosing(context, allOccupants, CALL_TYPE_VIDEO)
    }
}

private fun startOpponentsChoosing(context: Context, occupants: ArrayList<Int>, callType: Int) {
    val startIntent = Intent(context, SelectCallMembersActivity::class.java)
    startIntent.putIntegerArrayListExtra(EXTRA_USERS_TO_LOAD, occupants)
    startIntent.putExtra(EXTRA_CALL_TYPE, callType)
    startIntent.putExtra("call_check", true)
    context.startActivity(startIntent)
}