package org.orma.project_90.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.orma.project_90.designsystem.OrmaActionRow
import org.orma.project_90.designsystem.OrmaBadge
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
import org.orma.project_90.designsystem.OrmaUploadRow

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
        OnboardingStep.Notification -> NotificationPermissionStage(state = state, actions = actions)
        OnboardingStep.Complete -> CompleteStage(state = state, actions = actions)
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
            AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)
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
private fun AuthAlertCard(
    state: OnboardingUiState,
    onDismiss: () -> Unit,
) {
    val title = state.authErrorTitle
    val message = state.authErrorMessage
    val success = state.authStatusMessage
    when {
        success != null && title == null && message == null -> AuthAlertSurface(
            title = "Firebase connected",
            message = success,
            code = null,
            tone = OrmaStatusTone.Success,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun AuthAlertSurface(
    title: String,
    message: String,
    code: String?,
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.content,
                )
                OrmaTextButton(
                    text = "Dismiss",
                    onClick = onDismiss,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.content.copy(alpha = 0.78f),
            )
            code?.let {
                OrmaBadge(
                    text = it,
                    tone = tone,
                )
            }
        }
    }
}

@Composable
private fun OwnerStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    OrmaScreenColumn(
        eyebrow = "Owner profile",
        title = "Create the admin owner",
        body = "This user controls setup, billing, tax, invoices, and team access.",
    ) {
        OrmaFormCard {
            OrmaTextField(
                value = state.draft.ownerName,
                onValueChange = { actions.onDraftChange(state.draft.copy(ownerName = it)) },
                label = "Owner name",
                placeholder = "Full name",
            )
            OrmaKeyValueList(
                rows = listOf(
                    "Role" to "Workspace owner",
                    "Access" to "All setup modules",
                    "Login" to state.identifier.trim(),
                ),
            )
        }
        AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)
        OrmaActionRow(
            secondaryText = "Back",
            onSecondary = actions.onBack,
            primaryText = "Continue",
            onPrimary = actions.onContinue,
            primaryEnabled = state.ownerReady,
        )
    }
}

@Composable
private fun TeamStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    OrmaScreenColumn(
        eyebrow = "Team access",
        title = "Join workspace",
        body = "Your role and permissions come from the business owner after invite verification.",
    ) {
        OrmaFormCard {
            if (state.workspaceId.isBlank()) {
                OrmaTextField(
                    value = state.teamInviteCode,
                    onValueChange = actions.onTeamInviteCodeChange,
                    label = "Invite code",
                    placeholder = "Code from owner",
                )
            }
            OrmaKeyValueList(
                rows = listOf(
                    "Signed in as" to state.identifier.trim(),
                    "Workspace" to state.workspaceName.ifBlank { "Invite required" },
                    "Invite code" to state.teamInviteCode.trim().ifBlank { "Not linked" },
                    "Role source" to "Existing workspace",
                ),
            )
        }
        AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)
        OrmaActionRow(
            secondaryText = "Back",
            onSecondary = actions.onBack,
            primaryText = if (state.onboardingLoading) "Joining..." else "Finish",
            onPrimary = actions.onContinue,
            primaryEnabled = !state.onboardingLoading && (state.workspaceId.isNotBlank() || state.teamInviteCode.isNotBlank()),
        )
        OrmaTextButton(
            text = "Set up a business instead",
            onClick = actions.onCreateBusiness,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
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
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Set up your business",
                style = MaterialTheme.typography.displayMedium,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Step ${currentIndex + 1} of ${steps.size} - ${state.setupStep.title}",
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = state.setupStep.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        SetupProgressLine(currentIndex = currentIndex, total = steps.size)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.setupStep.title.uppercase(),
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextSecondary,
            )
            BusinessSetupForm(state = state, actions = actions)
        }

        AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "STEP ${currentIndex + 1} OF ${steps.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextSecondary,
                    )
                    Text(
                        text = state.setupStep.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = OrmaColors.TextPrimary,
                    )
                    Text(
                        text = state.setupStep.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextSecondary,
                    )
                }
                BusinessSetupForm(state = state, actions = actions)
                AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)
                SetupFlowActions(
                    currentIndex = currentIndex,
                    steps = steps,
                    state = state,
                    actions = actions,
                    large = true,
                )
            }

            BusinessSetupSidePanel(
                state = state,
                steps = steps,
                currentIndex = currentIndex,
                onStepSelected = actions.onSetupStepChange,
                modifier = Modifier.width(330.dp),
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
private fun BusinessSetupSidePanel(
    state: OnboardingUiState,
    steps: List<BusinessSetupStep>,
    currentIndex: Int,
    onStepSelected: (BusinessSetupStep) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.Field,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Setup progress",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = "${currentIndex + 1} of ${steps.size} sections",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { index, step ->
                    SetupRailItem(
                        index = index,
                        step = step,
                        active = index == currentIndex,
                        complete = index < currentIndex,
                        onClick = { onStepSelected(step) },
                    )
                }
            }

            HorizontalDivider(color = OrmaColors.Hairline)

            SetupDraftSummary(state = state)
        }
    }
}

@Composable
private fun SetupRailItem(
    index: Int,
    step: BusinessSetupStep,
    active: Boolean,
    complete: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = OrmaShapes.SmallCard,
        color = if (active) OrmaColors.CellBackground else Color.Transparent,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (active) OrmaColors.Accent.copy(alpha = 0.18f) else Color.Transparent,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (active || complete) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (active || complete) OrmaColors.ScreenBackground else OrmaColors.TextSecondary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active || complete) OrmaColors.ScreenBackground else OrmaColors.TextSecondary,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) OrmaColors.TextPrimary else OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = setupStepShortLabel(step),
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SetupDraftSummary(state: OnboardingUiState) {
    val draft = state.draft
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Workspace draft",
            style = MaterialTheme.typography.titleSmall,
            color = OrmaColors.TextPrimary,
        )
        SetupSummaryRow("Business", draft.businessName.ifBlank { "Not added" })
        SetupSummaryRow("Legal", draft.legalName.ifBlank { "Not added" })
        SetupSummaryRow("Tax", if (draft.isTaxRegistered) draft.taxLabel else "Not registered")
        SetupSummaryRow("Currency", draft.currency)
        SetupSummaryRow("Invoice", "${draft.invoicePrefix}-${draft.nextInvoiceNumber}")
    }
}

@Composable
private fun SetupSummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(78.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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

private fun setupStepShortLabel(step: BusinessSetupStep): String = when (step) {
    BusinessSetupStep.BusinessDetails -> "Name and industry"
    BusinessSetupStep.TaxDetails -> "GST/VAT"
    BusinessSetupStep.Address -> "Registered address"
    BusinessSetupStep.Logo -> "Branding"
    BusinessSetupStep.InvoiceSettings -> "Numbering"
    BusinessSetupStep.CurrencyTax -> "Currency"
}

@Composable
private fun NotificationPermissionStage(
    state: OnboardingUiState,
    actions: OnboardingActions,
) {
    var showPermissionAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showPermissionAnimation = true
    }

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
            AuthAlertCard(state = state, onDismiss = actions.onClearAuthAlert)
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
                        text = if (state.onboardingLoading) "Saving..." else "Continue",
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
    SelectorBlock(
        title = "Industry",
        options = OrmaSupportedIndustries,
        selected = draft.industry,
        label = { it },
        onSelected = { actions.onDraftChange(draft.copy(industry = it)) },
    )
    OrmaTextField(
        value = draft.website,
        onValueChange = { actions.onDraftChange(draft.copy(website = it)) },
        label = "Website",
        placeholder = "Optional",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )
}

@Composable
private fun TaxDetailsForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OrmaChoiceSurface(
            title = "GST/VAT registered",
            body = "Show tax identity on invoices and billing documents.",
            selected = draft.isTaxRegistered,
            onClick = { actions.onDraftChange(draft.copy(isTaxRegistered = true)) },
        )
        OrmaChoiceSurface(
            title = "Not registered",
            body = "Continue without a tax registration number.",
            selected = !draft.isTaxRegistered,
            onClick = { actions.onDraftChange(draft.copy(isTaxRegistered = false, taxNumber = "")) },
        )
    }
    OrmaTextField(
        value = draft.taxLabel,
        onValueChange = { actions.onDraftChange(draft.copy(taxLabel = it)) },
        label = "Tax label",
        placeholder = "GST/VAT",
    )
    AnimatedVisibility(draft.isTaxRegistered) {
        OrmaTextField(
            value = draft.taxNumber,
            onValueChange = { actions.onDraftChange(draft.copy(taxNumber = it)) },
            label = "GST/VAT number",
            placeholder = "Registration number",
        )
    }
}

@Composable
private fun AddressForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
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
    OrmaTextField(
        value = draft.region,
        onValueChange = { actions.onDraftChange(draft.copy(region = it)) },
        label = "State or emirate",
        placeholder = "Optional",
    )
    OrmaTextField(
        value = draft.country,
        onValueChange = { actions.onDraftChange(draft.copy(country = it)) },
        label = "Country",
        placeholder = "Country",
    )
    OrmaTextField(
        value = draft.postalCode,
        onValueChange = { actions.onDraftChange(draft.copy(postalCode = it)) },
        label = "Postal code",
        placeholder = "Optional",
    )
}

@Composable
private fun LogoForm(state: OnboardingUiState, actions: OnboardingActions) {
    val draft = state.draft
    OrmaUploadRow(
        title = draft.logoFileName.ifBlank { "Upload logo" },
        body = "Used on invoices, estimates, and business documents.",
        initials = businessInitial(draft),
        selected = draft.logoFileName.isNotBlank(),
        onClick = { actions.onDraftChange(draft.copy(logoFileName = "orma-business-logo.png")) },
    )
    AnimatedVisibility(draft.logoFileName.isNotBlank()) {
        OrmaSecondaryButton(
            text = "Remove logo",
            onClick = { actions.onDraftChange(draft.copy(logoFileName = "")) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

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
                        Text(
                            text = "v",
                            style = MaterialTheme.typography.labelMedium,
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
internal fun CountryPickerSheet(
    selectedCountry: OrmaCountryUi,
    onDismiss: () -> Unit,
    onSelect: (OrmaCountryUi) -> Unit,
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
        )
    }
}

@Composable
private fun CountryPickerSheetContent(
    selectedCountry: OrmaCountryUi,
    onDismiss: () -> Unit,
    onSelect: (OrmaCountryUi) -> Unit,
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
                    text = "Country code",
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
                        Text(text = "X", style = MaterialTheme.typography.labelLarge)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = country.dialCode,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrmaColors.Accent.copy(alpha = 0.40f),
                )
                if (selected) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.Accent,
                    )
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
            else -> CountryCodeFlagFallback(country.id)
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
private fun CountryCodeFlagFallback(countryId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrmaColors.Accent.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = countryId.take(2),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.Accent,
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
