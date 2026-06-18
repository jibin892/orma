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
    val allowedOrigins: List<String>,
) {
    val databaseConfigured: Boolean
        get() = !databaseUrl.isNullOrBlank()

    val firebaseAuthConfigured: Boolean
        get() = !firebaseProjectId.isNullOrBlank()

    companion object {
        fun load(env: Map<String, String> = System.getenv()): AppConfig {
            return AppConfig(
                host = env["HOST"].orEmpty().ifBlank { "0.0.0.0" },
                port = env["PORT"]?.toIntOrNull() ?: 8080,
                environment = env["ENVIRONMENT"].orEmpty().ifBlank { "local" },
                databaseUrl = env["DATABASE_URL"].orNullIfBlank(),
                databaseUser = env["DATABASE_USER"].orNullIfBlank(),
                databasePassword = env["DATABASE_PASSWORD"].orNullIfBlank(),
                runMigrations = env["RUN_MIGRATIONS"]?.toBooleanStrictOrNull() ?: false,
                firebaseProjectId = env["FIREBASE_PROJECT_ID"].orNullIfBlank(),
                firebaseCredentialsPath = env["FIREBASE_CREDENTIALS_PATH"].orNullIfBlank(),
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
            allowedOrigins = listOf("*"),
        )
    }
}

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
