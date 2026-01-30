package com.merkost.honq.integrity

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.merkost.honq.BuildKonfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.kimplify.cedar.logging.Cedar
import kotlin.coroutines.resume

class PlayIntegrityService(context: Context) {

    private val integrityManager = IntegrityManagerFactory.createStandard(context)
    private val cloudProjectNumber = BuildKonfig.GOOGLE_CLOUD_PROJECT_NUMBER.toLong()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var tokenProvider: com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    init {
        scope.launch {
            warmup().onFailure { e ->
                Cedar.tag("PlayIntegrity").e("Failed to warm up integrity: ${e.message}")
            }
        }
    }

    private suspend fun warmup(): Result<Unit> = suspendCancellableCoroutine { cont ->
        val request = PrepareIntegrityTokenRequest.builder()
            .setCloudProjectNumber(cloudProjectNumber)
            .build()

        integrityManager.prepareIntegrityToken(request)
            .addOnSuccessListener { provider ->
                tokenProvider = provider
                cont.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }
    }

    suspend fun requestIntegrityToken(requestHash: String): Result<String> {
        val provider = tokenProvider
            ?: return Result.failure(IllegalStateException("Integrity token provider not prepared. Warmup may still be in progress."))

        return suspendCancellableCoroutine { cont ->
            val request = StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()

            provider.request(request)
                .addOnSuccessListener { response ->
                    cont.resume(Result.success(response.token()))
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }
    }
}
