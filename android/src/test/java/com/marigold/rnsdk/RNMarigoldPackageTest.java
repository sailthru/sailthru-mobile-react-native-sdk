package com.marigold.rnsdk;

import android.content.Context;

import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.marigold.sdk.Marigold;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RNMarigoldPackageTest {
    @Mock
    private Context mockContext;
    @Mock
    private ReactApplicationContext reactApplicationContext;
    @Mock
    private MockedConstruction<RNMarigoldModule> staticRnMarigoldModule;
    @Mock
    private MockedConstruction<Marigold> staticMarigold;

    private Marigold marigold;

    private final String appKey = "App Key";
    private RNMarigoldPackage.Builder builder;

    private RNMarigoldPackage rnSTPackage;

    @Before
    public void setup() {
        builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey).setDisplayInAppNotifications(false).setGeoIPTrackingDefault(false);

        rnSTPackage = new RNMarigoldPackage(mockContext, appKey);

        marigold = staticMarigold.constructed().get(0);
    }

    @Test
    public void testConstructor() {
        verify(marigold).startEngine(mockContext, appKey);
    }

    @Test
    public void testBuilderConstructor() {
        RNMarigoldPackage rnSTPackageFromBuilderConstructor = new RNMarigoldPackage(builder);
        marigold = staticMarigold.constructed().get(1);
        verify(marigold).startEngine(mockContext, appKey);
        verify(marigold).setGeoIpTrackingDefault(false);
        verify(marigold).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderConstructor.displayInAppNotifications);
    }

    @Test
    public void testCreateNativeModules() {
        List<NativeModule> nativeModules = rnSTPackage.createNativeModules(reactApplicationContext);
        Assert.assertEquals(1, nativeModules.size());
        NativeModule nativeModule = nativeModules.get(0);
        Assert.assertEquals(staticRnMarigoldModule.constructed().get(0), nativeModule);
    }

    @Test
    public void testCreateJSModules() {
        List<Class<? extends JavaScriptModule>> jsModules = rnSTPackage.createJSModules();
        Assert.assertTrue(jsModules.isEmpty());
    }

    @Test
    public void testCreateViewManagers() {
        @SuppressWarnings("rawtypes")
        List<com.facebook.react.uimanager.ViewManager> viewManagers = rnSTPackage.createViewManagers(reactApplicationContext);
        Assert.assertTrue(viewManagers.isEmpty());
    }


    // Builder Tests

    @Test
    public void testBuilderCreateInstance() {
        RNMarigoldPackage.Builder builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey);
        Assert.assertEquals(mockContext, builder.context);
        Assert.assertEquals(appKey, builder.appKey);
    }

    @Test
    public void testBuilderSetDisplayInAppNotifications() {
        RNMarigoldPackage.Builder builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false);
        Assert.assertFalse(builder.displayInAppNotifications);
    }

    @Test
    public void testBuilderSetGeoIPTrackingDefault() {
        RNMarigoldPackage.Builder builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setGeoIPTrackingDefault(false);
        Assert.assertFalse(builder.geoIPTrackingDefault);
    }

    @Test
    public void testBuilderBuild() {
        RNMarigoldPackage rnSTPackageFromBuilderBuild = builder.build();
        marigold = staticMarigold.constructed().get(1);
        verify(marigold).startEngine(mockContext, appKey);
        verify(marigold).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderBuild.displayInAppNotifications);
    }
}
