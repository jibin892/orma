package org.orma.project_90.notifications

data class OrmaNotificationDeviceToken(
    val token: String,
    val platform: String,
    val deviceName: String? = null,
)

class OrmaNotificationTokenException(
    val title: String,
    override val message: String,
    val code: String? = null,
) : Exception(message)

expect suspend fun currentOrmaNotificationDeviceToken(): OrmaNotificationDeviceToken?
