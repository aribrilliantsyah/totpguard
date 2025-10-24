package io.github.aribrilliantsyah.totpguard.platform

import java.util.Base64

/**
 * JVM implementation of Base64 encoding/decoding using java.util.Base64
 */
actual class Base64Provider {
    
    actual fun encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }
    
    actual fun decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }
}
