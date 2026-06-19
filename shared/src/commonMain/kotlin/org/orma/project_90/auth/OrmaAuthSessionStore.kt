package org.orma.project_90.auth

internal expect suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession?

internal expect suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession)

internal expect suspend fun clearOrmaStoredAuthSession()

internal expect suspend fun clearOrmaProviderAuthSession()

internal fun String?.toStoredOrmaAuthProvider(): OrmaAuthProvider = when (this) {
    "phone" -> OrmaAuthProvider.PhoneOtp
    "password" -> OrmaAuthProvider.EmailPassword
    "google.com" -> OrmaAuthProvider.Google
    else -> OrmaAuthProvider.PhoneOtp
}

internal val OrmaAuthProvider.storedProviderId: String
    get() = when (this) {
        OrmaAuthProvider.PhoneOtp -> "phone"
        OrmaAuthProvider.EmailPassword -> "password"
        OrmaAuthProvider.Google -> "google.com"
    }
