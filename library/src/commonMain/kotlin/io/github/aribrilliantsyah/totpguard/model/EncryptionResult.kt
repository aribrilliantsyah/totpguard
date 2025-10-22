package io.github.aribrilliantsyah.totpguard.model

/**
 * Result of AES-256-GCM encryption
 *
 * @property ciphertext The encrypted data
 * @property iv The initialization vector used for encryption
 * @property authTag The authentication tag for GCM mode
 */
data class EncryptionResult(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val authTag: ByteArray
) {
    companion object { }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EncryptionResult

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!authTag.contentEquals(other.authTag)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + authTag.contentHashCode()
        return result
    }
}