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
//import com.marigold.sdk.model.ContentItem
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
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.EngageBySailthru
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Date

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
                //val setWrapperMethod = Marigold::class.java.getDeclaredMethod("setWrapper", String::class.java, String::class.java)
                val cArg = arrayOf(String::class.java, String::class.java)
//                val setWrapperMethod = Marigold::class.java.getDeclaredMethod("setWrapper", *cArg)

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
    var engage = EngageBySailthru()

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
    fun startEngine(sdkKey: String) {
        reactApplicationContext.runOnUiQueueThread { marigold.startEngine(reactApplicationContext, sdkKey) }
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
            override fun onSuccess(value: String?) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun logEvent(value: String) {
        engage.logEvent(value)
    }

    @ReactMethod
    fun logEvent(eventName: String, varsMap: ReadableMap) {
        var varsJson: JSONObject? = null
        try {
            varsJson = jsonConverter.convertMapToJson(varsMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        engage.logEvent(eventName, varsJson)
    }

    @ReactMethod
    fun setAttributes(readableMap: ReadableMap, promise: Promise) {
        val attributeMap = try {
            getAttributeMap(readableMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_DEVICE, e.message)
            return
        }
        engage.setAttributes(attributeMap, object : EngageBySailthru.AttributesHandler {
            override fun onSuccess() {
                promise.resolve(null)
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
    fun setUserId(userId: String?, promise: Promise) {
        engage.setUserId(userId, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun setUserEmail(userEmail: String?, promise: Promise) {
        engage.setUserEmail(userEmail, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
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
        val message = try {
            getMessage(messageMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        }
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
    fun markMessageAsRead(messageMap: ReadableMap, promise: Promise) {
        val message = try {
            getMessage(messageMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
            return
        }
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

    /*
    TRACK SPM
     */
//    @ReactMethod
//    fun getRecommendations(sectionId: String?, promise: Promise) {
//        marigold.getRecommendations(sectionId, object : Marigold.RecommendationsHandler() {
//            override fun onSuccess(contentItems: ArrayList<ContentItem>) {
//                val array = getWritableArray()
//                try {
//                    for (contentItem in contentItems) {
//                        array.pushMap(jsonConverter.convertJsonToMap(contentItem.toJSON()))
//                    }
//                    promise.resolve(array)
//                } catch (e: Exception) {
//                    promise.reject(ERROR_CODE_RECOMMENDATIONS, e.message)
//                }
//            }
//
//            override fun onFailure(error: Error) {
//                promise.reject(ERROR_CODE_RECOMMENDATIONS, error.message)
//            }
//        })
//    }

    @ReactMethod
    fun trackClick(sectionId: String, url: String, promise: Promise) {
        try {
            val uri = URI(url)
            engage.trackClick(sectionId, uri, object : EngageBySailthru.TrackHandler {
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
            URI(url)
        } catch (e: URISyntaxException) {
            promise.reject(ERROR_CODE_TRACKING, e.message)
            return
        }
        var convertedTags: List<String?>? = null
        if (tags != null) {
            convertedTags = ArrayList()
            for (i in 0 until tags.size()) {
                convertedTags.add(tags.getString(i))
            }
        }
        engage.trackPageview(uri, convertedTags, object : EngageBySailthru.TrackHandler {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_TRACKING, error.message)
            }
        })
    }

    @ReactMethod
    fun trackImpression(sectionId: String, urls: ReadableArray?, promise: Promise) {
        var convertedUrls: List<URI>? = null
        if (urls != null) {
            try {
                convertedUrls = ArrayList()
                for (i in 0 until urls.size()) {
                    convertedUrls.add(URI(urls.getString(i)))
                }
            } catch (e: URISyntaxException) {
                promise.reject(ERROR_CODE_TRACKING, e.message)
                return
            }
        }
        engage.trackImpression(sectionId, convertedUrls, object : EngageBySailthru.TrackHandler {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_TRACKING, error.message)
            }
        })
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean) {
        marigold.setGeoIpTrackingEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(aVoid: Void?) {
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

    @ReactMethod
    fun setProfileVars(vars: ReadableMap, promise: Promise) {
        val varsJson = try {
            jsonConverter.convertMapToJson(vars)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_VARS, e.message)
            return
        }
        engage.setProfileVars(varsJson, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_VARS, error.message)
            }
        })
    }

    @ReactMethod
    fun getProfileVars(promise: Promise) {
        engage.getProfileVars(object : Marigold.MarigoldHandler<JSONObject?> {
            override fun onSuccess(value: JSONObject?) {
                try {
                    val vars = value?.let { jsonConverter.convertJsonToMap(it) }
                    promise.resolve(vars)
                } catch (e: JSONException) {
                    promise.reject(ERROR_CODE_VARS, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_VARS, error.message)
            }
        })
    }

    @ReactMethod
    fun logPurchase(purchaseMap: ReadableMap, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        }
        engage.logPurchase(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    @ReactMethod
    fun logAbandonedCart(purchaseMap: ReadableMap, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(ERROR_CODE_PURCHASE, e.message)
            return
        }
        engage.logAbandonedCart(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    @VisibleForTesting
    @kotlin.Throws(JSONException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    fun getPurchaseInstance(purchaseMap: ReadableMap): Purchase {
        val purchaseJson = jsonConverter.convertMapToJson(purchaseMap, false)
        val purchaseConstructor = Purchase::class.java.getDeclaredConstructor(JSONObject::class.java)
        purchaseConstructor.isAccessible = true
        return purchaseConstructor.newInstance(purchaseJson)
    }

    /*
     * Helper Methods
     */
    @kotlin.Throws(JSONException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    fun getMessage(messageMap: ReadableMap): Message {
        val messageJson = jsonConverter.convertMapToJson(messageMap)
        val constructor = Message::class.java.getDeclaredConstructor(String::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(messageJson.toString())
    }

    @VisibleForTesting
    @kotlin.Throws(JSONException::class)
    fun getAttributeMap(readableMap: ReadableMap): AttributeMap {
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
                    val array: ArrayList<String> = ArrayList()
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
                    val array: ArrayList<Int> = ArrayList()
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
                "float" -> attributeMap.putFloat(key, attribute.getDouble("value").toFloat())
                "floatArray" -> {
                    val array: ArrayList<Float> = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val value = (values.get(i).toString()).toFloat()
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
                    val array: ArrayList<Date> = ArrayList()
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
