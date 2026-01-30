package com.merkost.honq.integrity

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.Functions
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class IntegrityVerdict(
    val deviceIntegrity: List<String> = emptyList(),
    val appIntegrity: String = "",
    val accountIntegrity: String = "",
)

class IntegrityRepository(
    private val playIntegrityService: PlayIntegrityService,
    private val supabaseClient: SupabaseClient,
) {

    suspend fun requestAndVerifyIntegrity(requestHash: String): Result<IntegrityVerdict> {
        val tokenResult = playIntegrityService.requestIntegrityToken(requestHash)
        val token = tokenResult.getOrElse { return Result.failure(it) }

        return try {
            val body = buildJsonObject {
                put("token", token)
            }

            val response = supabaseClient.functions.invoke(
                function = "verify-integrity",
                body = body,
                headers = Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                },
            )

            val verdict = response.body<IntegrityVerdict>()
            Result.success(verdict)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
