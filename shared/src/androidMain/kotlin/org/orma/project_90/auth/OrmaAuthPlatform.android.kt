package org.orma.project_90.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.android

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = null)

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = bearerToken)

private suspend fun postJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 15_000
        readTimeout = 15_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
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
