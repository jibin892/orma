package com.orma.backend.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiInfoResponse(
    val service: String,
    val status: String,
)

@Serializable
data class HealthResponse(
    val status: String,
    val environment: String,
    val databaseConfigured: Boolean,
    val firebaseAuthConfigured: Boolean,
    val firebaseStorageConfigured: Boolean,
    val mediaStorageProvider: String,
    val mediaStorageConfigured: Boolean,
    val cloudinaryConfigured: Boolean,
    val gstinCheckConfigured: Boolean,
)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
)

@Serializable
data class SessionRequest(
    val idToken: String,
    val provider: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val displayName: String? = null,
)

@Serializable
data class SessionResponse(
    val uid: String,
    val email: String?,
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val user: UserResponse? = null,
    val workspace: WorkspaceResponse? = null,
    val onboardingStatus: String = "unknown",
    val requiredStep: String = "unknown",
    val accessPath: String = "business_owner",
)

@Serializable
data class UserResponse(
    val id: String,
    val firebaseUid: String,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val role: String,
    val notificationsEnabled: Boolean,
)

@Serializable
data class WorkspaceResponse(
    val id: String,
    val businessName: String,
    val legalName: String,
    val role: String,
    val onboardingComplete: Boolean,
    val inviteCode: String? = null,
)

@Serializable
data class BusinessSetupRequest(
    val ownerName: String,
    val businessName: String,
    val legalName: String,
    val industry: String,
    val website: String = "",
    val isTaxRegistered: Boolean,
    val taxNumber: String = "",
    val taxLabel: String,
    val addressLine: String,
    val city: String,
    val region: String = "",
    val country: String,
    val postalCode: String = "",
    val logoFileName: String = "",
    val invoicePrefix: String,
    val nextInvoiceNumber: String,
    val paymentTerms: String,
    val invoiceFooter: String,
    val currency: String,
    val taxMode: String,
    val pricesIncludeTax: Boolean,
)

@Serializable
data class TeamInviteLookupRequest(
    val code: String,
)

@Serializable
data class TeamInviteJoinRequest(
    val code: String,
)

@Serializable
data class TeamInviteResponse(
    val code: String,
    val workspace: WorkspaceResponse,
)

@Serializable
data class NotificationPreferenceRequest(
    val enabled: Boolean,
)

@Serializable
data class OnboardingMutationResponse(
    val user: UserResponse,
    val workspace: WorkspaceResponse?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

@Serializable
data class MediaUploadResponse(
    val type: String,
    val id: String? = null,
    val workspaceId: String? = null,
    val productId: String? = null,
    val storagePath: String,
    val downloadUrl: String? = null,
    val contentType: String,
    val sizeBytes: Long,
)

@Serializable
data class GstinLookupResponse(
    val gstin: String,
    val flag: Boolean,
    val message: String,
    val data: JsonElement? = null,
    val source: String,
    val cached: Boolean,
    val cachedAt: String,
)
