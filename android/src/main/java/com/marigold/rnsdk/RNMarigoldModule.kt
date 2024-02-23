package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import android.location.Location
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.WritableNativeArray
import com.marigold.sdk.Marigold
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.Message
import com.marigold.sdk.MessageActivity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

/**
 * React native module for the Marigold SDK.
 */
class RNMarigoldModule(reactContext: ReactApplicationContext, private val displayInAppNotifications: Boolean) : ReactContextBaseJavaModule(reactContext), MessageStream.OnInAppNotificationDisplayListener {

    companion object {
        const val ERROR_CODE_DEVICE = "marigold.device"
        const val ERROR_CODE_MESSAGES = "marigold.messages"
        const val ERROR_CODE_RECOMMENDATIONS = "marigold.recommendations"
        const val ERROR_CODE_TRACKING = "marigold.tracking"
        const val ERROR_CODE_VARS = "marigold.vars"
        const val ERROR_CODE_PURCHASE = "marigold.purchase"
        const val MESSAGE_ID = "id"
        fun setWrapperInfo() {
            try {
                val cArg = arrayOf(String::class.java, String::class.java)
                val companionClass = Marigold.Companion::class.java
                val setWrapperMethod = companionClass.getDeclaredMethod("setWrapper", *cArg)

                setWrapperMethod.isAccessible = true
                setWrapperMethod.invoke(Marigold.Companion, "React Native", "10.0.0")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }

    @VisibleForTesting
    var marigold = Marigold()

    @VisibleForTesting
    var messageStream = MessageStream()

    @VisibleForTesting
    internal var jsonConverter: JsonConverter = JsonConverter()

    init {
        messageStream.setOnInAppNotificationDisplayListener(this)
        setWrapperInfo()
    }

    override fun shouldPresentInAppNotification(message: Message): Boolean {
        try {
            val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
            reactApplicationContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit("inappnotification", writableMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return displayInAppNotifications
    }

    override fun getName(): String {
        return "RNMarigold"
    }

    @ReactMethod
    fun registerForPushNotifications() {
        val activity = currentActivity() ?: return
        marigold.requestNotificationPermission(activity)
    }

    @ReactMethod
    fun syncNotificationSettings() {
        marigold.syncNotificationSettings()
    }

    @ReactMethod
    fun updateLocation(latitude: Double, longitude: Double) {
        val location = Location("React-Native")
        location.setLatitude(latitude)
        location.setLongitude(longitude)
        marigold.updateLocation(location)
    }

    @ReactMethod
    fun logRegistrationEvent(userId: String) {
        marigold.logRegistrationEvent(userId)
    }

    @ReactMethod
    fun getDeviceID(promise: Promise) {
        marigold.getDeviceId(object : Marigold.MarigoldHandler<String?> {
            override fun onSuccess(value: String?) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun getMessages(promise: Promise) {
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

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    fun getWritableArray(): WritableArray {
        return WritableNativeArray()
    }

    @ReactMethod
    fun getUnreadCount(promise: Promise) {
        messageStream.getUnreadMessageCount(object : MessageStream.MessageStreamHandler<Int> {
            override fun onSuccess(value: Int) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    @ReactMethod
    fun removeMessage(messageMap: ReadableMap, promise: Promise) {
        val message = getMessage(messageMap, promise) ?: return
        messageStream.deleteMessage(message, object : MessageStream.MessageDeletedHandler {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    @ReactMethod
    fun registerMessageImpression(typeCode: Int, messageMap: ReadableMap) {
        val type = when (typeCode) {
            0 -> ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW
            1 -> ImpressionType.IMPRESSION_TYPE_STREAM_VIEW
            2 -> ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW
            else -> return
        }
        val message = getMessage(messageMap, null) ?: return
        messageStream.registerMessageImpression(type, message)
    }

    @ReactMethod
    fun markMessageAsRead(messageMap: ReadableMap, promise: Promise) {
        val message = getMessage(messageMap, promise) ?: return
        messageStream.setMessageRead(message, object : MessageStream.MessagesReadHandler {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    @ReactMethod
    fun presentMessageDetail(message: ReadableMap) {
        val messageId = message.getString(MESSAGE_ID)
        val activity = currentActivity()
        if (messageId == null || activity == null) return
        val i = getMessageActivityIntent(activity, messageId)
        activity.startActivity(i)
    }

    // wrapped to expose for testing
    fun currentActivity(): Activity? {
        return currentActivity
    }

    // wrapped for testing
    fun getMessageActivityIntent(activity: Activity, messageId: String): Intent {
        return MessageActivity.intentForMessage(activity, null, messageId)
    }

    @ReactMethod
    @SuppressWarnings("unused")
    fun dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean) {
        marigold.setGeoIpTrackingEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    @SuppressWarnings("unused")
    fun setCrashHandlersEnabled(enabled: Boolean) {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    fun clearDevice(options: Int, promise: Promise) {
        marigold.clearDevice(options, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
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
            promise.reject(ERROR_CODE_MESSAGES, e.message)
        }
        null
    }
}
