package com.merkost.honq

import android.app.Application
import com.merkost.honq.core.analytics.initAmplitudeContext
import com.merkost.honq.data.local.initDataStore
import com.merkost.honq.security.SignatureVerifier
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.kimplify.cedar.logging.Cedar

class HonqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        verifyAppSignature()

        initDataStore(this)
        initAmplitudeContext(this)
    }

    private fun verifyAppSignature() {
        if (BuildConfig.DEBUG) {
            val signatureHash = SignatureVerifier.getSignatureHashForSetup(this)
            Cedar.tag("SignatureVerifier").d("Current signature hash: $signatureHash")
            Cedar.tag("SignatureVerifier").d("Add this to VALID_SIGNATURES in SignatureVerifier.kt")
        }

        when (val result = SignatureVerifier.verify(this)) {
            is SignatureVerifier.VerificationResult.Success -> {
                Cedar.tag("SignatureVerifier").d("App signature verification passed")
            }
            is SignatureVerifier.VerificationResult.Failure -> {
                handleTamperedApp(result)
            }
        }
    }

    private fun handleTamperedApp(failure: SignatureVerifier.VerificationResult.Failure) {
        val message = when (failure) {
            is SignatureVerifier.VerificationResult.Failure.NoSignatureFound ->
                "No signature found"
            is SignatureVerifier.VerificationResult.Failure.SignatureMismatch ->
                "Signature mismatch: ${failure.actualSignature}"
            is SignatureVerifier.VerificationResult.Failure.Error ->
                "Verification error: ${failure.message}"
        }
        Cedar.tag("SignatureVerifier").e("App integrity check failed: $message")

        // Option 1: Kill the app immediately (aggressive)
        // android.os.Process.killProcess(android.os.Process.myPid())

        // Option 2: Show a message and exit (user-friendly)
        // This will be handled in MainActivity

        // Option 3: Disable certain features silently
        // AppState.isTampered = true

        // For now, we'll set a flag that MainActivity can check
        isTampered = true
    }

    companion object {
        /**
         * Flag indicating the app has been tampered with.
         * Check this in MainActivity to show an error and exit.
         */
        var isTampered = false
            private set
    }
}
