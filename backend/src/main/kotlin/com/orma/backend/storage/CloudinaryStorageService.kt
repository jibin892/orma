package com.orma.backend.storage

import com.orma.backend.config.AppConfig
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class CloudinaryStorageNotConfiguredException : MediaStorageNotConfiguredException(
    providerName = "cloudinary",
    message = "Cloudinary storage is not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.",
)

class CloudinaryUploadException(
    val statusCode: Int,
    responseBody: String,
) : RuntimeException("Cloudinary upload failed with status $statusCode: ${responseBody.take(500)}")

class CloudinaryStorageService(
    private val config: AppConfig,
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build(),
) : MediaStorageService {
    override val providerName: String = "cloudinary"

    override suspend fun upload(
        storagePath: String,
        bytes: ByteArray,
        contentType: String,
    ): StoredMediaObject {
        val cloudName = config.cloudinaryCloudName?.takeIf { it.isNotBlank() }
            ?: throw CloudinaryStorageNotConfiguredException()
        val apiKey = config.cloudinaryApiKey?.takeIf { it.isNotBlank() }
            ?: throw CloudinaryStorageNotConfiguredException()
        val apiSecret = config.cloudinaryApiSecret?.takeIf { it.isNotBlank() }
            ?: throw CloudinaryStorageNotConfiguredException()

        val publicId = storagePath.substringBeforeLast('.', storagePath)
        val timestamp = Instant.now().epochSecond.toString()
        val signedParams = mapOf(
            "public_id" to publicId,
            "timestamp" to timestamp,
        )
        val formFields = signedParams + mapOf(
            "api_key" to apiKey,
            "signature" to signedParams.cloudinarySignature(apiSecret),
        )
        val boundary = "----OrmaCloudinary${System.currentTimeMillis()}"
        val request = HttpRequest.newBuilder(URI.create("https://api.cloudinary.com/v1_1/$cloudName/image/upload"))
            .timeout(Duration.ofSeconds(45))
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .POST(
                HttpRequest.BodyPublishers.ofByteArray(
                    buildMultipartBody(
                        boundary = boundary,
                        fields = formFields,
                        fileFieldName = "file",
                        fileName = storagePath.substringAfterLast('/').ifBlank { "upload" },
                        contentType = contentType,
                        fileBytes = bytes,
                    ),
                ),
            )
            .build()

        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        }
        if (response.statusCode() !in 200..299) {
            throw CloudinaryUploadException(response.statusCode(), response.body())
        }

        val responseBody = runCatching {
            Json.parseToJsonElement(response.body()).jsonObject
        }.getOrNull()

        return StoredMediaObject(
            storagePath = responseBody?.stringValue("public_id") ?: publicId,
            downloadUrl = responseBody?.stringValue("secure_url") ?: responseBody?.stringValue("url"),
            contentType = responseBody?.stringValue("resource_type")?.let { contentType } ?: contentType,
            sizeBytes = responseBody?.longValue("bytes") ?: bytes.size.toLong(),
        )
    }
}

private fun Map<String, String>.cloudinarySignature(apiSecret: String): String =
    entries
        .filter { it.value.isNotBlank() }
        .sortedBy { it.key }
        .joinToString("&") { (key, value) -> "$key=$value" }
        .plus(apiSecret)
        .sha1Hex()

private fun String.sha1Hex(): String =
    MessageDigest
        .getInstance("SHA-1")
        .digest(toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

private fun buildMultipartBody(
    boundary: String,
    fields: Map<String, String>,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    fileBytes: ByteArray,
): ByteArray {
    val output = ByteArrayOutputStream()
    fields.forEach { (name, value) ->
        output.writeUtf8("--$boundary\r\n")
        output.writeUtf8("Content-Disposition: form-data; name=\"${name.multipartEscaped()}\"\r\n\r\n")
        output.writeUtf8(value)
        output.writeUtf8("\r\n")
    }
    output.writeUtf8("--$boundary\r\n")
    output.writeUtf8(
        "Content-Disposition: form-data; name=\"${fileFieldName.multipartEscaped()}\"; filename=\"${fileName.multipartEscaped()}\"\r\n",
    )
    output.writeUtf8("Content-Type: $contentType\r\n\r\n")
    output.write(fileBytes)
    output.writeUtf8("\r\n--$boundary--\r\n")
    return output.toByteArray()
}

private fun ByteArrayOutputStream.writeUtf8(value: String) {
    write(value.toByteArray(StandardCharsets.UTF_8))
}

private fun String.multipartEscaped(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

private fun JsonObject.stringValue(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.longValue(key: String): Long? =
    this[key]?.jsonPrimitive?.longOrNull
