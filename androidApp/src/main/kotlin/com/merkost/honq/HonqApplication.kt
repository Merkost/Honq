package com.merkost.honq

import android.app.Application
import android.util.Log
import com.merkost.honq.core.analytics.initAmplitudeContext
import com.merkost.honq.data.local.initDataStore
import com.merkost.honq.security.SignatureVerifier

class HonqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Verify app signature to detect tampering
        verifyAppSignature()

        initDataStore(this)
        initAmplitudeContext(this)
    }

    private fun verifyAppSignature() {
        // Log the current signature hash during development
        // This helps you find the hash to add to VALID_SIGNATURES
        if (BuildConfig.DEBUG) {
            val signatureHash = SignatureVerifier.getSignatureHashForSetup(this)
            Log.d("SignatureVerifier", "Current signature hash: $signatureHash")
            Log.d("SignatureVerifier", "Add this to VALID_SIGNATURES in SignatureVerifier.kt")
        }

        // Verify signature
        when (val result = SignatureVerifier.verify(this)) {
            is SignatureVerifier.VerificationResult.Success -> {
                // Signature is valid, continue normally
            }
            is SignatureVerifier.VerificationResult.Failure -> {
                handleTamperedApp(result)
            }
        }
    }

    private fun handleTamperedApp(failure: SignatureVerifier.VerificationResult.Failure) {
        // Log the failure for analytics/debugging
        val message = when (failure) {
            is SignatureVerifier.VerificationResult.Failure.NoSignatureFound ->
                "No signature found"
            is SignatureVerifier.VerificationResult.Failure.SignatureMismatch ->
                "Signature mismatch: ${failure.actualSignature}"
            is SignatureVerifier.VerificationResult.Failure.Error ->
                "Verification error: ${failure.message}"
        }
        Log.e("SignatureVerifier", "App integrity check failed: $message")

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
