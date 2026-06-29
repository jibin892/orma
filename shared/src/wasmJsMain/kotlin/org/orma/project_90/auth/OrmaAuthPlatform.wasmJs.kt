@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.orma.project_90.auth

import kotlin.js.Promise
import kotlinx.coroutines.await
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.web

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse =
    fetchPost(url, body).toOrmaHttpResponse()

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse =
    fetchPostFormUrlEncoded(url, body).toOrmaHttpResponse()

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse =
    fetchPostAuthorized(url, body, bearerToken).toOrmaHttpResponse()

actual suspend fun ormaPutJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse =
    fetchPutAuthorized(url, body, bearerToken).toOrmaHttpResponse()

actual suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse =
    fetchGetAuthorized(url, bearerToken).toOrmaHttpResponse()

actual suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String>,
): OrmaHttpResponse {
    return fetchPostMultipartAuthorized(
        url = url,
        bearerToken = bearerToken,
        fileFieldName = fileFieldName,
        fileName = fileName,
        contentType = contentType,
        base64Bytes = bytes.toBase64(),
        fieldPayload = fields.toMultipartFieldPayload(),
    ).toOrmaHttpResponse()
}

private suspend fun Promise<JsAny?>.toOrmaHttpResponse(): OrmaHttpResponse {
    val response: JsAny? = await()
    val body: JsAny? = fetchResponseText(response).await()
    return OrmaHttpResponse(
        statusCode = fetchResponseStatus(response),
        body = body.toString(),
    )
}

private fun fetchResponseStatus(response: JsAny?): Int =
    fetchResponseStatusJs(response).toString().toIntOrNull() ?: 0

@Suppress("UNUSED_PARAMETER")
private fun fetchResponseStatusJs(response: JsAny?): JsAny? = js("response ? response.status : 0")

@Suppress("UNUSED_PARAMETER")
private fun fetchResponseText(response: JsAny?): Promise<JsAny?> = js("response ? response.text() : Promise.resolve('')")

@Suppress("UNUSED_PARAMETER")
private fun fetchPost(
    url: String,
    body: String,
): Promise<JsAny?> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostFormUrlEncoded(
    url: String,
    body: String,
): Promise<JsAny?> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): Promise<JsAny?> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + bearerToken }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPutAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): Promise<JsAny?> = js(
    "fetch(url, { method: 'PUT', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + bearerToken }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchGetAuthorized(
    url: String,
    bearerToken: String,
): Promise<JsAny?> = js(
    "fetch(url, { method: 'GET', headers: { 'Authorization': 'Bearer ' + bearerToken } })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    base64Bytes: String,
    fieldPayload: String,
): Promise<JsAny?> = js(
    """
    (() => {
      const formData = new FormData();
      const fields = (fieldPayload || '').split('\n').filter(Boolean);
      for (let index = 0; index < fields.length; index += 1) {
        const separator = fields[index].indexOf('=');
        if (separator > 0) {
          formData.append(
            decodeURIComponent(fields[index].slice(0, separator)),
            decodeURIComponent(fields[index].slice(separator + 1))
          );
        }
      }
      const binary = atob(base64Bytes || '');
      const bytes = new Uint8Array(binary.length);
      for (let index = 0; index < binary.length; index += 1) {
        bytes[index] = binary.charCodeAt(index);
      }
      const blob = new Blob([bytes], { type: contentType });
      formData.append(fileFieldName, blob, fileName);
      return fetch(url, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + bearerToken },
        body: formData
      });
    })()
    """,
)

private fun Map<String, String>.toMultipartFieldPayload(): String =
    entries.joinToString("\n") { (key, value) ->
        "${key.percentEncoded()}=${value.percentEncoded()}"
    }

private fun String.percentEncoded(): String =
    buildString {
        this@percentEncoded.encodeToByteArray().forEach { byte ->
            val value = byte.toInt() and 0xff
            val char = value.toChar()
            if (
                char in 'A'..'Z' ||
                char in 'a'..'z' ||
                char in '0'..'9' ||
                char == '-' ||
                char == '_' ||
                char == '.' ||
                char == '~'
            ) {
                append(char)
            } else {
                append('%')
                append(value.toString(16).uppercase().padStart(2, '0'))
            }
        }
    }

private fun ByteArray.toBase64(): String {
    if (isEmpty()) return ""
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val output = StringBuilder(((size + 2) / 3) * 4)
    var index = 0
    while (index < size) {
        val first = this[index].toInt() and 0xff
        val second = if (index + 1 < size) this[index + 1].toInt() and 0xff else 0
        val third = if (index + 2 < size) this[index + 2].toInt() and 0xff else 0
        val combined = (first shl 16) or (second shl 8) or third
        output.append(alphabet[(combined shr 18) and 0x3f])
        output.append(alphabet[(combined shr 12) and 0x3f])
        output.append(if (index + 1 < size) alphabet[(combined shr 6) and 0x3f] else '=')
        output.append(if (index + 2 < size) alphabet[combined and 0x3f] else '=')
        index += 3
    }
    return output.toString()
}
