package io.github.aribrilliantsyah.totpguard

import io.github.aribrilliantsyah.totpguard.auth.TotpGenerator
import io.github.aribrilliantsyah.totpguard.crypto.Encryption
import io.github.aribrilliantsyah.totpguard.qr.QrCodeGenerator
import io.github.aribrilliantsyah.totpguard.model.TotpData
import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm
import io.github.aribrilliantsyah.totpguard.model.TotpVerificationResult
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import io.github.aribrilliantsyah.totpguard.platform.CryptoProvider

/**
 * Main entry point for the TOTP-GUARD library.
 * Provides access to TOTP, encryption, and QR code functionality.
 */
object TotpGuard {
    private val cryptoProvider = CryptoProvider()

    /**
     * Generates a secure Base32-encoded TOTP secret.
     *
     * @param length The length of the secret key in bytes (default: 32)
     * @return A Base32-encoded secret key
     */
    fun generateTotpSecret(length: Int = 32): String {
        val random = cryptoProvider.generateSecureRandom(length)
        // Encode as Base32
        return Base32Encoder.encode(random)
    }

    /**
     * Generates a TOTP code for the current time.
     *
     * @param secret The Base32-encoded secret key
     * @param algorithm The hash algorithm to use (default: SHA1)
     * @param digits The number of digits in the code (default: 6)
     * @param period The period in seconds for which a code is valid (default: 30)
     * @return A TOTP code
     */
    fun generateTotpCode(
        secret: String,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        digits: Int = 6,
        period: Int = 30
    ): String {
        val totpGenerator = TotpGenerator(secret, period.toLong(), digits, algorithm)
        return totpGenerator.generateTotp()
    }

    /**
     * Verifies a TOTP code against a secret.
     *
     * @param secret The Base32-encoded secret key
     * @param code The TOTP code to verify
     * @param timeWindow The number of time periods to check before and after the current one (default: 1)
     * @param algorithm The hash algorithm to use (default: SHA1)
     * @param digits The number of digits in the code (default: 6)
     * @param period The period in seconds for which a code is valid (default: 30)
     * @return A TotpVerificationResult indicating whether the code is valid
     */
    fun verifyTotpCode(
        secret: String,
        code: String,
        timeWindow: Int = 1,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        digits: Int = 6,
        period: Int = 30
    ): TotpVerificationResult {
        val totpGenerator = TotpGenerator(secret, period.toLong(), digits, algorithm)
        val isValid = totpGenerator.verifyTotp(code, timeWindow)
        return TotpVerificationResult(isValid)
    }

    /**
     * Gets the number of seconds remaining until the current TOTP code expires.
     *
     * @param period The period in seconds for which a code is valid (default: 30)
     * @return The number of seconds until the code expires
     */
    fun getRemainingSeconds(period: Int = 30): Int {
        val totpGenerator = TotpGenerator("dummy", period.toLong())
        return totpGenerator.getRemainingTime().toInt()
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * @param plaintext The text to encrypt
     * @param key The encryption key
     * @return An EncryptionResult containing ciphertext, IV, and auth tag
     */
    fun encrypt(plaintext: String, key: ByteArray): EncryptionResult {
        val encryption = Encryption()
        return encryption.encrypt(plaintext, key)
    }

    /**
     * Decrypts ciphertext using AES-256-GCM.
     *
     * @param encryptedData The EncryptionResult to decrypt
     * @param key The encryption key
     * @return The decrypted plaintext
     */
    fun decrypt(encryptedData: EncryptionResult, key: ByteArray): String {
        val encryption = Encryption()
        return encryption.decrypt(encryptedData, key)
    }

    /**
     * Generates a secure 256-bit encryption key.
     *
     * @return A 32-byte array containing the key
     */
    fun generateEncryptionKey(): ByteArray {
        return cryptoProvider.generateAesKey()
    }

    /**
     * Rotates an encryption key.
     *
     * @param encryptedData The data encrypted with the old key
     * @param oldKey The old encryption key
     * @param newKey The new encryption key
     * @return The data encrypted with the new key
     */
    fun rotateKey(encryptedData: EncryptionResult, oldKey: ByteArray, newKey: ByteArray): EncryptionResult {
        val encryption = Encryption()
        val decrypted = encryption.decrypt(encryptedData, oldKey)
        return encryption.encrypt(decrypted, newKey)
    }

    /**
     * Generates a QR code as a PNG image.
     *
     * @param uri The URI to encode
     * @param size The size of the QR code in pixels (default: 300)
     * @return A byte array containing the PNG image
     */
    fun generateQrCodePng(uri: String, size: Int = 300): ByteArray {
        val qrCodeGenerator = QrCodeGenerator()
        return qrCodeGenerator.generateQrCode(uri, size)
    }

    /**
     * Generates a QR code as a Base64-encoded PNG image.
     *
     * @param uri The URI to encode
     * @param size The size of the QR code in pixels (default: 300)
     * @return A Base64-encoded PNG image
     */
    fun generateQrCodeBase64(uri: String, size: Int = 300): String {
        val qrCodeGenerator = QrCodeGenerator()
        return qrCodeGenerator.generateQrCodeBase64(uri, size)
    }

    /**
     * Generates an otpauth:// URI for use with authenticator apps.
     *
     * @param secret The Base32-encoded secret key
     * @param accountName The account name (e.g., email address)
     * @param issuer The issuer name (e.g., company name)
     * @param algorithm The hash algorithm (default: SHA1)
     * @param digits The number of digits in the code (default: 6)
     * @param period The period in seconds for which a code is valid (default: 30)
     * @return An otpauth:// URI
     */
    fun generateOtpAuthUri(
        secret: String,
        accountName: String,
        issuer: String,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        digits: Int = 6,
        period: Int = 30
    ): String {
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

/**
 * Simple Base32 encoder for internal use
 */
private object Base32Encoder {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    
    fun encode(data: ByteArray): String {
        val result = StringBuilder()
        var bits = 0
        var bitsCount = 0
        
        for (b in data) {
            bits = (bits shl 8) or (b.toInt() and 0xff)
            bitsCount += 8
            while (bitsCount >= 5) {
                bitsCount -= 5
                result.append(ALPHABET[(bits shr bitsCount) and 0x1f])
            }
        }
        
        if (bitsCount > 0) {
            result.append(ALPHABET[(bits shl (5 - bitsCount)) and 0x1f])
        }
        
        return result.toString()
    }
}