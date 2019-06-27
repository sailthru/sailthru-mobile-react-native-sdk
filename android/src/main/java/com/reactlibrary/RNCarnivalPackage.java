
package com.reactlibrary;

import android.content.Context;

import com.carnival.sdk.Carnival;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RNCarnivalPackage implements ReactPackage {
    protected ReactApplicationContext reactApplicationContext;
    protected boolean displayInAppNotifications = true;

    /**
     * Default constructor - should not be used
     */
    protected RNCarnivalPackage() {
        throw new UnsupportedOperationException("RNCarnivalPackage constructor with parameters must be used");
    }

    /**
     * Constructor for the RNCarnivalPackage.
     *
     * @param context                   the application context
     * @param appKey                    the app key provided when you registered your application.
     */
    public RNCarnivalPackage(Context context, String appKey) {
        Carnival.startEngine(context, appKey);
    }

    /**
     * Constructor for the RNCarnivalPackage.
     *
     * @param context                   the application context
     * @param appKey                    the app key provided when you registered your application.
     * @param displayInAppNotifications whether the SDK should handle displaying in app notifications
     * @deprecated Use {@link #RNCarnivalPackage(Context, String)} or the Builder pattern to specify
     * additional options
     */
    @Deprecated
    public RNCarnivalPackage(Context context, String appKey, boolean displayInAppNotifications) {
        Carnival.startEngine(context, appKey);
        this.displayInAppNotifications = displayInAppNotifications;
    }

    protected RNCarnivalPackage(Builder builder) {
        Carnival.setGeoIpTrackingDefault(builder.geoIPTrackingDefault);
        Carnival.startEngine(builder.context, builder.appKey);
        this.displayInAppNotifications = builder.displayInAppNotifications;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        reactApplicationContext = reactContext;
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new RNCarnivalModule(reactApplicationContext, displayInAppNotifications));
        return modules;
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * Builder for the RNCarnivalPackage class.
     */
    public static class Builder {
        protected Context context;
        protected String appKey;
        protected boolean displayInAppNotifications = true;
        protected boolean geoIPTrackingDefault = true;

        /**
         * Default constructor - should not be used
         */
        private Builder() {
        }

        /**
         * Create an instance of the RNCarnivalPackage builder.
         *
         * @param context                   the application context
         * @param appKey                    the app key provided when you registered your application.
         */
        private Builder(Context context, String appKey) {
            this.context = context;
            this.appKey = appKey;
        }

        /**
         * Get an instance of the RNCarnivalPackage builder.
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
         * Create the RNCarnivalPackage instance.
         * @return new RNCarnivalPackage instance
         */
        public RNCarnivalPackage build() {
            return new RNCarnivalPackage(this);
        }
    }
}