package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class RNCheetahModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    @VisibleForTesting
    internal var rnCheetahModuleImpl = RNCheetahModuleImpl()

    override fun getName(): String {
        return RNCheetahModuleImpl.NAME
    }

    @ReactMethod
    fun logRegistrationEvent(userId: String?, promise: Promise) {
        rnCheetahModuleImpl.logRegistrationEvent(userId, promise)
    }
}
