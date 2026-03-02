package com.marigold.rnsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.marigold.sdk.model.Message
import com.marigold.sdk.stream.MessageStream

class MessageBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(MessageStream.EXTRA_MESSAGE_ID)

        if (messageId != null) {
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
        } else {
            Log.e("MessageReceiver", "Message ID is null")
        }
    }
}