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
        val message: Message? = intent.getParcelableExtra(Marigold.EXTRA_PARCELABLE_MESSAGE)

        if (message != null) {
            Log.d("FullScreenMessage", "Displaying message: ${message.title}")
        } else {
            Log.e("FullScreenMessage", "Message object is null")
            finish()
        }
    }
}