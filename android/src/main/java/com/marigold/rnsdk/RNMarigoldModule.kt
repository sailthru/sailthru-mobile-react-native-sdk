package com.marigold.rnsdk

import android.location.Location
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_DEVICE
import com.marigold.sdk.Marigold
import java.lang.reflect.InvocationTargetException

class RNMarigoldModule(private val reactContext: ReactApplicationContext) : NativeRNMarigoldSpec(reactContext) {

    companion object {
        const val NAME = "RNMarigold"
        fun setWrapperInfo() {
            try {
                val cArg = arrayOf(String::class.java, String::class.java)
                val companionClass = Marigold.Companion::class.java
                val setWrapperMethod = companionClass.getDeclaredMethod("setWrapper", *cArg)

                setWrapperMethod.isAccessible = true
                setWrapperMethod.invoke(Marigold.Companion, "React Native", "17.0.0")
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

    override fun getName(): String {
        return NAME
    }

    override fun updateLocation(lat: Double, lon: Double) {
        val location = Location("React-Native").apply {
            latitude = lat
            longitude = lon
        }
        marigold.updateLocation(location)
    }

    override fun getDeviceID(promise: Promise?) {
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

    override fun setGeoIPTrackingEnabled(enabled: Boolean, promise: Promise?) {
        marigold.setGeoIpTrackingEnabled(enabled, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    override fun setCrashHandlersEnabled(enabled: Boolean) {
        // noop. It's here to share signatures with iOS.
    }

    override fun registerForPushNotifications() {
        val activity = reactContext.currentActivity ?: return
        marigold.requestNotificationPermission(activity)
    }

    override fun syncNotificationSettings() {
        marigold.syncNotificationSettings()
    }

    override fun setInAppNotificationsEnabled(enabled: Boolean) {
        marigold.setInAppNotificationsEnabled(enabled)
    }
}
