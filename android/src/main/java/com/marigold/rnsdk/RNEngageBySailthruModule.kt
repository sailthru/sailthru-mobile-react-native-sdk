package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.marigold.sdk.EngageBySailthru
import com.marigold.sdk.Marigold
import com.marigold.sdk.model.Purchase
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList

class RNEngageBySailthruModule (reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val ERROR_CODE_DEVICE = "marigold.device"
        const val ERROR_CODE_TRACKING = "marigold.tracking"
        const val ERROR_CODE_VARS = "marigold.vars"
        const val ERROR_CODE_PURCHASE = "marigold.purchase"
        const val ERROR_CODE_KEY = "marigold.key"
    }

    @VisibleForTesting
    internal var jsonConverter = JsonConverter()

    @ReactMethod
    fun logEvent(value: String) {
        createEngageBySailthru()?.logEvent(value)
    }

    @ReactMethod
    fun logEvent(eventName: String, varsMap: ReadableMap) {
        var varsJson: JSONObject? = null
        try {
            varsJson = jsonConverter.convertMapToJson(varsMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        createEngageBySailthru()?.logEvent(eventName, varsJson)
    }

    @ReactMethod
    fun clearEvents(promise: Promise) {
        createEngageBySailthru(promise)?.clearEvents(object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun setUserId(userId: String?, promise: Promise) {
        createEngageBySailthru(promise)?.setUserId(userId, object : Marigold.MarigoldHandler<Void?> {
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
        createEngageBySailthru(promise)?.setUserEmail(userEmail, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun trackClick(sectionId: String, url: String, promise: Promise) {
        try {
            val uri = URI(url)
            createEngageBySailthru(promise)?.trackClick(sectionId, uri, object : EngageBySailthru.TrackHandler {
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
        createEngageBySailthru(promise)?.trackPageview(uri, convertedTags, object : EngageBySailthru.TrackHandler {
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
        createEngageBySailthru(promise)?.trackImpression(sectionId, convertedUrls, object : EngageBySailthru.TrackHandler {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_TRACKING, error.message)
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
        createEngageBySailthru(promise)?.setProfileVars(varsJson, object : Marigold.MarigoldHandler<Void?> {
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
        createEngageBySailthru(promise)?.getProfileVars(object : Marigold.MarigoldHandler<JSONObject?> {
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
        val purchase = getPurchaseInstance(purchaseMap, promise) ?: return
        createEngageBySailthru(promise)?.logPurchase(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    @ReactMethod
    fun logAbandonedCart(purchaseMap: ReadableMap, promise: Promise) {
        val purchase = getPurchaseInstance(purchaseMap, promise) ?: return
        createEngageBySailthru(promise)?.logAbandonedCart(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    override fun getName(): String {
        return "RNEngageBySailthru"
    }

    //Helper method for instantiating EngageBySailthru
    fun createEngageBySailthru(promise: Promise? = null): EngageBySailthru? = try {
        EngageBySailthru()
    } catch (e: Exception) {
        promise?.reject(ERROR_CODE_KEY,  e.message)
        null
    }

    @VisibleForTesting
    fun getPurchaseInstance(purchaseMap: ReadableMap, promise: Promise): Purchase? = try {
        val purchaseJson = jsonConverter.convertMapToJson(purchaseMap, false)
        val purchaseConstructor = Purchase::class.java.getDeclaredConstructor(JSONObject::class.java)
        purchaseConstructor.isAccessible = true
        purchaseConstructor.newInstance(purchaseJson)
    } catch(e: Exception) {
        promise.reject(ERROR_CODE_PURCHASE, e.message)
        null
    }
}