package io.github.aribrilliantsyah.totpguard.model

/**
 * Result of backup codes generation
 *
 * @property plainCodes The generated plain backup codes
 * @property hashedCodes The generated backup codes in hashed form for storage
 * @property formattedCodes The generated backup codes formatted for display
 */
data class BackupCodesResult(
    val plainCodes: List<String>,
    val hashedCodes: List<String>,
    val formattedCodes: List<String>
)