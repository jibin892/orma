package org.orma.project_90.auth

import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences

private val ormaAuthPreferences: Preferences =
    Preferences.userRoot().node("org/orma/project_90/auth/session")

private val ormaAuthSessionFile: Path =
    Path.of(System.getProperty("user.home"), ".orma", "auth-session.properties")

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? {
    val fileValues = readSessionFile()
    val refreshToken = ormaAuthPreferences.get("refreshToken", "").orEmpty()
        .ifBlank { fileValues["refreshToken"].orEmpty() }
    val idToken = ormaAuthPreferences.get("idToken", "").orEmpty()
        .ifBlank { fileValues["idToken"].orEmpty() }
    if (refreshToken.isBlank() && idToken.isBlank()) return null
    return OrmaAuthSession(
        uid = ormaAuthPreferences.get("uid", "").orEmpty()
            .ifBlank { fileValues["uid"].orEmpty() },
        provider = (
            ormaAuthPreferences.get("provider", "").orEmpty()
                .ifBlank { fileValues["provider"].orEmpty() }
            ).toStoredOrmaAuthProvider(),
        idToken = idToken,
        refreshToken = refreshToken,
        email = ormaAuthPreferences.get("email", "").orEmpty()
            .ifBlank { fileValues["email"].orEmpty() }
            .ifBlank { null },
        phoneNumber = ormaAuthPreferences.get("phoneNumber", "").orEmpty()
            .ifBlank { fileValues["phoneNumber"].orEmpty() }
            .ifBlank { null },
        displayName = ormaAuthPreferences.get("displayName", "").orEmpty()
            .ifBlank { fileValues["displayName"].orEmpty() }
            .ifBlank { null },
    )
}

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) {
    ormaAuthPreferences.put("uid", session.uid)
    ormaAuthPreferences.put("provider", session.provider.storedProviderId)
    ormaAuthPreferences.put("idToken", session.idToken)
    ormaAuthPreferences.put("refreshToken", session.refreshToken)
    ormaAuthPreferences.put("email", session.email.orEmpty())
    ormaAuthPreferences.put("phoneNumber", session.phoneNumber.orEmpty())
    ormaAuthPreferences.put("displayName", session.displayName.orEmpty())
    ormaAuthPreferences.flush()
    writeSessionFile(session)
}

internal actual suspend fun clearOrmaStoredAuthSession() {
    listOf(
        "uid",
        "provider",
        "idToken",
        "refreshToken",
        "email",
        "phoneNumber",
        "displayName",
    ).forEach(ormaAuthPreferences::remove)
    ormaAuthPreferences.flush()
    runCatching { Files.deleteIfExists(ormaAuthSessionFile) }
}

internal actual suspend fun clearOrmaProviderAuthSession() = Unit

private fun readSessionFile(): Map<String, String> =
    runCatching {
        if (!Files.exists(ormaAuthSessionFile)) return emptyMap()
        Files.readAllLines(ormaAuthSessionFile)
            .mapNotNull { line ->
                val index = line.indexOf('=')
                if (index <= 0) null else {
                    line.take(index) to line.drop(index + 1)
                }
            }
            .toMap()
    }.getOrDefault(emptyMap())

private fun writeSessionFile(session: OrmaAuthSession) {
    runCatching {
        Files.createDirectories(ormaAuthSessionFile.parent)
        Files.writeString(
            ormaAuthSessionFile,
            listOf(
                "uid=${session.uid}",
                "provider=${session.provider.storedProviderId}",
                "idToken=${session.idToken}",
                "refreshToken=${session.refreshToken}",
                "email=${session.email.orEmpty()}",
                "phoneNumber=${session.phoneNumber.orEmpty()}",
                "displayName=${session.displayName.orEmpty()}",
            ).joinToString("\n"),
        )
    }
}
