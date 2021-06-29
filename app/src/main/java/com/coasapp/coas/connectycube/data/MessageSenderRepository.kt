package com.connectycube.messenger.data

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.coasapp.coas.utils.APPConstants.KEY_ORIGINAL_MSG
import com.coasapp.coas.utils.APPHelper
import com.connectycube.chat.model.ConnectycubeAttachment
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.messenger.api.*
import com.connectycube.messenger.utilities.convertToMessage
import com.connectycube.messenger.vo.AppExecutors
import com.connectycube.messenger.vo.Resource
import org.jivesoftware.smack.SmackException
import org.json.JSONObject
import timber.log.Timber


class MessageSenderRepository private constructor(private val messageDao: MessageDao,
                                                  private val appExecutors: AppExecutors
) {
    private val service: ConnectycubeService = ConnectycubeService()

    fun sendMessageAttachment(path: String,
                              data: String,
                              type: String,
                              text: String,
                              dialog: ConnectycubeChatDialog
    ): LiveData<Resource<ConnectycubeChatMessage>> {
        val messageToTempSave = createMessage(dialog, data, text)
        messageToTempSave.addAttachment(createAttachment(path, type))
        saveMediatorResult(messageToTempSave)

        val result = MediatorLiveData<Resource<ConnectycubeChatMessage>>()
        result.value = Resource.loading(null)

        val apiResponse = service.loadFileAsAttachment(path, type)
        result.addSource(apiResponse) { response ->
            when (response) {
                is ApiEmptyResponse -> {
                    result.value = Resource.success(null)
                }
                is ApiProgressResponse -> {
                    result.value = Resource.loadingProgress(messageToTempSave, response.progress)
                }
                is ApiErrorResponse -> {
                    result.value = Resource.error(response.errorMessage, null)
                    result.removeSource(apiResponse)
                }
                is ApiSuccessResponse -> {
                    val attachment = response.body
                    attachment.url = attachment.url;
                    attachment.contentType = APPHelper.getExtension(path)
                    val messageUpdated = buildMessage(messageToTempSave, attachment, dialog)
                    result.removeSource(apiResponse)

                    val apiSenderResponse = sendMessage(messageUpdated, dialog)
                    result.addSource(apiSenderResponse) {
                        result.removeSource(apiSenderResponse)
                        result.value = it
                    }

                }
            }
        }
        return result
    }


    fun sendMessageAttachmentContact(path: String,
                                     data: String,
                                     type: String,
                                     text: String,
                                     dialog: ConnectycubeChatDialog
    ): LiveData<Resource<ConnectycubeChatMessage>> {
        val messageToTempSave = createMessage(dialog, data, text)
        val attachment1 = ConnectycubeAttachment(type)
        attachment1.data = String(Base64.encode(path.toByteArray(), Base64.DEFAULT))

        messageToTempSave.addAttachment(attachment1)
        saveMediatorResult(messageToTempSave)

        val result = MediatorLiveData<Resource<ConnectycubeChatMessage>>()
        result.value = Resource.loading(null)
        val messageUpdated = buildMessage(messageToTempSave, attachment1, dialog)
        val apiSenderResponse = sendMessage(messageUpdated, dialog)
        result.addSource(apiSenderResponse) {
            result.removeSource(apiSenderResponse)
            result.value = it
        }


        /*val apiResponse = service.loadFileAsAttachment(path, type)
        result.addSource(apiResponse) { response ->
            when (response) {
                is ApiEmptyResponse -> {
                    result.value = Resource.success(null)
                }
                is ApiProgressResponse -> {
                    result.value = Resource.loadingProgress(messageToTempSave, response.progress)
                }
                is ApiErrorResponse -> {
                    result.value = Resource.error(response.errorMessage, null)
                    result.removeSource(apiResponse)
                }
                is ApiSuccessResponse -> {
                    val attachment = response.body

                    val messageUpdated = buildMessage(messageToTempSave, attachment, dialog)
                    result.removeSource(apiResponse)

                    val apiSenderResponse = sendMessage(messageUpdated, dialog)
                    result.addSource(apiSenderResponse) {
                        result.removeSource(apiSenderResponse)
                        result.value = it
                    }

                }
            }
        }*/
        return result
    }

    fun sendMessageText(text: String, data: String,
                        dialog: ConnectycubeChatDialog
    ): LiveData<Resource<ConnectycubeChatMessage>> {
        val messageToTempSave = createMessage(dialog, data, text)
        saveMediatorResult(messageToTempSave)

        val result = MediatorLiveData<Resource<ConnectycubeChatMessage>>()
        val messageUpdated = buildMessage(messageToTempSave, dialog = dialog)
        val apiSenderResponse = sendMessage(messageUpdated, dialog)
        result.addSource(apiSenderResponse) {
            result.removeSource(apiSenderResponse)
            result.value = it
        }
        return result
    }

    fun sendMessageAttachmentForward(message: ConnectycubeChatMessage,
                                     data: String,
                                     dialog: ConnectycubeChatDialog
    ): LiveData<Resource<ConnectycubeChatMessage>> {
        val messageToTempSave = createMessage(dialog, data, message.body)
        messageToTempSave.addAttachment(message.attachments.first())
        saveMediatorResult(messageToTempSave)

        val result = MediatorLiveData<Resource<ConnectycubeChatMessage>>()
        result.value = Resource.loading(null)

        // val messageUpdated = buildMessage(message, message.attachments.first(), dialog = dialog)
        //val apiSenderResponse = sendMessage(messageUpdated, dialog)

        val messageUpdated = buildMessage(messageToTempSave, message.attachments.first(), dialog)
        //result.removeSource(apiResponse)

        val apiSenderResponse = sendMessage(messageUpdated, dialog)
        result.addSource(apiSenderResponse) {
            result.removeSource(apiSenderResponse)
            result.value = it

        }


        return result
    }

    private fun saveMediatorResult(chatMessage: ConnectycubeChatMessage) {
        appExecutors.diskIO().execute { messageDao.insert(convertToMessage(chatMessage)) }
    }

    private fun sendMessage(chatMessage: ConnectycubeChatMessage,
                            dialog: ConnectycubeChatDialog
    ): LiveData<Resource<ConnectycubeChatMessage>> {
        val result = MediatorLiveData<Resource<ConnectycubeChatMessage>>()
        appExecutors.networkIO().execute {
            try {
                dialog.sendMessage(chatMessage)
                result.postValue(Resource.success(chatMessage))
                //APPHelper.writeToFile(""+chatMessage,"Message"+System.currentTimeMillis());

            } catch (e: SmackException.NotConnectedException) {
                Log.i("SendErrorS", "Error " + e.message);

                result.postValue(
                        Resource.error(
                                e.message ?: "SmackException.NotConnectedException",
                                chatMessage
                        )

                )
                APPHelper.writeToFile(""+e.message,"Message"+System.currentTimeMillis());

                Timber.d(e)
            } catch (e: InterruptedException) {
                Log.i("SendErrorI", "Error " + e.message);
                APPHelper.writeToFile(""+e.message,"Message"+System.currentTimeMillis());

                result.postValue(Resource.error(e.message ?: "InterruptedException", chatMessage))
                Timber.d(e)
            } catch (e: Exception) {
                Log.i("SendError", "Error " + e.message);
                result.postValue(Resource.error(e.message ?: "InterruptedException", chatMessage))
                Timber.d(e)
                APPHelper.writeToFile(""+e.message,"Message"+System.currentTimeMillis());
            }
        }
        return result
    }

    private fun createAttachment(path: String, type: String): ConnectycubeAttachment {
        val attachment = ConnectycubeAttachment(type)
        attachment.contentType = APPHelper.getExtension(path)

        attachment.url = path
        Log.i("AttachmentData", "" + APPHelper.getMimeType(path));
        Log.i("AttachmentData", "" + attachment.data);
        Log.i("AttachmentData", "" + attachment.contentType);


        return attachment
    }

    private fun createTextMessage(dialog: ConnectycubeChatDialog): ConnectycubeChatMessage {
        val chatMessage = ConnectycubeChatMessage()
        chatMessage.dateSent = System.currentTimeMillis() / 1000
        chatMessage.dialogId = dialog.dialogId
        return chatMessage
    }

    private fun createMessage(dialog: ConnectycubeChatDialog, data: String,
                              text: String
    ): ConnectycubeChatMessage {
        val chatMessage = ConnectycubeChatMessage()
        chatMessage.dateSent = System.currentTimeMillis() / 1000
        chatMessage.dialogId = dialog.dialogId
        chatMessage.body = text
        chatMessage.setProperty(KEY_ORIGINAL_MSG, data);
        return chatMessage
    }

    private fun buildMessage(messageToTempSave: ConnectycubeChatMessage,
                             attachment: ConnectycubeAttachment? = null,
                             dialog: ConnectycubeChatDialog
    ): ConnectycubeChatMessage {
        val chatMessage = ConnectycubeChatMessage()
        chatMessage.id = messageToTempSave.id
        chatMessage.setSaveToHistory(true)
        val map = HashMap<String, String>()
        chatMessage.setProperty(KEY_ORIGINAL_MSG, "" + messageToTempSave.getProperty(KEY_ORIGINAL_MSG))
        chatMessage.dateSent = System.currentTimeMillis() / 1000
        chatMessage.isMarkable = true
        if (dialog.isPrivate) chatMessage.recipientId = dialog.recipientId
        if (attachment != null) {
            chatMessage.addAttachment(attachment)
            Log.i("UploadUrl", "" + attachment.url)

            chatMessage.body = messageToTempSave.body
        } else {
            chatMessage.body = messageToTempSave.body
        }
        Log.i("PropertyMessage", messageToTempSave.getProperty(KEY_ORIGINAL_MSG).toString())
        return chatMessage
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MessageSenderRepository? = null

        fun getInstance(messageDao: MessageDao) =
                instance ?: synchronized(this) {
                    instance ?: MessageSenderRepository(messageDao, AppExecutors()).also {
                        instance = it
                    }
                }
    }
}