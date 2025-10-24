package io.github.aribrilliantsyah.totpguard.platform

import org.mindrot.jbcrypt.BCrypt

/**
 * JVM implementation of BCrypt password hashing using jBCrypt library
 */
actual class BCryptProvider {
    
    actual fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    
    actual fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashedPassword)
        } catch (e: Exception) {
            false
        }
    }
}
