@file:JvmName("NotificationConfigExtensions")

package com.marigold.rnsdk

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import com.marigold.sdk.NotificationBundle
import com.marigold.sdk.NotificationConfig
import java.util.UUID

/**
 * Routes Android campaign notification taps through the wrapper's host Activity.
 * Apply to the app's notification configuration at startup. Call `MessageStream.setPresentationCallbacksEnabled`
 * when the host can present detail. Taps wait for that call and for the Activity to resume.
 * Configure callbacks even when using SDK detail. If the host overrides `onNewIntent`,
 * call `super.onNewIntent(intent)` to deliver taps while the Activity is running.
 * Other notifications use normal routing.
 * This replaces any content intent builder already registered on this configuration.
 */
fun NotificationConfig.withHostFirstMessageDetail(hostActivity: Class<out Activity>): NotificationConfig =
    setContentIntentBuilder { context, extras ->
        val notification = NotificationBundle(extras)
        val messageId = notification.getMessageIdFromPayload()
            ?.takeUnless { it.isBlank() || it == "null" } ?: return@setContentIntentBuilder null

        val intent = Intent(context, hostActivity).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtras(extras)
            HostFirstMessageDetail.mark(this, messageId)
        }
        PendingIntent.getActivity(
            context,
            notification.generateAndroidNotificationID(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

internal object HostFirstMessageDetail {
    private const val ACTION = "com.marigold.rnsdk.HOST_FIRST_MESSAGE_DETAIL"
    private const val TAP = "com.marigold.rnsdk.HOST_FIRST_TAP"
    private const val MESSAGE = "com.marigold.rnsdk.HOST_FIRST_MESSAGE"
    private val consumedTaps = mutableSetOf<String>()

    fun mark(intent: Intent, messageId: String) {
        intent.action = ACTION
        intent.putExtra(TAP, UUID.randomUUID().toString())
        intent.putExtra(MESSAGE, messageId)
    }

    fun isPending(intent: Intent?): Boolean {
        if (intent == null || intent.action != ACTION) return false
        val tap = intent.getStringExtra(TAP) ?: return false
        return tap.isNotBlank() && tap !in consumedTaps && !intent.getStringExtra(MESSAGE).isNullOrBlank()
    }

    fun takeLatest(launchIntent: Intent?, newIntent: Intent?): String? {
        if (newIntent == null) return take(launchIntent)
        // The launch intent can also be a copy of the newly delivered tap.
        if (launchIntent?.getStringExtra(TAP) != newIntent.getStringExtra(TAP)) take(launchIntent)
        return take(newIntent)
    }

    fun take(intent: Intent?): String? {
        if (intent == null || !isPending(intent)) return null
        val tap = intent.getStringExtra(TAP) ?: return null
        val messageId = intent.getStringExtra(MESSAGE) ?: return null
        if (!consumedTaps.add(tap)) return null
        intent.removeExtra(TAP)
        intent.removeExtra(MESSAGE)
        return messageId
    }
}
