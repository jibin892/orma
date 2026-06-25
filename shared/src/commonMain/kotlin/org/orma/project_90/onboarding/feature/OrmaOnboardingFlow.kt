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
import org.orma.project_90.backend.OrmaDashboardFilters
import org.orma.project_90.backend.OrmaMetaConnectionDraft
import org.orma.project_90.backend.OrmaMetaWhatsAppTemplateDraft
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaPagedList
import org.orma.project_90.backend.OrmaPrinterDraft
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaProductCategoryDraft
import org.orma.project_90.backend.OrmaProductOfferDraft
import org.orma.project_90.backend.OrmaProductImportResult
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaSupplierDraft
import org.orma.project_90.backend.OrmaTeamInviteDraft
import org.orma.project_90.backend.OrmaTeamMemberAccessDraft
import org.orma.project_90.backend.OrmaTeamOverview
import org.orma.project_90.backend.OrmaWorkspacePaymentMethodDraft
import org.orma.project_90.backend.createOrmaBackendClient
import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthResult
import org.orma.project_90.auth.createOrmaAuthGateway
import org.orma.project_90.designsystem.OrmaAdaptiveSurface
import org.orma.project_90.designsystem.OrmaWindowClass
import org.orma.project_90.files.saveOrmaCsvFile
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
import org.orma.project_90.onboarding.DashboardPageTarget
import org.orma.project_90.onboarding.OrmaAuthFeedbackDialog
import org.orma.project_90.onboarding.OrmaSessionRestoreScreen
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingStep
import org.orma.project_90.onboarding.OnboardingUiState
import org.orma.project_90.onboarding.isGstinNumberComplete
import org.orma.project_90.onboarding.isOtpValid
import org.orma.project_90.onboarding.normalizeGstinNumber
import org.orma.project_90.onboarding.desktop.OrmaOnboardingDesktopUi
import org.orma.project_90.onboarding.mobile.OrmaOnboardingMobileUi
import org.orma.project_90.notifications.requestOrmaNotificationPermission
import org.orma.project_90.notifications.currentOrmaNotificationDeviceToken
import org.orma.project_90.notifications.OrmaNotificationTokenException

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
        val resolvedPath = backendSession.resolvedAccessPath()
        val workspace = backendSession.workspace
        val profileName = authenticatedState.teamProfileName
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
            workspaceCoverFileName = workspace?.coverFileName.orEmpty(),
            workspaceCoverUrl = workspace?.coverUrl.orEmpty(),
            notificationsEnabled = backendSession.user.notificationsEnabled,
            teamProfileName = if (resolvedPath == AccessPath.TeamMember) profileName else "",
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
            state.authIdToken.isBlank() &&
            state.authUserId.isBlank() &&
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
            coverUploadLoading = false,
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

    fun uploadBusinessCover(image: OrmaPickedImage) {
        val snapshot = state
        if (snapshot.coverUploadLoading) return
        if (image.sizeBytes > MaxLogoUploadBytes) {
            applyBackendFailure(
                title = "Cover photo too large",
                message = "Choose a PNG, JPG, or WebP image up to 5 MB.",
                code = "COVER_TOO_LARGE",
            )
            return
        }
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            coverUploadLoading = true,
            authStatusMessage = null,
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.uploadBusinessCover(idToken, image)) {
                is OrmaBackendResult.Success -> {
                    val storagePath = result.value.storagePath.ifBlank { image.fileName }
                    state = state.copy(
                        coverUploadLoading = false,
                        workspaceCoverFileName = storagePath,
                        workspaceCoverUrl = result.value.downloadUrl.orEmpty(),
                        authStatusMessage = "Cover photo uploaded.",
                        authErrorTitle = null,
                        authErrorMessage = null,
                        authErrorCode = null,
                    )
                }
                is OrmaBackendResult.Failure -> applyBackendFailure(result.title, result.message, result.code)
            }
        }
    }

    fun handleCoverPickerResult(result: OrmaLogoPickerResult) {
        when (result) {
            OrmaLogoPickerResult.Cancelled -> {
                state = state.copy(coverUploadLoading = false)
            }
            is OrmaLogoPickerResult.Failure -> applyBackendFailure(
                title = result.title,
                message = result.message,
                code = result.code,
            )
            is OrmaLogoPickerResult.Success -> uploadBusinessCover(result.image)
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

    fun lookupInvoiceGstin(input: String) {
        val gstin = normalizeGstinNumber(input)
        val snapshot = state
        if (!isGstinNumberComplete(gstin)) {
            state = snapshot.copy(
                dashboard = snapshot.dashboard.copy(
                    invoiceGstinLookupLoading = false,
                    invoiceGstinLookupNumber = gstin,
                    invoiceGstinLookupStatusMessage = null,
                    invoiceGstinLookupErrorMessage = if (gstin.isBlank()) {
                        null
                    } else {
                        "Enter a valid 15-character GSTIN."
                    },
                    invoiceGstinLookup = null,
                ),
            )
            return
        }
        if (snapshot.dashboard.invoiceGstinLookupLoading && snapshot.dashboard.invoiceGstinLookupNumber == gstin) return
        if (snapshot.dashboard.invoiceGstinLookupStatusMessage != null && snapshot.dashboard.invoiceGstinLookupNumber == gstin) return
        val idToken = backendTokenOrError(snapshot) ?: return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                invoiceGstinLookupLoading = true,
                invoiceGstinLookupNumber = gstin,
                invoiceGstinLookupStatusMessage = null,
                invoiceGstinLookupErrorMessage = null,
                invoiceGstinLookup = null,
            ),
            authErrorTitle = null,
            authErrorMessage = null,
            authErrorCode = null,
        )
        scope.launch {
            when (val result = backendClient.lookupGstin(idToken, gstin)) {
                is OrmaBackendResult.Success -> {
                    if (state.dashboard.invoiceGstinLookupNumber != gstin) return@launch
                    val lookup = result.value
                    val statusMessage = if (lookup.found) {
                        lookup.message.ifBlank { "GSTIN verified." }
                    } else {
                        lookup.message.ifBlank { "GSTIN was not found." }
                    }
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            invoiceGstinLookupLoading = false,
                            invoiceGstinLookupNumber = lookup.gstin.ifBlank { gstin },
                            invoiceGstinLookupStatusMessage = if (lookup.found) statusMessage else null,
                            invoiceGstinLookupErrorMessage = if (lookup.found) null else statusMessage,
                            invoiceGstinLookup = lookup.takeIf { it.found },
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> {
                    if (state.dashboard.invoiceGstinLookupNumber != gstin) return@launch
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            invoiceGstinLookupLoading = false,
                            invoiceGstinLookupNumber = gstin,
                            invoiceGstinLookupStatusMessage = null,
                            invoiceGstinLookupErrorMessage = result.message,
                            invoiceGstinLookup = null,
                        ),
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
        state = snapshot.copy(
            inviteStatusMessage = null,
            inviteErrorMessage = "Team invite setup is no longer available.",
        )
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
            var notificationDeviceToken: org.orma.project_90.notifications.OrmaNotificationDeviceToken? = null
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
                val tokenResult = runCatching { currentOrmaNotificationDeviceToken() }
                notificationDeviceToken = tokenResult.getOrNull()
                if (notificationDeviceToken == null) {
                    val tokenError = tokenResult.exceptionOrNull() as? OrmaNotificationTokenException
                    state = state.copy(
                        notificationsEnabled = false,
                        onboardingLoading = false,
                        authErrorTitle = tokenError?.title ?: "Notifications are not connected",
                        authErrorMessage = tokenError?.message
                            ?: "ORMA could not create a notification token for this device. Try again after checking browser or device notification settings.",
                        authErrorCode = tokenError?.code ?: "NOTIFICATION_DEVICE_TOKEN_MISSING",
                    )
                    return@launch
                }
                true
            } else {
                false
            }

            when (val result = backendClient.updateNotificationPreference(
                idToken = idToken,
                enabled = requestedEnabled,
                deviceToken = notificationDeviceToken?.token,
                platform = notificationDeviceToken?.platform,
                deviceName = notificationDeviceToken?.deviceName,
            )) {
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

    suspend fun freshDashboardTokenOrError(snapshot: OnboardingUiState): String? {
        return when (val result = authGateway.refreshSession()) {
            is OrmaAuthResult.Success -> {
                val session = result.session
                state = state.copy(
                    authUserId = session.uid,
                    authIdToken = session.idToken,
                    authProvider = session.provider.toOnboardingProvider(),
                )
                session.idToken
            }
            is OrmaAuthResult.Failure -> {
                applyDashboardFailure(
                    title = result.title,
                    message = result.message,
                    code = result.code,
                )
                null
            }
            is OrmaAuthResult.OtpSent -> {
                applyDashboardFailure(
                    title = "Session check failed",
                    message = "Sign in again so ORMA can continue securely.",
                    code = "SESSION_REFRESH_INTERRUPTED",
                )
                null
            }
            null -> {
                val token = snapshot.authIdToken.takeIf(String::isNotBlank)
                if (token == null) {
                    applyDashboardFailure(
                        title = "Session expired",
                        message = "Sign in again so ORMA can reopen your workspace securely.",
                        code = "MISSING_ID_TOKEN",
                    )
                }
                token
            }
        }
    }

    suspend fun listOrdersWithPageRecovery(
        idToken: String,
        filters: OrmaDashboardFilters,
        page: Int,
    ): OrmaBackendResult<OrmaPagedList<OrmaOrder>> = when (val result = backendClient.listOrders(idToken, filters.copy(page = page.coerceAtLeast(1)))) {
        is OrmaBackendResult.Success -> {
            if (page > 1 && result.value.items.isEmpty()) {
                backendClient.listOrders(idToken, filters.copy(page = 1))
            } else {
                result
            }
        }
        is OrmaBackendResult.Failure -> result
    }

    fun refreshDashboard(statusMessage: String? = null) {
        val snapshot = state
        if (snapshot.step != OnboardingStep.Dashboard) return
        if (snapshot.dashboard.loading) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                loading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = statusMessage,
                customerOrderHistory = emptyMap(),
                customerOrderHistoryPagination = emptyMap(),
                customerOrderHistoryLoading = emptySet(),
                customerOrderHistoryErrors = emptyMap(),
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            val rawFilters = snapshot.dashboard.filters
            val summary = when (val result = backendClient.getDashboardSummary(idToken, rawFilters)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val filters = rawFilters.forBusinessMode(summary.businessMode.ifBlank { snapshot.draft.businessMode })
            val customers = when (val result = backendClient.listCustomers(idToken, filters.copy(page = snapshot.dashboard.customerPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val suppliers = when (val result = backendClient.listSuppliers(idToken, filters.copy(page = snapshot.dashboard.supplierPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val categories = when (val result = backendClient.listProductCategories(idToken, filters.copy(page = snapshot.dashboard.categoryPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val offers = when (val result = backendClient.listProductOffers(idToken, filters.copy(page = snapshot.dashboard.offerPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val products = when (val result = backendClient.listProducts(idToken, filters.copy(page = snapshot.dashboard.productPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val orders = when (val result = listOrdersWithPageRecovery(idToken, filters, snapshot.dashboard.orderPagination.page)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val printers = when (val result = backendClient.listPrinters(idToken, filters.copy(page = snapshot.dashboard.printerPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val paymentMethods = when (val result = backendClient.listPaymentMethods(idToken, filters.copy(page = snapshot.dashboard.paymentMethodPagination.page))) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val teamOverview = when (val result = backendClient.getTeamOverview(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val metaConnection = when (val result = backendClient.getMetaConnectionStatus(idToken)) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> null
            }
            val metaTemplates = when (val result = backendClient.listMetaWhatsAppTemplates(idToken)) {
                is OrmaBackendResult.Success -> result.value.templates
                is OrmaBackendResult.Failure -> state.dashboard.metaWhatsAppTemplates
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
                    customers = customers.items,
                    suppliers = suppliers.items,
                    categories = categories?.items.orEmpty(),
                    offers = offers?.items.orEmpty(),
                    products = products.items,
                    orders = orders.items,
                    customerPagination = customers.pagination,
                    supplierPagination = suppliers.pagination,
                    categoryPagination = categories?.pagination ?: state.dashboard.categoryPagination.copy(page = 1),
                    offerPagination = offers?.pagination ?: state.dashboard.offerPagination.copy(page = 1),
                    productPagination = products.pagination,
                    orderPagination = orders.pagination,
                    printerPagination = printers?.pagination ?: state.dashboard.printerPagination.copy(page = 1),
                    paymentMethodPagination = paymentMethods?.pagination ?: state.dashboard.paymentMethodPagination.copy(page = 1),
                    printers = printers?.items.orEmpty(),
                    teamMembers = teamOverview?.members.orEmpty(),
                    teamInvites = teamOverview?.invites.orEmpty(),
                    paymentMethods = paymentMethods?.items.orEmpty(),
                    metaConnection = metaConnection,
                    metaWhatsAppTemplates = metaTemplates,
                    metaActionLoading = false,
                    filters = filters,
                ),
            )
        }
    }

    fun resetDashboardOrderPage() {
        state = state.copy(
            dashboard = state.dashboard.copy(
                orderPagination = state.dashboard.orderPagination.copy(page = 1),
            ),
        )
    }

    fun loadDashboardCustomerOrders(customerId: String) {
        val normalizedCustomerId = customerId.trim()
        if (normalizedCustomerId.isBlank()) return
        val snapshot = state
        if (
            snapshot.dashboard.customerOrderHistory.containsKey(normalizedCustomerId) ||
            normalizedCustomerId in snapshot.dashboard.customerOrderHistoryLoading
        ) {
            return
        }
        state = state.copy(
            dashboard = state.dashboard.copy(
                customerOrderHistoryLoading = state.dashboard.customerOrderHistoryLoading + normalizedCustomerId,
                customerOrderHistoryErrors = state.dashboard.customerOrderHistoryErrors - normalizedCustomerId,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot)
            if (idToken == null) {
                state = state.copy(
                    dashboard = state.dashboard.copy(
                        customerOrderHistoryLoading = state.dashboard.customerOrderHistoryLoading - normalizedCustomerId,
                        customerOrderHistoryErrors = state.dashboard.customerOrderHistoryErrors +
                            (normalizedCustomerId to "Sign in again to load this customer's booking history."),
                    ),
                )
                return@launch
            }
            when (val result = backendClient.listCustomerOrders(idToken, normalizedCustomerId)) {
                is OrmaBackendResult.Success -> {
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            customerOrderHistory = state.dashboard.customerOrderHistory + (normalizedCustomerId to result.value.items),
                            customerOrderHistoryPagination = state.dashboard.customerOrderHistoryPagination +
                                (normalizedCustomerId to result.value.pagination),
                            customerOrderHistoryLoading = state.dashboard.customerOrderHistoryLoading - normalizedCustomerId,
                            customerOrderHistoryErrors = state.dashboard.customerOrderHistoryErrors - normalizedCustomerId,
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> {
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            customerOrderHistoryLoading = state.dashboard.customerOrderHistoryLoading - normalizedCustomerId,
                            customerOrderHistoryErrors = state.dashboard.customerOrderHistoryErrors +
                                (normalizedCustomerId to "Could not load this customer's booking history."),
                        ),
                    )
                }
            }
        }
    }

    fun changeDashboardPage(target: DashboardPageTarget, page: Int) {
        val nextPage = page.coerceAtLeast(1)
        val snapshot = state
        if (snapshot.step != OnboardingStep.Dashboard || snapshot.dashboard.loading) return
        state = snapshot.copy(
            dashboard = when (target) {
                DashboardPageTarget.Orders -> snapshot.dashboard.copy(
                    orderPagination = snapshot.dashboard.orderPagination.copy(page = nextPage),
                )
                DashboardPageTarget.Customers -> snapshot.dashboard.copy(
                    customerPagination = snapshot.dashboard.customerPagination.copy(page = nextPage),
                )
                DashboardPageTarget.Suppliers -> snapshot.dashboard.copy(
                    supplierPagination = snapshot.dashboard.supplierPagination.copy(page = nextPage),
                )
                DashboardPageTarget.Products -> snapshot.dashboard.copy(
                    productPagination = snapshot.dashboard.productPagination.copy(page = nextPage),
                )
            },
        )
        refreshDashboard()
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
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createCustomer(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Customer created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardSupplier(draft: OrmaSupplierDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createSupplier(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Supplier created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardSupplier(supplierId: String, draft: OrmaSupplierDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || supplierId.isBlank() || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateSupplier(idToken, supplierId, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Supplier updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardProductCategory(draft: OrmaProductCategoryDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createProductCategory(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Category created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardProductOffer(draft: OrmaProductOfferDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createProductOffer(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Offer created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardProduct(draft: OrmaProductDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createProduct(idToken, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> {
                    val image = draft.image
                    if (image != null) {
                        when (val upload = backendClient.uploadProductImage(idToken, result.value.id, image)) {
                            is OrmaBackendResult.Success -> refreshDashboard("Product created with image.")
                            is OrmaBackendResult.Failure -> applyDashboardFailure(upload.title, "Product was created, but the image could not be uploaded. ${upload.message}", upload.code)
                        }
                    } else {
                        refreshDashboard("Product created.")
                    }
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardProduct(productId: String, draft: OrmaProductDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || productId.isBlank() || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateProduct(idToken, productId, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> {
                    val image = draft.image
                    if (image != null) {
                        when (val upload = backendClient.uploadProductImage(idToken, result.value.id, image)) {
                            is OrmaBackendResult.Success -> refreshDashboard("${result.value.itemType.sellableItemTypeLabel()} updated with image.")
                            is OrmaBackendResult.Failure -> applyDashboardFailure(upload.title, "${result.value.itemType.sellableItemTypeLabel()} was updated, but the image could not be uploaded. ${upload.message}", upload.code)
                        }
                    } else {
                        refreshDashboard("${result.value.itemType.sellableItemTypeLabel()} updated.")
                    }
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun uploadDashboardProductImage(productId: String, image: OrmaPickedImage) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || productId.isBlank()) return
        if (image.sizeBytes > MaxLogoUploadBytes) {
            applyDashboardFailure(
                title = "Image too large",
                message = "Choose a PNG, JPG, or WebP image up to 5 MB.",
                code = "PRODUCT_IMAGE_TOO_LARGE",
            )
            return
        }
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.uploadProductImage(idToken, productId, image)) {
                is OrmaBackendResult.Success -> refreshDashboard("Product image updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun importDashboardProductsCsv(csv: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || csv.trim().isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (
                val result = backendClient.importProductsCsv(
                    idToken = idToken,
                    csv = csv,
                )
            ) {
                is OrmaBackendResult.Success -> {
                    val importMessage = result.value.dashboardImportMessage()
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            productImportResult = result.value,
                            productExport = null,
                            statusMessage = importMessage,
                        ),
                    )
                    refreshDashboard(importMessage)
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun loadDashboardProductImportTemplate() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.getProductImportTemplate(idToken)) {
                is OrmaBackendResult.Success -> {
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            errorTitle = null,
                            errorMessage = null,
                            productImportTemplate = result.value,
                            statusMessage = "Product template ready.",
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun downloadDashboardProductImportTemplate() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading) return
        val existingTemplate = snapshot.dashboard.productImportTemplate
        markDashboardActionLoading()
        scope.launch {
            val template = existingTemplate ?: when (
                val result = backendClient.getProductImportTemplate(
                    freshDashboardTokenOrError(snapshot) ?: return@launch,
                )
            ) {
                is OrmaBackendResult.Success -> result.value
                is OrmaBackendResult.Failure -> {
                    applyDashboardFailure(result.title, result.message, result.code)
                    return@launch
                }
            }
            val saveResult = saveOrmaCsvFile(template.fileName, template.csv)
            state = state.copy(
                dashboard = state.dashboard.copy(
                    actionLoading = false,
                    errorTitle = if (saveResult.saved) null else "Template download failed",
                    errorMessage = if (saveResult.saved) null else saveResult.message,
                    productImportTemplate = template,
                    statusMessage = if (saveResult.saved) saveResult.message else null,
                ),
            )
        }
    }

    fun exportDashboardProductsCsv() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.exportProductsCsv(idToken, snapshot.dashboard.filters)) {
                is OrmaBackendResult.Success -> {
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            errorTitle = null,
                            errorMessage = null,
                            productExport = result.value,
                            productImportResult = null,
                            statusMessage = "Export ready with ${result.value.count} products.",
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun downloadDashboardProductExport() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading) return
        val export = snapshot.dashboard.productExport
        if (export == null || export.csv.isBlank()) {
            state = snapshot.copy(
                dashboard = snapshot.dashboard.copy(
                    errorTitle = "Export not ready",
                    errorMessage = "Prepare the product export first, then download the CSV.",
                    statusMessage = null,
                ),
            )
            return
        }
        markDashboardActionLoading()
        scope.launch {
            val saveResult = saveOrmaCsvFile(export.fileName, export.csv)
            state = state.copy(
                dashboard = state.dashboard.copy(
                    actionLoading = false,
                    errorTitle = if (saveResult.saved) null else "Export download failed",
                    errorMessage = if (saveResult.saved) null else saveResult.message,
                    statusMessage = if (saveResult.saved) saveResult.message else null,
                ),
            )
        }
    }

    fun clearDashboardProductTransfer() {
        state = state.copy(
            dashboard = state.dashboard.copy(
                productExport = null,
                productImportResult = null,
                statusMessage = null,
                errorTitle = null,
                errorMessage = null,
            ),
        )
    }

    fun adjustDashboardProductStock(productId: String, draft: OrmaStockAdjustmentDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || productId.isBlank() || draft.quantityDelta.trim().isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.adjustProductStock(idToken, productId, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Stock updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardOrder(draft: OrmaOrderDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.items.none { it.description.isNotBlank() || it.productId.isNotBlank() }) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createOrder(idToken, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> {
                    resetDashboardOrderPage()
                    refreshDashboard("Order created.")
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardOrder(orderId: String, draft: OrmaOrderDraft) {
        val snapshot = state
        if (
            snapshot.dashboard.actionLoading ||
            orderId.isBlank() ||
            draft.items.none { it.description.isNotBlank() || it.productId.isNotBlank() }
        ) {
            return
        }
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateOrder(idToken, orderId, draft.copy(currency = draft.currency.ifBlank { snapshot.dashboard.summary.currency }))) {
                is OrmaBackendResult.Success -> {
                    resetDashboardOrderPage()
                    refreshDashboard("Booking details saved.")
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardOrderStatus(orderId: String, status: String, paidTotal: String? = null) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || orderId.isBlank() || status.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateOrderStatus(idToken, orderId, status, paidTotal)) {
                is OrmaBackendResult.Success -> {
                    resetDashboardOrderPage()
                    refreshDashboard("Order updated.")
                }
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun applyTeamOverview(overview: OrmaTeamOverview, message: String) {
        state = state.copy(
            dashboard = state.dashboard.copy(
                actionLoading = false,
                errorTitle = null,
                errorMessage = null,
                statusMessage = message,
                teamMembers = overview.members,
                teamInvites = overview.invites,
            ),
        )
    }

    fun createDashboardTeamInvite(draft: OrmaTeamInviteDraft) {
        val snapshot = state
        val hasContact = draft.inviteeEmail.trim().isNotBlank() || draft.inviteePhoneNumber.trim().isNotBlank()
        if (snapshot.dashboard.actionLoading || !hasContact) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createTeamInvite(idToken, draft)) {
                is OrmaBackendResult.Success -> applyTeamOverview(result.value, "Team invite created.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun revokeDashboardTeamInvite(inviteId: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || inviteId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.revokeTeamInvite(idToken, inviteId)) {
                is OrmaBackendResult.Success -> applyTeamOverview(result.value, "Team invite revoked.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardTeamMemberAccess(memberId: String, draft: OrmaTeamMemberAccessDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || memberId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateTeamMemberAccess(idToken, memberId, draft)) {
                is OrmaBackendResult.Success -> applyTeamOverview(result.value, "Team access updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun removeDashboardTeamMember(memberId: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || memberId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.removeTeamMember(idToken, memberId)) {
                is OrmaBackendResult.Success -> applyTeamOverview(result.value, "Team member removed.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardPrinter(draft: OrmaPrinterDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createPrinter(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Printer saved.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardPrinter(printerId: String, draft: OrmaPrinterDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || printerId.isBlank() || draft.name.trim().length < 2) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updatePrinter(idToken, printerId, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("Printer updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun deleteDashboardPrinter(printerId: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || printerId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.deletePrinter(idToken, printerId)) {
                is OrmaBackendResult.Success -> refreshDashboard("Printer removed.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun createDashboardPaymentMethod(draft: OrmaWorkspacePaymentMethodDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || draft.label.trim().length < 2 || !draft.upiId.contains("@")) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createPaymentMethod(idToken, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("UPI saved.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun updateDashboardPaymentMethod(paymentMethodId: String, draft: OrmaWorkspacePaymentMethodDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || paymentMethodId.isBlank() || draft.label.trim().length < 2 || !draft.upiId.contains("@")) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updatePaymentMethod(idToken, paymentMethodId, draft)) {
                is OrmaBackendResult.Success -> refreshDashboard("UPI updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun setDefaultDashboardPaymentMethod(paymentMethodId: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || paymentMethodId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.setDefaultPaymentMethod(idToken, paymentMethodId)) {
                is OrmaBackendResult.Success -> refreshDashboard("Default UPI updated.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun deleteDashboardPaymentMethod(paymentMethodId: String) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || paymentMethodId.isBlank()) return
        markDashboardActionLoading()
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.deletePaymentMethod(idToken, paymentMethodId)) {
                is OrmaBackendResult.Success -> refreshDashboard("UPI deleted.")
                is OrmaBackendResult.Failure -> applyDashboardFailure(result.title, result.message, result.code)
            }
        }
    }

    fun syncDashboardMetaCatalog() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || snapshot.dashboard.metaActionLoading) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                actionLoading = true,
                metaActionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.syncMetaCatalog(idToken)) {
                is OrmaBackendResult.Success -> {
                    val status = when (val statusResult = backendClient.getMetaConnectionStatus(idToken)) {
                        is OrmaBackendResult.Success -> statusResult.value
                        is OrmaBackendResult.Failure -> state.dashboard.metaConnection
                    }
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            metaActionLoading = false,
                            errorTitle = null,
                            errorMessage = null,
                            statusMessage = result.value.message,
                            metaConnection = status,
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> {
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            metaActionLoading = false,
                            errorTitle = result.title,
                            errorMessage = result.message,
                            statusMessage = null,
                        ),
                        authErrorCode = result.code,
                    )
                }
            }
        }
    }

    fun updateDashboardMetaConnection(draft: OrmaMetaConnectionDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || snapshot.dashboard.metaActionLoading) return
        val hasWorkspaceSetup = draft.businessDisplayName.trim().length >= 2 ||
            draft.businessId.trim().isNotBlank() ||
            draft.whatsappBusinessAccountId.trim().isNotBlank() ||
            draft.phoneNumberId.trim().isNotBlank()
        if (!hasWorkspaceSetup) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                actionLoading = true,
                metaActionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.updateMetaConnection(idToken, draft)) {
                is OrmaBackendResult.Success -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = null,
                        errorMessage = null,
                        statusMessage = "WhatsApp setup saved. Backend credentials can be connected next.",
                        metaConnection = result.value,
                    ),
                )
                is OrmaBackendResult.Failure -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = result.title,
                        errorMessage = result.message,
                        statusMessage = null,
                    ),
                    authErrorCode = result.code,
                )
            }
        }
    }

    fun loadDashboardMetaWhatsAppTemplates() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || snapshot.dashboard.metaActionLoading) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                actionLoading = true,
                metaActionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.listMetaWhatsAppTemplates(idToken)) {
                is OrmaBackendResult.Success -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = null,
                        errorMessage = null,
                        statusMessage = result.value.message,
                        metaWhatsAppTemplates = result.value.templates,
                    ),
                )
                is OrmaBackendResult.Failure -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = result.title,
                        errorMessage = result.message,
                        statusMessage = null,
                    ),
                    authErrorCode = result.code,
                )
            }
        }
    }

    fun createDashboardMetaWhatsAppTemplate(draft: OrmaMetaWhatsAppTemplateDraft) {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || snapshot.dashboard.metaActionLoading) return
        if (draft.name.trim().length < 3 || draft.bodyText.trim().length < 10) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                actionLoading = true,
                metaActionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.createMetaWhatsAppTemplate(idToken, draft)) {
                is OrmaBackendResult.Success -> {
                    val templates = when (val listResult = backendClient.listMetaWhatsAppTemplates(idToken)) {
                        is OrmaBackendResult.Success -> listResult.value.templates
                        is OrmaBackendResult.Failure -> state.dashboard.metaWhatsAppTemplates
                    }
                    val status = when (val statusResult = backendClient.getMetaConnectionStatus(idToken)) {
                        is OrmaBackendResult.Success -> statusResult.value
                        is OrmaBackendResult.Failure -> state.dashboard.metaConnection
                    }
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            metaActionLoading = false,
                            errorTitle = null,
                            errorMessage = null,
                            statusMessage = result.value.message,
                            metaConnection = status,
                            metaWhatsAppTemplates = templates,
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = result.title,
                        errorMessage = result.message,
                        statusMessage = null,
                    ),
                    authErrorCode = result.code,
                )
            }
        }
    }

    fun syncDashboardMetaWhatsAppTemplates() {
        val snapshot = state
        if (snapshot.dashboard.actionLoading || snapshot.dashboard.metaActionLoading) return
        state = snapshot.copy(
            dashboard = snapshot.dashboard.copy(
                actionLoading = true,
                metaActionLoading = true,
                errorTitle = null,
                errorMessage = null,
                statusMessage = null,
            ),
        )
        scope.launch {
            val idToken = freshDashboardTokenOrError(snapshot) ?: return@launch
            when (val result = backendClient.syncMetaWhatsAppTemplates(idToken)) {
                is OrmaBackendResult.Success -> {
                    val templates = when (val listResult = backendClient.listMetaWhatsAppTemplates(idToken)) {
                        is OrmaBackendResult.Success -> listResult.value.templates
                        is OrmaBackendResult.Failure -> state.dashboard.metaWhatsAppTemplates
                    }
                    val status = when (val statusResult = backendClient.getMetaConnectionStatus(idToken)) {
                        is OrmaBackendResult.Success -> statusResult.value
                        is OrmaBackendResult.Failure -> state.dashboard.metaConnection
                    }
                    state = state.copy(
                        dashboard = state.dashboard.copy(
                            actionLoading = false,
                            metaActionLoading = false,
                            errorTitle = null,
                            errorMessage = null,
                            statusMessage = result.value.message,
                            metaConnection = status,
                            metaWhatsAppTemplates = templates,
                            metaTemplateSyncItems = result.value.templates,
                        ),
                    )
                }
                is OrmaBackendResult.Failure -> state = state.copy(
                    dashboard = state.dashboard.copy(
                        actionLoading = false,
                        metaActionLoading = false,
                        errorTitle = result.title,
                        errorMessage = result.message,
                        statusMessage = null,
                    ),
                    authErrorCode = result.code,
                )
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
                    workspaceCoverFileName = "",
                    workspaceCoverUrl = "",
                    teamProfileName = "",
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
    val coverPicker = rememberOrmaBusinessLogoPicker(::handleCoverPickerResult)

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
                inviteStatusMessage = null,
                inviteErrorMessage = null,
                step = when (path) {
                    AccessPath.BusinessOwner -> OnboardingStep.Owner
                    AccessPath.TeamMember -> OnboardingStep.Team
                },
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
        onCoverUploadRequest = {
            val snapshot = state
            if (snapshot.coverUploadLoading || snapshot.onboardingLoading) return@OnboardingActions
            if (backendTokenOrError(snapshot) == null) return@OnboardingActions
            coverPicker.launch()
        },
        onSetupStepChange = { state = state.copy(setupStep = it) },
        onNotificationDecision = ::saveNotificationDecision,
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
        onClearDashboardStatusMessage = {
            state = state.copy(
                dashboard = state.dashboard.copy(statusMessage = null),
            )
        },
        onShowDashboardStatusMessage = { message ->
            state = state.copy(
                dashboard = state.dashboard.copy(
                    errorTitle = null,
                    errorMessage = null,
                    statusMessage = message,
                ),
            )
        },
        onDashboardSearchChange = {
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(query = it.take(80)),
                ),
            )
        },
        onOrderStatusFilterChange = {
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(orderStatus = it),
                ),
            )
        },
        onOrderTypeFilterChange = {
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(orderType = it),
                ),
            )
        },
        onDashboardDatePresetChange = { preset, dateFrom, dateTo ->
            val normalizedPreset = preset.take(24).takeIf { it != "all" }.orEmpty()
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(
                        datePreset = normalizedPreset,
                        dateFrom = dateFrom.take(10),
                        dateTo = dateTo.take(10),
                    ),
                ),
            )
            refreshDashboard()
        },
        onDashboardDateFilterChange = { dateFrom, dateTo ->
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(
                        datePreset = "",
                        dateFrom = dateFrom.take(10),
                        dateTo = dateTo.take(10),
                    ),
                ),
            )
        },
        onProductItemTypeFilterChange = {
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(itemType = it),
                ),
            )
        },
        onProductLowStockFilterChange = {
            state = state.copy(
                dashboard = state.dashboard.withResetPagination(
                    filters = state.dashboard.filters.copy(lowStockOnly = it),
                ),
            )
        },
        onDashboardPageChange = ::changeDashboardPage,
        onLoadCustomerOrders = ::loadDashboardCustomerOrders,
        onCreateCustomer = ::createDashboardCustomer,
        onCreateSupplier = ::createDashboardSupplier,
        onUpdateSupplier = ::updateDashboardSupplier,
        onCreateProductCategory = ::createDashboardProductCategory,
        onCreateProductOffer = ::createDashboardProductOffer,
        onCreateProduct = ::createDashboardProduct,
        onUpdateProduct = ::updateDashboardProduct,
        onUploadProductImage = ::uploadDashboardProductImage,
        onLoadProductImportTemplate = ::loadDashboardProductImportTemplate,
        onDownloadProductImportTemplate = ::downloadDashboardProductImportTemplate,
        onImportProductsCsv = ::importDashboardProductsCsv,
        onExportProductsCsv = ::exportDashboardProductsCsv,
        onDownloadProductExport = ::downloadDashboardProductExport,
        onClearProductTransfer = ::clearDashboardProductTransfer,
        onAdjustProductStock = ::adjustDashboardProductStock,
        onCreateOrder = ::createDashboardOrder,
        onUpdateOrder = ::updateDashboardOrder,
        onUpdateOrderStatus = { orderId, status -> updateDashboardOrderStatus(orderId, status) },
        onUpdateOrderStatusWithPayment = { orderId, status, paidTotal ->
            updateDashboardOrderStatus(orderId, status, paidTotal)
        },
        onCreateTeamInvite = ::createDashboardTeamInvite,
        onUpdateTeamMemberAccess = ::updateDashboardTeamMemberAccess,
        onRevokeTeamInvite = ::revokeDashboardTeamInvite,
        onRemoveTeamMember = ::removeDashboardTeamMember,
        onInvoiceGstinLookupRequest = ::lookupInvoiceGstin,
        onCreatePrinter = ::createDashboardPrinter,
        onUpdatePrinter = ::updateDashboardPrinter,
        onDeletePrinter = ::deleteDashboardPrinter,
        onCreatePaymentMethod = ::createDashboardPaymentMethod,
        onUpdatePaymentMethod = ::updateDashboardPaymentMethod,
        onSetDefaultPaymentMethod = ::setDefaultDashboardPaymentMethod,
        onDeletePaymentMethod = ::deleteDashboardPaymentMethod,
        onUpdateMetaConnection = ::updateDashboardMetaConnection,
        onSyncMetaCatalog = ::syncDashboardMetaCatalog,
        onLoadMetaWhatsAppTemplates = ::loadDashboardMetaWhatsAppTemplates,
        onCreateMetaWhatsAppTemplate = ::createDashboardMetaWhatsAppTemplate,
        onSyncMetaWhatsAppTemplates = ::syncDashboardMetaWhatsAppTemplates,
        onCreateBusiness = {
            state = state.copy(
                accessPath = AccessPath.BusinessOwner,
                inviteStatusMessage = null,
                inviteErrorMessage = null,
                workspaceId = "",
                workspaceName = "",
                workspaceLegalName = "",
                workspaceLogoFileName = "",
                workspaceLogoUrl = "",
                workspaceCoverFileName = "",
                workspaceCoverUrl = "",
                teamProfileName = "",
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
        val isOpeningWorkspace = state.step == OnboardingStep.Authentication &&
            (
                state.authLoadingKind == AuthLoadingKind.RestoringSession ||
                    (
                        state.authLoadingKind == AuthLoadingKind.ResolvingWorkspace &&
                            state.authIdToken.isNotBlank()
                        )
                )
        Box(modifier = Modifier.fillMaxSize()) {
            if (isOpeningWorkspace) {
                OrmaSessionRestoreScreen(
                    wide = windowClass == OrmaWindowClass.Wide,
                    title = if (state.authLoadingKind == AuthLoadingKind.ResolvingWorkspace) {
                        "Checking workspace"
                    } else {
                        "Opening workspace"
                    },
                    body = if (state.authLoadingKind == AuthLoadingKind.ResolvingWorkspace) {
                        "Your sign-in is valid. ORMA is opening the right business workspace."
                    } else {
                        "Checking your saved sign-in and workspace access."
                    },
                )
            } else {
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

private fun OrmaBackendSession.resolvedAccessPath(): AccessPath {
    val membershipRole = workspace?.role?.takeIf { it.isNotBlank() } ?: user.role
    return when {
        membershipRole.isOrmaOwnerRole() -> AccessPath.BusinessOwner
        membershipRole.isOrmaTeamRole() -> AccessPath.TeamMember
        accessPath.isOrmaTeamRole() -> AccessPath.TeamMember
        else -> AccessPath.BusinessOwner
    }
}

private fun String?.isOrmaOwnerRole(): Boolean =
    this
        ?.trim()
        ?.lowercase() in setOf("business_owner", "owner", "admin", "administrator")

private fun String?.isOrmaTeamRole(): Boolean =
    this
        ?.trim()
        ?.lowercase() in setOf(
            "team_member",
            "staff",
            "manager",
            "cashier",
            "accountant",
            "inventory",
            "inventory_manager",
            "sales",
            "sales_staff",
            "delivery",
            "delivery_staff",
        )

private fun OrmaBackendSession.shouldOpenDashboard(): Boolean {
    val hasWorkspace = workspace?.id?.isNotBlank() == true
    val role = workspace?.role ?: user.role
    return when {
        requiredStep == "complete" -> true
        onboardingStatus == "complete" -> true
        hasWorkspace && role.isOrmaTeamRole() && onboardingStatus == "team_member_ready" -> true
        else -> false
    }
}

private fun OrmaProductImportResult.dashboardImportMessage(): String =
    when {
        created > 0 && skipped > 0 -> "Imported $created products. Skipped $skipped rows."
        created > 0 -> "Imported $created products."
        skipped > 0 -> "No new products imported. Skipped $skipped rows."
        else -> "No product rows were imported."
    }

private fun OrmaDashboardFilters.forBusinessMode(businessMode: String): OrmaDashboardFilters {
    val normalizedMode = businessMode.normalizedDashboardBusinessMode()
    val allowedOrderTypes = normalizedMode.allowedDashboardOrderTypes()
    val allowedItemTypes = normalizedMode.allowedDashboardItemTypes()
    val normalizedOrderType = orderType.trim().lowercase()
    val normalizedItemType = itemType.trim().lowercase()
    return copy(
        orderType = when {
            allowedOrderTypes.size > 1 && normalizedOrderType == "all" -> "all"
            normalizedOrderType in allowedOrderTypes -> normalizedOrderType
            allowedOrderTypes.size > 1 -> "all"
            else -> allowedOrderTypes.first()
        },
        itemType = when {
            allowedItemTypes.size > 1 && normalizedItemType == "all" -> "all"
            normalizedItemType in allowedItemTypes -> normalizedItemType
            allowedItemTypes.size > 1 -> "all"
            else -> allowedItemTypes.first()
        },
    )
}

private fun DashboardDataState.withResetPagination(filters: OrmaDashboardFilters): DashboardDataState =
    copy(
        filters = filters.copy(page = 1),
        customerPagination = customerPagination.copy(page = 1),
        supplierPagination = supplierPagination.copy(page = 1),
        categoryPagination = categoryPagination.copy(page = 1),
        offerPagination = offerPagination.copy(page = 1),
        productPagination = productPagination.copy(page = 1),
        orderPagination = orderPagination.copy(page = 1),
        printerPagination = printerPagination.copy(page = 1),
        paymentMethodPagination = paymentMethodPagination.copy(page = 1),
        customerOrderHistory = emptyMap(),
        customerOrderHistoryPagination = emptyMap(),
        customerOrderHistoryErrors = emptyMap(),
        customerOrderHistoryLoading = emptySet(),
    )

private fun String.normalizedDashboardBusinessMode(): String =
    when (trim().lowercase()) {
        "service_selling", "service" -> "service_selling"
        "appointment", "appointments" -> "appointment"
        "mixed" -> "mixed"
        else -> "product_selling"
    }

private fun String.allowedDashboardOrderTypes(): List<String> =
    when (normalizedDashboardBusinessMode()) {
        "service_selling" -> listOf("service")
        "appointment" -> listOf("appointment")
        "mixed" -> listOf("sale", "service", "appointment")
        else -> listOf("sale")
    }

private fun String.allowedDashboardItemTypes(): List<String> =
    when (normalizedDashboardBusinessMode()) {
        "service_selling" -> listOf("service")
        "appointment" -> listOf("appointment")
        "mixed" -> listOf("product", "service", "appointment")
        else -> listOf("product")
    }

private fun String.sellableItemTypeLabel(): String =
    when (this) {
        "service" -> "Service"
        "appointment" -> "Appointment"
        else -> "Product"
    }

private const val MaxLogoUploadBytes = 5 * 1024 * 1024
