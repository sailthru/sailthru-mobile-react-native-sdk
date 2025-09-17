package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import org.jetbrains.annotations.VisibleForTesting

class RNMessageStreamModule(private val reactContext: ReactApplicationContext, displayInAppNotifications: Boolean) : NativeRNMessageStreamSpec(reactContext) {
    private val inAppNotificationEmitter = RNMessageStreamModuleImpl.InAppNotificationEmitter { writableMap ->
        emitOnInAppNotification(writableMap)
    }

    @VisibleForTesting
    internal var rnMessageStreamModuleImpl = RNMessageStreamModuleImpl(displayInAppNotifications, inAppNotificationEmitter)

    override fun getName(): String {
        return RNMessageStreamModuleImpl.NAME
    }

    override fun notifyInAppHandled(handled: Boolean) {
        rnMessageStreamModuleImpl.notifyInAppHandled(handled)
    }

    override fun useDefaultInAppNotification(useDefault: Boolean) {
        rnMessageStreamModuleImpl.useDefaultInAppNotification(useDefault)
    }

    override fun getMessages(promise: Promise?) {
        rnMessageStreamModuleImpl.getMessages(promise)
    }

    override fun getUnreadCount(promise: Promise?) {
        rnMessageStreamModuleImpl.getUnreadCount(promise)
    }

    override fun markMessageAsRead(message: ReadableMap?, promise: Promise?) {
        rnMessageStreamModuleImpl.markMessageAsRead(message, promise)
    }

    override fun removeMessage(message: ReadableMap?, promise: Promise?) {
        rnMessageStreamModuleImpl.removeMessage(message, promise)
    }

    override fun presentMessageDetail(message: ReadableMap?) {
        rnMessageStreamModuleImpl.presentMessageDetail(message, reactContext.currentActivity)
    }

    override fun dismissMessageDetail() {
        rnMessageStreamModuleImpl.dismissMessageDetail()
    }

    override fun registerMessageImpression(impressionType: Double, message: ReadableMap?) {
        rnMessageStreamModuleImpl.registerMessageImpression(impressionType.toInt(), message)
    }

    override fun clearMessages(promise: Promise?) {
        rnMessageStreamModuleImpl.clearMessages(promise)
    }
}
