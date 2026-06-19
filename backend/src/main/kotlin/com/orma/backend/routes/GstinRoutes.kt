package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.GstinLookupRecord
import com.orma.backend.db.GstinRepository
import com.orma.backend.gstin.GstinCheckClient
import com.orma.backend.gstin.GstinCheckProviderException
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.GstinLookupResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull

fun Route.gstinRoutes(
    config: AppConfig,
    gstinRepository: GstinRepository?,
    gstinCheckClient: GstinCheckClient = GstinCheckClient(config),
) {
    get("/gstin/{gstin}") {
        val repository = gstinRepository ?: return@get call.respondGstinDatabaseNotConfigured()
        call.verifiedFirebaseUser(config) ?: return@get

        val gstin = call.parameters["gstin"].orEmpty().normalizedGstin()
        if (!gstin.isValidGstin()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "invalid_gstin",
                    message = "GSTIN must be a valid 15-character Indian GST number.",
                ),
            )
            return@get
        }

        val cached = repository.findByGstin(gstin)
        if (cached != null) {
            call.respond(cached.toResponse(source = "cache", cached = true))
            return@get
        }

        if (!config.gstinCheckConfigured) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse(
                    code = "gstin_check_not_configured",
                    message = "GSTINCHECK_API_KEY is required before GSTIN lookup can call the provider.",
                ),
            )
            return@get
        }

        try {
            val providerLookup = gstinCheckClient.lookup(gstin)
            val saved = repository.saveLookup(gstin, providerLookup)
            call.respond(saved.toResponse(source = "provider", cached = false))
        } catch (error: GstinCheckProviderException) {
            call.application.environment.log.warn("GSTINCheck lookup failed for $gstin", error)
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse(
                    code = "gstin_provider_failed",
                    message = "ORMA could not verify this GSTIN right now. Try again later.",
                ),
            )
        }
    }
}

private suspend fun ApplicationCall.respondGstinDatabaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before GSTIN lookup can run.",
        ),
    )
}

private fun GstinLookupRecord.toResponse(
    source: String,
    cached: Boolean,
): GstinLookupResponse =
    GstinLookupResponse(
        gstin = gstin,
        flag = flag,
        message = message,
        data = dataJson?.toJsonElementOrNull(),
        source = source,
        cached = cached,
        cachedAt = updatedAt,
    )

private fun String.toJsonElementOrNull() =
    takeUnless { it == "null" }
        ?.let { GstinRouteJson.parseToJsonElement(it) }
        ?.takeUnless { it is JsonNull }

private fun String.normalizedGstin(): String =
    trim().uppercase().filter(Char::isLetterOrDigit)

private fun String.isValidGstin(): Boolean =
    GstinRegex.matches(this)

private val GstinRegex = Regex("^[0-9]{2}[A-Z0-9]{13}$")

private val GstinRouteJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
