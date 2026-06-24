package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.web

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: Firebase REST auth is available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a JS fetch bridge."}}""",
)

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: Firebase token refresh is available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a JS fetch bridge."}}""",
)

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: ORMA backend calls are available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a JS fetch bridge."}}""",
)

actual suspend fun ormaPutJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: ORMA backend updates are available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a JS fetch bridge."}}""",
)

actual suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: GSTIN lookup is available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a JS fetch bridge."}}""",
)

actual suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String>,
): OrmaHttpResponse = OrmaHttpResponse(
    statusCode = 0,
    body = """{"error":{"message":"WASM_HTTP_BRIDGE_REQUIRED: Logo upload is available in Android, iOS, Desktop, and Kotlin/JS web. The Kotlin/Wasm web target still needs a multipart fetch bridge."}}""",
)
