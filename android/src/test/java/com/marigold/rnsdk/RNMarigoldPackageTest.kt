package com.marigold.rnsdk

import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.EngageBySailthru
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
    private lateinit var staticRnEngageBySailthru: MockedConstruction<RNEngageBySailthruModule>
    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>
    @Mock
    private lateinit var staticEngageBySailthru: MockedConstruction<EngageBySailthru>

    private lateinit var marigold: Marigold

    private lateinit var engageBySailthru: EngageBySailthru

    private lateinit var rnSTPackage: RNMarigoldPackage

    @Before
    fun setup() {
        rnSTPackage = RNMarigoldPackage()
        marigold = staticMarigold.constructed()[0]
        engageBySailthru = staticEngageBySailthru.constructed()[0]
    }

    @Test
    fun testCreateNativeModules() {
        val nativeModules = rnSTPackage.createNativeModules(reactApplicationContext)
        Assert.assertEquals(2, nativeModules.size)
        Assert.assertEquals(staticRnMarigoldModule.constructed()[0], nativeModules[0])
        Assert.assertEquals(staticRnEngageBySailthru.constructed()[0], nativeModules[1])
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
