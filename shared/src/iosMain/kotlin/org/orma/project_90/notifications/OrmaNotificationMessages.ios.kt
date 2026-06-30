package org.orma.project_90.notifications

import platform.Foundation.NSDate
import platform.Foundation.NSTimer
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

private const val OrmaIosNotificationMessageSequenceKey = "orma.notifications.lastMessageSequence"
private const val OrmaIosNotificationMessageTypeKey = "orma.notifications.lastMessageType"
private const val OrmaIosNotificationMessageOrderIdKey = "orma.notifications.lastMessageOrderId"
private const val OrmaIosNotificationMessageWorkspaceIdKey = "orma.notifications.lastMessageWorkspaceId"
private const val OrmaIosNotificationMessageTimestampKey = "orma.notifications.lastMessageTimestamp"
private const val OrmaIosNotificationTapMaxAgeSeconds = 600.0

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver {
    val defaults = NSUserDefaults.standardUserDefaults
    var lastSequence = (defaults.integerForKey(OrmaIosNotificationMessageSequenceKey) - 1).coerceAtLeast(0)

    fun consumePendingTap() {
        val sequence = defaults.integerForKey(OrmaIosNotificationMessageSequenceKey)
        if (sequence <= lastSequence) return
        lastSequence = sequence

        val timestamp = defaults.doubleForKey(OrmaIosNotificationMessageTimestampKey)
        if (timestamp > 0.0 && NSDate().timeIntervalSince1970 - timestamp > OrmaIosNotificationTapMaxAgeSeconds) {
            clearPendingTap(defaults)
            return
        }

        val type = defaults.stringForKey(OrmaIosNotificationMessageTypeKey).orEmpty()
        val orderId = defaults.stringForKey(OrmaIosNotificationMessageOrderIdKey).orEmpty()
        val workspaceId = defaults.stringForKey(OrmaIosNotificationMessageWorkspaceIdKey).orEmpty()
        if (type.isBlank() && orderId.isBlank()) return

        onMessage(
            OrmaNotificationMessage(
                type = type,
                orderId = orderId,
                workspaceId = workspaceId,
            ),
        )
        clearPendingTap(defaults)
    }

    consumePendingTap()
    val timer = NSTimer.scheduledTimerWithTimeInterval(
        interval = 0.35,
        repeats = true,
    ) { _ ->
        consumePendingTap()
    }
    return OrmaNotificationMessageObserver {
        timer.invalidate()
    }
}

private fun clearPendingTap(defaults: NSUserDefaults) {
    defaults.removeObjectForKey(OrmaIosNotificationMessageTypeKey)
    defaults.removeObjectForKey(OrmaIosNotificationMessageOrderIdKey)
    defaults.removeObjectForKey(OrmaIosNotificationMessageWorkspaceIdKey)
    defaults.removeObjectForKey(OrmaIosNotificationMessageTimestampKey)
}
