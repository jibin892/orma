package org.orma.project_90.onboarding

internal enum class OnboardingStep {
    Authentication,
    Otp,
    Owner,
    Team,
    BusinessSetup,
    Notification,
    Complete,
}

internal enum class AuthLoadingKind {
    None,
    SendingOtp,
    VerifyingOtp,
    SigningInEmail,
    SigningInGoogle,
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
    val authStatusMessage: String? = null,
    val authErrorTitle: String? = null,
    val authErrorMessage: String? = null,
    val authErrorCode: String? = null,
    val authLoadingKind: AuthLoadingKind = AuthLoadingKind.None,
    val onboardingLoading: Boolean = false,
    val setupStep: BusinessSetupStep = BusinessSetupStep.BusinessDetails,
    val notificationsEnabled: Boolean = false,
    val draft: BusinessSetupDraft = BusinessSetupDraft(),
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

    val currentStage: Int = when (step) {
        OnboardingStep.Authentication -> 1
        OnboardingStep.Otp -> 2
        OnboardingStep.Owner,
        OnboardingStep.Team -> 3
        OnboardingStep.BusinessSetup -> 4
        OnboardingStep.Notification -> 5
        OnboardingStep.Complete -> 6
    }
}

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
    val onSetupStepChange: (BusinessSetupStep) -> Unit,
    val onNotificationDecision: (Boolean) -> Unit,
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
}

internal fun OnboardingUiState.stageBody(): String = when (step) {
    OnboardingStep.Authentication -> "Use Firebase phone, email, or Google sign-in. ORMA opens the right workspace after sign-in."
    OnboardingStep.Otp -> "Verify access with the one-time code sent to ${selectedCountry.dialCode} ${identifier.trim()}."
    OnboardingStep.Owner -> "Set the accountable owner before business setup begins."
    OnboardingStep.Team -> "Confirm the invite and continue into the existing workspace."
    OnboardingStep.BusinessSetup -> setupStep.description
    OnboardingStep.Notification -> "Push notifications keep the workspace updated after setup."
    OnboardingStep.Complete -> "This first product milestone is ready for backend integration."
}

internal fun OnboardingUiState.progressItems(): List<Pair<String, Boolean>> = listOf(
    "Auth" to (step == OnboardingStep.Authentication),
    "OTP" to (step == OnboardingStep.Otp),
    "Owner" to (step == OnboardingStep.Owner || step == OnboardingStep.Team),
    "Setup" to (step == OnboardingStep.BusinessSetup),
    "Notify" to (step == OnboardingStep.Notification),
    "Ready" to (step == OnboardingStep.Complete),
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
