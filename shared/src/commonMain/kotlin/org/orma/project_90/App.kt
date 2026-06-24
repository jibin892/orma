package org.orma.project_90

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.orma.project_90.onboarding.feature.OrmaOnboardingFlow
import org.orma.project_90.publiccatalog.OrmaPublicCatalogFlow
import org.orma.project_90.publiccatalog.currentOrmaPublicCatalogWorkspaceId

@Composable
@Preview
fun App() {
    val publicCatalogWorkspaceId = currentOrmaPublicCatalogWorkspaceId()
    if (publicCatalogWorkspaceId != null) {
        OrmaPublicCatalogFlow(workspaceId = publicCatalogWorkspaceId)
    } else {
        OrmaOnboardingFlow()
    }
}
