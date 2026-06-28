package org.orma.project_90.notifications

import kotlin.js.Promise
import kotlinx.coroutines.await

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult {
    val result = requestWebNotificationPermission().await()
    return when (result.permission) {
        "granted" -> OrmaNotificationPermissionResult(
            enabled = true,
            title = "Notifications enabled",
            message = "ORMA can now send OneSignal workspace updates in this browser.",
        )
        "denied" -> OrmaNotificationPermissionResult(
            enabled = false,
            title = "Notifications are blocked",
            message = "Allow notifications for this site in browser settings to receive invoice, order, tax, and workspace alerts.",
            code = "WEB_NOTIFICATION_PERMISSION_DENIED",
        )
        "unsupported" -> OrmaNotificationPermissionResult(
            enabled = false,
            title = "Notifications are not supported",
            message = "This browser does not support web push notifications for ORMA.",
            code = "WEB_NOTIFICATION_UNSUPPORTED",
        )
        else -> OrmaNotificationPermissionResult(
            enabled = false,
            title = "Notifications are off",
            message = result.errorMessage ?: "Allow notifications when the browser asks to receive ORMA workspace alerts.",
            code = "WEB_NOTIFICATION_PERMISSION_DEFAULT",
        )
    }
}

private external interface JsNotificationPermissionResult {
    val permission: String
    val errorMessage: String?
}

private fun requestWebNotificationPermission(): Promise<JsNotificationPermissionResult> = js(
    """
    new Promise(function(resolve) {
      if (typeof window === 'undefined' || !('Notification' in window)) {
        resolve({ permission: 'unsupported', errorMessage: null });
        return;
      }
      if (Notification.permission === 'granted' || Notification.permission === 'denied') {
        resolve({ permission: Notification.permission, errorMessage: null });
        return;
      }
      Notification.requestPermission()
        .then(function(permission) {
          resolve({ permission: permission, errorMessage: null });
        })
        .catch(function(error) {
          resolve({
            permission: 'error',
            errorMessage: error && error.message ? error.message : String(error)
          });
        });
    })
    """,
)
