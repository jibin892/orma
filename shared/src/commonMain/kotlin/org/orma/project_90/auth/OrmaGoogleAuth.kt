package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig

sealed interface OrmaGoogleSignInResult {
    data class Success(
        val idToken: String,
    ) : OrmaGoogleSignInResult

    data class ExistingFirebaseSession(
        val session: OrmaAuthSession,
    ) : OrmaGoogleSignInResult

    data class Failure(
        val title: String,
        val message: String,
        val code: String,
    ) : OrmaGoogleSignInResult
}

expect suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult
