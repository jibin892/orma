package org.orma.project_90.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

private const val IosGoogleClientId = "469264683998-kfqiuua87h2matib7ni095enp6c1utq3.apps.googleusercontent.com"
private const val IosGoogleCallbackScheme = "com.googleusercontent.apps.469264683998-kfqiuua87h2matib7ni095enp6c1utq3"

private val iosGooglePresentationProvider = IosGooglePresentationContextProvider()
private var activeIosGoogleSession: ASWebAuthenticationSession? = null

actual suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult = suspendCancellableCoroutine { continuation ->
    val nonce = NSUUID.UUID().UUIDString
    val redirectUri = "$IosGoogleCallbackScheme:/oauth2redirect"
    val authUrl = listOf(
        "client_id" to IosGoogleClientId,
        "redirect_uri" to redirectUri,
        "response_type" to "id_token",
        "scope" to "openid email profile",
        "nonce" to nonce,
        "prompt" to "select_account",
    ).joinToString(
        prefix = "https://accounts.google.com/o/oauth2/v2/auth?",
        separator = "&",
    ) { (key, value) ->
        "$key=${value.formUrlEncodedForIos()}"
    }
    val url = NSURL.URLWithString(authUrl)
    if (url == null) {
        continuation.resume(
            OrmaGoogleSignInResult.Failure(
                title = "Google sign-in failed",
                message = "ORMA could not create the iOS Google authorization URL.",
                code = "GOOGLE_IOS_AUTH_URL_INVALID",
            ),
        )
        return@suspendCancellableCoroutine
    }

    val session = ASWebAuthenticationSession(
        uRL = url,
        callbackURLScheme = IosGoogleCallbackScheme,
        completionHandler = { callbackUrl: NSURL?, error: NSError? ->
        activeIosGoogleSession = null
        val callback = callbackUrl?.absoluteString
        val idToken = callback?.urlParameterValue("id_token")
        val result = when {
            !idToken.isNullOrBlank() -> OrmaGoogleSignInResult.Success(idToken = idToken)
            error != null -> OrmaGoogleSignInResult.Failure(
                title = "Google sign-in cancelled",
                message = error.localizedDescription,
                code = "GOOGLE_IOS_${error.code}",
            )
            else -> OrmaGoogleSignInResult.Failure(
                title = "Google token missing",
                message = "Google did not return an ID token to iOS.",
                code = "GOOGLE_IOS_ID_TOKEN_MISSING",
            )
        }
        continuation.resume(result)
    },
    )
    session.presentationContextProvider = iosGooglePresentationProvider
    session.prefersEphemeralWebBrowserSession = false
    activeIosGoogleSession = session

    if (!session.start()) {
        activeIosGoogleSession = null
        continuation.resume(
            OrmaGoogleSignInResult.Failure(
                title = "Google sign-in failed",
                message = "iOS could not start the Google authorization session.",
                code = "GOOGLE_IOS_SESSION_START_FAILED",
            ),
        )
    }

    continuation.invokeOnCancellation {
        session.cancel()
        if (activeIosGoogleSession == session) {
            activeIosGoogleSession = null
        }
    }
}

private class IosGooglePresentationContextProvider :
    NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession,
    ): UIWindow =
        UIApplication.sharedApplication.keyWindow ?: UIWindow()
}

private fun String.urlParameterValue(key: String): String? {
    val fragment = substringAfter("#", missingDelimiterValue = "")
    val query = substringAfter("?", missingDelimiterValue = "")
    return (fragment.ifBlank { query })
        .split("&")
        .firstOrNull { it.substringBefore("=") == key }
        ?.substringAfter("=", missingDelimiterValue = "")
        ?.percentDecodedForIos()
}

private fun String.formUrlEncodedForIos(): String =
    encodeToByteArray().joinToString(separator = "") { byte ->
        val value = byte.toInt() and 0xFF
        when (value.toChar()) {
            in 'A'..'Z',
            in 'a'..'z',
            in '0'..'9',
            '-',
            '_',
            '.',
            '~' -> value.toChar().toString()
            else -> "%" + value.toString(16).uppercase().padStart(2, '0')
        }
    }

private fun String.percentDecodedForIos(): String {
    val bytes = mutableListOf<Byte>()
    var index = 0
    while (index < length) {
        val char = this[index]
        if (char == '%' && index + 2 < length) {
            val hex = substring(index + 1, index + 3).toIntOrNull(16)
            if (hex != null) {
                bytes += hex.toByte()
                index += 3
                continue
            }
        }
        bytes += char.code.toByte()
        index += 1
    }
    return bytes.toByteArray().decodeToString()
}
