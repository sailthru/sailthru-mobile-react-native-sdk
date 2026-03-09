package com.marigold.rnsdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.marigold.sdk.MessageActivity
import com.marigold.sdk.MessageStream
import com.marigold.sdk.model.Message

class FullScreenMessageActivity : AppCompatActivity() {

    companion object {
        const val TAG = "FullScreenMessage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_message)

        val messageId = intent.getStringExtra("message_id")
        if (messageId != null) {
            MessageStream().getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
                override fun onSuccess(value: Message) {
                    Log.d(TAG, "Displaying message: ${value.title}")
                }

                override fun onFailure(error: Error) {
                    Log.e(TAG, "Failed to load message: ${error.message}")
                    fallbackToDefaultActivity(messageId)
                }
            })
        } else {
            Log.e(TAG, "Message ID is null")
            finish()
        }
    }

    private fun fallbackToDefaultActivity(messageId: String) {
        try {
            val messageActivityIntent = MessageActivity.intentForMessage(this, null, messageId)
            startActivity(messageActivityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start fallback MessageActivity: ${e.message}")
        } finally {
            finish()
        }
    }
}
