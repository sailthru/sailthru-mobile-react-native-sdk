package com.marigold.rnsdk

import android.content.Context
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.model.Message

object RNMessageHandler {
    fun sendMessageToReactNative(context: Context, message: Message) {
        val reactContext = context as? ReactContext
        reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            ?.emit("onMessageReceived", message.toJSON())
    }
}