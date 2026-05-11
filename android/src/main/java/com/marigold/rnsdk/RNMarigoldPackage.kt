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
            RNMarigoldModule.NAME -> RNMarigoldModule(reactContext)
            RNEngageBySailthruModule.NAME -> RNEngageBySailthruModule(reactContext)
            RNCheetahModule.NAME -> RNCheetahModule(reactContext)
            RNMessageStreamModule.NAME -> RNMessageStreamModule(reactContext)
            else -> null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            hashMapOf<String, ReactModuleInfo>(
                RNMarigoldModule.NAME to ReactModuleInfo(
                    RNMarigoldModule.NAME,
                    RNMarigoldModule::class.toString(),
                    false,
                    false,
                    false,
                    true
                ),
                RNEngageBySailthruModule.NAME to ReactModuleInfo(
                    RNEngageBySailthruModule.NAME,
                    RNEngageBySailthruModule::class.toString(),
                    false,
                    false,
                    false,
                    true
                ),
                RNCheetahModule.NAME to ReactModuleInfo(
                    RNCheetahModule.NAME,
                    RNCheetahModule::class.toString(),
                    false,
                    false,
                    false,
                    true
                ),
                RNMessageStreamModule.NAME to ReactModuleInfo(
                    RNMessageStreamModule.NAME,
                    RNMessageStreamModule::class.toString(),
                    false,
                    false,
                    false,
                    true
                )
            )
        }
    }
}
