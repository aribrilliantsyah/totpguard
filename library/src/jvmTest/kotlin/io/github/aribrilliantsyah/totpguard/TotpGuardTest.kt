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
    fun testBackupCodes() {
        val backupCodes = TotpGuard.generateBackupCodes(count = 5, length = 8)
        
        // Should have 5 codes
        assertEquals(5, backupCodes.plainCodes.size)
        assertEquals(5, backupCodes.hashedCodes.size)
        assertEquals(5, backupCodes.formattedCodes.size)
        
        // Each code should be 8 characters
        backupCodes.plainCodes.forEach {
            assertEquals(8, it.length)
        }
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
}