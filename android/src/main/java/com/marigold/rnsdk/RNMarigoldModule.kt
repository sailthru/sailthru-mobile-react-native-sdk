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
class RNMarigoldModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val ERROR_CODE_DEVICE = "marigold.device"
        const val ERROR_CODE_MESSAGES = "marigold.messages"
        const val ERROR_CODE_TRACKING = "marigold.tracking"
        const val ERROR_CODE_VARS = "marigold.vars"
        const val ERROR_CODE_PURCHASE = "marigold.purchase"
        const val MESSAGE_ID = "id"
        fun setWrapperInfo() {
            try {
                val cArg = arrayOf(String::class.java, String::class.java)
                val companionClass = Marigold.Companion::class.java
                val setWrapperMethod = companionClass.getDeclaredMethod("setWrapper", *cArg)

                setWrapperMethod.isAccessible = true
                setWrapperMethod.invoke(Marigold.Companion, "React Native", "11.0.0")
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
    internal var jsonConverter: JsonConverter = JsonConverter()

    init {
        setWrapperInfo()
    }

    override fun getName(): String {
        return "RNMarigold"
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
    fun logRegistrationEvent(userId: String) {
        marigold.logRegistrationEvent(userId)
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

    // wrapped to expose for testing
    fun currentActivity(): Activity? {
        return currentActivity
    }

    @ReactMethod
    fun setInAppNotificationsEnabled(enabled: Boolean) {
        marigold.setInAppNotificationsEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean) {
        marigold.setGeoIpTrackingEnabled(enabled)
    }

    @ReactMethod
    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
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
}
