package io.github.aribrilliantsyah.totpguard.model

/**
 * Result of backup code verification
 *
 * @property isValid Whether the code is valid
 * @property codeIndex The index of the code in the list of backup codes, or null if not valid
 */
data class BackupCodeVerificationResult(
    val isValid: Boolean,
    val codeIndex: Int? = null
)