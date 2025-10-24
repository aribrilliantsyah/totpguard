package io.github.aribrilliantsyah.totpguard.platform

/**
 * Platform-specific time operations.
 * Each platform must provide an actual implementation.
 */
expect class TimeProvider() {
    
    /**
     * Gets the current time in seconds since Unix epoch (1970-01-01T00:00:00Z)
     * 
     * @return Current time in seconds
     */
    fun currentTimeSeconds(): Long
}
