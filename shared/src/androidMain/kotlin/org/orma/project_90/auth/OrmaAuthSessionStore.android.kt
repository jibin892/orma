package org.orma.project_90.auth

import android.content.Context

object OrmaAndroidAuthSessionStore {
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
    }

    internal fun preferences() =
        appContext?.getSharedPreferences("orma_auth_session", Context.MODE_PRIVATE)
}

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    val preferences = OrmaAndroidAuthSessionStore.preferences() ?: return null
    val refreshToken = preferences.getString("refreshToken", "").orEmpty()
    val idToken = preferences.getString("idToken", "").orEmpty()
    if (refreshToken.isBlank() && idToken.isBlank()) return null
    return OrmaAuthSession(
        uid = preferences.getString("uid", "").orEmpty(),
        provider = preferences.getString("provider", null).toStoredOrmaAuthProvider(),
        idToken = idToken,
        refreshToken = refreshToken,
        email = preferences.getString("email", "").orEmpty().ifBlank { null },
        phoneNumber = preferences.getString("phoneNumber", "").orEmpty().ifBlank { null },
        displayName = preferences.getString("displayName", "").orEmpty().ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    val preferences = OrmaAndroidAuthSessionStore.preferences() ?: return
    preferences.edit()
        .putString("uid", session.uid)
        .putString("provider", session.provider.storedProviderId)
        .putString("idToken", session.idToken)
        .putString("refreshToken", session.refreshToken)
        .putString("email", session.email.orEmpty())
        .putString("phoneNumber", session.phoneNumber.orEmpty())
        .putString("displayName", session.displayName.orEmpty())
        .commit()
}

internal actual suspend fun clearOrmaStoredAuthSession() {
    OrmaAndroidAuthSessionStore.preferences()?.edit()?.clear()?.commit()
}
