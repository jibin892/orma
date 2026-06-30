package org.orma.project_90.notifications

actual suspend fun currentOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    OrmaNotificationPermissionResult(
        enabled = false,
        title = "Notifications are not available",
        message = "The Kotlin/Wasm target still needs its browser notification bridge. Use Android, iOS, Desktop, or Kotlin/JS web for notification setup.",
        code = "WASM_NOTIFICATION_BRIDGE_REQUIRED",
    )

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    OrmaNotificationPermissionResult(
        enabled = false,
        title = "Notifications are not available",
        message = "The Kotlin/Wasm target still needs its browser notification bridge. Use Android, iOS, Desktop, or Kotlin/JS web for notification setup.",
        code = "WASM_NOTIFICATION_BRIDGE_REQUIRED",
    )
