package com.marigold.rnsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.MessageStream

class MessageBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(MessageStream.EXTRA_MESSAGE_ID)

        if (messageId == null) {
            val fallbackIntent = Intent(context, MessageActivity::class.java)
            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (intent.extras != null) {
                fallbackIntent.putExtras(intent.extras!!)
            }
            context.startActivity(fallbackIntent)
            return
        }

        val reactContext = RNMessageStreamBridge.reactContext
        val moduleImpl = RNMessageStreamBridge.messageStreamModuleImpl

        if (reactContext == null || moduleImpl == null) {
            val fallbackIntent =
                MessageActivity.intentForMessage(context, null, messageId)
            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(fallbackIntent)
            return
        }
        val activity = reactContext.currentActivity

        if (activity == null) {
            val fallbackIntent =
                MessageActivity.intentForMessage(context, null, messageId)
            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(fallbackIntent)
            return
        }

        val pendingResult = goAsync()
        moduleImpl.handleFullScreenMessage(activity, messageId) {
            pendingResult.finish()
        }
    }
}
