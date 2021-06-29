package com.connectycube.messenger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coasapp.coas.utils.ChatConnectionListener
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.connections.tcp.TcpChatConnectionFabric
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.messenger.events.EVENT_CHAT_LOGIN
import com.connectycube.messenger.events.EventChatConnection
import com.connectycube.messenger.events.LiveDataBus
import com.connectycube.messenger.helpers.RTCSessionManager
import com.connectycube.messenger.utilities.SharedPreferencesManager
import com.connectycube.users.model.ConnectycubeUser
import org.jivesoftware.smack.AbstractConnectionListener
import org.jivesoftware.smack.XMPPConnection
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ChatConnectionManager {

    companion object {
        @Volatile
        private var instance: ChatConnectionManager? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: ChatConnectionManager().also { instance = it }
                }
    }

    public var chatConnectionListener: ChatConnectionListener? = null;

    private val isPending = AtomicBoolean(false)
    private val isInitialized = AtomicBoolean(false)

    fun initWith(context: Context) {
        Timber.d("initWith, isPending ${isPending.get()}")
        if (isPending.get() || isInitialized.get()) return
        Log.i("ChatCon", "Init")
//        Toast.makeText(context,"Login Init",Toast.LENGTH_SHORT).show();

        // Provide chat connection configuration
        /*    val chatServiceConfigurationBuilder = ConnectycubeChatService.ConfigurationBuilder().apply {
                socketTimeout = 60
                isKeepAlive = true
                isUseTls = true //By default TLS is disabled.
            }

            ConnectycubeChatService.setConnectionFabric(TcpChatConnectionFabric((chatServiceConfigurationBuilder)))*/

        if (SharedPreferencesManager.getInstance(context).currentUserExists()
                && !ConnectycubeChatService.getInstance().isLoggedIn) {
//Toast.makeText(context,"Login",Toast.LENGTH_SHORT).show();
            Log.i("ChatCon", "Login")

            isPending.set(true)
            Timber.d("Start chat login")
            initConnectionListener(context)
            ConnectycubeChatService.getInstance().login(
                    SharedPreferencesManager.getInstance(context).getCurrentUser(),
                    object : EntityCallback<Void> {
                        override fun onSuccess(void: Void?, bundle: Bundle?) {
                            Log.i("ChatCon", "Success")
                           // Toast.makeText(context,"Login Success",Toast.LENGTH_SHORT).show();

                            isPending.set(false)
                            isInitialized.set(true)
                            initCallManager(context)
                        }

                        override fun onError(ex: ResponseException) {
                            chatConnectionListener?.onFailure()

                            isPending.set(false)
                            Timber.d("Error while login to chat, error = ${ex.message}")
                            notifyErrorLoginToChat(ex)
                        }
                    })
        }
    }

    private fun initConnectionListener(context: Context) {
        ConnectycubeChatService.getInstance().addConnectionListener(object :
                AbstractConnectionListener() {
            override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
                Timber.d("authenticated")
                notifySuccessLoginToChat(context, ConnectycubeChatService.getInstance().user)
            }

            override fun connectionClosedOnError(e: Exception) {
                Timber.d("connectionClosedOnError e= $e")
                notifyErrorLoginToChat(e)
            }
        })
    }

    private fun notifyErrorLoginToChat(exception: Exception) {
        LiveDataBus.publish(EVENT_CHAT_LOGIN, EventChatConnection.error(exception))
    }

    private fun notifySuccessLoginToChat(context: Context, connectycubeUser: ConnectycubeUser) {
        chatConnectionListener?.onSuccess()
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("ChatLogin"));
        LiveDataBus.publish(
                EVENT_CHAT_LOGIN,
                EventChatConnection.success(connectycubeUser)
        )
    }

    private fun initCallManager(context: Context) {
        Log.i("ChatCon", "CallInit")

        val rtcSessionManager = RTCSessionManager.getInstance()
        rtcSessionManager.init(context.applicationContext)
        rtcSessionManager.initActivity(context)
        RTCSessionManager.getInstance().init(context.applicationContext)
    }

    fun terminate() {
        ConnectycubeChatService.getInstance().destroy()
        isPending.set(false)
        isInitialized.set(false)
        instance = null
    }
}