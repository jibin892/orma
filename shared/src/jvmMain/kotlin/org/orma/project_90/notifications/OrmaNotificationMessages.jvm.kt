package org.orma.project_90.notifications

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaNotificationMessageObserver {
    // Desktop keeps using ORMA's in-app refresh path; OneSignal native desktop push is not wired in this JVM target.
}
