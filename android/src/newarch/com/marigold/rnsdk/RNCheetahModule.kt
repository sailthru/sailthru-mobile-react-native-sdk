package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext

class RNCheetahModule(reactContext: ReactApplicationContext) : NativeRNCheetahSpec(reactContext) {
    @VisibleForTesting
    internal var rnCheetahModuleImpl = RNCheetahModuleImpl()

    override fun getName(): String {
        return RNCheetahModuleImpl.NAME
    }

    override fun logRegistrationEvent(userId: String?, promise: Promise?) {
        rnCheetahModuleImpl.logRegistrationEvent(userId, promise)
    }
}
