package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class RNMessageStreamModule(private val reactContext: ReactApplicationContext, displayInAppNotifications: Boolean) : ReactContextBaseJavaModule(reactContext) {
    private val inAppNotificationEmitter = RNMessageStreamModuleImpl.InAppNotificationEmitter { writableMap ->
        val emitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        emitter?.emit("inappnotification", writableMap)
    }

    @VisibleForTesting
    internal var rnMessageStreamModuleImpl = RNMessageStreamModuleImpl(displayInAppNotifications, inAppNotificationEmitter)

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
        rnMessageStreamModuleImpl.presentMessageDetail(message, reactContext.currentActivity)
    }

    @ReactMethod
    @Suppress("unused")
    fun dismissMessageDetail() {
        rnMessageStreamModuleImpl.dismissMessageDetail()
    }
}
