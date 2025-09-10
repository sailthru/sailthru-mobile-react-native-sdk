package com.marigold.rnsdk

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider

class RNMarigoldPackage: BaseReactPackage() {
    override fun getModule(
        name: String,
        reactContext: ReactApplicationContext
    ): NativeModule? {
        return when (name) {
            RNMarigoldModuleImpl.NAME -> RNMarigoldModule(reactContext)
            RNEngageBySailthruModuleImpl.NAME -> RNEngageBySailthruModule(reactContext)
            RNMessageStreamModuleImpl.NAME -> RNMessageStreamModule(reactContext, true)
            else -> null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        val isTurboModule = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        return ReactModuleInfoProvider {
            hashMapOf<String, ReactModuleInfo>(
                RNMarigoldModuleImpl.NAME to ReactModuleInfo(
                    RNMarigoldModuleImpl.NAME,
                    RNMarigoldModule::class.toString(),
                    false,
                    false,
                    false,
                    isTurboModule
                ),
                RNEngageBySailthruModuleImpl.NAME to ReactModuleInfo(
                    RNEngageBySailthruModuleImpl.NAME,
                    RNEngageBySailthruModule::class.toString(),
                    false,
                    false,
                    false,
                    isTurboModule
                ),
                RNMessageStreamModuleImpl.NAME to ReactModuleInfo(
                    RNMessageStreamModuleImpl.NAME,
                    RNMessageStreamModule::class.toString(),
                    false,
                    false,
                    false,
                    isTurboModule
                )
            )
        }
    }
}