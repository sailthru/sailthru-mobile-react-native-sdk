package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

class RNMessageStreamModule(reactContext: ReactApplicationContext, displayInAppNotifications: Boolean) : ReactContextBaseJavaModule(reactContext) {
    @VisibleForTesting
    internal var rnMessageStreamModuleImpl = RNMessageStreamModuleImpl(reactContext, displayInAppNotifications)

    override fun getName(): String {
        return RNMessageStreamModuleImpl.NAME
    }

    @ReactMethod
    fun notifyInAppHandled(shouldHandle: Boolean) {
        rnMessageStreamModuleImpl.notifyInAppHandled(shouldHandle)
    }

    @ReactMethod
    fun useDefaultInAppNotification(useDefault: Boolean) {
        rnMessageStreamModuleImpl.useDefaultInAppNotification(useDefault)
    }

    @ReactMethod
    fun getMessages(promise: Promise) {
        rnMessageStreamModuleImpl.getMessages(promise)
    }

    @ReactMethod
    fun getUnreadCount(promise: Promise) {
        rnMessageStreamModuleImpl.getUnreadCount(promise)
    }

    @ReactMethod
    fun removeMessage(messageMap: ReadableMap, promise: Promise) {
        rnMessageStreamModuleImpl.removeMessage(messageMap, promise)
    }

    @ReactMethod
    fun clearMessages(promise: Promise) {
        rnMessageStreamModuleImpl.clearMessages(promise)
    }

    @ReactMethod
    fun registerMessageImpression(typeCode: Double, messageMap: ReadableMap) {
        rnMessageStreamModuleImpl.registerMessageImpression(typeCode.toInt(), messageMap)
    }

    @ReactMethod
    fun markMessageAsRead(messageMap: ReadableMap, promise: Promise) {
        rnMessageStreamModuleImpl.markMessageAsRead(messageMap, promise)
    }

    @ReactMethod
    fun presentMessageDetail(message: ReadableMap) {
        rnMessageStreamModuleImpl.presentMessageDetail(message, currentActivity)
    }

    @ReactMethod
    @Suppress("unused")
    fun dismissMessageDetail() {
        rnMessageStreamModuleImpl.dismissMessageDetail()
    }
}
