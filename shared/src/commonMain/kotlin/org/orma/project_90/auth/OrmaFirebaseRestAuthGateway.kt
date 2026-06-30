package org.orma.project_90.auth

import org.orma.project_90.firebase.OrmaFirebaseClientConfig

internal class OrmaFirebaseRestAuthGateway(
    private val config: OrmaFirebaseClientConfig,
) : OrmaAuthGateway {
    private var pendingPhoneSessionInfo: String? = null
    private var pendingPhoneNumber: String? = null

    override suspend fun restoreSession(): OrmaAuthResult? {
        val storedSession = loadOrmaStoredAuthSession() ?: return null
        if (storedSession.refreshToken.isBlank()) {
            if (storedSession.idToken.isNotBlank()) {
                return storedSession.toRestoredResult()
            }
            return OrmaAuthResult.Failure(
                title = "Session expired",
                message = "Sign in again so ORMA can reopen your workspace securely.",
                code = "RESTORE_REFRESH_TOKEN_MISSING",
            )
        }
        return refreshStoredSession(storedSession, allowStoredFallback = true)
    }

    override suspend fun refreshSession(): OrmaAuthResult? {
        val storedSession = loadOrmaStoredAuthSession() ?: return null
        if (storedSession.refreshToken.isBlank()) {
            return OrmaAuthResult.Failure(
                title = "Session expired",
                message = "Sign in again so ORMA can continue securely.",
                code = "REFRESH_TOKEN_MISSING",
            )
        }
        return refreshStoredSession(storedSession, allowStoredFallback = false)
    }

    override suspend fun clearStoredSession() {
        clearOrmaStoredAuthSession()
        clearOrmaProviderAuthSession()
    }

    override suspend fun signInOrCreateWithEmail(
        email: String,
        password: String,
    ): OrmaAuthResult {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.substringAfter("@").contains(".")) {
            return OrmaAuthResult.Failure(
                title = "Check email",
                message = "Enter a valid email address.",
            )
        }
        if (password.length < 6) {
            return OrmaAuthResult.Failure(
                title = "Check password",
                message = "Use at least 6 characters for email sign-in.",
            )
        }

        val signIn = firebaseRequest(
            endpoint = "accounts:signInWithPassword",
            body = emailPasswordBody(trimmedEmail, password),
        )

        if (signIn is FirebaseAuthResponse.Success) {
            return persistResult(
                signIn.toResult(
                    provider = OrmaAuthProvider.EmailPassword,
                    message = "Signed in.",
                ),
            )
        }

        val signInError = signIn as FirebaseAuthResponse.Error
        if (signInError.code !in setOf("EMAIL_NOT_FOUND", "USER_NOT_FOUND", "INVALID_LOGIN_CREDENTIALS")) {
            return signInError.toFailure()
        }

        val signUp = firebaseRequest(
            endpoint = "accounts:signUp",
            body = emailPasswordBody(trimmedEmail, password),
        )

        return when (signUp) {
            is FirebaseAuthResponse.Success -> persistResult(
                signUp.toResult(
                    provider = OrmaAuthProvider.EmailPassword,
                    message = "Account created and signed in.",
                ),
            )
            is FirebaseAuthResponse.Error -> signUp.toFailure()
        }
    }

    override suspend fun requestPhoneOtp(phoneNumber: String): OrmaAuthResult {
        val normalizedPhoneNumber = phoneNumber.trim()
        if (!normalizedPhoneNumber.startsWith("+") || normalizedPhoneNumber.length < 8) {
            return OrmaAuthResult.Failure(
                title = "Check phone number",
                message = "Use the full international phone number, including country code.",
            )
        }

        val response = firebaseRawRequest(
            endpoint = "accounts:sendVerificationCode",
            body = buildJsonObject("phoneNumber" to normalizedPhoneNumber),
        )
        if (response is FirebaseAuthResponse.Error) {
            return response.toFailure()
        }

        val success = response as FirebaseRawResponse.Success
        val sessionInfo = success.body.jsonString("sessionInfo")
        if (sessionInfo.isNullOrBlank()) {
            return OrmaAuthResult.Failure(
                title = "OTP session missing",
                message = "ORMA could not start phone verification. Use the configured test number +971 50 000 0000 with OTP 123456 for desktop testing.",
                code = "OTP_SESSION_MISSING",
            )
        }

        pendingPhoneSessionInfo = sessionInfo
        pendingPhoneNumber = normalizedPhoneNumber
        return OrmaAuthResult.OtpSent(
            phoneNumber = normalizedPhoneNumber,
            message = if (normalizedPhoneNumber in FirebaseTestPhoneNumbers) {
                "Test OTP ready. Use code 123456."
            } else {
                "OTP sent. Enter the 6 digit code to continue."
            },
        )
    }

    override suspend fun verifyPhoneOtp(code: String): OrmaAuthResult {
        val sessionInfo = pendingPhoneSessionInfo
        if (sessionInfo.isNullOrBlank()) {
            return OrmaAuthResult.Failure(
                title = "OTP session missing",
                message = "Send OTP first. For desktop testing, use +971 50 000 0000 and OTP 123456.",
                code = "OTP_SESSION_MISSING",
            )
        }
        val trimmedCode = code.filter(Char::isDigit)
        if (trimmedCode.length != 6) {
            return OrmaAuthResult.Failure(
                title = "Check OTP",
                message = "Enter the 6 digit OTP.",
                code = "INVALID_CODE",
            )
        }

        val response = firebaseRequest(
            endpoint = "accounts:signInWithPhoneNumber",
            body = buildJsonObject(
                "sessionInfo" to sessionInfo,
                "code" to trimmedCode,
            ),
        )
        val verifiedPhoneNumber = pendingPhoneNumber
        return when (response) {
            is FirebaseAuthResponse.Success -> {
                pendingPhoneSessionInfo = null
                pendingPhoneNumber = null
                persistResult(
                    response.copy(phoneNumber = response.phoneNumber ?: verifiedPhoneNumber).toResult(
                        provider = OrmaAuthProvider.PhoneOtp,
                        message = "Phone number verified.",
                    ),
                )
            }
            is FirebaseAuthResponse.Error -> response.toFailure()
        }
    }

    override suspend fun signInWithGoogle(): OrmaAuthResult {
        val googleResult = requestGoogleIdToken(config)
        val googleIdToken = when (googleResult) {
            is OrmaGoogleSignInResult.Success -> googleResult.idToken
            is OrmaGoogleSignInResult.ExistingFirebaseSession -> return persistResult(
                OrmaAuthResult.Success(
                    session = googleResult.session,
                    message = "Signed in.",
                ),
            )
            is OrmaGoogleSignInResult.Failure -> return OrmaAuthResult.Failure(
                title = googleResult.title,
                message = googleResult.message,
                code = googleResult.code,
            )
        }

        val response = firebaseRequest(
            endpoint = "accounts:signInWithIdp",
            body = googleIdpBody(googleIdToken),
        )
        return when (response) {
            is FirebaseAuthResponse.Success -> persistResult(
                response.toResult(
                    provider = OrmaAuthProvider.Google,
                    message = "Signed in with Google.",
                ),
            )
            is FirebaseAuthResponse.Error -> response.toFailure()
        }
    }

    private suspend fun refreshStoredSession(
        storedSession: OrmaAuthSession,
        allowStoredFallback: Boolean,
    ): OrmaAuthResult {
        val url = "https://securetoken.googleapis.com/v1/token?key=${config.apiKey}"
        return try {
            val response = ormaPostFormUrlEncoded(
                url = url,
                body = "grant_type=refresh_token&refresh_token=${storedSession.refreshToken.formUrlEncoded()}",
            )
            if (response.statusCode !in 200..299) {
                val code = response.body.firebaseErrorCode()
                    ?: response.body.firebaseErrorMessage()
                    ?: "RESTORE_SESSION_FAILED"
                if (allowStoredFallback && storedSession.idToken.isNotBlank()) {
                    return storedSession.toRestoredResult()
                }
                return OrmaAuthResult.Failure(
                    title = "Session expired",
                    message = "Sign in again so ORMA can reopen your workspace securely.",
                    code = code,
                )
            }
            val refreshedIdToken = response.body.jsonString("id_token")
                ?: response.body.jsonString("access_token")
            if (refreshedIdToken.isNullOrBlank()) {
                clearOrmaStoredAuthSession()
                return OrmaAuthResult.Failure(
                    title = "Session expired",
                    message = "ORMA could not refresh your saved sign-in. Sign in again to continue.",
                    code = "RESTORE_ID_TOKEN_MISSING",
                )
            }
            val refreshedSession = storedSession.copy(
                uid = response.body.jsonString("user_id") ?: storedSession.uid,
                idToken = refreshedIdToken,
                refreshToken = response.body.jsonString("refresh_token") ?: storedSession.refreshToken,
            )
            saveOrmaStoredAuthSession(refreshedSession)
            OrmaAuthResult.Success(
                session = refreshedSession,
                message = "Session restored.",
            )
        } catch (error: Throwable) {
            if (allowStoredFallback && storedSession.idToken.isNotBlank()) {
                storedSession.toRestoredResult()
            } else {
                OrmaAuthResult.Failure(
                    title = "Session check failed",
                    message = error.message ?: "ORMA could not verify your saved sign-in. Check your connection and try again.",
                    code = "RESTORE_SESSION_NETWORK_ERROR",
                )
            }
        }
    }

    private suspend fun persistResult(result: OrmaAuthResult): OrmaAuthResult {
        if (result is OrmaAuthResult.Success) {
            saveOrmaStoredAuthSession(result.session)
        }
        return result
    }

    private suspend fun firebaseRequest(
        endpoint: String,
        body: String,
    ): FirebaseAuthResponse {
        val url = "https://identitytoolkit.googleapis.com/v1/$endpoint?key=${config.apiKey}"
        return try {
            val response = ormaPostJson(url = url, body = body)
            if (response.statusCode in 200..299) {
                FirebaseAuthResponse.Success(
                    uid = response.body.jsonString("localId").orEmpty(),
                    idToken = response.body.jsonString("idToken").orEmpty(),
                    refreshToken = response.body.jsonString("refreshToken").orEmpty(),
                    email = response.body.jsonString("email"),
                    phoneNumber = response.body.jsonString("phoneNumber"),
                    displayName = response.body.jsonString("displayName"),
                )
            } else {
                FirebaseAuthResponse.Error(
                    code = response.body.firebaseErrorCode() ?: "HTTP_${response.statusCode}",
                    message = response.body.firebaseErrorMessage() ?: "Authentication request failed.",
                )
            }
        } catch (error: Throwable) {
            FirebaseAuthResponse.Error(
                code = "NETWORK_ERROR",
                message = error.message ?: "Unable to reach the authentication service.",
            )
        }
    }

    private fun emailPasswordBody(email: String, password: String): String =
        buildJsonObject(
            "email" to email,
            "password" to password,
            "returnSecureToken" to "true",
        )

    private fun googleIdpBody(googleIdToken: String): String =
        buildJsonObject(
            "postBody" to "id_token=${googleIdToken.formUrlEncoded()}&providerId=google.com",
            "requestUri" to "http://localhost",
            "returnIdpCredential" to "true",
            "returnSecureToken" to "true",
        )

    private suspend fun firebaseRawRequest(
        endpoint: String,
        body: String,
    ): FirebaseRawResponse {
        val url = "https://identitytoolkit.googleapis.com/v1/$endpoint?key=${config.apiKey}"
        return try {
            val response = ormaPostJson(url = url, body = body)
            if (response.statusCode in 200..299) {
                FirebaseRawResponse.Success(body = response.body)
            } else {
                FirebaseAuthResponse.Error(
                    code = response.body.firebaseErrorCode() ?: "HTTP_${response.statusCode}",
                    message = response.body.firebaseErrorMessage() ?: "Authentication request failed.",
                )
            }
        } catch (error: Throwable) {
            FirebaseAuthResponse.Error(
                code = "NETWORK_ERROR",
                message = error.message ?: "Unable to reach the authentication service.",
            )
        }
    }
}

private fun OrmaAuthSession.toRestoredResult(): OrmaAuthResult.Success =
    OrmaAuthResult.Success(
        session = this,
        message = "Session restored.",
    )

private val FirebaseTestPhoneNumbers = setOf("+971500000000", "+15555550100")

private sealed interface FirebaseRawResponse {
    data class Success(
        val body: String,
    ) : FirebaseRawResponse
}

private sealed interface FirebaseAuthResponse {
    data class Success(
        val uid: String,
        val idToken: String,
        val refreshToken: String,
        val email: String?,
        val phoneNumber: String?,
        val displayName: String?,
    ) : FirebaseAuthResponse

    data class Error(
        val code: String,
        val message: String,
    ) : FirebaseAuthResponse, FirebaseRawResponse
}

private fun FirebaseAuthResponse.Success.toResult(
    provider: OrmaAuthProvider,
    message: String,
): OrmaAuthResult =
    OrmaAuthResult.Success(
        session = OrmaAuthSession(
            uid = uid,
            provider = provider,
            idToken = idToken,
            refreshToken = refreshToken,
            email = email,
            phoneNumber = phoneNumber,
            displayName = displayName,
        ),
        message = message,
    )

private fun FirebaseAuthResponse.Error.toFailure(): OrmaAuthResult.Failure =
    OrmaAuthResult.Failure(
        title = firebaseErrorTitle(code),
        message = firebaseErrorBody(code, message),
        code = code,
    )

private fun firebaseErrorTitle(code: String): String = when (code) {
    "EMAIL_EXISTS" -> "Email already exists"
    "INVALID_PASSWORD",
    "INVALID_LOGIN_CREDENTIALS" -> "Wrong password"
    "INVALID_CODE" -> "Wrong OTP"
    "SESSION_EXPIRED" -> "OTP expired"
    "MISSING_SESSION_INFO" -> "OTP session missing"
    "MISSING_PHONE_NUMBER" -> "Check phone number"
    "CAPTCHA_CHECK_FAILED",
    "MISSING_RECAPTCHA_TOKEN",
    "INVALID_APP_CREDENTIAL" -> "Use test number"
    "USER_DISABLED" -> "Account disabled"
    "OPERATION_NOT_ALLOWED" -> "Provider disabled"
    "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts"
    "NETWORK_ERROR" -> "Network error"
    else -> "Authentication failed"
}

private fun firebaseErrorBody(code: String, fallback: String): String = when (code) {
    "EMAIL_EXISTS" -> "This email is already registered. Use the same password to sign in."
    "INVALID_PASSWORD",
    "INVALID_LOGIN_CREDENTIALS" -> "The email exists, but the password does not match."
    "INVALID_CODE" -> "The OTP does not match this phone verification session."
    "SESSION_EXPIRED" -> "This OTP session expired. Send OTP again."
    "MISSING_SESSION_INFO" -> "Send OTP first, then enter the code from the OTP screen."
    "MISSING_PHONE_NUMBER" -> "Enter a valid phone number with the country code."
    "CAPTCHA_CHECK_FAILED",
    "MISSING_RECAPTCHA_TOKEN",
    "INVALID_APP_CREDENTIAL" -> "Desktop and web testing must use the configured test number: +971 50 000 0000 with OTP 123456. Real SMS needs mobile verification or web verification support."
    "USER_DISABLED" -> "This account is disabled."
    "OPERATION_NOT_ALLOWED" -> "This sign-in method is currently disabled."
    "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Sign-in is temporarily blocked due to repeated attempts. Try again later."
    "NETWORK_ERROR" -> fallback
    else -> fallback.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun buildJsonObject(vararg fields: Pair<String, String>): String =
    fields.joinToString(prefix = "{", postfix = "}") { (key, value) ->
        val encodedValue = if (value == "true" || value == "false") value else "\"${value.jsonEscaped()}\""
        "\"$key\":$encodedValue"
    }

private fun String.jsonEscaped(): String =
    buildString {
        for (char in this@jsonEscaped) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

private fun String.formUrlEncoded(): String =
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

private fun String.jsonString(key: String): String? {
    val colonIndex = jsonKeyColonIndex(key) ?: return null
    var index = colonIndex + 1
    while (index < length && this[index].isWhitespace()) index += 1
    if (index >= length || this[index] != '"') return null
    return readJsonStringToken(index)?.first
}

private fun String.jsonKeyColonIndex(key: String): Int? {
    var index = 0
    while (index < length) {
        if (this[index] != '"') {
            index += 1
            continue
        }
        val token = readJsonStringToken(index) ?: return null
        index = token.second + 1
        var afterKey = index
        while (afterKey < length && this[afterKey].isWhitespace()) afterKey += 1
        if (afterKey < length && this[afterKey] == ':' && token.first == key) {
            return afterKey
        }
    }
    return null
}

private fun String.readJsonStringToken(startIndex: Int): Pair<String, Int>? {
    if (startIndex >= length || this[startIndex] != '"') return null
    val builder = StringBuilder()
    var index = startIndex + 1
    while (index < length) {
        val char = this[index]
        when (char) {
            '"' -> return builder.toString() to index
            '\\' -> {
                index += 1
                if (index >= length) return null
                when (val escaped = this[index]) {
                    '"', '\\', '/' -> builder.append(escaped)
                    'b' -> builder.append('\b')
                    'f' -> builder.append('\u000C')
                    'n' -> builder.append('\n')
                    'r' -> builder.append('\r')
                    't' -> builder.append('\t')
                    'u' -> {
                        val end = index + 4
                        if (end >= length) return null
                        val code = substring(index + 1, end + 1).toIntOrNull(16) ?: return null
                        builder.append(code.toChar())
                        index = end
                    }
                    else -> builder.append(escaped)
                }
            }
            else -> builder.append(char)
        }
        index += 1
    }
    return null
}

private fun String.firebaseErrorCode(): String? =
    firebaseErrorMessage()?.substringBefore(":")?.trim()

private fun String.firebaseErrorMessage(): String? =
    jsonString("message")

private fun String.jsonUnescaped(): String =
    replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
