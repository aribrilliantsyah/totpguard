package io.github.aribrilliantsyah.totpguard.platform

import kotlin.math.floor
import kotlin.math.pow
import kotlin.system.currentTimeMillis

class IosTotpGenerator {

    private val timeStep = 30 // Time step in seconds
    private val digits = 6 // Number of digits in the TOTP code

    fun generateTotp(secret: String): String {
        val timeIndex = floor(currentTimeMillis() / 1000.0 / timeStep).toLong()
        val hmac = hmacSha1(secret.toByteArray(), timeIndex.toByteArray())
        val offset = hmac[hmac.size - 1] and 0x0F
        val binary = (hmac[offset.toInt()] and 0x7F) shl 24 or
                (hmac[offset.toInt() + 1].toInt() and 0xFF shl 16) or
                (hmac[offset.toInt() + 2].toInt() and 0xFF shl 8) or
                (hmac[offset.toInt() + 3].toInt() and 0xFF)

        val otp = binary % 10.0.pow(digits).toInt()
        return otp.toString().padStart(digits, '0')
    }

    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        // Implement HMAC-SHA1 algorithm
        // This is a placeholder for the actual HMAC-SHA1 implementation
        return ByteArray(20) // Return a dummy byte array for now
    }

    private fun Long.toByteArray(): ByteArray {
        return ByteArray(8).apply {
            for (i in indices) {
                this[i] = (this@toByteArray shr (56 - (i * 8))).toByte()
            }
        }
    }
}