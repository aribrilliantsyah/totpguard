package io.github.aribrilliantsyah.totpguard.platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA512
import dev.whyoleg.cryptography.random.CryptographyRandom

/**
 * Platform-agnostic cryptographic operations using cryptography-kotlin library.
 * This implementation works on all platforms (JVM, iOS, Android, etc.)
 */
@OptIn(DelicateCryptographyApi::class)
class CryptoProvider {
    private val provider = CryptographyProvider.Default
    private val random = CryptographyRandom
    
    /**
     * Generates cryptographically secure random bytes
     * 
     * @param size The number of bytes to generate
     * @return A ByteArray containing random bytes
     */
    fun generateSecureRandom(size: Int): ByteArray {
        return random.nextBytes(size)
    }
    
    /**
     * Computes HMAC-SHA1 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA1 hash
     */
    suspend fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = provider.get(HMAC)
        val hmacKey = hmac.keyDecoder(SHA1).decodeFromByteArray(HMAC.Key.Format.RAW, key)
        return hmacKey.signatureGenerator().generateSignature(data)
    }
    
    /**
     * Computes HMAC-SHA256 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA256 hash
     */
    suspend fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = provider.get(HMAC)
        val hmacKey = hmac.keyDecoder(SHA256).decodeFromByteArray(HMAC.Key.Format.RAW, key)
        return hmacKey.signatureGenerator().generateSignature(data)
    }
    
    /**
     * Computes HMAC-SHA512 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA512 hash
     */
    suspend fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = provider.get(HMAC)
        val hmacKey = hmac.keyDecoder(SHA512).decodeFromByteArray(HMAC.Key.Format.RAW, key)
        return hmacKey.signatureGenerator().generateSignature(data)
    }
    
    /**
     * Encrypts data using AES-256-GCM
     * 
     * @param plaintext The data to encrypt
     * @param key The encryption key (32 bytes for AES-256)
     * @param iv The initialization vector (12 bytes for GCM)
     * @return Encrypted data with authentication tag appended
     */
    suspend fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val aes = provider.get(AES.GCM)
        val aesKey = aes.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
        val cipher = aesKey.cipher()
        return cipher.encrypt(plaintext, iv)
    }
    
    /**
     * Decrypts data using AES-256-GCM
     * 
     * @param ciphertext The encrypted data with authentication tag
     * @param key The encryption key (32 bytes for AES-256)
     * @param iv The initialization vector (12 bytes for GCM)
     * @return Decrypted data
     * @throws Exception if authentication fails
     */
    suspend fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val aes = provider.get(AES.GCM)
        val aesKey = aes.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
        val cipher = aesKey.cipher()
        return cipher.decrypt(ciphertext, iv)
    }
    
    /**
     * Generates a secure 256-bit (32 bytes) encryption key
     * 
     * @return A 32-byte encryption key
     */
    fun generateAesKey(): ByteArray {
        return generateSecureRandom(32) // 256 bits
    }
}
