package com.marigold.rnsdk

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.LifecycleState
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_MESSAGES
import com.marigold.rnsdk.RNMessageStreamModule.Companion.MESSAGE_ID
import com.marigold.sdk.MessageStream
import com.marigold.sdk.InAppPresentationCompletion
import com.marigold.sdk.InAppPresentationDecision
import com.marigold.sdk.enums.InAppPresentationAction
import com.marigold.sdk.enums.InAppPresentationStyle
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.Message
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ConcurrentHashMap
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
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
    @Captor
    private lateinit var messageStreamMessageCaptor: ArgumentCaptor<MessageStream.MessageStreamHandler<Message>>

    private lateinit var messageStream: MessageStream

    private lateinit var rnMessageStreamModule: RNMessageStreamModule

    @Before
    fun setup() {
        rnMessageStreamModule = Mockito.spy(RNMessageStreamModule(mockContext))
        rnMessageStreamModule.jsonConverter = jsonConverter

        messageStream = staticMessageStream.constructed()[0]
    }

    @Test
    fun testShouldPresentInAppNotificationUseDefaultTrue() {
        val message: Message = mock()

        val shouldPresent = rnMessageStreamModule.shouldPresentInAppNotification(message)

        verify(rnMessageStreamModule, Mockito.times(0)).emitOnInAppNotification(any())
        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun `retired legacy listener allows SDK display without emitting to React Native`() {
        val module = RNMessageStreamModule(mockContext).apply {
            this.jsonConverter = this@RNMessageStreamModuleTest.jsonConverter
            useDefaultInAppNotification(false)
        }
        val stream = staticMessageStream.constructed().last()
        val listener = argumentCaptor<MessageStream.OnInAppNotificationDisplayListener>()
        verify(stream).setOnInAppNotificationDisplayListener(listener.capture())

        module.invalidate()

        Assert.assertTrue(listener.firstValue.shouldPresentInAppNotification(mock()))
        Mockito.verifyNoInteractions(jsonConverter)
        verify(stream).setOnInAppNotificationDisplayListener(anyOrNull())
    }

    @Test
    fun testShouldPresentInAppNotificationTimeout() {
        val message: Message = mock()
        val writableMap: WritableMap = mock()
        val jsonObject: JSONObject = mock()

        doReturn(jsonObject).whenever(message).toJSON()
        doReturn(writableMap).whenever(jsonConverter).convertJsonToMap(jsonObject)
        doNothing().whenever(rnMessageStreamModule).emitOnInAppNotification(any())

        rnMessageStreamModule.notificationTimeoutMs = 200L
        rnMessageStreamModule.useDefaultInAppNotification(false)

        val start = System.currentTimeMillis()
        val shouldPresent = rnMessageStreamModule.shouldPresentInAppNotification(message)
        val elapsed = System.currentTimeMillis() - start

        verify(rnMessageStreamModule).emitOnInAppNotification(writableMap)
        Assert.assertTrue(shouldPresent)
        Assert.assertTrue("Expected method to block for ~${rnMessageStreamModule.notificationTimeoutMs}ms but returned after ${elapsed}ms", elapsed >= rnMessageStreamModule.notificationTimeoutMs - 50L)
        Assert.assertTrue("Expected method to return near timeout but blocked for ${elapsed}ms", elapsed < rnMessageStreamModule.notificationTimeoutMs + 1000L)
    }

    @Test
    fun testShouldPresentInAppNotificationInAppHandledTrue() {
        val message: Message = mock()
        val writableMap: WritableMap = mock()
        val jsonObject: JSONObject = mock()
        val latch = CountDownLatch(1)

        doReturn(jsonObject).whenever(message).toJSON()
        doReturn(writableMap).whenever(jsonConverter).convertJsonToMap(jsonObject)
        doAnswer { latch.countDown(); null }.whenever(rnMessageStreamModule).emitOnInAppNotification(any())

        rnMessageStreamModule.useDefaultInAppNotification(false)
        var shouldPresent = true
        val thread = Thread { shouldPresent = rnMessageStreamModule.shouldPresentInAppNotification(message) }
        thread.start()
        latch.await()
        rnMessageStreamModule.notifyInAppHandled(true)
        thread.join()

        Assert.assertFalse(shouldPresent)
        verify(rnMessageStreamModule).emitOnInAppNotification(writableMap)
    }

    @Test
    fun testShouldPresentInAppNotificationInAppHandledFalse() {
        val message: Message = mock()
        val writableMap: WritableMap = mock()
        val jsonObject: JSONObject = mock()
        val latch = CountDownLatch(1)

        doReturn(jsonObject).whenever(message).toJSON()
        doReturn(writableMap).whenever(jsonConverter).convertJsonToMap(jsonObject)
        doAnswer { latch.countDown(); null }.whenever(rnMessageStreamModule).emitOnInAppNotification(any())

        rnMessageStreamModule.useDefaultInAppNotification(false)
        var shouldPresent = false
        val thread = Thread { shouldPresent = rnMessageStreamModule.shouldPresentInAppNotification(message) }
        thread.start()
        latch.await()
        rnMessageStreamModule.notifyInAppHandled(false)
        thread.join()

        Assert.assertTrue(shouldPresent)
        verify(rnMessageStreamModule).emitOnInAppNotification(writableMap)
    }

    @Test
    fun testShouldPresentInAppNotificationException() {
        val message: Message = mock()
        val jsonObject: JSONObject = mock()
        val jsonException: JSONException = mock()
        doReturn(jsonObject).whenever(message).toJSON()
        doThrow(jsonException).whenever(jsonConverter).convertJsonToMap(jsonObject)

        rnMessageStreamModule.useDefaultInAppNotification(false)
        val shouldPresent = rnMessageStreamModule.shouldPresentInAppNotification(message)

        verify(jsonException).printStackTrace()
        Assert.assertTrue(shouldPresent)
    }

    @Test
    fun testGetMessage() {
        // Setup mocks
        val message: Message = mock()
        val jsonObject: JSONObject = mock()
        val promise: Promise = mock()
        val writableMap: WritableMap = mock()
        val error: Error = mock()
        val messageId = "messageId123456"

        // Initiate test
        rnMessageStreamModule.getMessage(messageId, promise)

        // Capture handler to verify behaviour
        verify(messageStream).getMessage(eq(messageId), capture(messageStreamMessageCaptor))
        val messageHandler = messageStreamMessageCaptor.value

        // Replace native array with mock
        doReturn(jsonObject).whenever(message).toJSON()
        doReturn(writableMap).whenever(jsonConverter).convertJsonToMap(jsonObject)

        // Test success handler
        messageHandler.onSuccess(message)
        verify(promise).resolve(writableMap)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        messageHandler.onFailure(error)
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    fun testGetMessages() {
        // Setup mocks
        val promise: Promise = mock()
        val writableArray: WritableArray = mock()
        val error: Error = mock()

        // Initiate test
        rnMessageStreamModule.getMessages(promise)

        // Capture MessagesHandler to verify behaviour
        val argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler::class.java)
        verify(messageStream).getMessages(capture(argumentCaptor))
        val messagesHandler = argumentCaptor.value

        // Replace native array with mock
        doReturn(writableArray).whenever(rnMessageStreamModule).getWritableArray()

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
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testClearMessages() {
        // Create mocks
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMessageStreamModule.clearMessages(promise)

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
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
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
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveMessage() {
        // Create mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val readableMap: ReadableMap = mock()
        val message: Message = mock()
        doReturn(message).whenever(rnMessageStreamModule).createMessage(readableMap, promise)

        // Initiate test
        rnMessageStreamModule.removeMessage(readableMap, promise)

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
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveMessageException() {
        // Create mocks
        val promise: Promise = mock()
        val readableMap: ReadableMap = mock()
        val jsonException = JSONException("test exception")
        doThrow(jsonException).whenever(rnMessageStreamModule).createMessage(readableMap, promise)

        // Initiate test
        Assert.assertThrows(JSONException::class.java) {
            rnMessageStreamModule.removeMessage(readableMap, promise)
            verify(promise).reject(ERROR_CODE_MESSAGES, jsonException.message)
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
        doReturn(message).whenever(rnMessageStreamModule).createMessage(readableMap, null)

        // Initiate test
        rnMessageStreamModule.registerMessageImpression(typeCode.toDouble(), readableMap)

        // Verify result
        verify(messageStream).registerMessageImpression(ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message)
    }

    @Test
    fun testRegisterMessageImpressionInvalidCode() {
        // Create input
        val typeCode = 10
        val readableMap: ReadableMap = mock()

        // Initiate test
        rnMessageStreamModule.registerMessageImpression(typeCode.toDouble(), readableMap)

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
        doThrow(jsonException).whenever(rnMessageStreamModule).createMessage(readableMap, null)

        // Initiate test
        Assert.assertThrows(Exception::class.java) {
            rnMessageStreamModule.registerMessageImpression(typeCode.toDouble(), readableMap)
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
        doReturn(message).whenever(rnMessageStreamModule).createMessage(readableMap, promise)

        // Initiate test
        rnMessageStreamModule.markMessageAsRead(readableMap, promise)

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
        verify(promise).reject(ERROR_CODE_MESSAGES, errorMessage)
    }

    @Test
    @Throws(Exception::class)
    fun testMarkMessageAsReadException() {
        // Create mocks
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")
        doThrow(jsonException).whenever(rnMessageStreamModule).createMessage(readableMap, promise)

        // Initiate test
        Assert.assertThrows(JSONException::class.java) {
            rnMessageStreamModule.markMessageAsRead(readableMap, promise)
            verify(promise).reject(ERROR_CODE_MESSAGES, jsonException.message)
        }

        // Verify result
        verify(messageStream, Mockito.times(0)).setMessageRead(any(), any())
    }

    @Test
    fun `presentation replies match their request and ignore duplicates`() {
        val field = RNMessageStreamModule::class.java.getDeclaredField("pendingPresentations").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val pending = field.get(rnMessageStreamModule) as ConcurrentHashMap<String, InAppPresentationCompletion>
        var first: InAppPresentationDecision? = null
        var second: InAppPresentationDecision? = null
        pending["one"] = InAppPresentationCompletion { first = it }
        pending["two"] = InAppPresentationCompletion { second = it }
        val config: ReadableMap = mock()
        whenever(jsonConverter.convertMapToJson(config, omitNonFinite = true)).thenReturn(JSONObject("""{"style":"modal","shows_close_button":false}"""))
        rnMessageStreamModule.completePresentation("two", "display", config)
        rnMessageStreamModule.completePresentation("two", "suppress", null)
        Assert.assertNull(first)
        Assert.assertEquals(InAppPresentationAction.DISPLAY, second?.action)
        Assert.assertEquals(InAppPresentationStyle.MODAL, second?.configuration?.presentationStyle)
        Assert.assertEquals(false, second?.configuration?.showsCloseButton)
        rnMessageStreamModule.completePresentation("one", "handled", null)
        Assert.assertEquals(InAppPresentationAction.HANDLED, first?.action)
    }

    @Test
    fun `presentation listeners can be enabled independently`() {
        doAnswer { invocation -> invocation.getArgument<Runnable>(0).run(); null }
            .whenever(mockContext).runOnUiQueueThread(any())
        rnMessageStreamModule.setPresentationCallbacksEnabled(true, false)
        verify(messageStream).setInAppNotificationPreparationListener(any())
        verify(messageStream, org.mockito.kotlin.never()).setMessageDetailPreparationListener(any())
    }

    @Test
    fun `a newer notification supersedes the launch tap before callbacks and host resume`() {
        val module = RNMessageStreamModule(mockContext)
        val nativeStream = staticMessageStream.constructed().last()
        val host: Activity = mock()
        fun tap(messageId: String): Intent {
            val intent: Intent = mock()
            val extras = mutableMapOf<String, String>()
            whenever(intent.putExtra(any<String>(), any<String>())).thenAnswer { invocation ->
                extras[invocation.getArgument(0)] = invocation.getArgument(1)
                intent
            }
            whenever(intent.getStringExtra(any())).thenAnswer { extras[it.getArgument<String>(0)] }
            var action: String? = null
            whenever(intent.setAction(any())).thenAnswer {
                action = it.getArgument(0)
                intent
            }
            whenever(intent.action).thenAnswer { action }
            HostFirstMessageDetail.mark(intent, messageId)
            return intent
        }
        val launchIntent = tap("older")
        whenever(mockContext.currentActivity).thenReturn(host)
        whenever(host.intent).thenReturn(launchIntent)
        doAnswer { invocation -> invocation.getArgument<Runnable>(0).run(); null }
            .whenever(mockContext).runOnUiQueueThread(any())
        val lifecycle = argumentCaptor<LifecycleEventListener>()
        verify(mockContext, Mockito.times(2)).addLifecycleEventListener(lifecycle.capture())
        val hostLifecycle = lifecycle.lastValue
        val activityEvents = argumentCaptor<ActivityEventListener>()
        verify(mockContext, Mockito.times(2)).addActivityEventListener(activityEvents.capture())
        activityEvents.lastValue.onNewIntent(tap("newer"))

        whenever(mockContext.lifecycleState).thenReturn(LifecycleState.RESUMED)
        hostLifecycle.onHostResume()
        verify(nativeStream, Mockito.never()).presentMessageDetail(any(), any())

        whenever(mockContext.lifecycleState).thenReturn(LifecycleState.BEFORE_RESUME)
        module.setPresentationCallbacksEnabled(false, true)
        verify(nativeStream, Mockito.never()).presentMessageDetail(any(), any())

        whenever(mockContext.lifecycleState).thenReturn(LifecycleState.RESUMED)
        hostLifecycle.onHostResume()
        hostLifecycle.onHostResume()
        verify(nativeStream).presentMessageDetail(host, "newer")
        verify(nativeStream, Mockito.never()).presentMessageDetail(host, "older")
    }

    @Test
    fun testPresentMessageDetail() {
        // Setup input
        val messageID = "message ID"

        // Setup mocks
        val message: ReadableMap = mock()
        val activity: Activity = mock()

        // Mock behaviour
        doReturn(messageID).whenever(message).getString(MESSAGE_ID)
        doAnswer { invocation -> invocation.getArgument<Runnable>(0).run(); null }
            .whenever(mockContext).runOnUiQueueThread(any())
        doReturn(activity).whenever(mockContext).currentActivity

        // Initiate test
        rnMessageStreamModule.presentMessageDetail(message)

        // Verify result
        verify(messageStream).presentMessageDetail(activity, messageID)
    }

    @Test
    @Throws(Exception::class)
    fun testCreateMessage() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val messageJson = JSONObject().put("title", "test title")
        doReturn(messageJson).whenever(jsonConverter).convertMapToJson(readableMap)

        // Initiate test
        val message = rnMessageStreamModule.createMessage(readableMap, promise)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals("test title", message!!.title)
    }
}
