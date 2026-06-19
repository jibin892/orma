package org.orma.project_90.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.OrmaTextButton
import org.orma.project_90.designsystem.ormaStatusColors

@Composable
internal fun OrmaAuthFeedbackDialog(
    state: OnboardingUiState,
    onDismissError: () -> Unit,
) {
    val errorTitle = state.authErrorTitle
    val errorMessage = state.authErrorMessage
    when {
        state.isAuthLoading && state.authLoadingKind != AuthLoadingKind.VerifyingOtp -> {
            OrmaAuthLoadingDialog(kind = state.authLoadingKind)
        }
        errorTitle != null && errorMessage != null -> OrmaAuthErrorDialog(
            title = errorTitle,
            message = errorMessage,
            code = state.authErrorCode,
            onDismiss = onDismissError,
        )
    }
}

@Composable
private fun OrmaAuthLoadingDialog(kind: AuthLoadingKind) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = OrmaShapes.Sheet,
            color = OrmaColors.CardBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(1.dp, OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(34.dp),
                    color = OrmaColors.Accent,
                    strokeWidth = 3.dp,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = kind.dialogTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = kind.dialogBody(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrmaAuthErrorDialog(
    title: String,
    message: String,
    code: String?,
    onDismiss: () -> Unit,
) {
    val colors = ormaStatusColors(OrmaStatusTone.Danger)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp),
            shape = OrmaShapes.Sheet,
            color = OrmaColors.CardBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(1.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.content,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OrmaColors.TextPrimary.copy(alpha = 0.72f),
                    )
                }
                code?.takeIf { it.isNotBlank() }?.let {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OrmaBadge(
                            text = it,
                            tone = OrmaStatusTone.Danger,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrmaTextButton(
                        text = "OK",
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

private fun AuthLoadingKind.dialogTitle(): String = when (this) {
    AuthLoadingKind.RestoringSession -> "Opening workspace"
    AuthLoadingKind.SendingOtp -> "Sending verification code"
    AuthLoadingKind.VerifyingOtp -> "Verifying OTP"
    AuthLoadingKind.SigningInEmail -> "Signing in"
    AuthLoadingKind.SigningInGoogle -> "Opening Google sign-in"
    AuthLoadingKind.ResolvingWorkspace -> "Checking workspace"
    AuthLoadingKind.SigningOut -> "Signing out"
    AuthLoadingKind.None -> "Checking access"
}

private fun AuthLoadingKind.dialogBody(): String = when (this) {
    AuthLoadingKind.RestoringSession -> "ORMA is checking your saved sign-in and workspace access."
    AuthLoadingKind.SendingOtp -> "Keep this screen open while ORMA prepares the OTP session."
    AuthLoadingKind.VerifyingOtp -> "ORMA is checking the six-digit code and signing you in."
    AuthLoadingKind.SigningInEmail -> "ORMA is checking your account and workspace access."
    AuthLoadingKind.SigningInGoogle -> "Choose the Google account connected to this ORMA workspace."
    AuthLoadingKind.ResolvingWorkspace -> "Google sign-in is complete. ORMA is opening the right workspace for this account."
    AuthLoadingKind.SigningOut -> "ORMA is clearing this device session before returning to sign-in."
    AuthLoadingKind.None -> "This should only take a moment."
}
