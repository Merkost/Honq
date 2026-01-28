package com.merkost.honq.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

/**
 * Verifies the app's signing certificate to detect tampering or repackaging.
 *
 * If someone modifies the APK (to remove license checks, inject ads, etc.),
 * they must re-sign it with a different certificate. This class detects that.
 */
object SignatureVerifier {

    /**
     * SHA-256 hash of the release signing certificate.
     *
     * To get this value for your release keystore:
     * 1. Build a release APK
     * 2. Run: keytool -printcert -jarfile app-release.apk
     * 3. Copy the SHA-256 fingerprint (remove colons)
     *
     * Or use the debug hash during development and update for release.
     *
     * IMPORTANT: Replace this with your actual release certificate hash!
     */
    private val VALID_SIGNATURES = emptySet<String>(
        // Add your release signing certificate SHA-256 hash here
        // Format: lowercase hex without colons
        // Example: "a1b2c3d4e5f6..."

        // Debug signature (remove in production or keep for testing)
        // You can add multiple valid signatures if needed (e.g., for different build variants)
    )

    /**
     * Set to true to enable signature verification.
     * Keep false during development, enable for release builds.
     */
    private const val VERIFICATION_ENABLED = false

    /**
     * Verifies the app's signature is valid.
     *
     * @param context Application context
     * @return VerificationResult indicating success or the type of failure
     */
    fun verify(context: Context): VerificationResult {
        if (!VERIFICATION_ENABLED) {
            return VerificationResult.Success
        }

        if (VALID_SIGNATURES.isEmpty()) {
            // No signatures configured - skip verification
            // This prevents the app from breaking if you forget to add signatures
            return VerificationResult.Success
        }

        return try {
            val currentSignature = getCurrentSignatureHash(context)

            if (currentSignature == null) {
                VerificationResult.Failure.NoSignatureFound
            } else if (VALID_SIGNATURES.contains(currentSignature)) {
                VerificationResult.Success
            } else {
                VerificationResult.Failure.SignatureMismatch(currentSignature)
            }
        } catch (e: Exception) {
            VerificationResult.Failure.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Gets the SHA-256 hash of the current APK's signing certificate.
     */
    @Suppress("DEPRECATION")
    private fun getCurrentSignatureHash(context: Context): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                return null
            }

            // Get the first signature (primary signing certificate)
            val signature = signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signature.toByteArray())

            // Convert to lowercase hex string
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper function to get the current signature hash for setup.
     * Call this during development to get the hash to add to VALID_SIGNATURES.
     */
    fun getSignatureHashForSetup(context: Context): String {
        val hash = getCurrentSignatureHash(context)
        return hash ?: "Unable to get signature hash"
    }

    sealed class VerificationResult {
        data object Success : VerificationResult()

        sealed class Failure : VerificationResult() {
            data object NoSignatureFound : Failure()
            data class SignatureMismatch(val actualSignature: String) : Failure()
            data class Error(val message: String) : Failure()
        }
    }
}
