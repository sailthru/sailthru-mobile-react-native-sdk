package com.marigold.rnsdk

//import com.marigold.sdk.model.ContentItem
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
import com.marigold.sdk.model.PurchaseAdjustment
import com.marigold.sdk.model.PurchaseItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.net.URI
import java.util.Date
import java.util.List

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
//        rnMarigoldModule = RNMarigoldModule(mockContext, true)
//        rnMarigoldModule.jsonConverter = jsonConverter
//        rnMarigoldModuleSpy = spy(rnMarigoldModule)
//
//        marigold = staticMarigold.constructed()[0]
//        engage = staticEngageBySailthru.constructed()[0]
//        messageStream = staticMessageStream.constructed()[0]
    }

    @Test
    fun testConstructor() {
        verify(messageStream).setOnInAppNotificationDisplayListener(rnMarigoldModule)
        val mockCompanion = mock(RNMarigoldModule.Companion::class.java)
        mockCompanion.setWrapperInfo()
        verify(mockCompanion).setWrapperInfo()
    }

    @Test
    fun testShouldPresentInAppNotification() {
        val message = mock(Message::class.java)
        val module = mock(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        val writableMap = mock(WritableMap::class.java)
        val jsonObject = mock(JSONObject::class.java)

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
        val message: Message = mock(Message::class.java)
        val jsonObject: JSONObject = mock(JSONObject::class.java)
        val jsonException: JSONException = mock(JSONException::class.java)
        `when`(message.toJSON()).thenReturn(jsonObject)
        `when`(jsonConverter.convertJsonToMap(jsonObject)).thenThrow(jsonException)
        val shouldPresent: Boolean = rnMarigoldModuleSpy.shouldPresentInAppNotification(message)
        verify(mockContext, times(0)).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        verify(jsonException).printStackTrace()
        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testStartEngine() {
        val testKey = "TEST KEY"
        rnMarigoldModule.startEngine(testKey)
        verify(mockContext).runOnUiQueueThread(runnableCaptor.capture())
        runnableCaptor.value.run()
        verify(marigold).startEngine(mockContext, testKey)
    }

    @Test
    fun testUpdateLocation() {
        val latitude = 10.0
        val longitude = 10.0
        mockConstruction(Location::class.java).use { staticLocation ->
            rnMarigoldModule.updateLocation(latitude, longitude)
            val location: Location = staticLocation.constructed().get(0)
            verify(location).setLatitude(latitude)
            verify(location).setLongitude(longitude)
            verify(marigold).updateLocation(location)
        }
    }

    @Test
    fun testRegisterForPushNotifications() {
        val activity: Activity = mock(Activity::class.java)

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
        verify(marigold, never()).requestNotificationPermission(anyOrNull(), anyOrNull(), anyInt())
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
        val promise: Promise = mock(Promise::class.java)
        val error: Error = mock(Error::class.java)

        // Mock methods
        doReturn(errorMessage).`when`(error).message

        // Start test
        rnMarigoldModule.getDeviceID(promise)

        // Capture handler for verification
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).getDeviceId(argumentCaptor.capture() as Marigold.MarigoldHandler<String?>?)
        val marigoldHandler: Marigold.MarigoldHandler<String> = argumentCaptor.value as Marigold.MarigoldHandler<String>

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
    @kotlin.Throws(Exception::class)
    fun testLogEventWithVars() {
        val event = "event string"
        val varsJson = JSONObject()
        varsJson.put("varKey", "varValue")

        // setup mocks
        val readableMap: ReadableMap = mock(ReadableMap::class.java)

        // setup mocking
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(varsJson)
        rnMarigoldModuleSpy.logEvent(event, readableMap)
        verify(engage).logEvent(event, varsJson)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testLogEventWithVarsException() {
        val event = "event string"
        val jsonException: JSONException = mock(JSONException::class.java)

        // setup mocks
        val readableMap: ReadableMap = mock(ReadableMap::class.java)

        // setup mocking
        `when`(jsonConverter.convertMapToJson(readableMap)).thenThrow(jsonException)
        rnMarigoldModuleSpy.logEvent(event, readableMap)
        verify(jsonException).printStackTrace()
        verify(engage).logEvent(event, null)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testSetAttributes() {
        val stringAttributeJson: JSONObject = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val intAttributeJson: JSONObject = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val attributesJson: JSONObject = JSONObject()
                .put("string key", stringAttributeJson)
                .put("int key", intAttributeJson)
        val attributeMapJson: JSONObject = JSONObject()
                .put("mergeRule", AttributeMap.RULE_UPDATE)
                .put("attributes", attributesJson)
        val error = Error("test error")

        // setup mocks
        val promise: Promise = mock(Promise::class.java)
        val readableMap: ReadableMap = mock(ReadableMap::class.java)

        // setup mocking for conversion from ReadableMap to JSON
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeMapJson)

        // Capture Attributes to verify
        val attributeCaptor: ArgumentCaptor<AttributeMap> = ArgumentCaptor.forClass(AttributeMap::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.AttributesHandler> = ArgumentCaptor.forClass(EngageBySailthru.AttributesHandler::class.java)

        // Initiate test
        rnMarigoldModuleSpy.setAttributes(readableMap, promise)

        // Verify results
        verify(engage).setAttributes(attributeCaptor.capture(), handlerCaptor.capture())
        val attributes: AttributeMap = attributeCaptor.value
        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributes.getMergeRules())
        Assert.assertEquals("test string", attributes.getString("string key"))
        Assert.assertEquals(123, attributes.getInt("int key", 0))
        val handler: EngageBySailthru.AttributesHandler = handlerCaptor.getValue()
        handler.onSuccess()
        verify(promise).resolve(null)
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
    }

    @Test
    fun testGetMessages() {
        // Setup mocks
        val promise: Promise = mock(Promise::class.java)
        val writableArray: WritableArray = mock(WritableArray::class.java)
        val error: Error = mock(Error::class.java)

        // Initiate test
        rnMarigoldModuleSpy.getMessages(promise)

        // Capture MessagesHandler to verify behaviour
        val argumentCaptor: ArgumentCaptor<MessageStream.MessagesHandler> = ArgumentCaptor.forClass(MessageStream.MessagesHandler::class.java)
        verify(messageStream).getMessages(argumentCaptor.capture())
        val messagesHandler: MessageStream.MessagesHandler = argumentCaptor.getValue()

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
        val promise: Promise = mock(Promise::class.java)
        val error: Error = mock(Error::class.java)
        val userID = "user ID"
        rnMarigoldModule.setUserId(userID, promise)

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setUserId(eq(userID), argumentCaptor.capture() as Marigold.MarigoldHandler<Void?>?)
        val handler: Marigold.MarigoldHandler<Void> = argumentCaptor.value as Marigold.MarigoldHandler<Void>

        // Test success handler
        //handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        `when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

//    @Test
//    fun testSetUserEmail() {
//        // Setup mocks
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//        val userEmail = "user email"
//        rnMarigoldModule.setUserEmail(userEmail, promise)
//
//        // Capture MarigoldHandler to verify behaviour
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(engage).setUserEmail(eq(userEmail), argumentCaptor.capture())
//        val handler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        //handler.onSuccess(null)
//        verify(promise).resolve(null)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        handler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
//    }

//    @Test
//    fun testGetUnreadCount() {
//        val unreadCount: Int = 4
//
//        // Setup mocks
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Initiate test
//        rnMarigoldModule.getUnreadCount(promise)
//
//        // Capture MessagesHandler to verify behaviour
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<MessageStream.MessageStreamHandler<Int>> = ArgumentCaptor.forClass(MessageStream.MessageStreamHandler::class.java)
//        verify(messageStream).getUnreadMessageCount(argumentCaptor.capture())
//        val countHandler: MessageStream.MessageStreamHandler<Int> = argumentCaptor.getValue()
//
//        // Test success handler
//        countHandler.onSuccess(unreadCount)
//        verify(promise).resolve(unreadCount)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        countHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
//    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testRemoveMessage() {
        // Create mocks
        val promise: Promise = mock(Promise::class.java)
        val error: Error = mock(Error::class.java)
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val message: Message = mock(Message::class.java)
        val moduleSpy: RNMarigoldModule = spy(rnMarigoldModule)
        doReturn(message).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise)

        // Capture MarigoldHandler to verify behaviour
        val argumentCaptor: ArgumentCaptor<MessageStream.MessageDeletedHandler> = ArgumentCaptor.forClass(MessageStream.MessageDeletedHandler::class.java)
        verify(messageStream).deleteMessage(eq(message), argumentCaptor.capture())
        val handler: MessageStream.MessageDeletedHandler = argumentCaptor.getValue()

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
    @kotlin.Throws(Exception::class)
    fun testRemoveMessageException() {
        // Create mocks
        val promise: Promise = mock(Promise::class.java)
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val jsonException = JSONException("test exception")
        val moduleSpy: RNMarigoldModule = spy(rnMarigoldModule)
        doThrow(jsonException).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        verify(messageStream, times(0)).deleteMessage(any(Message::class.java), any(MessageStream.MessageDeletedHandler::class.java))
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testRegisterMessageImpression() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val message: Message = mock(Message::class.java)
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
        val readableMap: ReadableMap = mock(ReadableMap::class.java)

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(messageStream, times(0)).registerMessageImpression(any(ImpressionType::class.java), any(Message::class.java))
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testRegisterMessageImpressionException() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val jsonException: JSONException = mock(JSONException::class.java)
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getMessage(readableMap)

        // Initiate test
        rnMarigoldModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(jsonException).printStackTrace()
        verify(messageStream, times(0)).registerMessageImpression(any(ImpressionType::class.java), any(Message::class.java))
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testMarkMessageAsRead() {
        // Create mocks
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val promise: Promise = mock(Promise::class.java)
        val message: Message = mock(Message::class.java)
        val error: Error = mock(Error::class.java)
        val moduleSpy: RNMarigoldModule = spy(rnMarigoldModule)
        doReturn(message).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise)

        // Capture MarigoldHandler to verify behaviour
        val argumentCaptor: ArgumentCaptor<MessageStream.MessagesReadHandler> = ArgumentCaptor.forClass(MessageStream.MessagesReadHandler::class.java)
        verify(messageStream).setMessageRead(eq(message), argumentCaptor.capture())
        val handler: MessageStream.MessagesReadHandler = argumentCaptor.getValue()

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
    @kotlin.Throws(Exception::class)
    fun testMarkMessageAsReadException() {
        // Create mocks
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val promise: Promise = mock(Promise::class.java)
        val jsonException = JSONException("test exception")
        val moduleSpy: RNMarigoldModule = spy(rnMarigoldModule)
        doThrow(jsonException).`when`(moduleSpy).getMessage(readableMap)

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        verify(messageStream, times(0)).setMessageRead(any(Message::class.java), any(MessageStream.MessagesReadHandler::class.java))
    }

    @Test
    fun testPresentMessageDetail() {
        // Setup input
        val messageID = "message ID"

        // Setup mocks
        val message: ReadableMap = mock(ReadableMap::class.java)
        val activity: Activity = mock(Activity::class.java)
        val intent: Intent = mock(Intent::class.java)

        // Mock behaviour
        `when`(message.getString(RNMarigoldModule.MESSAGE_ID)).thenReturn(messageID)
        doReturn(activity).`when`(rnMarigoldModuleSpy).currentActivity()
        doReturn(intent).`when`(rnMarigoldModuleSpy).getMessageActivityIntent(any(Activity::class.java), anyString())

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
        val promise: Promise = mock(Promise::class.java)
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val error = Error("test error")

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackClick(eq(sectionID), uriCaptor.capture(), handlerCaptor.capture())
        val uri: URI = uriCaptor.getValue()
        val trackHandler: EngageBySailthru.TrackHandler = handlerCaptor.getValue()
        Assert.assertEquals(urlString, uri.toString())
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackClickException() {
        // Create input
        val promise: Promise = mock(Promise::class.java)
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"

        // Initiate test
        rnMarigoldModule.trackClick(sectionID, urlString, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackClick(anyString(), any(URI::class.java), any(EngageBySailthru.TrackHandler::class.java))
    }

//    @Test
//    @SuppressWarnings("unchecked")
//    fun testTrackPageview() {
//        // Create input
//        val urlString = "www.notarealurl.com"
//        val testTag = "some tag"
//        val error = Error("test error")
//
//        // Create mocks
//        val promise: Promise = mock(Promise::class.java)
//        val tagsArray: ReadableArray = mock(ReadableArray::class.java)
//        `when`(tagsArray.size()).thenReturn(1)
//        `when`(tagsArray.getString(anyInt())).thenReturn(testTag)
//
//        // Initiate test
//        rnMarigoldModule.trackPageview(urlString, tagsArray, promise)
//
//        // Capture arguments to verify behaviour
//        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
//        val arrayCaptor: ArgumentCaptor<java.util.ArrayList<String>> = ArgumentCaptor.forClass(ArrayList::class.java)
//        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)
//
//        // Verify result
//        verify(engage).trackPageview(uriCaptor.capture(), arrayCaptor.capture(), handlerCaptor.capture())
//        val uri: URI = uriCaptor.getValue()
//        val tags: ArrayList<String> = arrayCaptor.getValue()
//        val trackHandler: EngageBySailthru.TrackHandler = handlerCaptor.getValue()
//        Assert.assertEquals(urlString, uri.toString())
//        Assert.assertEquals(testTag, tags.get(0))
//        trackHandler.onSuccess()
//        verify(promise).resolve(true)
//        trackHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
//    }

    @Test
    fun testTrackPageviewNullTags() {
        // Create input
        val promise: Promise = mock(Promise::class.java)
        val urlString = "www.notarealurl.com"

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise)

        // Verify result
        verify(engage).trackPageview(any(URI::class.java), ArgumentMatchers.isNull(), any(EngageBySailthru.TrackHandler::class.java))
    }

    @Test
    fun testTrackPageviewException() {
        // Create input
        val promise: Promise = mock(Promise::class.java)
        val urlString = "Wrong URL Format"

        // Initiate test
        rnMarigoldModule.trackPageview(urlString, null, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackPageview(any(URI::class.java), ArgumentMatchers.anyList(), any(EngageBySailthru.TrackHandler::class.java))
    }

//    @Test
//    @SuppressWarnings("unchecked")
//    fun testTrackImpression() {
//        // Create input
//        val promise: Promise = mock(Promise::class.java)
//        val sectionID = "Section ID"
//        val urlString = "www.notarealurl.com"
//        val readableArray: ReadableArray = mock(ReadableArray::class.java)
//        val error = Error("test error")
//
//        // Mock methods
//        doReturn(1).`when`(readableArray).size()
//        doReturn(urlString).`when`(readableArray).getString(anyInt())
//
//        // Initiate test
//        rnMarigoldModule.trackImpression(sectionID, readableArray, promise)
//
//        // Capture arguments to verify behaviour
//        val uriCaptor: ArgumentCaptor<List<URI>> = ArgumentCaptor.forClass(List::class.java)
//        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)
//
//        // Verify result
//        verify(engage).trackImpression(eq(sectionID), uriCaptor.capture(), handlerCaptor.capture())
//        val uriList: List<URI> = uriCaptor.getValue()
//        val trackHandler: EngageBySailthru.TrackHandler = handlerCaptor.getValue()
//        Assert.assertEquals(urlString, uriList[0].toString())
//        trackHandler.onSuccess()
//        verify(promise).resolve(true)
//        trackHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
//    }

    @Test
    fun testTrackImpressionNullUrls() {
        // Create input
        val promise: Promise = mock(Promise::class.java)
        val sectionID = "Section ID"

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, null, promise)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), ArgumentMatchers.isNull(), any(EngageBySailthru.TrackHandler::class.java))
    }

    @Test
    fun testTrackImpressionException() {
        // Create input
        val promise: Promise = mock(Promise::class.java)
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"
        val readableArray: ReadableArray = mock(ReadableArray::class.java)

        // Mock methods
        doReturn(1).`when`(readableArray).size()
        doReturn(urlString).`when`(readableArray).getString(anyInt())

        // Initiate test
        rnMarigoldModule.trackImpression(sectionID, readableArray, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), anyString())
        verify(engage, times(0)).trackImpression(anyString(), ArgumentMatchers.anyList(), any(EngageBySailthru.TrackHandler::class.java))
    }

    @Test
    fun testSetGeoIPTrackingEnabled() {
        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(true)

        // Verify result
        verify(marigold).setGeoIpTrackingEnabled(true)
    }

//    @Test
//    fun testSeGeoIPTrackingEnabledWithPromise() {
//        // Create input
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Initiate test
//        rnMarigoldModule.setGeoIPTrackingEnabled(false, promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(marigold).setGeoIpTrackingEnabled(eq(false), argumentCaptor.capture())
//        val clearHandler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        clearHandler.onSuccess(null)
//        verify(promise).resolve(true)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        clearHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
//    }

//    @Test
//    fun testClearDevice() {
//        // Create input
//        val clearValue: Int = Marigold.CLEAR_ALL
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Initiate test
//        rnMarigoldModule.clearDevice(clearValue, promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(marigold).clearDevice(eq(clearValue), argumentCaptor.capture())
//        val clearHandler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        clearHandler.onSuccess(null)
//        verify(promise).resolve(true)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        clearHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
//    }

//    @Test
//    @kotlin.Throws(Exception::class)
//    fun testSetProfileVars() {
//        val varsJson: JSONObject = JSONObject().put("test var", 123)
//
//        // Create input
//        val vars: ReadableMap = mock(ReadableMap::class.java)
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Mock methods
//        `when`(jsonConverter.convertMapToJson(vars)).thenReturn(varsJson)
//
//        // Initiate test
//        rnMarigoldModuleSpy.setProfileVars(vars, promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(engage).setProfileVars(eq(varsJson), argumentCaptor.capture())
//        val setVarsHandler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        setVarsHandler.onSuccess(null)
//        verify(promise).resolve(true)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        setVarsHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
//    }

//    @Test
//    @SuppressWarnings("unchecked")
//    @kotlin.Throws(Exception::class)
//    fun testSetProfileVarsException() {
//        val jsonException = JSONException("test exception")
//
//        // Create input
//        val vars: ReadableMap = mock(ReadableMap::class.java)
//        val promise: Promise = mock(Promise::class.java)
//
//        // Mock methods
//        `when`(jsonConverter.convertMapToJson(vars)).thenThrow(jsonException)
//
//        // Initiate test
//        rnMarigoldModuleSpy.setProfileVars(vars, promise)
//
//        // Verify result
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, jsonException.message)
//        verify(engage, times(0)).setProfileVars(any(JSONObject::class.java), any(Marigold.MarigoldHandler::class.java))
//    }

//    @Test
//    @kotlin.Throws(Exception::class)
//    fun testGetProfileVars() {
//        // Create input
//        val varsJson: JSONObject = JSONObject().put("test var", 123)
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//        val mockMap: WritableMap = mock(WritableMap::class.java)
//
//        // Mock methods
//        `when`(jsonConverter.convertJsonToMap(varsJson)).thenReturn(mockMap)
//
//        // Initiate test
//        rnMarigoldModuleSpy.getProfileVars(promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<JSONObject>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(engage).getProfileVars(argumentCaptor.capture())
//        val getVarsHandler: Marigold.MarigoldHandler<JSONObject> = spy(argumentCaptor.getValue())
//
//        // Test success handler
//        getVarsHandler.onSuccess(varsJson)
//        verify(jsonConverter).convertJsonToMap(varsJson)
//        verify(promise).resolve(mockMap)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        getVarsHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
//    }

//    @Test
//    @kotlin.Throws(Exception::class)
//    fun testLogPurchase() {
//        // Create input
//        val purchaseMap: ReadableMap = mock(ReadableMap::class.java)
//        val purchase: Purchase = mock(Purchase::class.java)
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Mock methods
//        doReturn(purchase).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)
//
//        // Initiate test
//        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void>> = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(engage).logPurchase(eq(purchase), argumentCaptor.capture())
//        val purchaseHandler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        purchaseHandler.onSuccess(null)
//        verify(promise).resolve(true)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        purchaseHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
//    }

    @Test
    @SuppressWarnings("unchecked")
    @kotlin.Throws(Exception::class)
    fun testLogPurchaseException() {
        // Create input
        val purchaseMap: ReadableMap = mock(ReadableMap::class.java)
        val promise: Promise = mock(Promise::class.java)
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logPurchase(purchaseMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        verify(engage, times(0)).logPurchase(any(Purchase::class.java), any(Marigold.MarigoldHandler::class.java) as Marigold.MarigoldHandler<Void?>?)
    }

//    @Test
//    @kotlin.Throws(Exception::class)
//    fun testLogAbandonedCart() {
//        // Create input
//        val purchaseMap: ReadableMap = mock(ReadableMap::class.java)
//        val purchase: Purchase = mock(Purchase::class.java)
//        val promise: Promise = mock(Promise::class.java)
//        val error: Error = mock(Error::class.java)
//
//        // Mock methods
//        doReturn(purchase).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)
//
//        // Initiate test
//        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise)
//
//        // Verify result
//        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
//        verify(engage).logAbandonedCart(eq(purchase), argumentCaptor.capture())
//        val purchaseHandler: Marigold.MarigoldHandler<Void> = argumentCaptor.getValue()
//
//        // Test success handler
//        purchaseHandler.onSuccess(null)
//        verify(promise).resolve(true)
//
//        // Setup error
//        val errorMessage = "error message"
//        `when`(error.message).thenReturn(errorMessage)
//
//        // Test error handler
//        purchaseHandler.onFailure(error)
//        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
//    }

    @Test
    @SuppressWarnings("unchecked")
    @kotlin.Throws(Exception::class)
    fun testLogAbandonedCartException() {
        // Create input
        val purchaseMap: ReadableMap = mock(ReadableMap::class.java)
        val promise: Promise = mock(Promise::class.java)
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).`when`(rnMarigoldModuleSpy).getPurchaseInstance(purchaseMap)

        // Initiate test
        rnMarigoldModuleSpy.logAbandonedCart(purchaseMap, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        verify(engage, times(0)).logAbandonedCart(any(Purchase::class.java), any(Marigold.MarigoldHandler::class.java) as Marigold.MarigoldHandler<Void?>?)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetPurchaseInstancePositiveAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val purchaseJson: JSONObject = createPurchaseJson(234)
        `when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase: Purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item: PurchaseItem = purchase.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment: PurchaseAdjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(234, adjustment.price)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetPurchaseInstanceNegativeAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val purchaseJson: JSONObject = createPurchaseJson(-234)
        `when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase: Purchase = rnMarigoldModuleSpy.getPurchaseInstance(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item: PurchaseItem = purchase.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment: PurchaseAdjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(-234, adjustment.price)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetMessage() {
        // Mock methods
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val messageJson: JSONObject = JSONObject().put("title", "test title")
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(messageJson)

        // Initiate test
        val message: Message = rnMarigoldModuleSpy.getMessage(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals("test title", message.title)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetAttributeMap() {
        val date = Date()

        // Mock methods
        val readableMap: ReadableMap = mock(ReadableMap::class.java)
        val attributeJson: JSONObject = createAttributeMapJson(date)
        `when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeJson)

        // Initiate test
        val attributeMap: AttributeMap = rnMarigoldModuleSpy.getAttributeMap(readableMap)

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
    @kotlin.Throws(Exception::class)
    private fun createPurchaseJson(adjustmentPrice: Int): JSONObject {
        val adjustmentJson: JSONObject = JSONObject()
                .put("title", "tax")
                .put("price", adjustmentPrice)
        val adjustmentsArray: JSONArray = JSONArray()
                .put(adjustmentJson)
        val itemJson: JSONObject = JSONObject()
                .put("qty", 1)
                .put("title", "test title")
                .put("price", 123)
                .put("id", "456")
                .put("url", "http://mobile.sailthru.com")
        val itemsArray: JSONArray = JSONArray()
                .put(itemJson)
        return JSONObject()
                .put("items", itemsArray)
                .put("adjustments", adjustmentsArray)
    }

    @kotlin.Throws(Exception::class)
    private fun createAttributeMapJson(date: Date): JSONObject {
        val stringObject: JSONObject = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val integerObject: JSONObject = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val booleanObject: JSONObject = JSONObject()
                .put("type", "boolean")
                .put("value", true)
        val floatObject: JSONObject = JSONObject()
                .put("type", "float")
                .put("value", 1.23)
        val dateObject: JSONObject = JSONObject()
                .put("type", "date")
                .put("value", date.getTime())
        val attributesJson: JSONObject = JSONObject()
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
