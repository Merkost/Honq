package com.merkost.honq.data.remote.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import org.kimplify.cedar.logging.Cedar

@Serializable
private data class AppConfigRow(
    val key: String,
    val value: JsonElement
)

class AppConfigApi(
    private val client: SupabaseClient
) {
    suspend fun fetchDataVersion(): Int = try {
        Cedar.tag("AppConfigApi").d("Fetching data_version from app_config...")
        val version = (fetchValue("data_version") as? JsonPrimitive)?.intOrNull ?: 0
        Cedar.tag("AppConfigApi").d("Fetched data_version=$version")
        version
    } catch (e: Exception) {
        Cedar.tag("AppConfigApi").e("fetchDataVersion failed, returning 0: ${e.message}", e)
        0
    }

    suspend fun fetchString(key: String): String? = try {
        Cedar.tag("AppConfigApi").d("Fetching $key from app_config...")
        val value = (fetchValue(key) as? JsonPrimitive)?.contentOrNull
        Cedar.tag("AppConfigApi").d("Fetched $key=$value")
        value
    } catch (e: Exception) {
        Cedar.tag("AppConfigApi").e("fetchString($key) failed: ${e.message}", e)
        null
    }

    private suspend fun fetchValue(key: String): JsonElement? {
        val rows = client.postgrest["app_config"]
            .select {
                filter {
                    eq("key", key)
                }
            }
            .decodeList<AppConfigRow>()
        return rows.firstOrNull()?.value
    }
}
