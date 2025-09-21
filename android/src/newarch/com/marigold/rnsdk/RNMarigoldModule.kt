package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext

class RNMarigoldModule(reactContext: ReactApplicationContext) : NativeRNMarigoldSpec(reactContext) {
    private val rnMarigoldModuleImpl = RNMarigoldModuleImpl()

    override fun getName(): String {
        return RNMarigoldModuleImpl.NAME
    }

    override fun updateLocation(lat: Double, lon: Double) {
        rnMarigoldModuleImpl.updateLocation(lat, lon)
    }

    override fun getDeviceID(promise: Promise?) {
        rnMarigoldModuleImpl.getDeviceID(promise)
    }

    override fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise?) {
        rnMarigoldModuleImpl.setGeoIPTrackingEnabled(enabled, promise)
    }

    override fun setCrashHandlersEnabled(enabled: Boolean) {
        rnMarigoldModuleImpl.setCrashHandlersEnabled(enabled)
    }

    override fun logRegistrationEvent(userId: String?) {
        rnMarigoldModuleImpl.logRegistrationEvent(userId)
    }

    override fun registerForPushNotifications() {
        rnMarigoldModuleImpl.registerForPushNotifications(currentActivity)
    }

    override fun syncNotificationSettings() {
        rnMarigoldModuleImpl.syncNotificationSettings()
    }

    override fun setInAppNotificationsEnabled(enabled: Boolean) {
        rnMarigoldModuleImpl.setInAppNotificationsEnabled(enabled)
    }
}
