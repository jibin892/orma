package org.orma.project_90.notifications

data class OrmaNotificationMessage(
    val type: String = "",
    val orderId: String = "",
    val workspaceId: String = "",
)

class OrmaNotificationMessageObserver(
    private val disposeAction: () -> Unit,
) {
    fun dispose() = disposeAction()
}

expect fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver
