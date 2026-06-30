package org.orma.project_90.notifications

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

actual suspend fun currentOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    suspendCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                val enabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                    settings?.authorizationStatus == UNAuthorizationStatusProvisional
                continuation.resume(
                    if (enabled) {
                        OrmaNotificationPermissionResult(
                            enabled = true,
                            title = "Notifications enabled",
                            message = "ORMA can send workspace updates on this iPhone or iPad.",
                        )
                    } else {
                        OrmaNotificationPermissionResult(
                            enabled = false,
                            title = "Notifications are off",
                            message = "Allow notifications in iOS settings to receive invoice, order, tax, and workspace alerts.",
                            code = "IOS_NOTIFICATION_PERMISSION_DENIED",
                        )
                    },
                )
            }
    }

actual suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult =
    suspendCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionSound or
            UNAuthorizationOptionBadge

        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, error ->
                if (granted) {
                    continuation.resume(
                        OrmaNotificationPermissionResult(
                            enabled = true,
                            title = "Notifications enabled",
                            message = "ORMA can now send workspace updates on this device.",
                        ),
                    )
                } else {
                    continuation.resume(
                        OrmaNotificationPermissionResult(
                            enabled = false,
                            title = "Notifications are off",
                            message = error?.localizedDescription
                                ?: "Allow notifications in iOS settings to receive invoice, order, tax, and workspace alerts.",
                            code = "IOS_NOTIFICATION_PERMISSION_DENIED",
                        ),
                    )
                }
            }
    }
