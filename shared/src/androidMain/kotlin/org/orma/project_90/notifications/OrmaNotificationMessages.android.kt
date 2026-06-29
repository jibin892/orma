package org.orma.project_90.notifications

import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener

object OrmaAndroidNotificationClickBridge {
    private val listeners = mutableSetOf<(OrmaNotificationMessage) -> Unit>()
    private val pendingMessages = mutableListOf<OrmaNotificationMessage>()
    private var installed = false
    private val clickListener = object : INotificationClickListener {
        override fun onClick(event: INotificationClickEvent) {
            val data = event.notification.additionalData
            val message = OrmaNotificationMessage(
                type = data?.optString("type").orEmpty(),
                orderId = data?.optString("orderId").orEmpty(),
                workspaceId = data?.optString("workspaceId").orEmpty(),
            )
            if (message.type.isBlank() && message.orderId.isBlank()) return
            dispatch(message)
        }
    }

    fun install() {
        synchronized(this) {
            if (installed) return
            installed = true
        }
        OneSignal.Notifications.addClickListener(clickListener)
    }

    fun observe(onMessage: (OrmaNotificationMessage) -> Unit): OrmaNotificationMessageObserver {
        val pending = synchronized(this) {
            listeners += onMessage
            pendingMessages.toList().also { pendingMessages.clear() }
        }
        pending.forEach(onMessage)
        return OrmaNotificationMessageObserver {
            synchronized(this) {
                listeners -= onMessage
            }
        }
    }

    private fun dispatch(message: OrmaNotificationMessage) {
        val activeListeners = synchronized(this) {
            if (listeners.isEmpty()) {
                pendingMessages += message
                emptyList()
            } else {
                listeners.toList()
            }
        }
        activeListeners.forEach { it(message) }
    }
}

actual fun observeOrmaNotificationMessages(
    onMessage: (OrmaNotificationMessage) -> Unit,
): OrmaNotificationMessageObserver = OrmaAndroidNotificationClickBridge.observe(onMessage)
