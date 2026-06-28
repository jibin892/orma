package org.orma.project_90

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OrmaFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "ORMA"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "New workspace update"

        showWorkspaceNotification(title = title, body = body, data = message.data)
    }

    private fun showWorkspaceNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureWorkspaceAlertChannel(manager)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        data.forEach { (key, value) -> launchIntent.putExtra(key, value) }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, WorkspaceAlertsChannelId)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(android.app.Notification.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setDefaults(android.app.Notification.DEFAULT_SOUND)
            .build()

        manager.notify(data["orderId"]?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureWorkspaceAlertChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            WorkspaceAlertsChannelId,
            "Workspace alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Catalog orders, booking requests, and workspace updates."
            setSound(defaultSound, audioAttributes)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val WorkspaceAlertsChannelId = "orma_workspace_alerts"
    }
}
