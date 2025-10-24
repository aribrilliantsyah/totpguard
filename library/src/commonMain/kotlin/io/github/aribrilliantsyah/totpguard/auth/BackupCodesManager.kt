package io.github.aribrilliantsyah.totpguard.auth

import io.github.aribrilliantsyah.totpguard.platform.BCryptProvider
import kotlin.random.Random

/**
 * Manages the generation and verification of backup codes
 *
 * @property codeCount The number of backup codes to generate (default: 10)
 * @property codeLength The length of each backup code in characters (default: 8)
 */
class BackupCodesManager(private val codeCount: Int = 10, private val codeLength: Int = 8) {
    private val bcryptProvider = BCryptProvider()
    private val charset = ('A'..'Z') + ('0'..'9') - listOf('0', '1', 'I', 'O')
    
    /**
     * Generates a list of backup codes
     *
     * @return A list of randomly generated backup codes
     */
    fun generateBackupCodes(): List<String> {
        return List(codeCount) { generateCode() }
    }

    /**
     * Generates a single backup code
     *
     * @return A randomly generated backup code
     */
    private fun generateCode(): String {
        return (1..codeLength)
            .map { charset.random() }
            .joinToString("")
    }
    
    /**
     * Hashes a list of backup codes for secure storage
     *
     * @param plainCodes The list of plain backup codes
     * @return A list of hashed backup codes
     */
    fun hashCodes(plainCodes: List<String>): List<String> {
        return plainCodes.map { bcryptProvider.hashPassword(it) }
    }

    /**
     * Verifies a backup code against a list of hashed codes
     *
     * @param code The plain backup code to verify
     * @param hashedCodes The list of hashed backup codes
     * @return Whether the code is valid
     */
    fun verifyCode(code: String, hashedCodes: List<String>): Boolean {
        return hashedCodes.any { bcryptProvider.verifyPassword(code, it) }
    }

    /**
     * Formats a list of backup codes for display
     *
     * @param codes The list of backup codes to format
     * @return A formatted string with one code per line
     */
    fun formatCodesForDisplay(codes: List<String>): String {
        return codes.joinToString(separator = "\n") { "- $it" }
    }
    
    /**
     * Formats a single backup code by inserting separators
     *
     * @param code The backup code to format
     * @param groupSize The number of characters per group
     * @param separator The separator between groups
     * @return The formatted backup code
     */
    fun formatCode(code: String, groupSize: Int = 4, separator: String = "-"): String {
        return code.chunked(groupSize).joinToString(separator)
    }
}