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
    val metaWebhookVerifyToken: String?,
    val metaAppId: String?,
    val metaAppSecret: String?,
    val metaRedirectUri: String?,
    val metaGraphApiVersion: String,
    val metaTokenEncryptionSecret: String?,
    val metaSystemUserAccessToken: String?,
    val metaOAuthSuccessRedirectUrl: String?,
    val metaDefaultOrderTemplate: String,
    val metaDefaultLanguageCode: String,
    val metaOAuthScopes: List<String>,
    val allowedOrigins: List<String>,
) {
    val databaseConfigured: Boolean
        get() = !databaseUrl.isNullOrBlank()

    val firebaseAuthConfigured: Boolean
        get() = !firebaseProjectId.isNullOrBlank()

    val firebaseMessagingConfigured: Boolean
        get() = firebaseAuthConfigured

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

    val metaWebhookConfigured: Boolean
        get() = !metaWebhookVerifyToken.isNullOrBlank()

    val metaOAuthConfigured: Boolean
        get() = !metaAppId.isNullOrBlank() &&
            !metaAppSecret.isNullOrBlank() &&
            !metaRedirectUri.isNullOrBlank()

    val metaTokenStorageConfigured: Boolean
        get() = !metaTokenEncryptionSecret.isNullOrBlank()

    val metaSystemUserTokenConfigured: Boolean
        get() = !metaSystemUserAccessToken.isNullOrBlank()

    val metaBackendConfigured: Boolean
        get() = metaOAuthConfigured || metaSystemUserTokenConfigured

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
                metaWebhookVerifyToken = env["META_WEBHOOK_VERIFY_TOKEN"].orNullIfBlank(),
                metaAppId = env["META_APP_ID"].orNullIfBlank(),
                metaAppSecret = env["META_APP_SECRET"].orNullIfBlank(),
                metaRedirectUri = env["META_REDIRECT_URI"].orNullIfBlank(),
                metaGraphApiVersion = env["META_GRAPH_API_VERSION"].orEmpty().ifBlank { "v20.0" },
                metaTokenEncryptionSecret = env["META_TOKEN_ENCRYPTION_SECRET"].orNullIfBlank(),
                metaSystemUserAccessToken = env["META_SYSTEM_USER_ACCESS_TOKEN"].orNullIfBlank(),
                metaOAuthSuccessRedirectUrl = env["META_OAUTH_SUCCESS_REDIRECT_URL"].orNullIfBlank(),
                metaDefaultOrderTemplate = env["META_DEFAULT_ORDER_TEMPLATE"].orEmpty().ifBlank { "orma_order_update" },
                metaDefaultLanguageCode = env["META_DEFAULT_LANGUAGE_CODE"].orEmpty().ifBlank { "en_US" },
                metaOAuthScopes = env["META_OAUTH_SCOPES"]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    ?.takeIf { it.isNotEmpty() }
                    ?: defaultMetaOAuthScopes,
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

        private val defaultMetaOAuthScopes = listOf(
            "business_management",
            "catalog_management",
            "whatsapp_business_management",
            "whatsapp_business_messaging",
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
            metaWebhookVerifyToken = null,
            metaAppId = null,
            metaAppSecret = null,
            metaRedirectUri = null,
            metaGraphApiVersion = "v20.0",
            metaTokenEncryptionSecret = null,
            metaSystemUserAccessToken = null,
            metaOAuthSuccessRedirectUrl = null,
            metaDefaultOrderTemplate = "orma_order_update",
            metaDefaultLanguageCode = "en_US",
            metaOAuthScopes = defaultMetaOAuthScopes,
            allowedOrigins = listOf("*"),
        )
    }
}

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
