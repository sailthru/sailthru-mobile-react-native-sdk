package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeArray
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_MESSAGES
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.Message
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException

class RNMessageStreamModule(private val reactContext: ReactApplicationContext) : NativeRNMessageStreamSpec(reactContext), MessageStream.OnInAppNotificationDisplayListener {

    companion object {
        const val NAME = "RNMessageStream"
        const val MESSAGE_ID = "id"
    }

    @VisibleForTesting
    var messageStream = MessageStream()

    @VisibleForTesting
    internal var jsonConverter: JsonConverter = JsonConverter()

    private val eventChannel = Channel<Boolean>(1)
    private var defaultInAppNotification = true

    @VisibleForTesting
    internal var notificationTimeoutMs: Long = 2000L
    init {
        messageStream.setOnInAppNotificationDisplayListener(this)
    }

    override fun shouldPresentInAppNotification(message: Message): Boolean {
        if (defaultInAppNotification) {
            return true
        }

        return runBlocking {
            emitWithTimeout(message)
        }
    }

    private suspend fun emitWithTimeout(message: Message): Boolean {
        // Drain any stale signal from a previous or duplicate notifyInAppHandled call
        eventChannel.tryReceive()
        return withTimeoutOrNull(notificationTimeoutMs) {
            try {
                val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
                emitOnInAppNotification(writableMap)
                eventChannel.receive()
            } catch (e: JSONException) {
                e.printStackTrace()
                true
            }
        } != false
    }

    override fun getName(): String {
        return NAME
    }

    override fun notifyInAppHandled(handled: Boolean) {
        eventChannel.trySend(!handled)
    }

    override fun useDefaultInAppNotification(useDefault: Boolean) {
        defaultInAppNotification = useDefault
    }

    override fun getMessage(messageId: String, promise: Promise?) {
        messageStream.getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
            override fun onSuccess(value: Message) {
                try {
                    val toJsonMethod = Message::class.java.getDeclaredMethod("toJSON")
                    toJsonMethod.isAccessible = true

                    val messageMap =
                        (toJsonMethod.invoke(value) as? JSONObject)?.let { messageJson ->
                            jsonConverter.convertJsonToMap(messageJson)
                        }
                    promise?.resolve(messageMap)
                } catch (e: NoSuchMethodException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: IllegalAccessException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: JSONException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: InvocationTargetException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun getMessages(promise: Promise?) {
        promise ?: return
        messageStream.getMessages(object : MessageStream.MessagesHandler {
            override fun onSuccess(messages: ArrayList<Message>) {
                val array = getWritableArray()
                try {
                    val toJsonMethod = Message::class.java.getDeclaredMethod("toJSON")
                    toJsonMethod.isAccessible = true

                    for (message in messages) {
                        val messageJson = toJsonMethod.invoke(message) as? JSONObject ?: continue
                        array.pushMap(jsonConverter.convertJsonToMap(messageJson))
                    }
                    promise.resolve(array)
                } catch (e: NoSuchMethodException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: IllegalAccessException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: JSONException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: InvocationTargetException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun getUnreadCount(promise: Promise?) {
        promise ?: return
        messageStream.getUnreadMessageCount(object : MessageStream.MessageStreamHandler<Int> {
            override fun onSuccess(value: Int) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun markMessageAsRead(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = createMessage(messageMap, promise) ?: return
        messageStream.setMessageRead(message, object : MessageStream.MessagesReadHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun removeMessage(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = createMessage(messageMap, promise) ?: return
        messageStream.deleteMessage(message, object : MessageStream.MessageDeletedHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun presentMessageDetail(message: ReadableMap?) {
        message ?: return
        val activity = reactContext.currentActivity ?: return
        val messageId = message.getString(MESSAGE_ID)
        if (messageId.isNullOrEmpty()) return
        val i = getMessageActivityIntent(activity, messageId)
        activity.startActivity(i)
    }

    override fun dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    override fun registerMessageImpression(impressionType: Double, messageMap: ReadableMap?) {
        messageMap ?: return
        val type = when (impressionType.toInt()) {
            0 -> ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW
            1 -> ImpressionType.IMPRESSION_TYPE_STREAM_VIEW
            2 -> ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW
            else -> return
        }
        val message = createMessage(messageMap, null) ?: return
        messageStream.registerMessageImpression(type, message)
    }

    override fun clearMessages(promise: Promise?) {
        messageStream.clearMessages(object : MessageStream.MessageStreamHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    /*
     * Helper Methods
     */
    @VisibleForTesting
    fun createMessage(messageMap: ReadableMap, promise: Promise?): Message? = try {
        val messageJson = jsonConverter.convertMapToJson(messageMap)
        val constructor = Message::class.java.getDeclaredConstructor(String::class.java)
        constructor.isAccessible = true
        constructor.newInstance(messageJson.toString())
    } catch(e: Exception) {
        if (promise == null) {
            e.printStackTrace()
        } else {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
        }
        null
    }

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    fun getWritableArray(): WritableArray {
        return WritableNativeArray()
    }

    // wrapped for testing
    fun getMessageActivityIntent(activity: Activity, messageId: String): Intent {
        return MessageActivity.intentForMessage(activity, null, messageId)
    }
}
