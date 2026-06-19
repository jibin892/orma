package org.orma.project_90.auth

import platform.Foundation.NSUserDefaults

private const val OrmaAuthStorePrefix = "orma.auth.session."

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    val defaults = NSUserDefaults.standardUserDefaults
    val refreshToken = defaults.stringForKey(OrmaAuthStorePrefix + "refreshToken").orEmpty()
    val idToken = defaults.stringForKey(OrmaAuthStorePrefix + "idToken").orEmpty()
    if (refreshToken.isBlank() && idToken.isBlank()) return null
    return OrmaAuthSession(
        uid = defaults.stringForKey(OrmaAuthStorePrefix + "uid").orEmpty(),
        provider = defaults.stringForKey(OrmaAuthStorePrefix + "provider").toStoredOrmaAuthProvider(),
        idToken = idToken,
        refreshToken = refreshToken,
        email = defaults.stringForKey(OrmaAuthStorePrefix + "email").orEmpty().ifBlank { null },
        phoneNumber = defaults.stringForKey(OrmaAuthStorePrefix + "phoneNumber").orEmpty().ifBlank { null },
        displayName = defaults.stringForKey(OrmaAuthStorePrefix + "displayName").orEmpty().ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setObject(session.uid, OrmaAuthStorePrefix + "uid")
    defaults.setObject(session.provider.storedProviderId, OrmaAuthStorePrefix + "provider")
    defaults.setObject(session.idToken, OrmaAuthStorePrefix + "idToken")
    defaults.setObject(session.refreshToken, OrmaAuthStorePrefix + "refreshToken")
    defaults.setObject(session.email.orEmpty(), OrmaAuthStorePrefix + "email")
    defaults.setObject(session.phoneNumber.orEmpty(), OrmaAuthStorePrefix + "phoneNumber")
    defaults.setObject(session.displayName.orEmpty(), OrmaAuthStorePrefix + "displayName")
    defaults.synchronize()
}

internal actual suspend fun clearOrmaStoredAuthSession() {
    val defaults = NSUserDefaults.standardUserDefaults
    listOf(
        "uid",
        "provider",
        "idToken",
        "refreshToken",
        "email",
        "phoneNumber",
        "displayName",
    ).forEach { key ->
        defaults.removeObjectForKey(OrmaAuthStorePrefix + key)
    }
    defaults.synchronize()
}

internal actual suspend fun clearOrmaProviderAuthSession() = Unit
