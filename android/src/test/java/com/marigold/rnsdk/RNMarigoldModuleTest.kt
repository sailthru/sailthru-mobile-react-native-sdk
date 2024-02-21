package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import android.location.Location
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.Marigold
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.model.Message
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
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

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>

    @Mock
    private lateinit var staticMessageStream: MockedConstruction<MessageStream>

    private lateinit var marigold: Marigold
    private lateinit var messageStream: MessageStream

    private lateinit var rnMarigoldModule: RNMarigoldModule
    private lateinit var rnMarigoldModuleSpy: RNMarigoldModule

    @Before
    fun setup() {
        rnMarigoldModule = RNMarigoldModule(mockContext, true)
        rnMarigoldModule.jsonConverter = jsonConverter
        rnMarigoldModuleSpy = spy(rnMarigoldModule)

        marigold = staticMarigold.constructed()[0]
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
}
