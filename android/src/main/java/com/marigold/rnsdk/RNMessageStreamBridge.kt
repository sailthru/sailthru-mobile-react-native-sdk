package com.marigold.rnsdk

import com.facebook.react.bridge.ReactApplicationContext
import java.lang.ref.WeakReference

object RNMessageStreamBridge {

    @Volatile
    private var reactContextRef: WeakReference<ReactApplicationContext>? = null

    @Volatile
    private var messageStreamModuleImplRef: WeakReference<RNMessageStreamModuleImpl>? = null

    val reactContext: ReactApplicationContext?
        get() = reactContextRef?.get()

    val messageStreamModuleImpl: RNMessageStreamModuleImpl?
        get() = messageStreamModuleImplRef?.get()

    fun set(
        reactContext: ReactApplicationContext,
        messageStreamModuleImpl: RNMessageStreamModuleImpl
    ) {
        reactContextRef = WeakReference(reactContext)
        messageStreamModuleImplRef = WeakReference(messageStreamModuleImpl)
    }

    fun clear() {
        reactContextRef?.clear()
        messageStreamModuleImplRef?.clear()
        reactContextRef = null
        messageStreamModuleImplRef = null
    }
}
