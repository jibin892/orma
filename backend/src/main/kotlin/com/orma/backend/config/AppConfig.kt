package com.orma.backend.config

data class AppConfig(
    val host: String,
    val port: Int,
    val environment: String,
    val databaseUrl: String?,
    val databaseUser: String?,
    val databasePassword: String?,
    val runMigrations: Boolean,
    val firebaseProjectId: String?,
    val firebaseCredentialsPath: String?,
    val firebaseStorageBucket: String?,
    val mediaStorageProvider: String?,
    val cloudinaryCloudName: String?,
    val cloudinaryApiKey: String?,
    val cloudinaryApiSecret: String?,
    val gstinCheckApiKey: String?,
    val gstinCheckBaseUrl: String,
    val allowedOrigins: List<String>,
) {
    val databaseConfigured: Boolean
        get() = !databaseUrl.isNullOrBlank()

    val firebaseAuthConfigured: Boolean
        get() = !firebaseProjectId.isNullOrBlank()

    val firebaseStorageConfigured: Boolean
        get() = firebaseAuthConfigured && !firebaseStorageBucket.isNullOrBlank()

    val cloudinaryConfigured: Boolean
        get() = !cloudinaryCloudName.isNullOrBlank() &&
            !cloudinaryApiKey.isNullOrBlank() &&
            !cloudinaryApiSecret.isNullOrBlank()

    val activeMediaStorageProvider: String
        get() = mediaStorageProvider
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: when {
                cloudinaryConfigured -> "cloudinary"
                firebaseStorageConfigured -> "firebase"
                else -> "none"
            }

    val mediaStorageConfigured: Boolean
        get() = when (activeMediaStorageProvider) {
            "cloudinary" -> cloudinaryConfigured
            "firebase" -> firebaseStorageConfigured
            else -> false
        }

    val gstinCheckConfigured: Boolean
        get() = !gstinCheckApiKey.isNullOrBlank()

    companion object {
        fun load(env: Map<String, String> = System.getenv()): AppConfig {
            val firebaseProjectId = env["FIREBASE_PROJECT_ID"].orNullIfBlank()
            return AppConfig(
                host = env["HOST"].orEmpty().ifBlank { "0.0.0.0" },
                port = env["PORT"]?.toIntOrNull() ?: 8080,
                environment = env["ENVIRONMENT"].orEmpty().ifBlank { "local" },
                databaseUrl = env["DATABASE_URL"].orNullIfBlank(),
                databaseUser = env["DATABASE_USER"].orNullIfBlank(),
                databasePassword = env["DATABASE_PASSWORD"].orNullIfBlank(),
                runMigrations = env["RUN_MIGRATIONS"]?.toBooleanStrictOrNull() ?: false,
                firebaseProjectId = firebaseProjectId,
                firebaseCredentialsPath = env["FIREBASE_CREDENTIALS_PATH"].orNullIfBlank(),
                firebaseStorageBucket = env["FIREBASE_STORAGE_BUCKET"].orNullIfBlank()
                    ?: firebaseProjectId?.let { "$it.firebasestorage.app" },
                mediaStorageProvider = env["MEDIA_STORAGE_PROVIDER"].orNullIfBlank(),
                cloudinaryCloudName = env["CLOUDINARY_CLOUD_NAME"].orNullIfBlank(),
                cloudinaryApiKey = env["CLOUDINARY_API_KEY"].orNullIfBlank(),
                cloudinaryApiSecret = env["CLOUDINARY_API_SECRET"].orNullIfBlank(),
                gstinCheckApiKey = env["GSTINCHECK_API_KEY"].orNullIfBlank(),
                gstinCheckBaseUrl = env["GSTINCHECK_BASE_URL"].orEmpty()
                    .ifBlank { "https://sheet.gstincheck.co.in/check" },
                allowedOrigins = (
                    env["ALLOWED_ORIGINS"]
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty() + defaultAllowedOrigins
                    ).distinct(),
            )
        }

        private val defaultAllowedOrigins = listOf(
            "http://localhost:8080",
            "http://localhost:8081",
            "http://localhost:8090",
            "http://localhost:3000",
            "https://orma-web-dist-dev-api.vercel.app",
            "https://orma-web-dun.vercel.app",
        )

        fun test(): AppConfig = AppConfig(
            host = "127.0.0.1",
            port = 8080,
            environment = "test",
            databaseUrl = null,
            databaseUser = null,
            databasePassword = null,
            runMigrations = false,
            firebaseProjectId = null,
            firebaseCredentialsPath = null,
            firebaseStorageBucket = null,
            mediaStorageProvider = null,
            cloudinaryCloudName = null,
            cloudinaryApiKey = null,
            cloudinaryApiSecret = null,
            gstinCheckApiKey = null,
            gstinCheckBaseUrl = "https://sheet.gstincheck.co.in/check",
            allowedOrigins = listOf("*"),
        )
    }
}

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
