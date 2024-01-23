package com.marigold.rnsdk

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.marigold.sdk.Marigold
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import java.util.ArrayList
import java.util.Collections
import java.util.List

/**
 * React native package for the Marigold SDK.
 */
class RNMarigoldPackage : ReactPackage {
    protected var displayInAppNotifications = true

    @VisibleForTesting
    var marigold = Marigold()

    /**
     * Default constructor - for auto linking
     */
    constructor()

    /**
     * Constructor for the RNMarigoldPackage.
     *
     * @param context                   the application context
     * @param appKey                    the app key provided when you registered your application.
     */
    constructor(context: Context?, appKey: String?) {
        marigold.startEngine(context, appKey)
    }

    protected constructor(builder: Builder) {
        marigold.setGeoIpTrackingDefault(builder.geoIPTrackingDefault)
        marigold.startEngine(builder.context, builder.appKey)
        displayInAppNotifications = builder.displayInAppNotifications
    }
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules = ArrayList()
        modules.add(RNMarigoldModule(reactContext, displayInAppNotifications))
        return modules
    }

    fun createJSModules(): List<Class<out JavaScriptModule?>> {
        return Collections.emptyList()
    }
    @SuppressWarnings("rawtypes")
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager> {
        return Collections.emptyList()
    }

    /**
     * Builder for the RNMarigoldPackage class. Use this specify additional options
     * in the RNMarigoldPackage class such as whether to display in app messages.
     */
    class Builder {
        var context = null
        var appKey = null
        var displayInAppNotifications = true
        var geoIPTrackingDefault = true

        /**
         * Default constructor - should not be used
         */
        @SuppressWarnings("unused")
        private constructor()

        /**
         * Create an instance of the RNMarigoldPackage builder.
         *
         * @param context                   the application context
         * @param appKey                    the app key provided when you registered your application.
         */
        private constructor(context: Context, appKey: String) {
            this.context = context
            this.appKey = appKey
        }

        /**
         * Set whether the SDK should display in app notifications.
         * @param displayInAppNotifications Whether the SDK should display in app notifications
         * @return the Builder instance
         */
        fun setDisplayInAppNotifications(displayInAppNotifications: Boolean): Builder {
            this.displayInAppNotifications = displayInAppNotifications
            return this
        }

        /**
         * Set the default geo IP tracking value. Defaults to true. Set to false to have geo IP tracking
         * disabled on initial device creation.
         * @param geoIPTrackingDefault Whether geo IP tracking should be enabled by default.
         * @return the Builder instance
         */
        fun setGeoIPTrackingDefault(geoIPTrackingDefault: Boolean): Builder {
            this.geoIPTrackingDefault = geoIPTrackingDefault
            return this
        }

        /**
         * Create the RNMarigoldPackage instance.
         * @return new RNMarigoldPackage instance
         */
        fun build(): RNMarigoldPackage {
            return RNMarigoldPackage(this)
        }

        companion object {
            /**
             * Get an instance of the RNMarigoldPackage builder.
             *
             * @param context                   the application context
             * @param appKey                    the app key provided when you registered your application.
             */
            fun createInstance(context: Context, appKey: String): Builder {
                return Builder(context, appKey)
            }
        }
    }
}
