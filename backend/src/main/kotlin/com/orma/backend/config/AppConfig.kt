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
    val allowedOrigins: List<String>,
) {
    val databaseConfigured: Boolean
        get() = !databaseUrl.isNullOrBlank()

    val firebaseAuthConfigured: Boolean
        get() = !firebaseProjectId.isNullOrBlank()

    val firebaseStorageConfigured: Boolean
        get() = firebaseAuthConfigured && !firebaseStorageBucket.isNullOrBlank()

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
                allowedOrigins = env["ALLOWED_ORIGINS"]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: listOf("http://localhost:8080"),
            )
        }

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
            allowedOrigins = listOf("*"),
        )
    }
}

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
