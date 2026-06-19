package org.orma.project_90.notifications

object OrmaAndroidNotificationPermissionRegistry {
    var requestPermission: (suspend () -> Boolean)? = null
}

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult {
    val granted = OrmaAndroidNotificationPermissionRegistry.requestPermission?.invoke()
    return when (granted) {
        true -> OrmaNotificationPermissionResult(
            enabled = true,
            title = "Notifications enabled",
            message = "ORMA can now send workspace updates on this device.",
        )
        false -> OrmaNotificationPermissionResult(
            enabled = false,
            title = "Notifications are off",
            message = "Allow notifications in system settings to receive invoice, order, tax, and workspace alerts.",
            code = "ANDROID_NOTIFICATION_PERMISSION_DENIED",
        )
        null -> OrmaNotificationPermissionResult(
            enabled = false,
            title = "Notifications are not ready",
            message = "The Android notification permission bridge is not installed on this screen.",
            code = "ANDROID_NOTIFICATION_PROVIDER_MISSING",
        )
    }
}
