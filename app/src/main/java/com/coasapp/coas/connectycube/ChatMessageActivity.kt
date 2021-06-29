package com.connectycube.messenger

import android.Manifest.permission.*
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Rect
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coasapp.coas.R
import com.coasapp.coas.bargain.RequestActivity
import com.coasapp.coas.connectycube.ChatSettingsActivity
import com.coasapp.coas.connectycube.ForwardMessageActivity
import com.coasapp.coas.connectycube.ModelClasses.imageviewrecyclerModel
import com.coasapp.coas.connectycube.adapters.imageviewrecyclerAdapter
import com.coasapp.coas.connectycube.utilities.ChatUtils
import com.coasapp.coas.general.MyLocationActivity
import com.coasapp.coas.general.UserDetailsActivity
import com.coasapp.coas.general.VideoViewActivity
import com.coasapp.coas.utils.*
import com.coasapp.coas.utils.APPConstants.*
import com.connectycube.auth.session.ConnectycubeSessionManager
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.ConnectycubeRoster
import com.connectycube.chat.PrivacyListsManager
import com.connectycube.chat.exception.ChatException
import com.connectycube.chat.listeners.*
import com.connectycube.chat.model.*
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.core.request.PagedRequestBuilder
import com.connectycube.core.request.RequestGetBuilder
import com.connectycube.messenger.adapters.AttachmentClickListener
import com.connectycube.messenger.adapters.ChatMessageAdapter
import com.connectycube.messenger.data.AppDatabase
import com.connectycube.messenger.data.User
import com.connectycube.messenger.events.EVENT_CHAT_LOGIN
import com.connectycube.messenger.events.EventChatConnection
import com.connectycube.messenger.events.LiveDataBus
import com.connectycube.messenger.helpers.*
import com.connectycube.messenger.paging.Status
import com.connectycube.messenger.utilities.*
import com.connectycube.messenger.viewmodels.ChatMessageListViewModel
import com.connectycube.messenger.viewmodels.MessageSenderViewModel
import com.connectycube.pushnotifications.model.ConnectycubeEnvironment
import com.connectycube.pushnotifications.model.ConnectycubeEvent
import com.connectycube.pushnotifications.model.ConnectycubeNotificationType
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_START
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import com.zhihu.matisse.Matisse
import kotlinx.android.synthetic.main.activity_chatmessages.*
import kotlinx.android.synthetic.main.dialog_message_options.view.*
import kotlinx.android.synthetic.main.progress_dialog.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


const val TYPING_INTERVAL_MS: Long = 2000
const val EXTRA_CHAT = "chat_dialog"
const val EXTRA_CHAT_ID = "chat_id"

const val REQUEST_CODE_DETAILS = 55

private var imagePath: String? = null
//private var imagePathList: List<imageviewrecyclerModel>? = null
private val imagePathList = ArrayList<imageviewrecyclerModel>()
private lateinit var imageadapter: imageviewrecyclerAdapter
private lateinit var imagemodel: imageviewrecyclerModel

class ChatMessageActivity : BaseChatActivity(), APPConstants {


    var chatDialogList: ArrayList<ConnectycubeChatDialog> = arrayListOf();

    var isInGroup = false;

    var privacyListsManager: PrivacyListsManager? = null
    var databaseHandler: DatabaseHandler? = null
    var sqLiteDatabase: SQLiteDatabase? = null

    var PICK_IMAGE_MULTIPLE = 1
    var imageEncoded: String? = null
    var imagesEncodedList: List<String>? = null
    var flag = "1"
    var count = 0
    private val attachmentClickListener: AttachmentClickListener = this::onMessageAttachmentClicked
    private val messageListener: ChatDialogMessageListener = ChatMessageListener()
    private val messageStatusListener: MessageStatusListener = ChatMessagesStatusListener()
    private val messageSentListener: ChatDialogMessageSentListener = ChatMessagesSentListener()
    private val messageTypingListener: ChatDialogTypingListener = ChatTypingListener()
    private val permissionsHelper = PermissionsHelper(this)
    private lateinit var chatAdapter: ChatMessageAdapter
    private lateinit var chatDialog: ConnectycubeChatDialog
    private lateinit var modelChatMessageList: ChatMessageListViewModel
    private lateinit var modelMessageSender: MessageSenderViewModel
    private val occupants: HashMap<Int, ConnectycubeUser> = HashMap()
    private val membersNames: ArrayList<String> = ArrayList()

    var callBlockedCountries = "[]";

    var callsAllowed = true
    var callsAllowedSelf = true;

    var connectycubeUser: ConnectycubeUser? = null

    var chatRoster: ConnectycubeRoster? = null;

    // Do this after success Chat login


    var blockedByYou = "0";
    var blockedByOther = "0"


    var handler = Handler()

    var runnableActive = object : Runnable {
        override fun run() {
            checkActiveBlock()
        }
    }

    var runnableGroup = object : Runnable {
        override fun run() {

            Log.i("GroupOccupants", chatDialog.occupants.toString())

            /* if (!chatDialog.occupants.contains(SharedPreferencesManager.getInstance(applicationContext).getCurrentUser().id)) {
                 menuChat!!.findItem(R.id.menu_action_audio).setVisible(false)
                 menuChat!!.findItem(R.id.menu_action_video).setVisible(false)
                 menuChat!!.findItem(R.id.action_bargain).setVisible(false)

                 button_chat_attach.visibility = GONE
                 button_chat_send.visibility = GONE
                 input_chat_message.isEnabled = false
                 input_chat_message.setHint("You are not a group member")
             }*/

            getDialogs();


        }

    }


    var inputMsg = ""
    var msgId: String = ""
    var msgDeleteId: String = ""
    var msgType: String = "";
    var audioFile: String = ""
    var chronometerAudio: Chronometer? = null
    var path = ""
    val patharray = ArrayList<String>()
    var file: File? = null;
    var coasLoginReceiver = ""
    var userID = -1
    var receiverPhone = ""
    var menuChat: Menu? = null
    var dataReply = "";
    var textViewOriginalMessage: TextView? = null;
    var textViewOriginalSender: TextView? = null
    var imageViewOriginal: ImageView? = null

    var getRequestAsyncTask: GetRequestAsyncTask? = null


    internal var voiceRecordListener: VoiceRecordListener = object : VoiceRecordListener {
        override fun recordingStart() {
            if (chronometerAudio != null) {
                chronometerAudio!!.setBase(SystemClock.elapsedRealtime());
                chronometerAudio!!.start()
            };
        }

        override fun recordingStop(file: String) {
            chronometerAudio?.stop();
            audioFile = file
        }
    }

    var connectycubePrivacyList = arrayListOf<ConnectycubePrivacyList>()
    var asyncTask: PostRequestAsyncTask? = null
    var progressDialog: ProgressDialog? = null
    var downloadTask: DownloadTask? = null
    internal var apiCallbacks: APICallbacks = object : APICallbacks {
        override fun taskStart() {

        }


        override fun taskEnd(type: String?, response: String?) {
            if (type == "block") {
                try {
                    val objectRes = JSONObject(response)
                    if (objectRes.getString("response_code") == "1") {

                        handler.post(runnableActive)
                    }
                } catch (e: Exception) {
                }

            }
            if (type == "blocked_countries") {
                try {
                    val objectRes = JSONObject(response)
                    val arrayBlockedCall = objectRes.getJSONArray("call_blocked_countries");
                    callBlockedCountries = arrayBlockedCall.toString();
                    val myPhone = MyPrefs(applicationContext, APP_PREF).getString("std_code") + MyPrefs(applicationContext, APP_PREF).getString("phone");
                    MyPrefs(applicationContext, APP_PREF).putString("blocked_countries", callBlockedCountries);
                    for (i in 0 until arrayBlockedCall.length()) {
                        val code = arrayBlockedCall.getJSONObject(i).getString("std_code");
                        Log.i("BlockCallSelf", "$code $myPhone")
                        if (myPhone.startsWith(code)) {
                            callsAllowed = false;
                            callsAllowedSelf = false;
                            menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                            menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (type == "driver") {

                try {
                    val objectRes = JSONObject(response)

                    blockedByYou = objectRes.getString("blocked_by_you")
                    blockedByOther = objectRes.getString("blocked_by_other")

                    if (blockedByYou == "0" && blockedByOther == "0") {
                        // chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))
                        /*if (seconds > -1 && seconds < 300) {
                            chat_message_members_typing.setText("Online")
                        } else {
                            chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))

                        }*/
                        if (callsAllowedSelf) {
                            menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                            menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
//                        menuChat!!.findItem(R.id.action_bargain).setVisible(true)
                        }
                        menuChat!!.findItem(R.id.action_block).setTitle("Block")
                        button_chat_attach.visibility = VISIBLE
                        button_chat_send.visibility = VISIBLE
                        input_chat_message.isEnabled = true
                        input_chat_message.setHint("Message")


                        if (objectRes.getString("response_code") == "1") {

                            if (!APPHelper.isContact(applicationContext, userID)) {
                                menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                menuChat!!.findItem(R.id.menu_action_video).setVisible(true)

                                /* if (objectRes.getString("is_driver") == "1" && (objectRes.getString("driver_online") == "1")) {
                                     menuChat!!.findItem(R.id.action_bargain).setVisible(true)

                                     if (objectRes.getInt("bargain_to_complete") == 0) {
                                         menuChat!!.findItem(R.id.menu_action_audio).setVisible(false)
                                         menuChat!!.findItem(R.id.menu_action_video).setVisible(false)
                                     } else {
                                         menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                         menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                                     }

                                 }*/

                            } else {
                                if (callsAllowedSelf) {
                                    menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                    menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                                }
                            }

                            if (objectRes.getString("is_driver") == "1" && (objectRes.getString("driver_online") == "1")) {
                                menuChat!!.findItem(R.id.action_bargain).setVisible(true)
                                if (!APPHelper.isContact(applicationContext, userID)) {
                                    if (objectRes.getInt("bargain_to_complete") == 0) {
                                        menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                        menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                                    } else {
                                        if (callsAllowedSelf) {
                                            menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                            menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                                        }
                                    }
                                } else {
                                    if (callsAllowedSelf) {
                                        menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                                        menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                                    }
                                }

                            }
                        } else {
                            menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                            menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                            menuChat!!.findItem(R.id.action_bargain).setVisible(false)
                        }


                    } else {
                        menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                        menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                        menuChat!!.findItem(R.id.action_bargain).setVisible(false)
                        Log.i("ChatBlock", blockedByYou + " " + blockedByOther)
                        if (blockedByYou != "0") {
                            menuChat!!.findItem(R.id.action_block).setTitle("Unblock")
                            button_chat_attach.visibility = GONE
                            button_chat_send.visibility = GONE
                            input_chat_message.isEnabled = false
                            input_chat_message.setHint("You have blocked this user")
                        }
                        if (blockedByOther != "0") {
                            button_chat_attach.visibility = GONE
                            button_chat_send.visibility = GONE
                            input_chat_message.isEnabled = false
                            input_chat_message.setHint("You are blocked")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(applicationContext, "Error in Connection", Toast.LENGTH_LONG).show();
                    //finish()
                }

                handler.postDelayed(runnableActive, 3000)
            }
        }

    }
    internal var downloadCallbacks: DownloadCallbacks = object : DownloadCallbacks {
        override fun downloadStart() {
        }

        override fun downloadProgressUpdate(progress: Int) {
            progressBarDownload.progress = progress
        }

        override fun downloadComplete(requestCode: String?, file: String?) {
            Log.i("DownloadAudioComplete", file)
            cardViewDownload.visibility = GONE
            if (requestCode == "audio") {
                val voiceRecording = VoiceRecording(this@ChatMessageActivity)
                voiceRecording.showAudioAlert(this@ChatMessageActivity, "url", file)
            } else if (requestCode == "doc") {
                APPHelper.openFile(this@ChatMessageActivity, File(file));
            } else if (requestCode.equals("share")) {
                APPHelper.shareFile(this@ChatMessageActivity, file);
            } else if (requestCode.equals("video")) {
                val intent = Intent(this@ChatMessageActivity, VideoViewActivity::class.java)
                intent.putExtra("file", file)
                intent.putExtra("type", "local")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else if (requestCode.equals("image")) {
                val intent = Intent(this@ChatMessageActivity, AttachmentPreviewActivity::class.java)
                intent.putExtra(EXTRA_URL, file)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

        }

    }

    val messageDeleteListener: MessageDeleteListener = object : MessageDeleteListener {
        override fun processMessageDeleted(p0: String?, p1: String?) {
            chatDialog.removeMessageWithId(p0);
        }

    }

    internal var chatMessageLongClick: ChatMessageLongClick = object : ChatMessageLongClick {
        override fun onLongClick(view: View?, chatMessage: ConnectycubeChatMessage?) {
            if (blockedByOther == "0" && blockedByYou == "0") {

                var bottomSheetDialog = BottomSheetDialog(this@ChatMessageActivity);

                var viewDialog = layoutInflater.inflate(R.layout.dialog_message_options, null)

                bottomSheetDialog.setContentView(viewDialog)

                var onClickListener = object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        if (p0 != null) {
                            bottomSheetDialog.dismiss()
                            when (p0.id) {
                                R.id.textViewCopy -> {
                                    if (chatMessage != null) {
                                        if (chatMessage.body.length > 0)
                                            APPHelper.copyText(applicationContext, chatMessage.body)
                                    }
                                }
                                R.id.textViewForward -> {
                                    val intent = Intent(applicationContext, ForwardMessageActivity::class.java)
                                    if (chatMessage != null) {
                                        //intent.putExtra("message", chatMessage)
                                        intent.putExtra("message", "" + chatMessage.body)

                                        if (chatMessage.attachments != null && !chatMessage.attachments.isEmpty()) {
                                            intent.putExtra("is_attachment", true)
                                            intent.putExtra("attachment", chatMessage.attachments.first())
                                            /*intent.putExtra("attachment_data", "" + chatMessage.attachments.first().data);
                                        intent.putExtra("attachment_url", "" + chatMessage.attachments.first().url);
                                        intent.putExtra("attachment_type", "" + chatMessage.attachments.first().type);
                                        intent.putExtra("content_type", "" + chatMessage.attachments.first().data);*/
                                        } else {
                                            intent.putExtra("is_attachment", false)
                                        }
                                    };
                                    startActivity(intent)

                                }
                                R.id.textViewReply -> {

                                    if (chatMessage != null) {
                                        val objectOrgMsg = JSONObject();
                                        objectOrgMsg.put(ORIGINAL_MSG_ID, chatMessage.id)
                                        objectOrgMsg.put(ORIGINAL_MSG_BODY, chatMessage.body)
                                        //objectOrgMsg.put(ORIGINAL_MSG_SENDER_ID,""+ chatMessage.senderId)
                                        var senderName = occupants[chatMessage.senderId]?.fullName

                                        if (senderName == null)
                                            senderName = MyPrefs(applicationContext, APP_PREF).getString("firstName")

                                        objectOrgMsg.put(ORIGINAL_MSG_SENDER_NAME, "" + senderName)
                                        if (chatMessage.attachments != null && !chatMessage.attachments.isEmpty()) {
                                            val attachment = chatMessage.attachments.first()
                                            val isFile = !(attachment.type.equals("contact") || attachment.type.equals("location"))
                                            if (isFile) {
                                                /*if (attachment.url.startsWith(attachmentChatUrl)) {
                                                setPros(objectOrgMsg, attachment, chatMessage)
                                            } else {
                                                Toast.makeText(applicationContext, "File not uploaded yet", Toast.LENGTH_SHORT).show();
                                            }*/
                                                setPros(objectOrgMsg, attachment, chatMessage)

                                            } else {
                                                setPros(objectOrgMsg, attachment, chatMessage)
                                            }

                                            /*if (attachment.type.toLowerCase().equals("contact")) {
                                            val data = APPHelper.getAttachmentData(attachment.data);
                                            val obj = JSONObject(data);
                                            val phone = obj.getString("phone");
                                            val name = obj.getString("name")
                                            objectOrgMsg.put(ORIGINAL_MSG_BODY, obj.toString())
                                        }*/
                                        } else {
                                            imageViewOriginal!!.visibility = GONE
                                            prepareReply(objectOrgMsg, chatMessage)
                                        }
                                    }
                                }
                                R.id.textViewShare -> {
                                    if (chatMessage != null) {
                                        if (chatMessage.attachments == null || chatMessage.attachments.isEmpty()) {
                                            APPHelper.share(this@ChatMessageActivity, chatMessage.body)
                                        } else {
                                            val attachment = chatMessage.attachments.first();
                                            if (attachment.type.toLowerCase() == "contact") {
                                                val jsonObjectContact = ChatUtils().getPhoneObj(attachment.data)
                                                APPHelper.share(this@ChatMessageActivity, jsonObjectContact.getString("name") + " " + jsonObjectContact.getString("phone"))
                                            } else if (attachment.type.toLowerCase() == "location") {
                                                val jsonObjectLoc = ChatUtils().getLocObj(attachment.data)
                                                APPHelper.share(this@ChatMessageActivity, "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=" + jsonObjectLoc.getString("location_lat_lng"))

                                            } else {
                                                if (cardViewDownload.visibility == VISIBLE) {
                                                    Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
                                                } else {
                                                    if (attachment.contentType != null) {
                                                        if (attachment.url.startsWith(attachmentChatUrl)) {
                                                            val filename = attachment.url.substring(attachment.url.lastIndexOf("/") + 1)
                                                            val mime = Arrays.asList(attachment.data);
                                                            cardViewDownload.visibility = VISIBLE
                                                            if (mime.size > 1) {
                                                                downloadTask = DownloadTask(applicationContext, this@ChatMessageActivity, "share", filename, mime.get(1), downloadCallbacks)
                                                                downloadTask!!.execute(attachment.url)
                                                            } else {
                                                                downloadTask = DownloadTask(applicationContext, this@ChatMessageActivity, "share", filename, attachment.contentType, downloadCallbacks)
                                                                downloadTask!!.execute(attachment.url)
                                                            }

                                                        } else {
                                                            APPHelper.shareFile(this@ChatMessageActivity, attachment.url);
                                                        }


                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                R.id.textViewDelete -> {
                                    layoutProgress.visibility = VISIBLE
                                    val messageIDs = hashSetOf<String>()
                                    if (chatMessage != null) {
                                        msgDeleteId = chatMessage.id
                                        messageIDs.add(chatMessage.id)
                                    }
                                    ConnectycubeRestChatService.deleteMessage(msgDeleteId, false).performAsync(object : EntityCallback<Void> {
                                        override fun onSuccess(p0: Void?, p1: Bundle?) {
                                            layoutProgress.visibility = GONE

                                            DeleteMessage().execute();

                                        }

                                        override fun onError(p0: ResponseException?) {
                                            layoutProgress.visibility = GONE
                                            if (p0 != null) {
                                                layoutProgress.visibility = GONE
                                                Toast.makeText(applicationContext, p0.message, Toast.LENGTH_LONG).show()
                                            }
                                        }

                                    });
                                    /*ConnectycubeRestChatService.deleteMessages(messageIDs, false).performAsync(object : EntityCallback<Void> {
                                    override fun onSuccess(aVoid: Void, bundle: Bundle) {
                                        layoutProgress.visibility = GONE

                                    }

                                    override fun onError(e: ResponseException) {
                                        messageDeleteListener.processMessageDeleted(msgDeleteId, chatDialog.id.toString())

                                    }
                                })*/

                                }
                            }
                        }
                    }
                }

                viewDialog.textViewCopy.setOnClickListener(onClickListener)
                viewDialog.textViewReply.setOnClickListener(onClickListener)
                viewDialog.textViewShare.setOnClickListener(onClickListener)
                viewDialog.textViewDelete.setOnClickListener(onClickListener)
                viewDialog.textViewForward.setOnClickListener(onClickListener)

                bottomSheetDialog.show()


                /*val popupMenu = PopupMenu(this@ChatMessageActivity, view)
                //Inflating the Popup using xml file
                popupMenu.menuInflater.inflate(R.menu.menu_message_delete, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_copy -> {
                            if (chatMessage != null) {
                                if (chatMessage.body.length > 0)
                                    APPHelper.copyText(applicationContext, chatMessage.body)
                            }
                        }
                        R.id.action_forward -> {
                            val intent = Intent(applicationContext, ForwardMessageActivity::class.java)
                            if (chatMessage != null) {
                                //intent.putExtra("message", chatMessage)
                                intent.putExtra("message", "" + chatMessage.body)

                                if (chatMessage.attachments != null && !chatMessage.attachments.isEmpty()) {
                                    intent.putExtra("is_attachment", true)
                                    intent.putExtra("attachment", chatMessage.attachments.first())
                                    *//*intent.putExtra("attachment_data", "" + chatMessage.attachments.first().data);
                                intent.putExtra("attachment_url", "" + chatMessage.attachments.first().url);
                                intent.putExtra("attachment_type", "" + chatMessage.attachments.first().type);
                                intent.putExtra("content_type", "" + chatMessage.attachments.first().data);*//*
                                } else {
                                    intent.putExtra("is_attachment", false)
                                }
                            };
                            startActivity(intent)

                        }
                        R.id.action_reply -> {

                            if (chatMessage != null) {
                                val objectOrgMsg = JSONObject();
                                objectOrgMsg.put(ORIGINAL_MSG_ID, chatMessage.id)
                                objectOrgMsg.put(ORIGINAL_MSG_BODY, chatMessage.body)
                                //objectOrgMsg.put(ORIGINAL_MSG_SENDER_ID,""+ chatMessage.senderId)
                                var senderName = occupants[chatMessage.senderId]?.fullName

                                if (senderName == null)
                                    senderName = MyPrefs(applicationContext, APP_PREF).getString("firstName")

                                objectOrgMsg.put(ORIGINAL_MSG_SENDER_NAME, "" + senderName)
                                if (chatMessage.attachments != null && !chatMessage.attachments.isEmpty()) {
                                    val attachment = chatMessage.attachments.first()
                                    val isFile = !(attachment.type.equals("contact") || attachment.type.equals("location"))
                                    if (isFile) {
                                        *//*if (attachment.url.startsWith(attachmentChatUrl)) {
                                        setPros(objectOrgMsg, attachment, chatMessage)
                                    } else {
                                        Toast.makeText(applicationContext, "File not uploaded yet", Toast.LENGTH_SHORT).show();
                                    }*//*
                                        setPros(objectOrgMsg, attachment, chatMessage)

                                    } else {
                                        setPros(objectOrgMsg, attachment, chatMessage)
                                    }

                                    *//*if (attachment.type.toLowerCase().equals("contact")) {
                                    val data = APPHelper.getAttachmentData(attachment.data);
                                    val obj = JSONObject(data);
                                    val phone = obj.getString("phone");
                                    val name = obj.getString("name")
                                    objectOrgMsg.put(ORIGINAL_MSG_BODY, obj.toString())
                                }*//*
                                } else {
                                    imageViewOriginal!!.visibility = GONE
                                    prepareReply(objectOrgMsg, chatMessage)
                                }
                            }
                        }
                        R.id.action_share -> {
                            if (chatMessage != null) {
                                if (chatMessage.attachments == null || chatMessage.attachments.isEmpty()) {
                                    APPHelper.share(this@ChatMessageActivity, chatMessage.body)
                                } else {
                                    val attachment = chatMessage.attachments.first();
                                    if (attachment.type.toLowerCase() == "contact") {
                                        val jsonObjectContact = ChatUtils().getPhoneObj(attachment.data)
                                        APPHelper.share(this@ChatMessageActivity, jsonObjectContact.getString("name") + " " + jsonObjectContact.getString("phone"))
                                    } else if (attachment.type.toLowerCase() == "location") {
                                        val jsonObjectLoc = ChatUtils().getLocObj(attachment.data)
                                        APPHelper.share(this@ChatMessageActivity, "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=" + jsonObjectLoc.getString("location_lat_lng"))

                                    } else {
                                        if (cardViewDownload.visibility == VISIBLE) {
                                            Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
                                        } else {
                                            if (attachment.contentType != null) {
                                                if (attachment.url.startsWith(attachmentChatUrl)) {
                                                    val filename = attachment.url.substring(attachment.url.lastIndexOf("/") + 1)
                                                    val mime = Arrays.asList(attachment.data);
                                                    cardViewDownload.visibility = VISIBLE
                                                    if (mime.size > 1) {
                                                        downloadTask = DownloadTask(applicationContext, this@ChatMessageActivity, "share", filename, mime.get(1), downloadCallbacks)
                                                        downloadTask!!.execute(attachment.url)
                                                    } else {
                                                        downloadTask = DownloadTask(applicationContext, this@ChatMessageActivity, "share", filename, attachment.contentType, downloadCallbacks)
                                                        downloadTask!!.execute(attachment.url)
                                                    }

                                                } else {
                                                    APPHelper.shareFile(this@ChatMessageActivity, attachment.url);
                                                }


                                            }
                                        }
                                    }
                                }
                            }
                        }
                        R.id.action_delete -> {
                            layoutProgress.visibility = VISIBLE
                            val messageIDs = hashSetOf<String>()
                            if (chatMessage != null) {
                                msgDeleteId = chatMessage.id
                                messageIDs.add(chatMessage.id)
                            }
                            ConnectycubeRestChatService.deleteMessage(msgDeleteId, false).performAsync(object : EntityCallback<Void> {
                                override fun onSuccess(p0: Void?, p1: Bundle?) {
                                    layoutProgress.visibility = GONE

                                    DeleteMessage().execute();

                                }

                                override fun onError(p0: ResponseException?) {
                                    layoutProgress.visibility = GONE
                                    if (p0 != null) {
                                        layoutProgress.visibility = GONE
                                        Toast.makeText(applicationContext, p0.message, Toast.LENGTH_LONG).show()
                                    }
                                }

                            });
                            *//*ConnectycubeRestChatService.deleteMessages(messageIDs, false).performAsync(object : EntityCallback<Void> {
                            override fun onSuccess(aVoid: Void, bundle: Bundle) {
                                layoutProgress.visibility = GONE

                            }

                            override fun onError(e: ResponseException) {
                                messageDeleteListener.processMessageDeleted(msgDeleteId, chatDialog.id.toString())

                            }
                        })*//*

                        }
                    }
                    true
                }
                popupMenu.show()*/
            }
        }

    }

    private var clearTypingTimer: Timer? = null

    private val textTypingWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            chatDialog.sendIsTypingNotification(object : EntityCallback<Void> {
                override fun onSuccess(v: Void?, b: Bundle?) {
                    restartTypingTimer();
                }

                override fun onError(ex: ResponseException?) {
                    Timber.e(ex)
                }
            })

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        setContentView(R.layout.activity_chatmessages)

        var profileName=intent.getStringExtra("link")

        if(profileName!=null)
        {
            input_chat_message.setText(profileName);
        }

        databaseHandler = DatabaseHandler(applicationContext)
        sqLiteDatabase = databaseHandler!!.writableDatabase;
        setSupportActionBar(toolbar)

            initWithData(intent)


        textViewOriginalMessage = findViewById<TextView>(R.id.textViewOriginalMessage)
        textViewOriginalSender = findViewById<TextView>(R.id.textViewOriginalSender)
        imageViewOriginal = findViewById<ImageView>(R.id.imageViewOriginal)


        //checkBlock()
        switchBlock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    blockUser()
                } else {
                    unblock()
                }
            }
        }

        textViewCancelReply.setOnClickListener { v ->
            removeReplyMode()
        }

        getRequestAsyncTask = GetRequestAsyncTask(applicationContext, apiCallbacks);
        getRequestAsyncTask!!.type = "blocked_countries";
        getRequestAsyncTask!!.execute(MAIN_URL + "call_blocked_countries.php");



        /*input_chat_message.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                MyPrefs(applicationContext, APP_PREF).putString(chatDialog.dialogId, p0.toString())

                if (p0.toString().equals("")) {

                    button_chat_send.setImageResource(R.drawable.ic_mic_white_24dp)
                } else {
                    button_chat_send.setImageResource(R.drawable.ic_send_black_24dp)

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })*/

        textViewCancel.setOnClickListener { v ->
            if (downloadTask != null) {
                downloadTask!!.cancel(true)
                cardViewDownload.visibility = GONE
            }

        };
    }

    private fun initWithData(intent: Intent) {
        if (intent.hasExtra(EXTRA_CHAT)) {
            val chatDialog = intent.getSerializableExtra(EXTRA_CHAT) as ConnectycubeChatDialog
            modelChatMessageList = getChatMessageListViewModel(chatDialog.dialogId)
            AppNotificationManager.getInstance().clearNotificationData(this, chatDialog.dialogId)
        } else if (intent.hasExtra(EXTRA_CHAT_ID)) {
            val dialogId = intent.getStringExtra(EXTRA_CHAT_ID)
            modelChatMessageList = getChatMessageListViewModel(dialogId)
            AppNotificationManager.getInstance().clearNotificationData(this, dialogId)
        }
        if (this::modelChatMessageList.isInitialized) {
        subscribeToDialog()
}

    }

    private fun subscribeToDialog() {

            modelChatMessageList.liveDialog.observe(this, Observer { resource ->
                when (resource.status) {
                    com.connectycube.messenger.vo.Status.SUCCESS -> {
                        resource.data?.let { chatDialog ->
                            bindChatDialog(chatDialog)
                        }
                    }
                    com.connectycube.messenger.vo.Status.LOADING -> {
                    }
                    com.connectycube.messenger.vo.Status.ERROR -> {

                        resource.data?.let { chatDialog ->
                            bindChatDialog(chatDialog)
                        }
                        Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                    }
                }
            })

    }

    private fun subscribeToChatConnectionChanges() {

        progressDialog = APPHelper.createProgressDialog(this@ChatMessageActivity, "Loading", false)
        progressDialog!!.show();
        LiveDataBus.subscribe(EVENT_CHAT_LOGIN, this, Observer<EventChatConnection> {
            if (it.connected) {
                bindToChatConnection()
            } else {

                if (progressDialog != null) {
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }

                Toast.makeText(this, R.string.chat_connection_problem, Toast.LENGTH_LONG).show()
                //finish()
            }
        })
    }

    private fun bindChatDialog(chatDialog: ConnectycubeChatDialog) {
        this.chatDialog = chatDialog

        modelChatMessageList.unreadCounter = chatDialog.unreadMessageCount ?: 0

        initChatAdapter()
        initToolbar()
        initModelSender()
        Log.i("ChatType", chatDialog.type.name)

        subscribeToOccupants()
        getDialogOccupants(chatDialog)
        if (ConnectycubeChatService.getInstance().isLoggedIn) {
            bindToChatConnection()
        } else {

            subscribeToChatConnectionChanges()
        }

        input_chat_message.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                inputMsg = p0.toString();

                MyPrefs(applicationContext, APP_PREF).putString(chatDialog.dialogId, p0.toString())

                if (p0.toString().equals("")) {

                    button_chat_send.setImageResource(R.drawable.ic_mic_white_24dp)
                } else {
                    button_chat_send.setImageResource(R.drawable.ic_send_black_24dp)

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        if (intent.hasExtra("message")) {
            val message = intent.getStringExtra("message")
            msgType = message;
            if (intent.getBooleanExtra("is_attachment", false)) {

                val attachment: ConnectycubeAttachment = intent.getSerializableExtra("attachment") as ConnectycubeAttachment
                Log.i("Attachment", "" + Gson().toJson(attachment));

                //attachmentUrl = intent.getStringExtra("attachment_url")
                val attachmentType = attachment.type
                // contentType = intent.getStringExtra("content_type")
                msgType = attachmentType;
                if (attachmentType.equals("contact")) {
                    var attachmentData = attachment.data



                    if (attachmentData != null) {
                        if (!attachmentData.contains("\"name\"") || !attachmentData.contains("\"phone\"")) {
                            attachmentData = String(Base64.decode(attachmentData, Base64.DEFAULT));
                        }
                        uploadAttachmentContact(attachmentData, "contact", "Contact")
                    }
                } else if (attachmentType.equals("location")) {
                    var attachmentData = attachment.data


                    if (attachmentData != null) {
                        if (!attachmentData.contains("\"location\"")) {
                            attachmentData = String(Base64.decode(attachmentData, Base64.DEFAULT));
                        }
                        uploadAttachmentContact(attachmentData, "location", "Location")
                    }
                } else {
                    val attachmentUrl = attachment.url
                    Log.i("Attachment", "" + attachmentUrl);
                    if (attachmentUrl.startsWith(attachmentChatUrl)) {
                        val connectycubeChatMessage = ConnectycubeChatMessage();
                        connectycubeChatMessage.setSaveToHistory(true);
                        connectycubeChatMessage.body = message;

                        connectycubeChatMessage.addAttachment(attachment)
                        sendChatMessageForwardAttachment(connectycubeChatMessage)
                    } else {
                        uploadAttachment(attachmentUrl, attachmentType, message)
                    }
                }
            } else {
                sendChatMessage(message);
            }
        }

        input_chat_message.setText(MyPrefs(applicationContext, APP_PREF).getString(chatDialog.dialogId))

    }

    private fun initModelSender() {
        modelMessageSender = getMessageSenderViewModel()
    }

    private fun subscribeMessageSenderAttachment() {
        modelMessageSender.liveMessageAttachmentSender.observe(this, Observer { resource ->
            when {
                resource.status == com.connectycube.messenger.vo.Status.LOADING -> {
                    resource.progress?.let {
                        val msg = resource.data
                        if (msg?.id == chatAdapter.getItemByPosition(0)?.id) {
                            Timber.d("subscribeMessageSenderAttachment LOADING progress= $it")
                            chatAdapter.updateAttachmentProgress(0, it)
                        }
                    }
                }
                resource.status == com.connectycube.messenger.vo.Status.SUCCESS -> {
                    resource.data?.let {
                        submitMessage(it)
                    }
                }
                resource.status == com.connectycube.messenger.vo.Status.ERROR -> {
                    Toast.makeText(
                            applicationContext,
                            getString(R.string.sending_message_error, resource.message),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun subscribeMessageSenderText() {
        modelMessageSender.liveMessageSender.observe(this, Observer { resource ->
            when {
                resource.status == com.connectycube.messenger.vo.Status.LOADING -> {
                }
                resource.status == com.connectycube.messenger.vo.Status.SUCCESS -> {
                    resource.data?.let {
                        submitMessage(it)
                    }
                }
                resource.status == com.connectycube.messenger.vo.Status.ERROR -> {
                    Toast.makeText(
                            applicationContext,
                            getString(R.string.sending_message_error, resource.message),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun updateChatDialogData() {
        modelChatMessageList.getChatDialog().observe(this, Observer { resource ->
            when (resource.status) {
                com.connectycube.messenger.vo.Status.SUCCESS -> {
                    resource.data?.let { chatDialog ->
                        loadChatDialogPhoto(
                                applicationContext,
                                chatDialog.isPrivate,
                                chatDialog.photo,
                                avatar_img
                        )
                        chat_message_name.text = chatDialog.name
                        subscribeToOccupants(chatDialog)
                    }
                }
                else -> {
                    // Ignore all other status.
                }
            }
        })
    }

    private fun bindToChatConnection() {
        Log.i("ChatConnection", "BindSuccess")
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
        chatDialog.initForChat(ConnectycubeChatService.getInstance())
        initChat(chatDialog)

        subscribeMessageSenderAttachment()
        subscribeMessageSenderText()
        chatDialog.addMessageListener(messageListener)

        initManagers()

        input_chat_message.addTextChangedListener(textTypingWatcher)
        chat_message_name.text = chatDialog.name
        loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, chatDialog.photo, avatar_img)

        if (chatDialog.isPrivate) {

            if (intent.hasExtra("private_name")) {
                chat_message_name.text = intent.getStringExtra("private_name")
                loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, intent.getStringExtra("private_image"), avatar_img)
            }


            class UsersTask() : AsyncTask<Void, Void, List<User>>() {
                override fun doInBackground(vararg params: Void?): List<User> {
                    val users: List<User> = AppDatabase.getInstance(applicationContext).userDao().getUsersByIdsPvt(chatDialog.occupants, SharedPreferencesManager.getInstance(applicationContext).getCurrentUser().id)
                    return users

                }

                override fun onPostExecute(result: List<User>?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        if (result.isNotEmpty()) {
                            Log.i("DialogUser", Gson().toJson(result))
                            connectycubeUser = result.get(0).conUser;
                            coasLoginReceiver = connectycubeUser!!.login
                            receiverPhone = connectycubeUser!!.phone
                            userID = connectycubeUser!!.id
                            chat_message_name.text = APPHelper.getContactName(applicationContext, receiverPhone, connectycubeUser!!.fullName)
//                            val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(userID)
                            //val lastActive = System.currentTimeMillis() - seconds
                            if (connectycubeUser!!.lastRequestAt != null) {
                                chat_message_members_typing.setText(sdfNativeDateTime.format(connectycubeUser!!.lastRequestAt))
                            }
                            val image = connectycubeUser!!.avatar;
                            if (image != null) {
                                var imagetoload = image;
                                if (image.startsWith("profile")) {
                                    imagetoload = MAIN_URL_IMAGE + image;
                                }
                                loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, imagetoload, avatar_img)
                            }
                            handler.post(runnableActive)


                            subscribeOnline()


                        } else {
                            val image = chatDialog.photo;
                            var imagetoload = image;
                            if (image.startsWith("profile")) {
                                imagetoload = MAIN_URL_IMAGE + image;
                            }
                            loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, imagetoload, avatar_img)

                        }
                    }
                    // ...
                }
            }

            // UsersTask().execute();
        } else {
            chat_message_name.text = chatDialog.name
            getDialogOccupants(chatDialog)
            try {
                menuChat!!.findItem(R.id.action_voice).setVisible(true);
                menuChat!!.findItem(R.id.action__video).setVisible(true);
            } catch (e: Exception) {
                e.printStackTrace()
            }
            handler.post(runnableGroup)

            loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, chatDialog.photo, avatar_img)
        }
    }

    fun subscribeOnline() {
        if (blockedByYou == "0" && blockedByOther == "0") {
            var lastActive: Long = connectycubeUser!!.createdAt.time;
            try {
                lastActive = connectycubeUser!!.lastRequestAt.time
                chat_message_members_typing.text = sdfNativeDateTime.format(Date(lastActive))
                Log.i("Presence", "" + userID + " " + sdfNativeDateTime.format(Date(lastActive)));

            } catch (e: Exception) {
            }
            /*try {
                val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(userID)
                lastActive = System.currentTimeMillis() - seconds
                chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))
            } catch (e: Exception) {

            }*/

            val rosterListener: RosterListener = object : RosterListener {
                override fun entriesDeleted(userIds: Collection<Int?>?) {

                }

                override fun entriesAdded(userIds: Collection<Int?>?) {

                }

                override fun entriesUpdated(userIds: Collection<Int?>?) {

                }

                override fun presenceChanged(presence: ConnectycubePresence?) {
                    Log.i("Presence", "" + userID + " " + presence!!.type.toString())

                    try {

                        if (presence.type == ConnectycubePresence.Type.online) {
                            // User is online
                            chat_message_members_typing.setText("Online")
                        } else {
                            // User is offline
                            chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val subscriptionListener = object : SubscriptionListener {
                override fun subscriptionRequested(p0: Int) {
                    Log.i("Subscription", "" + p0)
                    chatRoster!!.confirmSubscription(p0)
                    val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(p0)
                    lastActive = System.currentTimeMillis() - seconds
                    chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))
                }
            }

            // Do this after success Chat login
            chatRoster = ConnectycubeChatService.getInstance()
                    .getRoster(
                            ConnectycubeRoster.SubscriptionMode.mutual,
                            subscriptionListener
                    )
            if (chatRoster != null) {


                chatRoster!!.addRosterListener(rosterListener)

                val entries = chatRoster!!.entries



                if (chatRoster!!.contains(userID)) {
                    chatRoster!!.subscribe(userID)
                } else {
                    chatRoster!!.createEntry(userID, null)
                }
            }

        } else {
            chat_message_members_typing.text = ""

        }
    }

    private fun initToolbar() {
        back_btn.setOnClickListener { onBackPressed() }
        toolbar_layout.setOnClickListener { startChatDetailsActivity() }

        /*if (chatDialog.isPrivate) {


                class UsersTask() : AsyncTask<Void, Void, List<User>>() {
                    override fun doInBackground(vararg params: Void?): List<User> {
                        val users: List<User> = AppDatabase.getInstance(applicationContext).userDao().getUsersByIdsPvt(chatDialog.occupants, SharedPreferencesManager.getInstance(applicationContext).getCurrentUser().id)
                        return users

                    }

                    override fun onPostExecute(result: List<User>?) {
                        super.onPostExecute(result)
                        if (result != null) {
                            if (result.isNotEmpty()) {
                                Log.i("DialogUser", Gson().toJson(result))
                                connectycubeUser = result.get(0).conUser;
                                coasLoginReceiver = connectycubeUser!!.login
                                receiverPhone = connectycubeUser!!.phone
                                userID = connectycubeUser!!.id

                                chat_message_name.text = connectycubeUser!!.fullName
                                chat_message_members_typing.setText(sdfNativeDateTime.format(connectycubeUser!!.lastRequestAt))

                                loadChatDialogPhoto(this@ChatMessageActivity, chatDialog.isPrivate, result.get(0).conUser.avatar, avatar_img)
                                handler.post(runnableActive)

                                val rosterListener: RosterListener = object : RosterListener {
                                    override fun entriesDeleted(userIds: Collection<Int?>?) {

                                    }

                                    override fun entriesAdded(userIds: Collection<Int?>?) {

                                    }

                                    override fun entriesUpdated(userIds: Collection<Int?>?) {

                                    }

                                    override fun presenceChanged(presence: ConnectycubePresence?) {
                                        val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(userID)
                                        val lastActive = System.currentTimeMillis() - seconds

                                        if (presence != null) {
                                            if (presence.type == ConnectycubePresence.Type.online) {
                                                // User is online
                                                chat_message_members_typing.setText("Online")
                                            } else {
                                                // User is offline
                                                chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))
                                            }
                                        }
                                    }
                                }

                                val subscriptionListener = object : SubscriptionListener {
                                    override fun subscriptionRequested(p0: Int) {
                                        Log.i("Subscription", "" + p0)
                                        chatRoster!!.confirmSubscription(p0)
                                    }
                                }

                                // Do this after success Chat login
                                chatRoster = ConnectycubeChatService.getInstance()
                                        .getRoster(
                                                ConnectycubeRoster.SubscriptionMode.mutual,
                                                subscriptionListener
                                        )
                                if (chatRoster != null) {


                                    chatRoster!!.addRosterListener(rosterListener)

                                    val entries = chatRoster!!.entries



                                    if (chatRoster!!.contains(userID)) {
                                        chatRoster!!.subscribe(userID)
                                    } else {
                                        chatRoster!!.createEntry(userID, null)
                                    }
                                }

                            } else {
                                loadChatDialogPhoto(this@ChatMessageActivity, chatDialog.isPrivate, chatDialog.photo, avatar_img)

                            }
                        }
                        // ...
                    }
                }

                UsersTask().execute();
        } else {
            chat_message_name.text = chatDialog.name

            handler.post(runnableGroup)

            loadChatDialogPhoto(this, chatDialog.isPrivate, chatDialog.photo, avatar_img)
        }*/

    }

    fun checkActiveBlock() {

        val map = HashMap<String, String>();
        map["coas_id"] = coasLoginReceiver
        map["device_id"] = MyPrefs(applicationContext, APP_PREF).getString("token")
        map["user_id"] = MyPrefs(applicationContext, APP_PREF).getString("userId")
        asyncTask = PostRequestAsyncTask(applicationContext, map, "driver", apiCallbacks);
        asyncTask!!.execute(MAIN_URL + "check_user_active.php")
    }

    private fun subscribeToOccupants(chatDialog: ConnectycubeChatDialog = this.chatDialog) {

        modelChatMessageList.getOccupants(chatDialog).observe(this, Observer { resource ->
            when (resource.status) {
                com.connectycube.messenger.vo.Status.LOADING -> {
                }
                com.connectycube.messenger.vo.Status.ERROR -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
                com.connectycube.messenger.vo.Status.SUCCESS -> {
                    resource.data?.let {
                        val occupantsWithoutCurrent = resource.data.filter { it.id != ConnectycubeSessionManager.getInstance().activeSession.userId }
                        occupants.putAll(occupantsWithoutCurrent.associateBy({ it.id }, { it }))
                        updateChatAdapter()
                    }

                    Log.i("Occupants", occupants.toString())

                    membersNames.run {
                        clear()
                        //addAll(occupants.map { it.value.fullName ?: it.value.login })
                        addAll(occupants.map {
                            APPHelper.getContactName(applicationContext, it.value.phone, it.value.fullName)
                                    ?: it.value.login
                        })

                    }

                    Log.i("UpdateGroup", membersNames.toString())

                    if (!chatDialog.isPrivate) chat_message_members_typing.text = membersNames.joinToString()
                }
            }
        })
    }

    private fun getChatMessageListViewModel(dialogId: String): ChatMessageListViewModel {
        val chatMessageListViewModel: ChatMessageListViewModel by viewModels {
            InjectorUtils.provideChatMessageListViewModelFactory(this.application, dialogId)
        }
        return chatMessageListViewModel
    }

    private fun getMessageSenderViewModel(): MessageSenderViewModel {
        val messageSender: MessageSenderViewModel by viewModels {
            InjectorUtils.provideMessageSenderViewModelFactory(this.application, chatDialog)
        }
        return messageSender
    }

    private fun initChatAdapter() {

        chatAdapter = ChatMessageAdapter(this, chatDialog, attachmentClickListener)
        chatAdapter.chatMessageLongClick = chatMessageLongClick
        scroll_fb.setOnClickListener { scrollDown() }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = false
        layoutManager.reverseLayout = true
        messages_recycleview.layoutManager = layoutManager
        messages_recycleview.adapter = chatAdapter
        messages_recycleview.addItemDecoration(
                MarginItemDecoration(
                        resources.getDimension(R.dimen.margin_normal).toInt()
                )
        )

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (modelChatMessageList.scroll) {
                    scrollDown()
                }
            }
        })

        messages_recycleview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            fun shrinkFab() {
                scroll_fb.iconGravity = ICON_GRAVITY_START
                scroll_fb.shrink()
                scroll_fb.hide(false)
                scroll_fb.text = ""
                modelChatMessageList.unreadCounter = 0
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = layoutManager.itemCount
                val firstVisible = layoutManager.findFirstVisibleItemPosition()

                val shouldShow = firstVisible >= 1
                if (totalItemCount > 0 && shouldShow) {
                    if (!scroll_fb.isShown) {
                        scroll_fb.show(false)
                        scroll_fb.alpha = 0.3f
                    }
                    val count: String? = Regex(pattern = "\\d+").find(input = scroll_fb.text.toString())?.value
                    val shouldAddCounter =
                            scroll_fb.text.isEmpty() || count?.toInt() != modelChatMessageList.unreadCounter
                    if (modelChatMessageList.unreadCounter > 0 && shouldAddCounter) {
                        scroll_fb.iconGravity = ICON_GRAVITY_TEXT_END
                        scroll_fb.text =
                                getString(R.string.fbd_scroll_counter_label, modelChatMessageList.unreadCounter.toString())
                        scroll_fb.extend()
                    }
                } else {
                    if (scroll_fb.isShown) shrinkFab()
                }
            }
        })
        modelChatMessageList.refreshState.observe(this, Observer {
            Timber.d("refreshState= $it")
            if (it.status == Status.RUNNING && chatAdapter.itemCount == 0) {
                showProgress(progressbar)
            } else if (it.status == Status.SUCCESS) {
                hideProgress(progressbar)
            }
        })

        modelChatMessageList.networkState.observe(this, Observer {
            Timber.d("networkState= $it")
            chatAdapter.setNetworkState(it)
        })

        modelChatMessageList.messages.observe(this, Observer {
            Timber.d("submitList= ${it.size}")
            input_layout.visibility = VISIBLE
            chatAdapter.submitList(it)
        })


    }

    private fun updateChatAdapter() {
        chatAdapter.setOccupants(occupants)
        Log.i("Occupants", Gson().toJson(occupants))

    }

    private fun initChat(chatDialog: ConnectycubeChatDialog) {
        when (chatDialog.type) {
            ConnectycubeDialogType.GROUP, ConnectycubeDialogType.BROADCAST -> {
                chatDialog.join(object : EntityCallback<Void> {
                    override fun onSuccess(result: Void?, args: Bundle?) {
                        layoutProgress.visibility = GONE
                        if (intent.hasExtra("chat_name")) {
                            msgType = MyPrefs(applicationContext, APP_PREF).getString("firstName") + " created group " + chatDialog.name;

                            sendChatMessage(MyPrefs(applicationContext, APP_PREF).getString("firstName") + " created group " + chatDialog.name)
                        }
                    }

                    override fun onError(exception: ResponseException) {
                        layoutProgress.visibility = GONE

                    }
                })
            }

            ConnectycubeDialogType.PRIVATE -> {
                Timber.d("ConnectycubeDialogType.PRIVATE type")

            }

            else -> {
                Timber.d("Unsupported type")

                finish()
            }
        }
    }

    private fun initManagers() {
        ConnectycubeChatService.getInstance().messageStatusesManager.addMessageStatusListener(messageStatusListener)
        ConnectycubeChatService.getInstance().messageStatusesManager.addMessageDeleteListener(messageDeleteListener)
        chatDialog.addIsTypingListener(messageTypingListener)
        chatDialog.addMessageSentListener(messageSentListener)
    }

    private fun unregisterChatManagers() {
        ConnectycubeChatService.getInstance().messageStatusesManager.removeMessageStatusListener(messageStatusListener)
        ConnectycubeChatService.getInstance().messageStatusesManager.removeMessageDeleteListener(messageDeleteListener)
        chatDialog.removeMessageListrener(messageListener)
        chatDialog.removeIsTypingListener(messageTypingListener)
        chatDialog.removeMessageSentListener(messageSentListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ConnectycubeChatService.getInstance().isLoggedIn) {
            unregisterChatManagers()
            input_chat_message.removeTextChangedListener(textTypingWatcher)
            handler.removeCallbacks(runnableActive)
            if (runnableGroup != null) {
                handler.removeCallbacks(runnableGroup)
            }

            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
            if (downloadTask != null) {
                downloadTask!!.cancel(true)
            }
        }

        try {
            if (chatDialog.isPrivate) {
                if (chatRoster != null) {
                    chatRoster!!.unsubscribe(userID)
                }
            }
        } catch (e: Exception) {
        }
    }

    fun onAttachClick(view: View) {
        val popupMenu = PopupMenu(this@ChatMessageActivity, view)
        //Inflating the Popup using xml file
        popupMenu.menuInflater.inflate(R.menu.menu_attachment_chat, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_image_video ->
                    if (permissionsHelper.areAllImageGranted()) {
                        Timber.d("onAttachClick areAllImageGranted")
                        file = GetPath.createImageFile(this@ChatMessageActivity)
                        APPHelper.launchPictureIntent(applicationContext, this@ChatMessageActivity, view, file)

                    } else permissionsHelper.requestImagePermissions()

                R.id.action_contact ->
                    if (APPHelper.checkPermissionGranted(applicationContext, READ_CONTACTS)) {
                        openContacts()
                    } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(READ_CONTACTS), 300)
                    }

                R.id.action__video ->
                    /*  if (APPHelper.checkPermissionGranted(applicationContext, WRITE_EXTERNAL_STORAGE)) {
                     //   APPHelper.launchVideoIntent(this@ChatMessageActivity, view)
                        val calleeId = receiverPhone.replace("+", "")

                        //    CallService.startCallActivity(mContext, user.getUserId(), user.getNickname(), false);
                    //    val calleeId: String = user.userId
                        if (!TextUtils.isEmpty(calleeId)) {
                            com.coasapp.coas.sendbird.call.CallService.startCallActivity(this@ChatMessageActivity, calleeId, chatDialog.name, true)
                            PrefUtils.setCalleeId(this@ChatMessageActivity, calleeId)
                        }*/

                    if (APPHelper.checkPermissionGranted(applicationContext, WRITE_EXTERNAL_STORAGE)) {
                        APPHelper.launchVideoIntent(this@ChatMessageActivity, view)
                    } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
                    }
                /*   } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
                    }*/
                R.id.action_voice ->
                    /*if (APPHelper.checkPermissionsGranted(applicationContext, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO))) {
                     //   recordDialog(this@ChatMessageActivity, "")
                        val calleeId = receiverPhone.replace("+", "")
                        if (!TextUtils.isEmpty(calleeId)) {
                            com.coasapp.coas.sendbird.call.CallService.startCallActivity(this@ChatMessageActivity, calleeId, chatDialog.name, false)
                            PrefUtils.setCalleeId(this@ChatMessageActivity, calleeId)
                        }
                    } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
                    }*/

                    if (APPHelper.checkPermissionsGranted(applicationContext, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO))) {
                        recordDialog(this@ChatMessageActivity, "")
                    } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
                    }

                R.id.action_document ->
                    if (APPHelper.checkPermissionGranted(applicationContext, WRITE_EXTERNAL_STORAGE)) {
                        APPHelper.launchDoc(this@ChatMessageActivity)
                    } else {
                        ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
                    }

                R.id.action_location -> {
                    /* val locationAddress = LocationHelper.getCurrentLocationString(this@ChatMessageActivity);
                     val latLngStr = LocationHelper.getCurrentLocationLatLng(this@ChatMessageActivity)
                     val thumb = "https://maps.googleapis.com/maps/api/staticmap?center" + locationAddress.replace(" ", "+").replace(", ", ",") + "&zoom=13&size=600x300&maptype=roadmap&markers=color:green%7C" + latLngStr + "&key="+getString(R.string.google_maps_key);
                     val jsonObjectLoc = JSONObject();
                     jsonObjectLoc.put("location_lat_lng", latLngStr);
                     jsonObjectLoc.put("location_address", locationAddress);
                     jsonObjectLoc.put("location_image", thumb)
                     msgType = "Location"
                     uploadAttachmentContact(jsonObjectLoc.toString(), "location", "Location");*/

                    startActivityForResult(Intent(applicationContext, MyLocationActivity::class.java), 30);
                }
            }
            true
        })

        popupMenu.show()
    }

    fun openContacts() {
        val i = Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, 97);
    }

    fun onSendChatClick(view: View) {
        val text = input_chat_message.text.toString().trim()
        msgType = input_chat_message.text.toString()
        if (text.isNotEmpty()) sendChatMessage(text)
        else {
            if (APPHelper.checkPermissionsGranted(applicationContext, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO))) {
                recordDialog(this@ChatMessageActivity, "")
            } else {
                ActivityCompat.requestPermissions(this@ChatMessageActivity, arrayOf(WRITE_EXTERNAL_STORAGE), 301)
            }
        }
    }

    private fun onMessageAttachmentClicked(attach: ConnectycubeAttachment) {
        Timber.d("message attachment= $attach")
        Log.i("Attachment", "" + attach.data);
        Log.i("Attachment", "" + attach.contentType);

        if (attach.url != null)
            Log.i("Attachment", attach.url)

        if (attach.type.toLowerCase().equals("image"))
            startAttachmentPreview(attach)
        else if (attach.type.toLowerCase().equals("video"))
            startAttachmentPreviewVideo(attach)
        else if (attach.type.toLowerCase().equals("audio")) {
            startAttachmentPreviewVoice(attach)
        } else if (attach.type.toLowerCase().equals("contact")) {
            var data = attach.data;
            if (!data.contains("\"name\"") || !data.contains("\"phone\"")) {
                data = String(Base64.decode(attach.data, Base64.DEFAULT));
            }
            val obj = JsonParser().parse(data).getAsJsonObject();
            val phone = obj.getAsJsonPrimitive("phone").getAsString();
            val name = obj.getAsJsonPrimitive("name").getAsString();
            APPHelper.openDialer(this, phone)
        }
        /* else if (attach.type.toLowerCase().equals("location")) {
             LocationHelper.openDirections(this@ChatMessageActivity,attach.mess)
         }*/
        else {
            startAttachmentPreviewDoc(attach)
        }

    }

    private fun startAttachmentPreview(attach: ConnectycubeAttachment) {
        /* val intent = Intent(this, AttachmentPreviewActivity::class.java)
         intent.putExtra(EXTRA_URL, attach.url)
         startActivity(intent)
         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)*/

        if (attach.url.startsWith(attachmentChatUrl)) {
            val filename = attach.url.substring(attach.url.lastIndexOf("/") + 1)

            val extr = Environment.getExternalStorageDirectory().toString()

            val coasFolder = File("$extr/COASAPP/")
            //val filePath = cDir!!.getPath() + "/.download/" + filename + ".aac"
            val filePath = coasFolder.toString() + "/" + filename + "." + attach.contentType

            val file = File(filePath)
            Log.i("DownloadVideoCheck", filePath)
            if (cardViewDownload.visibility == VISIBLE) {
                Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
            } else {
                if (!file.exists()) {
                    progressBarDownload.progress = 0;
                    cardViewDownload.visibility = VISIBLE

                    downloadTask = DownloadTask(applicationContext, this, "image", filename, attach.contentType, downloadCallbacks)
                    downloadTask!!.execute(attach.url)
                } else {
                    /* val voiceRecording = VoiceRecording(this@ChatMessageActivity)
                     voiceRecording.showAudioAlert(this, "url", filePath)*/

                    val intent = Intent(this, AttachmentPreviewActivity::class.java)
                    intent.putExtra(EXTRA_URL, attach.url)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                }
            }

        } else {
            /*val voiceRecording = VoiceRecording(this@ChatMessageActivity)
            voiceRecording.showAudioAlert(this, "url", attach.url)*/

            val intent = Intent(this, AttachmentPreviewActivity::class.java)
            intent.putExtra(EXTRA_URL, attach.url)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    private fun startAttachmentPreviewVideo(attach: ConnectycubeAttachment) {

        /*   val intent = Intent(this, VideoViewActivity::class.java)
           intent.putExtra("file", attach.url)
           intent.putExtra("type", "url")
           startActivity(intent)
           overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)*/

        val cDir = application.getExternalFilesDir(null)
        //  File saveFilePath = new File(cDir.getPath() + "/lkf/" + filename);
        if (attach.url.startsWith(attachmentChatUrl)) {
            val filename = attach.url.substring(attach.url.lastIndexOf("/") + 1)

            val extr = Environment.getExternalStorageDirectory().toString()

            val coasFolder = File("$extr/COASAPP/")
            //val filePath = cDir!!.getPath() + "/.download/" + filename + ".aac"
            val filePath = coasFolder.toString() + "/" + filename + "." + attach.contentType

            val file = File(filePath)
            Log.i("DownloadVideoCheck", filePath)
            if (cardViewDownload.visibility == VISIBLE) {
                Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
            } else {
                if (!file.exists()) {
                    progressBarDownload.progress = 0;
                    cardViewDownload.visibility = VISIBLE

                    downloadTask = DownloadTask(applicationContext, this, "video", filename, attach.contentType, downloadCallbacks)
                    downloadTask!!.execute(attach.url)
                } else {
                    /* val voiceRecording = VoiceRecording(this@ChatMessageActivity)
                     voiceRecording.showAudioAlert(this, "url", filePath)*/

                    val intent = Intent(this, VideoViewActivity::class.java)
                    intent.putExtra("file", attach.url)
                    intent.putExtra("type", "local")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                }
            }

        } else {
            /*val voiceRecording = VoiceRecording(this@ChatMessageActivity)
            voiceRecording.showAudioAlert(this, "url", attach.url)*/

            val intent = Intent(this, VideoViewActivity::class.java)
            intent.putExtra("file", attach.url)
            intent.putExtra("type", "local")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


    }

    private fun startAttachmentPreviewDoc(attach: ConnectycubeAttachment) {
        var file: File

        val cDir = application.getExternalFilesDir(null)
        //  File saveFilePath = new File(cDir.getPath() + "/lkf/" + filename);
        if (attach.url.startsWith(attachmentChatUrl)) {
            val filename = attach.url.substring(attach.url.lastIndexOf("/") + 1)
            // val filePath = cDir!!.getPath() + "/.download/" + filename + "." + attach.type.replace(".", "")

            val extr = Environment.getExternalStorageDirectory().toString()

            val coasFolder = File("$extr/COASAPP/")
            //val filePath = cDir!!.getPath() + "/.download/" + filename + ".aac"
            val filePath = coasFolder.toString() + "/" + filename + "." + attach.type.replace(".", "")

            file = File(filePath)
            Log.i("DownloadDocCheck", filePath)
            if (cardViewDownload.visibility == VISIBLE) {
                Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
            } else {
                if (!file.exists()) {
                    progressBarDownload.progress = 0;
                    cardViewDownload.visibility = VISIBLE

                    downloadTask = DownloadTask(applicationContext, this, "doc", filename, attach.type.replace(".", ""), downloadCallbacks)
                    downloadTask!!.execute(attach.url)
                } else {
                    APPHelper.openFile(this@ChatMessageActivity, file);
                }
            }
        } else {
            APPHelper.openFile(this@ChatMessageActivity, File(attach.url));
        }
        /* val voiceRecording = VoiceRecording(this@ChatMessageActivity)
         voiceRecording.showAudioAlert(this, "url", attach.url)*/
    }


    private fun startAttachmentPreviewVoice(attach: ConnectycubeAttachment) {

        val cDir = application.getExternalFilesDir(null)
        //  File saveFilePath = new File(cDir.getPath() + "/lkf/" + filename);
        if (attach.url.startsWith(attachmentChatUrl)) {
            val filename = attach.url.substring(attach.url.lastIndexOf("/") + 1)

            val extr = Environment.getExternalStorageDirectory().toString()

            val coasFolder = File("$extr/COASAPP/")
            //val filePath = cDir!!.getPath() + "/.download/" + filename + ".aac"
            val filePath = coasFolder.toString() + "/" + filename + ".aac"

            val file = File(filePath)
            Log.i("DownloadAudioCheck", filePath)
            if (cardViewDownload.visibility == VISIBLE) {
                Toast.makeText(applicationContext, "Another download in progress", Toast.LENGTH_LONG).show()
            } else {
                if (!file.exists()) {
                    progressBarDownload.progress = 0;
                    cardViewDownload.visibility = VISIBLE

                    downloadTask = DownloadTask(applicationContext, this, "audio", filename, "aac", downloadCallbacks)
                    downloadTask!!.execute(attach.url)
                } else {
                    val voiceRecording = VoiceRecording(this@ChatMessageActivity)
                    voiceRecording.showAudioAlert(this, "url", filePath)
                }
            }

        } else {
            val voiceRecording = VoiceRecording(this@ChatMessageActivity)
            voiceRecording.showAudioAlert(this, "url", attach.url)
        }
        /* val voiceRecording = VoiceRecording(this@ChatMessageActivity)
         voiceRecording.showAudioAlert(this, "url", attach.url)*/
    }

    private fun sendChatMessage(text: String) {
        Log.i("Connectycube", ConnectycubeChatService.getInstance().isLoggedIn.toString())

        if (!ConnectycubeChatService.getInstance().isLoggedIn) {
            Toast.makeText(this, R.string.chat_connection_problem, Toast.LENGTH_LONG).show()
            return
        }
        modelChatMessageList.scroll = true
        modelMessageSender.sendMessage(text, dataReply)
        MyPrefs(applicationContext, APP_PREF).putString(chatDialog.dialogId, "")
        /* if(layoutIsReply.visibility == VISIBLE){

             uploadAttachmentContact(dataReply,"Reply",text);
         }
         else {
             modelMessageSender.sendMessage(text, dataReply)
         }*/
        input_chat_message.setText("")
        removeReplyMode()
    }


    private fun sendChatMessageForwardAttachment(message: ConnectycubeChatMessage) {
        Log.i("Connectycube", ConnectycubeChatService.getInstance().isLoggedIn.toString())

        if (!ConnectycubeChatService.getInstance().isLoggedIn) {
            Toast.makeText(this, R.string.chat_connection_problem, Toast.LENGTH_LONG).show()
            return
        }
        modelChatMessageList.scroll = true
        modelMessageSender.sendMessageForwardAttachment(message, dataReply)
        input_chat_message.setText("")
        removeReplyMode()

    }

    fun submitMessage(message: ConnectycubeChatMessage) {
        Timber.d("submitMessage modelChatMessageList.messages.value")
        modelChatMessageList.postItem(message)
    }

    fun scrollDown() {
        messages_recycleview.smoothScrollToPosition(0)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuChat = menu;
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_message_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_action_video -> {

                if (!callsAllowed) {
                    Toast.makeText(this, R.string.calls_not_allowed, Toast.LENGTH_LONG).show()

                } else {
                    val calleeId = receiverPhone.replace("+", "")

                    //    CallService.startCallActivity(mContext, user.getUserId(), user.getNickname(), false);
                    //    val calleeId: String = user.userId
                    if (!TextUtils.isEmpty(calleeId)) {
                        com.coasapp.coas.sendbird.call.CallService.dial(this@ChatMessageActivity, calleeId, true)
                        PrefUtils.setCalleeId(this@ChatMessageActivity, calleeId)
                    }

                }
                // startCall(CALL_TYPE_VIDEO)
                true
            }
            R.id.menu_action_audio -> {


                //startCall(CALL_TYPE_AUDIO)
                if (!callsAllowed) {
                    Toast.makeText(this, R.string.calls_not_allowed, Toast.LENGTH_LONG).show()

                } else {
                    val calleeId = receiverPhone.replace("+", "")
                    if (!TextUtils.isEmpty(calleeId)) {
                        com.coasapp.coas.sendbird.call.CallService.dial(this@ChatMessageActivity, calleeId, false)
                        PrefUtils.setCalleeId(this@ChatMessageActivity, calleeId)
                    }
                }
                true
            }

            R.id.action_settings -> {
                val intent = Intent(applicationContext, ChatSettingsActivity::class.java);
                startActivity(intent)
                true
            }

            R.id.action_bargain -> {
                val intent = Intent(applicationContext, RequestActivity::class.java)
                /* if (receiverId.contains("COAS0")) {
                     receiverId = receiverId.substring(receiverId.lastIndexOf("COAS0") + 1);
                 } else {
                     receiverId = receiverId.substring(receiverId.lastIndexOf("COAS") + 1);
                 }*/
                intent.putExtra("customer", coasLoginReceiver);
                intent.putExtra("source", "");
                intent.putExtra("dest", "");
                intent.putExtra("name", "");
                startActivity(intent);
                true
            }
            R.id.action_block -> {
                val block = "0"
                handler.removeCallbacks(runnableActive)
                if (chatDialog.isPrivate) {
                    val map = HashMap<String, String>();
                    if (blockedByYou == "0") {
                        map["block"] = "1"
                    } else {
                        map["block"] = "0"
                    }
                    map["coas_id"] = coasLoginReceiver
                    map["user_id"] = MyPrefs(applicationContext, APP_PREF).getString("userId")
                    asyncTask = PostRequestAsyncTask(applicationContext, map, "block", apiCallbacks);
                    asyncTask!!.execute(MAIN_URL + "block_user_chat.php")
                } else {
                    Toast.makeText(applicationContext, "Block NA for group chats", Toast.LENGTH_LONG).show();
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startCall(callType: Int) {
        val arrayBlockedCall = JSONArray(MyPrefs(applicationContext, APP_PREF).getString("blocked_countries"))
        callsAllowed = true;
        if (chatDialog.isPrivate) {
            for (i in 0 until arrayBlockedCall.length()) {
                val code = arrayBlockedCall.getJSONObject(i).getString("std_code");
                Log.i("BlockedCall", "$code $receiverPhone")
                if (receiverPhone.startsWith(code)) {
                    callsAllowed = false
                }
            }
        }
        if (!ConnectycubeChatService.getInstance().isLoggedIn) {
            Toast.makeText(this, R.string.chat_connection_problem, Toast.LENGTH_LONG).show()
        } else {

            if (!callsAllowed) {
                Toast.makeText(this, R.string.calls_not_allowed, Toast.LENGTH_LONG).show()

            } else {
                Log.i("UsersCall", chatDialog.occupants.toString())
                Log.i("UsersCall", ConnectycubeChatService.getInstance().user.id.toString());
                when (callType) {
                    CALL_TYPE_VIDEO -> startVideoCall(
                            this,
                            ArrayList(chatDialog.occupants.filter { it != ConnectycubeChatService.getInstance().user.id }), coasLoginReceiver
                    )
                    CALL_TYPE_AUDIO -> startAudioCall(

                            this,
                            ArrayList(chatDialog.occupants.filter { it != ConnectycubeChatService.getInstance().user.id }), coasLoginReceiver

                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            if (data != null && Matisse.obtainPathResult(data) != null) {
                val path = Matisse.obtainPathResult(data).iterator().next()
                //uploadAttachment(path, ConnectycubeAttachment.IMAGE_TYPE, getString(R.string.message_attachment))
            }
        } else if (requestCode == REQUEST_CODE_DETAILS && resultCode == Activity.RESULT_OK) {
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent("chats"))
            //finish()
            //getDialogOccupants(data!!.getSerializableExtra(EXTRA_CHAT) as ConnectycubeChatDialog)
            //subscribeToOccupants(chatDialog)
        } else if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            path = ResizeImage.getResizedImage(file?.absolutePath);
            msgType = "Image"
            showSendDialog(ConnectycubeAttachment.IMAGE_TYPE)
            //uploadAttachment(path, ConnectycubeAttachment.IMAGE_TYPE, getString(R.string.message_attachment))

        } else if (requestCode == 98 && resultCode == Activity.RESULT_OK) {

        /*    imagePathList = ArrayList()

            if (data!!.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    getImageFilePath(imageUri)
                }
            } else if (data.data != null) {
                val imgUri = data.data
                getImageFilePath(imgUri)
            }*/


            if(data!!.getClipData() != null) {
                count = data.getClipData()!!.getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
            /*    Toast.makeText(this, data.data.toString(),
                        Toast.LENGTH_LONG).show()*/
                flag = "2"
                for (i in 1 until count) {
                var imageUri = data.getClipData()!!.getItemAt(i).getUri();

                    path = ResizeImage.getResizedImage(GetPath.getPath(applicationContext, imageUri));
                    patharray.add(path)
                    Toast.makeText(this, imageUri.toString(),
                            Toast.LENGTH_LONG).show()
                    imagemodel = imageviewrecyclerModel(imageUri.toString());
                    imagePathList!!.toMutableList().add(imagemodel)
                  /*  Toast.makeText(this, imageUri.toString(),
                            Toast.LENGTH_LONG).show()*/
               //     imageadapter = imageviewrecyclerAdapter(imagePathList,this@ChatMessageActivity);





                //do something with the image (save it to some directory or whatever you need to do with it here)
            }



        } else if(data.getData() != null) {
        /*    var imagePath = data.getData()!!.getPath();
                Toast.makeText(this, imagePath.toString(),
                        Toast.LENGTH_LONG).show()
            //    patharray.add(imagePath.toString())
                flag = "2"
            //do something with the image (save it to some directory or whatever you need to do with it here)*/
                path = ResizeImage.getResizedImage(GetPath.getPath(applicationContext, data.data));
                patharray.add(path)
                msgType = "Image"
              //  showSendDialog(ConnectycubeAttachment.IMAGE_TYPE)
        }

            showSendDialog(ConnectycubeAttachment.IMAGE_TYPE)


           //

               /* try {
                    // When an Image is picked
                    if (requestCode === PICK_IMAGE_MULTIPLE && resultCode === RESULT_OK && null != data) {
                        // Get the Image from data
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        imagesEncodedList = ArrayList()
                        if (data.data != null) {
                            val mImageUri = data.data
                            Toast.makeText(this, mImageUri.toString(),
                                    Toast.LENGTH_LONG).show()
                            // Get the cursor
                          *//*  val cursor: Cursor? = contentResolver.query(mImageUri!!,
                                    filePathColumn, null, null, null)
                            // Move to first row
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor!!.getColumnIndex(filePathColumn[0])
                            imageEncoded = cursor.getString(columnIndex)
                            cursor!!.close()*//*
                        } else {
                            if (data.clipData != null) {
                                val mClipData = data.clipData
                                val mArrayUri = ArrayList<Uri>()
                                for (i in 0 until mClipData!!.itemCount) {
                                    val item = mClipData!!.getItemAt(i)
                                    val uri = item.uri
                                    mArrayUri.add(uri)

                                    Toast.makeText(this, mArrayUri[0].toString(),
                                            Toast.LENGTH_LONG).show()
                                    // Get the cursor
                                *//*    val cursor: Cursor? = contentResolver.query(uri, filePathColumn, null, null, null)
                                    // Move to first row
                                    cursor.moveToFirst()
                                    val columnIndex: Int = cursor!!.getColumnIndex(filePathColumn[0])
                                    imageEncoded = cursor.getString(columnIndex)
                                    (imagesEncodedList as ArrayList<String>).add(imageEncoded!!)
                                    cursor.close()*//*
                                }
                                //Log.v("LOG_TAG", "Selected Images" + mArrayUri.size())
                            }
                        }
                    } else {
                        Toast.makeText(this, "You haven't picked Image",
                                Toast.LENGTH_LONG).show()
                    }
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show()
                }

            }*/
                /*if (data.data != null) {


                    path = ResizeImage.getResizedImage(GetPath.getPath(applicationContext, data.data));
                    msgType = "Image"
                    showSendDialog(ConnectycubeAttachment.IMAGE_TYPE)
                } else {
                    APPHelper.showToast(applicationContext, "Failed to read file")
                }
            } else {
                APPHelper.showToast(applicationContext, "Failed to read file")
            }*/
            //uploadAttachment(path, ConnectycubeAttachment.IMAGE_TYPE, getString(R.string.message_attachment))
        }else  if (resultCode == Activity.RESULT_OK && requestCode == 100) {

            // if multiple images are selected
            if (data?.getClipData() != null) {
                var count = data.clipData?.itemCount

                if (count != null) {
                    for (i in 0..count - 1) {
                        var imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                        //     iv_image.setImageURI(imageUri) Here you can assign your Image URI to the ImageViews
                    }
                }
                //uploadAttachment(path, ConnectycubeAttachment.IMAGE_TYPE, getString(R.string.message_attachment))
            }
        }else if (requestCode == 97 && resultCode == Activity.RESULT_OK) {
            msgType = "Contact"
            val contactUri = data?.data;
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val cursor = getContentResolver().query(contactUri!!, projection,
                    null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                val number = cursor.getString(numberIndex);
                val name = cursor.getString(nameIndex);
                Log.i("Contact", name + number)
                // Do something with the phone number
                val jc = JsonObject();
                jc.add("phone", JsonPrimitive(number));
                jc.add("name", JsonPrimitive(name));
                msgType = "Contact"

                uploadAttachmentContact(jc.toString(), "contact", "Contact")
            }

            cursor?.close();

        } else if ((requestCode == 94 || requestCode == 93) && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.data != null) {

                    val contactUri = data.data;
                    path = GetPath.getPath(applicationContext, contactUri)
                    //uploadAttachment(path, ConnectycubeAttachment.VIDEO_TYPE, getString(R.string.message_attachment))
                    showSendDialog(ConnectycubeAttachment.VIDEO_TYPE)
                    msgType = "Video"
                } else {
                    APPHelper.showToast(applicationContext, "Failed to read file")
                }
            } else {
                APPHelper.showToast(applicationContext, "Failed to read file")
            }

        } else if ((requestCode == 92) && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.data != null) {
                    path = GetPath.getPath(applicationContext, data.data)
                    //uploadAttachment(path, ConnectycubeAttachment.VIDEO_TYPE, getString(R.string.message_attachment))
                    val extension = path.substring(path.lastIndexOf(".") + 1);
                    if (extension.toLowerCase().equals("doc") or extension.toLowerCase().equals("docx") || extension.toLowerCase().equals("pdf")) {
                        showSendDialog(extension)
                        msgType = "Document"
                    } else {
                        APPHelper.showToast(applicationContext, "Only doc, docx & pdf allowed")
                    }
                } else {
                    APPHelper.showToast(applicationContext, "Failed to read file")
                }
            } else {
                APPHelper.showToast(applicationContext, "Failed to read file")
            }

        } else if (requestCode == 30 && resultCode == Activity.RESULT_OK) {
            val locStr = data?.getStringExtra("location")
            if (locStr != null) {
                msgType = "Location"
                uploadAttachmentContact(locStr, "location", "Location")
            };
        }
    }

    private fun uploadAttachment(path: String, type: String, text: String) {
        modelChatMessageList.scroll = true
        modelMessageSender.sendAttachment(path, dataReply, type, text)
        //sendNotification()
        removeReplyMode()

    }

    private fun uploadAttachmentContact(path: String, type: String, text: String) {
        modelChatMessageList.scroll = true

        modelMessageSender.sendAttachmentContact(path, dataReply, type, text)
        //sendNotification()
        removeReplyMode()

    }

    private fun startChatDetailsActivity() {
        if (chatDialog.isPrivate) {
            val intent = Intent(applicationContext, UserDetailsActivity::class.java)
            intent.putExtra(EXTRA_CHAT_DIALOG_ID, chatDialog.dialogId)
            intent.putExtra("coas_id", coasLoginReceiver)
            //Toast.makeText(applicationContext, coasLoginReceiver, Toast.LENGTH_LONG).show();
            startActivity(intent)
        } else {
            val intent = Intent(this, ChatDialogDetailsActivity::class.java)
            intent.putExtra(EXTRA_CHAT_DIALOG_ID, chatDialog.dialogId)
            intent.putExtra(EXTRA_CHAT, chatDialog)

            startActivityForResult(intent, REQUEST_CODE_DETAILS)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_IMAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (permissionsHelper.areAllImageGranted()) {
                    Timber.d("permission was granted")
                } else {
                    Timber.d("permission is denied")
                }
                return
            }
            300 -> {
                openContacts()
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }


    private fun restartTypingTimer() {
        if (clearTypingTimer != null)
            clearTypingTimer?.cancel()
        startTimer()
    }

    private fun startTimer() {
        clearTypingTimer = Timer()
        clearTypingTimer?.schedule(TimerTypingTask(), TYPING_INTERVAL_MS)
    }

    inner class TimerTypingTask : TimerTask() {
        override fun run() {


            runOnUiThread {
                chatDialog.sendStopTypingNotification(object : EntityCallback<Void> {
                    override fun onSuccess(v: Void?, b: Bundle?) {
                    }

                    override fun onError(ex: ResponseException?) {
                        Timber.e(ex)
                    }
                })
                /*if (!chatDialog.isPrivate) chat_message_members_typing.text = membersNames.joinToString()
                else {

                    subscribeOnline()


                    *//* val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(userID)
                     val lastActive = System.currentTimeMillis() - seconds
                     chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))*//*
                }*/
            }
        }
    }

    inner class ChatTypingListener : ChatDialogTypingListener {
        override fun processUserIsTyping(dialogId: String?, userId: Int?) {
            if (userId == ConnectycubeChatService.getInstance().user.id) return
            // var userStatus = occupants[userId]?.fullName ?: occupants[userId]?.login
            var userStatus = APPHelper.getContactName(applicationContext, occupants[userId]!!.phone, occupants[userId]!!.fullName);
/*
            userStatus?.let {
                userStatus = "typing"
            }*/
            chat_message_members_typing.text = "$userStatus typing"
            //restartTypingTimer()
        }

        override fun processUserStopTyping(dialogId: String?, userId: Int?) {
            if (!chatDialog.isPrivate) chat_message_members_typing.text = membersNames.joinToString()
            else {
                chat_message_members_typing.text = ""

                subscribeOnline()


                /* val seconds = ConnectycubeChatService.getInstance().getLastUserActivity(userID)
                 val lastActive = System.currentTimeMillis() - seconds
                 chat_message_members_typing.setText(sdfNativeDateTime.format(Date(lastActive)))*/
            }
        }
    }

    inner class ChatMessageListener : ChatDialogMessageListener {
        override fun processMessage(dialogId: String, chatMessage: ConnectycubeChatMessage, senderId: Int) {
            Timber.d("ChatMessageListener processMessage ${chatMessage.body}")
            val isIncoming = senderId != ConnectycubeChatService.getInstance().user.id
            if (isIncoming) {
                modelChatMessageList.unreadCounter++
            } else {
                modelChatMessageList.scroll = true
            }
            submitMessage(chatMessage)
        }

        override fun processError(s: String, e: ChatException, chatMessage: ConnectycubeChatMessage, integer: Int?) {

        }
    }

    inner class MarginItemDecoration(private val spaceHeight: Int) : StickyRecyclerHeadersDecoration(chatAdapter) {

        override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0 && chatAdapter.isHeaderView(1)) {
                    top = spaceHeight * 4
                } else if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceHeight
                }
            }
        }
    }

    inner class ChatMessagesSentListener : ChatDialogMessageSentListener {
        override fun processMessageSent(dialogId: String, message: ConnectycubeChatMessage) {
            Timber.d("processMessageSent $message")
            setResult(Activity.RESULT_OK)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent("chats"))
            modelChatMessageList.updateItemSentStatus(message.id, ConnectycubeChatService.getInstance().user.id)
            msgId = message.id
            //Toast.makeText(applicationContext, "Sent", Toast.LENGTH_LONG).show();

            sendNotification()

        }

        override fun processMessageFailed(dialogId: String, message: ConnectycubeChatMessage) {
            Log.d("MessageFail", "processMessageFailed $message")
            //Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show();
            msgDeleteId = message.id;
            //DeleteMessage().execute();
        }

    }

    inner class ChatMessagesStatusListener : MessageStatusListener {
        override fun processMessageRead(messageID: String, dialogId: String, userId: Int) {
            Timber.d("processMessageRead messageID= $messageID")
            msgId = messageID
            //Toast.makeText(applicationContext, "Read", Toast.LENGTH_LONG).show();

            modelChatMessageList.updateItemReadStatus(messageID, userId)
        }

        override fun processMessageDelivered(messageID: String, dialogId: String, userId: Int) {
            Timber.d("processMessageDelivered messageID= $messageID")
            modelChatMessageList.updateItemDeliveredStatus(messageID, userId)
            //Toast.makeText(applicationContext, "Delivered", Toast.LENGTH_LONG).show();

            msgId = messageID

        }

    }

    fun sendNotification() {
        val userIds: StringifyArrayList<Int> = StringifyArrayList()
        /* for ((key, value) in occupants) {
             if (key != MyPrefs(applicationContext, APP_PREF).getInt(CUBE_USER_ID))
                 userIds.add(key)
             Log.i("UserNotification", key.toString());

         }
 */
        for (i in 0 until chatDialog.occupants.size) {
            val key = chatDialog.occupants[i];
            if (key != MyPrefs(applicationContext, APP_PREF).getInt(CUBE_USER_ID))
                userIds.add(key)
            Log.i("UserNotification", key.toString());
        }

        val event = ConnectycubeEvent()
        event.userIds = userIds
        event.environment = ConnectycubeEnvironment.PRODUCTION
        event.notificationType = ConnectycubeNotificationType.PUSH
        val json = JSONObject()
        json.put("message", msgType)
        json.put("message_id", msgId)
        json.put("user_id", ConnectycubeChatService.getInstance().user.id)
        json.put("dialog_id", chatDialog.dialogId)
        if (chatDialog.isPrivate) {
            json.put("caller_name", "" + MyPrefs(applicationContext, APP_PREF).getString("firstName"))
        } else {
            json.put("caller_name", "" + chatDialog.name)
        }
        json.put("caller_phone", "" + MyPrefs(applicationContext, APP_PREF).getString("std_code") + MyPrefs(applicationContext, APP_PREF).getString("phone"))
        event.message = json.toString()


        /*ConnectycubePushNotifications.createEvent(event).performAsync(object : EntityCallback<ConnectycubeEvent> {
            override fun onSuccess(event: ConnectycubeEvent, args: Bundle) {
                Log.i("UserNotification", "success")
            }

            override fun onError(error: ResponseException) {
                Log.i("UserNotification", error.message)

            }
        });*/


        val map = HashMap<String, String>();
        map["users"] = APPHelper.getOtherUsers(applicationContext, chatDialog)
        map["message"] = msgType
        map["message_id"] = msgId
        map["user_id"] = "" + ConnectycubeChatService.getInstance().user.id
        map["dialog_id"] = chatDialog.dialogId
        if (chatDialog.isPrivate) {
            map["caller_name"] = "" + MyPrefs(applicationContext, APP_PREF).getString("firstName")
        } else {
            map["caller_name"] = "" + chatDialog.name
        }
        map["caller_phone"] = "" + MyPrefs(applicationContext, APP_PREF).getString("std_code") + MyPrefs(applicationContext, APP_PREF).getString("phone")

        asyncTask = PostRequestAsyncTask(applicationContext, map, "notification", apiCallbacks);
        asyncTask!!.execute(MAIN_URL + "send_chat_notification.php")
        msgType = "Message";

    }

    fun recordDialog(activity: Activity, data: String) {
        val voiceRecording = VoiceRecording(this@ChatMessageActivity, voiceRecordListener)

        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_record_audio, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(view)
        val checkBoxRecord: CheckBox = view.findViewById(R.id.checkBoxRecord)
        val buttonSend: Button = view.findViewById(R.id.buttonSend)
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)
        buttonSend.isEnabled = false

        chronometerAudio = view.findViewById(R.id.chronometerAudio)
        checkBoxRecord.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonSend.isEnabled = false
                voiceRecording.startRecording()
            } else {
                buttonSend.isEnabled = true

                voiceRecording.stopRecording()
            }
        }
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        /*builder.setPositiveButton("Send") { dialog, which ->
            if (audioFile.equals("")) {
                Toast.makeText(applicationContext, "No Audio", Toast.LENGTH_LONG).show()
            } else
                uploadAttachment(audioFile, ConnectycubeAttachment.AUDIO_TYPE, getString(R.string.message_attachment))

        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            audioFile = ""
        }*/

        buttonSend.setOnClickListener { v ->
            if (audioFile == "") {
                Toast.makeText(applicationContext, "No Audio", Toast.LENGTH_LONG).show()
            } else {
                msgType = "Audio"
                alertDialog.dismiss()
                uploadAttachment(audioFile, ConnectycubeAttachment.AUDIO_TYPE, "Voice")
            }
        }
        buttonCancel.setOnClickListener { v ->
            audioFile = ""
            if (checkBoxRecord.isChecked) {
                voiceRecording.stopRecording()

            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    fun showSendDialog(attachmentType: String) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_send_image, null)
        val builder = AlertDialog.Builder(this@ChatMessageActivity)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        val imageView: ImageView = view.findViewById(R.id.imageViewImage);
       // val image_RV: RecyclerView = view.findViewById(R.id.image_RV);
        val recyclerView = view.findViewById(R.id.image_RV) as RecyclerView
        if (attachmentType.equals(ConnectycubeAttachment.IMAGE_TYPE) || attachmentType.equals(ConnectycubeAttachment.VIDEO_TYPE)) {
            Glide.with(applicationContext).load(path).into(imageView)
          //  image_RV.setLayoutManager(new LinearLayoutManager(getActivity()));

            imageadapter = imageviewrecyclerAdapter(imagePathList,this@ChatMessageActivity)
            val layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.layoutManager = layoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = imageadapter
         //   val adapter = imageviewrecyclerAdapter(imagePathList,this@ChatMessageActivity)

            /* for (i in 0 until count - 1)
             {

             }*/

        }
        val editTextDesc: EditText = view.findViewById(R.id.editTextDesc);
        val buttonSend: Button = view.findViewById(R.id.buttonSend)
        buttonSend.setOnClickListener { v ->
            var message = editTextDesc.text.toString();
            if (message.equals("")) {
                message = attachmentType;
            }


            for (i in patharray)
            {
              //  Toast.makeText(applicationContext, i.toString(), Toast.LENGTH_LONG).show();
                uploadAttachment(i, attachmentType, message)
            }

            patharray.clear()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    /*fun showSendDialogVid() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_send_image, null)
        val builder = AlertDialog.Builder(this@ChatMessageActivity)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        val imageView: ImageView = view.findViewById(R.id.imageViewImage);
        Glide.with(applicationContext).load(path).into(imageView)
        val editTextDesc: EditText = view.findViewById(R.id.editTextDesc);
        val buttonSend: Button = view.findViewById(R.id.buttonSend)
        buttonSend.setOnClickListener { v ->
            val message = editTextDesc.text.toString();
            uploadAttachment(path, ConnectycubeAttachment.VIDEO_TYPE, message)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
*/

    fun removeReplyMode() {
        layoutIsReply.visibility = GONE
        dataReply = ""
    }

    fun setPros(objectOrgMsg: JSONObject, attachment: ConnectycubeAttachment, message: ConnectycubeChatMessage) {
        objectOrgMsg.put(ORIGINAL_MSG_URL, attachment.url);
        objectOrgMsg.put(ORIGINAL_MSG_TYPE, "" + attachment.type);
        objectOrgMsg.put(ORIGINAL_MSG_CONTENT_TYPE, "" + attachment.contentType);
        objectOrgMsg.put(ORIGINAL_MSG_DATA, "" + attachment.data);
        if (attachment.type.equals(ConnectycubeAttachment.IMAGE_TYPE) || attachment.type.equals(ConnectycubeAttachment.VIDEO_TYPE)) {

            imageViewOriginal?.let { Glide.with(applicationContext).load(attachment.url).into(it) }
        } else if (attachment.type.equals(ConnectycubeAttachment.AUDIO_TYPE)) {
            imageViewOriginal?.let { Glide.with(applicationContext).load(R.drawable.ic_mic_white_24dp).into(it) }

        } else if (attachment.type.toLowerCase().equals("location")) {
            imageViewOriginal?.let { Glide.with(applicationContext).load(R.mipmap.locationchat).into(it) }

        } else {
            imageViewOriginal?.let { Glide.with(applicationContext).load(R.drawable.ic_attachment_black_24dp).into(it) }
        }

        prepareReply(objectOrgMsg, message)
    }

    fun prepareReply(objectOrgMsg: JSONObject, chatMessage: ConnectycubeChatMessage) {
        dataReply = objectOrgMsg.toString();
        Log.i("PropertyMessageRe", dataReply)
        layoutIsReply.visibility = VISIBLE;
        textViewOriginalMessage!!.text = (chatMessage.body)
        textViewOriginalSender!!.text = (objectOrgMsg.getString(ORIGINAL_MSG_SENDER_NAME))
    }


    inner class DeleteMessage() : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val database = AppDatabase.getInstance(applicationContext)
            database.messageDao().deleteByMessageId(msgDeleteId)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent("chats"))

            messageDeleteListener.processMessageDeleted(msgDeleteId, chatDialog.dialogId);
        }
    }

    fun checkBlock() {
        connectycubePrivacyList.clear()

        val privacyListsManager = ConnectycubeChatService.getInstance().privacyListsManager

        if (privacyListsManager != null) {
            connectycubePrivacyList.addAll(privacyListsManager.privacyLists)
        }

        if (menuChat != null)
            menuChat!!.findItem(R.id.action_block).setTitle("Block")

        Log.i("PrivacyListCheck", Gson().toJson(connectycubePrivacyList))
        if (connectycubePrivacyList.size > 0) {
            val items = connectycubePrivacyList.get(0).items;
            Log.i("PrivacyItemsCheck", Gson().toJson(items))
            var item1: ConnectycubePrivacyListItem? = null;
            for (i in 0 until items.size) {
                val connectycubePrivacyListItem = items.get(i)
                if (connectycubePrivacyListItem.valueForType.contains(userID.toString())) {
                    item1 = connectycubePrivacyListItem;
                    if (menuChat != null)
                        menuChat!!.findItem(R.id.action_block).setTitle("Unblock")

                    switchBlock.isChecked = true;
                }
            }
        }
    }


    fun blockUser() {
        val privacyListsManager = ConnectycubeChatService.getInstance().privacyListsManager

        Log.i("PrivacyListBlock", Gson().toJson(connectycubePrivacyList))

        if (connectycubePrivacyList.size > 0) {
            val items = connectycubePrivacyList.get(0).items;

            Log.i("PrivacyItemsBlock1", Gson().toJson(items))

            val item1 = ConnectycubePrivacyListItem().apply {
                isAllow = false
                type = ConnectycubePrivacyListItem.Type.USER_ID
                valueForType = userID.toString()
                isMutualBlock = true
            }

            items.add(item1)
            privacyListsManager.declinePrivacyList()
            val list = ConnectycubePrivacyList().apply {
                name = MyPrefs(applicationContext, APP_PREF).getString("coasId")
            }

            Log.i("PrivacyItemsBlock2", Gson().toJson(items))

            list.items = items
            privacyListsManager.createPrivacyList(list)
            privacyListsManager.applyPrivacyList(list.name)

        } else {
            val list = ConnectycubePrivacyList().apply {
                name = MyPrefs(applicationContext, APP_PREF).getString("coasId")
            }

            val items = ArrayList<ConnectycubePrivacyListItem>()

            val item1 = ConnectycubePrivacyListItem().apply {
                isAllow = false
                type = ConnectycubePrivacyListItem.Type.USER_ID
                valueForType = userID.toString()
                isMutualBlock = true
            }

            items.add(item1)

            list.items = items


            privacyListsManager.createPrivacyList(list)
            privacyListsManager.applyPrivacyList(list.name)

        }

        checkBlock()
    }

    fun unblock() {
        val privacyListsManager = ConnectycubeChatService.getInstance().privacyListsManager

        Log.i("PrivacyListUnblock", Gson().toJson(connectycubePrivacyList))

        if (connectycubePrivacyList.size > 0) {
            val items = connectycubePrivacyList.get(0).items;
            Log.i("PrivacyItemUnblock1", Gson().toJson(items))

            var item1: ConnectycubePrivacyListItem? = null;
            for (i in 0 until items.size) {
                val connectycubePrivacyListItem = items.get(i)
                if (connectycubePrivacyListItem.valueForType.contains(userID.toString())) {
                    item1 = connectycubePrivacyListItem;
                }
            }

            items.remove(item1)

            Log.i("PrivacyItemUnblock2", Gson().toJson(items))


            connectycubePrivacyList.get(0).items = items

            privacyListsManager.applyPrivacyList(connectycubePrivacyList.get(0).name)

        }
        checkBlock()

    }

    fun getDialogs() {
        isInGroup = false;
        val requestBuilder = RequestGetBuilder()
        requestBuilder.limit = 100
        requestBuilder.skip = 0
        //requestBuilder.sortAsc(Consts.DIALOG_LAST_MESSAGE_DATE_SENT_FIELD_NAME);

        ConnectycubeRestChatService.getChatDialogs(null as ConnectycubeDialogType?, requestBuilder).performAsync(object : EntityCallback<java.util.ArrayList<ConnectycubeChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<ConnectycubeChatDialog>, params: Bundle) {
                chatDialogList.clear()

                Log.i("UserDialogs", Gson().toJson(dialogs));
                chatDialogList.addAll(dialogs)

                val currentId = chatDialog.dialogId;

                for (i in 0 until chatDialogList.size) {
                    Log.i("UserDialogIds", chatDialogList[i].dialogId + " " + currentId);

                    if (chatDialogList[i].dialogId == currentId) {
                        isInGroup = true
                        getDialogOccupants(chatDialogList[i])
                        chat_message_name.text = chatDialogList[i].name
                        break
                    }
                }

                if (!isInGroup) {
                    menuChat!!.findItem(R.id.menu_action_audio).setVisible(true)
                    menuChat!!.findItem(R.id.menu_action_video).setVisible(true)
                    menuChat!!.findItem(R.id.action_bargain).setVisible(false)

                    button_chat_attach.visibility = GONE
                    button_chat_send.visibility = GONE
                    input_chat_message.isEnabled = false
                    input_chat_message.setHint("You are not in group")
                } else {
                    /*menuChat!!.findItem(R.id.menu_action_audio).setVisible(false)
                    menuChat!!.findItem(R.id.menu_action_video).setVisible(false)
                    button_chat_attach.visibility = GONE
                    button_chat_send.visibility = GONE
                    input_chat_message.isEnabled = false
                    input_chat_message.setHint("Enter Message")*/
                }

                handler.postDelayed(runnableGroup, 2000)


                // new InsertChats().execute(chats);
            }

            override fun onError(exception: ResponseException) {

            }
        })
    }


    fun getDialogOccupants(chatDialog: ConnectycubeChatDialog) {
        val usersIds: MutableList<Int> = chatDialog.occupants

        val pagedRequestBuilder = PagedRequestBuilder().apply {
            page = 1
            perPage = usersIds.size
        }


        val params = Bundle()

        ConnectycubeUsers.getUsersByIDs(usersIds, pagedRequestBuilder, params).performAsync(object : EntityCallback<ArrayList<ConnectycubeUser>> {
            override fun onSuccess(users: ArrayList<ConnectycubeUser>, args: Bundle) {
                var names = ""
                val currentUserId = MyPrefs(applicationContext, APP_PREF).getInt(CUBE_USER_ID)

                for (i in 0 until users.size) {
                    val conUser = users[i];
                    if (conUser.id != currentUserId) {
                        names += APPHelper.getContactName(applicationContext, conUser.phone, "" + conUser.fullName) + ", ";
                        occupants.put(conUser.id, conUser);
                        receiverPhone = conUser.phone
                        coasLoginReceiver = conUser.login
                        userID = conUser.id
                        connectycubeUser = conUser;
                    }
                }
                updateChatAdapter()
                if (chatDialog.isPrivate) {
                    if (connectycubeUser!!.lastRequestAt != null) {
                        chat_message_members_typing.setText(sdfNativeDateTime.format(connectycubeUser!!.lastRequestAt))
                    }
                    val image = connectycubeUser!!.avatar;
                    if (image != null) {
                        var imagetoload = image;
                        if (image.startsWith("profile")) {
                            imagetoload = MAIN_URL_IMAGE + image;
                        }
                        loadChatDialogPhoto(applicationContext, chatDialog.isPrivate, imagetoload, avatar_img)
                    }
                    chat_message_name.text = APPHelper.getContactName(applicationContext, receiverPhone, connectycubeUser!!.fullName)

                    handler.post(runnableActive)


                    subscribeOnline()
                } else {
                    names += "You"
                    chat_message_members_typing.setText(names)
                }
            }

            override fun onError(error: ResponseException) {

            }
        })
    }

  /*  fun getImageFilePath(uri: Uri) {
        val file = File(uri.path)
        val filePath = file.path.split(":".toRegex()).toTypedArray()
        val image_id = filePath[filePath.size - 1]
        val cursor: Cursor? = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", arrayOf(image_id), null)
        if (cursor != null) {
            cursor.moveToFirst()
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            imagePathList.add(imagePath)
            cursor.close()
        }
    }*/

}