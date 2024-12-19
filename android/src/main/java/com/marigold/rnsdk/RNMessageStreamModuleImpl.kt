package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.modules.core.DeviceEventManagerModule
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

class RNMessageStreamModuleImpl (private val reactContext: ReactContext, displayInAppNotifications: Boolean) : MessageStream.OnInAppNotificationDisplayListener  {

    companion object {
        const val NAME = "RNMessageStream"
    }

    @VisibleForTesting
    var messageStream = MessageStream()

    @VisibleForTesting
    internal var jsonConverter: JsonConverter = JsonConverter()

    private val eventChannel = Channel<Boolean>()
    private var defaultInAppNotification = displayInAppNotifications
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
        return withTimeoutOrNull(5000L) {
            try {
                val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
                val emitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                emitter?.emit("inappnotification", writableMap)
                eventChannel.receive()
            } catch (e: JSONException) {
                e.printStackTrace()
                true
            }
        } != false
    }

    fun notifyInAppHandled(shouldHandle: Boolean) {
        runBlocking {
            eventChannel.send(!shouldHandle)
        }
    }

    fun useDefaultInAppNotification(useDefault: Boolean) {
        defaultInAppNotification = useDefault
    }

    fun getMessages(promise: Promise?) {
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
                    promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, e.message)
                } catch (e: IllegalAccessException) {
                    promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, e.message)
                } catch (e: JSONException) {
                    promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, e.message)
                } catch (e: InvocationTargetException) {
                    promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    fun getUnreadCount(promise: Promise?) {
        promise ?: return
        messageStream.getUnreadMessageCount(object : MessageStream.MessageStreamHandler<Int> {
            override fun onSuccess(value: Int) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    fun removeMessage(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = getMessage(messageMap, promise) ?: return
        messageStream.deleteMessage(message, object : MessageStream.MessageDeletedHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    fun clearMessages(promise: Promise?) {
        messageStream.clearMessages(object : MessageStream.MessageStreamHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise?.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    fun registerMessageImpression(typeCode: Int, messageMap: ReadableMap?) {
        messageMap ?: return
        val type = when (typeCode) {
            0 -> ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW
            1 -> ImpressionType.IMPRESSION_TYPE_STREAM_VIEW
            2 -> ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW
            else -> return
        }
        val message = getMessage(messageMap, null) ?: return
        messageStream.registerMessageImpression(type, message)
    }

    fun markMessageAsRead(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = getMessage(messageMap, promise) ?: return
        messageStream.setMessageRead(message, object : MessageStream.MessagesReadHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    fun presentMessageDetail(message: ReadableMap?, activity: Activity?) {
        message ?: return
        activity ?: return
        val messageId = message.getString(RNMarigoldModuleImpl.MESSAGE_ID)
        if (messageId == null) return
        val i = getMessageActivityIntent(activity, messageId)
        activity.startActivity(i)
    }

    // wrapped for testing
    fun getMessageActivityIntent(activity: Activity, messageId: String): Intent {
        return MessageActivity.intentForMessage(activity, null, messageId)
    }

    @Suppress("unused")
    fun dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    /*
 * Helper Methods
 */
    @VisibleForTesting
    fun getMessage(messageMap: ReadableMap, promise: Promise?): Message? = try {
        val messageJson = jsonConverter.convertMapToJson(messageMap)
        val constructor = Message::class.java.getDeclaredConstructor(String::class.java)
        constructor.isAccessible = true
        constructor.newInstance(messageJson.toString())
    } catch(e: Exception) {
        if (promise == null) {
            e.printStackTrace()
        } else {
            promise.reject(RNMarigoldModuleImpl.ERROR_CODE_MESSAGES, e.message)
        }
        null
    }

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    fun getWritableArray(): WritableArray {
        return WritableNativeArray()
    }
}
