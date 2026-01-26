package com.merkost.honq.data.remote.api

import com.merkost.honq.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    private var supabaseUrl: String = BuildKonfig.SUPABASE_URL
    private var supabaseKey: String = BuildKonfig.SUPABASE_KEY

    const val STORAGE_BUCKET_QUESTIONS = "questions"

    fun configure(url: String, key: String) {
        supabaseUrl = url
        supabaseKey = key
    }

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()

    fun createClient(): SupabaseClient {
        require(isConfigured) { "Supabase credentials not configured. Add supabase.url and supabase.anon.key to local.properties" }
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
        }
    }

    fun getStorageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        val baseUrl = supabaseUrl.trimEnd('/')
        val path = relativePath.trimStart('/')
        return "$baseUrl/storage/v1/object/public/$path"
    }
}
