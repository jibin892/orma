package com.orma.backend.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiInfoResponse(
    val service: String,
    val status: String,
)

@Serializable
data class HealthResponse(
    val status: String,
    val environment: String,
    val databaseConfigured: Boolean,
    val firebaseAuthConfigured: Boolean,
    val firebaseMessagingConfigured: Boolean,
    val firebaseStorageConfigured: Boolean,
    val mediaStorageProvider: String,
    val mediaStorageConfigured: Boolean,
    val cloudinaryConfigured: Boolean,
    val gstinCheckConfigured: Boolean,
    val metaWebhookConfigured: Boolean = false,
)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
)

@Serializable
data class SessionRequest(
    val idToken: String,
    val provider: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val displayName: String? = null,
)

@Serializable
data class SessionResponse(
    val uid: String,
    val email: String?,
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val user: UserResponse? = null,
    val workspace: WorkspaceResponse? = null,
    val pendingInvite: TeamInviteResponse? = null,
    val onboardingStatus: String = "unknown",
    val requiredStep: String = "unknown",
    val accessPath: String = "business_owner",
)

@Serializable
data class UserResponse(
    val id: String,
    val firebaseUid: String,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val role: String,
    val notificationsEnabled: Boolean,
)

@Serializable
data class WorkspaceResponse(
    val id: String,
    val businessName: String,
    val legalName: String,
    val role: String,
    val onboardingComplete: Boolean,
    val logoFileName: String? = null,
    val logoUrl: String? = null,
    val inviteCode: String? = null,
)

@Serializable
data class BusinessSetupRequest(
    val ownerName: String,
    val businessName: String,
    val legalName: String,
    val industry: String,
    val website: String = "",
    val isTaxRegistered: Boolean,
    val taxNumber: String = "",
    val taxLabel: String,
    val addressLine: String,
    val city: String,
    val region: String = "",
    val country: String,
    val postalCode: String = "",
    val logoFileName: String = "",
    val invoicePrefix: String,
    val nextInvoiceNumber: String,
    val paymentTerms: String,
    val invoiceFooter: String,
    val currency: String,
    val taxMode: String,
    val pricesIncludeTax: Boolean,
)

@Serializable
data class TeamInviteLookupRequest(
    val code: String,
)

@Serializable
data class TeamInviteCreateRequest(
    val name: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val role: String = "team_member",
)

@Serializable
data class TeamInviteJoinRequest(
    val code: String,
    val displayName: String = "",
)

@Serializable
data class TeamInviteResponse(
    val code: String,
    val workspace: WorkspaceResponse,
    val inviteeName: String? = null,
    val inviteeEmail: String? = null,
    val inviteePhoneNumber: String? = null,
    val role: String? = null,
)

@Serializable
data class TeamOverviewResponse(
    val workspace: WorkspaceResponse,
    val canInviteMembers: Boolean,
    val members: List<TeamMemberResponse>,
    val pendingInvites: List<TeamInviteListItemResponse>,
)

@Serializable
data class TeamMemberResponse(
    val id: String,
    val userId: String,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val role: String,
    val status: String,
    val joinedAt: String,
)

@Serializable
data class TeamInviteListItemResponse(
    val code: String,
    val inviteeName: String,
    val inviteeEmail: String? = null,
    val inviteePhoneNumber: String? = null,
    val role: String,
    val status: String,
    val createdAt: String,
    val expiresAt: String? = null,
)

@Serializable
data class NotificationPreferenceRequest(
    val enabled: Boolean,
    val deviceToken: String? = null,
    val platform: String? = null,
    val deviceName: String? = null,
)

@Serializable
data class OnboardingMutationResponse(
    val user: UserResponse,
    val workspace: WorkspaceResponse?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

@Serializable
data class MediaUploadResponse(
    val type: String,
    val id: String? = null,
    val workspaceId: String? = null,
    val productId: String? = null,
    val storagePath: String,
    val downloadUrl: String? = null,
    val contentType: String,
    val sizeBytes: Long,
)

@Serializable
data class GstinLookupResponse(
    val gstin: String,
    val flag: Boolean,
    val message: String,
    val data: JsonElement? = null,
    val source: String,
    val cached: Boolean,
    val cachedAt: String,
)

@Serializable
data class DashboardSummaryResponse(
    val currency: String,
    val totalCustomers: Int,
    val totalPaidAmount: String,
    val ordersCount: Int,
    val bookingsCount: Int,
    val productsInStock: Int,
    val lowStockProducts: Int,
    val recentOrders: List<OrderResponse> = emptyList(),
    val lowStockItems: List<ProductResponse> = emptyList(),
)

@Serializable
data class CustomerListResponse(
    val customers: List<CustomerResponse>,
)

@Serializable
data class CustomerRequest(
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val notes: String? = null,
)

@Serializable
data class CustomerResponse(
    val id: String,
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val notes: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SupplierListResponse(
    val suppliers: List<SupplierResponse>,
)

@Serializable
data class SupplierRequest(
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val taxNumber: String? = null,
    val addressLine: String? = null,
    val notes: String? = null,
)

@Serializable
data class SupplierResponse(
    val id: String,
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val taxNumber: String? = null,
    val addressLine: String? = null,
    val notes: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ProductListResponse(
    val products: List<ProductResponse>,
)

@Serializable
data class ProductImportTemplateResponse(
    val fileName: String,
    val columns: List<String>,
    val requiredColumns: List<String>,
    val csv: String,
)

@Serializable
data class ProductImportCsvRequest(
    val csv: String,
)

@Serializable
data class ProductImportRequest(
    val rows: List<ProductImportRowRequest>,
)

@Serializable
data class ProductImportRowRequest(
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val unit: String = "pcs",
    val sellingPrice: String = "0",
    val costPrice: String = "0",
    val currency: String? = null,
    val taxRate: String = "0",
    val pricesIncludeTax: Boolean = false,
    val stockQuantity: String = "0",
    val reorderLevel: String = "0",
    val trackStock: Boolean = true,
    val supplierName: String? = null,
)

@Serializable
data class ProductImportResponse(
    val created: Int,
    val skipped: Int,
    val errors: List<ProductImportErrorResponse> = emptyList(),
    val products: List<ProductResponse> = emptyList(),
)

@Serializable
data class ProductImportErrorResponse(
    val row: Int,
    val message: String,
)

@Serializable
data class ProductExportResponse(
    val fileName: String,
    val count: Int,
    val csv: String,
    val columns: List<String> = emptyList(),
)

@Serializable
data class ProductRequest(
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val unit: String = "pcs",
    val sellingPrice: String = "0",
    val costPrice: String = "0",
    val currency: String? = null,
    val taxRate: String = "0",
    val pricesIncludeTax: Boolean = false,
    val stockQuantity: String = "0",
    val reorderLevel: String = "0",
    val trackStock: Boolean = true,
    val supplierId: String? = null,
)

@Serializable
data class ProductResponse(
    val id: String,
    val supplierId: String? = null,
    val supplierName: String? = null,
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
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
    val imageUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PublicCatalogWorkspaceResponse(
    val id: String,
    val businessName: String,
    val industry: String,
    val city: String,
    val currency: String,
    val logoUrl: String? = null,
)

@Serializable
data class PublicCatalogProductResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val unit: String,
    val sellingPrice: String,
    val currency: String,
    val taxRate: String,
    val pricesIncludeTax: Boolean,
    val trackStock: Boolean,
    val stockQuantity: String,
    val inStock: Boolean,
    val imageUrl: String? = null,
)

@Serializable
data class PublicCatalogResponse(
    val workspace: PublicCatalogWorkspaceResponse,
    val products: List<PublicCatalogProductResponse>,
)

@Serializable
data class PublicCatalogOrderRequest(
    val customerName: String,
    val phoneNumber: String,
    val notes: String? = null,
    val items: List<PublicCatalogOrderItemRequest>,
)

@Serializable
data class PublicCatalogOrderItemRequest(
    val productId: String,
    val quantity: String = "1",
)

@Serializable
data class PublicCatalogOrderResponse(
    val message: String,
    val order: OrderResponse,
)

@Serializable
data class StockAdjustmentRequest(
    val quantityDelta: String,
    val note: String? = null,
)

@Serializable
data class StockMovementResponse(
    val id: String,
    val productId: String,
    val movementType: String,
    val quantityDelta: String,
    val balanceAfter: String,
    val note: String? = null,
    val createdAt: String,
)

@Serializable
data class OrderListResponse(
    val orders: List<OrderResponse>,
)

@Serializable
data class OrderRequest(
    val customerId: String? = null,
    val customerName: String? = null,
    val status: String = "confirmed",
    val scheduledAt: String? = null,
    val paidTotal: String = "0",
    val currency: String? = null,
    val notes: String? = null,
    val items: List<OrderItemRequest>,
)

@Serializable
data class OrderItemRequest(
    val productId: String? = null,
    val description: String,
    val quantity: String,
    val unitPrice: String,
    val taxRate: String = "0",
)

@Serializable
data class OrderStatusRequest(
    val status: String,
)

@Serializable
data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val status: String,
    val scheduledAt: String? = null,
    val subtotal: String,
    val taxTotal: String,
    val discountTotal: String,
    val paidTotal: String,
    val total: String,
    val currency: String,
    val notes: String? = null,
    val itemCount: Int,
    val items: List<OrderItemResponse> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OrderItemResponse(
    val id: String,
    val productId: String? = null,
    val productName: String? = null,
    val description: String,
    val quantity: String,
    val unitPrice: String,
    val taxRate: String,
    val lineSubtotal: String,
    val lineTax: String,
    val lineTotal: String,
)

@Serializable
data class PrinterProfileListResponse(
    val printers: List<PrinterProfileResponse>,
)

@Serializable
data class PrinterProfileRequest(
    val name: String,
    val connectionType: String = "mtp_usb",
    val address: String? = null,
    val paperWidthMm: Int = 80,
    val dpi: Int = 203,
    val supportsReceipts: Boolean = true,
    val supportsBarcodes: Boolean = true,
    val isDefaultReceipt: Boolean = false,
    val isDefaultBarcode: Boolean = false,
    val notes: String? = null,
)

@Serializable
data class PrinterProfileResponse(
    val id: String,
    val name: String,
    val connectionType: String,
    val address: String? = null,
    val paperWidthMm: Int,
    val dpi: Int,
    val supportsReceipts: Boolean,
    val supportsBarcodes: Boolean,
    val isDefaultReceipt: Boolean,
    val isDefaultBarcode: Boolean,
    val notes: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class MetaConnectionRequest(
    val status: String = "connected",
    val businessId: String? = null,
    val whatsappBusinessAccountId: String? = null,
    val phoneNumberId: String? = null,
    val catalogId: String? = null,
    val pageId: String? = null,
    val instagramBusinessAccountId: String? = null,
    val scopes: List<String> = emptyList(),
)

@Serializable
data class MetaConnectionStatusResponse(
    val connected: Boolean,
    val status: String,
    val businessId: String? = null,
    val whatsappBusinessAccountId: String? = null,
    val phoneNumberId: String? = null,
    val catalogId: String? = null,
    val pageId: String? = null,
    val instagramBusinessAccountId: String? = null,
    val scopes: List<String> = emptyList(),
    val lastSyncAt: String? = null,
    val lastError: String? = null,
    val productsReady: Int = 0,
    val productsBlocked: Int = 0,
    val productsSynced: Int = 0,
    val productReadiness: List<MetaProductReadinessResponse> = emptyList(),
)

@Serializable
data class MetaProductReadinessResponse(
    val productId: String,
    val productName: String,
    val ready: Boolean,
    val status: String,
    val issues: List<String> = emptyList(),
    val metaProductId: String? = null,
    val lastSyncAt: String? = null,
)

@Serializable
data class MetaCatalogSyncResponse(
    val connected: Boolean,
    val productsReady: Int,
    val productsBlocked: Int,
    val productsSynced: Int,
    val productReadiness: List<MetaProductReadinessResponse> = emptyList(),
    val message: String,
)

@Serializable
data class MetaWebhookEventResponse(
    val id: String,
    val status: String,
)
