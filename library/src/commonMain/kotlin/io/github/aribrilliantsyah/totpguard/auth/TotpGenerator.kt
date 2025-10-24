package io.github.aribrilliantsyah.totpguard.auth

import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm
import io.github.aribrilliantsyah.totpguard.platform.CryptoProvider
import io.github.aribrilliantsyah.totpguard.platform.TimeProvider
import kotlin.experimental.and
import kotlin.math.pow

/**
 * Implementation of the TOTP algorithm (RFC 6238)
 *
 * @property secret Base32-encoded secret key
 * @property period Time period in seconds (default: 30)
 * @property digits Number of digits in the code (default: 6)
 * @property algorithm Hash algorithm to use (default: SHA1)
 */
class TotpGenerator(
    private val secret: String,
    private val period: Long = 30,
    private val digits: Int = 6,
    private val algorithm: TotpAlgorithm = TotpAlgorithm.SHA1
) {
    private val cryptoProvider = CryptoProvider()
    private val timeProvider = TimeProvider()

    /**
     * Generates a TOTP code for the current time
     *
     * @return A TOTP code
     */
    fun generateTotp(): String {
        val counter = getTimeCounter()
        return generateCode(counter)
    }

    /**
     * Verifies a TOTP code against the current time
     *
     * @param code The code to verify
     * @param window Number of time periods to check before and after the current one
     * @return Whether the code is valid
     */
    fun verifyTotp(code: String, window: Int = 1): Boolean {
        if (code.length != digits) return false
        if (!code.all { it.isDigit() }) return false

        val counter = getTimeCounter()
        
        // Check current time period
        if (generateCode(counter) == code) return true
        
        // Check adjacent time periods within the window
        for (i in 1..window) {
            // Check past
            if (generateCode(counter - i) == code) return true
            // Check future
            if (generateCode(counter + i) == code) return true
        }
        
        return false
    }

    /**
     * Gets the number of seconds remaining until the current TOTP code expires
     *
     * @return Seconds until code expires
     */
    fun getRemainingTime(): Long {
        return period - (timeProvider.currentTimeSeconds() % period)
    }

    private fun getTimeCounter(): Long {
        return timeProvider.currentTimeSeconds() / period
    }

    private fun generateCode(counter: Long): String {
        val decodedKey = decodeBase32(secret)
        val counterBytes = ByteArray(8)
        var counterVar = counter
        for (i in 7 downTo 0) {
            counterBytes[i] = (counterVar and 0xffL).toByte()
            counterVar = counterVar shr 8
        }

        val hash = calculateHmac(decodedKey, counterBytes)
        
        // Dynamic truncation
        val offset = (hash[hash.size - 1] and 0x0f).toInt()
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                     ((hash[offset + 1].toInt() and 0xff) shl 16) or
                     ((hash[offset + 2].toInt() and 0xff) shl 8) or
                     (hash[offset + 3].toInt() and 0xff)
        
        // Generate code with specified number of digits
        val mod = 10.0.pow(digits.toDouble()).toLong()
        val otp = (binary % mod).toString()
        
        // Pad with leading zeros if necessary
        return otp.padStart(digits, '0')
    }

    private fun calculateHmac(key: ByteArray, counter: ByteArray): ByteArray {
        return when (algorithm) {
            TotpAlgorithm.SHA1 -> cryptoProvider.hmacSha1(key, counter)
            TotpAlgorithm.SHA256 -> cryptoProvider.hmacSha256(key, counter)
            TotpAlgorithm.SHA512 -> cryptoProvider.hmacSha512(key, counter)
        }
    }

    private fun decodeBase32(base32: String): ByteArray {
        val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleanInput = base32.trim().uppercase().replace(Regex("[^A-Z2-7]"), "")
        
        // Base32 decoding implementation
        val result = mutableListOf<Byte>()
        var buffer = 0
        var bitsInBuffer = 0
        
        for (c in cleanInput) {
            val value = ALPHABET.indexOf(c)
            if (value < 0) continue // Skip invalid characters
            
            buffer = (buffer shl 5) or value
            bitsInBuffer += 5
            
            if (bitsInBuffer >= 8) {
                bitsInBuffer -= 8
                result.add((buffer shr bitsInBuffer).toByte())
                buffer = buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        
        return result.toByteArray()
    }
}