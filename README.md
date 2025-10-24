# TOTP-GUARD# TOTP-GUARD



> **Secure, Cross-Platform TOTP (Time-based One-Time Password) Authentication Library**[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aribrilliantsyah/totp-guard.svg)](https://search.maven.org/artifact/io.github.aribrilliantsyah/totp-guard)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg)](https://kotlinlang.org)[![Version](https://img.shields.io/badge/version-0.0.1--beta-orange)](https://github.com/aribrilliantsyah/totpguard/releases)

[![Platforms](https://img.shields.io/badge/platforms-JVM%20%7C%20iOS-green.svg)](https://kotlinlang.org/docs/multiplatform.html)[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)

[![License](https://img.shields.io/badge/license-MIT-orange.svg)](LICENSE)[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)



A production-ready Kotlin Multiplatform library for implementing Two-Factor Authentication (2FA) using TOTP (RFC 6238). Supports JVM and iOS with Android and JavaScript coming soon.TOTP-GUARD adalah library Kotlin Multiplatform (KMP) untuk autentikasi TOTP (Time-based One-Time Password), enkripsi AES-256-GCM, dan pembuatan QR code. Library ini dirancang untuk mudah digunakan dalam aplikasi JVM (Spring Boot, Java), Android, dan iOS.



---## ✨ Fitur Utama



## ✨ Key Features- 🔐 **TOTP Authentication** - RFC 6238 compliant dengan support SHA1/SHA256/SHA512

- 🔒 **AES-256-GCM Encryption** - Enkripsi aman untuk penyimpanan secret

- 🔐 **RFC 6238 Compliant** - Industry-standard TOTP implementation- 📱 **QR Code Generation** - Generate QR code untuk authenticator apps

- 🔒 **AES-256-GCM Encryption** - Secure secret storage- 🌐 **Multiplatform** - JVM, iOS, Android (coming soon)

- 📱 **QR Code Generation** - Easy authenticator app setup- ⚡ **Simple API** - Parameter minimal dengan `@JvmOverloads` support

- 🎫 **Backup Codes** - Account recovery mechanism- 🛡️ **Platform Native** - JVM uses `javax.crypto`, iOS uses cryptography-kotlin

- 🌍 **Cross-Platform** - JVM and iOS support

- 🚀 **Simple API** - Clean, intuitive interface with default parameters## 🚀 Quick Start

- ⚡ **Zero Dependencies** - Self-contained on iOS, minimal on JVM

### Installation

---

#### Maven Local (Development)

## 🚀 Quick Start

```bash

### Java Example# Clone and build

git clone https://github.com/aribrilliantsyah/totpguard.git

```javacd totpguard/kotlin-totp-lib

import io.github.aribrilliantsyah.totpguard.TotpGuard;./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false

```

public class Example {

    public static void main(String[] args) {#### Add Dependency

        TotpGuard totp = TotpGuard.INSTANCE;

        **Gradle (Kotlin DSL)**

        // 1. Generate secret (once per user)```kotlin

        String secret = totp.generateTotpSecret();repositories {

            mavenLocal()

        // 2. Generate QR code for user to scan    mavenCentral()

        String uri = totp.generateOtpAuthUri(secret, "user@example.com", "MyApp");}

        String qrBase64 = totp.generateQrCodeBase64(uri);

        dependencies {

        // 3. Verify user's code - SIMPLE! Just 2 parameters    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")

        String userCode = "123456";}

        boolean isValid = totp.verifyTotpCode(secret, userCode).isValid();```

        

        System.out.println("Valid: " + isValid);**Maven**

    }```xml

}<dependency>

```    <groupId>io.github.aribrilliantsyah</groupId>

    <artifactId>totpguard-jvm</artifactId>

### Kotlin Example    <version>0.0.1-beta</version>

</dependency>

```kotlin```

import io.github.aribrilliantsyah.totpguard.TotpGuard

### Basic Usage

fun main() {

    // 1. Generate secret#### Kotlin

    val secret = TotpGuard.generateTotpSecret()

    ```kotlin

    // 2. Generate QR codeimport io.github.aribrilliantsyah.totpguard.TotpGuard

    val uri = TotpGuard.generateOtpAuthUri(

        secret = secret,fun main() {

        accountName = "user@example.com",    // Generate secret

        issuer = "MyApp"    val secret = TotpGuard.generateTotpSecret()

    )    

    val qrBase64 = TotpGuard.generateQrCodeBase64(uri)    // Generate TOTP code

        val code = TotpGuard.generateTotpCode(secret)

    // 3. Verify code - SIMPLE! Just 2 parameters    println("Current code: $code")

    val userCode = "123456"    

    val isValid = TotpGuard.verifyTotpCode(secret, userCode).isValid    // Verify code

        val isValid = TotpGuard.verifyTotpCode(secret, code).isValid

    println("Valid: $isValid")    println("Valid: $isValid")

}    

```    // Generate QR code for authenticator app

    val uri = TotpGuard.generateOtpAuthUri(

---        secret = secret,

        accountName = "user@example.com",

## 📦 Installation        issuer = "MyApp"

    )

### Maven Local (Development)    val qrBase64 = TotpGuard.generateQrCodeBase64(uri)

    println("QR Code generated!")

```bash}

# 1. Clone and build```

git clone https://github.com/aribrilliantsyah/totpguard.git

cd totpguard/kotlin-totp-lib#### Java

./gradlew clean build

```java

# 2. Publish to Maven Localimport io.github.aribrilliantsyah.totpguard.TotpGuard;

./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false

```public class Main {

    public static void main(String[] args) {

### Add to Your Project        TotpGuard totp = TotpGuard.INSTANCE;

        

**Gradle (Kotlin DSL):**        // Generate secret

```kotlin        String secret = totp.generateTotpSecret();

repositories {        

    mavenLocal()  // Must be first!        // Generate TOTP code

    mavenCentral()        String code = totp.generateTotpCode(secret);

}        System.out.println("Current code: " + code);

        

dependencies {        // Verify code

    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")        boolean isValid = totp.verifyTotpCode(secret, code).isValid();

}        System.out.println("Valid: " + isValid);

```        

        // Generate QR code

**Maven:**        String uri = totp.generateOtpAuthUri(secret, "user@example.com", "MyApp");

```xml        String qrBase64 = totp.generateQrCodeBase64(uri);

<dependency>        System.out.println("QR Code generated!");

    <groupId>io.github.aribrilliantsyah</groupId>    }

    <artifactId>totpguard-jvm</artifactId>}

    <version>0.0.1-beta</version>```

</dependency>

```## 📚 Documentation



📖 **Detailed installation guide:** [docs/INSTALLATION.md](docs/INSTALLATION.md)### 📖 Complete Guides



---- **[Installation Guide](docs/INSTALLATION.md)** - Detailed installation instructions

  - Maven Local setup

## 📚 Documentation  - Gradle/Maven configuration

  - Troubleshooting

| Document | Description |

|----------|-------------|- **[Usage Examples](USAGE_EXAMPLES.md)** - Complete usage examples

| **[Installation Guide](docs/INSTALLATION.md)** | Complete setup instructions for all platforms |  - Default vs custom parameters

| **[API Reference](docs/API_REFERENCE.md)** | Full API documentation with examples |  - Java and Kotlin examples

| **[Spring Boot Guide](docs/SPRING_BOOT_GUIDE.md)** | Integration with Spring Boot applications |  - Platform-specific notes

| **[Tech Stack](docs/TECH_STACK.md)** | Dependencies, architecture, and migrations |

| **[Usage Examples](USAGE_EXAMPLES.md)** | Complete code examples |- **[API Reference](docs/API_REFERENCE.md)** - Complete API documentation

| **[Architecture](ARCHITECTURE.md)** | System design and patterns |  - All functions with signatures

| **[iOS Build Fix](IOS_BUILD_FIX.md)** | iOS compilation troubleshooting |  - Parameter descriptions

| **[Changelog](CHANGELOG.md)** | Version history |  - Return types and examples



---- **[Spring Boot Integration](docs/SPRING_BOOT_GUIDE.md)** - Spring Boot integration

  - Service implementation

## 🔧 Core API  - Controller examples

  - Database storage

### TOTP Functions

### 🏗️ Architecture

```kotlin

// Generate secret (once per user)- **[Architecture Overview](ARCHITECTURE.md)** - Multiplatform architecture

val secret = TotpGuard.generateTotpSecret()  - Mermaid diagrams

  - expect/actual pattern

// Generate TOTP code  - Platform implementations

val code = TotpGuard.generateTotpCode(secret)

- **[Technology Stack](docs/TECH_STACK.md)** - Dependencies and versions

// Verify code (with time window tolerance)  - Per-platform dependencies

val result = TotpGuard.verifyTotpCode(secret, userCode)  - Crypto strategy

if (result.isValid) { /* Grant access */ }  - Migration notes



// Get remaining seconds for current code### 🔧 Platform-Specific

val remaining = TotpGuard.getRemainingSeconds()

```- **[iOS Build Fix](IOS_BUILD_FIX.md)** - Fix iOS compilation errors

  - Legacy file removal

### Encryption Functions  - Platform APIs

  - Testing on Mac

```kotlin

// Generate encryption key- **[JVM Overloads](JVMOVERLOADS_TEST.md)** - @JvmOverloads testing

val key = TotpGuard.generateEncryptionKey()  - Java parameter overloading

  - Test examples

// Encrypt secret for storage  - Before/after comparison

val encrypted = TotpGuard.encrypt(secret, key)

### 📋 Project Info

// Decrypt when needed

val decrypted = TotpGuard.decrypt(encrypted, key)- **[Changelog](CHANGELOG.md)** - Version history and changes

- **[Quick Start (Mac)](QUICKSTART_MAC.md)** - Mac-specific quick start

// Rotate encryption key

val reencrypted = TotpGuard.rotateKey(encrypted, oldKey, newKey)## 🎯 Key Features

```

### Simple API with Default Parameters

### QR Code Functions

Thanks to `@JvmOverloads`, you can use minimal parameters:

```kotlin

// Generate otpauth:// URI```java

val uri = TotpGuard.generateOtpAuthUri(// Java - Simple!

    secret = secret,String code = totp.generateTotpCode(secret);  // Uses SHA1, 6 digits, 30s period

    accountName = "user@example.com",

    issuer = "MyApp"// Or customize what you need

)String code = totp.generateTotpCode(secret, TotpAlgorithm.SHA256);

String code = totp.generateTotpCode(secret, TotpAlgorithm.SHA256, 8);

// Generate QR code as Base64 PNG```

val qrBase64 = TotpGuard.generateQrCodeBase64(uri, size = 300)

### Platform Native Crypto

// Or as byte array

val qrBytes = TotpGuard.generateQrCodePng(uri, size = 300)- **JVM**: Uses standard `javax.crypto` (zero external dependencies)

```- **iOS**: Uses `cryptography-kotlin` with Apple Security Framework

- **Android**: Will use `cryptography-kotlin` with JDK Provider

### Backup Code Functions

### Type-Safe API

```kotlin

// Generate backup codes```kotlin

val backupCodes = TotpGuard.generateBackupCodes(count = 10, length = 8)// Kotlin with type safety

val result: TotpVerificationResult = TotpGuard.verifyTotpCode(secret, code)

// Show to user (ONCE ONLY!)if (result.isValid) {

backupCodes.formattedCodes.forEach { println(it) }    println("Time offset: ${result.timeOffset}")

}

// Store hashed versions```

database.save(backupCodes.hashedCodes)

## 🔒 Security Best Practices

// Verify backup code

val result = TotpGuard.verifyBackupCode(userCode, storedHashedCodes)1. **Encrypt secrets** before storing in database

if (result.isValid) {2. **Use HTTPS** for all authentication endpoints  

    // Remove used code3. **Implement rate limiting** on verification endpoints

    database.remove(result.matchedHash!!)4. **Store encryption keys** securely (KeyStore, Vault, etc.)

}5. **Never log secrets** or codes in production

```

See [API Reference](docs/API_REFERENCE.md) for secure implementation patterns.

📖 **See [API Reference](docs/API_REFERENCE.md) for complete documentation**

## 🤝 Contributing

---

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

## 💡 Use Cases

## 📄 License

### Spring Boot Integration

This library is licensed under Apache License 2.0 - see [LICENSE](LICENSE) for details.

```kotlin

@Service## 🔗 Links

class TotpService(

    @Value("\${totp.encryption.key}") keyBase64: String- [GitHub Repository](https://github.com/aribrilliantsyah/totpguard)

) {- [Issue Tracker](https://github.com/aribrilliantsyah/totpguard/issues)

    private val encryptionKey = Base64.getDecoder().decode(keyBase64)- [Maven Central](https://search.maven.org/artifact/io.github.aribrilliantsyah/totp-guard) (coming soon)

    

    fun setupTotp(userId: Long, email: String): SetupResponse {---

        val secret = TotpGuard.generateTotpSecret()

        val encrypted = TotpGuard.encrypt(secret, encryptionKey)Made with ❤️ by [Ari Brilliant Syah](https://github.com/aribrilliantsyah)

        val backupCodes = TotpGuard.generateBackupCodes()
        
        // Save to database
        userRepository.saveTotpData(userId, encrypted, backupCodes.hashedCodes)
        
        // Generate QR code
        val uri = TotpGuard.generateOtpAuthUri(secret, email, "MyApp")
        val qrBase64 = TotpGuard.generateQrCodeBase64(uri)
        
        return SetupResponse(qrBase64, backupCodes.formattedCodes)
    }
    
    fun verifyCode(userId: Long, code: String): Boolean {
        val encrypted = userRepository.getTotpSecret(userId)
        val secret = TotpGuard.decrypt(encrypted, encryptionKey)
        
        return TotpGuard.verifyTotpCode(secret, code).isValid
    }
}
```

📖 **Full Spring Boot guide:** [docs/SPRING_BOOT_GUIDE.md](docs/SPRING_BOOT_GUIDE.md)

---

## 🔒 Security Best Practices

### 1. Secure Key Storage

```kotlin
// ✅ GOOD: Environment variable
val key = System.getenv("TOTP_ENCRYPTION_KEY")

// ✅ GOOD: Secrets manager (AWS, GCP, Azure)
val key = secretsManager.getSecret("totp-encryption-key")

// ❌ BAD: Hardcoded
val key = "my-secret-key-123"
```

### 2. Encrypted Secret Storage

```kotlin
// Always encrypt secrets before storing
val encrypted = TotpGuard.encrypt(secret, encryptionKey)
database.save("user_secret", encrypted.toBase64String())

// Decrypt only when verifying
val encrypted = EncryptionResult.fromBase64String(stored)
val secret = TotpGuard.decrypt(encrypted, encryptionKey)
```

### 3. Rate Limiting

```kotlin
// Limit verification attempts to prevent brute force
class RateLimiter {
    private val attempts = ConcurrentHashMap<String, AtomicInteger>()
    
    fun checkLimit(userId: String): Boolean {
        val count = attempts.computeIfAbsent(userId) { AtomicInteger(0) }
        return count.incrementAndGet() <= 5  // Max 5 attempts
    }
}
```

### 4. Time Window Configuration

```kotlin
// Use appropriate time window for your security requirements
val strictResult = TotpGuard.verifyTotpCode(
    secret = secret,
    code = userCode,
    timeWindow = 0  // Only current period (strict)
)

val lenientResult = TotpGuard.verifyTotpCode(
    secret = secret,
    code = userCode,
    timeWindow = 2  // ±2 periods (lenient, better UX)
)
```

### 5. Audit Logging

```kotlin
// Log all TOTP operations
logger.info("TOTP_SETUP: user=$userId")
logger.info("TOTP_VERIFY: user=$userId, success=$isValid")
logger.warn("TOTP_FAILED: user=$userId, attempts=$count")
```

---

## 🛠️ Technology Stack

| Component | JVM | iOS |
|-----------|-----|-----|
| **Crypto** | `javax.crypto` (built-in) | `cryptography-kotlin` 0.5.0 |
| **Time** | `System.currentTimeMillis()` | `Foundation.NSDate` |
| **Base64** | `java.util.Base64` | `platform.posix` |
| **QR Code** | ZXing 3.5.3 | CoreImage/CoreGraphics |
| **Random** | `SecureRandom` | `SecRandomCopyBytes` |

📖 **Detailed tech stack:** [docs/TECH_STACK.md](docs/TECH_STACK.md)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│        TotpGuard (Object)           │  ← Main API Entry Point
│  ┌──────────┬──────────┬─────────┐  │
│  │   TOTP   │  Crypto  │ QR Code │  │
│  └────┬─────┴────┬─────┴────┬────┘  │
└───────┼──────────┼──────────┼────────┘
        │          │          │
        ▼          ▼          ▼
┌───────────────────────────────────────┐
│     Platform Abstraction Layer        │
│      (expect/actual pattern)          │
└───────────┬───────────────────────────┘
            │
     ┌──────┴──────┐
     │             │
     ▼             ▼
┌─────────┐   ┌─────────┐
│   JVM   │   │   iOS   │
│  impl   │   │  impl   │
└─────────┘   └─────────┘
```

📖 **Full architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 🧪 Testing

### Run Tests

```bash
# All tests
./gradlew test

# JVM only
./gradlew jvmTest

# iOS only
./gradlew iosX64Test
```

### Test Coverage

```bash
# Generate coverage report
./gradlew jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- RFC 6238 (TOTP) specification
- ZXing library for QR code generation (JVM)
- cryptography-kotlin for iOS crypto
- Kotlin Multiplatform team

---

## 📞 Support

- 🐛 **Issues**: [GitHub Issues](https://github.com/aribrilliantsyah/totpguard/issues)
- 📧 **Email**: ari.brilliantsyah@gmail.com
- 💬 **Discussions**: [GitHub Discussions](https://github.com/aribrilliantsyah/totpguard/discussions)

---

## 🗺️ Roadmap

- [x] JVM support
- [x] iOS support
- [x] QR code generation
- [x] Backup codes
- [ ] Android native support
- [ ] JavaScript/Browser support
- [ ] Maven Central publication
- [ ] HOTP support
- [ ] Hardware security module (HSM) integration

---

<div align="center">

**Made with ❤️ using Kotlin Multiplatform**

[⭐ Star this repo](https://github.com/aribrilliantsyah/totpguard) if you find it helpful!

</div>
