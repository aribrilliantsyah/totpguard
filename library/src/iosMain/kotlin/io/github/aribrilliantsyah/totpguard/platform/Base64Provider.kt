package io.github.aribrilliantsyah.totpguard.platform

import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * iOS implementation of Base64 encoding/decoding using Foundation framework
 */
@OptIn(ExperimentalForeignApi::class)
actual class Base64Provider {
    
    actual fun encode(data: ByteArray): String {
        val nsData = data.toNSData()
        return nsData.base64EncodedStringWithOptions(0)
    }
    
    actual fun decode(data: String): ByteArray {
        val nsData = NSData.create(base64EncodedString = data, options = 0)
            ?: throw IllegalArgumentException("Invalid Base64 string")
        return nsData.toByteArray()
    }
}

// Extension functions for data conversion
private fun ByteArray.toNSData(): NSData {
    if (this.isEmpty()) return NSData()
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}

private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    if (size == 0) return ByteArray(0)
    
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, size.toULong())
    }
    return bytes
}
