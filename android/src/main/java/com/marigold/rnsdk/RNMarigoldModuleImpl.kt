package com.marigold.rnsdk

import android.app.Activity
import android.location.Location
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.Marigold
import java.lang.reflect.InvocationTargetException

/**
 * React native module for the Marigold SDK.
 */
class RNMarigoldModuleImpl(private val reactContext: ReactApplicationContext) {

    companion object {
        const val ERROR_CODE_DEVICE = "marigold.device"
        const val ERROR_CODE_MESSAGES = "marigold.messages"
        const val ERROR_CODE_TRACKING = "marigold.tracking"
        const val ERROR_CODE_VARS = "marigold.vars"
        const val ERROR_CODE_PURCHASE = "marigold.purchase"
        const val MESSAGE_ID = "id"
        const val NAME = "RNMarigold"
        fun setWrapperInfo() {
            try {
                val cArg = arrayOf(String::class.java, String::class.java)
                val companionClass = Marigold.Companion::class.java
                val setWrapperMethod = companionClass.getDeclaredMethod("setWrapper", *cArg)

                setWrapperMethod.isAccessible = true
                setWrapperMethod.invoke(Marigold.Companion, "React Native", "16.1.0")
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

    init {
        setWrapperInfo()
    }

    fun registerForPushNotifications() {
        val activity = reactContext.currentActivity ?: return
        marigold.requestNotificationPermission(activity)
    }

    fun syncNotificationSettings() {
        marigold.syncNotificationSettings()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        val location = Location("React-Native").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        marigold.updateLocation(location)
    }

    fun logRegistrationEvent(userId: String?) {
        marigold.logRegistrationEvent(userId)
    }

    fun getDeviceID(promise: Promise?) {
        promise ?: return
        marigold.getDeviceId(object : Marigold.MarigoldHandler<String?> {
            override fun onSuccess(value: String?) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    fun setInAppNotificationsEnabled(enabled: Boolean) {
        marigold.setInAppNotificationsEnabled(enabled)
    }

    fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise?) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @Suppress("unused", "unused_parameter")
    fun setCrashHandlersEnabled(enabled: Boolean) {
        // noop. It's here to share signatures with iOS.
    }
}
