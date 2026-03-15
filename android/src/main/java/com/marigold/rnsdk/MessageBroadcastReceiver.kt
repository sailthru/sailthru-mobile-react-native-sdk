package com.marigold.rnsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.MessageStream

class MessageBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("RNMessageStream", "MessageBroadcastReceiver triggered")

        val messageId = intent.getStringExtra(MessageStream.EXTRA_MESSAGE_ID)

        if (messageId == null) {
            Log.e("RNMessageStream", "Message ID missing")
            return
        }

        val reactContext = RNMessageStreamBridge.reactContext
        val moduleImpl = RNMessageStreamBridge.messageStreamModuleImpl

        if (reactContext == null || moduleImpl == null) {

            Log.e("RNMessageStream", "RN not ready, opening default MessageActivity")

            val fallbackIntent =
                MessageActivity.intentForMessage(context, null, messageId)

            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(fallbackIntent)

            return
        }

        val activity = reactContext.currentActivity

        if (activity == null) {

            Log.e("RNMessageStream", "No activity, fallback to MessageActivity")

            val fallbackIntent =
                MessageActivity.intentForMessage(context, null, messageId)

            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(fallbackIntent)

            return
        }

        Log.d("RNMessageStream", "Passing message to RN custom handler")

        moduleImpl.handleFullScreenMessage(activity, messageId)
    }
}
