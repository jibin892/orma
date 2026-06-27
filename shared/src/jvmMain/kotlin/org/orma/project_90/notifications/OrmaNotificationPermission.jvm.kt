package org.orma.project_90.notifications

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    OrmaNotificationPermissionResult(
        enabled = true,
        title = "Desktop notifications enabled",
        message = "ORMA will show desktop alerts while this app is open.",
        code = null,
    )
