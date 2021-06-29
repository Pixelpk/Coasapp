package com.connectycube.messenger

import android.content.ContentValues
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.DatabaseHandler
import com.coasapp.coas.utils.MyPrefs
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.messenger.helpers.RTCSessionManager
import com.connectycube.messenger.utilities.CUBE_USER_ID
import com.connectycube.messenger.utilities.InjectorUtils
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.messenger.viewmodels.CallViewModel
import com.connectycube.messenger.vo.Status
import com.connectycube.users.model.ConnectycubeUser
import com.connectycube.videochat.BaseSession
import com.connectycube.videochat.RTCSession
import kotlinx.android.synthetic.main.call_hang_up.*
import kotlinx.android.synthetic.main.outgoing_view.*
import timber.log.Timber

abstract class BaseCallFragment(
        @LayoutRes contentLayoutId: Int,
        @StringRes val title: Int
) : Fragment(contentLayoutId) {

    lateinit var callViewModel: CallViewModel
    protected var currentSession: RTCSession? = null
    protected var isIncomingCall: Boolean = false
    protected var chronometerInCall: Chronometer? = null
    private var chronometerStarted: Boolean = false
    protected var currentUser: ConnectycubeUser = ConnectycubeChatService.getInstance().user
    var sessionId=""
    private var opponentsIds: List<Int>? = null

    companion object {
        fun createInstance(fragment: BaseCallFragment, isIncomingCall: Boolean): BaseCallFragment {
            val args = Bundle()
            args.putBoolean(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setTitle(title)
        currentSession = RTCSessionManager.getInstance().currentCall
        sessionId= currentSession!!.sessionID

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            isIncomingCall = it.getBoolean(EXTRA_IS_INCOMING_CALL)
        }
        activity?.let {
            callViewModel = ViewModelProviders.of(
                    it,
                    InjectorUtils.provideCallViewModelFactory(it.application)
            ).get(CallViewModel::class.java)
        }
        initButtons()
        initViews()
        initOpponents()
        subscribeCallSessionAction()
    }

    override fun onStart() {
        super.onStart()
        initCurrentCall()
    }

    private fun initCurrentCall() {
        if (currentSession?.state != BaseSession.RTCSessionState.RTC_SESSION_CONNECTED) {
            if (isIncomingCall) {
                currentSession?.acceptCall(null)
            } else {
                currentSession?.startCall(null)
            }
        }
    }

    private fun initOpponents() {
        currentSession?.let { session ->
            val ids = ArrayList<Int>(session.opponents.apply { add(session.callerID) }).toIntArray()
            callViewModel.getOpponents(*ids).observe(this, Observer { resource ->
                if (resource.status == Status.SUCCESS) {
                    if (!isIncomingCall) {
                        val opponentsFiltered =
                                resource.data?.filterNot { it.id == ConnectycubeChatService.getInstance().user.id }
                        val names = opponentsFiltered?.joinToString {  APPHelper.getContactName(context,it.phone, it.fullName) ?: it.login }
                        text_opponents_names.text = names
                    }
                    resource.data?.let { list ->
                        currentUser =
                                list.first { it.id == ConnectycubeChatService.getInstance().user.id }
                    }
                    initWithOpponents(resource.data)
                }
            })
        }
    }

    protected abstract fun initWithOpponents(opponents: List<ConnectycubeUser>?)

    private fun subscribeCallSessionAction() {
        callViewModel.callSessionAction.observe(this, Observer {
            it?.let {
                when (it) {
                    CallViewModel.CallSessionAction.CALL_STARTED -> {
                        startInCallChronometer()
                        layout_outgoing_view.visibility = View.GONE
                    }
                    CallViewModel.CallSessionAction.CALL_STOPPED -> {
                        stopInCallChronometer()
                    }
                    else -> Timber.d("ignore")
                }
            }
        })
    }

    protected open fun initViews() {
        if (isIncomingCall) {
            layout_outgoing_view.visibility = View.GONE
        }
    }

    private fun startInCallChronometer() {
        if (!chronometerStarted) {
            chronometerInCall?.apply {
                chronometerStarted = true
                visibility = View.VISIBLE
                base = SystemClock.elapsedRealtime()
                start()
            }
        }
    }

    private fun stopInCallChronometer() {
        chronometerStarted = false
        chronometerInCall?.stop()
    }

    private fun initButtons() {
        opponentsIds = currentSession!!.opponents


        button_hangup.setOnClickListener {
            callViewModel.callUserAction.value = CallViewModel.CallUserAction.HANGUP
            val callActivity: CallActivity = activity as CallActivity

            val databaseHandler = DatabaseHandler(context)
            val sqLiteDatabase = databaseHandler.writableDatabase
            val contentValues = ContentValues();
            if (currentSession != null) {
                val jsonObject = APPHelper.getCallUsers(context, sessionId)
                val jsonObjectData = jsonObject.getJSONObject("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id)
                jsonObjectData.put("call_status", "Hang Up")
                jsonObjectData.put("call_end", APPHelper.getCurrentTime())
                jsonObject.put("" + SharedPreferencesManager.getInstance(context!!).getCurrentUser().id, jsonObjectData)
                contentValues.put("call_users_data", "" + jsonObject);
                contentValues.put("session_end_time", "" + APPHelper.getCurrentTime());
                sqLiteDatabase.update("call_history", contentValues, "session_id = ? ", arrayOf(sessionId));
            }
            sqLiteDatabase.close()
            APPHelper.exportDB()
            APPHelper.writeToFile("CallLogHangupSelf "+sessionId+" "+opponentsIds+" "+MyPrefs(context, APPConstants.APP_PREF).getInt(CUBE_USER_ID),sessionId);

        }
    }

}