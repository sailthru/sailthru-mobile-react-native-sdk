package com.marigold.rnsdk

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.common.LifecycleState
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_MESSAGES
import com.marigold.sdk.MessageStream
import com.marigold.sdk.InAppPresentationConfiguration
import com.marigold.sdk.InAppPresentationCompletion
import com.marigold.sdk.InAppPresentationDecision
import com.marigold.sdk.enums.ImpressionType
import com.marigold.sdk.model.Message
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap

class RNMessageStreamModule(private val reactContext: ReactApplicationContext) : NativeRNMessageStreamSpec(reactContext), MessageStream.OnInAppNotificationDisplayListener {

    companion object {
        const val NAME = "RNMessageStream"
        const val MESSAGE_ID = "id"
    }

    @VisibleForTesting
    var messageStream = MessageStream()

    @VisibleForTesting
    internal var jsonConverter: JsonConverter = JsonConverter()

    private val pendingPresentations = ConcurrentHashMap<String, InAppPresentationCompletion>()
    private val notificationListener = MessageStream.OnInAppPresentationListener { message, id, completion ->
        emitPresentationRequest("notification", message, id, completion)
    }
    private val detailListener = MessageStream.OnInAppPresentationListener { message, id, completion ->
        emitPresentationRequest("detail", message, id, completion)
    }
    private var notificationPreparationEnabled = false
    private var detailPreparationEnabled = false
    private var presentationCallbacksConfigured = false
    private var pendingDetailIntent: Intent? = null
    private val notificationTapListener = object : BaseActivityEventListener() {
        override fun onNewIntent(intent: Intent) {
            if (invalidated || !HostFirstMessageDetail.isPending(intent)) return
            pendingDetailIntent = intent
            presentPendingNotificationDetail()
        }
    }
    private val lifecycleListener = object : LifecycleEventListener {
        override fun onHostResume() = presentPendingNotificationDetail()
        override fun onHostPause() = Unit
        override fun onHostDestroy() = Unit
    }
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val eventChannel = Channel<Boolean>(1)
    @Volatile private var defaultInAppNotification = true
    @Volatile private var invalidated = false

    @VisibleForTesting
    internal var notificationTimeoutMs: Long = 2000L
    init {
        // The SDK retains the legacy listener and has no owner-checked removal for it.
        val module = WeakReference(this)
        messageStream.setOnInAppNotificationDisplayListener { message ->
            module.get()?.shouldPresentInAppNotification(message) ?: true
        }
        reactContext.addActivityEventListener(notificationTapListener)
        reactContext.addLifecycleEventListener(lifecycleListener)
    }

    override fun shouldPresentInAppNotification(message: Message): Boolean {
        if (invalidated || defaultInAppNotification) {
            return true
        }

        return runBlocking {
            emitWithTimeout(message)
        }
    }

    private suspend fun emitWithTimeout(message: Message): Boolean {
        // Drain any stale signal from a previous or duplicate notifyInAppHandled call
        eventChannel.tryReceive()
        return withTimeoutOrNull(notificationTimeoutMs) {
            try {
                val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
                if (invalidated) return@withTimeoutOrNull true
                emitOnInAppNotification(writableMap)
                eventChannel.receiveCatching().getOrNull()
            } catch (e: JSONException) {
                e.printStackTrace()
                true
            }
        } != false || invalidated
    }

    override fun setPresentationCallbacksEnabled(notification: Boolean, detail: Boolean) {
        reactContext.runOnUiQueueThread {
            if (invalidated) return@runOnUiQueueThread
            updatePresentationCallbacks(notification, detail)
            presentationCallbacksConfigured = true
            presentPendingNotificationDetail()
        }
    }

    private fun presentPendingNotificationDetail() {
        if (invalidated || !presentationCallbacksConfigured || reactContext.lifecycleState != LifecycleState.RESUMED) return
        val host = reactContext.currentActivity ?: return
        if (host.isFinishing || host.isDestroyed) return
        val messageId = HostFirstMessageDetail.takeLatest(host.intent, pendingDetailIntent)
        pendingDetailIntent = null
        if (messageId == null) return
        messageStream.presentMessageDetail(host, messageId)
    }

    @Synchronized
    private fun updatePresentationCallbacks(notification: Boolean, detail: Boolean) {
        if (invalidated && (notification || detail)) return
        if (notification != notificationPreparationEnabled) {
            notificationPreparationEnabled = notification
            if (notification) messageStream.setInAppNotificationPreparationListener(notificationListener)
            else messageStream.removeInAppNotificationPreparationListener(notificationListener)
        }
        if (detail != detailPreparationEnabled) {
            detailPreparationEnabled = detail
            if (detail) messageStream.setMessageDetailPreparationListener(detailListener)
            else messageStream.removeMessageDetailPreparationListener(detailListener)
        }
    }

    private fun emitPresentationRequest(kind: String, message: Message, id: String, completion: InAppPresentationCompletion) {
        synchronized(this) {
            if (invalidated) return
            pendingPresentations[id] = completion
        }
        // The native decision deadline is two seconds; discard abandoned bridge replies.
        mainHandler.postDelayed({ pendingPresentations.remove(id, completion) }, 2100)
        reactContext.runOnUiQueueThread {
            if (invalidated || pendingPresentations[id] !== completion) return@runOnUiQueueThread
            try {
                val event = jsonConverter.createNativeMap().apply {
                    putString("requestId", id)
                    putString("kind", kind)
                    putMap("message", jsonConverter.convertJsonToMap(message.toJSON()))
                }
                synchronized(this) {
                    if (!invalidated && pendingPresentations[id] === completion) {
                        emitOnPresentationRequest(event)
                    }
                }
            } catch (e: Exception) {
                if (pendingPresentations.remove(id, completion)) completion.complete(InAppPresentationDecision.display())
            }
        }
    }

    override fun completePresentation(requestId: String, action: String, configuration: ReadableMap?) {
        val completion = pendingPresentations.remove(requestId) ?: return
        val decision = when (action) {
            "suppress" -> InAppPresentationDecision.suppress()
            "handled" -> InAppPresentationDecision.handled()
            "display" -> try {
                InAppPresentationDecision.display(configuration?.let { InAppPresentationConfiguration.fromJSON(jsonConverter.convertMapToJson(it, omitNonFinite = true)) })
            } catch (e: JSONException) { InAppPresentationDecision.display() }
            else -> InAppPresentationDecision.display()
        }
        completion.complete(decision)
    }

    override fun invalidate() {
        synchronized(this) {
            invalidated = true
            pendingPresentations.clear()
        }
        reactContext.removeActivityEventListener(notificationTapListener)
        reactContext.removeLifecycleEventListener(lifecycleListener)
        eventChannel.close()
        reactContext.runOnUiQueueThread {
            updatePresentationCallbacks(false, false)
        }
        super.invalidate()
    }

    override fun getName(): String {
        return NAME
    }

    override fun notifyInAppHandled(handled: Boolean) {
        eventChannel.trySend(!handled)
    }

    override fun useDefaultInAppNotification(useDefault: Boolean) {
        defaultInAppNotification = useDefault
    }

    override fun getMessage(messageId: String, promise: Promise?) {
        messageStream.getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
            override fun onSuccess(value: Message) {
                try {
                    val toJsonMethod = Message::class.java.getDeclaredMethod("toJSON")
                    toJsonMethod.isAccessible = true

                    val messageMap =
                        (toJsonMethod.invoke(value) as? JSONObject)?.let { messageJson ->
                            jsonConverter.convertJsonToMap(messageJson)
                        }
                    promise?.resolve(messageMap)
                } catch (e: NoSuchMethodException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: IllegalAccessException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: JSONException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: InvocationTargetException) {
                    promise?.reject(ERROR_CODE_MESSAGES, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun getMessages(promise: Promise?) {
        promise ?: return
        messageStream.getMessages(object : MessageStream.MessagesHandler {
            override fun onSuccess(messages: ArrayList<Message>) {
                val array = getWritableArray()
                try {
                    val toJsonMethod = Message::class.java.getDeclaredMethod("toJSON")
                    toJsonMethod.isAccessible = true

                    for (message in messages) {
                        val messageJson = toJsonMethod.invoke(message) as? JSONObject ?: continue
                        array.pushMap(jsonConverter.convertJsonToMap(messageJson))
                    }
                    promise.resolve(array)
                } catch (e: NoSuchMethodException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: IllegalAccessException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: JSONException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                } catch (e: InvocationTargetException) {
                    promise.reject(ERROR_CODE_MESSAGES, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun getUnreadCount(promise: Promise?) {
        promise ?: return
        messageStream.getUnreadMessageCount(object : MessageStream.MessageStreamHandler<Int> {
            override fun onSuccess(value: Int) {
                promise.resolve(value)
            }

            override fun onFailure(error: Error) {
                promise.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun markMessageAsRead(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = createMessage(messageMap, promise) ?: return
        messageStream.setMessageRead(message, object : MessageStream.MessagesReadHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun removeMessage(messageMap: ReadableMap?, promise: Promise?) {
        messageMap ?: return
        val message = createMessage(messageMap, promise) ?: return
        messageStream.deleteMessage(message, object : MessageStream.MessageDeletedHandler {
            override fun onSuccess() {
                promise?.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    override fun presentMessageDetail(message: ReadableMap?) {
        message ?: return
        val activity = reactContext.currentActivity ?: return
        val messageId = message.getString(MESSAGE_ID)
        if (messageId.isNullOrBlank()) return
        reactContext.runOnUiQueueThread { messageStream.presentMessageDetail(activity, messageId) }
    }

    override fun dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    override fun registerMessageImpression(impressionType: Double, messageMap: ReadableMap?) {
        messageMap ?: return
        val type = when (impressionType.toInt()) {
            0 -> ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW
            1 -> ImpressionType.IMPRESSION_TYPE_STREAM_VIEW
            2 -> ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW
            else -> return
        }
        val message = createMessage(messageMap, null) ?: return
        messageStream.registerMessageImpression(type, message)
    }

    override fun clearMessages(promise: Promise?) {
        messageStream.clearMessages(object : MessageStream.MessageStreamHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise?.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise?.reject(ERROR_CODE_MESSAGES, error.message)
            }
        })
    }

    /*
     * Helper Methods
     */
    @VisibleForTesting
    fun createMessage(messageMap: ReadableMap, promise: Promise?): Message? = try {
        val messageJson = jsonConverter.convertMapToJson(messageMap)
        val constructor = Message::class.java.getDeclaredConstructor(String::class.java)
        constructor.isAccessible = true
        constructor.newInstance(messageJson.toString())
    } catch(e: Exception) {
        if (promise == null) {
            e.printStackTrace()
        } else {
            promise.reject(ERROR_CODE_MESSAGES, e.message)
        }
        null
    }

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    fun getWritableArray(): WritableArray {
        return WritableNativeArray()
    }
}
