package com.marigold.rnsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.model.Message
import com.marigold.sdk.MessageStream
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException

sealed class RNFullscreenResult {
    data class Success(val messageData: WritableMap) : RNFullscreenResult()
    data class Error(val errorCode: String, val errorMessage: String) : RNFullscreenResult()
}

class MessageBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val FULL_SCREEN_MESSAGE_BROADCAST = "com.marigold.rnsdk.FULL_SCREEN_MESSAGE"
        private val fullScreenEventChannel = Channel<Boolean>()

        private fun sendFullScreenResultToReactNative(context: Context, result: RNFullscreenResult) {
            val reactContext = context as? ReactContext
            val eventEmitter = reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

            when (result) {
                is RNFullscreenResult.Success -> {
                    eventEmitter?.emit("onFullScreenMessageReceived", result.messageData)
                }
                is RNFullscreenResult.Error -> {
                    eventEmitter?.emit("onFullScreenMessageError", mapOf(
                        "code" to result.errorCode,
                        "message" to result.errorMessage
                    ))
                }
            }
        }

        @Suppress("unused")
        fun notifyFullScreenHandled(handled: Boolean) {
            runBlocking {
                fullScreenEventChannel.send(handled)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(MessageStream.EXTRA_MESSAGE_ID)

        if (messageId != null) {
            // This receiver is only registered for FULL_SCREEN_MESSAGE_BROADCAST, so no need to check action
            handleFullScreenMessage(context, messageId)
        } else {
            Log.e("MessageReceiver", "Message ID is null")
        }
    }

    private fun handleFullScreenMessage(context: Context, messageId: String) {
        MessageStream().getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
            override fun onSuccess(value: Message) {
                val handled = runBlocking {
                    emitFullScreenWithTimeout(context, value)
                }

                if (!handled) {
                    // Fallback to native MessageActivity
                    val fallbackIntent = MessageActivity.intentForMessage(context, null, messageId)
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                }
            }

            override fun onFailure(error: Error) {
                Log.e("MessageReceiver", "Failed to fetch full-screen message: ${error.message}")

                // Send error to RN layer instead of falling back
                val errorResult = RNFullscreenResult.Error(
                    errorCode = "MESSAGE_FETCH_FAILED",
                    errorMessage = error.message ?: "Unknown error fetching message"
                )
                sendFullScreenResultToReactNative(context, errorResult)

                // Only fallback to native MessageActivity if RN layer can't handle the error
                try {
                    val fallbackIntent = MessageActivity.intentForMessage(context, null, messageId)
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                } catch (e: Exception) {
                    Log.e("MessageReceiver", "Failed to start fallback MessageActivity: ${e.message}")
                }
            }
        })
    }

    private suspend fun emitFullScreenWithTimeout(context: Context, message: Message): Boolean {
        return withTimeoutOrNull(5000L) {
            try {
                val jsonConverter = JsonConverter()
                val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
                val result = RNFullscreenResult.Success(writableMap)
                sendFullScreenResultToReactNative(context, result)
                fullScreenEventChannel.receive()
            } catch (e: JSONException) {
                e.printStackTrace()
                false
            }
        } ?: false
    }
}
