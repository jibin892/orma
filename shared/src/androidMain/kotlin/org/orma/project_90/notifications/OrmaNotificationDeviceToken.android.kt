package org.orma.project_90.notifications

import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun currentOrmaNotificationDeviceToken(): OrmaNotificationDeviceToken? =
    suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                val token = task.result?.takeIf { task.isSuccessful && it.isNotBlank() }
                continuation.resume(
                    token?.let {
                        OrmaNotificationDeviceToken(
                            token = it,
                            platform = "android",
                            deviceName = listOfNotNull(Build.MANUFACTURER, Build.MODEL)
                                .joinToString(" ")
                                .trim()
                                .ifBlank { null },
                        )
                    },
                )
            }
    }
