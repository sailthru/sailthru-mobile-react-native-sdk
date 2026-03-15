package com.marigold.rnsdk

import com.facebook.react.bridge.ReactApplicationContext

object RNMessageStreamBridge {

    var reactContext: ReactApplicationContext? = null
    var messageStreamModuleImpl: RNMessageStreamModuleImpl? = null
}
