package io.github.aribrilliantsyah.totpguard.platform

/**
 * Platform-specific BCrypt password hashing.
 * Each platform must provide an actual implementation.
 */
expect class BCryptProvider() {
    
    /**
     * Hashes a password using BCrypt
     * 
     * @param password The password to hash
     * @return The hashed password
     */
    fun hashPassword(password: String): String
    
    /**
     * Verifies a password against a BCrypt hash
     * 
     * @param password The plain password to verify
     * @param hashedPassword The BCrypt hash
     * @return true if the password matches, false otherwise
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean
}
