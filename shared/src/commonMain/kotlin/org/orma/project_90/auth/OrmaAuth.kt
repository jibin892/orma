package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebasePlatform

enum class OrmaAuthProvider {
    EmailPassword,
    PhoneOtp,
    Google,
}

enum class OrmaWorkspaceRole {
    BusinessOwner,
    TeamMember,
}

data class OrmaAuthSession(
    val uid: String,
    val provider: OrmaAuthProvider,
    val idToken: String,
    val refreshToken: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val role: OrmaWorkspaceRole = OrmaWorkspaceRole.BusinessOwner,
)

sealed interface OrmaAuthResult {
    data class OtpSent(
        val phoneNumber: String,
        val message: String,
    ) : OrmaAuthResult

    data class Success(
        val session: OrmaAuthSession,
        val message: String,
    ) : OrmaAuthResult

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaAuthResult
}

interface OrmaAuthGateway {
    suspend fun restoreSession(): OrmaAuthResult?

    suspend fun refreshSession(): OrmaAuthResult?

    suspend fun clearStoredSession()

    suspend fun signInOrCreateWithEmail(
        email: String,
        password: String,
    ): OrmaAuthResult

    suspend fun requestPhoneOtp(phoneNumber: String): OrmaAuthResult

    suspend fun verifyPhoneOtp(code: String): OrmaAuthResult

    suspend fun signInWithGoogle(): OrmaAuthResult
}

data class OrmaHttpResponse(
    val statusCode: Int,
    val body: String,
)

expect fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig

expect suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse

expect suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse

expect suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse

expect suspend fun ormaPutJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse

expect suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse

expect suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String> = emptyMap(),
): OrmaHttpResponse

fun createOrmaAuthGateway(): OrmaAuthGateway =
    OrmaFirebaseRestAuthGateway(currentFirebaseClientConfig())

internal fun OrmaFirebasePlatform.readerLabel(): String = when (this) {
    OrmaFirebasePlatform.Android -> "Android"
    OrmaFirebasePlatform.Ios -> "iOS"
    OrmaFirebasePlatform.Web -> "Web"
    OrmaFirebasePlatform.Desktop -> "Desktop"
}
