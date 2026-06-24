package org.orma.project_90.notifications

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    OrmaNotificationPermissionResult(
        enabled = false,
        title = "Desktop notifications are not connected",
        message = "Desktop push needs a native notification bridge before ORMA can receive workspace alerts on this device. Use Android or web notifications for now.",
        code = "DESKTOP_NOTIFICATION_TOKEN_UNAVAILABLE",
    )
