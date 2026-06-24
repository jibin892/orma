package org.orma.project_90.onboarding

import org.orma.project_90.backend.OrmaCustomer
import org.orma.project_90.backend.OrmaCustomerDraft
import org.orma.project_90.backend.OrmaDashboardFilters
import org.orma.project_90.backend.OrmaDashboardSummary
import org.orma.project_90.backend.OrmaGstinLookup
import org.orma.project_90.backend.OrmaMetaConnectionDraft
import org.orma.project_90.backend.OrmaMetaConnectionStatus
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderDraft
import org.orma.project_90.backend.OrmaPagination
import org.orma.project_90.backend.OrmaPrinterDraft
import org.orma.project_90.backend.OrmaPrinterProfile
import org.orma.project_90.backend.OrmaProduct
import org.orma.project_90.backend.OrmaProductCategory
import org.orma.project_90.backend.OrmaProductCategoryDraft
import org.orma.project_90.backend.OrmaProductDraft
import org.orma.project_90.backend.OrmaProductExport
import org.orma.project_90.backend.OrmaProductImportTemplate
import org.orma.project_90.backend.OrmaProductImportResult
import org.orma.project_90.backend.OrmaProductOffer
import org.orma.project_90.backend.OrmaProductOfferDraft
import org.orma.project_90.backend.OrmaStockAdjustmentDraft
import org.orma.project_90.backend.OrmaSupplier
import org.orma.project_90.backend.OrmaSupplierDraft
import org.orma.project_90.backend.OrmaTeamMember
import org.orma.project_90.backend.OrmaWorkspacePaymentMethod
import org.orma.project_90.backend.OrmaWorkspacePaymentMethodDraft
import org.orma.project_90.media.OrmaPickedImage

internal enum class OnboardingStep {
    Authentication,
    Otp,
    Owner,
    Team,
    BusinessSetup,
    Notification,
    Complete,
    Dashboard,
}

internal enum class AuthLoadingKind {
    None,
    RestoringSession,
    SendingOtp,
    VerifyingOtp,
    SigningInEmail,
    SigningInGoogle,
    ResolvingWorkspace,
    SigningOut,
}

internal enum class DashboardPageTarget {
    Orders,
    Customers,
    Products,
}

internal data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Authentication,
    val authProvider: AuthProvider = AuthProvider.PhoneOtp,
    val identifierType: AuthIdentifierType = AuthIdentifierType.Phone,
    val countryId: String = OrmaDefaultCountry.id,
    val identifier: String = "",
    val password: String = "",
    val accessPath: AccessPath = AccessPath.BusinessOwner,
    val otpCode: String = "",
    val authUserId: String = "",
    val authIdToken: String = "",
    val workspaceId: String = "",
    val workspaceName: String = "",
    val workspaceLegalName: String = "",
    val workspaceLogoFileName: String = "",
    val workspaceLogoUrl: String = "",
    val workspaceCoverFileName: String = "",
    val workspaceCoverUrl: String = "",
    val authStatusMessage: String? = null,
    val authErrorTitle: String? = null,
    val authErrorMessage: String? = null,
    val authErrorCode: String? = null,
    val authLoadingKind: AuthLoadingKind = AuthLoadingKind.None,
    val onboardingLoading: Boolean = false,
    val inviteLoading: Boolean = false,
    val inviteStatusMessage: String? = null,
    val inviteErrorMessage: String? = null,
    val teamProfileName: String = "",
    val logoUploadLoading: Boolean = false,
    val coverUploadLoading: Boolean = false,
    val gstinLookupLoading: Boolean = false,
    val gstinLookupNumber: String = "",
    val gstinLookupStatusMessage: String? = null,
    val gstinLookupErrorMessage: String? = null,
    val setupStep: BusinessSetupStep = BusinessSetupStep.TaxDetails,
    val notificationsEnabled: Boolean = false,
    val draft: BusinessSetupDraft = BusinessSetupDraft(),
    val dashboard: DashboardDataState = DashboardDataState(),
) {
    val isAuthLoading: Boolean
        get() = authLoadingKind != AuthLoadingKind.None

    val selectedCountry: OrmaCountryUi
        get() = ormaCountryById(countryId)

    val loginReady: Boolean = when (identifierType) {
        AuthIdentifierType.Phone -> isLoginIdentifierValid(AuthIdentifierType.Phone, identifier, selectedCountry)
        AuthIdentifierType.Email -> isLoginIdentifierValid(AuthIdentifierType.Email, identifier) && password.length >= 6
    }

    val ownerReady: Boolean = draft.ownerName.isNotBlank()

    val setupReady: Boolean = canContinueBusinessSetup(setupStep, draft)

    val currentStage: Int = when (step) {
        OnboardingStep.Authentication -> 1
        OnboardingStep.Otp -> 2
        OnboardingStep.Owner,
        OnboardingStep.Team -> 3
        OnboardingStep.BusinessSetup -> 4
        OnboardingStep.Notification -> 5
        OnboardingStep.Complete,
        OnboardingStep.Dashboard -> 6
    }
}

internal data class DashboardDataState(
    val hasLoaded: Boolean = false,
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val errorTitle: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val summary: OrmaDashboardSummary = OrmaDashboardSummary(),
    val customers: List<OrmaCustomer> = emptyList(),
    val suppliers: List<OrmaSupplier> = emptyList(),
    val categories: List<OrmaProductCategory> = emptyList(),
    val offers: List<OrmaProductOffer> = emptyList(),
    val products: List<OrmaProduct> = emptyList(),
    val orders: List<OrmaOrder> = emptyList(),
    val customerPagination: OrmaPagination = OrmaPagination(),
    val supplierPagination: OrmaPagination = OrmaPagination(),
    val categoryPagination: OrmaPagination = OrmaPagination(),
    val offerPagination: OrmaPagination = OrmaPagination(),
    val productPagination: OrmaPagination = OrmaPagination(),
    val orderPagination: OrmaPagination = OrmaPagination(),
    val printerPagination: OrmaPagination = OrmaPagination(),
    val paymentMethodPagination: OrmaPagination = OrmaPagination(),
    val customerOrderHistory: Map<String, List<OrmaOrder>> = emptyMap(),
    val customerOrderHistoryPagination: Map<String, OrmaPagination> = emptyMap(),
    val customerOrderHistoryLoading: Set<String> = emptySet(),
    val customerOrderHistoryErrors: Map<String, String> = emptyMap(),
    val printers: List<OrmaPrinterProfile> = emptyList(),
    val teamMembers: List<OrmaTeamMember> = emptyList(),
    val paymentMethods: List<OrmaWorkspacePaymentMethod> = emptyList(),
    val metaConnection: OrmaMetaConnectionStatus? = null,
    val metaActionLoading: Boolean = false,
    val productExport: OrmaProductExport? = null,
    val productImportTemplate: OrmaProductImportTemplate? = null,
    val productImportResult: OrmaProductImportResult? = null,
    val invoiceGstinLookupLoading: Boolean = false,
    val invoiceGstinLookupNumber: String = "",
    val invoiceGstinLookupStatusMessage: String? = null,
    val invoiceGstinLookupErrorMessage: String? = null,
    val invoiceGstinLookup: OrmaGstinLookup? = null,
    val filters: OrmaDashboardFilters = OrmaDashboardFilters(),
)

internal data class OnboardingActions(
    val onIdentifierTypeChange: (AuthIdentifierType) -> Unit,
    val onCountryChange: (OrmaCountryUi) -> Unit,
    val onIdentifierChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onAccessPathChange: (AccessPath) -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onOtpChange: (String) -> Unit,
    val onResendOtp: () -> Unit,
    val onClearAuthAlert: () -> Unit,
    val onDraftChange: (BusinessSetupDraft) -> Unit,
    val onGstinLookupRequest: (String) -> Unit,
    val onLogoUploadRequest: () -> Unit,
    val onCoverUploadRequest: () -> Unit,
    val onSetupStepChange: (BusinessSetupStep) -> Unit,
    val onNotificationDecision: (Boolean) -> Unit,
    val onTeamProfileNameChange: (String) -> Unit,
    val onDashboardRefresh: () -> Unit,
    val onClearDashboardMessage: () -> Unit,
    val onClearDashboardStatusMessage: () -> Unit,
    val onShowDashboardStatusMessage: (String) -> Unit,
    val onDashboardSearchChange: (String) -> Unit,
    val onOrderStatusFilterChange: (String) -> Unit,
    val onOrderTypeFilterChange: (String) -> Unit,
    val onDashboardDateFilterChange: (String, String) -> Unit,
    val onProductItemTypeFilterChange: (String) -> Unit,
    val onProductLowStockFilterChange: (Boolean) -> Unit,
    val onDashboardPageChange: (DashboardPageTarget, Int) -> Unit,
    val onLoadCustomerOrders: (String) -> Unit,
    val onCreateCustomer: (OrmaCustomerDraft) -> Unit,
    val onCreateSupplier: (OrmaSupplierDraft) -> Unit,
    val onCreateProductCategory: (OrmaProductCategoryDraft) -> Unit,
    val onCreateProductOffer: (OrmaProductOfferDraft) -> Unit,
    val onCreateProduct: (OrmaProductDraft) -> Unit,
    val onUpdateProduct: (String, OrmaProductDraft) -> Unit,
    val onUploadProductImage: (String, OrmaPickedImage) -> Unit,
    val onLoadProductImportTemplate: () -> Unit,
    val onDownloadProductImportTemplate: () -> Unit,
    val onImportProductsCsv: (String) -> Unit,
    val onExportProductsCsv: () -> Unit,
    val onDownloadProductExport: () -> Unit,
    val onClearProductTransfer: () -> Unit,
    val onAdjustProductStock: (String, OrmaStockAdjustmentDraft) -> Unit,
    val onCreateOrder: (OrmaOrderDraft) -> Unit,
    val onUpdateOrder: (String, OrmaOrderDraft) -> Unit,
    val onUpdateOrderStatus: (String, String) -> Unit,
    val onUpdateOrderStatusWithPayment: (String, String, String?) -> Unit,
    val onInvoiceGstinLookupRequest: (String) -> Unit,
    val onCreatePrinter: (OrmaPrinterDraft) -> Unit,
    val onCreatePaymentMethod: (OrmaWorkspacePaymentMethodDraft) -> Unit,
    val onUpdateMetaConnection: (OrmaMetaConnectionDraft) -> Unit,
    val onSyncMetaCatalog: () -> Unit,
    val onCreateBusiness: () -> Unit,
    val onBack: () -> Unit,
    val onContinue: () -> Unit,
    val onRestart: () -> Unit,
)

internal fun OnboardingUiState.stageTitle(): String = when (step) {
    OnboardingStep.Authentication -> "Authentication"
    OnboardingStep.Otp -> "OTP verification"
    OnboardingStep.Owner -> "Owner onboarding"
    OnboardingStep.Team -> "Team login"
    OnboardingStep.BusinessSetup -> setupStep.title
    OnboardingStep.Notification -> "Notifications"
    OnboardingStep.Complete -> "Ready"
    OnboardingStep.Dashboard -> "Dashboard"
}

internal fun OnboardingUiState.stageBody(): String = when (step) {
    OnboardingStep.Authentication -> "Sign in with phone, email, or Google. ORMA opens the right workspace after sign-in."
    OnboardingStep.Otp -> "Verify access with the one-time code sent to ${selectedCountry.dialCode} ${identifier.trim()}."
    OnboardingStep.Owner -> "Set the accountable owner before business setup begins."
    OnboardingStep.Team -> "Confirm the invite and continue into the existing workspace."
    OnboardingStep.BusinessSetup -> setupStep.description
    OnboardingStep.Notification -> "Push notifications keep the workspace updated after setup."
    OnboardingStep.Complete -> "This first product milestone is ready for backend integration."
    OnboardingStep.Dashboard -> "Authenticated workspace dashboard."
}

internal fun OnboardingUiState.progressItems(): List<Pair<String, Boolean>> = listOf(
    "Auth" to (step == OnboardingStep.Authentication),
    "OTP" to (step == OnboardingStep.Otp),
    "Owner" to (step == OnboardingStep.Owner || step == OnboardingStep.Team),
    "Setup" to (step == OnboardingStep.BusinessSetup),
    "Notify" to (step == OnboardingStep.Notification),
    "Ready" to (step == OnboardingStep.Complete || step == OnboardingStep.Dashboard),
)

internal fun loginIdentifierError(
    identifierType: AuthIdentifierType,
    identifier: String,
    country: OrmaCountryUi = OrmaDefaultCountry,
): String? =
    if (identifier.isNotBlank() && !isLoginIdentifierValid(identifierType, identifier, country)) {
        "Enter a valid ${identifierType.label.lowercase()}."
    } else {
        null
    }
