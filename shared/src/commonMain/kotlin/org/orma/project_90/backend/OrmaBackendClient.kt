package org.orma.project_90.backend

import org.orma.project_90.auth.OrmaAuthProvider
import org.orma.project_90.auth.OrmaAuthSession
import org.orma.project_90.auth.OrmaHttpResponse
import org.orma.project_90.auth.ormaGetAuthorized
import org.orma.project_90.auth.ormaPostJson
import org.orma.project_90.auth.ormaPostJsonAuthorized
import org.orma.project_90.auth.ormaPostMultipartAuthorized
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
    val inviteCode: String?,
)

data class OrmaBackendSession(
    val user: OrmaBackendUser,
    val workspace: OrmaBackendWorkspace?,
    val pendingInvite: OrmaTeamInvite?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

data class OrmaTeamInvite(
    val code: String,
    val workspace: OrmaBackendWorkspace,
    val inviteeName: String?,
    val inviteeEmail: String?,
    val inviteePhoneNumber: String?,
    val role: String?,
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
    val totalCustomers: Int = 0,
    val totalPaidAmount: String = "0.00",
    val ordersCount: Int = 0,
    val bookingsCount: Int = 0,
    val productsInStock: Int = 0,
    val lowStockProducts: Int = 0,
    val recentOrders: List<OrmaOrder> = emptyList(),
    val lowStockItems: List<OrmaProduct> = emptyList(),
)

data class OrmaCustomer(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val addressLine: String?,
    val city: String?,
    val region: String?,
    val country: String?,
    val postalCode: String?,
    val notes: String?,
    val status: String,
)

data class OrmaSupplier(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val taxNumber: String?,
    val addressLine: String?,
    val notes: String?,
    val status: String,
)

data class OrmaProduct(
    val id: String,
    val supplierId: String?,
    val supplierName: String?,
    val name: String,
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
    val lowStock: Boolean,
    val status: String,
)

data class OrmaOrder(
    val id: String,
    val orderNumber: String,
    val customerId: String?,
    val customerName: String?,
    val status: String,
    val scheduledAt: String?,
    val subtotal: String,
    val taxTotal: String,
    val discountTotal: String,
    val paidTotal: String,
    val total: String,
    val currency: String,
    val notes: String?,
    val itemCount: Int,
    val items: List<OrmaOrderItem> = emptyList(),
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
    val notes: String = "",
)

data class OrmaProductDraft(
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val description: String = "",
    val unit: String = "pcs",
    val sellingPrice: String = "",
    val costPrice: String = "",
    val currency: String = "INR",
    val taxRate: String = "0",
    val pricesIncludeTax: Boolean = false,
    val stockQuantity: String = "0",
    val reorderLevel: String = "0",
    val trackStock: Boolean = true,
    val supplierId: String = "",
)

data class OrmaOrderDraft(
    val customerId: String = "",
    val customerName: String = "",
    val status: String = "confirmed",
    val scheduledAt: String = "",
    val paidTotal: String = "0",
    val currency: String = "INR",
    val notes: String = "",
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

    suspend fun joinTeamInvite(
        idToken: String,
        code: String,
        displayName: String,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Join workspace"
        return executeBackendSessionRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/team-invites/join"),
                bearerToken = idToken,
                body = buildJsonObject(
                    "code" to JsonValue.StringValue(code),
                    "displayName" to JsonValue.StringValue(displayName),
                ),
            )
        }
    }

    suspend fun getActiveTeamInvite(
        idToken: String,
    ): OrmaBackendResult<OrmaTeamInvite> {
        val actionTitle = "Load team invite"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaGetAuthorized(
                    url = config.url("/onboarding/team-invites/active"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toTeamInvite() },
        )
    }

    suspend fun createTeamInvite(
        idToken: String,
        name: String,
        email: String?,
        phoneNumber: String?,
        role: String,
    ): OrmaBackendResult<OrmaTeamInvite> {
        val actionTitle = "Create team invite"
        return executeBackendRequest(
            actionTitle = actionTitle,
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/onboarding/team-invites"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(name),
                        "email" to JsonValue.StringValue(email),
                        "phoneNumber" to JsonValue.StringValue(phoneNumber),
                        "role" to JsonValue.StringValue(role),
                    ),
                )
            },
            parse = { it.toTeamInvite() },
        )
    }

    suspend fun updateNotificationPreference(
        idToken: String,
        enabled: Boolean,
    ): OrmaBackendResult<OrmaBackendSession> {
        val actionTitle = "Save notifications"
        return executeBackendSessionRequest(actionTitle) {
            ormaPostJsonAuthorized(
                url = config.url("/onboarding/notifications"),
                bearerToken = idToken,
                body = buildJsonObject("enabled" to JsonValue.BooleanValue(enabled)),
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

    suspend fun getDashboardSummary(idToken: String): OrmaBackendResult<OrmaDashboardSummary> =
        executeBackendRequest(
            actionTitle = "Load dashboard",
            request = {
                ormaGetAuthorized(
                    url = config.url("/dashboard/summary"),
                    bearerToken = idToken,
                )
            },
            parse = { it.toDashboardSummary() },
        )

    suspend fun listCustomers(idToken: String): OrmaBackendResult<List<OrmaCustomer>> =
        executeBackendRequest(
            actionTitle = "Load customers",
            request = {
                ormaGetAuthorized(
                    url = config.url("/customers"),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.jsonObjectsInArray("customers").map { it.toCustomer() } },
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

    suspend fun listSuppliers(idToken: String): OrmaBackendResult<List<OrmaSupplier>> =
        executeBackendRequest(
            actionTitle = "Load suppliers",
            request = {
                ormaGetAuthorized(
                    url = config.url("/suppliers"),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.jsonObjectsInArray("suppliers").map { it.toSupplier() } },
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
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(draft.name),
                        "phoneNumber" to JsonValue.StringValue(draft.phoneNumber.blankToNull()),
                        "email" to JsonValue.StringValue(draft.email.blankToNull()),
                        "taxNumber" to JsonValue.StringValue(draft.taxNumber.blankToNull()),
                        "addressLine" to JsonValue.StringValue(draft.addressLine.blankToNull()),
                        "notes" to JsonValue.StringValue(draft.notes.blankToNull()),
                    ),
                )
            },
            parse = { it.toSupplier() },
        )

    suspend fun listProducts(idToken: String): OrmaBackendResult<List<OrmaProduct>> =
        executeBackendRequest(
            actionTitle = "Load products",
            request = {
                ormaGetAuthorized(
                    url = config.url("/products"),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.jsonObjectsInArray("products").map { it.toProduct() } },
        )

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
                    body = buildJsonObject(
                        "name" to JsonValue.StringValue(draft.name),
                        "sku" to JsonValue.StringValue(draft.sku.blankToNull()),
                        "barcode" to JsonValue.StringValue(draft.barcode.blankToNull()),
                        "description" to JsonValue.StringValue(draft.description.blankToNull()),
                        "unit" to JsonValue.StringValue(draft.unit),
                        "sellingPrice" to JsonValue.StringValue(draft.sellingPrice.blankToZero()),
                        "costPrice" to JsonValue.StringValue(draft.costPrice.blankToZero()),
                        "currency" to JsonValue.StringValue(draft.currency),
                        "taxRate" to JsonValue.StringValue(draft.taxRate.blankToZero()),
                        "pricesIncludeTax" to JsonValue.BooleanValue(draft.pricesIncludeTax),
                        "stockQuantity" to JsonValue.StringValue(draft.stockQuantity.blankToZero()),
                        "reorderLevel" to JsonValue.StringValue(draft.reorderLevel.blankToZero()),
                        "trackStock" to JsonValue.BooleanValue(draft.trackStock),
                        "supplierId" to JsonValue.StringValue(draft.supplierId.blankToNull()),
                    ),
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
                    ),
                )
            },
            parse = { it.toProduct() },
        )

    suspend fun listOrders(idToken: String): OrmaBackendResult<List<OrmaOrder>> =
        executeBackendRequest(
            actionTitle = "Load orders",
            request = {
                ormaGetAuthorized(
                    url = config.url("/orders"),
                    bearerToken = idToken,
                )
            },
            parse = { body -> body.jsonObjectsInArray("orders").map { it.toOrder() } },
        )

    suspend fun createOrder(
        idToken: String,
        draft: OrmaOrderDraft,
    ): OrmaBackendResult<OrmaOrder> =
        executeBackendRequest(
            actionTitle = "Create order",
            request = {
                val itemsJson = draft.items
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
                ormaPostJsonAuthorized(
                    url = config.url("/orders"),
                    bearerToken = idToken,
                    body = buildJsonObject(
                        "customerId" to JsonValue.StringValue(draft.customerId.blankToNull()),
                        "customerName" to JsonValue.StringValue(draft.customerName.blankToNull()),
                        "status" to JsonValue.StringValue(draft.status),
                        "scheduledAt" to JsonValue.StringValue(draft.scheduledAt.blankToNull()),
                        "paidTotal" to JsonValue.StringValue(draft.paidTotal.blankToZero()),
                        "currency" to JsonValue.StringValue(draft.currency),
                        "notes" to JsonValue.StringValue(draft.notes.blankToNull()),
                        "items" to JsonValue.RawValue(itemsJson),
                    ),
                )
            },
            parse = { it.toOrder() },
        )

    suspend fun updateOrderStatus(
        idToken: String,
        orderId: String,
        status: String,
    ): OrmaBackendResult<OrmaOrder> =
        executeBackendRequest(
            actionTitle = "Update order status",
            request = {
                ormaPostJsonAuthorized(
                    url = config.url("/orders/$orderId/status"),
                    bearerToken = idToken,
                    body = buildJsonObject("status" to JsonValue.StringValue(status)),
                )
            },
            parse = { it.toOrder() },
        )

    private fun OrmaBackendConfig.url(path: String): String =
        baseUrl.trimEnd('/') + path

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
        return OrmaBackendResult.Failure(
            title = actionTitle,
            message = body.jsonString("message") ?: "ORMA backend request failed.",
            code = body.jsonString("code") ?: "HTTP_$statusCode",
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
                inviteCode = it.jsonString("inviteCode"),
            )
        },
        pendingInvite = jsonObject("pendingInvite")?.toTeamInviteFromObject(),
        onboardingStatus = jsonString("onboardingStatus").orEmpty(),
        requiredStep = jsonString("requiredStep").orEmpty(),
        accessPath = jsonString("accessPath").orEmpty(),
    )
}

private fun String.toTeamInvite(): OrmaTeamInvite {
    val workspaceJson = jsonObject("workspace") ?: error("Backend response is missing workspace.")
    return toTeamInviteFromObject(workspaceJson)
}

private fun String.toTeamInviteFromObject(
    workspaceJson: String = jsonObject("workspace") ?: error("Backend response is missing workspace."),
): OrmaTeamInvite {
    return OrmaTeamInvite(
        code = jsonString("code").orEmpty(),
        workspace = workspaceJson.toBackendWorkspace(),
        inviteeName = jsonString("inviteeName"),
        inviteeEmail = jsonString("inviteeEmail"),
        inviteePhoneNumber = jsonString("inviteePhoneNumber"),
        role = jsonString("role"),
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
        inviteCode = jsonString("inviteCode"),
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
        totalCustomers = jsonInt("totalCustomers") ?: 0,
        totalPaidAmount = jsonDecimalString("totalPaidAmount") ?: "0.00",
        ordersCount = jsonInt("ordersCount") ?: 0,
        bookingsCount = jsonInt("bookingsCount") ?: 0,
        productsInStock = jsonInt("productsInStock") ?: 0,
        lowStockProducts = jsonInt("lowStockProducts") ?: 0,
        recentOrders = jsonObjectsInArray("recentOrders").map { it.toOrder() },
        lowStockItems = jsonObjectsInArray("lowStockItems").map { it.toProduct() },
    )

private fun String.toCustomer(): OrmaCustomer =
    OrmaCustomer(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        phoneNumber = jsonString("phoneNumber"),
        email = jsonString("email"),
        addressLine = jsonString("addressLine"),
        city = jsonString("city"),
        region = jsonString("region"),
        country = jsonString("country"),
        postalCode = jsonString("postalCode"),
        notes = jsonString("notes"),
        status = jsonString("status").orEmpty(),
    )

private fun String.toSupplier(): OrmaSupplier =
    OrmaSupplier(
        id = jsonString("id").orEmpty(),
        name = jsonString("name").orEmpty(),
        phoneNumber = jsonString("phoneNumber"),
        email = jsonString("email"),
        taxNumber = jsonString("taxNumber"),
        addressLine = jsonString("addressLine"),
        notes = jsonString("notes"),
        status = jsonString("status").orEmpty(),
    )

private fun String.toProduct(): OrmaProduct =
    OrmaProduct(
        id = jsonString("id").orEmpty(),
        supplierId = jsonString("supplierId"),
        supplierName = jsonString("supplierName"),
        name = jsonString("name").orEmpty(),
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
        lowStock = jsonBoolean("lowStock") ?: false,
        status = jsonString("status").orEmpty(),
    )

private fun String.toOrder(): OrmaOrder =
    OrmaOrder(
        id = jsonString("id").orEmpty(),
        orderNumber = jsonString("orderNumber").orEmpty(),
        customerId = jsonString("customerId"),
        customerName = jsonString("customerName"),
        status = jsonString("status").orEmpty(),
        scheduledAt = jsonString("scheduledAt"),
        subtotal = jsonDecimalString("subtotal") ?: "0.00",
        taxTotal = jsonDecimalString("taxTotal") ?: "0.00",
        discountTotal = jsonDecimalString("discountTotal") ?: "0.00",
        paidTotal = jsonDecimalString("paidTotal") ?: "0.00",
        total = jsonDecimalString("total") ?: "0.00",
        currency = jsonString("currency") ?: "INR",
        notes = jsonString("notes"),
        itemCount = jsonInt("itemCount") ?: 0,
        items = jsonObjectsInArray("items").map { it.toOrderItem() },
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

private fun String.normalizedGstin(): String =
    uppercase().filter(Char::isLetterOrDigit).take(15)

private sealed interface JsonValue {
    data class StringValue(val value: String?) : JsonValue
    data class BooleanValue(val value: Boolean) : JsonValue
    data class RawValue(val value: String) : JsonValue
}

private fun buildJsonObject(vararg fields: Pair<String, JsonValue>): String =
    fields.joinToString(prefix = "{", postfix = "}") { (key, value) ->
        val encodedValue = when (value) {
            is JsonValue.BooleanValue -> value.value.toString()
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

private fun String.jsonUnescaped(): String =
    replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")

private fun String.blankToNull(): String? =
    trim().ifBlank { null }

private fun String.blankToZero(default: String = "0"): String =
    trim().ifBlank { default }
