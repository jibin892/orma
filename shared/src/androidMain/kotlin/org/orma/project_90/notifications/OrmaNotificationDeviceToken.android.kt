package org.orma.project_90.notifications

import android.os.Build
import com.onesignal.OneSignal

actual suspend fun currentOrmaNotificationDeviceToken(externalUserId: String?): OrmaNotificationDeviceToken? {
    val cleanExternalUserId = externalUserId?.trim()?.takeIf { it.isNotBlank() }
        ?: throw OrmaNotificationTokenException(
            title = "Notifications are not connected",
            message = "ORMA could not connect this Android device to the signed-in OneSignal user. Sign in again and retry.",
            code = "ONESIGNAL_EXTERNAL_USER_MISSING",
        )
    runCatching {
        OneSignal.login(cleanExternalUserId)
    }.onFailure { error ->
        throw OrmaNotificationTokenException(
            title = "OneSignal is not connected",
            message = error.message ?: "ORMA could not register this Android user with OneSignal.",
            code = "ONESIGNAL_ANDROID_LOGIN_FAILED",
        )
    }
    return OrmaNotificationDeviceToken(
        token = "onesignal-external:$cleanExternalUserId",
        platform = "android",
        deviceName = listOfNotNull(Build.MANUFACTURER, Build.MODEL)
            .joinToString(" ")
            .trim()
            .ifBlank { null },
    )
}
