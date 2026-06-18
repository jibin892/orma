package org.orma.project_90.onboarding.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.orma.project_90.designsystem.OrmaBrandRow
import org.orma.project_90.designsystem.OrmaMobileShell
import org.orma.project_90.onboarding.OnboardingActions
import org.orma.project_90.onboarding.OnboardingStageContent
import org.orma.project_90.onboarding.OnboardingStep
import org.orma.project_90.onboarding.OnboardingUiState

@Composable
internal fun OrmaOnboardingMobileUi(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    OrmaMobileShell(
        modifier = modifier,
        header = {
            if (state.step == OnboardingStep.Authentication) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrmaBrandRow()
                }
            }
        },
        content = {
            OnboardingStageContent(
                state = state,
                actions = actions,
                wide = false,
            )
        },
    )
}
