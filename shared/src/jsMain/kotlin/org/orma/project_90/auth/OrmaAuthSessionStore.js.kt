package org.orma.project_90.auth

import kotlin.js.Promise
import kotlinx.browser.window
import kotlinx.coroutines.await

private const val OrmaAuthStorePrefix = "orma.auth.session."

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    localOrmaStoredAuthSession()?.let { return it }
    val firebaseSession = restoreWebFirebaseSession().await().toOrmaAuthSessionOrNull()
    if (firebaseSession != null) {
        saveOrmaStoredAuthSession(firebaseSession)
    }
    return firebaseSession
}

private fun localOrmaStoredAuthSession(): OrmaAuthSession? {
    val refreshToken = localStorageGet(OrmaAuthStorePrefix + "refreshToken").orEmpty()
    val idToken = localStorageGet(OrmaAuthStorePrefix + "idToken").orEmpty()
    val uid = localStorageGet(OrmaAuthStorePrefix + "uid").orEmpty()
    if (refreshToken.isBlank() && idToken.isBlank()) return null
    return OrmaAuthSession(
        uid = uid,
        provider = localStorageGet(OrmaAuthStorePrefix + "provider").toStoredOrmaAuthProvider(),
        idToken = idToken,
        refreshToken = refreshToken,
        email = localStorageGet(OrmaAuthStorePrefix + "email").orEmpty().ifBlank { null },
        phoneNumber = localStorageGet(OrmaAuthStorePrefix + "phoneNumber").orEmpty().ifBlank { null },
        displayName = localStorageGet(OrmaAuthStorePrefix + "displayName").orEmpty().ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    localStorageSet(OrmaAuthStorePrefix + "uid", session.uid)
    localStorageSet(OrmaAuthStorePrefix + "provider", session.provider.storedProviderId)
    localStorageSet(OrmaAuthStorePrefix + "idToken", session.idToken)
    localStorageSet(OrmaAuthStorePrefix + "refreshToken", session.refreshToken)
    localStorageSet(OrmaAuthStorePrefix + "email", session.email.orEmpty())
    localStorageSet(OrmaAuthStorePrefix + "phoneNumber", session.phoneNumber.orEmpty())
    localStorageSet(OrmaAuthStorePrefix + "displayName", session.displayName.orEmpty())
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
    ).forEach { key ->
        localStorageRemove(OrmaAuthStorePrefix + key)
    }
    signOutWebFirebaseSession().await()
}

internal actual suspend fun clearOrmaProviderAuthSession() = Unit

private fun localStorageGet(key: String): String? =
    window.localStorage.getItem(key)

private fun localStorageSet(key: String, value: String) {
    window.localStorage.setItem(key, value)
}

private fun localStorageRemove(key: String) {
    window.localStorage.removeItem(key)
}

private external interface JsFirebaseSessionResult {
    val firebaseIdToken: String?
    val refreshToken: String?
    val uid: String?
    val email: String?
    val phoneNumber: String?
    val displayName: String?
    val providerId: String?
}

private fun JsFirebaseSessionResult?.toOrmaAuthSessionOrNull(): OrmaAuthSession? {
    val result = this ?: return null
    val idToken = result.firebaseIdToken.orEmpty()
    if (idToken.isBlank()) return null
    val phoneNumber = result.phoneNumber.orEmpty().ifBlank { null }
    val email = result.email.orEmpty().ifBlank { null }
    return OrmaAuthSession(
        uid = result.uid.orEmpty(),
        provider = when {
            result.providerId == "phone" || phoneNumber != null -> OrmaAuthProvider.PhoneOtp
            result.providerId == "password" -> OrmaAuthProvider.EmailPassword
            else -> OrmaAuthProvider.Google
        },
        idToken = idToken,
        refreshToken = result.refreshToken.orEmpty(),
        email = email,
        phoneNumber = phoneNumber,
        displayName = result.displayName.orEmpty().ifBlank { null },
    )
}

private fun restoreWebFirebaseSession(): Promise<JsFirebaseSessionResult?> = js(
    """
    (window.OrmaFirebaseGoogleAuth && window.OrmaFirebaseGoogleAuth.restoreSession)
      ? window.OrmaFirebaseGoogleAuth.restoreSession()
      : Promise.resolve({})
    """,
)

private fun signOutWebFirebaseSession(): Promise<JsFirebaseSessionResult?> = js(
    """
    (window.OrmaFirebaseGoogleAuth && window.OrmaFirebaseGoogleAuth.signOut)
      ? window.OrmaFirebaseGoogleAuth.signOut()
      : Promise.resolve({})
    """,
)
