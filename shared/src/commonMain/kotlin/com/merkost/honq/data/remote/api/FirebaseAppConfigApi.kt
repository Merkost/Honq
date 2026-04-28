package com.merkost.honq.data.remote.api

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable
import org.kimplify.cedar.logging.Cedar

class FirebaseAppConfigApi(
    private val firestore: FirebaseFirestore
) {
    @Serializable
    private data class LongConfigDoc(val value: Long? = null)

    @Serializable
    private data class StringConfigDoc(val value: String? = null)

    suspend fun fetchDataVersion(): Int = try {
        Cedar.tag("FbAppConfigApi").d("Fetching data_version from app_config...")
        val v = fetchInt("data_version") ?: 0
        Cedar.tag("FbAppConfigApi").d("Fetched data_version=$v")
        v
    } catch (e: Exception) {
        Cedar.tag("FbAppConfigApi").e("fetchDataVersion failed, returning 0: ${e.message}", e)
        0
    }

    suspend fun fetchString(key: String): String? = try {
        Cedar.tag("FbAppConfigApi").d("Fetching $key from app_config...")
        val value = firestore.collection("app_config")
            .document(key)
            .get()
            .data<StringConfigDoc>()
            .value
        Cedar.tag("FbAppConfigApi").d("Fetched $key=$value")
        value
    } catch (e: Exception) {
        Cedar.tag("FbAppConfigApi").e("fetchString($key) failed: ${e.message}", e)
        null
    }

    private suspend fun fetchInt(key: String): Int? = firestore.collection("app_config")
        .document(key)
        .get()
        .data<LongConfigDoc>()
        .value
        ?.toInt()
}
