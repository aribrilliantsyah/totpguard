package io.github.aribrilliantsyah.totpguard.model

/**
 * Result of TOTP code verification
 *
 * @property isValid Whether the code is valid
 */
data class TotpVerificationResult(
    val isValid: Boolean
)