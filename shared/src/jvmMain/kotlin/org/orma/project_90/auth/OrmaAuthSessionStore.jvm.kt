package org.orma.project_90.auth

import java.util.prefs.Preferences

private val ormaAuthPreferences: Preferences =
    Preferences.userRoot().node("org/orma/project_90/auth/session")

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    val refreshToken = ormaAuthPreferences.get("refreshToken", "").orEmpty()
    val idToken = ormaAuthPreferences.get("idToken", "").orEmpty()
    if (refreshToken.isBlank() && idToken.isBlank()) return null
    return OrmaAuthSession(
        uid = ormaAuthPreferences.get("uid", "").orEmpty(),
        provider = ormaAuthPreferences.get("provider", null).toStoredOrmaAuthProvider(),
        idToken = idToken,
        refreshToken = refreshToken,
        email = ormaAuthPreferences.get("email", "").orEmpty().ifBlank { null },
        phoneNumber = ormaAuthPreferences.get("phoneNumber", "").orEmpty().ifBlank { null },
        displayName = ormaAuthPreferences.get("displayName", "").orEmpty().ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    ormaAuthPreferences.put("uid", session.uid)
    ormaAuthPreferences.put("provider", session.provider.storedProviderId)
    ormaAuthPreferences.put("idToken", session.idToken)
    ormaAuthPreferences.put("refreshToken", session.refreshToken)
    ormaAuthPreferences.put("email", session.email.orEmpty())
    ormaAuthPreferences.put("phoneNumber", session.phoneNumber.orEmpty())
    ormaAuthPreferences.put("displayName", session.displayName.orEmpty())
    ormaAuthPreferences.flush()
}

internal actual suspend fun clearOrmaStoredAuthSession() {
    listOf(
        "uid",
        "provider",
        "idToken",
        "refreshToken",
        "email",
        "phoneNumber",
        "displayName",
    ).forEach(ormaAuthPreferences::remove)
    ormaAuthPreferences.flush()
}

internal actual suspend fun clearOrmaProviderAuthSession() = Unit
