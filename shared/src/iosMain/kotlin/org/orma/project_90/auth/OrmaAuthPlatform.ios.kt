package org.orma.project_90.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.header
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.HttpHeaders.ContentDisposition
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

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = ContentType.Application.FormUrlEncoded,
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
): OrmaHttpResponse {
    val response = ormaIosHttpClient.get(url) {
        header(HttpHeaders.Authorization, "Bearer $bearerToken")
    }
    return OrmaHttpResponse(
        statusCode = response.status.value,
        body = response.bodyAsText(),
    )
}

actual suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String>,
): OrmaHttpResponse {
    val response = ormaIosHttpClient.post(url) {
        header(HttpHeaders.Authorization, "Bearer $bearerToken")
        setBody(
            MultiPartFormDataContent(
                formData {
                    fields.forEach { (key, value) -> append(key, value) }
                    append(
                        key = fileFieldName,
                        value = bytes,
                        headers = Headers.build {
                            append(ContentDisposition, "form-data; name=\"$fileFieldName\"; filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, contentType)
                        },
                    )
                },
            ),
        )
    }
    return OrmaHttpResponse(
        statusCode = response.status.value,
        body = response.bodyAsText(),
    )
}

private suspend fun postJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = ContentType.Application.Json,
    bearerToken = bearerToken,
)

private suspend fun postBody(
    url: String,
    body: String,
    contentType: ContentType,
    bearerToken: String?,
): OrmaHttpResponse {
    val response = ormaIosHttpClient.post(url) {
        contentType(contentType)
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
