package org.orma.project_90.notifications

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaNotificationMessageObserver {
    // Android push delivery is handled by Firebase; foreground forwarding can attach here when needed.
}
