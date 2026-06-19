package com.orma.backend.gstin

import com.orma.backend.config.AppConfig
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GstinProviderLookup(
    val flag: Boolean,
    val message: String,
    val data: JsonElement?,
    val rawResponse: JsonObject,
)

class GstinCheckProviderException(
    val statusCode: Int,
    message: String,
) : RuntimeException(message)

class GstinCheckClient(
    private val config: AppConfig,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
) {
    suspend fun lookup(gstin: String): GstinProviderLookup = withContext(Dispatchers.IO) {
        val apiKey = config.gstinCheckApiKey
            ?: throw GstinCheckProviderException(0, "GSTINCheck API key is not configured.")
        val uri = URI.create("${config.gstinCheckBaseUrl.trimEnd('/')}/$apiKey/$gstin")
        val request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val body = response.body().orEmpty()

        if (response.statusCode() !in 200..299) {
            throw GstinCheckProviderException(
                statusCode = response.statusCode(),
                message = "GSTINCheck returned HTTP ${response.statusCode()}: ${body.take(MaxProviderErrorBody)}",
            )
        }

        val rawResponse = runCatching { json.parseToJsonElement(body).jsonObject }
            .getOrElse {
                throw GstinCheckProviderException(
                    statusCode = response.statusCode(),
                    message = "GSTINCheck returned invalid JSON.",
                )
            }

        GstinProviderLookup(
            flag = rawResponse["flag"]?.jsonPrimitive?.booleanOrNull ?: false,
            message = rawResponse["message"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            data = rawResponse["data"],
            rawResponse = rawResponse,
        )
    }

    private companion object {
        const val MaxProviderErrorBody = 500
    }
}
