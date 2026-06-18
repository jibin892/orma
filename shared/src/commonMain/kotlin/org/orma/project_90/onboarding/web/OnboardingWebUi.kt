package org.orma.project_90.onboarding.web

import androidx.compose.runtime.Composable
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingUiState
import org.orma.project_90.onboarding.desktop.OrmaOnboardingDesktopUi

@Composable
internal fun OrmaOnboardingWebUi(
    state: OnboardingUiState,
    actions: OnboardingActions,
    platformName: String,
) {
    OrmaOnboardingDesktopUi(
        state = state,
        actions = actions,
        platformName = platformName,
    )
}
