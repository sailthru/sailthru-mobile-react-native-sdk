package com.sailthru.mobile.rnsdk;

import android.content.Context;

import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.sailthru.mobile.sdk.SailthruMobile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RNSailthruMobilePackage.class, RNSailthruMobileModule.class})
public class RNSailthruMobilePackageTest {

    @Mock
    private Context mockContext;
    @Mock
    private ReactApplicationContext reactApplicationContext;
    @Mock
    private SailthruMobile sailthruMobile;

    private final String appKey = "App Key";
    private RNSailthruMobilePackage.Builder builder;

    private RNSailthruMobilePackage rnSTPackage;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        PowerMockito.whenNew(SailthruMobile.class).withAnyArguments().thenReturn(sailthruMobile);

        builder = RNSailthruMobilePackage.Builder.createInstance(mockContext, appKey).setDisplayInAppNotifications(false).setGeoIPTrackingDefault(false);

        rnSTPackage = new RNSailthruMobilePackage(mockContext, appKey);
    }

    @Test
    public void testConstructor() {
        verify(sailthruMobile).startEngine(mockContext, appKey);
    }

    @Test
    public void testBuilderConstructor() {
        reset(sailthruMobile);
        RNSailthruMobilePackage rnSTPackageFromBuilderConstructor = new RNSailthruMobilePackage(builder);
        verify(sailthruMobile).startEngine(mockContext, appKey);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderConstructor.displayInAppNotifications);
    }

    @Test
    public void testCreateNativeModules() throws Exception {
        RNSailthruMobileModule rnSailthruMobileModule = PowerMockito.mock(RNSailthruMobileModule.class);
        PowerMockito.whenNew(RNSailthruMobileModule.class).withAnyArguments().thenReturn(rnSailthruMobileModule);
        List<NativeModule> nativeModules = rnSTPackage.createNativeModules(reactApplicationContext);
        Assert.assertEquals(1, nativeModules.size());
        NativeModule nativeModule = nativeModules.get(0);
        Assert.assertEquals(rnSailthruMobileModule, nativeModule);
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
        RNSailthruMobilePackage.Builder builder = RNSailthruMobilePackage.Builder.createInstance(mockContext, appKey);
        Assert.assertEquals(mockContext, builder.context);
        Assert.assertEquals(appKey, builder.appKey);
    }

    @Test
    public void testBuilderSetDisplayInAppNotifications() {
        RNSailthruMobilePackage.Builder builder = RNSailthruMobilePackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false);
        Assert.assertFalse(builder.displayInAppNotifications);
    }

    @Test
    public void testBuilderSetGeoIPTrackingDefault() {
        RNSailthruMobilePackage.Builder builder = RNSailthruMobilePackage.Builder.createInstance(mockContext, appKey)
                .setGeoIPTrackingDefault(false);
        Assert.assertFalse(builder.geoIPTrackingDefault);
    }

    @Test
    public void testBuilderBuild() {
        reset(sailthruMobile);
        RNSailthruMobilePackage rnSTPackageFromBuilderBuild = builder.build();
        verify(sailthruMobile).startEngine(mockContext, appKey);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderBuild.displayInAppNotifications);
    }
}
