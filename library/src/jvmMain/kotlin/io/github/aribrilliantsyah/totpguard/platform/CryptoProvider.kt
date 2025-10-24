package io.github.aribrilliantsyah.totpguard.platform

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * JVM implementation of cryptographic operations using javax.crypto
 */
actual class CryptoProvider {
    private val secureRandom = SecureRandom()
    
    actual fun generateSecureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }
    
    actual fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(key, "HmacSHA1")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key, "HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    actual fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(key, "HmacSHA512")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    actual fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv) // 128-bit auth tag
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        return cipher.doFinal(plaintext)
    }
    
    actual fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv) // 128-bit auth tag
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        return cipher.doFinal(ciphertext)
    }
    
    actual fun generateAesKey(): ByteArray {
        return generateSecureRandom(32) // 256 bits
    }
}
