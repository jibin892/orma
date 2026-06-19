package org.orma.project_90.auth

internal actual suspend fun loadOrmaStoredAuthSession(): OrmaAuthSession? = null

internal actual suspend fun saveOrmaStoredAuthSession(session: OrmaAuthSession) = Unit

internal actual suspend fun clearOrmaStoredAuthSession() = Unit

internal actual suspend fun clearOrmaProviderAuthSession() = Unit
