package io.github.aribrilliantsyah.totpguard.platform

/**
 * Platform-agnostic cryptographic operations.
 * Each platform provides its own implementation using native crypto libraries.
 */
expect class CryptoProvider() {
    /**
     * Generates cryptographically secure random bytes
     * 
     * @param size The number of bytes to generate
     * @return A ByteArray containing random bytes
     */
    fun generateSecureRandom(size: Int): ByteArray
    
    /**
     * Computes HMAC-SHA1 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA1 hash
     */
    fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray
    
    /**
     * Computes HMAC-SHA256 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA256 hash
     */
    fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray
    
    /**
     * Computes HMAC-SHA512 hash
     * 
     * @param key The secret key
     * @param data The data to hash
     * @return The HMAC-SHA512 hash
     */
    fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray
    
    /**
     * Encrypts data using AES-256-GCM
     * 
     * @param plaintext The data to encrypt
     * @param key The encryption key (32 bytes for AES-256)
     * @param iv The initialization vector (12 bytes for GCM)
     * @return Encrypted data with authentication tag appended
     */
    fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    
    /**
     * Decrypts data using AES-256-GCM
     * 
     * @param ciphertext The encrypted data with authentication tag
     * @param key The encryption key (32 bytes for AES-256)
     * @param iv The initialization vector (12 bytes for GCM)
     * @return Decrypted data
     * @throws Exception if authentication fails
     */
    fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    
    /**
     * Generates a secure 256-bit (32 bytes) encryption key
     * 
     * @return A 32-byte encryption key
     */
    fun generateAesKey(): ByteArray
}
