package org.orma.project_90.notifications

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaNotificationMessageObserver {
    // Desktop has no native FCM token source in the current JVM target.
}
