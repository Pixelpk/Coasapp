package com.connectycube.messenger.adapters

import android.content.Context
import android.os.AsyncTask
import android.text.format.DateUtils
import android.text.util.Linkify
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coasapp.coas.R
import com.coasapp.coas.utils.APPConstants.*
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.ChatMessageLongClick
import com.coasapp.coas.utils.LocationHelper
import com.connectycube.auth.session.ConnectycubeSessionManager
import com.connectycube.chat.model.ConnectycubeAttachment
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.chat.model.ConnectycubeDialogType
import com.connectycube.core.helper.CollectionsUtil
import com.connectycube.messenger.data.AppDatabase
import com.connectycube.messenger.data.Message
import com.connectycube.messenger.paging.NetworkState
import com.connectycube.messenger.utilities.*
import com.connectycube.users.model.ConnectycubeUser
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import org.json.JSONObject
import timber.log.Timber


typealias AttachmentClickListener = (ConnectycubeAttachment) -> Unit
private typealias PAYLOAD_PROGRESS = ChatMessageAdapter.ProgressMessage

class ChatMessageAdapter(
        val context: Context,
        var chatDialog: ConnectycubeChatDialog,
        private val attachmentClickListener: AttachmentClickListener
) : PagedListAdapter<ConnectycubeChatMessage, RecyclerView.ViewHolder>(diffCallback),
        StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {


    val IN_PROGRESS = -1
    val TEXT_OUTCOMING = 1
    val TEXT_INCOMING = 2
    val ATTACH_IMAGE_OUTCOMING = 3
    val ATTACH_IMAGE_INCOMING = 4

    val ATTACH_CONTACT_OUTCOMING = 5
    val ATTACH_CONTACT_INCOMING = 6
    val ATTACH_VIDEO_OUTCOMING = 7
    val ATTACH_VIDEO_INCOMING = 8

    val ATTACH_AUDIO_OUTCOMING = 9
    val ATTACH_AUDIO_INCOMING = 10
    val ATTACH_DOC_OUTCOMING = 11
    val ATTACH_DOC_INCOMING = 12
    val ATTACH_LOC_OUTCOMING = 13
    val ATTACH_LOC_INCOMING = 14
    val localUserId = ConnectycubeSessionManager.getInstance().sessionParameters.userId
    val occupantsIds: ArrayList<Int> =
            ArrayList<Int>(chatDialog.occupants).apply { remove(localUserId) }
    val occupants: MutableMap<Int, ConnectycubeUser> = mutableMapOf()
    private var networkState: NetworkState? = null

    public var chatMessageLongClick: ChatMessageLongClick? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.d("onCreateViewHolder viewType= $viewType")
        return when (viewType) {
            TEXT_OUTCOMING -> ChatMessageOutcomingViewHolder(parent, R.layout.chat_outcoming_item)
            TEXT_INCOMING -> ChatMessageIncomingViewHolder(parent, R.layout.chat_incoming_item)
            ATTACH_IMAGE_OUTCOMING -> ChatImageAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_outcoming_attachimage_item
            )
            ATTACH_IMAGE_INCOMING -> ChatImageAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_incoming_attachimage_item
            )
            ATTACH_CONTACT_OUTCOMING -> ChatContactAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_contact_outgoing_item
            )
            ATTACH_CONTACT_INCOMING -> ChatContactAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_contact_incoming_item
            )
            ATTACH_VIDEO_OUTCOMING -> ChatVideoAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_video_outgoing
            )
            ATTACH_VIDEO_INCOMING -> ChatVideoAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_video_incoming
            )
            ATTACH_AUDIO_OUTCOMING -> ChatVoiceAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_voice_outgoing
            )
            ATTACH_AUDIO_INCOMING -> ChatVoiceAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_voice_incoming
            )
            ATTACH_DOC_OUTCOMING -> ChatDocAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_doc_outgoing
            )
            ATTACH_DOC_INCOMING -> ChatDocAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_doc_incoming
            )
            ATTACH_LOC_OUTCOMING -> ChatLocAttachOutcomingViewHolder(
                    parent,
                    R.layout.chat_location_outcoming
            )
            ATTACH_LOC_INCOMING -> ChatLocAttachIncomingViewHolder(
                    parent,
                    R.layout.chat_location_incoming
            )
            IN_PROGRESS -> NetworkStateItemViewHolder.create(parent)
            else -> throw IllegalArgumentException("Wrong type of viewType= $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int,
                                  payloads: MutableList<Any>
    ) {
        Timber.d("Binding view holder at position $position, payloads= ${payloads.isNotEmpty()},  payloads= $payloads")
        if (payloads.isNotEmpty()) {
            when (val payload = payloads[0]) {
                PAYLOAD_STATUS -> {
                    Timber.d("PAYLOAD_STATUS")
                    val message = getItem(position)
                    message?.let {
                        val imgStatus =
                                holder.itemView.findViewById<ImageView>(R.id.message_status_image_view)
                        setStatus(imgStatus, message)
                    }
                }
                is PAYLOAD_PROGRESS -> {
                    Timber.d("PROGRESS payloads= ${payload.progress}")
                    val progressBar =
                            holder.itemView.findViewById<ProgressBar>(R.id.progressbar)
                    setProgress(progressBar, payload.progress)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }


    fun getItemByPosition(position: Int): ConnectycubeChatMessage? {
        return getItem(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Timber.d("Binding view holder at position $position")
        val chatMessage = getItem(position)
        chatMessage?.let {
            if (isIncoming(chatMessage) && !isRead(chatMessage)) {
                markAsReadMessage(chatMessage)
            }
        }

        when (this.getItemViewType(position)) {
            TEXT_OUTCOMING -> onBindTextViewHolderOutComing(
                    holder as ChatMessageOutcomingViewHolder,
                    position
            )
            TEXT_INCOMING -> onBindTextViewHolderInComing(
                    holder as ChatMessageIncomingViewHolder,
                    position
            )
            ATTACH_IMAGE_OUTCOMING -> onBindAttachViewHolderOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_IMAGE_INCOMING -> onBindAttachViewHolderInComing(
                    holder as ChatImageAttachIncomingViewHolder,
                    position
            )
            ATTACH_CONTACT_OUTCOMING -> onBindAttachViewHolderContactOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_CONTACT_INCOMING -> onBindAttachViewHolderContactInComing(
                    holder as ChatContactAttachIncomingViewHolder,
                    position
            )

            ATTACH_VIDEO_OUTCOMING -> onBindAttachViewHolderVideoOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_VIDEO_INCOMING -> onBindAttachViewHolderVideoInComing(
                    holder as ChatVideoAttachIncomingViewHolder,
                    position
            )

            ATTACH_AUDIO_OUTCOMING -> onBindAttachViewHolderVoiceOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_AUDIO_INCOMING -> onBindAttachViewHolderVoiceInComing(
                    holder as ChatVoiceAttachIncomingViewHolder,
                    position
            )
            ATTACH_DOC_OUTCOMING -> onBindAttachViewHolderDocOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_DOC_INCOMING -> onBindAttachViewHolderDocInComing(
                    holder as ChatDocAttachIncomingViewHolder,
                    position
            )
            ATTACH_LOC_OUTCOMING -> onBindAttachViewHolderLocOutComing(
                    holder as BaseChatMessageViewHolder,
                    position
            )
            ATTACH_LOC_INCOMING -> onBindAttachViewHolderLocInComing(
                    holder as ChatLocAttachIncomingViewHolder,
                    position
            )
            IN_PROGRESS -> (holder as NetworkStateItemViewHolder).bindTo(
                    networkState
            )
        }
    }

    fun onBindTextViewHolderOutComing(holder: ChatMessageOutcomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                /* itemView.setOnClickListener {
                     attachmentClickListener(message.attachments.first())
                 }*/
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindTextViewHolderInComing(holder: ChatMessageIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                /* itemView.setOnClickListener {
                     attachmentClickListener(message.attachments.first())
                 }*/
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindAttachViewHolderOutComing(holder: BaseChatMessageViewHolder,
                                        position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        attachmentClickListener(message.attachments.first())
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderInComing(holder: ChatImageAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    attachmentClickListener(message.attachments.first())
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }


    fun onBindAttachViewHolderDocOutComing(holder: BaseChatMessageViewHolder,
                                           position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        attachmentClickListener(message.attachments.first())
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderDocInComing(holder: ChatDocAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    attachmentClickListener(message.attachments.first())
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindAttachViewHolderLocOutComing(holder: BaseChatMessageViewHolder,
                                           position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        //attachmentClickListener(message.attachments.first())
                        val objLoc = getLocObj(message.attachments.iterator().next().data)
                        LocationHelper.openDirections(context, objLoc.getString("location_lat_lng"))
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderLocInComing(holder: ChatLocAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    itemView.setOnClickListener {
                        //attachmentClickListener(message.attachments.first())

                        val objLoc = getLocObj(message.attachments.iterator().next().data)
                        LocationHelper.openDirections(context, objLoc.getString("location_lat_lng"))
                    }
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindAttachViewHolderContactOutComing(holder: BaseChatMessageViewHolder,
                                               position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        attachmentClickListener(message.attachments.first())
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderContactInComing(holder: ChatContactAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    attachmentClickListener(message.attachments.first())
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindAttachViewHolderVideoOutComing(holder: BaseChatMessageViewHolder,
                                             position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        attachmentClickListener(message.attachments.first())
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderVoiceOutComing(holder: BaseChatMessageViewHolder,
                                             position: Int
    ) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it)
                message.let {
                    itemView.setOnClickListener {
                        attachmentClickListener(message.attachments.first())
                    }
                    itemView.setOnLongClickListener {
                        chatMessageLongClick!!.onLongClick(itemView, message)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    fun onBindAttachViewHolderVideoInComing(holder: ChatVideoAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    attachmentClickListener(message.attachments.first())
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun onBindAttachViewHolderVoiceInComing(holder: ChatVoiceAttachIncomingViewHolder, position: Int) {
        val message = getItem(position)
        message?.let {
            with(holder) {
                bindTo(it, showAvatar(position, message), showName(position, message))
                itemView.setOnClickListener {
                    attachmentClickListener(message.attachments.first())
                }
                itemView.setOnLongClickListener {
                    chatMessageLongClick!!.onLongClick(itemView, message)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    private fun showAvatar(position: Int, currentMsg: ConnectycubeChatMessage): Boolean {
        return isNeedShowExtraData(position, currentMsg)
    }

    private fun showName(position: Int, currentMsg: ConnectycubeChatMessage): Boolean {
        return !chatDialog.isPrivate && isNeedShowExtraData(position, currentMsg)
    }

    private fun isNeedShowExtraData(position: Int, currentMsg: ConnectycubeChatMessage): Boolean {
        fun isPreviousTheSameSender(position: Int,
                                    currentMsg: ConnectycubeChatMessage
        ): Boolean {
            val previousPosition = position + 1
            if (previousPosition >= itemCount) {
                return false
            }
            val previousMsg = getItem(previousPosition)
            previousMsg?.let {
                return currentMsg.senderId == previousMsg.senderId
            }
            return false
        }

        fun isPreviousHeader(position: Int): Boolean {
            val previousPosition = position + 1
            if (previousPosition >= itemCount) {
                return true
            }
            return isHeaderView(previousPosition)
        }

        return !isPreviousTheSameSender(position, currentMsg) || isPreviousHeader(position)
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = this.getItem(position)
        Log.i("ChatMessage" + position, Gson().toJson(chatMessage))

        chatMessage?.let {
            val isReceived = isIncoming(chatMessage)
            return if (withAttachment(chatMessage)) {
                var attachment: ConnectycubeAttachment? = null
                /*for (connectycubeAttachment: ConnectycubeAttachment in chatMessage.attachments) {
                    attachment = connectycubeAttachment
                }*/
                attachment = chatMessage.attachments.first()


                if (isReceived) {

                    if (attachment!!.type.contains("contact")) {
                        ATTACH_CONTACT_INCOMING
                    } else if (attachment.type.contains("image")) {
                        ATTACH_IMAGE_INCOMING

                    } else if (attachment.type.contains("audio")) {
                        ATTACH_AUDIO_INCOMING

                    } else if (attachment.type.contains("video")) {
                        ATTACH_VIDEO_INCOMING
                    } else if (attachment.type.contains("location")) {
                        ATTACH_LOC_INCOMING
                    } else {
                        ATTACH_DOC_INCOMING
                    }
                } else {

                    if (attachment!!.type.contains("contact")) {
                        ATTACH_CONTACT_OUTCOMING
                    } else if (attachment.type.contains("image")) {
                        ATTACH_IMAGE_OUTCOMING
                    } else if (attachment.type.contains("audio")) {
                        ATTACH_AUDIO_OUTCOMING
                    } else if (attachment.type.contains("video")) {
                        ATTACH_VIDEO_OUTCOMING
                    } else if (attachment.type.contains("location")) {
                        ATTACH_LOC_OUTCOMING
                    } else {
                        ATTACH_DOC_OUTCOMING
                    }
                }
            } else if (isReceived) {
                TEXT_INCOMING
            } else {
                TEXT_OUTCOMING
            }
        }
        return IN_PROGRESS
    }

    fun isHeaderView(position: Int): Boolean {
        if (position >= itemCount) {
            return false
        }
        val msgCurrent = getItem(position)
        val msgNext = getItem(position - 1)
        if (msgCurrent != null && msgNext != null) {
            val dateMsgCurrent: Long? = getDateAsHeaderId(msgCurrent.dateSent * 1000)
            val dateMsgNext: Long? = getDateAsHeaderId(msgNext.dateSent * 1000)
            return dateMsgCurrent != dateMsgNext
        }
        return false
    }

    override fun getHeaderId(position: Int): Long {
        val chatMessage = getItem(position)
        var date = 0L
        chatMessage?.let {
            date = getDateAsHeaderId(chatMessage.dateSent * 1000)
        }
        return date
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(
                R.layout.chat_message_header,
                parent,
                false
        )
        return object : RecyclerView.ViewHolder(view) {
        }
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView
        val dateView = view.findViewById<TextView>(R.id.header_text_view)

        val chatMessage = getItem(position)
        chatMessage?.let {
            dateView.text = getPrettyMessageDate(context, chatMessage.dateSent * 1000)
        }
    }

    fun setStatus(imgStatus: ImageView?, msg: ConnectycubeChatMessage) {
        when {
            messageIsRead(msg) -> imgStatus?.setImageResource(R.drawable.ic_check_double_color_16)
            messageIsDelivered(msg) -> imgStatus?.setImageResource(R.drawable.ic_check_double_16)
            messageIsSent(msg) -> imgStatus?.setImageResource(R.drawable.ic_check_black_16dp)
            else -> imgStatus?.setImageResource(android.R.color.transparent)
        }
    }

    private fun setProgress(progressBar: ProgressBar?, value: Int) {
        progressBar?.apply {
            if (value < 100) {
                visibility = View.VISIBLE
                progress = value
            } else {
                visibility = View.GONE
            }
        }
    }

    fun updateAttachmentProgress(position: Int, progress: Int) {
        notifyItemChanged(position, PAYLOAD_PROGRESS(progress))
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    fun setOccupants(newOccupants: Map<Int, ConnectycubeUser>) {
        occupants.clear()
        occupants.putAll(newOccupants)
        occupantsIds.clear()
        occupantsIds.addAll(newOccupants.keys.toList())
    }

    fun isIncoming(chatMessage: ConnectycubeChatMessage): Boolean {
        return chatMessage.senderId != null && chatMessage.senderId != localUserId
    }

    fun withAttachment(chatMessage: ConnectycubeChatMessage): Boolean {
        val attachments = chatMessage.attachments
        return attachments != null && !attachments.isEmpty()
    }

    fun formatDate(seconds: Long): String {
        return DateUtils.formatDateTime(context, seconds * 1000L, DateUtils.FORMAT_SHOW_TIME)
    }

    private fun markAsReadMessage(chatMessage: ConnectycubeChatMessage) {
        try {
            chatDialog.readMessage(chatMessage)
        } catch (ex: Exception) {
            Timber.d(ex)
        }
    }

    private fun isRead(chatMessage: ConnectycubeChatMessage): Boolean {
        return !CollectionsUtil.isEmpty(chatMessage.readIds) && chatMessage.readIds.contains(
                localUserId
        )
    }

    companion object {
        /**
         * This diff callback informs the PagedListAdapter how to compute list differences when new
         * PagedLists arrive.
         */
        private val PAYLOAD_STATUS = Any()
        private val diffCallback = object : DiffUtil.ItemCallback<ConnectycubeChatMessage>() {
            override fun areItemsTheSame(oldItem: ConnectycubeChatMessage,
                                         newItem: ConnectycubeChatMessage
            ): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: ConnectycubeChatMessage,
                    newItem: ConnectycubeChatMessage
            ): Boolean =
                    oldItem.id == newItem.id && oldItem.readIds == newItem.readIds && oldItem.deliveredIds == newItem.deliveredIds

            override fun getChangePayload(oldItem: ConnectycubeChatMessage,
                                          newItem: ConnectycubeChatMessage
            ): Any? {
                return if (sameExceptStatus(oldItem, newItem)) {
                    PAYLOAD_STATUS
                } else null
            }

            fun sameExceptStatus(oldItem: ConnectycubeChatMessage,
                                 newItem: ConnectycubeChatMessage
            ): Boolean {
                return newItem.readIds != oldItem.readIds || newItem.deliveredIds != oldItem.deliveredIds
            }
        }
    }

    private fun messageIsSent(message: ConnectycubeChatMessage): Boolean {
        return message.deliveredIds?.contains(localUserId) ?: false
    }

    private fun messageIsRead(message: ConnectycubeChatMessage): Boolean {
        if (chatDialog.isPrivate) return message.readIds != null &&
                (message.recipientId == null || message.readIds.contains(message.recipientId))
        return message.readIds != null && message.readIds.any { it in occupantsIds }
    }

    private fun messageIsDelivered(message: ConnectycubeChatMessage): Boolean {
        if (chatDialog.isPrivate) return message.deliveredIds?.contains(message.recipientId)
                ?: false
        return message.deliveredIds != null && message.deliveredIds.any { it in occupantsIds }
    }

    fun getAttachImageUrl(attachment: ConnectycubeAttachment): String {
        return attachment.url
    }

    open inner class BaseChatMessageViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(
            itemView
    ) {
        val dateView = itemView.findViewById<TextView>(R.id.text_message_date)
        /**
         * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
         * ViewHolder when Item is loaded.
         */
        open fun bindTo(message: ConnectycubeChatMessage) {
            dateView.text = formatDate(message.dateSent)
        }
    }

    open inner class BaseChatMessageTextViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val bodyView = itemView.findViewById<TextView>(R.id.text_message_body)
        private var message: ConnectycubeChatMessage? = null


        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            findViewsReply(itemView, message)

            this.message = message
            bodyView.text = message.body
            Linkify.addLinks(bodyView, Linkify.ALL);
        }
    }

    inner class ChatMessageIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageTextViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE

                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatMessageOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageTextViewHolder(parent, chatItem) {
        val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)
        }
    }

    open inner class BaseChatImageAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val attachmentView: ImageView = itemView.findViewById(R.id.attachment_image_view)
        val textViewDesc: TextView = itemView.findViewById(R.id.textViewDesc);

        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            showImageAttachment(message)
            textViewDesc.setText(message.body)
            Linkify.addLinks(textViewDesc, Linkify.ALL);
            findViewsReply(itemView, message)

        }

        private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            loadAttachImage(validUrl, attachmentView, context)
        }
    }

    inner class ChatImageAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatImageAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatImageAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatImageAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)
        }
    }

    open inner class BaseChatDocAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val attachmentView: ImageView = itemView.findViewById(R.id.attachment_image_view)
        val textViewDesc: TextView = itemView.findViewById(R.id.textViewDesc);
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            //showImageAttachment(message)
            textViewDesc.setText(message.body)
            Linkify.addLinks(textViewDesc, Linkify.ALL);
            findViewsReply(itemView, message)


        }

        private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            loadAttachImage(validUrl, attachmentView, context)
        }
    }

    inner class ChatDocAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatDocAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatDocAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatDocAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)
        }
    }

    open inner class BaseChatVoiceAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        //private val attachmentView: ImageView = itemView.findViewById(R.id.attachment_image_view)

        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            findViewsReply(itemView, message)

            //showImageAttachment(message)
        }

        private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            //loadAttachImage(validUrl, attachmentView, context)
        }
    }

    inner class ChatVoiceAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatVoiceAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatVoiceAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatVoiceAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)

        }
    }

    open inner class BaseChatVideoAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val attachmentView: ImageView = itemView.findViewById(R.id.attachment_image_view)
        private val playView: ImageView = itemView.findViewById(R.id.imageViewPlay)
        val textViewDesc: TextView = itemView.findViewById(R.id.textViewDesc);

        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            showImageAttachment(message)
            textViewDesc.setText(message.body)
            Linkify.addLinks(textViewDesc, Linkify.ALL);
            findViewsReply(itemView, message)

        }

        private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            Log.i("Video", validUrl)
            loadAttachImage(validUrl, attachmentView, context)
        }
    }

    inner class ChatVideoAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatVideoAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatVideoAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatVideoAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)
        }
    }

    open inner class BaseChatContactAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val bodyView = itemView.findViewById<TextView>(R.id.text_message_body)
        private var message: ConnectycubeChatMessage? = null

        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            this.message = message
            val attachment: ConnectycubeAttachment = message.attachments.iterator().next()

            var data = attachment.data;
            if (!data.contains("\"name\"") || !data.contains("\"phone\"")) {
                data = String(Base64.decode(attachment.data, Base64.DEFAULT));
            }
            val obj = JSONObject(data);
            val phone = obj.getString("phone");
            val name = obj.getString("name")
            bodyView.text = name + " " + phone
            findViewsReply(itemView, message)

        }

        /* override fun bindTo(message: ConnectycubeChatMessage) {
             super.bindTo(message)
             showImageAttachment(message)
         }*/

        /*private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            loadAttachImage(validUrl, attachmentView, context)
        }*/
    }

    inner class ChatContactAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatContactAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatContactAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatContactAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)
        }
    }

    open inner class BaseChatLocAttachViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(chatItem, parent, false)
            ) {
        private val bodyView = itemView.findViewById<TextView>(R.id.textViewDesc)
        private var message: ConnectycubeChatMessage? = null
        private val attachmentView: ImageView = itemView.findViewById(R.id.attachment_image_view)

        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            this.message = message
            val attachment: ConnectycubeAttachment = message.attachments.iterator().next()

            var data = attachment.data;
            if (!data.contains("\"location\"")) {
                data = String(Base64.decode(attachment.data, Base64.DEFAULT));
            }
            val obj = JSONObject(data);
            val phone = obj.getString("location_lat_lng");
            val name = obj.getString("location_address")
            if (obj.has("location_image"))
                Glide.with(context).load(obj.getString("location_image")).into(attachmentView)
            bodyView.text = name;
            findViewsReply(itemView, message)

        }

        /* override fun bindTo(message: ConnectycubeChatMessage) {
             super.bindTo(message)
             showImageAttachment(message)
         }*/

        /*private fun showImageAttachment(message: ConnectycubeChatMessage) {
            val validUrl = getAttachImageUrl(message.attachments.iterator().next())
            loadAttachImage(validUrl, attachmentView, context)
        }*/
    }

    inner class ChatLocAttachIncomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatLocAttachViewHolder(parent, chatItem) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)

        fun bindTo(message: ConnectycubeChatMessage, showAvatar: Boolean, showName: Boolean) {
            super.bindTo(message)
            if (showAvatar || showName) {
                val sender = occupants[message.senderId]
                if (showAvatar) {
                    imgAvatar.visibility = View.VISIBLE
                    loadChatMessagePhoto(
                            chatDialog.type == ConnectycubeDialogType.PRIVATE,
                            sender?.avatar,
                            imgAvatar,
                            context
                    )
                } else {
                    imgAvatar.visibility = View.INVISIBLE
                }
                if (showName) {
                    senderName.visibility = View.VISIBLE
                    sender?.let {
                        senderName.text = sender.fullName ?: sender.login
                    }
                } else {
                    senderName.visibility = View.GONE
                }
            } else {
                imgAvatar.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }
        }
    }

    inner class ChatLocAttachOutcomingViewHolder(parent: ViewGroup, @LayoutRes chatItem: Int) :
            BaseChatLocAttachViewHolder(parent, chatItem) {
        private val imgStatus: ImageView = itemView.findViewById(R.id.message_status_image_view)
        override fun bindTo(message: ConnectycubeChatMessage) {
            super.bindTo(message)
            setStatus(imgStatus, message)

        }

    }


    fun getLocObj(data: String): JSONObject {
        var data1 = data
        if (!data1.contains("\"location\"")) {
            data1 = String(Base64.decode(data, Base64.NO_WRAP));
        }
        val obj = JSONObject(data1);
        return obj
    }

    fun getPhoneObj(data: String): JSONObject {
        var data1 = data
        if (!data1.contains("\"name\"") || !data1.contains("\"phone\"")) {
            data1 = String(Base64.decode(data, Base64.NO_WRAP));
        }
        val obj = JSONObject(data1);
        return obj
    }


    public fun findViewsReply(itemView: View, message: ConnectycubeChatMessage) {
        val obj = message.getProperty(KEY_ORIGINAL_MSG);
        Log.i("ChatMessagePro", "Original " + obj)
        val objOrg = "" + obj
        val layoutReply = itemView.findViewById<LinearLayout>(R.id.layoutReply);
        val textViewOriginalSender = itemView.findViewById<TextView>(R.id.textViewOriginalSender)
        val textViewOriginalMessage = itemView.findViewById<TextView>(R.id.textViewOriginalMessage)
        val imageViewOriginal = itemView.findViewById<ImageView>(R.id.imageViewOriginal)


        /*if (message.getProperty(KEY_ORIGINAL_MSG) != null) {
            layoutReply.visibility = VISIBLE*/
        if (objOrg != "null" && objOrg != "")
        /*if (message.getProperty(KEY_ORIGINAL_MSG) != "null" && message.getProperty(KEY_ORIGINAL_MSG)!="")*/ {


            layoutReply.visibility = VISIBLE

            //val objectOriginal = JSONObject(message.getProperty(KEY_ORIGINAL_MSG).toString());
            val objectOriginal = JSONObject(objOrg)

            class GetMessage() : AsyncTask<String, Void, Message>() {
                override fun doInBackground(vararg params: String?): Message {
                    val database = AppDatabase.getInstance(context)
                    Log.i("ChatMessageReply", "" + params[0])
                    val msg = database.messageDao().loadItem(params[0]!!);
                    Log.i("ChatMessageReply", "" + Gson().toJson(msg))
                    return msg;
                }

                override fun onPostExecute(result: Message?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        layoutReply.visibility = VISIBLE
                        val chatMessage = result.cubeMessage;
                        textViewOriginalMessage.text = chatMessage.body
                        textViewOriginalSender.text = objectOriginal.getString(ORIGINAL_MSG_SENDER_NAME)
                        if (chatMessage.attachments != null && !chatMessage.attachments.isEmpty()) {

                            imageViewOriginal.visibility = VISIBLE
                            if (objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.IMAGE_TYPE) || objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.VIDEO_TYPE)) {
                                Glide.with(context).load(chatMessage.attachments.first().url/*.replace("\\", "")*/).into(imageViewOriginal)
                                Log.i("ChatMessageProImage", chatMessage.attachments.first().url)

                                layoutReply.setOnClickListener { v ->

                                    attachmentClickListener(chatMessage.attachments.first());
                                }

                            } else if (objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.AUDIO_TYPE)) {
                                Glide.with(context).load(R.drawable.ic_mic_white_24dp).into(imageViewOriginal)

                                layoutReply.setOnClickListener { v ->

                                    attachmentClickListener(chatMessage.attachments.first());
                                }

                            } else if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("location")) {
                                val objLoc = getLocObj(objectOriginal.getString(ORIGINAL_MSG_DATA))
                                layoutReply.setOnClickListener { v ->

                                    LocationHelper.openDirections(context, objLoc.getString("location_lat_lng"))
                                }
                                Glide.with(context).load(R.mipmap.locationchat).into(imageViewOriginal)
                            } else {
                                Glide.with(context).load(R.drawable.ic_attachment_black_24dp).into(imageViewOriginal)
                                layoutReply.setOnClickListener { v ->

                                    attachmentClickListener(chatMessage.attachments.first());
                                }
                            }

                            if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("contact")) {
                                val objPhone = getPhoneObj(objectOriginal.getString(ORIGINAL_MSG_DATA));
                                val phone = objPhone.getString("phone");
                                val name = objPhone.getString("name")
                                textViewOriginalMessage.text = name + " " + phone

                                layoutReply.setOnClickListener { v ->
                                    APPHelper.openDialer(context, phone)

                                }
                            }
                            if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("location")) {
                                val objLoc = getLocObj(objectOriginal.getString(ORIGINAL_MSG_DATA))
                                textViewOriginalMessage.text = objLoc.getString("location_address")
                                Glide.with(context).load(objLoc.getString("location_image")).into(imageViewOriginal)

                            }
                        } else {
                            imageViewOriginal.visibility = GONE

                        }
                    } else {
                        layoutReply.visibility = GONE

                    }
                }

            }
            GetMessage().execute(objectOriginal.getString(ORIGINAL_MSG_ID))
            /*textViewOriginalMessage.text = objectOriginal.getString(ORIGINAL_MSG_BODY)
            textViewOriginalSender.text = objectOriginal.getString(ORIGINAL_MSG_SENDER_NAME)
            if (objectOriginal.has(ORIGINAL_MSG_TYPE)) {

                imageViewOriginal.visibility = VISIBLE


                if (objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.IMAGE_TYPE) || objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.VIDEO_TYPE)) {
                    Glide.with(context).load(objectOriginal.getString(ORIGINAL_MSG_URL)*//*.replace("\\", "")*//*).into(imageViewOriginal)
                    Log.i("ChatMessageProImage", objectOriginal.getString(ORIGINAL_MSG_URL))

                } else if (objectOriginal.getString(ORIGINAL_MSG_TYPE).equals(ConnectycubeAttachment.AUDIO_TYPE)) {
                    Glide.with(context).load(R.drawable.ic_mic_white_24dp).into(imageViewOriginal)

                } else if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("location")) {
                    Glide.with(context).load(R.mipmap.locationchat).into(imageViewOriginal)

                } else {
                    Glide.with(context).load(R.drawable.ic_attachment_black_24dp).into(imageViewOriginal)
                }

                if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("contact")) {
                    val objPhone = getPhoneObj(objectOriginal.getString(ORIGINAL_MSG_DATA));
                    val phone = objPhone.getString("phone");
                    val name = objPhone.getString("name")
                    textViewOriginalMessage.text = name + " " + phone
                }
                if (objectOriginal.getString(ORIGINAL_MSG_TYPE).toLowerCase().equals("location")) {
                    val objLoc = getLocObj(objectOriginal.getString(ORIGINAL_MSG_DATA))
                    textViewOriginalMessage.text = objLoc.getString("location_address")
                }
            } else {
                imageViewOriginal.visibility = GONE
            }*/
        } else {
            layoutReply.visibility = GONE
        }
        /*} else {
            layoutReply.visibility = GONE
        }*/
    }


    data class ProgressMessage(val progress: Int = 0)
}