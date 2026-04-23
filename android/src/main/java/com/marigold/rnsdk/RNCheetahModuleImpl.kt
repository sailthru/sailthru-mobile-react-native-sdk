package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.marigold.sdk.Cheetah
import com.marigold.sdk.Marigold

/**
 * React native module for the Marigold SDK.
 */
class RNCheetahModuleImpl() {

    companion object {
        const val ERROR_CODE_DEVICE = "marigold.device"
        const val ERROR_CODE_KEY = "marigold.key"
        const val NAME = "RNCheetah"
    }

    fun logRegistrationEvent(userId: String?, promise: Promise?) {
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
