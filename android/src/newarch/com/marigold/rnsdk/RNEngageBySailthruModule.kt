package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

class RNEngageBySailthruModule(reactContext: ReactApplicationContext) : NativeRNEngageBySailthruSpec(reactContext) {
    @VisibleForTesting
    internal var rnEngageBySailthruModuleImpl = RNEngageBySailthruModuleImpl()

    override fun getName(): String {
        return RNEngageBySailthruModuleImpl.NAME
    }

    override fun setAttributes(attributeMap: ReadableMap?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.setAttributes(attributeMap, promise)
    }

    override fun removeAttribute(key: String?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.removeAttribute(key, promise)
    }

    override fun clearAttributes(promise: Promise?) {
        rnEngageBySailthruModuleImpl.clearAttributes(promise)
    }

    override fun logEvent(name: String?, vars: ReadableMap?) {
        rnEngageBySailthruModuleImpl.logEvent(name, vars)
    }

    override fun setUserId(userId: String?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.setUserId(userId, promise)
    }

    override fun setUserEmail(userEmail: String?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.setUserEmail(userEmail, promise)
    }

    override fun trackClick(sectionId: String?, url: String?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.trackClick(sectionId, url, promise)
    }

    override fun trackPageview(url: String?, tags: ReadableArray?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.trackPageview(url, tags, promise)
    }

    override fun trackImpression(sectionId: String?, urls: ReadableArray?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.trackImpression(sectionId, urls, promise)
    }

    override fun setProfileVars(vars: ReadableMap?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.setProfileVars(vars, promise)
    }

    override fun getProfileVars(promise: Promise?) {
        rnEngageBySailthruModuleImpl.getProfileVars(promise)
    }

    override fun logPurchase(purchase: ReadableMap?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.logPurchase(purchase, promise)
    }

    override fun logAbandonedCart(purchase: ReadableMap?, promise: Promise?) {
        rnEngageBySailthruModuleImpl.logAbandonedCart(purchase, promise)
    }

    override fun clearEvents(promise: Promise?) {
        rnEngageBySailthruModuleImpl.clearEvents(promise)
    }
}
