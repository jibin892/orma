package com.orma.backend.storage

import com.google.firebase.cloud.StorageClient
import com.orma.backend.auth.FirebaseAppProvider
import com.orma.backend.config.AppConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseStorageNotConfiguredException : RuntimeException(
    "Firebase Storage is not configured. Set FIREBASE_PROJECT_ID, credentials, and FIREBASE_STORAGE_BUCKET.",
)

data class StoredMediaObject(
    val storagePath: String,
    val downloadUrl: String?,
    val contentType: String,
    val sizeBytes: Long,
)

class FirebaseStorageService(
    private val config: AppConfig,
) {
    suspend fun upload(
        storagePath: String,
        bytes: ByteArray,
        contentType: String,
    ): StoredMediaObject {
        if (!config.firebaseStorageConfigured) {
            throw FirebaseStorageNotConfiguredException()
        }

        return withContext(Dispatchers.IO) {
            val bucket = StorageClient.getInstance(FirebaseAppProvider.app(config)).bucket()
            val blob = bucket.create(storagePath, bytes, contentType)
            StoredMediaObject(
                storagePath = storagePath,
                downloadUrl = blob.signedUrlOrNull(),
                contentType = contentType,
                sizeBytes = bytes.size.toLong(),
            )
        }
    }

    private fun com.google.cloud.storage.Blob.signedUrlOrNull(): String? =
        runCatching {
            signUrl(7, TimeUnit.DAYS).toExternalForm()
        }.getOrNull()
}
