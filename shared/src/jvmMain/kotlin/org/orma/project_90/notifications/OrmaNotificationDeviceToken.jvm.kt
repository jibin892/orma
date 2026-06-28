package org.orma.project_90.notifications

import java.net.InetAddress
import java.util.UUID
import java.util.prefs.Preferences

actual suspend fun currentOrmaNotificationDeviceToken(externalUserId: String?): OrmaNotificationDeviceToken? {
    val preferences = Preferences.userRoot().node("org/orma/project_90/notifications")
    val installationId = preferences.get("desktop_installation_id", null)
        ?: UUID.randomUUID().toString().also { preferences.put("desktop_installation_id", it) }
    return OrmaNotificationDeviceToken(
        token = "desktop:$installationId",
        platform = "desktop",
        deviceName = desktopDeviceName(),
    )
}

private fun desktopDeviceName(): String {
    val hostName = runCatching { InetAddress.getLocalHost().hostName }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
    val userName = System.getProperty("user.name")
        ?.takeIf { it.isNotBlank() }
    return listOfNotNull(hostName, userName)
        .joinToString(" / ")
        .ifBlank { "Desktop app" }
        .take(120)
}
