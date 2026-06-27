package org.orma.project_90.notifications

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaNotificationMessageObserver {
    // Web push is wired through the JS target used by the current web app.
}
