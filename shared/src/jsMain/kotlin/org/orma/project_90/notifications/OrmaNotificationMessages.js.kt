package org.orma.project_90.notifications

import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver {
    val listener = EventListener { event ->
        val data = notificationPayloadData(event)
        onMessage(
            OrmaNotificationMessage(
                type = data.type.orEmpty(),
                orderId = data.orderId.orEmpty(),
                workspaceId = data.workspaceId.orEmpty(),
            ),
        )
    }
    window.addEventListener("orma:fcm-message", listener)
    return OrmaNotificationMessageObserver {
        window.removeEventListener("orma:fcm-message", listener)
    }
}

private external interface JsOrmaNotificationPayloadData {
    val type: String?
    val orderId: String?
    val workspaceId: String?
}

private fun notificationPayloadData(event: Event): JsOrmaNotificationPayloadData = js(
    """
    (function(event) {
      const detail = event && event.detail ? event.detail : {};
      const payload = detail.payload || detail;
      return (payload && payload.data) || detail.data || {};
    })(event)
    """,
)
