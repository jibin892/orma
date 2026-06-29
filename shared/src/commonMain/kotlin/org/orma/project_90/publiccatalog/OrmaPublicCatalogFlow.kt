package org.orma.project_90.publiccatalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orma.project_90.auth.OrmaAuthResult
import org.orma.project_90.auth.OrmaAuthSession
import org.orma.project_90.auth.createOrmaAuthGateway
import org.orma.project_90.backend.OrmaBackendResult
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaOrderItem
import org.orma.project_90.backend.OrmaOrderSession
import org.orma.project_90.backend.OrmaPublicCatalog
import org.orma.project_90.backend.OrmaPublicCatalogOrderDraft
import org.orma.project_90.backend.OrmaPublicCatalogOrderItemDraft
import org.orma.project_90.backend.OrmaPublicCatalogOrderReceipt
import org.orma.project_90.backend.OrmaPublicCatalogPaymentMethod
import org.orma.project_90.backend.OrmaPublicCatalogProduct
import org.orma.project_90.backend.OrmaProductVariant
import org.orma.project_90.backend.createOrmaBackendClient
import org.orma.project_90.backend.ormaClientRequestId
import org.orma.project_90.designsystem.OrmaAdaptiveSurface
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaBrandMark
import org.orma.project_90.designsystem.OrmaCalendarDateTimeField
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaFlatIcon
import org.orma.project_90.designsystem.OrmaFlatIconKind
import org.orma.project_90.designsystem.OrmaFullButton
import org.orma.project_90.designsystem.OrmaKeyValueList
import org.orma.project_90.designsystem.OrmaLightButton
import org.orma.project_90.designsystem.OrmaPrice
import org.orma.project_90.designsystem.OrmaQrCode
import org.orma.project_90.designsystem.OrmaSectionHeader
import org.orma.project_90.designsystem.OrmaSegmentedRow
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaSkeleton
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.OrmaTextField
import org.orma.project_90.designsystem.OrmaWindowClass
import org.orma.project_90.media.OrmaRemoteImage
import org.orma.project_90.onboarding.OrmaCountryPhoneField
import org.orma.project_90.onboarding.isOrmaInternationalPhoneValid
import kotlin.math.roundToInt

@Composable
fun OrmaPublicCatalogFlow(
    workspaceId: String,
    modifier: Modifier = Modifier,
) {
    val client = remember { createOrmaBackendClient() }
    val authGateway = remember { createOrmaAuthGateway() }
    val scope = rememberCoroutineScope()
    var catalog by remember(workspaceId) { mutableStateOf<OrmaPublicCatalog?>(null) }
    var loading by remember(workspaceId) { mutableStateOf(true) }
    var submitting by remember(workspaceId) { mutableStateOf(false) }
    var statusRefreshing by remember(workspaceId) { mutableStateOf(false) }
    var error by remember(workspaceId) { mutableStateOf<String?>(null) }
    var receipt by remember(workspaceId) { mutableStateOf<OrmaPublicCatalogOrderReceipt?>(null) }
    var quantities by remember(workspaceId) { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var customerName by remember(workspaceId) { mutableStateOf("") }
    var phoneNumber by remember(workspaceId) { mutableStateOf("") }
    var notes by remember(workspaceId) { mutableStateOf("") }
    var selectedCategoryId by remember(workspaceId) { mutableStateOf("all") }
    var fulfillmentType by remember(workspaceId) { mutableStateOf("take_away") }
    var scheduledAt by remember(workspaceId) { mutableStateOf("") }
    var paymentMode by remember(workspaceId) { mutableStateOf("pay_on_spot") }
    var checkoutRequestId by remember(workspaceId) { mutableStateOf(ormaClientRequestId("catalog")) }
    var customerSession by remember(workspaceId) { mutableStateOf<OrmaAuthSession?>(null) }
    var customerOrders by remember(workspaceId) { mutableStateOf<List<OrmaOrder>>(emptyList()) }
    var customerOrdersLoading by remember(workspaceId) { mutableStateOf(false) }
    var customerAuthBusy by remember(workspaceId) { mutableStateOf(false) }
    var customerAuthMessage by remember(workspaceId) { mutableStateOf<String?>(null) }
    var customerAuthError by remember(workspaceId) { mutableStateOf<String?>(null) }
    var loginPhoneNumber by remember(workspaceId) { mutableStateOf("") }
    var loginOtp by remember(workspaceId) { mutableStateOf("") }
    var loginOtpSent by remember(workspaceId) { mutableStateOf(false) }
    var customerProfileOpen by remember(workspaceId) { mutableStateOf(false) }
    var customerOrderDetailReceipt by remember(workspaceId) { mutableStateOf<OrmaPublicCatalogOrderReceipt?>(null) }
    var customerOrderDetailLoading by remember(workspaceId) { mutableStateOf(false) }
    var customerOrderDetailRefreshing by remember(workspaceId) { mutableStateOf(false) }
    var customerOrderDetailError by remember(workspaceId) { mutableStateOf<String?>(null) }
    var customerStatusNotice by remember(workspaceId) { mutableStateOf<PublicCatalogCustomerStatusNotice?>(null) }

    fun updateQuantity(cartKey: String, quantity: Int) {
        val productId = cartKey.publicCatalogCartProductId()
        val products = catalog?.products.orEmpty()
        val product = products.firstOrNull { it.id == productId } ?: return
        val activeType = products.firstOrNull { item ->
            item.id != productId && quantities.any { (key, count) ->
                count > 0 && key.publicCatalogCartProductId() == item.id
            }
        }?.itemType?.publicCatalogNormalizedItemType()
        val productType = product.itemType.publicCatalogNormalizedItemType()
        if (quantity > 0 && activeType != null && activeType != productType && (quantities[cartKey] ?: 0) == 0) {
            error = "Clear the ${activeType.publicCatalogItemTypeLabel().lowercase()} selection before choosing ${productType.publicCatalogItemTypeLabel().lowercase()} items."
            return
        }
        val variant = cartKey.publicCatalogCartVariantId()?.let { variantId ->
            product.variants.firstOrNull { it.id == variantId }
        }
        val maxQuantity = product.publicCatalogMaxSelectableQuantity(variant)
        quantities = quantities.toMutableMap().apply {
            if (quantity <= 0 || maxQuantity <= 0) remove(cartKey) else put(cartKey, quantity.coerceIn(1, maxQuantity))
        }
    }

    suspend fun loadCatalogAndReceiptStatus(refreshReceipt: Boolean) {
        if (catalog == null) loading = true
        val orderReceipt = receipt
        statusRefreshing = refreshReceipt && orderReceipt != null
        error = null
        when (val result = client.loadPublicCatalog(workspaceId)) {
            is OrmaBackendResult.Success -> catalog = result.value
            is OrmaBackendResult.Failure -> error = result.publicCatalogMessage(load = true)
        }
        if (refreshReceipt && orderReceipt != null) {
            when (val result = client.loadPublicCatalogOrderStatus(workspaceId, orderReceipt.order.id)) {
                is OrmaBackendResult.Success -> {
                    result.value.order.publicCatalogCustomerStatusNotice(orderReceipt.order)?.let { notice ->
                        customerStatusNotice = notice
                    }
                    receipt = result.value
                    customerOrderDetailReceipt = customerOrderDetailReceipt.publicCatalogReplaceReceipt(result.value)
                    customerOrders = customerOrders.publicCatalogReplaceOrder(result.value.order)
                }
                is OrmaBackendResult.Failure -> error = result.publicCatalogMessage(load = true)
            }
        }
        loading = false
        statusRefreshing = false
    }

    suspend fun refreshCurrentReceiptStatus(showRefreshing: Boolean) {
        val currentReceipt = receipt ?: return
        if (showRefreshing) statusRefreshing = true
        if (showRefreshing) error = null
        when (val result = client.loadPublicCatalogOrderStatus(workspaceId, currentReceipt.order.id)) {
            is OrmaBackendResult.Success -> {
                result.value.order.publicCatalogCustomerStatusNotice(currentReceipt.order)?.let { notice ->
                    customerStatusNotice = notice
                }
                receipt = result.value
                customerOrderDetailReceipt = customerOrderDetailReceipt.publicCatalogReplaceReceipt(result.value)
                customerOrders = customerOrders.publicCatalogReplaceOrder(result.value.order)
            }
            is OrmaBackendResult.Failure -> {
                if (showRefreshing) error = result.publicCatalogMessage(load = true)
            }
        }
        if (showRefreshing) statusRefreshing = false
    }

    fun applyCustomerSession(session: OrmaAuthSession) {
        customerSession = session
        customerAuthError = null
        customerAuthMessage = null
        session.publicCatalogCustomerNameFallback().takeIf { it.isNotBlank() }?.let { name ->
            if (customerName.isBlank()) customerName = name
        }
        session.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
            if (phoneNumber.isBlank()) phoneNumber = phone
            if (loginPhoneNumber.isBlank()) loginPhoneNumber = phone
        }
    }

    suspend fun loadCustomerOrderHistory(
        session: OrmaAuthSession,
        showLoading: Boolean = true,
        detectStatusUpdates: Boolean = false,
    ) {
        val previousOrders = customerOrders.associateBy { it.id }
        if (showLoading) customerOrdersLoading = true
        when (val result = client.loadPublicCatalogCustomerOrders(workspaceId, session.idToken)) {
            is OrmaBackendResult.Success -> {
                val nextOrders = result.value.items
                if (detectStatusUpdates) {
                    nextOrders.publicCatalogCustomerStatusNotice(previousOrders)?.let { notice ->
                        customerStatusNotice = notice
                    }
                }
                customerOrders = nextOrders
                customerAuthError = null
            }
            is OrmaBackendResult.Failure -> {
                if (showLoading) {
                    customerOrders = emptyList()
                    customerAuthError = result.publicCatalogMessage(load = true)
                }
            }
        }
        if (showLoading) customerOrdersLoading = false
    }

    suspend fun loadCustomerOrderDetail(orderId: String, refresh: Boolean) {
        if (orderId.isBlank()) return
        if (refresh) {
            customerOrderDetailRefreshing = true
        } else {
            customerOrderDetailLoading = true
        }
        customerOrderDetailError = null
        when (val result = client.loadPublicCatalogOrderStatus(workspaceId, orderId)) {
            is OrmaBackendResult.Success -> {
                result.value.order.publicCatalogCustomerStatusNotice(customerOrderDetailReceipt?.order)?.let { notice ->
                    customerStatusNotice = notice
                }
                customerOrderDetailReceipt = result.value
                receipt = receipt.publicCatalogReplaceReceipt(result.value)
                customerOrders = customerOrders.publicCatalogReplaceOrder(result.value.order)
                customerAuthError = null
            }
            is OrmaBackendResult.Failure -> {
                customerOrderDetailError = result.publicCatalogMessage(load = true)
            }
        }
        customerOrderDetailLoading = false
        customerOrderDetailRefreshing = false
    }

    suspend fun handleCustomerAuthResult(result: OrmaAuthResult) {
        when (result) {
            is OrmaAuthResult.OtpSent -> {
                loginOtpSent = true
                customerAuthMessage = result.message
                customerAuthError = null
            }
            is OrmaAuthResult.Success -> {
                applyCustomerSession(result.session)
                customerAuthMessage = result.message
                loginOtp = ""
                loginOtpSent = false
                loadCustomerOrderHistory(result.session)
            }
            is OrmaAuthResult.Failure -> {
                customerAuthError = result.message
                customerAuthMessage = null
            }
        }
    }

    LaunchedEffect(workspaceId) {
        loadCatalogAndReceiptStatus(refreshReceipt = false)
    }

    LaunchedEffect(receipt?.order?.id) {
        val activeOrderId = receipt?.order?.id?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        while (true) {
            delay(15_000)
            val currentOrder = receipt?.order ?: break
            if (currentOrder.id != activeOrderId || !currentOrder.status.publicCatalogShouldPollForCustomerStatus()) break
            refreshCurrentReceiptStatus(showRefreshing = false)
        }
    }

    LaunchedEffect(workspaceId) {
        customerAuthBusy = true
        when (val restored = authGateway.restoreSession()) {
            is OrmaAuthResult.Success -> {
                applyCustomerSession(restored.session)
                loadCustomerOrderHistory(restored.session)
            }
            is OrmaAuthResult.Failure -> {
                customerAuthError = null
                customerOrders = emptyList()
            }
            is OrmaAuthResult.OtpSent,
            null -> Unit
        }
        customerAuthBusy = false
    }

    LaunchedEffect(customerSession?.idToken) {
        val activeSession = customerSession ?: return@LaunchedEffect
        while (true) {
            delay(30_000)
            if (customerSession?.idToken != activeSession.idToken) break
            loadCustomerOrderHistory(
                session = activeSession,
                showLoading = false,
                detectStatusUpdates = true,
            )
        }
    }

    OrmaAdaptiveSurface(modifier = modifier) {
        val visibleProducts = catalog?.products
            .orEmpty()
            .filter { selectedCategoryId == "all" || it.categoryId == selectedCategoryId }
        val selectedItems = catalog?.products
            .orEmpty()
            .flatMap { product ->
                quantities.mapNotNull { (cartKey, quantity) ->
                    if (quantity <= 0 || cartKey.publicCatalogCartProductId() != product.id) {
                        null
                    } else {
                        val variant = cartKey.publicCatalogCartVariantId()?.let { variantId ->
                            product.variants.firstOrNull { it.id == variantId }
                        }
                        PublicCatalogSelection(product = product, variant = variant, quantity = quantity)
                    }
                }
            }
        val total = selectedItems.sumOf { selection ->
            selection.customerPrice.toDoubleOrNull().orZero() * selection.quantity
        }
        val selectedItemTypes = selectedItems
            .map { it.product.itemType.publicCatalogNormalizedItemType() }
            .distinct()
        val selectedCartItemType = selectedItemTypes.singleOrNull()
        val mixedSelection = selectedItemTypes.size > 1
        val selectedFlow = selectedItems.catalogOrderFlow()
        val appointmentRequired = selectedFlow == "appointment"
        val fulfillmentOptions = selectedFlow.publicCatalogFulfillmentOptions()
        val effectiveCustomerName = customerName.trim()
            .ifBlank { customerSession?.publicCatalogCustomerNameFallback().orEmpty() }
        val effectiveFulfillmentType = when {
            appointmentRequired -> "booking"
            fulfillmentType in fulfillmentOptions -> fulfillmentType
            else -> fulfillmentOptions.first()
        }
        val scheduledReady = (!appointmentRequired && effectiveFulfillmentType != "scheduled") || scheduledAt.trim().length >= 4
        val submitEnabled = !submitting &&
            selectedItems.isNotEmpty() &&
            !mixedSelection &&
            effectiveCustomerName.length >= 2 &&
            isOrmaInternationalPhoneValid(phoneNumber) &&
            scheduledReady

        fun submit() {
            if (!submitEnabled) {
                error = when {
                    selectedItems.isEmpty() -> "Select at least one item before continuing."
                    mixedSelection -> "Keep one checkout type at a time. Clear the cart before mixing products, services, and appointments."
                    effectiveCustomerName.length < 2 -> "Enter your name to submit this request."
                    (appointmentRequired || effectiveFulfillmentType == "scheduled") && scheduledAt.trim().length < 4 -> "Choose the preferred date or time for this booking."
                    else -> "Enter a valid phone number with country code so the business can contact you."
                }
                return
            }
            scope.launch {
                submitting = true
                error = null
                val draft = OrmaPublicCatalogOrderDraft(
                    clientRequestId = checkoutRequestId,
                    customerName = effectiveCustomerName,
                    phoneNumber = phoneNumber.trim(),
                    customerEmail = customerSession?.email.orEmpty(),
                    notes = notes.trim(),
                    fulfillmentType = effectiveFulfillmentType,
                    scheduledAt = if (appointmentRequired || effectiveFulfillmentType == "scheduled") scheduledAt.trim() else "",
                    paymentMode = paymentMode,
                    items = selectedItems.map {
                        OrmaPublicCatalogOrderItemDraft(
                            productId = it.product.id,
                            variantId = it.variant?.id,
                            quantity = it.quantity.toString(),
                        )
                    },
                )
                when (val result = client.submitPublicCatalogOrder(workspaceId, draft, customerSession?.idToken)) {
                    is OrmaBackendResult.Success -> {
                        receipt = result.value
                        customerStatusNotice = null
                        customerSession?.let { loadCustomerOrderHistory(it) }
                    }
                    is OrmaBackendResult.Failure -> error = result.publicCatalogMessage(load = false)
                }
                submitting = false
            }
        }

        val customerAccountState = PublicCatalogCustomerAccountState(
            session = customerSession,
            orders = customerOrders,
            ordersLoading = customerOrdersLoading,
            authBusy = customerAuthBusy,
            message = customerAuthMessage,
            error = customerAuthError,
            phoneNumber = loginPhoneNumber,
            otp = loginOtp,
            otpSent = loginOtpSent,
        )
        val customerAccountActions = PublicCatalogCustomerAccountActions(
            onPhoneChange = {
                loginPhoneNumber = it
                if (!loginOtpSent) customerAuthError = null
            },
            onOtpChange = {
                loginOtp = it
                customerAuthError = null
            },
            onSendOtp = {
                scope.launch {
                    customerAuthBusy = true
                    customerAuthError = null
                    val phone = loginPhoneNumber.ifBlank { phoneNumber }
                    if (loginPhoneNumber.isBlank()) loginPhoneNumber = phone
                    handleCustomerAuthResult(authGateway.requestPhoneOtp(phone))
                    customerAuthBusy = false
                }
            },
            onVerifyOtp = {
                scope.launch {
                    customerAuthBusy = true
                    customerAuthError = null
                    handleCustomerAuthResult(authGateway.verifyPhoneOtp(loginOtp))
                    customerAuthBusy = false
                }
            },
            onGoogleSignIn = {
                scope.launch {
                    customerAuthBusy = true
                    customerAuthError = null
                    handleCustomerAuthResult(authGateway.signInWithGoogle())
                    customerAuthBusy = false
                }
            },
            onRefreshOrders = {
                customerSession?.let { session ->
                    scope.launch { loadCustomerOrderHistory(session) }
                }
            },
            onLogout = {
                scope.launch {
                    customerAuthBusy = true
                    authGateway.clearStoredSession()
                    customerSession = null
                    customerOrders = emptyList()
                    customerProfileOpen = false
                    customerOrderDetailReceipt = null
                    customerOrderDetailError = null
                    customerAuthError = null
                    customerAuthMessage = "Guest checkout is active."
                    loginOtp = ""
                    loginOtpSent = false
                    customerAuthBusy = false
                }
            },
        )

        when (this) {
            OrmaWindowClass.Mobile -> PublicCatalogMobile(
                catalog = catalog,
                loading = loading,
                error = error,
                receipt = receipt,
                quantities = quantities,
                selectedCategoryId = selectedCategoryId,
                selectedCartItemType = selectedCartItemType,
                visibleProducts = visibleProducts,
                customerName = customerName,
                phoneNumber = phoneNumber,
                notes = notes,
                fulfillmentType = fulfillmentType,
                scheduledAt = scheduledAt,
                paymentMode = paymentMode,
                customerAccountState = customerAccountState,
                customerOrderDetailReceipt = customerOrderDetailReceipt,
                customerOrderDetailLoading = customerOrderDetailLoading,
                customerOrderDetailRefreshing = customerOrderDetailRefreshing,
                customerOrderDetailError = customerOrderDetailError,
                customerProfileOpen = customerProfileOpen,
                selectedItems = selectedItems,
                selectedFlow = selectedFlow,
                total = total,
                submitting = submitting,
                statusRefreshing = statusRefreshing,
                customerStatusNotice = customerStatusNotice,
                submitEnabled = submitEnabled,
                onRetry = {
                    scope.launch {
                        if (receipt != null) {
                            refreshCurrentReceiptStatus(showRefreshing = true)
                        } else {
                            loadCatalogAndReceiptStatus(refreshReceipt = false)
                        }
                    }
                },
                onQuantityChange = ::updateQuantity,
                onClearSelection = {
                    quantities = emptyMap()
                    checkoutRequestId = ormaClientRequestId("catalog")
                    error = null
                },
                onNewOrder = {
                    receipt = null
                    customerStatusNotice = null
                    quantities = emptyMap()
                    checkoutRequestId = ormaClientRequestId("catalog")
                    notes = ""
                    scheduledAt = ""
                    error = null
                },
                onCategoryChange = { selectedCategoryId = it },
                onCustomerNameChange = { customerName = it },
                onPhoneChange = { phoneNumber = it },
                onNotesChange = { notes = it },
                onFulfillmentChange = { fulfillmentType = it },
                onScheduledAtChange = { scheduledAt = it },
                onPaymentModeChange = { paymentMode = it },
                customerAccountActions = customerAccountActions,
                onCustomerProfileOpen = {
                    customerSession?.let { session ->
                        customerProfileOpen = true
                        scope.launch { loadCustomerOrderHistory(session) }
                    }
                },
                onCustomerProfileClose = { customerProfileOpen = false },
                onCustomerOrderOpen = { order ->
                    scope.launch { loadCustomerOrderDetail(order.id, refresh = false) }
                },
                onCustomerOrderDetailBack = {
                    customerOrderDetailReceipt = null
                    customerOrderDetailError = null
                },
                onCustomerOrderDetailRefresh = {
                    val detailOrderId = customerOrderDetailReceipt?.order?.id.orEmpty()
                    scope.launch { loadCustomerOrderDetail(detailOrderId, refresh = true) }
                },
                onSubmit = ::submit,
            )
            OrmaWindowClass.Wide -> PublicCatalogWide(
                catalog = catalog,
                loading = loading,
                error = error,
                receipt = receipt,
                quantities = quantities,
                selectedCategoryId = selectedCategoryId,
                selectedCartItemType = selectedCartItemType,
                visibleProducts = visibleProducts,
                customerName = customerName,
                phoneNumber = phoneNumber,
                notes = notes,
                fulfillmentType = fulfillmentType,
                scheduledAt = scheduledAt,
                paymentMode = paymentMode,
                customerAccountState = customerAccountState,
                customerOrderDetailReceipt = customerOrderDetailReceipt,
                customerOrderDetailLoading = customerOrderDetailLoading,
                customerOrderDetailRefreshing = customerOrderDetailRefreshing,
                customerOrderDetailError = customerOrderDetailError,
                customerProfileOpen = customerProfileOpen,
                selectedItems = selectedItems,
                selectedFlow = selectedFlow,
                total = total,
                submitting = submitting,
                statusRefreshing = statusRefreshing,
                customerStatusNotice = customerStatusNotice,
                submitEnabled = submitEnabled,
                onRetry = {
                    scope.launch {
                        if (receipt != null) {
                            refreshCurrentReceiptStatus(showRefreshing = true)
                        } else {
                            loadCatalogAndReceiptStatus(refreshReceipt = false)
                        }
                    }
                },
                onQuantityChange = ::updateQuantity,
                onClearSelection = {
                    quantities = emptyMap()
                    checkoutRequestId = ormaClientRequestId("catalog")
                    error = null
                },
                onNewOrder = {
                    receipt = null
                    customerStatusNotice = null
                    quantities = emptyMap()
                    checkoutRequestId = ormaClientRequestId("catalog")
                    notes = ""
                    scheduledAt = ""
                    error = null
                },
                onCategoryChange = { selectedCategoryId = it },
                onCustomerNameChange = { customerName = it },
                onPhoneChange = { phoneNumber = it },
                onNotesChange = { notes = it },
                onFulfillmentChange = { fulfillmentType = it },
                onScheduledAtChange = { scheduledAt = it },
                onPaymentModeChange = { paymentMode = it },
                customerAccountActions = customerAccountActions,
                onCustomerProfileOpen = {
                    customerSession?.let { session ->
                        customerProfileOpen = true
                        scope.launch { loadCustomerOrderHistory(session) }
                    }
                },
                onCustomerProfileClose = { customerProfileOpen = false },
                onCustomerOrderOpen = { order ->
                    scope.launch { loadCustomerOrderDetail(order.id, refresh = false) }
                },
                onCustomerOrderDetailBack = {
                    customerOrderDetailReceipt = null
                    customerOrderDetailError = null
                },
                onCustomerOrderDetailRefresh = {
                    val detailOrderId = customerOrderDetailReceipt?.order?.id.orEmpty()
                    scope.launch { loadCustomerOrderDetail(detailOrderId, refresh = true) }
                },
                onSubmit = ::submit,
            )
        }
    }
}

@Composable
private fun PublicCatalogMobile(
    catalog: OrmaPublicCatalog?,
    loading: Boolean,
    error: String?,
    receipt: OrmaPublicCatalogOrderReceipt?,
    quantities: Map<String, Int>,
    selectedCategoryId: String,
    selectedCartItemType: String?,
    visibleProducts: List<OrmaPublicCatalogProduct>,
    customerName: String,
    phoneNumber: String,
    notes: String,
    fulfillmentType: String,
    scheduledAt: String,
    paymentMode: String,
    customerAccountState: PublicCatalogCustomerAccountState,
    customerOrderDetailReceipt: OrmaPublicCatalogOrderReceipt?,
    customerOrderDetailLoading: Boolean,
    customerOrderDetailRefreshing: Boolean,
    customerOrderDetailError: String?,
    customerProfileOpen: Boolean,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    selectedItems: List<PublicCatalogSelection>,
    selectedFlow: String,
    total: Double,
    submitting: Boolean,
    statusRefreshing: Boolean,
    submitEnabled: Boolean,
    onRetry: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onClearSelection: () -> Unit,
    onNewOrder: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFulfillmentChange: (String) -> Unit,
    onScheduledAtChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    customerAccountActions: PublicCatalogCustomerAccountActions,
    onCustomerProfileOpen: () -> Unit,
    onCustomerProfileClose: () -> Unit,
    onCustomerOrderOpen: (OrmaOrder) -> Unit,
    onCustomerOrderDetailBack: () -> Unit,
    onCustomerOrderDetailRefresh: () -> Unit,
    onSubmit: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var checkoutOpen by remember(catalog?.workspace?.id) { mutableStateOf(false) }
    var variantPickerProductId by remember(catalog?.workspace?.id) { mutableStateOf<String?>(null) }
    val showFloatingCart = selectedItems.isNotEmpty() || receipt != null
    val variantPickerProduct = catalog?.products.orEmpty().firstOrNull { it.id == variantPickerProductId }

    LaunchedEffect(selectedItems.isEmpty(), receipt) {
        if (selectedItems.isEmpty() && receipt == null) {
            checkoutOpen = false
        }
    }

    if (customerProfileOpen && customerAccountState.session != null) {
        PublicCatalogCustomerProfileScreen(
            state = customerAccountState,
            actions = customerAccountActions,
            detailReceipt = customerOrderDetailReceipt,
            detailLoading = customerOrderDetailLoading,
            detailRefreshing = customerOrderDetailRefreshing,
            detailError = customerOrderDetailError,
            customerStatusNotice = customerStatusNotice,
            compact = true,
            onBack = onCustomerProfileClose,
            onOrderOpen = onCustomerOrderOpen,
            onDetailBack = onCustomerOrderDetailBack,
            onDetailRefresh = onCustomerOrderDetailRefresh,
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
        return
    }

    Box(
        modifier = Modifier
            .widthIn(max = 430.dp)
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PublicCatalogCheckoutTopBar(
                catalog = catalog,
                loading = loading,
                compact = true,
                customerAccountState = customerAccountState,
                customerStatusNotice = customerStatusNotice,
                onProfileClick = onCustomerProfileOpen,
            )
            PublicCatalogProductsCard {
                PublicCatalogProducts(
                    catalog = catalog,
                    loading = loading,
                    quantities = quantities,
                    selectedCategoryId = selectedCategoryId,
                    selectedCartItemType = selectedCartItemType,
                    visibleProducts = visibleProducts,
                    onQuantityChange = onQuantityChange,
                    onCategoryChange = onCategoryChange,
                    onVariantPickerOpen = { productId ->
                        checkoutOpen = false
                        variantPickerProductId = productId
                    },
                )
            }
            Spacer(modifier = Modifier.height(if (showFloatingCart) 104.dp else 6.dp))
        }
        if (showFloatingCart) {
            PublicCatalogFloatingCartButton(
                catalog = catalog,
                selectedItems = selectedItems,
                total = total,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    checkoutOpen = true
                },
            )
        }
        if (checkoutOpen) {
            PublicCatalogCheckoutSheet(
                compact = true,
                onDismiss = { checkoutOpen = false },
            ) {
                PublicCatalogCheckout(
                    catalog = catalog,
                    error = error,
                    receipt = receipt,
                    customerName = customerName,
                    phoneNumber = phoneNumber,
                    notes = notes,
                    fulfillmentType = fulfillmentType,
                    scheduledAt = scheduledAt,
                    paymentMode = paymentMode,
                    customerAccountState = customerAccountState,
                    selectedItems = selectedItems,
                    selectedFlow = selectedFlow,
                    total = total,
                    submitting = submitting,
                    statusRefreshing = statusRefreshing,
                    customerStatusNotice = customerStatusNotice,
                    submitEnabled = submitEnabled,
                    onRetry = onRetry,
                    onClearSelection = onClearSelection,
                    onNewOrder = {
                        checkoutOpen = false
                        onNewOrder()
                    },
                    onCustomerNameChange = onCustomerNameChange,
                    onPhoneChange = onPhoneChange,
                    onNotesChange = onNotesChange,
                    onFulfillmentChange = onFulfillmentChange,
                    onScheduledAtChange = onScheduledAtChange,
                    onPaymentModeChange = onPaymentModeChange,
                    customerAccountActions = customerAccountActions,
                    onSubmit = onSubmit,
                )
            }
        }
        variantPickerProduct?.let { product ->
            PublicCatalogVariantPickerSheet(
                compact = true,
                product = product,
                variantQuantities = product.publicCatalogActiveVariants().associate { variant ->
                    variant.id to (quantities[product.publicCatalogCartKey(variant.id)] ?: 0)
                },
                activeCartType = selectedCartItemType,
                onDismiss = { variantPickerProductId = null },
                onVariantQuantityChange = { variantId, quantity ->
                    onQuantityChange(product.publicCatalogCartKey(variantId), quantity)
                },
            )
        }
    }
}

@Composable
private fun PublicCatalogFloatingCartButton(
    catalog: OrmaPublicCatalog?,
    selectedItems: List<PublicCatalogSelection>,
    total: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    val itemLabel = if (selectedCount == 1) "1 item" else "$selectedCount items"
    val totalLabel = "${currency.ifBlank { "" }} ${money(total)}".trim()

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = OrmaShapes.CheckoutButton,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OrmaColors.OnAccent.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$itemLabel / $totalLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrmaColors.OnAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "View cart",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = OrmaColors.OnAccent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PublicCatalogWide(
    catalog: OrmaPublicCatalog?,
    loading: Boolean,
    error: String?,
    receipt: OrmaPublicCatalogOrderReceipt?,
    quantities: Map<String, Int>,
    selectedCategoryId: String,
    selectedCartItemType: String?,
    visibleProducts: List<OrmaPublicCatalogProduct>,
    customerName: String,
    phoneNumber: String,
    notes: String,
    fulfillmentType: String,
    scheduledAt: String,
    paymentMode: String,
    customerAccountState: PublicCatalogCustomerAccountState,
    customerOrderDetailReceipt: OrmaPublicCatalogOrderReceipt?,
    customerOrderDetailLoading: Boolean,
    customerOrderDetailRefreshing: Boolean,
    customerOrderDetailError: String?,
    customerProfileOpen: Boolean,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    selectedItems: List<PublicCatalogSelection>,
    selectedFlow: String,
    total: Double,
    submitting: Boolean,
    statusRefreshing: Boolean,
    submitEnabled: Boolean,
    onRetry: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onClearSelection: () -> Unit,
    onNewOrder: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFulfillmentChange: (String) -> Unit,
    onScheduledAtChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    customerAccountActions: PublicCatalogCustomerAccountActions,
    onCustomerProfileOpen: () -> Unit,
    onCustomerProfileClose: () -> Unit,
    onCustomerOrderOpen: (OrmaOrder) -> Unit,
    onCustomerOrderDetailBack: () -> Unit,
    onCustomerOrderDetailRefresh: () -> Unit,
    onSubmit: () -> Unit,
) {
    val wideScrollState = rememberScrollState()
    var checkoutOpen by remember(catalog?.workspace?.id) { mutableStateOf(false) }
    var variantPickerProductId by remember(catalog?.workspace?.id) { mutableStateOf<String?>(null) }
    val showFloatingCart = selectedItems.isNotEmpty() || receipt != null
    val variantPickerProduct = catalog?.products.orEmpty().firstOrNull { it.id == variantPickerProductId }

    LaunchedEffect(selectedItems.isEmpty(), receipt) {
        if (selectedItems.isEmpty() && receipt == null) {
            checkoutOpen = false
        }
    }

    if (customerProfileOpen && customerAccountState.session != null) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(28.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            PublicCatalogCustomerProfileScreen(
                state = customerAccountState,
                actions = customerAccountActions,
                detailReceipt = customerOrderDetailReceipt,
                detailLoading = customerOrderDetailLoading,
                detailRefreshing = customerOrderDetailRefreshing,
                detailError = customerOrderDetailError,
                customerStatusNotice = customerStatusNotice,
                compact = false,
                onBack = onCustomerProfileClose,
                onOrderOpen = onCustomerOrderOpen,
                onDetailBack = onCustomerOrderDetailBack,
                onDetailRefresh = onCustomerOrderDetailRefresh,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .widthIn(max = 920.dp),
            )
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(28.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        val sidePanelOpen = checkoutOpen && showFloatingCart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = if (sidePanelOpen) 1540.dp else 1180.dp)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(wideScrollState),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                PublicCatalogCheckoutTopBar(
                    catalog = catalog,
                    loading = loading,
                    compact = false,
                    customerAccountState = customerAccountState,
                    customerStatusNotice = customerStatusNotice,
                    onProfileClick = onCustomerProfileOpen,
                )
                PublicCatalogProductsCard {
                    PublicCatalogProducts(
                        catalog = catalog,
                        loading = loading,
                        quantities = quantities,
                        selectedCategoryId = selectedCategoryId,
                        selectedCartItemType = selectedCartItemType,
                        visibleProducts = visibleProducts,
                        onQuantityChange = onQuantityChange,
                        onCategoryChange = onCategoryChange,
                        onVariantPickerOpen = { productId ->
                            checkoutOpen = false
                            variantPickerProductId = productId
                        },
                    )
                }
                Spacer(modifier = Modifier.height(if (showFloatingCart && !sidePanelOpen) 86.dp else 0.dp))
            }
            if (sidePanelOpen) {
                PublicCatalogCheckoutSidePanel(
                    onDismiss = { checkoutOpen = false },
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 420.dp, max = 470.dp),
                ) {
                    PublicCatalogCheckout(
                        catalog = catalog,
                        error = error,
                        receipt = receipt,
                        customerName = customerName,
                        phoneNumber = phoneNumber,
                        notes = notes,
                        fulfillmentType = fulfillmentType,
                        scheduledAt = scheduledAt,
                        paymentMode = paymentMode,
                        customerAccountState = customerAccountState,
                        selectedItems = selectedItems,
                        selectedFlow = selectedFlow,
                        total = total,
                        submitting = submitting,
                        statusRefreshing = statusRefreshing,
                        customerStatusNotice = customerStatusNotice,
                        submitEnabled = submitEnabled,
                        onRetry = onRetry,
                        onClearSelection = onClearSelection,
                        onNewOrder = {
                            checkoutOpen = false
                            onNewOrder()
                        },
                        onCustomerNameChange = onCustomerNameChange,
                        onPhoneChange = onPhoneChange,
                        onNotesChange = onNotesChange,
                        onFulfillmentChange = onFulfillmentChange,
                        onScheduledAtChange = onScheduledAtChange,
                        onPaymentModeChange = onPaymentModeChange,
                        customerAccountActions = customerAccountActions,
                        onSubmit = onSubmit,
                    )
                }
            }
        }
        if (showFloatingCart && !sidePanelOpen) {
            PublicCatalogFloatingCartButton(
                catalog = catalog,
                selectedItems = selectedItems,
                total = total,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .widthIn(min = 300.dp, max = 390.dp)
                    .padding(18.dp),
                onClick = {
                    checkoutOpen = true
                },
            )
        }
        variantPickerProduct?.let { product ->
            PublicCatalogVariantPickerSheet(
                compact = false,
                product = product,
                variantQuantities = product.publicCatalogActiveVariants().associate { variant ->
                    variant.id to (quantities[product.publicCatalogCartKey(variant.id)] ?: 0)
                },
                activeCartType = selectedCartItemType,
                onDismiss = { variantPickerProductId = null },
                onVariantQuantityChange = { variantId, quantity ->
                    onQuantityChange(product.publicCatalogCartKey(variantId), quantity)
                },
            )
        }
    }
}

@Composable
private fun PublicCatalogCheckoutSheet(
    compact: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sheetScrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(onClick = onDismiss),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sheetModifier = if (compact) {
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .heightIn(max = maxHeight - 20.dp)
            } else {
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.38f)
                    .widthIn(min = 430.dp, max = 560.dp)
                    .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 18.dp)
            }
            Surface(
                modifier = sheetModifier,
                shape = OrmaShapes.PremiumCard,
                color = OrmaColors.CardBackground,
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(sheetScrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Cart",
                                style = MaterialTheme.typography.titleMedium,
                                color = OrmaColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Review selected items and checkout details.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        OrmaLightButton(
                            text = "Close",
                            onClick = onDismiss,
                            modifier = Modifier.widthIn(min = 96.dp, max = 124.dp),
                        )
                    }
                    content()
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckoutSidePanel(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sheetScrollState = rememberScrollState()
    Surface(
        modifier = modifier,
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(18.dp)
                .verticalScroll(sheetScrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Cart",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Keep adding items from the catalog while checkout stays open.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaLightButton(
                    text = "Keep adding",
                    onClick = onDismiss,
                    modifier = Modifier.widthIn(min = 118.dp, max = 142.dp),
                )
            }
            content()
        }
    }
}

@Composable
private fun PublicCatalogProductsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun PublicCatalogCheckoutTopBar(
    catalog: OrmaPublicCatalog?,
    loading: Boolean,
    compact: Boolean,
    customerAccountState: PublicCatalogCustomerAccountState,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    onProfileClick: () -> Unit,
) {
    val businessName = catalog?.workspace?.businessName?.ifBlank { "ORMA checkout" } ?: "ORMA checkout"
    val location = catalog?.workspace?.let {
        listOf(it.industry, it.city).filter(String::isNotBlank).joinToString(" / ")
    }.orEmpty().ifBlank { "Online checkout" }
    val logoUrl = catalog?.workspace?.logoUrl.publicCatalogRemoteImageUrl()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stacked = compact || maxWidth < 720.dp
            if (stacked) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PublicCatalogCheckoutBusinessLine(
                            businessName = businessName,
                            location = location,
                            logoUrl = logoUrl,
                            loading = loading,
                            modifier = Modifier.weight(1f),
                        )
                        if (customerAccountState.session != null) {
                            PublicCatalogProfileAvatar(
                                modifier = Modifier.size(48.dp),
                                onClick = onProfileClick,
                            )
                        }
                    }
                    customerStatusNotice?.let { notice ->
                        PublicCatalogCustomerStatusNoticeCard(notice = notice)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PublicCatalogCheckoutBusinessLine(
                            businessName = businessName,
                            location = location,
                            logoUrl = logoUrl,
                            loading = loading,
                            modifier = Modifier.weight(1f),
                        )
                        if (customerAccountState.session != null) {
                            PublicCatalogProfileAvatar(
                                modifier = Modifier.size(52.dp),
                                onClick = onProfileClick,
                            )
                        }
                    }
                    customerStatusNotice?.let { notice ->
                        PublicCatalogCustomerStatusNoticeCard(notice = notice)
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckoutBusinessLine(
    businessName: String,
    location: String,
    logoUrl: String?,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = OrmaShapes.StandardCell,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.OnAccent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                logoUrl?.let {
                    OrmaRemoteImage(
                        url = it,
                        contentDescription = businessName,
                        modifier = Modifier.fillMaxSize(),
                    )
                } ?: OrmaBrandMark(
                    modifier = Modifier.size(27.dp),
                    color = OrmaColors.ScreenBackground,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = if (loading) "Checkout loading" else "Online checkout",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = businessName,
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PublicCatalogHero(
    catalog: OrmaPublicCatalog?,
    loading: Boolean,
    selectedItems: List<PublicCatalogSelection>,
    visibleProducts: List<OrmaPublicCatalogProduct>,
    total: Double,
    compact: Boolean,
) {
    val businessName = catalog?.workspace?.businessName?.ifBlank { "ORMA ordering" } ?: "ORMA ordering"
    val location = catalog?.workspace?.let {
        listOf(it.industry, it.city).filter(String::isNotBlank).joinToString(" / ")
    }.orEmpty().ifBlank { "Live ordering page" }
    val itemCount = catalog?.products?.size ?: 0
    val selectedCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    val logoUrl = catalog?.workspace?.logoUrl.publicCatalogRemoteImageUrl()
    val whatsappUrl = catalog?.publicCatalogWhatsAppUrl(selectedItems)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stacked = compact || maxWidth < 740.dp
            if (stacked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    PublicCatalogHeroCopy(
                        businessName = businessName,
                        location = location,
                        loading = loading,
                        itemCount = itemCount,
                        selectedCount = selectedCount,
                        total = total,
                        currency = currency,
                        compact = true,
                        logoUrl = logoUrl,
                        whatsappUrl = whatsappUrl,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PublicCatalogHeroMedia(
                        catalog = catalog,
                        products = visibleProducts,
                        compact = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PublicCatalogHeroCopy(
                        businessName = businessName,
                        location = location,
                        loading = loading,
                        itemCount = itemCount,
                        selectedCount = selectedCount,
                        total = total,
                        currency = currency,
                        compact = false,
                        logoUrl = logoUrl,
                        whatsappUrl = whatsappUrl,
                        modifier = Modifier.weight(1f),
                    )
                    PublicCatalogHeroMedia(
                        catalog = catalog,
                        products = visibleProducts,
                        compact = false,
                        modifier = Modifier.weight(0.82f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogHeroCopy(
    businessName: String,
    location: String,
    loading: Boolean,
    itemCount: Int,
    selectedCount: Int,
    total: Double,
    currency: String,
    compact: Boolean,
    logoUrl: String?,
    whatsappUrl: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(if (compact) 46.dp else 54.dp),
                shape = OrmaShapes.Capsule,
                color = OrmaColors.ScreenBackground,
                contentColor = OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    logoUrl?.takeIf { it.isNotBlank() }?.let {
                        OrmaRemoteImage(
                            url = it,
                            contentDescription = businessName,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } ?: OrmaBrandMark(
                        modifier = Modifier.size(if (compact) 24.dp else 29.dp),
                        color = OrmaColors.Accent,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "QR ORDERING",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.ScreenBackground.copy(alpha = 0.62f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = if (loading) "Menu loading" else "Open for requests",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.ScreenBackground,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = businessName,
            style = if (compact) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium,
            color = OrmaColors.ScreenBackground,
            fontWeight = FontWeight.SemiBold,
            maxLines = if (compact) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$location. Build your cart, choose timing, send it straight to the counter.",
            style = MaterialTheme.typography.bodyLarge,
            color = OrmaColors.ScreenBackground.copy(alpha = 0.76f),
            maxLines = if (compact) 3 else 2,
            overflow = TextOverflow.Ellipsis,
        )
        whatsappUrl?.let { url ->
            OrmaLightButton(
                text = "Chat on WhatsApp",
                onClick = { openPublicCatalogPaymentLink(url) },
                modifier = if (compact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.widthIn(min = 180.dp, max = 240.dp)
                },
            )
        }
        if (compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PublicCatalogHeroMetric(
                    label = "Items",
                    value = if (loading) "--" else itemCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                PublicCatalogHeroMetric(
                    label = "Selected",
                    value = selectedCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            PublicCatalogHeroMetric(
                label = "Total",
                value = if (selectedCount == 0) "--" else "${currency.ifBlank { "" }} ${money(total)}".trim(),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PublicCatalogHeroMetric(
                    label = "Items",
                    value = if (loading) "--" else itemCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                PublicCatalogHeroMetric(
                    label = "Selected",
                    value = selectedCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                PublicCatalogHeroMetric(
                    label = "Total",
                    value = if (selectedCount == 0) "--" else "${currency.ifBlank { "" }} ${money(total)}".trim(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, OrmaColors.ScreenBackground.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.ScreenBackground.copy(alpha = 0.56f),
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.ScreenBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PublicCatalogHeroMedia(
    catalog: OrmaPublicCatalog?,
    products: List<OrmaPublicCatalogProduct>,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val coverUrl = catalog?.workspace?.coverUrl.publicCatalogRemoteImageUrl()
    val heroProducts = products.filter { !it.imageUrl.isNullOrBlank() }.take(if (compact) 2 else 3)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 150.dp else 220.dp),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.Accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        if (coverUrl != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                OrmaRemoteImage(
                    url = coverUrl,
                    contentDescription = catalog?.workspace?.businessName ?: "Business cover",
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = OrmaShapes.Capsule,
                    color = OrmaColors.Accent.copy(alpha = 0.86f),
                    contentColor = OrmaColors.OnAccent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = catalog?.workspace?.businessName?.ifBlank { "Open catalog" } ?: "Open catalog",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else if (heroProducts.isEmpty()) {
            PublicCatalogHeroPlaceholder(catalog = catalog)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                heroProducts.forEachIndexed { index, product ->
                    Surface(
                        modifier = Modifier
                            .weight(if (index == 0) 1.18f else 0.82f)
                            .fillMaxSize(),
                        shape = OrmaShapes.StandardCell,
                        color = OrmaColors.CellBackground,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            OrmaRemoteImage(
                                url = product.imageUrl.orEmpty(),
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp),
                                shape = OrmaShapes.Capsule,
                                color = OrmaColors.Accent.copy(alpha = 0.86f),
                                contentColor = OrmaColors.OnAccent,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                            ) {
                                Text(
                                    text = product.name.ifBlank { "Item" },
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogHeroPlaceholder(catalog: OrmaPublicCatalog?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrmaColors.ScreenBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            catalog?.workspace?.logoUrl.publicCatalogRemoteImageUrl()?.let { logoUrl ->
                OrmaRemoteImage(
                    url = logoUrl,
                    contentDescription = catalog?.workspace?.businessName,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(OrmaShapes.StandardCell),
                )
            } ?: OrmaBrandMark(
                modifier = Modifier.size(62.dp),
                color = OrmaColors.Accent,
            )
            Text(
                text = "Start building your request",
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PublicCatalogProducts(
    catalog: OrmaPublicCatalog?,
    loading: Boolean,
    quantities: Map<String, Int>,
    selectedCategoryId: String,
    selectedCartItemType: String?,
    visibleProducts: List<OrmaPublicCatalogProduct>,
    onQuantityChange: (String, Int) -> Unit,
    onCategoryChange: (String) -> Unit,
    onVariantPickerOpen: (String) -> Unit,
) {
    var searchQuery by remember(catalog?.workspace?.id) { mutableStateOf("") }
    var selectedTypeFilter by remember(catalog?.workspace?.id) { mutableStateOf("all") }
    val cleanSearchQuery = searchQuery.trim()
    val allDisplayItems = catalog?.products
        .orEmpty()
        .publicCatalogDisplayItems()
    val allRegularItems = allDisplayItems.filter { it.variant == null }
    val allPackageItems = allDisplayItems.filter { it.variant != null }
    val allOfferProducts = catalog?.products
        .orEmpty()
        .filter { it.offer != null && it.publicCatalogOfferSavings() > 0.0 }
    val availableTypeFilters = buildList {
        addAll(
            allRegularItems
                .map { it.product.itemType.publicCatalogNormalizedItemType() }
                .distinct(),
        )
        if (allPackageItems.isNotEmpty()) add("packages")
        if (allOfferProducts.isNotEmpty()) add("offers")
    }.distinct()
    val effectiveTypeFilter = selectedTypeFilter.takeIf { it == "all" || it in availableTypeFilters } ?: "all"
    val visibleCatalogItems = visibleProducts.publicCatalogDisplayItems()
    val visiblePackageItems = visibleCatalogItems.filter { it.variant != null }
    val visibleRegularItems = visibleCatalogItems.filter { it.variant == null }
    val searchedRegularItems = visibleRegularItems.filter { item ->
        val product = item.product
        val productType = product.itemType.publicCatalogNormalizedItemType()
        val matchesType = when (effectiveTypeFilter) {
            "all" -> true
            "packages", "offers" -> false
            else -> effectiveTypeFilter == productType
        }
        matchesType &&
            (cleanSearchQuery.isBlank() || item.matchesPublicCatalogSearch(cleanSearchQuery))
    }
    val searchedPackageItems = visiblePackageItems.filter { item ->
        (effectiveTypeFilter == "all" || effectiveTypeFilter == "packages") &&
            (cleanSearchQuery.isBlank() || item.matchesPublicCatalogSearch(cleanSearchQuery))
    }
    val searchedOfferProducts = if (effectiveTypeFilter == "offers") {
        visibleProducts
            .filter { it.offer != null && it.publicCatalogOfferSavings() > 0.0 }
            .filter { product ->
                cleanSearchQuery.isBlank() || PublicCatalogDisplayItem(product = product).matchesPublicCatalogSearch(cleanSearchQuery)
            }
            .distinctBy { it.id }
    } else {
        emptyList()
    }
    val shownCount = when (effectiveTypeFilter) {
        "packages" -> searchedPackageItems.size
        "offers" -> searchedOfferProducts.size
        else -> searchedRegularItems.size + searchedPackageItems.size
    }
    val hasSearchResults = when (effectiveTypeFilter) {
        "packages" -> searchedPackageItems.isNotEmpty()
        "offers" -> searchedOfferProducts.isNotEmpty()
        else -> searchedRegularItems.isNotEmpty() || searchedPackageItems.isNotEmpty()
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicCatalogProductsHeader(
            count = shownCount,
            activeType = selectedCartItemType,
        )
        when {
            loading -> PublicCatalogSkeleton()
            catalog == null -> PublicCatalogEmpty(
                title = "Ordering page unavailable",
                body = "This business ordering link is not active right now.",
            )
            catalog.products.isEmpty() -> PublicCatalogEmpty(
                title = "Nothing to order yet",
                body = "The business has not published products or services for this page.",
            )
            else -> {
                OrmaTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search",
                    placeholder = "Search products, services, or bookings",
                )
                PublicCatalogTypeFilter(
                    availableTypes = availableTypeFilters,
                    selectedType = effectiveTypeFilter,
                    activeCartType = selectedCartItemType,
                    onTypeChange = { selectedTypeFilter = it },
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    content = {
                        PublicCatalogCategoryFilter(
                            catalog = catalog,
                            selectedCategoryId = selectedCategoryId,
                            onCategoryChange = onCategoryChange,
                        )
                        if (!hasSearchResults) {
                            PublicCatalogEmpty(
                                title = when (effectiveTypeFilter) {
                                    "packages" -> "No matching packages"
                                    "offers" -> "No matching offers"
                                    else -> "No matches"
                                },
                                body = "Try another search, tab, or category.",
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                if ((effectiveTypeFilter == "all" || effectiveTypeFilter == "packages") && searchedPackageItems.isNotEmpty()) {
                                    PublicCatalogGridSection(
                                        title = "Packages",
                                        body = "Combos and session packs ready for checkout.",
                                        badge = "${searchedPackageItems.size} available",
                                        items = searchedPackageItems,
                                        quantities = quantities,
                                        activeCartType = selectedCartItemType,
                                        onQuantityChange = onQuantityChange,
                                        onVariantPickerOpen = onVariantPickerOpen,
                                    )
                                }
                                if (effectiveTypeFilter == "offers" && searchedOfferProducts.isNotEmpty()) {
                                    PublicCatalogOffersStrip(
                                        products = searchedOfferProducts,
                                        quantities = quantities,
                                        activeCartType = selectedCartItemType,
                                        onApplyOffer = { productId ->
                                            val product = searchedOfferProducts.firstOrNull { it.id == productId }
                                            if (product?.publicCatalogActiveVariants().orEmpty().isNotEmpty()) {
                                                onVariantPickerOpen(productId)
                                            } else {
                                                onQuantityChange(productId, (quantities[productId] ?: 0).coerceAtLeast(1))
                                            }
                                        },
                                    )
                                }
                                if ((effectiveTypeFilter == "all" || effectiveTypeFilter !in setOf("packages", "offers")) && searchedRegularItems.isNotEmpty()) {
                                    PublicCatalogGridSection(
                                        title = effectiveTypeFilter.publicCatalogCatalogSectionTitle(),
                                        items = searchedRegularItems,
                                        quantities = quantities,
                                        activeCartType = selectedCartItemType,
                                        onQuantityChange = onQuantityChange,
                                        onVariantPickerOpen = onVariantPickerOpen,
                                    )
                                }
                            }
                        }
                    })
            }
        }
    }
}

@Composable
private fun PublicCatalogProductsHeader(
    count: Int,
    activeType: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "Choose items",
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = activeType?.let {
                    "${it.publicCatalogItemTypeLabel()} checkout active. Clear cart to switch type."
                } ?: "Browse the catalog and add items to checkout.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OrmaBadge(
            text = "$count shown".uppercase(),
            tone = OrmaStatusTone.Info,
        )
    }
}

@Composable
private fun PublicCatalogTypeFilter(
    availableTypes: List<String>,
    selectedType: String,
    activeCartType: String?,
    onTypeChange: (String) -> Unit,
) {
    if (availableTypes.size <= 1) return
    val items = listOf("all") + availableTypes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { type ->
            val selected = selectedType == type
            val locked = activeCartType != null && type !in setOf("all", "packages", "offers") && type != activeCartType
            Surface(
                modifier = Modifier.clickable { onTypeChange(type) },
                shape = OrmaShapes.Capsule,
                color = when {
                    selected -> OrmaColors.Accent
                    locked -> OrmaColors.CellBackground.copy(alpha = 0.54f)
                    else -> OrmaColors.CellBackground
                },
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = when (type) {
                        "all" -> "All"
                        "packages" -> "Packages"
                        "offers" -> "Offers"
                        else -> type.publicCatalogItemTypeLabel()
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        selected -> OrmaColors.ScreenBackground
                        locked -> OrmaColors.TextTertiary
                        else -> OrmaColors.TextPrimary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogOffersStrip(
    products: List<OrmaPublicCatalogProduct>,
    quantities: Map<String, Int>,
    activeCartType: String?,
    onApplyOffer: (String) -> Unit,
) {
    val offerProducts = products
        .filter { it.offer != null && it.publicCatalogOfferSavings() > 0.0 }
        .take(12)
    if (offerProducts.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Offers",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Swipe and apply an offer to this cart.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(text = "${offerProducts.size} live".uppercase(), tone = OrmaStatusTone.Success)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            offerProducts.forEach { product ->
                PublicCatalogOfferCard(
                    product = product,
                    quantity = quantities[product.id] ?: 0,
                    activeCartType = activeCartType,
                    onApply = { onApplyOffer(product.id) },
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogOfferCard(
    product: OrmaPublicCatalogProduct,
    quantity: Int,
    activeCartType: String?,
    onApply: () -> Unit,
) {
    val offer = product.offer ?: return
    val productType = product.itemType.publicCatalogNormalizedItemType()
    val applied = quantity > 0
    val lockedByType = activeCartType != null && activeCartType != productType && !applied
    val enabled = product.inStock && !lockedByType
    val savings = product.publicCatalogOfferSavings()
    Surface(
        modifier = Modifier
            .width(252.dp)
            .clickable(enabled = enabled || applied, onClick = onApply),
        shape = OrmaShapes.StandardCell,
        color = if (applied) OrmaColors.Accent.copy(alpha = 0.07f) else OrmaColors.CardBackground,
        border = BorderStroke(
            width = if (applied) 1.1.dp else 0.8.dp,
            color = if (applied) OrmaColors.Accent.copy(alpha = 0.42f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaBadge(
                    text = if (applied) "APPLIED" else product.publicCatalogOfferShortLabel().uppercase(),
                    tone = if (applied) OrmaStatusTone.Success else OrmaStatusTone.Warning,
                )
                Text(
                    text = product.itemType.publicCatalogItemTypeLabel(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.TextTertiary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = offer.name.ifBlank { "Offer" },
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.name.ifBlank { "Item" },
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "${product.currency} ${product.customerPrice}",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${product.currency} ${product.sellingPrice}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.TextTertiary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "Save ${product.currency} ${money(savings)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.Success,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.Capsule,
                color = if (enabled || applied) OrmaColors.Accent else OrmaColors.CellBackground,
                contentColor = if (enabled || applied) OrmaColors.OnAccent else OrmaColors.TextTertiary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = when {
                        applied -> "In cart"
                        !product.inStock -> "Unavailable"
                        lockedByType -> "Switch cart type"
                        else -> "Apply offer"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogGridSection(
    title: String,
    items: List<PublicCatalogDisplayItem>,
    quantities: Map<String, Int>,
    activeCartType: String?,
    onQuantityChange: (String, Int) -> Unit,
    onVariantPickerOpen: (String) -> Unit,
    body: String? = null,
    badge: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                body?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            badge?.takeIf { it.isNotBlank() }?.let {
                OrmaBadge(text = it.uppercase(), tone = OrmaStatusTone.Info)
            }
        }
        PublicCatalogProductGrid(
            items = items,
            quantities = quantities,
            activeCartType = activeCartType,
            onQuantityChange = onQuantityChange,
            onVariantPickerOpen = onVariantPickerOpen,
        )
    }
}

@Composable
private fun PublicCatalogProductGrid(
    items: List<PublicCatalogDisplayItem>,
    quantities: Map<String, Int>,
    activeCartType: String?,
    onQuantityChange: (String, Int) -> Unit,
    onVariantPickerOpen: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 920.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 1
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    rowItems.forEach { item ->
                        val product = item.product
                        val itemKey = item.cartKey
                        PublicCatalogProductTile(
                            item = item,
                            quantity = quantities[itemKey] ?: 0,
                            variantQuantities = if (item.variant == null) {
                                product.variants.associate { variant ->
                                    variant.id to (quantities[product.publicCatalogCartKey(variant.id)] ?: 0)
                                }
                            } else {
                                emptyMap()
                            },
                            activeCartType = activeCartType,
                            onQuantityChange = { onQuantityChange(itemKey, it) },
                            onVariantPickerOpen = { onVariantPickerOpen(product.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogCategoryFilter(
    catalog: OrmaPublicCatalog,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit,
) {
    val categories = catalog.categories
    if (categories.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val items = listOf("all" to "All") + categories.map { it.id to it.name }
        items.forEach { (id, label) ->
            val selected = selectedCategoryId == id
            Surface(
                modifier = Modifier.clickable { onCategoryChange(id) },
                shape = OrmaShapes.Capsule,
                color = if (selected) OrmaColors.Accent else OrmaColors.CellBackground,
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckout(
    catalog: OrmaPublicCatalog?,
    error: String?,
    receipt: OrmaPublicCatalogOrderReceipt?,
    customerName: String,
    phoneNumber: String,
    notes: String,
    fulfillmentType: String,
    scheduledAt: String,
    paymentMode: String,
    customerAccountState: PublicCatalogCustomerAccountState,
    selectedItems: List<PublicCatalogSelection>,
    selectedFlow: String,
    total: Double,
    submitting: Boolean,
    statusRefreshing: Boolean,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    submitEnabled: Boolean,
    onRetry: () -> Unit,
    onClearSelection: () -> Unit,
    onNewOrder: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFulfillmentChange: (String) -> Unit,
    onScheduledAtChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    customerAccountActions: PublicCatalogCustomerAccountActions,
    onSubmit: () -> Unit,
) {
    val appointmentFlow = selectedFlow == "appointment"
    val serviceFlow = selectedFlow == "service"
    val fulfillmentOptions = selectedFlow.publicCatalogFulfillmentOptions()
    val selectedFulfillment = if (fulfillmentType in fulfillmentOptions) fulfillmentType else fulfillmentOptions.first()
    val submitText = when {
        submitting -> "Submitting..."
        appointmentFlow -> "Book appointment"
        serviceFlow -> "Request service"
        else -> "Place order"
    }
    val checkoutTitle = when {
        appointmentFlow -> "Book appointment"
        serviceFlow -> "Request service"
        else -> "Place order"
    }
    val checkoutPrompt = when {
        appointmentFlow -> "Choose the preferred slot and send this booking to the business."
        serviceFlow -> "Choose request timing and send the service details to the business."
        else -> "Choose pickup timing and send this order to the business."
    }
    val itemCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    val estimatedTotal = if (itemCount == 0) "--" else "${currency.ifBlank { "" }} ${money(total)}".trim()
    val offerSavings = selectedItems.publicCatalogOfferSavingsTotal()
    val upiPaymentMethod = catalog?.paymentMethods?.publicCatalogDefaultUpiPaymentMethod()
    val selectedPaymentMode = if (paymentMode == "upi" && upiPaymentMethod != null) "upi" else "pay_on_spot"
    val upiPaymentLink = if (selectedPaymentMode == "upi" && total > 0.0) {
        upiPaymentMethod?.publicCatalogUpiPaymentValue(
            amount = money(total),
            currency = currency.ifBlank { "INR" },
            note = catalog.workspace.businessName.ifBlank { "ORMA" },
        )
    } else {
        null
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (receipt != null) {
            PublicCatalogSuccess(
                receipt = receipt,
                refreshing = statusRefreshing,
                customerStatusNotice = customerStatusNotice,
                onRefresh = onRetry,
                onNewOrder = onNewOrder,
            )
        } else {
            PublicCatalogCheckoutHeader(
                title = checkoutTitle,
                body = checkoutPrompt,
                itemCount = itemCount,
                total = estimatedTotal,
                offerSavings = offerSavings,
                currency = currency,
                ready = submitEnabled,
                submitting = submitting,
            )
            PublicCatalogSelectionSummary(
                catalog = catalog,
                selectedItems = selectedItems,
                total = total,
                offerSavings = offerSavings,
                onClearSelection = onClearSelection,
            )
            if (selectedItems.isEmpty()) {
                error?.takeIf(String::isNotBlank)?.let {
                    PublicCatalogMessageCard(
                        title = "Could not continue",
                        body = it,
                        error = true,
                    )
                }
                if (catalog == null && !error.isNullOrBlank()) {
                    OrmaLightButton(
                        text = "Try again",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                PublicCatalogCustomerAccountPanel(
                    state = customerAccountState,
                    actions = customerAccountActions,
                )
                OrmaSectionHeader(text = "Contact details")
                OrmaTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = "Your name",
                    placeholder = "Full name",
                )
                OrmaCountryPhoneField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = "Phone number",
                    supportingText = "India is selected by default. Change country if needed.",
                )
                OrmaSectionHeader(
                    text = selectedFlow.publicCatalogTimingPaymentTitle(),
                )
                if (!appointmentFlow) {
                    OrmaSegmentedRow(
                        options = fulfillmentOptions,
                        selected = selectedFulfillment,
                        label = { it.publicCatalogCheckoutFulfillmentLabel(selectedFlow) },
                        onSelected = onFulfillmentChange,
                    )
                }
                if (appointmentFlow || selectedFulfillment == "scheduled") {
                    OrmaCalendarDateTimeField(
                        value = scheduledAt,
                        onValueChange = onScheduledAtChange,
                        label = if (appointmentFlow) "Appointment date/time" else "Preferred date/time",
                        placeholder = "Choose date",
                        supportingText = if (appointmentFlow) {
                            "Choose the appointment slot requested by the customer."
                        } else {
                            "Choose a date. Add a common time slot if needed."
                        },
                        allowClear = !appointmentFlow,
                    )
                }
                OrmaSegmentedRow(
                    options = if (upiPaymentMethod != null) listOf("pay_on_spot", "upi") else listOf("pay_on_spot"),
                    selected = selectedPaymentMode,
                    label = { it.publicCatalogCheckoutPaymentLabel(selectedFlow) },
                    onSelected = onPaymentModeChange,
                )
                if (upiPaymentMethod != null && upiPaymentLink != null) {
                    PublicCatalogCheckoutUpiPaymentCard(
                        method = upiPaymentMethod,
                        amount = "${currency.ifBlank { "INR" }} ${money(total)}",
                        paymentLink = upiPaymentLink,
                    )
                }
                OrmaTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = "Notes",
                    placeholder = "Optional",
                    singleLine = false,
                    minLines = 2,
                )
                error?.takeIf(String::isNotBlank)?.let {
                    PublicCatalogMessageCard(
                        title = "Could not continue",
                        body = it,
                        error = true,
                    )
                }
                OrmaFullButton(
                    text = submitText,
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = submitEnabled && !submitting,
                )
                catalog?.publicCatalogWhatsAppUrl(selectedItems)?.let { url ->
                    OrmaLightButton(
                        text = "Chat on WhatsApp",
                        onClick = { openPublicCatalogPaymentLink(url) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = "Your request goes straight into ORMA for this business. Payment is handled by the selected option.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckoutUpiPaymentCard(
    method: OrmaPublicCatalogPaymentMethod,
    amount: String,
    paymentLink: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Pay with UPI",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Scan this QR or open your UPI app, then place the order so the business can verify payment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(text = "UPI", tone = OrmaStatusTone.Success)
            }
            OrmaQrCode(
                value = paymentLink,
                modifier = Modifier.size(196.dp),
            )
            OrmaKeyValueList(
                rows = listOf(
                    "Amount" to amount,
                    "UPI ID" to method.upiId.orEmpty(),
                    "Pay to" to (method.payeeName ?: method.label).ifBlank { method.label },
                ),
            )
            OrmaLightButton(
                text = "Open UPI app",
                onClick = { openPublicCatalogPaymentLink(paymentLink) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PublicCatalogCheckoutHeader(
    title: String,
    body: String,
    itemCount: Int,
    total: String,
    offerSavings: Double,
    currency: String,
    ready: Boolean,
    submitting: Boolean,
) {
    val status = when {
        submitting -> "Sending"
        ready -> "Ready"
        itemCount > 0 -> "Details needed"
        else -> "Choose items"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.Accent,
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stacked = maxWidth < 430.dp
            val metrics = buildList {
                add("Items" to itemCount.toString())
                add("Total" to total)
                if (offerSavings > 0.0) {
                    add("Saved" to "${currency.ifBlank { "" }} ${money(offerSavings)}".trim())
                }
            }
            if (stacked) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PublicCatalogCheckoutTitle(title = title, body = body, status = status)
                    metrics.chunked(2).forEach { rowMetrics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowMetrics.forEach { (label, value) ->
                                PublicCatalogCheckoutMetric(
                                    label = label,
                                    value = value,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowMetrics.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PublicCatalogCheckoutTitle(
                        title = title,
                        body = body,
                        status = status,
                        modifier = Modifier.weight(1f),
                    )
                    Column(
                        modifier = Modifier.widthIn(min = 150.dp, max = 190.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        metrics.forEach { (label, value) ->
                            PublicCatalogCheckoutMetric(label = label, value = value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckoutTitle(
    title: String,
    body: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PublicCatalogDarkPill(text = status)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OrmaColors.ScreenBackground,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.ScreenBackground.copy(alpha = 0.72f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PublicCatalogDarkPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.Capsule,
        color = OrmaColors.ScreenBackground.copy(alpha = 0.14f),
        border = BorderStroke(0.8.dp, OrmaColors.ScreenBackground.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.ScreenBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PublicCatalogCheckoutMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, OrmaColors.ScreenBackground.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OrmaColors.ScreenBackground.copy(alpha = 0.58f),
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.ScreenBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PublicCatalogCustomerProfileScreen(
    state: PublicCatalogCustomerAccountState,
    actions: PublicCatalogCustomerAccountActions,
    detailReceipt: OrmaPublicCatalogOrderReceipt?,
    detailLoading: Boolean,
    detailRefreshing: Boolean,
    detailError: String?,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    compact: Boolean,
    onBack: () -> Unit,
    onOrderOpen: (OrmaOrder) -> Unit,
    onDetailBack: () -> Unit,
    onDetailRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = state.session ?: return
    val scrollState = rememberScrollState()
    var selectedStatusFilter by remember(session.publicCatalogAccountLabel()) { mutableStateOf("all") }
    val showingDetail = detailReceipt != null || detailLoading
    val filteredOrders = remember(state.orders, selectedStatusFilter) {
        state.orders.publicCatalogFilteredByStatus(selectedStatusFilter)
    }
    Surface(
        modifier = modifier,
        shape = OrmaShapes.PremiumCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(if (compact) 16.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaLightButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.widthIn(min = 88.dp, max = 112.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                OrmaBadge(text = "SIGNED IN", tone = OrmaStatusTone.Success)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PublicCatalogProfileAvatar(
                    modifier = Modifier.size(if (compact) 48.dp else 54.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Your profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = session.publicCatalogAccountLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OrmaLightButton(
                    text = if (state.ordersLoading) "Loading..." else "Refresh orders",
                    onClick = actions.onRefreshOrders,
                    modifier = Modifier.weight(1f),
                    enabled = !state.ordersLoading && !state.authBusy,
                )
                OrmaLightButton(
                    text = "Sign out",
                    onClick = actions.onLogout,
                    modifier = Modifier.weight(1f),
                    enabled = !state.authBusy,
                )
            }
            customerStatusNotice?.let { notice ->
                PublicCatalogCustomerStatusNoticeCard(notice = notice)
            }
            if (showingDetail) {
                PublicCatalogCustomerOrderDetail(
                    receipt = detailReceipt,
                    loading = detailLoading,
                    refreshing = detailRefreshing,
                    error = detailError,
                    onBack = onDetailBack,
                    onRefresh = onDetailRefresh,
                )
            } else {
                PublicCatalogOrderStatusTabs(
                    orders = state.orders,
                    selectedStatus = selectedStatusFilter,
                    onStatusSelected = { selectedStatusFilter = it },
                )
                PublicCatalogCustomerOrderHistory(
                    orders = filteredOrders,
                    totalOrders = state.orders.size,
                    loading = state.ordersLoading,
                    selectedStatus = selectedStatusFilter,
                    onOrderOpen = onOrderOpen,
                )
                state.error?.takeIf { it.isNotBlank() }?.let { message ->
                    PublicCatalogMessageCard(
                        title = "Order history unavailable",
                        body = message,
                        error = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogProfileAvatar(
    modifier: Modifier = Modifier.size(44.dp),
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        PublicCatalogProfileAvatarContent()
    }
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = OrmaShapes.Capsule,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.OnAccent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = OrmaShapes.Capsule,
            color = OrmaColors.Accent,
            contentColor = OrmaColors.OnAccent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = content,
        )
    }
}

@Composable
private fun PublicCatalogProfileAvatarContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        OrmaFlatIcon(
            kind = OrmaFlatIconKind.Profile,
            modifier = Modifier.size(24.dp),
            color = OrmaColors.OnAccent,
        )
    }
}

@Composable
private fun PublicCatalogCustomerAccountPanel(
    state: PublicCatalogCustomerAccountState,
    actions: PublicCatalogCustomerAccountActions,
) {
    OrmaSectionHeader(text = "Account")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.session == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Guest checkout",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    OrmaBadge(text = "OPTIONAL", tone = OrmaStatusTone.Info)
                }
                OrmaLightButton(
                    text = if (state.authBusy) "Signing in..." else "Continue with Google",
                    onClick = actions.onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.authBusy,
                )
                OrmaCountryPhoneField(
                    value = state.phoneNumber,
                    onValueChange = actions.onPhoneChange,
                    label = "Phone login",
                    supportingText = "Use the same phone number used for earlier bookings.",
                )
                if (state.otpSent) {
                    OrmaTextField(
                        value = state.otp,
                        onValueChange = actions.onOtpChange,
                        label = "OTP",
                        placeholder = "6 digit code",
                    )
                }
                OrmaLightButton(
                    text = when {
                        state.authBusy -> "Checking..."
                        state.otpSent -> "Verify OTP"
                        else -> "Send OTP"
                    },
                    onClick = if (state.otpSent) actions.onVerifyOtp else actions.onSendOtp,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.authBusy,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PublicCatalogProfileAvatar()
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = "Signed in",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.session.publicCatalogAccountLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(text = "SIGNED IN", tone = OrmaStatusTone.Success)
                }
                OrmaLightButton(
                    text = "Sign out",
                    onClick = actions.onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.authBusy,
                )
            }
            state.message?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            state.error?.takeIf { it.isNotBlank() }?.let { message ->
                PublicCatalogMessageCard(
                    title = "Account check failed",
                    body = message,
                    error = true,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogOrderStatusTabs(
    orders: List<OrmaOrder>,
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
) {
    val options = orders.publicCatalogStatusFilterOptions()
    if (options.size <= 1) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val selected = selectedStatus == option.key
            Surface(
                modifier = Modifier.clickable { onStatusSelected(option.key) },
                shape = OrmaShapes.Capsule,
                color = if (selected) OrmaColors.Accent else OrmaColors.CellBackground,
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = "${option.label} ${option.count}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) OrmaColors.ScreenBackground else OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogCustomerOrderHistory(
    orders: List<OrmaOrder>,
    totalOrders: Int,
    loading: Boolean,
    selectedStatus: String,
    onOrderOpen: (OrmaOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Your orders",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!loading && orders.isNotEmpty()) {
                OrmaBadge(text = "${orders.size} shown", tone = OrmaStatusTone.Info)
            }
        }
        when {
            loading -> {
                repeat(2) {
                    OrmaSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = OrmaShapes.StandardCell,
                    )
                }
            }
            orders.isEmpty() -> Text(
                text = if (selectedStatus == "all" && totalOrders == 0) {
                    "No purchased orders or bookings found for this account."
                } else {
                    "No orders found for this status."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            else -> orders.forEach { order ->
                PublicCatalogCustomerOrderHistoryRow(
                    order = order,
                    onClick = { onOrderOpen(order) },
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogCustomerOrderHistoryRow(
    order: OrmaOrder,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.orderNumber.ifBlank { order.id },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaBadge(
                    text = order.publicCatalogStatusLabel().uppercase(),
                    tone = order.publicCatalogStatusTone(),
                )
            }
            Text(
                text = order.publicCatalogHistorySummary(),
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listOfNotNull(
                        order.orderType.publicCatalogWorkTitle(),
                        order.scheduledAt?.takeIf { it.isNotBlank() },
                    ).joinToString(" / "),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextSecondary.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${order.currency} ${order.total}",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogCustomerOrderDetail(
    receipt: OrmaPublicCatalogOrderReceipt?,
    loading: Boolean,
    refreshing: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    val order = receipt?.order
    val balanceDue = order?.publicCatalogBalanceDueValue() ?: 0.0
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaLightButton(
                text = "Orders",
                onClick = onBack,
                modifier = Modifier.widthIn(min = 92.dp, max = 124.dp),
                enabled = !loading,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = order?.publicCatalogDetailTitle() ?: "Booking details",
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = order?.orderNumber?.ifBlank { order.id } ?: "Loading selected request",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            order?.let {
                OrmaBadge(
                    text = it.publicCatalogStatusLabel().uppercase(),
                    tone = it.publicCatalogStatusTone(),
                )
            }
        }
        when {
            loading && order == null -> {
                repeat(3) {
                    OrmaSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = OrmaShapes.StandardCell,
                    )
                }
            }
            order != null -> {
                PublicCatalogMessageCard(
                    title = order.publicCatalogStatusTitle(),
                    body = receipt.message.ifBlank { order.publicCatalogStatusBody() },
                    error = false,
                )
                OrmaKeyValueList(
                    rows = buildList {
                        add("Reference" to order.orderNumber.ifBlank { order.id })
                        add("Status" to order.publicCatalogStatusLabel())
                        add("Type" to order.orderType.publicCatalogWorkTitle())
                        order.fulfillmentType.takeIf { it.isNotBlank() }?.let { add("Fulfilment" to it.publicCatalogFulfillmentLabel()) }
                        order.scheduledAt?.takeIf { it.isNotBlank() }?.let { add("Preferred time" to it) }
                        add("Total" to "${order.currency} ${order.total}")
                        if (order.paidTotal.toDoubleOrNull().orZero() > 0.0) {
                            add("Paid" to "${order.currency} ${order.paidTotal}")
                        }
                        if (balanceDue > 0.0) {
                            add("Balance" to order.publicCatalogBalanceDueText())
                        }
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OrmaLightButton(
                        text = if (refreshing) "Checking..." else "Refresh status",
                        onClick = onRefresh,
                        modifier = Modifier.weight(1f),
                        enabled = !loading && !refreshing,
                    )
                    OrmaLightButton(
                        text = "Back",
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        enabled = !loading,
                    )
                }
                if (receipt.paymentLink?.isNotBlank() == true && balanceDue > 0.0) {
                    PublicCatalogUpiPaymentCard(
                        receipt = receipt,
                        balanceDue = balanceDue,
                    )
                }
                PublicCatalogOrderItemsDetail(order = order)
                PublicCatalogOrderSessionsDetail(order = order)
            }
        }
        error?.takeIf { it.isNotBlank() }?.let { message ->
            PublicCatalogMessageCard(
                title = "Could not refresh details",
                body = message,
                error = true,
            )
        }
    }
}

@Composable
private fun PublicCatalogUpiPaymentCard(
    receipt: OrmaPublicCatalogOrderReceipt,
    balanceDue: Double,
) {
    val order = receipt.order
    val link = receipt.paymentLink?.takeIf { it.isNotBlank() } ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Payment required",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Scan the QR or open your UPI app to pay the remaining balance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(text = "UPI", tone = OrmaStatusTone.Success)
            }
            if (link.publicCatalogQrSafe()) {
                OrmaQrCode(
                    value = link,
                    modifier = Modifier.size(196.dp),
                )
            }
            receipt.paymentMethod?.let { method ->
                OrmaKeyValueList(
                    rows = listOf(
                        "Amount" to "${order.currency.ifBlank { "INR" }} ${money(balanceDue)}",
                        "Order ID" to order.orderNumber.ifBlank { order.id },
                        "UPI ID" to method.upiId.orEmpty(),
                        "Pay to" to (method.payeeName ?: method.label),
                    ),
                )
            }
            OrmaLightButton(
                text = "Open UPI app",
                onClick = { openPublicCatalogPaymentLink(link) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PublicCatalogOrderItemsDetail(order: OrmaOrder) {
    if (order.items.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OrmaSectionHeader(text = "Items")
        order.items.forEach { item ->
            PublicCatalogOrderItemRow(item = item, currency = order.currency)
        }
    }
}

@Composable
private fun PublicCatalogOrderItemRow(
    item: OrmaOrderItem,
    currency: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = listOfNotNull(
                        item.productName?.takeIf { it.isNotBlank() } ?: item.description.takeIf { it.isNotBlank() },
                        item.variantName?.takeIf { it.isNotBlank() },
                    ).joinToString(" - ").ifBlank { "Item" },
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.quantity} x ${currency.ifBlank { "INR" }} ${item.unitPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "${currency.ifBlank { "INR" }} ${item.lineTotal}",
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PublicCatalogOrderSessionsDetail(order: OrmaOrder) {
    if (order.sessions.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OrmaSectionHeader(text = "Sessions")
        order.sessions.sortedBy { it.sequenceNumber }.forEach { session ->
            PublicCatalogOrderSessionRow(session = session)
        }
    }
}

@Composable
private fun PublicCatalogOrderSessionRow(session: OrmaOrderSession) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = session.title.ifBlank { "Session ${session.sequenceNumber}" },
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = session.scheduledAt?.takeIf { it.isNotBlank() } ?: "Not scheduled yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = session.status.ifBlank { "scheduled" }.replace('_', ' ').uppercase(),
                tone = session.status.publicCatalogSessionStatusTone(),
            )
        }
    }
}

@Composable
private fun PublicCatalogProductTile(
    item: PublicCatalogDisplayItem,
    quantity: Int,
    variantQuantities: Map<String, Int>,
    activeCartType: String?,
    onQuantityChange: (Int) -> Unit,
    onVariantPickerOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = item.product
    val displayVariant = item.variant
    val standalonePackage = displayVariant != null
    val activeVariants = if (standalonePackage) emptyList() else product.publicCatalogActiveVariants()
    val hasVariants = activeVariants.isNotEmpty()
    val selectedVariantCount = variantQuantities.values.sum()
    val selected = quantity > 0 || selectedVariantCount > 0
    val productType = product.itemType.publicCatalogNormalizedItemType()
    val lockedByType = activeCartType != null && activeCartType != productType && !selected
    val maxQuantity = product.publicCatalogMaxSelectableQuantity(displayVariant)
    val enabled = maxQuantity > 0 && !lockedByType
    val tileEnabled = enabled || selected
    val actionLabel = when {
        maxQuantity <= 0 -> "Unavailable"
        lockedByType -> "Clear ${activeCartType.publicCatalogItemTypeLabel().lowercase()} cart"
        hasVariants && selectedVariantCount > 0 -> "$selectedVariantCount selected"
        hasVariants -> "Choose option"
        selected -> "Selected"
        standalonePackage && productType == "appointment" -> "Book package"
        standalonePackage && productType == "service" -> "Request package"
        standalonePackage -> "Place order"
        productType == "appointment" -> "Book"
        productType == "service" -> "Request"
        else -> "Add"
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = tileEnabled) {
                if (hasVariants) {
                    onVariantPickerOpen()
                } else {
                    onQuantityChange(if (quantity == 0) 1 else quantity)
                }
            },
        shape = OrmaShapes.StandardCell,
        color = when {
            selected -> OrmaColors.Accent.copy(alpha = 0.06f)
            lockedByType -> OrmaColors.CellBackground.copy(alpha = 0.58f)
            else -> Color.White
        },
        border = BorderStroke(
            width = if (selected) 1.1.dp else 0.8.dp,
            color = when {
                selected -> OrmaColors.Accent.copy(alpha = 0.38f)
                lockedByType -> OrmaColors.Hairline.copy(alpha = 0.62f)
                else -> OrmaColors.Hairline
            },
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                PublicCatalogProductMedia(
                    product = product,
                    selected = selected,
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (product.offer != null) {
                        OrmaBadge(text = "OFFER", tone = OrmaStatusTone.Success)
                    }
                    if (standalonePackage) {
                        OrmaBadge(text = "PACKAGE", tone = OrmaStatusTone.Success)
                    }
                    OrmaBadge(
                        text = productType.publicCatalogItemTypeLabel().uppercase(),
                        tone = when (productType) {
                            "appointment" -> OrmaStatusTone.Warning
                            "service" -> OrmaStatusTone.Info
                            else -> OrmaStatusTone.Neutral
                        },
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (lockedByType) OrmaColors.TextSecondary else OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    PublicCatalogOfferPrice(product = product, variant = displayVariant)
                    Text(
                        text = item.availabilityText,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!hasVariants) {
                    QuantityStepper(
                        quantity = quantity,
                        enabled = enabled || selected,
                        maxQuantity = maxQuantity,
                        onQuantityChange = onQuantityChange,
                    )
                }
            }
            if (hasVariants) {
                PublicCatalogVariantSummary(
                    product = product,
                    activeVariants = activeVariants,
                    selectedCount = selectedVariantCount,
                    enabled = enabled || selected,
                    onClick = onVariantPickerOpen,
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = tileEnabled) {
                        if (hasVariants) {
                            onVariantPickerOpen()
                        } else {
                            onQuantityChange(if (quantity == 0) 1 else quantity)
                        }
                    },
                shape = OrmaShapes.Capsule,
                color = if (enabled || selected) OrmaColors.Accent else OrmaColors.CellBackground,
                contentColor = if (enabled || selected) OrmaColors.OnAccent else OrmaColors.TextTertiary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PublicCatalogVariantSummary(
    product: OrmaPublicCatalogProduct,
    activeVariants: List<OrmaProductVariant>,
    selectedCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (selectedCount > 0) "$selectedCount option${if (selectedCount == 1) "" else "s"} selected" else "${activeVariants.size} options available",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = activeVariants.take(3).joinToString(", ") { it.name.ifBlank { "Option" } },
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(
                text = product.itemType.publicCatalogItemTypeLabel().uppercase(),
                tone = OrmaStatusTone.Info,
            )
        }
    }
}

@Composable
private fun PublicCatalogVariantPickerSheet(
    compact: Boolean,
    product: OrmaPublicCatalogProduct,
    variantQuantities: Map<String, Int>,
    activeCartType: String?,
    onDismiss: () -> Unit,
    onVariantQuantityChange: (String, Int) -> Unit,
) {
    val sheetScrollState = rememberScrollState()
    val activeVariants = product.publicCatalogActiveVariants()
    val productType = product.itemType.publicCatalogNormalizedItemType()
    val selectedCount = variantQuantities.values.sum()
    val lockedByType = activeCartType != null && activeCartType != productType && selectedCount == 0
    val enabled = product.inStock && !lockedByType
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(onClick = onDismiss),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sheetModifier = if (compact) {
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .heightIn(max = maxHeight - 20.dp)
            } else {
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.38f)
                    .widthIn(min = 430.dp, max = 560.dp)
                    .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 18.dp)
            }
            Surface(
                modifier = sheetModifier,
                shape = OrmaShapes.PremiumCard,
                color = OrmaColors.CardBackground,
                border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(sheetScrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PublicCatalogProductMedia(product = product, selected = selectedCount > 0)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = product.name.ifBlank { "Item" },
                                style = MaterialTheme.typography.titleMedium,
                                color = OrmaColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = when {
                                    lockedByType -> "Clear the ${activeCartType.orEmpty().publicCatalogItemTypeLabel().lowercase()} cart to choose this."
                                    activeVariants.isEmpty() -> "No active options are available."
                                    else -> "Choose the option to add."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        OrmaLightButton(
                            text = "Close",
                            onClick = onDismiss,
                            modifier = Modifier.widthIn(min = 92.dp, max = 116.dp),
                        )
                    }
                    if (activeVariants.isEmpty()) {
                        PublicCatalogEmpty(
                            title = "No options",
                            body = "This item does not have active options right now.",
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activeVariants.forEach { variant ->
                                val variantQuantity = variantQuantities[variant.id] ?: 0
                                val variantMaxQuantity = product.publicCatalogMaxSelectableQuantity(variant)
                                PublicCatalogVariantPickerRow(
                                    product = product,
                                    variant = variant,
                                    quantity = variantQuantity,
                                    enabled = enabled && variantMaxQuantity > 0,
                                    maxQuantity = variantMaxQuantity,
                                    onQuantityChange = { onVariantQuantityChange(variant.id, it) },
                                )
                            }
                        }
                    }
                    OrmaFullButton(
                        text = if (selectedCount > 0) "Done ($selectedCount selected)" else "Done",
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogVariantPickerRow(
    product: OrmaPublicCatalogProduct,
    variant: OrmaProductVariant,
    quantity: Int,
    enabled: Boolean,
    maxQuantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                onQuantityChange(if (quantity == 0) 1 else quantity)
            },
        shape = OrmaShapes.StandardCell,
        color = if (quantity > 0) OrmaColors.Accent.copy(alpha = 0.08f) else OrmaColors.ScreenBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            width = if (quantity > 0) 1.1.dp else 0.7.dp,
            color = if (quantity > 0) OrmaColors.Accent.copy(alpha = 0.36f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = variant.name.ifBlank { "Option" },
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(product.currency)
                        append(" ")
                        append(product.customerPriceFor(variant))
                        variant.publicCatalogPackageDetail(product.itemType, product.unit)?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (variant.addons.isNotEmpty()) {
                    Text(
                        text = "${variant.addons.size} add-ons available",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.Success,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                variant.publicCatalogComponentSummary()?.let { componentSummary ->
                    Text(
                        text = componentSummary,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            QuantityStepper(
                quantity = quantity,
                enabled = enabled,
                maxQuantity = maxQuantity,
                onQuantityChange = onQuantityChange,
            )
        }
    }
}

@Composable
private fun PublicCatalogProductRow(
    product: OrmaPublicCatalogProduct,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    val selected = quantity > 0
    val maxQuantity = product.publicCatalogMaxSelectableQuantity()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.inStock) {
                onQuantityChange(if (quantity == 0) 1 else quantity)
            },
        shape = OrmaShapes.StandardCell,
        color = if (selected) OrmaColors.Accent.copy(alpha = 0.06f) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.2.dp else 0.8.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.40f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 460.dp) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PublicCatalogProductMedia(product = product, selected = selected)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = product.name.ifBlank { "Item" },
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = OrmaColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (!product.inStock) {
                                    OrmaBadge(text = "OUT", tone = OrmaStatusTone.Danger)
                                } else if (selected) {
                                    OrmaBadge(text = "IN CART", tone = OrmaStatusTone.Success)
                                } else if (product.itemType != "product") {
                                    OrmaBadge(text = product.itemType.publicCatalogItemTypeLabel().uppercase(), tone = OrmaStatusTone.Info)
                                }
                            }
                            product.description?.takeIf(String::isNotBlank)?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrmaColors.TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            PublicCatalogOfferPrice(product = product)
                            Text(
                                text = product.publicCatalogAvailabilityLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        QuantityStepper(
                            quantity = quantity,
                            enabled = product.inStock,
                            maxQuantity = maxQuantity,
                            onQuantityChange = onQuantityChange,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PublicCatalogProductMedia(product = product, selected = selected)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = product.name.ifBlank { "Item" },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = OrmaColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!product.inStock) {
                                OrmaBadge(text = "OUT", tone = OrmaStatusTone.Danger)
                            } else if (selected) {
                                OrmaBadge(text = "IN CART", tone = OrmaStatusTone.Success)
                            } else if (product.itemType != "product") {
                                OrmaBadge(text = product.itemType.publicCatalogItemTypeLabel().uppercase(), tone = OrmaStatusTone.Info)
                            }
                        }
                        product.description?.takeIf(String::isNotBlank)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            PublicCatalogOfferPrice(product = product)
                            Text(
                                text = product.publicCatalogAvailabilityLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    QuantityStepper(
                        quantity = quantity,
                        enabled = product.inStock,
                        maxQuantity = maxQuantity,
                        onQuantityChange = onQuantityChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogOfferPrice(
    product: OrmaPublicCatalogProduct,
    variant: OrmaProductVariant? = null,
) {
    val offer = product.offer
    val basePrice = variant?.sellingPrice?.takeIf { it.isNotBlank() } ?: product.sellingPrice
    val discountAmount = product.discountFor(variant)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        OrmaPrice(amount = product.customerPriceFor(variant), currency = product.currency)
        if (offer != null && discountAmount > 0.0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${product.currency} $basePrice",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                )
                Text(
                    text = "Save ${product.currency} ${money(discountAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrmaColors.Success,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaBadge(text = offer.name.uppercase(), tone = OrmaStatusTone.Success)
        }
    }
}

@Composable
private fun PublicCatalogProductMedia(
    product: OrmaPublicCatalogProduct,
    selected: Boolean,
) {
    Surface(
        modifier = Modifier
            .size(74.dp)
            .clip(OrmaShapes.StandardCell),
        shape = OrmaShapes.StandardCell,
        color = if (selected) OrmaColors.Accent else OrmaColors.CellBackground,
        contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        product.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
            OrmaRemoteImage(
                url = imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
            )
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = publicCatalogInitials(product.name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) OrmaColors.OnAccent else OrmaColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    enabled: Boolean,
    maxQuantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(text = "-", enabled = enabled && quantity > 0) {
            onQuantityChange(quantity - 1)
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            textAlign = TextAlign.Center,
        )
        StepperButton(text = "+", enabled = enabled && quantity < maxQuantity) {
            onQuantityChange(quantity + 1)
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(34.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
        contentColor = if (enabled) OrmaColors.OnAccent else OrmaColors.TextDisabled,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun PublicCatalogSelectionSummary(
    catalog: OrmaPublicCatalog?,
    selectedItems: List<PublicCatalogSelection>,
    total: Double,
    offerSavings: Double,
    onClearSelection: () -> Unit,
) {
    val itemCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground.copy(alpha = 0.72f),
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Your order",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (selectedItems.isEmpty()) {
                            "Add items from the menu below."
                        } else {
                            "$itemCount item${if (itemCount == 1) "" else "s"} selected"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = if (selectedItems.isEmpty()) "EMPTY" else "ACTIVE",
                    tone = if (selectedItems.isEmpty()) OrmaStatusTone.Neutral else OrmaStatusTone.Success,
                )
            }
            if (selectedItems.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = OrmaShapes.SmallCard,
                    color = OrmaColors.CardBackground,
                    border = BorderStroke(0.8.dp, OrmaColors.Hairline),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = "Choose a product, service, or booking slot from the menu to unlock checkout.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrmaColors.TextSecondary,
                    )
                }
            } else {
                selectedItems.forEach { selection ->
                    PublicCatalogSummaryLine(
                        selection = selection,
                    )
                }
                if (offerSavings > 0.0) {
                    PublicCatalogAppliedOffersCard(
                        selectedItems = selectedItems,
                        currency = currency,
                        offerSavings = offerSavings,
                    )
                }
                OrmaLightButton(
                    text = "Clear selection",
                    onClick = onClearSelection,
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider(color = OrmaColors.Divider)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = OrmaShapes.SmallCard,
                    color = OrmaColors.Accent,
                    contentColor = OrmaColors.OnAccent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Estimated total",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrmaColors.ScreenBackground.copy(alpha = 0.68f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (offerSavings > 0.0) "Offer savings included." else "Business confirms total.",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrmaColors.ScreenBackground.copy(alpha = 0.52f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "${currency.ifBlank { "" }} ${money(total)}".trim(),
                            style = MaterialTheme.typography.titleSmall,
                            color = OrmaColors.ScreenBackground,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogAppliedOffersCard(
    selectedItems: List<PublicCatalogSelection>,
    currency: String,
    offerSavings: Double,
) {
    val appliedOfferLines = selectedItems.mapNotNull { selection ->
        val offer = selection.product.offer ?: return@mapNotNull null
        val savings = selection.product.discountFor(selection.variant) * selection.quantity
        if (savings <= 0.0) return@mapNotNull null
        Triple(
            offer.name.ifBlank { "Offer" },
            listOf(selection.product.name.ifBlank { "Item" }, selection.variant?.name)
                .filterNotNull()
                .joinToString(" - "),
            savings,
        )
    }
    if (appliedOfferLines.isEmpty()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.Success.copy(alpha = 0.07f),
        border = BorderStroke(0.8.dp, OrmaColors.Success.copy(alpha = 0.22f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Offers applied",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Discounts are included in checkout total.",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OrmaBadge(
                    text = "${currency.ifBlank { "" }} ${money(offerSavings)} saved".trim().uppercase(),
                    tone = OrmaStatusTone.Success,
                )
            }
            appliedOfferLines.forEach { (offerName, productName, savings) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            text = offerName,
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "${currency.ifBlank { "" }} ${money(savings)}".trim(),
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.Success,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogSummaryLine(
    selection: PublicCatalogSelection,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = OrmaShapes.SmallCard,
            color = OrmaColors.CardBackground,
            contentColor = OrmaColors.Accent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            selection.product.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                OrmaRemoteImage(
                    url = imageUrl,
                    contentDescription = selection.product.name,
                    modifier = Modifier.fillMaxSize(),
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = publicCatalogInitials(selection.product.name),
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = listOf(selection.product.name.ifBlank { "Item" }, selection.variant?.name)
                    .filterNotNull()
                    .joinToString(" - "),
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${selection.quantity} x ${selection.product.currency} ${selection.customerPrice}",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            selection.product.offer?.let { offer ->
                val savings = selection.product.discountFor(selection.variant) * selection.quantity
                if (savings > 0.0) {
                    Text(
                        text = "${offer.name}: save ${selection.product.currency} ${money(savings)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.Success,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Text(
            text = "${selection.product.currency} ${money(selection.lineTotal)}",
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PublicCatalogSuccess(
    receipt: OrmaPublicCatalogOrderReceipt,
    refreshing: Boolean,
    customerStatusNotice: PublicCatalogCustomerStatusNotice?,
    onRefresh: () -> Unit,
    onNewOrder: () -> Unit,
) {
    val order = receipt.order
    val balanceDue = order.publicCatalogBalanceDueValue()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        customerStatusNotice
            ?.takeIf { it.orderId == order.id }
            ?.let { notice ->
                PublicCatalogCustomerStatusNoticeCard(notice = notice)
            }
        PublicCatalogMessageCard(
            title = order.publicCatalogStatusTitle(),
            body = receipt.message.ifBlank { order.publicCatalogStatusBody() },
            error = false,
        )
        OrmaKeyValueList(
            rows = buildList {
                add("Reference" to order.orderNumber.ifBlank { order.id })
                add("Status" to order.publicCatalogStatusLabel())
                add("Type" to order.orderType.publicCatalogWorkTitle())
                order.scheduledAt?.takeIf { it.isNotBlank() }?.let { add("Preferred time" to it) }
                add("Total" to "${order.currency} ${order.total}")
                if (order.paidTotal.toDoubleOrNull().orZero() > 0.0) {
                    add("Paid" to "${order.currency} ${order.paidTotal}")
                }
                if (balanceDue > 0.0) {
                    add("Balance" to order.publicCatalogBalanceDueText())
                }
            },
        )
        OrmaLightButton(
            text = if (refreshing) "Checking status..." else "Refresh status",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            enabled = !refreshing,
        )
        OrmaFullButton(
            text = "New order",
            onClick = onNewOrder,
            modifier = Modifier.fillMaxWidth(),
            enabled = !refreshing,
        )
        receipt.paymentLink?.takeIf { it.isNotBlank() && balanceDue > 0.0 }?.let { link ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.SmallCard,
                color = OrmaColors.ScreenBackground,
                contentColor = OrmaColors.TextPrimary,
                border = BorderStroke(0.6.dp, OrmaColors.Hairline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = "Pay with UPI",
                                style = MaterialTheme.typography.titleMedium,
                                color = OrmaColors.TextPrimary,
                            )
                            Text(
                                text = "Scan this QR to pay the balance for this order.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrmaColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        OrmaBadge(text = "UPI", tone = OrmaStatusTone.Success)
                    }
                    if (link.publicCatalogQrSafe()) {
                        OrmaQrCode(
                            value = link,
                            modifier = Modifier.size(196.dp),
                        )
                    }
                    receipt.paymentMethod?.let { method ->
                        OrmaKeyValueList(
                            rows = listOf(
                                "Amount" to order.publicCatalogBalanceDueText(),
                                "Order ID" to order.orderNumber.ifBlank { order.id },
                                "UPI ID" to method.upiId.orEmpty(),
                                "Pay to" to (method.payeeName ?: method.label),
                            ),
                        )
                    }
                    OrmaLightButton(
                        text = "Open UPI app",
                        onClick = { openPublicCatalogPaymentLink(link) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun OrmaOrder.publicCatalogBalanceDueValue(): Double =
    (total.toDoubleOrNull().orZero() - paidTotal.toDoubleOrNull().orZero()).coerceAtLeast(0.0)

private fun OrmaOrder.publicCatalogBalanceDueText(): String =
    "${currency.ifBlank { "INR" }} ${money(publicCatalogBalanceDueValue())}"

private fun String.publicCatalogQrSafe(): Boolean =
    encodeToByteArray().size <= 106

private fun OrmaOrder.publicCatalogStatusTitle(): String =
    when (status.trim().lowercase()) {
        "confirmed" -> "${orderType.publicCatalogWorkTitle()} confirmed"
        "part_paid" -> "Payment partly received"
        "paid" -> "Payment received"
        "completed" -> "${orderType.publicCatalogWorkTitle()} completed"
        "cancelled" -> "${orderType.publicCatalogWorkTitle()} cancelled"
        else -> "${orderType.publicCatalogWorkTitle()} sent"
    }

private fun OrmaOrder.publicCatalogStatusBody(): String =
    when (status.trim().lowercase()) {
        "confirmed" -> "The business accepted this request and will prepare the next step."
        "part_paid" -> "The business recorded a partial payment. They may contact you for the balance."
        "paid" -> "Payment is recorded. The business will prepare or complete the request."
        "completed" -> "This request is completed."
        "cancelled" -> "The business rejected or cancelled this request."
        else -> "This request is waiting for the business to confirm or reject."
    }

private fun OrmaOrder.publicCatalogStatusLabel(): String =
    when (status.trim().lowercase()) {
        "draft" -> "Waiting for confirmation"
        "confirmed" -> "Confirmed"
        "part_paid" -> "Part paid"
        "paid" -> "Paid"
        "completed" -> "Completed"
        "cancelled" -> "Rejected or cancelled"
        else -> status.ifBlank { "Pending" }.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

private fun OrmaOrder.publicCatalogStatusTone(): OrmaStatusTone =
    when (status.trim().lowercase()) {
        "new", "draft", "pending" -> OrmaStatusTone.Neutral
        "confirmed" -> OrmaStatusTone.Info
        "part_paid" -> OrmaStatusTone.Warning
        "paid", "completed", "fulfilled" -> OrmaStatusTone.Success
        "cancelled", "canceled", "rejected", "rejected_or_cancelled", "failed" -> OrmaStatusTone.Danger
        else -> OrmaStatusTone.Neutral
    }

private fun OrmaOrder.publicCatalogCustomerStatusNotice(previous: OrmaOrder?): PublicCatalogCustomerStatusNotice? {
    val oldStatus = previous?.status?.trim()?.lowercase().orEmpty()
    val nextStatus = status.trim().lowercase()
    val oldPaid = previous?.paidTotal.orEmpty()
    val oldSchedule = previous?.scheduledAt.orEmpty()
    if (
        previous == null ||
        (oldStatus == nextStatus && oldPaid == paidTotal && oldSchedule == scheduledAt.orEmpty())
    ) {
        return null
    }
    return PublicCatalogCustomerStatusNotice(
        orderId = id,
        orderNumber = orderNumber.ifBlank { id },
        title = publicCatalogCustomerUpdateTitle(),
        body = publicCatalogCustomerUpdateBody(),
        tone = publicCatalogStatusTone(),
    )
}

private fun List<OrmaOrder>.publicCatalogCustomerStatusNotice(
    previousOrders: Map<String, OrmaOrder>,
): PublicCatalogCustomerStatusNotice? =
    firstNotNullOfOrNull { order ->
        order.publicCatalogCustomerStatusNotice(previousOrders[order.id])
    }

private fun OrmaPublicCatalogOrderReceipt?.publicCatalogReplaceReceipt(
    next: OrmaPublicCatalogOrderReceipt,
): OrmaPublicCatalogOrderReceipt? =
    when {
        this == null -> null
        order.id == next.order.id -> next
        else -> this
    }

private fun List<OrmaOrder>.publicCatalogReplaceOrder(next: OrmaOrder): List<OrmaOrder> =
    if (none { it.id == next.id }) this else map { order -> if (order.id == next.id) next else order }

private fun String.publicCatalogShouldPollForCustomerStatus(): Boolean =
    trim().lowercase() !in setOf("completed", "cancelled", "canceled", "rejected", "rejected_or_cancelled", "failed")

private fun OrmaOrder.publicCatalogCustomerUpdateTitle(): String =
    when (status.trim().lowercase()) {
        "confirmed" -> "${orderType.publicCatalogWorkTitle()} confirmed"
        "part_paid" -> "Payment update received"
        "paid" -> "Payment marked as paid"
        "completed" -> "${orderType.publicCatalogWorkTitle()} completed"
        "cancelled", "canceled", "rejected", "rejected_or_cancelled" -> "${orderType.publicCatalogWorkTitle()} cancelled"
        else -> "Status updated"
    }

private fun OrmaOrder.publicCatalogCustomerUpdateBody(): String {
    val reference = orderNumber.ifBlank { id }
    val balance = publicCatalogBalanceDueValue()
    val statusLine = when (status.trim().lowercase()) {
        "confirmed" -> "The business accepted $reference and will prepare the next step."
        "part_paid" -> "The business recorded a partial payment for $reference."
        "paid" -> "The business marked payment as received for $reference."
        "completed" -> "$reference is completed."
        "cancelled", "canceled", "rejected", "rejected_or_cancelled" -> "The business rejected or cancelled $reference."
        else -> "$reference is now ${publicCatalogStatusLabel().lowercase()}."
    }
    val balanceLine = if (balance > 0.0 && status.trim().lowercase() in setOf("confirmed", "part_paid")) {
        " Balance due: ${publicCatalogBalanceDueText()}."
    } else {
        ""
    }
    val timeLine = scheduledAt?.takeIf { it.isNotBlank() }?.let { " Preferred time: $it." }.orEmpty()
    return "$statusLine$balanceLine$timeLine"
}

private fun OrmaOrder.publicCatalogStatusFilterKey(): String =
    status.trim().lowercase().ifBlank { "pending" }

private fun List<OrmaOrder>.publicCatalogFilteredByStatus(statusFilter: String): List<OrmaOrder> =
    if (statusFilter == "all") this else filter { it.publicCatalogStatusFilterKey() == statusFilter }

private fun List<OrmaOrder>.publicCatalogStatusFilterOptions(): List<PublicCatalogOrderStatusFilterOption> {
    val statusCounts = groupingBy { it.publicCatalogStatusFilterKey() }.eachCount()
    val sortedStatusKeys = statusCounts.keys.sortedWith(compareBy({ key ->
        when (key) {
            "new" -> 0
            "draft" -> 1
            "confirmed" -> 2
            "part_paid" -> 3
            "paid" -> 4
            "completed" -> 5
            "cancelled", "canceled", "rejected", "rejected_or_cancelled" -> 6
            else -> 7
        }
    }, { it }))
    return listOf(PublicCatalogOrderStatusFilterOption("all", "All", size)) +
        sortedStatusKeys.map { key ->
            val sample = firstOrNull { it.publicCatalogStatusFilterKey() == key }
            PublicCatalogOrderStatusFilterOption(
                key = key,
                label = sample?.publicCatalogStatusLabel() ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() },
                count = statusCounts[key] ?: 0,
            )
        }
}

private fun OrmaOrder.publicCatalogDetailTitle(): String =
    when (orderType.trim().lowercase()) {
        "appointment" -> "Booking details"
        "service" -> "Service details"
        else -> "Order details"
    }

private fun String.publicCatalogFulfillmentLabel(): String =
    when (trim().lowercase()) {
        "take_away" -> "Take away"
        "delivery" -> "Delivery"
        "scheduled" -> "Scheduled"
        "booking" -> "Booking"
        "standard" -> "Standard"
        else -> replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

private fun String.publicCatalogTimingPaymentTitle(): String =
    when (publicCatalogNormalizedItemType()) {
        "appointment" -> "Appointment and payment"
        "service" -> "Service timing and payment"
        else -> "Pickup and payment"
    }

private fun String.publicCatalogCheckoutFulfillmentLabel(selectedFlow: String): String =
    when (trim().lowercase()) {
        "scheduled" -> if (selectedFlow.publicCatalogNormalizedItemType() == "service") {
            "Schedule service"
        } else {
            "Schedule pickup"
        }
        "standard" -> "Request now"
        "delivery" -> "Delivery"
        else -> "Take away"
    }

private fun String.publicCatalogCheckoutPaymentLabel(selectedFlow: String): String =
    when (trim().lowercase()) {
        "upi" -> "Pay by UPI"
        else -> when (selectedFlow.publicCatalogNormalizedItemType()) {
            "appointment" -> "Pay at visit"
            "service" -> "Pay after service"
            else -> "Pay at pickup"
        }
    }

private fun String.publicCatalogSessionStatusTone(): OrmaStatusTone =
    when (trim().lowercase()) {
        "completed" -> OrmaStatusTone.Success
        "cancelled", "canceled", "missed" -> OrmaStatusTone.Danger
        "confirmed", "scheduled" -> OrmaStatusTone.Info
        "part_paid" -> OrmaStatusTone.Warning
        else -> OrmaStatusTone.Neutral
    }

private fun OrmaAuthSession.publicCatalogAccountLabel(): String =
    listOfNotNull(
        displayName?.takeIf { it.isNotBlank() },
        email?.takeIf { it.isNotBlank() },
        phoneNumber?.takeIf { it.isNotBlank() },
    ).firstOrNull()
        ?: "Customer account"

private fun OrmaAuthSession.publicCatalogCustomerNameFallback(): String =
    listOfNotNull(
        displayName?.trim()?.takeIf { it.length >= 2 },
        email
            ?.substringBefore("@")
            ?.replace('.', ' ')
            ?.replace('_', ' ')
            ?.replace('-', ' ')
            ?.trim()
            ?.takeIf { it.length >= 2 }
            ?.replaceFirstChar { it.uppercase() },
        phoneNumber?.trim()?.takeIf { it.length >= 2 },
    ).firstOrNull().orEmpty()

private fun OrmaOrder.publicCatalogHistorySummary(): String {
    val names = items.mapNotNull { item ->
        item.variantName?.takeIf { it.isNotBlank() }
            ?: item.productName?.takeIf { it.isNotBlank() }
            ?: item.description.takeIf { it.isNotBlank() }
    }.take(2)
    val summary = names.joinToString(", ").ifBlank { orderType.publicCatalogWorkTitle() }
    val remaining = (itemCount - names.size).coerceAtLeast(0)
    return if (remaining > 0) "$summary +$remaining" else summary
}

private fun String.publicCatalogWorkTitle(): String =
    when (trim().lowercase()) {
        "appointment" -> "Appointment"
        "service" -> "Service request"
        else -> "Order"
    }

@Composable
private fun PublicCatalogCustomerStatusNoticeCard(
    notice: PublicCatalogCustomerStatusNotice,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = when (notice.tone) {
            OrmaStatusTone.Danger -> OrmaColors.Error.copy(alpha = 0.08f)
            OrmaStatusTone.Success -> OrmaColors.Success.copy(alpha = 0.08f)
            OrmaStatusTone.Warning -> OrmaColors.Warning.copy(alpha = 0.10f)
            else -> OrmaColors.Accent.copy(alpha = 0.06f)
        },
        border = BorderStroke(
            width = 0.8.dp,
            color = when (notice.tone) {
                OrmaStatusTone.Danger -> OrmaColors.Error.copy(alpha = 0.24f)
                OrmaStatusTone.Success -> OrmaColors.Success.copy(alpha = 0.24f)
                OrmaStatusTone.Warning -> OrmaColors.Warning.copy(alpha = 0.28f)
                else -> OrmaColors.Hairline
            },
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notice.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OrmaBadge(
                    text = notice.orderNumber,
                    tone = notice.tone,
                )
            }
            Text(
                text = notice.body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PublicCatalogMessageCard(
    title: String,
    body: String,
    error: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.SmallCard,
        color = if (error) OrmaColors.Error.copy(alpha = 0.08f) else OrmaColors.Accent.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (error) OrmaColors.Error.copy(alpha = 0.24f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (error) OrmaColors.Error else OrmaColors.TextPrimary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (error) OrmaColors.Error.copy(alpha = 0.80f) else OrmaColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun PublicCatalogEmpty(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = OrmaColors.TextPrimary)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = OrmaColors.TextSecondary)
        }
    }
}

@Composable
private fun PublicCatalogSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(4) {
            OrmaSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                shape = OrmaShapes.SmallCard,
            )
        }
    }
}

private data class PublicCatalogDisplayItem(
    val product: OrmaPublicCatalogProduct,
    val variant: OrmaProductVariant? = null,
) {
    val cartKey: String = product.publicCatalogCartKey(variant?.id)
    val displayName: String = variant?.name?.takeIf { it.isNotBlank() } ?: product.name.ifBlank { "Item" }
    val supportingText: String
        get() = if (variant == null) {
            product.description?.takeIf(String::isNotBlank) ?: product.publicCatalogAvailabilityLabel()
        } else {
            listOfNotNull(
                "From ${product.name.ifBlank { "catalog item" }}",
                variant.publicCatalogPackageDetail(product.itemType, product.unit),
                variant.publicCatalogComponentSummary(),
                variant.publicCatalogAddonSummary(),
            ).joinToString(" / ").ifBlank {
                product.description?.takeIf(String::isNotBlank) ?: product.publicCatalogAvailabilityLabel(variant)
            }
        }
    val availabilityText: String
        get() = product.publicCatalogAvailabilityLabel(variant)
}

private data class PublicCatalogCustomerAccountState(
    val session: OrmaAuthSession?,
    val orders: List<OrmaOrder>,
    val ordersLoading: Boolean,
    val authBusy: Boolean,
    val message: String?,
    val error: String?,
    val phoneNumber: String,
    val otp: String,
    val otpSent: Boolean,
)

private data class PublicCatalogCustomerAccountActions(
    val onPhoneChange: (String) -> Unit,
    val onOtpChange: (String) -> Unit,
    val onSendOtp: () -> Unit,
    val onVerifyOtp: () -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onRefreshOrders: () -> Unit,
    val onLogout: () -> Unit,
)

private data class PublicCatalogCustomerStatusNotice(
    val orderId: String,
    val orderNumber: String,
    val title: String,
    val body: String,
    val tone: OrmaStatusTone,
)

private data class PublicCatalogOrderStatusFilterOption(
    val key: String,
    val label: String,
    val count: Int,
)

private data class PublicCatalogSelection(
    val product: OrmaPublicCatalogProduct,
    val variant: OrmaProductVariant? = null,
    val quantity: Int,
) {
    val customerPrice: String = product.customerPriceFor(variant)
    val lineTotal: Double = customerPrice.toDoubleOrNull().orZero() * quantity
}

private fun OrmaPublicCatalogProduct.publicCatalogCartKey(variantId: String? = null): String =
    if (variantId.isNullOrBlank()) id else "$id|$variantId"

private fun List<OrmaPublicCatalogProduct>.publicCatalogDisplayItems(): List<PublicCatalogDisplayItem> =
    flatMap { product ->
        listOf(PublicCatalogDisplayItem(product = product)) +
            product.publicCatalogActiveVariants()
                .filter { it.publicCatalogIsPackageVariant(product.itemType) }
                .map { variant -> PublicCatalogDisplayItem(product = product, variant = variant) }
    }

private fun PublicCatalogDisplayItem.matchesPublicCatalogSearch(query: String): Boolean {
    val productType = product.itemType.publicCatalogNormalizedItemType()
    val haystack = listOfNotNull(
        displayName,
        supportingText,
        availabilityText,
        product.name,
        product.description,
        product.categoryName,
        productType.publicCatalogItemTypeLabel(),
        variant?.sku,
        variant?.barcode,
        variant?.addons?.joinToString(" ") { it.name },
        variant?.components?.joinToString(" ") { component ->
            listOfNotNull(component.productName, component.variantName).joinToString(" ")
        },
    ).joinToString(" ")
    return haystack.contains(query, ignoreCase = true)
}

private fun String.publicCatalogCartProductId(): String = substringBefore("|")

private fun String.publicCatalogCartVariantId(): String? =
    substringAfter("|", "").takeIf { it.isNotBlank() }

private fun Double?.orZero(): Double = this ?: 0.0

private fun List<PublicCatalogSelection>.catalogOrderFlow(): String =
    when {
        any { it.product.itemType.publicCatalogNormalizedItemType() == "appointment" } -> "appointment"
        isNotEmpty() && all { it.product.itemType.publicCatalogNormalizedItemType() == "service" } -> "service"
        else -> "sale"
    }

private fun List<PublicCatalogSelection>.publicCatalogOfferSavingsTotal(): Double =
    sumOf { selection -> selection.product.discountFor(selection.variant) * selection.quantity }

private fun OrmaProductVariant.publicCatalogPackageDetail(itemType: String, unit: String): String? {
    val count = includedQuantity.takeIf { it > 1 } ?: return null
    return when (itemType.publicCatalogNormalizedItemType()) {
        "appointment" -> "$count sessions"
        "service" -> "$count service sessions"
        else -> "$count ${unit.ifBlank { "items" }}"
    }
}

private fun OrmaProductVariant.publicCatalogIsPackageVariant(itemType: String): Boolean =
    includedQuantity.coerceAtLeast(1) > 1 ||
        components.any { it.status.trim().lowercase() == "active" } ||
        (itemType.publicCatalogNormalizedItemType() != "product" &&
            addons.any { it.status.trim().lowercase() == "active" })

private fun OrmaProductVariant.publicCatalogAddonSummary(): String? {
    val activeAddons = addons.filter { it.status.trim().lowercase() == "active" }
    return activeAddons.takeIf { it.isNotEmpty() }?.let { addons ->
        "${addons.size} add-on${if (addons.size == 1) "" else "s"} available"
    }
}

private fun OrmaProductVariant.publicCatalogComponentSummary(): String? {
    val activeComponents = components.filter { it.status.trim().lowercase() == "active" }
    if (activeComponents.isEmpty()) return null
    val summary = activeComponents.take(2).joinToString(", ") { component ->
        val quantityValue = component.quantity.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0
        val quantityLabel = if (quantityValue % 1.0 == 0.0) quantityValue.toInt().toString() else component.quantity
        buildString {
            append(quantityLabel)
            append(" x ")
            append(component.productName.ifBlank { "Item" })
            component.variantName?.takeIf { it.isNotBlank() }?.let {
                append(" - ")
                append(it)
            }
        }
    }
    return if (activeComponents.size > 2) {
        "$summary +${activeComponents.size - 2}"
    } else {
        summary
    }
}

private fun OrmaPublicCatalogProduct.publicCatalogActiveVariants(): List<OrmaProductVariant> =
    variants.filter { it.status.trim().lowercase() == "active" }

private fun String.publicCatalogNormalizedItemType(): String =
    when (trim().lowercase()) {
        "appointment", "appointments", "booking", "bookings" -> "appointment"
        "service", "services" -> "service"
        else -> "product"
    }

private fun String.publicCatalogFulfillmentOptions(): List<String> =
    when (trim().lowercase()) {
        "appointment" -> listOf("booking")
        "service" -> listOf("standard", "scheduled")
        else -> listOf("take_away", "scheduled")
    }

private fun List<OrmaPublicCatalogPaymentMethod>.publicCatalogDefaultUpiPaymentMethod(): OrmaPublicCatalogPaymentMethod? =
    firstOrNull { it.publicCatalogUsableUpiPaymentMethod() && it.isDefault }
        ?: firstOrNull { it.publicCatalogUsableUpiPaymentMethod() }

private fun OrmaPublicCatalogPaymentMethod.publicCatalogUsableUpiPaymentMethod(): Boolean =
    type.trim().lowercase() == "upi" &&
        upiId.orEmpty().trim().contains("@")

private fun OrmaPublicCatalogPaymentMethod.publicCatalogUpiPaymentValue(
    amount: String,
    currency: String,
    note: String,
): String? {
    val upi = upiId?.trim()?.lowercase()?.takeIf { it.contains("@") } ?: return null
    val paymentAmount = amount.toDoubleOrNull()?.takeIf { it > 0.0 }?.let(::money) ?: return null
    val cleanCurrency = currency.trim().uppercase().ifBlank { "INR" }
    val payee = (payeeName ?: label).trim().ifBlank { "ORMA" }
    val fullValue = buildString {
        append("upi://pay?pa=")
        append(upi.publicCatalogUrlQueryEscaped())
        append("&pn=")
        append(payee.publicCatalogUrlQueryEscaped())
        append("&am=")
        append(paymentAmount.publicCatalogUrlQueryEscaped())
        append("&cu=")
        append(cleanCurrency.publicCatalogUrlQueryEscaped())
        append("&tn=")
        append(note.trim().ifBlank { "ORMA" }.take(24).publicCatalogUrlQueryEscaped())
    }
    if (fullValue.publicCatalogQrSafe()) return fullValue
    val compactValue = buildString {
        append("upi://pay?pa=")
        append(upi.publicCatalogUrlQueryEscaped())
        append("&am=")
        append(paymentAmount.publicCatalogUrlQueryEscaped())
        append("&cu=")
        append(cleanCurrency.publicCatalogUrlQueryEscaped())
    }
    return compactValue.takeIf { it.publicCatalogQrSafe() }
}

private fun String.publicCatalogItemTypeLabel(): String =
    when (publicCatalogNormalizedItemType()) {
        "service" -> "Service"
        "appointment" -> "Appointment"
        else -> "Product"
    }

private fun String.publicCatalogCatalogSectionTitle(): String =
    when (trim().lowercase()) {
        "all" -> "Catalog"
        else -> when (publicCatalogNormalizedItemType()) {
            "service" -> "Services"
            "appointment" -> "Appointments"
            "product" -> "Products"
            else -> "Catalog"
        }
    }

private fun OrmaPublicCatalogProduct.publicCatalogMaxSelectableQuantity(variant: OrmaProductVariant? = null): Int =
    when {
        !inStock -> 0
        variant != null && variant.status.trim().lowercase() != "active" -> 0
        itemType.publicCatalogNormalizedItemType() == "product" && trackStock -> (variant?.stockQuantity ?: stockQuantity).toDoubleOrNull()
            ?.toInt()
            ?.coerceIn(0, 99)
            ?: 0
        else -> 99
    }

private fun OrmaPublicCatalogProduct.publicCatalogAvailabilityLabel(
    variant: OrmaProductVariant? = null,
): String =
    when (itemType.publicCatalogNormalizedItemType()) {
        "appointment" -> variant?.publicCatalogPackageDetail(itemType, unit)
            ?: (variant?.durationMinutes ?: durationMinutes)?.let { "$it min appointment" }
            ?: "appointment booking"
        "service" -> variant?.publicCatalogPackageDetail(itemType, unit)
            ?: (variant?.durationMinutes ?: durationMinutes)?.let { "$it min service" }
            ?: "service"
        else -> variant?.publicCatalogPackageDetail(itemType, unit) ?: if (trackStock) {
            "${(variant?.stockQuantity ?: stockQuantity).publicCatalogQuantityLabel()} ${unit.ifBlank { "unit" }} left"
        } else {
            "per ${unit.ifBlank { "unit" }}"
        }
    }

private fun OrmaPublicCatalogProduct.publicCatalogOfferSavings(): Double =
    offer?.discountAmount?.toDoubleOrNull().orZero()

private fun OrmaPublicCatalogProduct.publicCatalogOfferShortLabel(): String {
    val offer = offer ?: return "Offer"
    val discountValue = offer.discountValue.toDoubleOrNull()
    val valueLabel = discountValue?.let {
        if (it % 1.0 == 0.0) it.toInt().toString() else money(it)
    } ?: offer.discountValue
    return when (offer.discountType.trim().lowercase()) {
        "fixed" -> "${currency.ifBlank { "" }} $valueLabel off".trim()
        else -> "$valueLabel% off"
    }
}

private fun String.publicCatalogQuantityLabel(): String {
    val value = toDoubleOrNull() ?: return "0"
    return if (value % 1.0 == 0.0) value.toInt().toString() else money(value)
}

private fun publicCatalogInitials(value: String): String {
    val initials = value.trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .take(2)
        .joinToString("") { it.first().uppercase() }
    return initials.ifBlank { "O" }
}

private fun String?.publicCatalogRemoteImageUrl(): String? {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return value.takeIf { it.startsWith("https://") || it.startsWith("http://") }
}

private fun OrmaPublicCatalog.publicCatalogWhatsAppUrl(
    selectedItems: List<PublicCatalogSelection>,
): String? {
    val phone = workspace.whatsappDisplayNumber.toPublicCatalogWhatsAppPhone() ?: return null
    val message = publicCatalogWhatsAppMessage(selectedItems)
    return "https://wa.me/$phone?text=${message.publicCatalogUrlQueryEscaped()}"
}

private fun OrmaPublicCatalog.publicCatalogWhatsAppMessage(
    selectedItems: List<PublicCatalogSelection>,
): String = buildString {
    val name = workspace.businessName.ifBlank { "ORMA" }
    append("Hi $name, I want to order from your ORMA online store.")
    if (selectedItems.isNotEmpty()) {
        append("\n\nItems:")
        selectedItems.forEach { item ->
            val itemName = listOf(item.product.name, item.variant?.name)
                .filterNotNull()
                .filter { it.isNotBlank() }
                .joinToString(" - ")
                .ifBlank { "Item" }
            append("\n- $itemName x${item.quantity}")
        }
        append("\n\nEstimated total: ${workspace.currency} ${money(selectedItems.sumOf { it.lineTotal })}")
    }
}

private fun String?.toPublicCatalogWhatsAppPhone(): String? {
    val digits = orEmpty().filter { it.isDigit() }
    return digits.takeIf { it.length >= 7 }
}

private fun String.publicCatalogUrlQueryEscaped(): String =
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

private val OrmaPublicCatalogProduct.customerPrice: String
    get() = offer?.finalPrice ?: sellingPrice

private fun OrmaPublicCatalogProduct.customerPriceFor(variant: OrmaProductVariant?): String {
    val basePrice = variant?.sellingPrice?.takeIf { it.isNotBlank() } ?: sellingPrice
    val discount = offer?.discountFor(basePrice.toDoubleOrNull().orZero()).orZero()
    return (basePrice.toDoubleOrNull().orZero() - discount)
        .coerceAtLeast(0.0)
        .let(::money)
}

private fun OrmaPublicCatalogProduct.discountFor(variant: OrmaProductVariant?): Double {
    val basePrice = variant?.sellingPrice?.takeIf { it.isNotBlank() } ?: sellingPrice
    return offer?.discountFor(basePrice.toDoubleOrNull().orZero()).orZero()
}

private fun org.orma.project_90.backend.OrmaPublicCatalogOffer.discountFor(price: Double): Double {
    val value = discountValue.toDoubleOrNull().orZero().coerceAtLeast(0.0)
    val raw = if (discountType.trim().lowercase() == "fixed") {
        value
    } else {
        price * value.coerceAtMost(100.0) / 100.0
    }
    val cap = discountCapAmount?.toDoubleOrNull()?.takeIf { it > 0.0 }
    return (cap?.let { raw.coerceAtMost(it) } ?: raw)
        .coerceIn(0.0, price)
}

private fun OrmaBackendResult.Failure.publicCatalogMessage(load: Boolean): String =
    when (code) {
        "public_catalog_not_found" -> "This ordering link is not active. Ask the business for the latest QR code."
        "public_items_unavailable" -> "One or more selected items are no longer available. Refresh and choose again."
        "public_customer_name_required" -> "Enter your name to continue."
        "public_customer_phone_required" -> "Enter your phone number to continue."
        "public_items_required" -> "Select at least one item before continuing."
        "public_appointment_time_required" -> "Choose a preferred date and time before booking."
        else -> if (load) {
            "Could not load this ordering page. Check the link and try again."
        } else {
            "Could not send this request. Please try again."
        }
    }

private fun money(amount: Double): String {
    val cents = (amount * 100.0).roundToInt()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

internal expect fun openPublicCatalogPaymentLink(url: String)
