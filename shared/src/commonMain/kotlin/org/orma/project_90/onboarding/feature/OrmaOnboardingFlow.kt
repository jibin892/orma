package org.orma.project_90.onboarding.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.orma.project_90.getPlatform
import org.orma.project_90.backend.OrmaBackendResult
import org.orma.project_90.backend.OrmaGstinLookup
import org.orma.project_90.backend.OrmaBackendSession
import org.orma.project_90.backend.OrmaCustomerDraft
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaSupplierDraft
import org.orma.project_90.backend.createOrmaBackendClient
import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthResult
import org.orma.project_90.auth.createOrmaAuthGateway
import org.orma.project_90.designsystem.OrmaAdaptiveSurface
import org.orma.project_90.designsystem.OrmaWindowClass
import org.orma.project_90.media.OrmaLogoPickerResult
import org.orma.project_90.media.OrmaPickedImage
import org.orma.project_90.media.rememberOrmaBusinessLogoPicker
import org.orma.project_90.onboarding.AccessPath
import org.orma.project_90.onboarding.AuthLoadingKind
import org.orma.project_90.onboarding.AuthIdentifierType
import org.orma.project_90.onboarding.AuthProvider
import org.orma.project_90.onboarding.BusinessSetupDraft
import org.orma.project_90.onboarding.BusinessSetupStep
import org.orma.project_90.onboarding.DashboardDataState
import org.orma.project_90.onboarding.OrmaAuthFeedbackDialog
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingStep
import org.orma.project_90.onboarding.OnboardingUiState
import org.orma.project_90.onboarding.TeamInviteContactType
import org.orma.project_90.onboarding.isGstinNumberComplete
import org.orma.project_90.onboarding.isOtpValid
import org.orma.project_90.onboarding.normalizeGstinNumber
import org.orma.project_90.onboarding.desktop.OrmaOnboardingDesktopUi
import org.orma.project_90.onboarding.mobile.OrmaOnboardingMobileUi
import org.orma.project_90.notifications.requestOrmaNotificationPermission

@Composable
fun OrmaOnboardingFlow(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(OnboardingUiState(authLoadingKind = AuthLoadingKind.RestoringSession)) }
    val authGateway = remember { createOrmaAuthGateway() }
    val backendClient = remember { createOrmaBackendClient() }
    val scope = rememberCoroutineScope()

    fun restart() {
        if (state.authLoadingKind == AuthLoadingKind.SigningOut) return
        state = state.copy(
            authLoadingKind = AuthLoadingKind.SigningOut,
            onboardingLoading = false,
            inviteLoading = false,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
            authStatusMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            try {
                authGateway.clearStoredSession()
                state = OnboardingUiState()
            } catch (error: Throwable) {
                state = OnboardingUiState().copy(
                    authErrorTitle = "Sign out failed",
                    authErrorMessage = error.message ?: "ORMA could not clear this device session. Try again.",
                    authErrorCode = "SIGN_OUT_FAILED",
                )
            }
        }
    }

    fun routeAfterBackendSession(
        authenticatedState: OnboardingUiState,
        backendSession: OrmaBackendSession,
    ): OnboardingUiState {
        val resolvedPath = when (backendSession.accessPath) {
            "team_member" -> AccessPath.TeamMember
            else -> AccessPath.BusinessOwner
        }
        val pendingInvite = backendSession.pendingInvite
        val workspace = backendSession.workspace ?: pendingInvite?.workspace
        val profileName = authenticatedState.teamProfileName
            .ifBlank { pendingInvite?.inviteeName.orEmpty() }
            .ifBlank { backendSession.user.displayName.orEmpty() }
            .ifBlank {
                authenticatedState.identifier
                    .takeIf { it.contains('@') }
                    ?.substringBefore('@')
                    .orEmpty()
            }
        val routedState = authenticatedState.copy(
            accessPath = resolvedPath,
            workspaceId = workspace?.id.orEmpty(),
            workspaceName = workspace?.businessName.orEmpty(),
            workspaceLegalName = workspace?.legalName.orEmpty(),
            workspaceLogoFileName = workspace?.logoFileName.orEmpty(),
            workspaceLogoUrl = workspace?.logoUrl.orEmpty(),
            notificationsEnabled = backendSession.user.notificationsEnabled,
            teamInviteCode = pendingInvite?.code
                ?: workspace?.inviteCode
                ?: if (resolvedPath == AccessPath.BusinessOwner) "" else authenticatedState.teamInviteCode,
            teamProfileName = if (resolvedPath == AccessPath.TeamMember) profileName else "",
            pendingInviteEmail = pendingInvite?.inviteeEmail.orEmpty(),
            pendingInvitePhoneNumber = pendingInvite?.inviteePhoneNumber.orEmpty(),
            pendingInviteRole = pendingInvite?.role.orEmpty(),
            inviteLoading = false,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
            dashboard = DashboardDataState(),
            draft = authenticatedState.draft.copy(
                logoFileName = authenticatedState.draft.logoFileName.ifBlank { workspace?.logoFileName.orEmpty() },
            ),
        )
        if (backendSession.shouldOpenDashboard()) {
            return routedState.copy(step = OnboardingStep.Dashboard)
        }
        return when (backendSession.requiredStep) {
            "complete" -> routedState.copy(step = OnboardingStep.Dashboard)
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
        val visibleState = if (
            state.step == OnboardingStep.Authentication &&
            (state.identifierType != AuthIdentifierType.Phone || state.authProvider != AuthProvider.PhoneOtp)
        ) {
            state.copy(
                authProvider = AuthProvider.PhoneOtp,
                identifierType = AuthIdentifierType.Phone,
                identifier = "",
                password = "",
                authUserId = "",
                authIdToken = "",
            )
        } else {
            state
        }
        state = visibleState.copy(
            authLoadingKind = AuthLoadingKind.None,
            onboardingLoading = false,
            authStatusMessage = null,
            authErrorTitle = title,
            authErrorMessage = message,
            authErrorCode = code,
            logoUploadLoading = false,
            gstinLookupLoading = false,
            inviteLoading = false,
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
                authStatusMessage = null,
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
                    authStatusMessage = null,
                    authProvider = session.provider.toOnboardingProvider(),
                    identifierType = identifierType,
                    identifier = identifier,
                    authUserId = session.uid,
                    authIdToken = session.idToken,
                    otpCode = "",
                )
                val visibleResolvingState = if (
                    state.step == OnboardingStep.Authentication &&
                    identifierType != AuthIdentifierType.Phone
                ) {
                    authenticatedState.copy(
                        authLoadingKind = AuthLoadingKind.ResolvingWorkspace,
                        authProvider = session.provider.toOnboardingProvider(),
                        identifierType = AuthIdentifierType.Phone,
                        identifier = state.identifier.filter(Char::isDigit).take(state.selectedCountry.maxDigits),
                        password = "",
                    )
                } else {
                    authenticatedState.copy(authLoadingKind = AuthLoadingKind.ResolvingWorkspace)
                }
                state = visibleResolvingState
                when (val backendResult = backendClient.resolveSession(session)) {
                    is OrmaBackendResult.Success -> {
                        state = routeAfterBackendSession(
                            authenticatedState.copy(
                                authLoadingKind = AuthLoadingKind.None,
                                authStatusMessage = null,
                            ),
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
                val clearFailedOtp = state.step == OnboardingStep.Otp &&
                    state.authLoadingKind == AuthLoadingKind.VerifyingOtp
                state = state.copy(
                    authLoadingKind = AuthLoadingKind.None,
                    authStatusMessage = null,
                    otpCode = if (clearFailedOtp) "" else state.otpCode,
                    authErrorTitle = result.title,
                    authErrorMessage = result.message,
                    authErrorCode = result.code,
                )
            }
        }
    }

    suspend fun restoreSavedSession() {
        when (val restoreResult = authGateway.restoreSession()) {
            null -> {
                state = state.copy(authLoadingKind = AuthLoadingKind.None)
            }
            is OrmaAuthResult.Success -> {
                applyAuthResult(restoreResult)
            }
            is OrmaAuthResult.Failure -> {
                state = OnboardingUiState().copy(
                    authErrorTitle = restoreResult.title,
                    authErrorMessage = restoreResult.message,
                    authErrorCode = restoreResult.code,
                )
            }
            is OrmaAuthResult.OtpSent -> {
                state = state.copy(authLoadingKind = AuthLoadingKind.None)
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
            title = "Session expired",
            message = "Sign in again so ORMA can reopen your workspace securely.",
            code = "MISSING_ID_TOKEN",
        )
        return null
    }

    fun uploadBusinessLogo(image: OrmaPickedImage) {
        val snapshot = state
        if (snapshot.logoUploadLoading) return
        if (image.sizeBytes > MaxLogoUploadBytes) {
            applyBackendFailure(
                title = "Logo too large",
                message = "Choose a PNG, JPG, or WebP image up to 5 MB.",
                code = "LOGO_TOO_LARGE",
            )
            return
        }
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            logoUploadLoading = true,
            authStatusMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
            draft = snapshot.draft.copy(
                logoFileName = image.fileName,
                logoPreviewContentType = image.contentType,
                logoPreviewBytes = image.bytes,
            ),
        )
        scope.launch {
            when (val result = backendClient.uploadBusinessLogo(idToken, image)) {
                is OrmaBackendResult.Success -> {
                    val storagePath = result.value.storagePath.ifBlank { image.fileName }
                    state = state.copy(
                        logoUploadLoading = false,
                        workspaceLogoFileName = storagePath,
                        workspaceLogoUrl = result.value.downloadUrl.orEmpty(),
                        authStatusMessage = "Business logo uploaded.",
                        authErrorTitle = null,
                        authErrorMessage = null,
                        authErrorCode = null,
                        draft = state.draft.copy(
                            logoFileName = storagePath,
                            logoPreviewContentType = image.contentType,
                            logoPreviewBytes = image.bytes,
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun handleLogoPickerResult(result: OrmaLogoPickerResult) {
        when (result) {
            OrmaLogoPickerResult.Cancelled -> {
                state = state.copy(logoUploadLoading = false)
            }
            is OrmaLogoPickerResult.Failure -> applyBackendFailure(
                title = result.title,
                message = result.message,
                code = result.code,
            )
            is OrmaLogoPickerResult.Success -> uploadBusinessLogo(result.image)
        }
    }

    fun BusinessSetupDraft.withGstinLookup(lookup: OrmaGstinLookup): BusinessSetupDraft =
        copy(
            taxNumber = lookup.gstin.ifBlank { taxNumber },
            taxLabel = "GSTIN",
            legalName = legalName.ifBlank { lookup.legalName.orEmpty() },
            businessName = businessName.ifBlank {
                lookup.tradeName
                    ?: lookup.legalName
                    ?: ""
            },
            addressLine = addressLine.ifBlank { lookup.addressLine.orEmpty() },
            city = city.ifBlank { lookup.city.orEmpty() },
            region = region.ifBlank { lookup.region.orEmpty() },
            country = "India",
            postalCode = postalCode.ifBlank { lookup.postalCode.orEmpty() },
            currency = "INR",
        )

    fun lookupGstin(input: String) {
        val gstin = normalizeGstinNumber(input)
        val snapshot = state
        if (!snapshot.draft.isTaxRegistered) return
        if (!isGstinNumberComplete(gstin)) {
            state = snapshot.copy(
                gstinLookupStatusMessage = null,
                gstinLookupErrorMessage = if (gstin.isBlank()) {
                    null
                } else {
                    "Enter a valid 15-character GSTIN."
                },
                gstinLookupNumber = gstin,
            )
            return
        }
        if (snapshot.gstinLookupLoading && snapshot.gstinLookupNumber == gstin) return
        if (snapshot.gstinLookupStatusMessage != null && snapshot.gstinLookupNumber == gstin) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            gstinLookupLoading = true,
            gstinLookupNumber = gstin,
            gstinLookupStatusMessage = null,
            gstinLookupErrorMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.lookupGstin(idToken, gstin)) {
                is OrmaBackendResult.Success -> {
                    if (!state.draft.isTaxRegistered || normalizeGstinNumber(state.draft.taxNumber) != gstin) {
                        return@launch
                    }
                    val lookup = result.value
                    val statusMessage = if (lookup.found) {
                        lookup.message.ifBlank { "GSTIN verified." }
                    } else {
                        lookup.message.ifBlank { "GSTIN was not found." }
                    }
                    state = state.copy(
                        gstinLookupLoading = false,
                        gstinLookupNumber = lookup.gstin.ifBlank { gstin },
                        gstinLookupStatusMessage = if (lookup.found) statusMessage else null,
                        gstinLookupErrorMessage = if (lookup.found) null else statusMessage,
                        draft = if (lookup.found) {
                            state.draft.withGstinLookup(lookup)
                        } else {
                            state.draft.copy(taxNumber = lookup.gstin.ifBlank { gstin })
                        },
                    )
                }
                is OrmaBackendResult.Failure -> {
                    if (!state.draft.isTaxRegistered || normalizeGstinNumber(state.draft.taxNumber) != gstin) {
                        return@launch
                    }
                    state = state.copy(
                        gstinLookupLoading = false,
                        gstinLookupNumber = gstin,
                        gstinLookupStatusMessage = null,
                        gstinLookupErrorMessage = result.message,
                    )
                }
            }
        }
    }

    fun saveBusinessSetup() {
        val snapshot = state
        if (snapshot.onboardingLoading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            onboardingLoading = true,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
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
        if (snapshot.teamInviteCode.isBlank() || snapshot.onboardingLoading) return
        val profileName = snapshot.teamProfileName.trim()
        if (profileName.length < 2) {
            state = snapshot.copy(
                inviteStatusMessage = null,
                inviteErrorMessage = "Enter your name before joining this workspace.",
            )
            return
        }
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            onboardingLoading = true,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (
                val result = backendClient.joinTeamInvite(
                    idToken = idToken,
                    code = snapshot.teamInviteCode,
                    displayName = profileName,
                )
            ) {
                is OrmaBackendResult.Success -> {
                    state = applyBackendSessionMutation(state, result.value, OnboardingStep.Notification)
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun refreshTeamInvite() {
        val snapshot = state
        if (snapshot.inviteLoading || snapshot.accessPath != AccessPath.BusinessOwner) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            inviteLoading = true,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.getActiveTeamInvite(idToken)) {
                is OrmaBackendResult.Success -> {
                    val invite = result.value
                    state = state.copy(
                        inviteLoading = false,
                        inviteStatusMessage = "Invite code ready.",
                        inviteErrorMessage = null,
                        teamInviteCode = invite.code.ifBlank { invite.workspace.inviteCode.orEmpty() },
                        workspaceId = invite.workspace.id.ifBlank { state.workspaceId },
                        workspaceName = invite.workspace.businessName.ifBlank { state.workspaceName },
                        workspaceLegalName = invite.workspace.legalName.ifBlank { state.workspaceLegalName },
                        workspaceLogoFileName = invite.workspace.logoFileName.orEmpty().ifBlank { state.workspaceLogoFileName },
                        workspaceLogoUrl = invite.workspace.logoUrl.orEmpty().ifBlank { state.workspaceLogoUrl },
                    )
                }
                is OrmaBackendResult.Failure -> {
                    state = state.copy(
                        inviteLoading = false,
                        inviteStatusMessage = null,
                        inviteErrorMessage = result.message,
                    )
                }
            }
        }
    }

    fun createTeamInvite() {
        val snapshot = state
        if (snapshot.inviteLoading || snapshot.accessPath != AccessPath.BusinessOwner) return
        val inviteeName = snapshot.teamInviteName.trim()
        val contact = snapshot.teamInviteContact.trim()
        if (inviteeName.length < 2) {
            state = snapshot.copy(
                inviteStatusMessage = null,
                inviteErrorMessage = "Enter the team member name.",
            )
            return
        }
        if (contact.isBlank()) {
            state = snapshot.copy(
                inviteStatusMessage = null,
                inviteErrorMessage = "Enter a phone number or email for this team member.",
            )
            return
        }
        val email = if (snapshot.teamInviteContactType == TeamInviteContactType.Email) contact else null
        val phoneNumber = if (snapshot.teamInviteContactType == TeamInviteContactType.Phone) contact else null
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            inviteLoading = true,
            inviteStatusMessage = null,
            inviteErrorMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (
                val result = backendClient.createTeamInvite(
                    idToken = idToken,
                    name = inviteeName,
                    email = email,
                    phoneNumber = phoneNumber,
                    role = snapshot.teamInviteRole,
                )
            ) {
                is OrmaBackendResult.Success -> {
                    val invite = result.value
                    state = state.copy(
                        inviteLoading = false,
                        inviteStatusMessage = "Invite created for ${invite.inviteeName ?: inviteeName}.",
                        inviteErrorMessage = null,
                        teamInviteCode = invite.code.ifBlank { invite.workspace.inviteCode.orEmpty() },
                        workspaceId = invite.workspace.id.ifBlank { state.workspaceId },
                        workspaceName = invite.workspace.businessName.ifBlank { state.workspaceName },
                        workspaceLegalName = invite.workspace.legalName.ifBlank { state.workspaceLegalName },
                        workspaceLogoFileName = invite.workspace.logoFileName.orEmpty().ifBlank { state.workspaceLogoFileName },
                        workspaceLogoUrl = invite.workspace.logoUrl.orEmpty().ifBlank { state.workspaceLogoUrl },
                    )
                }
                is OrmaBackendResult.Failure -> {
                    state = state.copy(
                        inviteLoading = false,
                        inviteStatusMessage = null,
                        inviteErrorMessage = result.message,
                    )
                }
            }
        }
    }

    fun saveNotificationDecision(enabled: Boolean) {
        val snapshot = state
        if (snapshot.onboardingLoading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            notificationsEnabled = if (enabled) snapshot.notificationsEnabled else false,
            onboardingLoading = true,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            val requestedEnabled = if (enabled) {
                val permission = requestOrmaNotificationPermission()
                if (!permission.enabled) {
                    state = state.copy(
                        notificationsEnabled = false,
                        onboardingLoading = false,
                        authErrorTitle = permission.title,
                        authErrorMessage = permission.message,
                        authErrorCode = permission.code,
                    )
                    return@launch
                }
                true
            } else {
                false
            }

            when (val result = backendClient.updateNotificationPreference(idToken, requestedEnabled)) {
                is OrmaBackendResult.Success -> {
                    state = routeAfterBackendSession(
                        state.copy(
                            notificationsEnabled = requestedEnabled,
                            onboardingLoading = false,
                            authStatusMessage = null,
                            authErrorTitle = null,
                            authErrorMessage = null,
                            authErrorCode = null,
                        ),
                        result.value,
                    )
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun applyDashboardFailure(
        title: String,
        message: String,
        code: String? = null,
    ) {
        state = state.copy(
            dashboard = state.dashboard.copy(
                loading = false,
                actionLoading = false,
                errorTitle = title,
                errorMessage = message,
                statusMessage = null,
            ),
            authErrorCode = code,
        )
    }

    fun refreshDashboard(statusMessage: String? = null) {
        val snapshot = state
        if (snapshot.step != OnboardingStep.Dashboard) return
        if (snapshot.dashboard.loading) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                loading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = statusMessage,
            ),
        )
        scope.launch {
            val summary = when (val result = backendClient.getDashboardSummary(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val customers = when (val result = backendClient.listCustomers(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val suppliers = when (val result = backendClient.listSuppliers(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val products = when (val result = backendClient.listProducts(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val orders = when (val result = backendClient.listOrders(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            state = state.copy(
                dashboard = state.dashboard.copy(
                    hasLoaded = true,
                    loading = false,
                    actionLoading = false,
                    errorTitle = null,
                    errorMessage = null,
                    statusMessage = statusMessage,
                    summary = summary,
                    customers = customers,
                    suppliers = suppliers,
                    products = products,
                    orders = orders,
                ),
            )
        }
    }

    fun markDashboardActionLoading() {
        state = state.copy(
            dashboard = state.dashboard.copy(
                actionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
    }

    fun createDashboardCustomer(draft: OrmaCustomerDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.createCustomer(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Customer created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardSupplier(draft: OrmaSupplierDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.createSupplier(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Supplier created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardProduct(draft: OrmaProductDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.createProduct(idToken, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> refreshDashboard("Product created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun adjustDashboardProductStock(productId: String, draft: OrmaStockAdjustmentDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || productId.isBlank() || draft.quantityDelta.trim().isBlank()) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.adjustProductStock(idToken, productId, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Stock updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardOrder(draft: OrmaOrderDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.items.none { it.description.isNotBlank() || it.productId.isNotBlank() }) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.createOrder(idToken, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> refreshDashboard("Order created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardOrderStatus(orderId: String, status: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || orderId.isBlank() || status.isBlank()) return
        val idToken = backendTokenOrError(snapshot) ?: return
        markDashboardActionLoading()
        scope.launch {
            when (val result = backendClient.updateOrderStatus(idToken, orderId, status)) {
                is OrmaBackendResult.Success -> refreshDashboard("Order updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
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
            OnboardingStep.Otp -> if (isSignedIn) {
                state.copy(onboardingLoading = false)
            } else {
                state.copy(
                    step = OnboardingStep.Authentication,
                    otpCode = "",
                    authLoadingKind = AuthLoadingKind.None,
                    authErrorTitle = null,
                    authErrorMessage = null,
                    authErrorCode = null,
                )
            }
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
                    workspaceLegalName = "",
                    workspaceLogoFileName = "",
                    workspaceLogoUrl = "",
                    teamProfileName = "",
                    pendingInviteEmail = "",
                    pendingInvitePhoneNumber = "",
                    pendingInviteRole = "",
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
            OnboardingStep.Complete -> state.copy(step = OnboardingStep.Dashboard)
            OnboardingStep.Dashboard -> state.copy(onboardingLoading = false)
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
            OnboardingStep.Notification,
            OnboardingStep.Complete -> state.copy(step = OnboardingStep.Dashboard)
            OnboardingStep.Dashboard -> state
        }
    }

    val logoPicker = rememberOrmaBusinessLogoPicker(::handleLogoPickerResult)

    LaunchedEffect(Unit) {
        restoreSavedSession()
    }

    LaunchedEffect(state.step, state.authIdToken, state.workspaceId, state.dashboard.hasLoaded) {
        if (
            state.step == OnboardingStep.Dashboard &&
            state.authIdToken.isNotBlank() &&
            !state.dashboard.hasLoaded &&
            !state.dashboard.loading
        ) {
            refreshDashboard()
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
                inviteStatusMessage = null,
                inviteErrorMessage = null,
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
                    inviteStatusMessage = null,
                    inviteErrorMessage = null,
                )
            } else {
                state.copy(
                    authProvider = AuthProvider.EmailPassword,
                    identifier = it.trim().take(120),
                    authErrorTitle = null,
                    authErrorMessage = null,
                    authErrorCode = null,
                    inviteStatusMessage = null,
                    inviteErrorMessage = null,
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
                inviteStatusMessage = null,
                inviteErrorMessage = null,
                step = when (path) {
                    AccessPath.BusinessOwner -> OnboardingStep.Owner
                    AccessPath.TeamMember -> OnboardingStep.Team
                },
            )
        },
        onTeamInviteCodeChange = {
            state = state.copy(
                teamInviteCode = it.uppercase().filter(Char::isLetterOrDigit).take(16),
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
                inviteStatusMessage = null,
                inviteErrorMessage = null,
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
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onDraftChange = {
            val currentTaxNumber = state.draft.taxNumber
            val nextTaxNumber = it.taxNumber
            val clearGstinLookup = currentTaxNumber != nextTaxNumber || !it.isTaxRegistered
            val clearLogo = state.draft.logoFileName.isNotBlank() && it.logoFileName.isBlank()
            state = state.copy(
                draft = it,
                workspaceLogoFileName = if (clearLogo) "" else state.workspaceLogoFileName,
                workspaceLogoUrl = if (clearLogo) "" else state.workspaceLogoUrl,
                authErrorTitle = null,
                authErrorMessage = null,
                authErrorCode = null,
                gstinLookupNumber = if (clearGstinLookup) "" else state.gstinLookupNumber,
                gstinLookupStatusMessage = if (clearGstinLookup) null else state.gstinLookupStatusMessage,
                gstinLookupErrorMessage = if (clearGstinLookup) null else state.gstinLookupErrorMessage,
                gstinLookupLoading = if (clearGstinLookup) false else state.gstinLookupLoading,
            )
        },
        onGstinLookupRequest = ::lookupGstin,
        onLogoUploadRequest = {
            val snapshot = state
            if (snapshot.logoUploadLoading || snapshot.onboardingLoading) return@OnboardingActions
            if (backendTokenOrError(snapshot) == null) return@OnboardingActions
            logoPicker.launch()
        },
        onSetupStepChange = { state = state.copy(setupStep = it) },
        onNotificationDecision = ::saveNotificationDecision,
        onRefreshTeamInvite = ::refreshTeamInvite,
        onTeamInviteNameChange = {
            state = state.copy(
                teamInviteName = it.take(120),
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onTeamInviteContactTypeChange = {
            state = state.copy(
                teamInviteContactType = it,
                teamInviteContact = "",
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onTeamInviteContactChange = {
            val value = if (state.teamInviteContactType == TeamInviteContactType.Phone) {
                it.filter { character -> character.isDigit() || character == '+' }.take(24)
            } else {
                it.trim().take(160)
            }
            state = state.copy(
                teamInviteContact = value,
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onTeamInviteRoleChange = {
            state = state.copy(
                teamInviteRole = it,
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onCreateTeamInvite = ::createTeamInvite,
        onTeamProfileNameChange = {
            state = state.copy(
                teamProfileName = it.take(120),
                inviteStatusMessage = null,
                inviteErrorMessage = null,
            )
        },
        onDashboardRefresh = { refreshDashboard() },
        onClearDashboardMessage = {
            state = state.copy(
                dashboard = state.dashboard.copy(
                    errorTitle = null,
                    errorMessage = null,
                    statusMessage = null,
                ),
            )
        },
        onCreateCustomer = ::createDashboardCustomer,
        onCreateSupplier = ::createDashboardSupplier,
        onCreateProduct = ::createDashboardProduct,
        onAdjustProductStock = ::adjustDashboardProductStock,
        onCreateOrder = ::createDashboardOrder,
        onUpdateOrderStatus = ::updateDashboardOrderStatus,
        onCreateBusiness = {
            state = state.copy(
                accessPath = AccessPath.BusinessOwner,
                teamInviteCode = "",
                inviteStatusMessage = null,
                inviteErrorMessage = null,
                teamInviteName = "",
                teamInviteContact = "",
                workspaceId = "",
                workspaceName = "",
                workspaceLegalName = "",
                workspaceLogoFileName = "",
                workspaceLogoUrl = "",
                teamProfileName = "",
                pendingInviteEmail = "",
                pendingInvitePhoneNumber = "",
                pendingInviteRole = "",
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

private fun OrmaBackendSession.shouldOpenDashboard(): Boolean {
    val hasWorkspace = workspace?.id?.isNotBlank() == true
    val role = workspace?.role ?: user.role
    return when {
        requiredStep == "complete" -> true
        onboardingStatus == "complete" -> true
        hasWorkspace && role == "team_member" && onboardingStatus == "team_member_ready" -> true
        else -> false
    }
}

private const val MaxLogoUploadBytes = 5 * 1024 * 1024
