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
private fun fetchPostAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): Promise<JsFetchResponse> = js(
    "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + bearerToken }, body: body })",
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
