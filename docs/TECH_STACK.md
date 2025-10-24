# Technology Stack

Detailed documentation of technologies, libraries, and architecture decisions used in TOTP-GUARD.

## Table of Contents

- [Platform Support](#platform-support)
- [Dependencies](#dependencies)
- [Architecture Patterns](#architecture-patterns)
- [Migration Notes](#migration-notes)
- [Performance Characteristics](#performance-characteristics)
- [Security Considerations](#security-considerations)

---

## Platform Support

### Supported Platforms

| Platform | Status | Notes |
|----------|--------|-------|
| **JVM** | ✅ Full Support | Java 11+ |
| **iOS** | ✅ Full Support | iosX64, iosArm64, iosSimulatorArm64 |
| **Android** | 🔄 Coming Soon | Native support planned |
| **JS/Browser** | 🔄 Coming Soon | Future release |

### Platform-Specific Implementations

The library uses Kotlin Multiplatform's **expect/actual** pattern for platform abstraction:

```
library/src/
├── commonMain/          # Shared code
│   ├── kotlin/
│   │   ├── TotpGuard.kt           # Main API
│   │   ├── auth/                  # TOTP generation
│   │   ├── crypto/                # Encryption
│   │   └── qr/                    # QR code generation
│   └── resources/
│
├── jvmMain/            # JVM-specific implementations
│   └── kotlin/
│       └── platform/
│           ├── CryptoProvider.kt   # Uses javax.crypto
│           ├── TimeProvider.kt     # Uses System.currentTimeMillis()
│           ├── Base64Provider.kt   # Uses java.util.Base64
│           └── QrCodeProvider.kt   # Uses ZXing library
│
└── iosMain/            # iOS-specific implementations
    └── kotlin/
        └── platform/
            ├── CryptoProvider.kt   # Uses cryptography-kotlin
            ├── TimeProvider.kt     # Uses Foundation.NSDate
            ├── Base64Provider.kt   # Uses platform.posix
            └── QrCodeProvider.kt   # Uses CoreImage/CoreGraphics
```

---

## Dependencies

### Core Dependencies

#### Kotlin Multiplatform
```kotlin
kotlin {
    version = "2.2.0"
}
```

**Why:** 
- Latest stable Kotlin version
- Improved KMP support
- Better type inference

#### JVM Platform

```kotlin
dependencies {
    // TOTP & Crypto (Built-in)
    // javax.crypto.Mac
    // javax.crypto.spec.SecretKeySpec
    // java.security.SecureRandom
    
    // QR Code Generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
}
```

**ZXing (Zebra Crossing):**
- Industry-standard QR code library
- Mature, well-tested
- Supports multiple barcode formats
- Active maintenance

#### iOS Platform

```kotlin
dependencies {
    // Cryptography
    implementation("dev.whyoleg.cryptography:cryptography-core:0.5.0")
    implementation("dev.whyoleg.cryptography:cryptography-provider-apple:0.5.0")
}
```

**cryptography-kotlin:**
- Pure Kotlin cryptography for KMP
- Uses native iOS CommonCrypto
- No external dependencies
- Type-safe API

**Native iOS APIs:**
```kotlin
import platform.Foundation.*          // NSDate, NSData
import platform.CoreImage.*          // CIImage, CIFilter, CIContext
import platform.CoreGraphics.*       // CGImage, CGContext
import platform.UIKit.*              // UIImage
import platform.posix.*              // memcpy for memory operations
```

---

## Architecture Patterns

### 1. Expect/Actual Pattern

**Common Interface (expect):**
```kotlin
// commonMain
expect class CryptoProvider() {
    fun generateSecureRandom(length: Int): ByteArray
    fun generateAesKey(): ByteArray
    fun hmacSha(algorithm: TotpAlgorithm, key: ByteArray, message: ByteArray): ByteArray
}
```

**JVM Implementation (actual):**
```kotlin
// jvmMain
actual class CryptoProvider {
    actual fun generateSecureRandom(length: Int): ByteArray {
        val random = SecureRandom()
        return ByteArray(length).apply { random.nextBytes(this) }
    }
    
    actual fun generateAesKey(): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey().encoded
    }
    
    actual fun hmacSha(
        algorithm: TotpAlgorithm,
        key: ByteArray,
        message: ByteArray
    ): ByteArray {
        val algorithmName = when (algorithm) {
            TotpAlgorithm.SHA1 -> "HmacSHA1"
            TotpAlgorithm.SHA256 -> "HmacSHA256"
            TotpAlgorithm.SHA512 -> "HmacSHA512"
        }
        
        val mac = Mac.getInstance(algorithmName)
        mac.init(SecretKeySpec(key, algorithmName))
        return mac.doFinal(message)
    }
}
```

**iOS Implementation (actual):**
```kotlin
// iosMain
@OptIn(ExperimentalForeignApi::class)
actual class CryptoProvider {
    actual fun generateSecureRandom(length: Int): ByteArray {
        return ByteArray(length).apply {
            usePinned { pinned ->
                SecRandomCopyBytes(
                    kSecRandomDefault,
                    length.toULong(),
                    pinned.addressOf(0)
                )
            }
        }
    }
    
    actual fun generateAesKey(): ByteArray = generateSecureRandom(32)
    
    actual fun hmacSha(
        algorithm: TotpAlgorithm,
        key: ByteArray,
        message: ByteArray
    ): ByteArray {
        val provider = CryptographyProvider.Default
        val digest = when (algorithm) {
            TotpAlgorithm.SHA1 -> SHA1
            TotpAlgorithm.SHA256 -> SHA256
            TotpAlgorithm.SHA512 -> SHA512
        }
        
        val hmac = provider.get(HMAC).hasher(digest)
        val keySpec = hmac.keyDerivation(key.asByteString())
        return hmac.createSignatureFunction(keySpec)
            .generateSignature(message.asByteString())
            .toByteArray()
    }
}
```

### 2. Singleton Object Pattern

```kotlin
object TotpGuard {
    private val cryptoProvider = CryptoProvider()
    
    fun generateTotpSecret(length: Int = 32): String {
        val random = cryptoProvider.generateSecureRandom(length)
        return Base32Encoder.encode(random)
    }
    
    // More functions...
}
```

**Benefits:**
- Single entry point
- Lazy initialization
- Thread-safe (Kotlin handles this)
- Easy to test/mock

### 3. Data Classes for Models

```kotlin
data class EncryptionResult(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val authTag: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptionResult) return false
        
        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!authTag.contentEquals(other.authTag)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + authTag.contentHashCode()
        return result
    }
}
```

**Benefits:**
- Immutable by default
- Auto-generated copy(), equals(), hashCode()
- Type-safe
- Serialization-friendly

### 4. Extension Functions

```kotlin
// EncryptionResultExtensions.kt
fun EncryptionResult.toBase64String(): String {
    val json = """
        {
            "ciphertext": "${Base64.getEncoder().encodeToString(ciphertext)}",
            "iv": "${Base64.getEncoder().encodeToString(iv)}",
            "authTag": "${Base64.getEncoder().encodeToString(authTag)}"
        }
    """.trimIndent()
    return Base64.getEncoder().encodeToString(json.toByteArray())
}

fun EncryptionResult.Companion.fromBase64String(str: String): EncryptionResult {
    val json = String(Base64.getDecoder().decode(str))
    // Parse JSON and reconstruct EncryptionResult
    // ...
}
```

**Benefits:**
- Clean API
- No subclassing needed
- Keeps data classes simple

---

## Migration Notes

### From CommonMain iOS (Removed)

**Previous (BROKEN):**
```kotlin
// ❌ This doesn't work on iOS!
import kotlin.system.currentTimeMillis

fun getCurrentTime(): Long {
    return currentTimeMillis()  // Unresolved reference on iOS
}
```

**Current (WORKING):**
```kotlin
// commonMain
expect class TimeProvider() {
    fun getCurrentTimeMillis(): Long
}

// iosMain
actual class TimeProvider {
    actual fun getCurrentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}

// jvmMain
actual class TimeProvider {
    actual fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
```

### Type Mismatches Fixed

**Previous (BROKEN on iOS):**
```kotlin
// iOS uses ULong for many C APIs
SecRandomCopyBytes(kSecRandomDefault, length, buffer)  // ❌ Type mismatch
```

**Current (WORKING):**
```kotlin
SecRandomCopyBytes(
    kSecRandomDefault,
    length.toULong(),  // ✅ Convert to ULong
    pinned.addressOf(0)
)
```

### Missing Imports Added

**Previous:**
```kotlin
// ❌ Missing import
CGAffineTransformMakeScale(scale, scale)  // Unresolved reference
```

**Current:**
```kotlin
import platform.CoreGraphics.CGAffineTransformMakeScale

// ✅ Works now
CGAffineTransformMakeScale(scale, scale)
```

### @OptIn Annotations

**Previous:**
```kotlin
// ❌ Warning: Experimental API usage
fun doSomething() {
    usePinned { ... }
}
```

**Current:**
```kotlin
@OptIn(ExperimentalForeignApi::class)
fun doSomething() {
    usePinned { ... }  // ✅ No warning
}
```

---

## Performance Characteristics

### Time Complexity

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| `generateTotpSecret()` | O(n) | n = secret length |
| `generateTotpCode()` | O(1) | Constant time HMAC |
| `verifyTotpCode()` | O(w) | w = time window (typically 3-5) |
| `encrypt()` | O(n) | n = plaintext length |
| `decrypt()` | O(n) | n = ciphertext length |
| `generateQrCode()` | O(n²) | n = data length (QR complexity) |
| `generateBackupCodes()` | O(m) | m = number of codes |
| `verifyBackupCode()` | O(m) | m = stored codes (consider indexing) |

### Memory Usage

| Operation | Memory | Notes |
|-----------|--------|-------|
| Secret Generation | ~32-64 bytes | Configurable |
| QR Code (300px) | ~50-100 KB | PNG format |
| Encryption Overhead | +28 bytes | IV (12) + AuthTag (16) |
| Backup Codes (10x8) | ~1 KB | Hashed versions |

### Recommendations

**For High-Traffic Applications:**

```kotlin
// ✅ GOOD: Cache encryption key
class TotpService(keyBase64: String) {
    private val encryptionKey = Base64.getDecoder().decode(keyBase64)
    
    fun verifyCode(encrypted: EncryptionResult, code: String): Boolean {
        val secret = TotpGuard.decrypt(encrypted, encryptionKey)
        return TotpGuard.verifyTotpCode(secret, code).isValid
    }
}

// ❌ BAD: Decode key every time
fun verifyCode(encrypted: EncryptionResult, code: String, keyBase64: String): Boolean {
    val key = Base64.getDecoder().decode(keyBase64)  // Wasteful!
    val secret = TotpGuard.decrypt(encrypted, key)
    return TotpGuard.verifyTotpCode(secret, code).isValid
}
```

---

## Security Considerations

### Cryptographic Algorithms

#### TOTP (RFC 6238)

```
Code = HOTP(K, T)
     = Truncate(HMAC-SHA(K, T))

Where:
- K = Secret key (Base32-decoded)
- T = (CurrentTime - T0) / TimeStep
- HMAC-SHA = HMAC-SHA1/256/512
- Truncate = Dynamic truncation per RFC
```

**Default Settings:**
- Algorithm: SHA1 (RFC 6238 standard)
- Digits: 6
- Period: 30 seconds
- Time Window: ±1 period (90 seconds total validity)

#### Encryption (AES-256-GCM)

```
Ciphertext, AuthTag = AES-256-GCM-Encrypt(Plaintext, Key, IV)

Where:
- Key = 256-bit (32 bytes) random key
- IV = 96-bit (12 bytes) random nonce
- AuthTag = 128-bit (16 bytes) authentication tag
- Mode = GCM (Galois/Counter Mode)
```

**Benefits:**
- Authenticated encryption (prevents tampering)
- Industry standard (NIST approved)
- Fast performance
- Parallel encryption/decryption

### Secure Random Number Generation

**JVM:**
```kotlin
SecureRandom()  // Uses /dev/urandom on Unix, CryptGenRandom on Windows
```

**iOS:**
```kotlin
SecRandomCopyBytes()  // Uses Apple's Secure Enclave when available
```

### Key Management Best Practices

```kotlin
// ✅ GOOD: Environment variable
val key = System.getenv("TOTP_ENCRYPTION_KEY")

// ✅ GOOD: Secrets manager
val key = awsSecretsManager.getSecretValue("totp-encryption-key")

// ✅ GOOD: Keystore (Android/iOS)
val key = keyStore.getKey("totp-encryption-key")

// ❌ BAD: Hardcoded
val key = "my-secret-key-123"

// ❌ BAD: Committed to git
val key = config.properties["key"]  // if in git
```

### Rate Limiting

```kotlin
// Implement rate limiting to prevent brute force
class TotpRateLimiter {
    private val attempts = ConcurrentHashMap<String, AtomicInteger>()
    
    fun checkRateLimit(userId: String): Boolean {
        val count = attempts.computeIfAbsent(userId) { AtomicInteger(0) }
        return count.incrementAndGet() <= 5  // Max 5 attempts per period
    }
}
```

### Audit Logging

```kotlin
// Log TOTP operations for security auditing
logger.info("TOTP_SETUP: userId=$userId, timestamp=$timestamp")
logger.info("TOTP_VERIFY: userId=$userId, success=$isValid, timestamp=$timestamp")
logger.warn("TOTP_FAILED: userId=$userId, attempts=$count, timestamp=$timestamp")
```

---

## Build Configuration

### Gradle Version Catalog

**gradle/libs.versions.toml:**
```toml
[versions]
kotlin = "2.2.0"
zxing = "3.5.3"
cryptography = "0.5.0"

[libraries]
zxing-core = { module = "com.google.zxing:core", version.ref = "zxing" }
zxing-javase = { module = "com.google.zxing:javase", version.ref = "zxing" }
cryptography-core = { module = "dev.whyoleg.cryptography:cryptography-core", version.ref = "cryptography" }
cryptography-provider-apple = { module = "dev.whyoleg.cryptography:cryptography-provider-apple", version.ref = "cryptography" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

### Library Build Configuration

**library/build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    `maven-publish`
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Common dependencies
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(libs.zxing.core)
                implementation(libs.zxing.javase)
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.cryptography.core)
                implementation(libs.cryptography.provider.apple)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.aribrilliantsyah"
            artifactId = "totpguard"
            version = "0.0.1-beta"
        }
    }
}
```

---

## Future Enhancements

### Planned Features

1. **Android Native Support**
   - Native Android Keystore integration
   - Biometric authentication support

2. **JavaScript/Browser Support**
   - Web Crypto API usage
   - Browser extension support

3. **Additional Algorithms**
   - HOTP (counter-based OTP)
   - OCRA (challenge-response)

4. **Enhanced Security**
   - Hardware security module (HSM) support
   - FIPS 140-2 compliance

---

## Version History

| Version | Release Date | Key Changes |
|---------|-------------|-------------|
| 0.0.1-beta | TBD | Initial release with JVM + iOS support |

---

## References

- [RFC 6238 - TOTP](https://tools.ietf.org/html/rfc6238)
- [RFC 4226 - HOTP](https://tools.ietf.org/html/rfc4226)
- [NIST SP 800-38D - GCM](https://csrc.nist.gov/publications/detail/sp/800-38d/final)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [ZXing Library](https://github.com/zxing/zxing)
- [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin)

---

Need help? [Open an issue](https://github.com/aribrilliantsyah/totpguard/issues)
