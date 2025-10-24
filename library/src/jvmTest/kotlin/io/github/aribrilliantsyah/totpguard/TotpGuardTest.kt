package io.github.aribrilliantsyah.totpguard

import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TotpGuardTest {

    @Test
    fun testTotpSecret() {
        // Test TOTP secret generation
        val secret = TotpGuard.generateTotpSecret()
        
        // Secret should be non-empty
        assertNotNull(secret)
        assertTrue(secret.isNotEmpty())
        
        // Secret should be Base32 encoded (A-Z, 2-7)
        assertTrue(secret.all { it in 'A'..'Z' || it in '2'..'7' })
        
        // Secret should be at least 16 characters
        assertTrue(secret.length >= 16)
    }
    
    @Test
    fun testTotpGeneration() {
        // Generate a secret
        val secret = TotpGuard.generateTotpSecret()
        
        // Generate a TOTP code
        val code = TotpGuard.generateTotpCode(secret)
        
        // Code should be 6 digits
        assertNotNull(code)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
        
        // Verify the code
        val result = TotpGuard.verifyTotpCode(secret, code)
        assertTrue(result.isValid)
    }
    
    @Test
    fun testTotpUri() {
        val secret = "ABCDEFGHIJKLMNOP"
        val accountName = "test@example.com"
        val issuer = "Test App"
        
        val uri = TotpGuard.generateOtpAuthUri(
            secret = secret,
            accountName = accountName,
            issuer = issuer,
            algorithm = TotpAlgorithm.SHA1,
            digits = 6,
            period = 30
        )
        
        // URI should start with otpauth://totp/
        assertTrue(uri.startsWith("otpauth://totp/"))
        
        // URI should contain all parameters
        assertTrue(uri.contains("secret=$secret"))
        assertTrue(uri.contains("issuer=$issuer"))
        assertTrue(uri.contains("algorithm=SHA1"))
        assertTrue(uri.contains("digits=6"))
        assertTrue(uri.contains("period=30"))
    }
    
    @Test
    fun testEncryption() {
        val plaintext = "This is a secret message"
        val key = TotpGuard.generateEncryptionKey()
        
        // Encrypt
        val encrypted = TotpGuard.encrypt(plaintext, key)
        
        // Check encrypted result
        assertNotNull(encrypted.ciphertext)
        assertNotNull(encrypted.iv)
        assertNotNull(encrypted.authTag)
        
        // Decrypt
        val decrypted = TotpGuard.decrypt(encrypted, key)
        
        // Should match original
        assertEquals(plaintext, decrypted)
    }
    
    @Test
    fun testRemainingSeconds() {
        val seconds = TotpGuard.getRemainingSeconds(30)
        
        // Should be between 0 and 30
        assertTrue(seconds in 0..30)
    }
    
    @Test
    fun testDefaultParameters() {
        val secret = TotpGuard.generateTotpSecret()
        
        // Test with all default parameters
        val code1 = TotpGuard.generateTotpCode(secret)
        assertNotNull(code1)
        assertEquals(6, code1.length)
        
        // Test verification with only required parameters
        val result1 = TotpGuard.verifyTotpCode(secret, code1)
        assertTrue(result1.isValid)
        
        // Test with custom algorithm only
        val code2 = TotpGuard.generateTotpCode(
            secret = secret,
            algorithm = TotpAlgorithm.SHA256
        )
        assertNotNull(code2)
        assertEquals(6, code2.length)
        
        // Test with custom digits only
        val code3 = TotpGuard.generateTotpCode(
            secret = secret,
            digits = 8
        )
        assertNotNull(code3)
        assertEquals(8, code3.length)
        
        // Test generateOtpAuthUri with minimal parameters
        val uri1 = TotpGuard.generateOtpAuthUri(
            secret = secret,
            accountName = "user@example.com",
            issuer = "MyApp"
        )
        assertTrue(uri1.contains("digits=6"))
        assertTrue(uri1.contains("period=30"))
        assertTrue(uri1.contains("algorithm=SHA1"))
        
        // Test generateQrCodePng with default size
        val qrPng = TotpGuard.generateQrCodePng(uri1)
        assertNotNull(qrPng)
        assertTrue(qrPng.isNotEmpty())
        
        // Test generateQrCodeBase64 with default size
        val qrBase64 = TotpGuard.generateQrCodeBase64(uri1)
        assertNotNull(qrBase64)
        assertTrue(qrBase64.isNotEmpty())
        
        // Test getRemainingSeconds with default period
        val remaining = TotpGuard.getRemainingSeconds()
        assertTrue(remaining in 0..30)
        
        // Test generateTotpSecret with default length
        val secret2 = TotpGuard.generateTotpSecret()
        assertNotNull(secret2)
        assertTrue(secret2.length >= 16)
    }
}