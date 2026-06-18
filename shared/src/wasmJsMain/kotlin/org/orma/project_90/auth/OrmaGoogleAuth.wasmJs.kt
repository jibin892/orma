package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig

actual suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult =
    OrmaGoogleSignInResult.Failure(
        title = "Google unavailable in Wasm",
        message = "The current Kotlin/Wasm target still needs a JavaScript auth bridge. Use the Kotlin/JS web build for browser Google sign-in.",
        code = "GOOGLE_WASM_BRIDGE_REQUIRED",
    )
