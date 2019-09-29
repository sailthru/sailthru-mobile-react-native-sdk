package com.reactlibrary;

import android.content.Context;

import com.carnival.sdk.Carnival;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

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

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Carnival.class})
public class RNCarnivalPackageTest {

    @Mock
    private Context mockContext;

    @Mock
    private ReactApplicationContext reactApplicationContext;

    private RNCarnivalPackage rnCarnivalPackage;
    private String appKey = "App Key";

    @Before
    public void setup() {

    }

    private void stubCarnival() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Carnival.class);
        PowerMockito.doNothing().when(Carnival.class, "startEngine", any(), any());
    }

    private void setupDefaultPackage() throws Exception {
        stubCarnival();
        rnCarnivalPackage = new RNCarnivalPackage(mockContext, appKey);
    }

    @Test
    public void testConstructor() throws Exception {
        setupDefaultPackage();
        PowerMockito.verifyStatic(Carnival.class);
        Carnival.startEngine(mockContext, appKey);
    }

    @Test
    public void testDisplayInAppConstructor() throws Exception {
        stubCarnival();
        RNCarnivalPackage carnivalPackage = new RNCarnivalPackage(mockContext, appKey, false);
        PowerMockito.verifyStatic(Carnival.class);
        Carnival.startEngine(mockContext, appKey);
        Assert.assertFalse(carnivalPackage.displayInAppNotifications);
    }

    @Test
    public void testBuilderConstructor() throws Exception {
        stubCarnival();
        RNCarnivalPackage.Builder builder = RNCarnivalPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false)
                .setGeoIPTrackingDefault(false);
        RNCarnivalPackage carnivalPackage = new RNCarnivalPackage(builder);

        PowerMockito.verifyStatic(Carnival.class);
        Carnival.setGeoIpTrackingDefault(false);
        PowerMockito.verifyStatic(Carnival.class);
        Carnival.startEngine(mockContext, appKey);
        Assert.assertFalse(carnivalPackage.displayInAppNotifications);
    }

    @Test
    public void testDefaultConstructor() {
        boolean exceptionThrown = false;
        try {
            new RNCarnivalPackage();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateNativeModules() throws Exception {
        setupDefaultPackage();
        List<NativeModule> nativeModules = rnCarnivalPackage.createNativeModules(reactApplicationContext);
        Assert.assertEquals(1, nativeModules.size());
        NativeModule nativeModule = nativeModules.get(0);
        Assert.assertTrue(nativeModule instanceof RNCarnivalModule);
    }

    @Test
    public void testCreateJSModules() throws Exception {
        setupDefaultPackage();
        List<Class<? extends JavaScriptModule>> jsModules = rnCarnivalPackage.createJSModules();
        Assert.assertTrue(jsModules.isEmpty());
    }

    @Test
    public void testCreateViewManagers() throws Exception {
        setupDefaultPackage();
        List<com.facebook.react.uimanager.ViewManager> viewManagers = rnCarnivalPackage.createViewManagers(reactApplicationContext);
        Assert.assertTrue(viewManagers.isEmpty());
    }


    // Builder Tests

    @Test
    public void testBuilderCreateInstance() {
        RNCarnivalPackage.Builder builder = RNCarnivalPackage.Builder.createInstance(mockContext, appKey);
        Assert.assertEquals(mockContext, builder.context);
        Assert.assertEquals(appKey, builder.appKey);
    }

    @Test
    public void testBuilderSetDisplayInAppNotifications() {
        RNCarnivalPackage.Builder builder = RNCarnivalPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false);
        Assert.assertFalse(builder.displayInAppNotifications);
    }

    @Test
    public void testBuilderSetGeoIPTrackingDefault() {
        RNCarnivalPackage.Builder builder = RNCarnivalPackage.Builder.createInstance(mockContext, appKey)
                .setGeoIPTrackingDefault(false);
        Assert.assertFalse(builder.geoIPTrackingDefault);
    }

    @Test
    public void testBuilderBuild() throws Exception {
        stubCarnival();
        RNCarnivalPackage carnivalPackage = RNCarnivalPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false)
                .setGeoIPTrackingDefault(false)
                .build();

        PowerMockito.verifyStatic(Carnival.class);
        Carnival.setGeoIpTrackingDefault(false);
        PowerMockito.verifyStatic(Carnival.class);
        Carnival.startEngine(mockContext, appKey);

        Assert.assertFalse(carnivalPackage.displayInAppNotifications);
    }
}
