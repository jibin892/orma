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
): OrmaHttpResponse {
    val response = fetchPost(url, body).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
    )
}

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse {
    val response = fetchPostFormUrlEncoded(url, body).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
    )
}

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse {
    val response = fetchPostAuthorized(url, body, bearerToken).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
    )
}

actual suspend fun ormaPutJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse {
    val response = fetchPutAuthorized(url, body, bearerToken).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
    )
}

actual suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse {
    val response = fetchGetAuthorized(url, bearerToken).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
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
    val fieldNames = fields.keys.toTypedArray()
    val fieldValues = fieldNames.map { fields.getValue(it) }.toTypedArray()
    val response = fetchPostMultipartAuthorized(
        url = url,
        bearerToken = bearerToken,
        fileFieldName = fileFieldName,
        fileName = fileName,
        contentType = contentType,
        bytes = bytes,
        fieldNames = fieldNames,
        fieldValues = fieldValues,
    ).await()
    val text = response.text().await()
    return OrmaHttpResponse(
        statusCode = response.status,
        body = text,
    )
}

private external interface JsFetchResponse {
    val status: Int
    fun text(): Promise<String>
}

@Suppress("UNUSED_PARAMETER")
private fun fetchPost(
    url: String,
    body: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostFormUrlEncoded(
    url: String,
    body: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + bearerToken }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPutAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'PUT', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + bearerToken }, body: body })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchGetAuthorized(
    url: String,
    bearerToken: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'GET', headers: { 'Authorization': 'Bearer ' + bearerToken } })",
)

@Suppress("UNUSED_PARAMETER")
private fun fetchPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fieldNames: Array<String>,
    fieldValues: Array<String>,
): Promise<JsFetchResponse> = js(
    """
    (function() {
    const formData = new FormData();
    for (let index = 0; index < fieldNames.length; index += 1) {
      formData.append(fieldNames[index], fieldValues[index]);
    }
    const array = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
    const blob = new Blob([array], { type: contentType });
    formData.append(fileFieldName, blob, fileName);
    return fetch(url, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + bearerToken },
      body: formData
    });
    })()
    """,
)

actual suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult {
    val result = requestWebGoogleIdToken().await()
    val idToken = result.idToken
    val firebaseIdToken = result.firebaseIdToken
    return when {
        !idToken.isNullOrBlank() -> OrmaGoogleSignInResult.Success(idToken = idToken)
        !firebaseIdToken.isNullOrBlank() -> OrmaGoogleSignInResult.ExistingFirebaseSession(
            session = OrmaAuthSession(
                uid = result.uid.orEmpty(),
                provider = result.providerId.toOrmaAuthProvider(),
                idToken = firebaseIdToken,
                refreshToken = result.refreshToken.orEmpty(),
                email = result.email,
                phoneNumber = result.phoneNumber,
                displayName = result.displayName,
            ),
        )
        else -> OrmaGoogleSignInResult.Failure(
            title = result.errorTitle ?: "Google sign-in failed",
            message = result.errorMessage ?: "Google sign-in did not return an ID token.",
            code = result.errorCode ?: "GOOGLE_WEB_FAILED",
        )
    }
}

private external interface JsGoogleSignInResult {
    val idToken: String?
    val firebaseIdToken: String?
    val refreshToken: String?
    val uid: String?
    val email: String?
    val phoneNumber: String?
    val displayName: String?
    val providerId: String?
    val errorTitle: String?
    val errorMessage: String?
    val errorCode: String?
}

private fun String?.toOrmaAuthProvider(): OrmaAuthProvider = when (this) {
    "phone" -> OrmaAuthProvider.PhoneOtp
    "password" -> OrmaAuthProvider.EmailPassword
    "google.com" -> OrmaAuthProvider.Google
    else -> OrmaAuthProvider.Google
}

private fun requestWebGoogleIdToken(): Promise<JsGoogleSignInResult> = js(
    """
    (window.OrmaFirebaseGoogleAuth && window.OrmaFirebaseGoogleAuth.signIn)
      ? window.OrmaFirebaseGoogleAuth.signIn()
      : Promise.resolve({
          errorTitle: 'Google sign-in not ready',
          errorMessage: 'The ORMA web Google auth bridge did not load.',
          errorCode: 'GOOGLE_WEB_BRIDGE_MISSING'
        })
    """,
)
