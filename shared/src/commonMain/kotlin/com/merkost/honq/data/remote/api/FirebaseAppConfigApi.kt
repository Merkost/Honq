package com.merkost.honq.data.remote.api

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.kimplify.cedar.logging.Cedar

class FirebaseAppConfigApi(
    private val firestore: FirebaseFirestore
) {
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
        val snap = firestore.collection("app_config").document(key).get()
        val value = snap.data<Map<String, Any?>>()["value"] as? String
        Cedar.tag("FbAppConfigApi").d("Fetched $key=$value")
        value
    } catch (e: Exception) {
        Cedar.tag("FbAppConfigApi").e("fetchString($key) failed: ${e.message}", e)
        null
    }

    private suspend fun fetchInt(key: String): Int? {
        val snap = firestore.collection("app_config").document(key).get()
        // Firestore returns numbers as Long via the gitlive wrapper; tolerate both.
        val raw = snap.data<Map<String, Any?>>()["value"]
        return (raw as? Long)?.toInt() ?: (raw as? Int)
    }
}
