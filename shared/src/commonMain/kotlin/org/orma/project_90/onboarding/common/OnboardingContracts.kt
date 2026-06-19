package org.orma.project_90.onboarding

import org.orma.project_90.backend.OrmaCustomer
import org.orma.project_90.backend.OrmaCustomerDraft
import org.orma.project_90.backend.OrmaDashboardSummary
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaProduct
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaSupplier
import org.orma.project_90.backend.OrmaSupplierDraft

internal enum class OnboardingStep {
    Authentication,
    Otp,
    Owner,
    Team,
    BusinessSetup,
    Notification,
    Complete,
    Dashboard,
}

internal enum class AuthLoadingKind {
    None,
    RestoringSession,
    SendingOtp,
    VerifyingOtp,
    SigningInEmail,
    SigningInGoogle,
    ResolvingWorkspace,
    SigningOut,
}

internal enum class TeamInviteContactType {
    Phone,
    Email,
}

internal data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Authentication,
    val authProvider: AuthProvider = AuthProvider.PhoneOtp,
    val identifierType: AuthIdentifierType = AuthIdentifierType.Phone,
    val countryId: String = OrmaDefaultCountry.id,
    val identifier: String = "",
    val password: String = "",
    val accessPath: AccessPath = AccessPath.BusinessOwner,
    val teamInviteCode: String = "",
    val otpCode: String = "",
    val authUserId: String = "",
    val authIdToken: String = "",
    val workspaceId: String = "",
    val workspaceName: String = "",
    val workspaceLegalName: String = "",
    val workspaceLogoFileName: String = "",
    val workspaceLogoUrl: String = "",
    val authStatusMessage: String? = null,
    val authErrorTitle: String? = null,
    val authErrorMessage: String? = null,
    val authErrorCode: String? = null,
    val authLoadingKind: AuthLoadingKind = AuthLoadingKind.None,
    val onboardingLoading: Boolean = false,
    val inviteLoading: Boolean = false,
    val inviteStatusMessage: String? = null,
    val inviteErrorMessage: String? = null,
    val teamInviteName: String = "",
    val teamInviteContactType: TeamInviteContactType = TeamInviteContactType.Phone,
    val teamInviteContact: String = "",
    val teamInviteRole: String = "team_member",
    val teamProfileName: String = "",
    val pendingInviteEmail: String = "",
    val pendingInvitePhoneNumber: String = "",
    val pendingInviteRole: String = "",
    val logoUploadLoading: Boolean = false,
    val gstinLookupLoading: Boolean = false,
    val gstinLookupNumber: String = "",
    val gstinLookupStatusMessage: String? = null,
    val gstinLookupErrorMessage: String? = null,
    val setupStep: BusinessSetupStep = BusinessSetupStep.TaxDetails,
    val notificationsEnabled: Boolean = false,
    val draft: BusinessSetupDraft = BusinessSetupDraft(),
    val dashboard: DashboardDataState = DashboardDataState(),
) {
    val isAuthLoading: Boolean
        get() = authLoadingKind != AuthLoadingKind.None

    val selectedCountry: OrmaCountryUi
        get() = ormaCountryById(countryId)

    val loginReady: Boolean = when (identifierType) {
        AuthIdentifierType.Phone -> isLoginIdentifierValid(AuthIdentifierType.Phone, identifier, selectedCountry)
        AuthIdentifierType.Email -> isLoginIdentifierValid(AuthIdentifierType.Email, identifier) && password.length >= 6
    }

    val ownerReady: Boolean = draft.ownerName.isNotBlank()

    val setupReady: Boolean = canContinueBusinessSetup(setupStep, draft)

    val teamProfileReady: Boolean = teamInviteCode.isNotBlank() && teamProfileName.trim().length >= 2

    val currentStage: Int = when (step) {
        OnboardingStep.Authentication -> 1
        OnboardingStep.Otp -> 2
        OnboardingStep.Owner,
        OnboardingStep.Team -> 3
        OnboardingStep.BusinessSetup -> 4
        OnboardingStep.Notification -> 5
        OnboardingStep.Complete,
        OnboardingStep.Dashboard -> 6
    }
}

internal data class DashboardDataState(
    val hasLoaded: Boolean = false,
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val errorTitle: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val summary: OrmaDashboardSummary = OrmaDashboardSummary(),
    val customers: List<OrmaCustomer> = emptyList(),
    val suppliers: List<OrmaSupplier> = emptyList(),
    val products: List<OrmaProduct> = emptyList(),
    val orders: List<OrmaOrder> = emptyList(),
)

internal data class OnboardingActions(
    val onIdentifierTypeChange: (AuthIdentifierType) -> Unit,
    val onCountryChange: (OrmaCountryUi) -> Unit,
    val onIdentifierChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onAccessPathChange: (AccessPath) -> Unit,
    val onTeamInviteCodeChange: (String) -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onOtpChange: (String) -> Unit,
    val onResendOtp: () -> Unit,
    val onClearAuthAlert: () -> Unit,
    val onDraftChange: (BusinessSetupDraft) -> Unit,
    val onGstinLookupRequest: (String) -> Unit,
    val onLogoUploadRequest: () -> Unit,
    val onSetupStepChange: (BusinessSetupStep) -> Unit,
    val onNotificationDecision: (Boolean) -> Unit,
    val onRefreshTeamInvite: () -> Unit,
    val onTeamInviteNameChange: (String) -> Unit,
    val onTeamInviteContactTypeChange: (TeamInviteContactType) -> Unit,
    val onTeamInviteContactChange: (String) -> Unit,
    val onTeamInviteRoleChange: (String) -> Unit,
    val onCreateTeamInvite: () -> Unit,
    val onTeamProfileNameChange: (String) -> Unit,
    val onDashboardRefresh: () -> Unit,
    val onClearDashboardMessage: () -> Unit,
    val onCreateCustomer: (OrmaCustomerDraft) -> Unit,
    val onCreateSupplier: (OrmaSupplierDraft) -> Unit,
    val onCreateProduct: (OrmaProductDraft) -> Unit,
    val onAdjustProductStock: (String, OrmaStockAdjustmentDraft) -> Unit,
    val onCreateOrder: (OrmaOrderDraft) -> Unit,
    val onUpdateOrderStatus: (String, String) -> Unit,
    val onCreateBusiness: () -> Unit,
    val onBack: () -> Unit,
    val onContinue: () -> Unit,
    val onRestart: () -> Unit,
)

internal fun OnboardingUiState.stageTitle(): String = when (step) {
    OnboardingStep.Authentication -> "Authentication"
    OnboardingStep.Otp -> "OTP verification"
    OnboardingStep.Owner -> "Owner onboarding"
    OnboardingStep.Team -> "Team login"
    OnboardingStep.BusinessSetup -> setupStep.title
    OnboardingStep.Notification -> "Notifications"
    OnboardingStep.Complete -> "Ready"
    OnboardingStep.Dashboard -> "Dashboard"
}

internal fun OnboardingUiState.stageBody(): String = when (step) {
    OnboardingStep.Authentication -> "Sign in with phone, email, or Google. ORMA opens the right workspace after sign-in."
    OnboardingStep.Otp -> "Verify access with the one-time code sent to ${selectedCountry.dialCode} ${identifier.trim()}."
    OnboardingStep.Owner -> "Set the accountable owner before business setup begins."
    OnboardingStep.Team -> "Confirm the invite and continue into the existing workspace."
    OnboardingStep.BusinessSetup -> setupStep.description
    OnboardingStep.Notification -> "Push notifications keep the workspace updated after setup."
    OnboardingStep.Complete -> "This first product milestone is ready for backend integration."
    OnboardingStep.Dashboard -> "Authenticated workspace dashboard."
}

internal fun OnboardingUiState.progressItems(): List<Pair<String, Boolean>> = listOf(
    "Auth" to (step == OnboardingStep.Authentication),
    "OTP" to (step == OnboardingStep.Otp),
    "Owner" to (step == OnboardingStep.Owner || step == OnboardingStep.Team),
    "Setup" to (step == OnboardingStep.BusinessSetup),
    "Notify" to (step == OnboardingStep.Notification),
    "Ready" to (step == OnboardingStep.Complete || step == OnboardingStep.Dashboard),
)

internal fun loginIdentifierError(
    identifierType: AuthIdentifierType,
    identifier: String,
    country: OrmaCountryUi = OrmaDefaultCountry,
): String? =
    if (identifier.isNotBlank() && !isLoginIdentifierValid(identifierType, identifier, country)) {
        "Enter a valid ${identifierType.label.lowercase()}."
    } else {
        null
    }
