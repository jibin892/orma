package com.orma.backend.meta

import com.orma.backend.config.AppConfig
import java.math.BigDecimal
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class MetaGraphClient(
    private val config: AppConfig,
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(12))
        .build(),
) {
    fun authorizationUrl(state: String): String? {
        if (!config.metaOAuthConfigured) return null
        val scopes = config.metaOAuthScopes.joinToString(",")
        val query = formEncode(
            mapOf(
                "client_id" to config.metaAppId.orEmpty(),
                "redirect_uri" to config.metaRedirectUri.orEmpty(),
                "state" to state,
                "scope" to scopes,
                "response_type" to "code",
            ),
        )
        return "https://www.facebook.com/${config.metaGraphApiVersion}/dialog/oauth?$query"
    }

    fun exchangeCode(code: String): MetaAccessToken {
        val query = formEncode(
            mapOf(
                "client_id" to config.metaAppId.orEmpty(),
                "redirect_uri" to config.metaRedirectUri.orEmpty(),
                "client_secret" to config.metaAppSecret.orEmpty(),
                "code" to code,
            ),
        )
        val body = graphGet("oauth/access_token?$query")
        val root = body.parseJsonObject()
        val token = root["access_token"]?.jsonPrimitive?.contentOrNull
            ?: throw MetaGraphException("Meta did not return an access token.")
        return MetaAccessToken(
            accessToken = token,
            expiresInSeconds = root["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(),
        )
    }

    fun upsertCatalogProduct(
        accessToken: String,
        catalogId: String,
        product: MetaGraphCatalogProductRequest,
    ): MetaGraphProductResult {
        val body = formEncode(
            mapOf(
                "access_token" to accessToken,
                "retailer_id" to product.retailerId,
                "name" to product.name,
                "description" to product.description,
                "availability" to product.availability,
                "condition" to "new",
                "price" to "${product.price.plainMoney()} ${product.currency}",
                "currency" to product.currency,
                "image_url" to product.imageUrl,
                "url" to product.productUrl,
            ),
        )
        val response = graphPostForm("$catalogId/products", body).parseJsonObject()
        return MetaGraphProductResult(
            metaProductId = response["id"]?.jsonPrimitive?.contentOrNull,
        )
    }

    fun sendWhatsAppTemplate(
        accessToken: String,
        phoneNumberId: String,
        recipientPhoneNumber: String,
        templateName: String,
        languageCode: String,
        parameters: List<String>,
    ): MetaGraphMessageResult {
        val payload = buildJsonObject {
            put("messaging_product", "whatsapp")
            put("to", recipientPhoneNumber.onlyDialableCharacters())
            put("type", "template")
            put(
                "template",
                buildJsonObject {
                    put("name", templateName)
                    put("language", buildJsonObject { put("code", languageCode) })
                    if (parameters.isNotEmpty()) {
                        put(
                            "components",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("type", "body")
                                        put(
                                            "parameters",
                                            buildJsonArray {
                                                parameters.forEach { value ->
                                                    add(
                                                        buildJsonObject {
                                                            put("type", "text")
                                                            put("text", value)
                                                        },
                                                    )
                                                }
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    }
                },
            )
        }
        val response = graphPostJson("$phoneNumberId/messages", accessToken, payload).parseJsonObject()
        val messageId = response["messages"]
            ?.jsonArrayOrNull()
            ?.firstOrNull()
            ?.jsonObjectOrNull()
            ?.get("id")
            ?.jsonPrimitive
            ?.contentOrNull
        return MetaGraphMessageResult(messageId = messageId)
    }

    private fun graphGet(pathWithQuery: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${graphBaseUrl()}/$pathWithQuery"))
            .timeout(Duration.ofSeconds(18))
            .GET()
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).successfulBody()
    }

    private fun graphPostForm(path: String, body: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${graphBaseUrl()}/$path"))
            .timeout(Duration.ofSeconds(25))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).successfulBody()
    }

    private fun graphPostJson(path: String, accessToken: String, payload: JsonObject): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${graphBaseUrl()}/$path"))
            .timeout(Duration.ofSeconds(25))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).successfulBody()
    }

    private fun graphBaseUrl(): String =
        "https://graph.facebook.com/${config.metaGraphApiVersion}"
}

data class MetaAccessToken(
    val accessToken: String,
    val expiresInSeconds: Long?,
)

data class MetaGraphCatalogProductRequest(
    val retailerId: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val currency: String,
    val imageUrl: String,
    val productUrl: String,
    val availability: String,
)

data class MetaGraphProductResult(
    val metaProductId: String?,
)

data class MetaGraphMessageResult(
    val messageId: String?,
)

class MetaGraphException(message: String) : RuntimeException(message)

private val JsonParser = Json { ignoreUnknownKeys = true }

private fun HttpResponse<String>.successfulBody(): String {
    if (statusCode() in 200..299) return body()
    val providerMessage = runCatching {
        body()
            .parseJsonObject()["error"]
            ?.jsonObjectOrNull()
            ?.get("message")
            ?.jsonPrimitive
            ?.contentOrNull
    }.getOrNull()
    throw MetaGraphException(providerMessage ?: "Meta request failed with HTTP ${statusCode()}.")
}

private fun String.parseJsonObject(): JsonObject =
    JsonParser.parseToJsonElement(this).jsonObject

private fun formEncode(values: Map<String, String>): String =
    values
        .filterValues { it.isNotBlank() }
        .entries
        .joinToString("&") { (key, value) ->
            "${key.urlEncoded()}=${value.urlEncoded()}"
        }

private fun String.urlEncoded(): String =
    URLEncoder.encode(this, Charsets.UTF_8).replace("+", "%20")

private fun BigDecimal.plainMoney(): String =
    setScale(2).stripTrailingZeros().toPlainString()

private fun String.onlyDialableCharacters(): String =
    filter { it.isDigit() || it == '+' }

private fun JsonElement?.jsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonElement?.jsonArrayOrNull(): JsonArray? = this as? JsonArray
