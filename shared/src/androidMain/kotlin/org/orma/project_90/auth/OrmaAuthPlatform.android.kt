package org.orma.project_90.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig
import java.io.DataOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.android

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = null)

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = "application/x-www-form-urlencoded",
    bearerToken = null,
)

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = bearerToken)

actual suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 15_000
        setRequestProperty("Authorization", "Bearer $bearerToken")
    }
    try {
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        OrmaHttpResponse(
            statusCode = status,
            body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty(),
        )
    } finally {
        connection.disconnect()
    }
}

actual suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String>,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val boundary = "----OrmaBoundary${System.currentTimeMillis()}"
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 30_000
        readTimeout = 30_000
        doOutput = true
        setRequestProperty("Authorization", "Bearer $bearerToken")
        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
    }
    try {
        DataOutputStream(connection.outputStream).use { output ->
            output.writeMultipartBody(boundary, fields, fileFieldName, fileName, contentType, bytes)
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        OrmaHttpResponse(
            statusCode = status,
            body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty(),
        )
    } finally {
        connection.disconnect()
    }
}

private suspend fun postJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = "application/json",
    bearerToken = bearerToken,
)

private suspend fun postBody(
    url: String,
    body: String,
    contentType: String,
    bearerToken: String?,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 15_000
        readTimeout = 15_000
        doOutput = true
        setRequestProperty("Content-Type", contentType)
        if (!bearerToken.isNullOrBlank()) {
            setRequestProperty("Authorization", "Bearer $bearerToken")
        }
    }
    try {
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        OrmaHttpResponse(
            statusCode = status,
            body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty(),
        )
    } finally {
        connection.disconnect()
    }
}

private fun DataOutputStream.writeMultipartBody(
    boundary: String,
    fields: Map<String, String>,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
) {
    fields.forEach { (name, value) ->
        writeUtf8("--$boundary\r\n")
        writeUtf8("Content-Disposition: form-data; name=\"${name.multipartEscaped()}\"\r\n\r\n")
        writeUtf8(value)
        writeUtf8("\r\n")
    }
    writeUtf8("--$boundary\r\n")
    writeUtf8(
        "Content-Disposition: form-data; name=\"${fileFieldName.multipartEscaped()}\"; " +
            "filename=\"${fileName.multipartEscaped()}\"\r\n",
    )
    writeUtf8("Content-Type: $contentType\r\n\r\n")
    write(bytes)
    writeUtf8("\r\n--$boundary--\r\n")
}

private fun DataOutputStream.writeUtf8(value: String) {
    write(value.toByteArray(Charsets.UTF_8))
}

private fun String.multipartEscaped(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")
