package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.DashboardRepository
import com.orma.backend.models.CustomerListResponse
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.OrderListResponse
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.OrderStatusRequest
import com.orma.backend.models.ProductListResponse
import com.orma.backend.models.ProductRequest
import com.orma.backend.models.StockAdjustmentRequest
import com.orma.backend.models.SupplierListResponse
import com.orma.backend.models.SupplierRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.dashboardRoutes(
    config: AppConfig,
    dashboardRepository: DashboardRepository?,
) {
    get("/dashboard/summary") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        call.respondWorkspaceResult(repository.summary(firebaseUser))
    }

    get("/customers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val customers = repository.customers(firebaseUser) ?: return@get call.workspaceNotFound()
        call.respond(CustomerListResponse(customers))
    }

    post("/customers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<CustomerRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("customer_name_required", "Enter the customer name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createCustomer(firebaseUser, request))
    }

    put("/customers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val customerId = call.parameters["id"].orEmpty()
        val request = call.receive<CustomerRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("customer_name_required", "Enter the customer name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateCustomer(firebaseUser, customerId, request))
    }

    get("/suppliers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val suppliers = repository.suppliers(firebaseUser) ?: return@get call.workspaceNotFound()
        call.respond(SupplierListResponse(suppliers))
    }

    post("/suppliers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<SupplierRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("supplier_name_required", "Enter the supplier name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createSupplier(firebaseUser, request))
    }

    put("/suppliers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val supplierId = call.parameters["id"].orEmpty()
        val request = call.receive<SupplierRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("supplier_name_required", "Enter the supplier name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateSupplier(firebaseUser, supplierId, request))
    }

    get("/products") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val products = repository.products(firebaseUser) ?: return@get call.workspaceNotFound()
        call.respond(ProductListResponse(products))
    }

    post("/products") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_name_required", "Enter the product name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createProduct(firebaseUser, request))
    }

    put("/products/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val productId = call.parameters["id"].orEmpty()
        val request = call.receive<ProductRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_name_required", "Enter the product name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateProduct(firebaseUser, productId, request))
    }

    post("/products/{id}/stock-adjustments") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val productId = call.parameters["id"].orEmpty()
        val request = call.receive<StockAdjustmentRequest>()
        if (request.quantityDelta.isBlank()) {
            call.respondValidation("stock_quantity_required", "Enter the stock adjustment quantity.")
            return@post
        }
        call.respondWorkspaceResult(repository.adjustStock(firebaseUser, productId, request))
    }

    get("/orders") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val orders = repository.orders(firebaseUser) ?: return@get call.workspaceNotFound()
        call.respond(OrderListResponse(orders))
    }

    post("/orders") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<OrderRequest>()
        if (request.items.isEmpty()) {
            call.respondValidation("order_items_required", "Add at least one item before creating the order.")
            return@post
        }
        call.respondWorkspaceResult(repository.createOrder(firebaseUser, request))
    }

    get("/orders/{id}") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val orderId = call.parameters["id"].orEmpty()
        call.respondWorkspaceResult(repository.order(firebaseUser, orderId))
    }

    put("/orders/{id}/status") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val orderId = call.parameters["id"].orEmpty()
        val request = call.receive<OrderStatusRequest>()
        if (request.status.isBlank()) {
            call.respondValidation("order_status_required", "Choose an order status.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status))
    }

    post("/orders/{id}/status") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val orderId = call.parameters["id"].orEmpty()
        val request = call.receive<OrderStatusRequest>()
        if (request.status.isBlank()) {
            call.respondValidation("order_status_required", "Choose an order status.")
            return@post
        }
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status))
    }
}

private suspend fun ApplicationCall.respondWorkspaceResult(value: Any?) {
    if (value == null) {
        workspaceNotFound()
    } else {
        respond(value)
    }
}

private suspend fun ApplicationCall.dashboardDatabaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before dashboard APIs can run.",
        ),
    )
}

private suspend fun ApplicationCall.workspaceNotFound() {
    respond(
        HttpStatusCode.NotFound,
        ErrorResponse(
            code = "workspace_not_found",
            message = "Complete business setup before using this workspace module.",
        ),
    )
}

private suspend fun ApplicationCall.respondValidation(code: String, message: String) {
    respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(code = code, message = message),
    )
}
