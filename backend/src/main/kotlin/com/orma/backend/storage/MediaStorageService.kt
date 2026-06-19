package com.orma.backend.storage

import com.orma.backend.config.AppConfig

open class MediaStorageNotConfiguredException(
    val providerName: String,
    message: String,
) : RuntimeException(message)

data class StoredMediaObject(
    val storagePath: String,
    val downloadUrl: String?,
    val contentType: String,
    val sizeBytes: Long,
)

interface MediaStorageService {
    val providerName: String

    suspend fun upload(
        storagePath: String,
        bytes: ByteArray,
        contentType: String,
    ): StoredMediaObject
}

fun createMediaStorageService(config: AppConfig): MediaStorageService =
    when (config.activeMediaStorageProvider) {
        "cloudinary" -> CloudinaryStorageService(config)
        "firebase" -> FirebaseStorageService(config)
        else -> UnavailableMediaStorageService(config.activeMediaStorageProvider)
    }

private class UnavailableMediaStorageService(
    override val providerName: String,
) : MediaStorageService {
    override suspend fun upload(
        storagePath: String,
        bytes: ByteArray,
        contentType: String,
    ): StoredMediaObject {
        throw MediaStorageNotConfiguredException(
            providerName = providerName,
            message = "Media storage is not configured. Set MEDIA_STORAGE_PROVIDER and the provider credentials.",
        )
    }
}
