package com.orma.backend.db

import com.orma.backend.gstin.GstinProviderLookup
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GstinLookupRecord(
    val gstin: String,
    val flag: Boolean,
    val message: String,
    val dataJson: String?,
    val rawResponseJson: String,
    val updatedAt: String,
)

class GstinRepository(
    private val dataSource: DataSource,
) {
    suspend fun findByGstin(gstin: String): GstinLookupRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.findGstinLookup(gstin)
        }
    }

    suspend fun saveLookup(
        gstin: String,
        providerLookup: GstinProviderLookup,
    ): GstinLookupRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.upsertGstinLookup(gstin, providerLookup)
        }
    }

    private fun Connection.findGstinLookup(gstin: String): GstinLookupRecord? {
        val sql = """
            select
                gstin,
                flag,
                message,
                response_data::text as data_json,
                raw_response::text as raw_response_json,
                updated_at
            from gstin_lookups
            where gstin = ?
            limit 1
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, gstin)
            statement.executeQuery().use { result ->
                if (result.next()) result.toGstinLookupRecord() else null
            }
        }
    }

    private fun Connection.upsertGstinLookup(
        gstin: String,
        providerLookup: GstinProviderLookup,
    ): GstinLookupRecord {
        val dataObject = providerLookup.data as? JsonObject
        val sql = """
            insert into gstin_lookups (
                gstin,
                flag,
                message,
                legal_name,
                trade_name,
                taxpayer_status,
                taxpayer_type,
                constitution,
                registration_date,
                cancellation_date,
                principal_address,
                state_jurisdiction,
                central_jurisdiction,
                response_data,
                raw_response,
                updated_at
            )
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, now())
            on conflict (gstin) do update set
                flag = excluded.flag,
                message = excluded.message,
                legal_name = excluded.legal_name,
                trade_name = excluded.trade_name,
                taxpayer_status = excluded.taxpayer_status,
                taxpayer_type = excluded.taxpayer_type,
                constitution = excluded.constitution,
                registration_date = excluded.registration_date,
                cancellation_date = excluded.cancellation_date,
                principal_address = excluded.principal_address,
                state_jurisdiction = excluded.state_jurisdiction,
                central_jurisdiction = excluded.central_jurisdiction,
                response_data = excluded.response_data,
                raw_response = excluded.raw_response,
                updated_at = now()
            returning
                gstin,
                flag,
                message,
                response_data::text as data_json,
                raw_response::text as raw_response_json,
                updated_at
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, gstin)
            statement.setBoolean(2, providerLookup.flag)
            statement.setString(3, providerLookup.message)
            statement.setNullableString(4, dataObject.stringValue("lgnm"))
            statement.setNullableString(5, dataObject.stringValue("tradeNam"))
            statement.setNullableString(6, dataObject.stringValue("sts"))
            statement.setNullableString(7, dataObject.stringValue("dty"))
            statement.setNullableString(8, dataObject.stringValue("ctb"))
            statement.setNullableString(9, dataObject.stringValue("rgdt"))
            statement.setNullableString(10, dataObject.stringValue("cxdt"))
            statement.setNullableString(11, dataObject?.get("pradr")?.jsonObjectOrNull()?.stringValue("adr"))
            statement.setNullableString(12, dataObject.stringValue("stj"))
            statement.setNullableString(13, dataObject.stringValue("ctj"))
            statement.setString(14, providerLookup.data?.toString() ?: "null")
            statement.setString(15, providerLookup.rawResponse.toString())
            statement.executeQuery().use { result ->
                result.next()
                result.toGstinLookupRecord()
            }
        }
    }

    private fun ResultSet.toGstinLookupRecord(): GstinLookupRecord =
        GstinLookupRecord(
            gstin = getString("gstin"),
            flag = getBoolean("flag"),
            message = getString("message"),
            dataJson = getString("data_json"),
            rawResponseJson = getString("raw_response_json"),
            updatedAt = getTimestamp("updated_at").toInstantString(),
        )

    private fun JsonObject?.stringValue(key: String): String? =
        this?.get(key)
            ?.jsonPrimitiveOrNull()
            ?.contentOrNull
            ?.trim()
            ?.ifBlank { null }

    private fun kotlinx.serialization.json.JsonElement.jsonObjectOrNull(): JsonObject? =
        runCatching { jsonObject }.getOrNull()

    private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
        runCatching { jsonPrimitive }.getOrNull()

    private fun PreparedStatement.setNullableString(index: Int, value: String?) {
        if (value.isNullOrBlank()) {
            setString(index, null)
        } else {
            setString(index, value)
        }
    }

    private fun java.sql.Timestamp?.toInstantString(): String =
        (this?.toInstant() ?: Instant.now()).toString()
}
