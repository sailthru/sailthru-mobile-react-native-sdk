package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.MessageStream
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.Message
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class RNMessageStreamModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var staticMessageStream: MockedConstruction<MessageStream>

    @Captor
    private lateinit var messageStreamVoidCaptor: ArgumentCaptor<MessageStream.MessageStreamHandler<Void?>>

    @Captor
    private lateinit var messageStreamIntCaptor: ArgumentCaptor<MessageStream.MessageStreamHandler<Int>>

    private lateinit var messageStream: MessageStream

    private lateinit var rnMessageStreamModule: RNMessageStreamModule
    private lateinit var rnMessageStreamModuleSpy: RNMessageStreamModule

    @Before
    fun setup() {
        rnMessageStreamModule = RNMessageStreamModule(mockContext, true)
        rnMessageStreamModule.jsonConverter = jsonConverter
        rnMessageStreamModuleSpy = Mockito.spy(rnMessageStreamModule)

        messageStream = staticMessageStream.constructed()[0]
    }

    @Test
    fun testShouldPresentInAppNotification() {
        val message: Message = mock()
        val module: DeviceEventManagerModule.RCTDeviceEventEmitter = mock()
        val writableMap: WritableMap = mock()
        val jsonObject: JSONObject = mock()

        doReturn(jsonObject).whenever(message).toJSON()
        doReturn(writableMap).whenever(jsonConverter).convertJsonToMap(jsonObject)
        doReturn(module).whenever(mockContext).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

        val shouldPresent = rnMessageStreamModuleSpy.shouldPresentInAppNotification(message)

        verify(mockContext).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        verify(module).emit("inappnotification", writableMap)

        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testShouldPresentInAppNotificationException() {
        val message: Message = mock()
        val jsonObject: JSONObject = mock()
        val jsonException: JSONException = mock()
        doReturn(jsonObject).whenever(message).toJSON()
        doThrow(jsonException).whenever(jsonConverter).convertJsonToMap(jsonObject)

        val shouldPresent = runBlocking {
            rnMessageStreamModuleSpy.shouldPresentInAppNotification(message)
        }

        verify(mockContext, Mockito.times(0)).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        verify(jsonException).printStackTrace()
        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testGetMessages() {
        // Setup mocks
        val promise: Promise = mock()
        val writableArray: WritableArray = mock()
        val error: Error = mock()

        // Initiate test
        rnMessageStreamModuleSpy.getMessages(promise)

        // Capture MessagesHandler to verify behaviour
        val argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler::class.java)
        verify(messageStream).getMessages(capture(argumentCaptor))
        val messagesHandler = argumentCaptor.value

        // Replace native array with mock
        doReturn(writableArray).whenever(rnMessageStreamModuleSpy).getWritableArray()

        // Setup message array
        val messages: ArrayList<Message> = ArrayList()

        // Test success handler
        messagesHandler.onSuccess(messages)
        verify(promise).resolve(writableArray)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        messagesHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testClearMessages() {
        // Create mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val moduleSpy = Mockito.spy(rnMessageStreamModule)

        // Initiate test
        moduleSpy.clearMessages(promise)

        // Capture MarigoldHandler to verify behaviour
        verify(messageStream).clearMessages(capture(messageStreamVoidCaptor))
        val handler = messageStreamVoidCaptor.value

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    fun testGetUnreadCount() {
        val unreadCount = 4

        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMessageStreamModule.getUnreadCount(promise)

        // Capture MessagesHandler to verify behaviour
        verify(messageStream).getUnreadMessageCount(capture(messageStreamIntCaptor))
        val countHandler = messageStreamIntCaptor.value

        // Test success handler
        countHandler.onSuccess(unreadCount)
        verify(promise).resolve(unreadCount)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

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
        val moduleSpy = Mockito.spy(rnMessageStreamModule)
        doReturn(message).whenever(moduleSpy).getMessage(readableMap, promise)

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
        doReturn(errorMessage).whenever(error).message

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
        val moduleSpy = Mockito.spy(rnMessageStreamModule)
        doThrow(jsonException).whenever(moduleSpy).getMessage(readableMap, promise)

        // Initiate test
        Assert.assertThrows(JSONException::class.java) {
            moduleSpy.removeMessage(readableMap, promise)
            verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        }

        // Verify result
        verify(messageStream, Mockito.times(0)).deleteMessage(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterMessageImpression() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock()
        val message: Message = mock()
        doReturn(message).whenever(rnMessageStreamModuleSpy).getMessage(readableMap, null)

        // Initiate test
        rnMessageStreamModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(messageStream).registerMessageImpression(ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message)
    }

    @Test
    fun testRegisterMessageImpressionInvalidCode() {
        // Create input
        val typeCode = 10
        val readableMap: ReadableMap = mock()

        // Initiate test
        rnMessageStreamModuleSpy.registerMessageImpression(typeCode, readableMap)

        // Verify result
        verify(messageStream, Mockito.times(0)).registerMessageImpression(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterMessageImpressionException() {
        // Create input
        val typeCode = 0
        val readableMap: ReadableMap = mock()
        val jsonException: JSONException = mock()
        doThrow(jsonException).whenever(rnMessageStreamModuleSpy).getMessage(readableMap, null)

        // Initiate test
        Assert.assertThrows(Exception::class.java) {
            rnMessageStreamModuleSpy.registerMessageImpression(typeCode, readableMap)
            verify(jsonException).printStackTrace()
        }

        // Verify result
        verify(messageStream, Mockito.times(0)).registerMessageImpression(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testMarkMessageAsRead() {
        // Create mocks
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val message: Message = mock()
        val error: Error = mock()
        val moduleSpy = Mockito.spy(rnMessageStreamModule)
        doReturn(message).whenever(moduleSpy).getMessage(readableMap, promise)

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
        doReturn(errorMessage).whenever(error).message

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
        val moduleSpy = Mockito.spy(rnMessageStreamModule)
        doThrow(jsonException).whenever(moduleSpy).getMessage(readableMap, promise)

        // Initiate test
        Assert.assertThrows(JSONException::class.java) {
            moduleSpy.markMessageAsRead(readableMap, promise)
            verify(promise).reject(RNMarigoldModule.ERROR_CODE_MESSAGES, jsonException.message)
        }

        // Verify result
        verify(messageStream, Mockito.times(0)).setMessageRead(any(), any())
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
        doReturn(messageID).whenever(message).getString(RNMarigoldModule.MESSAGE_ID)
        doReturn(activity).whenever(rnMessageStreamModuleSpy).currentActivity()
        doReturn(intent).whenever(rnMessageStreamModuleSpy).getMessageActivityIntent(any(), any())

        // Initiate test
        rnMessageStreamModuleSpy.presentMessageDetail(message)

        // Verify result
        verify(activity).startActivity(intent)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMessage() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val messageJson = JSONObject().put("title", "test title")
        doReturn(messageJson).whenever(jsonConverter).convertMapToJson(readableMap)

        // Initiate test
        val message = rnMessageStreamModuleSpy.getMessage(readableMap, promise)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals("test title", message!!.title)
    }
}