package org.orma.project_90.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orma.project_90.calendar.ormaCurrentIsoDate
import org.orma.project_90.backend.OrmaCustomer
import org.orma.project_90.backend.OrmaCustomerDraft
import org.orma.project_90.backend.OrmaDashboardFilters
import org.orma.project_90.backend.OrmaDashboardRevenuePoint
import org.orma.project_90.backend.OrmaGstinLookup
import org.orma.project_90.backend.OrmaMetaProductReadiness
import org.orma.project_90.backend.OrmaMetaConnectionDraft
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaOrderItem
import org.orma.project_90.backend.OrmaOrderItemDraft
import org.orma.project_90.backend.OrmaPagination
import org.orma.project_90.backend.OrmaPrinterDraft
import org.orma.project_90.backend.OrmaPrinterProfile
import org.orma.project_90.backend.OrmaProduct
import org.orma.project_90.backend.OrmaProductCategory
import org.orma.project_90.backend.OrmaProductCategoryDraft
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaProductOfferDraft
import org.orma.project_90.backend.OrmaSupplier
import org.orma.project_90.backend.OrmaSupplierDraft
import org.orma.project_90.backend.OrmaTeamInvite
import org.orma.project_90.backend.OrmaTeamInviteDraft
import org.orma.project_90.backend.OrmaTeamMember
import org.orma.project_90.backend.OrmaWorkspacePaymentMethodDraft
import org.orma.project_90.components.atoms.OrmaDashboardEmptyState
import org.orma.project_90.components.atoms.OrmaDashboardIconBubble
import org.orma.project_90.components.atoms.OrmaDashboardMetric
import org.orma.project_90.components.atoms.OrmaDashboardRecordSurface
import org.orma.project_90.components.molecules.OrmaDashboardAction
import org.orma.project_90.components.molecules.OrmaDashboardActionVariant
import org.orma.project_90.components.molecules.OrmaDashboardChecklistCard
import org.orma.project_90.components.molecules.OrmaDashboardChecklistRow
import org.orma.project_90.components.molecules.OrmaDashboardMetricLine
import org.orma.project_90.components.molecules.OrmaDashboardPanel
import org.orma.project_90.components.organisms.OrmaDashboardActivityPanel
import org.orma.project_90.components.organisms.OrmaDashboardBreakdownPanel
import org.orma.project_90.components.organisms.OrmaDashboardNotificationPanel
import org.orma.project_90.components.organisms.OrmaDashboardRevenueCard
import org.orma.project_90.components.organisms.OrmaDashboardRevenueChart
import org.orma.project_90.components.organisms.OrmaDashboardStatsGrid
import org.orma.project_90.components.organisms.OrmaDashboardTopItemsPanel
import org.orma.project_90.components.templates.OrmaDashboardResponsiveWorkspace
import org.orma.project_90.components.templates.OrmaDashboardSectionScaffold
import org.orma.project_90.clipboard.rememberOrmaClipboard
import org.orma.project_90.downloads.isOrmaWebDownloadSurface
import org.orma.project_90.files.OrmaCsvFilePickerResult
import org.orma.project_90.files.rememberOrmaCsvFilePicker
import org.orma.project_90.media.OrmaLogoPickerResult
import org.orma.project_90.media.OrmaLogoPreviewImage
import org.orma.project_90.media.OrmaPickedImage
import org.orma.project_90.media.OrmaProductAiImageResult
import org.orma.project_90.media.OrmaRemoteImage
import org.orma.project_90.media.generateOrmaProductAiImage
import org.orma.project_90.media.isOrmaProductAiImageGenerationAvailable
import org.orma.project_90.media.rememberOrmaBusinessLogoPicker
import org.orma.project_90.designsystem.OrmaActionRow
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaBrandMark
import org.orma.project_90.designsystem.OrmaCalendarDateField
import org.orma.project_90.designsystem.OrmaCalendarDateTimeField
import org.orma.project_90.designsystem.OrmaChevronDownIcon
import org.orma.project_90.designsystem.OrmaCloseIcon
import org.orma.project_90.designsystem.OrmaChoiceSurface
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaElevation
import org.orma.project_90.designsystem.OrmaFlatIcon
import org.orma.project_90.designsystem.OrmaFlatIconKind
import org.orma.project_90.designsystem.OrmaFormCard
import org.orma.project_90.designsystem.OrmaGoogleBrandIcon
import org.orma.project_90.designsystem.OrmaIndentedDivider
import org.orma.project_90.designsystem.OrmaKeyValueList
import org.orma.project_90.designsystem.OrmaListRow
import org.orma.project_90.designsystem.OrmaOtpCells
import org.orma.project_90.designsystem.OrmaQrCode
import org.orma.project_90.designsystem.OrmaScreenColumn
import org.orma.project_90.designsystem.OrmaSecondaryButton
import org.orma.project_90.designsystem.OrmaSectionHeader
import org.orma.project_90.designsystem.OrmaSegmentedRow
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.OrmaSwitchRow
import org.orma.project_90.designsystem.OrmaTextButton
import org.orma.project_90.designsystem.OrmaTextField
import org.orma.project_90.designsystem.OrmaUploadImageIcon
import org.orma.project_90.documents.rememberOrmaOrderDocumentExporter
import org.orma.project_90.publiccatalog.currentOrmaPublicCatalogUrl

private const val ProductImageMaxBytes = 5 * 1024 * 1024
private val DashboardCounterCreateOrderTypes = listOf("sale", "appointment", "service")

@Composable
internal fun OnboardingStageContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    when (state.step) {
        OnboardingStep.Authentication -> AuthenticationStage(state = state, actions = actions)
        OnboardingStep.Otp -> OtpStage(state = state, actions = actions)
        OnboardingStep.Owner -> OwnerStage(state = state, actions = actions)
        OnboardingStep.Team -> TeamStage(state = state, actions = actions)
        OnboardingStep.BusinessSetup -> BusinessSetupStage(state = state, actions = actions, wide = wide)
        OnboardingStep.Notification -> NotificationPermissionStage(state = state, actions = actions, wide = wide)
        OnboardingStep.Complete -> CompleteStage(state = state, actions = actions)
        OnboardingStep.Dashboard -> DashboardStage(state = state, actions = actions, wide = wide)
    }
}

@Composable
private fun AuthenticationStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    LaunchedEffect(Unit) {
        if (state.identifierType != AuthIdentifierType.Phone) {
            actions.onIdentifierTypeChange(AuthIdentifierType.Phone)
        }
    }

    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    val fieldError = loginIdentifierError(
        identifierType = AuthIdentifierType.Phone,
        identifier = state.identifier,
        country = state.selectedCountry,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Enter your phone number - we'll send you a verification code.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OrmaSectionHeader(text = "Phone number")
            LoginIdentifierField(
                value = state.identifier,
                onValueChange = actions.onIdentifierChange,
                type = AuthIdentifierType.Phone,
                selectedCountry = state.selectedCountry,
                onCountryClick = { showCountryPicker = true },
                supportingText = fieldError,
                showLabel = false,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            OrmaActionRow(
                primaryText = when (state.authLoadingKind) {
                    AuthLoadingKind.SendingOtp -> "Sending..."
                    else -> "Send code"
                },
                onPrimary = actions.onContinue,
                primaryEnabled = state.loginReady && !state.isAuthLoading,
            )
            AuthDivider()
            GoogleSignInButton(
                onClick = actions.onGoogleSignIn,
                enabled = !state.isAuthLoading,
            )
            Text(
                text = "By continuing you agree to ORMA's Terms & Privacy Policy.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (isOrmaWebDownloadSurface()) {
                OrmaDesktopDownloadPanel(wide = false)
            }
        }

        if (showCountryPicker) {
            CountryPickerSheet(
                selectedCountry = state.selectedCountry,
                onDismiss = { showCountryPicker = false },
                onSelect = { country ->
                    actions.onCountryChange(country)
                    showCountryPicker = false
                },
            )
        }
    }
}

@Composable
private fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = OrmaColors.Hairline)
        Text(
            text = "or",
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = OrmaColors.Hairline)
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = OrmaShapes.SmallCard,
        enabled = enabled,
        color = OrmaColors.ScreenBackground.copy(alpha = if (enabled) 1f else 0.62f),
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaGoogleBrandIcon(
                enabled = enabled,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (enabled) "Continue with Google" else "Please wait",
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            )
        }
    }
}

@Composable
private fun OtpStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    var resendCountdown by rememberSaveable { mutableStateOf(30) }
    var resendKey by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(resendKey) {
        resendCountdown = 30
        while (resendCountdown > 0) {
            delay(1_000)
            resendCountdown -= 1
        }
    }

    LaunchedEffect(state.otpCode, state.isAuthLoading) {
        if (isOtpValid(state.otpCode) && !state.isAuthLoading) {
            delay(300)
            actions.onContinue()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Enter verification code",
                style = MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "We sent a 6-digit code to ${state.otpDestinationLabel()}.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OrmaSectionHeader(text = "Verification code")
            OtpInput(code = state.otpCode, onCodeChange = actions.onOtpChange)
            state.authStatusMessage?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaActionRow(
                primaryText = when (state.authLoadingKind) {
                    AuthLoadingKind.VerifyingOtp -> "Verifying..."
                    AuthLoadingKind.SendingOtp -> "Sending..."
                    else -> "Verify code"
                },
                onPrimary = actions.onContinue,
                primaryEnabled = isOtpValid(state.otpCode) && !state.isAuthLoading,
            )
            OrmaTextButton(
                text = "Change phone number",
                onClick = actions.onBack,
                enabled = !state.isAuthLoading,
            )
            if (resendCountdown > 0) {
                Text(
                    text = "Resend code in ${resendCountdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            } else {
                OrmaTextButton(
                    text = "Resend code",
                    onClick = {
                        actions.onOtpChange("")
                        actions.onResendOtp()
                        resendKey += 1
                    },
                    enabled = !state.isAuthLoading,
                )
            }
            Text(
                text = "Keep this screen open while ORMA verifies your access.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OwnerStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MobileStageHeader(
            eyebrow = "OWNER PROFILE",
            title = "Create the admin owner",
            body = "This user controls setup, invoices, tax, billing, and team access.",
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaSectionHeader(text = "Profile")
            OrmaTextField(
                value = state.draft.ownerName,
                onValueChange = { actions.onDraftChange(state.draft.copy(ownerName = it)) },
                label = "Owner name",
                placeholder = "Full name",
            )
        }

        OrmaActionRow(
            primaryText = "Continue",
            onPrimary = actions.onContinue,
            primaryEnabled = state.ownerReady,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OrmaTextButton(
                text = "Use a different account",
                onClick = actions.onRestart,
            )
        }
    }
}

@Composable
private fun TeamStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MobileStageHeader(
            eyebrow = "TEAM ACCESS",
            title = "Use an active workspace account",
            body = "Team access is managed by the workspace. Sign in with an account that already has access, or set up a new business.",
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaSectionHeader(text = "Access")
            MobileAccountSummary(
                rows = listOf(
                    "Signed in as" to state.identifier.trim().ifBlank { "Authenticated user" },
                    "Workspace" to state.workspaceName.ifBlank { "Not linked" },
                    "Access" to "No active workspace access",
                ),
            )
        }

        val inviteErrorMessage = state.inviteErrorMessage
        if (!inviteErrorMessage.isNullOrBlank()) {
            Text(
                text = inviteErrorMessage,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.Error,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OrmaTextButton(
                text = "Set up a business instead",
                onClick = actions.onCreateBusiness,
            )
            OrmaTextButton(
                text = "Use a different account",
                onClick = actions.onRestart,
            )
        }
    }
}

private fun teamRoleLabel(role: String): String = when (role.trim().lowercase()) {
    "business_owner", "owner" -> "Business owner"
    "admin", "administrator" -> "Admin"
    "team_member", "staff" -> "Staff"
    "manager" -> "Manager"
    "cashier" -> "Cashier"
    "accountant" -> "Accountant"
    "inventory" -> "Inventory"
    "inventory_manager" -> "Inventory"
    "sales" -> "Sales"
    "sales_staff" -> "Sales"
    else -> "Staff"
}

@Composable
private fun MobileStageHeader(
    eyebrow: String,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.TextTertiary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = OrmaColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = OrmaColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MobileAccountSummary(
    rows: List<Pair<String, String>>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.first,
                        modifier = Modifier.weight(0.85f),
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = row.second,
                        modifier = Modifier.weight(1.15f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextPrimary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = OrmaColors.Hairline)
                }
            }
        }
    }
}

@Composable
private fun BusinessSetupStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    val steps = BusinessSetupStep.entries
    val currentIndex = steps.indexOf(state.setupStep)

    if (!wide) {
        BusinessSetupMobileStage(
            state = state,
            actions = actions,
            steps = steps,
            currentIndex = currentIndex,
        )
        return
    }

    BusinessSetupWideStage(
        state = state,
        actions = actions,
        steps = steps,
        currentIndex = currentIndex,
    )
}

@Composable
private fun BusinessSetupMobileStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    steps: List<BusinessSetupStep>,
    currentIndex: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        MobileStageHeader(
            eyebrow = "BUSINESS SETUP - STEP ${currentIndex + 1} OF ${steps.size}",
            title = state.setupStep.title,
            body = state.setupStep.description,
        )

        SetupProgressLine(currentIndex = currentIndex, total = steps.size)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaSectionHeader(text = "Details")
            BusinessSetupForm(state = state, actions = actions)
        }

        SetupFlowActions(
            currentIndex = currentIndex,
            steps = steps,
            state = state,
            actions = actions,
            large = false,
        )
    }
}

@Composable
private fun BusinessSetupWideStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    steps: List<BusinessSetupStep>,
    currentIndex: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.widthIn(min = 280.dp, max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OrmaSectionHeader(text = "Workspace setup")
            Text(
                text = "Set up your business",
                style = MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Complete the required details once. ORMA uses them for invoices, tax, orders, and account access.",
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
            )
            SetupProgressLine(
                currentIndex = currentIndex,
                total = steps.size,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.StandardCell,
                color = OrmaColors.CellBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    steps.forEachIndexed { index, step ->
                        OrmaListRow(
                            label = step.title,
                            value = if (index < currentIndex) "Done" else if (index == currentIndex) "Now" else null,
                            sub = "Step ${index + 1} of ${steps.size}",
                            enabled = index <= currentIndex,
                            onClick = if (index <= currentIndex) {
                                { actions.onSetupStepChange(step) }
                            } else {
                                null
                            },
                        )
                        if (index != steps.lastIndex) {
                            OrmaIndentedDivider()
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrmaSectionHeader(text = "Step ${currentIndex + 1} of ${steps.size}")
                Text(
                    text = state.setupStep.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = state.setupStep.description,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Start,
                )
            }
            BusinessSetupForm(state = state, actions = actions)
            SetupFlowActions(
                currentIndex = currentIndex,
                steps = steps,
                state = state,
                actions = actions,
                large = true,
            )
        }
    }
}

@Composable
private fun SetupProgressLine(
    currentIndex: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(OrmaShapes.Capsule)
                    .background(
                        if (index <= currentIndex) {
                            OrmaColors.Accent
                        } else {
                            OrmaColors.Accent.copy(alpha = 0.10f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun SetupFlowActions(
    currentIndex: Int,
    steps: List<BusinessSetupStep>,
    state: OnboardingUiState,
    actions: OnboardingActions,
    large: Boolean,
) {
    val primaryText = when {
        state.onboardingLoading -> "Saving..."
        currentIndex == steps.lastIndex -> "Finish setup"
        else -> "Continue"
    }
    val primaryEnabled = state.setupReady && !state.onboardingLoading
    if (currentIndex == 0) {
        SetupFlowButton(
            text = primaryText,
            onClick = actions.onContinue,
            enabled = primaryEnabled,
            large = large,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SetupFlowButton(
                text = "Back",
                onClick = actions.onBack,
                enabled = !state.onboardingLoading,
                large = large,
                secondary = true,
                modifier = Modifier.weight(0.82f),
            )
            SetupFlowButton(
                text = primaryText,
                onClick = actions.onContinue,
                enabled = primaryEnabled,
                large = large,
                modifier = Modifier.weight(1.18f),
            )
        }
    }
}

@Composable
private fun SetupFlowButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    large: Boolean,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
) {
    Surface(
        modifier = modifier
            .height(if (large) 64.dp else 56.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = OrmaShapes.Capsule,
        color = when {
            secondary -> OrmaColors.ScreenBackground
            enabled -> OrmaColors.Accent
            else -> OrmaColors.Accent.copy(alpha = 0.35f)
        },
        contentColor = if (secondary) OrmaColors.TextPrimary else OrmaColors.OnAccent,
        border = if (secondary) BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)) else null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = if (large) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
                color = if (secondary) {
                    if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled
                } else {
                    OrmaColors.OnAccent
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NotificationPermissionStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showPermissionAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showPermissionAnimation = true
    }

    if (wide) {
        NotificationPermissionWideStage(
            state = state,
            actions = actions,
            showPermissionAnimation = showPermissionAnimation,
        )
    } else {
        NotificationPermissionMobileStage(
            state = state,
            actions = actions,
            showPermissionAnimation = showPermissionAnimation,
        )
    }
}

@Composable
private fun NotificationPermissionMobileStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    showPermissionAnimation: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaSectionHeader(text = "Notifications")
            Text(
                text = "Stay connected",
                style = MaterialTheme.typography.headlineMedium,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Allow ORMA to send useful workspace updates.",
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary.copy(alpha = 0.45f),
                textAlign = TextAlign.Center,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = OrmaShapes.StandardCell,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.TextPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                OrmaListRow(label = "Orders", value = "New and pending")
                OrmaIndentedDivider()
                OrmaListRow(label = "Invoices", value = "Paid or overdue")
                OrmaIndentedDivider()
                OrmaListRow(label = "Workspace", value = "Team activity")
            }
        }
        Surface(
            onClick = { actions.onNotificationDecision(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = OrmaShapes.CheckoutButton,
            color = OrmaColors.Accent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (state.onboardingLoading) "Enabling..." else "Allow notifications",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.ScreenBackground,
                )
            }
        }
        Text(
            text = "Ask me later",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = OrmaColors.TextPrimary.copy(alpha = 0.45f),
            modifier = Modifier
                .clickable { actions.onNotificationDecision(false) }
                .padding(4.dp),
        )
    }
}

@Composable
private fun NotificationPermissionWideStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    showPermissionAnimation: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 520.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            OrmaSectionHeader(text = "Final step")
            Text(
                text = "Keep workspace updates visible",
                style = MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "ORMA can notify owners and team members when orders, invoices, tax reminders, or workspace activity need attention.",
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
            )
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = OrmaShapes.PremiumCard,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.TextPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                NotificationSignalItem(
                    label = "Invoices",
                    body = "Paid, overdue, and approval updates.",
                    marker = "01",
                )
                NotificationSignalItem(
                    label = "Orders",
                    body = "New orders and work that needs review.",
                    marker = "02",
                )
                NotificationSignalItem(
                    label = "Compliance",
                    body = "Tax registration and workspace reminders.",
                    marker = "03",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NotificationWideActionButton(
                        text = if (state.onboardingLoading) "Enabling..." else "Enable notifications",
                        primary = true,
                        onClick = { actions.onNotificationDecision(true) },
                        modifier = Modifier.weight(1f),
                    )
                    NotificationWideActionButton(
                        text = "Ask me later",
                        primary = false,
                        onClick = { actions.onNotificationDecision(false) },
                        modifier = Modifier.weight(0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSignalItem(
    label: String,
    body: String,
    marker: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = OrmaColors.Accent.copy(alpha = 0.08f),
            contentColor = OrmaColors.Accent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = marker,
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextPrimary,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NotificationWideActionButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = OrmaShapes.Capsule,
        color = if (primary) OrmaColors.Accent else OrmaColors.ScreenBackground,
        contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        border = if (primary) null else BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NotificationPreviewPanel(
    previewAlpha: Float,
    previewScale: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 500.dp),
        shape = RoundedCornerShape(28.dp),
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Notification center",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.ScreenBackground,
                        )
                        Text(
                            text = "Workspace activity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.ScreenBackground.copy(alpha = 0.56f),
                        )
                    }
                    Surface(
                        shape = OrmaShapes.Capsule,
                        color = OrmaColors.ScreenBackground.copy(alpha = 0.12f),
                        contentColor = OrmaColors.OnAccent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            text = "LIVE",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.ScreenBackground,
                        )
                    }
                }

                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = previewAlpha
                        scaleX = previewScale
                        scaleY = previewScale
                    },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NotificationPreviewCard(
                        label = "Invoice paid",
                        body = "ORMA-0001 was settled by customer account.",
                        time = "Now",
                        active = true,
                    )
                    NotificationPreviewCard(
                        label = "Order review",
                        body = "Two new orders are waiting for confirmation.",
                        time = "5 min",
                        active = false,
                    )
                    NotificationPreviewCard(
                        label = "Tax reminder",
                        body = "GST/VAT settings are ready for final review.",
                        time = "Today",
                        active = false,
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = previewAlpha
                            scaleX = previewScale
                            scaleY = previewScale
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = OrmaColors.ScreenBackground,
                    contentColor = OrmaColors.TextPrimary,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Allow ORMA to send notifications?",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.TextPrimary,
                        )
                        Text(
                            text = "You can change this later from workspace settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PermissionPromptButton(
                                text = "Not now",
                                primary = false,
                                modifier = Modifier.weight(1f),
                            )
                            PermissionPromptButton(
                                text = "Allow",
                                primary = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPreviewCard(
    label: String,
    body: String,
    time: String,
    active: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = OrmaColors.ScreenBackground.copy(alpha = if (active) 0.96f else 0.13f),
        contentColor = if (active) OrmaColors.TextPrimary else OrmaColors.ScreenBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (active) OrmaColors.Accent else OrmaColors.ScreenBackground.copy(alpha = 0.56f)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (active) OrmaColors.TextPrimary else OrmaColors.ScreenBackground,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) OrmaColors.TextSecondary else OrmaColors.ScreenBackground.copy(alpha = 0.66f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) OrmaColors.TextSecondary else OrmaColors.ScreenBackground.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun PermissionPromptButton(
    text: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = OrmaShapes.Capsule,
        color = if (primary) OrmaColors.Accent else OrmaColors.CellBackground,
        contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun NotificationPermissionPhoneMock(
    showPermissionAnimation: Boolean,
    modifier: Modifier = Modifier,
) {
    val alertOpacity by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 360f),
        label = "orma_permission_alert_opacity",
    )
    val alertScale by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 1f else 1.1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "orma_permission_alert_scale",
    )
    val tapScale by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 260f),
        label = "orma_permission_tap_scale",
    )
    val tapOpacity by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 260f),
        label = "orma_permission_tap_opacity",
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        val phoneWidth = maxWidth.coerceAtMost(402.dp) * 0.82f
        val frameShape = RoundedCornerShape((phoneWidth.value * 0.15f).dp)

        Box(
            modifier = Modifier
                .width(phoneWidth)
                .aspectRatio(402f / 874f)
                .clip(frameShape)
                .background(Color.Black.copy(alpha = 0.03f))
                .border(10.dp, Color.Gray.copy(alpha = 0.65f), frameShape)
                .border(4.dp, Color.Black.copy(alpha = 0.70f), frameShape)
                .padding(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(frameShape)
                    .background(Color.Black.copy(alpha = 0.05f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "9:41",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.Black.copy(alpha = 0.58f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Wi-Fi 84",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.Black.copy(alpha = 0.50f),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .width(phoneWidth * 0.30f)
                        .height(phoneWidth * 0.092f)
                        .clip(OrmaShapes.Capsule)
                        .background(Color.Black),
                )

                NotificationPermissionSystemAlert(
                    tapScale = tapScale,
                    tapOpacity = tapOpacity,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp)
                        .graphicsLayer {
                            alpha = alertOpacity
                            scaleX = alertScale
                            scaleY = alertScale
                        },
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionSystemAlert(
    tapScale: Float,
    tapOpacity: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f),
            ),
        shape = RoundedCornerShape(30.dp),
        color = OrmaColors.ScreenBackground.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.12f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.12f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(15.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.12f)),
            )

            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PermissionAlertButtonPlaceholder(modifier = Modifier.weight(1f))
                PermissionAlertButtonPlaceholder(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = tapScale
                            scaleY = tapScale
                        },
                    showTap = true,
                    tapOpacity = tapOpacity,
                )
            }
        }
    }
}

@Composable
private fun PermissionAlertButtonPlaceholder(
    modifier: Modifier = Modifier,
    showTap: Boolean = false,
    tapOpacity: Float = 0f,
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(OrmaShapes.Capsule)
            .background(Color.Black.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        if (showTap) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .alpha(tapOpacity)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.8f)),
            )
        }
    }
}

@Composable
private fun CompleteStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    val owner = state.accessPath == AccessPath.BusinessOwner
    OrmaScreenColumn(
        eyebrow = "Ready",
        title = if (owner) "Workspace prepared" else "Team login ready",
        body = if (owner) {
            "The first product milestone can now connect to backend authentication and persistence."
        } else {
            "Team member login is ready for role and permission sync."
        },
    ) {
        CompletionCard(state = state)
        OrmaActionRow(
            secondaryText = "Back",
            onSecondary = actions.onBack,
            primaryText = "Restart",
            onPrimary = actions.onRestart,
            primaryEnabled = true,
        )
    }
}

@Composable
private fun DashboardStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    val canManageRestrictedSections = state.accessPath == AccessPath.BusinessOwner
    val canInviteMembers = canManageRestrictedSections
    val canUseMarketing = canManageRestrictedSections
    var selectedSectionName by rememberSaveable { mutableStateOf(DashboardSection.Dashboard.name) }
    val requestedSection = DashboardSection.entries.firstOrNull { it.name == selectedSectionName }
        ?: DashboardSection.Dashboard
    val selectedSection = if (!canAccessDashboardSection(requestedSection, canInviteMembers, canUseMarketing)) {
        DashboardSection.Dashboard
    } else {
        requestedSection
    }
    LaunchedEffect(canInviteMembers, canUseMarketing, requestedSection) {
        if (!canAccessDashboardSection(requestedSection, canInviteMembers, canUseMarketing)) {
            selectedSectionName = DashboardSection.Dashboard.name
        }
    }
    val selectDashboardSection: (DashboardSection) -> Unit = { section ->
        if (canAccessDashboardSection(section, canInviteMembers, canUseMarketing)) {
            selectedSectionName = section.name
        }
    }

    val workspaceName = state.workspaceName
        .ifBlank { state.draft.businessName }
        .ifBlank { "ORMA workspace" }
    val roleLabel = when (state.accessPath) {
        AccessPath.BusinessOwner -> "Business owner"
        AccessPath.TeamMember -> "Team member"
    }

    if (wide) {
        DashboardWideStage(
            workspaceName = workspaceName,
            roleLabel = roleLabel,
            state = state,
            selectedSection = selectedSection,
            canInviteMembers = canInviteMembers,
            canUseMarketing = canUseMarketing,
            actions = actions,
            onSectionSelected = selectDashboardSection,
            onLogout = actions.onRestart,
        )
    } else {
        DashboardMobileStage(
            workspaceName = workspaceName,
            roleLabel = roleLabel,
            state = state,
            selectedSection = selectedSection,
            canInviteMembers = canInviteMembers,
            canUseMarketing = canUseMarketing,
            actions = actions,
            onSectionSelected = selectDashboardSection,
        )
    }
}

@Composable
private fun DashboardMobileStage(
    workspaceName: String,
    roleLabel: String,
    state: OnboardingUiState,
    selectedSection: DashboardSection,
    canInviteMembers: Boolean,
    canUseMarketing: Boolean,
    actions: OnboardingActions,
    onSectionSelected: (DashboardSection) -> Unit,
) {
    val scrollState = rememberScrollState()
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }
    var lastRootSectionName by rememberSaveable { mutableStateOf(DashboardSection.Dashboard.name) }
    var mobileSelectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val mobileRootSections = remember { DashboardMobileBottomNavItems.map { it.section }.toSet() }
    val selectedRootSection = selectedSection.takeIf { it in mobileRootSections }
    LaunchedEffect(selectedRootSection) {
        selectedRootSection?.let { lastRootSectionName = it.name }
    }
    LaunchedEffect(selectedSection) {
        if (selectedSection != DashboardSection.OrdersBookings) {
            mobileSelectedOrderId = null
        }
    }
    val selectedMobileOrder = if (selectedSection == DashboardSection.OrdersBookings) {
        state.dashboard.orders.firstOrNull { it.id == mobileSelectedOrderId }
    } else {
        null
    }
    val fallbackRootSection = DashboardSection.entries.firstOrNull { it.name == lastRootSectionName }
        ?: DashboardSection.Dashboard
    val canNavigateBack = selectedMobileOrder != null || selectedSection !in mobileRootSections
    val navigateBack: () -> Unit = {
        if (selectedMobileOrder != null) {
            mobileSelectedOrderId = null
        } else {
            onSectionSelected(fallbackRootSection)
        }
    }
    val topBarTitle = selectedMobileOrder
        ?.orderNumber
        ?.takeIf { it.isNotBlank() }
        ?: selectedMobileOrder?.id?.take(8)?.uppercase()
        ?: selectedSection.title(state)
    val topBarSubtitle = selectedMobileOrder?.let { order ->
        "${order.customerName ?: "Walk-in customer"} / ${order.status.dashboardStatusLabel()}"
    } ?: "$workspaceName / $roleLabel"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardMobileTopBar(
                workspaceName = workspaceName,
                roleLabel = roleLabel,
                selectedSection = selectedSection,
                state = state,
                titleOverride = topBarTitle,
                subtitleOverride = topBarSubtitle,
                canNavigateBack = canNavigateBack,
                onBack = navigateBack,
                onOpenAccountSheet = { showAccountSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
            )
            DashboardSwipeRefreshContainer(
                loading = state.dashboard.loading,
                canRefresh = scrollState.value == 0,
                onRefresh = actions.onDashboardRefresh,
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    DashboardFeedback(state = state, actions = actions)
                    DashboardSearchAndFilterBar(
                        state = state,
                        selectedSection = selectedSection,
                        actions = actions,
                        wide = false,
                    )
                    DashboardSectionContent(
                        state = state,
                        roleLabel = roleLabel,
                        selectedSection = selectedSection,
                        canInviteMembers = canInviteMembers,
                        actions = actions,
                        onOpenTeam = if (canInviteMembers) {
                            { onSectionSelected(DashboardSection.Team) }
                        } else {
                            null
                        },
                        onOpenOrders = { onSectionSelected(DashboardSection.OrdersBookings) },
                        onOpenProducts = { onSectionSelected(DashboardSection.Products) },
                        wide = false,
                        mobileSelectedOrderId = mobileSelectedOrderId,
                        onMobileSelectedOrderChange = { mobileSelectedOrderId = it },
                    )
                }
            }
        }

        if (selectedMobileOrder == null && selectedSection in mobileRootSections) {
            DashboardBottomBar(
                selectedSection = selectedSection,
                state = state,
                onSectionSelected = onSectionSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            )
        }

        DashboardStatusToast(
            message = state.dashboard.statusMessage,
            onDismiss = actions.onClearDashboardStatusMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .padding(bottom = if (selectedMobileOrder == null && selectedSection in mobileRootSections) 106.dp else 22.dp),
        )

        if (showAccountSheet) {
            DashboardMobileAccountSheet(
                state = state,
                selectedSection = selectedSection,
                canInviteMembers = canInviteMembers,
                canUseMarketing = canUseMarketing,
                onDismiss = { showAccountSheet = false },
                onSectionSelected = { section ->
                    showAccountSheet = false
                    onSectionSelected(section)
                },
            )
        }
    }
}

@Composable
private fun DashboardSwipeRefreshContainer(
    loading: Boolean,
    canRefresh: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val refreshThresholdPx = with(density) { 74.dp.toPx() }
    var pullDistance by remember { mutableStateOf(0f) }
    val indicatorVisible = loading || pullDistance > 6f
    val indicatorText = when {
        loading -> "Refreshing workspace"
        pullDistance >= refreshThresholdPx -> "Release to refresh"
        else -> "Pull to refresh"
    }
    val indicatorOffset = with(density) {
        (pullDistance.coerceIn(0f, refreshThresholdPx) * 0.16f).toDp()
    }

    LaunchedEffect(canRefresh, loading) {
        if (!canRefresh || loading) {
            pullDistance = 0f
        }
    }

    val refreshConnection = remember(canRefresh, loading, refreshThresholdPx, onRefresh) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || loading) return Offset.Zero
                val delta = available.y
                if (delta > 0f && canRefresh) {
                    pullDistance = (pullDistance + (delta * 0.55f)).coerceAtMost(refreshThresholdPx * 1.45f)
                } else if (delta < 0f && pullDistance > 0f) {
                    pullDistance = (pullDistance + delta).coerceAtLeast(0f)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullDistance >= refreshThresholdPx && canRefresh && !loading) {
                    onRefresh()
                }
                pullDistance = 0f
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(refreshConnection),
    ) {
        content()
        AnimatedVisibility(
            visible = indicatorVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .offset(y = indicatorOffset),
        ) {
            Surface(
                shape = OrmaShapes.Capsule,
                color = OrmaColors.CellBackground,
                contentColor = OrmaColors.Accent,
                border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = indicatorText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun DashboardWideStage(
    workspaceName: String,
    roleLabel: String,
    state: OnboardingUiState,
    selectedSection: DashboardSection,
    canInviteMembers: Boolean,
    canUseMarketing: Boolean,
    actions: OnboardingActions,
    onSectionSelected: (DashboardSection) -> Unit,
    onLogout: () -> Unit,
) {
    var sidebarCollapsed by rememberSaveable { mutableStateOf(false) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (sidebarCollapsed) 86.dp else 270.dp,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "orma-dashboard-sidebar-width",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrmaColors.WorkspaceChrome)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DashboardSidebar(
                workspaceName = workspaceName,
                roleLabel = roleLabel,
                selectedSection = selectedSection,
                state = state,
                canInviteMembers = canInviteMembers,
                canUseMarketing = canUseMarketing,
                onSectionSelected = onSectionSelected,
                onLogout = onLogout,
                collapsed = sidebarCollapsed,
                onToggleCollapsed = { sidebarCollapsed = !sidebarCollapsed },
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight(),
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = OrmaShapes.Sheet,
                color = OrmaColors.WorkspacePanel,
                contentColor = OrmaColors.TextPrimary,
                border = null,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardWideHeader(
                        workspaceName = workspaceName,
                        selectedSection = selectedSection,
                        state = state,
                        onRefresh = actions.onDashboardRefresh,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 26.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        DashboardFeedback(state = state, actions = actions)
                        DashboardSearchAndFilterBar(
                            state = state,
                            selectedSection = selectedSection,
                            actions = actions,
                            wide = true,
                        )
                        DashboardSectionContent(
                            state = state,
                            roleLabel = roleLabel,
                            selectedSection = selectedSection,
                            canInviteMembers = canInviteMembers,
                            actions = actions,
                            onOpenTeam = null,
                            onOpenOrders = { onSectionSelected(DashboardSection.OrdersBookings) },
                            onOpenProducts = { onSectionSelected(DashboardSection.Products) },
                            wide = true,
                        )
                    }
                }
            }
        }

        DashboardStatusToast(
            message = state.dashboard.statusMessage,
            onDismiss = actions.onClearDashboardStatusMessage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 96.dp, end = 34.dp),
        )
    }
}

@Composable
private fun DashboardMobileTopBar(
    workspaceName: String,
    roleLabel: String,
    selectedSection: DashboardSection,
    state: OnboardingUiState,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onOpenAccountSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canNavigateBack) {
                DashboardMobileBackButton(onClick = onBack)
            } else {
                DashboardWorkspaceMark(workspaceName = workspaceName)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = titleOverride ?: selectedSection.title(state),
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitleOverride ?: "$workspaceName / $roleLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            DashboardMobileAccountButton(
                onClick = onOpenAccountSheet,
            )
        }
    }
}

@Composable
private fun DashboardMobileBackButton(
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            OrmaFlatIcon(
                kind = OrmaFlatIconKind.Back,
                modifier = Modifier.size(19.dp),
                color = OrmaColors.IconPrimary,
            )
        }
    }
}

@Composable
private fun DashboardMobileAccountButton(
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            DashboardNavIcon(
                kind = DashboardNavIconKind.Account,
                color = OrmaColors.IconPrimary,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardMobileAccountSheet(
    state: OnboardingUiState,
    selectedSection: DashboardSection,
    canInviteMembers: Boolean,
    canUseMarketing: Boolean,
    onDismiss: () -> Unit,
    onSectionSelected: (DashboardSection) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    val requestSectionSelected: (DashboardSection) -> Unit = { section ->
        closeSheet { onSectionSelected(section) }
    }
    val items = remember(canInviteMembers, canUseMarketing) {
        buildList {
            add(DashboardMobileMoreItem(DashboardSection.Account, "Account", "Profile, payments, printers, sign out"))
            add(DashboardMobileMoreItem(DashboardSection.Invoices, "Invoices", "Create and preview tax invoices"))
            if (canUseMarketing) {
                add(DashboardMobileMoreItem(DashboardSection.Marketing, "Online store", "Catalog link, QR, WhatsApp"))
            }
            add(
                DashboardMobileMoreItem(
                    DashboardSection.Team,
                    if (canInviteMembers) "Team" else "My access",
                    if (canInviteMembers) "Staff access" else "Role and workspace",
                ),
            )
        }
    }
    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Account and tools",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Settings and secondary workspace screens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            items.forEach { item ->
                DashboardMobileAccountSheetRow(
                    item = item,
                    state = state,
                    selected = selectedSection == item.section,
                    onClick = { requestSectionSelected(item.section) },
                )
            }
        }
    }
}

private data class DashboardMobileMoreItem(
    val section: DashboardSection,
    val title: String,
    val body: String,
)

@Composable
private fun DashboardMobileAccountSheetRow(
    item: DashboardMobileMoreItem,
    state: OnboardingUiState,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = if (selected) OrmaColors.CellBackground else OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = OrmaShapes.Capsule,
                color = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(
                        kind = item.section.mobileMoreIcon(),
                        color = if (selected) OrmaColors.OnAccent else OrmaColors.IconPrimary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.section.title(state).ifBlank { item.title },
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "Open",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
        }
    }
}

private fun DashboardSection.mobileMoreIcon(): DashboardNavIconKind = when (this) {
    DashboardSection.Invoices -> DashboardNavIconKind.Invoice
    DashboardSection.Marketing -> DashboardNavIconKind.Marketing
    DashboardSection.Team -> DashboardNavIconKind.Invite
    DashboardSection.Account -> DashboardNavIconKind.Account
    else -> DashboardNavIconKind.Account
}

@Composable
private fun DashboardWorkspaceMark(
    workspaceName: String,
) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = dashboardWorkspaceInitials(workspaceName),
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.ScreenBackground,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

private fun dashboardWorkspaceInitials(workspaceName: String): String =
    workspaceName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "O" }

@Composable
private fun DashboardWorkspaceAvatar(
    workspaceName: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = OrmaShapes.SmallCard,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.OnAccent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = dashboardWorkspaceInitials(workspaceName),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.ScreenBackground,
                )
            }
        }
        Text(
            text = "ORMA",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
    }
}

@Composable
private fun DashboardFeedback(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    val title = state.dashboard.errorTitle
    val error = state.dashboard.errorMessage
    if (!error.isNullOrBlank()) {
        DashboardMessageCard(
            title = title ?: "Could not update workspace",
            body = error,
            tone = OrmaStatusTone.Danger,
            onDismiss = actions.onClearDashboardMessage,
        )
    }
}

@Composable
private fun DashboardStatusToast(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = !message.isNullOrBlank()

    LaunchedEffect(message) {
        if (visible) {
            delay(3600)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 260.dp, max = 420.dp),
            shape = OrmaShapes.StandardCell,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.OnAccent,
            border = null,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.OnAccent,
                        maxLines = 1,
                    )
                    Text(
                        text = message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.OnAccent.copy(alpha = 0.82f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    onClick = onDismiss,
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.OnAccent.copy(alpha = 0.12f),
                    contentColor = OrmaColors.OnAccent,
                    border = null,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = "Dismiss",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.OnAccent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSearchAndFilterBar(
    state: OnboardingUiState,
    selectedSection: DashboardSection,
    actions: OnboardingActions,
    wide: Boolean,
) {
    if (selectedSection in setOf(DashboardSection.Account, DashboardSection.Team)) return
    if (!wide) return
    if (wide && selectedSection in setOf(
            DashboardSection.OrdersBookings,
            DashboardSection.Invoices,
            DashboardSection.Customers,
            DashboardSection.Products,
        )
    ) return
    val filters = state.dashboard.filters
    val placeholder = when (selectedSection) {
        DashboardSection.Dashboard -> "Search work, customers, catalog"
        DashboardSection.OrdersBookings -> when {
            state.selectedDashboardOrderTypeFilter() == "all" -> "Order, service, appointment, customer"
            state.activeDashboardOrderType() == "service" -> "Service number, customer, note"
            state.activeDashboardOrderType() == "appointment" -> "Appointment, customer, date, note"
            else -> "Sale number, customer, note"
        }
        DashboardSection.Invoices -> "Invoice, order, customer"
        DashboardSection.Customers -> "Customer, phone, email, city"
        DashboardSection.Products -> when {
            state.selectedDashboardItemTypeFilter() == "all" -> "Product, service, appointment, category"
            state.activeDashboardItemType() == "service" -> "Service, category, price"
            state.activeDashboardItemType() == "appointment" -> "Appointment service, duration, category"
            else -> "Product, SKU, barcode, supplier"
        }
        DashboardSection.Marketing -> "Product, category, supplier"
        DashboardSection.Team -> "Search"
        DashboardSection.Account -> "Search"
    }
    if (wide) {
        DashboardWideSearchAndFilterBar(
            state = state,
            selectedSection = selectedSection,
            actions = actions,
            placeholder = placeholder,
            filters = filters,
        )
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OrmaTextField(
            value = filters.query,
            onValueChange = actions.onDashboardSearchChange,
            label = "Search",
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
        )
        when (selectedSection) {
            DashboardSection.OrdersBookings -> {
                val orderTypeOptions = state.dashboardOrderTypeFilterOptions()
                if (orderTypeOptions.size > 1) {
                    DashboardCompactSegmentedPicker(
                        options = orderTypeOptions,
                        selected = state.selectedDashboardOrderTypeFilter(),
                        label = { if (it == "all") "All flows" else it.orderTypeLabel() },
                        onSelected = actions.onOrderTypeFilterChange,
                    )
                }
                DashboardCompactSegmentedPicker(
                    options = DashboardOrderStatusFilters,
                    selected = filters.orderStatus,
                    label = { it.dashboardStatusFilterLabel() },
                    onSelected = actions.onOrderStatusFilterChange,
                )
            }
            DashboardSection.Products -> {
                val itemTypeOptions = state.dashboardItemTypeFilterOptions()
                if (itemTypeOptions.size > 1) {
                    DashboardCompactSegmentedPicker(
                        options = itemTypeOptions,
                        selected = state.selectedDashboardItemTypeFilter(),
                        label = { if (it == "all") "All items" else it.sellableItemTypeLabel() },
                        onSelected = actions.onProductItemTypeFilterChange,
                    )
                }
                OrmaSwitchRow(
                    title = "Low stock only",
                    body = "Show items that need restock or stock verification.",
                    checked = filters.lowStockOnly,
                    onCheckedChange = actions.onProductLowStockFilterChange,
                )
            }
            DashboardSection.Invoices -> {
                DashboardCompactSegmentedPicker(
                    options = DashboardInvoiceStatusFilters,
                    selected = state.selectedDashboardInvoiceStatusFilter(),
                    label = { it.invoiceStatusFilterLabel() },
                    onSelected = actions.onOrderStatusFilterChange,
                )
            }
            DashboardSection.Dashboard,
            DashboardSection.Customers,
            DashboardSection.Marketing,
            DashboardSection.Team,
            DashboardSection.Account -> Unit
        }
        if (selectedSection.shouldShowDashboardDateFilter(filters)) {
            DashboardDateRangeFilter(
                filters = filters,
                actions = actions,
                wide = false,
            )
        }
        if (selectedSection == DashboardSection.OrdersBookings) {
            if (state.hasActiveDashboardFilter()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DashboardSalesInlineResetButton(
                        onClick = { clearDashboardWorkspaceFilters(state, actions) },
                    )
                }
            }
        } else {
            OrmaActionRow(
                primaryText = if (state.dashboard.loading) "Searching..." else "Apply",
                onPrimary = actions.onDashboardRefresh,
                primaryEnabled = !state.dashboard.loading,
                secondaryText = if (state.hasActiveDashboardFilter()) "Clear" else null,
                onSecondary = if (state.hasActiveDashboardFilter()) {
                    {
                        actions.onDashboardSearchChange("")
                        actions.onOrderStatusFilterChange("all")
                        actions.onOrderTypeFilterChange(state.defaultDashboardOrderTypeFilter())
                        actions.onProductItemTypeFilterChange(state.defaultDashboardItemTypeFilter())
                        actions.onDashboardDateFilterChange("", "")
                        actions.onProductLowStockFilterChange(false)
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun DashboardWideSearchAndFilterBar(
    state: OnboardingUiState,
    selectedSection: DashboardSection,
    actions: OnboardingActions,
    placeholder: String,
    filters: OrmaDashboardFilters,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardCompactSearchField(
                value = filters.query,
                onValueChange = actions.onDashboardSearchChange,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
            )
            if (state.hasActiveDashboardFilter()) {
                DashboardToolbarButton(
                    text = "Clear",
                    onClick = {
                        actions.onDashboardSearchChange("")
                        actions.onOrderStatusFilterChange("all")
                        actions.onOrderTypeFilterChange(state.defaultDashboardOrderTypeFilter())
                        actions.onProductItemTypeFilterChange(state.defaultDashboardItemTypeFilter())
                        actions.onDashboardDateFilterChange("", "")
                        actions.onProductLowStockFilterChange(false)
                    },
                )
            }
            if (selectedSection != DashboardSection.Dashboard) {
                DashboardToolbarButton(
                    text = if (state.dashboard.loading) "Searching" else "Apply",
                    onClick = actions.onDashboardRefresh,
                    modifier = Modifier.widthIn(min = 104.dp, max = 132.dp),
                    enabled = !state.dashboard.loading,
                    primary = true,
                )
            }
        }
        when (selectedSection) {
            DashboardSection.OrdersBookings -> {
                val orderTypeOptions = state.dashboardOrderTypeFilterOptions()
                if (orderTypeOptions.size > 1) {
                    DashboardCompactSegmentedPicker(
                        options = orderTypeOptions,
                        selected = state.selectedDashboardOrderTypeFilter(),
                        label = { if (it == "all") "All flows" else it.orderTypeLabel() },
                        onSelected = actions.onOrderTypeFilterChange,
                    )
                }
                DashboardCompactSegmentedPicker(
                    options = DashboardOrderStatusFilters,
                    selected = filters.orderStatus,
                    label = { it.dashboardStatusFilterLabel() },
                    onSelected = actions.onOrderStatusFilterChange,
                )
            }
            DashboardSection.Products -> {
                val itemTypeOptions = state.dashboardItemTypeFilterOptions()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (itemTypeOptions.size > 1) {
                        OrmaSegmentedRow(
                            options = itemTypeOptions,
                            selected = state.selectedDashboardItemTypeFilter(),
                            label = { if (it == "all") "All items" else it.sellableItemTypeLabel() },
                            onSelected = actions.onProductItemTypeFilterChange,
                            modifier = Modifier.widthIn(max = 320.dp),
                        )
                    }
                    OrmaSegmentedRow(
                        options = listOf(false, true),
                        selected = filters.lowStockOnly,
                        label = { if (it) "Low stock" else "All stock" },
                        onSelected = actions.onProductLowStockFilterChange,
                        modifier = Modifier.widthIn(max = 260.dp),
                    )
                }
            }
            DashboardSection.Dashboard,
            DashboardSection.Invoices,
            DashboardSection.Customers,
            DashboardSection.Marketing,
            DashboardSection.Team,
            DashboardSection.Account -> Unit
        }
        if (selectedSection.shouldShowDashboardDateFilter(filters)) {
            DashboardDateRangeFilter(
                filters = filters,
                actions = actions,
                wide = true,
            )
        }
    }
}

@Composable
private fun DashboardDateRangeFilter(
    filters: OrmaDashboardFilters,
    actions: OnboardingActions,
    wide: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DashboardDatePresetFilterChips(
            filters = filters,
            actions = actions,
        )
        if (wide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                OrmaCalendarDateField(
                    value = filters.dateFrom,
                    onValueChange = { actions.onDashboardDateFilterChange(it, filters.dateTo) },
                    label = "From",
                    placeholder = "Start date",
                    modifier = Modifier.weight(1f),
                )
                OrmaCalendarDateField(
                    value = filters.dateTo,
                    onValueChange = { actions.onDashboardDateFilterChange(filters.dateFrom, it) },
                    label = "To",
                    placeholder = "End date",
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.weight(2f))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OrmaCalendarDateField(
                    value = filters.dateFrom,
                    onValueChange = { actions.onDashboardDateFilterChange(it, filters.dateTo) },
                    label = "From",
                    placeholder = "Start date",
                    modifier = Modifier.fillMaxWidth(),
                )
                OrmaCalendarDateField(
                    value = filters.dateTo,
                    onValueChange = { actions.onDashboardDateFilterChange(filters.dateFrom, it) },
                    label = "To",
                    placeholder = "End date",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DashboardDatePresetFilterChips(
    filters: OrmaDashboardFilters,
    actions: OnboardingActions,
    includeUpcoming: Boolean = false,
    onClearSelection: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val options = dashboardDatePresetOptions(includeUpcoming = includeUpcoming)
    val activeKey = dashboardActiveDatePresetKey(filters, options)
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            DashboardDatePresetChip(
                label = option.label,
                selected = activeKey == option.key,
                onClick = {
                    onClearSelection()
                    actions.onDashboardDatePresetChange(option.key, option.dateFrom, option.dateTo)
                },
            )
        }
        if (activeKey == "custom") {
            DashboardDatePresetChip(
                label = "Custom",
                selected = true,
                onClick = {},
            )
        }
    }
}

@Composable
private fun DashboardDatePresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = OrmaShapes.Capsule,
        color = if (selected) OrmaColors.Accent else OrmaColors.ScreenBackground,
        contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        border = BorderStroke(
            0.6.dp,
            if (selected) OrmaColors.Accent.copy(alpha = 0.44f) else OrmaColors.Hairline.copy(alpha = 0.16f),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardToolbarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    val iconKind = dashboardFlatIconForAction(text)
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(46.dp)
            .widthIn(min = if (primary) 112.dp else 84.dp, max = 154.dp),
        enabled = enabled,
        shape = OrmaShapes.Capsule,
        color = when {
            !enabled -> OrmaColors.Accent.copy(alpha = 0.12f)
            primary -> OrmaColors.Accent
            else -> Color.Transparent
        },
        contentColor = when {
            primary -> OrmaColors.ScreenBackground
            else -> OrmaColors.Accent
        },
        border = if (primary) {
            null
        } else {
            BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f))
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKind != null) {
                OrmaFlatIcon(
                    kind = iconKind,
                    modifier = Modifier.size(16.dp),
                    color = when {
                        !enabled -> OrmaColors.TextDisabled
                        primary -> OrmaColors.ScreenBackground
                        else -> OrmaColors.IconPrimary
                    },
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    !enabled -> OrmaColors.TextDisabled
                    primary -> OrmaColors.ScreenBackground
                    else -> OrmaColors.TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardCompactSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = OrmaShapes.Field,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = shape,
        color = OrmaColors.CardBackground.copy(alpha = 0.86f),
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(80)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = placeholder },
            singleLine = true,
            cursorBrush = SolidColor(OrmaColors.Accent),
            textStyle = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(color = OrmaColors.TextPrimary),
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrmaFlatIcon(
                        kind = OrmaFlatIconKind.Search,
                        modifier = Modifier.size(16.dp),
                        color = OrmaColors.TextSecondary,
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = OrmaColors.TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                    if (value.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onValueChange("") },
                            contentAlignment = Alignment.Center,
                        ) {
                            OrmaCloseIcon(
                                modifier = Modifier.size(12.dp),
                                color = OrmaColors.TextSecondary,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun <T> DashboardCompactSegmentedPicker(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                modifier = Modifier.clickable { onSelected(option) },
                shape = OrmaShapes.Capsule,
                color = if (isSelected) OrmaColors.Accent else OrmaColors.ScreenBackground,
                contentColor = if (isSelected) OrmaColors.OnAccent else OrmaColors.Accent,
                border = BorderStroke(
                    0.6.dp,
                    if (isSelected) OrmaColors.Accent.copy(alpha = 0.54f) else OrmaColors.Hairline.copy(alpha = 0.12f),
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = label(option),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun DashboardMessageCard(
    title: String,
    body: String,
    tone: OrmaStatusTone,
    onDismiss: () -> Unit,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(0.8.dp, colors.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.content,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.content.copy(alpha = 0.78f),
                )
            }
            OrmaTextButton(text = "Dismiss", onClick = onDismiss)
        }
    }
}

@Composable
private fun DashboardWideHeader(
    workspaceName: String,
    selectedSection: DashboardSection,
    state: OnboardingUiState,
    onRefresh: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = OrmaColors.WorkspacePanel,
        contentColor = OrmaColors.TextPrimary,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Workspace",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextTertiary,
                    )
                    Text(
                        text = selectedSection.title(state),
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextPrimary,
                    )
                }
                Text(
                    text = selectedSection.title(state),
                    style = MaterialTheme.typography.headlineSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = workspaceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardWideHeaderRefreshButton(
                    loading = state.dashboard.loading,
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

@Composable
private fun DashboardWideHeaderRefreshButton(
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    Surface(
        onClick = onRefresh,
        enabled = !loading,
        shape = OrmaShapes.Capsule,
        color = if (loading) OrmaColors.Accent.copy(alpha = 0.12f) else OrmaColors.ScreenBackground,
        contentColor = if (loading) OrmaColors.TextDisabled else OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaFlatIcon(
                kind = OrmaFlatIconKind.Refresh,
                color = if (loading) OrmaColors.TextDisabled else OrmaColors.IconPrimary,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = if (loading) "Refreshing" else "Refresh",
                style = MaterialTheme.typography.labelMedium,
                color = if (loading) OrmaColors.TextDisabled else OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardSidebar(
    workspaceName: String,
    roleLabel: String,
    selectedSection: DashboardSection,
    state: OnboardingUiState,
    canInviteMembers: Boolean,
    canUseMarketing: Boolean,
    onSectionSelected: (DashboardSection) -> Unit,
    onLogout: () -> Unit,
    collapsed: Boolean,
    onToggleCollapsed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.Sheet,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(if (collapsed) 12.dp else 18.dp),
            horizontalAlignment = if (collapsed) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(if (collapsed) 16.dp else 18.dp),
        ) {
            DashboardSidebarBrand(
                collapsed = collapsed,
                onToggleCollapsed = onToggleCollapsed,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (collapsed) Alignment.CenterHorizontally else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(if (collapsed) 10.dp else 14.dp),
            ) {
                DashboardSidebarSection(
                    title = "OPERATIONS",
                    items = DashboardOperationalSidebarNavItems.filter {
                        canAccessDashboardSection(it.section, canInviteMembers, canUseMarketing)
                    },
                    selectedSection = selectedSection,
                    state = state,
                    onSectionSelected = onSectionSelected,
                    collapsed = collapsed,
                )
                if (canUseMarketing) {
                    DashboardSidebarSection(
                        title = "MARKETING",
                        items = DashboardMarketingSidebarNavItems,
                        selectedSection = selectedSection,
                        state = state,
                        onSectionSelected = onSectionSelected,
                        collapsed = collapsed,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            DashboardSidebarFooter(
                selected = selectedSection == DashboardSection.Account,
                onClick = { onSectionSelected(DashboardSection.Account) },
                collapsed = collapsed,
            )
        }
    }
}

@Composable
private fun DashboardSidebarSection(
    title: String,
    items: List<DashboardNavItem>,
    selectedSection: DashboardSection,
    state: OnboardingUiState,
    onSectionSelected: (DashboardSection) -> Unit,
    collapsed: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (collapsed) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (collapsed) {
            HorizontalDivider(
                modifier = Modifier.width(34.dp),
                color = OrmaColors.Hairline.copy(alpha = 0.12f),
            )
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
            )
        }
        items.forEach { item ->
            DashboardSidebarItem(
                title = item.sidebarTitle(state),
                body = item.sidebarBody(state),
                selected = selectedSection == item.section,
                icon = item.icon,
                onClick = { onSectionSelected(item.section) },
                collapsed = collapsed,
            )
        }
    }
}

@Composable
private fun DashboardSidebarFooter(
    selected: Boolean,
    onClick: () -> Unit,
    collapsed: Boolean,
) {
    val background = if (selected) OrmaColors.WorkspaceChrome else OrmaColors.WorkspacePanel.copy(alpha = 0.62f)
    Surface(
        onClick = onClick,
        modifier = if (collapsed) Modifier.size(48.dp) else Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = background,
        contentColor = OrmaColors.TextPrimary,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = if (collapsed) {
                Modifier.fillMaxSize().padding(8.dp)
            } else {
                Modifier.padding(14.dp)
            },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = OrmaShapes.Capsule,
                color = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(
                        kind = DashboardNavIconKind.Account,
                        color = if (selected) OrmaColors.OnAccent else OrmaColors.IconPrimary,
                    )
                }
            }
            if (!collapsed) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Logo, printers, sign out",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSidebarBrand(
    collapsed: Boolean,
    onToggleCollapsed: () -> Unit,
) {
    if (collapsed) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardSidebarBrandMark()
            DashboardSidebarCollapseButton(
                collapsed = true,
                onClick = onToggleCollapsed,
            )
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardSidebarBrandMark()
        Text(
            text = "ORMA",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        DashboardSidebarCollapseButton(
            collapsed = false,
            onClick = onToggleCollapsed,
        )
    }
}

@Composable
private fun DashboardSidebarBrandMark() {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            OrmaBrandMark(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                color = OrmaColors.ScreenBackground,
            )
        }
    }
}

@Composable
private fun DashboardSidebarCollapseButton(
    collapsed: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.WorkspacePanel,
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            DashboardSidebarToggleIcon(
                collapsed = collapsed,
                color = OrmaColors.IconPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun DashboardSidebarToggleIcon(
    collapsed: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    OrmaFlatIcon(
        kind = if (collapsed) OrmaFlatIconKind.ChevronRight else OrmaFlatIconKind.ChevronLeft,
        color = color,
        modifier = modifier,
    )
}

@Composable
private fun DashboardSidebarItem(
    title: String,
    body: String,
    selected: Boolean,
    icon: DashboardNavIconKind,
    onClick: () -> Unit,
    collapsed: Boolean,
) {
    val itemBackground = if (selected) OrmaColors.WorkspaceChrome else Color.Transparent
    val iconBackground = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f)
    val iconColor = if (selected) OrmaColors.ScreenBackground else OrmaColors.Accent

    Surface(
        onClick = onClick,
        modifier = if (collapsed) Modifier.size(48.dp) else Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = itemBackground,
        contentColor = OrmaColors.TextPrimary,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = if (collapsed) {
                Modifier.fillMaxSize().padding(8.dp)
            } else {
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = OrmaShapes.Capsule,
                color = iconBackground,
                contentColor = iconColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(kind = icon, color = iconColor)
                }
            }
            if (!collapsed) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardBottomBar(
    selectedSection: DashboardSection,
    state: OnboardingUiState,
    onSectionSelected: (DashboardSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = DashboardMobileBottomNavItems
    val barHeight = 82.dp
    val barShape = RoundedCornerShape(32.dp)
    val itemShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OrmaColors.ScreenBackground.copy(alpha = 0.98f),
                            OrmaColors.ScreenBackground.copy(alpha = 0.96f),
                            OrmaColors.ScreenBackground,
                        ),
                    ),
                )
                .border(0.8.dp, OrmaColors.Divider, barShape),
            shape = barShape,
            color = Color.Transparent,
            contentColor = OrmaColors.Accent,
            border = BorderStroke(0.8.dp, OrmaColors.Divider),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val horizontalPadding = 8.dp
                val verticalPadding = 9.dp
                val itemSpacing = 2.dp
                val bottomSelectedSection = selectedSection.takeIf { section ->
                    tabs.any { it.section == section }
                }
                val selectedIndex = bottomSelectedSection?.let { section ->
                    tabs.indexOfFirst { it.section == section }
                } ?: -1
                val itemWidth = (maxWidth - (horizontalPadding * 2) - (itemSpacing * (tabs.size - 1))) / tabs.size
                val selectedPillOffset by animateDpAsState(
                    targetValue = horizontalPadding + ((itemWidth + itemSpacing) * selectedIndex.coerceAtLeast(0)),
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    label = "orma-dashboard-bottom-bar-pill",
                )

                if (selectedIndex >= 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = selectedPillOffset)
                            .padding(vertical = verticalPadding)
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(itemShape)
                            .background(OrmaColors.CellBackground),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEach { item ->
                        val selected = bottomSelectedSection == item.section
                        val tint = if (selected) OrmaColors.Accent else OrmaColors.TextTertiary
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(itemShape)
                                .dashboardPlainClickable { onSectionSelected(item.section) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Surface(
                                    modifier = Modifier.size(30.dp),
                                    shape = OrmaShapes.Capsule,
                                    color = if (selected) OrmaColors.Accent else Color.Transparent,
                                    contentColor = if (selected) OrmaColors.OnAccent else tint,
                                    tonalElevation = 0.dp,
                                    shadowElevation = 0.dp,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        DashboardNavIcon(
                                            kind = item.icon,
                                            color = if (selected) OrmaColors.OnAccent else tint,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = item.bottomLabel(state),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    color = tint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.dashboardPlainClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick,
    )

@Composable
private fun DashboardNavIcon(
    kind: DashboardNavIconKind,
    color: Color,
    modifier: Modifier = Modifier.size(20.dp),
) {
    Icon(
        imageVector = kind.vectorIcon(),
        contentDescription = null,
        modifier = modifier,
        tint = color,
    )
}

private fun DashboardNavIconKind.vectorIcon(): ImageVector = when (this) {
    DashboardNavIconKind.Home -> DashboardHomeIcon
    DashboardNavIconKind.Orders -> DashboardOrdersIcon
    DashboardNavIconKind.Invoice -> DashboardInvoiceIcon
    DashboardNavIconKind.Customers -> DashboardCustomersIcon
    DashboardNavIconKind.Invite -> DashboardTeamIcon
    DashboardNavIconKind.Products -> DashboardProductsIcon
    DashboardNavIconKind.Marketing -> DashboardMarketingIcon
    DashboardNavIconKind.Account -> DashboardAccountIcon
    DashboardNavIconKind.Printing -> DashboardPrinterIcon
    DashboardNavIconKind.Logout -> DashboardLogoutIcon
}

private fun dashboardVectorIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply(block).build()

private fun ImageVector.Builder.dashboardPath(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero,
    ) {
        block()
    }
}

private val DashboardHomeIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavHome") {
        dashboardPath {
            moveTo(10.2f, 21f)
            verticalLineTo(14.2f)
            horizontalLineTo(13.8f)
            verticalLineTo(21f)
            horizontalLineTo(18.7f)
            curveTo(19.8f, 21f, 20.7f, 20.1f, 20.7f, 19f)
            verticalLineTo(11.4f)
            lineTo(12f, 4.3f)
            lineTo(3.3f, 11.4f)
            verticalLineTo(19f)
            curveTo(3.3f, 20.1f, 4.2f, 21f, 5.3f, 21f)
            horizontalLineTo(10.2f)
            close()
            moveTo(2.5f, 10.2f)
            curveTo(1.9f, 10.7f, 1.8f, 11.6f, 2.3f, 12.2f)
            curveTo(2.8f, 12.8f, 3.7f, 12.9f, 4.3f, 12.4f)
            lineTo(12f, 6.1f)
            lineTo(19.7f, 12.4f)
            curveTo(20.3f, 12.9f, 21.2f, 12.8f, 21.7f, 12.2f)
            curveTo(22.2f, 11.6f, 22.1f, 10.7f, 21.5f, 10.2f)
            lineTo(13.2f, 3.4f)
            curveTo(12.5f, 2.8f, 11.5f, 2.8f, 10.8f, 3.4f)
            lineTo(2.5f, 10.2f)
            close()
        }
    }
}

private val DashboardOrdersIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavOrders") {
        dashboardPath {
            moveTo(6.5f, 3f)
            horizontalLineTo(17.5f)
            curveTo(18.9f, 3f, 20f, 4.1f, 20f, 5.5f)
            verticalLineTo(18.5f)
            curveTo(20f, 19.9f, 18.9f, 21f, 17.5f, 21f)
            horizontalLineTo(6.5f)
            curveTo(5.1f, 21f, 4f, 19.9f, 4f, 18.5f)
            verticalLineTo(5.5f)
            curveTo(4f, 4.1f, 5.1f, 3f, 6.5f, 3f)
            close()
            moveTo(7.4f, 8.2f)
            curveTo(7.4f, 8.9f, 7.9f, 9.4f, 8.6f, 9.4f)
            horizontalLineTo(15.4f)
            curveTo(16.1f, 9.4f, 16.6f, 8.9f, 16.6f, 8.2f)
            curveTo(16.6f, 7.5f, 16.1f, 7f, 15.4f, 7f)
            horizontalLineTo(8.6f)
            curveTo(7.9f, 7f, 7.4f, 7.5f, 7.4f, 8.2f)
            close()
            moveTo(7.4f, 12f)
            curveTo(7.4f, 12.7f, 7.9f, 13.2f, 8.6f, 13.2f)
            horizontalLineTo(15.4f)
            curveTo(16.1f, 13.2f, 16.6f, 12.7f, 16.6f, 12f)
            curveTo(16.6f, 11.3f, 16.1f, 10.8f, 15.4f, 10.8f)
            horizontalLineTo(8.6f)
            curveTo(7.9f, 10.8f, 7.4f, 11.3f, 7.4f, 12f)
            close()
            moveTo(7.4f, 15.8f)
            curveTo(7.4f, 16.5f, 7.9f, 17f, 8.6f, 17f)
            horizontalLineTo(13.2f)
            curveTo(13.9f, 17f, 14.4f, 16.5f, 14.4f, 15.8f)
            curveTo(14.4f, 15.1f, 13.9f, 14.6f, 13.2f, 14.6f)
            horizontalLineTo(8.6f)
            curveTo(7.9f, 14.6f, 7.4f, 15.1f, 7.4f, 15.8f)
            close()
        }
    }
}

private val DashboardInvoiceIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavInvoice") {
        dashboardPath {
            moveTo(7f, 2.5f)
            horizontalLineTo(14.2f)
            lineTo(20f, 8.3f)
            verticalLineTo(19f)
            curveTo(20f, 20.4f, 18.9f, 21.5f, 17.5f, 21.5f)
            horizontalLineTo(6.5f)
            curveTo(5.1f, 21.5f, 4f, 20.4f, 4f, 19f)
            verticalLineTo(5f)
            curveTo(4f, 3.6f, 5.1f, 2.5f, 6.5f, 2.5f)
            horizontalLineTo(7f)
            close()
            moveTo(14f, 4.8f)
            verticalLineTo(8.4f)
            curveTo(14f, 9.1f, 14.6f, 9.7f, 15.3f, 9.7f)
            horizontalLineTo(18f)
            lineTo(14f, 4.8f)
            close()
            moveTo(7.4f, 12f)
            curveTo(7.4f, 12.6f, 7.9f, 13.1f, 8.5f, 13.1f)
            horizontalLineTo(15.5f)
            curveTo(16.1f, 13.1f, 16.6f, 12.6f, 16.6f, 12f)
            curveTo(16.6f, 11.4f, 16.1f, 10.9f, 15.5f, 10.9f)
            horizontalLineTo(8.5f)
            curveTo(7.9f, 10.9f, 7.4f, 11.4f, 7.4f, 12f)
            close()
            moveTo(7.4f, 15.6f)
            curveTo(7.4f, 16.2f, 7.9f, 16.7f, 8.5f, 16.7f)
            horizontalLineTo(14.2f)
            curveTo(14.8f, 16.7f, 15.3f, 16.2f, 15.3f, 15.6f)
            curveTo(15.3f, 15f, 14.8f, 14.5f, 14.2f, 14.5f)
            horizontalLineTo(8.5f)
            curveTo(7.9f, 14.5f, 7.4f, 15f, 7.4f, 15.6f)
            close()
        }
    }
}

private val DashboardCustomersIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavCustomers") {
        dashboardPath {
            moveTo(9.1f, 12f)
            curveTo(11.2f, 12f, 12.9f, 10.3f, 12.9f, 8.2f)
            curveTo(12.9f, 6.1f, 11.2f, 4.4f, 9.1f, 4.4f)
            curveTo(7f, 4.4f, 5.3f, 6.1f, 5.3f, 8.2f)
            curveTo(5.3f, 10.3f, 7f, 12f, 9.1f, 12f)
            close()
            moveTo(4.4f, 20f)
            horizontalLineTo(13.8f)
            curveTo(14.7f, 20f, 15.3f, 19.2f, 15.1f, 18.3f)
            curveTo(14.5f, 15.5f, 12.1f, 13.6f, 9.1f, 13.6f)
            curveTo(6.1f, 13.6f, 3.7f, 15.5f, 3.1f, 18.3f)
            curveTo(2.9f, 19.2f, 3.5f, 20f, 4.4f, 20f)
            close()
            moveTo(16.9f, 10.4f)
            curveTo(18.6f, 10.4f, 19.9f, 9.1f, 19.9f, 7.4f)
            curveTo(19.9f, 5.7f, 18.6f, 4.4f, 16.9f, 4.4f)
            curveTo(16.1f, 4.4f, 15.4f, 4.7f, 14.9f, 5.1f)
            curveTo(15.5f, 6f, 15.8f, 7f, 15.8f, 8.2f)
            curveTo(15.8f, 8.9f, 15.7f, 9.5f, 15.4f, 10.1f)
            curveTo(15.8f, 10.3f, 16.3f, 10.4f, 16.9f, 10.4f)
            close()
            moveTo(17f, 12.2f)
            curveTo(16.4f, 12.2f, 15.8f, 12.3f, 15.2f, 12.5f)
            curveTo(16.3f, 13.6f, 17.1f, 15f, 17.5f, 16.7f)
            curveTo(17.6f, 17.1f, 17.6f, 17.5f, 17.5f, 17.9f)
            horizontalLineTo(20.5f)
            curveTo(21.4f, 17.9f, 22f, 17f, 21.7f, 16.1f)
            curveTo(21.1f, 13.8f, 19.3f, 12.2f, 17f, 12.2f)
            close()
        }
    }
}

private val DashboardTeamIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavTeam") {
        dashboardPath {
            moveTo(9f, 11.4f)
            curveTo(10.9f, 11.4f, 12.4f, 9.9f, 12.4f, 8f)
            curveTo(12.4f, 6.1f, 10.9f, 4.6f, 9f, 4.6f)
            curveTo(7.1f, 4.6f, 5.6f, 6.1f, 5.6f, 8f)
            curveTo(5.6f, 9.9f, 7.1f, 11.4f, 9f, 11.4f)
            close()
            moveTo(4.2f, 20f)
            horizontalLineTo(12.2f)
            curveTo(13.1f, 20f, 13.8f, 19.2f, 13.6f, 18.3f)
            curveTo(13f, 15.3f, 11.2f, 13.4f, 9f, 13.4f)
            curveTo(6.8f, 13.4f, 5f, 15.3f, 4.4f, 18.3f)
            curveTo(4.2f, 19.2f, 3.9f, 20f, 4.2f, 20f)
            close()
            moveTo(16.9f, 9.2f)
            horizontalLineTo(19f)
            curveTo(19.7f, 9.2f, 20.2f, 8.7f, 20.2f, 8f)
            curveTo(20.2f, 7.3f, 19.7f, 6.8f, 19f, 6.8f)
            horizontalLineTo(16.9f)
            verticalLineTo(4.8f)
            curveTo(16.9f, 4.1f, 16.4f, 3.6f, 15.7f, 3.6f)
            curveTo(15f, 3.6f, 14.5f, 4.1f, 14.5f, 4.8f)
            verticalLineTo(6.8f)
            horizontalLineTo(12.5f)
            curveTo(11.8f, 6.8f, 11.3f, 7.3f, 11.3f, 8f)
            curveTo(11.3f, 8.7f, 11.8f, 9.2f, 12.5f, 9.2f)
            horizontalLineTo(14.5f)
            verticalLineTo(11.2f)
            curveTo(14.5f, 11.9f, 15f, 12.4f, 15.7f, 12.4f)
            curveTo(16.4f, 12.4f, 16.9f, 11.9f, 16.9f, 11.2f)
            verticalLineTo(9.2f)
            close()
            moveTo(17.4f, 13.4f)
            curveTo(16.5f, 13.4f, 15.7f, 13.6f, 15f, 14f)
            curveTo(15.6f, 15.1f, 16f, 16.4f, 16.3f, 17.9f)
            horizontalLineTo(20f)
            curveTo(20.9f, 17.9f, 21.5f, 17.1f, 21.3f, 16.2f)
            curveTo(20.8f, 14.5f, 19.3f, 13.4f, 17.4f, 13.4f)
            close()
        }
    }
}

private val DashboardProductsIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavProducts") {
        dashboardPath {
            moveTo(4.8f, 7.2f)
            lineTo(12f, 3.2f)
            lineTo(19.2f, 7.2f)
            lineTo(12f, 11.2f)
            lineTo(4.8f, 7.2f)
            close()
            moveTo(3.6f, 9.3f)
            verticalLineTo(16.8f)
            curveTo(3.6f, 17.5f, 4f, 18.2f, 4.6f, 18.5f)
            lineTo(10.8f, 22f)
            verticalLineTo(13.3f)
            lineTo(3.6f, 9.3f)
            close()
            moveTo(13.2f, 22f)
            lineTo(19.4f, 18.5f)
            curveTo(20f, 18.2f, 20.4f, 17.5f, 20.4f, 16.8f)
            verticalLineTo(9.3f)
            lineTo(13.2f, 13.3f)
            verticalLineTo(22f)
            close()
        }
    }
}

private val DashboardMarketingIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavMarketing") {
        dashboardPath {
            moveTo(4.3f, 10.2f)
            horizontalLineTo(6.8f)
            verticalLineTo(15.1f)
            horizontalLineTo(4.3f)
            curveTo(3.1f, 15.1f, 2.2f, 14.2f, 2.2f, 13f)
            verticalLineTo(12.3f)
            curveTo(2.2f, 11.1f, 3.1f, 10.2f, 4.3f, 10.2f)
            close()
            moveTo(8f, 9.8f)
            lineTo(17.6f, 5.2f)
            curveTo(18.6f, 4.7f, 19.8f, 5.4f, 19.8f, 6.6f)
            verticalLineTo(18.7f)
            curveTo(19.8f, 19.9f, 18.6f, 20.6f, 17.6f, 20.1f)
            lineTo(8f, 15.5f)
            verticalLineTo(9.8f)
            close()
            moveTo(6.7f, 16.3f)
            horizontalLineTo(4.6f)
            lineTo(5.5f, 20.1f)
            curveTo(5.7f, 20.8f, 6.3f, 21.2f, 7f, 21.1f)
            curveTo(7.8f, 21f, 8.3f, 20.2f, 8.1f, 19.5f)
            lineTo(6.7f, 16.3f)
            close()
        }
    }
}

private val DashboardAccountIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavAccount") {
        dashboardPath {
            moveTo(12f, 12.1f)
            curveTo(14.4f, 12.1f, 16.3f, 10.2f, 16.3f, 7.8f)
            curveTo(16.3f, 5.4f, 14.4f, 3.5f, 12f, 3.5f)
            curveTo(9.6f, 3.5f, 7.7f, 5.4f, 7.7f, 7.8f)
            curveTo(7.7f, 10.2f, 9.6f, 12.1f, 12f, 12.1f)
            close()
            moveTo(5.1f, 20.5f)
            horizontalLineTo(18.9f)
            curveTo(19.9f, 20.5f, 20.6f, 19.5f, 20.2f, 18.6f)
            curveTo(18.9f, 15.5f, 15.9f, 13.6f, 12f, 13.6f)
            curveTo(8.1f, 13.6f, 5.1f, 15.5f, 3.8f, 18.6f)
            curveTo(3.4f, 19.5f, 4.1f, 20.5f, 5.1f, 20.5f)
            close()
        }
    }
}

private val DashboardPrinterIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavPrinter") {
        dashboardPath {
            moveTo(7f, 3.2f)
            horizontalLineTo(17f)
            curveTo(18.1f, 3.2f, 19f, 4.1f, 19f, 5.2f)
            verticalLineTo(8f)
            horizontalLineTo(5f)
            verticalLineTo(5.2f)
            curveTo(5f, 4.1f, 5.9f, 3.2f, 7f, 3.2f)
            close()
            moveTo(5.4f, 9.3f)
            horizontalLineTo(18.6f)
            curveTo(20.3f, 9.3f, 21.6f, 10.6f, 21.6f, 12.3f)
            verticalLineTo(16.2f)
            curveTo(21.6f, 17.1f, 20.9f, 17.8f, 20f, 17.8f)
            horizontalLineTo(18.4f)
            verticalLineTo(15.2f)
            curveTo(18.4f, 14.2f, 17.6f, 13.4f, 16.6f, 13.4f)
            horizontalLineTo(7.4f)
            curveTo(6.4f, 13.4f, 5.6f, 14.2f, 5.6f, 15.2f)
            verticalLineTo(17.8f)
            horizontalLineTo(4f)
            curveTo(3.1f, 17.8f, 2.4f, 17.1f, 2.4f, 16.2f)
            verticalLineTo(12.3f)
            curveTo(2.4f, 10.6f, 3.7f, 9.3f, 5.4f, 9.3f)
            close()
            moveTo(7.7f, 15.5f)
            horizontalLineTo(16.3f)
            verticalLineTo(21f)
            horizontalLineTo(7.7f)
            verticalLineTo(15.5f)
            close()
            moveTo(17.7f, 12.9f)
            curveTo(18.4f, 12.9f, 18.9f, 12.4f, 18.9f, 11.7f)
            curveTo(18.9f, 11f, 18.4f, 10.5f, 17.7f, 10.5f)
            curveTo(17f, 10.5f, 16.5f, 11f, 16.5f, 11.7f)
            curveTo(16.5f, 12.4f, 17f, 12.9f, 17.7f, 12.9f)
            close()
        }
    }
}

private val DashboardLogoutIcon: ImageVector by lazy {
    dashboardVectorIcon("OrmaNavLogout") {
        dashboardPath {
            moveTo(5.5f, 3.5f)
            horizontalLineTo(12.4f)
            curveTo(13.4f, 3.5f, 14.2f, 4.3f, 14.2f, 5.3f)
            verticalLineTo(7.2f)
            curveTo(14.2f, 7.9f, 13.7f, 8.4f, 13f, 8.4f)
            curveTo(12.3f, 8.4f, 11.8f, 7.9f, 11.8f, 7.2f)
            verticalLineTo(6.1f)
            horizontalLineTo(6.1f)
            verticalLineTo(17.9f)
            horizontalLineTo(11.8f)
            verticalLineTo(16.8f)
            curveTo(11.8f, 16.1f, 12.3f, 15.6f, 13f, 15.6f)
            curveTo(13.7f, 15.6f, 14.2f, 16.1f, 14.2f, 16.8f)
            verticalLineTo(18.7f)
            curveTo(14.2f, 19.7f, 13.4f, 20.5f, 12.4f, 20.5f)
            horizontalLineTo(5.5f)
            curveTo(4.5f, 20.5f, 3.7f, 19.7f, 3.7f, 18.7f)
            verticalLineTo(5.3f)
            curveTo(3.7f, 4.3f, 4.5f, 3.5f, 5.5f, 3.5f)
            close()
            moveTo(15.3f, 8.1f)
            curveTo(15.8f, 7.6f, 16.5f, 7.6f, 17f, 8.1f)
            lineTo(20.1f, 11.2f)
            curveTo(20.6f, 11.7f, 20.6f, 12.3f, 20.1f, 12.8f)
            lineTo(17f, 15.9f)
            curveTo(16.5f, 16.4f, 15.8f, 16.4f, 15.3f, 15.9f)
            curveTo(14.8f, 15.4f, 14.8f, 14.7f, 15.3f, 14.2f)
            lineTo(16.4f, 13.2f)
            horizontalLineTo(10.6f)
            curveTo(9.9f, 13.2f, 9.4f, 12.7f, 9.4f, 12f)
            curveTo(9.4f, 11.3f, 9.9f, 10.8f, 10.6f, 10.8f)
            horizontalLineTo(16.4f)
            lineTo(15.3f, 9.8f)
            curveTo(14.8f, 9.3f, 14.8f, 8.6f, 15.3f, 8.1f)
            close()
        }
    }
}

@Composable
private fun DashboardQrGlyph(
    color: Color,
    modifier: Modifier = Modifier.size(20.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = (size.minDimension * 0.10f).coerceAtLeast(1.8f)
        val cell = size.minDimension / 5f
        fun square(column: Int, row: Int, filled: Boolean = false) {
            val topLeft = Offset(column * cell + strokeWidth, row * cell + strokeWidth)
            val squareSize = Size(cell * 1.38f, cell * 1.38f)
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = squareSize,
                cornerRadius = CornerRadius(cell * 0.16f, cell * 0.16f),
                style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = strokeWidth),
            )
        }
        square(0, 0)
        square(3, 0)
        square(0, 3)
        square(2, 2, filled = true)
        square(4, 3, filled = true)
        square(3, 4, filled = true)
        drawLine(
            color = color,
            start = Offset(cell * 2.4f, cell * 0.7f),
            end = Offset(cell * 2.4f, cell * 1.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(cell * 3.0f, cell * 2.7f),
            end = Offset(cell * 4.5f, cell * 2.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private enum class DashboardNavIconKind {
    Home,
    Orders,
    Invoice,
    Customers,
    Invite,
    Products,
    Marketing,
    Account,
    Printing,
    Logout,
}

private enum class DashboardSection {
    Dashboard,
    OrdersBookings,
    Invoices,
    Customers,
    Products,
    Marketing,
    Team,
    Account,
}

private val DashboardSection.title: String
    get() = when (this) {
        DashboardSection.Dashboard -> "Home"
        DashboardSection.OrdersBookings -> "Orders"
        DashboardSection.Invoices -> "Invoices"
        DashboardSection.Customers -> "Customers"
        DashboardSection.Products -> "Products"
        DashboardSection.Marketing -> "Online store"
        DashboardSection.Team -> "Team"
        DashboardSection.Account -> "Account"
    }

private val DashboardSection.description: String
    get() = when (this) {
        DashboardSection.Dashboard -> "See today's sales, orders, customers, and stock."
        DashboardSection.OrdersBookings -> "Create and manage orders, services, appointments, payments, and dispatch."
        DashboardSection.Invoices -> "Create and preview tax invoices."
        DashboardSection.Customers -> "Manage customer details and order history."
        DashboardSection.Products -> "Manage products, services, stock, suppliers, and pricing."
        DashboardSection.Marketing -> "Share your online catalog through WhatsApp, Meta, and QR links."
        DashboardSection.Team -> "Manage staff access."
        DashboardSection.Account -> "Manage business profile, logo, printers, payments, and sign out."
    }

private data class DashboardNavItem(
    val section: DashboardSection,
    val bottomLabel: String,
    val sidebarTitle: String,
    val sidebarBody: String,
    val icon: DashboardNavIconKind,
)

private val DashboardBottomNavItems = listOf(
    DashboardNavItem(
        section = DashboardSection.Dashboard,
        bottomLabel = "Home",
        sidebarTitle = "Home",
        sidebarBody = "Business overview",
        icon = DashboardNavIconKind.Home,
    ),
    DashboardNavItem(
        section = DashboardSection.OrdersBookings,
        bottomLabel = "Orders",
        sidebarTitle = "Orders",
        sidebarBody = "Sales and bookings",
        icon = DashboardNavIconKind.Orders,
    ),
    DashboardNavItem(
        section = DashboardSection.Invoices,
        bottomLabel = "Invoices",
        sidebarTitle = "Invoices",
        sidebarBody = "Tax bills",
        icon = DashboardNavIconKind.Invoice,
    ),
    DashboardNavItem(
        section = DashboardSection.Customers,
        bottomLabel = "Customers",
        sidebarTitle = "Customers",
        sidebarBody = "People and history",
        icon = DashboardNavIconKind.Customers,
    ),
    DashboardNavItem(
        section = DashboardSection.Products,
        bottomLabel = "Products",
        sidebarTitle = "Products",
        sidebarBody = "Items and stock",
        icon = DashboardNavIconKind.Products,
    ),
    DashboardNavItem(
        section = DashboardSection.Marketing,
        bottomLabel = "Online",
        sidebarTitle = "Online store",
        sidebarBody = "Catalog and QR",
        icon = DashboardNavIconKind.Marketing,
    ),
    DashboardNavItem(
        section = DashboardSection.Account,
        bottomLabel = "Account",
        sidebarTitle = "Account",
        sidebarBody = "Settings and sign out",
        icon = DashboardNavIconKind.Account,
    ),
)

private val DashboardMobileBottomNavItems = listOf(
    DashboardBottomNavItems[0],
    DashboardBottomNavItems[1],
    DashboardBottomNavItems[3],
    DashboardBottomNavItems[4],
    DashboardBottomNavItems[6],
)

private val DashboardOperationalSidebarNavItems = buildList {
    addAll(DashboardBottomNavItems.take(5))
    add(
        DashboardNavItem(
            section = DashboardSection.Team,
            bottomLabel = "Team",
            sidebarTitle = "Team",
            sidebarBody = "Staff access",
            icon = DashboardNavIconKind.Invite,
        ),
    )
}

private val DashboardMarketingSidebarNavItems = listOf(
    DashboardNavItem(
        section = DashboardSection.Marketing,
        bottomLabel = "Online",
        sidebarTitle = "Online store",
        sidebarBody = "Catalog and QR",
        icon = DashboardNavIconKind.Marketing,
    ),
)

private fun canAccessDashboardSection(
    section: DashboardSection,
    canInviteMembers: Boolean,
    canUseMarketing: Boolean,
): Boolean = when (section) {
    DashboardSection.Team -> true
    DashboardSection.Marketing -> canUseMarketing
    else -> true
}

private fun DashboardNavItem.bottomLabel(state: OnboardingUiState): String =
    when (section) {
        DashboardSection.OrdersBookings -> when {
            state.selectedDashboardOrderTypeFilter() == "all" -> bottomLabel
            state.activeDashboardOrderType() == "service" -> "Services"
            state.activeDashboardOrderType() == "appointment" -> "Appointments"
            else -> "Sales"
        }
        DashboardSection.Products -> when {
            state.selectedDashboardItemTypeFilter() == "all" -> bottomLabel
            state.activeDashboardItemType() == "service" -> "Services"
            state.activeDashboardItemType() == "appointment" -> "Appointments"
            else -> "Products"
        }
        else -> bottomLabel
    }

private fun DashboardNavItem.sidebarTitle(state: OnboardingUiState): String =
    when (section) {
        DashboardSection.Team -> if (state.accessPath == AccessPath.TeamMember) "My access" else sidebarTitle
        DashboardSection.OrdersBookings -> when {
            state.selectedDashboardOrderTypeFilter() == "all" -> sidebarTitle
            state.activeDashboardOrderType() == "service" -> "Services"
            state.activeDashboardOrderType() == "appointment" -> "Appointments"
            else -> "Sales"
        }
        DashboardSection.Products -> when {
            state.selectedDashboardItemTypeFilter() == "all" -> sidebarTitle
            state.activeDashboardItemType() == "service" -> "Services"
            state.activeDashboardItemType() == "appointment" -> "Appointment services"
            else -> sidebarTitle
        }
        else -> sidebarTitle
    }

private fun DashboardNavItem.sidebarBody(state: OnboardingUiState): String =
    when (section) {
        DashboardSection.Team -> if (state.accessPath == AccessPath.TeamMember) "Role and team" else sidebarBody
        DashboardSection.OrdersBookings -> when {
            state.selectedDashboardOrderTypeFilter() == "all" -> sidebarBody
            state.activeDashboardOrderType() == "service" -> "Requests, payment, completion"
            state.activeDashboardOrderType() == "appointment" -> "Bookings and schedules"
            else -> sidebarBody
        }
        DashboardSection.Products -> when {
            state.selectedDashboardItemTypeFilter() == "all" -> sidebarBody
            state.activeDashboardItemType() == "service" -> "Service pricing"
            state.activeDashboardItemType() == "appointment" -> "Duration and booking"
            else -> sidebarBody
        }
        else -> sidebarBody
    }

@Composable
private fun DashboardSectionContent(
    state: OnboardingUiState,
    roleLabel: String,
    selectedSection: DashboardSection,
    canInviteMembers: Boolean,
    actions: OnboardingActions,
    onOpenTeam: (() -> Unit)?,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
    wide: Boolean,
    mobileSelectedOrderId: String? = null,
    onMobileSelectedOrderChange: ((String?) -> Unit)? = null,
) {
    when (selectedSection) {
        DashboardSection.Dashboard -> DashboardHomeContent(
            state = state,
            roleLabel = roleLabel,
            actions = actions,
            onOpenOrders = onOpenOrders,
            onOpenProducts = onOpenProducts,
            wide = wide,
        )
        DashboardSection.OrdersBookings -> DashboardOrdersContent(
            state = state,
            actions = actions,
            wide = wide,
            mobileSelectedOrderId = mobileSelectedOrderId,
            onMobileSelectedOrderChange = onMobileSelectedOrderChange,
        )
        DashboardSection.Invoices -> DashboardInvoicesContent(
            state = state,
            actions = actions,
            wide = wide,
        )
        DashboardSection.Customers -> DashboardCustomersContent(
            state = state,
            actions = actions,
            wide = wide,
        )
        DashboardSection.Products -> DashboardProductsContent(
            state = state,
            actions = actions,
            wide = wide,
        )
        DashboardSection.Marketing -> DashboardMarketingContent(
            state = state,
            actions = actions,
            onOpenProducts = onOpenProducts,
            wide = wide,
        )
        DashboardSection.Team -> DashboardTeamContent(
            state = state,
            actions = actions,
            canInviteMembers = canInviteMembers,
            wide = wide,
        )
        DashboardSection.Account -> DashboardAccountContent(
            state = state,
            actions = actions,
            roleLabel = roleLabel,
            canInviteMembers = canInviteMembers,
            onOpenTeam = onOpenTeam,
            wide = wide,
        )
    }
}

@Composable
private fun DashboardHomeContent(
    state: OnboardingUiState,
    roleLabel: String,
    actions: OnboardingActions,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
    wide: Boolean,
) {
    var showOrderSheet by rememberSaveable { mutableStateOf(false) }
    var showCustomerSheet by rememberSaveable { mutableStateOf(false) }
    var showProductSheet by rememberSaveable { mutableStateOf(false) }

    if (wide) {
        DashboardWideHomeContent(
            state = state,
            roleLabel = roleLabel,
            onOrder = { showOrderSheet = true },
            onCustomer = { showCustomerSheet = true },
            onProduct = { showProductSheet = true },
            onOpenOrders = onOpenOrders,
            onOpenProducts = onOpenProducts,
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardOperatingBrief(
                state = state,
                wide = false,
                onOrder = { showOrderSheet = true },
                onCustomer = { showCustomerSheet = true },
                onProduct = { showProductSheet = true },
                onOpenOrders = onOpenOrders,
                onOpenProducts = onOpenProducts,
            )
            OrmaDashboardRevenueCard(
                series = state.dashboard.summary.revenueSeries,
                currency = state.dashboard.summary.currency,
            )
            DashboardMobileKpiRail(state = state)
            DashboardWorkQueuePreviewCard(
                state = state,
                onOpenOrders = null,
            )
            OrmaDashboardActivityPanel(
                activities = state.dashboard.summary.recentActivity,
            )
            DashboardBusinessSnapshotCard(state = state)
            DashboardWorkspaceCard(
                state = state,
                roleLabel = roleLabel,
            )
        }
    }

    if (showOrderSheet) {
        OrderFormSheet(
            state = state,
            actions = actions,
            wide = wide,
            onDismiss = { showOrderSheet = false },
            onSubmit = { draft ->
                actions.onCreateOrder(draft)
                showOrderSheet = false
            },
        )
    }
    if (showCustomerSheet) {
        CustomerFormSheet(
            onDismiss = { showCustomerSheet = false },
            onSubmit = { draft ->
                actions.onCreateCustomer(draft)
                showCustomerSheet = false
            },
        )
    }
    if (showProductSheet) {
        ProductFormSheet(
            state = state,
            onDismiss = { showProductSheet = false },
            onSubmit = { draft ->
                actions.onCreateProduct(draft)
                showProductSheet = false
            },
        )
    }
}

@Composable
private fun DashboardWideHomeContent(
    state: OnboardingUiState,
    roleLabel: String,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
) {
    val summary = state.dashboard.summary
    val activeOrders = state.dashboard.orders.count { it.status in DashboardActiveOrderStatuses || it.status == "paid" }
    val unpaidOrders = state.dashboard.orders.count { order ->
        order.status !in setOf("cancelled", "completed") &&
            (order.total.toDoubleOrNull().orZero() - order.paidTotal.toDoubleOrNull().orZero()) > 0.0
    }
    val missingImages = state.dashboard.products.count {
        it.status == "active" && it.itemType in state.allowedDashboardItemTypes() && it.imageUrl.isNullOrBlank()
    }
    val reachableCustomers = state.dashboard.customers.count {
        !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank()
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardExecutiveKpiHero(
            state = state,
            activeOrders = activeOrders,
            unpaidOrders = unpaidOrders,
            missingImages = missingImages,
            reachableCustomers = reachableCustomers,
            onOpenOrders = onOpenOrders,
            onOpenProducts = onOpenProducts,
            onOrder = onOrder,
            onProduct = onProduct,
        )
        DashboardWideKpiGrid(state = state)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 1240.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DashboardWideAnalyticsPanel(state = state)
                    DashboardWideCommandBoard(
                        state = state,
                        onOrder = onOrder,
                        onCustomer = onCustomer,
                        onProduct = onProduct,
                    )
                    DashboardWideReadinessPanel(
                        state = state,
                        activeOrders = activeOrders,
                        unpaidOrders = unpaidOrders,
                        missingImages = missingImages,
                        reachableCustomers = reachableCustomers,
                        onOrder = onOpenOrders ?: onOrder,
                        onProduct = onOpenProducts ?: onProduct,
                        onCustomer = onCustomer,
                    )
                    OrmaDashboardTopItemsPanel(
                        items = summary.topItems.take(5),
                        currency = summary.currency,
                    )
                    DashboardWorkQueuePreviewCard(
                        state = state,
                        onOpenOrders = onOpenOrders,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OrmaDashboardActivityPanel(
                        activities = summary.recentActivity.take(6),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OrmaDashboardBreakdownPanel(
                        title = "Order stages",
                        body = "Where current work and revenue are sitting.",
                        items = summary.orderStatusBreakdown,
                        currency = summary.currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OrmaDashboardBreakdownPanel(
                        title = "Business mix",
                        body = "Product sales, services, and appointments.",
                        items = summary.orderTypeBreakdown,
                        currency = summary.currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OrmaDashboardNotificationPanel(
                        notifications = summary.notificationPreview.take(3),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1.08f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        DashboardWideAnalyticsPanel(state = state)
                        DashboardWideCommandBoard(
                            state = state,
                            onOrder = onOrder,
                            onCustomer = onCustomer,
                            onProduct = onProduct,
                        )
                        DashboardWorkQueuePreviewCard(
                            state = state,
                            onOpenOrders = onOpenOrders,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OrmaDashboardBreakdownPanel(
                            title = "Order stages",
                            body = "Where current work and revenue are sitting.",
                            items = summary.orderStatusBreakdown,
                            currency = summary.currency,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.92f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        DashboardWideReadinessPanel(
                            state = state,
                            activeOrders = activeOrders,
                            unpaidOrders = unpaidOrders,
                            missingImages = missingImages,
                            reachableCustomers = reachableCustomers,
                            onOrder = onOpenOrders ?: onOrder,
                            onProduct = onOpenProducts ?: onProduct,
                            onCustomer = onCustomer,
                        )
                        OrmaDashboardTopItemsPanel(
                            items = summary.topItems.take(5),
                            currency = summary.currency,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OrmaDashboardActivityPanel(
                            activities = summary.recentActivity.take(6),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OrmaDashboardBreakdownPanel(
                            title = "Business mix",
                            body = "Product sales, services, and appointments.",
                            items = summary.orderTypeBreakdown,
                            currency = summary.currency,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OrmaDashboardNotificationPanel(
                            notifications = summary.notificationPreview.take(3),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardExecutiveKpiHero(
    state: OnboardingUiState,
    activeOrders: Int,
    unpaidOrders: Int,
    missingImages: Int,
    reachableCustomers: Int,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
    onOrder: () -> Unit,
    onProduct: () -> Unit,
) {
    val summary = state.dashboard.summary
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    val totalOrderValue = state.dashboard.orders.sumOf { it.total.toDoubleOrNull().orZero() }
    val paidValue = summary.totalPaidAmount.toDoubleOrNull().orZero()
    val balanceValue = state.dashboard.orders.sumOf { order ->
        (order.total.toDoubleOrNull().orZero() - order.paidTotal.toDoubleOrNull().orZero()).coerceAtLeast(0.0)
    }
    val collectionRate = when {
        totalOrderValue > 0.0 -> ((paidValue / totalOrderValue) * 100.0).coerceIn(0.0, 100.0)
        paidValue > 0.0 -> 100.0
        else -> 0.0
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.Sheet,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            OrmaColors.HeroAccentStart,
                            OrmaColors.HeroAccentEnd,
                            OrmaColors.Accent,
                        ),
                    ),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.92f)
                        .heightIn(min = 226.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "KPI COMMAND CENTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.62f),
                        )
                        Text(
                            text = dashboardMoney(summary.totalPaidAmount, summary.currency),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (paidValue > 0.0) {
                                "Collected revenue from confirmed business activity."
                            } else {
                                "Revenue appears here after the first paid sale, service, or appointment."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.74f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardHeroActionButton(
                            text = orderType.orderActionText(),
                            primary = true,
                            enabled = !state.dashboard.actionLoading,
                            onClick = onOrder,
                        )
                        DashboardHeroActionButton(
                            text = "Open queue",
                            enabled = !state.dashboard.actionLoading,
                            onClick = onOpenOrders ?: onOrder,
                        )
                    }
                }
                DashboardHeroSparkline(
                    series = summary.revenueSeries,
                    currency = summary.currency,
                    modifier = Modifier
                        .weight(1.18f)
                        .heightIn(min = 226.dp),
                )
                Column(
                    modifier = Modifier
                        .weight(1.04f)
                        .heightIn(min = 226.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardHeroMetricTile(
                            label = "Open work",
                            value = activeOrders.toString(),
                            detail = orderType.orderProgressPlural(),
                            modifier = Modifier.weight(1f),
                        )
                        DashboardHeroMetricTile(
                            label = "Collection",
                            value = "${collectionRate.toInt()}%",
                            detail = dashboardMoney(balanceValue.toDashboardMoneyInput(), summary.currency) + " due",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardHeroMetricTile(
                            label = "Catalog",
                            value = state.dashboard.products.count {
                                it.status == "active" && it.itemType in state.allowedDashboardItemTypes()
                            }.toString(),
                            detail = if (missingImages > 0) "$missingImages images missing" else itemType.catalogSectionTitle(),
                            modifier = Modifier.weight(1f),
                        )
                        DashboardHeroMetricTile(
                            label = "Customers",
                            value = reachableCustomers.toString(),
                            detail = "reachable",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    DashboardHeroActionButton(
                        text = if (unpaidOrders > 0) "Review $unpaidOrders unpaid" else itemType.catalogActionText(),
                        enabled = !state.dashboard.actionLoading,
                        onClick = if (unpaidOrders > 0) onOpenOrders ?: onOrder else onOpenProducts ?: onProduct,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeroSparkline(
    series: List<OrmaDashboardRevenuePoint>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val values = series.map { it.amount.toDoubleOrNull().orZero() }
    val maxAmount = values.maxOrNull().orZero()
    Surface(
        modifier = modifier,
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Revenue trend",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (maxAmount > 0.0) "$currency ${maxAmount.toDashboardMoneyInput()} peak" else "No paid trend",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (series.isEmpty() || maxAmount <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Paid sales will draw this chart automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                OrmaDashboardRevenueChart(
                    series = series,
                    currency = currency,
                    modifier = Modifier.fillMaxWidth(),
                    chartHeight = 132.dp,
                )
            }
        }
    }
}

@Composable
private fun DashboardHeroMetricTile(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 96.dp),
        shape = OrmaShapes.PremiumCard,
        color = Color.White.copy(alpha = 0.12f),
        contentColor = Color.White,
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.13f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.58f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardHeroActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = OrmaShapes.Capsule,
        color = if (primary) Color.White else Color.White.copy(alpha = 0.10f),
        contentColor = if (primary) OrmaColors.Accent else Color.White,
        border = if (primary) null else BorderStroke(0.8.dp, Color.White.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    !enabled -> Color.White.copy(alpha = 0.38f)
                    primary -> OrmaColors.TextPrimary
                    else -> Color.White
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWideAnalyticsPanel(
    state: OnboardingUiState,
) {
    val summary = state.dashboard.summary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "Performance overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = "Revenue, work volume, and customer movement from real workspace activity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = if (state.dashboard.loading) "SYNCING" else "LIVE",
                    tone = if (state.dashboard.loading) OrmaStatusTone.Info else OrmaStatusTone.Success,
                )
            }
            DashboardLightSparkline(
                series = summary.revenueSeries,
                currency = summary.currency,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                DashboardAnalyticsMiniMetric(
                    label = "Sales",
                    value = summary.salesCount.toString(),
                    detail = "counter and catalog",
                    modifier = Modifier.weight(1f),
                )
                DashboardAnalyticsMiniMetric(
                    label = "Services",
                    value = summary.serviceOrdersCount.toString(),
                    detail = "requests",
                    modifier = Modifier.weight(1f),
                )
                DashboardAnalyticsMiniMetric(
                    label = "Appointments",
                    value = summary.appointmentsCount.toString(),
                    detail = if (summary.todayAppointmentsCount > 0) "${summary.todayAppointmentsCount} today" else "scheduled work",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DashboardLightSparkline(
    series: List<OrmaDashboardRevenuePoint>,
    currency: String,
) {
    val values = series.map { it.amount.toDoubleOrNull().orZero() }
    val maxAmount = values.maxOrNull().orZero()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Paid revenue trend",
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = if (maxAmount > 0.0) "$currency ${maxAmount.toDashboardMoneyInput()} peak" else "Waiting for paid activity",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (series.isEmpty() || maxAmount <= 0.0) {
            OrmaDashboardEmptyState(
                title = "No revenue curve yet",
                body = "Paid sales, services, and appointments will draw this chart without sample data.",
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                OrmaDashboardRevenueChart(
                    series = series,
                    currency = currency,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(),
                    chartHeight = 188.dp,
                )
            }
        }
    }
}

@Composable
private fun DashboardAnalyticsMiniMetric(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 92.dp),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.WorkspacePanel,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWideReadinessPanel(
    state: OnboardingUiState,
    activeOrders: Int,
    unpaidOrders: Int,
    missingImages: Int,
    reachableCustomers: Int,
    onOrder: () -> Unit,
    onProduct: () -> Unit,
    onCustomer: () -> Unit,
) {
    val sellableItems = state.dashboard.products.count {
        it.status == "active" && it.itemType in state.allowedDashboardItemTypes()
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "Business health",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = "Problems that block selling, fulfilment, or follow-up.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
                OrmaBadge(
                    text = if (activeOrders + unpaidOrders + missingImages == 0) "HEALTHY" else "REVIEW",
                    tone = if (activeOrders + unpaidOrders + missingImages == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                )
            }
            DashboardReadinessRow(
                label = "Operations",
                value = if (activeOrders == 0) "Clear" else "$activeOrders open",
                tone = if (activeOrders == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                onClick = onOrder,
            )
            DashboardReadinessRow(
                label = "Payments",
                value = if (unpaidOrders == 0) "No gaps" else "$unpaidOrders unpaid",
                tone = if (unpaidOrders == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                onClick = onOrder,
            )
            DashboardReadinessRow(
                label = "Catalog",
                value = if (missingImages == 0) "$sellableItems ready" else "$missingImages image gaps",
                tone = if (missingImages == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                onClick = onProduct,
            )
            DashboardReadinessRow(
                label = "Customers",
                value = if (reachableCustomers == 0) "Needs contacts" else "$reachableCustomers reachable",
                tone = if (reachableCustomers == 0) OrmaStatusTone.Info else OrmaStatusTone.Success,
                onClick = onCustomer,
            )
        }
    }
}

@Composable
private fun DashboardOperatingBrief(
    state: OnboardingUiState,
    wide: Boolean,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
) {
    val summary = state.dashboard.summary
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    val activeOrders = state.dashboard.orders.count { it.status in DashboardActiveOrderStatuses || it.status == "paid" }
    val unpaidOrders = state.dashboard.orders.count { order ->
        order.status !in setOf("cancelled", "completed") &&
            (order.total.toDoubleOrNull().orZero() - order.paidTotal.toDoubleOrNull().orZero()) > 0.0
    }
    val readyToClose = state.dashboard.orders.count { it.status == "paid" }
    val sellableItems = state.dashboard.products.count {
        it.status == "active" && it.itemType in state.allowedDashboardItemTypes()
    }
    val missingImages = state.dashboard.products.count {
        it.status == "active" && it.itemType in state.allowedDashboardItemTypes() && it.imageUrl.isNullOrBlank()
    }
    val reachableCustomers = state.dashboard.customers.count {
        !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank()
    }
    val nextActionTitle: String
    val nextActionBody: String
    val nextActionButton: String
    val nextAction: () -> Unit
    when {
        activeOrders > 0 -> {
            nextActionTitle = "$activeOrders active ${orderType.orderProgressPlural()}"
            nextActionBody = "Review captured, accepted, paid, and pending work before creating more."
            nextActionButton = "Open queue"
            nextAction = onOpenOrders ?: onOrder
        }
        summary.lowStockProducts > 0 && itemType == "product" -> {
            nextActionTitle = "${summary.lowStockProducts} stock alerts"
            nextActionBody = "Restock items before they block checkout, catalog orders, or counter sales."
            nextActionButton = "Review stock"
            nextAction = onOpenProducts ?: onProduct
        }
        missingImages > 0 -> {
            nextActionTitle = "$missingImages catalog images missing"
            nextActionBody = "Add images so public catalog, WhatsApp selling, and customer ordering feel complete."
            nextActionButton = "Fix catalog"
            nextAction = onOpenProducts ?: onProduct
        }
        summary.totalCustomers == 0 -> {
            nextActionTitle = "Build customer memory"
            nextActionBody = "Save customer contact, location, and notes before repeat orders start."
            nextActionButton = "Add customer"
            nextAction = onCustomer
        }
        else -> {
            nextActionTitle = "Ready to take work"
            nextActionBody = "Capture the next sale, service request, or appointment with customer and payment context."
            nextActionButton = orderType.orderActionText()
            nextAction = onOrder
        }
    }

    if (wide) {
        DashboardWideOperatingBrief(
            state = state,
            orderType = orderType,
            itemType = itemType,
            activeOrders = activeOrders,
            unpaidOrders = unpaidOrders,
            readyToClose = readyToClose,
            sellableItems = sellableItems,
            missingImages = missingImages,
            reachableCustomers = reachableCustomers,
            nextActionTitle = nextActionTitle,
            nextActionBody = nextActionBody,
            nextActionButton = nextActionButton,
            nextAction = nextAction,
            onOrder = onOrder,
            onCustomer = onCustomer,
            onProduct = onProduct,
            onOpenOrders = onOpenOrders,
            onOpenProducts = onOpenProducts,
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(if (wide) 20.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (wide) 18.dp else 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state.dashboardBusinessMode().businessModeBriefEyebrow(),
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.TextSecondary,
                    )
                    Text(
                        text = nextActionTitle,
                        style = if (wide) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        color = OrmaColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = nextActionBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = if (wide) 2 else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = if (state.dashboard.loading) "SYNCING" else "LIVE",
                    tone = if (state.dashboard.loading) OrmaStatusTone.Info else OrmaStatusTone.Success,
                )
            }

            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    DashboardOperatingSignalCell(
                        label = "Queue",
                        value = activeOrders.toString(),
                        detail = if (readyToClose > 0) "$readyToClose paid to close" else orderType.orderProgressPlural(),
                        tone = if (activeOrders > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardOperatingSignalCell(
                        label = "Unpaid",
                        value = unpaidOrders.toString(),
                        detail = if (unpaidOrders == 0) "No collection gaps" else "Need payment follow-up",
                        tone = if (unpaidOrders > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardOperatingSignalCell(
                        label = itemType.catalogSectionTitle(),
                        value = sellableItems.toString(),
                        detail = if (missingImages > 0) "$missingImages images missing" else "Ready to sell",
                        tone = if (missingImages > 0 || summary.lowStockProducts > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
                        onClick = onOpenProducts,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardOperatingSignalCell(
                        label = "Reachable",
                        value = reachableCustomers.toString(),
                        detail = "Customers with phone/email",
                        tone = if (reachableCustomers == 0) OrmaStatusTone.Info else OrmaStatusTone.Success,
                        onClick = null,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DashboardOperatingSignalCell(
                        label = "Queue",
                        value = activeOrders.toString(),
                        detail = orderType.orderProgressPlural(),
                        tone = if (activeOrders > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardOperatingSignalCell(
                        label = "Unpaid",
                        value = unpaidOrders.toString(),
                        detail = "Payments",
                        tone = if (unpaidOrders > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardOperatingSignalCell(
                        label = "Catalog",
                        value = sellableItems.toString(),
                        detail = if (missingImages > 0) "Needs images" else "Ready",
                        tone = if (missingImages > 0 || summary.lowStockProducts > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
                        onClick = onOpenProducts,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            OrmaActionRow(
                primaryText = if (state.dashboard.actionLoading) "Saving..." else nextActionButton,
                onPrimary = nextAction,
                primaryEnabled = !state.dashboard.actionLoading,
                secondaryText = itemType.catalogActionText(),
                onSecondary = onProduct,
            )
        }
    }
}

@Composable
private fun DashboardWideOperatingBrief(
    state: OnboardingUiState,
    orderType: String,
    itemType: String,
    activeOrders: Int,
    unpaidOrders: Int,
    readyToClose: Int,
    sellableItems: Int,
    missingImages: Int,
    reachableCustomers: Int,
    nextActionTitle: String,
    nextActionBody: String,
    nextActionButton: String,
    nextAction: () -> Unit,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
    onOpenOrders: (() -> Unit)?,
    onOpenProducts: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .weight(1.22f)
        .heightIn(min = 226.dp)
        .clip(OrmaShapes.Sheet)
        .background(
            Brush.linearGradient(
                        colors = listOf(
                            OrmaColors.HeroAccentStart,
                            OrmaColors.HeroAccentEnd,
                        ),
                    ),
                )
        .border(
            width = 0.8.dp,
            color = Color.White.copy(alpha = 0.08f),
            shape = OrmaShapes.Sheet,
        ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = state.dashboardBusinessMode().businessModeBriefEyebrow(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.64f),
                        )
                        Text(
                            text = nextActionTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = nextActionBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(
                        text = if (state.dashboard.loading) "SYNCING" else "LIVE",
                        tone = if (state.dashboard.loading) OrmaStatusTone.Info else OrmaStatusTone.Success,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashboardHeroSignalPill(
                        label = "Queue",
                        value = activeOrders.toString(),
                        detail = if (readyToClose > 0) "$readyToClose paid" else orderType.orderProgressPlural(),
                        modifier = Modifier.weight(1f),
                    )
                    DashboardHeroSignalPill(
                        label = "Unpaid",
                        value = unpaidOrders.toString(),
                        detail = if (unpaidOrders == 0) "Clear" else "Follow up",
                        modifier = Modifier.weight(1f),
                    )
                    DashboardHeroSignalPill(
                        label = itemType.catalogSectionTitle(),
                        value = sellableItems.toString(),
                        detail = if (missingImages > 0) "$missingImages images" else "Ready",
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = nextAction,
                        enabled = !state.dashboard.actionLoading,
                        shape = OrmaShapes.Capsule,
                        color = Color.White,
                        contentColor = OrmaColors.Accent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            text = if (state.dashboard.actionLoading) "Saving..." else nextActionButton,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (state.dashboard.actionLoading) OrmaColors.TextDisabled else OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        onClick = onProduct,
                        shape = OrmaShapes.Capsule,
                        color = Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White,
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.16f)),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            text = itemType.catalogActionText(),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(0.88f)
                .heightIn(min = 226.dp),
            shape = OrmaShapes.Sheet,
            color = OrmaColors.CardBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(0.6.dp, OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = OrmaElevation.Subtle,
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "Workspace readiness",
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                        )
                        Text(
                            text = "Signals that affect taking, fulfilling, and selling work.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(
                        text = "${reachableCustomers} reachable",
                        tone = if (reachableCustomers == 0) OrmaStatusTone.Info else OrmaStatusTone.Success,
                    )
                }
                DashboardReadinessRow(
                    label = "Operations",
                    value = if (activeOrders == 0) "Clear" else "$activeOrders open",
                    tone = if (activeOrders == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                    onClick = onOpenOrders ?: onOrder,
                )
                DashboardReadinessRow(
                    label = "Payments",
                    value = if (unpaidOrders == 0) "No gaps" else "$unpaidOrders unpaid",
                    tone = if (unpaidOrders == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                    onClick = onOpenOrders ?: onOrder,
                )
                DashboardReadinessRow(
                    label = "Catalog",
                    value = if (missingImages == 0) "$sellableItems sellable" else "$missingImages missing images",
                    tone = if (missingImages == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                    onClick = onOpenProducts ?: onProduct,
                )
                DashboardReadinessRow(
                    label = "Customers",
                    value = if (reachableCustomers == 0) "Needs contacts" else "$reachableCustomers reachable",
                    tone = if (reachableCustomers == 0) OrmaStatusTone.Info else OrmaStatusTone.Success,
                    onClick = onCustomer,
                )
            }
        }
    }
}

@Composable
private fun DashboardHeroSignalPill(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.PremiumCard,
        color = Color.White.copy(alpha = 0.12f),
        contentColor = Color.White,
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardReadinessRow(
    label: String,
    value: String,
    tone: OrmaStatusTone,
    onClick: () -> Unit,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.WorkspacePanel,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = OrmaShapes.Capsule,
                color = colors.content,
                contentColor = colors.content,
            ) {}
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardOperatingSignalCell(
    label: String,
    value: String,
    detail: String,
    tone: OrmaStatusTone,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.heightIn(min = 104.dp),
            shape = OrmaShapes.StandardCell,
            color = colors.container,
            contentColor = colors.content,
            border = BorderStroke(0.8.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.heightIn(min = 104.dp),
            shape = OrmaShapes.StandardCell,
            color = colors.container,
            contentColor = colors.content,
            border = BorderStroke(0.8.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            content()
        }
    }
}

@Composable
private fun DashboardWideKpiGrid(state: OnboardingUiState) {
    val summary = state.dashboard.summary
    OrmaDashboardStatsGrid(
        wide = true,
        metrics = listOf(
            OrmaDashboardMetric(
                label = "Paid collected",
                value = dashboardMoney(summary.totalPaidAmount, summary.currency),
                detail = if (summary.totalPaidAmount == "0.00") "No payments yet" else "Completed and paid work",
                badge = "Live",
                trend = if (summary.totalPaidAmount == "0.00") "Ready for first payment" else "Revenue tracked",
            ),
            OrmaDashboardMetric(
                label = "Customers",
                value = summary.totalCustomers.toString(),
                detail = if (summary.totalCustomers == 0) "Build customer records" else "Saved customer profiles",
                badge = "Live",
                trend = if (summary.totalCustomers == 0) "No contacts yet" else "CRM active",
            ),
            OrmaDashboardMetric(
                label = "Orders",
                value = summary.ordersCount.toString(),
                detail = if (summary.bookingsCount == 0) "Orders and sales" else "${summary.bookingsCount} scheduled bookings",
                badge = "Live",
                trend = if (summary.ordersCount == 0) "Ready to sell" else "Fulfilment active",
            ),
            OrmaDashboardMetric(
                label = "Low stock",
                value = summary.lowStockProducts.toString(),
                detail = if (summary.lowStockProducts == 0) "Catalog is clear" else "Needs restock",
                tone = if (summary.lowStockProducts > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
                badge = if (summary.lowStockProducts > 0) "Check" else "Clear",
                trend = if (summary.lowStockProducts > 0) "Restock needed" else "Stock healthy",
            ),
        ),
    )
}

@Composable
private fun DashboardWideKpiCard(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
) {
    Surface(
        modifier = modifier.heightIn(min = 126.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaBadge(
                    text = if (warning) "CHECK" else "LIVE",
                    tone = if (warning) OrmaStatusTone.Warning else OrmaStatusTone.Info,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWideRecentWorkCard(state: OnboardingUiState) {
    DashboardWidePanel(
        title = "Recent workspace activity",
        body = "Live orders and stock signals from this business.",
        action = if (state.dashboard.loading) "Syncing" else null,
    ) {
        val recentOrders = state.dashboard.summary.recentOrders.take(5)
        val lowStock = state.dashboard.summary.lowStockItems.take(4)
        when {
            recentOrders.isEmpty() && lowStock.isEmpty() -> {
                DashboardEmptyModuleCard(
                    icon = DashboardNavIconKind.Home,
                    title = "No activity yet",
                    body = "Orders, payments, customers, stock changes, and booking work will appear here after real activity starts.",
                )
            }
            else -> {
                if (recentOrders.isNotEmpty()) {
                    DashboardMiniListHeader(title = "Recent orders")
                    recentOrders.forEach { order ->
                        DashboardInlineOrderLine(order = order)
                    }
                }
                if (recentOrders.isNotEmpty() && lowStock.isNotEmpty()) {
                    HorizontalDivider(color = OrmaColors.Divider)
                }
                if (lowStock.isNotEmpty()) {
                    DashboardMiniListHeader(title = "Low stock")
                    lowStock.forEach { product ->
                        DashboardInlineProductLine(product = product)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardWidePanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OrmaDashboardPanel(
        title = title,
        body = body,
        modifier = modifier,
        action = action,
        content = content,
    )
}

@Composable
private fun DashboardMobileTodayCard(
    state: OnboardingUiState,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
) {
    val summary = state.dashboard.summary
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    val openOrders = state.dashboard.orders.count { it.status in DashboardActiveOrderStatuses }
    val readyOrders = state.dashboard.orders.count { it.status == "paid" }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.DarkTextSecondary,
                    )
                    Text(
                        text = when {
                            openOrders > 0 -> "$openOrders orders need attention"
                            readyOrders > 0 -> "$readyOrders paid orders ready"
                            summary.lowStockProducts > 0 -> "${summary.lowStockProducts} stock alerts"
                            else -> "Ready for business"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = OrmaColors.ScreenBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = if (state.dashboard.loading) "SYNC" else "LIVE",
                    tone = OrmaStatusTone.Success,
                )
            }
            DashboardDarkMetricStrip(
                rows = listOf(
                    "Paid" to dashboardMoney(summary.totalPaidAmount, summary.currency),
                    "Orders" to summary.ordersCount.toString(),
                    "Low" to summary.lowStockProducts.toString(),
                ),
            )
            OrmaActionRow(
                primaryText = if (state.dashboard.actionLoading) "Saving..." else orderType.orderActionText(),
                onPrimary = onOrder,
                primaryEnabled = !state.dashboard.actionLoading,
                secondaryText = "Add customer",
                onSecondary = onCustomer,
            )
            OrmaSecondaryButton(
                text = itemType.catalogActionText(),
                onClick = onProduct,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.dashboard.actionLoading,
            )
        }
    }
}

@Composable
private fun DashboardMobileKpiRail(state: OnboardingUiState) {
    val summary = state.dashboard.summary
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardCompactKpiPill(
                label = "Paid",
                value = dashboardMoney(summary.totalPaidAmount, summary.currency),
                tone = OrmaStatusTone.Success,
                modifier = Modifier.weight(1f),
            )
            DashboardCompactKpiPill(
                label = "Customers",
                value = summary.totalCustomers.toString(),
                tone = OrmaStatusTone.Info,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardCompactKpiPill(
                label = "Stock",
                value = "${summary.productsInStock} in",
                tone = if (summary.lowStockProducts > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                modifier = Modifier.weight(1f),
            )
            DashboardCompactKpiPill(
                label = "Bookings",
                value = summary.bookingsCount.toString(),
                tone = OrmaStatusTone.Info,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardCompactKpiPill(
    label: String,
    value: String,
    tone: OrmaStatusTone,
    modifier: Modifier = Modifier,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = modifier.heightIn(min = 86.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, colors.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWorkQueuePreviewCard(
    state: OnboardingUiState,
    onOpenOrders: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val orderType = state.activeDashboardOrderType()
    val activeOrders = state.dashboard.orders
        .filter { it.status in DashboardActiveOrderStatuses || it.status == "paid" }
        .take(4)
    DashboardRecordCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Live ${orderType.orderProgressPlural()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "${orderType.orderProgressPlural().replaceFirstChar { it.uppercase() }} that need confirmation, payment, preparation, or completion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "${activeOrders.size} ACTIVE",
                tone = if (activeOrders.isEmpty()) OrmaStatusTone.Info else OrmaStatusTone.Warning,
            )
        }
        if (activeOrders.isEmpty()) {
            DashboardChecklistRow(text = "${orderType.orderActionText()} to create the first live task.")
            DashboardChecklistRow(text = "Status changes will keep staff aligned.")
        } else {
            activeOrders.forEach { order ->
                DashboardInlineOrderLine(order = order)
            }
        }
        onOpenOrders?.let {
            OrmaSecondaryButton(
                text = "Open ${orderType.orderProgressPlural()}",
                onClick = it,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DashboardWideCommandBoard(
    state: OnboardingUiState,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
) {
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "Quick actions",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = "Fast actions for orders, customers, and products.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
                OrmaBadge(
                    text = if (state.dashboard.actionLoading) "SAVING" else "READY",
                    tone = OrmaStatusTone.Success,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                DashboardQuickActionTile(
                    title = orderType.orderActionText(),
                    body = orderType.orderSheetBody(),
                    icon = DashboardNavIconKind.Orders,
                    primary = true,
                    enabled = !state.dashboard.actionLoading,
                    onClick = onOrder,
                    modifier = Modifier.weight(1f),
                )
                DashboardQuickActionTile(
                    title = "Add customer",
                    body = "Phone, address, notes for repeat work.",
                    icon = DashboardNavIconKind.Customers,
                    enabled = !state.dashboard.actionLoading,
                    onClick = onCustomer,
                    modifier = Modifier.weight(1f),
                )
                DashboardQuickActionTile(
                    title = itemType.catalogActionText(),
                    body = itemType.catalogSectionDescription(),
                    icon = DashboardNavIconKind.Products,
                    enabled = !state.dashboard.actionLoading,
                    onClick = onProduct,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DashboardQuickActionTile(
    title: String,
    body: String,
    icon: DashboardNavIconKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 112.dp),
        enabled = enabled,
        shape = OrmaShapes.PremiumCard,
        color = if (primary) OrmaColors.Accent else OrmaColors.ScreenBackground,
        contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        border = if (primary) null else BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = OrmaShapes.Capsule,
                color = if (primary) OrmaColors.OnAccent.copy(alpha = 0.12f) else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(
                        kind = icon,
                        color = if (primary) OrmaColors.OnAccent else OrmaColors.IconPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (primary) OrmaColors.DarkTextSecondary else OrmaColors.TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardOperationsCommandCard(
    state: OnboardingUiState,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaBadge(
                text = "OPERATIONS",
                tone = OrmaStatusTone.Success,
            )
            Text(
                text = "Run today's business",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.ScreenBackground,
            )
            Text(
                text = "Capture orders, bookings, payments, fulfilment, customer follow-up, and catalog work from one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.DarkTextSecondary,
            )
            DashboardDarkMetricStrip(
                rows = listOf(
                    "Orders" to state.dashboard.summary.ordersCount.toString(),
                    "Customers" to state.dashboard.summary.totalCustomers.toString(),
                    "Catalog" to state.dashboard.products.size.toString(),
                ),
            )
            OrmaActionRow(
                primaryText = if (state.dashboard.actionLoading) "Saving..." else orderType.orderActionText(),
                onPrimary = onOrder,
                primaryEnabled = !state.dashboard.actionLoading,
                secondaryText = "Add customer",
                onSecondary = onCustomer,
            )
            OrmaSecondaryButton(
                text = itemType.catalogActionText(),
                onClick = onProduct,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.dashboard.actionLoading,
            )
        }
    }
}

@Composable
private fun DashboardDarkMetricStrip(
    rows: List<Pair<String, String>>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = OrmaShapes.StandardCell,
                color = OrmaColors.DarkSubtleBadge,
                contentColor = OrmaColors.OnAccent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.ScreenBackground,
                    )
                    Text(
                        text = row.first,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.DarkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardBusinessSnapshotCard(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val summary = state.dashboard.summary
    val orderType = state.activeDashboardOrderType()
    val itemType = state.activeDashboardItemType()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaBadge(
                text = "BUSINESS",
                tone = OrmaStatusTone.Info,
            )
            Text(
                text = "Business pulse",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = if (state.dashboard.hasLoaded) {
                    "Live operating numbers from this workspace."
                } else if (state.dashboard.loading) {
                    "Syncing workspace records."
                } else {
                    "Pull down to load workspace records."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )
            DashboardMetricLine(
                label = "Paid collected",
                value = dashboardMoney(summary.totalPaidAmount, summary.currency),
                detail = if (summary.totalPaidAmount == "0.00") "No collected payments yet" else "Across completed and paid work",
            )
            DashboardMetricLine(
                label = "Customers",
                value = summary.totalCustomers.toString(),
                detail = if (summary.totalCustomers == 0) "Add contacts for repeat business" else "Saved for orders and follow-up",
            )
            DashboardMetricLine(
                label = orderType.orderProgressPlural().replaceFirstChar { it.uppercase() },
                value = summary.ordersCount.toString(),
                detail = when (orderType) {
                    "appointment" -> if (summary.todayAppointmentsCount == 0) "No appointments today" else "${summary.todayAppointmentsCount} today"
                    "service" -> "${summary.serviceOrdersCount} service requests"
                    else -> if (summary.bookingsCount == 0) "No scheduled fulfilment" else "${summary.bookingsCount} scheduled"
                },
            )
            DashboardMetricLine(
                label = itemType.catalogSectionTitle(),
                value = summary.productsInStock.toString(),
                detail = if (itemType == "product") {
                    if (summary.lowStockProducts == 0) "No low-stock alerts" else "${summary.lowStockProducts} low-stock alerts"
                } else {
                    itemType.catalogSectionDescription()
                },
            )
            if (summary.recentOrders.isNotEmpty()) {
                HorizontalDivider(color = OrmaColors.Divider)
                DashboardMiniListHeader(title = "Recent orders")
                summary.recentOrders.take(3).forEach { order ->
                    DashboardInlineOrderLine(order = order)
                }
            }
            if (summary.lowStockItems.isNotEmpty()) {
                HorizontalDivider(color = OrmaColors.Divider)
                DashboardMiniListHeader(title = "Low stock")
                summary.lowStockItems.take(3).forEach { product ->
                    DashboardInlineProductLine(product = product)
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricLine(
    label: String,
    value: String,
    detail: String,
) {
    OrmaDashboardMetricLine(
        label = label,
        value = value,
        detail = detail,
    )
}

@Composable
private fun DashboardFulfillmentQueueCard(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val orders = state.dashboard.orders
    val orderType = state.activeDashboardOrderType()
    val openCount = orders.count { it.status in setOf("draft", "confirmed", "part_paid") }
    val paidCount = orders.count { it.status == "paid" }
    val completedCount = orders.count { it.status == "completed" }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaBadge(text = "FULFILMENT", tone = OrmaStatusTone.Warning)
            Text(
                text = "${orderType.orderTypeLabel()} queue",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Use this to know what should be prepared, served, delivered, scheduled, or closed.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )
            DashboardMetricLine(
                label = "Open work",
                value = openCount.toString(),
                detail = "Draft, confirmed, or partly paid",
            )
            DashboardMetricLine(
                label = "Ready to close",
                value = paidCount.toString(),
                detail = "Paid work waiting for completion or dispatch",
            )
            DashboardMetricLine(
                label = "Completed",
                value = completedCount.toString(),
                detail = "Fulfilled ${orderType.orderProgressPlural()}",
            )
            if (orders.isEmpty()) {
                DashboardChecklistRow(text = "${orderType.orderActionText()} to start the live queue.")
                DashboardChecklistRow(text = "Move status as work is confirmed, paid, and completed.")
            } else {
                HorizontalDivider(color = OrmaColors.Divider)
                DashboardMiniListHeader(title = "Needs attention")
                orders
                    .filter { it.status in setOf("draft", "confirmed", "part_paid", "paid") }
                    .take(3)
                    .forEach { order ->
                        DashboardInlineOrderLine(order = order)
                    }
            }
        }
    }
}

@Composable
private fun DashboardEngagementCard(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val customers = state.dashboard.customers
    val reachableCustomers = customers.count {
        !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank()
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaBadge(text = "ENGAGE", tone = OrmaStatusTone.Success)
            Text(
                text = "Customer reach",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Build a customer list that can support repeat orders, reminders, and service follow-up.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )
            DashboardMetricLine(
                label = "Saved customers",
                value = customers.size.toString(),
                detail = if (customers.isEmpty()) "No contacts saved yet" else "Available for order selection",
            )
            DashboardMetricLine(
                label = "Reachable",
                value = reachableCustomers.toString(),
                detail = "Customers with phone or email",
            )
            if (customers.isEmpty()) {
                DashboardChecklistRow(text = "Add a customer before repeat order workflows.")
                DashboardChecklistRow(text = "Save phone or email for digital follow-up.")
            } else {
                HorizontalDivider(color = OrmaColors.Divider)
                DashboardMiniListHeader(title = "Recent customers")
                customers.take(3).forEach { customer ->
                    DashboardInlineCustomerLine(customer = customer)
                }
            }
        }
    }
}

@Composable
private fun DashboardOrdersContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
    mobileSelectedOrderId: String? = null,
    onMobileSelectedOrderChange: ((String?) -> Unit)? = null,
) {
    var showOrderSheet by rememberSaveable { mutableStateOf(false) }
    var createOrderTypeOverride by rememberSaveable { mutableStateOf<String?>(null) }
    var editOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var fullDetailsOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var partPaymentOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val visibleOrders = filteredDashboardOrders(state)
    val activeSelectedOrderId = if (!wide && onMobileSelectedOrderChange != null) {
        mobileSelectedOrderId
    } else {
        selectedOrderId
    }
    val selectedOrder = visibleOrders.firstOrNull { it.id == activeSelectedOrderId }
        ?: state.dashboard.orders.firstOrNull { it.id == activeSelectedOrderId }
    val fullDetailsOrder = visibleOrders.firstOrNull { it.id == fullDetailsOrderId }
        ?: state.dashboard.orders.firstOrNull { it.id == fullDetailsOrderId }
    val editOrder = state.dashboard.orders.firstOrNull { it.id == editOrderId }
    val partPaymentOrder = state.dashboard.orders.firstOrNull { it.id == partPaymentOrderId }
    val orderType = state.activeDashboardOrderType()
    val selectedOrderType = state.selectedDashboardOrderTypeFilter()
    val requestOrderStatusChange: (OrmaOrder, String) -> Unit = { order, status ->
        if (status == "part_paid") {
            partPaymentOrderId = order.id
        } else {
            actions.onUpdateOrderStatus(order.id, status)
        }
    }
    if (activeSelectedOrderId != null && selectedOrder == null) {
        if (!wide && onMobileSelectedOrderChange != null) {
            onMobileSelectedOrderChange(null)
        } else {
            selectedOrderId = null
        }
    }
    if (fullDetailsOrderId != null && fullDetailsOrder == null) {
        fullDetailsOrderId = null
    }
    if (partPaymentOrderId != null && partPaymentOrder == null) {
        partPaymentOrderId = null
    }
    if (selectedOrder != null && !wide) {
        DashboardBookingDetailsScreen(
            state = state,
            order = selectedOrder,
            wide = wide,
            onBack = { onMobileSelectedOrderChange?.invoke(null) ?: run { selectedOrderId = null } },
            onEdit = { editOrderId = selectedOrder.id },
            onStatusChange = { status -> requestOrderStatusChange(selectedOrder, status) },
            onUpdateOrder = { draft -> actions.onUpdateOrder(selectedOrder.id, draft) },
            onDownloadStatus = actions.onShowDashboardStatusMessage,
            actionLoading = state.dashboard.actionLoading,
        )
        if (editOrder != null) {
            OrderFormSheet(
                state = state,
                actions = actions,
                initialOrder = editOrder,
                wide = wide,
                onDismiss = { editOrderId = null },
                onSubmit = { draft ->
                    actions.onUpdateOrder(editOrder.id, draft)
                    editOrderId = null
                },
            )
        }
        return
    }
    if (wide) {
            DashboardSalesCounterWorkspace(
            state = state,
            actions = actions,
            selectedOrder = selectedOrder,
            onOpenOrder = {
                selectedOrderId = null
                fullDetailsOrderId = it.id
            },
            onClearSelection = { selectedOrderId = null },
            onEditOrder = { editOrderId = it.id },
            onOpenFullDetails = { fullDetailsOrderId = it.id },
            onStatusChange = requestOrderStatusChange,
            onCreateOrder = { orderType ->
                createOrderTypeOverride = orderType?.takeIf { it in DashboardCounterCreateOrderTypes }
                showOrderSheet = true
            },
        )
    } else {
        DashboardMobileSalesWorkspace(
            state = state,
            actions = actions,
            visibleOrders = visibleOrders,
            stageOrders = filteredDashboardOrders(state, ignoreStatus = true),
            onCreateOrder = { orderTypeOverride ->
                createOrderTypeOverride = orderTypeOverride?.takeIf { it in DashboardCounterCreateOrderTypes }
                    ?: state.activeDashboardOrderType()
                showOrderSheet = true
            },
            onOpenOrder = { order -> onMobileSelectedOrderChange?.invoke(order.id) ?: run { selectedOrderId = order.id } },
            onStatusChange = requestOrderStatusChange,
        )
    }

    if (showOrderSheet) {
        OrderFormSheet(
            state = state,
            actions = actions,
            initialOrderType = createOrderTypeOverride,
            wide = wide,
            onDismiss = {
                showOrderSheet = false
                createOrderTypeOverride = null
            },
            onSubmit = { draft ->
                actions.onCreateOrder(draft)
                showOrderSheet = false
                createOrderTypeOverride = null
            },
        )
    }
    if (editOrder != null) {
        OrderFormSheet(
            state = state,
            actions = actions,
            initialOrder = editOrder,
            wide = wide,
            onDismiss = { editOrderId = null },
            onSubmit = { draft ->
                actions.onUpdateOrder(editOrder.id, draft)
                editOrderId = null
            },
        )
    }
    if (fullDetailsOrder != null) {
        DashboardBookingDetailsSheet(
            state = state,
            order = fullDetailsOrder,
            wide = wide,
            onDismiss = { fullDetailsOrderId = null },
            onEdit = {
                editOrderId = fullDetailsOrder.id
                fullDetailsOrderId = null
            },
            onStatusChange = { status -> requestOrderStatusChange(fullDetailsOrder, status) },
            onUpdateOrder = { draft -> actions.onUpdateOrder(fullDetailsOrder.id, draft) },
            onDownloadStatus = actions.onShowDashboardStatusMessage,
            actionLoading = state.dashboard.actionLoading,
        )
    }
    if (partPaymentOrder != null) {
        DashboardPartPaidAmountSheet(
            order = partPaymentOrder,
            wide = wide,
            actionLoading = state.dashboard.actionLoading,
            onDismiss = { partPaymentOrderId = null },
            onSubmit = { paidAmount ->
                actions.onUpdateOrderStatusWithPayment(partPaymentOrder.id, "part_paid", paidAmount)
                partPaymentOrderId = null
            },
        )
    }
}

@Composable
private fun DashboardMobileSalesWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    visibleOrders: List<OrmaOrder>,
    stageOrders: List<OrmaOrder>,
    onCreateOrder: (String?) -> Unit,
    onOpenOrder: (OrmaOrder) -> Unit,
    onStatusChange: (OrmaOrder, String) -> Unit,
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var statusOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val statusOrder = state.dashboard.orders.firstOrNull { it.id == statusOrderId }
    val currency = state.dashboard.summary.currency.ifBlank {
        stageOrders.firstOrNull()?.currency ?: "INR"
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DashboardMobileSalesSummaryCard(
            state = state,
            orders = stageOrders,
            currency = currency,
            onCreateOrder = onCreateOrder,
        )
        DashboardMobileSalesSearchCard(
            state = state,
            actions = actions,
            visibleCount = visibleOrders.size,
            totalCount = state.dashboard.orderPagination.totalItems.coerceAtLeast(state.dashboard.orders.size),
            onShowFilters = { showFilterSheet = true },
            onResetFilters = { clearDashboardWorkspaceFilters(state, actions) },
        )
        DashboardSalesStageTabs(
            state = state,
            actions = actions,
            orders = stageOrders,
            onClearSelection = {},
        )
        if (state.dashboard.loading && visibleOrders.isNotEmpty()) {
            DashboardSalesInlineLoadingState()
        }
        when {
            state.dashboard.loading && visibleOrders.isEmpty() -> DashboardMobileSalesLoadingList()
            !state.dashboard.errorMessage.isNullOrBlank() && visibleOrders.isEmpty() -> DashboardSalesErrorTable(
                title = state.dashboard.errorTitle ?: "Could not load sales",
                body = state.dashboard.errorMessage.orEmpty(),
                onRetry = actions.onDashboardRefresh,
            )
            visibleOrders.isEmpty() -> DashboardSalesEmptyTable(
                state = state,
                onCreateOrder = { onCreateOrder(state.activeDashboardOrderType()) },
                onResetFilters = { clearDashboardWorkspaceFilters(state, actions) },
            )
            else -> {
                visibleOrders.forEach { order ->
                    DashboardMobileSaleCard(
                        order = order,
                        statusEnabled = !state.dashboard.actionLoading && !state.dashboard.loading,
                        onOpen = { onOpenOrder(order) },
                        onStatusClick = { statusOrderId = order.id },
                    )
                }
                DashboardPaginationControls(
                    pagination = state.dashboard.orderPagination,
                    wide = false,
                    loading = state.dashboard.loading,
                    onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Orders, it) },
                )
            }
        }
    }

    if (showFilterSheet) {
        DashboardMobileSalesFilterSheet(
            state = state,
            actions = actions,
            stageOrders = stageOrders,
            onDismiss = { showFilterSheet = false },
            onResetFilters = { clearDashboardWorkspaceFilters(state, actions) },
        )
    }
    statusOrder?.let { order ->
        DashboardMobileOrderStatusSheet(
            order = order,
            enabled = !state.dashboard.actionLoading && !state.dashboard.loading,
            onDismiss = { statusOrderId = null },
            onStatusChange = { status ->
                onStatusChange(order, status)
                statusOrderId = null
            },
        )
    }
}

@Composable
private fun DashboardMobileSalesSummaryCard(
    state: OnboardingUiState,
    orders: List<OrmaOrder>,
    currency: String,
    onCreateOrder: (String?) -> Unit,
) {
    val openCount = orders.count { it.status in DashboardActiveOrderStatuses }
    val collected = orders.sumOf { it.paidTotal.toDoubleOrNull().orZero() }
    val outstanding = orders.sumOf { it.balanceDueValue() }
    val orderTypes = state.allowedDashboardOrderTypes()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "TODAY'S QUEUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.DarkTextSecondary,
                    )
                    Text(
                        text = when {
                            openCount > 0 -> "$openCount orders need attention"
                            outstanding > 0.0 -> "${dashboardMoney(outstanding.toDashboardMoneyInput(), currency)} to collect"
                            orders.isNotEmpty() -> "${orders.size} orders in view"
                            else -> "Ready for first sale"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = OrmaColors.ScreenBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = if (state.dashboard.loading) "SYNC" else "LIVE",
                    tone = OrmaStatusTone.Success,
                )
            }
            DashboardDarkMetricStrip(
                rows = listOf(
                    "Collected" to dashboardMoney(collected.toDashboardMoneyInput(), currency),
                    "Due" to dashboardMoney(outstanding.toDashboardMoneyInput(), currency),
                    "Open" to openCount.toString(),
                ),
            )
            DashboardSalesCreateActionRow(
                orderTypes = orderTypes,
                activeOrderType = state.activeDashboardOrderType(),
                enabled = !state.dashboard.loading && !state.dashboard.actionLoading,
                compact = true,
                modifier = Modifier.fillMaxWidth(),
                onCreate = { onCreateOrder(it) },
            )
        }
    }
}

@Composable
private fun DashboardMobileSalesSearchCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
    visibleCount: Int,
    totalCount: Int,
    onShowFilters: () -> Unit,
    onResetFilters: () -> Unit,
) {
    val filters = state.dashboard.filters
    val filtersActive = state.hasActiveDashboardFilter()
    val summary = dashboardSalesFilterSummary(state)
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Find orders",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (filtersActive) {
                        "$visibleCount matching ${if (totalCount > 0) "of $totalCount" else "records"}"
                    } else {
                        "$visibleCount orders in the current queue"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (filtersActive) "FILTERED" else "ALL",
                tone = if (filtersActive) OrmaStatusTone.Warning else OrmaStatusTone.Info,
            )
        }
        DashboardCompactSearchField(
            value = filters.query,
            onValueChange = actions.onDashboardSearchChange,
            placeholder = when {
                state.selectedDashboardOrderTypeFilter() == "all" -> "Order, customer, payment, status"
                state.activeDashboardOrderType() == "service" -> "Service, customer, payment"
                state.activeDashboardOrderType() == "appointment" -> "Appointment, customer, date"
                else -> "Sale, customer, payment"
            },
            modifier = Modifier.fillMaxWidth(),
            shape = OrmaShapes.Field,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaSecondaryButton(
                text = "Filters",
                onClick = onShowFilters,
                modifier = Modifier.weight(1f),
            )
            OrmaSecondaryButton(
                text = if (filtersActive) "Reset" else if (state.dashboard.loading) "Syncing" else "Refresh",
                onClick = if (filtersActive) onResetFilters else actions.onDashboardRefresh,
                modifier = Modifier.weight(1f),
                enabled = filtersActive || !state.dashboard.loading,
            )
        }
        summary?.let {
            DashboardSalesActiveFilterSummary(text = it)
        }
    }
}

@Composable
private fun DashboardMobileSalesLoadingList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(OrmaShapes.SmallCard)
                    .background(OrmaColors.SkeletonBase.copy(alpha = 0.72f)),
            )
        }
    }
}

@Composable
private fun DashboardMobileSaleCard(
    order: OrmaOrder,
    statusEnabled: Boolean,
    onOpen: () -> Unit,
    onStatusClick: () -> Unit,
) {
    val itemSummary = order.items
        .mapNotNull { it.productName ?: it.description.takeIf { description -> description.isNotBlank() } }
        .take(2)
        .joinToString(", ")
        .ifBlank { "${order.itemCount.coerceAtLeast(order.items.size)} item${if (order.itemCount == 1) "" else "s"}" }
    DashboardRecordCard(
        modifier = Modifier.clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = order.orderNumber.ifBlank { order.id.take(8).uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = order.customerName ?: "Walk-in customer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = itemSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = order.status.dashboardStatusLabel().uppercase(),
                tone = order.status.dashboardOrderStatusTone(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Total",
                value = dashboardMoney(order.total, order.currency),
                detail = order.orderType.orderTypeLabel(),
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Info,
            )
            DashboardMiniMetricCell(
                label = if (order.balanceDueValue() > 0.0) "Due" else "Paid",
                value = if (order.balanceDueValue() > 0.0) order.balanceDueText() else dashboardMoney(order.paidTotal, order.currency),
                detail = order.paymentMode.paymentModeLabel(),
                modifier = Modifier.weight(1f),
                tone = if (order.balanceDueValue() > 0.0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            )
        }
        order.scheduledAt?.takeIf { it.isNotBlank() }?.let {
            DashboardChecklistRow(text = "Scheduled: ${it.dashboardDateLabel()}")
        }
        OrmaActionRow(
            primaryText = "Open details",
            onPrimary = onOpen,
            secondaryText = "Status",
            onSecondary = onStatusClick,
            primaryEnabled = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!statusEnabled) {
            Text(
                text = "Status updates are temporarily disabled while ORMA is syncing.",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardMobileSalesFilterSheet(
    state: OnboardingUiState,
    actions: OnboardingActions,
    stageOrders: List<OrmaOrder>,
    onDismiss: () -> Unit,
    onResetFilters: () -> Unit,
) {
    DashboardFormSheet(
        title = "Filter sales",
        body = "Narrow the mobile queue by flow, status, and date.",
        onDismiss = onDismiss,
    ) {
        val closeSheet = LocalSmoothSheetDismiss.current ?: onDismiss
        val orderTypeOptions = state.dashboardOrderTypeFilterOptions()
        if (orderTypeOptions.size > 1) {
            DashboardCompactSegmentedPicker(
                options = orderTypeOptions,
                selected = state.selectedDashboardOrderTypeFilter(),
                label = { if (it == "all") "All flows" else it.orderTypeLabel() },
                onSelected = actions.onOrderTypeFilterChange,
            )
        }
        DashboardSalesStageTabs(
            state = state,
            actions = actions,
            orders = stageOrders,
            onClearSelection = {},
        )
        DashboardSalesQuickDateFilters(
            filters = state.dashboard.filters,
            actions = actions,
            onClearSelection = {},
        )
        DashboardSalesDateRangeControls(
            filters = state.dashboard.filters,
            actions = actions,
            onClearSelection = {},
            compact = true,
        )
        OrmaActionRow(
            primaryText = if (state.dashboard.loading) "Syncing" else "Apply",
            onPrimary = {
                actions.onDashboardRefresh()
                closeSheet()
            },
            primaryEnabled = !state.dashboard.loading,
            secondaryText = "Reset",
            onSecondary = {
                onResetFilters()
                closeSheet()
            },
        )
    }
}

@Composable
private fun DashboardMobileOrderStatusSheet(
    order: OrmaOrder,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    val nextStatuses = order.dashboardNextStatuses()
    DashboardFormSheet(
        title = "Update status",
        body = "${order.orderNumber.ifBlank { order.id.take(8).uppercase() }} / ${order.customerName ?: "Walk-in customer"}",
        onDismiss = onDismiss,
    ) {
        val closeSheet = LocalSmoothSheetDismiss.current ?: onDismiss
        OrmaKeyValueList(
            rows = listOf(
                "Current status" to order.status.dashboardStatusLabel(),
                "Payment" to if (order.balanceDueValue() > 0.0) "Due ${order.balanceDueText()}" else "Paid",
                "Total" to dashboardMoney(order.total, order.currency),
            ),
        )
        if (nextStatuses.isEmpty()) {
            DashboardChecklistRow(text = order.status.dashboardTerminalStatusCopy())
        } else {
            nextStatuses.forEach { status ->
                DashboardOrderStatusOption(
                    status = status,
                    onClick = {
                        if (enabled) {
                            onStatusChange(status)
                        }
                    },
                )
            }
        }
        OrmaSecondaryButton(
            text = "Close",
            onClick = closeSheet,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DashboardSalesCounterWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    selectedOrder: OrmaOrder?,
    onOpenOrder: (OrmaOrder) -> Unit,
    onClearSelection: () -> Unit,
    onEditOrder: (OrmaOrder) -> Unit,
    onOpenFullDetails: (OrmaOrder) -> Unit,
    onStatusChange: (OrmaOrder, String) -> Unit,
    onCreateOrder: (String?) -> Unit,
) {
    val visibleOrders = filteredDashboardOrders(state)
    val stageOrders = filteredDashboardOrders(state, ignoreStatus = true)
    OrmaDashboardResponsiveWorkspace(
        wide = true,
        primaryWeight = 1.72f,
        secondaryMinWidth = 340.dp,
        secondaryMaxWidth = 420.dp,
        stackBelowWidth = 1180.dp,
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardSalesFocusToolbar(
                    state = state,
                    actions = actions,
                    orders = stageOrders,
                    currency = state.dashboard.summary.currency.ifBlank {
                        stageOrders.firstOrNull()?.currency ?: "INR"
                    },
                    createEnabled = !state.dashboard.loading,
                    onClearSelection = onClearSelection,
                    onCreate = onCreateOrder,
                )
                DashboardSalesRecordsSurface(
                    state = state,
                    actions = actions,
                    orders = visibleOrders,
                    stageOrders = stageOrders,
                    selectedOrder = selectedOrder,
                    onOpenOrder = onOpenOrder,
                    onEditOrder = onEditOrder,
                    onClearSelection = onClearSelection,
                    onCreateOrder = { onCreateOrder(null) },
                    onStatusChange = onStatusChange,
                )
            }
        },
        secondary = {
            if (selectedOrder != null) {
                DashboardSaleInspectorPanel(
                    order = selectedOrder,
                    onClose = onClearSelection,
                    onEdit = { onEditOrder(selectedOrder) },
                    onOpenFullDetails = { onOpenFullDetails(selectedOrder) },
                    onStatusChange = { status -> onStatusChange(selectedOrder, status) },
                )
            } else {
                DashboardSalesFocusPanel(
                    state = state,
                    orders = stageOrders,
                )
            }
        },
    )
}

@Composable
private fun DashboardSalesKpiStrip(
    orders: List<OrmaOrder>,
    currency: String,
) {
    val openCount = orders.count { it.status in DashboardActiveOrderStatuses }
    val paidCount = orders.count { it.status == "paid" }
    val collected = orders.sumOf { it.paidTotal.toDoubleOrNull().orZero() }
    val outstanding = orders.sumOf { it.balanceDueValue() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        DashboardSalesMetricCard(
            label = "Records",
            value = orders.size.toString(),
            detail = "after filters",
            modifier = Modifier.weight(1f),
            tone = OrmaStatusTone.Info,
        )
        DashboardSalesMetricCard(
            label = "Collected",
            value = dashboardMoney(collected.toDashboardMoneyInput(), currency),
            detail = "paid amount",
            modifier = Modifier.weight(1.32f),
            tone = OrmaStatusTone.Success,
        )
        DashboardSalesMetricCard(
            label = "Outstanding",
            value = dashboardMoney(outstanding.toDashboardMoneyInput(), currency),
            detail = if (outstanding > 0.0) "to collect" else "settled",
            modifier = Modifier.weight(1.32f),
            tone = if (outstanding > 0.0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
        )
        DashboardSalesMetricCard(
            label = "Open",
            value = openCount.toString(),
            detail = "needs follow-up",
            modifier = Modifier.weight(1f),
            tone = if (openCount > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
        )
        DashboardSalesMetricCard(
            label = "Ready",
            value = paidCount.toString(),
            detail = "paid, not closed",
            modifier = Modifier.weight(1f),
            tone = OrmaStatusTone.Success,
        )
    }
}

@Composable
private fun DashboardSalesFocusToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    orders: List<OrmaOrder>,
    currency: String,
    createEnabled: Boolean,
    onClearSelection: () -> Unit,
    onCreate: (String?) -> Unit,
) {
    val filters = state.dashboard.filters
    val filtersActive = state.hasActiveDashboardFilter()
    val orderTypeOptions = state.dashboardOrderTypeFilterOptions()
    val createOrderTypes = state.allowedDashboardOrderTypes()
    val activeSummary = dashboardSalesFilterSummary(state)
    val clearFilters: () -> Unit = {
        onClearSelection()
        clearDashboardWorkspaceFilters(state, actions)
    }
    val searchPlaceholder = when {
        state.selectedDashboardOrderTypeFilter() == "all" -> "Search order, customer, payment, status..."
        state.activeDashboardOrderType() == "service" -> "Search service, customer, payment, status..."
        state.activeDashboardOrderType() == "appointment" -> "Search appointment, customer, date, payment..."
        else -> "Search sale, customer, payment, status..."
    }
    val searchField: @Composable (Modifier) -> Unit = { modifier ->
        DashboardCompactSearchField(
            value = filters.query,
            onValueChange = {
                onClearSelection()
                actions.onDashboardSearchChange(it)
            },
            placeholder = searchPlaceholder,
            modifier = modifier,
            shape = OrmaShapes.Field,
        )
    }
    val typeFilters: @Composable () -> Unit = {
        if (orderTypeOptions.size > 1) {
            DashboardCompactSegmentedPicker(
                options = orderTypeOptions,
                selected = state.selectedDashboardOrderTypeFilter(),
                label = { if (it == "all") "All flows" else it.orderTypeLabel() },
                onSelected = {
                    onClearSelection()
                    actions.onOrderTypeFilterChange(it)
                },
            )
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 820.dp
                val titleBlock: @Composable (Modifier) -> Unit = { modifier ->
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Sale focus",
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Search first, review the queue, then narrow by date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                val createActions: @Composable (Modifier) -> Unit = { modifier ->
                    DashboardSalesCreateActionRow(
                        orderTypes = createOrderTypes,
                        activeOrderType = state.activeDashboardOrderType(),
                        enabled = createEnabled,
                        compact = compact,
                        modifier = modifier,
                        onCreate = { orderType ->
                            onClearSelection()
                            onCreate(orderType)
                        },
                    )
                }
                val statusActions: @Composable () -> Unit = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (filtersActive) {
                            DashboardSalesInlineResetButton(onClick = clearFilters)
                        }
                        OrmaBadge(
                            text = if (state.dashboard.loading) "SYNC" else "LIVE",
                            tone = OrmaStatusTone.Info,
                        )
                    }
                }
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            titleBlock(Modifier.weight(1f))
                            statusActions()
                        }
                        createActions(Modifier.fillMaxWidth())
                        searchField(Modifier.fillMaxWidth())
                        DashboardSalesFocusMetricStrip(
                            orders = orders,
                            currency = currency,
                            compact = true,
                        )
                        typeFilters()
                        DashboardSalesQuickDateFilters(
                            filters = filters,
                            actions = actions,
                            onClearSelection = onClearSelection,
                        )
                        DashboardSalesDateRangeControls(
                            filters = filters,
                            actions = actions,
                            onClearSelection = onClearSelection,
                            compact = true,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            titleBlock(Modifier.weight(1f))
                            createActions(Modifier.widthIn(min = 176.dp, max = 220.dp))
                            statusActions()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            searchField(Modifier.fillMaxWidth())
                        }
                        DashboardSalesFocusMetricStrip(
                            orders = orders,
                            currency = currency,
                            compact = false,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column(
                                modifier = Modifier.weight(0.86f),
                                verticalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                Text(
                                    text = "DATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrmaColors.TextSecondary,
                                    maxLines = 1,
                                )
                                DashboardSalesQuickDateFilters(
                                    filters = filters,
                                    actions = actions,
                                    onClearSelection = onClearSelection,
                                )
                            }
                            DashboardSalesDateRangeControls(
                                filters = filters,
                                actions = actions,
                                onClearSelection = onClearSelection,
                                compact = false,
                                modifier = Modifier.weight(1.14f),
                            )
                        }
                        typeFilters()
                    }
                }
            }
            activeSummary?.let {
                DashboardSalesActiveFilterSummary(text = it)
            }
        }
    }
}

@Composable
private fun DashboardSalesInlineResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(32.dp)
            .widthIn(min = 78.dp),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaFlatIcon(
                kind = OrmaFlatIconKind.Close,
                modifier = Modifier.size(13.dp),
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Reset",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardSalesCreateActionRow(
    orderTypes: List<String>,
    activeOrderType: String,
    enabled: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onCreate: (String) -> Unit,
) {
    if (orderTypes.isEmpty()) return
    if (orderTypes.size == 1) {
        val orderType = orderTypes.first()
        DashboardWideActionButton(
            text = orderType.orderActionText(),
            onClick = { onCreate(orderType) },
            modifier = if (compact) modifier.fillMaxWidth() else modifier.width(188.dp),
            primary = true,
            enabled = enabled,
        )
        return
    }
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = if (compact) Alignment.Start else Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        orderTypes.forEach { orderType ->
            DashboardWideActionButton(
                text = orderType.orderActionText(),
                onClick = { onCreate(orderType) },
                primary = orderType == activeOrderType || orderTypes.size == 1,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun DashboardSalesFocusMetricStrip(
    orders: List<OrmaOrder>,
    currency: String,
    compact: Boolean,
) {
    val openCount = orders.count { it.status in DashboardActiveOrderStatuses }
    val paidCount = orders.count { it.status == "paid" }
    val completedCount = orders.count { it.status == "completed" }
    val collected = orders.sumOf { it.paidTotal.toDoubleOrNull().orZero() }
    val outstanding = orders.sumOf { it.balanceDueValue() }
    val metrics = listOf(
        DashboardSalesFocusMetric(
            label = "Collected",
            value = dashboardMoney(collected.toDashboardMoneyInput(), currency),
            detail = "paid amount",
            tone = OrmaStatusTone.Success,
            weight = 1.25f,
        ),
        DashboardSalesFocusMetric(
            label = "Outstanding",
            value = dashboardMoney(outstanding.toDashboardMoneyInput(), currency),
            detail = if (outstanding > 0.0) "to collect" else "settled",
            tone = if (outstanding > 0.0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            weight = 1.25f,
        ),
        DashboardSalesFocusMetric(
            label = "Needs attention",
            value = openCount.toString(),
            detail = "captured to part paid",
            tone = if (openCount > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
        ),
        DashboardSalesFocusMetric(
            label = "Ready",
            value = paidCount.toString(),
            detail = "paid, not closed",
            tone = OrmaStatusTone.Success,
        ),
        DashboardSalesFocusMetric(
            label = "Closed",
            value = completedCount.toString(),
            detail = "completed",
            tone = OrmaStatusTone.Info,
        ),
    )
    if (compact) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            metrics.forEach { metric ->
                DashboardSalesMetricCard(
                    label = metric.label,
                    value = metric.value,
                    detail = metric.detail,
                    tone = metric.tone,
                    modifier = Modifier.width(if (metric.weight > 1f) 188.dp else 154.dp),
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            metrics.forEach { metric ->
                DashboardSalesMetricCard(
                    label = metric.label,
                    value = metric.value,
                    detail = metric.detail,
                    tone = metric.tone,
                    modifier = Modifier.weight(metric.weight),
                )
            }
        }
    }
}

private data class DashboardSalesFocusMetric(
    val label: String,
    val value: String,
    val detail: String,
    val tone: OrmaStatusTone,
    val weight: Float = 1f,
)

@Composable
private fun DashboardSalesMetricCard(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    tone: OrmaStatusTone,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = modifier.heightIn(min = 78.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class DashboardFocusMetric(
    val label: String,
    val value: String,
    val detail: String,
    val tone: OrmaStatusTone,
    val weight: Float = 1f,
)

@Composable
private fun DashboardFocusMetricStrip(
    metrics: List<DashboardFocusMetric>,
    compact: Boolean = false,
) {
    if (metrics.isEmpty()) return
    if (compact) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            metrics.forEach { metric ->
                DashboardSalesMetricCard(
                    label = metric.label,
                    value = metric.value,
                    detail = metric.detail,
                    tone = metric.tone,
                    modifier = Modifier.width(if (metric.weight > 1f) 188.dp else 154.dp),
                )
            }
        }
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        metrics.forEach { metric ->
            DashboardSalesMetricCard(
                label = metric.label,
                value = metric.value,
                detail = metric.detail,
                tone = metric.tone,
                modifier = Modifier.weight(metric.weight),
            )
        }
    }
}

@Composable
private fun DashboardSalesRecordsSurface(
    state: OnboardingUiState,
    actions: OnboardingActions,
    orders: List<OrmaOrder>,
    stageOrders: List<OrmaOrder>,
    selectedOrder: OrmaOrder?,
    onOpenOrder: (OrmaOrder) -> Unit,
    onEditOrder: (OrmaOrder) -> Unit,
    onClearSelection: () -> Unit,
    onCreateOrder: () -> Unit,
    onStatusChange: (OrmaOrder, String) -> Unit,
) {
    val selectedOrderType = state.selectedDashboardOrderTypeFilter()
    val title = if (selectedOrderType == "all") "Order records" else "${state.activeDashboardOrderType().orderTypeLabel()} records"
    var sortKey by rememberSaveable { mutableStateOf(DashboardSalesSortNone) }
    var sortAscending by rememberSaveable { mutableStateOf(true) }
    val sortedOrders = remember(orders, sortKey, sortAscending) {
        sortedDashboardSalesOrders(
            orders = orders,
            sortKey = sortKey,
            ascending = sortAscending,
        )
    }
    val onSortChange: (String) -> Unit = { nextSortKey ->
        if (sortKey == nextSortKey) {
            sortAscending = !sortAscending
        } else {
            sortKey = nextSortKey
            sortAscending = nextSortKey !in DashboardSalesDescendingFirstSorts
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardSalesRecordsHeader(
                title = title,
                count = orders.size,
                pagination = state.dashboard.orderPagination,
                loading = state.dashboard.loading,
                onPageChange = {
                    onClearSelection()
                    actions.onDashboardPageChange(DashboardPageTarget.Orders, it)
                },
            )
            DashboardSalesStageTabs(
                state = state,
                actions = actions,
                orders = stageOrders,
                onClearSelection = onClearSelection,
            )
            if (state.dashboard.loading && orders.isNotEmpty()) {
                DashboardSalesInlineLoadingState()
            }
            when {
                state.dashboard.loading && orders.isEmpty() -> DashboardSalesLoadingTable()
                !state.dashboard.errorMessage.isNullOrBlank() && orders.isEmpty() -> DashboardSalesErrorTable(
                    title = state.dashboard.errorTitle ?: "Could not load sales",
                    body = state.dashboard.errorMessage.orEmpty(),
                    onRetry = actions.onDashboardRefresh,
                )
                orders.isEmpty() -> DashboardSalesEmptyTable(
                    state = state,
                    onCreateOrder = onCreateOrder,
                    onResetFilters = {
                        onClearSelection()
                        clearDashboardWorkspaceFilters(state, actions)
                    },
                )
                else -> DashboardSalesTable(
                    orders = sortedOrders,
                    selectedOrder = selectedOrder,
                    sortKey = sortKey,
                    sortAscending = sortAscending,
                    statusEnabled = !state.dashboard.actionLoading && !state.dashboard.loading,
                    onSortChange = onSortChange,
                    onOpenOrder = onOpenOrder,
                    onEditOrder = onEditOrder,
                    onStatusChange = onStatusChange,
                )
            }
            if (orders.isNotEmpty()) {
                DashboardPaginationControls(
                    pagination = state.dashboard.orderPagination,
                    wide = true,
                    loading = state.dashboard.loading,
                    onPageChange = {
                        onClearSelection()
                        actions.onDashboardPageChange(DashboardPageTarget.Orders, it)
                    },
                )
            }
        }
    }
}

private const val DashboardSalesSortNone = "none"
private const val DashboardSalesSortOrder = "order"
private const val DashboardSalesSortCustomer = "customer"
private const val DashboardSalesSortItems = "items"
private const val DashboardSalesSortPayment = "payment"
private const val DashboardSalesSortTotal = "total"
private const val DashboardSalesSortPaid = "paid"
private const val DashboardSalesSortStatus = "status"
private const val DashboardSalesSortDate = "date"

private val DashboardSalesDescendingFirstSorts: Set<String> = setOf(
    DashboardSalesSortItems,
    DashboardSalesSortTotal,
    DashboardSalesSortPaid,
    DashboardSalesSortDate,
)

private fun sortedDashboardSalesOrders(
    orders: List<OrmaOrder>,
    sortKey: String,
    ascending: Boolean,
): List<OrmaOrder> {
    val sorted = when (sortKey) {
        DashboardSalesSortOrder -> orders.sortedBy { it.orderNumber.ifBlank { it.id } }
        DashboardSalesSortCustomer -> orders.sortedBy { it.customerName.orEmpty().lowercase() }
        DashboardSalesSortItems -> orders.sortedBy { it.itemCount.coerceAtLeast(it.items.size) }
        DashboardSalesSortPayment -> orders.sortedBy { it.paymentMode.paymentModeLabel() }
        DashboardSalesSortTotal -> orders.sortedBy { it.total.toDoubleOrNull().orZero() }
        DashboardSalesSortPaid -> orders.sortedBy { it.paidTotal.toDoubleOrNull().orZero() }
        DashboardSalesSortStatus -> orders.sortedBy { it.status.dashboardStatusLabel() }
        DashboardSalesSortDate -> orders.sortedBy { it.scheduledAt.orEmpty() }
        else -> orders
    }
    return if (ascending || sortKey == DashboardSalesSortNone) sorted else sorted.asReversed()
}

private const val DashboardCustomerSortName = "customer_name"
private const val DashboardCustomerSortContact = "customer_contact"
private const val DashboardCustomerSortLocation = "customer_location"
private const val DashboardCustomerSortStatus = "customer_status"

private fun sortedDashboardCustomers(
    customers: List<OrmaCustomer>,
    sortKey: String,
    ascending: Boolean,
): List<OrmaCustomer> {
    val sorted = when (sortKey) {
        DashboardCustomerSortContact -> customers.sortedBy {
            listOfNotNull(it.phoneNumber, it.email).joinToString(" / ").lowercase()
        }
        DashboardCustomerSortLocation -> customers.sortedBy {
            listOfNotNull(it.city, it.region, it.country, it.addressLine).joinToString(", ").lowercase()
        }
        DashboardCustomerSortStatus -> customers.sortedBy { it.status.dashboardTeamStatusLabel() }
        else -> customers.sortedBy { it.name.lowercase() }
    }
    return if (ascending) sorted else sorted.asReversed()
}

private const val DashboardProductSortItem = "product_item"
private const val DashboardProductSortType = "product_type"
private const val DashboardProductSortStock = "product_stock"
private const val DashboardProductSortPrice = "product_price"
private val DashboardProductActionColumnWidth = 324.dp

private val DashboardProductDescendingFirstSorts: Set<String> = setOf(
    DashboardProductSortStock,
    DashboardProductSortPrice,
)

private fun sortedDashboardProducts(
    products: List<OrmaProduct>,
    sortKey: String,
    ascending: Boolean,
): List<OrmaProduct> {
    val sorted = when (sortKey) {
        DashboardProductSortType -> products.sortedBy { it.itemType.sellableItemTypeLabel() }
        DashboardProductSortStock -> products.sortedBy { it.stockQuantity.toDoubleOrNull().orZero() }
        DashboardProductSortPrice -> products.sortedBy { it.sellingPrice.toDoubleOrNull().orZero() }
        else -> products.sortedBy { it.name.lowercase() }
    }
    return if (ascending) sorted else sorted.asReversed()
}

private const val DashboardInvoiceSortInvoice = "invoice_number"
private const val DashboardInvoiceSortCustomer = "invoice_customer"
private const val DashboardInvoiceSortTotal = "invoice_total"
private const val DashboardInvoiceSortStatus = "invoice_status"

private val DashboardInvoiceDescendingFirstSorts: Set<String> = setOf(
    DashboardInvoiceSortTotal,
)

private fun sortedDashboardInvoices(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
    sortKey: String,
    ascending: Boolean,
): List<OrmaOrder> {
    val sorted = when (sortKey) {
        DashboardInvoiceSortCustomer -> invoices.sortedBy { it.customerName.orEmpty().lowercase() }
        DashboardInvoiceSortTotal -> invoices.sortedBy { it.total.toDoubleOrNull().orZero() }
        DashboardInvoiceSortStatus -> invoices.sortedBy { it.invoiceStatusLabel() }
        else -> invoices.sortedBy { invoiceNumberFor(state, it) }
    }
    return if (ascending) sorted else sorted.asReversed()
}

private const val DashboardTeamSortMember = "team_member"
private const val DashboardTeamSortContact = "team_contact"
private const val DashboardTeamSortRole = "team_role"
private const val DashboardTeamSortJoined = "team_joined"

private val DashboardTeamDescendingFirstSorts: Set<String> = setOf(
    DashboardTeamSortJoined,
)

private fun sortedDashboardTeamMembers(
    members: List<OrmaTeamMember>,
    sortKey: String,
    ascending: Boolean,
): List<OrmaTeamMember> {
    val sorted = when (sortKey) {
        DashboardTeamSortContact -> members.sortedBy {
            listOfNotNull(it.email, it.phoneNumber).joinToString(" / ").lowercase()
        }
        DashboardTeamSortRole -> members.sortedBy { teamRoleLabel(it.role) }
        DashboardTeamSortJoined -> members.sortedBy { it.joinedAt }
        else -> members.sortedBy { it.dashboardDisplayName().lowercase() }
    }
    return if (ascending) sorted else sorted.asReversed()
}

private fun dashboardSalesFilterSummary(state: OnboardingUiState): String? {
    val filters = state.dashboard.filters
    val parts = buildList {
        if (filters.query.isNotBlank()) add("Search \"${filters.query.trim()}\"")
        if (filters.orderStatus != "all") add("Status ${filters.orderStatus.dashboardStatusLabel()}")
        val selectedOrderType = state.selectedDashboardOrderTypeFilter()
        if (selectedOrderType != state.defaultDashboardOrderTypeFilter()) {
            add(if (selectedOrderType == "all") "All flows" else selectedOrderType.orderTypeLabel())
        }
        when {
            filters.dateFrom.isNotBlank() && filters.dateTo.isNotBlank() -> add("${filters.dateFrom} to ${filters.dateTo}")
            filters.dateFrom.isNotBlank() -> add("From ${filters.dateFrom}")
            filters.dateTo.isNotBlank() -> add("Until ${filters.dateTo}")
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" / ")
}

@Composable
private fun DashboardSalesRecordsHeader(
    title: String,
    count: Int,
    pagination: OrmaPagination,
    loading: Boolean,
    onPageChange: (Int) -> Unit,
) {
    val totalItems = pagination.totalItems.coerceAtLeast(count)
    val shownText = if (count > 0 && totalItems > count) {
        "$count SHOWN / $totalItems TOTAL"
    } else {
        "$count SHOWN"
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 620.dp
        val titleBlock: @Composable (Modifier) -> Unit = { modifier ->
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Search, filter, update payment stage, and inspect each sale without leaving the queue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val actionsBlock: @Composable (Modifier) -> Unit = { modifier ->
            Row(
                modifier = modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaBadge(
                    text = shownText,
                    tone = OrmaStatusTone.Info,
                )
                DashboardSalesPaginationMiniControls(
                    pagination = pagination,
                    loading = loading,
                    onPageChange = onPageChange,
                )
            }
        }
        if (compact) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                titleBlock(Modifier.fillMaxWidth())
                actionsBlock(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                titleBlock(Modifier.weight(1f))
                actionsBlock(Modifier)
            }
        }
    }
}

@Composable
private fun DashboardSalesPaginationMiniControls(
    pagination: OrmaPagination,
    loading: Boolean,
    onPageChange: (Int) -> Unit,
) {
    val page = pagination.page.coerceAtLeast(1)
    val totalPages = pagination.totalPages.coerceAtLeast(page)
    if (totalPages <= 1 && !pagination.hasPrevious && !pagination.hasNext) return
    Surface(
        shape = OrmaShapes.Capsule,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardSalesPagerIconButton(
                kind = OrmaFlatIconKind.ChevronLeft,
                enabled = !loading && pagination.hasPrevious,
                onClick = { onPageChange(page - 1) },
            )
            Text(
                text = "Page $page/$totalPages",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            DashboardSalesPagerIconButton(
                kind = OrmaFlatIconKind.ChevronRight,
                enabled = !loading && pagination.hasNext,
                onClick = { onPageChange(page + 1) },
            )
        }
    }
}

@Composable
private fun DashboardSalesPagerIconButton(
    kind: OrmaFlatIconKind,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(26.dp),
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.CardBackground else OrmaColors.Accent.copy(alpha = 0.05f),
        contentColor = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = if (enabled) 0.14f else 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            OrmaFlatIcon(
                kind = kind,
                modifier = Modifier.size(13.dp),
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            )
        }
    }
}

@Composable
private fun DashboardSalesToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    filters: OrmaDashboardFilters,
    createText: String,
    createEnabled: Boolean,
    onClearSelection: () -> Unit,
    onCreate: () -> Unit,
) {
    val filtersActive = state.hasActiveDashboardFilter()
    val orderTypeOptions = state.dashboardOrderTypeFilterOptions()
    val activeSummary = dashboardSalesFilterSummary(state)
    val clearFilters: () -> Unit = {
        onClearSelection()
        clearDashboardWorkspaceFilters(state, actions)
    }
    val searchField: @Composable (Modifier) -> Unit = { modifier ->
        DashboardCompactSearchField(
            value = filters.query,
            onValueChange = {
                onClearSelection()
                actions.onDashboardSearchChange(it)
            },
            placeholder = when {
                state.selectedDashboardOrderTypeFilter() == "all" -> "Search order, customer, payment, status..."
                state.activeDashboardOrderType() == "service" -> "Search service, customer, payment, status..."
                state.activeDashboardOrderType() == "appointment" -> "Search appointment, customer, date, payment..."
                else -> "Search sale, customer, payment, status..."
            },
            modifier = modifier,
            shape = OrmaShapes.Field,
        )
    }
    val typeFilters: @Composable () -> Unit = {
        if (orderTypeOptions.size > 1) {
            DashboardCompactSegmentedPicker(
                options = orderTypeOptions,
                selected = state.selectedDashboardOrderTypeFilter(),
                label = { if (it == "all") "All flows" else it.orderTypeLabel() },
                onSelected = {
                    onClearSelection()
                    actions.onOrderTypeFilterChange(it)
                },
            )
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 560.dp
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        searchField(Modifier.fillMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DashboardWideActionButton(
                                text = if (state.dashboard.loading) "Searching" else "Apply",
                                onClick = actions.onDashboardRefresh,
                                enabled = !state.dashboard.loading,
                            )
                            if (filtersActive) {
                                DashboardWideActionButton(
                                    text = "Reset",
                                    onClick = clearFilters,
                                )
                            }
                            DashboardWideActionButton(
                                text = createText,
                                onClick = onCreate,
                                primary = true,
                                enabled = createEnabled,
                            )
                        }
                        DashboardSalesQuickDateFilters(
                            filters = filters,
                            actions = actions,
                            onClearSelection = onClearSelection,
                        )
                        DashboardSalesDateRangeControls(
                            filters = filters,
                            actions = actions,
                            onClearSelection = onClearSelection,
                            compact = true,
                        )
                        typeFilters()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            searchField(Modifier.weight(1.35f))
                            DashboardSalesQuickDateFilters(
                                filters = filters,
                                actions = actions,
                                onClearSelection = onClearSelection,
                                modifier = Modifier.weight(1.12f),
                            )
                            DashboardWideActionButton(
                                text = if (state.dashboard.loading) "Searching" else "Apply",
                                onClick = actions.onDashboardRefresh,
                                enabled = !state.dashboard.loading,
                            )
                            if (filtersActive) {
                                DashboardWideActionButton(
                                    text = "Reset",
                                    onClick = clearFilters,
                                )
                            }
                            DashboardWideActionButton(
                                text = createText,
                                onClick = onCreate,
                                primary = true,
                                enabled = createEnabled,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            DashboardSalesDateRangeControls(
                                filters = filters,
                                actions = actions,
                                onClearSelection = onClearSelection,
                                compact = false,
                                modifier = Modifier.weight(1.12f),
                            )
                            if (orderTypeOptions.size > 1) {
                                Column(
                                    modifier = Modifier.weight(0.88f),
                                    verticalArrangement = Arrangement.spacedBy(7.dp),
                                ) {
                                    Text(
                                        text = "FLOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrmaColors.TextSecondary,
                                        maxLines = 1,
                                    )
                                    typeFilters()
                                }
                            }
                        }
                    }
                }
            }
            activeSummary?.let {
                DashboardSalesActiveFilterSummary(text = it)
            }
        }
    }
}

@Composable
private fun DashboardSalesQuickDateFilters(
    filters: OrmaDashboardFilters,
    actions: OnboardingActions,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardDatePresetFilterChips(
        filters = filters,
        actions = actions,
        includeUpcoming = true,
        onClearSelection = onClearSelection,
        modifier = modifier,
    )
}

@Composable
private fun DashboardSalesDateRangeControls(
    filters: OrmaDashboardFilters,
    actions: OnboardingActions,
    onClearSelection: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaCalendarDateField(
                value = filters.dateFrom,
                onValueChange = {
                    onClearSelection()
                    actions.onDashboardDateFilterChange(it, filters.dateTo)
                },
                label = "From",
                placeholder = "Start date",
                modifier = Modifier.fillMaxWidth(),
            )
            OrmaCalendarDateField(
                value = filters.dateTo,
                onValueChange = {
                    onClearSelection()
                    actions.onDashboardDateFilterChange(filters.dateFrom, it)
                },
                label = "To",
                placeholder = "End date",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            OrmaCalendarDateField(
                value = filters.dateFrom,
                onValueChange = {
                    onClearSelection()
                    actions.onDashboardDateFilterChange(it, filters.dateTo)
                },
                label = "From",
                placeholder = "Start date",
                modifier = Modifier.weight(1f),
            )
            OrmaCalendarDateField(
                value = filters.dateTo,
                onValueChange = {
                    onClearSelection()
                    actions.onDashboardDateFilterChange(filters.dateFrom, it)
                },
                label = "To",
                placeholder = "End date",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardSalesActiveFilterSummary(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardSalesStageTabs(
    state: OnboardingUiState,
    actions: OnboardingActions,
    orders: List<OrmaOrder>,
    onClearSelection: () -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardStatusCountChip(
            label = "All",
            count = orders.size,
            selected = state.dashboard.filters.orderStatus == "all",
            tone = OrmaStatusTone.Info,
            onClick = {
                onClearSelection()
                actions.onOrderStatusFilterChange("all")
            },
        )
        DashboardOrderStatuses.forEach { status ->
            DashboardStatusCountChip(
                label = status.dashboardStatusLabel(),
                count = orders.count { it.status == status },
                selected = state.dashboard.filters.orderStatus == status,
                tone = status.dashboardOrderStatusTone(),
                onClick = {
                    onClearSelection()
                    actions.onOrderStatusFilterChange(status)
                },
            )
        }
    }
}

@Composable
private fun DashboardSalesInlineLoadingState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaFlatIcon(
                kind = OrmaFlatIconKind.Refresh,
                modifier = Modifier.size(14.dp),
                color = OrmaColors.TextSecondary,
            )
            Text(
                text = "Updating the sales queue...",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardSalesLoadingTable() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(OrmaShapes.SmallCard)
                    .background(OrmaColors.SkeletonBase.copy(alpha = 0.72f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(6) { column ->
                    Box(
                        modifier = Modifier
                            .weight(if (column == 1) 1.25f else 1f)
                            .height(if (column % 2 == 0) 14.dp else 10.dp)
                            .clip(OrmaShapes.Skeleton)
                            .background(OrmaColors.SkeletonHighlight.copy(alpha = 0.72f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSalesErrorTable(
    title: String,
    body: String,
    onRetry: () -> Unit,
) {
    DashboardSalesStatePanel(
        title = title,
        body = body.ifBlank { "Refresh the workspace and try again." },
        tone = OrmaStatusTone.Danger,
        primaryText = "Retry",
        onPrimary = onRetry,
        iconKind = OrmaFlatIconKind.Refresh,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardSalesTable(
    orders: List<OrmaOrder>,
    selectedOrder: OrmaOrder?,
    sortKey: String,
    sortAscending: Boolean,
    statusEnabled: Boolean,
    onSortChange: (String) -> Unit,
    onOpenOrder: (OrmaOrder) -> Unit,
    onEditOrder: (OrmaOrder) -> Unit,
    onStatusChange: (OrmaOrder, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp),
    ) {
        stickyHeader {
            DashboardSalesTableHeader(
                sortKey = sortKey,
                sortAscending = sortAscending,
                onSortChange = onSortChange,
            )
        }
        itemsIndexed(
            items = orders,
            key = { _, order -> order.id },
        ) { index, order ->
            DashboardSaleRecordRow(
                order = order,
                selected = selectedOrder?.id == order.id,
                zebra = index % 2 == 1,
                statusEnabled = statusEnabled,
                onOpen = { onOpenOrder(order) },
                onEdit = { onEditOrder(order) },
                onStatusChange = { status -> onStatusChange(order, status) },
            )
        }
    }
}

@Composable
private fun DashboardSalesTableHeader(
    sortKey: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardSaleHeaderCell(
                    text = "Order",
                    sortKey = DashboardSalesSortOrder,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1.08f),
                )
                DashboardSaleHeaderCell(
                    text = "Customer",
                    sortKey = DashboardSalesSortCustomer,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1.18f),
                )
                DashboardSaleHeaderCell(
                    text = "Items",
                    sortKey = DashboardSalesSortItems,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(0.72f),
                )
                DashboardSaleHeaderCell(
                    text = "Payment",
                    sortKey = DashboardSalesSortPayment,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1.02f),
                )
                DashboardSaleHeaderCell(
                    text = "Total",
                    sortKey = DashboardSalesSortTotal,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(0.9f),
                )
                DashboardSaleHeaderCell(
                    text = "Status",
                    sortKey = DashboardSalesSortStatus,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1.12f),
                )
                DashboardSaleHeaderCell(
                    text = "Actions",
                    sortKey = null,
                    activeSortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange,
                    modifier = Modifier.width(154.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp),
                thickness = 0.8.dp,
                color = OrmaColors.CellBackground.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun DashboardSaleHeaderCell(
    text: String,
    sortKey: String?,
    activeSortKey: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = sortKey != null && sortKey == activeSortKey
    val cellModifier = if (sortKey == null) {
        modifier
    } else {
        modifier
            .clip(OrmaShapes.SmallCard)
            .clickable { onSortChange(sortKey) }
    }
    Row(
        modifier = cellModifier.padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (active) OrmaColors.TextPrimary else OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (active) {
            Text(
                text = if (sortAscending) "^" else "v",
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DashboardSaleRecordRow(
    order: OrmaOrder,
    selected: Boolean,
    zebra: Boolean,
    statusEnabled: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackground = when {
        selected -> OrmaColors.ScreenBackground.copy(alpha = 0.62f)
        hovered -> OrmaColors.ScreenBackground.copy(alpha = 0.44f)
        zebra -> OrmaColors.ScreenBackground.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OrmaShapes.SmallCard)
                .background(rowBackground)
                .hoverable(interactionSource)
                .clickable(onClick = onOpen)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardWideCell(
                primary = order.orderNumber.ifBlank { order.id.take(8).uppercase() },
                secondary = order.source.dashboardTitleCase(),
                modifier = Modifier.weight(1.08f),
            )
            DashboardWideCell(
                primary = order.customerName ?: "Walk-in customer",
                secondary = order.fulfillmentType.fulfillmentModeLabel(),
                modifier = Modifier.weight(1.18f),
            )
            DashboardWideCell(
                primary = "${order.itemCount.coerceAtLeast(order.items.size)} items",
                secondary = order.orderType.orderTypeLabel(),
                modifier = Modifier.weight(0.72f),
            )
            DashboardWideCell(
                primary = order.paymentMode.paymentModeLabel(),
                secondary = if (order.balanceDueValue() > 0.0) {
                    "Due ${order.balanceDueText()}"
                } else {
                    "Paid ${dashboardMoney(order.paidTotal, order.currency)}"
                },
                modifier = Modifier.weight(1.02f),
            )
            DashboardWideCell(
                primary = dashboardMoney(order.total, order.currency),
                secondary = order.scheduledAt?.dashboardDateLabel(),
                modifier = Modifier.weight(0.9f),
            )
            DashboardOrderStatusCompactControl(
                order = order,
                enabled = statusEnabled,
                onStatusChange = onStatusChange,
                modifier = Modifier.weight(1.12f),
            )
            Row(
                modifier = Modifier.width(154.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardTableActionButton(
                    text = "Open",
                    iconKind = OrmaFlatIconKind.View,
                    onClick = onOpen,
                )
                DashboardTableActionButton(
                    text = "Edit",
                    iconKind = OrmaFlatIconKind.Edit,
                    onClick = onEdit,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp),
            thickness = 0.8.dp,
            color = if (selected || hovered) {
                OrmaColors.CellBackground.copy(alpha = 0.86f)
            } else {
                OrmaColors.CellBackground.copy(alpha = 0.62f)
            },
        )
    }
}

@Composable
private fun DashboardOrderStatusCompactControl(
    order: OrmaOrder,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val currentStatus = order.status.takeIf { it in DashboardOrderStatuses } ?: "confirmed"
    val nextStatuses = order.dashboardNextStatuses()
    var expanded by rememberSaveable(order.id, currentStatus) { mutableStateOf(false) }
    val terminal = nextStatuses.isEmpty()
    val interactive = enabled && !terminal
    val colors = org.orma.project_90.designsystem.ormaStatusColors(currentStatus.dashboardOrderStatusTone())
    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            onClick = { if (interactive) expanded = !expanded },
            enabled = interactive,
            shape = OrmaShapes.Capsule,
            color = if (enabled) colors.container else OrmaColors.Accent.copy(alpha = 0.06f),
            contentColor = if (enabled) colors.content else OrmaColors.TextDisabled,
            border = BorderStroke(0.8.dp, if (enabled) colors.border else OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (enabled) currentStatus.dashboardStatusLabel() else "Updating",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) colors.content else OrmaColors.TextDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (interactive) {
                    OrmaChevronDownIcon(
                        modifier = Modifier.size(14.dp),
                        color = colors.content,
                    )
                }
            }
        }
        AnimatedVisibility(visible = expanded && nextStatuses.isNotEmpty() && enabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                nextStatuses.forEach { status ->
                    DashboardOrderStatusOption(
                        status = status,
                        onClick = {
                            expanded = false
                            onStatusChange(status)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSalesEmptyTable(
    state: OnboardingUiState,
    onCreateOrder: () -> Unit,
    onResetFilters: () -> Unit,
) {
    val filtered = state.hasActiveDashboardFilter()
    val orderType = state.activeDashboardOrderType()
    DashboardSalesStatePanel(
        title = if (filtered) {
            "No records match this view"
        } else {
            orderType.emptyOrderTitle()
        },
        body = if (filtered) {
            "Clear the current search, status, and date filters to bring the queue back."
        } else {
            orderType.emptyOrderBody()
        },
        tone = if (filtered) OrmaStatusTone.Info else OrmaStatusTone.Success,
        primaryText = if (filtered) "Reset filters" else orderType.orderActionText(),
        onPrimary = if (filtered) onResetFilters else onCreateOrder,
        secondaryText = if (filtered) orderType.orderActionText() else null,
        onSecondary = if (filtered) onCreateOrder else null,
        iconKind = if (filtered) OrmaFlatIconKind.Search else OrmaFlatIconKind.Plus,
    )
}

@Composable
private fun DashboardSalesStatePanel(
    title: String,
    body: String,
    tone: OrmaStatusTone,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    iconKind: OrmaFlatIconKind = OrmaFlatIconKind.Search,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.7.dp, colors.border.copy(alpha = 0.42f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 640.dp
            val contentPadding = if (compact) 16.dp else 18.dp
            val iconBubble: @Composable () -> Unit = {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = OrmaShapes.Capsule,
                    color = colors.container.copy(alpha = 0.72f),
                    contentColor = colors.content,
                    border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.28f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        OrmaFlatIcon(
                            kind = iconKind,
                            color = colors.content,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            val textBlock: @Composable (Modifier, TextAlign) -> Unit = { modifier, alignment ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = if (alignment == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        textAlign = alignment,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        textAlign = alignment,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val actions: @Composable (Modifier) -> Unit = { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DashboardWideActionButton(
                        text = primaryText,
                        onClick = onPrimary,
                        primary = true,
                        modifier = if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(min = 144.dp, max = 220.dp),
                    )
                    if (secondaryText != null && onSecondary != null) {
                        DashboardWideActionButton(
                            text = secondaryText,
                            onClick = onSecondary,
                            modifier = if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(min = 144.dp, max = 220.dp),
                        )
                    }
                }
            }

            if (compact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    iconBubble()
                    textBlock(Modifier.fillMaxWidth(), TextAlign.Center)
                    actions(Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = contentPadding, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    iconBubble()
                    textBlock(Modifier.weight(1f), TextAlign.Start)
                    actions(Modifier.widthIn(min = 144.dp, max = 220.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardSaleInspectorPanel(
    order: OrmaOrder,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onOpenFullDetails: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DashboardRecordCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OrmaBadge(
                        text = order.orderType.orderSectionEyebrow(),
                        tone = OrmaStatusTone.Info,
                    )
                    Text(
                        text = order.orderNumber.ifBlank { order.id.take(8).uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = order.customerName ?: "Walk-in customer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DashboardWideActionButton(
                    text = "Close",
                    onClick = onClose,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardWideActionButton(
                    text = "Edit details",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    primary = true,
                )
                DashboardWideActionButton(
                    text = "Full details",
                    onClick = onOpenFullDetails,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        BookingDetailsSummaryCard(order = order)
        BookingDetailsFulfillmentCard(
            order = order,
            onStatusChange = onStatusChange,
        )
        BookingDetailsLineItemsCard(order = order)
        BookingDetailsCustomerCard(order = order)
    }
}

@Composable
private fun DashboardSalesFocusPanel(
    state: OnboardingUiState,
    orders: List<OrmaOrder>,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Sale inspector",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Select a sale to inspect customer, line items, payment, and fulfilment without losing the queue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (state.dashboard.loading) "SYNC" else "LIVE",
                tone = OrmaStatusTone.Info,
            )
        }
        DashboardChecklistRow(text = "${orders.size} sales are in the current queue.")
        DashboardChecklistRow(text = "Use Open for review and Edit for payment or fulfilment changes.")
    }
}

@Composable
private fun DashboardRecordsSurfaceHeader(
    title: String,
    body: String,
    badgeText: String,
    badgeTone: OrmaStatusTone = OrmaStatusTone.Info,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OrmaBadge(
            text = badgeText,
            tone = badgeTone,
        )
    }
}

@Composable
private fun DashboardEmbeddedSearchToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    placeholder: String,
    primaryText: String? = null,
    onPrimary: (() -> Unit)? = null,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    tertiaryText: String? = null,
    onTertiary: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardCompactSearchField(
            value = state.dashboard.filters.query,
            onValueChange = actions.onDashboardSearchChange,
            placeholder = placeholder,
            modifier = Modifier.weight(1f),
            shape = OrmaShapes.Field,
        )
        if (state.hasActiveDashboardFilter()) {
            DashboardWideActionButton(
                text = "Clear",
                onClick = { clearDashboardWorkspaceFilters(state, actions) },
            )
        }
        DashboardWideActionButton(
            text = if (state.dashboard.loading) "Syncing" else "Refresh",
            onClick = actions.onDashboardRefresh,
            enabled = !state.dashboard.loading,
        )
        if (tertiaryText != null && onTertiary != null) {
            DashboardWideActionButton(
                text = tertiaryText,
                onClick = onTertiary,
            )
        }
        if (secondaryText != null && onSecondary != null) {
            DashboardWideActionButton(
                text = secondaryText,
                onClick = onSecondary,
            )
        }
        if (primaryText != null && onPrimary != null) {
            DashboardWideActionButton(
                text = primaryText,
                onClick = onPrimary,
                primary = true,
                enabled = !state.dashboard.loading,
            )
        }
    }
}

@Composable
private fun DashboardWorkspaceToolbarCard(
    title: String,
    body: String,
    badgeText: String,
    primaryText: String? = null,
    onPrimary: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    secondaryEnabled: Boolean = true,
    badgeTone: OrmaStatusTone = OrmaStatusTone.Info,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 760.dp
                val titleBlock: @Composable (Modifier) -> Unit = { modifier ->
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                val statusBlock: @Composable (Modifier) -> Unit = { modifier ->
                    Row(
                        modifier = modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (primaryText != null && onPrimary != null) {
                            DashboardWideActionButton(
                                text = primaryText,
                                onClick = onPrimary,
                                primary = true,
                                enabled = primaryEnabled,
                            )
                        }
                        if (secondaryText != null && onSecondary != null) {
                            DashboardToolbarHeaderActionButton(
                                text = secondaryText,
                                onClick = onSecondary,
                                enabled = secondaryEnabled,
                            )
                        }
                        OrmaBadge(
                            text = badgeText,
                            tone = badgeTone,
                        )
                    }
                }
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            titleBlock(Modifier.weight(1f))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (secondaryText != null && onSecondary != null) {
                                    DashboardToolbarHeaderActionButton(
                                        text = secondaryText,
                                        onClick = onSecondary,
                                        enabled = secondaryEnabled,
                                    )
                                }
                                OrmaBadge(
                                    text = badgeText,
                                    tone = badgeTone,
                                )
                            }
                        }
                        if (primaryText != null && onPrimary != null) {
                            DashboardWideActionButton(
                                text = primaryText,
                                onClick = onPrimary,
                                modifier = Modifier.fillMaxWidth(),
                                primary = true,
                                enabled = primaryEnabled,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        titleBlock(Modifier.weight(1f))
                        statusBlock(Modifier)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun DashboardToolbarHeaderActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(32.dp),
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.ScreenBackground else OrmaColors.Accent.copy(alpha = 0.08f),
        contentColor = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = if (enabled) 0.14f else 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaFlatIcon(
                kind = OrmaFlatIconKind.Refresh,
                modifier = Modifier.size(13.dp),
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardCustomerSearchToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    placeholder: String,
    onAddCustomer: () -> Unit,
) {
    val filters = state.dashboard.filters
    val filtersActive = state.hasActiveDashboardFilter()
    DashboardWorkspaceToolbarCard(
        title = "Customer focus",
        body = "Search first, then use the customer queue for repeat orders and follow-up.",
        badgeText = if (state.dashboard.loading) "SYNC" else "LIVE",
        primaryText = "Add customer",
        onPrimary = onAddCustomer,
        primaryEnabled = !state.dashboard.loading,
        secondaryText = if (state.dashboard.loading) "Syncing" else "Sync",
        onSecondary = actions.onDashboardRefresh,
        secondaryEnabled = !state.dashboard.loading,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 620.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashboardCompactSearchField(
                        value = state.dashboard.filters.query,
                        onValueChange = actions.onDashboardSearchChange,
                        placeholder = placeholder,
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrmaShapes.Field,
                    )
                    if (filtersActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardWorkspaceFilters(state, actions) },
                            )
                        }
                    }
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = false,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardCompactSearchField(
                            value = filters.query,
                            onValueChange = actions.onDashboardSearchChange,
                            placeholder = placeholder,
                            modifier = Modifier.weight(1f),
                            shape = OrmaShapes.Field,
                        )
                        if (filtersActive) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardWorkspaceFilters(state, actions) },
                            )
                        }
                    }
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCatalogSearchToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    placeholder: String,
    primaryText: String? = null,
    onAddItem: (() -> Unit)? = null,
    onTransferClick: () -> Unit,
    onCategoryClick: () -> Unit,
    showToolActions: Boolean = true,
) {
    val filters = state.dashboard.filters
    val filtersActive = state.hasActiveDashboardFilter()
    val itemTypeOptions = state.dashboardItemTypeFilterOptions()
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    val activeItemType = state.activeDashboardItemType()
    val showItemTypeFilter = itemTypeOptions.size > 1
    val showStockFilter = selectedItemType == "all" || activeItemType == "product"
    val itemTypeFilter: @Composable (Modifier) -> Unit = { modifier ->
        if (showItemTypeFilter) {
            DashboardCompactSegmentedPicker(
                options = itemTypeOptions,
                selected = selectedItemType,
                label = { if (it == "all") "All items" else it.sellableItemTypeLabel() },
                onSelected = {
                    actions.onProductItemTypeFilterChange(it)
                    if (it != "all" && it != "product") {
                        actions.onProductLowStockFilterChange(false)
                    }
                },
                modifier = modifier,
            )
        }
    }
    val stockFilter: @Composable (Modifier) -> Unit = { modifier ->
        if (showStockFilter) {
            DashboardCompactSegmentedPicker(
                options = listOf(false, true),
                selected = filters.lowStockOnly,
                label = { if (it) "Low stock" else "All stock" },
                onSelected = actions.onProductLowStockFilterChange,
                modifier = modifier,
            )
        }
    }
    DashboardWorkspaceToolbarCard(
        title = "Catalog focus",
        body = "Search first, then narrow by item type, stock, and date.",
        badgeText = if (state.dashboard.loading) "SYNC" else "LIVE",
        primaryText = primaryText,
        onPrimary = onAddItem,
        primaryEnabled = !state.dashboard.loading,
        secondaryText = if (state.dashboard.loading) "Syncing" else "Sync",
        onSecondary = actions.onDashboardRefresh,
        secondaryEnabled = !state.dashboard.loading,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 700.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashboardCompactSearchField(
                        value = state.dashboard.filters.query,
                        onValueChange = actions.onDashboardSearchChange,
                        placeholder = placeholder,
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrmaShapes.Field,
                    )
                    if (showItemTypeFilter) {
                        itemTypeFilter(Modifier.fillMaxWidth())
                    }
                    if (showStockFilter) {
                        stockFilter(Modifier.fillMaxWidth())
                    }
                    if (filtersActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardWorkspaceFilters(state, actions) },
                            )
                        }
                    }
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = false,
                    )
                    if (showToolActions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DashboardWideActionButton(
                                text = "Category",
                                onClick = onCategoryClick,
                                modifier = Modifier.weight(1f),
                            )
                            DashboardWideActionButton(
                                text = "Import / Export",
                                onClick = onTransferClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardCompactSearchField(
                            value = filters.query,
                            onValueChange = actions.onDashboardSearchChange,
                            placeholder = placeholder,
                            modifier = Modifier.weight(1.35f),
                            shape = OrmaShapes.Field,
                        )
                        if (showItemTypeFilter) {
                            itemTypeFilter(Modifier.weight(1.05f))
                        }
                        if (showStockFilter) {
                            stockFilter(Modifier.widthIn(max = 260.dp))
                        }
                        if (filtersActive) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardWorkspaceFilters(state, actions) },
                            )
                        }
                    }
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (showToolActions) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Text(
                                text = "TOOLS",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrmaColors.TextSecondary,
                                maxLines = 1,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DashboardWideActionButton(
                                    text = "Category",
                                    onClick = onCategoryClick,
                                    modifier = Modifier.width(160.dp),
                                )
                                DashboardWideActionButton(
                                    text = "Import / Export",
                                    onClick = onTransferClick,
                                    modifier = Modifier.width(190.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardInvoiceSearchToolbar(
    state: OnboardingUiState,
    actions: OnboardingActions,
    onCreateInvoice: () -> Unit,
) {
    val filters = state.dashboard.filters
    val filtersActive = state.hasActiveInvoiceFilter()
    DashboardWorkspaceToolbarCard(
        title = "Invoice focus",
        body = "Find invoice-ready work, then preview tax details before sharing.",
        badgeText = if (state.dashboard.loading) "SYNC" else "LIVE",
        primaryText = "Create invoice",
        onPrimary = onCreateInvoice,
        primaryEnabled = !state.dashboard.loading,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 600.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashboardCompactSearchField(
                        value = filters.query,
                        onValueChange = actions.onDashboardSearchChange,
                        placeholder = "Invoice, order, customer, amount",
                        modifier = Modifier.fillMaxWidth(),
                        shape = OrmaShapes.Field,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (filtersActive) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardInvoiceFilters(actions) },
                            )
                        }
                        DashboardWideActionButton(
                            text = if (state.dashboard.loading) "Syncing" else "Refresh",
                            onClick = actions.onDashboardRefresh,
                            enabled = !state.dashboard.loading,
                        )
                    }
                    DashboardCompactSegmentedPicker(
                        options = DashboardInvoiceStatusFilters,
                        selected = state.selectedDashboardInvoiceStatusFilter(),
                        label = { it.invoiceStatusFilterLabel() },
                        onSelected = actions.onOrderStatusFilterChange,
                    )
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = false,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardCompactSearchField(
                            value = filters.query,
                            onValueChange = actions.onDashboardSearchChange,
                            placeholder = "Invoice, order, customer, amount",
                            modifier = Modifier.weight(1f),
                            shape = OrmaShapes.Field,
                        )
                        if (filtersActive) {
                            DashboardSalesInlineResetButton(
                                onClick = { clearDashboardInvoiceFilters(actions) },
                            )
                        }
                        DashboardWideActionButton(
                            text = if (state.dashboard.loading) "Syncing" else "Refresh",
                            onClick = actions.onDashboardRefresh,
                            enabled = !state.dashboard.loading,
                        )
                    }
                    DashboardCompactSegmentedPicker(
                        options = DashboardInvoiceStatusFilters,
                        selected = state.selectedDashboardInvoiceStatusFilter(),
                        label = { it.invoiceStatusFilterLabel() },
                        onSelected = actions.onOrderStatusFilterChange,
                    )
                    DashboardDateRangeFilter(
                        filters = filters,
                        actions = actions,
                        wide = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardInlineEmptyRecords(
    icon: DashboardNavIconKind,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OrmaDashboardIconBubble(modifier = Modifier.size(42.dp)) {
            DashboardNavIcon(
                kind = icon,
                color = OrmaColors.IconPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun clearDashboardWorkspaceFilters(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    actions.onDashboardSearchChange("")
    actions.onOrderStatusFilterChange("all")
    actions.onOrderTypeFilterChange(state.defaultDashboardOrderTypeFilter())
    actions.onProductItemTypeFilterChange(state.defaultDashboardItemTypeFilter())
    actions.onDashboardDateFilterChange("", "")
    actions.onProductLowStockFilterChange(false)
}

private fun OnboardingUiState.hasActiveInvoiceFilter(): Boolean =
    dashboard.filters.query.isNotBlank() ||
        selectedDashboardInvoiceStatusFilter() != "all" ||
        dashboard.filters.dateFrom.isNotBlank() ||
        dashboard.filters.dateTo.isNotBlank()

private fun clearDashboardInvoiceFilters(actions: OnboardingActions) {
    actions.onDashboardSearchChange("")
    actions.onOrderStatusFilterChange("all")
    actions.onDashboardDateFilterChange("", "")
}

@Composable
private fun DashboardInvoicesContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showInvoiceSheet by rememberSaveable { mutableStateOf(false) }
    var previewOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val invoices = filteredDashboardInvoiceOrders(state)
    val previewOrder = invoices.firstOrNull { it.id == previewOrderId }
    LaunchedEffect(invoices.size, invoices.firstOrNull()?.id) {
        if (previewOrderId != null && invoices.none { it.id == previewOrderId }) {
            previewOrderId = null
        }
    }

    if (wide) {
        DashboardInvoicesWorkspace(
            state = state,
            actions = actions,
            invoices = invoices,
            onCreateInvoice = { showInvoiceSheet = true },
            onPreviewInvoice = { previewOrderId = it.id },
        )
    } else {
        DashboardListScaffold(
            eyebrow = "INVOICES",
            title = "Tax invoices",
            body = if (invoices.isEmpty()) {
                "Create an invoice from manual line items or open an existing order as a tax invoice."
            } else if (state.hasActiveInvoiceFilter()) {
                "${invoices.size} matching invoice records from ${state.dashboard.orders.size} orders"
            } else {
                "${invoices.size} invoice records ready from order activity"
            },
            primaryText = "Create invoice",
            onPrimary = { showInvoiceSheet = true },
            secondaryText = if (state.dashboard.loading) "Refreshing..." else "Refresh",
            onSecondary = actions.onDashboardRefresh,
            loading = state.dashboard.loading,
            wide = false,
        ) {
            if (invoices.isEmpty()) {
                DashboardEmptyModuleCard(
                    icon = DashboardNavIconKind.Invoice,
                    title = if (state.hasActiveInvoiceFilter()) "No matching invoices" else "No invoices yet",
                    body = if (state.hasActiveInvoiceFilter()) {
                        "Try another invoice, order number, or customer search."
                    } else {
                        "Create a manual invoice with line items, tax settings, and customer billing details."
                    },
                )
                DashboardInvoiceCreateGuideCard(
                    state = state,
                    onCreate = { showInvoiceSheet = true },
                )
            } else {
                DashboardInvoiceSummaryCard(state = state, invoices = invoices)
                DashboardInvoiceCreateGuideCard(
                    state = state,
                    onCreate = { showInvoiceSheet = true },
                )
                DashboardInvoiceRecords(
                    state = state,
                    invoices = invoices,
                    selectedOrderId = null,
                    onSelect = { previewOrderId = it.id },
                    wide = false,
                )
            }
        }
    }

    if (showInvoiceSheet) {
        OrderFormSheet(
            state = state,
            actions = actions,
            invoiceMode = true,
            wide = wide,
            onDismiss = { showInvoiceSheet = false },
            onSubmit = { draft ->
                actions.onCreateOrder(draft)
                showInvoiceSheet = false
            },
        )
    }
    previewOrder?.let { order ->
        DashboardInvoicePreviewSheet(
            state = state,
            order = order,
            wide = wide,
            onDismiss = { previewOrderId = null },
            onRefresh = actions.onDashboardRefresh,
            onDownloadStatus = actions.onShowDashboardStatusMessage,
        )
    }
}

@Composable
private fun DashboardInvoicesWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    invoices: List<OrmaOrder>,
    onCreateInvoice: () -> Unit,
    onPreviewInvoice: (OrmaOrder) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DashboardInvoiceSearchToolbar(
            state = state,
            actions = actions,
            onCreateInvoice = onCreateInvoice,
        )
        DashboardInvoiceKpiStrip(state = state, invoices = invoices)
        DashboardInvoiceRecordsSurface(
            state = state,
            actions = actions,
            invoices = invoices,
            onPreviewInvoice = onPreviewInvoice,
        )
    }
}

@Composable
private fun DashboardInvoiceKpiStrip(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
) {
    val currency = invoices.firstOrNull()?.currency ?: state.dashboard.summary.currency.ifBlank { "INR" }
    val readyCount = invoices.count { it.status in setOf("paid", "completed") }
    val draftCount = (invoices.size - readyCount).coerceAtLeast(0)
    val total = invoices.sumOf { it.total.toDoubleOrNull().orZero() }
    val tax = invoices.sumOf { it.taxTotal.toDoubleOrNull().orZero() }
    val unpaid = invoices.sumOf { it.balanceDueValue() }
    DashboardFocusMetricStrip(
        metrics = listOf(
            DashboardFocusMetric(
                label = "Invoices",
                value = invoices.size.toString(),
                detail = if (state.hasActiveInvoiceFilter()) "after filters" else "ready records",
                tone = OrmaStatusTone.Info,
            ),
            DashboardFocusMetric(
                label = "Invoice value",
                value = dashboardMoney(total.toDashboardMoneyInput(), currency),
                detail = "before settlement",
                tone = OrmaStatusTone.Success,
                weight = 1.28f,
            ),
            DashboardFocusMetric(
                label = "Tax",
                value = dashboardMoney(tax.toDashboardMoneyInput(), currency),
                detail = "invoice tax",
                tone = OrmaStatusTone.Info,
            ),
            DashboardFocusMetric(
                label = "Open",
                value = draftCount.toString(),
                detail = if (unpaid > 0.0) dashboardMoney(unpaid.toDashboardMoneyInput(), currency) else "settled",
                tone = if (draftCount > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            ),
        ),
    )
}

@Composable
private fun DashboardInvoiceRecordsSurface(
    state: OnboardingUiState,
    actions: OnboardingActions,
    invoices: List<OrmaOrder>,
    onPreviewInvoice: (OrmaOrder) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordsSurfaceHeader(
                title = "Invoice records",
                body = "Search invoices, narrow by date, then open the tax document preview.",
                badgeText = "${invoices.size} SHOWN",
            )
            if (invoices.isEmpty()) {
                DashboardInlineEmptyRecords(
                    icon = DashboardNavIconKind.Invoice,
                    title = when {
                        state.dashboard.loading -> "Loading invoices"
                        !state.dashboard.errorMessage.isNullOrBlank() -> "Could not load invoices"
                        state.hasActiveInvoiceFilter() -> "No matching invoices"
                        else -> "No invoices yet"
                    },
                    body = when {
                        state.dashboard.loading -> "ORMA is refreshing invoice-ready records."
                        !state.dashboard.errorMessage.isNullOrBlank() -> state.dashboard.errorMessage.orEmpty()
                        state.hasActiveInvoiceFilter() -> "Clear filters or search another invoice, order, or customer."
                        else -> "Create a manual invoice or complete a sale to generate invoice records."
                    },
                )
            } else {
                var sortKey by rememberSaveable { mutableStateOf(DashboardInvoiceSortInvoice) }
                var sortAscending by rememberSaveable { mutableStateOf(true) }
                val sortedInvoices = remember(state, invoices, sortKey, sortAscending) {
                    sortedDashboardInvoices(
                        state = state,
                        invoices = invoices,
                        sortKey = sortKey,
                        ascending = sortAscending,
                    )
                }
                DashboardInvoiceTable(
                    state = state,
                    invoices = sortedInvoices,
                    sortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = { nextSortKey ->
                        if (sortKey == nextSortKey) {
                            sortAscending = !sortAscending
                        } else {
                            sortKey = nextSortKey
                            sortAscending = nextSortKey !in DashboardInvoiceDescendingFirstSorts
                        }
                    },
                    onPreviewInvoice = onPreviewInvoice,
                )
                DashboardPaginationControls(
                    pagination = state.dashboard.orderPagination,
                    wide = true,
                    loading = state.dashboard.loading,
                    onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Orders, it) },
                )
            }
        }
    }
}

@Composable
private fun DashboardInvoiceBillingFocusCard(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
) {
    val taxReady = invoices.count { it.taxTotal.toDoubleOrNull().orZero() > 0.0 }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Billing focus",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Keep invoice setup complete before printing or sharing with customers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
                tone = OrmaStatusTone.Info,
            )
        }
        DashboardChecklistRow(text = "Confirm legal name, address, and tax number in Account.")
        DashboardChecklistRow(text = "Use customer billing details for GST/VAT-ready invoices.")
        DashboardChecklistRow(text = "$taxReady invoices currently include tax lines.")
    }
}

@Composable
private fun DashboardInvoiceSummaryCard(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
) {
    val currency = invoices.firstOrNull()?.currency ?: state.dashboard.summary.currency
    val readyCount = invoices.count { it.status in setOf("paid", "completed") }
    val draftCount = invoices.size - readyCount
    val total = invoices.sumOf { it.total.toDoubleOrNull().orZero() }.toDashboardMoneyInput()
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Invoice desk",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Generated from order totals, tax, customer, and workspace billing details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "$readyCount READY",
                tone = if (readyCount > 0) OrmaStatusTone.Success else OrmaStatusTone.Info,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Invoice value",
                value = dashboardMoney(total, currency),
                detail = "${invoices.size} records",
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Success,
            )
            DashboardMiniMetricCell(
                label = "Draft",
                value = draftCount.toString(),
                detail = "not fully paid",
                modifier = Modifier.weight(1f),
                tone = if (draftCount > 0) OrmaStatusTone.Warning else OrmaStatusTone.Info,
            )
        }
    }
}

@Composable
private fun DashboardInvoiceRecords(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
    selectedOrderId: String?,
    onSelect: (OrmaOrder) -> Unit,
    wide: Boolean,
) {
    if (wide) {
        DashboardWideDataSurface(
            title = "Invoice records",
            columns = listOf("Invoice", "Customer", "Total", "Status", "Action"),
        ) {
            invoices.forEach { order ->
                DashboardWideInvoiceRow(
                    state = state,
                    order = order,
                    selected = order.id == selectedOrderId,
                    onSelect = { onSelect(order) },
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            invoices.forEach { order ->
                DashboardInvoiceRow(
                    state = state,
                    order = order,
                    selected = order.id == selectedOrderId,
                    onSelect = { onSelect(order) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DashboardInvoiceTable(
    state: OnboardingUiState,
    invoices: List<OrmaOrder>,
    sortKey: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    onPreviewInvoice: (OrmaOrder) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp),
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrmaColors.CardBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardSaleHeaderCell(
                            text = "Invoice",
                            sortKey = DashboardInvoiceSortInvoice,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.22f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Customer",
                            sortKey = DashboardInvoiceSortCustomer,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.08f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Total",
                            sortKey = DashboardInvoiceSortTotal,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.94f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Status",
                            sortKey = DashboardInvoiceSortStatus,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.82f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Action",
                            sortKey = null,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.width(132.dp),
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        thickness = 0.8.dp,
                        color = OrmaColors.CellBackground.copy(alpha = 0.72f),
                    )
                }
            }
        }
        itemsIndexed(
            items = invoices,
            key = { _, order -> order.id },
        ) { index, order ->
            DashboardWideInvoiceRow(
                state = state,
                order = order,
                selected = false,
                zebra = index % 2 == 1,
                onSelect = { onPreviewInvoice(order) },
            )
        }
    }
}

@Composable
private fun DashboardWideInvoiceRow(
    state: OnboardingUiState,
    order: OrmaOrder,
    selected: Boolean,
    zebra: Boolean = false,
    onSelect: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackground = when {
        selected -> OrmaColors.ScreenBackground.copy(alpha = 0.62f)
        hovered -> OrmaColors.ScreenBackground.copy(alpha = 0.44f)
        zebra -> OrmaColors.ScreenBackground.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OrmaShapes.SmallCard)
                .background(rowBackground)
                .hoverable(interactionSource)
                .clickable(onClick = onSelect)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardWideCell(
                primary = invoiceNumberFor(state, order),
                secondary = order.orderNumber.ifBlank { order.orderType.orderTypeLabel() },
                modifier = Modifier.weight(1.22f),
            )
            DashboardWideCell(
                primary = order.customerName ?: "Walk-in customer",
                secondary = order.orderType.orderTypeLabel(),
                modifier = Modifier.weight(1.08f),
            )
            DashboardWideCell(
                primary = dashboardMoney(order.total, order.currency),
                secondary = "Tax ${dashboardMoney(order.taxTotal, order.currency)}",
                modifier = Modifier.weight(0.94f),
            )
            Box(modifier = Modifier.weight(0.82f), contentAlignment = Alignment.CenterStart) {
                OrmaBadge(
                    text = order.invoiceStatusLabel().uppercase(),
                    tone = order.invoiceStatusTone(),
                )
            }
            Box(modifier = Modifier.width(132.dp), contentAlignment = Alignment.CenterStart) {
                DashboardTableActionButton(
                    text = if (selected) "Open" else "Preview",
                    iconKind = OrmaFlatIconKind.View,
                    onClick = onSelect,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp),
            thickness = 0.8.dp,
            color = if (selected || hovered) {
                OrmaColors.CellBackground.copy(alpha = 0.86f)
            } else {
                OrmaColors.CellBackground.copy(alpha = 0.62f)
            },
        )
    }
}

@Composable
private fun DashboardInvoiceRow(
    state: OnboardingUiState,
    order: OrmaOrder,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = if (selected) OrmaColors.Accent.copy(alpha = 0.08f) else OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, if (selected) OrmaColors.Accent.copy(alpha = 0.32f) else OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = invoiceNumberFor(state, order),
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${order.customerName ?: "Walk-in customer"} · ${order.orderNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = order.invoiceStatusLabel().uppercase(),
                    tone = order.invoiceStatusTone(),
                )
            }
            DashboardMetricLine(
                label = "Invoice total",
                value = dashboardMoney(order.total, order.currency),
                detail = "Tax ${dashboardMoney(order.taxTotal, order.currency)} · Paid ${dashboardMoney(order.paidTotal, order.currency)}",
            )
        }
    }
}

@Composable
private fun DashboardInvoiceCreateGuideCard(
    state: OnboardingUiState,
    onCreate: () -> Unit,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Invoice setup",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Use the top Create invoice action when you need a manual invoice with customer billing and tax behavior.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
                tone = OrmaStatusTone.Info,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DashboardChecklistRow(text = "Enable GST/VAT or keep the invoice tax-free.")
            DashboardChecklistRow(text = "Add manual line items with separate tax percentages.")
            DashboardChecklistRow(text = "Save it as an invoice-ready sale record.")
        }
        OrmaSecondaryButton(
            text = "Create invoice",
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DashboardInvoicePreviewScreen(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    if (!wide) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        OrmaBadge(text = "INVOICE PREVIEW", tone = OrmaStatusTone.Info)
                        Text(
                            text = invoiceNumberFor(state, order),
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${order.customerName ?: "Walk-in customer"} · ${dashboardMoney(order.total, order.currency)} · ${order.invoiceStatusLabel()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(text = order.invoiceStatusLabel().uppercase(), tone = order.invoiceStatusTone())
                }
                OrmaSecondaryButton(
                    text = if (state.dashboard.loading) "Refreshing" else "Refresh",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.dashboard.loading,
                )
            }
            DashboardInvoicePreviewDocumentCard(
                state = state,
                order = order,
                wide = false,
            )
        }
        return
    }

    DashboardListScaffold(
        eyebrow = "INVOICE PREVIEW",
        title = invoiceNumberFor(state, order),
        body = "${order.customerName ?: "Walk-in customer"} · ${dashboardMoney(order.total, order.currency)} · ${order.invoiceStatusLabel()}",
        primaryText = "Back to invoices",
        onPrimary = onBack,
        secondaryText = "Refresh",
        onSecondary = onRefresh,
        loading = state.dashboard.loading,
        wide = wide,
    ) {
        DashboardInvoicePreviewDocumentCard(
            state = state,
            order = order,
            wide = true,
        )
    }
}

@Composable
private fun DashboardInvoicePreviewSheet(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onDownloadStatus: (String) -> Unit,
) {
    val exporter = rememberOrmaOrderDocumentExporter()
    var documentStatus by rememberSaveable(order.id) { mutableStateOf<String?>(null) }
    fun downloadInvoicePdf() {
        val document = orderInvoicePdfDocument(state = state, order = order)
        val message = if (exporter.downloadPdf(fileName = document.fileName, pdfBase64 = document.pdfBase64)) {
            "Invoice PDF download complete."
        } else {
            "Invoice PDF download is not available on this device yet."
        }
        documentStatus = message
        onDownloadStatus(message)
    }
    DashboardFormSheet(
        title = "Invoice preview",
        body = "${invoiceNumberFor(state, order)} · ${order.customerName ?: "Walk-in customer"}",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        DashboardRecordCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = invoiceNumberFor(state, order),
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${order.customerName ?: "Walk-in customer"} · ${order.orderType.orderTypeLabel()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = order.invoiceStatusLabel().uppercase(),
                    tone = order.invoiceStatusTone(),
                )
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Total" to dashboardMoney(order.total, order.currency),
                    "Tax" to dashboardMoney(order.taxTotal, order.currency),
                    "Paid" to dashboardMoney(order.paidTotal, order.currency),
                ),
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 430.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DashboardWideActionButton(
                            text = "Download PDF",
                            onClick = ::downloadInvoicePdf,
                            modifier = Modifier.fillMaxWidth(),
                            primary = true,
                        )
                        DashboardWideActionButton(
                            text = if (state.dashboard.loading) "Refreshing" else "Refresh",
                            onClick = onRefresh,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.dashboard.loading,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DashboardWideActionButton(
                            text = "Download PDF",
                            onClick = ::downloadInvoicePdf,
                            modifier = Modifier.weight(1f),
                            primary = true,
                        )
                        DashboardWideActionButton(
                            text = if (state.dashboard.loading) "Refreshing" else "Refresh",
                            onClick = onRefresh,
                            modifier = Modifier.weight(1f),
                            enabled = !state.dashboard.loading,
                        )
                    }
                }
            }
            documentStatus?.let { status ->
                DashboardChecklistRow(text = status)
            }
        }
        DashboardInvoicePreviewDocumentCard(
            state = state,
            order = order,
            wide = wide,
        )
    }
}

@Composable
private fun DashboardInvoicePreviewDocumentCard(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Invoice document",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Preview before sharing, printing, or downloading.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(text = order.invoiceStatusLabel().uppercase(), tone = order.invoiceStatusTone())
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (wide) Modifier else Modifier.horizontalScroll(rememberScrollState())),
            contentAlignment = Alignment.TopCenter,
        ) {
            DashboardInvoiceDocument(
                state = state,
                order = order,
                compact = wide,
                modifier = if (wide) Modifier.widthIn(max = 940.dp) else Modifier.width(700.dp),
            )
        }
    }
}

@Composable
private fun DashboardInvoicePreviewCard(
    state: OnboardingUiState,
    order: OrmaOrder?,
    wide: Boolean,
) {
    if (order == null) {
        DashboardEmptyModuleCard(
            icon = DashboardNavIconKind.Invoice,
            title = "Select an invoice",
            body = "Choose an order record to preview the tax invoice.",
        )
        return
    }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tax invoice preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Based on the provided DarDoc invoice template.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaBadge(text = "ORIGINAL", tone = OrmaStatusTone.Info)
        }
        if (wide) {
            DashboardInvoiceDocument(state = state, order = order, compact = true)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                DashboardInvoiceDocument(
                    state = state,
                    order = order,
                    compact = false,
                    modifier = Modifier.width(680.dp),
                )
            }
        }
    }
}

@Composable
private fun DashboardInvoiceDocument(
    state: OnboardingUiState,
    order: OrmaOrder,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val issuerName = invoiceIssuerName(state)
    val customer = order.customerId?.let { id -> state.dashboard.customers.firstOrNull { it.id == id } }
    val billToName = order.customerName?.takeIf { it.isNotBlank() } ?: customer?.name ?: "Walk-in customer"
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        shadowElevation = if (compact) 0.dp else 8.dp,
        tonalElevation = 0.dp,
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrmaColors.Accent,
                contentColor = OrmaColors.OnAccent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = issuerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.OnAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = invoiceIssuerSubLine(state),
                            style = MaterialTheme.typography.bodySmall,
                            color = OrmaColors.OnAccent.copy(alpha = 0.62f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "CUSTOMER BILLING",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrmaColors.ScreenBackground.copy(alpha = 0.46f),
                        )
                        Text(
                            text = invoiceNumberFor(state, order),
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.ScreenBackground,
                        )
                        Text(
                            text = invoiceIssueDateLabel(order),
                            style = MaterialTheme.typography.bodySmall,
                            color = OrmaColors.ScreenBackground.copy(alpha = 0.62f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tax Invoice",
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    color = OrmaColors.TextPrimary,
                )
                OrmaBadge(text = "ORIGINAL", tone = OrmaStatusTone.Info)
            }
            HorizontalDivider(color = OrmaColors.Divider)

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                InvoiceMetaBand(state = state, order = order)
                InvoicePartyRow(state = state, order = order, customer = customer, billToName = billToName)
                InvoiceItemsTable(state = state, order = order, compact = compact)
                InvoiceTotalsBlock(order = order)
                InvoiceAmountWords(order = order)
                InvoiceTermsAndSignature(state = state, billToName = billToName)
                InvoiceFooter(state = state, order = order)
            }
        }
    }
}

@Composable
private fun InvoiceMetaBand(
    state: OnboardingUiState,
    order: OrmaOrder,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        InvoiceMetaCell(
            label = "Date of Issue",
            value = invoiceIssueDateLabel(order),
            modifier = Modifier.weight(1f),
        )
        InvoiceMetaCell(
            label = "Invoice No.",
            value = invoiceNumberFor(state, order),
            modifier = Modifier.weight(1f),
        )
        InvoiceMetaCell(
            label = "Reference",
            value = order.orderNumber.ifBlank { order.id.take(8) },
            modifier = Modifier.weight(1f),
        )
        InvoiceMetaCell(
            label = state.draft.taxLabel.ifBlank { "GST/VAT" },
            value = state.draft.taxNumber.ifBlank { "Not registered" },
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(color = OrmaColors.Divider)
}

@Composable
private fun InvoiceMetaCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InvoicePartyRow(
    state: OnboardingUiState,
    order: OrmaOrder,
    customer: OrmaCustomer?,
    billToName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        InvoicePartyBlock(
            label = "Issued By",
            name = invoiceIssuerName(state),
            detail = invoiceIssuerDetail(state),
            modifier = Modifier.weight(1f),
        )
        InvoicePartyBlock(
            label = "Bill To",
            name = billToName,
            detail = invoiceBillToDetail(order = order, customer = customer),
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(color = OrmaColors.Divider)
}

@Composable
private fun InvoicePartyBlock(
    label: String,
    name: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.TextSecondary,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = detail.ifBlank { "Details not available" },
            style = MaterialTheme.typography.bodySmall,
            color = OrmaColors.TextSecondary,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun InvoiceItemsTable(
    state: OnboardingUiState,
    order: OrmaOrder,
    compact: Boolean,
) {
    val invoiceItems = invoiceRenderableItems(order)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "LINE ITEMS",
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.TextSecondary,
        )
        InvoiceItemHeader()
        if (invoiceItems.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.StandardCell,
                color = OrmaColors.CellBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = "No billable line items are available for this invoice yet.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        } else {
            invoiceItems.forEachIndexed { index, item ->
                InvoiceItemRow(
                    state = state,
                    index = index,
                    item = item,
                    currency = order.currency,
                    compact = compact,
                )
            }
        }
    }
}

@Composable
private fun InvoiceItemHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InvoiceTableText("#", Modifier.weight(0.34f), secondary = true)
        InvoiceTableText("Item / Description", Modifier.weight(2.1f), secondary = true)
        InvoiceTableText("UOM", Modifier.weight(0.72f), secondary = true)
        InvoiceTableText("Qty", Modifier.weight(0.62f), secondary = true, alignEnd = true)
        InvoiceTableText("Unit Price", Modifier.weight(1f), secondary = true, alignEnd = true)
        InvoiceTableText("VAT", Modifier.weight(0.7f), secondary = true, alignEnd = true)
        InvoiceTableText("Amount", Modifier.weight(1f), secondary = true, alignEnd = true)
    }
    HorizontalDivider(color = OrmaColors.Divider)
}

@Composable
private fun InvoiceItemRow(
    state: OnboardingUiState,
    index: Int,
    item: OrmaOrderItem,
    currency: String,
    compact: Boolean,
) {
    val product = item.productId?.let { productId -> state.dashboard.products.firstOrNull { it.id == productId } }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InvoiceTableText((index + 1).toString().padStart(2, '0'), Modifier.weight(0.34f), secondary = true)
        Column(
            modifier = Modifier.weight(2.1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.description.ifBlank { item.productName ?: "Line item" },
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
            product?.categoryName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        InvoiceTableText(product?.unit ?: "Service", Modifier.weight(0.72f), secondary = true)
        InvoiceTableText(item.quantity, Modifier.weight(0.62f), alignEnd = true)
        InvoiceTableText(dashboardMoney(item.unitPrice, currency), Modifier.weight(1f), alignEnd = true)
        InvoiceTableText(item.taxRate.invoiceTaxLabel(), Modifier.weight(0.7f), secondary = true, alignEnd = true)
        InvoiceTableText(dashboardMoney(item.lineTotal, currency), Modifier.weight(1f), alignEnd = true)
    }
    HorizontalDivider(color = OrmaColors.Divider.copy(alpha = 0.65f))
}

@Composable
private fun InvoiceTableText(
    text: String,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    alignEnd: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        style = if (secondary) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        color = if (secondary) OrmaColors.TextSecondary else OrmaColors.TextPrimary,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun InvoiceTotalsBlock(order: OrmaOrder) {
    val invoiceItems = invoiceRenderableItems(order)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(0.62f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InvoiceTotalLine("Total Quantity", invoiceItems.sumOf { it.quantity.toDoubleOrNull().orZero() }.toDashboardMoneyInput())
            InvoiceTotalLine("Subtotal (ex. tax)", dashboardMoney(order.subtotal, order.currency))
            InvoiceTotalLine("Tax", dashboardMoney(order.taxTotal, order.currency))
            HorizontalDivider(color = OrmaColors.Divider)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "GRAND TOTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = dashboardMoney(order.total, order.currency),
                    style = MaterialTheme.typography.titleLarge,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun InvoiceTotalLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OrmaColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = OrmaColors.TextPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun InvoiceAmountWords(order: OrmaOrder) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "AMOUNT IN WORDS",
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
            )
            Text(
                text = invoiceAmountInWords(order),
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun InvoiceTermsAndSignature(
    state: OnboardingUiState,
    billToName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "TERMS & CONDITIONS",
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
            )
            listOf(
                "This invoice reflects the amount captured for the referenced order.",
                "Payment is due as per the agreed payment terms.",
                "Discrepancies must be reported within 48 hours of receipt.",
                state.draft.invoiceFooter.ifBlank { "Thank you for your business." },
            ).forEachIndexed { index, term ->
                Text(
                    text = "${index + 1}. $term",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextSecondary,
                    lineHeight = 18.sp,
                )
            }
        }
        Column(
            modifier = Modifier.weight(0.78f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InvoiceSignatureBox(
                label = "Authorized By",
                name = invoiceIssuerName(state),
                role = "Workspace owner",
            )
            InvoiceSignatureBox(
                label = "Acknowledged By",
                name = billToName,
                role = "Customer",
            )
        }
    }
}

@Composable
private fun InvoiceSignatureBox(
    label: String,
    name: String,
    role: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
            )
            HorizontalDivider(color = OrmaColors.Divider)
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun InvoiceFooter(
    state: OnboardingUiState,
    order: OrmaOrder,
) {
    HorizontalDivider(color = OrmaColors.Divider)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = invoiceIssuerName(state),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = invoiceIssuerSubLine(state),
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "${invoiceNumberFor(state, order)} · ${invoiceIssueDateLabel(order)}",
            style = MaterialTheme.typography.bodySmall,
            color = OrmaColors.TextTertiary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DashboardCustomersContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showCustomerSheet by rememberSaveable { mutableStateOf(false) }
    var selectedCustomerId by rememberSaveable { mutableStateOf<String?>(null) }
    val visibleCustomers = filteredDashboardCustomers(state)
    val selectedCustomer = state.dashboard.customers.firstOrNull { it.id == selectedCustomerId }
    val selectedCustomerOrders = selectedCustomer?.let { state.customerOrders(it) }.orEmpty()
    val selectedCustomerHistoryLoading = selectedCustomerId != null &&
        selectedCustomerId in state.dashboard.customerOrderHistoryLoading
    val selectedCustomerHistoryError = selectedCustomerId?.let { state.dashboard.customerOrderHistoryErrors[it] }
    LaunchedEffect(selectedCustomerId) {
        val customerId = selectedCustomerId ?: return@LaunchedEffect
        if (
            customerId !in state.dashboard.customerOrderHistory &&
            customerId !in state.dashboard.customerOrderHistoryLoading
        ) {
            actions.onLoadCustomerOrders(customerId)
        }
    }
    if (wide) {
        DashboardCustomersWorkspace(
            state = state,
            actions = actions,
            customers = visibleCustomers,
            selectedCustomer = null,
            selectedCustomerOrders = emptyList(),
            selectedCustomerHistoryLoading = false,
            selectedCustomerHistoryError = null,
            onCustomerClick = { selectedCustomerId = it.id },
            onCloseCustomer = { selectedCustomerId = null },
            onAddCustomer = { showCustomerSheet = true },
        )
    } else {
        DashboardListScaffold(
            eyebrow = "CUSTOMERS",
            title = "Customer engagement",
            body = if (state.dashboard.customers.isEmpty()) {
                "Save customers so repeat orders, reminders, and service follow-up become possible."
            } else if (state.hasActiveDashboardFilter()) {
                "${visibleCustomers.size} matching customers from ${state.dashboard.customers.size} saved"
            } else {
                "${state.dashboard.customers.size} customers available for orders and follow-up"
            },
            primaryText = "Add customer",
            onPrimary = { showCustomerSheet = true },
            loading = state.dashboard.loading,
            wide = false,
        ) {
            DashboardCustomerPulseCard(state = state)
            DashboardCustomerRecords(
                state = state,
                actions = actions,
                wide = false,
                onCustomerClick = { selectedCustomerId = it.id },
            )
            DashboardCustomerEngagementGuideCard(state = state)
        }
    }

    if (showCustomerSheet) {
        CustomerFormSheet(
            onDismiss = { showCustomerSheet = false },
            onSubmit = { draft ->
                actions.onCreateCustomer(draft)
                showCustomerSheet = false
            },
        )
    }
    if (selectedCustomer != null) {
        CustomerDetailsSheet(
            customer = selectedCustomer,
            orders = selectedCustomerOrders,
            loadingHistory = selectedCustomerHistoryLoading,
            historyError = selectedCustomerHistoryError,
            wide = wide,
            onDismiss = { selectedCustomerId = null },
        )
    }
}

@Composable
private fun DashboardCustomersWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    customers: List<OrmaCustomer>,
    selectedCustomer: OrmaCustomer?,
    selectedCustomerOrders: List<OrmaOrder>,
    selectedCustomerHistoryLoading: Boolean,
    selectedCustomerHistoryError: String?,
    onCustomerClick: (OrmaCustomer) -> Unit,
    onCloseCustomer: () -> Unit,
    onAddCustomer: () -> Unit,
) {
    if (selectedCustomer == null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DashboardCustomerSearchToolbar(
                state = state,
                actions = actions,
                placeholder = "Customer, phone, email, city",
                onAddCustomer = onAddCustomer,
            )
            DashboardCustomerKpiStrip(state = state, customers = customers)
            DashboardCustomerWorkQueueBar(
                state = state,
                customers = customers,
            )
            DashboardCustomerRecordsSurface(
                state = state,
                actions = actions,
                customers = customers,
                selectedCustomer = null,
                onCustomerClick = onCustomerClick,
                onAddCustomer = onAddCustomer,
            )
        }
        return
    }

    OrmaDashboardResponsiveWorkspace(
        wide = true,
        primaryWeight = 1.72f,
        secondaryMinWidth = 340.dp,
        secondaryMaxWidth = 400.dp,
        stackBelowWidth = 1120.dp,
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardCustomerSearchToolbar(
                    state = state,
                    actions = actions,
                    placeholder = "Customer, phone, email, city",
                    onAddCustomer = onAddCustomer,
                )
                DashboardCustomerKpiStrip(state = state, customers = customers)
                DashboardCustomerRecordsSurface(
                    state = state,
                    actions = actions,
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    onCustomerClick = onCustomerClick,
                    onAddCustomer = onAddCustomer,
                )
            }
        },
        secondary = {
            DashboardCustomerDetailsPanel(
                customer = selectedCustomer,
                orders = selectedCustomerOrders,
                loadingHistory = selectedCustomerHistoryLoading,
                historyError = selectedCustomerHistoryError,
                onClose = onCloseCustomer,
            )
        },
    )
}

@Composable
private fun DashboardCustomerKpiStrip(
    state: OnboardingUiState,
    customers: List<OrmaCustomer>,
) {
    val allCustomers = state.dashboard.customers
    val reachable = customers.count { !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank() }
    val withAddress = customers.count { !it.addressLine.isNullOrBlank() || !it.city.isNullOrBlank() }
    val active = customers.count { it.status.dashboardTeamStatusLabel() == "Active" }
    DashboardFocusMetricStrip(
        metrics = listOf(
            DashboardFocusMetric(
                label = "Customers",
                value = customers.size.toString(),
                detail = if (state.hasActiveDashboardFilter()) "from ${allCustomers.size}" else "saved records",
                tone = OrmaStatusTone.Info,
            ),
            DashboardFocusMetric(
                label = "Reachable",
                value = reachable.toString(),
                detail = "phone or email",
                tone = if (reachable == customers.size && customers.isNotEmpty()) OrmaStatusTone.Success else OrmaStatusTone.Warning,
            ),
            DashboardFocusMetric(
                label = "Delivery",
                value = withAddress.toString(),
                detail = "address ready",
                tone = OrmaStatusTone.Info,
            ),
            DashboardFocusMetric(
                label = "Active",
                value = active.toString(),
                detail = "usable records",
                tone = OrmaStatusTone.Success,
            ),
        ),
    )
}

@Composable
private fun DashboardCustomerWorkQueueBar(
    state: OnboardingUiState,
    customers: List<OrmaCustomer>,
) {
    val needsContact = customers.count { !it.hasCustomerContact() }
    val needsAddress = customers.count { !it.hasCustomerLocation() }
    val repeatReady = customers.count { it.hasCustomerContact() && it.status.dashboardTeamStatusLabel() == "Active" }
    val openWork = needsContact + needsAddress
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "CRM queue",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (state.hasActiveDashboardFilter()) {
                        "Filtered customer readiness across contact, delivery, and repeat ordering."
                    } else {
                        "Customer readiness across contact, delivery, and repeat ordering."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (openWork > 0) "$openWork OPEN" else "READY",
                tone = if (openWork > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 720.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardCustomerQueueMetricRows(
                        needsContact = needsContact,
                        needsAddress = needsAddress,
                        repeatReady = repeatReady,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashboardMiniMetricCell(
                        label = "Need contact",
                        value = needsContact.toString(),
                        detail = "phone/email",
                        modifier = Modifier.weight(1f),
                        tone = if (needsContact == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                    )
                    DashboardMiniMetricCell(
                        label = "Need address",
                        value = needsAddress.toString(),
                        detail = "delivery area",
                        modifier = Modifier.weight(1f),
                        tone = if (needsAddress == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                    )
                    DashboardMiniMetricCell(
                        label = "Repeat-ready",
                        value = repeatReady.toString(),
                        detail = "active contact",
                        modifier = Modifier.weight(1f),
                        tone = OrmaStatusTone.Success,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCustomerQueueMetricRows(
    needsContact: Int,
    needsAddress: Int,
    repeatReady: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DashboardMiniMetricCell(
            label = "Need contact",
            value = needsContact.toString(),
            detail = "phone/email",
            modifier = Modifier.weight(1f),
            tone = if (needsContact == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
        )
        DashboardMiniMetricCell(
            label = "Need address",
            value = needsAddress.toString(),
            detail = "delivery area",
            modifier = Modifier.weight(1f),
            tone = if (needsAddress == 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DashboardMiniMetricCell(
            label = "Repeat-ready",
            value = repeatReady.toString(),
            detail = "active contact",
            modifier = Modifier.weight(1f),
            tone = OrmaStatusTone.Success,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DashboardCustomerRecordsSurface(
    state: OnboardingUiState,
    actions: OnboardingActions,
    customers: List<OrmaCustomer>,
    selectedCustomer: OrmaCustomer?,
    onCustomerClick: (OrmaCustomer) -> Unit,
    onAddCustomer: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordsSurfaceHeader(
                title = "Customer records",
                body = "Find repeat customers, review contact readiness, and open order history.",
                badgeText = "${customers.size} SHOWN",
            )
            if (customers.isEmpty()) {
                DashboardInlineEmptyRecords(
                    icon = DashboardNavIconKind.Customers,
                    title = when {
                        state.dashboard.loading -> "Loading customers"
                        !state.dashboard.errorMessage.isNullOrBlank() -> "Could not load customers"
                        state.hasActiveDashboardFilter() -> "No matching customers"
                        else -> "No customers yet"
                    },
                    body = when {
                        state.dashboard.loading -> "ORMA is refreshing the customer list."
                        !state.dashboard.errorMessage.isNullOrBlank() -> state.dashboard.errorMessage.orEmpty()
                        state.hasActiveDashboardFilter() -> "Clear filters or search another name, phone, email, or city."
                        else -> "Add customer details before repeat orders, reminders, and delivery follow-up."
                    },
                )
            } else {
                var sortKey by rememberSaveable { mutableStateOf(DashboardCustomerSortName) }
                var sortAscending by rememberSaveable { mutableStateOf(true) }
                val sortedCustomers = remember(customers, sortKey, sortAscending) {
                    sortedDashboardCustomers(
                        customers = customers,
                        sortKey = sortKey,
                        ascending = sortAscending,
                    )
                }
                DashboardCustomerTable(
                    customers = sortedCustomers,
                    selectedCustomer = selectedCustomer,
                    sortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = { nextSortKey ->
                        if (sortKey == nextSortKey) {
                            sortAscending = !sortAscending
                        } else {
                            sortKey = nextSortKey
                            sortAscending = true
                        }
                    },
                    onCustomerClick = onCustomerClick,
                )
                DashboardPaginationControls(
                    pagination = state.dashboard.customerPagination,
                    wide = true,
                    loading = state.dashboard.loading,
                    onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Customers, it) },
                )
            }
        }
    }
}

@Composable
private fun DashboardCustomerFocusPanel(
    state: OnboardingUiState,
    customers: List<OrmaCustomer>,
) {
    val needsContact = customers.count { it.phoneNumber.isNullOrBlank() && it.email.isNullOrBlank() }
    val needsAddress = customers.count { it.addressLine.isNullOrBlank() && it.city.isNullOrBlank() }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "CRM focus",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Select a customer to inspect billing, address, notes, and order history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (state.dashboard.loading) "SYNC" else "LIVE",
                tone = OrmaStatusTone.Info,
            )
        }
        DashboardChecklistRow(text = "$needsContact records still need phone or email.")
        DashboardChecklistRow(text = "$needsAddress records still need delivery area or address.")
        DashboardChecklistRow(text = "Open a customer before taking repeat service or delivery orders.")
    }
}

@Composable
private fun DashboardProductsContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showProductSheet by rememberSaveable { mutableStateOf(false) }
    var showSupplierSheet by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showOfferSheet by rememberSaveable { mutableStateOf(false) }
    var showProductTransferSheet by rememberSaveable { mutableStateOf(false) }
    var editProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var stockProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var imageProductId by rememberSaveable { mutableStateOf<String?>(null) }
    val editProduct = state.dashboard.products.firstOrNull { it.id == editProductId }
    val stockProduct = state.dashboard.products.firstOrNull { it.id == stockProductId }
    val imageProduct = state.dashboard.products.firstOrNull { it.id == imageProductId }
    val visibleProducts = filteredDashboardProducts(state)
    val itemType = state.activeDashboardItemType()
    val selectedItemType = state.selectedDashboardItemTypeFilter()

    if (wide) {
        DashboardProductsWorkspace(
            state = state,
            actions = actions,
            products = visibleProducts,
            onAddProduct = { showProductSheet = true },
            onTransferClick = { showProductTransferSheet = true },
            onCategoryClick = { showCategorySheet = true },
            onSupplierClick = { showSupplierSheet = true },
            onOfferClick = { showOfferSheet = true },
            onEditClick = { editProductId = it.id },
            onStockClick = { stockProductId = it.id },
            onImageClick = { imageProductId = it.id },
        )
    } else {
        DashboardListScaffold(
            eyebrow = if (selectedItemType == "all") "CATALOG" else itemType.catalogSectionEyebrow(),
            title = if (selectedItemType == "all") "Catalog" else itemType.catalogSectionTitle(),
            body = if (state.dashboard.products.isEmpty()) {
                if (selectedItemType == "all") {
                    "Add products, services, and appointment items before selling or sharing the catalog."
                } else {
                    itemType.emptyCatalogBody()
                }
            } else if (state.hasActiveDashboardFilter()) {
                "${visibleProducts.size} matching items from ${state.dashboard.products.size} catalog records"
            } else {
                if (selectedItemType == "all") {
                    "Manage products, services, appointment items, categories, offers, images, and stock."
                } else {
                    itemType.catalogSectionDescription()
                }
            },
            primaryText = if (selectedItemType == "all") "Add item" else itemType.catalogActionText(),
            onPrimary = { showProductSheet = true },
            secondaryText = "Import / Export",
            onSecondary = { showProductTransferSheet = true },
            tertiaryText = "Category",
            onTertiary = { showCategorySheet = true },
            loading = state.dashboard.loading,
            wide = false,
        ) {
            DashboardCatalogSearchToolbar(
                state = state,
                actions = actions,
                placeholder = when {
                    selectedItemType == "all" -> "Product, service, appointment, category"
                    itemType == "service" -> "Service, category, price"
                    itemType == "appointment" -> "Appointment service, duration, category"
                    else -> "Product, SKU, barcode, supplier"
                },
                onTransferClick = { showProductTransferSheet = true },
                onCategoryClick = { showCategorySheet = true },
                showToolActions = false,
            )
            DashboardCatalogPulseCard(state = state)
            DashboardCatalogToolsRow(
                state = state,
                onSupplierClick = { showSupplierSheet = true },
                onOfferClick = { showOfferSheet = true },
            )
            DashboardProductRecords(
                state = state,
                actions = actions,
                onEditClick = { editProductId = it.id },
                onStockClick = { stockProductId = it.id },
                onImageClick = { imageProductId = it.id },
                wide = false,
            )
        }
    }

    if (showProductSheet) {
        ProductFormSheet(
            state = state,
            onDismiss = { showProductSheet = false },
            onSubmit = { draft ->
                actions.onCreateProduct(draft)
                showProductSheet = false
            },
        )
    }
    editProduct?.let { product ->
        ProductFormSheet(
            state = state,
            product = product,
            onDismiss = { editProductId = null },
            onSubmit = { draft ->
                actions.onUpdateProduct(product.id, draft)
                editProductId = null
            },
        )
    }
    if (showSupplierSheet) {
        SupplierFormSheet(
            onDismiss = { showSupplierSheet = false },
            onSubmit = { draft ->
                actions.onCreateSupplier(draft)
                showSupplierSheet = false
            },
        )
    }
    if (showCategorySheet) {
        CategoryFormSheet(
            initialItemType = if (selectedItemType == "all") "all" else itemType,
            allowedItemTypes = state.allowedDashboardItemTypes(),
            onDismiss = { showCategorySheet = false },
            onSubmit = { draft ->
                actions.onCreateProductCategory(draft)
                showCategorySheet = false
            },
        )
    }
    if (showOfferSheet) {
        OfferFormSheet(
            state = state,
            onDismiss = { showOfferSheet = false },
            onSubmit = { draft ->
                actions.onCreateProductOffer(draft)
                showOfferSheet = false
            },
        )
    }
    if (showProductTransferSheet) {
        ProductTransferSheet(
            state = state,
            actions = actions,
            onDismiss = {
                actions.onClearProductTransfer()
                showProductTransferSheet = false
            },
        )
    }
    stockProduct?.let { product ->
        StockAdjustmentSheet(
            product = product,
            onDismiss = { stockProductId = null },
            onSubmit = { draft ->
                actions.onAdjustProductStock(product.id, draft)
                stockProductId = null
            },
        )
    }
    imageProduct?.let { product ->
        ProductImageSheet(
            product = product,
            state = state,
            onDismiss = { imageProductId = null },
            onSubmit = { image ->
                actions.onUploadProductImage(product.id, image)
                imageProductId = null
            },
        )
    }
}

@Composable
private fun DashboardProductsWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    products: List<OrmaProduct>,
    onAddProduct: () -> Unit,
    onTransferClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onSupplierClick: () -> Unit,
    onOfferClick: () -> Unit,
    onEditClick: (OrmaProduct) -> Unit,
    onStockClick: (OrmaProduct) -> Unit,
    onImageClick: (OrmaProduct) -> Unit,
) {
    OrmaDashboardResponsiveWorkspace(
        wide = true,
        primaryWeight = 1.74f,
        secondaryMinWidth = 330.dp,
        secondaryMaxWidth = 390.dp,
        stackBelowWidth = 1160.dp,
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardCatalogSearchToolbar(
                    state = state,
                    actions = actions,
                    placeholder = when {
                        state.selectedDashboardItemTypeFilter() == "all" -> "Product, service, appointment, category"
                        state.activeDashboardItemType() == "service" -> "Service, category, price"
                        state.activeDashboardItemType() == "appointment" -> "Appointment service, duration, category"
                        else -> "Product, SKU, barcode, supplier"
                    },
                    primaryText = if (state.selectedDashboardItemTypeFilter() == "all") {
                        "Add item"
                    } else {
                        state.activeDashboardItemType().catalogActionText()
                    },
                    onAddItem = onAddProduct,
                    onTransferClick = onTransferClick,
                    onCategoryClick = onCategoryClick,
                )
                DashboardProductKpiStrip(state = state, products = products)
                DashboardCatalogOperationsPanel(
                    state = state,
                    onSupplierClick = onSupplierClick,
                    onOfferClick = onOfferClick,
                    onTransferClick = onTransferClick,
                )
                DashboardProductRecordsSurface(
                    state = state,
                    actions = actions,
                    products = products,
                    onAddProduct = onAddProduct,
                    onTransferClick = onTransferClick,
                    onCategoryClick = onCategoryClick,
                    onEditClick = onEditClick,
                    onStockClick = onStockClick,
                    onImageClick = onImageClick,
                )
            }
        },
        secondary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardCatalogGuideCard(
                    state = state,
                    onSupplierClick = onSupplierClick,
                    onTransferClick = onTransferClick,
                )
            }
        },
    )
}

@Composable
private fun DashboardProductKpiStrip(
    state: OnboardingUiState,
    products: List<OrmaProduct>,
) {
    val allProducts = state.dashboard.products
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    val activeItemType = state.activeDashboardItemType()
    val productFocused = selectedItemType == "all" || activeItemType == "product"
    val stockTracked = products.count { it.trackStock }
    val lowStock = products.count { it.lowStock }
    val withImages = products.count { !it.imageUrl.isNullOrBlank() }
    val timedItems = products.count { (it.durationMinutes ?: 0) > 0 }
    val bookableItems = products.count { it.bookingRequired }
    DashboardFocusMetricStrip(
        metrics = buildList {
            add(
                DashboardFocusMetric(
                    label = "Items",
                    value = products.size.toString(),
                    detail = if (state.hasActiveDashboardFilter()) "from ${allProducts.size}" else "catalog records",
                    tone = OrmaStatusTone.Info,
                ),
            )
            if (productFocused) {
                add(
                    DashboardFocusMetric(
                        label = "Tracked",
                        value = stockTracked.toString(),
                        detail = "stock enabled",
                        tone = OrmaStatusTone.Success,
                    ),
                )
                add(
                    DashboardFocusMetric(
                        label = "Low stock",
                        value = lowStock.toString(),
                        detail = "needs action",
                        tone = if (lowStock > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
                    ),
                )
            } else {
                add(
                    DashboardFocusMetric(
                        label = "Timed",
                        value = timedItems.toString(),
                        detail = if (activeItemType == "appointment") "with duration" else "duration set",
                        tone = OrmaStatusTone.Info,
                    ),
                )
                add(
                    DashboardFocusMetric(
                        label = if (activeItemType == "appointment") "Bookable" else "Requests",
                        value = bookableItems.toString(),
                        detail = if (activeItemType == "appointment") "booking enabled" else "needs booking",
                        tone = OrmaStatusTone.Success,
                    ),
                )
            }
            add(
                DashboardFocusMetric(
                    label = "Images",
                    value = withImages.toString(),
                    detail = "catalog ready",
                    tone = if (products.isNotEmpty() && withImages == products.size) OrmaStatusTone.Success else OrmaStatusTone.Info,
                ),
            )
        },
    )
}

@Composable
private fun DashboardProductRecordsSurface(
    state: OnboardingUiState,
    actions: OnboardingActions,
    products: List<OrmaProduct>,
    onAddProduct: () -> Unit,
    onTransferClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onEditClick: (OrmaProduct) -> Unit,
    onStockClick: (OrmaProduct) -> Unit,
    onImageClick: (OrmaProduct) -> Unit,
) {
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    val activeItemType = state.activeDashboardItemType()
    val productPagination = state.dashboard.productPagination
    val totalProducts = productPagination.totalItems.coerceAtLeast(products.size)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordsSurfaceHeader(
                title = if (selectedItemType == "all") "Catalog records" else "${activeItemType.sellableItemTypeLabel()} records",
                body = "Manage prices, stock, images, categories, and selling readiness from one catalog table.",
                badgeText = if (totalProducts > products.size) {
                    "${products.size} SHOWN / $totalProducts TOTAL"
                } else {
                    "${products.size} SHOWN"
                },
            )
            if (products.isEmpty()) {
                DashboardInlineEmptyRecords(
                    icon = DashboardNavIconKind.Products,
                    title = when {
                        state.dashboard.loading -> "Loading catalog"
                        !state.dashboard.errorMessage.isNullOrBlank() -> "Could not load catalog"
                        state.hasActiveDashboardFilter() -> {
                            if (selectedItemType == "all") "No matching catalog items" else "No matching ${activeItemType.sellableItemTypeLabel().lowercase()}s"
                        }
                        else -> if (selectedItemType == "all") "No catalog items yet" else activeItemType.emptyCatalogTitle()
                    },
                    body = when {
                        state.dashboard.loading -> "ORMA is refreshing products, services, and appointment items."
                        !state.dashboard.errorMessage.isNullOrBlank() -> state.dashboard.errorMessage.orEmpty()
                        state.hasActiveDashboardFilter() -> "Clear filters or search another name, SKU, barcode, supplier, or category."
                        selectedItemType == "all" -> {
                            "Add products, services, or appointment items before selling."
                        }
                        else -> activeItemType.emptyCatalogBody()
                    },
                )
            } else {
                var sortKey by rememberSaveable { mutableStateOf(DashboardProductSortItem) }
                var sortAscending by rememberSaveable { mutableStateOf(true) }
                val sortedProducts = remember(products, sortKey, sortAscending) {
                    sortedDashboardProducts(
                        products = products,
                        sortKey = sortKey,
                        ascending = sortAscending,
                    )
                }
                DashboardProductTable(
                    products = sortedProducts,
                    sortKey = sortKey,
                    sortAscending = sortAscending,
                    onSortChange = { nextSortKey ->
                        if (sortKey == nextSortKey) {
                            sortAscending = !sortAscending
                        } else {
                            sortKey = nextSortKey
                            sortAscending = nextSortKey !in DashboardProductDescendingFirstSorts
                        }
                    },
                    onEditClick = onEditClick,
                    onStockClick = onStockClick,
                    onImageClick = onImageClick,
                )
                DashboardPaginationControls(
                    pagination = productPagination,
                    wide = true,
                    loading = state.dashboard.loading,
                    onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Products, it) },
                )
            }
        }
    }
}

@Composable
private fun DashboardCatalogOperationsPanel(
    state: OnboardingUiState,
    onSupplierClick: () -> Unit,
    onOfferClick: () -> Unit,
    onTransferClick: () -> Unit,
) {
    val supplierCount = state.dashboard.suppliers.size
    val offerCount = state.dashboard.offers.size
    val lowStock = state.dashboard.summary.lowStockProducts
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Catalog operations",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Use this panel for supplier setup, offers, imports, and restock attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (lowStock > 0) "$lowStock LOW" else "READY",
                tone = if (lowStock > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            )
        }
        BookingMetricGrid(
            metrics = listOf(
                BookingMetric("Suppliers", supplierCount.toString(), "purchase sources"),
                BookingMetric("Offers", offerCount.toString(), "active promos"),
                BookingMetric("Low stock", lowStock.toString(), "restock queue"),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaSecondaryButton(
                text = "Suppliers",
                onClick = onSupplierClick,
                modifier = Modifier.weight(1f),
            )
            OrmaSecondaryButton(
                text = "Offers",
                onClick = onOfferClick,
                modifier = Modifier.weight(1f),
            )
        }
        OrmaSecondaryButton(
            text = "Import / Export",
            onClick = onTransferClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DashboardMarketingContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    onOpenProducts: (() -> Unit)?,
    wide: Boolean,
) {
    val products = filteredDashboardProducts(state)
    var selectedMarketingTab by rememberSaveable { mutableStateOf("store") }
    DashboardListScaffold(
        eyebrow = "MARKETING",
        title = if (selectedMarketingTab == "store") "Store" else "Sales channels",
        body = when {
            selectedMarketingTab == "store" && state.workspaceId.isNotBlank() ->
                "Share the public ORMA store link or QR code so customers can order, request services, or book appointments."
            selectedMarketingTab == "store" ->
                "Complete workspace setup to create the public ORMA store link and QR code."
            state.dashboard.products.isEmpty() ->
                "Add catalog items first, then connect WhatsApp, Facebook, Instagram, and campaign sync."
            state.hasActiveDashboardFilter() ->
                "${products.size} matching catalog items ready to review for online selling."
            else ->
                "${state.dashboard.products.size} catalog items available for WhatsApp, Meta, and social selling."
        },
        primaryText = if (selectedMarketingTab == "store") "Manage catalog" else "Review catalog",
        onPrimary = { onOpenProducts?.invoke() },
        loading = state.dashboard.loading,
        wide = wide,
    ) {
        OrmaSegmentedRow(
            options = listOf("store", "channels"),
            selected = selectedMarketingTab,
            label = { if (it == "store") "Store" else "Channels" },
            onSelected = { selectedMarketingTab = it },
            modifier = Modifier.widthIn(max = 340.dp),
        )
        if (selectedMarketingTab == "store") {
            if (wide) {
                DashboardModuleWorkspace(
                    wide = true,
                    primary = {
                        DashboardMarketingProductRecords(
                            state = state,
                            products = products,
                        )
                    },
                    secondary = {
                        DashboardMarketingShopLinkCard(state = state, wide = true)
                    },
                )
            } else {
                DashboardMarketingShopLinkCard(state = state, wide = false)
                DashboardMarketingProductRecords(
                    state = state,
                    products = products,
                )
            }
        } else if (wide) {
            DashboardModuleWorkspace(
                wide = true,
                primary = {
                    DashboardMarketingChannelCard(state = state, actions = actions)
                },
                secondary = {
                    DashboardMarketingProductRecords(
                        state = state,
                        products = products,
                    )
                },
            )
        } else {
            DashboardMarketingChannelCard(state = state, actions = actions)
            DashboardMarketingProductRecords(
                state = state,
                products = products,
            )
        }
    }
}

@Composable
private fun DashboardMarketingShopLinkCard(
    state: OnboardingUiState,
    wide: Boolean,
) {
    val workspaceId = state.workspaceId.trim()
    if (workspaceId.isBlank()) {
        DashboardRecordCard {
            OrmaBadge(text = "SHOP LINK", tone = OrmaStatusTone.Warning)
            Text(
                text = "Complete setup to create a shop link",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "After workspace setup, ORMA will create a public catalog link and QR code for customer ordering.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )
        }
        return
    }
    val clipboard = rememberOrmaClipboard()
    val uriHandler = LocalUriHandler.current
    val shopLink = currentOrmaPublicCatalogUrl(workspaceId)
    var actionMessage by rememberSaveable { mutableStateOf<String?>(null) }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OrmaBadge(text = "QR SHOP LINK", tone = OrmaStatusTone.Success)
                Text(
                    text = "Customer catalog",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Print this QR code, place it on the counter, or share the link on WhatsApp, Instagram, and Facebook.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = if (wide) 3 else 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaDashboardIconBubble(modifier = Modifier.size(42.dp)) {
                DashboardQrGlyph(
                    color = OrmaColors.Accent,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            OrmaQrCode(
                value = shopLink,
                modifier = Modifier.widthIn(max = if (wide) 220.dp else 250.dp),
            )
        }
        OrmaKeyValueList(
            rows = listOf(
                "Shop link" to shopLink,
                "Use for" to "QR menu, catalog, booking, and counter ordering",
                "Orders" to "Customer requests appear in Orders",
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaSecondaryButton(
                text = "Copy shop link",
                onClick = {
                    actionMessage = if (clipboard.copyText(shopLink)) {
                        "Shop link copied."
                    } else {
                        "Could not copy automatically. Long press the link to copy."
                    }
                },
                modifier = Modifier.weight(1f),
            )
            OrmaSecondaryButton(
                text = "Open shop",
                onClick = {
                    actionMessage = runCatching {
                        uriHandler.openUri(shopLink)
                        "Opening shop link."
                    }.getOrElse {
                        "Could not open this link here."
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        actionMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.startsWith("Could")) OrmaColors.Warning else OrmaColors.Success,
            )
        }
    }
}

@Composable
private fun DashboardMarketingChannelCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    var showMetaSetupSheet by rememberSaveable { mutableStateOf(false) }
    val workspaceId = state.workspaceId.trim()
    val hasCatalogLink = workspaceId.isNotBlank()
    val metaConnection = state.dashboard.metaConnection
    val connected = metaConnection?.connected == true
    val setupSaved = metaConnection != null && metaConnection.status != "not_connected"
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrmaBadge(
                    text = when {
                        connected -> "WHATSAPP LIVE"
                        setupSaved -> "SETUP SAVED"
                        else -> "NOT CONNECTED"
                    },
                    tone = if (connected) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                )
                Text(
                    text = when {
                        connected -> "WhatsApp sales channel is live"
                        setupSaved -> "WhatsApp account details are ready for backend credentials"
                        else -> "Connect each business to its own WhatsApp account"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Use the ORMA catalog for WhatsApp chats, Facebook and Instagram discovery, QR ordering, and future campaign sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaDashboardIconBubble(modifier = Modifier.size(42.dp)) {
                DashboardNavIcon(
                    kind = DashboardNavIconKind.Marketing,
                    color = OrmaColors.IconPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        OrmaKeyValueList(
            rows = listOf(
                "WhatsApp account" to (metaConnection?.businessDisplayName ?: state.workspaceName.ifBlank { "Not set" }),
                "WhatsApp number" to (metaConnection?.whatsappDisplayNumber ?: "Not set"),
                "Setup status" to (metaConnection?.status?.dashboardTitleCase() ?: "Not connected"),
                "Credentials" to (metaConnection?.accessTokenStatus?.dashboardTitleCase() ?: "Not configured"),
                "Messaging" to (metaConnection?.messagingStatus?.dashboardTitleCase() ?: "Not configured"),
                "Ready products" to (metaConnection?.productsReady ?: state.dashboard.products.count {
                    dashboardMarketingReadinessIssues(it).isEmpty()
                }).toString(),
                "Blocked products" to (metaConnection?.productsBlocked ?: state.dashboard.products.count {
                    dashboardMarketingReadinessIssues(it).isNotEmpty()
                }).toString(),
                "Last check" to (metaConnection?.lastSyncAt ?: "Not checked yet"),
                "Public link" to if (hasCatalogLink) currentOrmaPublicCatalogUrl(workspaceId) else "Complete workspace setup",
            ),
        )
        OrmaSecondaryButton(
            text = if (setupSaved) "Manage WhatsApp setup" else "Set up WhatsApp",
            onClick = { showMetaSetupSheet = true },
            enabled = !state.dashboard.loading && !state.dashboard.metaActionLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        OrmaSecondaryButton(
            text = if (state.dashboard.metaActionLoading) "Checking catalog..." else "Check catalog readiness",
            onClick = actions.onSyncMetaCatalog,
            enabled = !state.dashboard.loading && !state.dashboard.metaActionLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!connected) {
            DashboardChecklistRow(text = "Connect Meta Business, WhatsApp Business Account, and catalog before automated sync.")
        }
        DashboardChecklistRow(text = "Public catalog requests already appear in Orders.")
        DashboardChecklistRow(text = "Products need image, price, and stock readiness before campaigns.")
    }
    if (showMetaSetupSheet) {
        MetaConnectionSetupSheet(
            state = state,
            onDismiss = { showMetaSetupSheet = false },
            onSubmit = { draft ->
                actions.onUpdateMetaConnection(draft)
                showMetaSetupSheet = false
            },
        )
    }
}

@Composable
private fun MetaConnectionSetupSheet(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaMetaConnectionDraft) -> Unit,
) {
    val current = state.dashboard.metaConnection
    var draft by remember {
        mutableStateOf(
            OrmaMetaConnectionDraft(
                status = current?.status?.takeIf { it != "not_connected" } ?: "credentials_pending",
                connectionMode = current?.connectionMode ?: "manual_setup",
                businessDisplayName = current?.businessDisplayName ?: state.workspaceName,
                businessId = current?.businessId.orEmpty(),
                whatsappDisplayNumber = current?.whatsappDisplayNumber.orEmpty(),
                whatsappBusinessAccountId = current?.whatsappBusinessAccountId.orEmpty(),
                phoneNumberId = current?.phoneNumberId.orEmpty(),
                catalogId = current?.catalogId.orEmpty(),
                pageId = current?.pageId.orEmpty(),
                instagramBusinessAccountId = current?.instagramBusinessAccountId.orEmpty(),
                scopes = current?.scopes?.takeIf { it.isNotEmpty() }
                    ?: listOf("whatsapp_business_messaging", "catalog_management"),
            ),
        )
    }
    DashboardFormSheet(
        title = "WhatsApp Business setup",
        body = "Save this workspace's own Meta Business and WhatsApp account identifiers. Secret access tokens stay backend-only.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(
            value = draft.businessDisplayName,
            onValueChange = { draft = draft.copy(businessDisplayName = it.take(120)) },
            label = "Business display name",
            placeholder = state.workspaceName.ifBlank { "Business name" },
        )
        OrmaTextField(
            value = draft.whatsappDisplayNumber,
            onValueChange = { draft = draft.copy(whatsappDisplayNumber = it.filter { char -> char.isDigit() || char == '+' || char == ' ' }.take(32)) },
            label = "WhatsApp display number",
            placeholder = "+91 98765 43210",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        OrmaTextField(
            value = draft.businessId,
            onValueChange = { draft = draft.copy(businessId = it.filter(Char::isDigit).take(40)) },
            label = "Meta Business ID",
            placeholder = "Business Manager ID",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OrmaTextField(
            value = draft.whatsappBusinessAccountId,
            onValueChange = { draft = draft.copy(whatsappBusinessAccountId = it.filter(Char::isDigit).take(40)) },
            label = "WhatsApp Business Account ID",
            placeholder = "WABA ID",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OrmaTextField(
            value = draft.phoneNumberId,
            onValueChange = { draft = draft.copy(phoneNumberId = it.filter(Char::isDigit).take(40)) },
            label = "Phone Number ID",
            placeholder = "Cloud API phone number ID",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OrmaTextField(
            value = draft.catalogId,
            onValueChange = { draft = draft.copy(catalogId = it.filter(Char::isDigit).take(40)) },
            label = "Meta catalog ID",
            placeholder = "Optional until catalog sync",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(
                value = draft.pageId,
                onValueChange = { draft = draft.copy(pageId = it.filter(Char::isDigit).take(40)) },
                label = "Facebook Page ID",
                placeholder = "Optional",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OrmaTextField(
                value = draft.instagramBusinessAccountId,
                onValueChange = { draft = draft.copy(instagramBusinessAccountId = it.filter(Char::isDigit).take(40)) },
                label = "Instagram ID",
                placeholder = "Optional",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        DashboardModuleChecklistCard(
            title = "Next backend step",
            items = listOf(
                "Owner connects the business's own WhatsApp account.",
                "Backend stores and refreshes Meta credentials securely.",
                "Then ORMA can send approved WhatsApp templates and sync catalog items.",
            ),
            tertiaryText = current?.accessTokenStatus?.dashboardTitleCase() ?: "Credentials pending",
        )
        OrmaActionRow(
            primaryText = if (state.dashboard.metaActionLoading) "Saving..." else "Save WhatsApp setup",
            onPrimary = {
                onSubmit(
                    draft.copy(
                        status = if (draft.status == "connected") "connected" else "credentials_pending",
                        connectionMode = "manual_setup",
                    ),
                )
            },
            primaryEnabled = !state.dashboard.metaActionLoading &&
                draft.businessDisplayName.trim().length >= 2 &&
                (
                    draft.whatsappDisplayNumber.trim().isNotBlank() ||
                        draft.whatsappBusinessAccountId.trim().isNotBlank() ||
                        draft.phoneNumberId.trim().isNotBlank()
                    ),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun DashboardMarketingProductRecords(
    state: OnboardingUiState,
    products: List<OrmaProduct>,
) {
    val readinessByProduct = state.dashboard.metaConnection
        ?.productReadiness
        ?.associateBy { it.productId }
        .orEmpty()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (products.isEmpty()) {
            DashboardEmptyModuleCard(
                icon = DashboardNavIconKind.Marketing,
                title = if (state.hasActiveDashboardFilter()) "No matching products" else "No products to market yet",
                body = if (state.hasActiveDashboardFilter()) {
                    "Try another product name, SKU, barcode, supplier, or stock filter."
                } else {
                    "Add real catalog items before creating WhatsApp, Facebook, Instagram, or QR selling flows."
                },
            )
        } else {
            products.forEach { product ->
                DashboardMarketingProductRow(
                    product = product,
                    readiness = readinessByProduct[product.id],
                )
            }
        }
    }
}

@Composable
private fun DashboardMarketingProductRow(
    product: OrmaProduct,
    readiness: OrmaMetaProductReadiness?,
) {
    val readyIssues = readiness?.issues ?: dashboardMarketingReadinessIssues(product)
    val ready = readiness?.ready ?: readyIssues.isEmpty()
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (!product.imageUrl.isNullOrBlank()) {
                OrmaRemoteImage(
                    url = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(OrmaShapes.SmallCard),
                )
            } else {
                OrmaDashboardIconBubble(modifier = Modifier.size(54.dp)) {
                    DashboardNavIcon(
                        kind = DashboardNavIconKind.Marketing,
                        color = OrmaColors.IconPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOfNotNull(product.sku, product.barcode, product.supplierName).joinToString(" / ")
                        .ifBlank { product.unit },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (ready) "READY" else "FIX ${readyIssues.size}",
                tone = if (ready) OrmaStatusTone.Success else OrmaStatusTone.Warning,
            )
        }
        OrmaKeyValueList(
            rows = listOf(
                "Price" to dashboardMoney(product.sellingPrice, product.currency),
                "Stock" to if (product.trackStock) "${product.stockQuantity} ${product.unit}" else "Service / no stock",
                "Image" to if (product.imageUrl.isNullOrBlank()) "Missing" else "Ready",
                "Meta state" to (readiness?.status?.dashboardTitleCase() ?: "Not checked"),
            ),
        )
        DashboardMarketingChannelChips()
        if (readyIssues.isNotEmpty()) {
            readyIssues.forEach { issue ->
                DashboardChecklistRow(text = issue)
            }
        }
    }
}

@Composable
private fun DashboardMarketingChannelChips() {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("WhatsApp", "Facebook", "Instagram", "QR catalog").forEach { channel ->
            OrmaBadge(
                text = channel.uppercase(),
                tone = OrmaStatusTone.Info,
            )
        }
    }
}

private fun dashboardMarketingReadinessIssues(product: OrmaProduct): List<String> = buildList {
    if (product.status.lowercase() != "active") {
        add("Set product status active before sharing.")
    }
    if ((product.sellingPrice.toDoubleOrNull() ?: 0.0) <= 0.0) {
        add("Add a selling price for online customers.")
    }
    if (product.imageUrl.isNullOrBlank()) {
        add("Add a product image for social selling.")
    }
    if (product.trackStock && product.lowStock) {
        add("Check stock before promoting this item.")
    }
}

private fun String.dashboardTitleCase(): String =
    split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.uppercase() }
        }

@Composable
private fun DashboardOrderPipelineCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    val orders = filteredDashboardOrders(state)
    val orderType = state.activeDashboardOrderType()
    val selectedOrderType = state.selectedDashboardOrderTypeFilter()
    val selected = state.dashboard.filters.orderStatus
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = if (selectedOrderType == "all") "Operations pipeline" else "${orderType.orderTypeLabel()} pipeline",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (selectedOrderType == "all") {
                        "Tap a stage to focus sales, service requests, and bookings as work is accepted, paid, and completed."
                    } else {
                        "Tap a stage to focus the queue. Move ${orderType.orderProgressPlural()} forward as work is accepted, paid, and completed."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "${orders.size} TOTAL",
                tone = OrmaStatusTone.Info,
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardStatusCountChip(
                label = "All",
                count = orders.size,
                selected = selected == "all",
                tone = OrmaStatusTone.Info,
                onClick = { actions.onOrderStatusFilterChange("all") },
            )
            DashboardOrderStatuses.forEach { status ->
                val count = orders.count { it.status == status }
                DashboardStatusCountChip(
                    label = status.dashboardStatusLabel(),
                    count = count,
                    selected = selected == status,
                    tone = when (status) {
                        "paid", "completed" -> OrmaStatusTone.Success
                        "cancelled" -> OrmaStatusTone.Danger
                        "part_paid" -> OrmaStatusTone.Warning
                        else -> OrmaStatusTone.Info
                    },
                    onClick = { actions.onOrderStatusFilterChange(status) },
                )
            }
        }
    }
}

@Composable
private fun DashboardStatusCountChip(
    label: String,
    count: Int,
    selected: Boolean,
    tone: OrmaStatusTone,
    onClick: () -> Unit,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        onClick = onClick,
        shape = OrmaShapes.Capsule,
        color = if (selected) OrmaColors.Accent else colors.container,
        contentColor = if (selected) OrmaColors.OnAccent else colors.content,
        border = BorderStroke(0.8.dp, if (selected) OrmaColors.Accent else colors.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) OrmaColors.OnAccent else colors.content,
                maxLines = 1,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) OrmaColors.OnAccent else colors.content.copy(alpha = 0.72f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DashboardMiniMetricCell(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    tone: OrmaStatusTone = OrmaStatusTone.Info,
) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = modifier,
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWideDataSurface(
    title: String,
    columns: List<String>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                columns.forEach { column ->
                    Text(
                        text = column.uppercase(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            HorizontalDivider(color = OrmaColors.Divider.copy(alpha = 0.52f))
            content()
        }
    }
}

@Composable
private fun DashboardWideCell(
    primary: String,
    secondary: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = primary,
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        secondary?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardWideOrderRow(
    order: OrmaOrder,
    onOpen: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrmaShapes.SmallCard)
            .clickable(onClick = onOpen)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardWideCell(
            primary = order.orderNumber.ifBlank { "Order" },
            secondary = order.source.dashboardTitleCase(),
            modifier = Modifier.weight(1f),
        )
        DashboardWideCell(
            primary = order.customerName ?: "Walk-in customer",
            secondary = order.fulfillmentType.dashboardTitleCase(),
            modifier = Modifier.weight(1f),
        )
        DashboardWideCell(
            primary = order.orderType.orderTypeLabel(),
            secondary = order.scheduledAt?.dashboardDateLabel(),
            modifier = Modifier.weight(1f),
        )
        DashboardWideCell(
            primary = dashboardMoney(order.total, order.currency),
            secondary = "Paid ${dashboardMoney(order.paidTotal, order.currency)}",
            modifier = Modifier.weight(1f),
        )
        DashboardOrderStatusDropdown(
            order = order,
            onStatusChange = onStatusChange,
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(color = OrmaColors.Divider)
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DashboardCustomerTable(
    customers: List<OrmaCustomer>,
    selectedCustomer: OrmaCustomer?,
    sortKey: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    onCustomerClick: (OrmaCustomer) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp),
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrmaColors.CardBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardSaleHeaderCell(
                            text = "Name",
                            sortKey = DashboardCustomerSortName,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.18f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Contact",
                            sortKey = DashboardCustomerSortContact,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.18f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Location",
                            sortKey = DashboardCustomerSortLocation,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.12f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Status",
                            sortKey = DashboardCustomerSortStatus,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.72f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Action",
                            sortKey = null,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.width(126.dp),
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        thickness = 0.8.dp,
                        color = OrmaColors.CellBackground.copy(alpha = 0.72f),
                    )
                }
            }
        }
        itemsIndexed(
            items = customers,
            key = { _, customer -> customer.id },
        ) { index, customer ->
            DashboardWideCustomerRow(
                customer = customer,
                selected = selectedCustomer?.id == customer.id,
                zebra = index % 2 == 1,
                onDetailsClick = { onCustomerClick(customer) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DashboardProductTable(
    products: List<OrmaProduct>,
    sortKey: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    onEditClick: (OrmaProduct) -> Unit,
    onStockClick: (OrmaProduct) -> Unit,
    onImageClick: (OrmaProduct) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp),
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrmaColors.CardBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardSaleHeaderCell(
                            text = "Item",
                            sortKey = DashboardProductSortItem,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(2.08f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Type",
                            sortKey = DashboardProductSortType,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.78f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Stock",
                            sortKey = DashboardProductSortStock,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Price",
                            sortKey = DashboardProductSortPrice,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.95f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Actions",
                            sortKey = null,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.width(DashboardProductActionColumnWidth),
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        thickness = 0.8.dp,
                        color = OrmaColors.CellBackground.copy(alpha = 0.72f),
                    )
                }
            }
        }
        itemsIndexed(
            items = products,
            key = { _, product -> product.id },
        ) { index, product ->
            DashboardWideProductRow(
                product = product,
                zebra = index % 2 == 1,
                onEditClick = { onEditClick(product) },
                onStockClick = { onStockClick(product) },
                onImageClick = { onImageClick(product) },
            )
        }
    }
}

@Composable
private fun DashboardWideCustomerRow(
    customer: OrmaCustomer,
    selected: Boolean = false,
    zebra: Boolean = false,
    onDetailsClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackground = when {
        selected -> OrmaColors.ScreenBackground.copy(alpha = 0.62f)
        hovered -> OrmaColors.ScreenBackground.copy(alpha = 0.44f)
        zebra -> OrmaColors.ScreenBackground.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OrmaShapes.SmallCard)
                .background(rowBackground)
                .hoverable(interactionSource)
                .clickable(onClick = onDetailsClick)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardWideCell(
                primary = customer.name,
                secondary = customer.notes,
                modifier = Modifier.weight(1.18f),
            )
            DashboardWideCell(
                primary = listOfNotNull(customer.phoneNumber, customer.email).joinToString(" / ").ifBlank { "No contact" },
                modifier = Modifier.weight(1.18f),
            )
            DashboardWideCell(
                primary = listOfNotNull(customer.city, customer.region, customer.country).joinToString(", ").ifBlank { "No location" },
                secondary = customer.addressLine,
                modifier = Modifier.weight(1.12f),
            )
            Box(modifier = Modifier.weight(0.72f), contentAlignment = Alignment.CenterStart) {
                OrmaBadge(
                    text = customer.status.dashboardTeamStatusLabel().uppercase(),
                    tone = if (customer.status.lowercase() == "active") OrmaStatusTone.Success else OrmaStatusTone.Info,
                )
            }
            Box(modifier = Modifier.width(126.dp), contentAlignment = Alignment.CenterStart) {
                DashboardTableActionButton(
                    text = "Details",
                    iconKind = OrmaFlatIconKind.View,
                    onClick = onDetailsClick,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp),
            thickness = 0.8.dp,
            color = if (selected || hovered) {
                OrmaColors.CellBackground.copy(alpha = 0.86f)
            } else {
                OrmaColors.CellBackground.copy(alpha = 0.62f)
            },
        )
    }
}

@Composable
private fun DashboardWideProductRow(
    product: OrmaProduct,
    zebra: Boolean = false,
    onEditClick: () -> Unit,
    onStockClick: () -> Unit,
    onImageClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackground = when {
        hovered -> OrmaColors.ScreenBackground.copy(alpha = 0.44f)
        zebra -> OrmaColors.ScreenBackground.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OrmaShapes.SmallCard)
                .background(rowBackground)
                .hoverable(interactionSource)
                .clickable(onClick = onEditClick)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(2.08f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    OrmaRemoteImage(
                        url = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(OrmaShapes.CheckoutButton),
                    )
                } else {
                    OrmaDashboardIconBubble(modifier = Modifier.size(38.dp)) {
                        DashboardNavIcon(
                            kind = DashboardNavIconKind.Products,
                            color = OrmaColors.IconPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                DashboardWideCell(
                    primary = product.name,
                    secondary = listOfNotNull(product.sku, product.barcode, product.supplierName)
                        .joinToString(" / ")
                        .ifBlank { product.categoryName ?: "Uncategorized" },
                    modifier = Modifier.weight(1f),
                )
            }
            Box(modifier = Modifier.weight(0.78f), contentAlignment = Alignment.CenterStart) {
                DashboardItemTypeChip(itemType = product.itemType)
            }
            DashboardWideCell(
                primary = when {
                    product.itemType != "product" -> "No stock"
                    product.trackStock -> "${product.stockQuantity} ${product.unit}"
                    else -> "Not tracked"
                },
                secondary = product.expiryDate.productExpiryLabel() ?: if (product.lowStock) "Low stock" else null,
                modifier = Modifier.weight(1f),
            )
            DashboardWideCell(
                primary = dashboardMoney(product.sellingPrice, product.currency),
                secondary = "Tax ${product.taxRate}%",
                modifier = Modifier.weight(0.95f),
            )
            Row(
                modifier = Modifier.width(DashboardProductActionColumnWidth),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardTableActionButton(
                    text = "Edit",
                    iconKind = OrmaFlatIconKind.Edit,
                    onClick = onEditClick,
                )
                DashboardTableActionButton(
                    text = "Image",
                    onClick = onImageClick,
                )
                if (product.itemType == "product") {
                    DashboardTableActionButton(
                        text = "Stock",
                        onClick = onStockClick,
                    )
                } else {
                    Spacer(modifier = Modifier.width(108.dp))
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp),
            thickness = 0.8.dp,
            color = if (hovered) {
                OrmaColors.CellBackground.copy(alpha = 0.86f)
            } else {
                OrmaColors.CellBackground.copy(alpha = 0.62f)
            },
        )
    }
}

@Composable
private fun DashboardItemTypeChip(
    itemType: String,
) {
    val tone = if (itemType == "product") OrmaStatusTone.Success else OrmaStatusTone.Info
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        modifier = Modifier
            .height(30.dp)
            .widthIn(min = 78.dp, max = 104.dp),
        shape = OrmaShapes.Capsule,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(0.8.dp, colors.border.copy(alpha = 0.72f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = itemType.sellableItemTypeLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DashboardTableActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconKind: OrmaFlatIconKind? = null,
) {
    val resolvedIconKind = iconKind ?: dashboardCompactTableIconForAction(text)
    val buttonWidth = when {
        resolvedIconKind == null -> 74.dp
        text.length <= 4 -> 88.dp
        text.length <= 7 -> 108.dp
        else -> 130.dp
    }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(buttonWidth)
            .height(34.dp),
        enabled = enabled,
        shape = OrmaShapes.Capsule,
        color = if (enabled) Color.Transparent else OrmaColors.Accent.copy(alpha = 0.05f),
        contentColor = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = if (enabled) 0.14f else 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (resolvedIconKind != null) {
                OrmaFlatIcon(
                    kind = resolvedIconKind,
                    modifier = Modifier.size(15.dp),
                    color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun dashboardCompactTableIconForAction(text: String): OrmaFlatIconKind? =
    when (text.trim().lowercase()) {
        "open", "view" -> OrmaFlatIconKind.View
        "edit" -> OrmaFlatIconKind.Edit
        "image" -> OrmaFlatIconKind.Image
        "stock" -> OrmaFlatIconKind.Stock
        else -> null
    }

private fun dashboardFlatIconForAction(text: String): OrmaFlatIconKind? {
    val normalized = text.trim().lowercase()
    return when {
        normalized in setOf("refresh", "syncing", "searching", "apply", "checking") -> OrmaFlatIconKind.Refresh
        normalized in setOf("clear", "reset", "close", "close details") -> OrmaFlatIconKind.Close
        normalized in setOf("back", "back to invoices") -> OrmaFlatIconKind.Back
        normalized == "previous" -> OrmaFlatIconKind.ChevronLeft
        normalized == "next" -> OrmaFlatIconKind.ChevronRight
        normalized.startsWith("create") || normalized.startsWith("add") || normalized.startsWith("new") || normalized.startsWith("book") -> OrmaFlatIconKind.Plus
        normalized.startsWith("edit") || normalized.startsWith("update") || normalized == "saving..." -> OrmaFlatIconKind.Edit
        normalized.startsWith("open") || normalized.startsWith("preview") || normalized == "full details" || normalized == "view details" -> OrmaFlatIconKind.View
        normalized.contains("schedule") || normalized.contains("dispatch") -> OrmaFlatIconKind.Calendar
        normalized.contains("image") -> OrmaFlatIconKind.Image
        normalized.contains("stock") -> OrmaFlatIconKind.Stock
        normalized.contains("print") -> OrmaFlatIconKind.Print
        normalized.contains("download") -> OrmaFlatIconKind.Download
        normalized.contains("upload") -> OrmaFlatIconKind.Upload
        normalized.contains("import") || normalized.contains("export") -> OrmaFlatIconKind.Download
        normalized.contains("category") -> OrmaFlatIconKind.Category
        else -> null
    }
}

@Composable
private fun DashboardOrderRecords(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
    onOpenOrder: (OrmaOrder) -> Unit,
    onStatusChange: (OrmaOrder, String) -> Unit,
) {
    val orders = filteredDashboardOrders(state)
    val selectedOrderType = state.selectedDashboardOrderTypeFilter()
    if (orders.isEmpty()) {
        val orderType = state.activeDashboardOrderType()
        DashboardEmptyModuleCard(
            icon = DashboardNavIconKind.Orders,
            title = if (state.hasActiveDashboardFilter()) {
                if (selectedOrderType == "all") "No matching orders" else "No matching ${orderType.orderProgressPlural()}"
            } else {
                if (selectedOrderType == "all") "No orders yet" else orderType.emptyOrderTitle()
            },
            body = if (state.hasActiveDashboardFilter()) {
                "Try a different search or status filter."
            } else {
                if (selectedOrderType == "all") {
                    "Create the first sale, service request, or appointment booking."
                } else {
                    orderType.emptyOrderBody()
                }
            },
        )
        return
    }
    if (wide) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardWideDataSurface(
                title = if (selectedOrderType == "all") "Order records" else "${state.activeDashboardOrderType().orderTypeLabel()} records",
                columns = listOf("Order", "Customer", "Type", "Total", "Status"),
            ) {
                orders.forEach { order ->
                    DashboardWideOrderRow(
                        order = order,
                        onOpen = { onOpenOrder(order) },
                        onStatusChange = { status -> onStatusChange(order, status) },
                    )
                }
            }
            DashboardPaginationControls(
                pagination = state.dashboard.orderPagination,
                wide = true,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Orders, it) },
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            orders.forEach { order ->
                DashboardOrderRow(
                    order = order,
                    compact = false,
                    onOpen = { onOpenOrder(order) },
                    onStatusChange = { status -> onStatusChange(order, status) },
                )
            }
            DashboardPaginationControls(
                pagination = state.dashboard.orderPagination,
                wide = false,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Orders, it) },
            )
        }
    }
}

@Composable
private fun DashboardBookingDetailsScreen(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onUpdateOrder: (OrmaOrderDraft) -> Unit,
    onDownloadStatus: (String) -> Unit,
    actionLoading: Boolean,
) {
    if (!wide) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        OrmaBadge(
                            text = order.orderType.orderSectionEyebrow(),
                            tone = OrmaStatusTone.Info,
                        )
                        Text(
                            text = order.orderType.orderTypeLabel(),
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${order.orderNumber.ifBlank { "Order" }} / ${order.customerName ?: "Walk-in customer"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(
                        text = order.status.dashboardStatusLabel().uppercase(),
                        tone = order.status.dashboardOrderStatusTone(),
                    )
                }
                OrmaActionRow(
                    primaryText = "Edit details",
                    onPrimary = onEdit,
                    primaryEnabled = !state.dashboard.loading,
                )
            }
            DashboardBookingDetailsContent(
                state = state,
                order = order,
                wide = false,
                onStatusChange = onStatusChange,
                onUpdateOrder = onUpdateOrder,
                onDownloadStatus = onDownloadStatus,
                actionLoading = actionLoading,
            )
        }
        return
    }
    DashboardListScaffold(
        eyebrow = order.orderType.orderSectionEyebrow(),
        title = "${order.orderType.orderTypeLabel()} details",
        body = "${order.orderNumber.ifBlank { "Booking" }} · ${order.customerName ?: "Walk-in customer"}",
        primaryText = "Edit details",
        onPrimary = onEdit,
        secondaryText = "Back",
        onSecondary = onBack,
        loading = state.dashboard.loading,
        wide = wide,
    ) {
        DashboardBookingDetailsContent(
            state = state,
            order = order,
            wide = wide,
            onStatusChange = onStatusChange,
            onUpdateOrder = onUpdateOrder,
            onDownloadStatus = onDownloadStatus,
            actionLoading = actionLoading,
        )
    }
}

@Composable
private fun DashboardBookingDetailsSheet(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onUpdateOrder: (OrmaOrderDraft) -> Unit,
    onDownloadStatus: (String) -> Unit,
    actionLoading: Boolean,
) {
    DashboardFormSheet(
        title = "${order.orderType.orderTypeLabel()} details",
        body = "${order.orderNumber.ifBlank { "Booking" }} · ${order.customerName ?: "Walk-in customer"}",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        val closeSheet = LocalSmoothSheetDismiss.current ?: onDismiss
        DashboardBookingDetailsContent(
            state = state,
            order = order,
            wide = wide,
            onStatusChange = onStatusChange,
            onUpdateOrder = onUpdateOrder,
            onDownloadStatus = onDownloadStatus,
            actionLoading = actionLoading,
        )
        OrmaActionRow(
            secondaryText = "Close",
            onSecondary = closeSheet,
            primaryText = "Edit details",
            onPrimary = onEdit,
            primaryEnabled = !state.dashboard.loading,
        )
    }
}

@Composable
private fun DashboardBookingDetailsContent(
    state: OnboardingUiState,
    order: OrmaOrder,
    wide: Boolean,
    onStatusChange: (String) -> Unit,
    onUpdateOrder: (OrmaOrderDraft) -> Unit,
    onDownloadStatus: (String) -> Unit,
    actionLoading: Boolean,
) {
    var showDeliverySheet by rememberSaveable(order.id) { mutableStateOf(false) }
    var showDispatchSheet by rememberSaveable(order.id) { mutableStateOf(false) }
    if (wide) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1.35f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BookingDetailsSummaryCard(order = order)
            BookingDetailsDocumentsCard(
                state = state,
                order = order,
                onDownloadStatus = onDownloadStatus,
            )
            BookingDetailsLineItemsCard(order = order)
        }
            Column(
                modifier = Modifier.weight(0.92f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BookingDetailsCustomerCard(
                    order = order,
                    actionLoading = actionLoading,
                    onDeliveryClick = { showDeliverySheet = true },
                )
                BookingDetailsFulfillmentCard(
                    order = order,
                    onStatusChange = onStatusChange,
                    onScheduleDispatch = { showDispatchSheet = true },
                    onMarkCompleted = { onStatusChange("completed") },
                    actionLoading = actionLoading,
                )
            }
        }
    } else {
        BookingDetailsSummaryCard(order = order)
        BookingDetailsDocumentsCard(
            state = state,
            order = order,
            onDownloadStatus = onDownloadStatus,
        )
        BookingDetailsLineItemsCard(order = order)
        BookingDetailsCustomerCard(
            order = order,
            actionLoading = actionLoading,
            onDeliveryClick = { showDeliverySheet = true },
        )
        BookingDetailsFulfillmentCard(
            order = order,
            onStatusChange = onStatusChange,
            onScheduleDispatch = { showDispatchSheet = true },
            onMarkCompleted = { onStatusChange("completed") },
            actionLoading = actionLoading,
        )
    }
    if (showDeliverySheet) {
        BookingDetailsDeliveryAddressSheet(
            order = order,
            wide = wide,
            actionLoading = actionLoading,
            onDismiss = { showDeliverySheet = false },
            onSubmit = { draft ->
                onUpdateOrder(draft)
                showDeliverySheet = false
            },
        )
    }
    if (showDispatchSheet) {
        BookingDetailsDispatchScheduleSheet(
            order = order,
            wide = wide,
            actionLoading = actionLoading,
            onDismiss = { showDispatchSheet = false },
            onSubmit = { draft ->
                onUpdateOrder(draft)
                showDispatchSheet = false
            },
        )
    }
}

@Composable
private fun BookingDetailsSummaryCard(order: OrmaOrder) {
    val partPaid = order.isPartPaidRecord()
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "${order.orderType.orderTypeLabel()} value",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = order.orderType.orderSheetBody(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = order.status.dashboardStatusLabel().uppercase(),
                tone = order.status.dashboardOrderStatusTone(),
            )
        }
        BookingMetricGrid(
            metrics = listOf(
                BookingMetric("Total", dashboardMoney(order.total, order.currency), "${order.itemCount} items"),
                BookingMetric("Paid", dashboardMoney(order.paidTotal, order.currency), order.paymentMode.paymentModeLabel()),
                BookingMetric(
                    "Balance",
                    order.balanceDueText(),
                    if (order.balanceDueValue() > 0.0) "payable" else "settled",
                ),
            ),
        )
        if (partPaid) {
            BookingDetailsPartPaidBreakdown(order = order)
        }
        DashboardChecklistRow(
            text = when {
                partPaid -> {
                    "Part paid: ${dashboardMoney(order.paidTotal, order.currency)} collected, ${order.balanceDueText()} balance due."
                }
                order.balanceDueValue() > 0.0 -> {
                    "Collect ${order.balanceDueText()} before closing this ${order.orderType.orderTypeLabel().lowercase()}."
                }
                else -> {
                    "Payment is fully collected for this ${order.orderType.orderTypeLabel().lowercase()}."
                }
            },
        )
    }
}

@Composable
private fun BookingDetailsPartPaidBreakdown(order: OrmaOrder) {
    val colors = org.orma.project_90.designsystem.ormaStatusColors(OrmaStatusTone.Warning)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = colors.container.copy(alpha = 0.62f),
        contentColor = colors.content,
        border = BorderStroke(0.8.dp, colors.border.copy(alpha = 0.72f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Part payment",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = "This record is not fully settled yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = "BALANCE DUE",
                    tone = OrmaStatusTone.Warning,
                )
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Order total" to dashboardMoney(order.total, order.currency),
                    "Paid amount" to dashboardMoney(order.paidTotal, order.currency),
                    "Balance amount" to order.balanceDueText(),
                    "Payment mode" to order.paymentMode.paymentModeLabel(),
                ),
            )
        }
    }
}

@Composable
private fun BookingDetailsDocumentsCard(
    state: OnboardingUiState,
    order: OrmaOrder,
    onDownloadStatus: (String) -> Unit,
) {
    val exporter = rememberOrmaOrderDocumentExporter()
    var documentStatus by rememberSaveable(order.id) { mutableStateOf<String?>(null) }
    fun reportDocumentStatus(message: String) {
        documentStatus = message
        onDownloadStatus(message)
    }
    fun downloadInvoice() {
        val document = orderInvoicePdfDocument(state = state, order = order)
        reportDocumentStatus(if (exporter.downloadPdf(fileName = document.fileName, pdfBase64 = document.pdfBase64)) {
            "Invoice PDF download complete."
        } else {
            "Invoice PDF download is not available on this device yet."
        })
    }
    fun downloadReceipt() {
        val document = orderReceiptPdfDocument(state = state, order = order)
        reportDocumentStatus(if (exporter.downloadPdf(fileName = document.fileName, pdfBase64 = document.pdfBase64)) {
            "Receipt PDF download complete."
        } else {
            "Receipt PDF download is not available on this device yet."
        })
    }
    fun printReceipt() {
        val document = orderReceiptDocument(state = state, order = order)
        reportDocumentStatus(if (exporter.printHtml(title = document.title, html = document.html)) {
            "Receipt print view opened."
        } else {
            "Receipt printing is not available on this device yet."
        })
    }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Invoice, receipt, and counter printout for this order.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "FILES",
                tone = OrmaStatusTone.Info,
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 520.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DashboardWideActionButton(
                        text = "Download PDF",
                        onClick = ::downloadInvoice,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DashboardWideActionButton(
                        text = "Receipt PDF",
                        onClick = ::downloadReceipt,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DashboardWideActionButton(
                        text = "Print receipt",
                        onClick = ::printReceipt,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DashboardWideActionButton(
                        text = "Download PDF",
                        onClick = ::downloadInvoice,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardWideActionButton(
                        text = "Receipt PDF",
                        onClick = ::downloadReceipt,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardWideActionButton(
                        text = "Print receipt",
                        onClick = ::printReceipt,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        documentStatus?.let { status ->
            DashboardChecklistRow(text = status)
        }
    }
}

@Composable
private fun BookingDetailsLineItemsCard(order: OrmaOrder) {
    val items = order.items
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Items and services",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (items.isEmpty()) "No line items returned for this record." else "${items.size} line items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaBadge(
                text = dashboardMoney(order.total, order.currency),
                tone = OrmaStatusTone.Success,
            )
        }
        if (items.isEmpty()) {
            DashboardChecklistRow(text = "Use Edit details to add products, services, or manual invoice lines.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items.forEachIndexed { index, item ->
                    BookingLineItemRow(
                        index = index,
                        item = item,
                        currency = order.currency,
                    )
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = OrmaColors.Divider)
                    }
                }
            }
            HorizontalDivider(color = OrmaColors.Divider)
            OrmaKeyValueList(
                rows = listOf(
                    "Subtotal" to dashboardMoney(order.subtotal, order.currency),
                    "Tax" to dashboardMoney(order.taxTotal, order.currency),
                    "Discount" to dashboardMoney(order.discountTotal, order.currency),
                    "Total" to dashboardMoney(order.total, order.currency),
                ),
            )
        }
    }
}

@Composable
private fun BookingLineItemRow(
    index: Int,
    item: OrmaOrderItem,
    currency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrmaDashboardIconBubble(modifier = Modifier.size(38.dp)) {
            Text(
                text = (index + 1).toString(),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.productName ?: item.description.ifBlank { "Line item ${index + 1}" },
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${orderQuantityText(item.quantity.toDoubleOrNull().orZero())} x ${dashboardMoney(item.unitPrice, currency)} · Tax ${item.taxRate.ifBlank { "0" }}%",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = dashboardMoney(item.lineTotal, currency),
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BookingDetailsCustomerCard(
    order: OrmaOrder,
    actionLoading: Boolean = false,
    onDeliveryClick: (() -> Unit)? = null,
) {
    val rows = buildList {
        add("Customer" to (order.customerName ?: "Walk-in customer"))
        order.customerPhoneNumber?.takeIf { it.isNotBlank() }?.let { add("Phone" to it) }
        order.customerEmail?.takeIf { it.isNotBlank() }?.let { add("Email" to it) }
        order.customerTaxNumber?.takeIf { it.isNotBlank() }?.let { add("GST/VAT" to it) }
        add("Delivery location" to order.deliveryLocationText())
    }
    DashboardRecordCard {
        Text(
            text = "Customer and delivery",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        OrmaKeyValueList(rows = rows)
        if (order.deliveryLocationMissing()) {
            DashboardChecklistRow(text = "Add a delivery address when this booking needs dispatch or home service.")
        }
        onDeliveryClick?.let { deliveryClick ->
            DashboardWideActionButton(
                text = if (order.deliveryLocationText() == "Not required" || order.deliveryLocationMissing()) {
                    "Add address"
                } else {
                    "Edit address"
                },
                onClick = deliveryClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !actionLoading,
            )
        }
    }
}

@Composable
private fun BookingDetailsFulfillmentCard(
    order: OrmaOrder,
    onStatusChange: (String) -> Unit,
    onScheduleDispatch: (() -> Unit)? = null,
    onMarkCompleted: (() -> Unit)? = null,
    actionLoading: Boolean = false,
) {
    DashboardRecordCard {
        Text(
            text = "Status and fulfilment",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        DashboardOrderStatusDropdown(
            order = order,
            onStatusChange = onStatusChange,
        )
        OrmaKeyValueList(
            rows = listOfNotNull(
                "Fulfilment" to order.fulfillmentType.fulfillmentModeLabel(),
                "Payment" to order.paymentMode.paymentModeLabel(),
                order.scheduledAt?.takeIf { it.isNotBlank() }?.let { "Scheduled" to it.dashboardDateLabel() },
                "Source" to order.source.dashboardTitleCase(),
                order.notes?.takeIf { it.isNotBlank() }?.let { "Notes" to it },
            ),
        )
        if (onScheduleDispatch != null || onMarkCompleted != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 460.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        onScheduleDispatch?.let { scheduleDispatch ->
                            DashboardWideActionButton(
                                text = if (order.scheduledAt.isNullOrBlank()) "Schedule dispatch" else "Edit dispatch",
                                onClick = scheduleDispatch,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !actionLoading,
                            )
                        }
                        onMarkCompleted?.let { markCompleted ->
                            DashboardWideActionButton(
                                text = "Mark completed",
                                onClick = markCompleted,
                                modifier = Modifier.fillMaxWidth(),
                                primary = true,
                                enabled = !actionLoading && order.status !in setOf("completed", "cancelled"),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        onScheduleDispatch?.let { scheduleDispatch ->
                            DashboardWideActionButton(
                                text = if (order.scheduledAt.isNullOrBlank()) "Schedule dispatch" else "Edit dispatch",
                                onClick = scheduleDispatch,
                                modifier = Modifier.weight(1f),
                                enabled = !actionLoading,
                            )
                        }
                        onMarkCompleted?.let { markCompleted ->
                            DashboardWideActionButton(
                                text = "Mark completed",
                                onClick = markCompleted,
                                modifier = Modifier.weight(1f),
                                primary = true,
                                enabled = !actionLoading && order.status !in setOf("completed", "cancelled"),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingDetailsDeliveryAddressSheet(
    order: OrmaOrder,
    wide: Boolean,
    actionLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (OrmaOrderDraft) -> Unit,
) {
    var draft by remember(order.id) {
        mutableStateOf(
            order.toOrderDraft().copy(
                fulfillmentType = "delivery",
            ),
        )
    }
    val addressReady = listOf(
        draft.customerAddressLine,
        draft.customerCity,
        draft.customerRegion,
        draft.customerCountry,
    ).any { it.trim().isNotBlank() }
    DashboardFormSheet(
        title = "Delivery address",
        body = "${order.orderNumber.ifBlank { "Order" }} · ${order.customerName ?: "Walk-in customer"}",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        DashboardRecordCard {
            Text(
                text = "Contact",
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            OrmaTextField(
                value = draft.customerName,
                onValueChange = { draft = draft.copy(customerName = it.take(120)) },
                label = "Customer name",
                placeholder = "Walk-in customer",
            )
            OrmaCountryPhoneField(
                value = draft.customerPhoneNumber,
                onValueChange = { draft = draft.copy(customerPhoneNumber = it) },
                label = "Phone",
                supportingText = "Used by staff during dispatch.",
            )
        }
        DashboardRecordCard {
            DashboardDeliveryLocationFields(
                draft = draft,
                onDraftChange = { draft = it.copy(fulfillmentType = "delivery") },
            )
        }
        OrmaActionRow(
            secondaryText = "Cancel",
            onSecondary = onDismiss,
            primaryText = if (actionLoading) "Saving" else "Save address",
            onPrimary = {
                onSubmit(
                    draft.copy(
                        customerName = draft.customerName.trim(),
                        customerPhoneNumber = draft.customerPhoneNumber.trim(),
                        customerAddressLine = draft.customerAddressLine.trim(),
                        customerCity = draft.customerCity.trim(),
                        customerRegion = draft.customerRegion.trim(),
                        customerCountry = draft.customerCountry.trim(),
                        customerPostalCode = draft.customerPostalCode.trim(),
                        fulfillmentType = "delivery",
                    ),
                )
            },
            primaryEnabled = addressReady && !actionLoading,
        )
    }
}

@Composable
private fun BookingDetailsDispatchScheduleSheet(
    order: OrmaOrder,
    wide: Boolean,
    actionLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (OrmaOrderDraft) -> Unit,
) {
    val initialFulfillment = when {
        order.fulfillmentType.isBlank() && order.orderType == "appointment" -> "booking"
        order.fulfillmentType.isBlank() -> "delivery"
        else -> order.fulfillmentType
    }
    var draft by remember(order.id) {
        mutableStateOf(
            order.toOrderDraft().copy(
                fulfillmentType = initialFulfillment,
            ),
        )
    }
    val dispatchReady = draft.scheduledAt.trim().length >= 4
    DashboardFormSheet(
        title = "Schedule dispatch",
        body = "${order.orderNumber.ifBlank { "Order" }} · ${order.fulfillmentType.fulfillmentModeLabel()}",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        DashboardRecordCard {
            Text(
                text = "Dispatch plan",
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            if (draft.orderType != "appointment") {
                DashboardCompactSegmentedPicker(
                    options = draft.orderType.orderFulfillmentOptions(),
                    selected = draft.fulfillmentType,
                    label = { it.fulfillmentModeLabel() },
                    onSelected = { draft = draft.copy(fulfillmentType = it) },
                )
            }
            OrmaCalendarDateTimeField(
                value = draft.scheduledAt,
                onValueChange = { draft = draft.copy(scheduledAt = it) },
                label = "Dispatch date and time",
                placeholder = "Choose dispatch time",
                supportingText = "Used for scheduled delivery, pickup, or service.",
                allowClear = true,
            )
            if (draft.fulfillmentType == "delivery" && draft.requiresDeliveryLocation()) {
                DashboardChecklistRow(
                    text = if (order.deliveryLocationMissing()) {
                        "Add delivery address before sending this out."
                    } else {
                        "Delivery location is attached."
                    },
                )
            }
            OrmaTextField(
                value = draft.notes,
                onValueChange = { draft = draft.copy(notes = it.take(400)) },
                label = "Dispatch notes",
                placeholder = "Optional",
                singleLine = false,
                minLines = 2,
            )
        }
        OrmaActionRow(
            secondaryText = "Cancel",
            onSecondary = onDismiss,
            primaryText = if (actionLoading) "Saving" else "Save dispatch",
            onPrimary = {
                onSubmit(
                    draft.copy(
                        scheduledAt = draft.scheduledAt.trim(),
                        notes = draft.notes.trim(),
                        fulfillmentType = if (draft.orderType == "appointment") "booking" else draft.fulfillmentType,
                    ),
                )
            },
            primaryEnabled = dispatchReady && !actionLoading,
        )
    }
}

private data class BookingMetric(
    val label: String,
    val value: String,
    val detail: String,
)

@Composable
private fun BookingMetricGrid(metrics: List<BookingMetric>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 520.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                metrics.chunked(2).forEach { rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowMetrics.forEach { metric ->
                            DashboardMiniMetricCell(
                                label = metric.label,
                                value = metric.value,
                                detail = metric.detail,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowMetrics.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                metrics.forEach { metric ->
                    DashboardMiniMetricCell(
                        label = metric.label,
                        value = metric.value,
                        detail = metric.detail,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCustomerPulseCard(
    state: OnboardingUiState,
) {
    val customers = state.dashboard.customers
    val reachable = customers.count { !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank() }
    val withAddress = customers.count { !it.addressLine.isNullOrBlank() || !it.city.isNullOrBlank() }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Customer book",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Use this as business memory: contact, location, notes, and repeat-order context.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "${customers.size} SAVED",
                tone = OrmaStatusTone.Info,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Reachable",
                value = reachable.toString(),
                detail = "phone/email",
                modifier = Modifier.weight(1f),
            )
            DashboardMiniMetricCell(
                label = "Delivery",
                value = withAddress.toString(),
                detail = "address saved",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardCustomerRecords(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
    onCustomerClick: (OrmaCustomer) -> Unit,
) {
    val customers = filteredDashboardCustomers(state)
    if (customers.isEmpty()) {
        DashboardEmptyModuleCard(
            icon = DashboardNavIconKind.Customers,
            title = if (state.hasActiveDashboardFilter()) "No matching customers" else "No customers yet",
            body = if (state.hasActiveDashboardFilter()) {
                "Try searching by name, phone, email, or city."
            } else {
                "Save contact details for repeat orders, delivery addresses, reminders, and notes."
            },
        )
        return
    }
    if (wide) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardWideDataSurface(
                title = "Customer records",
                columns = listOf("Name", "Contact", "Location", "Status"),
            ) {
                customers.forEach { customer ->
                    DashboardWideCustomerRow(
                        customer = customer,
                        onDetailsClick = { onCustomerClick(customer) },
                    )
                }
            }
            DashboardPaginationControls(
                pagination = state.dashboard.customerPagination,
                wide = true,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Customers, it) },
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            customers.forEach { customer ->
                DashboardCustomerRow(
                    customer = customer,
                    onDetailsClick = { onCustomerClick(customer) },
                )
            }
            DashboardPaginationControls(
                pagination = state.dashboard.customerPagination,
                wide = false,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Customers, it) },
            )
        }
    }
}

@Composable
private fun DashboardCustomerEngagementGuideCard(
    state: OnboardingUiState,
) {
    val reachableCustomers = state.dashboard.customers.count {
        !it.phoneNumber.isNullOrBlank() || !it.email.isNullOrBlank()
    }
    DashboardModuleChecklistCard(
        title = "Engagement readiness",
        items = listOf(
            "Save phone or email for digital follow-up.",
            "Use notes for preferences, delivery instructions, or service history.",
            "Select customers directly while taking orders.",
        ),
        tertiaryText = if (state.dashboard.customers.isNotEmpty()) {
            "$reachableCustomers reachable"
        } else {
            null
        },
    )
}

@Composable
private fun DashboardCatalogPulseCard(
    state: OnboardingUiState,
) {
    val products = filteredDashboardProducts(state)
    val itemType = state.activeDashboardItemType()
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    val stockTracked = products.count { it.trackStock }
    val lowStock = products.count { it.lowStock }
    val withImages = products.count { !it.imageUrl.isNullOrBlank() }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = if (selectedItemType == "all") "Catalog readiness" else itemType.catalogSectionTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (selectedItemType == "all") {
                        "Products, services, and appointment items ready for orders, online catalog, and marketing."
                    } else {
                        itemType.catalogSectionDescription()
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (lowStock > 0) "$lowStock LOW" else "CLEAR",
                tone = if (lowStock > 0) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Items",
                value = products.size.toString(),
                detail = "$stockTracked tracked",
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Info,
            )
            DashboardMiniMetricCell(
                label = "Images",
                value = withImages.toString(),
                detail = "catalog ready",
                modifier = Modifier.weight(1f),
                tone = if (withImages == products.size && products.isNotEmpty()) OrmaStatusTone.Success else OrmaStatusTone.Warning,
            )
        }
    }
}

@Composable
private fun DashboardProductRecords(
    state: OnboardingUiState,
    actions: OnboardingActions,
    onEditClick: (OrmaProduct) -> Unit,
    onStockClick: (OrmaProduct) -> Unit,
    onImageClick: (OrmaProduct) -> Unit,
    wide: Boolean,
) {
    val products = filteredDashboardProducts(state)
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    if (products.isEmpty()) {
        val itemType = state.activeDashboardItemType()
        DashboardEmptyModuleCard(
            icon = DashboardNavIconKind.Products,
            title = if (state.hasActiveDashboardFilter()) {
                if (selectedItemType == "all") "No matching catalog items" else "No matching ${itemType.sellableItemTypeLabel().lowercase()}s"
            } else {
                if (selectedItemType == "all") "No catalog items yet" else itemType.emptyCatalogTitle()
            },
            body = if (state.hasActiveDashboardFilter()) {
                "Try another name, SKU, barcode, supplier, category, or stock filter."
            } else {
                if (selectedItemType == "all") {
                    "Add products, services, or appointment items before creating work."
                } else {
                    itemType.emptyCatalogBody()
                }
            },
        )
        return
    }
    if (wide) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardWideProductDataSurface(
                title = if (selectedItemType == "all") "Catalog records" else "${state.activeDashboardItemType().sellableItemTypeLabel()} records",
            ) {
                products.forEach { product ->
                    DashboardWideProductRow(
                        product = product,
                        onEditClick = { onEditClick(product) },
                        onStockClick = { onStockClick(product) },
                        onImageClick = { onImageClick(product) },
                    )
                }
            }
            DashboardPaginationControls(
                pagination = state.dashboard.productPagination,
                wide = true,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Products, it) },
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            products.forEach { product ->
                DashboardProductRow(
                    product = product,
                    onEditClick = { onEditClick(product) },
                    onStockClick = { onStockClick(product) },
                    onImageClick = { onImageClick(product) },
                )
            }
            DashboardPaginationControls(
                pagination = state.dashboard.productPagination,
                wide = false,
                onPageChange = { actions.onDashboardPageChange(DashboardPageTarget.Products, it) },
            )
        }
    }
}

@Composable
private fun DashboardPaginationControls(
    pagination: OrmaPagination,
    wide: Boolean,
    loading: Boolean = false,
    onPageChange: (Int) -> Unit,
) {
    if (pagination.totalPages <= 1 && !pagination.hasPrevious && !pagination.hasNext) return
    val page = pagination.page.coerceAtLeast(1)
    val pageSize = pagination.pageSize.coerceAtLeast(1)
    val totalItems = pagination.totalItems.coerceAtLeast(0)
    val startItem = if (totalItems == 0) 0 else ((page - 1) * pageSize) + 1
    val endItem = (page * pageSize).coerceAtMost(totalItems)
    val totalPages = pagination.totalPages.coerceAtLeast(page)
    val pages = dashboardPaginationWindow(page = page, totalPages = totalPages)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        if (wide) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardPaginationSummary(
                    page = page,
                    totalPages = totalPages,
                    startItem = startItem,
                    endItem = endItem,
                    totalItems = totalItems,
                    modifier = Modifier.weight(1f),
                )
                DashboardRowsPerPagePill(pageSize = pageSize)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    pages.forEach { pageNumber ->
                        DashboardPaginationPageButton(
                            text = pageNumber.toString(),
                            selected = pageNumber == page,
                            enabled = !loading && pageNumber != page,
                            onClick = { onPageChange(pageNumber) },
                        )
                    }
                }
                DashboardToolbarButton(
                    text = "Previous",
                    onClick = { onPageChange(page - 1) },
                    modifier = Modifier.widthIn(min = 104.dp, max = 118.dp),
                    enabled = !loading && pagination.hasPrevious,
                )
                DashboardToolbarButton(
                    text = "Next",
                    onClick = { onPageChange(page + 1) },
                    modifier = Modifier.widthIn(min = 88.dp, max = 104.dp),
                    enabled = !loading && pagination.hasNext,
                    primary = true,
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DashboardPaginationSummary(
                    page = page,
                    totalPages = totalPages,
                    startItem = startItem,
                    endItem = endItem,
                    totalItems = totalItems,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrmaSecondaryButton(
                        text = "Previous",
                        onClick = { onPageChange(page - 1) },
                        modifier = Modifier.weight(1f),
                        enabled = !loading && pagination.hasPrevious,
                    )
                    OrmaSecondaryButton(
                        text = "Next",
                        onClick = { onPageChange(page + 1) },
                        modifier = Modifier.weight(1f),
                        enabled = !loading && pagination.hasNext,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardPaginationSummary(
    page: Int,
    totalPages: Int,
    startItem: Int,
    endItem: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "Page $page of $totalPages",
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (totalItems == 0) {
                "No records"
            } else {
                "Showing $startItem-$endItem of $totalItems"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardRowsPerPagePill(pageSize: Int) {
    Surface(
        shape = OrmaShapes.Capsule,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = "$pageSize / page",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardPaginationPageButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .widthIn(min = 36.dp)
            .height(36.dp),
        shape = OrmaShapes.Capsule,
        color = if (selected) OrmaColors.Accent else OrmaColors.ScreenBackground,
        contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
        border = if (selected) null else BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) OrmaColors.OnAccent else if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun dashboardPaginationWindow(
    page: Int,
    totalPages: Int,
): List<Int> {
    if (totalPages <= 5) return (1..totalPages).toList()
    val start = (page - 2).coerceAtLeast(1).coerceAtMost((totalPages - 4).coerceAtLeast(1))
    return (start until (start + 5).coerceAtMost(totalPages + 1)).toList()
}

@Composable
private fun DashboardWideProductDataSurface(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardWideHeaderLabel(
                    text = "Item",
                    modifier = Modifier.weight(2.08f),
                )
                DashboardWideHeaderLabel(
                    text = "Type",
                    modifier = Modifier.weight(0.78f),
                )
                DashboardWideHeaderLabel(
                    text = "Stock",
                    modifier = Modifier.weight(1f),
                )
                DashboardWideHeaderLabel(
                    text = "Price",
                    modifier = Modifier.weight(0.95f),
                )
                DashboardWideHeaderLabel(
                    text = "Actions",
                    modifier = Modifier.width(220.dp),
                )
            }
            HorizontalDivider(color = OrmaColors.Divider.copy(alpha = 0.52f))
            content()
        }
    }
}

@Composable
private fun DashboardWideHeaderLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = OrmaColors.TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DashboardCatalogToolsRow(
    state: OnboardingUiState,
    onSupplierClick: () -> Unit,
    onOfferClick: () -> Unit,
) {
    val lowStockCount = state.dashboard.summary.lowStockProducts
    val supplierCount = state.dashboard.suppliers.size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            val compact = maxWidth < 520.dp
            val summary = when {
                lowStockCount > 0 -> "$lowStockCount items need restock"
                supplierCount > 0 -> "$supplierCount suppliers connected"
                else -> "Manage suppliers, offers, and catalog readiness"
            }
            if (compact) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardCatalogToolsCopy(summary = summary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OrmaSecondaryButton(
                            text = "Suppliers",
                            onClick = onSupplierClick,
                            modifier = Modifier.weight(1f),
                        )
                        OrmaSecondaryButton(
                            text = "Offers",
                            onClick = onOfferClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DashboardCatalogToolsCopy(
                        summary = summary,
                        modifier = Modifier.weight(1f),
                    )
                    OrmaSecondaryButton(
                        text = "Suppliers",
                        onClick = onSupplierClick,
                        modifier = Modifier.widthIn(min = 132.dp),
                    )
                    OrmaSecondaryButton(
                        text = "Offers",
                        onClick = onOfferClick,
                        modifier = Modifier.widthIn(min = 132.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCatalogToolsCopy(
    summary: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = "Catalog tools",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardCatalogGuideCard(
    state: OnboardingUiState,
    onSupplierClick: () -> Unit,
    onTransferClick: () -> Unit,
) {
    DashboardRecordCard {
        Text(
            text = "Catalog readiness",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        listOf(
            "Use products for retail, bakery, restaurant menu, salon services, or service packages.",
            "Track stock only for items that need inventory control.",
            "Attach suppliers when purchasing or restocking matters.",
        ).forEach { item ->
            DashboardChecklistRow(text = item)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaSecondaryButton(
                text = "Supplier",
                onClick = onSupplierClick,
                modifier = Modifier.weight(1f),
            )
            OrmaSecondaryButton(
                text = "Import / Export",
                onClick = onTransferClick,
                modifier = Modifier.weight(1f),
            )
        }
        when {
            state.dashboard.summary.lowStockProducts > 0 -> DashboardChecklistRow(
                text = "${state.dashboard.summary.lowStockProducts} products need restock.",
            )
            state.dashboard.suppliers.isNotEmpty() -> DashboardChecklistRow(
                text = "${state.dashboard.suppliers.size} suppliers are connected to catalog operations.",
            )
        }
    }
}

@Composable
private fun DashboardListScaffold(
    eyebrow: String,
    title: String,
    body: String,
    primaryText: String,
    onPrimary: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    tertiaryText: String? = null,
    onTertiary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OrmaDashboardSectionScaffold(
        eyebrow = eyebrow,
        title = title,
        body = body,
        primaryAction = OrmaDashboardAction(
            text = primaryText,
            onClick = onPrimary,
            enabled = !loading,
            variant = OrmaDashboardActionVariant.Primary,
        ),
        secondaryAction = if (secondaryText != null && onSecondary != null) {
            OrmaDashboardAction(
                text = secondaryText,
                onClick = onSecondary,
                variant = OrmaDashboardActionVariant.Secondary,
            )
        } else {
            null
        },
        tertiaryAction = if (tertiaryText != null && onTertiary != null) {
            OrmaDashboardAction(
                text = tertiaryText,
                onClick = onTertiary,
                variant = OrmaDashboardActionVariant.Tertiary,
            )
        } else {
            null
        },
        loading = loading,
        modifier = modifier,
        wide = wide,
        content = content,
    )
}

@Composable
private fun DashboardWideSectionHeader(
    eyebrow: String,
    title: String,
    body: String,
    primaryText: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean,
    secondaryText: String?,
    onSecondary: (() -> Unit)?,
    tertiaryText: String?,
    onTertiary: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OrmaBadge(text = eyebrow, tone = OrmaStatusTone.Info)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (tertiaryText != null && onTertiary != null) {
                    DashboardWideActionButton(
                        text = tertiaryText,
                        onClick = onTertiary,
                    )
                }
                if (secondaryText != null && onSecondary != null) {
                    DashboardWideActionButton(
                        text = secondaryText,
                        onClick = onSecondary,
                    )
                }
                DashboardWideActionButton(
                    text = primaryText,
                    onClick = onPrimary,
                    primary = true,
                    enabled = primaryEnabled,
                )
            }
        }
    }
}

@Composable
private fun DashboardWideActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    val iconKind = dashboardFlatIconForAction(text)
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp).widthIn(min = 92.dp),
        enabled = enabled,
        shape = OrmaShapes.SmallCard,
        color = when {
            !enabled -> OrmaColors.Accent.copy(alpha = 0.16f)
            primary -> OrmaColors.Accent
            else -> OrmaColors.ScreenBackground
        },
        contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        border = if (primary) null else BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKind != null) {
                OrmaFlatIcon(
                    kind = iconKind,
                    modifier = Modifier.size(15.dp),
                    color = when {
                        !enabled -> OrmaColors.TextDisabled
                        primary -> OrmaColors.ScreenBackground
                        else -> OrmaColors.TextPrimary
                    },
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    !enabled -> OrmaColors.TextDisabled
                    primary -> OrmaColors.ScreenBackground
                    else -> OrmaColors.TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardMiniListHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = OrmaColors.TextSecondary,
    )
}

@Composable
private fun DashboardInlineOrderLine(order: OrmaOrder) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = order.orderNumber.ifBlank { "Order" },
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = order.customerName ?: "Walk-in customer",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = order.status.dashboardStatusLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun DashboardInlineCustomerLine(customer: OrmaCustomer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(customer.phoneNumber, customer.email).joinToString(" / ").ifBlank { "No contact added" },
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardInlineProductLine(product: OrmaProduct) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        product.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
            OrmaRemoteImage(
                url = imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(38.dp)
                    .clip(OrmaShapes.SmallCard),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when (product.itemType) {
                    "service" -> "Service"
                    "appointment" -> product.durationMinutes?.let { "$it min appointment" } ?: "Appointment"
                    else -> listOfNotNull(
                        "${product.stockQuantity} ${product.unit}",
                        product.expiryDate.productExpiryLabel(),
                    ).joinToString(" / ")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
            )
        }
        if (product.lowStock) {
            Text(
                text = "Low",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.Warning,
            )
        }
    }
}

@Composable
private fun DashboardCustomerRow(
    customer: OrmaCustomer,
    onDetailsClick: () -> Unit,
) {
    val contact = listOfNotNull(customer.phoneNumber, customer.email).joinToString(" / ").ifBlank { "No contact added" }
    val location = listOfNotNull(customer.city, customer.region, customer.country).joinToString(", ")
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = contact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = customer.status.dashboardTeamStatusLabel().uppercase(),
                tone = if (customer.status.lowercase() == "active") OrmaStatusTone.Success else OrmaStatusTone.Info,
            )
        }
        if (customer.addressLine.isNullOrBlank() && location.isBlank() && customer.notes.isNullOrBlank()) {
            DashboardChecklistRow(text = "Add address or notes when this customer returns.")
        } else {
            OrmaKeyValueList(
                rows = buildList {
                    customer.addressLine?.takeIf { it.isNotBlank() }?.let { add("Address" to it) }
                    location.takeIf { it.isNotBlank() }?.let { add("Area" to it) }
                    customer.notes?.takeIf { it.isNotBlank() }?.let { add("Notes" to it) }
                },
            )
        }
        OrmaSecondaryButton(
            text = "View details",
            onClick = onDetailsClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CustomerDetailsSheet(
    customer: OrmaCustomer,
    orders: List<OrmaOrder>,
    loadingHistory: Boolean,
    historyError: String?,
    wide: Boolean,
    onDismiss: () -> Unit,
) {
    DashboardFormSheet(
        title = customer.name,
        body = "Customer profile, contact details, address, notes, and previous bookings.",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        CustomerDetailsContent(
            customer = customer,
            orders = orders,
            loadingHistory = loadingHistory,
            historyError = historyError,
            showCloseAction = false,
            onClose = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
        OrmaActionRow(
            primaryText = "Close",
            onPrimary = LocalSmoothSheetDismiss.current ?: onDismiss,
            primaryEnabled = true,
        )
    }
}

@Composable
private fun DashboardCustomerDetailsPanel(
    customer: OrmaCustomer,
    orders: List<OrmaOrder>,
    loadingHistory: Boolean,
    historyError: String?,
    onClose: () -> Unit,
) {
    CustomerDetailsContent(
        customer = customer,
        orders = orders,
        loadingHistory = loadingHistory,
        historyError = historyError,
        showCloseAction = true,
        onClose = onClose,
    )
}

@Composable
private fun CustomerDetailsContent(
    customer: OrmaCustomer,
    orders: List<OrmaOrder>,
    loadingHistory: Boolean,
    historyError: String?,
    showCloseAction: Boolean,
    onClose: () -> Unit,
) {
    val currency = orders.firstOrNull()?.currency ?: "INR"
    val totalBilled = orders.sumOf { it.total.toDoubleOrNull() ?: 0.0 }.toDashboardMoneyInput()
    val totalPaid = orders.sumOf { it.paidTotal.toDoubleOrNull() ?: 0.0 }.toDashboardMoneyInput()
    val pendingAmount = orders.sumOf {
        (it.total.toDoubleOrNull() ?: 0.0) - (it.paidTotal.toDoubleOrNull() ?: 0.0)
    }.coerceAtLeast(0.0).toDashboardMoneyInput()
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = listOfNotNull(customer.phoneNumber, customer.email).joinToString(" / ").ifBlank { "No contact added" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = customer.status.dashboardTeamStatusLabel().uppercase(),
                tone = if (customer.status.lowercase() == "active") OrmaStatusTone.Success else OrmaStatusTone.Info,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Bookings",
                value = orders.size.toString(),
                detail = "previous",
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Info,
            )
            DashboardMiniMetricCell(
                label = "Paid",
                value = dashboardMoney(totalPaid, currency),
                detail = "collected",
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Success,
            )
        }
        OrmaKeyValueList(
            rows = buildList {
                add("Phone" to customer.phoneNumber.orEmpty().ifBlank { "Not added" })
                add("Email" to customer.email.orEmpty().ifBlank { "Not added" })
                add("Address" to customer.addressLine.orEmpty().ifBlank { "Not added" })
                add("Area" to listOfNotNull(customer.city, customer.region, customer.country, customer.postalCode).joinToString(", ").ifBlank { "Not added" })
                add("Notes" to customer.notes.orEmpty().ifBlank { "No notes" })
            },
        )
        if (showCloseAction) {
            OrmaSecondaryButton(
                text = "Close details",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    DashboardRecordCard {
        Text(
            text = "Booking history",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        OrmaKeyValueList(
            rows = listOf(
                "Total billed" to dashboardMoney(totalBilled, currency),
                "Total paid" to dashboardMoney(totalPaid, currency),
                "Pending" to dashboardMoney(pendingAmount, currency),
            ),
        )
        if (loadingHistory) {
            DashboardChecklistRow(text = "Loading complete booking history...")
        }
        historyError?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.Warning,
            )
        }
        if (!loadingHistory && orders.isEmpty()) {
            DashboardChecklistRow(
                text = "No previous bookings yet. Orders, services, and appointments for this customer will appear here after they are created.",
            )
        } else if (orders.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                orders.forEach { order ->
                    CustomerBookingHistoryRow(order = order)
                }
            }
        }
    }
}

@Composable
private fun CustomerBookingHistoryRow(order: OrmaOrder) {
    val itemSummary = order.items
        .mapNotNull { it.productName ?: it.description.takeIf { description -> description.isNotBlank() } }
        .take(2)
        .joinToString(", ")
        .ifBlank { "${order.itemCount} item${if (order.itemCount == 1) "" else "s"}" }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = order.orderNumber.ifBlank { order.orderType.orderTypeLabel() },
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = itemSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                order.scheduledAt?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it.dashboardDateLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            OrmaBadge(
                text = "${order.orderType.orderTypeLabel()} / ${order.status.dashboardStatusLabel()}",
                tone = when (order.status) {
                    "paid", "completed" -> OrmaStatusTone.Success
                    "cancelled" -> OrmaStatusTone.Danger
                    else -> OrmaStatusTone.Info
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Total",
                value = dashboardMoney(order.total, order.currency),
                detail = order.fulfillmentType.dashboardTitleCase(),
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Info,
            )
            DashboardMiniMetricCell(
                label = "Paid",
                value = dashboardMoney(order.paidTotal, order.currency),
                detail = order.paymentMode.dashboardTitleCase(),
                modifier = Modifier.weight(1f),
                tone = if (order.paidTotal == order.total) OrmaStatusTone.Success else OrmaStatusTone.Warning,
            )
        }
        HorizontalDivider(color = OrmaColors.Divider)
    }
}

@Composable
private fun DashboardProductRow(
    product: OrmaProduct,
    onEditClick: (() -> Unit)?,
    onStockClick: (() -> Unit)?,
    onImageClick: (() -> Unit)?,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (!product.imageUrl.isNullOrBlank()) {
                OrmaRemoteImage(
                    url = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(OrmaShapes.SmallCard),
                )
            } else {
                OrmaDashboardIconBubble(modifier = Modifier.size(58.dp)) {
                    DashboardNavIcon(
                        kind = DashboardNavIconKind.Products,
                        color = OrmaColors.IconPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOfNotNull(product.sku, product.barcode, product.supplierName).joinToString(" / ").ifBlank { product.unit },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when (product.itemType) {
                        "appointment" -> product.durationMinutes?.let { "$it min booking" } ?: "Booking required"
                        "service" -> product.durationMinutes?.let { "$it min service" } ?: "Service item"
                        else -> if (product.trackStock) "${product.stockQuantity} ${product.unit} in stock" else "Non-stock product"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.lowStock) OrmaColors.Warning else OrmaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = when {
                    product.lowStock -> "LOW"
                    product.itemType != "product" -> product.itemType.sellableItemTypeLabel().uppercase()
                    product.imageUrl != null -> "IMAGE"
                    else -> "STOCK"
                },
                tone = when {
                    product.lowStock -> OrmaStatusTone.Warning
                    product.itemType != "product" -> OrmaStatusTone.Info
                    product.imageUrl != null -> OrmaStatusTone.Info
                    else -> OrmaStatusTone.Success
                },
            )
        }
        OrmaKeyValueList(
            rows = buildList {
                add("Price" to dashboardMoney(product.sellingPrice, product.currency))
                add("Tax" to "${product.taxRate}%")
                if (product.itemType == "product") {
                    add("Reorder" to product.reorderLevel)
                    product.expiryDate.productExpiryLabel()?.let { add("Expiry" to it.removePrefix("Expires ")) }
                } else {
                    add("Type" to product.itemType.sellableItemTypeLabel())
                }
            },
        )
        if (onEditClick != null || onStockClick != null || onImageClick != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    onEditClick?.let {
                        OrmaSecondaryButton(
                            text = "Edit details",
                            onClick = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    onImageClick?.let {
                        OrmaSecondaryButton(
                            text = if (product.imageUrl == null) "Add image" else "Update image",
                            onClick = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                onStockClick?.takeIf { product.itemType == "product" }?.let {
                    OrmaSecondaryButton(
                        text = "Update stock",
                        onClick = it,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardOrderRow(
    order: OrmaOrder,
    compact: Boolean,
    onOpen: (() -> Unit)? = null,
    onStatusChange: ((String) -> Unit)?,
) {
    val itemSummary = order.items
        .mapNotNull { it.productName ?: it.description.takeIf { description -> description.isNotBlank() } }
        .take(3)
        .joinToString(", ")
        .ifBlank { "${order.itemCount} item${if (order.itemCount == 1) "" else "s"}" }
    DashboardRecordCard(
        modifier = if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = order.orderNumber.ifBlank { "Order" },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = order.customerName ?: "Walk-in customer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = itemSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = "${order.orderType.orderTypeLabel()} / ${order.status.dashboardStatusLabel()}",
                tone = when (order.status) {
                    "paid", "completed" -> OrmaStatusTone.Success
                    "cancelled" -> OrmaStatusTone.Danger
                    else -> OrmaStatusTone.Info
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DashboardMiniMetricCell(
                label = "Total",
                value = dashboardMoney(order.total, order.currency),
                detail = "bill value",
                modifier = Modifier.weight(1f),
                tone = OrmaStatusTone.Info,
            )
            DashboardMiniMetricCell(
                label = "Paid",
                value = dashboardMoney(order.paidTotal, order.currency),
                detail = if (order.paidTotal == order.total) "settled" else "collected",
                modifier = Modifier.weight(1f),
                tone = if (order.paidTotal == order.total) OrmaStatusTone.Success else OrmaStatusTone.Warning,
            )
        }
        order.scheduledAt?.takeIf { it.isNotBlank() }?.let {
            DashboardChecklistRow(text = "Scheduled: ${it.dashboardDateLabel()}")
        }
        if (!compact && onStatusChange != null) {
            DashboardOrderStatusDropdown(order = order, onStatusChange = onStatusChange)
        }
    }
}

@Composable
private fun DashboardOrderStatusDropdown(
    order: OrmaOrder,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentStatus = order.status.takeIf { it in DashboardOrderStatuses } ?: "confirmed"
    val nextStatuses = order.dashboardNextStatuses()
    var expanded by rememberSaveable(order.id, currentStatus) { mutableStateOf(false) }
    val terminal = nextStatuses.isEmpty()
    val tone = currentStatus.dashboardOrderStatusTone()
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            onClick = { if (!terminal) expanded = !expanded },
            enabled = !terminal,
            shape = OrmaShapes.Capsule,
            color = colors.container,
            contentColor = colors.content,
            border = BorderStroke(0.8.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = currentStatus.dashboardStatusLabel(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!terminal) {
                    OrmaChevronDownIcon(
                        modifier = Modifier.size(14.dp),
                        color = colors.content,
                    )
                }
            }
        }
        AnimatedVisibility(visible = expanded && nextStatuses.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                nextStatuses.forEach { status ->
                    DashboardOrderStatusOption(
                        status = status,
                        onClick = {
                            expanded = false
                            onStatusChange(status)
                        },
                    )
                }
            }
        }
        if (terminal) {
            Text(
                text = currentStatus.dashboardTerminalStatusCopy(),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardOrderStatusOption(
    status: String,
    onClick: () -> Unit,
) {
    val tone = status.dashboardOrderStatusTone()
    val colors = org.orma.project_90.designsystem.ormaStatusColors(tone)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = status.dashboardStatusLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = status.dashboardStatusActionCopy(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardPartPaidAmountSheet(
    order: OrmaOrder,
    wide: Boolean,
    actionLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val initialAmount = order.paidTotal
        .takeIf { it.toDoubleOrNull().orZero() > 0.0 }
        .orEmpty()
    var paidAmount by rememberSaveable(order.id) { mutableStateOf(initialAmount) }
    var attemptedSubmit by rememberSaveable(order.id) { mutableStateOf(false) }
    val amountValue = paidAmount.toDoubleOrNull()
    val totalValue = order.total.toDoubleOrNull().orZero()
    val balanceAfter = (totalValue - amountValue.orZero()).coerceAtLeast(0.0)
    val amountError = partPaidAmountError(paidAmount, order)
    val closeSheet = LocalSmoothSheetDismiss.current ?: onDismiss
    DashboardFormSheet(
        title = "Record part payment",
        body = "Enter the amount already collected for ${order.orderNumber.ifBlank { order.id.take(8).uppercase() }}. The remaining balance stays open.",
        onDismiss = onDismiss,
        wide = wide,
    ) {
        OrmaKeyValueList(
            rows = listOf(
                "Order total" to dashboardMoney(order.total, order.currency),
                "Current paid" to dashboardMoney(order.paidTotal.ifBlank { "0" }, order.currency),
                "Balance after" to dashboardMoney(balanceAfter.toDashboardMoneyInput(), order.currency),
            ),
        )
        OrmaTextField(
            value = paidAmount,
            onValueChange = { paidAmount = it.moneyInput() },
            label = "Part paid amount",
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Text(
            text = "Use Paid instead when the full ${dashboardMoney(order.total, order.currency)} has been collected.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        if (attemptedSubmit) {
            amountError?.let { FormValidationText(it) }
        }
        OrmaActionRow(
            primaryText = if (actionLoading) "Saving..." else "Mark part paid",
            onPrimary = {
                attemptedSubmit = true
                if (amountError == null && amountValue != null) {
                    onSubmit(amountValue.toDashboardMoneyInput())
                }
            },
            primaryEnabled = !actionLoading,
            secondaryText = "Cancel",
            onSecondary = closeSheet,
        )
    }
}

@Composable
private fun DashboardRecordCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OrmaDashboardRecordSurface(
        modifier = modifier,
        content = content,
    )
}

private val LocalSmoothSheetDismiss = staticCompositionLocalOf<(() -> Unit)?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSmoothSheetDismiss(
    sheetState: SheetState,
): (afterHidden: () -> Unit) -> Unit {
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    return { afterHidden ->
        if (!closing) {
            closing = true
            scope.launch {
                runCatching { sheetState.hide() }
                afterHidden()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardFormSheet(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    wide: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        modifier = if (wide) {
            Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 1220.dp)
        } else {
            Modifier
        },
        sheetState = sheetState,
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        dragHandle = null,
    ) {
        CompositionLocalProvider(LocalSmoothSheetDismiss provides requestDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (wide) 28.dp else 22.dp, vertical = if (wide) 26.dp else 22.dp),
                verticalArrangement = Arrangement.spacedBy(if (wide) 18.dp else 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = OrmaColors.TextPrimary,
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = requestDismiss),
                        shape = OrmaShapes.Capsule,
                        color = OrmaColors.CellBackground,
                        contentColor = OrmaColors.Accent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            OrmaCloseIcon()
                        }
                    }
                }
                content()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun OrmaCountryPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Phone number",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    errorText: String? = null,
) {
    var selectedCountryId by rememberSaveable { mutableStateOf(ormaCountryForPhoneNumber(value).id) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    val selectedCountry = ormaCountryById(selectedCountryId)
    val nationalNumber = selectedCountry.nationalPhoneDigits(value)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OrmaTextField(
            value = nationalNumber,
            onValueChange = { input ->
                val nextCountry = if (input.trim().startsWith("+")) {
                    ormaCountryForPhoneNumber(input)
                } else {
                    selectedCountry
                }
                selectedCountryId = nextCountry.id
                onValueChange(nextCountry.formatInternationalPhone(input))
            },
            label = label,
            placeholder = selectedCountry.placeholder,
            supportingText = if (errorText == null) supportingText else null,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leading = {
                Surface(
                    onClick = { if (enabled) showCountryPicker = true },
                    enabled = enabled,
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.ScreenBackground,
                    contentColor = OrmaColors.Accent,
                    border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CountryFlagIcon(country = selectedCountry)
                        Text(
                            text = selectedCountry.dialCode,
                            style = MaterialTheme.typography.labelLarge,
                            color = OrmaColors.TextPrimary,
                        )
                        OrmaChevronDownIcon(
                            modifier = Modifier.size(13.dp),
                            color = OrmaColors.TextSecondary,
                        )
                    }
                }
            },
        )
        errorText?.takeIf { it.isNotBlank() }?.let {
            FormValidationText(text = it)
        }
    }

    if (showCountryPicker) {
        CountryPickerSheet(
            selectedCountry = selectedCountry,
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                val nextValue = country.formatInternationalPhone(nationalNumber)
                selectedCountryId = country.id
                onValueChange(nextValue)
                showCountryPicker = false
            },
        )
    }
}

@Composable
private fun FormValidationText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = OrmaColors.Error,
    )
}

@Composable
private fun CustomerFormSheet(
    onDismiss: () -> Unit,
    onSubmit: (OrmaCustomerDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaCustomerDraft()) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val nameError = if (draft.name.trim().length >= 2) null else "Enter at least 2 characters."
    val phoneError = optionalOrmaPhoneError(draft.phoneNumber)
    val emailError = optionalOrmaEmailError(draft.email)
    val formReady = nameError == null && phoneError == null && emailError == null
    DashboardFormSheet(
        title = "Add customer",
        body = "Save contact, address, and notes for orders, reminders, and follow-up.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.name, { draft = draft.copy(name = it) }, "Customer name", placeholder = "Full name")
        if (attemptedSubmit) nameError?.let { FormValidationText(it) }
        OrmaCountryPhoneField(
            value = draft.phoneNumber,
            onValueChange = { draft = draft.copy(phoneNumber = it) },
            supportingText = "India is selected by default. Change country if needed.",
            errorText = if (attemptedSubmit) phoneError else null,
        )
        OrmaTextField(draft.email, { draft = draft.copy(email = it.trim().take(160)) }, "Email", placeholder = "name@example.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        if (attemptedSubmit) emailError?.let { FormValidationText(it) }
        OrmaTextField(draft.addressLine, { draft = draft.copy(addressLine = it) }, "Address", placeholder = "Building, street, area")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.city, { draft = draft.copy(city = it) }, "City", modifier = Modifier.weight(1f))
            OrmaTextField(draft.region, { draft = draft.copy(region = it) }, "State", modifier = Modifier.weight(1f))
        }
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Save customer",
            onPrimary = {
                attemptedSubmit = true
                if (formReady) onSubmit(draft.copy(name = draft.name.trim(), email = draft.email.trim()))
            },
            primaryEnabled = draft.name.trim().isNotBlank(),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun SupplierFormSheet(
    onDismiss: () -> Unit,
    onSubmit: (OrmaSupplierDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaSupplierDraft()) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val nameError = if (draft.name.trim().length >= 2) null else "Enter at least 2 characters."
    val phoneError = optionalOrmaPhoneError(draft.phoneNumber)
    val emailError = optionalOrmaEmailError(draft.email)
    val formReady = nameError == null && phoneError == null && emailError == null
    DashboardFormSheet(
        title = "Add supplier",
        body = "Attach suppliers to catalog items and stock updates.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.name, { draft = draft.copy(name = it) }, "Supplier name", placeholder = "Company name")
        if (attemptedSubmit) nameError?.let { FormValidationText(it) }
        OrmaCountryPhoneField(
            value = draft.phoneNumber,
            onValueChange = { draft = draft.copy(phoneNumber = it) },
            supportingText = "India is selected by default. Change country if needed.",
            errorText = if (attemptedSubmit) phoneError else null,
        )
        OrmaTextField(draft.email, { draft = draft.copy(email = it.trim().take(160)) }, "Email", placeholder = "supplier@example.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        if (attemptedSubmit) emailError?.let { FormValidationText(it) }
        OrmaTextField(draft.taxNumber, { draft = draft.copy(taxNumber = it.uppercase().take(24)) }, "GST/VAT number", placeholder = "Optional")
        OrmaTextField(draft.addressLine, { draft = draft.copy(addressLine = it) }, "Address", placeholder = "Optional")
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Save supplier",
            onPrimary = {
                attemptedSubmit = true
                if (formReady) onSubmit(draft.copy(name = draft.name.trim(), email = draft.email.trim()))
            },
            primaryEnabled = draft.name.trim().isNotBlank(),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun ProductImagePickerCard(
    productName: String,
    image: OrmaPickedImage?,
    currentImageAvailable: Boolean,
    aiGenerating: Boolean,
    aiAvailable: Boolean,
    errorMessage: String?,
    onUpload: () -> Unit,
    onGenerate: () -> Unit,
    onRemove: () -> Unit,
) {
    val selected = image != null || currentImageAvailable
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.24f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LogoPreviewTile(
                    initials = productName.take(1).uppercase().ifBlank { "P" },
                    previewBytes = image?.bytes ?: byteArrayOf(),
                    selected = selected,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = when {
                            aiGenerating -> "Creating AI image"
                            image != null -> "Product image ready"
                            currentImageAvailable -> "Product image attached"
                            else -> "Product image"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = when {
                            aiGenerating -> "Generating a clean catalog image from the product details."
                            image != null -> displayLogoFileName(image.fileName)
                            currentImageAvailable -> "Upload or generate a new image to replace it."
                            aiAvailable -> "Upload a photo or create an AI catalog image."
                            else -> "Upload a PNG, JPG, or WebP product image."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaSecondaryButton(
                    text = "Upload",
                    onClick = onUpload,
                    modifier = Modifier.weight(1f),
                    enabled = !aiGenerating,
                )
                OrmaSecondaryButton(
                    text = if (aiAvailable) "Create AI" else "AI on web",
                    onClick = onGenerate,
                    modifier = Modifier.weight(1f),
                    enabled = aiAvailable && !aiGenerating,
                )
            }
            if (image != null) {
                OrmaTextButton(
                    text = "Remove selected image",
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !aiGenerating,
                )
            }
            errorMessage?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.Error,
                )
            }
        }
    }
}

private fun handleProductImagePickerResult(
    result: OrmaLogoPickerResult,
    onSuccess: (OrmaPickedImage) -> Unit,
    onFailure: (String) -> Unit,
) {
    when (result) {
        OrmaLogoPickerResult.Cancelled -> Unit
        is OrmaLogoPickerResult.Failure -> onFailure(result.message)
        is OrmaLogoPickerResult.Success -> {
            if (result.image.sizeBytes > ProductImageMaxBytes) {
                onFailure("Choose a PNG, JPG, or WebP image up to 5 MB.")
            } else {
                onSuccess(result.image)
            }
        }
    }
}

private fun productAiImagePrompt(
    name: String,
    description: String,
    industry: String,
): String? {
    val cleanName = name.trim().takeIf { it.length >= 2 } ?: return null
    val details = description.trim().takeIf { it.isNotBlank() }
    val businessType = industry.trim().takeIf { it.isNotBlank() } ?: "local business"
    return buildString {
        append("Create a realistic clean product catalog image for ORMA. ")
        append("Product or service: $cleanName. ")
        details?.let { append("Details: $it. ") }
        append("Business type: $businessType. ")
        append("Warm neutral background, professional ecommerce lighting, centered composition, no text, no watermark, no logo.")
    }
}

@Composable
private fun CategoryFormSheet(
    initialItemType: String = "all",
    allowedItemTypes: List<String> = listOf("product"),
    onDismiss: () -> Unit,
    onSubmit: (OrmaProductCategoryDraft) -> Unit,
) {
    val itemTypeOptions = remember(allowedItemTypes) {
        (listOf("all") + allowedItemTypes).distinct()
    }
    var draft by remember {
        mutableStateOf(
            OrmaProductCategoryDraft(
                itemType = initialItemType.takeIf { it in itemTypeOptions } ?: "all",
            ),
        )
    }
    DashboardFormSheet(
        title = "Category",
        body = "Group products, services, and appointments so staff and customers can browse faster.",
        onDismiss = onDismiss,
    ) {
        OrmaSegmentedRow(
            options = itemTypeOptions,
            selected = draft.itemType,
            label = { it.categoryScopeLabel() },
            onSelected = { draft = draft.copy(itemType = it) },
        )
        OrmaTextField(
            value = draft.name,
            onValueChange = { draft = draft.copy(name = it.take(80)) },
            label = "Category name",
            placeholder = "Bakery, salon, lunch menu",
        )
        OrmaTextField(
            value = draft.sortOrder,
            onValueChange = { draft = draft.copy(sortOrder = it.filter(Char::isDigit).take(3)) },
            label = "Display order",
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OrmaActionRow(
            primaryText = "Save category",
            onPrimary = {
                onSubmit(
                    draft.copy(
                        name = draft.name.trim(),
                        itemType = draft.itemType,
                    ),
                )
            },
            primaryEnabled = draft.name.trim().length >= 2,
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun OfferFormSheet(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaProductOfferDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaProductOfferDraft(appliesTo = "all")) }
    val scopedTargetReady = when (draft.appliesTo) {
        "category" -> draft.categoryId.isNotBlank()
        "product" -> draft.productId.isNotBlank()
        else -> true
    }
    DashboardFormSheet(
        title = "Offer setup",
        body = "Create a live customer-facing offer for all items, a category, or one product.",
        onDismiss = onDismiss,
    ) {
        OrmaSegmentedRow(
            options = listOf("all", "category", "product"),
            selected = draft.appliesTo,
            label = {
                when (it) {
                    "category" -> "Category"
                    "product" -> "Product"
                    else -> "All"
                }
            },
            onSelected = { draft = draft.copy(appliesTo = it, categoryId = "", productId = "") },
        )
        when (draft.appliesTo) {
            "category" -> if (state.dashboard.categories.isNotEmpty()) {
                DashboardChipPicker(
                    label = "Offer category",
                    options = state.dashboard.categories,
                    selectedId = draft.categoryId,
                    optionId = { it.id },
                    optionLabel = { it.name },
                    onSelected = { draft = draft.copy(categoryId = it.id) },
                )
            } else {
                DashboardChecklistRow(text = "Create a category before adding a category offer.")
            }
            "product" -> if (state.dashboard.products.isNotEmpty()) {
                DashboardChipPicker(
                    label = "Offer product",
                    options = state.dashboard.products,
                    selectedId = draft.productId,
                    optionId = { it.id },
                    optionLabel = { it.name },
                    onSelected = { draft = draft.copy(productId = it.id) },
                )
            } else {
                DashboardChecklistRow(text = "Add a product or service before adding a product offer.")
            }
        }
        OrmaTextField(
            value = draft.name,
            onValueChange = { draft = draft.copy(name = it.take(100)) },
            label = "Offer title",
            placeholder = "Weekend special",
        )
        OrmaTextField(
            value = draft.description,
            onValueChange = { draft = draft.copy(description = it.take(180)) },
            label = "Description",
            placeholder = "Optional customer-facing note",
            singleLine = false,
            minLines = 2,
        )
        OrmaSegmentedRow(
            options = listOf("percentage", "fixed"),
            selected = draft.discountType,
            label = { if (it == "fixed") "Fixed" else "%" },
            onSelected = { draft = draft.copy(discountType = it) },
        )
        OrmaTextField(
            value = draft.discountValue,
            onValueChange = { draft = draft.copy(discountValue = it.moneyInput()) },
            label = if (draft.discountType == "fixed") "Discount amount" else "Discount %",
            placeholder = if (draft.discountType == "fixed") "50" else "10",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaCalendarDateField(
                value = draft.startsAt,
                onValueChange = { draft = draft.copy(startsAt = it) },
                label = "Start",
                placeholder = "Optional",
                modifier = Modifier.weight(1f),
            )
            OrmaCalendarDateField(
                value = draft.endsAt,
                onValueChange = { draft = draft.copy(endsAt = it) },
                label = "End",
                placeholder = "Optional",
                modifier = Modifier.weight(1f),
            )
        }
        OrmaActionRow(
            primaryText = "Save offer",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.name.trim().length >= 2 &&
                (draft.discountValue.toDoubleOrNull() ?: 0.0) > 0.0 &&
                scopedTargetReady,
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun ProductFormSheet(
    state: OnboardingUiState,
    product: OrmaProduct? = null,
    onDismiss: () -> Unit,
    onSubmit: (OrmaProductDraft) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val editing = product != null
    val allowedItemTypes = state.allowedDashboardItemTypes()
    val initialItemType = product?.itemType
        ?.takeIf { it in allowedItemTypes }
        ?: state.activeDashboardItemType().takeIf { it in allowedItemTypes }
        ?: allowedItemTypes.first()
    var imageError by remember { mutableStateOf<String?>(null) }
    var aiGenerating by remember { mutableStateOf(false) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var draft by remember(product?.id) {
        mutableStateOf(
            product?.toProductDraft()
                ?: OrmaProductDraft(
                    itemType = initialItemType,
                    unit = when (initialItemType) {
                        "appointment" -> "booking"
                        "service" -> "service"
                        else -> "pcs"
                    },
                    trackStock = initialItemType == "product",
                    bookingRequired = initialItemType == "appointment",
                    currency = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
                    pricesIncludeTax = state.draft.pricesIncludeTax,
                ),
        )
    }
    val categoryOptions = remember(state.dashboard.categories, draft.itemType) {
        state.dashboard.categories.filter { it.matchesCategoryItemType(draft.itemType) }
    }
    LaunchedEffect(draft.itemType, draft.categoryId, state.dashboard.categories) {
        if (draft.categoryId.isNotBlank() && categoryOptions.none { it.id == draft.categoryId }) {
            draft = draft.copy(categoryId = "")
        }
    }
    val productImagePicker = rememberOrmaBusinessLogoPicker { result ->
        handleProductImagePickerResult(
            result = result,
            onSuccess = { image ->
                draft = draft.copy(image = image)
                imageError = null
            },
            onFailure = { imageError = it },
        )
    }
    val nameError = if (draft.name.trim().length >= 2) null else "Enter at least 2 characters."
    val unitError = if (draft.unit.trim().isNotBlank()) null else "Enter a unit such as pcs, kg, plate, or service."
    val sellingPriceError = requiredNonNegativeDecimalError(draft.sellingPrice, "Enter the selling price.")
    val costPriceError = optionalNonNegativeDecimalError(draft.costPrice, "Cost price must be zero or higher.")
    val stockFieldsVisible = draft.itemType == "product"
    val durationVisible = draft.itemType != "product"
    val stockError = if (stockFieldsVisible) requiredNonNegativeDecimalError(draft.stockQuantity, "Opening stock must be zero or higher.") else null
    val reorderError = if (stockFieldsVisible) requiredNonNegativeDecimalError(draft.reorderLevel, "Reorder level must be zero or higher.") else null
    val durationError = when {
        draft.itemType != "appointment" -> null
        (draft.durationMinutes.toIntOrNull() ?: 0) > 0 -> null
        else -> "Enter appointment duration in minutes."
    }
    val taxError = percentageDecimalError(draft.taxRate)
    val formReady = listOf(
        nameError,
        unitError,
        sellingPriceError,
        costPriceError,
        stockError,
        reorderError,
        durationError,
        taxError,
    ).all { it == null }
    DashboardFormSheet(
        title = if (editing) "Edit ${draft.itemType.sellableItemTypeLabel().lowercase()}" else draft.itemType.catalogActionText(),
        body = if (editing) {
            "Update the details customers, staff, stock, and orders use for this ${draft.itemType.sellableItemTypeLabel().lowercase()}."
        } else {
            draft.itemType.catalogSectionDescription()
        },
        onDismiss = onDismiss,
    ) {
        OrmaTextField(
            draft.name,
            { draft = draft.copy(name = it) },
            "${draft.itemType.sellableItemTypeLabel()} name",
            placeholder = when (draft.itemType) {
                "service" -> "Service name"
                "appointment" -> "Appointment service"
                else -> "Product name"
            },
        )
        if (attemptedSubmit) nameError?.let { FormValidationText(it) }
        if (allowedItemTypes.size > 1) {
            OrmaSegmentedRow(
                options = allowedItemTypes,
                selected = draft.itemType,
                label = { it.sellableItemTypeLabel() },
                onSelected = { itemType ->
                    val nextCategoryId = draft.categoryId
                        .takeIf { selectedId ->
                            state.dashboard.categories.any { category ->
                                category.id == selectedId && category.matchesCategoryItemType(itemType)
                            }
                        }
                        .orEmpty()
                    draft = draft.copy(
                        itemType = itemType,
                        categoryId = nextCategoryId,
                        categoryName = "",
                        unit = when (itemType) {
                            "product" -> draft.unit.takeIf { it != "service" && it != "booking" } ?: "pcs"
                            "appointment" -> "booking"
                            else -> "service"
                        },
                        trackStock = itemType == "product" && draft.trackStock,
                        stockQuantity = if (itemType == "product") draft.stockQuantity else "0",
                        reorderLevel = if (itemType == "product") draft.reorderLevel else "0",
                        bookingRequired = itemType == "appointment",
                        expiryDate = if (itemType == "product") draft.expiryDate else "",
                    )
                },
            )
        } else {
            OrmaBadge(
                text = draft.itemType.sellableItemTypeLabel().uppercase(),
                tone = OrmaStatusTone.Info,
            )
        }
        if (categoryOptions.isNotEmpty()) {
            DashboardChipPicker(
                label = "${draft.itemType.sellableItemTypeLabel()} category",
                options = categoryOptions,
                selectedId = draft.categoryId,
                optionId = { it.id },
                optionLabel = { it.categoryOptionLabel() },
                onSelected = { draft = draft.copy(categoryId = it.id, categoryName = "") },
            )
        }
        OrmaTextField(
            value = draft.categoryName,
            onValueChange = { draft = draft.copy(categoryName = it.take(80), categoryId = "") },
            label = "New category",
            placeholder = when (draft.itemType) {
                "service" -> "Repairs, consulting"
                "appointment" -> "Consultation, salon"
                else -> "Bakery, lunch menu"
            },
        )
        if (draft.categoryId.isNotBlank() || draft.categoryName.isNotBlank()) {
            OrmaTextButton(
                text = "Clear category",
                onClick = { draft = draft.copy(categoryId = "", categoryName = "") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ProductImagePickerCard(
            productName = draft.name,
            image = draft.image,
            currentImageAvailable = !product?.imageUrl.isNullOrBlank(),
            aiGenerating = aiGenerating,
            aiAvailable = isOrmaProductAiImageGenerationAvailable(),
            errorMessage = imageError,
            onUpload = { productImagePicker.launch() },
            onGenerate = {
                val prompt = productAiImagePrompt(
                    name = draft.name,
                    description = draft.description,
                    industry = state.draft.industry,
                )
                if (prompt == null) {
                    imageError = "Enter a product name before creating an AI image."
                    return@ProductImagePickerCard
                }
                scope.launch {
                    aiGenerating = true
                    imageError = null
                    when (val result = generateOrmaProductAiImage(prompt)) {
                        is OrmaProductAiImageResult.Success -> draft = draft.copy(image = result.image)
                        is OrmaProductAiImageResult.Failure -> imageError = result.message
                    }
                    aiGenerating = false
                }
            },
            onRemove = { draft = draft.copy(image = null) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (stockFieldsVisible) {
                OrmaTextField(draft.sku, { draft = draft.copy(sku = it.uppercase().take(40)) }, "SKU", modifier = Modifier.weight(1f), placeholder = "Optional")
            }
            OrmaTextField(draft.unit, { draft = draft.copy(unit = it.take(12)) }, "Unit", modifier = Modifier.weight(1f), placeholder = if (stockFieldsVisible) "pcs" else "service")
        }
        if (attemptedSubmit) unitError?.let { FormValidationText(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.sellingPrice, { draft = draft.copy(sellingPrice = it.moneyInput()) }, "Selling price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OrmaTextField(draft.costPrice, { draft = draft.copy(costPrice = it.moneyInput()) }, "Cost price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        if (attemptedSubmit) {
            sellingPriceError?.let { FormValidationText(it) }
            costPriceError?.let { FormValidationText(it) }
        }
        if (stockFieldsVisible) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrmaTextField(draft.stockQuantity, { draft = draft.copy(stockQuantity = it.moneyInput()) }, "Opening stock", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OrmaTextField(draft.reorderLevel, { draft = draft.copy(reorderLevel = it.moneyInput()) }, "Reorder level", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            if (attemptedSubmit) {
                stockError?.let { FormValidationText(it) }
                reorderError?.let { FormValidationText(it) }
            }
            OrmaCalendarDateField(
                value = draft.expiryDate,
                onValueChange = { draft = draft.copy(expiryDate = it) },
                label = "Expiry date",
                placeholder = "Optional",
                supportingText = "Use for food, pharmacy, cosmetics, batches, and perishable stock.",
            )
        }
        if (durationVisible) {
            OrmaTextField(
                value = draft.durationMinutes,
                onValueChange = { draft = draft.copy(durationMinutes = it.filter(Char::isDigit).take(4)) },
                label = if (draft.itemType == "appointment") "Appointment duration" else "Duration",
                placeholder = "Minutes",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (attemptedSubmit) durationError?.let { FormValidationText(it) }
        }
        OrmaTextField(draft.taxRate, { draft = draft.copy(taxRate = it.moneyInput()) }, "Tax rate %", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        if (attemptedSubmit) taxError?.let { FormValidationText(it) }
        if (stockFieldsVisible && state.dashboard.suppliers.isNotEmpty()) {
            DashboardChipPicker(
                label = "Supplier",
                options = state.dashboard.suppliers,
                selectedId = draft.supplierId,
                optionId = { it.id },
                optionLabel = { it.name },
                onSelected = { draft = draft.copy(supplierId = it.id) },
            )
        }
        if (stockFieldsVisible) {
            OrmaSwitchRow(
                title = "Track stock",
                body = "Show low-stock alerts and apply order stock movements.",
                checked = draft.trackStock,
                onCheckedChange = { draft = draft.copy(trackStock = it) },
            )
        }
        OrmaActionRow(
            primaryText = if (editing) "Update ${draft.itemType.sellableItemTypeLabel().lowercase()}" else "Save ${draft.itemType.sellableItemTypeLabel().lowercase()}",
            onPrimary = {
                attemptedSubmit = true
                if (formReady) {
                    onSubmit(
                        draft.copy(
                            name = draft.name.trim(),
                            unit = draft.unit.trim(),
                            sellingPrice = draft.sellingPrice.trim(),
                            costPrice = draft.costPrice.trim(),
                            stockQuantity = if (draft.itemType == "product") draft.stockQuantity.trim().ifBlank { "0" } else "0",
                            reorderLevel = if (draft.itemType == "product") draft.reorderLevel.trim().ifBlank { "0" } else "0",
                            trackStock = draft.itemType == "product" && draft.trackStock,
                            durationMinutes = draft.durationMinutes.trim(),
                            bookingRequired = draft.itemType == "appointment" || draft.bookingRequired,
                            expiryDate = if (draft.itemType == "product") draft.expiryDate.trim() else "",
                            categoryName = if (draft.categoryId.isBlank()) draft.categoryName.trim() else "",
                            taxRate = draft.taxRate.trim().ifBlank { "0" },
                        ),
                    )
                }
            },
            primaryEnabled = draft.name.trim().isNotBlank(),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

private fun OrmaProduct.toProductDraft(): OrmaProductDraft =
    OrmaProductDraft(
        name = name,
        itemType = itemType,
        categoryId = categoryId.orEmpty(),
        categoryName = "",
        sku = sku.orEmpty(),
        barcode = barcode.orEmpty(),
        description = description.orEmpty(),
        unit = unit,
        sellingPrice = sellingPrice,
        costPrice = costPrice,
        currency = currency,
        taxRate = taxRate,
        pricesIncludeTax = pricesIncludeTax,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel,
        trackStock = trackStock,
        durationMinutes = durationMinutes?.toString().orEmpty(),
        bookingRequired = bookingRequired,
        expiryDate = expiryDate.orEmpty(),
        supplierId = supplierId.orEmpty(),
        image = null,
    )

@Composable
private fun ProductImageSheet(
    product: OrmaProduct,
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaPickedImage) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var image by remember(product.id) { mutableStateOf<OrmaPickedImage?>(null) }
    var imageError by remember(product.id) { mutableStateOf<String?>(null) }
    var aiGenerating by remember(product.id) { mutableStateOf(false) }
    val productImagePicker = rememberOrmaBusinessLogoPicker { result ->
        handleProductImagePickerResult(
            result = result,
            onSuccess = {
                image = it
                imageError = null
            },
            onFailure = { imageError = it },
        )
    }
    DashboardFormSheet(
        title = "Product image",
        body = "Upload a catalog photo or create an AI image from the product details.",
        onDismiss = onDismiss,
    ) {
        ProductImagePickerCard(
            productName = product.name,
            image = image,
            currentImageAvailable = product.imageUrl != null,
            aiGenerating = aiGenerating,
            aiAvailable = isOrmaProductAiImageGenerationAvailable(),
            errorMessage = imageError,
            onUpload = { productImagePicker.launch() },
            onGenerate = {
                val prompt = productAiImagePrompt(
                    name = product.name,
                    description = product.description.orEmpty(),
                    industry = state.draft.industry,
                )
                if (prompt == null) {
                    imageError = "Add a product name before creating an AI image."
                    return@ProductImagePickerCard
                }
                scope.launch {
                    aiGenerating = true
                    imageError = null
                    when (val result = generateOrmaProductAiImage(prompt)) {
                        is OrmaProductAiImageResult.Success -> image = result.image
                        is OrmaProductAiImageResult.Failure -> imageError = result.message
                    }
                    aiGenerating = false
                }
            },
            onRemove = { image = null },
        )
        OrmaActionRow(
            primaryText = if (state.dashboard.actionLoading) "Saving..." else "Save image",
            onPrimary = { image?.let(onSubmit) },
            primaryEnabled = image != null && !state.dashboard.actionLoading && !aiGenerating,
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun ProductTransferSheet(
    state: OnboardingUiState,
    actions: OnboardingActions,
    onDismiss: () -> Unit,
) {
    var importMode by rememberSaveable { mutableStateOf(true) }
    val template = state.dashboard.productImportTemplate
    val fallbackTemplate = productImportCsvTemplate()
    val templateCsv = template?.csv?.takeIf { it.isNotBlank() }
    val templateColumns = template?.columns?.takeIf { it.isNotEmpty() }
    val requiredColumns = template?.requiredColumns?.takeIf { it.isNotEmpty() } ?: listOf("name")
    var csvText by rememberSaveable { mutableStateOf(templateCsv ?: fallbackTemplate) }
    var csvFileMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var csvFileError by rememberSaveable { mutableStateOf<String?>(null) }
    val actionLoading = state.dashboard.actionLoading
    val csvFilePicker = rememberOrmaCsvFilePicker { result ->
        when (result) {
            is OrmaCsvFilePickerResult.Success -> {
                csvText = result.file.text
                val rows = countCsvDataRows(result.file.text)
                csvFileMessage = "Loaded ${result.file.fileName} · $rows product rows"
                csvFileError = null
            }
            OrmaCsvFilePickerResult.Cancelled -> Unit
            is OrmaCsvFilePickerResult.Failure -> {
                csvFileMessage = null
                csvFileError = "${result.title}. ${result.message}"
            }
        }
    }
    LaunchedEffect(Unit) {
        if (template == null) actions.onLoadProductImportTemplate()
    }
    LaunchedEffect(templateCsv) {
        if (!templateCsv.isNullOrBlank() && (csvText.isBlank() || csvText == fallbackTemplate)) {
            csvText = templateCsv
        }
    }
    DashboardFormSheet(
        title = "Import / export catalog",
        body = "Move catalog, barcode, price, stock, and supplier data with ORMA CSV.",
        onDismiss = onDismiss,
    ) {
        OrmaSegmentedRow(
            options = listOf(true, false),
            selected = importMode,
            label = { if (it) "Import" else "Export" },
            onSelected = { importMode = it },
        )
        if (importMode) {
            DashboardModuleChecklistCard(
                title = "CSV columns",
                items = listOf(
                    "Required column: ${requiredColumns.joinToString(", ")}.",
                    "Columns: ${(templateColumns ?: fallbackTemplate.lineSequence().first().split(",")).joinToString(", ")}.",
                    "SKU or barcode duplicates are skipped to protect existing catalog items.",
                    "Supplier names are matched or created automatically.",
                ),
                tertiaryText = "${countCsvDataRows(csvText)} rows",
            )
            OrmaSecondaryButton(
                text = "Choose CSV file",
                onClick = { csvFilePicker.launch() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !actionLoading,
            )
            csvFileMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            csvFileError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.Warning,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaSecondaryButton(
                    text = "Download template",
                    onClick = actions.onDownloadProductImportTemplate,
                    modifier = Modifier.weight(1f),
                    enabled = !actionLoading,
                )
                OrmaSecondaryButton(
                    text = "Use template",
                    onClick = {
                        val nextTemplate = state.dashboard.productImportTemplate?.csv?.takeIf { it.isNotBlank() }
                        if (nextTemplate == null) actions.onLoadProductImportTemplate()
                        csvText = nextTemplate ?: fallbackTemplate
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !actionLoading,
                )
            }
            OrmaTextField(
                value = csvText,
                onValueChange = { csvText = it },
                label = "Catalog CSV",
                placeholder = "Paste ORMA catalog CSV",
                singleLine = false,
                minLines = 9,
            )
            state.dashboard.productImportResult?.let { result ->
                DashboardRecordCard {
                    Text(
                        text = "Import summary",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                    )
                    OrmaKeyValueList(
                        rows = listOf(
                            "Created" to result.created.toString(),
                            "Skipped" to result.skipped.toString(),
                            "Errors" to result.errors.size.toString(),
                        ),
                    )
                    result.errors.take(4).forEach { error ->
                        Text(
                            text = "Row ${error.row}: ${error.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.Warning,
                        )
                    }
                }
            }
            OrmaActionRow(
                primaryText = if (actionLoading) "Importing..." else "Import catalog",
                onPrimary = { actions.onImportProductsCsv(csvText) },
                primaryEnabled = !actionLoading && countCsvDataRows(csvText) > 0,
                secondaryText = "Reset template",
                onSecondary = {
                    actions.onClearProductTransfer()
                    csvText = state.dashboard.productImportTemplate?.csv?.takeIf { it.isNotBlank() } ?: fallbackTemplate
                },
            )
        } else {
            DashboardModuleChecklistCard(
                title = "Export scope",
                items = listOf(
                    "Export uses the current product search and low-stock filters.",
                    "The CSV is ready for Excel, Sheets, barcode setup, and bulk editing.",
                    "Import the edited CSV later to add new catalog rows.",
                ),
                tertiaryText = "${state.dashboard.products.size} visible",
            )
            OrmaActionRow(
                primaryText = if (actionLoading) "Preparing..." else "Prepare export",
                onPrimary = actions.onExportProductsCsv,
                primaryEnabled = !actionLoading,
                secondaryText = "Clear",
                onSecondary = actions.onClearProductTransfer,
            )
            state.dashboard.productExport?.let { export ->
                DashboardRecordCard {
                    OrmaSecondaryButton(
                        text = if (actionLoading) "Saving..." else "Download CSV file",
                        onClick = actions.onDownloadProductExport,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !actionLoading && export.csv.isNotBlank(),
                    )
                    Text(
                        text = export.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${export.count} catalog items exported. Select the CSV text below to copy it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                    OrmaTextField(
                        value = export.csv,
                        onValueChange = {},
                        label = "Export CSV",
                        placeholder = "No export prepared",
                        singleLine = false,
                        minLines = 10,
                    )
                }
            }
        }
    }
}

@Composable
private fun StockAdjustmentSheet(
    product: OrmaProduct,
    onDismiss: () -> Unit,
    onSubmit: (OrmaStockAdjustmentDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaStockAdjustmentDraft()) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val quantityError = signedDecimalAdjustmentError(draft.quantityDelta)
    DashboardFormSheet(
        title = "Update stock",
        body = "${product.name}: current stock ${product.stockQuantity} ${product.unit}.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.quantityDelta, { draft = draft.copy(quantityDelta = it.signedMoneyInput()) }, "Adjustment", placeholder = "Use - for reduction", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        if (attemptedSubmit) quantityError?.let { FormValidationText(it) }
        OrmaTextField(draft.note, { draft = draft.copy(note = it) }, "Note", placeholder = "Opening correction, purchase, damage", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Apply stock",
            onPrimary = {
                attemptedSubmit = true
                if (quantityError == null) onSubmit(draft.copy(quantityDelta = draft.quantityDelta.trim()))
            },
            primaryEnabled = draft.quantityDelta.trim().isNotBlank(),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun OrderFormSheet(
    state: OnboardingUiState,
    actions: OnboardingActions,
    invoiceMode: Boolean = false,
    initialOrderType: String? = null,
    initialOrder: OrmaOrder? = null,
    wide: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (OrmaOrderDraft) -> Unit,
) {
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val editingOrder = initialOrder != null
    val allowedOrderTypes = if (invoiceMode) {
        listOf("sale")
    } else {
        (
            state.allowedDashboardOrderTypes() +
                listOfNotNull(
                    initialOrderType?.takeIf { it in DashboardCounterCreateOrderTypes },
                    initialOrder?.orderType?.takeIf { it in DashboardCounterCreateOrderTypes },
                )
            ).distinct()
    }
    var draft by remember(initialOrder?.id, invoiceMode, initialOrderType) {
        val defaultOrderType = if (invoiceMode) {
            "sale"
        } else {
            initialOrderType
                ?.takeIf { it in allowedOrderTypes }
                ?: state.activeDashboardOrderType()
                .takeIf { it in allowedOrderTypes }
                ?: allowedOrderTypes.first()
        }
        mutableStateOf(
            initialOrder?.toOrderDraft()
                ?: OrmaOrderDraft(
                    orderType = defaultOrderType,
                    fulfillmentType = if (defaultOrderType == "appointment") "booking" else "standard",
                    status = if (invoiceMode) "confirmed" else if (defaultOrderType == "appointment") "draft" else "confirmed",
                    currency = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
                    customerCountry = if (invoiceMode) state.draft.country.ifBlank { "India" } else "",
                    items = emptyList(),
                    notes = if (invoiceMode) "Created from invoice desk" else "",
                ),
        )
    }
    var taxEnabled by rememberSaveable(invoiceMode, state.draft.isTaxRegistered) {
        mutableStateOf(!invoiceMode || state.draft.isTaxRegistered)
    }
    var defaultTaxRate by rememberSaveable(invoiceMode, state.draft.isTaxRegistered) {
        mutableStateOf(if (invoiceMode) state.defaultInvoiceTaxRate() else "")
    }
    var itemSearch by rememberSaveable(draft.orderType) { mutableStateOf("") }
    val effectiveItems = if (invoiceMode && !taxEnabled) {
        draft.items.map { it.copy(taxRate = "0") }
    } else {
        draft.items
    }
    val invoiceGstin = normalizeGstinNumber(draft.customerTaxNumber)
    val invoiceGstinComplete = isGstinNumberComplete(invoiceGstin)
    val invoiceGstinError = if (invoiceMode && taxEnabled && invoiceGstin.isNotBlank() && !invoiceGstinComplete) {
        "Enter a valid 15-character GSTIN."
    } else {
        null
    }
    val invoiceLookupMatchesInput = state.dashboard.invoiceGstinLookupNumber == invoiceGstin
    val invoiceLookupLoading = invoiceLookupMatchesInput && state.dashboard.invoiceGstinLookupLoading
    val invoiceLookupStatusMessage = state.dashboard.invoiceGstinLookupStatusMessage.takeIf { invoiceLookupMatchesInput }
    val invoiceLookupErrorMessage = state.dashboard.invoiceGstinLookupErrorMessage.takeIf { invoiceLookupMatchesInput }
    val invoiceLookupAlreadyHandled = invoiceLookupMatchesInput &&
        (
            state.dashboard.invoiceGstinLookupLoading ||
                state.dashboard.invoiceGstinLookupStatusMessage != null ||
                state.dashboard.invoiceGstinLookupErrorMessage != null
            )
    var appliedInvoiceGstin by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(invoiceMode, taxEnabled, invoiceGstin, invoiceLookupAlreadyHandled) {
        if (invoiceMode && taxEnabled && invoiceGstinComplete && !invoiceLookupAlreadyHandled) {
            actions.onInvoiceGstinLookupRequest(invoiceGstin)
        }
    }
    LaunchedEffect(
        invoiceMode,
        taxEnabled,
        state.dashboard.invoiceGstinLookupNumber,
        state.dashboard.invoiceGstinLookup?.gstin,
    ) {
        val lookup = state.dashboard.invoiceGstinLookup
        val lookupGstin = normalizeGstinNumber(lookup?.gstin.orEmpty())
        if (invoiceMode && taxEnabled && lookup != null && lookupGstin.isNotBlank() && appliedInvoiceGstin != lookupGstin) {
            draft = draft.withInvoiceGstinLookup(lookup)
            appliedInvoiceGstin = lookupGstin
        }
    }
    val itemErrors = effectiveItems.map(::orderItemValidationError)
    val defaultTaxError = if (invoiceMode && taxEnabled) percentageDecimalError(defaultTaxRate) else null
    val paidError = optionalNonNegativeDecimalError(draft.paidTotal, "Paid amount must be zero or higher.")
    val scheduledError = if (draft.orderType == "appointment" && draft.scheduledAt.trim().length < 4) {
        "Choose a preferred date/time for this appointment."
    } else {
        null
    }
    val visibleProducts = state.dashboard.products.filter { product ->
        when (draft.orderType) {
            "service" -> product.itemType == "service"
            "appointment" -> product.itemType == "appointment"
            else -> product.itemType == "product"
        }
    }
        .filter { product -> product.matchesOrderSearch(itemSearch) }
        .ifEmpty {
            state.dashboard.products
                .filter { product -> product.matchesOrderSearch(itemSearch) }
                .takeIf { itemSearch.isNotBlank() }
                ?: emptyList()
        }
    val formReady = draft.items.isNotEmpty() &&
        itemErrors.all { it == null } &&
        paidError == null &&
        scheduledError == null &&
        defaultTaxError == null &&
        invoiceGstinError == null
    fun updateItem(index: Int, item: OrmaOrderItemDraft) {
        draft = draft.copy(items = draft.items.mapIndexed { itemIndex, old -> if (itemIndex == index) item else old })
    }
    fun removeItem(index: Int) {
        draft = draft.copy(items = draft.items.filterIndexed { itemIndex, _ -> itemIndex != index })
    }
    fun addOrIncrementProduct(product: OrmaProduct) {
        val existingIndex = draft.items.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val item = draft.items[existingIndex]
            updateItem(
                existingIndex,
                item.copy(quantity = orderQuantityText((item.quantity.toDoubleOrNull() ?: 0.0) + 1.0)),
            )
        } else {
            val nextItem = product.toOrderItemDraft().copy(
                taxRate = if (invoiceMode && !taxEnabled) {
                    "0"
                } else if (invoiceMode && defaultTaxRate.isNotBlank()) {
                    defaultTaxRate
                } else {
                    product.taxRate.ifBlank { "0" }
                },
            )
            draft = draft.copy(items = draft.items + nextItem)
        }
    }
    fun changeItemQuantity(index: Int, delta: Double) {
        val item = draft.items.getOrNull(index) ?: return
        val next = (item.quantity.toDoubleOrNull() ?: 0.0) + delta
        if (next <= 0.0) {
            removeItem(index)
        } else {
            updateItem(index, item.copy(quantity = orderQuantityText(next)))
        }
    }
    fun addCustomLine() {
        draft = draft.copy(
            items = draft.items + OrmaOrderItemDraft(
                taxRate = if (invoiceMode && taxEnabled) defaultTaxRate.ifBlank { "0" } else "0",
            ),
        )
    }
    fun markFullyPaid() {
        val total = orderCartTotalValue(effectiveItems, !invoiceMode || taxEnabled).toDashboardMoneyInput()
        draft = draft.copy(
            paidTotal = total,
            status = if (draft.status == "completed" || draft.status == "cancelled") draft.status else "paid",
            paymentMode = if (draft.paymentMode == DashboardCreditPaymentMode) "pay_on_spot" else draft.paymentMode,
        )
    }
    fun submitOrder() {
        attemptedSubmit = true
        if (formReady) {
            onSubmit(
                draft.copy(
                    customerName = draft.customerName.trim(),
                    customerPhoneNumber = draft.customerPhoneNumber.trim(),
                    customerEmail = draft.customerEmail.trim(),
                    customerTaxNumber = if (invoiceMode && taxEnabled) normalizeGstinNumber(draft.customerTaxNumber) else draft.customerTaxNumber.trim(),
                    customerAddressLine = draft.customerAddressLine.trim(),
                    customerCity = draft.customerCity.trim(),
                    customerRegion = draft.customerRegion.trim(),
                    customerCountry = draft.customerCountry.trim(),
                    customerPostalCode = draft.customerPostalCode.trim(),
                    orderType = draft.orderType,
                    scheduledAt = draft.scheduledAt.trim(),
                    paidTotal = draft.paidTotal.trim().ifBlank { "0" },
                    notes = draft.notes.trim(),
                    fulfillmentType = if (draft.orderType == "appointment") "booking" else draft.fulfillmentType,
                    paymentMode = draft.paymentMode,
                    items = effectiveItems.map {
                        it.copy(
                            description = it.description.trim(),
                            quantity = it.quantity.trim(),
                            unitPrice = it.unitPrice.trim().ifBlank { "0" },
                            taxRate = if (invoiceMode && !taxEnabled) "0" else it.taxRate.trim().ifBlank { "0" },
                        )
                    },
                ),
            )
        }
    }
    DashboardFormSheet(
        title = if (editingOrder) {
            "Edit ${draft.orderType.orderTypeLabel().lowercase()} details"
        } else if (invoiceMode) {
            "Create tax invoice"
        } else {
            draft.orderType.orderSheetTitle()
        },
        body = if (invoiceMode) {
            "Build an invoice from customer details, manual line items, payment amount, and tax settings."
        } else if (editingOrder) {
            "Update customer, line items, delivery or schedule, payment, and status."
        } else {
            draft.orderType.orderSheetBody()
        },
        onDismiss = onDismiss,
        wide = wide && !invoiceMode,
    ) {
        if (wide && !invoiceMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .widthIn(min = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DashboardRecordCard {
                        OrmaBadge(text = "CUSTOMER", tone = OrmaStatusTone.Info)
                        Text(
                            text = "Who is this for?",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.TextPrimary,
                        )
                        if (state.dashboard.customers.isNotEmpty()) {
                            DashboardChipPicker(
                                label = "Saved customer",
                                options = state.dashboard.customers,
                                selectedId = draft.customerId,
                                optionId = { it.id },
                                optionLabel = { it.name },
                                onSelected = { draft = draft.withInvoiceCustomer(it) },
                            )
                        }
                        OrmaTextField(
                            value = draft.customerName,
                            onValueChange = { draft = draft.copy(customerName = it, customerId = "") },
                            label = "Walk-in or new customer",
                            placeholder = "Optional",
                        )
                        OrmaCountryPhoneField(
                            value = draft.customerPhoneNumber,
                            onValueChange = { draft = draft.copy(customerPhoneNumber = it) },
                            label = "Customer phone",
                            supportingText = "Optional, useful for delivery and follow-up.",
                        )
                        OrmaTextField(
                            value = draft.customerEmail,
                            onValueChange = { draft = draft.copy(customerEmail = it.trim().take(160)) },
                            label = "Customer email",
                            placeholder = "Optional",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        )
                    }
                    DashboardRecordCard {
                        OrmaBadge(text = draft.orderType.orderTypeLabel().uppercase(), tone = OrmaStatusTone.Info)
                        if (allowedOrderTypes.size > 1) {
                            OrmaSegmentedRow(
                                options = allowedOrderTypes,
                                selected = draft.orderType,
                                label = { it.orderTypeLabel() },
                                onSelected = { orderType ->
                                    draft = draft.copy(
                                        orderType = orderType,
                                        status = if (orderType == "appointment") "draft" else draft.status,
                                        fulfillmentType = when (orderType) {
                                            "appointment" -> "booking"
                                            else -> "standard"
                                        },
                                        items = emptyList(),
                                    )
                                    itemSearch = ""
                                },
                            )
                        }
                        OrmaSegmentedRow(
                            options = DashboardOrderStatuses,
                            selected = draft.status,
                            label = { it.dashboardStatusLabel() },
                            onSelected = { draft = draft.withOrderStatus(it) },
                        )
                    }
                    DashboardOrderCheckoutCard(
                        draft = draft,
                        orderType = draft.orderType,
                        totalText = orderCartTotal(draft.items, draft.currency),
                        attemptedSubmit = attemptedSubmit,
                        paidError = paidError,
                        scheduledError = scheduledError,
                        formReady = formReady,
                        primaryText = if (editingOrder) "Save details" else draft.orderType.orderActionText(),
                        onDraftChange = { draft = it },
                        onMarkPaid = ::markFullyPaid,
                        onSubmit = ::submitOrder,
                        onDismiss = onDismiss,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1.35f)
                        .widthIn(min = 540.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DashboardOrderQuickPicker(
                        orderType = draft.orderType,
                        query = itemSearch,
                        onQueryChange = { itemSearch = it.take(80) },
                        products = visibleProducts.take(20),
                        selectedItems = draft.items,
                        onProductClick = ::addOrIncrementProduct,
                    )
                    DashboardOrderCart(
                        orderType = draft.orderType,
                        currency = draft.currency,
                        items = draft.items,
                        products = state.dashboard.products,
                        validationMessages = if (attemptedSubmit) itemErrors else emptyList(),
                        editingOrder = editingOrder,
                        invoiceMode = false,
                        taxEnabled = true,
                        onIncrement = { index -> changeItemQuantity(index, 1.0) },
                        onDecrement = { index -> changeItemQuantity(index, -1.0) },
                        onChange = ::updateItem,
                        onRemove = ::removeItem,
                    )
                    OrmaSecondaryButton(
                        text = draft.orderType.addOrderLineText(),
                        onClick = ::addCustomLine,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
        if (state.dashboard.customers.isNotEmpty()) {
            DashboardChipPicker(
                label = if (invoiceMode) "Bill to customer" else "Customer",
                options = state.dashboard.customers,
                selectedId = draft.customerId,
                optionId = { it.id },
                optionLabel = { it.name },
                onSelected = { draft = draft.withInvoiceCustomer(it) },
            )
        }
        OrmaTextField(
            value = draft.customerName,
            onValueChange = { draft = draft.copy(customerName = it, customerId = "") },
            label = if (invoiceMode) "Invoice customer name" else "Walk-in or new customer",
            placeholder = "Optional",
        )
        if (!invoiceMode) {
            OrmaCountryPhoneField(
                value = draft.customerPhoneNumber,
                onValueChange = { draft = draft.copy(customerPhoneNumber = it) },
                label = "Customer phone",
                supportingText = "Optional, useful for delivery and follow-up.",
            )
            OrmaTextField(
                value = draft.customerEmail,
                onValueChange = { draft = draft.copy(customerEmail = it.trim().take(160)) },
                label = "Customer email",
                placeholder = "Optional",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
        }
        if (invoiceMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrmaTextField(
                    value = draft.customerPhoneNumber,
                    onValueChange = { draft = draft.copy(customerPhoneNumber = it.filter { character -> character.isDigit() || character == '+' }.take(24)) },
                    label = "Billing phone",
                    placeholder = "Optional",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                OrmaTextField(
                    value = draft.customerEmail,
                    onValueChange = { draft = draft.copy(customerEmail = it.trim().take(160)) },
                    label = "Billing email",
                    placeholder = "Optional",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
            }
            OrmaBadge(text = "SALE INVOICE", tone = OrmaStatusTone.Info)
            OrmaSwitchRow(
                title = "GST/VAT",
                body = if (taxEnabled) {
                    "Tax is enabled. Set a default rate, then override individual lines when needed."
                } else {
                    "Tax is disabled. This invoice will use zero tax on every line."
                },
                checked = taxEnabled,
                onCheckedChange = { enabled ->
                    taxEnabled = enabled
                    draft = draft.copy(
                        items = draft.items.map {
                            it.copy(taxRate = if (enabled) it.taxRate.ifBlank { defaultTaxRate.ifBlank { "0" } } else "0")
                        },
                    )
                },
            )
            if (taxEnabled) {
                GstinLookupField(
                    value = draft.customerTaxNumber,
                    loading = invoiceLookupLoading,
                    complete = invoiceGstinComplete,
                    statusMessage = invoiceLookupStatusMessage,
                    errorMessage = invoiceLookupErrorMessage ?: invoiceGstinError,
                    label = "Bill-to GST/VAT number",
                    supportingText = "Search the billed customer's GSTIN and prefill billing details.",
                    onValueChange = { value ->
                        draft = draft.copy(customerTaxNumber = normalizeGstinNumber(value))
                    },
                    onSearch = { actions.onInvoiceGstinLookupRequest(invoiceGstin) },
                )
                OrmaTextField(
                    value = draft.customerAddressLine,
                    onValueChange = { draft = draft.copy(customerAddressLine = it) },
                    label = "Bill-to address",
                    placeholder = "Building, street, area",
                    singleLine = false,
                    minLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrmaTextField(
                        value = draft.customerCity,
                        onValueChange = { draft = draft.copy(customerCity = it) },
                        label = "City",
                        modifier = Modifier.weight(1f),
                    )
                    OrmaTextField(
                        value = draft.customerRegion,
                        onValueChange = { draft = draft.copy(customerRegion = it) },
                        label = "State",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrmaTextField(
                        value = draft.customerCountry,
                        onValueChange = { draft = draft.copy(customerCountry = it) },
                        label = "Country",
                        modifier = Modifier.weight(1f),
                    )
                    OrmaTextField(
                        value = draft.customerPostalCode,
                        onValueChange = { draft = draft.copy(customerPostalCode = it) },
                        label = "Postal code",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OrmaTextField(
                    value = defaultTaxRate,
                    onValueChange = { defaultTaxRate = it.moneyInput() },
                    label = "Default tax %",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                defaultTaxError?.let { FormValidationText(it) }
                OrmaSecondaryButton(
                    text = "Apply tax to all lines",
                    onClick = {
                        val rate = defaultTaxRate.ifBlank { "0" }
                        draft = draft.copy(items = draft.items.map { it.copy(taxRate = rate) })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = defaultTaxError == null,
                )
            }
        } else if (allowedOrderTypes.size > 1) {
            OrmaSegmentedRow(
                options = allowedOrderTypes,
                selected = draft.orderType,
                label = { it.orderTypeLabel() },
                onSelected = { orderType ->
                    draft = draft.copy(
                        orderType = orderType,
                        status = if (orderType == "appointment") "draft" else draft.status,
                        fulfillmentType = when (orderType) {
                            "appointment" -> "booking"
                            "service" -> "standard"
                            else -> "standard"
                        },
                        items = emptyList(),
                    )
                    itemSearch = ""
                },
            )
        } else {
            OrmaBadge(
                text = draft.orderType.orderTypeLabel().uppercase(),
                tone = OrmaStatusTone.Info,
            )
        }
        OrmaSegmentedRow(
            options = DashboardOrderStatuses,
            selected = draft.status,
            label = { it.dashboardStatusLabel() },
            onSelected = { draft = draft.withOrderStatus(it) },
        )
        DashboardOrderQuickPicker(
            orderType = draft.orderType,
            query = itemSearch,
            onQueryChange = { itemSearch = it.take(80) },
            products = visibleProducts.take(12),
            selectedItems = draft.items,
            onProductClick = ::addOrIncrementProduct,
        )
        DashboardOrderCart(
            orderType = draft.orderType,
            currency = draft.currency,
            items = draft.items,
            products = state.dashboard.products,
            validationMessages = if (attemptedSubmit) itemErrors else emptyList(),
            editingOrder = editingOrder,
            invoiceMode = invoiceMode,
            taxEnabled = !invoiceMode || taxEnabled,
            onIncrement = { index -> changeItemQuantity(index, 1.0) },
            onDecrement = { index -> changeItemQuantity(index, -1.0) },
            onChange = ::updateItem,
            onRemove = ::removeItem,
        )
        OrmaSecondaryButton(
            text = if (invoiceMode) "Add invoice line" else draft.orderType.addOrderLineText(),
            onClick = ::addCustomLine,
            modifier = Modifier.fillMaxWidth(),
        )
        OrmaCalendarDateTimeField(
            value = draft.scheduledAt,
            onValueChange = { draft = draft.copy(scheduledAt = it) },
            label = draft.orderType.orderScheduleLabel(),
            placeholder = draft.orderType.orderSchedulePlaceholder(),
            supportingText = if (draft.orderType == "appointment") {
                "Choose the booking date. Add a common time slot when needed."
            } else {
                "Optional. Choose only when this work is scheduled."
            },
            allowClear = draft.orderType != "appointment",
        )
        if (attemptedSubmit) scheduledError?.let { FormValidationText(it) }
        if (!invoiceMode) {
            if (draft.orderType != "appointment") {
                Text(
                    text = "Fulfilment",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                )
                DashboardCompactSegmentedPicker(
                    options = draft.orderType.orderFulfillmentOptions(),
                    selected = draft.fulfillmentType,
                    label = { it.fulfillmentModeLabel() },
                    onSelected = { draft = draft.copy(fulfillmentType = it) },
                )
            }
            if (draft.requiresDeliveryLocation()) {
                DashboardDeliveryLocationFields(
                    draft = draft,
                    onDraftChange = { draft = it },
                )
            }
            Text(
                text = "Payment",
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
            DashboardCompactSegmentedPicker(
                options = DashboardOrderPaymentModes,
                selected = draft.paymentMode,
                label = { it.paymentModeLabel() },
                onSelected = { draft = draft.withPaymentMode(it) },
            )
        }
        OrmaTextField(draft.paidTotal, { draft = draft.copy(paidTotal = it.moneyInput()) }, "Paid amount", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        if (attemptedSubmit) paidError?.let { FormValidationText(it) }
        val balancePayable = (orderCartTotalValue(effectiveItems, !invoiceMode || taxEnabled) - draft.paidTotal.toDoubleOrNull().orZero()).coerceAtLeast(0.0)
        OrmaKeyValueList(
            rows = listOf(
                "Total" to orderCartTotal(effectiveItems, draft.currency, !invoiceMode || taxEnabled),
                "Paid" to dashboardMoney(draft.paidTotal.ifBlank { "0" }, draft.currency),
                "Balance payable" to dashboardMoney(balancePayable.toDashboardMoneyInput(), draft.currency),
            ),
        )
        if (!invoiceMode) {
            OrmaSecondaryButton(
                text = "Mark fully paid",
                onClick = ::markFullyPaid,
                enabled = draft.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = if (editingOrder) {
                "Save details"
            } else if (invoiceMode) {
                "Create invoice"
            } else {
                draft.orderType.orderActionText()
            },
            onPrimary = ::submitOrder,
            primaryEnabled = draft.items.isNotEmpty() && defaultTaxError == null && invoiceGstinError == null,
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
        }
    }
}

@Composable
private fun DashboardOrderQuickPicker(
    orderType: String,
    query: String,
    onQueryChange: (String) -> Unit,
    products: List<OrmaProduct>,
    selectedItems: List<OrmaOrderItemDraft>,
    onProductClick: (OrmaProduct) -> Unit,
) {
    DashboardRecordCard {
        Text(
            text = orderType.orderItemPickerLabel(),
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = "Search, tap to add, tap again to increase quantity.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        OrmaTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "Search catalog",
            placeholder = orderType.orderLinePlaceholder(),
        )
        if (products.isEmpty()) {
            DashboardChecklistRow(
                text = if (query.isBlank()) {
                    "Add catalog items first, or use a custom line below."
                } else {
                    "No matching items. Try another name, SKU, or barcode."
                },
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                products.forEach { product ->
                    val selectedQuantity = selectedItems
                        .filter { it.productId == product.id }
                        .sumOf { it.quantity.toDoubleOrNull().orZero() }
                    DashboardOrderProductRow(
                        product = product,
                        selectedQuantity = selectedQuantity,
                        onClick = { onProductClick(product) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardOrderProductRow(
    product: OrmaProduct,
    selectedQuantity: Double,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrmaShapes.StandardCell)
            .clickable(onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = if (selectedQuantity > 0.0) {
            OrmaColors.Accent.copy(alpha = 0.07f)
        } else {
            OrmaColors.CellBackground
        },
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (selectedQuantity > 0.0) OrmaColors.Accent.copy(alpha = 0.35f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!product.imageUrl.isNullOrBlank()) {
                OrmaRemoteImage(
                    url = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(OrmaShapes.SmallCard),
                )
            } else {
                OrmaDashboardIconBubble(modifier = Modifier.size(48.dp)) {
                    DashboardNavIcon(
                        kind = DashboardNavIconKind.Products,
                        color = OrmaColors.IconPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = product.orderPickerMeta(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = dashboardMoney(product.sellingPrice, product.currency),
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaBadge(
                    text = if (selectedQuantity > 0.0) "QTY ${orderQuantityText(selectedQuantity)}" else "ADD",
                    tone = if (selectedQuantity > 0.0) OrmaStatusTone.Success else OrmaStatusTone.Info,
                )
            }
        }
    }
}

@Composable
private fun DashboardOrderCart(
    orderType: String,
    currency: String,
    items: List<OrmaOrderItemDraft>,
    products: List<OrmaProduct>,
    validationMessages: List<String?>,
    editingOrder: Boolean = false,
    invoiceMode: Boolean = false,
    taxEnabled: Boolean = true,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onChange: (Int, OrmaOrderItemDraft) -> Unit,
    onRemove: (Int) -> Unit,
) {
    val catalogProducts = products.filter { it.itemType == orderType.orderCatalogItemType() }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = when {
                        invoiceMode -> "Invoice line items"
                        editingOrder -> orderType.addedOrderItemsTitle()
                        else -> "Selected items"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (items.isEmpty()) {
                        "No items added yet"
                    } else if (invoiceMode && !taxEnabled) {
                        "${items.size} lines · tax disabled"
                    } else if (editingOrder) {
                        "${items.size} lines · details open"
                    } else {
                        "${items.size} lines ready"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = orderCartTotal(items, currency, taxEnabled),
                modifier = Modifier.widthIn(min = 96.dp),
                tone = OrmaStatusTone.Success,
            )
        }
        if (items.isEmpty()) {
            DashboardChecklistRow(text = "Pick ${orderType.orderItemPickerLabel().lowercase()} items above or add a custom line.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEachIndexed { index, item ->
                    DashboardOrderCartLine(
                        index = index,
                        item = item,
                        orderType = orderType,
                        currency = currency,
                        product = products.firstOrNull { it.id == item.productId },
                        products = catalogProducts,
                        validationMessage = validationMessages.getOrNull(index),
                        expandDetailsInitially = editingOrder,
                        taxEnabled = taxEnabled,
                        invoiceMode = invoiceMode,
                        onIncrement = { onIncrement(index) },
                        onDecrement = { onDecrement(index) },
                        onChange = { onChange(index, it) },
                        onRemove = { onRemove(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardDeliveryLocationFields(
    draft: OrmaOrderDraft,
    onDraftChange: (OrmaOrderDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Delivery location",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        OrmaTextField(
            value = draft.customerAddressLine,
            onValueChange = { onDraftChange(draft.copy(customerAddressLine = it)) },
            label = "Address",
            placeholder = "Building, street, area",
            singleLine = false,
            minLines = 2,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 460.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrmaTextField(
                        value = draft.customerCity,
                        onValueChange = { onDraftChange(draft.copy(customerCity = it)) },
                        label = "City",
                    )
                    OrmaTextField(
                        value = draft.customerRegion,
                        onValueChange = { onDraftChange(draft.copy(customerRegion = it)) },
                        label = "State or area",
                    )
                    OrmaTextField(
                        value = draft.customerCountry,
                        onValueChange = { onDraftChange(draft.copy(customerCountry = it)) },
                        label = "Country",
                        placeholder = "India",
                    )
                    OrmaTextField(
                        value = draft.customerPostalCode,
                        onValueChange = { onDraftChange(draft.copy(customerPostalCode = it.take(20))) },
                        label = "Postal code",
                        placeholder = "Optional",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrmaTextField(
                            value = draft.customerCity,
                            onValueChange = { onDraftChange(draft.copy(customerCity = it)) },
                            label = "City",
                            modifier = Modifier.weight(1f),
                        )
                        OrmaTextField(
                            value = draft.customerRegion,
                            onValueChange = { onDraftChange(draft.copy(customerRegion = it)) },
                            label = "State or area",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrmaTextField(
                            value = draft.customerCountry,
                            onValueChange = { onDraftChange(draft.copy(customerCountry = it)) },
                            label = "Country",
                            placeholder = "India",
                            modifier = Modifier.weight(1f),
                        )
                        OrmaTextField(
                            value = draft.customerPostalCode,
                            onValueChange = { onDraftChange(draft.copy(customerPostalCode = it.take(20))) },
                            label = "Postal code",
                            placeholder = "Optional",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardOrderCheckoutCard(
    draft: OrmaOrderDraft,
    orderType: String,
    totalText: String,
    attemptedSubmit: Boolean,
    paidError: String?,
    scheduledError: String?,
    formReady: Boolean,
    primaryText: String = orderType.orderActionText(),
    onDraftChange: (OrmaOrderDraft) -> Unit,
    onMarkPaid: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val balancePayable = (orderCartTotalValue(draft.items) - draft.paidTotal.toDoubleOrNull().orZero()).coerceAtLeast(0.0)
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Schedule, fulfilment, payment, and notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = totalText,
                modifier = Modifier.widthIn(min = 96.dp),
                tone = OrmaStatusTone.Success,
            )
        }
        OrmaCalendarDateTimeField(
            value = draft.scheduledAt,
            onValueChange = { onDraftChange(draft.copy(scheduledAt = it)) },
            label = orderType.orderScheduleLabel(),
            placeholder = orderType.orderSchedulePlaceholder(),
            supportingText = if (orderType == "appointment") {
                "Required for appointment bookings."
            } else {
                "Optional for scheduled pickup, delivery, or service."
            },
            allowClear = orderType != "appointment",
        )
        if (attemptedSubmit) scheduledError?.let { FormValidationText(it) }
        if (orderType != "appointment") {
            Text(
                text = "Fulfilment",
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
            DashboardCompactSegmentedPicker(
                options = orderType.orderFulfillmentOptions(),
                selected = draft.fulfillmentType,
                label = { it.fulfillmentModeLabel() },
                onSelected = { onDraftChange(draft.copy(fulfillmentType = it)) },
            )
        }
        if (draft.requiresDeliveryLocation()) {
            DashboardDeliveryLocationFields(
                draft = draft,
                onDraftChange = onDraftChange,
            )
        }
        Text(
            text = "Payment",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        DashboardCompactSegmentedPicker(
            options = DashboardOrderPaymentModes,
            selected = draft.paymentMode,
            label = { it.paymentModeLabel() },
            onSelected = { onDraftChange(draft.withPaymentMode(it)) },
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 430.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrmaTextField(
                        value = draft.paidTotal,
                        onValueChange = { onDraftChange(draft.copy(paidTotal = it.moneyInput())) },
                        label = "Paid amount",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OrmaSecondaryButton(
                        text = "Mark paid",
                        onClick = onMarkPaid,
                        enabled = draft.items.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    OrmaTextField(
                        value = draft.paidTotal,
                        onValueChange = { onDraftChange(draft.copy(paidTotal = it.moneyInput())) },
                        label = "Paid amount",
                        placeholder = "0",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OrmaSecondaryButton(
                        text = "Mark paid",
                        onClick = onMarkPaid,
                        enabled = draft.items.isNotEmpty(),
                        modifier = Modifier.widthIn(min = 132.dp),
                    )
                }
            }
        }
        if (attemptedSubmit) paidError?.let { FormValidationText(it) }
        OrmaKeyValueList(
            rows = listOf(
                "Total" to totalText,
                "Paid" to dashboardMoney(draft.paidTotal.ifBlank { "0" }, draft.currency),
                "Balance payable" to dashboardMoney(balancePayable.toDashboardMoneyInput(), draft.currency),
            ),
        )
        OrmaTextField(
            value = draft.notes,
            onValueChange = { onDraftChange(draft.copy(notes = it)) },
            label = "Notes",
            placeholder = "Optional",
            singleLine = false,
            minLines = 2,
        )
        OrmaActionRow(
            primaryText = primaryText,
            onPrimary = onSubmit,
            primaryEnabled = draft.items.isNotEmpty(),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
        if (attemptedSubmit && !formReady) {
            FormValidationText("Add at least one valid item and complete required fields.")
        }
    }
}

@Composable
private fun DashboardOrderCartLine(
    index: Int,
    item: OrmaOrderItemDraft,
    orderType: String,
    currency: String,
    product: OrmaProduct?,
    products: List<OrmaProduct>,
    validationMessage: String?,
    expandDetailsInitially: Boolean,
    taxEnabled: Boolean,
    invoiceMode: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onChange: (OrmaOrderItemDraft) -> Unit,
    onRemove: () -> Unit,
) {
    var showDetails by rememberSaveable(item.productId, index, expandDetailsInitially) {
        mutableStateOf(expandDetailsInitially || item.productId.isBlank())
    }
    val lineCurrency = product?.currency ?: currency
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = item.description.ifBlank { "Custom ${orderType.orderItemPickerLabel().lowercase()} ${index + 1}" },
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${dashboardMoney(item.unitPrice.ifBlank { "0" }, lineCurrency)} each",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrderQuantityButton(text = "-", onClick = onDecrement)
                Text(
                    text = item.quantity.ifBlank { "1" },
                    modifier = Modifier.width(42.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                OrderQuantityButton(text = "+", onClick = onIncrement)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildString {
                        append("Line total ")
                        append(orderLineTotal(item, lineCurrency, taxEnabled))
                        if (taxEnabled) {
                            append(" · Tax ")
                            append(item.taxRate.ifBlank { "0" })
                            append("%")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrmaTextButton(
                        text = if (showDetails) "Hide details" else "Edit",
                        onClick = { showDetails = !showDetails },
                    )
                    OrmaTextButton(text = "Remove", onClick = onRemove)
                }
            }
            if (showDetails) {
                if (!invoiceMode && products.isNotEmpty()) {
                    DashboardChipPicker(
                        label = orderType.orderItemPickerLabel(),
                        options = products,
                        selectedId = item.productId,
                        optionId = { it.id },
                        optionLabel = { it.name },
                        onSelected = { selectedProduct ->
                            onChange(
                                item.copy(
                                    productId = selectedProduct.id,
                                    description = selectedProduct.name,
                                    unitPrice = selectedProduct.sellingPrice.ifBlank { item.unitPrice },
                                    taxRate = selectedProduct.taxRate.ifBlank { item.taxRate.ifBlank { "0" } },
                                ),
                            )
                        },
                    )
                }
                OrmaTextField(
                    item.description,
                    { onChange(item.copy(description = it)) },
                    "Description",
                    placeholder = orderType.orderLinePlaceholder(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrmaTextField(
                        item.quantity,
                        { onChange(item.copy(quantity = it.moneyInput())) },
                        "Qty",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OrmaTextField(
                        item.unitPrice,
                        { onChange(item.copy(unitPrice = it.moneyInput())) },
                        "Price",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
                if (taxEnabled) {
                    OrmaTextField(
                        item.taxRate,
                        { onChange(item.copy(taxRate = it.moneyInput())) },
                        if (invoiceMode) "Line tax %" else "Tax %",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
            validationMessage?.let { FormValidationText(it) }
        }
    }
}

@Composable
private fun OrderQuantityButton(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(OrmaShapes.Capsule)
            .clickable(onClick = onClick),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.ScreenBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DashboardOrderItemEditor(
    index: Int,
    item: OrmaOrderItemDraft,
    orderType: String,
    products: List<OrmaProduct>,
    validationMessage: String?,
    onChange: (OrmaOrderItemDraft) -> Unit,
    onRemove: (() -> Unit)?,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Item ${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
            )
            onRemove?.let {
                OrmaTextButton(text = "Remove", onClick = it)
            }
        }
        if (products.isNotEmpty()) {
            DashboardChipPicker(
                label = orderType.orderItemPickerLabel(),
                options = products,
                selectedId = item.productId,
                optionId = { it.id },
                optionLabel = { it.name },
                onSelected = { product ->
                    onChange(
                        item.copy(
                            productId = product.id,
                            description = product.name,
                            unitPrice = product.sellingPrice,
                            taxRate = product.taxRate,
                        ),
                    )
                },
            )
        }
        OrmaTextField(
            item.description,
            { onChange(item.copy(description = it)) },
            "Description",
            placeholder = orderType.orderLinePlaceholder(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(item.quantity, { onChange(item.copy(quantity = it.moneyInput())) }, "Qty", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OrmaTextField(item.unitPrice, { onChange(item.copy(unitPrice = it.moneyInput())) }, "Price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        OrmaTextField(item.taxRate, { onChange(item.copy(taxRate = it.moneyInput())) }, "Tax %", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        validationMessage?.let { FormValidationText(it) }
    }
}

@Composable
private fun <T> DashboardChipPicker(
    label: String,
    options: List<T>,
    selectedId: String,
    optionId: (T) -> String,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var query by rememberSaveable(label) { mutableStateOf("") }
    val visibleOptions = remember(options, query) {
        val tokens = dashboardSearchTokens(query)
        options
            .filter { optionLabel(it).containsAllDashboardTokens(tokens) }
            .take(16)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        if (options.size > 6) {
            OrmaTextField(
                value = query,
                onValueChange = { query = it.take(80) },
                label = "Find $label",
                placeholder = "Type to narrow choices",
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visibleOptions.forEach { option ->
                val selected = optionId(option) == selectedId
                Surface(
                    modifier = Modifier.clickable { onSelected(option) },
                    shape = OrmaShapes.Capsule,
                    color = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                    contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = optionLabel(option),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (visibleOptions.isEmpty()) {
                Text(
                    text = "No matches",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        }
    }
}

private val DashboardOrderStatuses = listOf(
    "draft",
    "confirmed",
    "part_paid",
    "paid",
    "completed",
    "cancelled",
)

private val DashboardActiveOrderStatuses = setOf("draft", "confirmed", "part_paid")

private val DashboardFullPaymentStatuses = setOf("paid", "completed")

private val DashboardOrderStatusFilters = listOf("all") + DashboardOrderStatuses

private val DashboardInvoiceStatusFilters = listOf(
    "all",
    "draft",
    "confirmed",
    "part_paid",
    "paid",
    "completed",
)

private val DashboardOrderPaymentModes = listOf(
    "pay_on_spot",
    DashboardCreditPaymentMode,
    "cash",
    "upi",
    "card",
    "online",
    "bank_transfer",
)

private const val DashboardCreditPaymentMode = "credit"

private val DashboardPrinterConnectionTypes = listOf(
    "mtp_usb",
    "bluetooth",
    "network",
    "airprint",
    "system",
    "esc_pos",
)

private fun String.orderFulfillmentOptions(): List<String> =
    when (trim().lowercase()) {
        "appointment" -> listOf("booking")
        "service" -> listOf("standard", "scheduled", "delivery")
        else -> listOf("standard", "take_away", "delivery", "dine_in", "pickup")
    }

private fun String.fulfillmentModeLabel(): String =
    when (trim().lowercase()) {
        "booking" -> "Booking"
        "take_away" -> "Take away"
        "dine_in" -> "Dine in"
        "pickup" -> "Pickup"
        "delivery" -> "Delivery"
        "scheduled" -> "Scheduled"
        "standard" -> "Standard"
        else -> dashboardTitleCase()
    }

private fun String.paymentModeLabel(): String =
    when (trim().lowercase()) {
        "pay_on_spot" -> "Pay on spot"
        "bank_transfer" -> "Bank transfer"
        "upi" -> "UPI"
        "card" -> "Card"
        "cash" -> "Cash"
        DashboardCreditPaymentMode -> "Credit"
        "online" -> "Online"
        else -> dashboardTitleCase()
    }

private fun OrmaOrderDraft.withPaymentMode(paymentMode: String): OrmaOrderDraft {
    val normalized = paymentMode.trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
    val cleanPaymentMode = normalized.takeIf { it in DashboardOrderPaymentModes } ?: "pay_on_spot"
    return if (cleanPaymentMode == DashboardCreditPaymentMode) {
        copy(
            paymentMode = DashboardCreditPaymentMode,
            paidTotal = "0",
            status = if (status in DashboardFullPaymentStatuses) "confirmed" else status,
        )
    } else {
        copy(paymentMode = cleanPaymentMode)
    }
}

private fun OrmaOrderDraft.withOrderStatus(status: String): OrmaOrderDraft {
    val normalized = status.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
    val cleanStatus = normalized.takeIf { it in DashboardOrderStatuses } ?: "confirmed"
    return if (paymentMode == DashboardCreditPaymentMode && cleanStatus in DashboardFullPaymentStatuses) {
        copy(
            status = cleanStatus,
            paymentMode = "pay_on_spot",
        )
    } else {
        copy(status = cleanStatus)
    }
}

private fun String.printerConnectionLabel(): String =
    when (this) {
        "mtp_usb" -> "MTP / USB"
        "usb" -> "USB"
        "bluetooth" -> "Bluetooth"
        "network" -> "Network IP"
        "airprint" -> "AirPrint"
        "system" -> "System printer"
        "esc_pos" -> "ESC/POS"
        else -> replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

private fun String.dashboardStatusFilterLabel(): String =
    if (this == "all") "All" else dashboardStatusLabel()

private fun String.invoiceStatusFilterLabel(): String =
    when (trim().lowercase()) {
        "all" -> "All invoices"
        "paid" -> "Ready"
        "completed" -> "Completed"
        "part_paid" -> "Part paid"
        "draft" -> "Draft"
        "confirmed" -> "Confirmed"
        else -> dashboardStatusFilterLabel()
    }

private fun String.sellableItemTypeLabel(): String =
    when (trim().lowercase()) {
        "service" -> "Service"
        "appointment" -> "Appointment"
        else -> "Product"
    }

private fun String.categoryScopeLabel(): String =
    when (trim().lowercase()) {
        "all" -> "Shared"
        else -> sellableItemTypeLabel()
    }

private fun OrmaProductCategory.matchesCategoryItemType(itemType: String): Boolean =
    this.itemType.ifBlank { "all" } == "all" || this.itemType == itemType

private fun OrmaProductCategory.categoryOptionLabel(): String =
    if (itemType.ifBlank { "all" } == "all") "$name - Shared" else name

private fun String.orderTypeLabel(): String =
    when (trim().lowercase()) {
        "service" -> "Service"
        "appointment" -> "Appointment"
        else -> "Sale"
    }

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

private fun String.defaultDashboardOrderType(): String =
    when (normalizedDashboardBusinessMode()) {
        "service_selling" -> "service"
        "appointment" -> "appointment"
        else -> "sale"
    }

private fun String.defaultDashboardItemType(): String =
    when (normalizedDashboardBusinessMode()) {
        "service_selling" -> "service"
        "appointment" -> "appointment"
        else -> "product"
    }

private fun String.businessModeBriefEyebrow(): String =
    when (normalizedDashboardBusinessMode()) {
        "service_selling" -> "SERVICE OPERATIONS"
        "appointment" -> "APPOINTMENT OPERATIONS"
        "mixed" -> "MIXED BUSINESS OPERATIONS"
        else -> "PRODUCT SALES OPERATIONS"
    }

private fun OnboardingUiState.dashboardBusinessMode(): String =
    stateDashboardBusinessMode().ifBlank { "product_selling" }

private fun OnboardingUiState.stateDashboardBusinessMode(): String =
    dashboard.summary.businessMode.ifBlank { draft.businessMode }.normalizedDashboardBusinessMode()

private fun OnboardingUiState.allowedDashboardOrderTypes(): List<String> =
    dashboardBusinessMode().allowedDashboardOrderTypes()

private fun OnboardingUiState.allowedDashboardItemTypes(): List<String> =
    dashboardBusinessMode().allowedDashboardItemTypes()

private fun OnboardingUiState.defaultDashboardOrderTypeFilter(): String =
    if (allowedDashboardOrderTypes().size > 1) "all" else allowedDashboardOrderTypes().first()

private fun OnboardingUiState.defaultDashboardItemTypeFilter(): String =
    if (allowedDashboardItemTypes().size > 1) "all" else allowedDashboardItemTypes().first()

private fun OnboardingUiState.dashboardOrderTypeFilterOptions(): List<String> {
    val allowedTypes = allowedDashboardOrderTypes()
    return if (allowedTypes.size > 1) listOf("all") + allowedTypes else allowedTypes
}

private fun OnboardingUiState.dashboardItemTypeFilterOptions(): List<String> {
    val allowedTypes = allowedDashboardItemTypes()
    return if (allowedTypes.size > 1) listOf("all") + allowedTypes else allowedTypes
}

private fun OnboardingUiState.selectedDashboardOrderTypeFilter(): String {
    val allowedTypes = allowedDashboardOrderTypes()
    val selected = dashboard.filters.orderType.trim().lowercase()
    return when {
        allowedTypes.size > 1 && selected == "all" -> "all"
        selected in allowedTypes -> selected
        else -> defaultDashboardOrderTypeFilter()
    }
}

private fun OnboardingUiState.selectedDashboardItemTypeFilter(): String {
    val allowedTypes = allowedDashboardItemTypes()
    val selected = dashboard.filters.itemType.trim().lowercase()
    return when {
        allowedTypes.size > 1 && selected == "all" -> "all"
        selected in allowedTypes -> selected
        else -> defaultDashboardItemTypeFilter()
    }
}

private fun OnboardingUiState.selectedDashboardInvoiceStatusFilter(): String {
    val selected = dashboard.filters.orderStatus.trim().lowercase()
    return selected.takeIf { it in DashboardInvoiceStatusFilters } ?: "all"
}

private fun OnboardingUiState.activeDashboardOrderType(): String =
    selectedDashboardOrderTypeFilter()
        .takeIf { it in allowedDashboardOrderTypes() }
        ?: dashboardBusinessMode().defaultDashboardOrderType()

private fun OnboardingUiState.activeDashboardItemType(): String =
    selectedDashboardItemTypeFilter()
        .takeIf { it in allowedDashboardItemTypes() }
        ?: dashboardBusinessMode().defaultDashboardItemType()

private fun OnboardingUiState.customerOrders(customer: OrmaCustomer): List<OrmaOrder> {
    dashboard.customerOrderHistory[customer.id]?.let { return it }
    val customerName = customer.name.trim()
    return dashboard.orders.filter { order ->
        order.customerId == customer.id ||
            (
                order.customerId.isNullOrBlank() &&
                    customerName.isNotBlank() &&
                    order.customerName?.trim()?.equals(customerName, ignoreCase = true) == true
                )
    }
}

private fun String.orderActionText(): String =
    when (trim().lowercase()) {
        "service" -> "Create service"
        "appointment" -> "Book appointment"
        else -> "Create sale"
    }

private fun String.orderProgressNoun(): String =
    when (trim().lowercase()) {
        "service" -> "service request"
        "appointment" -> "appointment"
        else -> "sale"
    }

private fun String.orderProgressPlural(): String =
    when (trim().lowercase()) {
        "service" -> "service requests"
        "appointment" -> "appointments"
        else -> "sales"
    }

private fun String.orderSectionEyebrow(): String =
    when (trim().lowercase()) {
        "service" -> "SERVICES"
        "appointment" -> "APPOINTMENTS"
        else -> "SALES"
    }

private fun String.orderSectionTitle(): String =
    when (trim().lowercase()) {
        "service" -> "Service desk"
        "appointment" -> "Appointment book"
        else -> "Sales counter"
    }

private fun String.orderSectionDescription(): String =
    when (trim().lowercase()) {
        "service" -> "Create service requests, collect payment, and move work from accepted to completed."
        "appointment" -> "Book appointments, confirm preferred times, and keep upcoming work visible."
        else -> "Create sales, collect payment, and move items through fulfilment."
    }

private fun String.emptyOrderTitle(): String =
    when (trim().lowercase()) {
        "service" -> "No service requests yet"
        "appointment" -> "No appointments yet"
        else -> "No sales yet"
    }

private fun String.emptyOrderBody(): String =
    when (trim().lowercase()) {
        "service" -> "Create a service request with customer, service, optional schedule, payment, and notes."
        "appointment" -> "Book an appointment with customer, appointment service, preferred date/time, and status."
        else -> "Create a sale with customer, product, quantity, payment, and fulfilment details."
    }

private fun String.orderSheetTitle(): String =
    when (trim().lowercase()) {
        "service" -> "Create service request"
        "appointment" -> "Book appointment"
        else -> "Create sale"
    }

private fun String.orderSheetBody(): String =
    when (trim().lowercase()) {
        "service" -> "Capture customer, service, optional schedule, payment, and service status."
        "appointment" -> "Capture customer, appointment service, required preferred date/time, and booking status."
        else -> "Capture customer, products, quantity, payment, and fulfilment status."
    }

private fun String.orderScheduleLabel(): String =
    when (trim().lowercase()) {
        "service" -> "Service date/time"
        "appointment" -> "Preferred date/time"
        else -> "Fulfilment date/time"
    }

private fun String.orderSchedulePlaceholder(): String =
    when (trim().lowercase()) {
        "appointment" -> "Required"
        else -> "Optional"
    }

private fun String.orderItemPickerLabel(): String =
    when (trim().lowercase()) {
        "service" -> "Service"
        "appointment" -> "Appointment service"
        else -> "Product"
    }

private fun String.orderLinePlaceholder(): String =
    when (trim().lowercase()) {
        "service" -> "Service description"
        "appointment" -> "Appointment service"
        else -> "Product"
    }

private fun String.addOrderLineText(): String =
    when (trim().lowercase()) {
        "service" -> "Add service line"
        "appointment" -> "Add appointment line"
        else -> "Add product line"
    }

private fun String.addedOrderItemsTitle(): String =
    when (trim().lowercase()) {
        "service" -> "Added services"
        "appointment" -> "Booked appointment items"
        else -> "Added products"
    }

private fun String.orderCatalogItemType(): String =
    when (trim().lowercase()) {
        "service" -> "service"
        "appointment" -> "appointment"
        else -> "product"
    }

private fun String.catalogActionText(): String =
    when (trim().lowercase()) {
        "service" -> "Add service"
        "appointment" -> "Add appointment service"
        else -> "Add product"
    }

private fun String.catalogSectionEyebrow(): String =
    when (trim().lowercase()) {
        "service" -> "SERVICES"
        "appointment" -> "APPOINTMENTS"
        else -> "PRODUCTS"
    }

private fun String.catalogSectionTitle(): String =
    when (trim().lowercase()) {
        "service" -> "Services catalog"
        "appointment" -> "Appointment services"
        else -> "Products and stock"
    }

private fun String.catalogSectionDescription(): String =
    when (trim().lowercase()) {
        "service" -> "Add services with price, duration, categories, images, and offers."
        "appointment" -> "Add appointment services with duration, booking rules, categories, images, and offers."
        else -> "Add products with price, stock, supplier, SKU, barcode, images, and offers."
    }

private fun String.emptyCatalogTitle(): String =
    when (trim().lowercase()) {
        "service" -> "No services yet"
        "appointment" -> "No appointment services yet"
        else -> "No products yet"
    }

private fun String.emptyCatalogBody(): String =
    when (trim().lowercase()) {
        "service" -> "Add services before creating service requests or sharing your catalog."
        "appointment" -> "Add appointment services before accepting bookings from customers."
        else -> "Add products before creating sales, printing labels, or sharing your catalog."
    }

private fun DashboardSection.title(state: OnboardingUiState): String =
    when (this) {
        DashboardSection.Team -> if (state.accessPath == AccessPath.TeamMember) "My access" else title
        DashboardSection.OrdersBookings -> if (state.selectedDashboardOrderTypeFilter() == "all") {
            title
        } else {
            state.activeDashboardOrderType().orderSectionTitle()
        }
        DashboardSection.Products -> if (state.selectedDashboardItemTypeFilter() == "all") {
            title
        } else {
            state.activeDashboardItemType().catalogSectionTitle()
        }
        else -> title
    }

private fun DashboardSection.description(state: OnboardingUiState): String =
    when (this) {
        DashboardSection.Team -> if (state.accessPath == AccessPath.TeamMember) {
            "View your workspace role and team context."
        } else {
            description
        }
        DashboardSection.OrdersBookings -> if (state.selectedDashboardOrderTypeFilter() == "all") {
            description
        } else {
            state.activeDashboardOrderType().orderSectionDescription()
        }
        DashboardSection.Products -> if (state.selectedDashboardItemTypeFilter() == "all") {
            description
        } else {
            state.activeDashboardItemType().catalogSectionDescription()
        }
        else -> description
    }

private fun invoiceNumberFor(state: OnboardingUiState, order: OrmaOrder): String {
    val prefix = state.draft.invoicePrefix
        .trim()
        .uppercase()
        .ifBlank { "ORMA" }
    val orderRef = order.orderNumber
        .trim()
        .ifBlank { order.id.take(8).uppercase() }
    return "$prefix-$orderRef"
}

private fun invoiceIssuerName(state: OnboardingUiState): String =
    state.workspaceLegalName
        .ifBlank { state.workspaceName }
        .ifBlank { state.draft.legalName }
        .ifBlank { state.draft.businessName }
        .ifBlank { "ORMA Workspace" }

private fun invoiceIssuerSubLine(state: OnboardingUiState): String =
    listOf(
        invoiceRegistrationLine(state),
        invoiceAddressLine(state),
        state.identifier.trim().takeIf { it.isNotBlank() },
    ).filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { "Business billing profile" }

private fun invoiceIssuerDetail(state: OnboardingUiState): String =
    listOf(
        invoiceRegistrationLine(state),
        invoiceAddressLine(state),
        state.identifier.trim().takeIf { it.isNotBlank() }?.let { "Contact $it" },
    ).filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .ifBlank { "Workspace billing details not completed" }

private fun invoiceRegistrationLine(state: OnboardingUiState): String? {
    if (!state.draft.isTaxRegistered) return null
    val taxNumber = state.draft.taxNumber.trim()
    if (taxNumber.isBlank()) return null
    return "${state.draft.taxLabel.ifBlank { "GST/VAT" }} $taxNumber"
}

private fun OrmaOrderDraft.withInvoiceCustomer(customer: OrmaCustomer): OrmaOrderDraft =
    copy(
        customerId = customer.id,
        customerName = "",
        customerPhoneNumber = customer.phoneNumber.orEmpty(),
        customerEmail = customer.email.orEmpty(),
        customerTaxNumber = customer.taxNumber.orEmpty(),
        customerAddressLine = customer.addressLine.orEmpty(),
        customerCity = customer.city.orEmpty(),
        customerRegion = customer.region.orEmpty(),
        customerCountry = customer.country.orEmpty(),
        customerPostalCode = customer.postalCode.orEmpty(),
    )

private fun OrmaOrderDraft.withInvoiceGstinLookup(lookup: OrmaGstinLookup): OrmaOrderDraft {
    val lookupName = lookup.tradeName ?: lookup.legalName ?: ""
    return copy(
        customerName = if (customerId.isBlank()) customerName.ifBlank { lookupName } else customerName,
        customerTaxNumber = lookup.gstin.ifBlank { customerTaxNumber },
        customerAddressLine = customerAddressLine.ifBlank { lookup.addressLine.orEmpty() },
        customerCity = customerCity.ifBlank { lookup.city.orEmpty() },
        customerRegion = customerRegion.ifBlank { lookup.region.orEmpty() },
        customerCountry = customerCountry.ifBlank { "India" },
        customerPostalCode = customerPostalCode.ifBlank { lookup.postalCode.orEmpty() },
    )
}

private fun invoiceAddressLine(state: OnboardingUiState): String =
    listOf(
        state.draft.addressLine,
        state.draft.city,
        state.draft.region,
        state.draft.country,
        state.draft.postalCode,
    ).map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(", ")

private fun invoiceBillToDetail(
    order: OrmaOrder,
    customer: OrmaCustomer?,
): String =
    listOf(
        (customer?.taxNumber ?: order.customerTaxNumber)
            ?.takeIf { it.isNotBlank() }
            ?.let { "GST/VAT $it" },
        customer?.phoneNumber ?: order.customerPhoneNumber,
        customer?.email ?: order.customerEmail,
        customer?.addressLine ?: order.customerAddressLine,
        listOfNotNull(
            customer?.city ?: order.customerCity,
            customer?.region ?: order.customerRegion,
            customer?.country ?: order.customerCountry,
            customer?.postalCode ?: order.customerPostalCode,
        )
            .joinToString(", ")
            .takeIf { it.isNotBlank() },
        order.fulfillmentType.dashboardTitleCase().takeIf { it.isNotBlank() }?.let { "Fulfilment $it" },
    ).filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString("\n")

private fun invoiceIssueDateLabel(order: OrmaOrder): String =
    order.scheduledAt
        ?.takeIf { it.isNotBlank() }
        ?.dashboardDateLabel()
        ?.take(10)
        ?: "Today"

private fun OrmaOrder.invoiceStatusLabel(): String =
    when (status.trim().lowercase()) {
        "paid", "completed" -> "Ready"
        "part_paid" -> "Part paid"
        "draft" -> "Draft"
        "confirmed" -> "Confirmed"
        else -> status.dashboardStatusLabel()
    }

private fun OrmaOrder.invoiceStatusTone(): OrmaStatusTone =
    when (status.trim().lowercase()) {
        "paid", "completed" -> OrmaStatusTone.Success
        "part_paid" -> OrmaStatusTone.Warning
        else -> OrmaStatusTone.Info
    }

private fun invoiceRenderableItems(order: OrmaOrder): List<OrmaOrderItem> {
    if (order.items.isNotEmpty()) return order.items
    val total = order.total.toDoubleOrNull().orZero()
    if (total <= 0.0) return emptyList()

    val subtotalValue = order.subtotal.toDoubleOrNull().orZero().takeIf { it > 0.0 } ?: total
    val taxValue = order.taxTotal.toDoubleOrNull().orZero()
    val inferredTaxRate = if (subtotalValue > 0.0 && taxValue > 0.0) {
        ((taxValue / subtotalValue) * 100.0).toDashboardMoneyInput()
    } else {
        "0"
    }
    val description = when (order.orderType.trim().lowercase()) {
        "appointment" -> "Appointment booking ${order.orderNumber.ifBlank { order.id.take(8) }}"
        "service" -> "Service request ${order.orderNumber.ifBlank { order.id.take(8) }}"
        else -> "Order ${order.orderNumber.ifBlank { order.id.take(8) }}"
    }
    return listOf(
        OrmaOrderItem(
            id = "${order.id}-summary",
            productId = null,
            productName = null,
            description = description,
            quantity = "1",
            unitPrice = subtotalValue.toDashboardMoneyInput(),
            taxRate = inferredTaxRate,
            lineSubtotal = subtotalValue.toDashboardMoneyInput(),
            lineTax = taxValue.toDashboardMoneyInput(),
            lineTotal = order.total.ifBlank { total.toDashboardMoneyInput() },
        ),
    )
}

private data class OrderHtmlDocument(
    val title: String,
    val fileName: String,
    val html: String,
)

private data class OrderPdfDocument(
    val title: String,
    val fileName: String,
    val pdfBase64: String,
)

private fun orderInvoiceDocument(
    state: OnboardingUiState,
    order: OrmaOrder,
): OrderHtmlDocument {
    val invoiceNumber = invoiceNumberFor(state, order)
    val title = "Invoice $invoiceNumber"
    return OrderHtmlDocument(
        title = title,
        fileName = "${orderDocumentFileStem(invoiceNumber)}-invoice.html",
        html = orderInvoiceHtml(state = state, order = order, invoiceNumber = invoiceNumber, title = title),
    )
}

private fun orderInvoicePdfDocument(
    state: OnboardingUiState,
    order: OrmaOrder,
): OrderPdfDocument {
    val invoiceNumber = invoiceNumberFor(state, order)
    val title = "Invoice $invoiceNumber"
    return OrderPdfDocument(
        title = title,
        fileName = "${orderDocumentFileStem(invoiceNumber)}-invoice.pdf",
        pdfBase64 = orderInvoicePdfBase64(state = state, order = order, invoiceNumber = invoiceNumber),
    )
}

private fun orderReceiptDocument(
    state: OnboardingUiState,
    order: OrmaOrder,
): OrderHtmlDocument {
    val reference = order.orderNumber.ifBlank { order.id.take(8).uppercase() }
    val title = "Receipt $reference"
    return OrderHtmlDocument(
        title = title,
        fileName = "${orderDocumentFileStem(reference)}-receipt.html",
        html = orderReceiptHtml(state = state, order = order, reference = reference, title = title),
    )
}

private fun orderReceiptPdfDocument(
    state: OnboardingUiState,
    order: OrmaOrder,
): OrderPdfDocument {
    val reference = order.orderNumber.ifBlank { order.id.take(8).uppercase() }
    val title = "Receipt $reference"
    return OrderPdfDocument(
        title = title,
        fileName = "${orderDocumentFileStem(reference)}-receipt.pdf",
        pdfBase64 = orderReceiptPdfBase64(state = state, order = order, reference = reference),
    )
}

private fun orderInvoicePdfBase64(
    state: OnboardingUiState,
    order: OrmaOrder,
    invoiceNumber: String,
): String {
    val customer = order.customerId?.let { id -> state.dashboard.customers.firstOrNull { it.id == id } }
    val billToName = order.customerName?.takeIf { it.isNotBlank() } ?: customer?.name ?: "Walk-in customer"
    val billToDetail = invoiceBillToDetail(order = order, customer = customer)
    val reference = order.orderNumber.ifBlank { order.id.take(8).uppercase() }
    val canvas = OrderPdfCanvas()

    canvas.fillRect(24.0, 24.0, 547.0, 794.0, "FFFDF8")
    canvas.strokeRect(24.0, 24.0, 547.0, 794.0, "E7E0D4", 0.8)
    canvas.fillRect(36.0, 744.0, 523.0, 68.0, "143D3D")
    canvas.text(56.0, 786.0, invoiceIssuerName(state), 18, "FFFDF8", "F2")
    drawOrderPdfTextBlock(
        canvas = canvas,
        x = 56.0,
        y = 767.0,
        text = invoiceIssuerSubLine(state),
        maxChars = 48,
        maxLines = 2,
        size = 9,
        color = "D8E4E0",
    )
    canvas.text(402.0, 786.0, "TAX INVOICE", 9, "D8E4E0", "F2")
    canvas.text(402.0, 768.0, invoiceNumber, 13, "FFFDF8", "F2")
    canvas.text(402.0, 752.0, invoiceIssueDateLabel(order), 9, "D8E4E0")

    val metaY = 678.0
    val metaWidth = 119.0
    drawOrderPdfMetaCard(canvas, 56.0, metaY, metaWidth, "Issue date", invoiceIssueDateLabel(order))
    drawOrderPdfMetaCard(canvas, 183.0, metaY, metaWidth, "Reference", reference)
    drawOrderPdfMetaCard(canvas, 310.0, metaY, metaWidth, "Status", order.status.dashboardStatusLabel())
    drawOrderPdfMetaCard(canvas, 437.0, metaY, metaWidth, "Payment", order.paymentMode.paymentModeLabel())

    canvas.fillRect(56.0, 580.0, 235.0, 74.0, "FFFFFF")
    canvas.strokeRect(56.0, 580.0, 235.0, 74.0, "E7E0D4", 0.7)
    canvas.text(72.0, 635.0, "ISSUED BY", 8, "7C9290", "F2")
    canvas.text(72.0, 618.0, invoiceIssuerName(state), 11, "143D3D", "F2")
    drawOrderPdfTextBlock(
        canvas = canvas,
        x = 72.0,
        y = 602.0,
        text = invoiceIssuerDetail(state),
        maxChars = 42,
        maxLines = 3,
        size = 8,
        color = "7C9290",
    )

    canvas.fillRect(304.0, 580.0, 235.0, 74.0, "FFFFFF")
    canvas.strokeRect(304.0, 580.0, 235.0, 74.0, "E7E0D4", 0.7)
    canvas.text(320.0, 635.0, "BILL TO", 8, "7C9290", "F2")
    canvas.text(320.0, 618.0, billToName, 11, "143D3D", "F2")
    drawOrderPdfTextBlock(
        canvas = canvas,
        x = 320.0,
        y = 602.0,
        text = billToDetail.ifBlank { "Details not available" },
        maxChars = 42,
        maxLines = 3,
        size = 8,
        color = "7C9290",
    )

    canvas.text(56.0, 548.0, "Line items", 12, "143D3D", "F2")
    var rowTop = 512.0
    canvas.fillRect(56.0, rowTop, 483.0, 24.0, "F4EADB")
    canvas.text(64.0, rowTop + 8.0, "#", 8, "7C9290", "F2")
    canvas.text(94.0, rowTop + 8.0, "ITEM / DESCRIPTION", 8, "7C9290", "F2")
    canvas.text(300.0, rowTop + 8.0, "QTY", 8, "7C9290", "F2")
    canvas.text(350.0, rowTop + 8.0, "UNIT", 8, "7C9290", "F2")
    canvas.text(415.0, rowTop + 8.0, "TAX", 8, "7C9290", "F2")
    canvas.text(472.0, rowTop + 8.0, "AMOUNT", 8, "7C9290", "F2")
    rowTop -= 34.0

    val items = invoiceRenderableItems(order)
    if (items.isEmpty()) {
        canvas.text(72.0, rowTop, "No billable line items are available for this invoice yet.", 9, "7C9290")
        rowTop -= 34.0
    } else {
        val visibleItems = items.take(6)
        visibleItems.forEachIndexed { index, item ->
            val description = item.description.ifBlank { item.productName ?: "Line item" }
            canvas.line(56.0, rowTop + 16.0, 539.0, rowTop + 16.0, "EFE8DD", 0.6)
            canvas.text(64.0, rowTop, (index + 1).toString().padStart(2, '0'), 9, "143D3D", "F2")
            canvas.text(94.0, rowTop, description, 10, "143D3D", "F2")
            canvas.text(
                94.0,
                rowTop - 14.0,
                "Qty ${orderQuantityText(item.quantity.toDoubleOrNull().orZero())} | ${dashboardMoney(item.unitPrice, order.currency)} | VAT ${item.taxRate.invoiceTaxLabel()}",
                8,
                "7C9290",
            )
            canvas.text(300.0, rowTop, orderQuantityText(item.quantity.toDoubleOrNull().orZero()), 9, "143D3D")
            canvas.text(350.0, rowTop, dashboardMoney(item.unitPrice, order.currency), 9, "143D3D")
            canvas.text(415.0, rowTop, item.taxRate.invoiceTaxLabel(), 9, "143D3D")
            canvas.text(472.0, rowTop, dashboardMoney(item.lineTotal, order.currency), 9, "143D3D", "F2")
            rowTop -= 40.0
        }
        if (items.size > visibleItems.size) {
            canvas.line(56.0, rowTop + 16.0, 539.0, rowTop + 16.0, "EFE8DD", 0.6)
            canvas.text(94.0, rowTop, "+ ${items.size - visibleItems.size} more line items are available in ORMA.", 9, "7C9290")
            rowTop -= 28.0
        }
    }

    val totalsTop = rowTop.coerceAtLeast(136.0)
    canvas.fillRect(332.0, totalsTop - 10.0, 207.0, 132.0, "FFFFFF")
    canvas.strokeRect(332.0, totalsTop - 10.0, 207.0, 132.0, "E7E0D4", 0.7)
    var totalY = totalsTop + 94.0
    totalY = drawOrderPdfTotalRow(canvas, totalY, "Subtotal", dashboardMoney(order.subtotal, order.currency))
    totalY = drawOrderPdfTotalRow(canvas, totalY, "Tax", dashboardMoney(order.taxTotal, order.currency))
    totalY = drawOrderPdfTotalRow(canvas, totalY, "Discount", dashboardMoney(order.discountTotal, order.currency))
    totalY = drawOrderPdfTotalRow(canvas, totalY, "Paid", dashboardMoney(order.paidTotal, order.currency))
    totalY = drawOrderPdfTotalRow(canvas, totalY, "Balance", order.balanceDueText())
    canvas.line(348.0, totalY + 7.0, 523.0, totalY + 7.0, "E7E0D4", 0.7)
    canvas.text(348.0, totalY - 8.0, "Total", 11, "143D3D", "F2")
    canvas.text(430.0, totalY - 8.0, dashboardMoney(order.total, order.currency), 11, "143D3D", "F2")

    canvas.text(56.0, totalsTop + 96.0, "Amount in words", 8, "7C9290", "F2")
    drawOrderPdfTextBlock(
        canvas = canvas,
        x = 56.0,
        y = totalsTop + 78.0,
        text = invoiceAmountInWords(order),
        maxChars = 43,
        maxLines = 4,
        size = 9,
        color = "143D3D",
    )
    canvas.line(56.0, 68.0, 539.0, 68.0, "E7E0D4", 0.7)
    canvas.text(56.0, 48.0, "Generated from ORMA sales records.", 8, "7C9290")

    return orderDocumentPdfFileBase64(content = canvas.content())
}

private fun orderReceiptPdfBase64(
    state: OnboardingUiState,
    order: OrmaOrder,
    reference: String,
): String {
    val customerName = order.customerName?.takeIf { it.isNotBlank() } ?: "Walk-in customer"
    val canvas = OrderPdfCanvas()
    val x = 150.0
    val width = 295.0

    canvas.fillRect(24.0, 24.0, 547.0, 794.0, "FFFDF8")
    canvas.fillRect(x, 58.0, width, 726.0, "FFFFFF")
    canvas.strokeRect(x, 58.0, width, 726.0, "E7E0D4", 0.8)
    canvas.fillRect(x, 734.0, width, 50.0, "143D3D")
    canvas.text(x + 20.0, 764.0, invoiceIssuerName(state), 14, "FFFDF8", "F2")
    canvas.text(x + 20.0, 746.0, "Receipt $reference", 9, "D8E4E0")

    var y = 704.0
    y = drawOrderPdfReceiptRow(canvas, x, y, "Date", invoiceIssueDateLabel(order))
    y = drawOrderPdfReceiptRow(canvas, x, y, "Customer", customerName)
    y = drawOrderPdfReceiptRow(canvas, x, y, "Status", order.status.dashboardStatusLabel())
    y = drawOrderPdfReceiptRow(canvas, x, y, "Payment", order.paymentMode.paymentModeLabel())
    canvas.line(x + 20.0, y - 2.0, x + width - 20.0, y - 2.0, "E7E0D4", 0.8)
    y -= 26.0

    val items = invoiceRenderableItems(order)
    if (items.isEmpty()) {
        canvas.text(x + 20.0, y, "No receipt line items available.", 9, "7C9290")
        y -= 24.0
    } else {
        items.take(8).forEach { item ->
            val description = item.description.ifBlank { item.productName ?: "Line item" }
            canvas.text(x + 20.0, y, description, 9, "143D3D", "F2")
            canvas.text(x + 190.0, y, dashboardMoney(item.lineTotal, order.currency), 9, "143D3D", "F2")
            canvas.text(
                x + 20.0,
                y - 14.0,
                "${orderQuantityText(item.quantity.toDoubleOrNull().orZero())} x ${dashboardMoney(item.unitPrice, order.currency)} | Tax ${item.taxRate.invoiceTaxLabel()}",
                8,
                "7C9290",
            )
            y -= 34.0
        }
        if (items.size > 8) {
            canvas.text(x + 20.0, y, "+ ${items.size - 8} more items", 8, "7C9290")
            y -= 18.0
        }
    }

    canvas.line(x + 20.0, y + 6.0, x + width - 20.0, y + 6.0, "E7E0D4", 0.8)
    y -= 16.0
    y = drawOrderPdfReceiptRow(canvas, x, y, "Subtotal", dashboardMoney(order.subtotal, order.currency))
    y = drawOrderPdfReceiptRow(canvas, x, y, "Tax", dashboardMoney(order.taxTotal, order.currency))
    y = drawOrderPdfReceiptRow(canvas, x, y, "Paid", dashboardMoney(order.paidTotal, order.currency))
    y = drawOrderPdfReceiptRow(canvas, x, y, "Balance", order.balanceDueText())
    canvas.line(x + 20.0, y + 8.0, x + width - 20.0, y + 8.0, "143D3D", 0.9)
    canvas.text(x + 20.0, y - 10.0, "Total", 12, "143D3D", "F2")
    canvas.text(x + 175.0, y - 10.0, dashboardMoney(order.total, order.currency), 12, "143D3D", "F2")
    canvas.text(x + 112.0, 86.0, "Thank you", 10, "7C9290", "F2")

    return orderDocumentPdfFileBase64(content = canvas.content())
}

private fun orderInvoiceHtml(
    state: OnboardingUiState,
    order: OrmaOrder,
    invoiceNumber: String,
    title: String,
): String {
    val customer = order.customerId?.let { id -> state.dashboard.customers.firstOrNull { it.id == id } }
    val billToName = order.customerName?.takeIf { it.isNotBlank() } ?: customer?.name ?: "Walk-in customer"
    val billToDetail = invoiceBillToDetail(order = order, customer = customer)
    return """
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>${title.orderDocumentEscaped()}</title>
          <style>
            * { box-sizing: border-box; }
            body {
              margin: 0;
              background: #F4F7FB;
              color: #173B3D;
              font-family: "Google Sans", Arial, Helvetica, sans-serif;
              font-size: 14px;
              line-height: 1.45;
            }
            .page {
              max-width: 880px;
              margin: 28px auto;
              background: #FFFFFF;
              border: 1px solid #EEF2FF;
              border-radius: 12px;
              overflow: hidden;
            }
            .header {
              display: flex;
              justify-content: space-between;
              gap: 24px;
              padding: 28px;
              background: #4F46E5;
              color: #FFFFFF;
            }
            h1, h2, h3, p { margin: 0; }
            h1 { font-size: 26px; font-weight: 600; }
            h2 { font-size: 18px; font-weight: 600; }
            .muted { color: rgba(23, 59, 61, 0.56); }
            .header .muted { color: rgba(255, 255, 255, 0.72); }
            .header .label { color: rgba(255, 255, 255, 0.72); }
            .content { padding: 28px; }
            .meta {
              display: grid;
              grid-template-columns: repeat(4, 1fr);
              gap: 12px;
              margin-bottom: 24px;
            }
            .cell {
              padding: 14px;
              background: #EEF2FF;
              border: 1px solid rgba(129, 140, 248, 0.35);
              border-radius: 10px;
            }
            .label {
              display: block;
              margin-bottom: 5px;
              color: rgba(23, 59, 61, 0.56);
              font-size: 11px;
              font-weight: 600;
              letter-spacing: 0.08em;
              text-transform: uppercase;
            }
            .party {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 20px;
              margin-bottom: 26px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              margin: 10px 0 24px;
            }
            th {
              padding: 10px 8px;
              color: rgba(23, 59, 61, 0.56);
              border-bottom: 1px solid rgba(23, 59, 61, 0.24);
              font-size: 11px;
              letter-spacing: 0.08em;
              text-align: left;
              text-transform: uppercase;
            }
            td {
              padding: 12px 8px;
              border-bottom: 1px solid #EEF2FF;
              vertical-align: top;
            }
            .right { text-align: right; }
            .totals {
              width: min(360px, 100%);
              margin-left: auto;
            }
            .total-row {
              display: flex;
              justify-content: space-between;
              gap: 16px;
              padding: 7px 0;
              border-bottom: 1px solid #EEF2FF;
            }
            .grand {
              padding-top: 12px;
              font-size: 22px;
              font-weight: 600;
              border-bottom: 0;
            }
            .footer {
              margin-top: 28px;
              padding-top: 18px;
              border-top: 1px solid #EEF2FF;
              color: rgba(23, 59, 61, 0.56);
              font-size: 12px;
            }
            @media print {
              body { background: #fff; }
              .page { margin: 0; max-width: none; border: 0; border-radius: 0; }
            }
          </style>
        </head>
        <body>
          <main class="page">
            <section class="header">
              <div>
                <h1>${invoiceIssuerName(state).orderDocumentEscaped()}</h1>
                <p class="muted">${invoiceIssuerSubLine(state).orderDocumentLineBreaks()}</p>
              </div>
              <div class="right">
                <p class="label">Tax invoice</p>
                <h2>${invoiceNumber.orderDocumentEscaped()}</h2>
                <p class="muted">${invoiceIssueDateLabel(order).orderDocumentEscaped()}</p>
              </div>
            </section>
            <section class="content">
              <div class="meta">
                ${orderDocumentMetaCell("Issue date", invoiceIssueDateLabel(order))}
                ${orderDocumentMetaCell("Reference", order.orderNumber.ifBlank { order.id.take(8).uppercase() })}
                ${orderDocumentMetaCell("Status", order.status.dashboardStatusLabel())}
                ${orderDocumentMetaCell("Payment", order.paymentMode.paymentModeLabel())}
              </div>
              <div class="party">
                <div>
                  <span class="label">Issued by</span>
                  <h2>${invoiceIssuerName(state).orderDocumentEscaped()}</h2>
                  <p class="muted">${invoiceIssuerDetail(state).orderDocumentLineBreaks()}</p>
                </div>
                <div>
                  <span class="label">Bill to</span>
                  <h2>${billToName.orderDocumentEscaped()}</h2>
                  <p class="muted">${billToDetail.ifBlank { "Details not available" }.orderDocumentLineBreaks()}</p>
                </div>
              </div>
              ${orderDocumentItemsTable(order)}
              <section class="totals">
                ${orderDocumentTotalRow("Subtotal", dashboardMoney(order.subtotal, order.currency))}
                ${orderDocumentTotalRow("Tax", dashboardMoney(order.taxTotal, order.currency))}
                ${orderDocumentTotalRow("Discount", dashboardMoney(order.discountTotal, order.currency))}
                ${orderDocumentTotalRow("Paid", dashboardMoney(order.paidTotal, order.currency))}
                ${orderDocumentTotalRow("Balance", order.balanceDueText())}
                <div class="total-row grand"><span>Total</span><span>${dashboardMoney(order.total, order.currency).orderDocumentEscaped()}</span></div>
              </section>
              <p class="footer">Amount in words: ${invoiceAmountInWords(order).orderDocumentEscaped()}</p>
            </section>
          </main>
        </body>
        </html>
    """.trimIndent()
}

private fun orderReceiptHtml(
    state: OnboardingUiState,
    order: OrmaOrder,
    reference: String,
    title: String,
): String =
    """
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>${title.orderDocumentEscaped()}</title>
          <style>
            @page { size: 80mm auto; margin: 4mm; }
            * { box-sizing: border-box; }
            body {
              width: 72mm;
              margin: 0 auto;
              color: #173B3D;
              background: #fff;
              font-family: "Google Sans", Arial, Helvetica, sans-serif;
              font-size: 12px;
              line-height: 1.35;
            }
            .receipt { padding: 10px 0; }
            .center { text-align: center; }
            h1 { margin: 0 0 4px; font-size: 18px; font-weight: 600; }
            p { margin: 0; }
            .muted { color: rgba(23, 59, 61, 0.56); }
            .rule { border-top: 1px dashed rgba(23, 59, 61, 0.32); margin: 10px 0; }
            .row {
              display: flex;
              justify-content: space-between;
              gap: 8px;
              padding: 3px 0;
            }
            .item { padding: 5px 0; }
            .item-name { font-weight: 600; }
            .total {
              margin-top: 6px;
              padding-top: 8px;
              border-top: 1px solid #173B3D;
              font-size: 16px;
              font-weight: 600;
            }
            @media screen {
              body { background: #F4F7FB; }
              .receipt {
                margin: 16px auto;
                padding: 14px;
                background: #FFFFFF;
                border: 1px solid #EEF2FF;
                border-radius: 8px;
              }
            }
          </style>
        </head>
        <body>
          <main class="receipt">
            <section class="center">
              <h1>${invoiceIssuerName(state).orderDocumentEscaped()}</h1>
              <p class="muted">${invoiceIssuerSubLine(state).orderDocumentLineBreaks()}</p>
            </section>
            <div class="rule"></div>
            ${orderDocumentReceiptRow("Receipt", reference)}
            ${orderDocumentReceiptRow("Date", invoiceIssueDateLabel(order))}
            ${orderDocumentReceiptRow("Customer", order.customerName?.takeIf { it.isNotBlank() } ?: "Walk-in customer")}
            ${orderDocumentReceiptRow("Status", order.status.dashboardStatusLabel())}
            <div class="rule"></div>
            ${orderDocumentReceiptItems(order)}
            <div class="rule"></div>
            ${orderDocumentReceiptRow("Subtotal", dashboardMoney(order.subtotal, order.currency))}
            ${orderDocumentReceiptRow("Tax", dashboardMoney(order.taxTotal, order.currency))}
            ${orderDocumentReceiptRow("Paid", dashboardMoney(order.paidTotal, order.currency))}
            ${orderDocumentReceiptRow("Balance", order.balanceDueText())}
            <div class="row total"><span>Total</span><span>${dashboardMoney(order.total, order.currency).orderDocumentEscaped()}</span></div>
            <div class="rule"></div>
            <p class="center muted">Thank you</p>
          </main>
        </body>
        </html>
    """.trimIndent()

private fun orderDocumentItemsTable(order: OrmaOrder): String {
    val rows = invoiceRenderableItems(order).mapIndexed { index, item ->
        val description = item.description.ifBlank { item.productName ?: "Line item" }
        """
            <tr>
              <td>${(index + 1).toString().padStart(2, '0').orderDocumentEscaped()}</td>
              <td>${description.orderDocumentEscaped()}</td>
              <td class="right">${orderQuantityText(item.quantity.toDoubleOrNull().orZero()).orderDocumentEscaped()}</td>
              <td class="right">${dashboardMoney(item.unitPrice, order.currency).orderDocumentEscaped()}</td>
              <td class="right">${item.taxRate.invoiceTaxLabel().orderDocumentEscaped()}</td>
              <td class="right">${dashboardMoney(item.lineTotal, order.currency).orderDocumentEscaped()}</td>
            </tr>
        """.trimIndent()
    }.joinToString("\n")
    val body = rows.ifBlank {
        """
            <tr>
              <td colspan="6">No billable line items are available for this invoice yet.</td>
            </tr>
        """.trimIndent()
    }
    return """
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Item / Description</th>
              <th class="right">Qty</th>
              <th class="right">Unit price</th>
              <th class="right">VAT</th>
              <th class="right">Amount</th>
            </tr>
          </thead>
          <tbody>
            $body
          </tbody>
        </table>
    """.trimIndent()
}

private fun orderDocumentReceiptItems(order: OrmaOrder): String {
    val rows = invoiceRenderableItems(order).map { item ->
        val description = item.description.ifBlank { item.productName ?: "Line item" }
        """
            <div class="item">
              <div class="row"><span class="item-name">${description.orderDocumentEscaped()}</span><span>${dashboardMoney(item.lineTotal, order.currency).orderDocumentEscaped()}</span></div>
              <p class="muted">${orderQuantityText(item.quantity.toDoubleOrNull().orZero()).orderDocumentEscaped()} x ${dashboardMoney(item.unitPrice, order.currency).orderDocumentEscaped()} · Tax ${item.taxRate.invoiceTaxLabel().orderDocumentEscaped()}</p>
            </div>
        """.trimIndent()
    }.joinToString("\n")
    return rows.ifBlank {
        """<p class="muted">No receipt line items available.</p>"""
    }
}

private fun orderDocumentMetaCell(
    label: String,
    value: String,
): String =
    """<div class="cell"><span class="label">${label.orderDocumentEscaped()}</span><strong>${value.orderDocumentEscaped()}</strong></div>"""

private fun orderDocumentTotalRow(
    label: String,
    value: String,
): String =
    """<div class="total-row"><span>${label.orderDocumentEscaped()}</span><span>${value.orderDocumentEscaped()}</span></div>"""

private fun orderDocumentReceiptRow(
    label: String,
    value: String,
): String =
    """<div class="row"><span>${label.orderDocumentEscaped()}</span><span>${value.orderDocumentEscaped()}</span></div>"""

private class OrderPdfCanvas {
    private val commands = StringBuilder()

    fun fillRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        color: String,
    ) {
        commands.append("q\n")
            .append(color.orderPdfRgb())
            .append(" rg\n")
            .append(x.orderPdfNumber())
            .append(' ')
            .append(y.orderPdfNumber())
            .append(' ')
            .append(width.orderPdfNumber())
            .append(' ')
            .append(height.orderPdfNumber())
            .append(" re f\nQ\n")
    }

    fun strokeRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        color: String,
        lineWidth: Double,
    ) {
        commands.append("q\n")
            .append(color.orderPdfRgb())
            .append(" RG\n")
            .append(lineWidth.orderPdfNumber())
            .append(" w\n")
            .append(x.orderPdfNumber())
            .append(' ')
            .append(y.orderPdfNumber())
            .append(' ')
            .append(width.orderPdfNumber())
            .append(' ')
            .append(height.orderPdfNumber())
            .append(" re S\nQ\n")
    }

    fun line(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        color: String,
        lineWidth: Double,
    ) {
        commands.append("q\n")
            .append(color.orderPdfRgb())
            .append(" RG\n")
            .append(lineWidth.orderPdfNumber())
            .append(" w\n")
            .append(x1.orderPdfNumber())
            .append(' ')
            .append(y1.orderPdfNumber())
            .append(" m ")
            .append(x2.orderPdfNumber())
            .append(' ')
            .append(y2.orderPdfNumber())
            .append(" l S\nQ\n")
    }

    fun text(
        x: Double,
        y: Double,
        value: String,
        size: Int,
        color: String,
        font: String = "F1",
    ) {
        val safeValue = value.orderDocumentPdfSafe()
        if (safeValue.isBlank()) return
        commands.append("BT\n/")
            .append(font)
            .append(' ')
            .append(size)
            .append(" Tf\n")
            .append(color.orderPdfRgb())
            .append(" rg\n1 0 0 1 ")
            .append(x.orderPdfNumber())
            .append(' ')
            .append(y.orderPdfNumber())
            .append(" Tm\n(")
            .append(safeValue.orderDocumentPdfEscaped())
            .append(") Tj\nET\n")
    }

    fun content(): String = commands.toString()
}

private fun drawOrderPdfMetaCard(
    canvas: OrderPdfCanvas,
    x: Double,
    y: Double,
    width: Double,
    label: String,
    value: String,
) {
    canvas.fillRect(x, y, width, 48.0, "FFFFFF")
    canvas.strokeRect(x, y, width, 48.0, "E7E0D4", 0.7)
    canvas.text(x + 12.0, y + 29.0, label.uppercase(), 7, "7C9290", "F2")
    canvas.text(x + 12.0, y + 13.0, value, 9, "143D3D", "F2")
}

private fun drawOrderPdfTextBlock(
    canvas: OrderPdfCanvas,
    x: Double,
    y: Double,
    text: String,
    maxChars: Int,
    maxLines: Int,
    size: Int,
    color: String,
    font: String = "F1",
    lineGap: Double = size + 3.0,
): Double {
    val wrappedLines = text
        .split('\n')
        .flatMap { it.orderDocumentPdfWrappedLines(maxLength = maxChars) }
        .ifEmpty { listOf("") }
        .toMutableList()
    val visibleLines = wrappedLines.take(maxLines).toMutableList()
    if (wrappedLines.size > maxLines && visibleLines.isNotEmpty()) {
        val lastIndex = visibleLines.lastIndex
        val lastLine = visibleLines[lastIndex].take((maxChars - 3).coerceAtLeast(1)).trimEnd()
        visibleLines[lastIndex] = "$lastLine..."
    }
    var currentY = y
    visibleLines.forEach { line ->
        canvas.text(x, currentY, line, size, color, font)
        currentY -= lineGap
    }
    return currentY
}

private fun drawOrderPdfTotalRow(
    canvas: OrderPdfCanvas,
    y: Double,
    label: String,
    value: String,
): Double {
    canvas.text(348.0, y, label, 8, "7C9290")
    canvas.text(430.0, y, value, 8, "143D3D", "F2")
    return y - 17.0
}

private fun drawOrderPdfReceiptRow(
    canvas: OrderPdfCanvas,
    x: Double,
    y: Double,
    label: String,
    value: String,
): Double {
    canvas.text(x + 20.0, y, label, 8, "7C9290")
    canvas.text(x + 145.0, y, value, 8, "143D3D", "F2")
    return y - 19.0
}

private fun orderDocumentFileStem(value: String): String =
    value
        .trim()
        .ifBlank { "order" }
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-')
        .ifBlank { "order" }

private fun String.orderDocumentEscaped(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

private fun String.orderDocumentLineBreaks(): String =
    orderDocumentEscaped().replace("\n", "<br>")

private fun orderDocumentPdfBase64(lines: List<String>): String {
    val maxLines = 58
    val printableLines = if (lines.size > maxLines) {
        lines.take(maxLines - 1) + "Additional invoice lines are available in the on-screen preview."
    } else {
        lines
    }
    val content = buildString {
        append("BT\n")
        append("/F1 10 Tf\n")
        append("13 TL\n")
        append("50 800 Td\n")
        printableLines.forEach { line ->
            append("(")
            append(line.orderDocumentPdfEscaped())
            append(") Tj\n")
            append("T*\n")
        }
        append("ET\n")
    }
    return orderDocumentPdfFileBase64(content = content)
}

private fun orderDocumentPdfFileBase64(content: String): String {
    val objects = listOf(
        "<< /Type /Catalog /Pages 2 0 R >>",
        "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>",
        "<< /Length ${content.encodeToByteArray().size} >>\nstream\n$content\nendstream",
    )
    val builder = StringBuilder("%PDF-1.4\n")
    val offsets = mutableListOf<Int>()
    objects.forEachIndexed { index, body ->
        offsets += builder.toString().encodeToByteArray().size
        builder.append(index + 1)
            .append(" 0 obj\n")
            .append(body)
            .append("\nendobj\n")
    }
    val xrefOffset = builder.toString().encodeToByteArray().size
    builder.append("xref\n")
        .append("0 ")
        .append(objects.size + 1)
        .append("\n")
        .append("0000000000 65535 f \n")
    offsets.forEach { offset ->
        builder.append(offset.toString().padStart(10, '0'))
            .append(" 00000 n \n")
    }
    builder.append("trailer\n")
        .append("<< /Size ")
        .append(objects.size + 1)
        .append(" /Root 1 0 R >>\n")
        .append("startxref\n")
        .append(xrefOffset)
        .append("\n%%EOF")
    return builder.toString().encodeToByteArray().orderDocumentBase64()
}

private fun String.orderPdfRgb(): String {
    val hex = trim().removePrefix("#").take(6).padEnd(6, '0')
    val red = hex.substring(0, 2).toIntOrNull(16) ?: 0
    val green = hex.substring(2, 4).toIntOrNull(16) ?: 0
    val blue = hex.substring(4, 6).toIntOrNull(16) ?: 0
    return "${(red / 255.0).orderPdfNumber()} ${(green / 255.0).orderPdfNumber()} ${(blue / 255.0).orderPdfNumber()}"
}

private fun Double.orderPdfNumber(): String {
    val rounded = kotlin.math.round(this * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

private fun String.orderDocumentPdfWrappedLines(maxLength: Int = 88): List<String> {
    val safe = orderDocumentPdfSafe()
    if (safe.isBlank()) return listOf("")
    val lines = mutableListOf<String>()
    var current = ""
    safe.split(' ')
        .filter { it.isNotBlank() }
        .forEach { word ->
            if (word.length > maxLength) {
                if (current.isNotBlank()) {
                    lines += current
                    current = ""
                }
                lines += word.chunked(maxLength)
            } else if (current.isBlank()) {
                current = word
            } else if (current.length + 1 + word.length <= maxLength) {
                current += " $word"
            } else {
                lines += current
                current = word
            }
        }
    if (current.isNotBlank()) lines += current
    return lines
}

private fun String.orderDocumentPdfEscaped(): String =
    orderDocumentPdfSafe()
        .replace("\\", "\\\\")
        .replace("(", "\\(")
        .replace(")", "\\)")

private fun String.orderDocumentPdfSafe(): String =
    buildString {
        this@orderDocumentPdfSafe.forEach { character ->
            when {
                character == '\n' || character == '\r' || character == '\t' -> append(' ')
                character.code in 32..126 -> append(character)
                else -> append('?')
            }
        }
    }
        .replace(Regex("\\s+"), " ")
        .trim()

private fun ByteArray.orderDocumentBase64(): String {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val output = StringBuilder(((size + 2) / 3) * 4)
    var index = 0
    while (index < size) {
        val first = this[index++].toInt() and 0xff
        val second = if (index < size) this[index++].toInt() and 0xff else -1
        val third = if (index < size) this[index++].toInt() and 0xff else -1
        val secondIndex = ((first and 0x03) shl 4) or (if (second >= 0) (second shr 4) else 0)
        val thirdIndex = if (second >= 0) {
            ((second and 0x0f) shl 2) or (if (third >= 0) (third shr 6) else 0)
        } else {
            0
        }
        output.append(alphabet[first shr 2])
        output.append(alphabet[secondIndex])
        output.append(if (second >= 0) alphabet[thirdIndex] else '=')
        output.append(if (third >= 0) alphabet[third and 0x3f] else '=')
    }
    return output.toString()
}

private fun String.invoiceTaxLabel(): String {
    val tax = toDoubleOrNull().orZero()
    return if (tax > 0.0) {
        "${orderQuantityText(tax)}%"
    } else {
        "No tax"
    }
}

private fun invoiceAmountInWords(order: OrmaOrder): String {
    val amount = order.total.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val whole = kotlin.math.floor(amount).toLong()
    val cents = kotlin.math.round((amount - whole) * 100.0).toInt().coerceIn(0, 99)
    return buildString {
        append(order.currency.ifBlank { "INR" })
        append(" ")
        append(invoiceNumberToWords(whole))
        if (cents > 0) {
            append(" and ")
            append(invoiceNumberToWords(cents.toLong()))
            append(" cents")
        }
        append(" Only")
    }
}

private fun invoiceNumberToWords(number: Long): String {
    val ones = listOf(
        "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen",
    )
    val tens = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
    fun below100(value: Long): String =
        if (value < 20) {
            ones[value.toInt()]
        } else {
            tens[(value / 10).toInt()] + if (value % 10 > 0) " ${ones[(value % 10).toInt()]}" else ""
        }
    fun below1000(value: Long): String =
        if (value < 100) {
            below100(value)
        } else {
            ones[(value / 100).toInt()] + " Hundred" + if (value % 100 > 0) " and ${below100(value % 100)}" else ""
        }
    if (number == 0L) return "Zero"
    var remaining = number
    val parts = mutableListOf<String>()
    if (remaining >= 1_000_000L) {
        parts += "${below1000(remaining / 1_000_000L)} Million"
        remaining %= 1_000_000L
    }
    if (remaining >= 1_000L) {
        parts += "${below1000(remaining / 1_000L)} Thousand"
        remaining %= 1_000L
    }
    if (remaining > 0L) {
        parts += below1000(remaining)
    }
    return parts.joinToString(" ")
}

private fun String.dashboardStatusLabel(): String =
    when (this) {
        "draft" -> "Captured"
        "confirmed" -> "Confirmed"
        "part_paid" -> "Part paid"
        "paid" -> "Paid"
        "completed" -> "Completed"
        "cancelled" -> "Cancelled"
        else -> replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

private fun OrmaOrder.dashboardNextStatuses(): List<String> =
    when (status.trim().lowercase()) {
        "draft" -> listOf("confirmed", "cancelled")
        "confirmed" -> listOf("part_paid", "paid", "completed")
        "part_paid" -> listOf("paid", "completed")
        "paid" -> listOf("completed")
        "completed",
        "cancelled" -> emptyList()
        else -> listOf("confirmed", "cancelled")
    }

private fun String.dashboardOrderStatusTone(): OrmaStatusTone =
    when (trim().lowercase()) {
        "draft" -> OrmaStatusTone.Info
        "confirmed" -> OrmaStatusTone.Info
        "part_paid" -> OrmaStatusTone.Warning
        "paid" -> OrmaStatusTone.Success
        "completed" -> OrmaStatusTone.Success
        "cancelled" -> OrmaStatusTone.Danger
        else -> OrmaStatusTone.Neutral
    }

private fun String.dashboardStatusActionCopy(): String =
    when (trim().lowercase()) {
        "confirmed" -> "Accept the request and move it into active work."
        "part_paid" -> "Mark that some payment has been collected."
        "paid" -> "Mark payment collected and keep fulfilment open."
        "completed" -> "Close the order after fulfilment or service completion."
        "cancelled" -> "Cancel only before the work is accepted."
        else -> "Move this order to ${dashboardStatusLabel().lowercase()}."
    }

private fun String.dashboardTerminalStatusCopy(): String =
    when (trim().lowercase()) {
        "paid" -> "Paid orders can still be completed."
        "completed" -> "Completed orders are closed."
        "cancelled" -> "Cancelled orders are closed."
        else -> "No further status action."
    }

private fun dashboardMoney(amount: String, currency: String): String =
    "${currency.ifBlank { "INR" }} ${amount.ifBlank { "0.00" }}"

private fun Double.toDashboardMoneyInput(): String {
    val cents = kotlin.math.round(this * 100.0).toLong()
    val whole = cents / 100
    val fraction = kotlin.math.abs(cents % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

private fun OrmaProduct.matchesOrderSearch(query: String): Boolean {
    val tokens = dashboardSearchTokens(query)
    return listOf(
        name,
        itemType.sellableItemTypeLabel(),
        sku.orEmpty(),
        barcode.orEmpty(),
        supplierName.orEmpty(),
        description.orEmpty(),
        sellingPrice,
    ).joinToString(" ").containsAllDashboardTokens(tokens)
}

private fun OrmaProduct.orderPickerMeta(): String =
    when (itemType.trim().lowercase()) {
        "service" -> durationMinutes?.let { "$it min service" } ?: "Service"
        "appointment" -> durationMinutes?.let { "$it min appointment" } ?: "Appointment"
        else -> listOfNotNull(
            if (trackStock) "${stockQuantity.ifBlank { "0" }} ${unit.ifBlank { "unit" }} in stock" else "Stock not tracked",
            sku?.takeIf { it.isNotBlank() }?.let { "SKU $it" },
            barcode?.takeIf { it.isNotBlank() }?.let { "Barcode $it" },
        ).joinToString(" / ").ifBlank { "Product" }
    }

private fun OrmaProduct.toOrderItemDraft(): OrmaOrderItemDraft =
    OrmaOrderItemDraft(
        productId = id,
        description = name,
        quantity = "1",
        unitPrice = sellingPrice.ifBlank { "0" },
        taxRate = taxRate.ifBlank { "0" },
    )

private fun OrmaOrder.toOrderDraft(): OrmaOrderDraft =
    OrmaOrderDraft(
        customerId = customerId.orEmpty(),
        customerName = customerName.orEmpty(),
        customerPhoneNumber = customerPhoneNumber.orEmpty(),
        customerEmail = customerEmail.orEmpty(),
        customerTaxNumber = customerTaxNumber.orEmpty(),
        customerAddressLine = customerAddressLine.orEmpty(),
        customerCity = customerCity.orEmpty(),
        customerRegion = customerRegion.orEmpty(),
        customerCountry = customerCountry.orEmpty(),
        customerPostalCode = customerPostalCode.orEmpty(),
        orderType = orderType.ifBlank { "sale" },
        status = status.ifBlank { "confirmed" },
        scheduledAt = scheduledAt.orEmpty(),
        paidTotal = paidTotal.ifBlank { "0" },
        currency = currency.ifBlank { "INR" },
        notes = notes.orEmpty(),
        fulfillmentType = fulfillmentType.ifBlank { if (orderType == "appointment") "booking" else "standard" },
        paymentMode = paymentMode.ifBlank { "pay_on_spot" },
        items = items.map { it.toOrderItemDraft() },
    )

private fun OrmaOrderItem.toOrderItemDraft(): OrmaOrderItemDraft =
    OrmaOrderItemDraft(
        productId = productId.orEmpty(),
        description = productName ?: description,
        quantity = quantity.ifBlank { "1" },
        unitPrice = unitPrice.ifBlank { "0" },
        taxRate = taxRate.ifBlank { "0" },
    )

private fun OrmaOrderDraft.requiresDeliveryLocation(): Boolean =
    fulfillmentType.trim().lowercase() == "delivery" ||
        customerAddressLine.isNotBlank() ||
        customerCity.isNotBlank() ||
        customerRegion.isNotBlank() ||
        customerCountry.isNotBlank() ||
        customerPostalCode.isNotBlank()

private fun OrmaOrder.balanceDueValue(): Double =
    (total.toDoubleOrNull().orZero() - paidTotal.toDoubleOrNull().orZero()).coerceAtLeast(0.0)

private fun OrmaOrder.balanceDueText(): String =
    dashboardMoney(balanceDueValue().toDashboardMoneyInput(), currency)

private fun OrmaOrder.isPartPaidRecord(): Boolean =
    status == "part_paid" ||
        (paidTotal.toDoubleOrNull().orZero() > 0.0 && balanceDueValue() > 0.0)

private fun partPaidAmountError(amount: String, order: OrmaOrder): String? {
    val trimmed = amount.trim()
    if (trimmed.isBlank()) return "Enter the amount collected."
    val value = trimmed.toDoubleOrNull() ?: return "Enter a valid amount."
    val total = order.total.toDoubleOrNull().orZero()
    return when {
        total <= 0.0 -> "Order total must be above zero before recording payment."
        value <= 0.0 -> "Enter an amount above zero."
        value >= total -> "For full collection, choose Paid instead of Part paid."
        else -> null
    }
}

private fun OrmaOrder.deliveryLocationText(): String =
    listOf(
        customerAddressLine,
        customerCity,
        customerRegion,
        customerCountry,
        customerPostalCode,
    )
        .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
        .joinToString(", ")
        .ifBlank {
            if (fulfillmentType.trim().lowercase() == "delivery") {
                "Delivery address not added"
            } else {
                "Not required"
            }
        }

private fun OrmaOrder.deliveryLocationMissing(): Boolean =
    fulfillmentType.trim().lowercase() == "delivery" &&
        listOf(customerAddressLine, customerCity, customerRegion, customerCountry).all { it.isNullOrBlank() }

private fun OrmaCustomer.hasCustomerContact(): Boolean =
    !phoneNumber.isNullOrBlank() || !email.isNullOrBlank()

private fun OrmaCustomer.hasCustomerLocation(): Boolean =
    !addressLine.isNullOrBlank() ||
        !city.isNullOrBlank() ||
        !region.isNullOrBlank() ||
        !country.isNullOrBlank() ||
        !postalCode.isNullOrBlank()

private fun OnboardingUiState.defaultInvoiceTaxRate(): String {
    dashboard.products
        .asSequence()
        .map { it.taxRate.trim() }
        .firstOrNull { rate -> rate.toDoubleOrNull()?.let { it > 0.0 } == true }
        ?.let { return it }
    return if (draft.isTaxRegistered) "5" else "0"
}

private fun Double?.orZero(): Double = this ?: 0.0

private data class DashboardDatePresetOption(
    val key: String,
    val label: String,
    val dateFrom: String,
    val dateTo: String,
)

private data class DashboardIsoDateParts(
    val year: Int,
    val month: Int,
    val day: Int,
)

private fun dashboardDatePresetOptions(includeUpcoming: Boolean = false): List<DashboardDatePresetOption> {
    val today = ormaCurrentIsoDate().take(10)
    val todayParts = today.dashboardIsoDatePartsOrNull()
    val yesterday = todayParts?.plusDays(-1)?.toIsoDate().orEmpty()
    val weekStart = todayParts?.weekStartMonday()?.toIsoDate().orEmpty()
    val monthStart = todayParts?.copy(day = 1)?.toIsoDate().orEmpty()
    return buildList {
        add(DashboardDatePresetOption("all", "All dates", "", ""))
        add(DashboardDatePresetOption("today", "Today", today, today))
        if (yesterday.isNotBlank()) add(DashboardDatePresetOption("yesterday", "Yesterday", yesterday, yesterday))
        if (weekStart.isNotBlank()) add(DashboardDatePresetOption("week", "This week", weekStart, today))
        if (monthStart.isNotBlank()) add(DashboardDatePresetOption("month", "This month", monthStart, today))
        if (includeUpcoming) add(DashboardDatePresetOption("upcoming", "Upcoming", today, ""))
    }
}

private fun dashboardActiveDatePresetKey(
    filters: OrmaDashboardFilters,
    options: List<DashboardDatePresetOption>,
): String {
    val normalizedPreset = filters.datePreset.trim().lowercase().replace("-", "_")
    val supportedKeys = options.map { it.key }.toSet()
    if (normalizedPreset in supportedKeys) return normalizedPreset
    val from = filters.dateFrom.trim().take(10)
    val to = filters.dateTo.trim().take(10)
    if (from.isBlank() && to.isBlank()) return "all"
    return options.firstOrNull { it.dateFrom == from && it.dateTo == to }?.key ?: "custom"
}

private fun String.dashboardIsoDatePartsOrNull(): DashboardIsoDateParts? {
    if (length < 10) return null
    val year = substring(0, 4).toIntOrNull() ?: return null
    val month = substring(5, 7).toIntOrNull() ?: return null
    val day = substring(8, 10).toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..daysInDashboardMonth(year, month)) return null
    return DashboardIsoDateParts(year, month, day)
}

private fun DashboardIsoDateParts.toIsoDate(): String =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

private fun DashboardIsoDateParts.plusDays(delta: Int): DashboardIsoDateParts =
    dashboardDatePartsFromEpochDays(toEpochDays() + delta)

private fun DashboardIsoDateParts.weekStartMonday(): DashboardIsoDateParts {
    val epochDays = toEpochDays()
    val isoDayOfWeek = ((epochDays + 3).floorMod(7)) + 1
    return dashboardDatePartsFromEpochDays(epochDays - (isoDayOfWeek - 1))
}

private fun DashboardIsoDateParts.toEpochDays(): Int {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = adjustedYear.floorDiv(400)
    val yearOfEra = adjustedYear - era * 400
    val monthPrime = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthPrime + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

private fun dashboardDatePartsFromEpochDays(epochDays: Int): DashboardIsoDateParts {
    val shifted = epochDays + 719468
    val era = shifted.floorDiv(146097)
    val dayOfEra = shifted - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPrime = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
    val month = monthPrime + if (monthPrime < 10) 3 else -9
    return DashboardIsoDateParts(
        year = year + if (month <= 2) 1 else 0,
        month = month,
        day = day,
    )
}

private fun Int.floorDiv(other: Int): Int {
    val quotient = this / other
    val remainder = this % other
    return if (remainder != 0 && ((remainder < 0) != (other < 0))) quotient - 1 else quotient
}

private fun Int.floorMod(other: Int): Int =
    this - floorDiv(other) * other

private fun daysInDashboardMonth(year: Int, month: Int): Int =
    when (month) {
        2 -> if (year.isDashboardLeapYear()) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun Int.isDashboardLeapYear(): Boolean =
    this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)

private fun orderLineTotal(
    item: OrmaOrderItemDraft,
    currency: String,
    taxEnabled: Boolean = true,
): String {
    val quantity = item.quantity.toDoubleOrNull().orZero()
    val unitPrice = item.unitPrice.toDoubleOrNull().orZero()
    val subtotal = (quantity * unitPrice).coerceAtLeast(0.0)
    val taxRate = if (taxEnabled) item.taxRate.toDoubleOrNull().orZero().coerceAtLeast(0.0) else 0.0
    return dashboardMoney((subtotal + (subtotal * taxRate / 100.0)).coerceAtLeast(0.0).toString(), currency)
}

private fun orderCartTotal(
    items: List<OrmaOrderItemDraft>,
    currency: String,
    taxEnabled: Boolean = true,
): String =
    dashboardMoney(
        items.sumOf { item ->
            val subtotal = item.quantity.toDoubleOrNull().orZero() * item.unitPrice.toDoubleOrNull().orZero()
            val taxRate = if (taxEnabled) item.taxRate.toDoubleOrNull().orZero().coerceAtLeast(0.0) else 0.0
            subtotal + (subtotal * taxRate / 100.0)
        }.coerceAtLeast(0.0).toString(),
        currency,
    )

private fun orderCartTotalValue(
    items: List<OrmaOrderItemDraft>,
    taxEnabled: Boolean = true,
): Double =
    items.sumOf { item ->
        val subtotal = item.quantity.toDoubleOrNull().orZero() * item.unitPrice.toDoubleOrNull().orZero()
        val taxRate = if (taxEnabled) item.taxRate.toDoubleOrNull().orZero().coerceAtLeast(0.0) else 0.0
        subtotal + (subtotal * taxRate / 100.0)
    }.coerceAtLeast(0.0)

private fun orderQuantityText(quantity: Double): String {
    val positiveQuantity = if (quantity < 0.0) 0.0 else quantity
    val wholeQuantity = positiveQuantity.toLong()
    return if (positiveQuantity == wholeQuantity.toDouble()) {
        wholeQuantity.toString()
    } else {
        positiveQuantity.toString().trimEnd('0').trimEnd('.')
    }
}

private fun OnboardingUiState.hasActiveDashboardFilter(): Boolean =
    dashboard.filters.query.isNotBlank() ||
        dashboard.filters.orderStatus != "all" ||
        selectedDashboardItemTypeFilter() != defaultDashboardItemTypeFilter() ||
        selectedDashboardOrderTypeFilter() != defaultDashboardOrderTypeFilter() ||
        dashboard.filters.datePreset.isNotBlank() && dashboard.filters.datePreset != "all" ||
        dashboard.filters.dateFrom.isNotBlank() ||
        dashboard.filters.dateTo.isNotBlank() ||
        dashboard.filters.lowStockOnly ||
        dashboard.filters.supplierId.isNotBlank() ||
        dashboard.filters.barcode.isNotBlank() ||
        dashboard.filters.scheduledOnly

private fun DashboardSection.supportsDashboardDateFilter(): Boolean =
    this in setOf(
        DashboardSection.Dashboard,
        DashboardSection.OrdersBookings,
        DashboardSection.Invoices,
        DashboardSection.Customers,
        DashboardSection.Products,
        DashboardSection.Marketing,
    )

private fun DashboardSection.shouldShowDashboardDateFilter(filters: OrmaDashboardFilters): Boolean =
    supportsDashboardDateFilter() || filters.dateFrom.isNotBlank() || filters.dateTo.isNotBlank()

private fun dashboardSearchTokens(query: String): List<String> =
    query
        .trim()
        .lowercase()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

private fun String?.containsAllDashboardTokens(tokens: List<String>): Boolean {
    if (tokens.isEmpty()) return true
    val haystack = this.orEmpty().lowercase()
    return tokens.all { haystack.contains(it) }
}

private fun String?.matchesDashboardDateRange(filters: OrmaDashboardFilters): Boolean {
    val from = filters.dateFrom.trim().take(10)
    val to = filters.dateTo.trim().take(10)
    if (from.isBlank() && to.isBlank()) return true
    val dateKey = this?.trim()?.take(10)?.takeIf { it.length == 10 } ?: return false
    return (from.isBlank() || dateKey >= from) && (to.isBlank() || dateKey <= to)
}

private fun OrmaOrder.dashboardFilterDate(): String? =
    scheduledAt?.takeIf { it.isNotBlank() } ?: createdAt.takeIf { it.isNotBlank() }

private fun filteredDashboardOrders(
    state: OnboardingUiState,
    ignoreStatus: Boolean = false,
): List<OrmaOrder> {
    val filters = state.dashboard.filters
    val allowedOrderTypes = state.allowedDashboardOrderTypes()
    val selectedOrderType = state.selectedDashboardOrderTypeFilter()
    val tokens = dashboardSearchTokens(filters.query)
    return state.dashboard.orders.filter { order ->
        val statusMatches = ignoreStatus || filters.orderStatus == "all" || order.status == filters.orderStatus
        val modeMatches = order.orderType in allowedOrderTypes
        val orderTypeMatches = selectedOrderType == "all" || order.orderType == selectedOrderType
        val dateMatches = order.dashboardFilterDate().matchesDashboardDateRange(filters)
        val textMatches = listOf(
            order.id,
            order.orderNumber,
            order.orderType.orderTypeLabel(),
            order.customerName.orEmpty(),
            order.customerPhoneNumber.orEmpty(),
            order.customerEmail.orEmpty(),
            order.fulfillmentType.fulfillmentModeLabel(),
            order.paymentMode.paymentModeLabel(),
            order.source.dashboardTitleCase(),
            "${order.itemCount.coerceAtLeast(order.items.size)} items",
            order.notes.orEmpty(),
            order.total,
            order.paidTotal,
            order.balanceDueText(),
            order.scheduledAt.orEmpty(),
            order.createdAt,
            order.status.dashboardStatusLabel(),
        ).joinToString(" ").containsAllDashboardTokens(tokens)
        modeMatches && statusMatches && orderTypeMatches && dateMatches && textMatches
    }
}

private fun filteredDashboardInvoiceOrders(state: OnboardingUiState): List<OrmaOrder> {
    val filters = state.dashboard.filters
    val tokens = dashboardSearchTokens(filters.query)
    val statusFilter = state.selectedDashboardInvoiceStatusFilter()
    return state.dashboard.orders
        .filter { it.status.trim().lowercase() != "cancelled" }
        .filter { order ->
            val textMatches = listOf(
                invoiceNumberFor(state, order),
                order.orderNumber,
                order.customerName.orEmpty(),
                order.customerPhoneNumber.orEmpty(),
                order.customerEmail.orEmpty(),
                order.customerTaxNumber.orEmpty(),
                order.orderType.orderTypeLabel(),
                order.invoiceStatusLabel(),
                order.status.dashboardStatusLabel(),
                order.total,
                order.taxTotal,
                order.scheduledAt.orEmpty(),
                order.createdAt,
                invoiceIssueDateLabel(order),
            ).joinToString(" ").containsAllDashboardTokens(tokens)
            textMatches &&
                order.matchesInvoiceStatusFilter(statusFilter) &&
                order.dashboardFilterDate().matchesDashboardDateRange(filters)
        }
}

private fun OrmaOrder.matchesInvoiceStatusFilter(filter: String): Boolean {
    val statusKey = status.trim().lowercase()
    return when (filter.trim().lowercase()) {
        "all" -> true
        "paid" -> statusKey == "paid"
        "completed" -> statusKey == "completed"
        "part_paid" -> statusKey == "part_paid"
        "draft" -> statusKey == "draft"
        "confirmed" -> statusKey == "confirmed"
        else -> true
    }
}

private fun filteredDashboardCustomers(state: OnboardingUiState): List<OrmaCustomer> {
    val filters = state.dashboard.filters
    val tokens = dashboardSearchTokens(filters.query)
    return state.dashboard.customers.filter { customer ->
        val dateMatches = customer.createdAt.matchesDashboardDateRange(filters)
        val textMatches = listOf(
            customer.name,
            customer.phoneNumber.orEmpty(),
            customer.email.orEmpty(),
            customer.taxNumber.orEmpty(),
            customer.addressLine.orEmpty(),
            customer.city.orEmpty(),
            customer.region.orEmpty(),
            customer.country.orEmpty(),
            customer.postalCode.orEmpty(),
            customer.notes.orEmpty(),
            customer.status,
            customer.createdAt,
        ).joinToString(" ").containsAllDashboardTokens(tokens)
        dateMatches && textMatches
    }
}

private fun filteredDashboardProducts(state: OnboardingUiState): List<OrmaProduct> {
    val filters = state.dashboard.filters
    val allowedItemTypes = state.allowedDashboardItemTypes()
    val selectedItemType = state.selectedDashboardItemTypeFilter()
    val tokens = dashboardSearchTokens(filters.query)
    return state.dashboard.products.filter { product ->
        val stockMatches = !filters.lowStockOnly || product.lowStock
        val modeMatches = product.itemType in allowedItemTypes
        val itemTypeMatches = selectedItemType == "all" || selectedItemType == product.itemType
        val dateMatches = product.createdAt.matchesDashboardDateRange(filters)
        val textMatches = listOf(
            product.name,
            product.itemType.sellableItemTypeLabel(),
            product.categoryName.orEmpty(),
            product.sku.orEmpty(),
            product.barcode.orEmpty(),
            product.supplierName.orEmpty(),
            product.description.orEmpty(),
            product.unit,
            product.expiryDate.orEmpty(),
            product.status,
            product.createdAt,
        ).joinToString(" ").containsAllDashboardTokens(tokens)
        modeMatches && stockMatches && itemTypeMatches && dateMatches && textMatches
    }
}

private fun productImportCsvTemplate(): String =
    listOf(
        "name",
        "itemType",
        "sku",
        "barcode",
        "description",
        "unit",
        "sellingPrice",
        "costPrice",
        "currency",
        "taxRate",
        "pricesIncludeTax",
        "stockQuantity",
        "reorderLevel",
        "trackStock",
        "durationMinutes",
        "bookingRequired",
        "expiryDate",
        "supplierName",
    ).joinToString(",") { it.csvCell() } + "\n"

private fun countCsvDataRows(csv: String): Int =
    csv.lineSequence()
        .drop(1)
        .count { line -> line.split(',').any { it.trim().isNotBlank() } }

private fun String.csvCell(): String =
    if (any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else {
        this
    }

private fun String.moneyInput(): String =
    filter { it.isDigit() || it == '.' }
        .let { value ->
            val firstDot = value.indexOf('.')
            if (firstDot < 0) value else value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
        }
        .take(12)

private fun String.signedMoneyInput(): String {
    val negative = trim().startsWith("-")
    val cleaned = moneyInput()
    return if (negative && cleaned.isNotBlank()) "-$cleaned" else cleaned
}

private fun requiredNonNegativeDecimalError(
    value: String,
    blankMessage: String,
): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return blankMessage
    val number = trimmed.toDoubleOrNull() ?: return "Enter a valid number."
    return if (number < 0.0) "Enter zero or higher." else null
}

private fun optionalNonNegativeDecimalError(
    value: String,
    invalidMessage: String,
): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val number = trimmed.toDoubleOrNull() ?: return "Enter a valid number."
    return if (number < 0.0) invalidMessage else null
}

private fun percentageDecimalError(value: String): String? {
    val trimmed = value.trim().ifBlank { "0" }
    val number = trimmed.toDoubleOrNull() ?: return "Enter a valid tax percentage."
    return when {
        number < 0.0 -> "Tax rate must be zero or higher."
        number > 100.0 -> "Tax rate cannot be above 100%."
        else -> null
    }
}

private fun signedDecimalAdjustmentError(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return "Enter the adjustment quantity."
    val number = trimmed.toDoubleOrNull() ?: return "Enter a valid adjustment quantity."
    return if (number == 0.0) "Adjustment cannot be zero." else null
}

private fun orderItemValidationError(item: OrmaOrderItemDraft): String? {
    val hasItem = item.productId.isNotBlank() || item.description.trim().isNotBlank()
    if (!hasItem) return "Choose a product or enter an item description."
    val quantity = item.quantity.trim().toDoubleOrNull()
    if (quantity == null || quantity <= 0.0) return "Quantity must be greater than zero."
    val unitPrice = item.unitPrice.trim().ifBlank { "0" }.toDoubleOrNull()
    if (unitPrice == null || unitPrice < 0.0) return "Price must be zero or higher."
    return percentageDecimalError(item.taxRate)
}

@Composable
private fun DashboardModuleWorkspace(
    wide: Boolean,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
) {
    OrmaDashboardResponsiveWorkspace(
        wide = wide,
        primary = primary,
        secondary = secondary,
    )
}

@Composable
private fun DashboardModuleActionCard(
    icon: DashboardNavIconKind,
    title: String,
    body: String,
    primaryText: String,
    secondaryText: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = OrmaShapes.Capsule,
                color = OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(
                        kind = icon,
                        color = OrmaColors.IconPrimary,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )
            OrmaActionRow(
                primaryText = primaryText,
                onPrimary = {},
                primaryEnabled = false,
            )
            OrmaSecondaryButton(
                text = secondaryText,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
            Text(
                text = "This action will become active when this module is available for the workspace.",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun DashboardModuleChecklistCard(
    title: String,
    items: List<String>,
    tertiaryText: String? = null,
) {
    OrmaDashboardChecklistCard(
        title = title,
        items = items,
        tertiaryText = tertiaryText,
    )
}

@Composable
private fun DashboardChecklistRow(text: String) {
    OrmaDashboardChecklistRow(text = text)
}

@Composable
private fun DashboardEmptyModuleCard(
    icon: DashboardNavIconKind,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    OrmaDashboardEmptyState(
        title = title,
        body = body,
        modifier = modifier,
        icon = {
            OrmaDashboardIconBubble(modifier = Modifier.size(44.dp)) {
                DashboardNavIcon(
                    kind = icon,
                    color = OrmaColors.IconPrimary,
                    modifier = Modifier.size(21.dp),
                )
            }
        },
    )
}

@Composable
private fun DashboardWorkspaceCard(
    state: OnboardingUiState,
    roleLabel: String,
    modifier: Modifier = Modifier,
) {
    val rows = dashboardWorkspaceRows(state, roleLabel)
    OrmaFormCard(modifier = modifier) {
        OrmaBadge(
            text = "WORKSPACE",
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = "Current setup",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        OrmaKeyValueList(rows = rows)
    }
}

private fun dashboardWorkspaceRows(
    state: OnboardingUiState,
    roleLabel: String,
): List<Pair<String, String>> = buildList {
    add("Role" to roleLabel)
    state.workspaceName
        .ifBlank { state.draft.businessName }
        .takeIf { it.isNotBlank() }
        ?.let { add("Workspace" to it) }
    state.draft.legalName.takeIf { it.isNotBlank() }?.let { add("Legal name" to it) }
    listOf(state.draft.city, state.draft.region, state.draft.country)
        .filter { it.isNotBlank() }
        .joinToString(", ")
        .takeIf { it.isNotBlank() }
        ?.let { add("Address" to it) }
    state.draft.currency.takeIf { it.isNotBlank() }?.let { add("Currency" to it) }
    if (state.draft.isTaxRegistered) {
        add(
            "Tax" to state.draft.taxNumber
                .ifBlank { state.draft.taxLabel.ifBlank { "Registered" } },
        )
    }
    when {
        state.workspaceLogoUrl.isNotBlank() -> add("Logo" to "Synced")
        state.workspaceLogoFileName.isNotBlank() || state.draft.logoFileName.isNotBlank() -> add("Logo" to "Uploaded")
    }
    add("Notifications" to if (state.notificationsEnabled) "Enabled" else "Off")
}

private fun OnboardingUiState.currentDashboardTeamMember(): OrmaTeamMember? {
    val authId = authUserId.trim()
    val email = identifier.trim().lowercase().takeIf { it.contains("@") }
    val phoneDigits = identifier.dashboardDigits()
    return dashboard.teamMembers.firstOrNull { member ->
        (authId.isNotBlank() && member.userId == authId) ||
            (email != null && member.email?.trim()?.lowercase() == email) ||
            (
                phoneDigits.isNotBlank() &&
                    member.phoneNumber.dashboardDigits().takeLast(10) == phoneDigits.takeLast(10)
                )
    }
}

private fun String?.dashboardDigits(): String =
    orEmpty().filter { it.isDigit() }

private fun OnboardingUiState.currentTeamContactLabel(member: OrmaTeamMember?): String =
    member?.email?.takeIf { it.isNotBlank() }
        ?: member?.phoneNumber?.takeIf { it.isNotBlank() }
        ?: identifier.trim().takeIf { it.isNotBlank() }
        ?: "Signed-in account"

private fun OrmaTeamMember.dashboardDisplayName(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: phoneNumber?.takeIf { it.isNotBlank() }
        ?: "Team member"

@Composable
private fun DashboardTeamContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    canInviteMembers: Boolean,
    wide: Boolean,
) {
    val teamTitle = if (canInviteMembers) "Team access" else "My workspace access"
    val teamBody = if (canInviteMembers) {
        "Review active users who can access this workspace."
    } else {
        "Review your role, contact, and workspace team visibility."
    }
    if (wide) {
        DashboardTeamWorkspace(
            state = state,
            actions = actions,
            canInviteMembers = canInviteMembers,
        )
    } else {
        DashboardListScaffold(
            eyebrow = "TEAM",
            title = teamTitle,
            body = teamBody,
            primaryText = "Refresh",
            onPrimary = actions.onDashboardRefresh,
            loading = state.dashboard.loading,
            wide = false,
        ) {
            DashboardTeamMembersCard(state = state, canInviteMembers = canInviteMembers)
            DashboardTeamAccessPanel(state = state, canInviteMembers = canInviteMembers)
            DashboardTeamGuideCard(canInviteMembers = canInviteMembers)
        }
    }
}

@Composable
private fun DashboardTeamWorkspace(
    state: OnboardingUiState,
    actions: OnboardingActions,
    canInviteMembers: Boolean,
) {
    OrmaDashboardResponsiveWorkspace(
        wide = true,
        primaryWeight = 1.58f,
        secondaryMinWidth = 330.dp,
        secondaryMaxWidth = 400.dp,
        stackBelowWidth = 1080.dp,
        primary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardTeamKpiStrip(state = state, canInviteMembers = canInviteMembers)
                DashboardTeamMembersSurface(
                    state = state,
                    actions = actions,
                    canInviteMembers = canInviteMembers,
                )
            }
        },
        secondary = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DashboardTeamAccessPanel(
                    state = state,
                    canInviteMembers = canInviteMembers,
                )
                DashboardTeamGuideCard(canInviteMembers = canInviteMembers)
            }
        },
    )
}

@Composable
private fun DashboardTeamKpiStrip(
    state: OnboardingUiState,
    canInviteMembers: Boolean,
) {
    val members = state.dashboard.teamMembers
    val invites = state.dashboard.teamInvites
    val active = members.count { it.status.dashboardTeamStatusLabel() == "Active" }
    val owners = members.count { it.role == "business_owner" }
    val reachable = members.count { !it.email.isNullOrBlank() || !it.phoneNumber.isNullOrBlank() }
    val currentMember = state.currentDashboardTeamMember()
    val currentRole = currentMember?.role
        ?.let { teamRoleLabel(it) }
        ?: if (state.accessPath == AccessPath.TeamMember) "Team member" else "Business owner"
    val currentStatus = currentMember?.status?.dashboardTeamStatusLabel()
        ?: if (state.accessPath == AccessPath.TeamMember) "Active" else "Owner"
    DashboardFocusMetricStrip(
        metrics = if (canInviteMembers) {
            listOf(
                DashboardFocusMetric(
                    label = "Members",
                    value = members.size.toString(),
                    detail = "$active active",
                    tone = OrmaStatusTone.Info,
                ),
                DashboardFocusMetric(
                    label = "Invites",
                    value = invites.size.toString(),
                    detail = "pending",
                    tone = OrmaStatusTone.Success,
                ),
                DashboardFocusMetric(
                    label = "Owners",
                    value = owners.toString(),
                    detail = "admin access",
                    tone = if (owners > 0) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                ),
                DashboardFocusMetric(
                    label = "Reachable",
                    value = reachable.toString(),
                    detail = "phone/email",
                    tone = OrmaStatusTone.Info,
                ),
            )
        } else {
            listOf(
                DashboardFocusMetric(
                    label = "Your role",
                    value = currentRole,
                    detail = "workspace access",
                    tone = OrmaStatusTone.Info,
                ),
                DashboardFocusMetric(
                    label = "Status",
                    value = currentStatus,
                    detail = "signed-in user",
                    tone = if (currentStatus == "Active") OrmaStatusTone.Success else OrmaStatusTone.Warning,
                ),
                DashboardFocusMetric(
                    label = "Workspace",
                    value = if (state.workspaceName.isNotBlank()) "Linked" else "Active",
                    detail = state.workspaceName.ifBlank { "current business" },
                    tone = OrmaStatusTone.Success,
                ),
                DashboardFocusMetric(
                    label = "Team",
                    value = members.size.toString(),
                    detail = "visible members",
                    tone = OrmaStatusTone.Info,
                ),
            )
        },
    )
}

@Composable
private fun DashboardTeamMembersSurface(
    state: OnboardingUiState,
    actions: OnboardingActions,
    canInviteMembers: Boolean,
) {
    val members = state.dashboard.teamMembers
    val invites = state.dashboard.teamInvites
    val currentMemberId = state.currentDashboardTeamMember()?.id
    var showInviteSheet by rememberSaveable { mutableStateOf(false) }
    var detailMemberId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailInviteId by rememberSaveable { mutableStateOf<String?>(null) }
    val detailMember = members.firstOrNull { it.id == detailMemberId }
    val detailInvite = invites.firstOrNull { it.id == detailInviteId }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardRecordsSurfaceHeader(
                title = if (canInviteMembers) "Team members" else "Workspace team",
                body = if (canInviteMembers) {
                    "Invite staff, review active access, and remove users who should no longer enter the workspace."
                } else {
                    "People currently linked to the same workspace as you."
                },
                badgeText = if (canInviteMembers) {
                    "${members.size} ACTIVE / ${invites.size} INVITED"
                } else {
                    "${members.size} ACTIVE"
                },
                badgeTone = OrmaStatusTone.Success,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canInviteMembers) {
                    DashboardWideActionButton(
                        text = "Add invite",
                        onClick = { showInviteSheet = true },
                        enabled = !state.dashboard.actionLoading,
                        primary = true,
                    )
                }
                DashboardWideActionButton(
                    text = if (state.dashboard.loading) "Syncing" else "Refresh",
                    onClick = actions.onDashboardRefresh,
                    enabled = !state.dashboard.loading,
                )
            }
            if (canInviteMembers) {
                DashboardTeamInvitesQueue(
                    invites = invites,
                    actionLoading = state.dashboard.actionLoading,
                    onOpenInvite = { detailInviteId = it.id },
                    onRevokeInvite = { actions.onRevokeTeamInvite(it.id) },
                )
            }
            if (members.isEmpty()) {
                DashboardInlineEmptyRecords(
                    icon = DashboardNavIconKind.Invite,
                    title = when {
                        state.dashboard.loading -> "Loading team"
                        !state.dashboard.errorMessage.isNullOrBlank() -> "Could not load team"
                        else -> "No active team loaded"
                    },
                    body = when {
                        state.dashboard.loading -> "ORMA is checking active workspace members."
                        !state.dashboard.errorMessage.isNullOrBlank() -> state.dashboard.errorMessage.orEmpty()
                        else -> "Refresh the dashboard to load active members for this workspace."
                    },
                )
            } else {
                var sortKey by rememberSaveable { mutableStateOf(DashboardTeamSortMember) }
                var sortAscending by rememberSaveable { mutableStateOf(true) }
                val sortedMembers = remember(members, sortKey, sortAscending) {
                    sortedDashboardTeamMembers(
                        members = members,
                        sortKey = sortKey,
                        ascending = sortAscending,
                    )
                }
                DashboardTeamMembersTable(
                    members = sortedMembers,
                    sortKey = sortKey,
                    sortAscending = sortAscending,
                    canInviteMembers = canInviteMembers,
                    currentMemberId = currentMemberId,
                    actionLoading = state.dashboard.actionLoading,
                    onOpenMember = { detailMemberId = it.id },
                    onRemoveMember = { actions.onRemoveTeamMember(it.id) },
                    onSortChange = { nextSortKey ->
                        if (sortKey == nextSortKey) {
                            sortAscending = !sortAscending
                        } else {
                            sortKey = nextSortKey
                            sortAscending = nextSortKey !in DashboardTeamDescendingFirstSorts
                        }
                    }
                )
            }
        }
    }
    if (showInviteSheet) {
        DashboardTeamInviteSheet(
            onDismiss = { showInviteSheet = false },
            onSubmit = { draft ->
                actions.onCreateTeamInvite(draft)
                showInviteSheet = false
            },
        )
    }
    if (detailMember != null) {
        DashboardTeamMemberDetailsSheet(
            member = detailMember,
            canRemove = canInviteMembers &&
                detailMember.id != currentMemberId &&
                detailMember.role != "business_owner",
            actionLoading = state.dashboard.actionLoading,
            onDismiss = { detailMemberId = null },
            onRemove = {
                actions.onRemoveTeamMember(detailMember.id)
                detailMemberId = null
            },
        )
    }
    if (detailInvite != null) {
        DashboardTeamInviteDetailsSheet(
            invite = detailInvite,
            actionLoading = state.dashboard.actionLoading,
            onDismiss = { detailInviteId = null },
            onRevoke = {
                actions.onRevokeTeamInvite(detailInvite.id)
                detailInviteId = null
            },
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DashboardTeamMembersTable(
    members: List<OrmaTeamMember>,
    sortKey: String,
    sortAscending: Boolean,
    canInviteMembers: Boolean,
    currentMemberId: String?,
    actionLoading: Boolean,
    onOpenMember: (OrmaTeamMember) -> Unit,
    onRemoveMember: (OrmaTeamMember) -> Unit,
    onSortChange: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp),
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrmaColors.CardBackground,
                contentColor = OrmaColors.TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DashboardSaleHeaderCell(
                            text = "Member",
                            sortKey = DashboardTeamSortMember,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.05f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Contact",
                            sortKey = DashboardTeamSortContact,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(1.15f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Role",
                            sortKey = DashboardTeamSortRole,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.85f),
                        )
                        DashboardSaleHeaderCell(
                            text = "Joined",
                            sortKey = DashboardTeamSortJoined,
                            activeSortKey = sortKey,
                            sortAscending = sortAscending,
                            onSortChange = onSortChange,
                            modifier = Modifier.weight(0.85f),
                        )
                        if (canInviteMembers) {
                            Text(
                                text = "ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrmaColors.TextSecondary,
                                letterSpacing = 1.8.sp,
                                modifier = Modifier.weight(0.95f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        thickness = 0.8.dp,
                        color = OrmaColors.CellBackground.copy(alpha = 0.72f),
                    )
                }
            }
        }
        itemsIndexed(
            items = members,
            key = { _, member -> member.id },
        ) { index, member ->
            DashboardWideTeamMemberRow(
                member = member,
                zebra = index % 2 == 1,
                canInviteMembers = canInviteMembers,
                currentMemberId = currentMemberId,
                actionLoading = actionLoading,
                onOpenMember = onOpenMember,
                onRemoveMember = onRemoveMember,
            )
        }
    }
}

@Composable
private fun DashboardWideTeamMemberRow(
    member: OrmaTeamMember,
    zebra: Boolean = false,
    canInviteMembers: Boolean = false,
    currentMemberId: String? = null,
    actionLoading: Boolean = false,
    onOpenMember: (OrmaTeamMember) -> Unit = {},
    onRemoveMember: (OrmaTeamMember) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackground = when {
        hovered -> OrmaColors.ScreenBackground.copy(alpha = 0.44f)
        zebra -> OrmaColors.ScreenBackground.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OrmaShapes.SmallCard)
                .background(rowBackground)
                .hoverable(interactionSource)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardWideCell(
                primary = member.displayName?.takeIf { it.isNotBlank() }
                    ?: member.email
                    ?: member.phoneNumber
                    ?: "Team member",
                secondary = member.status.dashboardTeamStatusLabel(),
                modifier = Modifier.weight(1.05f),
            )
            DashboardWideCell(
                primary = listOfNotNull(member.email, member.phoneNumber).joinToString(" / ")
                    .ifBlank { "No contact saved" },
                modifier = Modifier.weight(1.15f),
            )
            Box(modifier = Modifier.weight(0.85f), contentAlignment = Alignment.CenterStart) {
                OrmaBadge(
                    text = teamRoleLabel(member.role).uppercase(),
                    tone = if (member.role == "business_owner") OrmaStatusTone.Success else OrmaStatusTone.Info,
                )
            }
            DashboardWideCell(
                primary = member.joinedAt.dashboardDateLabel(),
                modifier = Modifier.weight(0.85f),
            )
            if (canInviteMembers) {
                val canRemove = member.id != currentMemberId && member.role != "business_owner"
                Row(
                    modifier = Modifier.weight(0.95f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DashboardTableActionButton(
                        text = "Details",
                        onClick = { onOpenMember(member) },
                        enabled = true,
                        iconKind = OrmaFlatIconKind.View,
                    )
                    DashboardTableActionButton(
                        text = "Remove",
                        onClick = { onRemoveMember(member) },
                        enabled = canRemove && !actionLoading,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp),
            thickness = 0.8.dp,
            color = if (hovered) {
                OrmaColors.CellBackground.copy(alpha = 0.86f)
            } else {
                OrmaColors.CellBackground.copy(alpha = 0.62f)
            },
        )
    }
}

@Composable
private fun DashboardTeamAccessPanel(
    state: OnboardingUiState,
    canInviteMembers: Boolean,
) {
    val currentMember = state.currentDashboardTeamMember()
    val currentRole = currentMember?.role
        ?.let { teamRoleLabel(it) }
        ?: if (state.accessPath == AccessPath.TeamMember) "Team member" else "Business owner"
    val currentStatus = currentMember?.status?.dashboardTeamStatusLabel()
        ?: if (state.accessPath == AccessPath.TeamMember) "Active" else "Owner"
    val title = if (canInviteMembers) "Access policy" else "My workspace access"
    val body = if (canInviteMembers) {
        "Use this page as the workspace access audit before changing account or business setup."
    } else {
        "This is the role and contact ORMA resolved for your signed-in account."
    }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (canInviteMembers) "OWNER" else "TEAM MEMBER",
                tone = if (canInviteMembers) OrmaStatusTone.Success else OrmaStatusTone.Info,
            )
        }
        if (canInviteMembers) {
            DashboardChecklistRow(text = "${state.dashboard.teamMembers.size} members are currently linked to this workspace.")
            DashboardChecklistRow(text = "Owner access can manage workspace-level team changes.")
            DashboardChecklistRow(text = "Refresh after backend role or invite changes.")
        } else {
            OrmaKeyValueList(
                rows = listOf(
                    "Name" to (currentMember?.dashboardDisplayName() ?: state.teamProfileName.ifBlank { "Team member" }),
                    "Signed in as" to state.currentTeamContactLabel(currentMember),
                    "Role" to currentRole,
                    "Status" to currentStatus,
                    "Joined" to (currentMember?.joinedAt?.dashboardDateLabel() ?: "Loaded from session"),
                ),
            )
            DashboardChecklistRow(text = "Owners or admins manage role changes for this workspace.")
            DashboardChecklistRow(text = "Use Account to sign out or switch to a different workspace.")
        }
    }
}

@Composable
private fun DashboardTeamMembersCard(
    state: OnboardingUiState,
    canInviteMembers: Boolean,
) {
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                OrmaBadge(text = "ACTIVE", tone = OrmaStatusTone.Success)
                Text(
                    text = if (canInviteMembers) "Active team members" else "Workspace team",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (canInviteMembers) {
                        "People who can currently access this workspace."
                    } else {
                        "People linked to this workspace. Owners manage changes."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaBadge(
                text = state.dashboard.teamMembers.size.toString(),
                tone = OrmaStatusTone.Info,
            )
        }
        if (state.dashboard.teamMembers.isEmpty()) {
            DashboardEmptyModuleCard(
                icon = DashboardNavIconKind.Invite,
                title = if (state.dashboard.loading) "Loading team" else "No active team loaded",
                body = if (state.dashboard.loading) {
                    "ORMA is checking active workspace members."
                } else {
                    "Refresh the dashboard to load active members for this workspace."
                },
            )
        } else {
            state.dashboard.teamMembers.forEach { member ->
                DashboardTeamMemberRow(member = member)
            }
        }
    }
}

@Composable
private fun DashboardTeamMemberRow(
    member: OrmaTeamMember,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = member.displayName?.takeIf { it.isNotBlank() }
                            ?: member.email
                            ?: member.phoneNumber
                            ?: "Team member",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(member.email, member.phoneNumber).joinToString(" / ")
                            .ifBlank { "No contact saved" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = teamRoleLabel(member.role).uppercase(),
                    tone = if (member.role == "business_owner") OrmaStatusTone.Success else OrmaStatusTone.Info,
                )
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Status" to member.status.dashboardTeamStatusLabel(),
                    "Joined" to member.joinedAt.dashboardDateLabel(),
                ),
            )
        }
    }
}

private fun String.dashboardTeamStatusLabel(): String = when (trim().lowercase()) {
    "active" -> "Active"
    "disabled" -> "Disabled"
    "suspended" -> "Suspended"
    else -> trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.ifBlank { "Active" }
}

private fun String.dashboardDateLabel(): String =
    trim()
        .replace("T", " ")
        .substringBefore(".")
        .take(16)
        .ifBlank { "Not available" }

private fun String?.productExpiryLabel(): String? =
    this
        ?.take(10)
        ?.takeIf { it.length == 10 }
        ?.let { "Expires ${it.dashboardDateLabel().take(10)}" }

@Composable
private fun DashboardTeamGuideCard(
    canInviteMembers: Boolean,
) {
    DashboardModuleChecklistCard(
        title = if (canInviteMembers) "Team access" else "Team member setup",
        items = if (canInviteMembers) {
            listOf(
                "Active members are linked to this workspace by the backend.",
                "Use Account when you need to switch or sign out.",
                "Team access is shown here after the backend links a user to this workspace.",
            )
        } else {
            listOf(
                "Your account is connected to this workspace as a team member.",
                "Owners or admins manage your role, permissions, and workspace access.",
                "Use Account when you need to switch workspace or sign out.",
            )
        },
    )
}

@Composable
private fun DashboardAccountContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    roleLabel: String,
    canInviteMembers: Boolean,
    onOpenTeam: (() -> Unit)?,
    wide: Boolean,
) {
    if (wide) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DashboardAccountProfileCard(state = state, roleLabel = roleLabel)
                DashboardAccountLogoCard(state = state, actions = actions)
                DashboardAccountOrderingLinkCard(state = state)
                DashboardAccountPaymentCard(state = state, actions = actions)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DashboardAccountPrinterCard(state = state, actions = actions)
                DashboardAccountSessionCard(
                    roleLabel = roleLabel,
                    onLogout = actions.onRestart,
                )
                if (isOrmaWebDownloadSurface()) {
                    OrmaDesktopDownloadPanel(wide = true)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DashboardAccountProfileCard(state = state, roleLabel = roleLabel)
            DashboardAccountLogoCard(state = state, actions = actions)
            DashboardAccountOrderingLinkCard(state = state)
            DashboardAccountPaymentCard(state = state, actions = actions)
            DashboardAccountPrinterCard(state = state, actions = actions)
            if (canInviteMembers) {
                DashboardAccountTeamNavigationCard(
                    canInviteMembers = true,
                    onOpenTeam = onOpenTeam,
                )
            }
            DashboardAccountSessionCard(
                roleLabel = roleLabel,
                onLogout = actions.onRestart,
            )
            if (isOrmaWebDownloadSurface()) {
                OrmaDesktopDownloadPanel(wide = false)
            }
        }
    }
}

@Composable
private fun DashboardAccountTeamNavigationCard(
    canInviteMembers: Boolean,
    onOpenTeam: (() -> Unit)?,
) {
    Surface(
        onClick = { onOpenTeam?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        enabled = onOpenTeam != null,
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.Subtle,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = OrmaShapes.Capsule,
                color = OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardNavIcon(
                        kind = DashboardNavIconKind.Invite,
                        color = OrmaColors.IconPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Team",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = if (canInviteMembers) {
                        "View active workspace members."
                    } else {
                        "View team access for this workspace."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            Text(
                text = "Open",
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun DashboardAccountProfileCard(
    state: OnboardingUiState,
    roleLabel: String,
) {
    OrmaFormCard {
        OrmaBadge(
            text = "ACCOUNT",
            tone = OrmaStatusTone.Info,
        )
        Text(
            text = "Workspace account",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        OrmaKeyValueList(
            rows = listOf(
                "Workspace" to state.workspaceName.ifBlank { state.draft.businessName.ifBlank { "ORMA workspace" } },
                "Signed in as" to state.identifier.trim().ifBlank { "Authenticated user" },
                "Role" to roleLabel,
                "Notifications" to if (state.notificationsEnabled) "Enabled" else "Off",
            ),
        )
    }
}

@Composable
private fun DashboardAccountSessionCard(
    roleLabel: String,
    onLogout: () -> Unit,
) {
    DashboardRecordCard {
        OrmaBadge(
            text = "SESSION",
            tone = OrmaStatusTone.Info,
        )
        Text(
            text = "Account access",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = "Sign out only when you want to switch workspace access or use a different account.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        OrmaKeyValueList(
            rows = listOf(
                "Current role" to roleLabel,
                "Session" to "Active",
            ),
        )
        OrmaSecondaryButton(
            text = "Sign out",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DashboardAccountLogoCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    val draft = state.draft
    val logoFileName = state.workspaceLogoFileName
        .ifBlank { draft.logoFileName }
        .ifBlank { state.workspaceLogoUrl }
    val coverFileName = state.workspaceCoverFileName.ifBlank { state.workspaceCoverUrl }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OrmaBadge(
            text = "BRAND MEDIA",
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = "Business logo and cover",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = "Used on your workspace, public catalog, QR ordering, invoices, and customer-facing documents.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        CoverUploadCard(
            fileName = coverFileName,
            remoteUrl = state.workspaceCoverUrl,
            selected = coverFileName.isNotBlank(),
            uploading = state.coverUploadLoading,
            onClick = actions.onCoverUploadRequest,
        )
        LogoUploadCard(
            initials = businessInitial(draft),
            fileName = logoFileName,
            previewBytes = draft.logoPreviewBytes,
            selected = logoFileName.isNotBlank() || draft.logoPreviewBytes.isNotEmpty(),
            uploading = state.logoUploadLoading,
            onClick = actions.onLogoUploadRequest,
        )
    }
}

@Composable
private fun DashboardAccountOrderingLinkCard(
    state: OnboardingUiState,
) {
    val workspaceId = state.workspaceId.trim()
    if (workspaceId.isBlank()) return
    val orderingUrl = currentOrmaPublicCatalogUrl(workspaceId)
    DashboardRecordCard {
        OrmaBadge(
            text = "QR ORDERING",
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = "Customer ordering page",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = "Use this link for QR menus, counter ordering, booking requests, or shared product catalogs.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        OrmaKeyValueList(
            rows = listOf(
                "Workspace" to state.workspaceName.ifBlank { state.draft.businessName.ifBlank { "ORMA workspace" } },
                "Public link" to orderingUrl,
                "Dashboard" to "Submitted requests appear in Orders",
            ),
        )
    }
}

@Composable
private fun DashboardAccountPaymentCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }
    val defaultMethod = state.dashboard.paymentMethods.firstOrNull { it.isDefault }
        ?: state.dashboard.paymentMethods.firstOrNull()
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrmaBadge(
                    text = "UPI",
                    tone = if (defaultMethod != null) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                )
                Text(
                    text = "Customer payments",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Set one default UPI ID for QR catalog payment links. ORMA records the request; payment confirmation stays manual.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaTextButton(text = "Add", onClick = { showPaymentSheet = true })
        }
        if (state.dashboard.paymentMethods.isEmpty()) {
            DashboardChecklistRow(text = "Add a UPI ID before showing UPI payment links on public orders.")
        } else {
            state.dashboard.paymentMethods.forEach { method ->
                OrmaKeyValueList(
                    rows = listOf(
                        "Label" to method.label,
                        "UPI ID" to method.upiId.orEmpty().ifBlank { "Not set" },
                        "Default" to if (method.isDefault) "Yes" else "No",
                    ),
                )
            }
        }
    }
    if (showPaymentSheet) {
        PaymentMethodFormSheet(
            state = state,
            onDismiss = { showPaymentSheet = false },
            onSubmit = { draft ->
                actions.onCreatePaymentMethod(draft)
                showPaymentSheet = false
            },
        )
    }
}

@Composable
private fun PaymentMethodFormSheet(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaWorkspacePaymentMethodDraft) -> Unit,
) {
    var draft by remember {
        mutableStateOf(
            OrmaWorkspacePaymentMethodDraft(
                isDefault = state.dashboard.paymentMethods.none { it.isDefault },
            ),
        )
    }
    DashboardFormSheet(
        title = "UPI payment",
        body = "Add a UPI ID for customer payment links in the ordering page.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(
            value = draft.label,
            onValueChange = { draft = draft.copy(label = it.take(80)) },
            label = "Label",
            placeholder = "Main counter",
        )
        OrmaTextField(
            value = draft.upiId,
            onValueChange = { draft = draft.copy(upiId = it.lowercase().take(120)) },
            label = "UPI ID",
            placeholder = "business@upi",
        )
        OrmaTextField(
            value = draft.payeeName,
            onValueChange = { draft = draft.copy(payeeName = it.take(120)) },
            label = "Payee name",
            placeholder = state.workspaceName.ifBlank { "Business name" },
        )
        OrmaSwitchRow(
            title = "Make default",
            body = "Use this UPI ID for public catalog payment links.",
            checked = draft.isDefault,
            onCheckedChange = { draft = draft.copy(isDefault = it) },
        )
        OrmaActionRow(
            primaryText = "Save UPI",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.label.trim().length >= 2 && draft.upiId.contains("@"),
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun DashboardAccountPrinterCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    var showPrinterSheet by rememberSaveable { mutableStateOf(false) }
    DashboardRecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrmaBadge(
                    text = "PRINTING",
                    tone = OrmaStatusTone.Info,
                )
                Text(
                    text = "Printers",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "Set up receipt, bill, booking, order, and barcode printers for this workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
            OrmaTextButton(
                text = "Add",
                onClick = { showPrinterSheet = true },
            )
        }
        if (state.dashboard.printers.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.Accent.copy(alpha = 0.08f),
                    contentColor = OrmaColors.Accent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DashboardNavIcon(
                            kind = DashboardNavIconKind.Printing,
                            color = OrmaColors.IconPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "No printers configured",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = "Add MTP/USB, Bluetooth, network, system, or ESC/POS printers before printing bills or barcodes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
            }
        } else {
            state.dashboard.printers.forEach { printer ->
                DashboardPrinterRow(printer = printer)
            }
        }
        OrmaSecondaryButton(
            text = "Add printer",
            onClick = { showPrinterSheet = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.dashboard.actionLoading,
        )
    }

    if (showPrinterSheet) {
        PrinterFormSheet(
            onDismiss = { showPrinterSheet = false },
            onSubmit = { draft ->
                actions.onCreatePrinter(draft)
                showPrinterSheet = false
            },
        )
    }
}

@Composable
private fun DashboardPrinterRow(printer: OrmaPrinterProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = printer.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = printer.connectionType.printerConnectionLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
                if (printer.isDefaultReceipt || printer.isDefaultBarcode) {
                    OrmaBadge(
                        text = when {
                            printer.isDefaultReceipt && printer.isDefaultBarcode -> "DEFAULT"
                            printer.isDefaultBarcode -> "BARCODE"
                            else -> "RECEIPT"
                        },
                        tone = OrmaStatusTone.Success,
                    )
                }
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Paper" to "${printer.paperWidthMm}mm",
                    "DPI" to printer.dpi.toString(),
                    "Receipts" to if (printer.supportsReceipts) "Yes" else "No",
                    "Barcodes" to if (printer.supportsBarcodes) "Yes" else "No",
                ),
            )
            printer.address?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PrinterFormSheet(
    onDismiss: () -> Unit,
    onSubmit: (OrmaPrinterDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaPrinterDraft(isDefaultReceipt = true)) }
    DashboardFormSheet(
        title = "Add printer",
        body = "Configure the printer once. ORMA can use it later for bills, orders, bookings, sales, and barcode labels.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(
            value = draft.name,
            onValueChange = { draft = draft.copy(name = it.take(120)) },
            label = "Printer name",
            placeholder = "Counter receipt printer",
        )
        DashboardCompactSegmentedPicker(
            options = DashboardPrinterConnectionTypes,
            selected = draft.connectionType,
            label = { it.printerConnectionLabel() },
            onSelected = { draft = draft.copy(connectionType = it) },
        )
        OrmaTextField(
            value = draft.address,
            onValueChange = { draft = draft.copy(address = it.take(160)) },
            label = "Address or path",
            placeholder = "USB path, IP address, Bluetooth name, or MTP id",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(
                value = draft.paperWidthMm,
                onValueChange = { draft = draft.copy(paperWidthMm = it.filter(Char::isDigit).take(3)) },
                label = "Paper mm",
                modifier = Modifier.weight(1f),
                placeholder = "80",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OrmaTextField(
                value = draft.dpi,
                onValueChange = { draft = draft.copy(dpi = it.filter(Char::isDigit).take(3)) },
                label = "DPI",
                modifier = Modifier.weight(1f),
                placeholder = "203",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        OrmaSwitchRow(
            title = "Print receipts and bills",
            body = "Use this printer for sales, bills, bookings, orders, and receipts.",
            checked = draft.supportsReceipts,
            onCheckedChange = { draft = draft.copy(supportsReceipts = it) },
        )
        OrmaSwitchRow(
            title = "Print barcodes",
            body = "Use this printer for product barcode labels.",
            checked = draft.supportsBarcodes,
            onCheckedChange = { draft = draft.copy(supportsBarcodes = it) },
        )
        OrmaSwitchRow(
            title = "Default receipt printer",
            body = "Use this printer first for bills and order receipts.",
            checked = draft.isDefaultReceipt,
            onCheckedChange = { draft = draft.copy(isDefaultReceipt = it) },
        )
        OrmaSwitchRow(
            title = "Default barcode printer",
            body = "Use this printer first for barcode labels.",
            checked = draft.isDefaultBarcode,
            onCheckedChange = { draft = draft.copy(isDefaultBarcode = it) },
        )
        OrmaTextField(
            value = draft.notes,
            onValueChange = { draft = draft.copy(notes = it.take(240)) },
            label = "Notes",
            placeholder = "Optional setup note",
            singleLine = false,
            minLines = 2,
        )
        OrmaActionRow(
            primaryText = "Save printer",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.name.trim().length >= 2,
            secondaryText = "Cancel",
            onSecondary = LocalSmoothSheetDismiss.current ?: onDismiss,
        )
    }
}

@Composable
private fun BusinessSetupForm(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    OrmaFormCard {
        when (state.setupStep) {
            BusinessSetupStep.BusinessDetails -> BusinessDetailsForm(state, actions)
            BusinessSetupStep.TaxDetails -> TaxDetailsForm(state, actions)
            BusinessSetupStep.Address -> AddressForm(state, actions)
            BusinessSetupStep.Logo -> LogoForm(state, actions)
            BusinessSetupStep.InvoiceSettings -> InvoiceSettingsForm(state, actions)
            BusinessSetupStep.CurrencyTax -> CurrencyTaxForm(state, actions)
        }
    }
}

@Composable
private fun BusinessDetailsForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    var showIndustryPicker by rememberSaveable { mutableStateOf(false) }
    var showBusinessModePicker by rememberSaveable { mutableStateOf(false) }

    OrmaTextField(
        value = draft.ownerName,
        onValueChange = { actions.onDraftChange(draft.copy(ownerName = it)) },
        label = "Owner name",
        placeholder = "Full name",
    )
    OrmaTextField(
        value = draft.businessName,
        onValueChange = { actions.onDraftChange(draft.copy(businessName = it)) },
        label = "Business name",
        placeholder = "Trading name",
    )
    OrmaTextField(
        value = draft.legalName,
        onValueChange = { actions.onDraftChange(draft.copy(legalName = it)) },
        label = "Legal name",
        placeholder = "Registered legal name",
    )
    BusinessOptionPickerField(
        label = "Industry",
        value = draft.industry,
        placeholder = "Select industry",
        onClick = { showIndustryPicker = true },
    )
    BusinessOptionPickerField(
        label = "How do you sell?",
        value = OrmaBusinessModeOptions.firstOrNull { it.id == draft.businessMode }?.title ?: "Product sales",
        placeholder = "Choose selling mode",
        onClick = { showBusinessModePicker = true },
    )
    OrmaTextField(
        value = draft.website,
        onValueChange = { actions.onDraftChange(draft.copy(website = it)) },
        label = "Website",
        placeholder = "Optional",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )

    if (showIndustryPicker) {
        IndustryPickerSheet(
            selectedIndustry = draft.industry,
            onDismiss = { showIndustryPicker = false },
            onSelect = { industry ->
                actions.onDraftChange(
                    draft.copy(
                        industry = industry,
                        businessMode = ormaBusinessModeForIndustry(industry),
                    ),
                )
                showIndustryPicker = false
            },
        )
    }
    if (showBusinessModePicker) {
        BusinessModePickerSheet(
            selectedMode = draft.businessMode,
            onDismiss = { showBusinessModePicker = false },
            onSelect = { mode ->
                actions.onDraftChange(draft.copy(businessMode = mode))
                showBusinessModePicker = false
            },
        )
    }
}

@Composable
private fun TaxDetailsForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    val normalizedGstin = normalizeGstinNumber(draft.taxNumber)
    val gstinComplete = isGstinNumberComplete(normalizedGstin)
    val lookupAlreadyHandled = state.gstinLookupNumber == normalizedGstin &&
        (state.gstinLookupLoading || state.gstinLookupStatusMessage != null || state.gstinLookupErrorMessage != null)

    LaunchedEffect(draft.isTaxRegistered, normalizedGstin, lookupAlreadyHandled) {
        if (draft.isTaxRegistered && gstinComplete && !lookupAlreadyHandled) {
            actions.onGstinLookupRequest(normalizedGstin)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OrmaChoiceSurface(
            title = "GST/VAT registered",
            body = "Verify GSTIN and prefill business details where available.",
            selected = draft.isTaxRegistered,
            onClick = { actions.onDraftChange(draft.copy(isTaxRegistered = true, taxLabel = "GSTIN")) },
        )
        OrmaChoiceSurface(
            title = "Not registered",
            body = "Continue without a tax registration number.",
            selected = !draft.isTaxRegistered,
            onClick = { actions.onDraftChange(draft.copy(isTaxRegistered = false, taxNumber = "")) },
        )
    }
    AnimatedVisibility(draft.isTaxRegistered) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GstinLookupField(
                value = draft.taxNumber,
                loading = state.gstinLookupLoading,
                complete = gstinComplete,
                statusMessage = state.gstinLookupStatusMessage,
                errorMessage = state.gstinLookupErrorMessage,
                onValueChange = { value ->
                    actions.onDraftChange(
                        draft.copy(
                            taxNumber = normalizeGstinNumber(value),
                            taxLabel = "GSTIN",
                        ),
                    )
                },
                onSearch = { actions.onGstinLookupRequest(normalizedGstin) },
            )
            OrmaTextField(
                value = draft.taxLabel,
                onValueChange = { actions.onDraftChange(draft.copy(taxLabel = it)) },
                label = "Tax label",
                placeholder = "GSTIN",
            )
        }
    }
}

@Composable
private fun GstinLookupField(
    value: String,
    loading: Boolean,
    complete: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    label: String = "GST/VAT number",
    supportingText: String = "Search starts automatically when the GSTIN is complete.",
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrmaTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = "15-character GSTIN",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            supportingText = supportingText,
            trailing = {
                GstinSearchButton(
                    loading = loading,
                    enabled = complete && !loading,
                    onClick = onSearch,
                )
            },
        )
        when {
            loading -> GstinLookupMessage(
                text = "Checking GSTIN...",
                tone = OrmaStatusTone.Neutral,
            )
            statusMessage != null -> GstinLookupMessage(
                text = statusMessage,
                tone = OrmaStatusTone.Success,
            )
            errorMessage != null -> GstinLookupError(text = errorMessage)
            value.isNotBlank() && !complete -> Text(
                text = "GSTIN must be 15 characters: 2 digits followed by 13 letters or digits.",
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.Error,
            )
        }
    }
}

@Composable
private fun GstinSearchButton(
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        enabled = enabled,
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.18f),
        contentColor = if (enabled) OrmaColors.OnAccent else OrmaColors.TextDisabled,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (loading) "Checking" else "Search",
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) OrmaColors.OnAccent else OrmaColors.TextDisabled,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GstinLookupMessage(
    text: String,
    tone: OrmaStatusTone,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrmaBadge(
            text = if (tone == OrmaStatusTone.Success) "VERIFIED" else "CHECKING",
            tone = tone,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
    }
}

@Composable
private fun GstinLookupError(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = OrmaColors.Error,
    )
}

@Composable
private fun AddressForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showRegionPicker by rememberSaveable { mutableStateOf(false) }
    val selectedBusinessCountry = businessCountryForName(draft.country)
    val regionOptions = remember(selectedBusinessCountry.id) {
        ormaBusinessRegionsForCountry(selectedBusinessCountry.id)
    }
    val regionLabel = ormaBusinessRegionLabel(selectedBusinessCountry.id)

    OrmaTextField(
        value = draft.addressLine,
        onValueChange = { actions.onDraftChange(draft.copy(addressLine = it)) },
        label = "Address line",
        placeholder = "Building, street, area",
        singleLine = false,
        minLines = 2,
    )
    OrmaTextField(
        value = draft.city,
        onValueChange = { actions.onDraftChange(draft.copy(city = it)) },
        label = "City",
        placeholder = "City",
    )
    BusinessCountryPickerField(
        country = selectedBusinessCountry,
        onClick = { showCountryPicker = true },
    )
    if (regionOptions.isEmpty()) {
        OrmaTextField(
            value = draft.region,
            onValueChange = { actions.onDraftChange(draft.copy(region = it)) },
            label = "State / province / region",
            placeholder = "Optional",
        )
    } else {
        BusinessRegionPickerField(
            label = regionLabel,
            value = draft.region,
            placeholder = "Select ${regionLabel.lowercase()}",
            onClick = { showRegionPicker = true },
        )
    }
    OrmaTextField(
        value = draft.postalCode,
        onValueChange = { actions.onDraftChange(draft.copy(postalCode = it)) },
        label = "Postal code",
        placeholder = "Optional",
    )

    if (showCountryPicker) {
        CountryPickerSheet(
            selectedCountry = selectedBusinessCountry,
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                actions.onDraftChange(
                    draft.copy(
                        country = country.name,
                        region = if (country.id == selectedBusinessCountry.id) draft.region else "",
                        currency = ormaDefaultCurrencyForCountry(country.id),
                    ),
                )
                showCountryPicker = false
            },
            title = "Country",
            showDialCode = false,
        )
    }

    if (showRegionPicker && regionOptions.isNotEmpty()) {
        RegionPickerSheet(
            title = regionLabel,
            regions = regionOptions,
            selectedRegion = draft.region,
            onDismiss = { showRegionPicker = false },
            onSelect = { region ->
                actions.onDraftChange(draft.copy(region = region.name))
                showRegionPicker = false
            },
        )
    }
}

@Composable
private fun BusinessCountryPickerField(
    country: OrmaCountryUi,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Country",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = OrmaShapes.SmallCard,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.Accent,
            border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountryFlagIcon(country = country)
                Text(
                    text = country.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaChevronDownIcon(
                    modifier = Modifier.size(16.dp),
                    color = OrmaColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun BusinessRegionPickerField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    BusinessOptionPickerField(
        label = label,
        value = value,
        placeholder = placeholder,
        onClick = onClick,
    )
}

@Composable
private fun BusinessOptionPickerField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = OrmaShapes.SmallCard,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.Accent,
            border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) OrmaColors.TextSecondary else OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaChevronDownIcon(
                    modifier = Modifier.size(16.dp),
                    color = OrmaColors.TextSecondary,
                )
            }
        }
    }
}

private fun businessCountryForName(countryName: String): OrmaCountryUi =
    OrmaSupportedCountries.firstOrNull { country ->
        country.name.equals(countryName, ignoreCase = true) ||
            country.id.equals(countryName, ignoreCase = true)
    } ?: OrmaDefaultCountry

@Composable
private fun LogoForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    val logoSelected = draft.logoFileName.isNotBlank() || draft.logoPreviewBytes.isNotEmpty()

    LogoUploadCard(
        initials = businessInitial(draft),
        fileName = draft.logoFileName,
        previewBytes = draft.logoPreviewBytes,
        selected = logoSelected,
        uploading = state.logoUploadLoading,
        onClick = actions.onLogoUploadRequest,
    )
    AnimatedVisibility(logoSelected) {
        OrmaSecondaryButton(
            text = "Remove logo",
            onClick = {
                actions.onDraftChange(
                    draft.copy(
                        logoFileName = "",
                        logoPreviewContentType = "",
                        logoPreviewBytes = byteArrayOf(),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.logoUploadLoading,
        )
    }
}

@Composable
private fun LogoUploadCard(
    initials: String,
    fileName: String,
    previewBytes: ByteArray,
    selected: Boolean,
    uploading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrmaShapes.StandardCell)
            .clickable(enabled = !uploading, onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.24f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            val compact = maxWidth < 360.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LogoPreviewTile(
                            initials = initials,
                            previewBytes = previewBytes,
                            selected = selected,
                        )
                        LogoUploadCopy(
                            selected = selected,
                            fileName = fileName,
                            uploading = uploading,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    LogoUploadActionPill(
                        selected = selected,
                        uploading = uploading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LogoPreviewTile(
                        initials = initials,
                        previewBytes = previewBytes,
                        selected = selected,
                    )
                    LogoUploadCopy(
                        selected = selected,
                        fileName = fileName,
                        uploading = uploading,
                        modifier = Modifier.weight(1f),
                    )
                    LogoUploadActionPill(selected = selected, uploading = uploading)
                }
            }
        }
    }
}

@Composable
private fun CoverUploadCard(
    fileName: String,
    remoteUrl: String,
    selected: Boolean,
    uploading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrmaShapes.StandardCell)
            .clickable(enabled = !uploading, onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.24f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                OrmaColors.Accent.copy(alpha = 0.16f),
                                OrmaColors.CellBackground,
                                OrmaColors.Accent.copy(alpha = 0.10f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (remoteUrl.isNotBlank()) {
                    OrmaRemoteImage(
                        url = remoteUrl,
                        contentDescription = "Business cover photo preview",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OrmaUploadImageIcon(
                            modifier = Modifier.size(34.dp),
                            color = OrmaColors.IconPrimary.copy(alpha = 0.72f),
                        )
                        Text(
                            text = if (selected) "Cover photo saved" else "Add cover photo",
                            style = MaterialTheme.typography.labelLarge,
                            color = OrmaColors.TextPrimary,
                        )
                    }
                }
                OrmaBadge(
                    text = when {
                        uploading -> "UPLOADING"
                        selected -> "COVER READY"
                        else -> "COVER"
                    },
                    tone = if (selected) OrmaStatusTone.Success else OrmaStatusTone.Info,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = when {
                            uploading -> "Uploading cover photo"
                            selected -> "Cover photo ready"
                            else -> "Upload cover photo"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = when {
                            uploading -> "Please wait while ORMA saves the image."
                            selected -> displayLogoFileName(fileName)
                            else -> "Wide JPG, PNG, or WebP. Best for public catalog and workspace profile."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LogoUploadActionPill(
                    selected = selected,
                    uploading = uploading,
                )
            }
        }
    }
}

@Composable
private fun LogoPreviewTile(
    initials: String,
    previewBytes: ByteArray,
    selected: Boolean,
) {
    val hasPreview = previewBytes.isNotEmpty()
    Surface(
        modifier = Modifier.size(72.dp),
        shape = RoundedCornerShape(22.dp),
        color = if (hasPreview) OrmaColors.ScreenBackground else if (selected) OrmaColors.Accent else OrmaColors.ScreenBackground,
        contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.18f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                hasPreview -> {
                    OrmaLogoPreviewImage(
                        bytes = previewBytes,
                        contentDescription = "Business logo preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp)),
                    )
                }
                selected -> {
                Text(
                    text = initials.ifBlank { "O" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                }
                else -> {
                    OrmaUploadImageIcon(
                        modifier = Modifier.size(34.dp),
                        color = OrmaColors.IconPrimary.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoUploadCopy(
    selected: Boolean,
    fileName: String,
    uploading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = when {
                uploading -> "Uploading business logo"
                selected -> "Business logo ready"
                else -> "Upload business logo"
            },
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = when {
                uploading -> "Please wait while ORMA saves the image."
                selected -> displayLogoFileName(fileName)
                else -> "PNG or JPG, ideally square. Used on invoices and estimates."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LogoUploadActionPill(
    selected: Boolean,
    uploading: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = OrmaShapes.Capsule,
        color = if (selected || uploading) OrmaColors.ScreenBackground else OrmaColors.Accent,
        contentColor = if (selected || uploading) OrmaColors.Accent else OrmaColors.OnAccent,
        border = if (selected || uploading) BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f)) else null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    uploading -> "Uploading"
                    selected -> "Change"
                    else -> "Choose logo"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

private fun displayLogoFileName(fileName: String): String =
    fileName.substringAfterLast('/').ifBlank { fileName }

@Composable
private fun InvoiceSettingsForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    OrmaTextField(
        value = draft.invoicePrefix,
        onValueChange = { actions.onDraftChange(draft.copy(invoicePrefix = it.uppercase().take(8))) },
        label = "Invoice prefix",
        placeholder = "ORMA",
    )
    OrmaTextField(
        value = draft.nextInvoiceNumber,
        onValueChange = { actions.onDraftChange(draft.copy(nextInvoiceNumber = it.filter(Char::isDigit).take(8))) },
        label = "Next invoice number",
        placeholder = "0001",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    OrmaTextField(
        value = draft.paymentTerms,
        onValueChange = { actions.onDraftChange(draft.copy(paymentTerms = it)) },
        label = "Payment terms",
        placeholder = "Due on receipt",
    )
    OrmaTextField(
        value = draft.invoiceFooter,
        onValueChange = { actions.onDraftChange(draft.copy(invoiceFooter = it)) },
        label = "Invoice footer",
        placeholder = "Thank you for your business.",
        singleLine = false,
        minLines = 3,
    )
}

@Composable
private fun CurrencyTaxForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    SelectorBlock(
        title = "Default currency",
        options = OrmaSupportedCurrencies,
        selected = draft.currency,
        label = { it },
        onSelected = { actions.onDraftChange(draft.copy(currency = it)) },
    )
    SelectorBlock(
        title = "Tax behavior",
        options = OrmaTaxModes,
        selected = draft.taxMode,
        label = { it },
        onSelected = { actions.onDraftChange(draft.copy(taxMode = it)) },
    )
    OrmaSwitchRow(
        title = "Prices include tax",
        body = "Use inclusive pricing for invoices and checkout totals.",
        checked = draft.pricesIncludeTax,
        onCheckedChange = { actions.onDraftChange(draft.copy(pricesIncludeTax = it)) },
    )
}

@Composable
private fun LoginIdentifierField(
    value: String,
    onValueChange: (String) -> Unit,
    type: AuthIdentifierType,
    selectedCountry: OrmaCountryUi,
    onCountryClick: () -> Unit,
    supportingText: String?,
    showLabel: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showLabel) {
            Text(
                text = type.fieldLabel,
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
        }
        if (type == AuthIdentifierType.Phone) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onCountryClick,
                    modifier = Modifier
                        .height(56.dp)
                        .width(114.dp),
                    shape = OrmaShapes.Field,
                    color = OrmaColors.CellBackground,
                    contentColor = OrmaColors.Accent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CountryFlagIcon(country = selectedCountry)
                        Text(text = selectedCountry.dialCode, style = MaterialTheme.typography.bodyLarge, color = OrmaColors.TextPrimary)
                        OrmaChevronDownIcon(
                            modifier = Modifier.size(14.dp),
                            color = OrmaColors.TextSecondary,
                        )
                    }
                }
                AuthFilledTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = selectedCountry.placeholder,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            AuthFilledTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "owner@business.com",
                keyboardType = KeyboardType.Email,
            )
        }
        supportingText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.Error,
            )
        }
    }
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Password",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        AuthFilledTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Minimum 6 characters",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
        )
        supportingText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.Error,
            )
        }
    }
}

@Composable
private fun AuthFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = OrmaColors.TextPrimary)),
        cursorBrush = SolidColor(OrmaColors.Accent),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.Field,
                color = OrmaColors.CellBackground,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OrmaColors.TextSecondary,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun AuthChromeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.Accent,
        border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "<",
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndustryPickerSheet(
    selectedIndustry: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    val requestSelect: (String) -> Unit = { industry ->
        closeSheet { onSelect(industry) }
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.Accent,
        scrimColor = Color.Black.copy(alpha = 0.36f),
        tonalElevation = 0.dp,
    ) {
        IndustryPickerSheetContent(
            selectedIndustry = selectedIndustry,
            onDismiss = requestDismiss,
            onSelect = requestSelect,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessModePickerSheet(
    selectedMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    val requestSelect: (String) -> Unit = { mode ->
        closeSheet { onSelect(mode) }
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.Accent,
        scrimColor = Color.Black.copy(alpha = 0.36f),
        tonalElevation = 0.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val horizontalGutter = if (maxWidth < 720.dp) 0.dp else 32.dp
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalGutter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 8.dp),
                ) {
                    Text(
                        text = "How do you sell?",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                    )
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(44.dp),
                        shape = OrmaShapes.Capsule,
                        color = OrmaColors.CellBackground,
                        contentColor = OrmaColors.Accent,
                        border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            OrmaCloseIcon(
                                modifier = Modifier.size(18.dp),
                                color = OrmaColors.IconPrimary,
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OrmaBusinessModeOptions.forEach { option ->
                        OrmaChoiceSurface(
                            title = option.title,
                            body = option.body,
                            selected = option.id == selectedMode,
                            onClick = { requestSelect(option.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndustryPickerSheetContent(
    selectedIndustry: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val horizontalGutter = if (maxWidth < 720.dp) 0.dp else 32.dp

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalGutter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 8.dp),
            ) {
                Text(
                    text = "Industry",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp),
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.CellBackground,
                    contentColor = OrmaColors.Accent,
                    border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        OrmaCloseIcon(
                            modifier = Modifier.size(18.dp),
                            color = OrmaColors.IconPrimary,
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .padding(top = 6.dp, bottom = 18.dp),
            ) {
                items(
                    items = OrmaSupportedIndustries,
                    key = { it },
                ) { industry ->
                    IndustryPickerRow(
                        industry = industry,
                        selected = industry.equals(selectedIndustry, ignoreCase = true),
                        onClick = { onSelect(industry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun IndustryPickerRow(
    industry: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OrmaColors.CellBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = industry,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextPrimary,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 24.dp),
            color = OrmaColors.Accent.copy(alpha = 0.06f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionPickerSheet(
    title: String,
    regions: List<OrmaRegionUi>,
    selectedRegion: String,
    onDismiss: () -> Unit,
    onSelect: (OrmaRegionUi) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    val requestSelect: (OrmaRegionUi) -> Unit = { region ->
        closeSheet { onSelect(region) }
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.Accent,
        scrimColor = Color.Black.copy(alpha = 0.36f),
        tonalElevation = 0.dp,
    ) {
        RegionPickerSheetContent(
            title = title,
            regions = regions,
            selectedRegion = selectedRegion,
            onDismiss = requestDismiss,
            onSelect = requestSelect,
        )
    }
}

@Composable
private fun RegionPickerSheetContent(
    title: String,
    regions: List<OrmaRegionUi>,
    selectedRegion: String,
    onDismiss: () -> Unit,
    onSelect: (OrmaRegionUi) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredRegions = remember(query, regions) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            regions
        } else {
            regions.filter { region ->
                region.name.lowercase().contains(normalizedQuery)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.82f)
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val horizontalGutter = if (maxWidth < 720.dp) 0.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalGutter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 8.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp),
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.CellBackground,
                    contentColor = OrmaColors.Accent,
                    border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        OrmaCloseIcon(
                            modifier = Modifier.size(18.dp),
                            color = OrmaColors.IconPrimary,
                        )
                    }
                }
            }

            CountrySearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search ${title.lowercase()}",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp),
            ) {
                items(
                    items = filteredRegions,
                    key = { it.id },
                ) { region ->
                    RegionPickerRow(
                        region = region,
                        selected = region.name.equals(selectedRegion, ignoreCase = true),
                        onClick = { onSelect(region) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionPickerRow(
    region: OrmaRegionUi,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OrmaColors.CellBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = region.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextPrimary,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 24.dp),
            color = OrmaColors.Accent.copy(alpha = 0.06f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountryPickerSheet(
    selectedCountry: OrmaCountryUi,
    onDismiss: () -> Unit,
    onSelect: (OrmaCountryUi) -> Unit,
    title: String = "Country code",
    showDialCode: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet = rememberSmoothSheetDismiss(sheetState)
    val requestDismiss = { closeSheet(onDismiss) }
    val requestSelect: (OrmaCountryUi) -> Unit = { country ->
        closeSheet { onSelect(country) }
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.Accent,
        scrimColor = Color.Black.copy(alpha = 0.36f),
        tonalElevation = 0.dp,
    ) {
        CountryPickerSheetContent(
            selectedCountry = selectedCountry,
            onDismiss = requestDismiss,
            onSelect = requestSelect,
            title = title,
            showDialCode = showDialCode,
        )
    }
}

@Composable
private fun CountryPickerSheetContent(
    selectedCountry: OrmaCountryUi,
    onDismiss: () -> Unit,
    onSelect: (OrmaCountryUi) -> Unit,
    title: String,
    showDialCode: Boolean,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredCountries = remember(query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            OrmaSupportedCountries
        } else {
            OrmaSupportedCountries.filter { country ->
                country.name.lowercase().contains(normalizedQuery) ||
                    country.dialCode.contains(normalizedQuery) ||
                    country.id.lowercase().contains(normalizedQuery)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val horizontalGutter = if (maxWidth < 720.dp) 0.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 920.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalGutter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 8.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp),
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.CellBackground,
                    contentColor = OrmaColors.Accent,
                    border = BorderStroke(0.5.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        OrmaCloseIcon(
                            modifier = Modifier.size(18.dp),
                            color = OrmaColors.IconPrimary,
                        )
                    }
                }
            }

            CountrySearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search countries",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp),
            ) {
                items(
                    items = filteredCountries,
                    key = { it.id },
                ) { country ->
                    CountryPickerRow(
                        country = country,
                        selected = country.id == selectedCountry.id,
                        showDialCode = showDialCode,
                        onClick = { onSelect(country) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryPickerRow(
    country: OrmaCountryUi,
    selected: Boolean,
    showDialCode: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OrmaColors.CellBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CountryFlagIcon(country = country)
            Text(
                text = country.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showDialCode || selected) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showDialCode) {
                        Text(
                            text = country.dialCode,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OrmaColors.TextPrimary.copy(alpha = 0.40f),
                        )
                    }
                    if (selected) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.TextPrimary,
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 90.dp),
            color = OrmaColors.Accent.copy(alpha = 0.06f),
        )
    }
}

@Composable
internal fun CountryFlagIcon(
    country: OrmaCountryUi,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .size(width = 30.dp, height = 20.dp)
            .clip(shape)
            .background(Color.White)
            .border(0.6.dp, OrmaColors.Accent.copy(alpha = 0.14f), shape),
    ) {
        when (country.id) {
            "AE" -> FlagUnitedArabEmirates()
            "SA" -> FlagSaudiArabia()
            "BH" -> FlagBahrain()
            "KW" -> FlagKuwait()
            "OM" -> FlagOman()
            "QA" -> FlagQatar()
            "IN" -> FlagIndia()
            "US" -> FlagUnitedStates()
            "GB" -> FlagUnitedKingdom()
            else -> CountryCodeFlagBadge(country.id)
        }
    }
}

@Composable
private fun FlagUnitedArabEmirates() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(7.dp)
                .fillMaxHeight()
                .background(Color(0xFFFF1E2D)),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF009A44)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF000000)),
            )
        }
    }
}

@Composable
private fun FlagSaudiArabia() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF006C35)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(15.dp)
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(1.dp)),
        )
    }
}

@Composable
private fun FlagBahrain() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight()
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCE1126)),
        )
    }
}

@Composable
private fun FlagKuwait() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(9.dp)
                .fillMaxHeight()
                .background(Color(0xFF000000)),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF007A3D)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFCE1126)),
            )
        }
    }
}

@Composable
private fun FlagOman() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(9.dp)
                .fillMaxHeight()
                .background(Color(0xFFC8102E)),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFC8102E)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF009A44)),
            )
        }
    }
}

@Composable
private fun FlagQatar() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight()
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF8A1538)),
        )
    }
}

@Composable
private fun FlagIndia() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFFF9933)),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color(0xFF000080), CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF138808)),
        )
    }
}

@Composable
private fun FlagUnitedStates() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color(0xFFB22234) else Color.White),
                )
            }
        }
        Box(
            modifier = Modifier
                .width(13.dp)
                .height(10.dp)
                .background(Color(0xFF3C3B6E)),
        )
    }
}

@Composable
private fun FlagUnitedKingdom() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF012169)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFFC8102E)),
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Color(0xFFC8102E)),
        )
    }
}

@Composable
private fun CountryCodeFlagBadge(countryId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrmaColors.CellBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = countryId.take(2).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = OrmaColors.TextPrimary.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun CountrySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = OrmaColors.TextPrimary)),
        cursorBrush = SolidColor(OrmaColors.Accent),
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(OrmaShapes.Field)
            .background(OrmaColors.Accent.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaFlatIcon(
                    kind = OrmaFlatIconKind.Search,
                    modifier = Modifier.size(15.dp),
                    color = OrmaColors.IconPrimary.copy(alpha = 0.42f),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OrmaColors.TextPrimary.copy(alpha = 0.32f),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun <T> SelectorBlock(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        OrmaSegmentedRow(
            options = options,
            selected = selected,
            label = label,
            onSelected = onSelected,
        )
    }
}

@Composable
private fun SetupStepSelector(
    selected: BusinessSetupStep,
    onSelected: (BusinessSetupStep) -> Unit,
) {
    val labels = mapOf(
        BusinessSetupStep.BusinessDetails to "Details",
        BusinessSetupStep.TaxDetails to "Tax",
        BusinessSetupStep.Address to "Address",
        BusinessSetupStep.Logo to "Logo",
        BusinessSetupStep.InvoiceSettings to "Invoice",
        BusinessSetupStep.CurrencyTax to "Currency",
    )
    OrmaSegmentedRow(
        options = BusinessSetupStep.entries,
        selected = selected,
        label = { "${BusinessSetupStep.entries.indexOf(it) + 1}. ${labels.getValue(it)}" },
        onSelected = onSelected,
    )
}

@Composable
private fun SetupActionRow(
    currentIndex: Int,
    steps: List<BusinessSetupStep>,
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    OrmaActionRow(
        secondaryText = if (currentIndex == 0) "Owner" else "Back",
        onSecondary = actions.onBack,
        primaryText = if (state.onboardingLoading) {
            "Saving..."
        } else if (currentIndex == steps.lastIndex) {
            "Finish"
        } else {
            "Next"
        },
        onPrimary = actions.onContinue,
        primaryEnabled = state.setupReady && !state.onboardingLoading,
    )
}

@Composable
private fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = code,
        onValueChange = { onCodeChange(it.filter(Char::isDigit).take(6)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = MaterialTheme.typography.headlineMedium.merge(TextStyle(color = Color.Transparent)),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .focusRequester(focusRequester),
        decorationBox = {
            OrmaOtpCells(code = code)
        },
    )
}

@Composable
internal fun BusinessPreviewCard(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val draft = state.draft
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = androidx.compose.foundation.BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = OrmaShapes.SmallCard,
                    color = OrmaColors.Accent,
                    contentColor = OrmaColors.OnAccent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = businessInitial(draft),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = draft.businessName.ifBlank { "Business name" },
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = state.setupStep.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Legal" to draft.legalName,
                    "Tax" to if (draft.isTaxRegistered) draft.taxNumber.ifBlank { "Required" } else "Not registered",
                    "Invoice" to "${draft.invoicePrefix}-${draft.nextInvoiceNumber}",
                    "Currency" to draft.currency,
                ),
            )
        }
    }
}

@Composable
private fun CompletionCard(state: OnboardingUiState) {
    val owner = state.accessPath == AccessPath.BusinessOwner
    val draft = state.draft
    OrmaFormCard {
        OrmaBadge(
            text = if (owner && isBusinessSetupComplete(draft)) "READY" else "VERIFIED",
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = if (owner) draft.businessName.ifBlank { "New workspace" } else "Team workspace access",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        OrmaKeyValueList(rows = completionRows(state))
    }
}

private fun completionRows(state: OnboardingUiState): List<Pair<String, String>> {
    val draft = state.draft
    return if (state.accessPath == AccessPath.BusinessOwner) {
        listOf(
            "Business" to draft.businessName,
            "Legal" to draft.legalName,
            "Address" to listOf(draft.city, draft.country).filter { it.isNotBlank() }.joinToString(", "),
            "Invoice" to "${draft.invoicePrefix}-${draft.nextInvoiceNumber}",
            "Currency" to draft.currency,
            "Notifications" to if (state.notificationsEnabled) "Enabled" else "Ask later",
        )
    } else {
        listOf(
            "Signed in as" to state.identifier,
            "Workspace" to state.workspaceName.ifBlank { "Not linked" },
            "Role source" to "Workspace access required",
            "Notifications" to if (state.notificationsEnabled) "Enabled" else "Ask later",
        )
    }
}

private fun OnboardingUiState.otpDestinationLabel(): String =
    if (identifierType == AuthIdentifierType.Phone) {
        "${selectedCountry.dialCode} ${maskedPhone(identifier)}"
    } else {
        identifier.trim()
    }

private fun maskedPhone(value: String): String {
    val digits = value.filter(Char::isDigit)
    return if (digits.length <= 4) digits else "**** ${digits.takeLast(4)}"
}

private fun businessInitial(draft: BusinessSetupDraft): String =
    draft.businessName.take(1).uppercase().ifBlank { "O" }
