package org.orma.project_90.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
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
import org.orma.project_90.backend.OrmaCustomer
import org.orma.project_90.backend.OrmaCustomerDraft
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaOrderItemDraft
import org.orma.project_90.backend.OrmaProduct
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaSupplier
import org.orma.project_90.backend.OrmaSupplierDraft
import org.orma.project_90.media.OrmaLogoPreviewImage
import org.orma.project_90.designsystem.OrmaActionRow
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaBrandMark
import org.orma.project_90.designsystem.OrmaChevronDownIcon
import org.orma.project_90.designsystem.OrmaCloseIcon
import org.orma.project_90.designsystem.OrmaChoiceSurface
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaFormCard
import org.orma.project_90.designsystem.OrmaGoogleBrandIcon
import org.orma.project_90.designsystem.OrmaKeyValueList
import org.orma.project_90.designsystem.OrmaOtpCells
import org.orma.project_90.designsystem.OrmaScreenColumn
import org.orma.project_90.designsystem.OrmaSecondaryButton
import org.orma.project_90.designsystem.OrmaSegmentedRow
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.OrmaSwitchRow
import org.orma.project_90.designsystem.OrmaTextButton
import org.orma.project_90.designsystem.OrmaTextField
import org.orma.project_90.designsystem.OrmaUploadImageIcon

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
            Text(
                text = "PHONE NUMBER",
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextSecondary,
            )
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
        border = BorderStroke(1.dp, OrmaColors.Hairline),
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
                color = if (enabled) OrmaColors.Accent else OrmaColors.TextDisabled,
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
            Text(
                text = "VERIFICATION CODE",
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextSecondary,
            )
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
                text = "Join existing workspace",
                onClick = { actions.onAccessPathChange(AccessPath.TeamMember) },
            )
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
    val hasMatchedInvite = state.workspaceId.isNotBlank() && state.teamInviteCode.isNotBlank()
    val contactLabel = state.pendingInviteEmail
        .ifBlank { state.pendingInvitePhoneNumber }
        .ifBlank { state.identifier.trim() }
        .ifBlank { "Signed-in account" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MobileStageHeader(
            eyebrow = if (hasMatchedInvite) "INVITED WORKSPACE" else "TEAM ACCESS",
            title = if (hasMatchedInvite) "Complete team profile" else "Join workspace",
            body = if (hasMatchedInvite) {
                "Confirm the business and add the name your team will see."
            } else {
                "Enter the invite from your business owner, then finish your team profile."
            },
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (hasMatchedInvite) {
                MobileAccountSummary(
                    rows = buildList {
                        add("Business" to state.workspaceName.ifBlank { "Workspace" })
                        state.workspaceLegalName.takeIf { it.isNotBlank() }?.let { add("Legal name" to it) }
                        add("Role" to teamRoleLabel(state.pendingInviteRole.ifBlank { "team_member" }))
                        add("Invited account" to contactLabel)
                    },
                )
            } else {
                OrmaTextField(
                    value = state.teamInviteCode,
                    onValueChange = actions.onTeamInviteCodeChange,
                    label = "Invite code",
                    placeholder = "Code from owner",
                )
            }
            OrmaTextField(
                value = state.teamProfileName,
                onValueChange = actions.onTeamProfileNameChange,
                label = "Your name",
                placeholder = state.pendingInviteNameOrFallback(),
                enabled = !state.onboardingLoading,
            )
            MobileAccountSummary(
                rows = buildList {
                    add("Signed in as" to state.identifier.trim().ifBlank { "Authenticated user" })
                    add("Workspace" to state.workspaceName.ifBlank { "Invite required" })
                    if (!hasMatchedInvite) {
                        add("Invite code" to state.teamInviteCode.trim().ifBlank { "Not linked" })
                    }
                    add("Access" to "Existing workspace")
                },
            )
        }

        OrmaActionRow(
            primaryText = if (state.onboardingLoading) "Joining..." else "Finish",
            onPrimary = actions.onContinue,
            primaryEnabled = !state.onboardingLoading && state.teamProfileReady,
        )
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

private fun OnboardingUiState.pendingInviteNameOrFallback(): String =
    teamInviteName
        .ifBlank { teamProfileName }
        .ifBlank { "Full name" }

private fun teamRoleLabel(role: String): String = when (role) {
    "manager" -> "Manager"
    "cashier" -> "Cashier"
    "accountant" -> "Accountant"
    "inventory_manager" -> "Inventory"
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.TextSecondary,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = row.first,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                    )
                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextPrimary,
                        maxLines = 2,
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
            Text(
                text = "DETAILS",
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextSecondary,
            )
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Column(
            modifier = Modifier.widthIn(max = 800.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Set up your business",
                style = MaterialTheme.typography.displayLarge,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Complete each setup step before ORMA opens the workspace.",
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        SetupProgressLine(
            currentIndex = currentIndex,
            total = steps.size,
            modifier = Modifier.widthIn(max = 800.dp),
        )

        Column(
            modifier = Modifier.widthIn(max = 800.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "STEP ${currentIndex + 1} OF ${steps.size}",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Start,
                )
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
        contentColor = if (secondary) OrmaColors.TextPrimary else OrmaColors.ScreenBackground,
        border = if (secondary) BorderStroke(1.dp, OrmaColors.Hairline) else null,
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
                    OrmaColors.ScreenBackground
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(720.dp),
    ) {
        NotificationPermissionPhoneMock(
            showPermissionAnimation = showPermissionAnimation,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(top = 8.dp, bottom = 120.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(OrmaColors.ScreenBackground.copy(alpha = 0.96f)),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 15.dp)
                .padding(bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Text(
                text = "Stay connected with\npush notifications",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = OrmaColors.Accent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "ORMA will send updates for invoices, orders, tax alerts, and workspace activity.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = OrmaColors.Accent.copy(alpha = 0.45f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Surface(
                onClick = { actions.onNotificationDecision(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 28.dp),
                shape = OrmaShapes.Capsule,
                color = OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.onboardingLoading) "Enabling..." else "Allow notifications",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = Color.White,
                    )
                }
            }
            Text(
                text = "Ask me later",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = OrmaColors.Accent.copy(alpha = 0.45f),
                modifier = Modifier
                    .clickable { actions.onNotificationDecision(false) }
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun NotificationPermissionWideStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    showPermissionAnimation: Boolean,
) {
    val previewAlpha by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 260f),
        label = "orma_wide_notification_preview_alpha",
    )
    val previewScale by animateFloatAsState(
        targetValue = if (showPermissionAnimation) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f),
        label = "orma_wide_notification_preview_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 620.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 1080.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                ),
            shape = RoundedCornerShape(34.dp),
            color = OrmaColors.ScreenBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(1.dp, OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(34.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.92f)
                        .heightIn(min = 500.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        OrmaBadge(
                            text = "FINAL STEP",
                            tone = OrmaStatusTone.Info,
                        )
                        Text(
                            text = "Keep every workspace signal in reach",
                            style = MaterialTheme.typography.displayLarge,
                            color = OrmaColors.TextPrimary,
                        )
                        Text(
                            text = "ORMA can notify the right owner or team member when invoices move, orders need action, or tax deadlines are close.",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.TextSecondary,
                            lineHeight = 27.sp,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    }

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

                NotificationPreviewPanel(
                    previewAlpha = previewAlpha,
                    previewScale = previewScale,
                    modifier = Modifier.weight(1f),
                )
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
                    color = OrmaColors.Accent,
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
        contentColor = if (primary) OrmaColors.ScreenBackground else OrmaColors.TextPrimary,
        border = if (primary) null else BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (primary) OrmaColors.ScreenBackground else OrmaColors.TextPrimary,
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
        contentColor = OrmaColors.ScreenBackground,
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
                        contentColor = OrmaColors.ScreenBackground,
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
        contentColor = if (primary) OrmaColors.ScreenBackground else OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (primary) OrmaColors.ScreenBackground else OrmaColors.TextPrimary,
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
    val canInviteMembers = state.accessPath == AccessPath.BusinessOwner
    var selectedSectionName by rememberSaveable { mutableStateOf(DashboardSection.Dashboard.name) }
    val selectedSection = DashboardSection.entries.firstOrNull { it.name == selectedSectionName }
        ?: DashboardSection.Dashboard

    val workspaceName = state.workspaceName
        .ifBlank { state.draft.businessName }
        .ifBlank { "ORMA workspace" }
    val roleLabel = when (state.accessPath) {
        AccessPath.BusinessOwner -> "Business owner"
        AccessPath.TeamMember -> "Team member"
    }

    LaunchedEffect(
        selectedSection,
        canInviteMembers,
        state.teamInviteCode,
        state.inviteLoading,
        state.inviteErrorMessage,
    ) {
        if (
            selectedSection == DashboardSection.Account &&
            canInviteMembers &&
            state.teamInviteCode.isBlank() &&
            !state.inviteLoading &&
            state.inviteErrorMessage == null
        ) {
            actions.onRefreshTeamInvite()
        }
    }

    if (wide) {
        DashboardWideStage(
            workspaceName = workspaceName,
            roleLabel = roleLabel,
            state = state,
            selectedSection = selectedSection,
            canInviteMembers = canInviteMembers,
            actions = actions,
            onSectionSelected = { selectedSectionName = it.name },
            onLogout = actions.onRestart,
        )
    } else {
        DashboardMobileStage(
            workspaceName = workspaceName,
            roleLabel = roleLabel,
            state = state,
            selectedSection = selectedSection,
            canInviteMembers = canInviteMembers,
            actions = actions,
            onSectionSelected = { selectedSectionName = it.name },
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
    actions: OnboardingActions,
    onSectionSelected: (DashboardSection) -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DashboardSwipeRefreshContainer(
            loading = state.dashboard.loading,
            canRefresh = scrollState.value == 0,
            onRefresh = actions.onDashboardRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp)
                    .padding(top = 18.dp, bottom = 148.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DashboardMobileHeader(
                    workspaceName = workspaceName,
                    roleLabel = roleLabel,
                    selectedSection = selectedSection,
                )
                DashboardFeedback(state = state, actions = actions)
                DashboardSectionContent(
                    state = state,
                    roleLabel = roleLabel,
                    selectedSection = selectedSection,
                    canInviteMembers = canInviteMembers,
                    actions = actions,
                    wide = false,
                )
            }
        }

        DashboardBottomBar(
            selectedSection = selectedSection,
            onSectionSelected = onSectionSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 12.dp),
        )
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
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = indicatorText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.Accent,
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
    actions: OnboardingActions,
    onSectionSelected: (DashboardSection) -> Unit,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DashboardSidebar(
            workspaceName = workspaceName,
            roleLabel = roleLabel,
            selectedSection = selectedSection,
            onSectionSelected = onSectionSelected,
            onLogout = onLogout,
            modifier = Modifier
                .width(286.dp)
                .fillMaxHeight(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DashboardWideHeader(
                workspaceName = workspaceName,
                roleLabel = roleLabel,
                selectedSection = selectedSection,
            )
            DashboardFeedback(state = state, actions = actions)
            DashboardSectionContent(
                state = state,
                roleLabel = roleLabel,
                selectedSection = selectedSection,
                canInviteMembers = canInviteMembers,
                actions = actions,
                wide = true,
            )
        }
    }
}

@Composable
private fun DashboardMobileHeader(
    workspaceName: String,
    roleLabel: String,
    selectedSection: DashboardSection,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardWorkspaceAvatar(workspaceName = workspaceName)
        }
        OrmaBadge(
            text = roleLabel.uppercase(),
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = selectedSection.title,
            style = MaterialTheme.typography.headlineSmall,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = workspaceName,
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

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
            contentColor = OrmaColors.ScreenBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = workspaceName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.take(1).uppercase() }
                        .ifBlank { "O" },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.ScreenBackground,
                )
            }
        }
        Text(
            text = "ORMA",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.Accent,
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
    val status = state.dashboard.statusMessage
    when {
        !error.isNullOrBlank() -> DashboardMessageCard(
            title = title ?: "Could not update workspace",
            body = error,
            tone = OrmaStatusTone.Danger,
            onDismiss = actions.onClearDashboardMessage,
        )
        !status.isNullOrBlank() -> DashboardMessageCard(
            title = "Done",
            body = status,
            tone = OrmaStatusTone.Success,
            onDismiss = actions.onClearDashboardMessage,
        )
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
    roleLabel: String,
    selectedSection: DashboardSection,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrmaBadge(
                text = roleLabel.uppercase(),
                tone = OrmaStatusTone.Success,
            )
            Text(
                text = workspaceName,
                style = MaterialTheme.typography.displayLarge,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = selectedSection.title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextSecondary,
            )
        }
        Surface(
            shape = OrmaShapes.Capsule,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.Accent,
            border = BorderStroke(1.dp, OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = "Live workspace",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.Accent,
            )
        }
    }
}

@Composable
private fun DashboardSidebar(
    workspaceName: String,
    roleLabel: String,
    selectedSection: DashboardSection,
    onSectionSelected: (DashboardSection) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DashboardSidebarBrand()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WORKSPACE",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.TextSecondary,
                )
                Text(
                    text = workspaceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrmaColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }

            HorizontalDivider(color = OrmaColors.Divider)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardNavItems.forEach { item ->
                    DashboardSidebarItem(
                        title = item.sidebarTitle,
                        body = item.sidebarBody,
                        selected = selectedSection == item.section,
                        icon = item.icon,
                        onClick = { onSectionSelected(item.section) },
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            DashboardSidebarItem(
                title = "Logout",
                body = "Clear this session",
                selected = false,
                icon = DashboardNavIconKind.Logout,
                onClick = onLogout,
            )
        }
    }
}

@Composable
private fun DashboardSidebarBrand() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = OrmaShapes.SmallCard,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.ScreenBackground,
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
        Text(
            text = "ORMA",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.Accent,
        )
    }
}

@Composable
private fun DashboardSidebarItem(
    title: String,
    body: String,
    selected: Boolean,
    icon: DashboardNavIconKind,
    onClick: () -> Unit,
) {
    val itemBackground = if (selected) OrmaColors.ScreenBackground else Color.Transparent
    val iconBackground = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f)
    val iconColor = if (selected) OrmaColors.ScreenBackground else OrmaColors.Accent

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = itemBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, if (selected) OrmaColors.Hairline else Color.Transparent),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DashboardBottomBar(
    selectedSection: DashboardSection,
    onSectionSelected: (DashboardSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = DashboardNavItems
    val barHeight = 72.dp
    val barShape = RoundedCornerShape(36.dp)
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
                            OrmaColors.ScreenBackground.copy(alpha = 0.99f),
                            OrmaColors.ScreenBackground.copy(alpha = 0.99f),
                            OrmaColors.ScreenBackground,
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.69f), barShape),
            shape = barShape,
            color = Color.Transparent,
            contentColor = OrmaColors.Accent,
            border = BorderStroke(0.8.dp, OrmaColors.Hairline),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val horizontalPadding = 7.dp
                val verticalPadding = 8.dp
                val itemSpacing = 4.dp
                val selectedIndex = tabs.indexOfFirst { it.section == selectedSection }.coerceAtLeast(0)
                val itemWidth = (maxWidth - (horizontalPadding * 2) - (itemSpacing * (tabs.size - 1))) / tabs.size
                val selectedPillOffset by animateDpAsState(
                    targetValue = horizontalPadding + ((itemWidth + itemSpacing) * selectedIndex),
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    label = "orma-dashboard-bottom-bar-pill",
                )

                Box(
                    modifier = Modifier
                        .offset(x = selectedPillOffset)
                        .padding(vertical = verticalPadding)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .clip(itemShape)
                        .background(OrmaColors.Accent.copy(alpha = 0.15f)),
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEach { item ->
                        val selected = selectedSection == item.section
                        val tint = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.86f)
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
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                DashboardNavIcon(
                                    kind = item.icon,
                                    color = tint,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    text = item.bottomLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 12.5.sp,
                                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
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
    Canvas(modifier = modifier) {
        val strokeWidth = (size.minDimension * 0.115f).coerceAtLeast(2.0f)
        when (kind) {
            DashboardNavIconKind.Home -> {
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.18f, size.height * 0.48f),
                    end = Offset(size.width * 0.50f, size.height * 0.22f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.50f, size.height * 0.22f),
                    end = Offset(size.width * 0.82f, size.height * 0.48f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.30f, size.height * 0.46f),
                    size = Size(size.width * 0.40f, size.height * 0.34f),
                    cornerRadius = CornerRadius(size.minDimension * 0.06f, size.minDimension * 0.06f),
                    style = Stroke(width = strokeWidth),
                )
            }
            DashboardNavIconKind.Invite -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.14f,
                    center = Offset(size.width * 0.36f, size.height * 0.34f),
                    style = Stroke(width = strokeWidth),
                )
                drawRoundRect(
                    color = color.copy(alpha = 0.78f),
                    topLeft = Offset(size.width * 0.18f, size.height * 0.58f),
                    size = Size(size.width * 0.36f, size.height * 0.20f),
                    cornerRadius = CornerRadius(size.minDimension * 0.10f, size.minDimension * 0.10f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.68f, size.height * 0.34f),
                    end = Offset(size.width * 0.68f, size.height * 0.68f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.51f, size.height * 0.51f),
                    end = Offset(size.width * 0.85f, size.height * 0.51f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            DashboardNavIconKind.Orders -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.16f),
                    size = Size(size.width * 0.52f, size.height * 0.68f),
                    cornerRadius = CornerRadius(size.minDimension * 0.07f, size.minDimension * 0.07f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.36f),
                    end = Offset(size.width * 0.66f, size.height * 0.36f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.54f),
                    end = Offset(size.width * 0.66f, size.height * 0.54f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.70f),
                    end = Offset(size.width * 0.54f, size.height * 0.70f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            DashboardNavIconKind.Customers -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.15f,
                    center = Offset(size.width * 0.50f, size.height * 0.34f),
                    style = Stroke(width = strokeWidth),
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.60f),
                    size = Size(size.width * 0.52f, size.height * 0.22f),
                    cornerRadius = CornerRadius(size.minDimension * 0.13f, size.minDimension * 0.13f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.73f, size.height * 0.28f),
                    end = Offset(size.width * 0.73f, size.height * 0.48f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.63f, size.height * 0.38f),
                    end = Offset(size.width * 0.83f, size.height * 0.38f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            DashboardNavIconKind.Products -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.24f),
                    size = Size(size.width * 0.52f, size.height * 0.56f),
                    cornerRadius = CornerRadius(size.minDimension * 0.08f, size.minDimension * 0.08f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.36f, size.height * 0.42f),
                    end = Offset(size.width * 0.64f, size.height * 0.42f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.36f, size.height * 0.58f),
                    end = Offset(size.width * 0.58f, size.height * 0.58f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            DashboardNavIconKind.Account -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.16f,
                    center = Offset(size.width * 0.50f, size.height * 0.34f),
                    style = Stroke(width = strokeWidth),
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.58f),
                    size = Size(size.width * 0.52f, size.height * 0.22f),
                    cornerRadius = CornerRadius(size.minDimension * 0.13f, size.minDimension * 0.13f),
                    style = Stroke(width = strokeWidth),
                )
            }
            DashboardNavIconKind.Logout -> {
                drawRoundRect(
                    color = color.copy(alpha = 0.74f),
                    topLeft = Offset(size.width * 0.18f, size.height * 0.24f),
                    size = Size(size.width * 0.34f, size.height * 0.52f),
                    cornerRadius = CornerRadius(size.minDimension * 0.05f, size.minDimension * 0.05f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.42f, size.height * 0.50f),
                    end = Offset(size.width * 0.82f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.68f, size.height * 0.36f),
                    end = Offset(size.width * 0.82f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.68f, size.height * 0.64f),
                    end = Offset(size.width * 0.82f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private enum class DashboardNavIconKind {
    Home,
    Orders,
    Customers,
    Invite,
    Products,
    Account,
    Logout,
}

private enum class DashboardSection {
    Dashboard,
    OrdersBookings,
    Customers,
    Products,
    Account,
}

private val DashboardSection.title: String
    get() = when (this) {
        DashboardSection.Dashboard -> "Dashboard"
        DashboardSection.OrdersBookings -> "Orders and bookings"
        DashboardSection.Customers -> "Customers"
        DashboardSection.Products -> "Products"
        DashboardSection.Account -> "Account"
    }

private val DashboardSection.description: String
    get() = when (this) {
        DashboardSection.Dashboard -> "Run orders, fulfilment, customers, and catalog."
        DashboardSection.OrdersBookings -> "Take orders, bookings, payments, and dispatch."
        DashboardSection.Customers -> "Keep customer records ready for follow-up."
        DashboardSection.Products -> "Manage sellable products, services, stock, and suppliers."
        DashboardSection.Account -> "Workspace, logo, team, and session settings."
    }

private data class DashboardNavItem(
    val section: DashboardSection,
    val bottomLabel: String,
    val sidebarTitle: String,
    val sidebarBody: String,
    val icon: DashboardNavIconKind,
)

private val DashboardNavItems = listOf(
    DashboardNavItem(
        section = DashboardSection.Dashboard,
        bottomLabel = "Home",
        sidebarTitle = "Command",
        sidebarBody = "Daily operations",
        icon = DashboardNavIconKind.Home,
    ),
    DashboardNavItem(
        section = DashboardSection.OrdersBookings,
        bottomLabel = "Orders",
        sidebarTitle = "Orders",
        sidebarBody = "Take, fulfil, dispatch",
        icon = DashboardNavIconKind.Orders,
    ),
    DashboardNavItem(
        section = DashboardSection.Customers,
        bottomLabel = "Customer",
        sidebarTitle = "Customers",
        sidebarBody = "Engagement records",
        icon = DashboardNavIconKind.Customers,
    ),
    DashboardNavItem(
        section = DashboardSection.Products,
        bottomLabel = "Product",
        sidebarTitle = "Catalog",
        sidebarBody = "Products and stock",
        icon = DashboardNavIconKind.Products,
    ),
    DashboardNavItem(
        section = DashboardSection.Account,
        bottomLabel = "Account",
        sidebarTitle = "Account",
        sidebarBody = "Logo and team",
        icon = DashboardNavIconKind.Account,
    ),
)

@Composable
private fun DashboardSectionContent(
    state: OnboardingUiState,
    roleLabel: String,
    selectedSection: DashboardSection,
    canInviteMembers: Boolean,
    actions: OnboardingActions,
    wide: Boolean,
) {
    when (selectedSection) {
        DashboardSection.Dashboard -> DashboardHomeContent(
            state = state,
            roleLabel = roleLabel,
            actions = actions,
            wide = wide,
        )
        DashboardSection.OrdersBookings -> DashboardOrdersContent(
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
        DashboardSection.Account -> DashboardAccountContent(
            state = state,
            actions = actions,
            roleLabel = roleLabel,
            canInviteMembers = canInviteMembers,
            wide = wide,
        )
    }
}

@Composable
private fun DashboardHomeContent(
    state: OnboardingUiState,
    roleLabel: String,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showOrderSheet by rememberSaveable { mutableStateOf(false) }
    var showCustomerSheet by rememberSaveable { mutableStateOf(false) }
    var showProductSheet by rememberSaveable { mutableStateOf(false) }

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
                DashboardOperationsCommandCard(
                    state = state,
                    onOrder = { showOrderSheet = true },
                    onCustomer = { showCustomerSheet = true },
                    onProduct = { showProductSheet = true },
                )
                DashboardBusinessSnapshotCard(state = state)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DashboardFulfillmentQueueCard(state = state)
                DashboardEngagementCard(state = state)
                DashboardWorkspaceCard(
                    state = state,
                    roleLabel = roleLabel,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DashboardOperationsCommandCard(
                state = state,
                onOrder = { showOrderSheet = true },
                onCustomer = { showCustomerSheet = true },
                onProduct = { showProductSheet = true },
            )
            DashboardBusinessSnapshotCard(state = state)
            DashboardFulfillmentQueueCard(state = state)
            DashboardEngagementCard(state = state)
            DashboardWorkspaceCard(
                state = state,
                roleLabel = roleLabel,
            )
        }
    }

    if (showOrderSheet) {
        OrderFormSheet(
            state = state,
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
private fun DashboardOperationsCommandCard(
    state: OnboardingUiState,
    onOrder: () -> Unit,
    onCustomer: () -> Unit,
    onProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.ScreenBackground,
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
                primaryText = if (state.dashboard.actionLoading) "Saving..." else "Take order",
                onPrimary = onOrder,
                primaryEnabled = !state.dashboard.actionLoading,
                secondaryText = "Add customer",
                onSecondary = onCustomer,
            )
            OrmaSecondaryButton(
                text = "Add product or service",
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
                contentColor = OrmaColors.ScreenBackground,
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
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
                label = "Orders and bookings",
                value = summary.ordersCount.toString(),
                detail = if (summary.bookingsCount == 0) "No scheduled bookings" else "${summary.bookingsCount} scheduled bookings",
            )
            DashboardMetricLine(
                label = "Catalog health",
                value = summary.productsInStock.toString(),
                detail = if (summary.lowStockProducts == 0) "No low-stock alerts" else "${summary.lowStockProducts} low-stock alerts",
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
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
    }
}

@Composable
private fun DashboardFulfillmentQueueCard(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val orders = state.dashboard.orders
    val openCount = orders.count { it.status in setOf("draft", "confirmed", "part_paid") }
    val paidCount = orders.count { it.status == "paid" }
    val completedCount = orders.count { it.status == "completed" }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrmaBadge(text = "FULFILMENT", tone = OrmaStatusTone.Warning)
            Text(
                text = "Work queue",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Use this to know what should be prepared, served, delivered, or closed.",
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
                detail = "Fulfilled orders and bookings",
            )
            if (orders.isEmpty()) {
                DashboardChecklistRow(text = "Take an order to start the live queue.")
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
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
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
) {
    var showOrderSheet by rememberSaveable { mutableStateOf(false) }
    DashboardListScaffold(
        eyebrow = "ORDERS",
        title = "Order counter",
        body = if (state.dashboard.orders.isEmpty()) {
            "Take the first order, booking, or service request."
        } else {
            "${state.dashboard.orders.size} records across order, payment, and fulfilment stages"
        },
        primaryText = "Take order",
        onPrimary = { showOrderSheet = true },
        loading = state.dashboard.loading,
    ) {
        if (wide) {
            DashboardModuleWorkspace(
                wide = true,
                primary = {
                    DashboardOrderRecords(state = state, actions = actions)
                },
                secondary = {
                    DashboardDispatchGuideCard(state = state)
                },
            )
        } else {
            DashboardDispatchGuideCard(state = state)
            DashboardOrderRecords(state = state, actions = actions)
        }
    }

    if (showOrderSheet) {
        OrderFormSheet(
            state = state,
            onDismiss = { showOrderSheet = false },
            onSubmit = { draft ->
                actions.onCreateOrder(draft)
                showOrderSheet = false
            },
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
    DashboardListScaffold(
        eyebrow = "CUSTOMERS",
        title = "Customer engagement",
        body = if (state.dashboard.customers.isEmpty()) {
            "Save customers so repeat orders, reminders, and service follow-up become possible."
        } else {
            "${state.dashboard.customers.size} customers available for orders and follow-up"
        },
        primaryText = "Add customer",
        onPrimary = { showCustomerSheet = true },
        loading = state.dashboard.loading,
    ) {
        if (wide) {
            DashboardModuleWorkspace(
                wide = true,
                primary = { DashboardCustomerRecords(state = state) },
                secondary = { DashboardCustomerEngagementGuideCard(state = state) },
            )
        } else {
            DashboardCustomerEngagementGuideCard(state = state)
            DashboardCustomerRecords(state = state)
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
}

@Composable
private fun DashboardProductsContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    wide: Boolean,
) {
    var showProductSheet by rememberSaveable { mutableStateOf(false) }
    var showSupplierSheet by rememberSaveable { mutableStateOf(false) }
    var stockProductId by rememberSaveable { mutableStateOf<String?>(null) }
    val stockProduct = state.dashboard.products.firstOrNull { it.id == stockProductId }

    DashboardListScaffold(
        eyebrow = "PRODUCTS",
        title = "Catalog and stock",
        body = if (state.dashboard.products.isEmpty()) {
            "Add products or services before creating itemized orders."
        } else {
            "${state.dashboard.products.size} sellable items with price and stock context"
        },
        primaryText = "Add item",
        onPrimary = { showProductSheet = true },
        secondaryText = "Add supplier",
        onSecondary = { showSupplierSheet = true },
        loading = state.dashboard.loading,
    ) {
        if (wide) {
            DashboardModuleWorkspace(
                wide = true,
                primary = {
                    DashboardProductRecords(
                        state = state,
                        onStockClick = { stockProductId = it.id },
                    )
                },
                secondary = {
                    DashboardCatalogGuideCard(state = state)
                },
            )
        } else {
            DashboardCatalogGuideCard(state = state)
            DashboardProductRecords(
                state = state,
                onStockClick = { stockProductId = it.id },
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
    if (showSupplierSheet) {
        SupplierFormSheet(
            onDismiss = { showSupplierSheet = false },
            onSubmit = { draft ->
                actions.onCreateSupplier(draft)
                showSupplierSheet = false
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
}

@Composable
private fun DashboardOrderRecords(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.dashboard.orders.isEmpty()) {
            DashboardEmptyModuleCard(
                icon = DashboardNavIconKind.Orders,
                title = "No live orders yet",
                body = "Take an order with customer, item, quantity, payment, and optional booking time.",
            )
        } else {
            state.dashboard.orders.forEach { order ->
                DashboardOrderRow(
                    order = order,
                    compact = false,
                    onStatusChange = { status -> actions.onUpdateOrderStatus(order.id, status) },
                )
            }
        }
    }
}

@Composable
private fun DashboardDispatchGuideCard(
    state: OnboardingUiState,
) {
    val openCount = state.dashboard.orders.count { it.status in setOf("draft", "confirmed", "part_paid") }
    val paidCount = state.dashboard.orders.count { it.status == "paid" }
    DashboardModuleChecklistCard(
        title = "Fulfilment flow",
        items = listOf(
            "Draft: order captured but not confirmed.",
            "Confirmed: business accepted the work.",
            "Paid: ready to prepare, serve, dispatch, or close.",
            "Completed: fulfilled work kept for reporting.",
        ),
        tertiaryText = when {
            openCount > 0 -> "$openCount open"
            paidCount > 0 -> "$paidCount ready"
            else -> null
        },
    )
}

@Composable
private fun DashboardCustomerRecords(
    state: OnboardingUiState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.dashboard.customers.isEmpty()) {
            DashboardEmptyModuleCard(
                icon = DashboardNavIconKind.Customers,
                title = "No customers yet",
                body = "Save contact details for repeat orders, delivery addresses, reminders, and notes.",
            )
        } else {
            state.dashboard.customers.forEach { customer ->
                DashboardCustomerRow(customer = customer)
            }
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
private fun DashboardProductRecords(
    state: OnboardingUiState,
    onStockClick: (OrmaProduct) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.dashboard.products.isEmpty()) {
            DashboardEmptyModuleCard(
                icon = DashboardNavIconKind.Products,
                title = "No catalog items yet",
                body = "Add products or services with price, tax, supplier, stock quantity, and reorder level.",
            )
        } else {
            state.dashboard.products.forEach { product ->
                DashboardProductRow(
                    product = product,
                    onStockClick = { onStockClick(product) },
                )
            }
        }
    }
}

@Composable
private fun DashboardCatalogGuideCard(
    state: OnboardingUiState,
) {
    DashboardModuleChecklistCard(
        title = "Catalog readiness",
        items = listOf(
            "Use products for retail, bakery, restaurant menu, salon services, or service packages.",
            "Track stock only for items that need inventory control.",
            "Attach suppliers when purchasing or restocking matters.",
        ),
        tertiaryText = when {
            state.dashboard.summary.lowStockProducts > 0 -> "${state.dashboard.summary.lowStockProducts} low-stock"
            state.dashboard.suppliers.isNotEmpty() -> "${state.dashboard.suppliers.size} suppliers"
            else -> null
        },
    )
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
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
        )
        OrmaActionRow(
            primaryText = if (loading) "Syncing..." else primaryText,
            onPrimary = onPrimary,
            primaryEnabled = !loading,
            secondaryText = secondaryText,
            onSecondary = onSecondary,
        )
        content()
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                text = "${product.stockQuantity} ${product.unit}",
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
private fun DashboardCustomerRow(customer: OrmaCustomer) {
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
                    text = listOfNotNull(customer.phoneNumber, customer.email).joinToString(" / ").ifBlank { "No contact added" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            customer.city?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        }
        customer.addressLine?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        customer.notes?.takeIf { it.isNotBlank() }?.let {
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

@Composable
private fun DashboardProductRow(
    product: OrmaProduct,
    onStockClick: (() -> Unit)?,
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
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOfNotNull(product.sku, product.supplierName).joinToString(" / ").ifBlank { product.unit },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = if (product.lowStock) "LOW" else "STOCK",
                tone = if (product.lowStock) OrmaStatusTone.Warning else OrmaStatusTone.Success,
            )
        }
        OrmaKeyValueList(
            rows = listOf(
                "Price" to dashboardMoney(product.sellingPrice, product.currency),
                "Stock" to "${product.stockQuantity} ${product.unit}",
                "Reorder" to product.reorderLevel,
            ),
        )
        onStockClick?.let {
            OrmaSecondaryButton(
                text = "Update stock",
                onClick = it,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DashboardOrderRow(
    order: OrmaOrder,
    compact: Boolean,
    onStatusChange: ((String) -> Unit)?,
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
            }
            OrmaBadge(
                text = order.status.dashboardStatusLabel(),
                tone = when (order.status) {
                    "paid", "completed" -> OrmaStatusTone.Success
                    "cancelled" -> OrmaStatusTone.Danger
                    else -> OrmaStatusTone.Info
                },
            )
        }
        OrmaKeyValueList(
            rows = listOf(
                "Total" to dashboardMoney(order.total, order.currency),
                "Paid" to dashboardMoney(order.paidTotal, order.currency),
                "Items" to order.itemCount.toString(),
            ),
        )
        if (!compact && onStatusChange != null) {
            OrmaSegmentedRow(
                options = DashboardOrderStatuses,
                selected = DashboardOrderStatuses.firstOrNull { it == order.status } ?: DashboardOrderStatuses.first(),
                label = { it.dashboardStatusLabel() },
                onSelected = onStatusChange,
            )
        }
    }
}

@Composable
private fun DashboardRecordCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardFormSheet(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 22.dp),
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
                        .clickable(onClick = onDismiss),
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

@Composable
private fun CustomerFormSheet(
    onDismiss: () -> Unit,
    onSubmit: (OrmaCustomerDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaCustomerDraft()) }
    DashboardFormSheet(
        title = "Add customer",
        body = "Save contact, address, and notes for orders, reminders, and follow-up.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.name, { draft = draft.copy(name = it) }, "Customer name", placeholder = "Full name")
        OrmaTextField(draft.phoneNumber, { draft = draft.copy(phoneNumber = it.filter { char -> char.isDigit() || char == '+' }.take(24)) }, "Phone number", placeholder = "+91 98765 43210", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        OrmaTextField(draft.email, { draft = draft.copy(email = it.take(160)) }, "Email", placeholder = "name@example.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OrmaTextField(draft.addressLine, { draft = draft.copy(addressLine = it) }, "Address", placeholder = "Building, street, area")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.city, { draft = draft.copy(city = it) }, "City", modifier = Modifier.weight(1f))
            OrmaTextField(draft.region, { draft = draft.copy(region = it) }, "State", modifier = Modifier.weight(1f))
        }
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Save customer",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.name.trim().length >= 2,
            secondaryText = "Cancel",
            onSecondary = onDismiss,
        )
    }
}

@Composable
private fun SupplierFormSheet(
    onDismiss: () -> Unit,
    onSubmit: (OrmaSupplierDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaSupplierDraft()) }
    DashboardFormSheet(
        title = "Add supplier",
        body = "Attach suppliers to catalog items and stock updates.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.name, { draft = draft.copy(name = it) }, "Supplier name", placeholder = "Company name")
        OrmaTextField(draft.phoneNumber, { draft = draft.copy(phoneNumber = it.filter { char -> char.isDigit() || char == '+' }.take(24)) }, "Phone number", placeholder = "+91 98765 43210", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        OrmaTextField(draft.email, { draft = draft.copy(email = it.take(160)) }, "Email", placeholder = "supplier@example.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OrmaTextField(draft.taxNumber, { draft = draft.copy(taxNumber = it.uppercase().take(24)) }, "GST/VAT number", placeholder = "Optional")
        OrmaTextField(draft.addressLine, { draft = draft.copy(addressLine = it) }, "Address", placeholder = "Optional")
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Save supplier",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.name.trim().length >= 2,
            secondaryText = "Cancel",
            onSecondary = onDismiss,
        )
    }
}

@Composable
private fun ProductFormSheet(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaProductDraft) -> Unit,
) {
    var draft by remember {
        mutableStateOf(
            OrmaProductDraft(
                currency = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
                pricesIncludeTax = state.draft.pricesIncludeTax,
            ),
        )
    }
    DashboardFormSheet(
        title = "Add product or service",
        body = "Create a sellable item for retail, menu, salon, booking, or service orders.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.name, { draft = draft.copy(name = it) }, "Product name", placeholder = "Item name")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.sku, { draft = draft.copy(sku = it.uppercase().take(40)) }, "SKU", modifier = Modifier.weight(1f), placeholder = "Optional")
            OrmaTextField(draft.unit, { draft = draft.copy(unit = it.take(12)) }, "Unit", modifier = Modifier.weight(1f), placeholder = "pcs")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.sellingPrice, { draft = draft.copy(sellingPrice = it.moneyInput()) }, "Selling price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OrmaTextField(draft.costPrice, { draft = draft.copy(costPrice = it.moneyInput()) }, "Cost price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(draft.stockQuantity, { draft = draft.copy(stockQuantity = it.signedMoneyInput()) }, "Opening stock", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OrmaTextField(draft.reorderLevel, { draft = draft.copy(reorderLevel = it.moneyInput()) }, "Reorder level", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        OrmaTextField(draft.taxRate, { draft = draft.copy(taxRate = it.moneyInput()) }, "Tax rate %", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        if (state.dashboard.suppliers.isNotEmpty()) {
            DashboardChipPicker(
                label = "Supplier",
                options = state.dashboard.suppliers,
                selectedId = draft.supplierId,
                optionId = { it.id },
                optionLabel = { it.name },
                onSelected = { draft = draft.copy(supplierId = it.id) },
            )
        }
        OrmaSwitchRow(
            title = "Track stock",
            body = "Show low-stock alerts and apply order stock movements.",
            checked = draft.trackStock,
            onCheckedChange = { draft = draft.copy(trackStock = it) },
        )
        OrmaActionRow(
            primaryText = "Save product",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.name.trim().length >= 2,
            secondaryText = "Cancel",
            onSecondary = onDismiss,
        )
    }
}

@Composable
private fun StockAdjustmentSheet(
    product: OrmaProduct,
    onDismiss: () -> Unit,
    onSubmit: (OrmaStockAdjustmentDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(OrmaStockAdjustmentDraft()) }
    DashboardFormSheet(
        title = "Update stock",
        body = "${product.name}: current stock ${product.stockQuantity} ${product.unit}.",
        onDismiss = onDismiss,
    ) {
        OrmaTextField(draft.quantityDelta, { draft = draft.copy(quantityDelta = it.signedMoneyInput()) }, "Adjustment", placeholder = "Use - for reduction", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OrmaTextField(draft.note, { draft = draft.copy(note = it) }, "Note", placeholder = "Opening correction, purchase, damage", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Apply stock",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.quantityDelta.trim().isNotBlank(),
            secondaryText = "Cancel",
            onSecondary = onDismiss,
        )
    }
}

@Composable
private fun OrderFormSheet(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
    onSubmit: (OrmaOrderDraft) -> Unit,
) {
    var draft by remember {
        mutableStateOf(
            OrmaOrderDraft(
                currency = state.dashboard.summary.currency.ifBlank { state.draft.currency.ifBlank { "INR" } },
            ),
        )
    }
    fun updateItem(index: Int, item: OrmaOrderItemDraft) {
        draft = draft.copy(items = draft.items.mapIndexed { itemIndex, old -> if (itemIndex == index) item else old })
    }
    DashboardFormSheet(
        title = "Take order or booking",
        body = "Capture customer, items, booking time, payment, and fulfilment status.",
        onDismiss = onDismiss,
    ) {
        if (state.dashboard.customers.isNotEmpty()) {
            DashboardChipPicker(
                label = "Customer",
                options = state.dashboard.customers,
                selectedId = draft.customerId,
                optionId = { it.id },
                optionLabel = { it.name },
                onSelected = { draft = draft.copy(customerId = it.id, customerName = "") },
            )
        }
        OrmaTextField(
            value = draft.customerName,
            onValueChange = { draft = draft.copy(customerName = it, customerId = "") },
            label = "Walk-in or new customer",
            placeholder = "Optional",
        )
        OrmaSegmentedRow(
            options = DashboardOrderStatuses,
            selected = draft.status,
            label = { it.dashboardStatusLabel() },
            onSelected = { draft = draft.copy(status = it) },
        )
        draft.items.forEachIndexed { index, item ->
            DashboardOrderItemEditor(
                index = index,
                item = item,
                products = state.dashboard.products,
                onChange = { updateItem(index, it) },
                onRemove = if (draft.items.size > 1) {
                    {
                        draft = draft.copy(items = draft.items.filterIndexed { itemIndex, _ -> itemIndex != index })
                    }
                } else {
                    null
                },
            )
        }
        OrmaSecondaryButton(
            text = "Add line",
            onClick = { draft = draft.copy(items = draft.items + OrmaOrderItemDraft()) },
            modifier = Modifier.fillMaxWidth(),
        )
        OrmaTextField(draft.scheduledAt, { draft = draft.copy(scheduledAt = it) }, "Booking time", placeholder = "Optional date/time")
        OrmaTextField(draft.paidTotal, { draft = draft.copy(paidTotal = it.moneyInput()) }, "Paid amount", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OrmaTextField(draft.notes, { draft = draft.copy(notes = it) }, "Notes", placeholder = "Optional", singleLine = false, minLines = 2)
        OrmaActionRow(
            primaryText = "Create order",
            onPrimary = { onSubmit(draft) },
            primaryEnabled = draft.items.any { it.description.isNotBlank() || it.productId.isNotBlank() },
            secondaryText = "Cancel",
            onSecondary = onDismiss,
        )
    }
}

@Composable
private fun DashboardOrderItemEditor(
    index: Int,
    item: OrmaOrderItemDraft,
    products: List<OrmaProduct>,
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
                label = "Product",
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
        OrmaTextField(item.description, { onChange(item.copy(description = it)) }, "Description", placeholder = "Product or service")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrmaTextField(item.quantity, { onChange(item.copy(quantity = it.moneyInput())) }, "Qty", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OrmaTextField(item.unitPrice, { onChange(item.copy(unitPrice = it.moneyInput())) }, "Price", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        OrmaTextField(item.taxRate, { onChange(item.copy(taxRate = it.moneyInput())) }, "Tax %", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val selected = optionId(option) == selectedId
                Surface(
                    modifier = Modifier.clickable { onSelected(option) },
                    shape = OrmaShapes.Capsule,
                    color = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                    contentColor = if (selected) OrmaColors.ScreenBackground else OrmaColors.Accent,
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

private fun String.dashboardStatusLabel(): String =
    when (this) {
        "draft" -> "Captured"
        "confirmed" -> "Confirmed"
        "part_paid" -> "Part paid"
        "paid" -> "Ready"
        "completed" -> "Done"
        "cancelled" -> "Cancelled"
        else -> replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

private fun dashboardMoney(amount: String, currency: String): String =
    "${currency.ifBlank { "INR" }} ${amount.ifBlank { "0.00" }}"

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

@Composable
private fun DashboardModuleWorkspace(
    wide: Boolean,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
) {
    if (wide) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.weight(1f)) { primary() }
            Box(modifier = Modifier.weight(1f)) { secondary() }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            primary()
            secondary()
        }
    }
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
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
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
                        color = OrmaColors.Accent,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            items.forEach { item ->
                DashboardChecklistRow(text = item)
            }
            tertiaryText?.let {
                OrmaBadge(
                    text = it.uppercase(),
                    tone = OrmaStatusTone.Info,
                )
            }
        }
    }
}

@Composable
private fun DashboardChecklistRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(7.dp),
            shape = OrmaShapes.Capsule,
            color = OrmaColors.Accent.copy(alpha = 0.42f),
            contentColor = OrmaColors.Accent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {}
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
    }
}

@Composable
private fun DashboardEmptyModuleCard(
    icon: DashboardNavIconKind,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        color = OrmaColors.Accent,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        }
    }
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

@Composable
private fun DashboardInviteMemberCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val inviteCode = state.teamInviteCode.trim()
    val workspaceName = state.workspaceName.ifBlank { state.draft.businessName }
    val inviteFormReady = state.teamInviteName.trim().length >= 2 && state.teamInviteContact.trim().isNotBlank()
    val actionText = when {
        state.inviteLoading -> "Creating..."
        else -> "Create invite"
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp),
        ) {
            OrmaBadge(
                text = "INVITE MEMBER",
                tone = OrmaStatusTone.Success,
            )
            Text(
                text = "Invite team member",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = "Invite by phone or email. When the team member signs in with that account, ORMA will show this business and finish their profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
            )

            OrmaTextField(
                value = state.teamInviteName,
                onValueChange = actions.onTeamInviteNameChange,
                label = "Team member name",
                placeholder = "Full name",
                enabled = !state.inviteLoading,
            )
            OrmaSegmentedRow(
                options = TeamInviteContactType.entries,
                selected = state.teamInviteContactType,
                label = {
                    when (it) {
                        TeamInviteContactType.Phone -> "Phone"
                        TeamInviteContactType.Email -> "Email"
                    }
                },
                onSelected = actions.onTeamInviteContactTypeChange,
            )
            OrmaTextField(
                value = state.teamInviteContact,
                onValueChange = actions.onTeamInviteContactChange,
                label = when (state.teamInviteContactType) {
                    TeamInviteContactType.Phone -> "Phone number"
                    TeamInviteContactType.Email -> "Email"
                },
                placeholder = when (state.teamInviteContactType) {
                    TeamInviteContactType.Phone -> "+91 98765 43210"
                    TeamInviteContactType.Email -> "name@business.com"
                },
                supportingText = when (state.teamInviteContactType) {
                    TeamInviteContactType.Phone -> "Include country code so ORMA can match the sign-in."
                    TeamInviteContactType.Email -> "The team member should sign in with this email."
                },
                enabled = !state.inviteLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (state.teamInviteContactType) {
                        TeamInviteContactType.Phone -> KeyboardType.Phone
                        TeamInviteContactType.Email -> KeyboardType.Email
                    },
                ),
            )
            OrmaSegmentedRow(
                options = DashboardTeamRoles,
                selected = DashboardTeamRoles.firstOrNull { it.id == state.teamInviteRole }
                    ?: DashboardTeamRoles.first(),
                label = { it.label },
                onSelected = { actions.onTeamInviteRoleChange(it.id) },
            )

            if (inviteCode.isBlank()) {
                Text(
                    text = if (state.inviteLoading) {
                        "Loading the active invite code for this workspace."
                    } else {
                        "No active invite code is available yet."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            } else {
                DashboardInviteCodePanel(
                    inviteCode = inviteCode,
                    workspaceName = workspaceName,
                    compact = compact,
                )
            }

            state.inviteErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                DashboardInviteStatusCard(
                    title = "Could not create invite",
                    body = message,
                    tone = OrmaStatusTone.Danger,
                )
            }

            if (state.inviteErrorMessage == null && state.inviteStatusMessage != null && inviteCode.isNotBlank()) {
                DashboardInviteStatusCard(
                    title = "Invite ready",
                    body = state.inviteStatusMessage,
                    tone = OrmaStatusTone.Success,
                )
            }

            OrmaActionRow(
                primaryText = actionText,
                onPrimary = actions.onCreateTeamInvite,
                primaryEnabled = !state.inviteLoading && inviteFormReady,
                secondaryText = "Load active code",
                onSecondary = actions.onRefreshTeamInvite,
            )
        }
    }
}

@Composable
private fun DashboardInviteCodePanel(
    inviteCode: String,
    workspaceName: String,
    compact: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Invite code",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
            )
            Text(
                text = inviteCode.chunked(4).joinToString(" "),
                style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HorizontalDivider(color = OrmaColors.Divider)
            OrmaKeyValueList(
                rows = listOf(
                    "Workspace" to workspaceName,
                    "Role" to "Team member",
                    "Flow" to "Sign in, then enter this code",
                ).filter { it.second.isNotBlank() },
            )
        }
    }
}

@Composable
private fun DashboardInviteStatusCard(
    title: String,
    body: String?,
    tone: OrmaStatusTone,
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
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.content,
            )
            body?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.content.copy(alpha = 0.78f),
                )
            }
        }
    }
}

private data class DashboardTeamRole(
    val id: String,
    val label: String,
)

private val DashboardTeamRoles = listOf(
    DashboardTeamRole("team_member", "Staff"),
    DashboardTeamRole("manager", "Manager"),
    DashboardTeamRole("cashier", "Cashier"),
    DashboardTeamRole("accountant", "Accountant"),
    DashboardTeamRole("inventory_manager", "Inventory"),
    DashboardTeamRole("sales_staff", "Sales"),
)

@Composable
private fun DashboardAccountContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    roleLabel: String,
    canInviteMembers: Boolean,
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
                DashboardAccountSessionCard(
                    roleLabel = roleLabel,
                    onLogout = actions.onRestart,
                )
                DashboardAccountLogoCard(state = state, actions = actions)
            }
            DashboardAccountTeamCard(
                state = state,
                actions = actions,
                canInviteMembers = canInviteMembers,
                compact = false,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DashboardAccountProfileCard(state = state, roleLabel = roleLabel)
            DashboardAccountSessionCard(
                roleLabel = roleLabel,
                onLogout = actions.onRestart,
            )
            DashboardAccountLogoCard(state = state, actions = actions)
            DashboardAccountTeamCard(
                state = state,
                actions = actions,
                canInviteMembers = canInviteMembers,
                compact = true,
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OrmaBadge(
            text = "LOGO",
            tone = OrmaStatusTone.Success,
        )
        Text(
            text = "Business logo",
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = "Used on invoices, estimates, and workspace documents.",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
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
private fun DashboardAccountTeamCard(
    state: OnboardingUiState,
    actions: OnboardingActions,
    canInviteMembers: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (canInviteMembers) {
        DashboardInviteMemberCard(
            state = state,
            actions = actions,
            compact = compact,
            modifier = modifier,
        )
    } else {
        DashboardEmptyModuleCard(
            icon = DashboardNavIconKind.Invite,
            title = "Owner access required",
            body = "Team members can view the workspace, but only the business owner can invite new users.",
            modifier = modifier,
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
                actions.onDraftChange(draft.copy(industry = industry))
                showIndustryPicker = false
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
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrmaTextField(
            value = value,
            onValueChange = onValueChange,
            label = "GST/VAT number",
            placeholder = "15-character GSTIN",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            supportingText = "Search starts automatically when the GSTIN is complete.",
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
        contentColor = if (enabled) OrmaColors.ScreenBackground else OrmaColors.TextDisabled,
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
                color = if (enabled) OrmaColors.ScreenBackground else OrmaColors.TextDisabled,
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
                    color = OrmaColors.Accent,
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
                    color = if (value.isBlank()) OrmaColors.TextSecondary else OrmaColors.Accent,
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
        contentColor = if (selected) OrmaColors.ScreenBackground else OrmaColors.Accent,
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
                        color = OrmaColors.Accent.copy(alpha = 0.72f),
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
        contentColor = if (selected || uploading) OrmaColors.Accent else OrmaColors.ScreenBackground,
        border = if (selected || uploading) BorderStroke(1.dp, OrmaColors.Hairline) else null,
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
                    shape = OrmaShapes.SmallCard,
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
                        Text(text = selectedCountry.dialCode, style = MaterialTheme.typography.bodyLarge, color = OrmaColors.Accent)
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
        textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = OrmaColors.Accent)),
        cursorBrush = SolidColor(OrmaColors.Accent),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.SmallCard,
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
                color = OrmaColors.Accent,
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            onDismiss = onDismiss,
            onSelect = onSelect,
        )
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
                    color = OrmaColors.Accent,
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
                            color = OrmaColors.Accent,
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
                color = OrmaColors.Accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.Accent,
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            onDismiss = onDismiss,
            onSelect = onSelect,
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
                    color = OrmaColors.Accent,
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
                            color = OrmaColors.Accent,
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
                color = OrmaColors.Accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.Accent,
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            onDismiss = onDismiss,
            onSelect = onSelect,
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
                    color = OrmaColors.Accent,
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
                            color = OrmaColors.Accent,
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
                color = OrmaColors.Accent,
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
                            color = OrmaColors.Accent.copy(alpha = 0.40f),
                        )
                    }
                    if (selected) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.Accent,
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
            color = OrmaColors.Accent.copy(alpha = 0.78f),
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
        textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = OrmaColors.Accent)),
        cursorBrush = SolidColor(OrmaColors.Accent),
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(OrmaShapes.CheckoutButton)
            .background(OrmaColors.Accent.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OrmaColors.Accent.copy(alpha = 0.32f),
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
        textStyle = TextStyle(color = Color.Transparent),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, OrmaColors.Hairline),
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
                    contentColor = OrmaColors.ScreenBackground,
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
            "Invite code" to state.teamInviteCode,
            "Role source" to "Existing workspace",
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
