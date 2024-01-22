
package com.marigold.rnsdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.marigold.sdk.Marigold;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * React native package for the Marigold SDK.
 */
public class RNMarigoldPackage implements ReactPackage {
    protected boolean displayInAppNotifications = true;
    @VisibleForTesting
    Marigold marigold = new Marigold();

    /**
     * Default constructor - for auto linking
     */
    public RNMarigoldPackage() {}

    /**
     * Constructor for the RNMarigoldPackage.
     *
     * @param context                   the application context
     * @param appKey                    the app key provided when you registered your application.
     */
    public RNMarigoldPackage(Context context, String appKey) {
        marigold.startEngine(context, appKey);
    }

    protected RNMarigoldPackage(Builder builder) {
        marigold.setGeoIpTrackingDefault(builder.geoIPTrackingDefault);
        marigold.startEngine(builder.context, builder.appKey);
        this.displayInAppNotifications = builder.displayInAppNotifications;
    }

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new RNMarigoldModule(reactContext, displayInAppNotifications));
        return modules;
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    @SuppressWarnings("rawtypes")
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * Builder for the RNMarigoldPackage class. Use this specify additional options
     * in the RNMarigoldPackage class such as whether to display in app messages.
     */
    public static class Builder {
        protected Context context;
        protected String appKey;
        protected boolean displayInAppNotifications = true;
        protected boolean geoIPTrackingDefault = true;

        /**
         * Default constructor - should not be used
         */
        @SuppressWarnings("unused")
        private Builder() {
        }

        /**
         * Create an instance of the RNMarigoldPackage builder.
         *
         * @param context                   the application context
         * @param appKey                    the app key provided when you registered your application.
         */
        private Builder(Context context, String appKey) {
            this.context = context;
            this.appKey = appKey;
        }

        /**
         * Get an instance of the RNMarigoldPackage builder.
         *
         * @param context                   the application context
         * @param appKey                    the app key provided when you registered your application.
         */
        public static Builder createInstance(Context context, String appKey) {
            return new Builder(context, appKey);
        }

        /**
         * Set whether the SDK should display in app notifications.
         * @param displayInAppNotifications Whether the SDK should display in app notifications
         * @return the Builder instance
         */
        public Builder setDisplayInAppNotifications(boolean displayInAppNotifications) {
            this.displayInAppNotifications = displayInAppNotifications;
            return this;
        }

        /**
         * Set the default geo IP tracking value. Defaults to true. Set to false to have geo IP tracking
         * disabled on initial device creation.
         * @param geoIPTrackingDefault Whether geo IP tracking should be enabled by default.
         * @return the Builder instance
         */
        public Builder setGeoIPTrackingDefault(boolean geoIPTrackingDefault) {
            this.geoIPTrackingDefault = geoIPTrackingDefault;
            return this;
        }

        /**
         * Create the RNMarigoldPackage instance.
         * @return new RNMarigoldPackage instance
         */
        public RNMarigoldPackage build() {
            return new RNMarigoldPackage(this);
        }
    }
}
