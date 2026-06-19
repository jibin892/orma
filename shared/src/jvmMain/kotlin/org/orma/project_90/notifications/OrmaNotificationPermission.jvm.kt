package org.orma.project_90.notifications

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    OrmaNotificationPermissionResult(
        enabled = true,
        title = "Notifications enabled",
        message = "ORMA notification preference is enabled for this desktop workspace.",
    )
