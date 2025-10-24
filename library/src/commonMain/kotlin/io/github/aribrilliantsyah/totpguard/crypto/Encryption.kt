package io.github.aribrilliantsyah.totpguard.crypto

import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import io.github.aribrilliantsyah.totpguard.platform.CryptoProvider
import kotlinx.coroutines.runBlocking

/**
 * Provides AES-256-GCM encryption and decryption
 */
class Encryption {
    private val cryptoProvider = CryptoProvider()
    private val ivSize = 12 // GCM standard IV length
    private val tagSize = 16 // GCM authentication tag length

    /**
     * Encrypts plaintext using AES-256-GCM
     *
     * @param plaintext The text to encrypt
     * @param key The encryption key
     * @return An EncryptionResult containing ciphertext, IV, and auth tag
     */
    fun encrypt(plaintext: String, key: ByteArray): EncryptionResult = runBlocking {
        val iv = cryptoProvider.generateSecureRandom(ivSize)
        val plaintextBytes = plaintext.encodeToByteArray()
        
        val encryptedBytes = cryptoProvider.aesGcmEncrypt(plaintextBytes, key, iv)
        
        // In GCM mode, the authentication tag is appended to the ciphertext
        // Extract the tag (last 16 bytes)
        val ciphertext = encryptedBytes.copyOfRange(0, encryptedBytes.size - tagSize)
        val authTag = encryptedBytes.copyOfRange(encryptedBytes.size - tagSize, encryptedBytes.size)
        
        EncryptionResult(ciphertext, iv, authTag)
    }

    /**
     * Decrypts ciphertext using AES-256-GCM
     *
     * @param encryptedData The EncryptionResult to decrypt
     * @param key The encryption key
     * @return The decrypted plaintext
     */
    fun decrypt(encryptedData: EncryptionResult, key: ByteArray): String = runBlocking {
        // Combine ciphertext and tag for decryption
        val combined = ByteArray(encryptedData.ciphertext.size + encryptedData.authTag.size)
        encryptedData.ciphertext.copyInto(combined, 0)
        encryptedData.authTag.copyInto(combined, encryptedData.ciphertext.size)
        
        val decryptedBytes = cryptoProvider.aesGcmDecrypt(combined, key, encryptedData.iv)
        decryptedBytes.decodeToString()
    }

    /**
     * Generates a secure 256-bit encryption key
     *
     * @return A 32-byte array containing the key
     */
    fun generateKey(): ByteArray {
        return cryptoProvider.generateAesKey()
    }
}