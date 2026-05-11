package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_DEVICE
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_KEY
import com.marigold.sdk.Cheetah
import com.marigold.sdk.Marigold

class RNCheetahModule(reactContext: ReactApplicationContext) : NativeRNCheetahSpec(reactContext) {

    companion object {
        const val NAME = "RNCheetah"
    }

    override fun getName(): String {
        return NAME
    }

    override fun logRegistrationEvent(userId: String?, promise: Promise?) {
        createCheetah(promise)?.logRegistrationEvent(userId, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    //Helper method for instantiating Cheetah
    fun createCheetah(promise: Promise? = null): Cheetah? = try {
        Cheetah()
    } catch (e: Exception) {
        promise?.reject(ERROR_CODE_KEY,  e.message)
        null
    }
}
