package org.orma.project_90.onboarding.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaGoogleBrandIcon
import org.orma.project_90.designsystem.OrmaOtpCells
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.onboarding.AccessPath
import org.orma.project_90.onboarding.AuthLoadingKind
import org.orma.project_90.onboarding.AuthIdentifierType
import org.orma.project_90.onboarding.CountryPickerSheet
import org.orma.project_90.onboarding.CountryFlagIcon
import org.orma.project_90.onboarding.OnboardingStageContent
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingStep
import org.orma.project_90.onboarding.OnboardingUiState
import org.orma.project_90.onboarding.OrmaCountryUi
import org.orma.project_90.onboarding.isOtpValid
import org.orma.project_90.onboarding.loginIdentifierError

@Composable
internal fun OrmaOnboardingDesktopUi(
    state: OnboardingUiState,
    actions: OnboardingActions,
    platformName: String,
    modifier: Modifier = Modifier,
) {
    when (state.step) {
        OnboardingStep.Authentication -> WebPhoneAuthenticationScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.Otp -> WebOtpVerificationScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.Owner -> WebOwnerProfileScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.Team -> WebTeamAccessScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.BusinessSetup -> WebBusinessSetupScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.Notification,
        OnboardingStep.Complete -> WebSharedWideStageScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
        OnboardingStep.Dashboard -> WebDashboardScreen(
            state = state,
            actions = actions,
            modifier = modifier,
        )
    }
}

@Composable
private fun WebOwnerProfileScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Create the workspace owner",
                    style = MaterialTheme.typography.displayLarge,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Add the person responsible for setup and workspace administration.",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "OWNER NAME",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextSecondary,
                )
                WebTextInputField(
                    value = state.draft.ownerName,
                    onValueChange = { actions.onDraftChange(state.draft.copy(ownerName = it.take(80))) },
                    placeholder = "Full name",
                    enabled = !state.onboardingLoading,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WebSendCodeButton(
                    text = "Continue",
                    enabled = state.ownerReady && !state.onboardingLoading,
                    onClick = actions.onContinue,
                )
                WebTextAuthButton(
                    text = "Back",
                    enabled = !state.onboardingLoading,
                    onClick = actions.onBack,
                )
                WebTextAuthButton(
                    text = "Join existing workspace",
                    enabled = !state.onboardingLoading,
                    onClick = { actions.onAccessPathChange(AccessPath.TeamMember) },
                )
            }
        }
    }
}

@Composable
private fun WebTeamAccessScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    val hasMatchedInvite = state.workspaceId.isNotBlank() && state.teamInviteCode.isNotBlank()
    val contactLabel = state.pendingInviteEmail
        .ifBlank { state.pendingInvitePhoneNumber }
        .ifBlank { state.identifier.trim() }
        .ifBlank { "Signed-in account" }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (hasMatchedInvite) "Complete team profile" else "Join your workspace",
                    style = MaterialTheme.typography.displayLarge,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (hasMatchedInvite) {
                        "Confirm the invited business and add the name your team will see."
                    } else {
                        "Enter the invite code from the business owner, then finish your team profile."
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            if (hasMatchedInvite) {
                WebInviteSummaryCard(
                    rows = buildList {
                        add("Business" to state.workspaceName.ifBlank { "Workspace" })
                        state.workspaceLegalName.takeIf { it.isNotBlank() }?.let { add("Legal name" to it) }
                        add("Role" to webTeamRoleLabel(state.pendingInviteRole.ifBlank { "team_member" }))
                        add("Invited account" to contactLabel)
                    },
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "INVITE CODE",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextSecondary,
                    )
                    WebTextInputField(
                        value = state.teamInviteCode,
                        onValueChange = actions.onTeamInviteCodeChange,
                        placeholder = "Code from owner",
                        enabled = !state.onboardingLoading,
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "TEAM PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextSecondary,
                )
                WebTextInputField(
                    value = state.teamProfileName,
                    onValueChange = actions.onTeamProfileNameChange,
                    placeholder = state.teamProfileName.ifBlank { "Full name" },
                    enabled = !state.onboardingLoading,
                )
            }

            val inviteErrorMessage = state.inviteErrorMessage
            if (!inviteErrorMessage.isNullOrBlank()) {
                Text(
                    text = inviteErrorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrmaColors.Error,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WebSendCodeButton(
                    text = if (state.onboardingLoading) "Joining..." else "Continue",
                    enabled = !state.onboardingLoading && state.teamProfileReady,
                    onClick = actions.onContinue,
                )
                WebTextAuthButton(
                    text = "Set up a business instead",
                    enabled = !state.onboardingLoading,
                    onClick = actions.onCreateBusiness,
                )
            }
        }
    }
}

@Composable
private fun WebInviteSummaryCard(
    rows: List<Pair<String, String>>,
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
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.first,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextSecondary,
                    )
                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextPrimary,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

private fun webTeamRoleLabel(role: String): String = when (role) {
    "manager" -> "Manager"
    "cashier" -> "Cashier"
    "accountant" -> "Accountant"
    "inventory_manager" -> "Inventory"
    "sales_staff" -> "Sales"
    else -> "Staff"
}

@Composable
private fun WebSharedWideStageScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 1180.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 36.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OnboardingStageContent(
                state = state,
                actions = actions,
                wide = true,
            )
        }
    }
}

@Composable
private fun WebDashboardScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        OnboardingStageContent(
            state = state,
            actions = actions,
            wide = true,
        )
    }
}

@Composable
private fun WebPhoneAuthenticationScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.displayLarge,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Enter your phone number - we'll send you a verification code.",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "PHONE NUMBER",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    WebCountrySelector(
                        country = state.selectedCountry,
                        onClick = { showCountryPicker = true },
                    )
                    WebPhoneTextField(
                        value = state.identifier,
                        onValueChange = actions.onIdentifierChange,
                        placeholder = state.selectedCountry.placeholder,
                        enabled = !state.isAuthLoading,
                        modifier = Modifier.weight(1f),
                    )
                }
                fieldError?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.Error,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                WebSendCodeButton(
                    text = if (state.authLoadingKind == AuthLoadingKind.SendingOtp) {
                        "Sending..."
                    } else {
                        "Send code"
                    },
                    enabled = state.loginReady && !state.isAuthLoading,
                    onClick = actions.onContinue,
                )
                WebAuthDivider()
                WebGoogleAuthButton(
                    text = if (state.authLoadingKind == AuthLoadingKind.SigningInGoogle) {
                        "Signing in..."
                    } else {
                        "Continue with Google"
                    },
                    enabled = !state.isAuthLoading,
                    onClick = actions.onGoogleSignIn,
                )
            }

            Text(
                text = "By continuing you agree to ORMA's Terms & Privacy Policy.",
                style = MaterialTheme.typography.bodyLarge,
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
private fun WebOtpVerificationScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.otpCode, state.isAuthLoading) {
        if (isOtpValid(state.otpCode) && !state.isAuthLoading) {
            delay(300)
            actions.onContinue()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Enter verification code",
                    style = MaterialTheme.typography.displayLarge,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "We sent a 6-digit code to ${state.webOtpDestinationLabel()}.",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "VERIFICATION CODE",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextSecondary,
                )
                WebOtpCodeField(
                    code = state.otpCode,
                    onCodeChange = actions.onOtpChange,
                    enabled = !state.isAuthLoading,
                )
                state.authStatusMessage?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextSecondary,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                WebSendCodeButton(
                    text = when (state.authLoadingKind) {
                        AuthLoadingKind.VerifyingOtp -> "Verifying..."
                        AuthLoadingKind.SendingOtp -> "Sending..."
                        else -> "Verify code"
                    },
                    enabled = isOtpValid(state.otpCode) && !state.isAuthLoading,
                    onClick = actions.onContinue,
                )
                WebTextAuthButton(
                    text = "Change phone number",
                    enabled = !state.isAuthLoading,
                    onClick = actions.onBack,
                )
                WebTextAuthButton(
                    text = "Resend code",
                    enabled = !state.isAuthLoading,
                    onClick = actions.onResendOtp,
                )
            }

            Text(
                text = "Keep this page open while ORMA verifies your access.",
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WebBusinessSetupScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground)
            .safeContentPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 1180.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 36.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OnboardingStageContent(
                state = state,
                actions = actions,
                wide = true,
            )
        }
    }
}

@Composable
private fun WebCountrySelector(
    country: OrmaCountryUi,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = OrmaShapes.Field,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountryFlagIcon(country = country)
            Text(
                text = country.dialCode,
                style = MaterialTheme.typography.headlineMedium,
                color = OrmaColors.TextPrimary,
            )
            WebChevronDown()
        }
    }
}

@Composable
private fun WebOtpCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = OrmaShapes.Field,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BasicTextField(
            value = code,
            onValueChange = { onCodeChange(it.filter(Char::isDigit).take(6)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .focusRequester(focusRequester),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = MaterialTheme.typography.headlineMedium.merge(
                TextStyle(color = Color.Transparent),
            ),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    OrmaOtpCells(code = code)
                }
            },
        )
    }
}

@Composable
private fun WebPhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = OrmaShapes.Field,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = MaterialTheme.typography.headlineMedium.merge(
                TextStyle(color = OrmaColors.TextPrimary),
            ),
            cursorBrush = SolidColor(OrmaColors.Accent),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.headlineMedium,
                            color = OrmaColors.TextSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun WebTextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = OrmaShapes.Field,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            textStyle = MaterialTheme.typography.headlineMedium.merge(
                TextStyle(color = OrmaColors.TextPrimary),
            ),
            cursorBrush = SolidColor(OrmaColors.Accent),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.headlineMedium,
                            color = OrmaColors.TextSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun WebSendCodeButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.35f),
        contentColor = OrmaColors.ScreenBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.ScreenBackground,
            )
        }
    }
}

@Composable
private fun WebTextAuthButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WebAuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(OrmaColors.Hairline),
        )
        Text(
            text = "or",
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(OrmaColors.Hairline),
        )
    }
}

@Composable
private fun WebGoogleAuthButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaGoogleBrandIcon(
                enabled = enabled,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            )
        }
    }
}

@Composable
private fun WebChevronDown() {
    Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(
            color = OrmaColors.TextSecondary,
            start = Offset(1.dp.toPx(), 2.dp.toPx()),
            end = Offset(size.width / 2f, size.height - 2.dp.toPx()),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = OrmaColors.TextSecondary,
            start = Offset(size.width - 1.dp.toPx(), 2.dp.toPx()),
            end = Offset(size.width / 2f, size.height - 2.dp.toPx()),
            strokeWidth = strokeWidth,
        )
    }
}

private fun OnboardingUiState.webOtpDestinationLabel(): String =
    if (identifierType == AuthIdentifierType.Phone) {
        "${selectedCountry.dialCode} ${identifier.webMaskedPhone()}"
    } else {
        identifier.trim()
    }

private fun String.webMaskedPhone(): String {
    val digits = filter(Char::isDigit)
    return if (digits.length <= 4) digits else "**** ${digits.takeLast(4)}"
}
