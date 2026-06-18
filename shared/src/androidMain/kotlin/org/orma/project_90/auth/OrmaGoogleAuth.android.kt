package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig

object OrmaAndroidGoogleAuthRegistry {
    var requestIdToken: (suspend () -> OrmaGoogleSignInResult)? = null
}

actual suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult {
    val provider = OrmaAndroidGoogleAuthRegistry.requestIdToken
    return if (provider == null) {
        OrmaGoogleSignInResult.Failure(
            title = "Google sign-in not ready",
            message = "Android Google sign-in is not installed on this Activity yet.",
            code = "GOOGLE_ANDROID_PROVIDER_MISSING",
        )
    } else {
        provider()
    }
}
