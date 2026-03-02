package com.marigold.rnsdk

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.marigold.sdk.Marigold
import com.marigold.sdk.model.Message

class FullScreenMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_message)

        val messageId = intent.getStringExtra("message_id")
        if (messageId != null) {
            MessageStream().getMessage(messageId, object : MessageStream.MessageStreamHandler<Message> {
                override fun onSuccess(value: Message) {
                    Log.d("FullScreenMessage", "Displaying message: ${value.title}")
                }

                override fun onFailure(error: Error) {
                    Log.e("FullScreenMessage", "Failed to load message: ${error.message}")
                    finish()
                }
            })
        } else {
            Log.e("FullScreenMessage", "Message ID is null")
            finish()
        }
    }
}