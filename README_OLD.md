# TOTP-GUARD

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.aribrilliantsyah/totp-guard.svg)](https://search.maven.org/artifact/io.github.aribrilliantsyah/totp-guard)
[![Version](https://img.shields.io/badge/version-0.0.1--beta-orange)](https://github.com/aribrilliantsyah/totpguard/releases)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)

TOTP-GUARD adalah library Kotlin Multiplatform (KMP) untuk autentikasi TOTP (Time-based One-Time Password), enkripsi AES-256-GCM, dan pembuatan QR code. Library ini dirancang untuk mudah digunakan dalam aplikasi JVM (Spring Boot, Java), Android, dan iOS.

## ✨ Fitur Utama

- 🔐 **TOTP Authentication** - RFC 6238 compliant dengan support SHA1/SHA256/SHA512
- 🔒 **AES-256-GCM Encryption** - Enkripsi aman untuk penyimpanan secret
- 📱 **QR Code Generation** - Generate QR code untuk authenticator apps
- 🌐 **Multiplatform** - JVM, iOS, Android (coming soon)
- ⚡ **Simple API** - Parameter minimal dengan `@JvmOverloads` support
- 🛡️ **Platform Native** - JVM uses `javax.crypto`, iOS uses cryptography-kotlin

## 🚀 Quick Start

### Installation

#### Maven Local (Development)

```bash
# Clone and build
git clone https://github.com/aribrilliantsyah/totpguard.git
cd totpguard/kotlin-totp-lib
./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

#### Add Dependency

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
}
```

**Maven**
```xml
<dependency>
    <groupId>io.github.aribrilliantsyah</groupId>
    <artifactId>totpguard-jvm</artifactId>
    <version>0.0.1-beta</version>
</dependency>
```

### Basic Usage

#### Kotlin

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard

fun main() {
    // Generate secret
    val secret = TotpGuard.generateTotpSecret()
    
    // Generate TOTP code
    val code = TotpGuard.generateTotpCode(secret)
    println("Current code: $code")
    
    // Verify code
    val isValid = TotpGuard.verifyTotpCode(secret, code).isValid
    println("Valid: $isValid")
    
    // Generate QR code for authenticator app
    val uri = TotpGuard.generateOtpAuthUri(
        secret = secret,
        accountName = "user@example.com",
        issuer = "MyApp"
    )
    val qrBase64 = TotpGuard.generateQrCodeBase64(uri)
    println("QR Code generated!")
}
```

#### Java

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;

public class Main {
    public static void main(String[] args) {
        TotpGuard totp = TotpGuard.INSTANCE;
        
        // Generate secret
        String secret = totp.generateTotpSecret();
        
        // Generate TOTP code
        String code = totp.generateTotpCode(secret);
        System.out.println("Current code: " + code);
        
        // Verify code
        boolean isValid = totp.verifyTotpCode(secret, code).isValid();
        System.out.println("Valid: " + isValid);
        
        // Generate QR code
        String uri = totp.generateOtpAuthUri(secret, "user@example.com", "MyApp");
        String qrBase64 = totp.generateQrCodeBase64(uri);
        System.out.println("QR Code generated!");
    }
}
```

## 📚 Documentation

### 📖 Complete Guides

- **[Installation Guide](docs/INSTALLATION.md)** - Detailed installation instructions
  - Maven Local setup
  - Gradle/Maven configuration
  - Troubleshooting

- **[Usage Examples](USAGE_EXAMPLES.md)** - Complete usage examples
  - Default vs custom parameters
  - Java and Kotlin examples
  - Platform-specific notes

- **[API Reference](docs/API_REFERENCE.md)** - Complete API documentation
  - All functions with signatures
  - Parameter descriptions
  - Return types and examples

- **[Spring Boot Integration](docs/SPRING_BOOT_GUIDE.md)** - Spring Boot integration
  - Service implementation
  - Controller examples
  - Database storage

### 🏗️ Architecture

- **[Architecture Overview](ARCHITECTURE.md)** - Multiplatform architecture
  - Mermaid diagrams
  - expect/actual pattern
  - Platform implementations

- **[Technology Stack](docs/TECH_STACK.md)** - Dependencies and versions
  - Per-platform dependencies
  - Crypto strategy
  - Migration notes

### 🔧 Platform-Specific

- **[iOS Build Fix](IOS_BUILD_FIX.md)** - Fix iOS compilation errors
  - Legacy file removal
  - Platform APIs
  - Testing on Mac

- **[JVM Overloads](JVMOVERLOADS_TEST.md)** - @JvmOverloads testing
  - Java parameter overloading
  - Test examples
  - Before/after comparison

### 📋 Project Info

- **[Changelog](CHANGELOG.md)** - Version history and changes
- **[Quick Start (Mac)](QUICKSTART_MAC.md)** - Mac-specific quick start

## 🎯 Key Features

### Simple API with Default Parameters

Thanks to `@JvmOverloads`, you can use minimal parameters:

```java
// Java - Simple!
String code = totp.generateTotpCode(secret);  // Uses SHA1, 6 digits, 30s period

// Or customize what you need
String code = totp.generateTotpCode(secret, TotpAlgorithm.SHA256);
String code = totp.generateTotpCode(secret, TotpAlgorithm.SHA256, 8);
```

### Platform Native Crypto

- **JVM**: Uses standard `javax.crypto` (zero external dependencies)
- **iOS**: Uses `cryptography-kotlin` with Apple Security Framework
- **Android**: Will use `cryptography-kotlin` with JDK Provider

### Type-Safe API

```kotlin
// Kotlin with type safety
val result: TotpVerificationResult = TotpGuard.verifyTotpCode(secret, code)
if (result.isValid) {
    println("Time offset: ${result.timeOffset}")
}
```

## 🔒 Security Best Practices

1. **Encrypt secrets** before storing in database
2. **Use HTTPS** for all authentication endpoints  
3. **Implement rate limiting** on verification endpoints
4. **Store encryption keys** securely (KeyStore, Vault, etc.)
5. **Never log secrets** or codes in production

See [API Reference](docs/API_REFERENCE.md) for secure implementation patterns.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

## 📄 License

This library is licensed under Apache License 2.0 - see [LICENSE](LICENSE) for details.

## 🔗 Links

- [GitHub Repository](https://github.com/aribrilliantsyah/totpguard)
- [Issue Tracker](https://github.com/aribrilliantsyah/totpguard/issues)
- [Maven Central](https://search.maven.org/artifact/io.github.aribrilliantsyah/totp-guard) (coming soon)

---

Made with ❤️ by [Ari Brilliant Syah](https://github.com/aribrilliantsyah)
