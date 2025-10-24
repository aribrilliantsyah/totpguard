# TOTP-GUARD Architecture Documentation

## 📐 Arsitektur Multiplatform Bridge

Dokumen ini menjelaskan arsitektur internal library TOTP-GUARD yang menggunakan Kotlin Multiplatform dengan bridge pattern untuk menyediakan API yang konsisten di berbagai platform.

---

## 1. Overview Arsitektur

### High-Level Architecture

```mermaid
graph TB
    subgraph "Application Layer"
        A1[Spring Boot<br/>Java/Kotlin]
        A2[Android App<br/>Kotlin]
        A3[iOS App<br/>Swift/Kotlin]
    end

    subgraph "TOTP-GUARD Library"
        B1[TotpGuard<br/>Unified API]
        
        subgraph "Common Main - Business Logic"
            C1[TotpGenerator]
            C2[Encryption]
            C3[BackupCodeGenerator]
            C4[CryptoProvider<br/><i>cryptography-kotlin</i>]
        end
        
        subgraph "Platform Bridge - expect/actual"
            D1[QrCodeProvider]
            D2[Base64Provider]
            D4[TimeProvider]
        end
        
        subgraph "JVM Implementation"
            E1[ZXing QR]
            E2[Java Base64]
            E4[System Time]
        end
        
        subgraph "iOS Implementation"
            F1[CoreImage QR]
            F2[Foundation Base64]
            F4[Foundation Date]
        end
    end

    subgraph "Cryptography Backend"
        G1[cryptography-kotlin<br/>Unified Crypto API]
        G2[JDK Provider<br/>javax.crypto]
        G3[Apple Provider<br/>Security Framework]
    end

    A1 --> B1
    A2 --> B1
    A3 --> B1
    
    B1 --> C1
    B1 --> C2
    
    C1 --> C4
    C2 --> C4
    C1 --> D1
    C1 --> D2
    C1 --> D4
    C2 --> D1
    C2 --> D2
    C2 --> D4
    
    C4 --> G1
    G1 -.JVM.-> G2
    G1 -.iOS.-> G3
    
    D1 -.JVM.-> E1
    D2 -.JVM.-> E2
    D4 -.JVM.-> E4
    
    D1 -.iOS.-> F1
    D2 -.iOS.-> F2
    D4 -.iOS.-> F4

    style B1 fill:#4CAF50,color:#fff
    style C4 fill:#2196F3,color:#fff
    style G1 fill:#FF9800,color:#fff
    style A1 fill:#E8F5E9
    style A2 fill:#E8F5E9
    style A3 fill:#E8F5E9
```

---

## 2. Layer Architecture

### 2.1 Application Layer
Konsumen library dari berbagai platform:
- **Spring Boot**: Backend server (JVM)
- **Android**: Mobile app (JVM/Android)
- **iOS**: Mobile app (iOS/Native)

### 2.2 API Layer (`TotpGuard`)
- Single object singleton sebagai entry point
- API yang sama untuk semua platform
- Menyembunyikan kompleksitas implementasi

#### 3️⃣ **Common Main** (Business Logic)
- **TotpGenerator**: RFC 6238 TOTP implementation
- **Encryption**: AES-256-GCM encryption/decryption
- **CryptoProvider**: Unified cryptography operations

### 2.4 Platform Abstraction Layer (expect/actual)
Bridge untuk platform-specific implementations:
- **QrCodeProvider**: QR code generation
- **Base64Provider**: Base64 encoding/decoding
- **TimeProvider**: System time access

### 2.5 Platform Implementation Layer
Native implementations per platform:
- **JVM**: ZXing, Java stdlib
- **iOS**: CoreImage, Foundation

### 2.6 Cryptography Layer
Modern unified crypto using cryptography-kotlin:
- Automatic platform provider selection
- Consistent API across platforms
- Secure by default

---

## 3. Component Dependency Diagram

```mermaid
graph LR
    subgraph "Public API"
        TG[TotpGuard<br/>Object Singleton]
    end
    
    subgraph "Core Components - commonMain"
        TGen[TotpGenerator]
        Enc[Encryption]
    end
    
    subgraph "Crypto Layer"
        CP[CryptoProvider<br/>cryptography-kotlin]
    end
    
    subgraph "Platform Abstraction"
        QR[QrCodeProvider<br/>expect/actual]
        B64[Base64Provider<br/>expect/actual]
        Time[TimeProvider<br/>expect/actual]
    end
    
    subgraph "Data Models"
        M1[TotpVerificationResult]
        M2[EncryptionResult]
        M4[TotpAlgorithm]
    end

    TG --> TGen
    TG --> Enc
    
    TGen --> CP
    TGen --> Time
    TGen --> B64
    TGen --> M1
    TGen --> M4
    
    Enc --> CP
    Enc --> M2
    
    style TG fill:#4CAF50,color:#fff
    style CP fill:#2196F3,color:#fff
    style QR fill:#FF9800,color:#000
    style B64 fill:#FF9800,color:#000
    style Time fill:#FF9800,color:#000
```

---

## 4. Data Flow Diagrams

### 4.1 TOTP Setup Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant API as TotpGuard API
    participant Logic as Business Logic
    participant Crypto as CryptoProvider
    participant Bridge as Platform Bridge
    participant Native as Native Implementation

    Note over App,Native: Complete TOTP Setup Flow
    
    App->>API: 1. generateTotpSecret()
    API->>Logic: TotpGenerator.generate()
    Logic->>Crypto: generateSecureRandom(32)
    Crypto->>Native: CryptographyRandom.nextBytes()
    Native-->>Crypto: Random bytes
    Crypto-->>Logic: Base32 encoded secret
    Logic-->>API: Secret string
    API-->>App: "JBSWY3DPEHPK3PXP..."

    App->>API: 2. encrypt(secret, key)
    API->>Logic: Encryption.encrypt()
    Logic->>Crypto: aesGcmEncrypt(plaintext, key, iv)
    Crypto->>Native: AES.GCM encryption
    Native-->>Crypto: Ciphertext + AuthTag
    Crypto-->>Logic: EncryptionResult
    Logic-->>API: EncryptionResult
    API-->>App: {ciphertext, iv, authTag}

    App->>API: 3. generateQrCodeBase64(uri)
    API->>Logic: QrCodeProvider.generate()
    Logic->>Bridge: QrCodeProvider.generateQrCode()
    Bridge->>Native: ZXing (JVM) / CoreImage (iOS)
    Native-->>Bridge: PNG bytes
    Bridge-->>Logic: ByteArray
    Logic->>Bridge: Base64Provider.encode()
    Bridge->>Native: Base64 encode
    Native-->>Bridge: Base64 string
    Bridge-->>Logic: Base64 string
    Logic-->>API: QR Base64
    API-->>App: "iVBORw0KGgo..."
```

### 4.2 TOTP Verification Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant API as TotpGuard API
    participant Logic as TotpGenerator
    participant Crypto as CryptoProvider
    participant Time as TimeProvider

    App->>API: verifyTotpCode(secret, code)
    API->>Logic: verify()
    
    Logic->>Time: currentTimeMillis()
    Time-->>Logic: timestamp
    
    Logic->>Logic: Calculate time counter
    Note over Logic: counter = timestamp / 30
    
    loop For each time window (-1, 0, +1)
        Logic->>Crypto: hmacSha1(key, counter)
        Crypto-->>Logic: HMAC bytes
        Logic->>Logic: Dynamic Truncation
        Logic->>Logic: Generate 6-digit code
        
        alt Code matches
            Logic-->>API: TotpVerificationResult(true, offset)
            API-->>App: {isValid: true, timeOffset}
        end
    end
    
    Logic-->>API: TotpVerificationResult(false, 0)
    API-->>App: {isValid: false}
```

### 4.3 Encryption/Decryption Flow

```mermaid
sequenceDiagram
    participant App
    participant Encryption
    participant Crypto as CryptoProvider
    participant Native as Native Crypto

    Note over App,Native: Encryption Flow
    App->>Encryption: encrypt(plaintext, key)
    Encryption->>Crypto: generateSecureRandom(12)
    Crypto-->>Encryption: IV
    Encryption->>Crypto: aesGcmEncrypt(plaintext, key, IV)
    Crypto->>Native: AES-256-GCM Encrypt
    Native-->>Crypto: Ciphertext + AuthTag
    Crypto-->>Encryption: EncryptionResult
    Encryption-->>App: {ciphertext, iv, authTag}

    Note over App,Native: Decryption Flow
    App->>Encryption: decrypt(encryptedData, key)
    Encryption->>Crypto: aesGcmDecrypt(ciphertext, key, iv)
    Crypto->>Native: AES-256-GCM Decrypt + Verify
    
    alt AuthTag Valid
        Native-->>Crypto: Plaintext
        Crypto-->>Encryption: Plaintext
        Encryption-->>App: Decrypted string
    else AuthTag Invalid
        Native-->>Crypto: Exception
        Crypto-->>Encryption: Exception
        Encryption-->>App: Throw Exception
    end
```

---

## 5. expect/actual Pattern Implementation

### 5.1 Concept

```mermaid
graph TD
    A[Common Code] -->|declares| B[expect class QrCodeProvider]
    B -->|implemented by| C[actual class QrCodeProvider<br/>JVM]
    B -->|implemented by| D[actual class QrCodeProvider<br/>iOS]
    
    C -->|uses| E[ZXing Library]
    D -->|uses| F[CoreImage Framework]
    
    G[Your App Code] -->|calls| A
    A -->|compiler resolves| C
    A -->|compiler resolves| D

    style B fill:#FFC107,color:#000
    style C fill:#4CAF50,color:#fff
    style D fill:#2196F3,color:#fff
```

### 5.2 Code Example

**commonMain/platform/QrCodeProvider.kt:**
```kotlin
expect class QrCodeProvider() {
    fun generateQrCode(content: String, size: Int): ByteArray
}
```

**jvmMain/platform/QrCodeProvider.kt:**
```kotlin
actual class QrCodeProvider {
    actual fun generateQrCode(content: String, size: Int): ByteArray {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        return MatrixToImageWriter.toBufferedImage(bitMatrix).toByteArray()
    }
}
```

**iosMain/platform/QrCodeProvider.kt:**
```kotlin
actual class QrCodeProvider {
    actual fun generateQrCode(content: String, size: Int): ByteArray {
        val filter = CIFilter.filterWithName("CIQRCodeGenerator")!!
        filter.setValue(content.toNSData(), "inputMessage")
        val cgImage = CIContext().createCGImage(filter.outputImage!!)
        return UIImagePNGRepresentation(UIImage.imageWithCGImage(cgImage))!!
    }
}
```

---

## 6. Technology Stack Matrix

| Komponen | JVM Platform | iOS Platform | Common |
|----------|--------------|--------------|--------|
| **Crypto (HMAC/AES)** | cryptography-kotlin<br/>→ JDK Provider<br/>→ javax.crypto | cryptography-kotlin<br/>→ Apple Provider<br/>→ Security Framework | ✅ Unified API |
| **QR Code** | ZXing Core<br/>ZXing JavaSE | CoreImage<br/>CIFilter | expect/actual |
| **Base64** | java.util.Base64 | Foundation<br/>Data.base64EncodedString() | expect/actual |
| **Time** | System.currentTimeMillis() | Date().timeIntervalSince1970 | expect/actual |
| **Random** | cryptography-kotlin<br/>CryptographyRandom | cryptography-kotlin<br/>CryptographyRandom | ✅ Unified API |

---

## 7. Evolution: Manual → cryptography-kotlin

### 7.1 Before (v0.0.0)

```kotlin
// Manual expect/actual untuk crypto
// commonMain
expect class CryptoProvider {
    fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray
    fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
}

// jvmMain - 100 lines
actual class CryptoProvider {
    actual fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(key, "HmacSHA1")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    // ... more JVM-specific code
}

// iosMain - 140 lines
actual class CryptoProvider {
    actual fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val algorithm = CCHmacAlgorithm.kCCHmacAlgSHA1
        val result = ByteArray(CC_SHA1_DIGEST_LENGTH)
        CCHmac(algorithm, key, key.size, data, data.size, result)
        return result
    }
    // ... more iOS-specific code
}
```

**Issues:**
- ❌ Code duplication (~240 lines total)
- ❌ Maintain 2 implementations
- ❌ Risk of platform-specific bugs
- ❌ Different APIs per platform

### 7.2 After (v0.0.1-beta)

```kotlin
// Unified implementation using cryptography-kotlin
// commonMain only - 100 lines
@OptIn(DelicateCryptographyApi::class)
class CryptoProvider {
    private val provider = CryptographyProvider.Default
    
    suspend fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = provider.get(HMAC).keyDecoder(SHA1)
            .decodeFromByteArray(HMAC.Key.Format.RAW, key)
        return hmac.signatureGenerator().generateSignature(data)
    }
    
    suspend fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val aes = provider.get(AES.GCM).keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, key)
        return aes.cipher().encrypt(plaintext, iv)
    }
}
```

**Benefits:**
- ✅ Single implementation (~100 lines total)
- ✅ Reduced code by 58%
- ✅ One place to maintain
- ✅ Same API across all platforms
- ✅ cryptography-kotlin handles platform differences
- ✅ Easy to add new platforms

---

## 8. File Structure

```
library/src/
├── commonMain/kotlin/io/github/aribrilliantsyah/totpguard/
│   ├── TotpGuard.kt                    # 🎯 Main API
│   ├── auth/
│   │   └── TotpGenerator.kt            # 🔐 TOTP Logic
│   ├── crypto/
│   │   └── Encryption.kt               # 🔒 AES Encryption
│   ├── platform/
│   │   ├── CryptoProvider.kt           # ✨ Unified Crypto
│   │   ├── QrCodeProvider.kt           # 📱 expect
│   │   ├── Base64Provider.kt           # 📝 expect
│   │   └── TimeProvider.kt             # ⏰ expect
│   ├── model/
│   │   ├── TotpAlgorithm.kt
│   │   ├── TotpVerificationResult.kt
│   │   └── EncryptionResult.kt
│   └── util/
│       └── Base32.kt
│
├── jvmMain/kotlin/.../platform/
│   ├── QrCodeProvider.kt                # actual: ZXing
│   ├── Base64Provider.kt                # actual: Java
│   └── TimeProvider.kt                  # actual: System
│
└── iosMain/kotlin/.../platform/
    ├── QrCodeProvider.kt                # actual: CoreImage
    ├── Base64Provider.kt                # actual: Foundation
    └── TimeProvider.kt                  # actual: Date
```

---

## 9. Design Principles

### 9.1 Separation of Concerns
- **API Layer**: User-facing interface
- **Business Logic**: Platform-independent algorithms
- **Platform Abstraction**: Bridge to native implementations
- **Native Implementations**: Platform-optimized code

### 9.2 DRY (Don't Repeat Yourself)
- Shared business logic in commonMain
- Platform-specific code only when necessary
- Prefer unified implementations (cryptography-kotlin)

### 9.3 Open/Closed Principle
- Easy to add new platforms without changing existing code
- expect/actual allows platform extension

### 9.4 Dependency Inversion
- High-level modules depend on abstractions (expect)
- Low-level modules implement abstractions (actual)

### 9.5 Single Responsibility
- Each class has one clear purpose
- TotpGuard delegates to specialized components

---

## 10. Adding New Platforms

To add a new platform (e.g., Android, JS, Native):

1. **Add target to build.gradle.kts:**
```kotlin
kotlin {
    jvm()
    ios()
    android()  // New platform
}
```

2. **Create platform source set:**
```
mkdir -p library/src/androidMain/kotlin/io/github/aribrilliantsyah/totpguard/platform
```

3. **Implement actual classes:**
```kotlin
// androidMain/platform/QrCodeProvider.kt
actual class QrCodeProvider {
    actual fun generateQrCode(content: String, size: Int): ByteArray {
        // Android-specific implementation using ZXing Android
    }
}
```

4. **Add platform dependencies:**
```kotlin
sourceSets {
    val androidMain by getting {
        dependencies {
            implementation("com.google.zxing:core:3.5.3")
            // ... other Android-specific deps
        }
    }
}
```

5. **Test and publish!**

The beauty of this architecture: CryptoProvider works automatically on the new platform thanks to cryptography-kotlin!

---

## 11. Performance Considerations

### Crypto Operations
- **cryptography-kotlin** uses native implementations:
  - JVM: Hardware-accelerated AES via JCA
  - iOS: Hardware-accelerated via Security Framework
- Suspend functions allow non-blocking operations
- runBlocking wrapper maintains synchronous API

### QR Code Generation
- Generated once during setup
- Cached by application if needed
- Size configurable (default 300x300)

### TOTP Verification
- Fast: Only 3 HMAC calculations (time window ±1)
- No network calls required
- Constant-time comparison for security

---

## 12. Security Architecture

### Defense in Depth

```mermaid
graph TB
    A[User Input] --> B[Input Validation]
    B --> C[Business Logic]
    C --> D[Cryptography Layer]
    D --> E[Platform Crypto]
    
    F[Encryption Key] -.stored securely.-> G[Key Management]
    G --> D
    
    H[Secret Storage] -.encrypted.-> I[Database]
    C --> H
    
    style D fill:#FF5722,color:#fff
    style G fill:#FF5722,color:#fff
    style H fill:#FF5722,color:#fff
```

### Security Features
- ✅ AES-256-GCM authenticated encryption
- ✅ Secure random number generation
- ✅ BCrypt for backup code hashing
- ✅ Time-based code expiration
- ✅ No secrets in plaintext
- ✅ Platform-native crypto backends

---

## Conclusion

This architecture provides:
- **Consistency**: Same API across all platforms
- **Performance**: Native implementations where it matters
- **Maintainability**: Shared code, minimal duplication
- **Security**: Best-practice crypto implementations
- **Extensibility**: Easy to add new platforms

The combination of Kotlin Multiplatform, expect/actual pattern, and cryptography-kotlin creates a robust, maintainable, and secure foundation for TOTP authentication across platforms.
