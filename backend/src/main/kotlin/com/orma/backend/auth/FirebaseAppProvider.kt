package com.orma.backend.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.orma.backend.config.AppConfig
import java.io.FileInputStream

object FirebaseAppProvider {
    fun app(config: AppConfig): FirebaseApp {
        FirebaseApp.getApps().firstOrNull { it.name == AppName }?.let { return it }

        val optionsBuilder = FirebaseOptions.builder()
            .setProjectId(config.firebaseProjectId)

        config.firebaseStorageBucket
            ?.takeIf { it.isNotBlank() }
            ?.let(optionsBuilder::setStorageBucket)

        val credentialsPath = config.firebaseCredentialsPath
        val credentials = if (credentialsPath.isNullOrBlank()) {
            GoogleCredentials.getApplicationDefault()
        } else {
            FileInputStream(credentialsPath).use { GoogleCredentials.fromStream(it) }
        }

        val options = optionsBuilder
            .setCredentials(credentials)
            .build()

        return FirebaseApp.initializeApp(options, AppName)
    }

    private const val AppName = "orma-backend"
}
