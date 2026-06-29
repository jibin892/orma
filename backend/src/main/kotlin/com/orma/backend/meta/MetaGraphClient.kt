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
import kotlinx.serialization.json.JsonPrimitive
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

    fun createWhatsAppTemplate(
        accessToken: String,
        whatsappBusinessAccountId: String,
        template: MetaGraphWhatsAppTemplateRequest,
    ): MetaGraphTemplateResult {
        val payload = buildJsonObject {
            put("name", template.name)
            put("category", template.category)
            put("language", template.languageCode)
            put("components", template.toComponentsJson())
        }
        val response = graphPostJson("$whatsappBusinessAccountId/message_templates", accessToken, payload).parseJsonObject()
        return MetaGraphTemplateResult(
            id = response["id"]?.jsonPrimitive?.contentOrNull,
            status = response["status"]?.jsonPrimitive?.contentOrNull,
        )
    }

    fun updateWhatsAppTemplate(
        accessToken: String,
        templateId: String,
        template: MetaGraphWhatsAppTemplateRequest,
    ): MetaGraphTemplateResult {
        val payload = buildJsonObject {
            put("category", template.category)
            put("components", template.toComponentsJson())
        }
        val response = graphPostJson(templateId, accessToken, payload).parseJsonObject()
        return MetaGraphTemplateResult(
            id = response["id"]?.jsonPrimitive?.contentOrNull ?: templateId,
            status = response["status"]?.jsonPrimitive?.contentOrNull,
        )
    }

    fun listWhatsAppTemplates(
        accessToken: String,
        whatsappBusinessAccountId: String,
    ): List<MetaGraphWhatsAppTemplate> {
        val response = graphGetJson(
            pathWithQuery = "$whatsappBusinessAccountId/message_templates?fields=id,name,status,category,language,components,rejected_reason&limit=100",
            accessToken = accessToken,
        ).parseJsonObject()
        return response["data"]
            ?.jsonArrayOrNull()
            ?.mapNotNull { it.jsonObjectOrNull()?.toWhatsAppTemplate() }
            .orEmpty()
    }

    private fun graphGet(pathWithQuery: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${graphBaseUrl()}/$pathWithQuery"))
            .timeout(Duration.ofSeconds(18))
            .GET()
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).successfulBody()
    }

    private fun graphGetJson(pathWithQuery: String, accessToken: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${graphBaseUrl()}/$pathWithQuery"))
            .timeout(Duration.ofSeconds(18))
            .header("Authorization", "Bearer $accessToken")
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

private fun MetaGraphWhatsAppTemplateRequest.toComponentsJson(): JsonArray =
    buildJsonArray {
        add(
            buildJsonObject {
                put("type", "BODY")
                put("text", bodyText)
                if (sampleParameters.isNotEmpty()) {
                    put(
                        "example",
                        buildJsonObject {
                            put(
                                "body_text",
                                buildJsonArray {
                                    add(
                                        buildJsonArray {
                                            sampleParameters.forEach { value ->
                                                add(JsonPrimitive(value))
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

data class MetaGraphWhatsAppTemplateRequest(
    val name: String,
    val category: String,
    val languageCode: String,
    val bodyText: String,
    val sampleParameters: List<String>,
)

data class MetaGraphTemplateResult(
    val id: String?,
    val status: String?,
)

data class MetaGraphWhatsAppTemplate(
    val id: String?,
    val name: String,
    val status: String,
    val category: String,
    val languageCode: String,
    val bodyText: String?,
    val rejectedReason: String?,
)

data class MetaGraphMessageResult(
    val messageId: String?,
)

class MetaGraphException(
    message: String,
    val httpStatus: Int? = null,
    val providerCode: String? = null,
    val providerSubcode: String? = null,
    val providerType: String? = null,
    val providerDetails: String? = null,
    val providerTraceId: String? = null,
) : RuntimeException(message)

private data class MetaGraphErrorPayload(
    val message: String?,
    val details: String?,
    val code: String?,
    val subcode: String?,
    val type: String?,
    val traceId: String?,
) {
    val publicMessage: String?
        get() = listOfNotNull(message, details)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(": ")
}

private val JsonParser = Json { ignoreUnknownKeys = true }

private fun HttpResponse<String>.successfulBody(): String {
    if (statusCode() in 200..299) return body()
    val providerError = body().parseMetaGraphError()
    throw MetaGraphException(
        message = providerError?.publicMessage ?: "Meta request failed with HTTP ${statusCode()}.",
        httpStatus = statusCode(),
        providerCode = providerError?.code,
        providerSubcode = providerError?.subcode,
        providerType = providerError?.type,
        providerDetails = providerError?.details,
        providerTraceId = providerError?.traceId,
    )
}

private fun String.parseJsonObject(): JsonObject =
    JsonParser.parseToJsonElement(this).jsonObject

private fun String.parseMetaGraphError(): MetaGraphErrorPayload? =
    runCatching {
        val error = parseJsonObject()["error"]?.jsonObjectOrNull() ?: return@runCatching null
        val errorData = error["error_data"]?.jsonObjectOrNull()
        val details = errorData?.stringValue("details")
            ?: error.stringValue("error_user_msg")
            ?: error.stringValue("error_user_title")
        MetaGraphErrorPayload(
            message = error.stringValue("message"),
            details = details,
            code = error.stringValue("code"),
            subcode = error.stringValue("error_subcode"),
            type = error.stringValue("type"),
            traceId = error.stringValue("fbtrace_id"),
        )
    }.getOrNull()

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

private fun JsonObject.stringValue(key: String): String? =
    (get(key) as? JsonPrimitive)
        ?.contentOrNull
        ?.trim()
        ?.takeIf { it.isNotBlank() }

private fun JsonObject.toWhatsAppTemplate(): MetaGraphWhatsAppTemplate? {
    val name = get("name")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
    val bodyText = get("components")
        ?.jsonArrayOrNull()
        ?.mapNotNull { it.jsonObjectOrNull() }
        ?.firstOrNull { component ->
            component["type"]?.jsonPrimitive?.contentOrNull.equals("BODY", ignoreCase = true)
        }
        ?.get("text")
        ?.jsonPrimitive
        ?.contentOrNull
    return MetaGraphWhatsAppTemplate(
        id = get("id")?.jsonPrimitive?.contentOrNull,
        name = name,
        status = get("status")?.jsonPrimitive?.contentOrNull ?: "unknown",
        category = get("category")?.jsonPrimitive?.contentOrNull ?: "UTILITY",
        languageCode = get("language")?.jsonPrimitive?.contentOrNull ?: "en_US",
        bodyText = bodyText,
        rejectedReason = get("rejected_reason")?.jsonPrimitive?.contentOrNull,
    )
}
