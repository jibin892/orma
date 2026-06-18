package org.orma.project_90.backend

import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthSession
import org.orma.project_90.auth.ormaPostJson
import org.orma.project_90.auth.ormaPostJsonAuthorized
import org.orma.project_90.onboarding.BusinessSetupDraft

data class OrmaBackendUser(
    val id: String,
    val role: String,
    val notificationsEnabled: Boolean,
)

data class OrmaBackendWorkspace(
    val id: String,
    val businessName: String,
    val legalName: String,
    val role: String,
    val onboardingComplete: Boolean,
    val inviteCode: String?,
)

data class OrmaBackendSession(
    val user: OrmaBackendUser,
    val workspace: OrmaBackendWorkspace?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

sealed interface OrmaBackendResult<out T> {
    data class Success<T>(
        val value: T,
    ) : OrmaBackendResult<T>

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaBackendResult<Nothing>
}

class OrmaBackendClient(
    private val config: OrmaBackendConfig,
) {
    suspend fun resolveSession(session: OrmaAuthSession): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Resolve ORMA workspace"
        return executeBackendRequest(actionTitle) {
            ormaPostJson(
                url = config.url("/auth/session"),
                body = buildJsonObject(
                    "idToken" to JsonValue.StringValue(session.idToken),
                    "provider" to JsonValue.StringValue(session.provider.backendName),
                    "email" to JsonValue.StringValue(session.email),
                    "phoneNumber" to JsonValue.StringValue(session.phoneNumber),
                    "displayName" to JsonValue.StringValue(session.displayName),
                ),
            )
        }
    }

    suspend fun completeBusinessSetup(
        idToken: String,
        draft: BusinessSetupDraft,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Save business setup"
        return executeBackendRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/business"),
                bearerToken = idToken,
                body = buildJsonObject(
                    "ownerName" to JsonValue.StringValue(draft.ownerName),
                    "businessName" to JsonValue.StringValue(draft.businessName),
                    "legalName" to JsonValue.StringValue(draft.legalName),
                    "industry" to JsonValue.StringValue(draft.industry),
                    "website" to JsonValue.StringValue(draft.website),
                    "isTaxRegistered" to JsonValue.BooleanValue(draft.isTaxRegistered),
                    "taxNumber" to JsonValue.StringValue(draft.taxNumber),
                    "taxLabel" to JsonValue.StringValue(draft.taxLabel),
                    "addressLine" to JsonValue.StringValue(draft.addressLine),
                    "city" to JsonValue.StringValue(draft.city),
                    "region" to JsonValue.StringValue(draft.region),
                    "country" to JsonValue.StringValue(draft.country),
                    "postalCode" to JsonValue.StringValue(draft.postalCode),
                    "logoFileName" to JsonValue.StringValue(draft.logoFileName),
                    "invoicePrefix" to JsonValue.StringValue(draft.invoicePrefix),
                    "nextInvoiceNumber" to JsonValue.StringValue(draft.nextInvoiceNumber),
                    "paymentTerms" to JsonValue.StringValue(draft.paymentTerms),
                    "invoiceFooter" to JsonValue.StringValue(draft.invoiceFooter),
                    "currency" to JsonValue.StringValue(draft.currency),
                    "taxMode" to JsonValue.StringValue(draft.taxMode),
                    "pricesIncludeTax" to JsonValue.BooleanValue(draft.pricesIncludeTax),
                ),
            )
        }
    }

    suspend fun joinTeamInvite(
        idToken: String,
        code: String,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Join workspace"
        return executeBackendRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/team-invites/join"),
                bearerToken = idToken,
                body = buildJsonObject("code" to JsonValue.StringValue(code)),
            )
        }
    }

    suspend fun updateNotificationPreference(
        idToken: String,
        enabled: Boolean,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Save notifications"
        return executeBackendRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/notifications"),
                bearerToken = idToken,
                body = buildJsonObject("enabled" to JsonValue.BooleanValue(enabled)),
            )
        }
    }

    private fun OrmaBackendConfig.url(path: String): String =
        baseUrl.trimEnd('/') + path

    private suspend fun executeBackendRequest(
        actionTitle: String,
        request: suspend () -> org.orma.project_90.auth.OrmaHttpResponse,
    ): OrmaBackendResult<OrmaBackendSession> =
        try {
            request().toBackendSessionResult(actionTitle)
        } catch (error: Throwable) {
            OrmaBackendResult.Failure(
                title = actionTitle,
                message = error.backendNetworkMessage(config.baseUrl),
                code = "BACKEND_NETWORK_ERROR",
            )
        }
}

fun createOrmaBackendClient(): OrmaBackendClient =
    OrmaBackendClient(currentOrmaBackendConfig())

private val OrmaAuthProvider.backendName: String
    get() = when (this) {
        OrmaAuthProvider.EmailPassword -> "password"
        OrmaAuthProvider.PhoneOtp -> "phone"
        OrmaAuthProvider.Google -> "google.com"
    }

private fun org.orma.project_90.auth.OrmaHttpResponse.toBackendSessionResult(
    actionTitle: String,
): OrmaBackendResult<OrmaBackendSession> {
    if (statusCode !in 200..299) {
        return OrmaBackendResult.Failure(
            title = actionTitle,
            message = body.jsonString("message") ?: "ORMA backend request failed.",
            code = body.jsonString("code") ?: "HTTP_$statusCode",
        )
    }
    return try {
        OrmaBackendResult.Success(body.toBackendSession())
    } catch (error: Throwable) {
        OrmaBackendResult.Failure(
            title = actionTitle,
            message = error.message ?: "ORMA backend returned an unreadable response.",
            code = "BACKEND_RESPONSE_PARSE_FAILED",
        )
    }
}

private fun Throwable.backendNetworkMessage(baseUrl: String): String {
    val detail = message?.takeIf(String::isNotBlank)
    val target = baseUrl.trimEnd('/')
    return if (detail == null) {
        "ORMA could not reach the workspace service at $target. Check the backend connection and try again."
    } else {
        "ORMA could not reach the workspace service at $target. $detail"
    }
}

private fun String.toBackendSession(): OrmaBackendSession {
    val userJson = jsonObject("user") ?: error("Backend response is missing user.")
    val workspaceJson = jsonObject("workspace")
    return OrmaBackendSession(
        user = OrmaBackendUser(
            id = userJson.jsonString("id").orEmpty(),
            role = userJson.jsonString("role").orEmpty(),
            notificationsEnabled = userJson.jsonBoolean("notificationsEnabled") ?: false,
        ),
        workspace = workspaceJson?.let {
            OrmaBackendWorkspace(
                id = it.jsonString("id").orEmpty(),
                businessName = it.jsonString("businessName").orEmpty(),
                legalName = it.jsonString("legalName").orEmpty(),
                role = it.jsonString("role").orEmpty(),
                onboardingComplete = it.jsonBoolean("onboardingComplete") ?: false,
                inviteCode = it.jsonString("inviteCode"),
            )
        },
        onboardingStatus = jsonString("onboardingStatus").orEmpty(),
        requiredStep = jsonString("requiredStep").orEmpty(),
        accessPath = jsonString("accessPath").orEmpty(),
    )
}

private sealed interface JsonValue {
    data class StringValue(val value: String?) : JsonValue
    data class BooleanValue(val value: Boolean) : JsonValue
}

private fun buildJsonObject(vararg fields: Pair<String, JsonValue>): String =
    fields.joinToString(prefix = "{", postfix = "}") { (key, value) ->
        val encodedValue = when (value) {
            is JsonValue.BooleanValue -> value.value.toString()
            is JsonValue.StringValue -> {
                if (value.value == null) {
                    "null"
                } else {
                    "\"${value.value.jsonEscaped()}\""
                }
            }
        }
        "\"$key\":$encodedValue"
    }

private fun String.jsonString(key: String): String? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    return pattern.find(this)?.groupValues?.get(1)?.jsonUnescaped()
}

private fun String.jsonBoolean(key: String): Boolean? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(true|false)")
    return pattern.find(this)?.groupValues?.get(1)?.toBooleanStrictOrNull()
}

private fun String.jsonObject(key: String): String? {
    val marker = "\"${key.jsonEscaped()}\""
    val keyIndex = indexOf(marker)
    if (keyIndex < 0) return null
    val colonIndex = indexOf(':', startIndex = keyIndex + marker.length)
    if (colonIndex < 0) return null
    val start = indexOf('{', startIndex = colonIndex + 1)
    if (start < 0) return null

    var depth = 0
    var inString = false
    var escaped = false
    for (index in start until length) {
        val char = this[index]
        when {
            escaped -> escaped = false
            char == '\\' && inString -> escaped = true
            char == '"' -> inString = !inString
            !inString && char == '{' -> depth += 1
            !inString && char == '}' -> {
                depth -= 1
                if (depth == 0) return substring(start, index + 1)
            }
        }
    }
    return null
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

private fun String.jsonUnescaped(): String =
    replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
