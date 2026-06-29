package org.orma.project_90.notifications

import kotlinx.coroutines.delay
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice

private const val OrmaIosApnsTokenKey = "orma.notifications.apnsToken"
private const val OrmaIosApnsRegistrationErrorKey = "orma.notifications.apnsRegistrationError"

actual suspend fun currentOrmaNotificationDeviceToken(externalUserId: String?): OrmaNotificationDeviceToken? {
    repeat(20) {
        apnsToken()?.let { token ->
            return OrmaNotificationDeviceToken(
                token = "apns:$token",
                platform = "ios",
                deviceName = UIDevice.currentDevice.name,
            )
        }
        delay(350)
    }
    val registrationError = NSUserDefaults.standardUserDefaults
        .stringForKey(OrmaIosApnsRegistrationErrorKey)
        ?.takeIf { it.isNotBlank() }
    throw OrmaNotificationTokenException(
        title = "iOS push is not registered",
        message = registrationError
            ?: "iOS accepted notification permission, but APNs has not returned a device token yet. Check Push Notifications capability, aps-environment entitlement, and the Apple provisioning profile, then retry.",
        code = "IOS_APNS_TOKEN_UNAVAILABLE",
    )
}

private fun apnsToken(): String? =
    NSUserDefaults.standardUserDefaults
        .stringForKey(OrmaIosApnsTokenKey)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
