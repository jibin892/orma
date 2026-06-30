package org.orma.project_90.notifications

data class OrmaNotificationPermissionResult(
    val enabled: Boolean,
    val title: String,
    val message: String,
    val code: String? = null,
)

expect suspend fun currentOrmaNotificationPermission(): OrmaNotificationPermissionResult

expect suspend fun requestOrmaNotificationPermission(): OrmaNotificationPermissionResult
