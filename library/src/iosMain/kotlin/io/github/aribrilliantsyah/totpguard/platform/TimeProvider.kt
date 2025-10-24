package io.github.aribrilliantsyah.totpguard.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of time operations using Foundation framework
 */
actual class TimeProvider {
    
    actual fun currentTimeSeconds(): Long {
        return NSDate().timeIntervalSince1970.toLong()
    }
}
