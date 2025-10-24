package io.github.aribrilliantsyah.totpguard.platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA512
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.coroutines.runBlocking

/**
 * iOS implementation using cryptography-kotlin with Apple Security Framework backend
 */
@OptIn(DelicateCryptographyApi::class)
actual class CryptoProvider {
    private val provider = CryptographyProvider.Default
    private val random = CryptographyRandom
    
    actual fun generateSecureRandom(size: Int): ByteArray {
        return random.nextBytes(size)
    }
    
    actual fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray = runBlocking {
        val hmac = provider.get(HMAC).keyDecoder(SHA1)
            .decodeFromByteArray(HMAC.Key.Format.RAW, key)
        hmac.signatureGenerator().generateSignature(data)
    }
    
    actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray = runBlocking {
        val hmac = provider.get(HMAC).keyDecoder(SHA256)
            .decodeFromByteArray(HMAC.Key.Format.RAW, key)
        hmac.signatureGenerator().generateSignature(data)
    }
    
    actual fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray = runBlocking {
        val hmac = provider.get(HMAC).keyDecoder(SHA512)
            .decodeFromByteArray(HMAC.Key.Format.RAW, key)
        hmac.signatureGenerator().generateSignature(data)
    }
    
    actual fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray = runBlocking {
        val aes = provider.get(AES.GCM).keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, key)
        aes.cipher().encrypt(plaintext, iv)
    }
    
    actual fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray = runBlocking {
        val aes = provider.get(AES.GCM).keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, key)
        aes.cipher().decrypt(ciphertext, iv)
    }
    
    actual fun generateAesKey(): ByteArray {
        return generateSecureRandom(32) // 256 bits
    }
}
