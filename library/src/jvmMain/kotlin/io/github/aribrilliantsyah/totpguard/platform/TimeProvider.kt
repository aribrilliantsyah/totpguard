package io.github.aribrilliantsyah.totpguard.platform

import java.time.Instant

/**
 * JVM implementation of time operations using java.time.Instant
 */
actual class TimeProvider {
    
    actual fun currentTimeSeconds(): Long {
        return Instant.now().epochSecond
    }
}
