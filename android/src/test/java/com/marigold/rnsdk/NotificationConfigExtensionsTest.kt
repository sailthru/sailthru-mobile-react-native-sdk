package com.marigold.rnsdk

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.marigold.sdk.NotificationBundle
import com.marigold.sdk.NotificationConfig
import com.marigold.sdk.interfaces.ContentIntentBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationConfigExtensionsTest {
    @Test
    fun `notifications without a campaign keep default routing`() {
        val builder = contentIntentBuilder()
        val context: Context = mock()
        mockStatic(PendingIntent::class.java).use { pendingIntents ->
            for (payload in listOf(null, """{"mid":""}""", """{"mid":"null"}""")) {
                val extras: Bundle = mock()
                whenever(extras.getString(NotificationBundle.BUNDLE_KEY_PAYLOAD)).thenReturn(payload)
                assertNull(builder.build(context, extras))
            }
            pendingIntents.verifyNoInteractions()
        }
    }

    @Test
    fun `campaign notifications target the supplied host and carry a single-use tap`() {
        val builder = contentIntentBuilder()
        val context: Context = mock()
        val extras: Bundle = mock()
        val pendingIntent: PendingIntent = mock()
        val notificationId = "notification"
        whenever(extras.getString(NotificationBundle.BUNDLE_KEY_PAYLOAD))
            .thenReturn("""{"mid":"campaign"}""")
        whenever(extras.getString(eq(NotificationBundle.EXTRA_NOTIFICATION_ID), any<String>()))
            .thenReturn(notificationId)
        whenever(extras.getString(NotificationBundle.BUNDLE_KEY_COLLAPSE_KEY, notificationId))
            .thenReturn(notificationId)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        mockConstruction(Intent::class.java) { intent, constructor ->
            assertEquals(listOf(context, Activity::class.java), constructor.arguments())
            val intentExtras = mutableMapOf<String, String>()
            whenever(intent.putExtra(any<String>(), any<String>())).thenAnswer {
                intentExtras[it.getArgument(0)] = it.getArgument(1)
                intent
            }
            whenever(intent.getStringExtra(any())).thenAnswer { intentExtras[it.getArgument<String>(0)] }
        }.use { intents ->
            mockStatic(PendingIntent::class.java).use { pendingIntents ->
                pendingIntents.`when`<PendingIntent> {
                    PendingIntent.getActivity(eq(context), eq(notificationId.hashCode()), any(), eq(flags))
                }.thenReturn(pendingIntent)

                assertSame(pendingIntent, builder.build(context, extras))

                val intent = intents.constructed().single()
                verify(intent).putExtras(extras)
                verify(intent).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                assertEquals("campaign", HostFirstMessageDetail.take(intent))
                assertNull(HostFirstMessageDetail.take(intent))
            }
        }
    }

    private fun contentIntentBuilder(): ContentIntentBuilder {
        val config: NotificationConfig = mock()
        whenever(config.setContentIntentBuilder(any())).thenReturn(config)
        assertSame(config, config.withHostFirstMessageDetail(Activity::class.java))
        val builder = argumentCaptor<ContentIntentBuilder>()
        verify(config).setContentIntentBuilder(builder.capture())
        return builder.firstValue
    }
}
