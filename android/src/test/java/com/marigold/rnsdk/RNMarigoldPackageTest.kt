package com.marigold.rnsdk

import android.content.Context
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.Marigold
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldPackageTest {
    @Mock
    private lateinit var reactApplicationContext: ReactApplicationContext
    @Mock
    private lateinit var staticRnMarigoldModule: MockedConstruction<RNMarigoldModule>
    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>

    private lateinit var marigold: Marigold

    private val appKey = "App Key"

    private lateinit var rnSTPackage: RNMarigoldPackage

    @Before
    fun setup() {
        rnSTPackage = RNMarigoldPackage()
        marigold = staticMarigold.constructed()[0]
    }

    @Test
    fun testCreateNativeModules() {
        val nativeModules = rnSTPackage.createNativeModules(reactApplicationContext)
        Assert.assertEquals(1, nativeModules.size)
        val nativeModule = nativeModules[0]
        Assert.assertEquals(staticRnMarigoldModule.constructed()[0], nativeModule)
    }

    @Test
    fun testCreateJSModules() {
        val jsModules = rnSTPackage.createJSModules()
        Assert.assertTrue(jsModules.isEmpty())
    }

    @Test
    fun testCreateViewManagers() {
        val viewManagers = rnSTPackage.createViewManagers(reactApplicationContext)
        Assert.assertTrue(viewManagers.isEmpty())
    }
}
