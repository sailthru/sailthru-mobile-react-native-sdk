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
    private boolean displayInAppNotifications = false;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Carnival.class);
        PowerMockito.doNothing().when(Carnival.class, "startEngine", any(), any());

        rnCarnivalPackage = new RNCarnivalPackage(mockContext, appKey, displayInAppNotifications);
    }

    @Test
    public void testConstructor() {
        PowerMockito.verifyStatic();
        Carnival.startEngine(mockContext, appKey);
        Assert.assertFalse(rnCarnivalPackage.displayInAppNotifications);
    }

    @Test
    public void testDefaultConstructor() {
        boolean exceptionThrown = false;
        try {
            RNCarnivalPackage rnCarnivalPackage = new RNCarnivalPackage();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateNativeModules() {
        List<NativeModule> nativeModules = rnCarnivalPackage.createNativeModules(reactApplicationContext);
        Assert.assertEquals(1, nativeModules.size());
        NativeModule nativeModule = nativeModules.get(0);
        Assert.assertTrue(nativeModule instanceof RNCarnivalModule);
    }

    @Test
    public void testCreateJSModules() {
        List<Class<? extends JavaScriptModule>> jsModules = rnCarnivalPackage.createJSModules();
        Assert.assertTrue(jsModules.isEmpty());
    }

    @Test
    public void testCreateViewManagers() {
        List<com.facebook.react.uimanager.ViewManager> viewManagers = rnCarnivalPackage.createViewManagers(reactApplicationContext);
        Assert.assertTrue(viewManagers.isEmpty());
    }

}
