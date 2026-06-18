package com.orma.backend.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.orma.backend.config.AppConfig
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAuthNotConfiguredException : RuntimeException(
    "Firebase Auth is not configured. Set FIREBASE_PROJECT_ID and credentials before calling auth routes.",
)

data class VerifiedFirebaseUser(
    val uid: String,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val provider: String?,
)

class FirebaseTokenVerifier(
    private val config: AppConfig,
) {
    suspend fun verify(idToken: String): VerifiedFirebaseUser {
        if (!config.firebaseAuthConfigured) {
            throw FirebaseAuthNotConfiguredException()
        }

        val decodedToken = withContext(Dispatchers.IO) {
            FirebaseAuth.getInstance(firebaseApp()).verifyIdToken(idToken)
        }

        return VerifiedFirebaseUser(
            uid = decodedToken.uid,
            email = decodedToken.email,
            phoneNumber = decodedToken.claims["phone_number"] as? String,
            displayName = decodedToken.claims["name"] as? String,
            provider = (decodedToken.claims["firebase"] as? Map<*, *>)?.get("sign_in_provider") as? String,
        )
    }

    private fun firebaseApp(): FirebaseApp {
        FirebaseApp.getApps().firstOrNull { it.name == APP_NAME }?.let { return it }

        val optionsBuilder = FirebaseOptions.builder()
            .setProjectId(config.firebaseProjectId)

        val credentialsPath = config.firebaseCredentialsPath
        val credentials = if (credentialsPath.isNullOrBlank()) {
            GoogleCredentials.getApplicationDefault()
        } else {
            FileInputStream(credentialsPath).use { GoogleCredentials.fromStream(it) }
        }

        val options = optionsBuilder
            .setCredentials(credentials)
            .build()

        return FirebaseApp.initializeApp(options, APP_NAME)
    }

    private companion object {
        const val APP_NAME = "orma-backend"
    }
}
