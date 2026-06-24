package org.orma.project_90.publiccatalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlinx.coroutines.launch
import org.orma.project_90.backend.OrmaBackendResult
import org.orma.project_90.backend.OrmaOrder
import org.orma.project_90.backend.OrmaPublicCatalog
import org.orma.project_90.backend.OrmaPublicCatalogOrderDraft
import org.orma.project_90.backend.OrmaPublicCatalogOrderItemDraft
import org.orma.project_90.backend.OrmaPublicCatalogOrderReceipt
import org.orma.project_90.backend.OrmaPublicCatalogProduct
import org.orma.project_90.backend.createOrmaBackendClient
import org.orma.project_90.designsystem.OrmaAdaptiveSurface
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaBrandMark
import org.orma.project_90.designsystem.OrmaCalendarDateTimeField
import org.orma.project_90.designsystem.OrmaColors
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

    fun updateQuantity(productId: String, quantity: Int) {
        val products = catalog?.products.orEmpty()
        val product = products.firstOrNull { it.id == productId } ?: return
        val activeType = products.firstOrNull { item ->
            item.id != productId && (quantities[item.id] ?: 0) > 0
        }?.itemType?.publicCatalogNormalizedItemType()
        val productType = product.itemType.publicCatalogNormalizedItemType()
        if (quantity > 0 && activeType != null && activeType != productType && (quantities[productId] ?: 0) == 0) {
            error = "Clear the ${activeType.publicCatalogItemTypeLabel().lowercase()} selection before choosing ${productType.publicCatalogItemTypeLabel().lowercase()} items."
            return
        }
        val maxQuantity = product.publicCatalogMaxSelectableQuantity()
        quantities = quantities.toMutableMap().apply {
            if (quantity <= 0 || maxQuantity <= 0) remove(productId) else put(productId, quantity.coerceIn(1, maxQuantity))
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
                is OrmaBackendResult.Success -> receipt = result.value
                is OrmaBackendResult.Failure -> error = result.publicCatalogMessage(load = true)
            }
        }
        loading = false
        statusRefreshing = false
    }

    LaunchedEffect(workspaceId) {
        loadCatalogAndReceiptStatus(refreshReceipt = false)
    }

    OrmaAdaptiveSurface(modifier = modifier) {
        val visibleProducts = catalog?.products
            .orEmpty()
            .filter { selectedCategoryId == "all" || it.categoryId == selectedCategoryId }
        val selectedItems = catalog?.products
            .orEmpty()
            .mapNotNull { product ->
                val quantity = quantities[product.id] ?: 0
                if (quantity > 0) PublicCatalogSelection(product = product, quantity = quantity) else null
            }
        val total = selectedItems.sumOf { selection ->
            selection.product.customerPrice.toDoubleOrNull().orZero() * selection.quantity
        }
        val selectedItemTypes = selectedItems
            .map { it.product.itemType.publicCatalogNormalizedItemType() }
            .distinct()
        val selectedCartItemType = selectedItemTypes.singleOrNull()
        val mixedSelection = selectedItemTypes.size > 1
        val selectedFlow = selectedItems.catalogOrderFlow()
        val appointmentRequired = selectedFlow == "appointment"
        val fulfillmentOptions = selectedFlow.publicCatalogFulfillmentOptions()
        val effectiveFulfillmentType = when {
            appointmentRequired -> "booking"
            fulfillmentType in fulfillmentOptions -> fulfillmentType
            else -> fulfillmentOptions.first()
        }
        val scheduledReady = (!appointmentRequired && effectiveFulfillmentType != "scheduled") || scheduledAt.trim().length >= 4
        val submitEnabled = !submitting &&
            selectedItems.isNotEmpty() &&
            !mixedSelection &&
            customerName.trim().length >= 2 &&
            isOrmaInternationalPhoneValid(phoneNumber) &&
            scheduledReady

        fun submit() {
            if (!submitEnabled) {
                error = when {
                    selectedItems.isEmpty() -> "Select at least one item before continuing."
                    mixedSelection -> "Keep one checkout type at a time. Clear the cart before mixing products, services, and appointments."
                    customerName.trim().length < 2 -> "Enter your name to submit this request."
                    (appointmentRequired || effectiveFulfillmentType == "scheduled") && scheduledAt.trim().length < 4 -> "Choose the preferred date or time for this booking."
                    else -> "Enter a valid phone number with country code so the business can contact you."
                }
                return
            }
            scope.launch {
                submitting = true
                error = null
                val draft = OrmaPublicCatalogOrderDraft(
                    customerName = customerName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    notes = notes.trim(),
                    fulfillmentType = effectiveFulfillmentType,
                    scheduledAt = if (appointmentRequired || effectiveFulfillmentType == "scheduled") scheduledAt.trim() else "",
                    paymentMode = paymentMode,
                    items = selectedItems.map {
                        OrmaPublicCatalogOrderItemDraft(
                            productId = it.product.id,
                            quantity = it.quantity.toString(),
                        )
                    },
                )
                when (val result = client.submitPublicCatalogOrder(workspaceId, draft)) {
                    is OrmaBackendResult.Success -> receipt = result.value
                    is OrmaBackendResult.Failure -> error = result.publicCatalogMessage(load = false)
                }
                submitting = false
            }
        }

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
                selectedItems = selectedItems,
                selectedFlow = selectedFlow,
                total = total,
                submitting = submitting,
                statusRefreshing = statusRefreshing,
                submitEnabled = submitEnabled,
                onRetry = {
                    scope.launch {
                        loadCatalogAndReceiptStatus(refreshReceipt = true)
                    }
                },
                onQuantityChange = ::updateQuantity,
                onClearSelection = {
                    quantities = emptyMap()
                    error = null
                },
                onNewOrder = {
                    receipt = null
                    quantities = emptyMap()
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
                selectedItems = selectedItems,
                selectedFlow = selectedFlow,
                total = total,
                submitting = submitting,
                statusRefreshing = statusRefreshing,
                submitEnabled = submitEnabled,
                onRetry = {
                    scope.launch {
                        loadCatalogAndReceiptStatus(refreshReceipt = true)
                    }
                },
                onQuantityChange = ::updateQuantity,
                onClearSelection = {
                    quantities = emptyMap()
                    error = null
                },
                onNewOrder = {
                    receipt = null
                    quantities = emptyMap()
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
    onSubmit: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val mobileScope = rememberCoroutineScope()
    val showFloatingCart = selectedItems.isNotEmpty() && receipt == null

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
                selectedItems = selectedItems,
                visibleProducts = visibleProducts,
                total = total,
                compact = true,
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
                )
            }
            PublicCatalogCheckoutCard {
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
                    selectedItems = selectedItems,
                    selectedFlow = selectedFlow,
                    total = total,
                    submitting = submitting,
                    statusRefreshing = statusRefreshing,
                    submitEnabled = submitEnabled,
                    onRetry = onRetry,
                    onClearSelection = onClearSelection,
                    onNewOrder = onNewOrder,
                    onCustomerNameChange = onCustomerNameChange,
                    onPhoneChange = onPhoneChange,
                    onNotesChange = onNotesChange,
                    onFulfillmentChange = onFulfillmentChange,
                    onScheduledAtChange = onScheduledAtChange,
                    onPaymentModeChange = onPaymentModeChange,
                    onSubmit = onSubmit,
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
                    mobileScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
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
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(28.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1180.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PublicCatalogCheckoutTopBar(
                catalog = catalog,
                loading = loading,
                selectedItems = selectedItems,
                visibleProducts = visibleProducts,
                total = total,
                compact = false,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                PublicCatalogProductsCard(modifier = Modifier.weight(1.38f)) {
                    PublicCatalogProducts(
                        catalog = catalog,
                        loading = loading,
                        quantities = quantities,
                        selectedCategoryId = selectedCategoryId,
                        selectedCartItemType = selectedCartItemType,
                        visibleProducts = visibleProducts,
                        onQuantityChange = onQuantityChange,
                        onCategoryChange = onCategoryChange,
                    )
                }
                PublicCatalogCheckoutCard(modifier = Modifier.weight(0.82f)) {
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
                        selectedItems = selectedItems,
                        selectedFlow = selectedFlow,
                        total = total,
                        submitting = submitting,
                        statusRefreshing = statusRefreshing,
                        submitEnabled = submitEnabled,
                        onRetry = onRetry,
                        onClearSelection = onClearSelection,
                        onNewOrder = onNewOrder,
                        onCustomerNameChange = onCustomerNameChange,
                        onPhoneChange = onPhoneChange,
                        onNotesChange = onNotesChange,
                        onFulfillmentChange = onFulfillmentChange,
                        onScheduledAtChange = onScheduledAtChange,
                        onPaymentModeChange = onPaymentModeChange,
                        onSubmit = onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogCheckoutCard(
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
    selectedItems: List<PublicCatalogSelection>,
    visibleProducts: List<OrmaPublicCatalogProduct>,
    total: Double,
    compact: Boolean,
) {
    val businessName = catalog?.workspace?.businessName?.ifBlank { "ORMA checkout" } ?: "ORMA checkout"
    val location = catalog?.workspace?.let {
        listOf(it.industry, it.city).filter(String::isNotBlank).joinToString(" / ")
    }.orEmpty().ifBlank { "Online checkout" }
    val logoUrl = catalog?.workspace?.logoUrl.publicCatalogRemoteImageUrl()
    val itemCount = catalog?.products?.size ?: 0
    val selectedCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    val availableTypes = visibleProducts
        .map { it.itemType.publicCatalogNormalizedItemType() }
        .distinct()
    val typeSummary = when {
        loading -> "Loading"
        availableTypes.isEmpty() -> "No items"
        availableTypes.size == 1 -> availableTypes.first().publicCatalogItemTypeLabel()
        else -> "${availableTypes.size} checkout types"
    }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PublicCatalogCheckoutBusinessLine(
                        businessName = businessName,
                        location = location,
                        logoUrl = logoUrl,
                        loading = loading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PublicCatalogCheckoutTopMetric(
                            label = "Catalog",
                            value = if (loading) "--" else itemCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        PublicCatalogCheckoutTopMetric(
                            label = "Selected",
                            value = selectedCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    PublicCatalogCheckoutTopMetric(
                        label = typeSummary,
                        value = if (selectedCount == 0) "Choose items" else "${currency.ifBlank { "" }} ${money(total)}".trim(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(18.dp),
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
                    Row(
                        modifier = Modifier.weight(1.08f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PublicCatalogCheckoutTopMetric(
                            label = "Catalog",
                            value = if (loading) "--" else itemCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        PublicCatalogCheckoutTopMetric(
                            label = "Selected",
                            value = selectedCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        PublicCatalogCheckoutTopMetric(
                            label = typeSummary,
                            value = if (selectedCount == 0) "--" else "${currency.ifBlank { "" }} ${money(total)}".trim(),
                            modifier = Modifier.weight(1.25f),
                        )
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
private fun PublicCatalogCheckoutTopMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        border = BorderStroke(0.8.dp, OrmaColors.Hairline),
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
                color = OrmaColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
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
) {
    var searchQuery by remember(catalog?.workspace?.id) { mutableStateOf("") }
    var selectedTypeFilter by remember(catalog?.workspace?.id) { mutableStateOf("all") }
    val cleanSearchQuery = searchQuery.trim()
    val availableTypeFilters = catalog?.products
        .orEmpty()
        .map { it.itemType.publicCatalogNormalizedItemType() }
        .distinct()
    val searchedProducts = visibleProducts.filter { product ->
        val productType = product.itemType.publicCatalogNormalizedItemType()
        (selectedTypeFilter == "all" || selectedTypeFilter == productType) &&
            (cleanSearchQuery.isBlank() ||
            product.name.contains(cleanSearchQuery, ignoreCase = true) ||
            product.description.orEmpty().contains(cleanSearchQuery, ignoreCase = true) ||
            product.categoryName.orEmpty().contains(cleanSearchQuery, ignoreCase = true) ||
            productType.publicCatalogItemTypeLabel().contains(cleanSearchQuery, ignoreCase = true))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicCatalogProductsHeader(
            count = searchedProducts.size,
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
                    selectedType = selectedTypeFilter,
                    activeCartType = selectedCartItemType,
                    onTypeChange = { selectedTypeFilter = it },
                )
                PublicCatalogCategoryFilter(
                    catalog = catalog,
                    selectedCategoryId = selectedCategoryId,
                    onCategoryChange = onCategoryChange,
                )
                if (searchedProducts.isEmpty()) {
                    PublicCatalogEmpty(
                        title = "No matches",
                        body = "Try another search or category.",
                    )
                } else {
                    PublicCatalogProductGrid(
                        products = searchedProducts,
                        quantities = quantities,
                        activeCartType = selectedCartItemType,
                        onQuantityChange = onQuantityChange,
                    )
                }
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
            val locked = activeCartType != null && type != "all" && type != activeCartType
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
                    text = if (type == "all") "All" else type.publicCatalogItemTypeLabel(),
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
private fun PublicCatalogProductGrid(
    products: List<OrmaPublicCatalogProduct>,
    quantities: Map<String, Int>,
    activeCartType: String?,
    onQuantityChange: (String, Int) -> Unit,
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
            products.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    rowItems.forEach { product ->
                        PublicCatalogProductTile(
                            product = product,
                            quantity = quantities[product.id] ?: 0,
                            activeCartType = activeCartType,
                            onQuantityChange = { onQuantityChange(product.id, it) },
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
    selectedItems: List<PublicCatalogSelection>,
    selectedFlow: String,
    total: Double,
    submitting: Boolean,
    statusRefreshing: Boolean,
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
        else -> "Checkout"
    }
    val checkoutPrompt = when {
        appointmentFlow -> "Reserve a slot, share your phone number, and the business will confirm the booking."
        serviceFlow -> "Share the service details and timing so the business can confirm availability."
        else -> "Review your items, choose timing, and send the order directly to the business."
    }
    val itemCount = selectedItems.sumOf { it.quantity }
    val currency = catalog?.workspace?.currency ?: selectedItems.firstOrNull()?.product?.currency.orEmpty()
    val estimatedTotal = if (itemCount == 0) "--" else "${currency.ifBlank { "" }} ${money(total)}".trim()
    val hasUpi = catalog?.paymentMethods?.any { it.type == "upi" && !it.upiId.isNullOrBlank() } == true
    val hasWhatsApp = catalog?.workspace?.whatsappDisplayNumber.toPublicCatalogWhatsAppPhone() != null
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (receipt != null) {
            PublicCatalogSuccess(
                receipt = receipt,
                refreshing = statusRefreshing,
                onRefresh = onRetry,
                onNewOrder = onNewOrder,
            )
        } else {
            PublicCatalogCheckoutHeader(
                title = checkoutTitle,
                body = checkoutPrompt,
                itemCount = itemCount,
                total = estimatedTotal,
                ready = submitEnabled,
                submitting = submitting,
            )
            PublicCatalogWorkflowRail(
                selectedItems = selectedItems,
                contactReady = customerName.trim().length >= 2 && isOrmaInternationalPhoneValid(phoneNumber),
                timingReady = (!appointmentFlow && selectedFulfillment != "scheduled") || scheduledAt.trim().length >= 4,
                submitting = submitting,
                receipt = receipt,
            )
            PublicCatalogSelectionSummary(
                catalog = catalog,
                selectedItems = selectedItems,
                total = total,
                onClearSelection = onClearSelection,
            )
            PublicCatalogTrustStrip(
                hasUpi = hasUpi,
                hasWhatsApp = hasWhatsApp,
                selectedFulfillment = selectedFulfillment,
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
            OrmaSectionHeader(text = "Timing and payment")
            if (!appointmentFlow) {
                OrmaSegmentedRow(
                    options = fulfillmentOptions,
                    selected = selectedFulfillment,
                    label = {
                        when (it) {
                            "scheduled" -> "Schedule"
                            "standard" -> "Request now"
                            else -> "Take away"
                        }
                    },
                    onSelected = onFulfillmentChange,
                )
            }
            if (appointmentFlow || selectedFulfillment == "scheduled") {
                OrmaCalendarDateTimeField(
                    value = scheduledAt,
                    onValueChange = onScheduledAtChange,
                    label = "Preferred date/time",
                    placeholder = "Choose date",
                    supportingText = "Choose a date. Add a common time slot if needed.",
                    allowClear = !appointmentFlow,
                )
            }
            OrmaSegmentedRow(
                options = if (hasUpi) listOf("pay_on_spot", "upi") else listOf("pay_on_spot"),
                selected = if (hasUpi) paymentMode else "pay_on_spot",
                label = { if (it == "upi") "UPI" else "Pay on spot" },
                onSelected = onPaymentModeChange,
            )
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
            if (catalog == null && !error.isNullOrBlank()) {
                OrmaLightButton(
                    text = "Try again",
                    onClick = onRetry,
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

@Composable
private fun PublicCatalogCheckoutHeader(
    title: String,
    body: String,
    itemCount: Int,
    total: String,
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
            if (stacked) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PublicCatalogCheckoutTitle(title = title, body = body, status = status)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PublicCatalogCheckoutMetric(
                            label = "Items",
                            value = itemCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        PublicCatalogCheckoutMetric(
                            label = "Total",
                            value = total,
                            modifier = Modifier.weight(1.2f),
                        )
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
                        PublicCatalogCheckoutMetric(label = "Items", value = itemCount.toString())
                        PublicCatalogCheckoutMetric(label = "Total", value = total)
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
private fun PublicCatalogTrustStrip(
    hasUpi: Boolean,
    hasWhatsApp: Boolean,
    selectedFulfillment: String,
) {
    val timingLabel = when (selectedFulfillment) {
        "booking" -> "Booking request"
        "scheduled" -> "Scheduled timing"
        "standard" -> "Service request"
        else -> "Take-away ready"
    }
    val items = listOf(
        "Direct to ORMA",
        timingLabel,
        if (hasUpi) "UPI available" else "Pay on spot",
        if (hasWhatsApp) "WhatsApp fallback" else "No login needed",
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 420.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowItems.forEach { item ->
                            PublicCatalogTrustItem(text = item, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEach { item ->
                    PublicCatalogTrustItem(text = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogTrustItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.Accent.copy(alpha = 0.055f),
        border = BorderStroke(0.8.dp, OrmaColors.Accent.copy(alpha = 0.08f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PublicCatalogWorkflowRail(
    selectedItems: List<PublicCatalogSelection>,
    contactReady: Boolean,
    timingReady: Boolean,
    submitting: Boolean,
    receipt: OrmaPublicCatalogOrderReceipt?,
) {
    val cartReady = selectedItems.isNotEmpty()
    val sent = receipt != null
    val contactComplete = cartReady && contactReady
    val timingComplete = contactComplete && timingReady
    val activeStep = when {
        sent || submitting -> "4"
        !cartReady -> "1"
        !contactReady -> "2"
        !timingReady -> "3"
        else -> "4"
    }
    val steps = listOf(
        PublicCatalogStep("1", "Choose", cartReady, activeStep == "1"),
        PublicCatalogStep("2", "Contact", contactComplete, activeStep == "2"),
        PublicCatalogStep("3", "Timing", timingComplete, activeStep == "3"),
        PublicCatalogStep("4", if (submitting) "Sending" else "Sent", sent, activeStep == "4"),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 430.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.chunked(2).forEach { rowSteps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowSteps.forEach { step ->
                            PublicCatalogWorkflowStepCard(
                                step = step,
                                submitting = submitting,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowSteps.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                steps.forEach { step ->
                    PublicCatalogWorkflowStepCard(
                        step = step,
                        submitting = submitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicCatalogWorkflowStepCard(
    step: PublicCatalogStep,
    submitting: Boolean,
    modifier: Modifier = Modifier,
) {
    val active = step.complete || step.active || (step.number == "4" && submitting)
    Surface(
        modifier = modifier,
        shape = OrmaShapes.StandardCell,
        color = if (active) OrmaColors.Accent else OrmaColors.CellBackground,
        border = BorderStroke(
            width = 0.8.dp,
            color = if (active) OrmaColors.Accent.copy(alpha = 0.36f) else OrmaColors.Hairline,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = step.number,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) OrmaColors.OnAccent else OrmaColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = step.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) OrmaColors.OnAccent.copy(alpha = 0.82f) else OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class PublicCatalogStep(
    val number: String,
    val label: String,
    val complete: Boolean,
    val active: Boolean,
)

@Composable
private fun PublicCatalogProductTile(
    product: OrmaPublicCatalogProduct,
    quantity: Int,
    activeCartType: String?,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = quantity > 0
    val productType = product.itemType.publicCatalogNormalizedItemType()
    val lockedByType = activeCartType != null && activeCartType != productType && !selected
    val enabled = product.inStock && !lockedByType
    val maxQuantity = product.publicCatalogMaxSelectableQuantity()
    val actionLabel = when {
        !product.inStock -> "Unavailable"
        lockedByType -> "Clear ${activeCartType.publicCatalogItemTypeLabel().lowercase()} cart"
        selected -> "Selected"
        productType == "appointment" -> "Book"
        productType == "service" -> "Request"
        else -> "Add"
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                onQuantityChange(if (quantity == 0) 1 else quantity)
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
                    text = product.name.ifBlank { "Item" },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (lockedByType) OrmaColors.TextSecondary else OrmaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = product.description?.takeIf(String::isNotBlank)
                        ?: product.publicCatalogAvailabilityLabel(),
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
                    OrmaPrice(amount = product.customerPrice, currency = product.currency)
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
                    enabled = enabled || selected,
                    maxQuantity = maxQuantity,
                    onQuantityChange = onQuantityChange,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = OrmaShapes.Capsule,
                color = if (enabled || selected) OrmaColors.Accent.copy(alpha = 0.08f) else OrmaColors.CellBackground,
                contentColor = if (enabled || selected) OrmaColors.Accent else OrmaColors.TextTertiary,
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
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OrmaPrice(amount = product.customerPrice, currency = product.currency)
                            product.offer?.let {
                                OrmaBadge(text = it.name.uppercase(), tone = OrmaStatusTone.Success)
                            }
                            Text(
                                text = product.publicCatalogAvailabilityLabel(),
                                modifier = Modifier.weight(1f),
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OrmaPrice(amount = product.customerPrice, currency = product.currency)
                            product.offer?.let {
                                OrmaBadge(text = it.name.uppercase(), tone = OrmaStatusTone.Success)
                            }
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
                                text = "Business confirms total.",
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
                text = selection.product.name.ifBlank { "Item" },
                style = MaterialTheme.typography.bodyLarge,
                color = OrmaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${selection.quantity} x ${selection.product.currency} ${selection.product.customerPrice}",
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    onRefresh: () -> Unit,
    onNewOrder: () -> Unit,
) {
    val order = receipt.order
    val balanceDue = order.publicCatalogBalanceDueValue()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

private fun String.publicCatalogWorkTitle(): String =
    when (trim().lowercase()) {
        "appointment" -> "Appointment"
        "service" -> "Service request"
        else -> "Order"
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

private data class PublicCatalogSelection(
    val product: OrmaPublicCatalogProduct,
    val quantity: Int,
) {
    val lineTotal: Double = product.customerPrice.toDoubleOrNull().orZero() * quantity
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun List<PublicCatalogSelection>.catalogOrderFlow(): String =
    when {
        any { it.product.itemType.publicCatalogNormalizedItemType() == "appointment" } -> "appointment"
        isNotEmpty() && all { it.product.itemType.publicCatalogNormalizedItemType() == "service" } -> "service"
        else -> "sale"
    }

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

private fun String.publicCatalogItemTypeLabel(): String =
    when (publicCatalogNormalizedItemType()) {
        "service" -> "Service"
        "appointment" -> "Appointment"
        else -> "Product"
    }

private fun OrmaPublicCatalogProduct.publicCatalogMaxSelectableQuantity(): Int =
    when {
        !inStock -> 0
        itemType.publicCatalogNormalizedItemType() == "product" && trackStock -> stockQuantity.toDoubleOrNull()
            ?.toInt()
            ?.coerceIn(0, 99)
            ?: 0
        else -> 99
    }

private fun OrmaPublicCatalogProduct.publicCatalogAvailabilityLabel(): String =
    when (itemType.publicCatalogNormalizedItemType()) {
        "appointment" -> durationMinutes?.let { "$it min appointment" } ?: "appointment booking"
        "service" -> durationMinutes?.let { "$it min service" } ?: "service"
        else -> if (trackStock) {
            "${stockQuantity.publicCatalogQuantityLabel()} ${unit.ifBlank { "unit" }} left"
        } else {
            "per ${unit.ifBlank { "unit" }}"
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
            append("\n- ${item.product.name} x${item.quantity}")
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
