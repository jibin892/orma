@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.orma.project_90.auth

private const val OrmaWasmAuthSessionKey = "orma.auth.session"
private const val OrmaWasmAuthSessionSeparator = "\u001F"

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    val encoded = localStorageGet(OrmaWasmAuthSessionKey)?.takeIf { it.isNotBlank() } ?: return null
    val parts = encoded.split(OrmaWasmAuthSessionSeparator)
    if (parts.size < 7) return null
    return OrmaAuthSession(
        uid = parts[0].decodeStorageComponent(),
        provider = parts[1].decodeStorageComponent().toStoredOrmaAuthProvider(),
        idToken = parts[2].decodeStorageComponent(),
        refreshToken = parts[3].decodeStorageComponent(),
        email = parts[4].decodeStorageComponent().ifBlank { null },
        phoneNumber = parts[5].decodeStorageComponent().ifBlank { null },
        displayName = parts[6].decodeStorageComponent().ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    localStorageSet(
        key = OrmaWasmAuthSessionKey,
        value = listOf(
            session.uid,
            session.provider.storedProviderId,
            session.idToken,
            session.refreshToken,
            session.email.orEmpty(),
            session.phoneNumber.orEmpty(),
            session.displayName.orEmpty(),
        )
            .joinToString(OrmaWasmAuthSessionSeparator) { it.encodeStorageComponent() },
    )
}

internal actual suspend fun clearOrmaStoredAuthSession() {
    localStorageRemove(OrmaWasmAuthSessionKey)
}

internal actual suspend fun clearOrmaProviderAuthSession() = Unit

private fun String.encodeStorageComponent(): String =
    encodeURIComponent(this).toString()

private fun String.decodeStorageComponent(): String =
    decodeURIComponent(this).toString()

private fun localStorageGet(key: String): String? =
    localStorageGetJs(key).toString().takeIf { it != "null" }

private fun localStorageSet(key: String, value: String) {
    localStorageSetJs(key, value)
}

private fun localStorageRemove(key: String) {
    localStorageRemoveJs(key)
}

@Suppress("UNUSED_PARAMETER")
private fun encodeURIComponent(value: String): JsAny? = js("encodeURIComponent(value)")

@Suppress("UNUSED_PARAMETER")
private fun decodeURIComponent(value: String): JsAny? = js("decodeURIComponent(value)")

@Suppress("UNUSED_PARAMETER")
private fun localStorageGetJs(key: String): JsAny? = js(
    "(typeof window !== 'undefined' && window.localStorage) ? window.localStorage.getItem(key) : null",
)

@Suppress("UNUSED_PARAMETER")
private fun localStorageSetJs(key: String, value: String): Unit = js(
    "if (typeof window !== 'undefined' && window.localStorage) window.localStorage.setItem(key, value)",
)

@Suppress("UNUSED_PARAMETER")
private fun localStorageRemoveJs(key: String): Unit = js(
    "if (typeof window !== 'undefined' && window.localStorage) window.localStorage.removeItem(key)",
)
