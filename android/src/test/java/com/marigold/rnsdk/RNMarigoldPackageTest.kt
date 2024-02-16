package com.marigold.rnsdk

import android.content.Context
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.Marigold
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldPackageTest {
    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var reactApplicationContext: ReactApplicationContext
    @Mock
    private lateinit var staticRnMarigoldModule: MockedConstruction<RNMarigoldModule>
    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>

    private lateinit var marigold: Marigold

    private val appKey = "App Key"
    private lateinit var builder: RNMarigoldPackage.Builder

    private lateinit var rnSTPackage: RNMarigoldPackage

    @Before
    fun setup() {
        builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey).setDisplayInAppNotifications(false).setGeoIPTrackingDefault(false)
        rnSTPackage = RNMarigoldPackage(mockContext, appKey)

        marigold = staticMarigold.constructed()[0]
    }

    @Test
    fun testConstructor() {
        verify(marigold).startEngine(mockContext, appKey)
    }

    @Test
    fun testBuilderConstructor() {
        val rnSTPackageFromBuilderConstructor = RNMarigoldPackage(builder)
        marigold = staticMarigold.constructed()[1]
        verify(marigold).startEngine(mockContext, appKey)
        verify(marigold).setGeoIpTrackingDefault(false)
        verify(marigold).setGeoIpTrackingDefault(false)
        Assert.assertFalse(rnSTPackageFromBuilderConstructor.displayInAppNotifications)
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

    // Builder Tests

    @Test
    fun testBuilderCreateInstance() {
        val builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
        Assert.assertEquals(mockContext, builder.context)
        Assert.assertEquals(appKey, builder.appKey)
    }

    @Test
    fun testBuilderSetDisplayInAppNotifications() {
        val builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false)
        Assert.assertFalse(builder.displayInAppNotifications)
    }

    @Test
    fun testBuilderSetGeoIPTrackingDefault() {
        val builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setGeoIPTrackingDefault(false)
        Assert.assertFalse(builder.geoIPTrackingDefault)
    }

    @Test
    fun testBuilderBuild() {
        val rnSTPackageFromBuilderBuild = builder.build()
        marigold = staticMarigold.constructed()[1]
        verify(marigold).startEngine(mockContext, appKey)
        verify(marigold).setGeoIpTrackingDefault(false)
        Assert.assertFalse(rnSTPackageFromBuilderBuild.displayInAppNotifications)
    }
}
