package com.reactlibrary;

import android.content.Context;

import com.sailthru.mobile.sdk.Carnival;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RNSailthruMobilePackageTest {

    @Mock
    private Context mockContext;
    @Mock
    private ReactApplicationContext reactApplicationContext;
    @Mock
    private SailthruMobile sailthruMobile;

    private String appKey = "App Key";
    private RNSailthruMobilePackage.Builder builder = RNSailthruMobilePackage.Builder.createInstance(mockContext, appKey).setDisplayInAppNotifications(false).setGeoIPTrackingDefault(false);

    @InjectMocks
    private RNSailthruMobilePackage rnSTPackage = new RNSailthruMobilePackage(mockContext, appKey);
    @InjectMocks
    private RNSailthruMobilePackage rnSTPackageFromBuilderConstructor = new RNSailthruMobilePackage(builder);
    @InjectMocks
    private RNSailthruMobilePackage rnSTPackageFromBuilderBuild = builder.build();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() throws Exception {
        verify(sailthruMobile, times(2)).startEngine(mockContext, appKey);
    }

    @Test
    public void testBuilderConstructor() throws Exception {
        verify(sailthruMobile, times(2)).startEngine(mockContext, appKey);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderConstructor.displayInAppNotifications);
    }

    @Test
    public void testDefaultConstructor() {
        boolean exceptionThrown = false;
        try {
            new RNSailthruMobilePackage();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateNativeModules() throws Exception {
        List<NativeModule> nativeModules = rnSTPackage.createNativeModules(reactApplicationContext);
        Assert.assertEquals(1, nativeModules.size());
        NativeModule nativeModule = nativeModules.get(0);
        Assert.assertTrue(nativeModule instanceof RNSailthruMobileModule);
    }

    @Test
    public void testCreateJSModules() throws Exception {
        List<Class<? extends JavaScriptModule>> jsModules = rnSTPackage.createJSModules();
        Assert.assertTrue(jsModules.isEmpty());
    }

    @Test
    public void testCreateViewManagers() throws Exception {
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
    public void testBuilderBuild() throws Exception {
        verify(sailthruMobile, times(3)).startEngine(mockContext, appKey);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        verify(sailthruMobile).setGeoIpTrackingDefault(false);
        Assert.assertFalse(rnSTPackageFromBuilderBuild.displayInAppNotifications);
    }
}
