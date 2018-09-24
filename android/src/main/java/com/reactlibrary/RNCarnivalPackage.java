
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
    protected boolean displayInAppNotifications;

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
     * @param displayInAppNotifications whether the SDK should handle displaying in app notifications
     */
    public RNCarnivalPackage(Context context, String appKey, boolean displayInAppNotifications) {
        Carnival.startEngine(context, appKey);
        this.displayInAppNotifications = displayInAppNotifications;
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
}