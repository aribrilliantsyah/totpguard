# TotpGuard - Usage Examples

Dokumentasi ini menunjukkan berbagai cara penggunaan library TotpGuard dengan parameter default dan custom.

## 1. Generate TOTP Secret

### Dengan Default Parameters (32 bytes)
```kotlin
val secret = TotpGuard.generateTotpSecret()
// Output: "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567..." (Base32 encoded)
```

### Dengan Custom Length
```kotlin
val secret = TotpGuard.generateTotpSecret(length = 64)
// Menghasilkan secret 64 bytes
```

---

## 2. Generate TOTP Code

### Dengan Semua Default Parameters (SHA1, 6 digits, 30 seconds)
```kotlin
val secret = "JBSWY3DPEHPK3PXP"
val code = TotpGuard.generateTotpCode(secret)
// Output: "123456" (6 digit code)
```

### Dengan Custom Algorithm Saja
```kotlin
val code = TotpGuard.generateTotpCode(
    secret = secret,
    algorithm = TotpAlgorithm.SHA256
)
// Menggunakan SHA256, sisanya default (6 digits, 30 seconds)
```

### Dengan Custom Digits Saja
```kotlin
val code = TotpGuard.generateTotpCode(
    secret = secret,
    digits = 8
)
// Output: "12345678" (8 digit code)
```

### Dengan Custom Period Saja
```kotlin
val code = TotpGuard.generateTotpCode(
    secret = secret,
    period = 60
)
// Code berlaku 60 detik, sisanya default
```

### Dengan Semua Parameter Custom
```kotlin
val code = TotpGuard.generateTotpCode(
    secret = secret,
    algorithm = TotpAlgorithm.SHA512,
    digits = 8,
    period = 60
)
// Full customization: SHA512, 8 digits, 60 seconds
```

---

## 3. Verify TOTP Code

### Dengan Default Parameters (timeWindow = 1)
```kotlin
val result = TotpGuard.verifyTotpCode(
    secret = secret,
    code = "123456"
)
if (result.isValid) {
    println("Code valid!")
}
```

### Dengan Custom Time Window
```kotlin
val result = TotpGuard.verifyTotpCode(
    secret = secret,
    code = "123456",
    timeWindow = 2  // Check ±2 time periods
)
```

### Dengan Custom Algorithm dan Time Window
```kotlin
val result = TotpGuard.verifyTotpCode(
    secret = secret,
    code = "12345678",
    timeWindow = 2,
    algorithm = TotpAlgorithm.SHA256,
    digits = 8,
    period = 60
)
```

---

## 4. Generate OTP Auth URI

### Dengan Default Parameters (SHA1, 6 digits, 30 seconds)
```kotlin
val uri = TotpGuard.generateOtpAuthUri(
    secret = secret,
    accountName = "user@example.com",
    issuer = "MyApp"
)
// Output: "otpauth://totp/MyApp:user@example.com?secret=...&issuer=MyApp&algorithm=SHA1&digits=6&period=30"
```

### Dengan Custom Algorithm Saja
```kotlin
val uri = TotpGuard.generateOtpAuthUri(
    secret = secret,
    accountName = "user@example.com",
    issuer = "MyApp",
    algorithm = TotpAlgorithm.SHA256
)
// Hanya algorithm yang custom, sisanya default
```

### Dengan Semua Parameter Custom
```kotlin
val uri = TotpGuard.generateOtpAuthUri(
    secret = secret,
    accountName = "user@example.com",
    issuer = "MyApp",
    algorithm = TotpAlgorithm.SHA512,
    digits = 8,
    period = 60
)
```

---

## 5. Generate QR Code

### Dengan Default Size (300x300 pixels)
```kotlin
val qrPng = TotpGuard.generateQrCodePng(uri)
// PNG byte array dengan size 300x300
```

### Dengan Custom Size
```kotlin
val qrPng = TotpGuard.generateQrCodePng(
    uri = uri,
    size = 512
)
// PNG byte array dengan size 512x512
```

### Generate Base64 QR Code dengan Default Size
```kotlin
val qrBase64 = TotpGuard.generateQrCodeBase64(uri)
// Base64 string untuk embed di HTML/JSON
```

---

## 6. Encryption/Decryption

### Generate Encryption Key
```kotlin
val key = TotpGuard.generateEncryptionKey()
// 256-bit (32 bytes) AES key
```

### Encrypt Data
```kotlin
val plaintext = "Sensitive data"
val encrypted = TotpGuard.encrypt(plaintext, key)
// Menghasilkan EncryptionResult dengan ciphertext, iv, dan authTag
```

### Decrypt Data
```kotlin
val decrypted = TotpGuard.decrypt(encrypted, key)
println(decrypted)  // "Sensitive data"
```

### Rotate Encryption Key
```kotlin
val newKey = TotpGuard.generateEncryptionKey()
val reencrypted = TotpGuard.rotateKey(encrypted, key, newKey)
// Data sekarang dienkripsi dengan key baru
```

---

## 7. Get Remaining Time

### Dengan Default Period (30 seconds)
```kotlin
val seconds = TotpGuard.getRemainingSeconds()
println("Code expires in $seconds seconds")
```

### Dengan Custom Period
```kotlin
val seconds = TotpGuard.getRemainingSeconds(period = 60)
println("Code expires in $seconds seconds")
```

---

## Complete Usage Example

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard
import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm

fun main() {
    // 1. Generate secret
    val secret = TotpGuard.generateTotpSecret()
    println("Secret: $secret")
    
    // 2. Generate OTP Auth URI dengan default parameters
    val uri = TotpGuard.generateOtpAuthUri(
        secret = secret,
        accountName = "user@example.com",
        issuer = "MyApp"
    )
    println("URI: $uri")
    
    // 3. Generate QR Code dengan default size
    val qrBase64 = TotpGuard.generateQrCodeBase64(uri)
    println("QR Code (Base64): ${qrBase64.take(50)}...")
    
    // 4. Generate TOTP code dengan default parameters
    val code = TotpGuard.generateTotpCode(secret)
    println("Current code: $code")
    
    // 5. Verify code
    val result = TotpGuard.verifyTotpCode(secret, code)
    println("Code is valid: ${result.isValid}")
    
    // 6. Check remaining time dengan default period
    val remaining = TotpGuard.getRemainingSeconds()
    println("Code expires in $remaining seconds")
    
    // 7. Encrypt/Decrypt secret
    val encryptionKey = TotpGuard.generateEncryptionKey()
    val encrypted = TotpGuard.encrypt(secret, encryptionKey)
    println("Encrypted ciphertext: ${encrypted.ciphertext.take(20)}...")
    
    val decrypted = TotpGuard.decrypt(encrypted, encryptionKey)
    println("Decrypted matches original: ${decrypted == secret}")
}
```

---

## Platform-Specific Notes

### JVM (Java/Kotlin/Spring Boot)
```kotlin
// Semua fungsi bekerja secara synchronous
val code = TotpGuard.generateTotpCode(secret)
```

### iOS (Swift via Kotlin/Native)
```swift
// Call dari Swift
let secret = TotpGuard.shared.generateTotpSecret()
let code = TotpGuard.shared.generateTotpCode(secret: secret)
```

### Android (Kotlin)
```kotlin
// Sama seperti JVM
val code = TotpGuard.generateTotpCode(secret)
```

---

## Key Points

✅ **Semua parameter opsional memiliki default values yang masuk akal**
- `algorithm` default: SHA1 (paling umum digunakan)
- `digits` default: 6 (standar industry)
- `period` default: 30 seconds (standar TOTP)
- `size` default: 300 pixels (ukuran QR code yang pas)
- `timeWindow` default: 1 (toleransi ±30 detik)
- `length` default: 32 bytes (256-bit security)

✅ **Anda bisa hanya mengisi parameter yang ingin diubah**
```kotlin
// Hanya ubah algorithm, sisanya default
val code = TotpGuard.generateTotpCode(
    secret = secret,
    algorithm = TotpAlgorithm.SHA256
)
```

✅ **Kompatibel dengan Google Authenticator, Authy, dan authenticator apps lainnya**

✅ **Aman untuk production** - menggunakan cryptographic libraries platform native
