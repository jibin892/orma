package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.MediaUploadResponse
import com.orma.backend.storage.MediaStorageNotConfiguredException
import com.orma.backend.storage.MediaStorageService
import com.orma.backend.storage.StoredMediaObject
import com.orma.backend.storage.createMediaStorageService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.utils.io.readRemaining
import java.util.UUID
import kotlinx.io.readByteArray

fun Route.mediaRoutes(
    config: AppConfig,
    onboardingRepository: OnboardingRepository?,
    storageService: MediaStorageService = createMediaStorageService(config),
) {
    post("/media/business-logo") {
        val repository = onboardingRepository ?: return@post call.respondDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val upload = call.receiveImageUpload(requireProductId = false) ?: return@post

        val session = repository.resolveSession(
            firebaseUser = firebaseUser,
            providerFallback = upload.fields["provider"],
            emailFallback = upload.fields["email"],
            phoneNumberFallback = upload.fields["phoneNumber"],
            displayNameFallback = upload.fields["displayName"],
        )
        val workspaceId = session.workspace?.id
        val storagePath = if (workspaceId == null) {
            "users/${firebaseUser.uid}/business-logo/${UUID.randomUUID()}.${upload.extension}"
        } else {
            "business/$workspaceId/logo/${UUID.randomUUID()}.${upload.extension}"
        }

        val stored = call.storeImageOrRespond(storageService, storagePath, upload) ?: return@post
        if (workspaceId != null) {
            repository.saveBusinessLogo(workspaceId, stored.storagePath)
        }

        call.respond(
            MediaUploadResponse(
                type = "business_logo",
                workspaceId = workspaceId,
                storagePath = stored.storagePath,
                downloadUrl = stored.downloadUrl,
                contentType = stored.contentType,
                sizeBytes = stored.sizeBytes,
            ),
        )
    }

    post("/media/business-cover") {
        val repository = onboardingRepository ?: return@post call.respondDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val upload = call.receiveImageUpload(requireProductId = false) ?: return@post

        val session = repository.resolveSession(
            firebaseUser = firebaseUser,
            providerFallback = upload.fields["provider"],
            emailFallback = upload.fields["email"],
            phoneNumberFallback = upload.fields["phoneNumber"],
            displayNameFallback = upload.fields["displayName"],
        )
        val workspaceId = session.workspace?.id
        val storagePath = if (workspaceId == null) {
            "users/${firebaseUser.uid}/business-cover/${UUID.randomUUID()}.${upload.extension}"
        } else {
            "business/$workspaceId/cover/${UUID.randomUUID()}.${upload.extension}"
        }

        val stored = call.storeImageOrRespond(storageService, storagePath, upload) ?: return@post
        if (workspaceId != null) {
            repository.saveBusinessCover(workspaceId, stored.storagePath)
        }

        call.respond(
            MediaUploadResponse(
                type = "business_cover",
                workspaceId = workspaceId,
                storagePath = stored.storagePath,
                downloadUrl = stored.downloadUrl,
                contentType = stored.contentType,
                sizeBytes = stored.sizeBytes,
            ),
        )
    }

    post("/media/receipt-logo") {
        val repository = onboardingRepository ?: return@post call.respondDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val upload = call.receiveImageUpload(requireProductId = false) ?: return@post

        val session = repository.resolveSession(
            firebaseUser = firebaseUser,
            providerFallback = upload.fields["provider"],
            emailFallback = upload.fields["email"],
            phoneNumberFallback = upload.fields["phoneNumber"],
            displayNameFallback = upload.fields["displayName"],
        )
        val workspaceId = session.workspace?.id
        val storagePath = if (workspaceId == null) {
            "users/${firebaseUser.uid}/receipt-logo/${UUID.randomUUID()}.${upload.extension}"
        } else {
            "business/$workspaceId/receipt-logo/${UUID.randomUUID()}.${upload.extension}"
        }

        val stored = call.storeImageOrRespond(storageService, storagePath, upload) ?: return@post
        if (workspaceId != null) {
            repository.saveReceiptLogo(workspaceId, stored.storagePath)
        }

        call.respond(
            MediaUploadResponse(
                type = "receipt_logo",
                workspaceId = workspaceId,
                storagePath = stored.storagePath,
                downloadUrl = stored.downloadUrl,
                contentType = stored.contentType,
                sizeBytes = stored.sizeBytes,
            ),
        )
    }

    post("/media/product-images") {
        val repository = onboardingRepository ?: return@post call.respondDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val upload = call.receiveImageUpload(requireProductId = true) ?: return@post
        val productId = upload.fields["productId"]?.trim().orEmpty()
        if (productId.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "missing_product_id",
                    message = "productId form field is required.",
                ),
            )
            return@post
        }

        val session = repository.resolveSession(
            firebaseUser = firebaseUser,
            providerFallback = null,
            emailFallback = null,
            phoneNumberFallback = null,
            displayNameFallback = null,
        )
        val workspace = session.workspace
        if (workspace == null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    code = "workspace_required",
                    message = "Complete business setup before uploading product images.",
                ),
            )
            return@post
        }

        val safeProductId = productId.sanitizedPathSegment()
        val storagePath = "business/${workspace.id}/products/$safeProductId/${UUID.randomUUID()}.${upload.extension}"
        val stored = call.storeImageOrRespond(storageService, storagePath, upload) ?: return@post
        val record = repository.saveProductImage(
            workspaceId = workspace.id,
            userId = session.user.id,
            productId = productId,
            storagePath = stored.storagePath,
            originalFileName = upload.fileName,
            contentType = stored.contentType,
            sizeBytes = stored.sizeBytes,
        )

        call.respond(
            MediaUploadResponse(
                type = "product_image",
                id = record.id,
                workspaceId = record.workspaceId,
                productId = record.productId,
                storagePath = stored.storagePath,
                downloadUrl = stored.downloadUrl,
                contentType = stored.contentType,
                sizeBytes = stored.sizeBytes,
            ),
        )
    }
}

private data class IncomingImageUpload(
    val fileName: String?,
    val contentType: String,
    val extension: String,
    val bytes: ByteArray,
    val fields: Map<String, String>,
)

private suspend fun io.ktor.server.application.ApplicationCall.receiveImageUpload(
    requireProductId: Boolean,
): IncomingImageUpload? {
    var fileName: String? = null
    var contentType: String? = null
    var bytes: ByteArray? = null
    val fields = mutableMapOf<String, String>()

    receiveMultipart().forEachPart { part ->
        try {
            when (part) {
                is PartData.FileItem -> {
                    if (bytes == null) {
                        fileName = part.originalFileName
                        contentType = part.contentType?.toString()
                        bytes = part.provider().readRemaining().readByteArray()
                    }
                }

                is PartData.FormItem -> fields[part.name.orEmpty()] = part.value
                else -> Unit
            }
        } finally {
            part.dispose()
        }
    }

    val uploadBytes = bytes
    if (uploadBytes == null || uploadBytes.isEmpty()) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "missing_file",
                message = "Multipart file field is required.",
            ),
        )
        return null
    }

    if (uploadBytes.size > MaxUploadBytes) {
        respond(
            HttpStatusCode.PayloadTooLarge,
            ErrorResponse(
                code = "file_too_large",
                message = "Image must be 5 MB or smaller.",
            ),
        )
        return null
    }

    val normalizedContentType = contentType.orEmpty().lowercase()
    val extension = AllowedImageTypes[normalizedContentType]
    if (extension == null) {
        respond(
            HttpStatusCode.UnsupportedMediaType,
            ErrorResponse(
                code = "unsupported_image_type",
                message = "Only JPEG, PNG, and WebP images are supported.",
            ),
        )
        return null
    }

    if (requireProductId && fields["productId"].isNullOrBlank()) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "missing_product_id",
                message = "productId form field is required.",
            ),
        )
        return null
    }

    return IncomingImageUpload(
        fileName = fileName,
        contentType = normalizedContentType,
        extension = extension,
        bytes = uploadBytes,
        fields = fields,
    )
}

private suspend fun ApplicationCall.storeImageOrRespond(
    storageService: MediaStorageService,
    storagePath: String,
    upload: IncomingImageUpload,
): StoredMediaObject? =
    try {
        storageService.upload(storagePath, upload.bytes, upload.contentType)
    } catch (error: MediaStorageNotConfiguredException) {
        throw error
    } catch (error: Throwable) {
        application.environment.log.error("${storageService.providerName} media upload failed for $storagePath", error)
        respond(
            HttpStatusCode.BadGateway,
            ErrorResponse(
                code = "media_storage_upload_failed",
                message = "ORMA could not save this image in ${storageService.providerName}. Check the media storage credentials and try again.",
            ),
        )
        null
    }

private suspend fun io.ktor.server.application.ApplicationCall.respondDatabaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before media APIs can run.",
        ),
    )
}

private fun String.sanitizedPathSegment(): String =
    trim()
        .map { if (it.isLetterOrDigit() || it == '-' || it == '_') it else '-' }
        .joinToString("")
        .trim('-')
        .ifBlank { "product" }
        .take(80)

private const val MaxUploadBytes = 5 * 1024 * 1024

private val AllowedImageTypes = mapOf(
    "image/jpeg" to "jpg",
    "image/png" to "png",
    "image/webp" to "webp",
)
