package org.orma.project_90.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.ios

private val ormaIosHttpClient = HttpClient(Darwin) {
    expectSuccess = false
}

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
): OrmaHttpResponse {
    val response = ormaIosHttpClient.post(url) {
        contentType(ContentType.Application.Json)
        if (!bearerToken.isNullOrBlank()) {
            header(HttpHeaders.Authorization, "Bearer $bearerToken")
        }
        setBody(body)
    }
    return OrmaHttpResponse(
        statusCode = response.status.value,
        body = response.bodyAsText(),
    )
}
