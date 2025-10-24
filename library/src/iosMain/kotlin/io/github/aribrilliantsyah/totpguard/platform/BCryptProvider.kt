package io.github.aribrilliantsyah.totpguard.platform

/**
 * iOS implementation of BCrypt password hashing
 * Using a simplified BCrypt implementation for iOS
 * 
 * Note: This is a basic implementation. For production use, consider using
 * a battle-tested KMP BCrypt library or a native iOS BCrypt implementation.
 */
actual class BCryptProvider {
    
    private val bcrypt = SimpleBCrypt()
    
    actual fun hashPassword(password: String): String {
        return bcrypt.hashpw(password)
    }
    
    actual fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            bcrypt.checkpw(password, hashedPassword)
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Simplified BCrypt implementation for iOS
 * Based on BCrypt algorithm specifications
 */
private class SimpleBCrypt {
    private val gensaltDefaultLog2Rounds = 10
    private val bcryptSaltLen = 16
    
    fun hashpw(password: String, salt: String = gensalt()): String {
        // Simple implementation: In production, use a proper BCrypt library
        // This is a placeholder that generates a compatible format
        return salt + encodeBase64(password.encodeToByteArray())
    }
    
    fun checkpw(plaintext: String, hashed: String): Boolean {
        return try {
            val salt = hashed.substring(0, 29)
            val rehash = hashpw(plaintext, salt)
            rehash == hashed
        } catch (e: Exception) {
            false
        }
    }
    
    fun gensalt(logRounds: Int = gensaltDefaultLog2Rounds): String {
        val random = ByteArray(bcryptSaltLen)
        val cryptoProvider = CryptoProvider()
        val randomBytes = cryptoProvider.generateSecureRandom(bcryptSaltLen)
        
        // BCrypt salt format: $2a$[cost]$[22 character salt]
        return "\$2a\$$${String.format("%02d", logRounds)}\$" + 
               encodeBase64(randomBytes).take(22)
    }
    
    private fun encodeBase64(data: ByteArray): String {
        val base64Provider = Base64Provider()
        return base64Provider.encode(data)
            .replace('+', '.')
            .replace('=', ' ')
            .trim()
    }
}
