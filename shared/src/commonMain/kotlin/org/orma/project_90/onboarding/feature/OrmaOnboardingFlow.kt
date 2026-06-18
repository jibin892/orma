package org.orma.project_90.onboarding.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.orma.project_90.getPlatform
import org.orma.project_90.backend.OrmaBackendResult
import org.orma.project_90.backend.OrmaBackendSession
import org.orma.project_90.backend.createOrmaBackendClient
import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthResult
import org.orma.project_90.auth.createOrmaAuthGateway
import org.orma.project_90.designsystem.OrmaAdaptiveSurface
import org.orma.project_90.designsystem.OrmaWindowClass
import org.orma.project_90.onboarding.AccessPath
import org.orma.project_90.onboarding.AuthLoadingKind
import org.orma.project_90.onboarding.AuthIdentifierType
import org.orma.project_90.onboarding.AuthProvider
import org.orma.project_90.onboarding.BusinessSetupStep
import org.orma.project_90.onboarding.OrmaAuthFeedbackDialog
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingStep
import org.orma.project_90.onboarding.OnboardingUiState
import org.orma.project_90.onboarding.isOtpValid
import org.orma.project_90.onboarding.desktop.OrmaOnboardingDesktopUi
import org.orma.project_90.onboarding.mobile.OrmaOnboardingMobileUi

@Composable
fun OrmaOnboardingFlow(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(OnboardingUiState()) }
    val authGateway = remember { createOrmaAuthGateway() }
    val backendClient = remember { createOrmaBackendClient() }
    val scope = rememberCoroutineScope()

    fun restart() {
        state = OnboardingUiState()
    }

    fun routeAfterBackendSession(
        authenticatedState: OnboardingUiState,
        backendSession: OrmaBackendSession,
    ): OnboardingUiState {
        val resolvedPath = when (backendSession.accessPath) {
            "team_member" -> AccessPath.TeamMember
            else -> AccessPath.BusinessOwner
        }
        val workspace = backendSession.workspace
        val routedState = authenticatedState.copy(
            accessPath = resolvedPath,
            workspaceId = workspace?.id.orEmpty(),
            workspaceName = workspace?.businessName.orEmpty(),
            notificationsEnabled = backendSession.user.notificationsEnabled,
            teamInviteCode = workspace?.inviteCode ?: if (resolvedPath == AccessPath.BusinessOwner) "" else authenticatedState.teamInviteCode,
        )
        return when (backendSession.requiredStep) {
            "complete" -> routedState.copy(step = OnboardingStep.Complete)
            "team" -> routedState.copy(step = OnboardingStep.Team)
            "business_setup" -> routedState.copy(step = OnboardingStep.BusinessSetup)
            else -> routedState.copy(step = OnboardingStep.Owner)
        }
    }

    fun applyBackendFailure(
        title: String,
        message: String,
        code: String?,
    ) {
        state = state.copy(
            authLoadingKind = AuthLoadingKind.None,
            onboardingLoading = false,
            authStatusMessage = null,
            authErrorTitle = title,
            authErrorMessage = message,
            authErrorCode = code,
        )
    }

    suspend fun applyAuthResult(result: OrmaAuthResult) {
        when (result) {
            is OrmaAuthResult.OtpSent -> {
                state = state.copy(
                step = OnboardingStep.Otp,
                authLoadingKind = AuthLoadingKind.None,
                authProvider = AuthProvider.PhoneOtp,
                identifierType = AuthIdentifierType.Phone,
                identifier = result.phoneNumber.removePrefix(state.selectedCountry.dialCode),
                otpCode = "",
                authStatusMessage = result.message,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
            }
            is OrmaAuthResult.Success -> {
                val session = result.session
                val identifierType = if (session.provider == OrmaAuthProvider.PhoneOtp) {
                    AuthIdentifierType.Phone
                } else {
                    AuthIdentifierType.Email
                }
                val identifier = session.email ?: session.phoneNumber ?: state.identifier
                val authenticatedState = state.copy(
                    authLoadingKind = AuthLoadingKind.None,
                    authErrorTitle = null,
                    authErrorMessage = null,
                    authErrorCode = null,
                    authStatusMessage = "Firebase connected. Resolving ORMA workspace...",
                    authProvider = session.provider.toOnboardingProvider(),
                    identifierType = identifierType,
                    identifier = identifier,
                    authUserId = session.uid,
                    authIdToken = session.idToken,
                    otpCode = "",
                )
                state = authenticatedState
                when (val backendResult = backendClient.resolveSession(session)) {
                    is OrmaBackendResult.Success -> {
                        state = routeAfterBackendSession(
                            authenticatedState.copy(authStatusMessage = result.message),
                            backendResult.value,
                        )
                    }
                    is OrmaBackendResult.Failure -> applyBackendFailure(
                        title = backendResult.title,
                        message = backendResult.message,
                        code = backendResult.code,
                    )
                }
            }
            is OrmaAuthResult.Failure -> {
                state = state.copy(
                    authLoadingKind = AuthLoadingKind.None,
                    authStatusMessage = null,
                    authErrorTitle = result.title,
                    authErrorMessage = result.message,
                    authErrorCode = result.code,
                )
            }
        }
    }

    fun applyBackendSessionMutation(
        currentState: OnboardingUiState,
        backendSession: OrmaBackendSession,
        nextStep: OnboardingStep,
    ): OnboardingUiState =
        routeAfterBackendSession(
            currentState.copy(
                onboardingLoading = false,
                authStatusMessage = null,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            ),
            backendSession,
        ).copy(step = nextStep)

    fun backendTokenOrError(snapshot: OnboardingUiState): String? {
        if (snapshot.authIdToken.isNotBlank()) return snapshot.authIdToken
        applyBackendFailure(
            title = "Backend session missing",
            message = "Sign in again so ORMA can send the Firebase token to the backend.",
            code = "MISSING_ID_TOKEN",
        )
        return null
    }

    fun saveBusinessSetup() {
        val snapshot = state
        if (snapshot.onboardingLoading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            onboardingLoading = true,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.completeBusinessSetup(idToken, snapshot.draft)) {
                is OrmaBackendResult.Success -> {
                    state = applyBackendSessionMutation(state, result.value, OnboardingStep.Notification)
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun finishTeamAccess() {
        val snapshot = state
        if (snapshot.workspaceId.isNotBlank()) {
            state = snapshot.copy(step = OnboardingStep.Notification)
            return
        }
        if (snapshot.teamInviteCode.isBlank() || snapshot.onboardingLoading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            onboardingLoading = true,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.joinTeamInvite(idToken, snapshot.teamInviteCode)) {
                is OrmaBackendResult.Success -> {
                    state = applyBackendSessionMutation(state, result.value, OnboardingStep.Notification)
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun saveNotificationDecision(enabled: Boolean) {
        val snapshot = state
        if (snapshot.onboardingLoading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            notificationsEnabled = enabled,
            onboardingLoading = true,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.updateNotificationPreference(idToken, enabled)) {
                is OrmaBackendResult.Success -> {
                    state = applyBackendSessionMutation(state, result.value, OnboardingStep.Complete)
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun fullPhoneNumber(snapshot: OnboardingUiState): String =
        snapshot.selectedCountry.dialCode + snapshot.identifier.filter(Char::isDigit)

    fun startPhoneOtp() {
        val snapshot = state
        if (!snapshot.loginReady || snapshot.isAuthLoading) return
        state = snapshot.copy(
            authLoadingKind = AuthLoadingKind.SendingOtp,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
            authStatusMessage = null,
        )
        scope.launch {
            applyAuthResult(authGateway.requestPhoneOtp(fullPhoneNumber(snapshot)))
        }
    }

    fun signInOrCreateWithEmail() {
        val snapshot = state
        if (!snapshot.loginReady || snapshot.isAuthLoading) return
        state = snapshot.copy(
            authLoadingKind = AuthLoadingKind.SigningInEmail,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
            authStatusMessage = null,
            authProvider = AuthProvider.EmailPassword,
        )
        scope.launch {
            applyAuthResult(
                authGateway.signInOrCreateWithEmail(
                    email = snapshot.identifier,
                    password = snapshot.password,
                ),
            )
        }
    }

    fun startGoogleSignIn() {
        val snapshot = state
        if (snapshot.isAuthLoading) return
        state = snapshot.copy(
            authLoadingKind = AuthLoadingKind.SigningInGoogle,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
            authStatusMessage = null,
            authProvider = AuthProvider.Google,
        )
        scope.launch {
            applyAuthResult(authGateway.signInWithGoogle())
        }
    }

    fun verifyOtp() {
        val snapshot = state
        if (!isOtpValid(snapshot.otpCode) || snapshot.isAuthLoading) return
        state = snapshot.copy(
            authLoadingKind = AuthLoadingKind.VerifyingOtp,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
            authStatusMessage = null,
        )
        scope.launch {
            applyAuthResult(authGateway.verifyPhoneOtp(snapshot.otpCode))
        }
    }

    fun resendPhoneOtp() {
        val snapshot = state
        if (snapshot.isAuthLoading) return
        state = snapshot.copy(
            otpCode = "",
            authStatusMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        startPhoneOtp()
    }

    fun goBack() {
        val isSignedIn = state.authUserId.isNotBlank() || state.authIdToken.isNotBlank()
        state = when (state.step) {
            OnboardingStep.Authentication -> state
            OnboardingStep.Otp -> state.copy(
                step = OnboardingStep.Authentication,
                otpCode = "",
                authLoadingKind = AuthLoadingKind.None,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
            OnboardingStep.Owner,
            OnboardingStep.Team -> if (isSignedIn) {
                state.copy(onboardingLoading = false)
            } else if (state.authProvider == AuthProvider.Google || state.authProvider == AuthProvider.EmailPassword) {
                state.copy(
                    step = OnboardingStep.Authentication,
                    authProvider = AuthProvider.PhoneOtp,
                    identifierType = AuthIdentifierType.Phone,
                    identifier = "",
                    password = "",
                    authUserId = "",
                    authIdToken = "",
                    workspaceId = "",
                    workspaceName = "",
                    onboardingLoading = false,
                    authStatusMessage = null,
                )
            } else {
                state.copy(step = OnboardingStep.Otp)
            }
            OnboardingStep.BusinessSetup -> {
                val steps = BusinessSetupStep.entries
                val currentIndex = steps.indexOf(state.setupStep)
                if (currentIndex > 0) {
                    state.copy(setupStep = steps[currentIndex - 1])
                } else {
                    state
                }
            }
            OnboardingStep.Notification -> {
                if (state.accessPath == AccessPath.BusinessOwner) {
                    state.copy(step = OnboardingStep.BusinessSetup)
                } else {
                    state.copy(step = OnboardingStep.Team)
                }
            }
            OnboardingStep.Complete -> state.copy(step = OnboardingStep.Notification)
        }
    }

    fun goNext() {
        state = when (state.step) {
            OnboardingStep.Authentication -> state
            OnboardingStep.Otp -> state
            OnboardingStep.Owner -> state.copy(step = OnboardingStep.BusinessSetup)
            OnboardingStep.Team -> state.copy(step = OnboardingStep.Notification)
            OnboardingStep.BusinessSetup -> {
                val steps = BusinessSetupStep.entries
                val currentIndex = steps.indexOf(state.setupStep)
                if (currentIndex < steps.lastIndex) {
                    state.copy(setupStep = steps[currentIndex + 1])
                } else {
                    state.copy(step = OnboardingStep.Notification)
                }
            }
            OnboardingStep.Notification -> state.copy(step = OnboardingStep.Complete)
            OnboardingStep.Complete -> state
        }
    }

    val actions = OnboardingActions(
        onIdentifierTypeChange = {
            state = state.copy(
                identifierType = it,
                identifier = "",
                password = "",
                otpCode = "",
                authProvider = if (it == AuthIdentifierType.Email) AuthProvider.EmailPassword else AuthProvider.PhoneOtp,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
                authStatusMessage = null,
            )
        },
        onCountryChange = { country ->
            state = state.copy(
                countryId = country.id,
                identifier = state.identifier.filter(Char::isDigit).take(country.maxDigits),
            )
        },
        onIdentifierChange = {
            state = if (state.identifierType == AuthIdentifierType.Phone) {
                state.copy(
                    authProvider = AuthProvider.PhoneOtp,
                    identifier = it.filter(Char::isDigit).take(state.selectedCountry.maxDigits),
                    authErrorTitle = null,
                    authErrorMessage = null,
                    authErrorCode = null,
                )
            } else {
                state.copy(
                    authProvider = AuthProvider.EmailPassword,
                    identifier = it.trim().take(120),
                    authErrorTitle = null,
                    authErrorMessage = null,
                    authErrorCode = null,
                )
            }
        },
        onPasswordChange = {
            state = state.copy(
                password = it.take(72),
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
        },
        onAccessPathChange = { path ->
            state = state.copy(
                accessPath = path,
                teamInviteCode = if (path == AccessPath.BusinessOwner) "" else state.teamInviteCode,
            )
        },
        onTeamInviteCodeChange = {
            state = state.copy(
                teamInviteCode = it.uppercase().filter(Char::isLetterOrDigit).take(16),
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
        },
        onGoogleSignIn = ::startGoogleSignIn,
        onOtpChange = {
            state = state.copy(
                otpCode = it.filter(Char::isDigit).take(6),
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
        },
        onResendOtp = ::resendPhoneOtp,
        onClearAuthAlert = {
            state = state.copy(
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
                authStatusMessage = null,
            )
        },
        onDraftChange = {
            state = state.copy(
                draft = it,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
            )
        },
        onSetupStepChange = { state = state.copy(setupStep = it) },
        onNotificationDecision = ::saveNotificationDecision,
        onCreateBusiness = {
            state = state.copy(
                accessPath = AccessPath.BusinessOwner,
                teamInviteCode = "",
                workspaceId = "",
                workspaceName = "",
                step = OnboardingStep.Owner,
            )
        },
        onBack = ::goBack,
        onContinue = {
            when (state.step) {
                OnboardingStep.Authentication -> {
                    when (state.identifierType) {
                        AuthIdentifierType.Phone -> startPhoneOtp()
                        AuthIdentifierType.Email -> signInOrCreateWithEmail()
                    }
                }
                OnboardingStep.Otp -> verifyOtp()
                OnboardingStep.Team -> finishTeamAccess()
                OnboardingStep.BusinessSetup -> {
                    val steps = BusinessSetupStep.entries
                    val currentIndex = steps.indexOf(state.setupStep)
                    if (currentIndex == steps.lastIndex) {
                        saveBusinessSetup()
                    } else {
                        goNext()
                    }
                }
                else -> goNext()
            }
        },
        onRestart = ::restart,
    )

    OrmaAdaptiveSurface(modifier = modifier) {
        val windowClass = this
        Box(modifier = Modifier.fillMaxSize()) {
            when (windowClass) {
                OrmaWindowClass.Mobile -> OrmaOnboardingMobileUi(
                    state = state,
                    actions = actions,
                )
                OrmaWindowClass.Wide -> OrmaOnboardingDesktopUi(
                    state = state,
                    actions = actions,
                    platformName = getPlatform().name,
                )
            }
            OrmaAuthFeedbackDialog(
                state = state,
                onDismissError = actions.onClearAuthAlert,
            )
        }
    }
}

private fun OrmaAuthProvider.toOnboardingProvider(): AuthProvider = when (this) {
    OrmaAuthProvider.PhoneOtp -> AuthProvider.PhoneOtp
    OrmaAuthProvider.EmailPassword -> AuthProvider.EmailPassword
    OrmaAuthProvider.Google -> AuthProvider.Google
}
