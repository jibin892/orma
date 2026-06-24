package org.orma.project_90.notifications

import kotlin.js.Promise
import kotlinx.coroutines.await

actual suspend fun currentOrmaNotificationDeviceToken(): OrmaNotificationDeviceToken? {
    val result = requestWebNotificationDeviceToken().await()
    val token = result.token?.takeIf { it.isNotBlank() }
        ?: throw OrmaNotificationTokenException(
            title = result.errorTitle ?: "Web notifications are not connected",
            message = result.errorMessage
                ?: "ORMA could not create a browser notification token. Allow notifications and confirm the Firebase Web Push certificate key is configured.",
            code = result.errorCode ?: "WEB_NOTIFICATION_TOKEN_MISSING",
        )
    return OrmaNotificationDeviceToken(
        token = token,
        platform = "web",
        deviceName = result.deviceName?.takeIf { it.isNotBlank() } ?: "Web browser",
    )
}

private external interface JsNotificationDeviceTokenResult {
    val token: String?
    val deviceName: String?
    val errorTitle: String?
    val errorMessage: String?
    val errorCode: String?
}

private fun requestWebNotificationDeviceToken(): Promise<JsNotificationDeviceTokenResult> = js(
    """
    new Promise(function(resolve) {
      const bridge = typeof window !== 'undefined' ? window.OrmaFirebaseGoogleAuth : null;
      if (!bridge || typeof bridge.messagingToken !== 'function') {
        resolve({
          token: null,
          deviceName: null,
          errorTitle: 'Web notifications are not connected',
          errorMessage: 'The Firebase web messaging bridge is not available in this browser build.',
          errorCode: 'WEB_MESSAGING_BRIDGE_MISSING'
        });
        return;
      }
      bridge.messagingToken()
        .then(function(result) {
          resolve({
            token: result && result.token ? result.token : null,
            deviceName: result && result.deviceName ? result.deviceName : null,
            errorTitle: result && result.errorTitle ? result.errorTitle : null,
            errorMessage: result && result.errorMessage ? result.errorMessage : null,
            errorCode: result && result.errorCode ? result.errorCode : null
          });
        })
        .catch(function(error) {
          resolve({
            token: null,
            deviceName: null,
            errorTitle: 'Web notifications are not connected',
            errorMessage: error && error.message ? error.message : 'ORMA could not create a browser notification token.',
            errorCode: error && error.code ? error.code : 'WEB_NOTIFICATION_TOKEN_FAILED'
          });
        });
    })
    """,
)
