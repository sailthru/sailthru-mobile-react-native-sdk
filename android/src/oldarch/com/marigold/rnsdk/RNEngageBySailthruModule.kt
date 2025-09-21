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
import com.marigold.sdk.enums.MergeRules
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.model.Purchase
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Date

class RNEngageBySailthruModule (reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    @VisibleForTesting
    internal var rnEngageBySailthruModuleImpl = RNEngageBySailthruModuleImpl()

    override fun getName(): String {
        return RNEngageBySailthruModuleImpl.NAME
    }

    @ReactMethod
    fun logEvent(eventName: String, varsMap: ReadableMap?) {
        rnEngageBySailthruModuleImpl.logEvent(eventName, varsMap)
    }

    @ReactMethod
    fun clearEvents(promise: Promise) {
        rnEngageBySailthruModuleImpl.clearEvents(promise)
    }

    @ReactMethod
    fun setAttributes(readableMap: ReadableMap, promise: Promise) {
        rnEngageBySailthruModuleImpl.setAttributes(readableMap, promise)
    }

    @ReactMethod
    fun removeAttribute(key: String, promise: Promise) {
        rnEngageBySailthruModuleImpl.removeAttribute(key, promise)
    }

    @ReactMethod
    fun clearAttributes(promise: Promise) {
        rnEngageBySailthruModuleImpl.clearAttributes(promise)
    }

    @ReactMethod
    fun setUserId(userId: String?, promise: Promise) {
        rnEngageBySailthruModuleImpl.setUserId(userId, promise)
    }

    @ReactMethod
    fun setUserEmail(userEmail: String?, promise: Promise) {
        rnEngageBySailthruModuleImpl.setUserEmail(userEmail, promise)
    }

    @ReactMethod
    fun trackClick(sectionId: String, url: String, promise: Promise) {
        rnEngageBySailthruModuleImpl.trackClick(sectionId, url, promise)
    }

    @ReactMethod
    fun trackPageview(url: String?, tags: ReadableArray?, promise: Promise) {
        rnEngageBySailthruModuleImpl.trackPageview(url, tags, promise)
    }

    @ReactMethod
    fun trackImpression(sectionId: String, urls: ReadableArray?, promise: Promise) {
        rnEngageBySailthruModuleImpl.trackImpression(sectionId, urls, promise)
    }

    @ReactMethod
    fun setProfileVars(vars: ReadableMap, promise: Promise) {
        rnEngageBySailthruModuleImpl.setProfileVars(vars, promise)
    }

    @ReactMethod
    fun getProfileVars(promise: Promise) {
        rnEngageBySailthruModuleImpl.getProfileVars(promise)
    }

    @ReactMethod
    fun logPurchase(purchaseMap: ReadableMap, promise: Promise) {
        rnEngageBySailthruModuleImpl.logPurchase(purchaseMap, promise)
    }

    @ReactMethod
    fun logAbandonedCart(purchaseMap: ReadableMap, promise: Promise) {
        rnEngageBySailthruModuleImpl.logAbandonedCart(purchaseMap, promise)
    }
}
