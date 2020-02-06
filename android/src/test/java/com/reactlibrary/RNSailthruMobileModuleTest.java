package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.sailthru.mobile.sdk.model.AttributeMap;
import com.sailthru.mobile.sdk.SailthruMobile;
import com.sailthru.mobile.sdk.MessageStream;
import com.sailthru.mobile.sdk.enums.CarnivalImpressionType;
import com.sailthru.mobile.sdk.model.ContentItem;
import com.sailthru.mobile.sdk.model.Message;
import com.sailthru.mobile.sdk.model.Purchase;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RNSailthruMobileModuleTest {

    @Mock
    private ReactApplicationContext mockContext;
    @Mock
    private SailthruMobile sailthruMobile;
    @Mock
    private MessageStream messageStream;
    @InjectMocks
    private RNSailthruMobileModule rnSTModule = spy(new RNSailthruMobileModule(mockContext, true));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() {
        verify(rnSTModule).setWrapperInfo();
    }

    @Test
    public void testUpdateLocation() throws Exception {
        double latitude = 10, longitude = 11;
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);

        rnSTModule.updateLocation(latitude, longitude);

        verify(sailthruMobile).updateLocation(captor.capture());
        Location location = captor.getValue();
        Assert.assertEquals(latitude, location.getLatitude(), 0);
        Assert.assertEquals(longitude, location.getLongitude(), 0);
    }

    @Test
    public void testGetDeviceID() throws Exception {
        // Setup variables
        String deviceID = "device ID";
        String errorMessage = "error message";
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Capture handler for verification
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);

        // Mock methods
        doReturn(errorMessage).when(error).getMessage();

        // Start test
        rnSTModule.getDeviceID(promise);

        verify(sailthruMobile).getDeviceId(argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler stHandler = argumentCaptor.getValue();

        // Test success
        stHandler.onSuccess(deviceID);
        verify(promise).resolve(deviceID);

        // Test failure
        stHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testLogEvent() throws Exception {
        String event = "event string";

        rnSTModule.logEvent(event);

        verify(sailthruMobile).logEvent(event);
    }

    @Test
    public void testLogEventWithVars() throws Exception {
        String event = "event string";
        JSONObject varsJson = new JSONObject().put("varKey", "varValue");

        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // setup mocking
        doReturn(varsJson).when(rnSTModule).convertMapToJson(readableMap);

        rnSTModule.logEvent(event, readableMap);

        verify(sailthruMobile).logEvent(event, varsJson);
    }

    @Test
    public void testSetAttributes() throws Exception {
        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject attributeMapJson = mock(JSONObject.class);
        JSONObject attributeJson = mock(JSONObject.class);
        Iterator<String> keys = mock(Iterator.class);
        ArgumentCaptor<AttributeMap> captor = ArgumentCaptor.forClass(AttributeMap.class);

        // setup mocking for conversion from ReadableMap to JSON
        doReturn(attributeMapJson).when(rnSTModule).convertMapToJson(readableMap);
        when(attributeMapJson.getJSONObject("attributes")).thenReturn(attributeJson);

        // Setup JSON objects
        when(attributeJson.getInt("mergeRule")).thenReturn(0);
        when(attributeJson.keys()).thenReturn(keys);
        when(keys.hasNext()).thenReturn(false);

        // Initiate test
        rnSTModule.setAttributes(readableMap, null);

        // Verify results
        verify(sailthruMobile).setAttributes(captor.capture(), any());
        AttributeMap attributeMap = captor.getValue();
        Assert.assertEquals(0, attributeMap.getMergeRules());
    }

    @Test
    public void testGetMessages() throws Exception {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);
        ArgumentCaptor<MessageStream.MessagesHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler.class);

        // Initiate test
        rnSTModule.getMessages(promise);

        // Capture MessagesHandler to verify behaviour
        verify(messageStream).getMessages(argumentCaptor.capture());
        MessageStream.MessagesHandler messagesHandler = argumentCaptor.getValue();

        // Replace native array with mock
        doReturn(writableArray).when(rnSTModule).getWritableArray();

        // Setup message array
        ArrayList<Message> messages = new ArrayList<>();

        // Test success handler
        messagesHandler.onSuccess(messages);
        verify(promise).resolve(writableArray);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        messagesHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testSetUserId() throws Exception {
        String userID = "user ID";

        rnSTModule.setUserId(userID);

        verify(sailthruMobile).setUserId(userID, null);
    }

    @Test
    public void testSetUserEmail() throws Exception {
        String userEmail = "user email";

        rnSTModule.setUserEmail(userEmail);

        verify(sailthruMobile).setUserEmail(userEmail, null);
    }

    @Test
    public void testGetUnreadCount() throws Exception {
        // Setup
        Integer unreadCount = 4;
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);

        // Initiate test
        rnSTModule.getUnreadCount(promise);

        // Capture MessagesHandler to verify behaviour
        verify(messageStream).getUnreadMessageCount(argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler countHandler = argumentCaptor.getValue();

        // Setup message array
        ArrayList<Message> messages = new ArrayList<>();

        // Test success handler
        countHandler.onSuccess(unreadCount);
        verify(promise).resolve(unreadCount.intValue());

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        countHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testRemoveMessage() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnSTModule).getMessage(readableMap);

        // Initiate test
        rnSTModule.removeMessage(readableMap);

        // Verify result
        verify(messageStream).deleteMessage(message, null);
    }

    @Test
    public void testRegisterMessageImpression() throws Exception {
        // Create input
        ReadableMap readableMap = mock(ReadableMap.class);
        int typeCode = 0;

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnSTModule).getMessage(readableMap);

        // Initiate test
        rnSTModule.registerMessageImpression(typeCode, readableMap);

        // Verify result
        verify(messageStream).registerMessageImpression(CarnivalImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message);
    }

    @Test
    public void testMarkMessageAsRead() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnSTModule).getMessage(readableMap);

        // Initiate test
        rnSTModule.markMessageAsRead(readableMap, promise);

        // Verify result
        verify(messageStream).setMessageRead(eq(message), any(Carnival.MessagesReadHandler.class));
    }

    @Test
    public void testPresentMessageDetail() {
        // Setup input
        String messageID = "message ID";

        // Setup mocks
        ReadableMap message = mock(ReadableMap.class);
        Activity activity = mock(Activity.class);

        // Mock behaviour
        when(message.getString(RNSailthruMobileModule.MESSAGE_ID)).thenReturn(messageID);
        doReturn(activity).when(rnSTModule).currentActivity();
        doReturn(intent).when(intent).putExtra(MessageStream.EXTRA_MESSAGE_ID, messageID);

        // Initiate test
        rnSTModule.presentMessageDetail(message);

        // Verify result
        verify(activity).startActivity(any(Intent.class));
    }

    @Test
    public void testGetRecommendations() throws Exception {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);
        String sectionID = "Section ID";
        ArgumentCaptor<SailthruMobile.RecommendationsHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.RecommendationsHandler.class);

        // Initiate test
        rnSTModule.getRecommendations(sectionID, promise);

        // Capture MessagesHandler to verify behaviour
        verify(sailthruMobile).getRecommendations(eq(sectionID), argumentCaptor.capture());
        SailthruMobile.RecommendationsHandler recommendationsHandler = argumentCaptor.getValue();

        // Replace native array with mock
        doReturn(writableArray).when(rnSTModule.getWritableArray());

        // Setup message array
        ArrayList<ContentItem> contentItems = new ArrayList<>();

        // Test success handler
        recommendationsHandler.onSuccess(contentItems);
        verify(promise).resolve(writableArray);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        recommendationsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_RECOMMENDATIONS, errorMessage);
    }

    @Test
    public void testTrackClick() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";

        // Initiate test
        rnSTModule.trackClick(sectionID, urlString, promise);

        // Verify result
        verify(sailthruMobile).trackClick(eq(sectionID), any(URI.class), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackClickException() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";

        // Initiate test
        rnSTModule.trackClick(sectionID, urlString, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    public void testTrackPageview() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "www.notarealurl.com";

        // Initiate test
        rnSTModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(sailthruMobile).trackPageview(any(URI.class), isNull(List.class), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackPageviewException() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "Wrong URL Format";

        // Initiate test
        rnSTModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    public void testTrackImpression() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";
        ReadableArray readableArray = mock(ReadableArray.class);

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnSTModule.trackImpression(sectionID, readableArray, promise);

        // Verify result
        verify(sailthruMobile).trackImpression(eq(sectionID), anyListOf(URI.class), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackImpressionException() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";
        ReadableArray readableArray = mock(ReadableArray.class);

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnSTModule.trackImpression(sectionID, readableArray, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    public void testSetGeoIPTrackingEnabled() throws Exception {
        // Create input
        boolean enabled = true;

        // Initiate test
        rnSTModule.setGeoIPTrackingEnabled(enabled);

        // Verify result
        verify(sailthruMobile).setGeoIpTrackingEnabled(enabled);
    }

    @Test
    public void testSeGeoIPTrackingEnabledWithPromise() throws Exception {
        // Create input
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSTModule.setGeoIPTrackingEnabled(false, promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setGeoIpTrackingEnabled(eq(false), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler geoTrackingHandler = argumentCaptor.getValue();

        // Test success handler
        geoTrackingHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        geoTrackingHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testClearDevice() throws Exception {
        // Create input
        int clearValue = SailthruMobile.ATTRIBUTES;
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSTModule.clearDevice(clearValue, promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).clearDevice(eq(clearValue), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler clearHandler = argumentCaptor.getValue();

        // Test success handler
        clearHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        clearHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testSetProfileVars() throws Exception {
        // Create input
        ReadableMap vars = mock(ReadableMap.class);
        JSONObject varsJson = mock(JSONObject.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(varsJson).when(rnSTModule).convertMapToJson(vars);

        // Initiate test
        rnSTModule.setProfileVars(vars, promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setProfileVars(eq(varsJson), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler setVarsHandler = argumentCaptor.getValue();

        // Test success handler
        setVarsHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        setVarsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    public void testGetProfileVars() throws Exception {
        // Create input
        JSONObject varsJson = new JSONObject();
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        WritableMap mockMap = mock(WritableMap.class);

        // Mock methods
        doReturn(mockMap).when(rnSTModule).convertJsonToMap(any(JSONObject.class));

        // Initiate test
        rnSTModule.getProfileVars(promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).getProfileVars(argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler getVarsHandler = spy(argumentCaptor.getValue());

        // Test success handler
        getVarsHandler.onSuccess(varsJson);
        verify(promise).resolve(mockMap);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        getVarsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    public void testLogPurchase() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(purchase).when(rnSTModule).getPurchaseInstance(purchaseMap, promise);

        // Initiate test
        rnSTModule.logPurchase(purchaseMap, promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).logPurchase(eq(purchase), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_PURCHASE, errorMessage);
    }

    @Test
    public void testLogAbandonedCart() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(purchase).when(rnSTModule).getPurchaseInstance(purchaseMap, promise);

        // Initiate test
        rnSTModule.logAbandonedCart(purchaseMap, promise);

        // Verify result
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).logAbandonedCart(eq(purchase), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_PURCHASE, errorMessage);
    }
}
