package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import android.location.Location
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.EngageBySailthru
import com.marigold.sdk.Marigold
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.model.Message
import com.marigold.sdk.model.Purchase
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.net.URI
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>

    @Mock
    private lateinit var staticEngageBySailthru: MockedConstruction<EngageBySailthru>

    @Mock
    private lateinit var staticMessageStream: MockedConstruction<MessageStream>

    private lateinit var marigold: Marigold
    private lateinit var engage: EngageBySailthru
    private lateinit var messageStream: MessageStream

    @Captor
    private lateinit var runnableCaptor: ArgumentCaptor<Runnable>
    @Captor
    private lateinit var attributeCaptor: ArgumentCaptor<AttributeMap>

    private lateinit var rnMarigoldModule: RNMarigoldModule
    private lateinit var rnMarigoldModuleSpy: RNMarigoldModule

    @Before
    fun setup() {
        rnMarigoldModule = RNMarigoldModule(mockContext, true)
        rnMarigoldModule.jsonConverter = jsonConverter
        rnMarigoldModuleSpy = spy(rnMarigoldModule)

        marigold = staticMarigold.constructed()[0]
        engage = staticEngageBySailthru.constructed()[0]
        messageStream = staticMessageStream.constructed()[0]
    }

    @Test
    fun testConstructor() {
        verify(messageStream).setOnInAppNotificationDisplayListener(rnMarigoldModule)
        val mockCompanion: RNMarigoldModule.Companion = mock()
        mockCompanion.setWrapperInfo()
        verify(mockCompanion).setWrapperInfo()
    }

    @Test
    fun testShouldPresentInAppNotification() {
        val message: Message = mock()
        val module: DeviceEventManagerModule.RCTDeviceEventEmitter = mock()
        val writableMap: WritableMap = mock()
        val jsonObject: JSONObject = mock()

        `when`(message.toJSON()).thenReturn(jsonObject)
        `when`(jsonConverter.convertJsonToMap(jsonObject)).thenReturn(writableMap)
        `when`(mockContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)).thenReturn(module)

        val shouldPresent = rnMarigoldModuleSpy.shouldPresentInAppNotification(message)

        verify(mockContext).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        verify(module).emit("inappnotification", writableMap)

        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testShouldPresentInAppNotificationException() {
        val message: Message = mock()
        val jsonObject: JSONObject = mock()
        val jsonException: JSONException = mock()
        `when`(message.toJSON()).thenReturn(jsonObject)
        `when`(jsonConverter.convertJsonToMap(jsonObject)).thenThrow(jsonException)
        val shouldPresent = rnMarigoldModuleSpy.shouldPresentInAppNotification(message)
        verify(mockContext, times(0)).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        verify(jsonException).printStackTrace()
        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testStartEngine() {
        val testKey = "TEST KEY"
        rnMarigoldModule.startEngine(testKey)
        verify(mockContext).runOnUiQueueThread(capture(runnableCaptor))
        runnableCaptor.value.run()
        verify(marigold).startEngine(mockContext, testKey)
    }

    @Test
    fun testUpdateLocation() {
        val latitude = 10.0
        val longitude = 10.0
        mockConstruction(Location::class.java).use { staticLocation ->
            rnMarigoldModule.updateLocation(latitude, longitude)
            val location = staticLocation.constructed()[0]
            verify(location).latitude = latitude
            verify(location).longitude = longitude
            verify(marigold).updateLocation(location)
        }
    }

    @Test
    fun testRegisterForPushNotifications() {
        val activity: Activity = mock()

        // Mock behaviour
        doReturn(activity).`when`(rnMarigoldModuleSpy).currentActivity()
        rnMarigoldModuleSpy.registerForPushNotifications()
        verify(marigold).requestNotificationPermission(activity)
    }

    @Test
    fun testRegisterForPushNotificationsNoActivity() {
        // Mock behaviour
        doReturn(null).`when`(rnMarigoldModuleSpy).currentActivity()
        rnMarigoldModuleSpy.registerForPushNotifications()
        verify(marigold, never()).requestNotificationPermission(any(), any(), any())
    }

    @Test
    fun testSyncNotificationSettings() {
        rnMarigoldModule.syncNotificationSettings()
        verify(marigold).syncNotificationSettings()
    }

    @Test
    fun testGetDeviceID() {
        // Setup variables
        val deviceID = "device ID"
        val errorMessage = "error message"
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        doReturn(errorMessage).`when`(error).message

        // Start test
        rnMarigoldModule.getDeviceID(promise)

        // Capture handler for verification
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).getDeviceId(capture(argumentCaptor) as Marigold.MarigoldHandler<String?>?)
        val marigoldHandler = argumentCaptor.value as Marigold.MarigoldHandler<String>

        // Test success
        marigoldHandler.onSuccess(deviceID)
        verify(promise).resolve(deviceID)

        // Test failure
        marigoldHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testLogEvent() {
        val event = "event string"
        rnMarigoldModule.logEvent(event)
        verify(engage).logEvent(event)
    }

    @Test
    @Throws(Exception::class)
    fun testLogEventWithVars() {
        val event = "event string"
        val varsJson = JSONObject()
        varsJson.put("varKey", "varValue")

        // setup mocks
        val readableMap: ReadableMap = mock()

        // setup mocking
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(varsJson)
        rnMarigoldModuleSpy.logEvent(event, readableMap)
        verify(engage).logEvent(event, varsJson)
    }

    @Test
    @Throws(Exception::class)
    fun testLogEventWithVarsException() {
        val event = "event string"
        val jsonException: JSONException = mock()

        // setup mocks
        val readableMap: ReadableMap = mock()

        // setup mocking
        `when`(jsonConverter.convertMapToJson(readableMap)).thenThrow(jsonException)
        rnMarigoldModuleSpy.logEvent(event, readableMap)
        verify(jsonException).printStackTrace()
        verify(engage).logEvent(event, null)
    }

    @Test
    @Throws(Exception::class)
    fun testSetAttributes() {
        val stringAttributeJson = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val intAttributeJson = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val attributesJson = JSONObject()
                .put("string key", stringAttributeJson)
                .put("int key", intAttributeJson)
        val attributeMapJson = JSONObject()
                .put("mergeRule", AttributeMap.RULE_UPDATE)
                .put("attributes", attributesJson)
        val error = Error("test error")

        // setup mocks
        val promise: Promise = mock()
        val readableMap: ReadableMap = mock()

        // setup mocking for conversion from ReadableMap to JSON
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeMapJson)

        // Capture Attributes to verify
        val handlerCaptor = ArgumentCaptor.forClass(EngageBySailthru.AttributesHandler::class.java)

        // Initiate test
        rnMarigoldModuleSpy.setAttributes(readableMap, promise)

        // Verify results
        verify(engage).setAttributes(capture(attributeCaptor), capture(handlerCaptor))
        val attributes: AttributeMap = attributeCaptor.value
        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributes.getMergeRules())
        Assert.assertEquals("test string", attributes.getString("string key"))
        Assert.assertEquals(123, attributes.getInt("int key", 0))
        val handler: EngageBySailthru.AttributesHandler = handlerCaptor.value
        handler.onSuccess()
        verify(promise).resolve(null)
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
    }

    @Test
    fun testGetMessages() {
        // Setup mocks
        val promise: Promise = mock()
        val writableArray: WritableArray = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModuleSpy.getMessages(promise)

        // Capture MessagesHandler to verify behaviour
        val argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler::class.java)
        verify(messageStream).getMessages(capture(argumentCaptor))
        val messagesHandler = argumentCaptor.value

        // Replace native array with mock
        doReturn(writableArray).`when`(rnMarigoldModuleSpy).getWritableArray()

        // Setup message array
        val messages: ArrayList<Message> = ArrayList()

        // Test success handler
        messagesHandler.onSuccess(messages)
        verify(promise).resolve(writableArray)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        messagesHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    fun testSetUserId() {
        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val userID = "user ID"
        rnMarigoldModule.setUserId(userID, promise)

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setUserId(eq(userID), capture(argumentCaptor) as Marigold.MarigoldHandler<Void?>?)
        val handler = argumentCaptor.value as Marigold.MarigoldHandler<Void?>

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testSetUserEmail() {
        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val userEmail = "user email"
        rnMarigoldModule.setUserEmail(userEmail, promise)

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setUserEmail(eq(userEmail), capture(argumentCaptor) as Marigold.MarigoldHandler<Void?>?)
        val handler = argumentCaptor.value as Marigold.MarigoldHandler<Void?>

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testGetUnreadCount() {
        val unreadCount = 4

        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModule.getUnreadCount(promise)

        // Capture MessagesHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageStreamHandler::class.java)
        verify(messageStream).getUnreadMessageCount(capture(argumentCaptor) as MessageStream.MessageStreamHandler<Int>?)
        val countHandler = argumentCaptor.value as MessageStream.MessageStreamHandler<Int>

        // Test success handler
        countHandler.onSuccess(unreadCount)
        verify(promise).resolve(unreadCount)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        countHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveMessage() {
        // Create mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val readableMap: ReadableMap = mock()
        val message: Message = mock()
        val moduleSpy = spy(rnMarigoldModule)
        doReturn(message).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise)

        // Capture MarigoldHandler to verify behaviour
        val argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageDeletedHandler::class.java)
        verify(messageStream).deleteMessage(eq(message), capture(argumentCaptor))
        val handler = argumentCaptor.value

        // Test success handler
        handler.onSuccess()
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveMessageException() {
        // Create mocks
        val promise: Promise = mock()
        val readableMap: ReadableMap = mock()
        val jsonException = JSONException("test exception")
        val moduleSpy = spy(rnMarigoldModule)
        doThrow(jsonException).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        verify(messageStream, times(0)).deleteMessage(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterMessageImpression() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock()
        val message: Message = mock()
        doReturn(message).`when`(rnMarigoldModuleSpy).getMessage(readableMap)

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(messageStream).registerMessageImpression(ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message)
    }

    @Test
    fun testRegisterMessageImpressionInvalidCode() {
        // Create input
        val typeCode = 10
        val readableMap: ReadableMap = mock()

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(messageStream, times(0)).registerMessageImpression(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterMessageImpressionException() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock()
        val jsonException: JSONException = mock()
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getMessage(readableMap)

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(jsonException).printStackTrace()
        verify(messageStream, times(0)).registerMessageImpression(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testMarkMessageAsRead() {
        // Create mocks
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val message: Message = mock()
        val error: Error = mock()
        val moduleSpy = spy(rnMarigoldModule)
        doReturn(message).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise)

        // Capture MarigoldHandler to verify behaviour
        val argumentCaptor: ArgumentCaptor<MessageStream.MessagesReadHandler> = ArgumentCaptor.forClass(MessageStream.MessagesReadHandler::class.java)
        verify(messageStream).setMessageRead(eq(message), capture(argumentCaptor))
        val handler: MessageStream.MessagesReadHandler = argumentCaptor.value

        // Test success handler
        handler.onSuccess()
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testMarkMessageAsReadException() {
        // Create mocks
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")
        val moduleSpy = spy(rnMarigoldModule)
        doThrow(jsonException).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        verify(messageStream, times(0)).setMessageRead(any(), any())
    }

    @Test
    fun testPresentMessageDetail() {
        // Setup input
        val messageID = "message ID"

        // Setup mocks
        val message: ReadableMap = mock()
        val activity: Activity = mock()
        val intent: Intent = mock()

        // Mock behaviour
        `when`(message.getString(RNMarigoldModule.MESSAGE_ID)).thenReturn(messageID)
        doReturn(activity).`when`(rnMarigoldModuleSpy).currentActivity()
        doReturn(intent).`when`(rnMarigoldModuleSpy).getMessageActivityIntent(any(), any())

        // Initiate test
        rnMarigoldModuleSpy.presentMessageDetail(message)

        // Verify result
        verify(activity).startActivity(intent)
    }

//    @Test
//    fun testGetRecommendations() {
//        // Setup mocks
//        val promise: Promise = mock(Promise::class.java)
//        val writableArray: WritableArray = mock(WritableArray::class.java)
//        val error: Error = mock(Error::class.java)
//        val sectionID = "Section ID"
//
//        // Initiate test
//        rnMarigoldModuleSpy.getRecommendations(sectionID, promise)
//
//        // Capture MessagesHandler to verify behaviour
//        val argumentCaptor: ArgumentCaptor<Marigold.RecommendationsHandler> = ArgumentCaptor.forClass(Marigold.RecommendationsHandler::class.java)
//        verify(marigold).getRecommendations(eq(sectionID), argumentCaptor.capture())
//        val recommendationsHandler: Marigold.RecommendationsHandler = argumentCaptor.getValue()
//
//        // Replace native array with mock
//        doReturn(writableArray).`when`(rnMarigoldModuleSpy).getWritableArray()
//
//        // Setup message array
//        val contentItems: ArrayList<ContentItem> = ArrayList()
//
//        // Test success handler
//        recommendationsHandler.onSuccess(contentItems)
//        verify(promise).resolve(writableArray)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.getMessage()).thenReturn(errorMessage)
//
//        // Test error handler
//        recommendationsHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_RECOMMENDATIONS, errorMessage)
//    }

    @Test
    fun testTrackClick() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val error = Error("test error")

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackClick(eq(sectionID), capture(uriCaptor), capture(handlerCaptor))
        val uri: URI = uriCaptor.value
        val trackHandler: EngageBySailthru.TrackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uri.toString())
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackClickException() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackClick(any(), any(), any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun testTrackPageview() {
        // Create input
        val urlString = "www.notarealurl.com"
        val testTag = "some tag"
        val error = Error("test error")

        // Create mocks
        val promise: Promise = mock()
        val tagsArray: ReadableArray = mock()
        `when`(tagsArray.size()).thenReturn(1)
        `when`(tagsArray.getString(anyInt())).thenReturn(testTag)

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, tagsArray, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val arrayCaptor: ArgumentCaptor<List<*>>? = ArgumentCaptor.forClass(List::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackPageview(capture(uriCaptor), capture(arrayCaptor as ArgumentCaptor<List<String>>), capture(handlerCaptor))
        val uri = uriCaptor.value
        val tags = arrayCaptor.value
        val trackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uri.toString())
        Assert.assertEquals(testTag, tags[0])
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackPageviewNullTags() {
        // Create input
        val promise: Promise = mock()
        val urlString = "www.notarealurl.com"

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise)

        // Verify result
        verify(engage).trackPageview(any(), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackPageviewException() {
        // Create input
        val promise: Promise = mock()
        val urlString = "Wrong URL Format"

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackPageview(any(), ArgumentMatchers.anyList(), any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun testTrackImpression() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val readableArray: ReadableArray = mock()
        val error = Error("test error")

        // Mock methods
        doReturn(1).`when`(readableArray).size()
        doReturn(urlString).`when`(readableArray).getString(any())

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, readableArray, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<List<*>>? = ArgumentCaptor.forClass(List::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), capture(uriCaptor as ArgumentCaptor<List<URI>>), capture(handlerCaptor))
        val uriList = uriCaptor.value
        val trackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uriList[0].toString())
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackImpressionNullUrls() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, null, promise)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackImpressionException() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"
        val readableArray: ReadableArray = mock()

        // Mock methods
        doReturn(1).`when`(readableArray).size()
        doReturn(urlString).`when`(readableArray).getString(any())

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, readableArray, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackImpression(any(), any(), any())
    }

    @Test
    fun testSetGeoIPTrackingEnabled() {
        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(true)

        // Verify result
        verify(marigold).setGeoIpTrackingEnabled(true)
    }

    @Test
    fun testSeGeoIPTrackingEnabledWithPromise() {
        // Create input
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(false, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).setGeoIpTrackingEnabled(eq(false), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val clearHandler = argumentCaptor.value

        // Test success handler
        clearHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        clearHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testClearDevice() {
        // Create input
        val clearValue = Marigold.CLEAR_ALL
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModule.clearDevice(clearValue, promise)

        // Verify result
        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).clearDevice(eq(clearValue), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val clearHandler = argumentCaptor.value

        // Test success handler
        clearHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        clearHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testSetProfileVars() {
        val varsJson: JSONObject = JSONObject().put("test var", 123)

        // Create input
        val vars: ReadableMap = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        `when`(jsonConverter.convertMapToJson(vars)).thenReturn(varsJson)

        // Initiate test
        rnMarigoldModuleSpy.setProfileVars(vars, promise)

        // Verify result
        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setProfileVars(eq(varsJson), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val setVarsHandler = argumentCaptor.value

        // Test success handler
        setVarsHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        setVarsHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @kotlin.Throws(Exception::class)
    fun testSetProfileVarsException() {
        val jsonException = JSONException("test exception")

        // Create input
        val vars: ReadableMap = mock()
        val promise: Promise = mock()

        // Mock methods
        `when`(jsonConverter.convertMapToJson(vars)).thenThrow(jsonException)

        // Initiate test
        rnMarigoldModuleSpy.setProfileVars(vars, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, jsonException.message)
        verify(engage, times(0)).setProfileVars(any(), any())
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetProfileVars() {
        // Create input
        val varsJson = JSONObject().put("test var", 123)
        val promise: Promise = mock()
        val error: Error = mock()
        val mockMap: WritableMap = mock()

        // Mock methods
        `when`(jsonConverter.convertJsonToMap(varsJson)).thenReturn(mockMap)

        // Initiate test
        rnMarigoldModuleSpy.getProfileVars(promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).getProfileVars(capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<JSONObject?>>))
        val getVarsHandler = spy(argumentCaptor.value)

        // Test success handler
        getVarsHandler.onSuccess(varsJson)
        verify(jsonConverter).convertJsonToMap(varsJson)
        verify(promise).resolve(mockMap)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        getVarsHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testLogPurchase() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val purchase: Purchase = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        doReturn(purchase).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).logPurchase(eq(purchase), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val purchaseHandler = argumentCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        purchaseHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @Throws(Exception::class)
    fun testLogPurchaseException() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        verify(engage, times(0)).logPurchase(any(), any())
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testLogAbandonedCart() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val purchase: Purchase = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        doReturn(purchase).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).logAbandonedCart(eq(purchase), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val purchaseHandler = argumentCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        purchaseHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @Throws(Exception::class)
    fun testLogAbandonedCartException() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        verify(engage, times(0)).logAbandonedCart(any(), any() as Marigold.MarigoldHandler<Void?>?)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPurchaseInstancePositiveAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val purchaseJson = createPurchaseJson(234)
        `when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase: Purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item = purchase.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(234, adjustment.price)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPurchaseInstanceNegativeAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val purchaseJson = createPurchaseJson(-234)
        `when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item = purchase.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(-234, adjustment.price)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMessage() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val messageJson = JSONObject().put("title", "test title")
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(messageJson)

        // Initiate test
        val message = rnMarigoldModuleSpy.getMessage(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals("test title", message.title)
    }

    @Test
    @Throws(Exception::class)
    fun testGetAttributeMap() {
        val date = Date()

        // Mock methods
        val readableMap: ReadableMap = mock()
        val attributeJson = createAttributeMapJson(date)
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeJson)

        // Initiate test
        val attributeMap = rnMarigoldModuleSpy.getAttributeMap(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributeMap.getMergeRules())
        Assert.assertEquals("test string", attributeMap.getString("stringKey"))
        Assert.assertEquals(123, attributeMap.getInt("integerKey", 0))
        Assert.assertTrue(attributeMap.getBoolean("booleanKey", false))
        Assert.assertEquals(1.23F, attributeMap.getFloat("floatKey", 0F), 0.001F)
        Assert.assertEquals(date, attributeMap.getDate("dateKey"))
    }

    /** Helpers  */
    @Throws(Exception::class)
    private fun createPurchaseJson(adjustmentPrice: Int): JSONObject {
        val adjustmentJson = JSONObject()
                .put("title", "tax")
                .put("price", adjustmentPrice)
        val adjustmentsArray = JSONArray()
                .put(adjustmentJson)
        val itemJson: JSONObject = JSONObject()
                .put("qty", 1)
                .put("title", "test title")
                .put("price", 123)
                .put("id", "456")
                .put("url", "http://mobile.sailthru.com")
        val itemsArray = JSONArray()
                .put(itemJson)
        return JSONObject()
                .put("items", itemsArray)
                .put("adjustments", adjustmentsArray)
    }

    @Throws(Exception::class)
    private fun createAttributeMapJson(date: Date): JSONObject {
        val stringObject = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val integerObject = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val booleanObject = JSONObject()
                .put("type", "boolean")
                .put("value", true)
        val floatObject = JSONObject()
                .put("type", "float")
                .put("value", 1.23)
        val dateObject = JSONObject()
                .put("type", "date")
                .put("value", date.time)
        val attributesJson = JSONObject()
                .put("stringKey", stringObject)
                .put("integerKey", integerObject)
                .put("booleanKey", booleanObject)
                .put("floatKey", floatObject)
                .put("dateKey", dateObject)
        return JSONObject()
                .put("attributes", attributesJson)
                .put("mergeRule", AttributeMap.RULE_UPDATE)
    }
}
