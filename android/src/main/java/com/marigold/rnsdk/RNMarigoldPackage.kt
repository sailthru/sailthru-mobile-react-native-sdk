package com.marigold.rnsdk

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class RNMarigoldPackage : ReactPackage {

    var displayInAppNotifications = true

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules: MutableList<NativeModule> = ArrayList()
        modules.add(RNMarigoldModule(reactContext, displayInAppNotifications))
        modules.add(RNEngageBySailthruModule(reactContext))
        return modules
    }

    fun createJSModules(): List<Class<out JavaScriptModule?>> {
        return emptyList()
    }

    @SuppressWarnings("rawtypes")
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }

}
