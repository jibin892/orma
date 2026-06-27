package org.orma.project_90.notifications

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaNotificationMessageObserver {
    // iOS push token support is not connected in the current target.
}
