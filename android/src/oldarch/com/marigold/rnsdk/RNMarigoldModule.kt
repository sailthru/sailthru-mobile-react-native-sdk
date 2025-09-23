package com.marigold.rnsdk

import android.app.Activity
import android.location.Location
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.marigold.sdk.Marigold
import java.lang.reflect.InvocationTargetException

/**
 * React native module for the Marigold SDK.
 */
class RNMarigoldModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val rnMarigoldModuleImpl = RNMarigoldModuleImpl(reactContext)

    override fun getName(): String {
        return RNMarigoldModuleImpl.NAME
    }

    @ReactMethod
    fun registerForPushNotifications() {
        rnMarigoldModuleImpl.registerForPushNotifications()
    }

    @ReactMethod
    fun syncNotificationSettings() {
        rnMarigoldModuleImpl.syncNotificationSettings()
    }

    @ReactMethod
    fun updateLocation(latitude: Double, longitude: Double) {
        rnMarigoldModuleImpl.updateLocation(latitude, longitude)
    }

    @ReactMethod
    fun logRegistrationEvent(userId: String) {
        rnMarigoldModuleImpl.logRegistrationEvent(userId)
    }

    @ReactMethod
    fun getDeviceID(promise: Promise) {
        rnMarigoldModuleImpl.getDeviceID(promise)
    }

    @ReactMethod
    fun setInAppNotificationsEnabled(enabled: Boolean) {
        rnMarigoldModuleImpl.setInAppNotificationsEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise) {
        rnMarigoldModuleImpl.setGeoIPTrackingEnabled(enabled, promise)
    }

    @ReactMethod
    fun setCrashHandlersEnabled(enabled: Boolean) {
        rnMarigoldModuleImpl.setCrashHandlersEnabled(enabled)
    }
}
