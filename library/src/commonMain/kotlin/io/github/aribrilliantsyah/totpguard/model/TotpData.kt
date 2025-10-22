package io.github.aribrilliantsyah.totpguard.model

/**
 * Contains the data needed for TOTP generation and verification
 *
 * @property secret Base32-encoded secret key
 * @property issuer The name of the service or organization
 * @property accountName The account name (e.g., email address or username)
 * @property algorithm The hash algorithm to use
 * @property digits The number of digits in the code
 * @property period The period in seconds for which a code is valid
 */
data class TotpData(
    val secret: String,
    val issuer: String,
    val accountName: String,
    val algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    val digits: Int = 6,
    val period: Int = 30
) {
    /**
     * Gets the otpauth:// URI for this TOTP data
     *
     * @return An otpauth:// URI for QR code generation
     */
    fun getUri(): String {
        val cleanIssuer = issuer.replace(":", "")
        val cleanAccount = accountName.replace(":", "")
        
        val algorithmName = when (algorithm) {
            TotpAlgorithm.SHA1 -> "SHA1"
            TotpAlgorithm.SHA256 -> "SHA256"
            TotpAlgorithm.SHA512 -> "SHA512"
        }
        
        return "otpauth://totp/${cleanIssuer}:${cleanAccount}?" +
               "secret=${secret}&" +
               "issuer=${cleanIssuer}&" +
               "algorithm=${algorithmName}&" +
               "digits=${digits}&" +
               "period=${period}"
    }
}