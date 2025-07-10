package com.marigold.rnsdk

import com.facebook.react.bridge.ReactApplicationContext
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
    private lateinit var staticRnMessageStreamModuleImpl: MockedConstruction<RNMessageStreamModule>

    private lateinit var rnSTPackage: RNMarigoldPackage

    @Before
    fun setup() {
        rnSTPackage = RNMarigoldPackage()
    }

    @Test
    fun testCreateNativeModules() {
        val nativeModules = rnSTPackage.createNativeModules(reactApplicationContext)
        Assert.assertEquals(3, nativeModules.size)
        Assert.assertEquals(staticRnMarigoldModule.constructed()[0], nativeModules[0])
        Assert.assertEquals(staticRnEngageBySailthru.constructed()[0], nativeModules[1])
        Assert.assertEquals(staticRnMessageStreamModuleImpl.constructed()[0], nativeModules[2])
    }

//    @Test
//    fun testCreateJSModules() {
//        val jsModules = rnSTPackage.createJSModules()
//        Assert.assertTrue(jsModules.isEmpty())
//    }
//
//    @Test
//    fun testCreateViewManagers() {
//        val viewManagers = rnSTPackage.createViewManagers(reactApplicationContext)
//        Assert.assertTrue(viewManagers.isEmpty())
//    }
}
