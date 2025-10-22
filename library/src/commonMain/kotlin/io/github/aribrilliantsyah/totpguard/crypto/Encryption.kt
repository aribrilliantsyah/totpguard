package io.github.aribrilliantsyah.totpguard.crypto

import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

/**
 * Provides AES-256-GCM encryption and decryption
 */
class Encryption {
    private val algorithm = "AES"
    private val transformation = "AES/GCM/NoPadding"
    private val keySize = 256
    private val gcmTagLength = 128
    private val ivSize = 12 // GCM standard IV length

    /**
     * Encrypts plaintext using AES-256-GCM
     *
     * @param plaintext The text to encrypt
     * @param key The encryption key
     * @return An EncryptionResult containing ciphertext, IV, and auth tag
     */
    fun encrypt(plaintext: String, key: ByteArray): EncryptionResult {
        val cipher = Cipher.getInstance(transformation)
        val iv = ByteArray(ivSize)
        SecureRandom().nextBytes(iv)
        
        val secretKey = SecretKeySpec(key, algorithm)
        val gcmParameterSpec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
        
        // In GCM mode, the authentication tag is appended to the ciphertext
        // For proper separation, we'll extract the tag (usually last 16 bytes)
        val tagSize = 16
        val ciphertext = encryptedBytes.copyOfRange(0, encryptedBytes.size - tagSize)
        val authTag = encryptedBytes.copyOfRange(encryptedBytes.size - tagSize, encryptedBytes.size)
        
        return EncryptionResult(ciphertext, iv, authTag)
    }

    /**
     * Decrypts ciphertext using AES-256-GCM
     *
     * @param encryptedData The EncryptionResult to decrypt
     * @param key The encryption key
     * @return The decrypted plaintext
     */
    fun decrypt(encryptedData: EncryptionResult, key: ByteArray): String {
        val cipher = Cipher.getInstance(transformation)
        val secretKey = SecretKeySpec(key, algorithm)
        
        val gcmParameterSpec = GCMParameterSpec(gcmTagLength, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        
        // Combine ciphertext and tag for decryption
        val combined = ByteArray(encryptedData.ciphertext.size + encryptedData.authTag.size)
        System.arraycopy(encryptedData.ciphertext, 0, combined, 0, encryptedData.ciphertext.size)
        System.arraycopy(encryptedData.authTag, 0, combined, encryptedData.ciphertext.size, encryptedData.authTag.size)
        
        val decryptedBytes = cipher.doFinal(combined)
        return String(decryptedBytes)
    }

    /**
     * Generates a secure 256-bit encryption key
     *
     * @return A 32-byte array containing the key
     */
    fun generateKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(algorithm)
        keyGenerator.init(keySize)
        return keyGenerator.generateKey().encoded
    }
}