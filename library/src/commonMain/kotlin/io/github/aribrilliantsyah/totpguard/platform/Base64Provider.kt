package io.github.aribrilliantsyah.totpguard.platform

/**
 * Platform-specific Base64 encoding/decoding.
 * Each platform must provide an actual implementation.
 */
expect class Base64Provider() {
    
    /**
     * Encodes bytes to Base64 string
     * 
     * @param data The bytes to encode
     * @return Base64-encoded string
     */
    fun encode(data: ByteArray): String
    
    /**
     * Decodes Base64 string to bytes
     * 
     * @param data The Base64 string to decode
     * @return Decoded bytes
     */
    fun decode(data: String): ByteArray
}
