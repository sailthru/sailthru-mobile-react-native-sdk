package com.marigold.rnsdk;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.marigold.sdk.MessageStream;
import com.marigold.sdk.model.AttributeMap;
import com.marigold.sdk.Marigold;
import com.marigold.sdk.enums.ImpressionType;
import com.marigold.sdk.model.ContentItem;
import com.marigold.sdk.model.Message;
import com.marigold.sdk.model.Purchase;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.marigold.sdk.model.PurchaseAdjustment;
import com.marigold.sdk.model.PurchaseItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RNMarigoldModuleTest {
    @Mock
    private ReactApplicationContext mockContext;
    @Mock
    private JsonConverter jsonConverter;
    @Mock
    private MockedStatic<RNMarigoldModule> staticRnMarigoldModule;
    @Mock
    private MockedConstruction<Marigold> staticMarigold;
    @Mock
    private MockedConstruction<MessageStream> staticMessageStream;

    private Marigold marigold;
    private MessageStream messageStream;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private RNMarigoldModule rnMarigoldModule;
    private RNMarigoldModule rnMarigoldModuleSpy;

    @Before
    public void setup() {
        rnMarigoldModule = new RNMarigoldModule(mockContext, true);
        rnMarigoldModule.jsonConverter = jsonConverter;
        rnMarigoldModuleSpy = spy(rnMarigoldModule);

        marigold = staticMarigold.constructed().get(0);
        messageStream = staticMessageStream.constructed().get(0);
    }

    @Test
    public void testConstructor() {
        verify(messageStream).setOnInAppNotificationDisplayListener(rnMarigoldModule);

        staticRnMarigoldModule.verify(RNMarigoldModule::setWrapperInfo);
    }

    @Test
    public void testShouldPresentInAppNotification() throws Exception {
        Message message = mock(Message.class);
        DeviceEventManagerModule.RCTDeviceEventEmitter module = mock(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        WritableMap writableMap = mock(WritableMap.class);
        JSONObject jsonObject = mock(JSONObject.class);

        when(message.toJSON()).thenReturn(jsonObject);
        when(jsonConverter.convertJsonToMap(jsonObject)).thenReturn(writableMap);
        when(mockContext.getJSModule(any())).thenReturn(module);

        boolean shouldPresent = rnMarigoldModuleSpy.shouldPresentInAppNotification(message);

        verify(mockContext).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        verify(module).emit("inappnotification", writableMap);

        Assert.assertTrue(shouldPresent);
    }

    @Test
    public void testShouldPresentInAppNotificationException() throws Exception {
        Message message = mock(Message.class);
        JSONObject jsonObject = mock(JSONObject.class);
        JSONException jsonException = mock(JSONException.class);

        when(message.toJSON()).thenReturn(jsonObject);
        when(jsonConverter.convertJsonToMap(jsonObject)).thenThrow(jsonException);

        boolean shouldPresent = rnMarigoldModuleSpy.shouldPresentInAppNotification(message);

        verify(mockContext, times(0)).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        verify(jsonException).printStackTrace();

        Assert.assertTrue(shouldPresent);
    }

    @Test
    public void testStartEngine() {
        String testKey = "TEST KEY";

        rnMarigoldModule.startEngine(testKey);

        verify(mockContext).runOnUiQueueThread(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(marigold).startEngine(mockContext, testKey);
    }

    @Test
    public void testUpdateLocation() {
        double latitude = 10, longitude = 10;

        try(MockedConstruction<Location> staticLocation = mockConstruction(Location.class)) {
            rnMarigoldModule.updateLocation(latitude, longitude);

            Location location = staticLocation.constructed().get(0);
            verify(location).setLatitude(latitude);
            verify(location).setLongitude(longitude);
            verify(marigold).updateLocation(location);
        }
    }

    @Test
    public void testRegisterForPushNotifications() {
        Activity activity = mock(Activity.class);

        // Mock behaviour
        doReturn(activity).when(rnMarigoldModuleSpy).currentActivity();

        rnMarigoldModuleSpy.registerForPushNotifications();

        verify(marigold).requestNotificationPermission(activity);
    }

    @Test
    public void testRegisterForPushNotificationsNoActivity() {
        // Mock behaviour
        doReturn(null).when(rnMarigoldModuleSpy).currentActivity();

        rnMarigoldModuleSpy.registerForPushNotifications();

        verify(marigold, never()).requestNotificationPermission(any());
    }

    @Test
    public void testSyncNotificationSettings() {
        rnMarigoldModule.syncNotificationSettings();

        verify(marigold).syncNotificationSettings();
    }

    @Test
    public void testGetDeviceID() {
        // Setup variables
        String deviceID = "device ID";
        String errorMessage = "error message";
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(errorMessage).when(error).getMessage();

        // Start test
        rnMarigoldModule.getDeviceID(promise);

        // Capture handler for verification
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<String>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).getDeviceId(argumentCaptor.capture());
        Marigold.MarigoldHandler<String> marigoldHandler = argumentCaptor.getValue();

        // Test success
        marigoldHandler.onSuccess(deviceID);
        verify(promise).resolve(deviceID);

        // Test failure
        marigoldHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testLogEvent() {
        String event = "event string";

        rnMarigoldModule.logEvent(event);

        verify(Marigold).logEvent(event);
    }

    @Test
    public void testLogEventWithVars() throws Exception {
        String event = "event string";
        JSONObject varsJson = new JSONObject();
        varsJson.put("varKey", "varValue");

        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // setup mocking
        when(jsonConverter.convertMapToJson(readableMap)).thenReturn(varsJson);

        rnMarigoldModuleSpy.logEvent(event, readableMap);

        verify(marigold).logEvent(event, varsJson);
    }

    @Test
    public void testLogEventWithVarsException() throws Exception {
        String event = "event string";
        JSONException jsonException = mock(JSONException.class);

        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // setup mocking
        when(jsonConverter.convertMapToJson(readableMap)).thenThrow(jsonException);

        rnMarigoldModuleSpy.logEvent(event, readableMap);

        verify(jsonException).printStackTrace();
        verify(marigold).logEvent(event, null);
    }

    @Test
    public void testSetAttributes() throws Exception {
        JSONObject stringAttributeJson = new JSONObject()
                .put("type", "string")
                .put("value", "test string");
        JSONObject intAttributeJson = new JSONObject()
                .put("type", "integer")
                .put("value", 123);
        JSONObject attributesJson = new JSONObject()
                .put("string key", stringAttributeJson)
                .put("int key", intAttributeJson);
        JSONObject attributeMapJson = new JSONObject()
                .put("mergeRule", AttributeMap.RULE_UPDATE)
                .put("attributes", attributesJson);

        Error error = new Error("test error");

        // setup mocks
        Promise promise = mock(Promise.class);
        ReadableMap readableMap = mock(ReadableMap.class);

        // setup mocking for conversion from ReadableMap to JSON
        when(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeMapJson);

        // Capture Attributes to verify
        ArgumentCaptor<AttributeMap> attributeCaptor = ArgumentCaptor.forClass(AttributeMap.class);
        ArgumentCaptor<Marigold.AttributesHandler> handlerCaptor = ArgumentCaptor.forClass(Marigold.AttributesHandler.class);

        // Initiate test
        rnMarigoldModuleSpy.setAttributes(readableMap, promise);

        // Verify results
        verify(marigold).setAttributes(attributeCaptor.capture(), handlerCaptor.capture());

        AttributeMap attributes = attributeCaptor.getValue();

        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributes.getMergeRules());
        Assert.assertEquals("test string", attributes.getString("string key"));
        Assert.assertEquals(123, attributes.getInt("int key", 0));

        Marigold.AttributesHandler handler = handlerCaptor.getValue();

        handler.onSuccess();
        verify(promise).resolve(null);

        handler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.getMessage());
    }

    @Test
    public void testGetMessages() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);

        // Initiate test
        rnMarigoldModuleSpy.getMessages(promise);

        // Capture MessagesHandler to verify behaviour
        ArgumentCaptor<MessageStream.MessagesHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler.class);
        verify(messageStream).getMessages(argumentCaptor.capture());
        MessageStream.MessagesHandler messagesHandler = argumentCaptor.getValue();

        // Replace native array with mock
        doReturn(writableArray).when(rnMarigoldModuleSpy).getWritableArray();

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
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testSetUserId() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        String userID = "user ID";

        rnMarigoldModule.setUserId(userID, promise);

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).setUserId(eq(userID), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess(null);
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testSetUserEmail() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        String userEmail = "user email";

        rnMarigoldModule.setUserEmail(userEmail, promise);

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).setUserEmail(eq(userEmail), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess(null);
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testGetUnreadCount() {
        Integer unreadCount = 4;

        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnMarigoldModule.getUnreadCount(promise);

        // Capture MessagesHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MessageStream.MessageStreamHandler<Integer>> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageStreamHandler.class);
        verify(messageStream).getUnreadMessageCount(argumentCaptor.capture());
        MessageStream.MessageStreamHandler<Integer> countHandler = argumentCaptor.getValue();

        // Test success handler
        countHandler.onSuccess(unreadCount);
        verify(promise).resolve(unreadCount);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        countHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testRemoveMessage() throws Exception {
        // Create mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        ReadableMap readableMap = mock(ReadableMap.class);
        Message message = mock(Message.class);

        RNMarigoldModule moduleSpy = spy(rnMarigoldModule);
        doReturn(message).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise);

        // Capture MarigoldHandler to verify behaviour
        ArgumentCaptor<MessageStream.MessageDeletedHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageDeletedHandler.class);
        verify(messageStream).deleteMessage(eq(message), argumentCaptor.capture());
        MessageStream.MessageDeletedHandler handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess();
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testRemoveMessageException() throws Exception {
        // Create mocks
        Promise promise = mock(Promise.class);
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONException jsonException = new JSONException("test exception");

        RNMarigoldModule moduleSpy = spy(rnMarigoldModule);
        doThrow(jsonException).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise);

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.getMessage());
        verify(messageStream, times(0)).deleteMessage(any(Message.class), any(MessageStream.MessageDeletedHandler.class));
    }

    @Test
    public void testRegisterMessageImpression() throws Exception {
        // Create input
        int typeCode = 0;
        ReadableMap readableMap = mock(ReadableMap.class);
        Message message = mock(Message.class);

        doReturn(message).when(rnMarigoldModuleSpy).getMessage(readableMap);

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap);

        // Verify result
        verify(messageStream).registerMessageImpression(ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message);
    }

    @Test
    public void testRegisterMessageImpressionInvalidCode() {
        // Create input
        int typeCode = 10;
        ReadableMap readableMap = mock(ReadableMap.class);

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap);

        // Verify result
        verify(messageStream, times(0)).registerMessageImpression(any(ImpressionType.class), any(Message.class));
    }

    @Test
    public void testRegisterMessageImpressionException() throws Exception {
        // Create input
        int typeCode = 0;
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONException jsonException = mock(JSONException.class);

        doThrow(jsonException).when(rnMarigoldModuleSpy).getMessage(readableMap);

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap);

        // Verify result
        verify(jsonException).printStackTrace();
        verify(messageStream, times(0)).registerMessageImpression(any(ImpressionType.class), any(Message.class));
    }

    @Test
    public void testMarkMessageAsRead() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);
        Message message = mock(Message.class);
        Error error = mock(Error.class);

        RNMarigoldModule moduleSpy = spy(rnMarigoldModule);
        doReturn(message).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise);

        // Capture MarigoldHandler to verify behaviour
        ArgumentCaptor<MessageStream.MessagesReadHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesReadHandler.class);
        verify(messageStream).setMessageRead(eq(message), argumentCaptor.capture());
        MessageStream.MessagesReadHandler handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess();
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testMarkMessageAsReadException() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);
        JSONException jsonException = new JSONException("test exception");

        RNMarigoldModule moduleSpy = spy(rnMarigoldModule);
        doThrow(jsonException).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise);

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.getMessage());
        verify(messageStream, times(0)).setMessageRead(any(Message.class), any(MessageStream.MessagesReadHandler.class));
    }

    @Test
    public void testPresentMessageDetail() {
        // Setup input
        String messageID = "message ID";

        // Setup mocks
        ReadableMap message = mock(ReadableMap.class);
        Activity activity = mock(Activity.class);
        Intent intent = mock(Intent.class);

        // Mock behaviour
        when(message.getString(RNMarigoldModule.MESSAGE_ID)).thenReturn(messageID);
        doReturn(activity).when(rnMarigoldModuleSpy).currentActivity();
        doReturn(intent).when(rnMarigoldModuleSpy).getMessageActivityIntent(any(Activity.class), anyString());

        // Initiate test
        rnMarigoldModuleSpy.presentMessageDetail(message);

        // Verify result
        verify(activity).startActivity(intent);
    }

    @Test
    public void testGetRecommendations() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);
        String sectionID = "Section ID";

        // Initiate test
        rnMarigoldModuleSpy.getRecommendations(sectionID, promise);

        // Capture MessagesHandler to verify behaviour
        ArgumentCaptor<Marigold.RecommendationsHandler> argumentCaptor = ArgumentCaptor.forClass(Marigold.RecommendationsHandler.class);
        verify(marigold).getRecommendations(eq(sectionID), argumentCaptor.capture());
        Marigold.RecommendationsHandler recommendationsHandler = argumentCaptor.getValue();

        // Replace native array with mock
        doReturn(writableArray).when(rnMarigoldModuleSpy).getWritableArray();

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
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_RECOMMENDATIONS, errorMessage);
    }

    @Test
    public void testTrackClick() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";
        Error error = new Error("test error");

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise);

        // Capture arguments to verify behaviour
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<Marigold.TrackHandler> handlerCaptor = ArgumentCaptor.forClass(Marigold.TrackHandler.class);

        // Verify result
        verify(marigold).trackClick(eq(sectionID), uriCaptor.capture(), handlerCaptor.capture());
        URI uri = uriCaptor.getValue();
        Marigold.TrackHandler trackHandler = handlerCaptor.getValue();

        Assert.assertEquals(urlString, uri.toString());

        trackHandler.onSuccess();
        verify(promise).resolve(true);

        trackHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.getMessage());
    }

    @Test
    public void testTrackClickException() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise);

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString());
        verify(marigold, times(0)).trackClick(anyString(), any(URI.class), any(Marigold.TrackHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTrackPageview() {
        // Create input
        String urlString = "www.notarealurl.com";
        String testTag = "some tag";
        Error error = new Error("test error");

        // Create mocks
        Promise promise = mock(Promise.class);
        ReadableArray tagsArray = mock(ReadableArray.class);
        when(tagsArray.size()).thenReturn(1);
        when(tagsArray.getString(anyInt())).thenReturn(testTag);

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, tagsArray, promise);

        // Capture arguments to verify behaviour
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<ArrayList<String>> arrayCaptor = ArgumentCaptor.forClass(ArrayList.class);
        ArgumentCaptor<Marigold.TrackHandler> handlerCaptor = ArgumentCaptor.forClass(Marigold.TrackHandler.class);

        // Verify result
        verify(marigold).trackPageview(uriCaptor.capture(), arrayCaptor.capture(), handlerCaptor.capture());
        URI uri = uriCaptor.getValue();
        ArrayList<String> tags = arrayCaptor.getValue();
        Marigold.TrackHandler trackHandler = handlerCaptor.getValue();

        Assert.assertEquals(urlString, uri.toString());

        Assert.assertEquals(testTag, tags.get(0));

        trackHandler.onSuccess();
        verify(promise).resolve(true);

        trackHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.getMessage());
    }

    @Test
    public void testTrackPageviewNullTags() {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "www.notarealurl.com";

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(marigold).trackPageview(any(URI.class), ArgumentMatchers.isNull(), any(Marigold.TrackHandler.class));
    }

    @Test
    public void testTrackPageviewException() {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "Wrong URL Format";

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString());
        verify(marigold, times(0)).trackPageview(any(URI.class), ArgumentMatchers.anyList(), any(Marigold.TrackHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTrackImpression() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";
        ReadableArray readableArray = mock(ReadableArray.class);
        Error error = new Error("test error");

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, readableArray, promise);

        // Capture arguments to verify behaviour
        ArgumentCaptor<List<URI>> uriCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Marigold.TrackHandler> handlerCaptor = ArgumentCaptor.forClass(Marigold.TrackHandler.class);

        // Verify result
        verify(marigold).trackImpression(eq(sectionID), uriCaptor.capture(), handlerCaptor.capture());
        List<URI> uriList = uriCaptor.getValue();
        Marigold.TrackHandler trackHandler = handlerCaptor.getValue();

        Assert.assertEquals(urlString, uriList.get(0).toString());

        trackHandler.onSuccess();
        verify(promise).resolve(true);

        trackHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.getMessage());
    }

    @Test
    public void testTrackImpressionNullUrls() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, null, promise);

        // Verify result
        verify(marigold).trackImpression(eq(sectionID), ArgumentMatchers.isNull(), any(Marigold.TrackHandler.class));
    }

    @Test
    public void testTrackImpressionException() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";
        ReadableArray readableArray = mock(ReadableArray.class);

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, readableArray, promise);

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString());
        verify(marigold, times(0)).trackImpression(anyString(), ArgumentMatchers.anyList(), any(Marigold.TrackHandler.class));
    }

    @Test
    public void testSetGeoIPTrackingEnabled() {
        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(true);

        // Verify result
        verify(marigold).setGeoIpTrackingEnabled(true);
    }

    @Test
    public void testSeGeoIPTrackingEnabledWithPromise() {
        // Create input
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(false, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).setGeoIpTrackingEnabled(eq(false), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> clearHandler = argumentCaptor.getValue();

        // Test success handler
        clearHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        clearHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testClearDevice() {
        // Create input
        int clearValue = Marigold.ATTRIBUTES;
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnMarigoldModule.clearDevice(clearValue, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).clearDevice(eq(clearValue), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> clearHandler = argumentCaptor.getValue();

        // Test success handler
        clearHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        clearHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testSetProfileVars() throws Exception {
        JSONObject varsJson = new JSONObject().put("test var", 123);

        // Create input
        ReadableMap vars = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        when(jsonConverter.convertMapToJson(vars)).thenReturn(varsJson);

        // Initiate test
        rnMarigoldModuleSpy.setProfileVars(vars, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).setProfileVars(eq(varsJson), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> setVarsHandler = argumentCaptor.getValue();

        // Test success handler
        setVarsHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        setVarsHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetProfileVarsException() throws Exception {
        JSONException jsonException = new JSONException("test exception");

        // Create input
        ReadableMap vars = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);

        // Mock methods
        when(jsonConverter.convertMapToJson(vars)).thenThrow(jsonException);

        // Initiate test
        rnMarigoldModuleSpy.setProfileVars(vars, promise);

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, jsonException.getMessage());
        verify(marigold, times(0)).setProfileVars(any(JSONObject.class), any(Marigold.MarigoldHandler.class));
    }

    @Test
    public void testGetProfileVars() throws Exception {
        // Create input
        JSONObject varsJson = new JSONObject().put("test var", 123);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        WritableMap mockMap = mock(WritableMap.class);

        // Mock methods
        when(jsonConverter.convertJsonToMap(varsJson)).thenReturn(mockMap);

        // Initiate test
        rnMarigoldModuleSpy.getProfileVars(promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<JSONObject>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).getProfileVars(argumentCaptor.capture());
        Marigold.MarigoldHandler<JSONObject> getVarsHandler = spy(argumentCaptor.getValue());

        // Test success handler
        getVarsHandler.onSuccess(varsJson);
        verify(jsonConverter).convertJsonToMap(varsJson);
        verify(promise).resolve(mockMap);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        getVarsHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    public void testLogPurchase() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(purchase).when(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap);

        // Initiate test
        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).logPurchase(eq(purchase), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLogPurchaseException() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);
        JSONException jsonException = new JSONException("test exception");

        // Mock methods
        doThrow(jsonException).when(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap);

        // Initiate test
        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise);

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.getMessage());
        verify(marigold, times(0)).logPurchase(any(Purchase.class), any(Marigold.MarigoldHandler.class));
    }

    @Test
    public void testLogAbandonedCart() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(purchase).when(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap);

        // Initiate test
        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Marigold.MarigoldHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler.class);
        verify(marigold).logAbandonedCart(eq(purchase), argumentCaptor.capture());
        Marigold.MarigoldHandler<Void> purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLogAbandonedCartException() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);
        JSONException jsonException = new JSONException("test exception");

        // Mock methods
        doThrow(jsonException).when(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap);

        // Initiate test
        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise);

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.getMessage());
        verify(marigold, times(0)).logAbandonedCart(any(Purchase.class), any(Marigold.MarigoldHandler.class));
    }

    @Test
    public void testGetPurchaseInstancePositiveAdjustment() throws Exception {
        // Mock methods
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject purchaseJson = createPurchaseJson(234);
        when(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson);

        // Initiate test
        Purchase purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap);

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false);

        PurchaseItem item = purchase.getPurchaseItems().get(0);
        Assert.assertEquals(1, item.getQuantity());
        Assert.assertEquals("test title", item.getTitle());
        Assert.assertEquals(123, item.getPrice());
        Assert.assertEquals("456", item.getID());
        Assert.assertEquals(new URI("http://mobile.sailthru.com"), item.getUrl());

        PurchaseAdjustment adjustment = purchase.getPurchaseAdjustments().get(0);
        Assert.assertEquals("tax", adjustment.getTitle());
        Assert.assertEquals(234, adjustment.getPrice());
    }

    @Test
    public void testGetPurchaseInstanceNegativeAdjustment() throws Exception {
        // Mock methods
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject purchaseJson = createPurchaseJson(-234);
        when(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson);

        // Initiate test
        Purchase purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap);

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false);

        PurchaseItem item = purchase.getPurchaseItems().get(0);
        Assert.assertEquals(1, item.getQuantity());
        Assert.assertEquals("test title", item.getTitle());
        Assert.assertEquals(123, item.getPrice());
        Assert.assertEquals("456", item.getID());
        Assert.assertEquals(new URI("http://mobile.sailthru.com"), item.getUrl());

        PurchaseAdjustment adjustment = purchase.getPurchaseAdjustments().get(0);
        Assert.assertEquals("tax", adjustment.getTitle());
        Assert.assertEquals(-234, adjustment.getPrice());
    }

    @Test
    public void testGetMessage() throws Exception {
        // Mock methods
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject messageJson = new JSONObject().put("title", "test title");
        when(jsonConverter.convertMapToJson(readableMap)).thenReturn(messageJson);

        // Initiate test
        Message message = rnMarigoldModuleSpy.getMessage(readableMap);

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap);

        Assert.assertEquals("test title", message.getTitle());
    }

    @Test
    public void testGetAttributeMap() throws Exception {
        Date date = new Date();

        // Mock methods
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject attributeJson = createAttributeMapJson(date);
        when(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeJson);

        // Initiate test
        AttributeMap attributeMap = rnMarigoldModuleSpy.getAttributeMap(readableMap);

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap);

        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributeMap.getMergeRules());
        Assert.assertEquals("test string", attributeMap.getString("stringKey"));
        Assert.assertEquals(123, attributeMap.getInt("integerKey", 0));
        Assert.assertTrue(attributeMap.getBoolean("booleanKey", false));
        Assert.assertEquals(1.23, attributeMap.getFloat("floatKey", 0), 0.001);
        Assert.assertEquals(date, attributeMap.getDate("dateKey"));
    }


    /** Helpers **/

    private JSONObject createPurchaseJson(int adjustmentPrice) throws Exception {
        JSONObject adjustmentJson = new JSONObject()
                .put("title", "tax")
                .put("price", adjustmentPrice);

        JSONArray adjustmentsArray = new JSONArray()
                .put(adjustmentJson);

        JSONObject itemJson = new JSONObject()
                .put("qty", 1)
                .put("title", "test title")
                .put("price", 123)
                .put("id", "456")
                .put("url", "http://mobile.sailthru.com");

        JSONArray itemsArray = new JSONArray()
                .put(itemJson);

        return new JSONObject()
                .put("items", itemsArray)
                .put("adjustments", adjustmentsArray);
    }

    private JSONObject createAttributeMapJson(Date date) throws Exception {
        JSONObject stringObject = new JSONObject()
                .put("type", "string")
                .put("value", "test string");
        JSONObject integerObject = new JSONObject()
                .put("type", "integer")
                .put("value", 123);
        JSONObject booleanObject = new JSONObject()
                .put("type", "boolean")
                .put("value", true);
        JSONObject floatObject = new JSONObject()
                .put("type", "float")
                .put("value", 1.23);
        JSONObject dateObject = new JSONObject()
                .put("type", "date")
                .put("value", date.getTime());

        JSONObject attributesJson = new JSONObject()
                .put("stringKey", stringObject)
                .put("integerKey", integerObject)
                .put("booleanKey", booleanObject)
                .put("floatKey", floatObject)
                .put("dateKey", dateObject);

        return new JSONObject()
                .put("attributes", attributesJson)
                .put("mergeRule", AttributeMap.RULE_UPDATE);
    }
}
