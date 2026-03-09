package com.marigold.rnsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.marigold.sdk.model.Message
import com.marigold.sdk.MessageStream
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException

class MessageBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val FULL_SCREEN_MESSAGE_BROADCAST = "com.marigold.rnsdk.FULL_SCREEN_MESSAGE"
        private val fullScreenEventChannel = Channel<Boolean>()

        private fun sendFullScreenMessageToReactNative(context: Context, messageData: com.facebook.react.bridge.WritableMap) {
            val reactContext = context as? ReactContext
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onFullScreenMessageReceived", messageData)
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
            if (intent.action == FULL_SCREEN_MESSAGE_BROADCAST) {
                handleFullScreenMessage(context, messageId)
            } else {
                handleRegularMessage(context, messageId)
            }
        } else {
            Log.e("MessageReceiver", "Message ID is null")
        }
    }

    private fun handleRegularMessage(context: Context, messageId: String) {
        MessageStream().getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
            override fun onSuccess(value: Message) {
                RNMessageHandler.sendMessageToReactNative(context, value)
            }

            override fun onFailure(error: Error) {
                Log.e("MessageReceiver", "Failed to fetch message: ${error.message}")
                val fallbackIntent = Intent(context, FullScreenMessageActivity::class.java)
                fallbackIntent.putExtra("message_id", messageId)
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(fallbackIntent)
            }
        })
    }

    private fun handleFullScreenMessage(context: Context, messageId: String) {
        MessageStream().getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
            override fun onSuccess(value: Message) {
                val handled = runBlocking {
                    emitFullScreenWithTimeout(context, value)
                }

                if (!handled) {
                    val fallbackIntent = Intent(context, FullScreenMessageActivity::class.java)
                    fallbackIntent.putExtra("message_id", messageId)
                    fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(fallbackIntent)
                }
            }

            override fun onFailure(error: Error) {
                Log.e("MessageReceiver", "Failed to fetch full-screen message: ${error.message}")
                val fallbackIntent = Intent(context, FullScreenMessageActivity::class.java)
                fallbackIntent.putExtra("message_id", messageId)
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(fallbackIntent)
            }
        })
    }

    private suspend fun emitFullScreenWithTimeout(context: Context, message: Message): Boolean {
        return withTimeoutOrNull(5000L) {
            try {
                val jsonConverter = JsonConverter()
                val writableMap = jsonConverter.convertJsonToMap(message.toJSON())
                sendFullScreenMessageToReactNative(context, writableMap)
                fullScreenEventChannel.receive()
            } catch (e: JSONException) {
                e.printStackTrace()
                false
            }
        } ?: false
    }
}