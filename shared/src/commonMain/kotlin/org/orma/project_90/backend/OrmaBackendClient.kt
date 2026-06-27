package org.orma.project_90.backend

import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthSession
import org.orma.project_90.auth.OrmaHttpResponse
import org.orma.project_90.auth.ormaGetAuthorized
import org.orma.project_90.auth.ormaPostJson
import org.orma.project_90.auth.ormaPostJsonAuthorized
import org.orma.project_90.auth.ormaPostMultipartAuthorized
import org.orma.project_90.auth.ormaPutJsonAuthorized
import org.orma.project_90.media.OrmaPickedImage
import org.orma.project_90.onboarding.BusinessSetupDraft

data class OrmaBackendUser(
    val id: String,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val role: String,
    val notificationsEnabled: Boolean,
)

data class OrmaBackendWorkspace(
    val id: String,
    val businessName: String,
    val legalName: String,
    val role: String,
    val onboardingComplete: Boolean,
    val logoFileName: String?,
    val logoUrl: String?,
    val coverFileName: String?,
    val coverUrl: String?,
)

data class OrmaBackendSession(
    val user: OrmaBackendUser,
    val workspace: OrmaBackendWorkspace?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

data class OrmaTeamOverview(
    val workspace: OrmaBackendWorkspace,
    val canInviteMembers: Boolean,
    val members: List<OrmaTeamMember>,
    val invites: List<OrmaTeamInvite> = emptyList(),
)

data class OrmaTeamMember(
    val id: String,
    val userId: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: String,
    val permissions: List<String> = emptyList(),
    val status: String,
    val joinedAt: String,
)

data class OrmaTeamInvite(
    val id: String,
    val code: String,
    val inviteeName: String?,
    val inviteeEmail: String?,
    val inviteePhoneNumber: String?,
    val role: String,
    val permissions: List<String> = emptyList(),
    val status: String,
    val createdAt: String,
    val expiresAt: String?,
    val createdByDisplayName: String?,
    val createdByEmail: String?,
)

data class OrmaTeamInviteDraft(
    val inviteeName: String = "",
    val inviteeEmail: String = "",
    val inviteePhoneNumber: String = "",
    val role: String = "team_member",
    val permissions: List<String> = emptyList(),
)

data class OrmaTeamMemberAccessDraft(
    val role: String = "team_member",
    val permissions: List<String> = emptyList(),
)

data class OrmaMediaUpload(
    val type: String,
    val storagePath: String,
    val downloadUrl: String?,
    val contentType: String,
    val sizeBytes: Long,
)

data class OrmaGstinLookup(
    val gstin: String,
    val found: Boolean,
    val message: String,
    val source: String,
    val cached: Boolean,
    val cachedAt: String?,
    val legalName: String?,
    val tradeName: String?,
    val addressLine: String?,
    val city: String?,
    val region: String?,
    val postalCode: String?,
)

data class OrmaDashboardSummary(
    val currency: String = "INR",
    val businessMode: String = "product_selling",
    val totalCustomers: Int = 0,
    val totalPaidAmount: String = "0.00",
    val ordersCount: Int = 0,
    val bookingsCount: Int = 0,
    val salesCount: Int = 0,
    val serviceOrdersCount: Int = 0,
    val appointmentsCount: Int = 0,
    val todayAppointmentsCount: Int = 0,
    val productsInStock: Int = 0,
    val lowStockProducts: Int = 0,
    val recentOrders: List<OrmaOrder> = emptyList(),
    val lowStockItems: List<OrmaProduct> = emptyList(),
    val revenueSeries: List<OrmaDashboardRevenuePoint> = emptyList(),
    val orderStatusBreakdown: List<OrmaDashboardBreakdown> = emptyList(),
    val orderTypeBreakdown: List<OrmaDashboardBreakdown> = emptyList(),
    val topItems: List<OrmaDashboardTopItem> = emptyList(),
    val recentActivity: List<OrmaDashboardActivity> = emptyList(),
    val dashboardTasks: List<OrmaDashboardTask> = emptyList(),
    val notificationPreview: List<OrmaDashboardNotificationPreview> = emptyList(),
)

data class OrmaDashboardRevenuePoint(
    val date: String,
    val amount: String,
    val ordersCount: Int,
)

data class OrmaDashboardBreakdown(
    val key: String,
    val label: String,
    val count: Int,
    val amount: String,
)

data class OrmaDashboardTopItem(
    val productId: String?,
    val name: String,
    val itemType: String,
    val quantity: String,
    val amount: String,
    val imageUrl: String?,
)

data class OrmaDashboardActivity(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val occurredAt: String,
    val tone: String,
    val performedByUserId: String? = null,
    val performedByDisplayName: String? = null,
    val performedByEmail: String? = null,
    val performedByPhoneNumber: String? = null,
    val performedByRole: String? = null,
)

data class OrmaDashboardTask(
    val id: String,
    val title: String,
    val body: String,
    val action: String,
    val priority: String,
    val tone: String,
    val count: Int,
)

data class OrmaDashboardNotificationPreview(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val tone: String,
)

data class OrmaCustomer(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val taxNumber: String?,
    val addressLine: String?,
    val city: String?,
    val region: String?,
    val country: String?,
    val postalCode: String?,
    val notes: String?,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaSupplier(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val taxNumber: String?,
    val addressLine: String?,
    val paymentTerms: String? = null,
    val paymentMode: String? = null,
    val paymentReference: String? = null,
    val payableTotal: String = "0.00",
    val paidTotal: String = "0.00",
    val balanceDue: String = "0.00",
    val currency: String = "INR",
    val lastPaymentAt: String? = null,
    val notes: String?,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaProduct(
    val id: String,
    val categoryId: String?,
    val categoryName: String?,
    val supplierId: String?,
    val supplierName: String?,
    val name: String,
    val itemType: String = "product",
    val sku: String?,
    val barcode: String?,
    val description: String?,
    val unit: String,
    val sellingPrice: String,
    val costPrice: String,
    val currency: String,
    val taxRate: String,
    val pricesIncludeTax: Boolean,
    val stockQuantity: String,
    val reorderLevel: String,
    val trackStock: Boolean,
    val durationMinutes: Int? = null,
    val bookingRequired: Boolean = false,
    val expiryDate: String? = null,
    val lowStock: Boolean,
    val status: String,
    val imageUrl: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaProductCategory(
    val id: String,
    val name: String,
    val itemType: String = "all",
    val sortOrder: Int,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaProductCategoryDraft(
    val name: String = "",
    val itemType: String = "all",
    val sortOrder: String = "0",
)

data class OrmaProductOffer(
    val id: String,
    val appliesTo: String,
    val productId: String?,
    val productName: String?,
    val categoryId: String?,
    val categoryName: String?,
    val customerId: String?,
    val customerName: String?,
    val name: String,
    val itemType: String = "product",
    val description: String?,
    val discountType: String,
    val discountValue: String,
    val discountCapAmount: String?,
    val couponCode: String?,
    val startsAt: String?,
    val endsAt: String?,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaProductOfferDraft(
    val appliesTo: String = "product",
    val productId: String = "",
    val categoryId: String = "",
    val customerId: String = "",
    val name: String = "",
    val description: String = "",
    val discountType: String = "percentage",
    val discountValue: String = "",
    val discountCapAmount: String = "",
    val couponCode: String = "",
    val startsAt: String = "",
    val endsAt: String = "",
)

data class OrmaPublicCatalogWorkspace(
    val id: String,
    val businessName: String,
    val industry: String,
    val city: String,
    val currency: String,
    val whatsappDisplayNumber: String?,
    val logoUrl: String?,
    val coverUrl: String?,
)

data class OrmaPublicCatalogCategory(
    val id: String,
    val name: String,
    val sortOrder: Int,
)

data class OrmaPublicCatalogPaymentMethod(
    val id: String,
    val type: String,
    val label: String,
    val upiId: String?,
    val payeeName: String?,
    val isDefault: Boolean,
)

data class OrmaPublicCatalogOffer(
    val id: String,
    val name: String,
    val description: String?,
    val discountType: String,
    val discountValue: String,
    val discountAmount: String,
    val finalPrice: String,
)

data class OrmaPublicCatalogProduct(
    val id: String,
    val categoryId: String?,
    val categoryName: String?,
    val name: String,
    val itemType: String = "product",
    val description: String?,
    val unit: String,
    val sellingPrice: String,
    val currency: String,
    val taxRate: String,
    val pricesIncludeTax: Boolean,
    val trackStock: Boolean,
    val stockQuantity: String,
    val inStock: Boolean,
    val durationMinutes: Int? = null,
    val bookingRequired: Boolean = false,
    val imageUrl: String? = null,
    val offer: OrmaPublicCatalogOffer? = null,
)

data class OrmaPublicCatalog(
    val workspace: OrmaPublicCatalogWorkspace,
    val categories: List<OrmaPublicCatalogCategory>,
    val paymentMethods: List<OrmaPublicCatalogPaymentMethod>,
    val products: List<OrmaPublicCatalogProduct>,
)

data class OrmaPublicCatalogOrderDraft(
    val customerName: String = "",
    val phoneNumber: String = "",
    val notes: String = "",
    val fulfillmentType: String = "take_away",
    val scheduledAt: String = "",
    val paymentMode: String = "pay_on_spot",
    val items: List<OrmaPublicCatalogOrderItemDraft> = emptyList(),
)

data class OrmaPublicCatalogOrderItemDraft(
    val productId: String,
    val quantity: String,
)

data class OrmaPublicCatalogOrderReceipt(
    val message: String,
    val order: OrmaOrder,
    val paymentLink: String?,
    val paymentMethod: OrmaPublicCatalogPaymentMethod?,
)

data class OrmaProductExport(
    val fileName: String,
    val count: Int,
    val csv: String,
    val columns: List<String> = emptyList(),
)

data class OrmaProductImportTemplate(
    val fileName: String,
    val columns: List<String>,
    val requiredColumns: List<String>,
    val csv: String,
)

data class OrmaProductImportResult(
    val created: Int,
    val skipped: Int,
    val errors: List<OrmaProductImportError>,
    val products: List<OrmaProduct>,
)

data class OrmaProductImportError(
    val row: Int,
    val message: String,
)

private data class OrmaProductImportCsvRow(
    val name: String,
    val itemType: String,
    val categoryName: String,
    val sku: String,
    val barcode: String,
    val description: String,
    val unit: String,
    val sellingPrice: String,
    val costPrice: String,
    val currency: String,
    val taxRate: String,
    val pricesIncludeTax: Boolean,
    val stockQuantity: String,
    val reorderLevel: String,
    val trackStock: Boolean,
    val durationMinutes: String,
    val bookingRequired: Boolean,
    val expiryDate: String,
    val supplierName: String,
)

data class OrmaOrder(
    val id: String,
    val orderNumber: String,
    val customerId: String?,
    val customerName: String?,
    val customerPhoneNumber: String? = null,
    val customerEmail: String? = null,
    val customerTaxNumber: String? = null,
    val customerAddressLine: String? = null,
    val customerCity: String? = null,
    val customerRegion: String? = null,
    val customerCountry: String? = null,
    val customerPostalCode: String? = null,
    val orderType: String = "sale",
    val status: String,
    val scheduledAt: String?,
    val subtotal: String,
    val taxTotal: String,
    val discountTotal: String,
    val paidTotal: String,
    val total: String,
    val currency: String,
    val notes: String?,
    val fulfillmentType: String = "standard",
    val paymentMode: String = "pay_on_spot",
    val source: String = "dashboard",
    val itemCount: Int,
    val items: List<OrmaOrderItem> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaOrderItem(
    val id: String,
    val productId: String?,
    val productName: String?,
    val description: String,
    val quantity: String,
    val unitPrice: String,
    val taxRate: String,
    val lineSubtotal: String,
    val lineTax: String,
    val lineTotal: String,
)

data class OrmaCustomerDraft(
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val taxNumber: String = "",
    val addressLine: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "India",
    val postalCode: String = "",
    val notes: String = "",
)

data class OrmaSupplierDraft(
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val taxNumber: String = "",
    val addressLine: String = "",
    val paymentTerms: String = "",
    val paymentMode: String = "",
    val paymentReference: String = "",
    val payableTotal: String = "",
    val paidTotal: String = "",
    val currency: String = "INR",
    val lastPaymentAt: String = "",
    val notes: String = "",
)

data class OrmaProductDraft(
    val name: String = "",
    val itemType: String = "product",
    val categoryId: String = "",
    val categoryName: String = "",
    val sku: String = "",
    val barcode: String = "",
    val description: String = "",
    val unit: String = "pcs",
    val sellingPrice: String = "",
    val costPrice: String = "",
    val currency: String = "INR",
    val taxRate: String = "0",
    val pricesIncludeTax: Boolean = false,
    val stockQuantity: String = "",
    val reorderLevel: String = "",
    val trackStock: Boolean = true,
    val durationMinutes: String = "",
    val bookingRequired: Boolean = false,
    val expiryDate: String = "",
    val supplierId: String = "",
    val status: String = "active",
    val image: OrmaPickedImage? = null,
)

data class OrmaWorkspacePaymentMethod(
    val id: String,
    val type: String,
    val label: String,
    val upiId: String?,
    val payeeName: String?,
    val isDefault: Boolean,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaWorkspacePaymentMethodDraft(
    val label: String = "",
    val upiId: String = "",
    val payeeName: String = "",
    val isDefault: Boolean = false,
)

data class OrmaOrderDraft(
    val customerId: String = "",
    val customerName: String = "",
    val customerPhoneNumber: String = "",
    val customerEmail: String = "",
    val customerTaxNumber: String = "",
    val customerAddressLine: String = "",
    val customerCity: String = "",
    val customerRegion: String = "",
    val customerCountry: String = "",
    val customerPostalCode: String = "",
    val orderType: String = "sale",
    val status: String = "confirmed",
    val scheduledAt: String = "",
    val paidTotal: String = "0",
    val discountTotal: String = "0",
    val currency: String = "INR",
    val notes: String = "",
    val fulfillmentType: String = "standard",
    val paymentMode: String = "pay_on_spot",
    val items: List<OrmaOrderItemDraft> = listOf(OrmaOrderItemDraft()),
)

data class OrmaOrderItemDraft(
    val productId: String = "",
    val description: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val taxRate: String = "0",
)

data class OrmaStockAdjustmentDraft(
    val quantityDelta: String = "",
    val note: String = "",
    val sellingPrice: String = "",
    val costPrice: String = "",
    val supplierId: String = "",
    val status: String = "active",
    val expiryDate: String = "",
)

data class OrmaDashboardFilters(
    val query: String = "",
    val orderStatus: String = "all",
    val itemType: String = "all",
    val orderType: String = "all",
    val datePreset: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val page: Int = 1,
    val lowStockOnly: Boolean = false,
    val supplierId: String = "",
    val barcode: String = "",
    val scheduledOnly: Boolean = false,
    val excludeCancelled: Boolean = false,
    val limit: Int = 50,
)

data class OrmaPagination(
    val page: Int = 1,
    val pageSize: Int = 50,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
)

data class OrmaPagedList<T>(
    val items: List<T>,
    val pagination: OrmaPagination = OrmaPagination(totalItems = items.size),
)

data class OrmaPrinterProfile(
    val id: String,
    val name: String,
    val connectionType: String,
    val address: String?,
    val paperWidthMm: Int,
    val dpi: Int,
    val supportsReceipts: Boolean,
    val supportsBarcodes: Boolean,
    val isDefaultReceipt: Boolean,
    val isDefaultBarcode: Boolean,
    val notes: String?,
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class OrmaPrinterDraft(
    val name: String = "",
    val connectionType: String = "mtp_usb",
    val address: String = "",
    val paperWidthMm: String = "80",
    val dpi: String = "203",
    val supportsReceipts: Boolean = true,
    val supportsBarcodes: Boolean = true,
    val isDefaultReceipt: Boolean = false,
    val isDefaultBarcode: Boolean = false,
    val notes: String = "",
)

data class OrmaMetaConnectionStatus(
    val connected: Boolean,
    val status: String,
    val connectionMode: String,
    val businessDisplayName: String?,
    val businessId: String?,
    val whatsappDisplayNumber: String?,
    val whatsappBusinessAccountId: String?,
    val phoneNumberId: String?,
    val catalogId: String?,
    val pageId: String?,
    val instagramBusinessAccountId: String?,
    val scopes: List<String>,
    val accessTokenStatus: String,
    val credentialSource: String,
    val tokenExpiresAt: String?,
    val webhookSubscribedAt: String?,
    val messagingStatus: String,
    val lastSyncAt: String?,
    val lastError: String?,
    val productsReady: Int,
    val productsBlocked: Int,
    val productsSynced: Int,
    val productReadiness: List<OrmaMetaProductReadiness>,
)

data class OrmaMetaConnectionDraft(
    val status: String = "credentials_pending",
    val connectionMode: String = "manual_setup",
    val businessDisplayName: String = "",
    val businessId: String = "",
    val whatsappDisplayNumber: String = "",
    val whatsappBusinessAccountId: String = "",
    val phoneNumberId: String = "",
    val catalogId: String = "",
    val pageId: String = "",
    val instagramBusinessAccountId: String = "",
    val scopes: List<String> = emptyList(),
)

data class OrmaMetaProductReadiness(
    val productId: String,
    val productName: String,
    val ready: Boolean,
    val status: String,
    val issues: List<String>,
    val metaProductId: String?,
    val lastSyncAt: String?,
)

data class OrmaMetaCatalogSyncResult(
    val connected: Boolean,
    val productsReady: Int,
    val productsBlocked: Int,
    val productsSynced: Int,
    val productReadiness: List<OrmaMetaProductReadiness>,
    val message: String,
)

data class OrmaMetaWhatsAppTemplate(
    val id: String?,
    val name: String,
    val status: String,
    val category: String,
    val languageCode: String,
    val bodyText: String?,
    val rejectedReason: String?,
)

data class OrmaMetaWhatsAppTemplateDraft(
    val name: String = "",
    val category: String = "UTILITY",
    val languageCode: String = "en_US",
    val bodyText: String = "",
    val sampleParameters: List<String> = emptyList(),
)

data class OrmaMetaWhatsAppTemplateListResult(
    val connected: Boolean,
    val templates: List<OrmaMetaWhatsAppTemplate>,
    val message: String,
)

data class OrmaMetaWhatsAppTemplateCreateResult(
    val created: Boolean,
    val status: String,
    val template: OrmaMetaWhatsAppTemplate?,
    val message: String,
)

data class OrmaMetaWhatsAppTemplateSyncItem(
    val name: String,
    val status: String,
    val message: String,
)

data class OrmaMetaWhatsAppTemplateSyncResult(
    val connected: Boolean,
    val created: Int,
    val failed: Int,
    val templates: List<OrmaMetaWhatsAppTemplateSyncItem>,
    val message: String,
)

sealed interface OrmaBackendResult<out T> {
    data class Success<T>(
        val value: T,
    ) : OrmaBackendResult<T>

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaBackendResult<Nothing>
}

class OrmaBackendClient(
    private val config: OrmaBackendConfig,
) {
    suspend fun resolveSession(session: OrmaAuthSession): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Resolve ORMA workspace"
        return executeBackendSessionRequest(actionTitle) {
            ormaPostJson(
                url = config.url("/auth/session"),
                body = buildJsonObject(
                    "idToken" to JsonValue.StringValue(session.idToken),
                    "provider" to JsonValue.StringValue(session.provider.backendName),
                    "email" to JsonValue.StringValue(session.email),
                    "phoneNumber" to JsonValue.StringValue(session.phoneNumber),
                    "displayName" to JsonValue.StringValue(session.displayName),
                ),
            )
        }
    }

    suspend fun completeBusinessSetup(
        idToken: String,
        draft: BusinessSetupDraft,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Save business setup"
        return executeBackendSessionRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/business"),
                bearerToken = idToken,
                body = buildJsonObject(
                    "ownerName" to JsonValue.StringValue(draft.ownerName),
                    "businessName" to JsonValue.StringValue(draft.businessName),
                    "legalName" to JsonValue.StringValue(draft.legalName),
                    "industry" to JsonValue.StringValue(draft.industry),
                    "businessMode" to JsonValue.StringValue(draft.businessMode),
                    "website" to JsonValue.StringValue(draft.website),
                    "isTaxRegistered" to JsonValue.BooleanValue(draft.isTaxRegistered),
                    "taxNumber" to JsonValue.StringValue(draft.taxNumber),
                    "taxLabel" to JsonValue.StringValue(draft.taxLabel),
                    "addressLine" to JsonValue.StringValue(draft.addressLine),
                    "city" to JsonValue.StringValue(draft.city),
                    "region" to JsonValue.StringValue(draft.region),
                    "country" to JsonValue.StringValue(draft.country),
                    "postalCode" to JsonValue.StringValue(draft.postalCode),
                    "logoFileName" to JsonValue.StringValue(draft.logoFileName),
                    "invoicePrefix" to JsonValue.StringValue(draft.invoicePrefix),
                    "nextInvoiceNumber" to JsonValue.StringValue(draft.nextInvoiceNumber),
                    "paymentTerms" to JsonValue.StringValue(draft.paymentTerms),
                    "invoiceFooter" to JsonValue.StringValue(draft.invoiceFooter),
                    "currency" to JsonValue.StringValue(draft.currency),
                    "taxMode" to JsonValue.StringValue(draft.taxMode),
                    "pricesIncludeTax" to JsonValue.BooleanValue(draft.pricesIncludeTax),
                ),
            )
        }
    }

    suspend fun getTeamOverview(
        idToken: String,
    ): OrmaBackendResult<OrmaTeamOverview> {
        val actionTitle = "Load team"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaGetAuthorized(
                    url = config.url("/onboarding/team"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toTeamOverview() },
        )
    }

    suspend fun createTeamInvite(
        idToken: String,
        draft: OrmaTeamInviteDraft,
    ): OrmaBackendResult<OrmaTeamOverview> =
        executeBackendRequest(
            actionTitle = "Invite team member",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/onboarding/team/invites"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "inviteeName" to JsonValue.StringValue(draft.inviteeName.blankToNull()),
                        "inviteeEmail" to JsonValue.StringValue(draft.inviteeEmail.blankToNull()),
                        "inviteePhoneNumber" to JsonValue.StringValue(draft.inviteePhoneNumber.blankToNull()),
                        "role" to JsonValue.StringValue(draft.role.ifBlank { "team_member" }),
                        "permissions" to JsonValue.RawValue(draft.permissions.jsonStringArrayLiteral()),
                    ),
                )
            },
            parse = { it.toTeamOverview() },
        )

    suspend fun revokeTeamInvite(
        idToken: String,
        inviteId: String,
    ): OrmaBackendResult<OrmaTeamOverview> =
        executeBackendRequest(
            actionTitle = "Revoke invite",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/onboarding/team/invites/${inviteId.urlQueryEscaped()}/revoke"),
                    bearerToken = idToken,
                    body = buildJsonObject(),
                )
            },
            parse = { it.toTeamOverview() },
        )

    suspend fun removeTeamMember(
        idToken: String,
        memberId: String,
    ): OrmaBackendResult<OrmaTeamOverview> =
        executeBackendRequest(
            actionTitle = "Remove team member",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/onboarding/team/members/${memberId.urlQueryEscaped()}/remove"),
                    bearerToken = idToken,
                    body = buildJsonObject(),
                )
            },
            parse = { it.toTeamOverview() },
        )

    suspend fun updateTeamMemberAccess(
        idToken: String,
        memberId: String,
        draft: OrmaTeamMemberAccessDraft,
    ): OrmaBackendResult<OrmaTeamOverview> =
        executeBackendRequest(
            actionTitle = "Update team access",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/onboarding/team/members/${memberId.urlQueryEscaped()}/access"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "role" to JsonValue.StringValue(draft.role.ifBlank { "team_member" }),
                        "permissions" to JsonValue.RawValue(draft.permissions.jsonStringArrayLiteral()),
                    ),
                )
            },
            parse = { it.toTeamOverview() },
        )

    suspend fun updateNotificationPreference(
        idToken: String,
        enabled: Boolean,
        deviceToken: String? = null,
        platform: String? = null,
        deviceName: String? = null,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Save notifications"
        return executeBackendSessionRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/notifications"),
                bearerToken = idToken,
                body = buildJsonObject(
                    "enabled" to JsonValue.BooleanValue(enabled),
                    "deviceToken" to JsonValue.StringValue(deviceToken),
                    "platform" to JsonValue.StringValue(platform),
                    "deviceName" to JsonValue.StringValue(deviceName),
                ),
            )
        }
    }

    suspend fun uploadBusinessLogo(
        idToken: String,
        image: OrmaPickedImage,
    ): OrmaBackendResult<OrmaMediaUpload> {
        val actionTitle = "Upload business logo"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaPostMultipartAuthorized(
                    url = config.url("/media/business-logo"),
                    bearerToken = idToken,
                    fileFieldName = "file",
                    fileName = image.fileName,
                    contentType = image.contentType,
                    bytes = image.bytes,
                )
            },
            parse = { it.toMediaUpload() },
        )
    }

    suspend fun uploadBusinessCover(
        idToken: String,
        image: OrmaPickedImage,
    ): OrmaBackendResult<OrmaMediaUpload> {
        val actionTitle = "Upload cover photo"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaPostMultipartAuthorized(
                    url = config.url("/media/business-cover"),
                    bearerToken = idToken,
                    fileFieldName = "file",
                    fileName = image.fileName,
                    contentType = image.contentType,
                    bytes = image.bytes,
                )
            },
            parse = { it.toMediaUpload() },
        )
    }

    suspend fun uploadProductImage(
        idToken: String,
        productId: String,
        image: OrmaPickedImage,
    ): OrmaBackendResult<OrmaMediaUpload> {
        val actionTitle = "Upload product image"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaPostMultipartAuthorized(
                    url = config.url("/media/product-images"),
                    bearerToken = idToken,
                    fileFieldName = "file",
                    fileName = image.fileName,
                    contentType = image.contentType,
                    bytes = image.bytes,
                    fields = mapOf("productId" to productId),
                )
            },
            parse = { it.toMediaUpload() },
        )
    }

    suspend fun lookupGstin(
        idToken: String,
        gstin: String,
    ): OrmaBackendResult<OrmaGstinLookup> {
        val actionTitle = "Verify GSTIN"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaGetAuthorized(
                    url = config.url("/gstin/${gstin.normalizedGstin()}"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toGstinLookup() },
        )
    }

    suspend fun getDashboardSummary(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaDashboardSummary> =
        executeBackendRequest(
            actionTitle = "Load dashboard",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/dashboard/summary", filters),
                    bearerToken = idToken,
                )
            },
            parse = { it.toDashboardSummary() },
        )

    suspend fun listCustomers(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaCustomer>> =
        executeBackendRequest(
            actionTitle = "Load customers",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/customers", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("customers") { it.toCustomer() } },
        )

    suspend fun createCustomer(
        idToken: String,
        draft: OrmaCustomerDraft,
    ): OrmaBackendResult<OrmaCustomer> =
        executeBackendRequest(
            actionTitle = "Create customer",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/customers"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(draft.name),
                        "phoneNumber" to JsonValue.StringValue(draft.phoneNumber.blankToNull()),
                        "email" to JsonValue.StringValue(draft.email.blankToNull()),
                        "taxNumber" to JsonValue.StringValue(draft.taxNumber.blankToNull()),
                        "addressLine" to JsonValue.StringValue(draft.addressLine.blankToNull()),
                        "city" to JsonValue.StringValue(draft.city.blankToNull()),
                        "region" to JsonValue.StringValue(draft.region.blankToNull()),
                        "country" to JsonValue.StringValue(draft.country.blankToNull()),
                        "postalCode" to JsonValue.StringValue(draft.postalCode.blankToNull()),
                        "notes" to JsonValue.StringValue(draft.notes.blankToNull()),
                    ),
                )
            },
            parse = { it.toCustomer() },
        )

    suspend fun listSuppliers(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaSupplier>> =
        executeBackendRequest(
            actionTitle = "Load suppliers",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/suppliers", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("suppliers") { it.toSupplier() } },
        )

    suspend fun createSupplier(
        idToken: String,
        draft: OrmaSupplierDraft,
    ): OrmaBackendResult<OrmaSupplier> =
        executeBackendRequest(
            actionTitle = "Create supplier",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/suppliers"),
                    bearerToken = idToken,
                    body = draft.toSupplierRequestJson(),
                )
            },
            parse = { it.toSupplier() },
        )

    suspend fun updateSupplier(
        idToken: String,
        supplierId: String,
        draft: OrmaSupplierDraft,
    ): OrmaBackendResult<OrmaSupplier> =
        executeBackendRequest(
            actionTitle = "Update supplier",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/suppliers/$supplierId"),
                    bearerToken = idToken,
                    body = draft.toSupplierRequestJson(),
                )
            },
            parse = { it.toSupplier() },
        )

    suspend fun listProductCategories(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaProductCategory>> =
        executeBackendRequest(
            actionTitle = "Load categories",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/product-categories", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("categories") { it.toProductCategory() } },
        )

    suspend fun createProductCategory(
        idToken: String,
        draft: OrmaProductCategoryDraft,
    ): OrmaBackendResult<OrmaProductCategory> =
        executeBackendRequest(
            actionTitle = "Create category",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/product-categories"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(draft.name),
                        "itemType" to JsonValue.StringValue(draft.itemType),
                        "sortOrder" to JsonValue.RawValue(draft.sortOrder.intInput(default = "0")),
                    ),
                )
            },
            parse = { it.toProductCategory() },
        )

    suspend fun listProductOffers(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaProductOffer>> =
        executeBackendRequest(
            actionTitle = "Load offers",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/offers", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("offers") { it.toProductOffer() } },
        )

    suspend fun createProductOffer(
        idToken: String,
        draft: OrmaProductOfferDraft,
    ): OrmaBackendResult<OrmaProductOffer> =
        executeBackendRequest(
            actionTitle = "Create offer",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/offers"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "appliesTo" to JsonValue.StringValue(draft.appliesTo),
                        "productId" to JsonValue.StringValue(draft.productId.blankToNull()),
                        "categoryId" to JsonValue.StringValue(draft.categoryId.blankToNull()),
                        "customerId" to JsonValue.StringValue(draft.customerId.blankToNull()),
                        "name" to JsonValue.StringValue(draft.name),
                        "description" to JsonValue.StringValue(draft.description.blankToNull()),
                        "discountType" to JsonValue.StringValue(draft.discountType),
                        "discountValue" to JsonValue.StringValue(draft.discountValue.blankToZero()),
                        "discountCapAmount" to JsonValue.StringValue(draft.discountCapAmount.blankToNull()),
                        "couponCode" to JsonValue.StringValue(draft.couponCode.blankToNull()),
                        "startsAt" to JsonValue.StringValue(draft.startsAt.blankToNull()),
                        "endsAt" to JsonValue.StringValue(draft.endsAt.blankToNull()),
                    ),
                )
            },
            parse = { it.toProductOffer() },
        )

    suspend fun updateProductOffer(
        idToken: String,
        offerId: String,
        draft: OrmaProductOfferDraft,
    ): OrmaBackendResult<OrmaProductOffer> =
        executeBackendRequest(
            actionTitle = "Update offer",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/offers/$offerId"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "appliesTo" to JsonValue.StringValue(draft.appliesTo),
                        "productId" to JsonValue.StringValue(draft.productId.blankToNull()),
                        "categoryId" to JsonValue.StringValue(draft.categoryId.blankToNull()),
                        "customerId" to JsonValue.StringValue(draft.customerId.blankToNull()),
                        "name" to JsonValue.StringValue(draft.name),
                        "description" to JsonValue.StringValue(draft.description.blankToNull()),
                        "discountType" to JsonValue.StringValue(draft.discountType),
                        "discountValue" to JsonValue.StringValue(draft.discountValue.blankToZero()),
                        "discountCapAmount" to JsonValue.StringValue(draft.discountCapAmount.blankToNull()),
                        "couponCode" to JsonValue.StringValue(draft.couponCode.blankToNull()),
                        "startsAt" to JsonValue.StringValue(draft.startsAt.blankToNull()),
                        "endsAt" to JsonValue.StringValue(draft.endsAt.blankToNull()),
                    ),
                )
            },
            parse = { it.toProductOffer() },
        )

    suspend fun listProducts(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaProduct>> =
        executeBackendRequest(
            actionTitle = "Load products",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/products", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("products") { it.toProduct() } },
        )

    suspend fun exportProductsCsv(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaProductExport> =
        executeBackendRequest(
            actionTitle = "Export products",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/products/export", filters.copy(limit = 200)),
                    bearerToken = idToken,
                )
            },
            parse = { it.toProductExport() },
        )

    suspend fun getProductImportTemplate(
        idToken: String,
    ): OrmaBackendResult<OrmaProductImportTemplate> =
        executeBackendRequest(
            actionTitle = "Load catalog template",
            request = {
                ormaGetAuthorized(
                    url = config.url("/products/import-template"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toProductImportTemplate() },
        )

    suspend fun importProductsCsv(
        idToken: String,
        csv: String,
    ): OrmaBackendResult<OrmaProductImportResult> {
        if (csv.trim().isBlank()) {
            return OrmaBackendResult.Failure(
                title = "Import catalog",
                message = "Paste at least one catalog row before importing.",
                code = "PRODUCT_IMPORT_EMPTY",
            )
        }
        return executeBackendRequest(
            actionTitle = "Import catalog",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/products/import-csv"),
                    bearerToken = idToken,
                    body = buildJsonObject("csv" to JsonValue.StringValue(csv)),
                )
            },
            parse = { it.toProductImportResult() },
        )
    }

    suspend fun createProduct(
        idToken: String,
        draft: OrmaProductDraft,
    ): OrmaBackendResult<OrmaProduct> =
        executeBackendRequest(
            actionTitle = "Create product",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/products"),
                    bearerToken = idToken,
                    body = draft.toProductRequestJson(),
                )
            },
            parse = { it.toProduct() },
        )

    suspend fun updateProduct(
        idToken: String,
        productId: String,
        draft: OrmaProductDraft,
    ): OrmaBackendResult<OrmaProduct> =
        executeBackendRequest(
            actionTitle = "Update product",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/products/$productId"),
                    bearerToken = idToken,
                    body = draft.toProductRequestJson(),
                )
            },
            parse = { it.toProduct() },
        )

    suspend fun adjustProductStock(
        idToken: String,
        productId: String,
        draft: OrmaStockAdjustmentDraft,
    ): OrmaBackendResult<OrmaProduct> =
        executeBackendRequest(
            actionTitle = "Update stock",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/products/$productId/stock-adjustments"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "quantityDelta" to JsonValue.StringValue(draft.quantityDelta),
                        "note" to JsonValue.StringValue(draft.note.blankToNull()),
                        "sellingPrice" to JsonValue.StringValue(draft.sellingPrice.blankToNull()),
                        "costPrice" to JsonValue.StringValue(draft.costPrice.blankToNull()),
                        "supplierId" to JsonValue.StringValue(draft.supplierId.blankToNull()),
                        "status" to JsonValue.StringValue(draft.status.blankToNull()),
                        "expiryDate" to JsonValue.StringValue(draft.expiryDate.blankToNull()),
                    ),
                )
            },
            parse = { it.toProduct() },
        )

    suspend fun listOrders(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaOrder>> =
        executeBackendRequest(
            actionTitle = "Load orders",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/orders", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("orders") { it.toOrder() } },
        )

    suspend fun listCustomerOrders(
        idToken: String,
        customerId: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaOrder>> =
        executeBackendRequest(
            actionTitle = "Load customer bookings",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/customers/${customerId.urlPathEscaped()}/orders", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("orders") { it.toOrder() } },
        )

    suspend fun createOrder(
        idToken: String,
        draft: OrmaOrderDraft,
    ): OrmaBackendResult<OrmaOrder> =
        executeBackendRequest(
            actionTitle = draft.orderType.backendOrderActionTitle(),
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/orders"),
                    bearerToken = idToken,
                    body = draft.toOrderRequestJson(),
                )
            },
            parse = { it.toOrder() },
        )

    suspend fun updateOrder(
        idToken: String,
        orderId: String,
        draft: OrmaOrderDraft,
    ): OrmaBackendResult<OrmaOrder> =
        executeBackendRequest(
            actionTitle = "Save booking details",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/orders/$orderId"),
                    bearerToken = idToken,
                    body = draft.toOrderRequestJson(),
                )
            },
            parse = { it.toOrder() },
        )

    suspend fun updateOrderStatus(
        idToken: String,
        orderId: String,
        status: String,
        paidTotal: String? = null,
    ): OrmaBackendResult<OrmaOrder> =
        executeBackendRequest(
            actionTitle = "Update order status",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/orders/$orderId/status"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "status" to JsonValue.StringValue(status),
                        "paidTotal" to JsonValue.StringValue(paidTotal?.blankToZero()),
                    ),
                )
            },
            parse = { it.toOrder() },
        )

    suspend fun listPrinters(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaPrinterProfile>> =
        executeBackendRequest(
            actionTitle = "Load printers",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/printers", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("printers") { it.toPrinterProfile() } },
        )

    suspend fun createPrinter(
        idToken: String,
        draft: OrmaPrinterDraft,
    ): OrmaBackendResult<OrmaPrinterProfile> =
        executeBackendRequest(
            actionTitle = "Save printer",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/printers"),
                    bearerToken = idToken,
                    body = draft.toPrinterRequestJson(),
                )
            },
            parse = { it.toPrinterProfile() },
        )

    suspend fun updatePrinter(
        idToken: String,
        printerId: String,
        draft: OrmaPrinterDraft,
    ): OrmaBackendResult<OrmaPrinterProfile> =
        executeBackendRequest(
            actionTitle = "Update printer",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/printers/$printerId"),
                    bearerToken = idToken,
                    body = draft.toPrinterRequestJson(),
                )
            },
            parse = { it.toPrinterProfile() },
        )

    suspend fun deletePrinter(
        idToken: String,
        printerId: String,
    ): OrmaBackendResult<OrmaPrinterProfile> =
        executeBackendRequest(
            actionTitle = "Delete printer",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/printers/$printerId/delete"),
                    bearerToken = idToken,
                    body = "{}",
                )
            },
            parse = { it.toPrinterProfile() },
        )

    suspend fun listPaymentMethods(
        idToken: String,
        filters: OrmaDashboardFilters = OrmaDashboardFilters(),
    ): OrmaBackendResult<OrmaPagedList<OrmaWorkspacePaymentMethod>> =
        executeBackendRequest(
            actionTitle = "Load payment methods",
            request = {
                ormaGetAuthorized(
                    url = config.urlWithDashboardFilters("/payment-methods", filters),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.toPagedList("paymentMethods") { it.toWorkspacePaymentMethod() } },
        )

    suspend fun createPaymentMethod(
        idToken: String,
        draft: OrmaWorkspacePaymentMethodDraft,
    ): OrmaBackendResult<OrmaWorkspacePaymentMethod> =
        executeBackendRequest(
            actionTitle = "Save UPI",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/payment-methods"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "type" to JsonValue.StringValue("upi"),
                        "label" to JsonValue.StringValue(draft.label),
                        "upiId" to JsonValue.StringValue(draft.upiId),
                        "payeeName" to JsonValue.StringValue(draft.payeeName.blankToNull()),
                        "isDefault" to JsonValue.BooleanValue(draft.isDefault),
                    ),
                )
            },
            parse = { it.toWorkspacePaymentMethod() },
        )

    suspend fun updatePaymentMethod(
        idToken: String,
        paymentMethodId: String,
        draft: OrmaWorkspacePaymentMethodDraft,
    ): OrmaBackendResult<OrmaWorkspacePaymentMethod> =
        executeBackendRequest(
            actionTitle = "Update UPI",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/payment-methods/$paymentMethodId"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "type" to JsonValue.StringValue("upi"),
                        "label" to JsonValue.StringValue(draft.label),
                        "upiId" to JsonValue.StringValue(draft.upiId),
                        "payeeName" to JsonValue.StringValue(draft.payeeName.blankToNull()),
                        "isDefault" to JsonValue.BooleanValue(draft.isDefault),
                    ),
                )
            },
            parse = { it.toWorkspacePaymentMethod() },
        )

    suspend fun setDefaultPaymentMethod(
        idToken: String,
        paymentMethodId: String,
    ): OrmaBackendResult<OrmaWorkspacePaymentMethod> =
        executeBackendRequest(
            actionTitle = "Set default UPI",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/payment-methods/$paymentMethodId/default"),
                    bearerToken = idToken,
                    body = "{}",
                )
            },
            parse = { it.toWorkspacePaymentMethod() },
        )

    suspend fun deletePaymentMethod(
        idToken: String,
        paymentMethodId: String,
    ): OrmaBackendResult<OrmaWorkspacePaymentMethod> =
        executeBackendRequest(
            actionTitle = "Delete UPI",
            request = {
                ormaPutJsonAuthorized(
                    url = config.url("/payment-methods/$paymentMethodId/delete"),
                    bearerToken = idToken,
                    body = "{}",
                )
            },
            parse = { it.toWorkspacePaymentMethod() },
        )

    suspend fun getMetaConnectionStatus(
        idToken: String,
    ): OrmaBackendResult<OrmaMetaConnectionStatus> =
        executeBackendRequest(
            actionTitle = "Load Meta channels",
            request = {
                ormaGetAuthorized(
                    url = config.url("/integrations/meta/status"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toMetaConnectionStatus() },
        )

    suspend fun updateMetaConnection(
        idToken: String,
        draft: OrmaMetaConnectionDraft,
    ): OrmaBackendResult<OrmaMetaConnectionStatus> =
        executeBackendRequest(
            actionTitle = "Save Meta connection",
            request = {
                val scopesJson = draft.scopes.joinToString(prefix = "[", postfix = "]") { scope ->
                    "\"${scope.jsonEscaped()}\""
                }
                ormaPostJsonAuthorized(
                    url = config.url("/integrations/meta/connection"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "status" to JsonValue.StringValue(draft.status),
                        "connectionMode" to JsonValue.StringValue(draft.connectionMode),
                        "businessDisplayName" to JsonValue.StringValue(draft.businessDisplayName.blankToNull()),
                        "businessId" to JsonValue.StringValue(draft.businessId.blankToNull()),
                        "whatsappDisplayNumber" to JsonValue.StringValue(draft.whatsappDisplayNumber.blankToNull()),
                        "whatsappBusinessAccountId" to JsonValue.StringValue(draft.whatsappBusinessAccountId.blankToNull()),
                        "phoneNumberId" to JsonValue.StringValue(draft.phoneNumberId.blankToNull()),
                        "catalogId" to JsonValue.StringValue(draft.catalogId.blankToNull()),
                        "pageId" to JsonValue.StringValue(draft.pageId.blankToNull()),
                        "instagramBusinessAccountId" to JsonValue.StringValue(draft.instagramBusinessAccountId.blankToNull()),
                        "scopes" to JsonValue.RawValue(scopesJson),
                    ),
                )
            },
            parse = { it.toMetaConnectionStatus() },
        )

    suspend fun syncMetaCatalog(
        idToken: String,
    ): OrmaBackendResult<OrmaMetaCatalogSyncResult> =
        executeBackendRequest(
            actionTitle = "Check Meta catalog",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/integrations/meta/catalog/sync"),
                    bearerToken = idToken,
                    body = "{}",
                )
            },
            parse = { it.toMetaCatalogSyncResult() },
        )

    suspend fun listMetaWhatsAppTemplates(
        idToken: String,
    ): OrmaBackendResult<OrmaMetaWhatsAppTemplateListResult> =
        executeBackendRequest(
            actionTitle = "Load WhatsApp templates",
            request = {
                ormaGetAuthorized(
                    url = config.url("/integrations/meta/whatsapp/templates/created"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toMetaWhatsAppTemplateListResult() },
        )

    suspend fun createMetaWhatsAppTemplate(
        idToken: String,
        draft: OrmaMetaWhatsAppTemplateDraft,
    ): OrmaBackendResult<OrmaMetaWhatsAppTemplateCreateResult> =
        executeBackendRequest(
            actionTitle = "Create WhatsApp template",
            request = {
                val samplesJson = draft.sampleParameters
                    .filter { it.isNotBlank() }
                    .joinToString(prefix = "[", postfix = "]") { sample ->
                        "\"${sample.jsonEscaped()}\""
                    }
                ormaPostJsonAuthorized(
                    url = config.url("/integrations/meta/whatsapp/templates"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(draft.name),
                        "category" to JsonValue.StringValue(draft.category),
                        "languageCode" to JsonValue.StringValue(draft.languageCode.blankToNull()),
                        "bodyText" to JsonValue.StringValue(draft.bodyText),
                        "sampleParameters" to JsonValue.RawValue(samplesJson),
                    ),
                )
            },
            parse = { it.toMetaWhatsAppTemplateCreateResult() },
        )

    suspend fun syncMetaWhatsAppTemplates(
        idToken: String,
    ): OrmaBackendResult<OrmaMetaWhatsAppTemplateSyncResult> =
        executeBackendRequest(
            actionTitle = "Create WhatsApp templates",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/integrations/meta/whatsapp/templates/sync"),
                    bearerToken = idToken,
                    body = "{}",
                )
            },
            parse = { it.toMetaWhatsAppTemplateSyncResult() },
        )

    suspend fun loadPublicCatalog(
        workspaceId: String,
    ): OrmaBackendResult<OrmaPublicCatalog> =
        executeBackendRequest(
            actionTitle = "Load ordering page",
            request = {
                ormaPostJson(
                    url = config.url("/public/workspaces/${workspaceId.urlQueryEscaped()}/catalog"),
                    body = "{}",
                )
            },
            parse = { it.toPublicCatalog() },
        )

    suspend fun submitPublicCatalogOrder(
        workspaceId: String,
        draft: OrmaPublicCatalogOrderDraft,
    ): OrmaBackendResult<OrmaPublicCatalogOrderReceipt> =
        executeBackendRequest(
            actionTitle = "Submit order request",
            request = {
                val itemsJson = draft.items
                    .filter { it.productId.isNotBlank() && it.quantity.isNotBlank() }
                    .joinToString(prefix = "[", postfix = "]") { item ->
                        buildJsonObject(
                            "productId" to JsonValue.StringValue(item.productId),
                            "quantity" to JsonValue.StringValue(item.quantity.blankToZero(default = "1")),
                        )
                    }
                ormaPostJson(
                    url = config.url("/public/workspaces/${workspaceId.urlQueryEscaped()}/orders"),
                    body = buildJsonObject(
                        "customerName" to JsonValue.StringValue(draft.customerName),
                        "phoneNumber" to JsonValue.StringValue(draft.phoneNumber),
                        "notes" to JsonValue.StringValue(draft.notes.blankToNull()),
                        "fulfillmentType" to JsonValue.StringValue(draft.fulfillmentType),
                        "scheduledAt" to JsonValue.StringValue(draft.scheduledAt.blankToNull()),
                        "paymentMode" to JsonValue.StringValue(draft.paymentMode),
                        "items" to JsonValue.RawValue(itemsJson),
                    ),
                )
            },
            parse = { it.toPublicCatalogOrderReceipt() },
        )

    suspend fun loadPublicCatalogOrderStatus(
        workspaceId: String,
        orderId: String,
    ): OrmaBackendResult<OrmaPublicCatalogOrderReceipt> =
        executeBackendRequest(
            actionTitle = "Refresh request status",
            request = {
                ormaPostJson(
                    url = config.url(
                        "/public/workspaces/${workspaceId.urlQueryEscaped()}/orders/${orderId.urlQueryEscaped()}",
                    ),
                    body = "{}",
                )
            },
            parse = { it.toPublicCatalogOrderReceipt() },
        )

    private fun OrmaBackendConfig.url(path: String): String =
        baseUrl.trimEnd('/') + path

    private fun OrmaBackendConfig.urlWithDashboardFilters(
        path: String,
        filters: OrmaDashboardFilters,
    ): String {
        val params = buildList {
            filters.query.trim().takeIf { it.isNotBlank() }?.let { add("q" to it) }
            filters.orderStatus.trim().takeIf { it.isNotBlank() && it != "all" }?.let { add("status" to it) }
            filters.itemType.trim().takeIf { it.isNotBlank() && it != "all" }?.let { add("itemType" to it) }
            filters.orderType.trim().takeIf { it.isNotBlank() && it != "all" }?.let { add("orderType" to it) }
            filters.datePreset.trim().takeIf { it.isNotBlank() && it != "all" }?.let { add("datePreset" to it) }
            filters.dateFrom.trim().takeIf { it.isNotBlank() }?.let { add("dateFrom" to it) }
            filters.dateTo.trim().takeIf { it.isNotBlank() }?.let { add("dateTo" to it) }
            if (filters.lowStockOnly) add("lowStock" to "true")
            filters.supplierId.trim().takeIf { it.isNotBlank() }?.let { add("supplierId" to it) }
            filters.barcode.trim().takeIf { it.isNotBlank() }?.let { add("barcode" to it) }
            if (filters.scheduledOnly) add("scheduledOnly" to "true")
            if (filters.excludeCancelled) add("excludeCancelled" to "true")
            add("page" to filters.page.coerceAtLeast(1).toString())
            add("limit" to filters.limit.coerceIn(1, 200).toString())
        }
        if (params.isEmpty()) return url(path)
        return url(path) + params.joinToString(prefix = "?", separator = "&") { (key, value) ->
            "${key.urlQueryEscaped()}=${value.urlQueryEscaped()}"
        }
    }

    private suspend fun executeBackendSessionRequest(
        actionTitle: String,
        request: suspend () -> OrmaHttpResponse,
    ): OrmaBackendResult<OrmaBackendSession> =
        executeBackendRequest(actionTitle, request, String::toBackendSession)

    private suspend fun <T> executeBackendRequest(
        actionTitle: String,
        request: suspend () -> OrmaHttpResponse,
        parse: (String) -> T,
    ): OrmaBackendResult<T> =
        try {
            request().toBackendResult(actionTitle, parse)
        } catch (error: Throwable) {
            OrmaBackendResult.Failure(
                title = actionTitle,
                message = error.backendNetworkMessage(config.baseUrl),
                code = "BACKEND_NETWORK_ERROR",
            )
        }
}

fun createOrmaBackendClient(): OrmaBackendClient =
    OrmaBackendClient(currentOrmaBackendConfig())

private val OrmaAuthProvider.backendName: String
    get() = when (this) {
        OrmaAuthProvider.EmailPassword -> "password"
        OrmaAuthProvider.PhoneOtp -> "phone"
        OrmaAuthProvider.Google -> "google.com"
    }

private fun <T> OrmaHttpResponse.toBackendResult(
    actionTitle: String,
    parse: (String) -> T,
): OrmaBackendResult<T> {
    if (statusCode !in 200..299) {
        val backendMessage = body.jsonString("message") ?: "ORMA backend request failed."
        return OrmaBackendResult.Failure(
            title = actionTitle,
            message = backendMessage.toUserSafeBackendMessage(),
            code = body.jsonString("code") ?: backendMessage.toUserSafeBackendCode() ?: "HTTP_$statusCode",
        )
    }
    return try {
        OrmaBackendResult.Success(parse(body))
    } catch (error: Throwable) {
        OrmaBackendResult.Failure(
            title = actionTitle,
            message = error.message ?: "ORMA backend returned an unreadable response.",
            code = "BACKEND_RESPONSE_PARSE_FAILED",
        )
    }
}

private fun String.toUserSafeBackendMessage(): String =
    if (isFirebaseIdTokenExpiredMessage()) {
        "Your secure session expired. Try the action again so ORMA can refresh your sign-in."
    } else {
        this
    }

private fun String.toUserSafeBackendCode(): String? =
    if (isFirebaseIdTokenExpiredMessage()) "FIREBASE_ID_TOKEN_EXPIRED" else null

private fun String.isFirebaseIdTokenExpiredMessage(): Boolean =
    contains("Firebase ID token has expired", ignoreCase = true) ||
        contains("verify-id-tokens", ignoreCase = true)

private fun Throwable.backendNetworkMessage(baseUrl: String): String {
    val detail = message?.takeIf(String::isNotBlank)
    val target = baseUrl.trimEnd('/')
    return if (detail == null) {
        "ORMA could not reach the workspace service at $target. Check the backend connection and try again."
    } else {
        "ORMA could not reach the workspace service at $target. $detail"
    }
}

private fun String.toBackendSession(): OrmaBackendSession {
    val userJson = jsonObject("user") ?: error("Backend response is missing user.")
    val workspaceJson = jsonObject("workspace")
    return OrmaBackendSession(
        user = OrmaBackendUser(
            id = userJson.jsonString("id").orEmpty(),
            email = userJson.jsonString("email"),
            phoneNumber = userJson.jsonString("phoneNumber"),
            displayName = userJson.jsonString("displayName"),
            role = userJson.jsonString("role").orEmpty(),
            notificationsEnabled = userJson.jsonBoolean("notificationsEnabled") ?: false,
        ),
        workspace = workspaceJson?.let {
            OrmaBackendWorkspace(
                id = it.jsonString("id").orEmpty(),
                businessName = it.jsonString("businessName").orEmpty(),
                legalName = it.jsonString("legalName").orEmpty(),
                role = it.jsonString("role").orEmpty(),
                onboardingComplete = it.jsonBoolean("onboardingComplete") ?: false,
                logoFileName = it.jsonString("logoFileName"),
                logoUrl = it.jsonString("logoUrl"),
                coverFileName = it.jsonString("coverFileName"),
                coverUrl = it.jsonString("coverUrl"),
            )
        },
        onboardingStatus = jsonString("onboardingStatus").orEmpty(),
        requiredStep = jsonString("requiredStep").orEmpty(),
        accessPath = jsonString("accessPath").orEmpty(),
    )
}

private fun String.toBackendWorkspace(): OrmaBackendWorkspace =
    OrmaBackendWorkspace(
        id = jsonString("id").orEmpty(),
        businessName = jsonString("businessName").orEmpty(),
        legalName = jsonString("legalName").orEmpty(),
        role = jsonString("role").orEmpty(),
        onboardingComplete = jsonBoolean("onboardingComplete") ?: false,
        logoFileName = jsonString("logoFileName"),
        logoUrl = jsonString("logoUrl"),
        coverFileName = jsonString("coverFileName"),
        coverUrl = jsonString("coverUrl"),
    )

private fun String.toMediaUpload(): OrmaMediaUpload =
    OrmaMediaUpload(
        type = jsonString("type").orEmpty(),
        storagePath = jsonString("storagePath").orEmpty(),
        downloadUrl = jsonString("downloadUrl"),
        contentType = jsonString("contentType").orEmpty(),
        sizeBytes = jsonLong("sizeBytes") ?: 0L,
    )

private fun String.toGstinLookup(): OrmaGstinLookup {
    val dataJson = jsonObject("data")
    val addressJson = dataJson
        ?.jsonObject("pradr")
        ?.jsonObject("addr")
    val addressLine = listOfNotNull(
        addressJson?.jsonString("bno"),
        addressJson?.jsonString("bnm"),
        addressJson?.jsonString("flno"),
        addressJson?.jsonString("st"),
        addressJson?.jsonString("loc"),
    ).filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
        .takeIf { it.isNotBlank() }
    return OrmaGstinLookup(
        gstin = jsonString("gstin").orEmpty(),
        found = jsonBoolean("flag") ?: false,
        message = jsonString("message").orEmpty(),
        source = jsonString("source").orEmpty(),
        cached = jsonBoolean("cached") ?: false,
        cachedAt = jsonString("cachedAt"),
        legalName = dataJson?.jsonString("lgnm")?.takeIf(String::isNotBlank),
        tradeName = dataJson?.jsonString("tradeNam")?.takeIf(String::isNotBlank),
        addressLine = addressLine,
        city = addressJson?.jsonString("city")
            ?.takeIf(String::isNotBlank)
            ?: addressJson?.jsonString("dst")?.takeIf(String::isNotBlank),
        region = addressJson?.jsonString("stcd")?.takeIf(String::isNotBlank),
        postalCode = addressJson?.jsonString("pncd")?.takeIf(String::isNotBlank),
    )
}

private fun String.toDashboardSummary(): OrmaDashboardSummary =
    OrmaDashboardSummary(
        currency = jsonString("currency") ?: "INR",
        businessMode = jsonString("businessMode") ?: "product_selling",
        totalCustomers = jsonInt("totalCustomers") ?: 0,
        totalPaidAmount = jsonDecimalString("totalPaidAmount") ?: "0.00",
        ordersCount = jsonInt("ordersCount") ?: 0,
        bookingsCount = jsonInt("bookingsCount") ?: 0,
        salesCount = jsonInt("salesCount") ?: 0,
        serviceOrdersCount = jsonInt("serviceOrdersCount") ?: 0,
        appointmentsCount = jsonInt("appointmentsCount") ?: 0,
        todayAppointmentsCount = jsonInt("todayAppointmentsCount") ?: 0,
        productsInStock = jsonInt("productsInStock") ?: 0,
        lowStockProducts = jsonInt("lowStockProducts") ?: 0,
        recentOrders = jsonObjectsInArray("recentOrders").map { it.toOrder() },
        lowStockItems = jsonObjectsInArray("lowStockItems").map { it.toProduct() },
        revenueSeries = jsonObjectsInArray("revenueSeries").map { it.toDashboardRevenuePoint() },
        orderStatusBreakdown = jsonObjectsInArray("orderStatusBreakdown").map { it.toDashboardBreakdown() },
        orderTypeBreakdown = jsonObjectsInArray("orderTypeBreakdown").map { it.toDashboardBreakdown() },
        topItems = jsonObjectsInArray("topItems").map { it.toDashboardTopItem() },
        recentActivity = jsonObjectsInArray("recentActivity").map { it.toDashboardActivity() },
        dashboardTasks = jsonObjectsInArray("dashboardTasks").map { it.toDashboardTask() },
        notificationPreview = jsonObjectsInArray("notificationPreview").map { it.toDashboardNotificationPreview() },
    )

private fun <T> String.toPagedList(
    arrayName: String,
    mapper: (String) -> T,
): OrmaPagedList<T> {
    val items = jsonObjectsInArray(arrayName).map(mapper)
    return OrmaPagedList(
        items = items,
        pagination = jsonObject("pagination")?.toPagination(defaultCount = items.size)
            ?: OrmaPagination(totalItems = items.size),
    )
}

private fun String.toPagination(defaultCount: Int): OrmaPagination =
    OrmaPagination(
        page = (jsonInt("page") ?: 1).coerceAtLeast(1),
        pageSize = (jsonInt("pageSize") ?: 50).coerceIn(1, 200),
        totalItems = (jsonInt("totalItems") ?: defaultCount).coerceAtLeast(0),
        totalPages = (jsonInt("totalPages") ?: 0).coerceAtLeast(0),
        hasPrevious = jsonBoolean("hasPrevious") ?: false,
        hasNext = jsonBoolean("hasNext") ?: false,
    )

private fun String.toDashboardRevenuePoint(): OrmaDashboardRevenuePoint =
    OrmaDashboardRevenuePoint(
        date = jsonString("date").orEmpty(),
        amount = jsonDecimalString("amount") ?: "0.00",
        ordersCount = jsonInt("ordersCount") ?: 0,
    )

private fun String.toDashboardBreakdown(): OrmaDashboardBreakdown =
    OrmaDashboardBreakdown(
        key = jsonString("key").orEmpty(),
        label = jsonString("label").orEmpty(),
        count = jsonInt("count") ?: 0,
        amount = jsonDecimalString("amount") ?: "0.00",
    )

private fun String.toDashboardTopItem(): OrmaDashboardTopItem =
    OrmaDashboardTopItem(
        productId = jsonString("productId"),
        name = jsonString("name").orEmpty(),
        itemType = jsonString("itemType") ?: "product",
        quantity = jsonDecimalString("quantity") ?: "0",
        amount = jsonDecimalString("amount") ?: "0.00",
        imageUrl = jsonString("imageUrl"),
    )

private fun String.toDashboardActivity(): OrmaDashboardActivity =
    OrmaDashboardActivity(
        id = jsonString("id").orEmpty(),
        type = jsonString("type") ?: "activity",
        title = jsonString("title").orEmpty(),
        body = jsonString("body").orEmpty(),
        occurredAt = jsonString("occurredAt").orEmpty(),
        tone = jsonString("tone") ?: "info",
        performedByUserId = jsonString("performedByUserId"),
        performedByDisplayName = jsonString("performedByDisplayName"),
        performedByEmail = jsonString("performedByEmail"),
        performedByPhoneNumber = jsonString("performedByPhoneNumber"),
        performedByRole = jsonString("performedByRole"),
    )

private fun String.toDashboardTask(): OrmaDashboardTask =
    OrmaDashboardTask(
        id = jsonString("id").orEmpty(),
        title = jsonString("title").orEmpty(),
        body = jsonString("body").orEmpty(),
        action = jsonString("action").orEmpty(),
        priority = jsonString("priority") ?: "normal",
        tone = jsonString("tone") ?: "info",
        count = jsonInt("count") ?: 0,
    )

private fun String.toDashboardNotificationPreview(): OrmaDashboardNotificationPreview =
    OrmaDashboardNotificationPreview(
        id = jsonString("id").orEmpty(),
        title = jsonString("title").orEmpty(),
        body = jsonString("body").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        tone = jsonString("tone") ?: "info",
    )

private fun String.toTeamOverview(): OrmaTeamOverview {
    val workspaceJson = jsonObject("workspace") ?: error("Team response is missing workspace.")
    return OrmaTeamOverview(
        workspace = workspaceJson.toBackendWorkspace(),
        canInviteMembers = jsonBoolean("canInviteMembers") ?: false,
        members = jsonObjectsInArray("members").map { it.toTeamMember() },
        invites = jsonObjectsInArray("invites").map { it.toTeamInvite() },
    )
}

private fun String.toTeamMember(): OrmaTeamMember =
    OrmaTeamMember(
        id = jsonString("id").orEmpty(),
        userId = jsonString("userId").orEmpty(),
        displayName = jsonString("displayName"),
        email = jsonString("email"),
        phoneNumber = jsonString("phoneNumber"),
        role = jsonString("role").orEmpty(),
        permissions = jsonStringArray("permissions"),
        status = jsonString("status").orEmpty(),
        joinedAt = jsonString("joinedAt").orEmpty(),
    )

private fun String.toTeamInvite(): OrmaTeamInvite =
    OrmaTeamInvite(
        id = jsonString("id").orEmpty(),
        code = jsonString("code").orEmpty(),
        inviteeName = jsonString("inviteeName"),
        inviteeEmail = jsonString("inviteeEmail"),
        inviteePhoneNumber = jsonString("inviteePhoneNumber"),
        role = jsonString("role").orEmpty(),
        permissions = jsonStringArray("permissions"),
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        expiresAt = jsonString("expiresAt"),
        createdByDisplayName = jsonString("createdByDisplayName"),
        createdByEmail = jsonString("createdByEmail"),
    )

private fun String.toCustomer(): OrmaCustomer =
    OrmaCustomer(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        phoneNumber = jsonString("phoneNumber"),
        email = jsonString("email"),
        taxNumber = jsonString("taxNumber"),
        addressLine = jsonString("addressLine"),
        city = jsonString("city"),
        region = jsonString("region"),
        country = jsonString("country"),
        postalCode = jsonString("postalCode"),
        notes = jsonString("notes"),
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toSupplier(): OrmaSupplier =
    OrmaSupplier(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        phoneNumber = jsonString("phoneNumber"),
        email = jsonString("email"),
        taxNumber = jsonString("taxNumber"),
        addressLine = jsonString("addressLine"),
        paymentTerms = jsonString("paymentTerms"),
        paymentMode = jsonString("paymentMode"),
        paymentReference = jsonString("paymentReference"),
        payableTotal = jsonDecimalString("payableTotal") ?: "0.00",
        paidTotal = jsonDecimalString("paidTotal") ?: "0.00",
        balanceDue = jsonDecimalString("balanceDue") ?: "0.00",
        currency = jsonString("currency") ?: "INR",
        lastPaymentAt = jsonString("lastPaymentAt"),
        notes = jsonString("notes"),
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toProduct(): OrmaProduct =
    OrmaProduct(
        id = jsonString("id").orEmpty(),
        categoryId = jsonString("categoryId"),
        categoryName = jsonString("categoryName"),
        supplierId = jsonString("supplierId"),
        supplierName = jsonString("supplierName"),
        name = jsonString("name").orEmpty(),
        itemType = jsonString("itemType") ?: "product",
        sku = jsonString("sku"),
        barcode = jsonString("barcode"),
        description = jsonString("description"),
        unit = jsonString("unit").orEmpty(),
        sellingPrice = jsonDecimalString("sellingPrice") ?: "0.00",
        costPrice = jsonDecimalString("costPrice") ?: "0.00",
        currency = jsonString("currency") ?: "INR",
        taxRate = jsonDecimalString("taxRate") ?: "0",
        pricesIncludeTax = jsonBoolean("pricesIncludeTax") ?: false,
        stockQuantity = jsonDecimalString("stockQuantity") ?: "0",
        reorderLevel = jsonDecimalString("reorderLevel") ?: "0",
        trackStock = jsonBoolean("trackStock") ?: true,
        durationMinutes = jsonInt("durationMinutes"),
        bookingRequired = jsonBoolean("bookingRequired") ?: false,
        expiryDate = jsonString("expiryDate"),
        lowStock = jsonBoolean("lowStock") ?: false,
        status = jsonString("status").orEmpty(),
        imageUrl = jsonString("imageUrl"),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toProductCategory(): OrmaProductCategory =
    OrmaProductCategory(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        itemType = jsonString("itemType") ?: "all",
        sortOrder = jsonInt("sortOrder") ?: 0,
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toProductOffer(): OrmaProductOffer =
    OrmaProductOffer(
        id = jsonString("id").orEmpty(),
        appliesTo = jsonString("appliesTo") ?: "product",
        productId = jsonString("productId"),
        productName = jsonString("productName"),
        categoryId = jsonString("categoryId"),
        categoryName = jsonString("categoryName"),
        customerId = jsonString("customerId"),
        customerName = jsonString("customerName"),
        name = jsonString("name").orEmpty(),
        itemType = jsonString("itemType") ?: "product",
        description = jsonString("description"),
        discountType = jsonString("discountType") ?: "percentage",
        discountValue = jsonDecimalString("discountValue") ?: "0",
        discountCapAmount = jsonDecimalString("discountCapAmount"),
        couponCode = jsonString("couponCode"),
        startsAt = jsonString("startsAt"),
        endsAt = jsonString("endsAt"),
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toPublicCatalog(): OrmaPublicCatalog {
    val workspaceJson = jsonObject("workspace") ?: error("Ordering page response is missing workspace.")
    return OrmaPublicCatalog(
        workspace = workspaceJson.toPublicCatalogWorkspace(),
        categories = jsonObjectsInArray("categories").map { it.toPublicCatalogCategory() },
        paymentMethods = jsonObjectsInArray("paymentMethods").map { it.toPublicCatalogPaymentMethod() },
        products = jsonObjectsInArray("products").map { it.toPublicCatalogProduct() },
    )
}

private fun String.toPublicCatalogWorkspace(): OrmaPublicCatalogWorkspace =
    OrmaPublicCatalogWorkspace(
        id = jsonString("id").orEmpty(),
        businessName = jsonString("businessName").orEmpty(),
        industry = jsonString("industry").orEmpty(),
        city = jsonString("city").orEmpty(),
        currency = jsonString("currency") ?: "INR",
        whatsappDisplayNumber = jsonString("whatsappDisplayNumber"),
        logoUrl = jsonString("logoUrl"),
        coverUrl = jsonString("coverUrl"),
    )

private fun String.toPublicCatalogCategory(): OrmaPublicCatalogCategory =
    OrmaPublicCatalogCategory(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        sortOrder = jsonInt("sortOrder") ?: 0,
    )

private fun String.toPublicCatalogPaymentMethod(): OrmaPublicCatalogPaymentMethod =
    OrmaPublicCatalogPaymentMethod(
        id = jsonString("id").orEmpty(),
        type = jsonString("type") ?: "upi",
        label = jsonString("label").orEmpty(),
        upiId = jsonString("upiId"),
        payeeName = jsonString("payeeName"),
        isDefault = jsonBoolean("isDefault") ?: false,
    )

private fun String.toPublicCatalogOffer(): OrmaPublicCatalogOffer =
    OrmaPublicCatalogOffer(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        description = jsonString("description"),
        discountType = jsonString("discountType") ?: "percentage",
        discountValue = jsonDecimalString("discountValue") ?: "0",
        discountAmount = jsonDecimalString("discountAmount") ?: "0.00",
        finalPrice = jsonDecimalString("finalPrice") ?: "0.00",
    )

private fun String.toPublicCatalogProduct(): OrmaPublicCatalogProduct =
    OrmaPublicCatalogProduct(
        id = jsonString("id").orEmpty(),
        categoryId = jsonString("categoryId"),
        categoryName = jsonString("categoryName"),
        name = jsonString("name").orEmpty(),
        description = jsonString("description"),
        unit = jsonString("unit") ?: "pcs",
        sellingPrice = jsonDecimalString("sellingPrice") ?: "0.00",
        currency = jsonString("currency") ?: "INR",
        taxRate = jsonDecimalString("taxRate") ?: "0",
        pricesIncludeTax = jsonBoolean("pricesIncludeTax") ?: false,
        trackStock = jsonBoolean("trackStock") ?: true,
        stockQuantity = jsonDecimalString("stockQuantity") ?: "0",
        inStock = jsonBoolean("inStock") ?: false,
        durationMinutes = jsonInt("durationMinutes"),
        bookingRequired = jsonBoolean("bookingRequired") ?: false,
        imageUrl = jsonString("imageUrl"),
        offer = jsonObject("offer")?.toPublicCatalogOffer(),
    )

private fun String.toPublicCatalogOrderReceipt(): OrmaPublicCatalogOrderReceipt =
    OrmaPublicCatalogOrderReceipt(
        message = jsonString("message") ?: "Request received.",
        order = jsonObject("order")?.toOrder() ?: error("Ordering response is missing order."),
        paymentLink = jsonString("paymentLink"),
        paymentMethod = jsonObject("paymentMethod")?.toPublicCatalogPaymentMethod(),
    )

private fun String.toProductExport(): OrmaProductExport =
    OrmaProductExport(
        fileName = jsonString("fileName") ?: "orma-products.csv",
        count = jsonInt("count") ?: 0,
        csv = jsonString("csv").orEmpty(),
        columns = jsonStringArray("columns"),
    )

private fun String.toProductImportTemplate(): OrmaProductImportTemplate =
    OrmaProductImportTemplate(
        fileName = jsonString("fileName") ?: "orma-products-template.csv",
        columns = jsonStringArray("columns"),
        requiredColumns = jsonStringArray("requiredColumns"),
        csv = jsonString("csv").orEmpty(),
    )

private fun String.toProductImportResult(): OrmaProductImportResult =
    OrmaProductImportResult(
        created = jsonInt("created") ?: 0,
        skipped = jsonInt("skipped") ?: 0,
        errors = jsonObjectsInArray("errors").map {
            OrmaProductImportError(
                row = it.jsonInt("row") ?: 0,
                message = it.jsonString("message").orEmpty(),
            )
        },
        products = jsonObjectsInArray("products").map { it.toProduct() },
    )

private fun String.toOrder(): OrmaOrder =
    OrmaOrder(
        id = jsonString("id").orEmpty(),
        orderNumber = jsonString("orderNumber").orEmpty(),
        customerId = jsonString("customerId"),
        customerName = jsonString("customerName"),
        customerPhoneNumber = jsonString("customerPhoneNumber"),
        customerEmail = jsonString("customerEmail"),
        customerTaxNumber = jsonString("customerTaxNumber"),
        customerAddressLine = jsonString("customerAddressLine"),
        customerCity = jsonString("customerCity"),
        customerRegion = jsonString("customerRegion"),
        customerCountry = jsonString("customerCountry"),
        customerPostalCode = jsonString("customerPostalCode"),
        orderType = jsonString("orderType") ?: "sale",
        status = jsonString("status").orEmpty(),
        scheduledAt = jsonString("scheduledAt"),
        subtotal = jsonDecimalString("subtotal") ?: "0.00",
        taxTotal = jsonDecimalString("taxTotal") ?: "0.00",
        discountTotal = jsonDecimalString("discountTotal") ?: "0.00",
        paidTotal = jsonDecimalString("paidTotal") ?: "0.00",
        total = jsonDecimalString("total") ?: "0.00",
        currency = jsonString("currency") ?: "INR",
        notes = jsonString("notes"),
        fulfillmentType = jsonString("fulfillmentType") ?: "standard",
        paymentMode = jsonString("paymentMode") ?: "pay_on_spot",
        source = jsonString("source") ?: "dashboard",
        itemCount = jsonInt("itemCount") ?: 0,
        items = jsonObjectsInArray("items").map { it.toOrderItem() },
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toWorkspacePaymentMethod(): OrmaWorkspacePaymentMethod =
    OrmaWorkspacePaymentMethod(
        id = jsonString("id").orEmpty(),
        type = jsonString("type") ?: "upi",
        label = jsonString("label").orEmpty(),
        upiId = jsonString("upiId"),
        payeeName = jsonString("payeeName"),
        isDefault = jsonBoolean("isDefault") ?: false,
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toOrderItem(): OrmaOrderItem =
    OrmaOrderItem(
        id = jsonString("id").orEmpty(),
        productId = jsonString("productId"),
        productName = jsonString("productName"),
        description = jsonString("description").orEmpty(),
        quantity = jsonDecimalString("quantity") ?: "0",
        unitPrice = jsonDecimalString("unitPrice") ?: "0.00",
        taxRate = jsonDecimalString("taxRate") ?: "0",
        lineSubtotal = jsonDecimalString("lineSubtotal") ?: "0.00",
        lineTax = jsonDecimalString("lineTax") ?: "0.00",
        lineTotal = jsonDecimalString("lineTotal") ?: "0.00",
    )

private fun String.toPrinterProfile(): OrmaPrinterProfile =
    OrmaPrinterProfile(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        connectionType = jsonString("connectionType") ?: "mtp_usb",
        address = jsonString("address"),
        paperWidthMm = jsonInt("paperWidthMm") ?: 80,
        dpi = jsonInt("dpi") ?: 203,
        supportsReceipts = jsonBoolean("supportsReceipts") ?: true,
        supportsBarcodes = jsonBoolean("supportsBarcodes") ?: true,
        isDefaultReceipt = jsonBoolean("isDefaultReceipt") ?: false,
        isDefaultBarcode = jsonBoolean("isDefaultBarcode") ?: false,
        notes = jsonString("notes"),
        status = jsonString("status").orEmpty(),
        createdAt = jsonString("createdAt").orEmpty(),
        updatedAt = jsonString("updatedAt").orEmpty(),
    )

private fun String.toMetaConnectionStatus(): OrmaMetaConnectionStatus =
    OrmaMetaConnectionStatus(
        connected = jsonBoolean("connected") ?: false,
        status = jsonString("status") ?: "not_connected",
        connectionMode = jsonString("connectionMode") ?: "manual_setup",
        businessDisplayName = jsonString("businessDisplayName"),
        businessId = jsonString("businessId"),
        whatsappDisplayNumber = jsonString("whatsappDisplayNumber"),
        whatsappBusinessAccountId = jsonString("whatsappBusinessAccountId"),
        phoneNumberId = jsonString("phoneNumberId"),
        catalogId = jsonString("catalogId"),
        pageId = jsonString("pageId"),
        instagramBusinessAccountId = jsonString("instagramBusinessAccountId"),
        scopes = jsonStringArray("scopes"),
        accessTokenStatus = jsonString("accessTokenStatus") ?: "not_configured",
        credentialSource = jsonString("credentialSource") ?: "none",
        tokenExpiresAt = jsonString("tokenExpiresAt"),
        webhookSubscribedAt = jsonString("webhookSubscribedAt"),
        messagingStatus = jsonString("messagingStatus") ?: "not_configured",
        lastSyncAt = jsonString("lastSyncAt"),
        lastError = jsonString("lastError"),
        productsReady = jsonInt("productsReady") ?: 0,
        productsBlocked = jsonInt("productsBlocked") ?: 0,
        productsSynced = jsonInt("productsSynced") ?: 0,
        productReadiness = jsonObjectsInArray("productReadiness").map { it.toMetaProductReadiness() },
    )

private fun String.toMetaCatalogSyncResult(): OrmaMetaCatalogSyncResult =
    OrmaMetaCatalogSyncResult(
        connected = jsonBoolean("connected") ?: false,
        productsReady = jsonInt("productsReady") ?: 0,
        productsBlocked = jsonInt("productsBlocked") ?: 0,
        productsSynced = jsonInt("productsSynced") ?: 0,
        productReadiness = jsonObjectsInArray("productReadiness").map { it.toMetaProductReadiness() },
        message = jsonString("message") ?: "Catalog readiness checked.",
    )

private fun String.toMetaWhatsAppTemplateListResult(): OrmaMetaWhatsAppTemplateListResult =
    OrmaMetaWhatsAppTemplateListResult(
        connected = jsonBoolean("connected") ?: false,
        templates = jsonObjectsInArray("templates").map { it.toMetaWhatsAppTemplate() },
        message = jsonString("message") ?: "WhatsApp templates loaded.",
    )

private fun String.toMetaWhatsAppTemplateCreateResult(): OrmaMetaWhatsAppTemplateCreateResult =
    OrmaMetaWhatsAppTemplateCreateResult(
        created = jsonBoolean("created") ?: false,
        status = jsonString("status") ?: "unknown",
        template = jsonObject("template")?.toMetaWhatsAppTemplate(),
        message = jsonString("message") ?: "WhatsApp template submitted.",
    )

private fun String.toMetaWhatsAppTemplateSyncResult(): OrmaMetaWhatsAppTemplateSyncResult =
    OrmaMetaWhatsAppTemplateSyncResult(
        connected = jsonBoolean("connected") ?: false,
        created = jsonInt("created") ?: 0,
        failed = jsonInt("failed") ?: 0,
        templates = jsonObjectsInArray("templates").map {
            OrmaMetaWhatsAppTemplateSyncItem(
                name = it.jsonString("name").orEmpty(),
                status = it.jsonString("status") ?: "unknown",
                message = it.jsonString("message") ?: "",
            )
        },
        message = jsonString("message") ?: "WhatsApp template sync finished.",
    )

private fun String.toMetaProductReadiness(): OrmaMetaProductReadiness =
    OrmaMetaProductReadiness(
        productId = jsonString("productId").orEmpty(),
        productName = jsonString("productName").orEmpty(),
        ready = jsonBoolean("ready") ?: false,
        status = jsonString("status") ?: "not_synced",
        issues = jsonStringArray("issues"),
        metaProductId = jsonString("metaProductId"),
        lastSyncAt = jsonString("lastSyncAt"),
    )

private fun String.toMetaWhatsAppTemplate(): OrmaMetaWhatsAppTemplate =
    OrmaMetaWhatsAppTemplate(
        id = jsonString("id"),
        name = jsonString("name").orEmpty(),
        status = jsonString("status") ?: "unknown",
        category = jsonString("category") ?: "UTILITY",
        languageCode = jsonString("languageCode") ?: "en_US",
        bodyText = jsonString("bodyText"),
        rejectedReason = jsonString("rejectedReason"),
    )

private fun String.normalizedGstin(): String =
    uppercase().filter(Char::isLetterOrDigit).take(15)

private sealed interface JsonValue {
    data class StringValue(val value: String?) : JsonValue
    data class BooleanValue(val value: Boolean) : JsonValue
    data class IntValue(val value: Int?) : JsonValue
    data class RawValue(val value: String) : JsonValue
}

private fun buildJsonObject(vararg fields: Pair<String, JsonValue>): String =
    fields.joinToString(prefix = "{", postfix = "}") { (key, value) ->
        val encodedValue = when (value) {
            is JsonValue.BooleanValue -> value.value.toString()
            is JsonValue.IntValue -> value.value?.toString() ?: "null"
            is JsonValue.RawValue -> value.value
            is JsonValue.StringValue -> {
                if (value.value == null) {
                    "null"
                } else {
                    "\"${value.value.jsonEscaped()}\""
                }
            }
        }
        "\"$key\":$encodedValue"
    }

private fun OrmaSupplierDraft.toSupplierRequestJson(): String =
    buildJsonObject(
        "name" to JsonValue.StringValue(name),
        "phoneNumber" to JsonValue.StringValue(phoneNumber.blankToNull()),
        "email" to JsonValue.StringValue(email.blankToNull()),
        "taxNumber" to JsonValue.StringValue(taxNumber.blankToNull()),
        "addressLine" to JsonValue.StringValue(addressLine.blankToNull()),
        "paymentTerms" to JsonValue.StringValue(paymentTerms.blankToNull()),
        "paymentMode" to JsonValue.StringValue(paymentMode.blankToNull()),
        "paymentReference" to JsonValue.StringValue(paymentReference.blankToNull()),
        "payableTotal" to JsonValue.StringValue(payableTotal.blankToZero()),
        "paidTotal" to JsonValue.StringValue(paidTotal.blankToZero()),
        "currency" to JsonValue.StringValue(currency.ifBlank { "INR" }),
        "lastPaymentAt" to JsonValue.StringValue(lastPaymentAt.blankToNull()),
        "notes" to JsonValue.StringValue(notes.blankToNull()),
    )

private fun OrmaProductDraft.toProductRequestJson(): String =
    buildJsonObject(
        "name" to JsonValue.StringValue(name),
        "itemType" to JsonValue.StringValue(itemType),
        "categoryId" to JsonValue.StringValue(categoryId.blankToNull()),
        "categoryName" to JsonValue.StringValue(categoryName.blankToNull()),
        "sku" to JsonValue.StringValue(sku.blankToNull()),
        "barcode" to JsonValue.StringValue(barcode.blankToNull()),
        "description" to JsonValue.StringValue(description.blankToNull()),
        "unit" to JsonValue.StringValue(unit),
        "sellingPrice" to JsonValue.StringValue(sellingPrice.blankToZero()),
        "costPrice" to JsonValue.StringValue(costPrice.blankToZero()),
        "currency" to JsonValue.StringValue(currency),
        "taxRate" to JsonValue.StringValue(taxRate.blankToZero()),
        "pricesIncludeTax" to JsonValue.BooleanValue(pricesIncludeTax),
        "stockQuantity" to JsonValue.StringValue(stockQuantity.blankToZero()),
        "reorderLevel" to JsonValue.StringValue(reorderLevel.blankToZero()),
        "trackStock" to JsonValue.BooleanValue(trackStock),
        "durationMinutes" to JsonValue.IntValue(durationMinutes.intValueOrNull()),
        "bookingRequired" to JsonValue.BooleanValue(bookingRequired),
        "expiryDate" to JsonValue.StringValue(expiryDate.blankToNull()),
        "supplierId" to JsonValue.StringValue(supplierId.blankToNull()),
        "status" to JsonValue.StringValue(status.ifBlank { "active" }),
    )

private fun OrmaPrinterDraft.toPrinterRequestJson(): String =
    buildJsonObject(
        "name" to JsonValue.StringValue(name.trim()),
        "connectionType" to JsonValue.StringValue(connectionType),
        "address" to JsonValue.StringValue(address.blankToNull()),
        "paperWidthMm" to JsonValue.RawValue(paperWidthMm.intInput(default = "80")),
        "dpi" to JsonValue.RawValue(dpi.intInput(default = "203")),
        "supportsReceipts" to JsonValue.BooleanValue(supportsReceipts),
        "supportsBarcodes" to JsonValue.BooleanValue(supportsBarcodes),
        "isDefaultReceipt" to JsonValue.BooleanValue(isDefaultReceipt && supportsReceipts),
        "isDefaultBarcode" to JsonValue.BooleanValue(isDefaultBarcode && supportsBarcodes),
        "notes" to JsonValue.StringValue(notes.blankToNull()),
    )

private fun OrmaOrderDraft.toOrderRequestJson(): String {
    val itemsJson = items
        .filter { it.description.isNotBlank() || it.productId.isNotBlank() }
        .joinToString(prefix = "[", postfix = "]") { item ->
            buildJsonObject(
                "productId" to JsonValue.StringValue(item.productId.blankToNull()),
                "description" to JsonValue.StringValue(item.description),
                "quantity" to JsonValue.StringValue(item.quantity.blankToZero(default = "1")),
                "unitPrice" to JsonValue.StringValue(item.unitPrice.blankToZero()),
                "taxRate" to JsonValue.StringValue(item.taxRate.blankToZero()),
            )
        }
    return buildJsonObject(
        "customerId" to JsonValue.StringValue(customerId.blankToNull()),
        "customerName" to JsonValue.StringValue(customerName.blankToNull()),
        "customerPhoneNumber" to JsonValue.StringValue(customerPhoneNumber.blankToNull()),
        "customerEmail" to JsonValue.StringValue(customerEmail.blankToNull()),
        "customerTaxNumber" to JsonValue.StringValue(customerTaxNumber.blankToNull()),
        "customerAddressLine" to JsonValue.StringValue(customerAddressLine.blankToNull()),
        "customerCity" to JsonValue.StringValue(customerCity.blankToNull()),
        "customerRegion" to JsonValue.StringValue(customerRegion.blankToNull()),
        "customerCountry" to JsonValue.StringValue(customerCountry.blankToNull()),
        "customerPostalCode" to JsonValue.StringValue(customerPostalCode.blankToNull()),
        "orderType" to JsonValue.StringValue(orderType),
        "status" to JsonValue.StringValue(status),
        "scheduledAt" to JsonValue.StringValue(scheduledAt.blankToNull()),
        "paidTotal" to JsonValue.StringValue(paidTotal.blankToZero()),
        "discountTotal" to JsonValue.StringValue(discountTotal.blankToZero()),
        "currency" to JsonValue.StringValue(currency),
        "notes" to JsonValue.StringValue(notes.blankToNull()),
        "fulfillmentType" to JsonValue.StringValue(fulfillmentType),
        "paymentMode" to JsonValue.StringValue(paymentMode),
        "items" to JsonValue.RawValue(itemsJson),
    )
}

private fun String.jsonString(key: String): String? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    return pattern.find(this)?.groupValues?.get(1)?.jsonUnescaped()
}

private fun String.jsonBoolean(key: String): Boolean? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(true|false)")
    return pattern.find(this)?.groupValues?.get(1)?.toBooleanStrictOrNull()
}

private fun String.jsonLong(key: String): Long? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(\\d+)")
    return pattern.find(this)?.groupValues?.get(1)?.toLongOrNull()
}

private fun String.jsonInt(key: String): Int? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(-?\\d+)")
    return pattern.find(this)?.groupValues?.get(1)?.toIntOrNull()
}

private fun String.jsonDecimalString(key: String): String? =
    jsonString(key) ?: Regex("\"${Regex.escape(key)}\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
        .find(this)
        ?.groupValues
        ?.get(1)

private fun String.jsonObject(key: String): String? {
    val marker = "\"${key.jsonEscaped()}\""
    val keyIndex = indexOf(marker)
    if (keyIndex < 0) return null
    val colonIndex = indexOf(':', startIndex = keyIndex + marker.length)
    if (colonIndex < 0) return null
    val start = indexOf('{', startIndex = colonIndex + 1)
    if (start < 0) return null

    var depth = 0
    var inString = false
    var escaped = false
    for (index in start until length) {
        val char = this[index]
        when {
            escaped -> escaped = false
            char == '\\' && inString -> escaped = true
            char == '"' -> inString = !inString
            !inString && char == '{' -> depth += 1
            !inString && char == '}' -> {
                depth -= 1
                if (depth == 0) return substring(start, index + 1)
            }
        }
    }
    return null
}

private fun String.jsonArray(key: String): String? {
    val marker = "\"${key.jsonEscaped()}\""
    val keyIndex = indexOf(marker)
    if (keyIndex < 0) return null
    val colonIndex = indexOf(':', startIndex = keyIndex + marker.length)
    if (colonIndex < 0) return null
    val start = indexOf('[', startIndex = colonIndex + 1)
    if (start < 0) return null

    var depth = 0
    var inString = false
    var escaped = false
    for (index in start until length) {
        val char = this[index]
        when {
            escaped -> escaped = false
            char == '\\' && inString -> escaped = true
            char == '"' -> inString = !inString
            !inString && char == '[' -> depth += 1
            !inString && char == ']' -> {
                depth -= 1
                if (depth == 0) return substring(start, index + 1)
            }
        }
    }
    return null
}

private fun String.jsonStringArray(key: String): List<String> {
    val array = jsonArray(key) ?: return emptyList()
    return Regex("\"((?:\\\\.|[^\"])*)\"")
        .findAll(array)
        .map { it.groupValues[1].jsonUnescaped() }
        .toList()
}

private fun String.jsonObjectsInArray(key: String): List<String> {
    val array = jsonArray(key) ?: return emptyList()
    val objects = mutableListOf<String>()
    var objectStart = -1
    var objectDepth = 0
    var inString = false
    var escaped = false
    for (index in array.indices) {
        val char = array[index]
        when {
            escaped -> escaped = false
            char == '\\' && inString -> escaped = true
            char == '"' -> inString = !inString
            !inString && char == '{' -> {
                if (objectDepth == 0) objectStart = index
                objectDepth += 1
            }
            !inString && char == '}' -> {
                objectDepth -= 1
                if (objectDepth == 0 && objectStart >= 0) {
                    objects += array.substring(objectStart, index + 1)
                    objectStart = -1
                }
            }
        }
    }
    return objects
}

private fun String.jsonEscaped(): String =
    buildString {
        for (char in this@jsonEscaped) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

private fun List<String>.jsonStringArrayLiteral(): String =
    joinToString(prefix = "[", postfix = "]") { "\"${it.jsonEscaped()}\"" }

private fun String.jsonUnescaped(): String =
    replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")

private fun String.toProductImportCsvRows(defaultCurrency: String): List<OrmaProductImportCsvRow> {
    val rows = parseCsvRows()
        .filter { row -> row.any { it.isNotBlank() } }
    if (rows.isEmpty()) return emptyList()
    val headers = rows.first().map { it.normalizedCsvHeader() }
    if ("name" !in headers) {
        throw IllegalArgumentException("CSV header must include a name column.")
    }
    fun List<String>.value(vararg aliases: String): String {
        val index = aliases
            .map { it.normalizedCsvHeader() }
            .firstNotNullOfOrNull { alias -> headers.indexOf(alias).takeIf { it >= 0 } }
            ?: return ""
        return getOrNull(index).orEmpty().trim()
    }
    return rows.drop(1).mapNotNull { row ->
        if (row.all { it.isBlank() }) return@mapNotNull null
        OrmaProductImportCsvRow(
            name = row.value("name", "productName", "itemName"),
            itemType = row.value("itemType", "item type", "type", "sellableType").ifBlank { "product" },
            categoryName = row.value("categoryName", "category name", "category"),
            sku = row.value("sku"),
            barcode = row.value("barcode", "barCode"),
            description = row.value("description", "details"),
            unit = row.value("unit").ifBlank { "pcs" },
            sellingPrice = row.value("sellingPrice", "selling price", "price").ifBlank { "0" },
            costPrice = row.value("costPrice", "cost price", "cost").ifBlank { "0" },
            currency = row.value("currency").ifBlank { defaultCurrency.ifBlank { "INR" } },
            taxRate = row.value("taxRate", "tax rate", "tax").ifBlank { "0" },
            pricesIncludeTax = row.value("pricesIncludeTax", "prices include tax").csvBoolean(defaultValue = false),
            stockQuantity = row.value("stockQuantity", "stock quantity", "stock", "openingStock").ifBlank { "0" },
            reorderLevel = row.value("reorderLevel", "reorder level", "lowStockLevel").ifBlank { "0" },
            trackStock = row.value("trackStock", "track stock").csvBoolean(defaultValue = true),
            durationMinutes = row.value("durationMinutes", "duration minutes", "duration", "minutes"),
            bookingRequired = row.value("bookingRequired", "booking required", "requiresBooking").csvBoolean(defaultValue = false),
            expiryDate = row.value("expiryDate", "expiry date", "expiresAt", "expiry", "bestBefore"),
            supplierName = row.value("supplierName", "supplier name", "supplier"),
        )
    }
}

private fun List<OrmaProductImportCsvRow>.toProductImportRowsJson(): String =
    joinToString(prefix = "[", postfix = "]") { row ->
        buildJsonObject(
            "name" to JsonValue.StringValue(row.name),
            "itemType" to JsonValue.StringValue(row.itemType),
            "categoryName" to JsonValue.StringValue(row.categoryName.blankToNull()),
            "sku" to JsonValue.StringValue(row.sku.blankToNull()),
            "barcode" to JsonValue.StringValue(row.barcode.blankToNull()),
            "description" to JsonValue.StringValue(row.description.blankToNull()),
            "unit" to JsonValue.StringValue(row.unit.ifBlank { "pcs" }),
            "sellingPrice" to JsonValue.StringValue(row.sellingPrice.blankToZero()),
            "costPrice" to JsonValue.StringValue(row.costPrice.blankToZero()),
            "currency" to JsonValue.StringValue(row.currency.blankToNull()),
            "taxRate" to JsonValue.StringValue(row.taxRate.blankToZero()),
            "pricesIncludeTax" to JsonValue.BooleanValue(row.pricesIncludeTax),
            "stockQuantity" to JsonValue.StringValue(row.stockQuantity.blankToZero()),
            "reorderLevel" to JsonValue.StringValue(row.reorderLevel.blankToZero()),
            "trackStock" to JsonValue.BooleanValue(row.trackStock),
            "durationMinutes" to JsonValue.IntValue(row.durationMinutes.intValueOrNull()),
            "bookingRequired" to JsonValue.BooleanValue(row.bookingRequired),
            "expiryDate" to JsonValue.StringValue(row.expiryDate.blankToNull()),
            "supplierName" to JsonValue.StringValue(row.supplierName.blankToNull()),
        )
    }

private fun String.parseCsvRows(): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var index = 0
    while (index < length) {
        val char = this[index]
        when {
            inQuotes && char == '"' && getOrNull(index + 1) == '"' -> {
                field.append('"')
                index += 1
            }
            char == '"' -> inQuotes = !inQuotes
            !inQuotes && char == ',' -> {
                row += field.toString()
                field.clear()
            }
            !inQuotes && (char == '\n' || char == '\r') -> {
                if (char == '\r' && getOrNull(index + 1) == '\n') index += 1
                row += field.toString()
                field.clear()
                rows += row.toList()
                row.clear()
            }
            else -> field.append(char)
        }
        index += 1
    }
    row += field.toString()
    if (row.any { it.isNotBlank() }) rows += row.toList()
    return rows
}

private fun String.normalizedCsvHeader(): String =
    trim().lowercase().filter(Char::isLetterOrDigit)

private fun String.csvBoolean(defaultValue: Boolean): Boolean =
    when (trim().lowercase()) {
        "true", "1", "yes", "y" -> true
        "false", "0", "no", "n" -> false
        else -> defaultValue
    }

private fun String.blankToNull(): String? =
    trim().ifBlank { null }

private fun String.blankToZero(default: String = "0"): String =
    trim().ifBlank { default }

private fun String.intInput(default: String): String =
    filter(Char::isDigit).take(4).ifBlank { default }

private fun String.intValueOrNull(): Int? =
    trim().takeIf { it.isNotBlank() }?.toIntOrNull()?.takeIf { it > 0 }

private fun String.backendOrderActionTitle(): String =
    when (trim().lowercase()) {
        "service" -> "Create service"
        "appointment" -> "Book appointment"
        else -> "Create sale"
    }

private fun String.urlQueryEscaped(): String =
    encodeToByteArray().joinToString(separator = "") { byte ->
        val value = byte.toInt() and 0xff
        val char = value.toChar()
        if (
            char in 'A'..'Z' ||
            char in 'a'..'z' ||
            char in '0'..'9' ||
            char == '-' ||
            char == '_' ||
            char == '.' ||
            char == '~'
        ) {
            char.toString()
        } else {
            "%" + value.toString(16).uppercase().padStart(2, '0')
        }
    }

private fun String.urlPathEscaped(): String = urlQueryEscaped()
