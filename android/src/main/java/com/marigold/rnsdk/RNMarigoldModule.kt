package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import android.location.Location
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.WritableNativeArray
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.Marigold
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.ContentItem
import com.marigold.sdk.model.Message
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.model.Purchase
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Date
import java.util.Iterator
import java.util.List

/**
 * React native module for the Marigold SDK.
 */
class RNMarigoldModule(reactApplicationContext: ReactApplicationContext?, private val displayInAppNotifications: Boolean) : ReactContextBaseJavaModule(reactContext), MessageStream.OnInAppNotificationDisplayListener {

    companion object {
        protected const val ERROR_CODE_DEVICE = "marigold.device"
        protected const val ERROR_CODE_MESSAGES = "marigold.messages"
        protected const val ERROR_CODE_RECOMMENDATIONS = "marigold.recommendations"
        protected const val ERROR_CODE_TRACKING = "marigold.tracking"
        protected const val ERROR_CODE_VARS = "marigold.vars"
        protected const val ERROR_CODE_PURCHASE = "marigold.purchase"
        protected const val MESSAGE_ID = "id"
        protected fun setWrapperInfo() {
            try {
                val cArg = arrayOf(String::class.java, String::class.java)
                val setWrapperMethod = Marigold.Companion.getClass().getDeclaredMethod("setWrapper", cArg)
                setWrapperMethod.setAccessible(true)
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
    var jsonConverter = JsonConverter()

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
    fun startEngine(sdkKey: String?) {
        reactApplicationContext.runOnUiQueueThread {
            marigold.startEngine(reactApplicationContext, sdkKey)
        }
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
    fun getDeviceID(promise: Promise) {
        marigold.getDeviceId(object : Marigold.MarigoldHandler<String?> {
            override fun onSuccess(s: String) {
                promise.resolve(s)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun logEvent(value: String?) {
        marigold.logEvent(value)
    }

    @ReactMethod
    fun logEvent(eventName: String?, varsMap: ReadableMap?) {
        var varsJson = null
        try {
            varsJson = jsonConverter.convertMapToJson(varsMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        marigold.logEvent(eventName, varsJson)
    }

    @ReactMethod
    fun setAttributes(readableMap: ReadableMap?, promise: Promise) {
        val attributeMap = try {
            getAttributeMap(readableMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_DEVICE, e.getMessage())
            return
        }
        marigold.setAttributes(attributeMap, object : Marigold.AttributesHandler() {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage())
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
                        val messageJson = toJsonMethod.invoke(message) as? JSONObject
                        if (messageJson == null) continue
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
    fun setUserId(userId: String?, promise: Promise) {
        marigold.setUserId(userId, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(@NotNull error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun setUserEmail(userEmail: String?, promise: Promise) {
        marigold.setUserEmail(userEmail, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(@NotNull error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun getUnreadCount(promise: Promise) {
        messageStream.getUnreadMessageCount(object : MessageStream.MessageStreamHandler<Integer?>() {
            override fun onSuccess(integer: Integer?) {
                promise.resolve(integer)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun removeMessage(messageMap: ReadableMap?, promise: Promise) {
        val message = try {
            getMessage(messageMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        }
        messageStream.deleteMessage(message, object : MessageStream.MessageDeletedHandler() {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun registerMessageImpression(typeCode: Int, messageMap: ReadableMap?) {
        val type = when (typeCode) {
            0 -> ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW
            1 -> ImpressionType.IMPRESSION_TYPE_STREAM_VIEW
            2 -> ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW
            else -> return
        }
        val message = try {
            getMessage(messageMap)
        } catch (e: JSONException) {
            e.printStackTrace()
            return
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            return
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            return
        } catch (e: InstantiationException) {
            e.printStackTrace()
            return
        }
        messageStream.registerMessageImpression(type, message)
    }

    @ReactMethod
    fun markMessageAsRead(messageMap: ReadableMap?, promise: Promise) {
        val message = try {
            getMessage(messageMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage())
            return
        }
        messageStream.setMessageRead(message, object : MessageStream.MessagesReadHandler() {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage())
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
    protected fun currentActivity(): Activity {
        return getCurrentActivity()
    }

    // wrapped for testing
    protected fun getMessageActivityIntent(activity: Activity, messageId: String): Intent {
        return MessageActivity.intentForMessage(activity, null, messageId)
    }

    @ReactMethod
    @SuppressWarnings("unused")
    fun dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    /*
    TRACK SPM
     */
    @ReactMethod
    fun getRecommendations(sectionId: String?, promise: Promise) {
        marigold.getRecommendations(sectionId, object : Marigold.RecommendationsHandler() {
            override fun onSuccess(contentItems: ArrayList<ContentItem>) {
                val array = getWritableArray()
                try {
                    for (contentItem in contentItems) {
                        array.pushMap(jsonConverter.convertJsonToMap(contentItem.toJSON()))
                    }
                    promise.resolve(array)
                } catch (e: Exception) {
                    promise.reject(ERROR_CODE_RECOMMENDATIONS, e.getMessage())
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_RECOMMENDATIONS, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun trackClick(sectionId: String, url: String, promise: Promise) {
        try {
            val uri = URI(url)
            marigold.trackClick(sectionId, uri, object : Marigold.TrackHandler {
                override fun onSuccess() {
                    promise.resolve(true)
                }

                override fun onFailure(error: Error) {
                    promise.reject(ERROR_CODE_TRACKING, error.message)
                }
            })
        } catch (e: URISyntaxException) {
            promise.reject(ERROR_CODE_TRACKING, e.message)
        }
    }

    @ReactMethod
    fun trackPageview(url: String?, tags: ReadableArray?, promise: Promise) {
        val uri = try {
            uri = URI(url)
        } catch (e: URISyntaxException) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage())
            return
        }
        var convertedTags: List<String?>? = null
        if (tags != null) {
            convertedTags = ArrayList()
            for (i in 0 until tags.size()) {
                convertedTags.add(tags.getString(i))
            }
        }
        marigold.trackPageview(uri, convertedTags, object : TrackHandler() {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_TRACKING, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun trackImpression(sectionId: String?, urls: ReadableArray?, promise: Promise) {
        var convertedUrls: List<URI?>? = null
        if (urls != null) {
            try {
                convertedUrls = ArrayList()
                for (i in 0 until urls.size()) {
                    convertedUrls.add(URI(urls.getString(i)))
                }
            } catch (e: URISyntaxException) {
                promise.reject(ERROR_CODE_TRACKING, e.getMessage())
                return
            }
        }
        marigold.trackImpression(sectionId, convertedUrls, object : TrackHandler() {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_TRACKING, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean) {
        marigold.setGeoIpTrackingEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage())
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
        marigold.clearDevice(options, object : MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun setProfileVars(vars: ReadableMap?, promise: Promise) {
        val varsJson = try {
            jsonConverter.convertMapToJson(vars)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_VARS, e.getMessage())
            return
        }
        marigold.setProfileVars(varsJson, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun getProfileVars(promise: Promise) {
        marigold.getProfileVars(object : Marigold.MarigoldHandler<JSONObject?>() {
            override fun onSuccess(jsonObject: JSONObject?) {
                try {
                    val vars = jsonConverter.convertJsonToMap(jsonObject)
                    promise.resolve(vars)
                } catch (e: JSONException) {
                    promise.reject(ERROR_CODE_VARS, e.getMessage())
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun logPurchase(purchaseMap: ReadableMap?, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        }
        marigold.logPurchase(purchase, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.getMessage())
            }
        })
    }

    @ReactMethod
    fun logAbandonedCart(purchaseMap: ReadableMap?, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage())
            return
        }
        marigold.logAbandonedCart(purchase, object : Marigold.MarigoldHandler<Void?>() {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.getMessage())
            }
        })
    }

    @VisibleForTesting
    @NotNull
    @kotlin.Throws(JSONException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    fun getPurchaseInstance(purchaseMap: ReadableMap?): Purchase {
        val purchaseJson = jsonConverter.convertMapToJson(purchaseMap, false)
        val purchaseConstructor = Purchase::class.java.getDeclaredConstructor(JSONObject::class.java)
        purchaseConstructor.setAccessible(true)
        return purchaseConstructor.newInstance(purchaseJson)
    }

    /*
     * Helper Methods
     */
    @NotNull
    @kotlin.Throws(JSONException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    protected fun getMessage(messageMap: ReadableMap?): Message {
        val messageJson = jsonConverter.convertMapToJson(messageMap)
        val constructor = Message::class.java.getDeclaredConstructor(String::class.java)
        constructor.setAccessible(true)
        return constructor.newInstance(messageJson.toString())
    }

    @VisibleForTesting
    @NotNull
    @kotlin.Throws(JSONException::class)
    fun getAttributeMap(readableMap: ReadableMap?): AttributeMap {
        val attributeMapJson = jsonConverter.convertMapToJson(readableMap)
        val attributes = attributeMapJson.getJSONObject("attributes")
        val attributeMap = AttributeMap()
        attributeMap.setMergeRules(attributeMapJson.getInt("mergeRule"))
        val keys = attributes.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val attribute = attributes.getJSONObject(key)
            val attributeType = attribute.getString("type")
            when (attributeType) {
                "string" -> attributeMap.putString(key, attribute.getString("value"))
                "stringArray" -> {
                    val array = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        array.add(values.get(i) as String)
                        i++
                    }
                    attributeMap.putStringArray(key, array)
                }

                "integer" -> attributeMap.putInt(key, attribute.getInt("value"))
                "integerArray" -> {
                    val array = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val j = values.getInt(i)
                        array.add(j)
                        i++
                    }
                    attributeMap.putIntArray(key, array)
                }

                "boolean" -> attributeMap.putBoolean(key, attribute.getBoolean("value"))
                "float" -> attributeMap.putFloat(key, attribute.getDouble("value") as Float)
                "floatArray" -> {
                    val array = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val value = Float.parseFloat(values.get(i).toString())
                        array.add(value)
                        i++
                    }
                    attributeMap.putFloatArray(key, array)
                }

                "date" -> {
                    val value = Date(attribute.getLong("value"))
                    attributeMap.putDate(key, value)
                }

                "dateArray" -> {
                    val array = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val dateValue = values.getLong(i)
                        val date = Date(dateValue)
                        array.add(date)
                        i++
                    }
                    attributeMap.putDateArray(key, array)
                }
            }
        }
        return attributeMap
    }
}
