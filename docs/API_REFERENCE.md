# API Reference

Complete API documentation for TOTP-GUARD library.

## Table of Contents

- [TotpGuard Object](#totpguard-object)
  - [TOTP Functions](#totp-functions)
  - [Encryption Functions](#encryption-functions)
  - [QR Code Functions](#qr-code-functions)
  - [Backup Code Functions](#backup-code-functions)
- [Model Classes](#model-classes)
- [Enums](#enums)
- [Extension Functions](#extension-functions)
- [Usage Examples](#usage-examples)

---

## TotpGuard Object

Main entry point for all library functionality. All functions are accessible through the singleton `TotpGuard` object.

### Import

**Kotlin:**
```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard
```

**Java:**
```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;

// Access via singleton
TotpGuard totp = TotpGuard.INSTANCE;
```

---

## TOTP Functions

### generateTotpSecret()

Generates a secure Base32-encoded TOTP secret key.

**Signature:**
```kotlin
@JvmOverloads
fun generateTotpSecret(length: Int = 32): String
```

**Parameters:**
- `length` (optional): Length of secret in bytes. Default: `32`
  - Recommended values: 16, 20, 32
  - Minimum: 16 bytes
  - Larger = more secure

**Returns:**
- `String`: Base32-encoded secret (e.g., `"JBSWY3DPEHPK3PXP"`)

**Example:**

```kotlin
// Kotlin
val secret = TotpGuard.generateTotpSecret()  // 32 bytes
val shortSecret = TotpGuard.generateTotpSecret(16)  // 16 bytes
```

```java
// Java
String secret = totp.generateTotpSecret();  // Default 32 bytes
String shortSecret = totp.generateTotpSecret(16);
```

---

### generateTotpCode()

Generates a TOTP code for the current time.

**Signature:**
```kotlin
@JvmOverloads
fun generateTotpCode(
    secret: String,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): String
```

**Parameters:**
- `secret` (required): Base32-encoded secret key
- `algorithm` (optional): Hash algorithm. Default: `TotpAlgorithm.SHA1`
  - Options: `SHA1`, `SHA256`, `SHA512`
- `digits` (optional): Number of digits in code. Default: `6`
  - Standard: 6 or 8 digits
- `period` (optional): Code validity period in seconds. Default: `30`
  - Standard: 30 seconds

**Returns:**
- `String`: TOTP code (e.g., `"123456"`)

**Example:**

```kotlin
// Kotlin - Minimal usage (recommended)
val code = TotpGuard.generateTotpCode(secret)

// With custom parameters
val code8digit = TotpGuard.generateTotpCode(
    secret = secret,
    algorithm = TotpAlgorithm.SHA256,
    digits = 8,
    period = 60
)
```

```java
// Java - Minimal usage (recommended)
String code = totp.generateTotpCode(secret);

// With custom parameters
String code8digit = totp.generateTotpCode(
    secret,
    TotpAlgorithm.SHA256,
    8,
    60
);
```

---

### verifyTotpCode()

Verifies a TOTP code against a secret.

**Signature:**
```kotlin
@JvmOverloads
fun verifyTotpCode(
    secret: String,
    code: String,
    timeWindow: Int = 1,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): TotpVerificationResult
```

**Parameters:**
- `secret` (required): Base32-encoded secret key
- `code` (required): TOTP code to verify
- `timeWindow` (optional): Number of periods to check ± current. Default: `1`
  - `0` = only current period
  - `1` = current + 1 before + 1 after (recommended)
  - `2` = current + 2 before + 2 after
- `algorithm` (optional): Hash algorithm. Default: `TotpAlgorithm.SHA1`
- `digits` (optional): Number of digits. Default: `6`
- `period` (optional): Period in seconds. Default: `30`

**Returns:**
- `TotpVerificationResult`: Contains `isValid: Boolean`

**Example:**

```kotlin
// Kotlin - Minimal usage (recommended)
val result = TotpGuard.verifyTotpCode(secret, userCode)
if (result.isValid) {
    // Code is valid!
}

// With custom time window
val strictResult = TotpGuard.verifyTotpCode(
    secret = secret,
    code = userCode,
    timeWindow = 0  // Only current period
)
```

```java
// Java - Minimal usage (recommended)
TotpVerificationResult result = totp.verifyTotpCode(secret, userCode);
if (result.isValid()) {
    // Code is valid!
}

// With custom time window
TotpVerificationResult strictResult = totp.verifyTotpCode(
    secret,
    userCode,
    0  // Only current period
);
```

---

### getRemainingSeconds()

Gets the number of seconds remaining until the current TOTP code expires.

**Signature:**
```kotlin
@JvmOverloads
fun getRemainingSeconds(period: Int = 30): Int
```

**Parameters:**
- `period` (optional): Period in seconds. Default: `30`

**Returns:**
- `Int`: Seconds remaining (0-29 for default period)

**Example:**

```kotlin
// Kotlin
val remaining = TotpGuard.getRemainingSeconds()
println("Code expires in $remaining seconds")
```

```java
// Java
int remaining = totp.getRemainingSeconds();
System.out.println("Code expires in " + remaining + " seconds");
```

---

## Encryption Functions

### encrypt()

Encrypts plaintext using AES-256-GCM.

**Signature:**
```kotlin
fun encrypt(plaintext: String, key: ByteArray): EncryptionResult
```

**Parameters:**
- `plaintext` (required): Text to encrypt
- `key` (required): 32-byte (256-bit) encryption key

**Returns:**
- `EncryptionResult`: Contains `ciphertext`, `iv`, and `authTag`

**Example:**

```kotlin
// Kotlin
val key = TotpGuard.generateEncryptionKey()
val encrypted = TotpGuard.encrypt(secret, key)

// Store encrypted.ciphertext, encrypted.iv, encrypted.authTag
```

```java
// Java
byte[] key = totp.generateEncryptionKey();
EncryptionResult encrypted = totp.encrypt(secret, key);

// Store encrypted.getCiphertext(), getIv(), getAuthTag()
```

---

### decrypt()

Decrypts ciphertext using AES-256-GCM.

**Signature:**
```kotlin
fun decrypt(encryptedData: EncryptionResult, key: ByteArray): String
```

**Parameters:**
- `encryptedData` (required): EncryptionResult object
- `key` (required): 32-byte encryption key (same used to encrypt)

**Returns:**
- `String`: Decrypted plaintext

**Throws:**
- Exception if decryption fails (wrong key or tampered data)

**Example:**

```kotlin
// Kotlin
val decrypted = TotpGuard.decrypt(encrypted, key)
```

```java
// Java
String decrypted = totp.decrypt(encrypted, key);
```

---

### generateEncryptionKey()

Generates a secure 256-bit (32-byte) encryption key.

**Signature:**
```kotlin
fun generateEncryptionKey(): ByteArray
```

**Returns:**
- `ByteArray`: 32-byte cryptographically secure random key

**Example:**

```kotlin
// Kotlin
val key = TotpGuard.generateEncryptionKey()

// Store securely (e.g., environment variable)
val keyBase64 = key.encodeBase64()
```

```java
// Java
byte[] key = totp.generateEncryptionKey();

// Store securely
String keyBase64 = Base64.getEncoder().encodeToString(key);
```

---

### rotateKey()

Rotates an encryption key by decrypting with old key and re-encrypting with new key.

**Signature:**
```kotlin
fun rotateKey(
    encryptedData: EncryptionResult,
    oldKey: ByteArray,
    newKey: ByteArray
): EncryptionResult
```

**Parameters:**
- `encryptedData` (required): Data encrypted with old key
- `oldKey` (required): Current encryption key
- `newKey` (required): New encryption key

**Returns:**
- `EncryptionResult`: Data encrypted with new key

**Example:**

```kotlin
// Kotlin
val newKey = TotpGuard.generateEncryptionKey()
val reencrypted = TotpGuard.rotateKey(encrypted, oldKey, newKey)

// Update stored key and encrypted data
```

```java
// Java
byte[] newKey = totp.generateEncryptionKey();
EncryptionResult reencrypted = totp.rotateKey(encrypted, oldKey, newKey);
```

---

## QR Code Functions

### generateQrCodePng()

Generates a QR code as PNG byte array.

**Signature:**
```kotlin
@JvmOverloads
fun generateQrCodePng(uri: String, size: Int = 300): ByteArray
```

**Parameters:**
- `uri` (required): otpauth:// URI to encode
- `size` (optional): Size in pixels (width = height). Default: `300`

**Returns:**
- `ByteArray`: PNG image data

**Example:**

```kotlin
// Kotlin
val uri = TotpGuard.generateOtpAuthUri(secret, "user@example.com", "MyApp")
val pngBytes = TotpGuard.generateQrCodePng(uri, 400)

// Save to file
File("qrcode.png").writeBytes(pngBytes)
```

```java
// Java
String uri = totp.generateOtpAuthUri(secret, "user@example.com", "MyApp");
byte[] pngBytes = totp.generateQrCodePng(uri, 400);

// Save to file
Files.write(Paths.get("qrcode.png"), pngBytes);
```

---

### generateQrCodeBase64()

Generates a QR code as Base64-encoded PNG.

**Signature:**
```kotlin
@JvmOverloads
fun generateQrCodeBase64(uri: String, size: Int = 300): String
```

**Parameters:**
- `uri` (required): otpauth:// URI to encode
- `size` (optional): Size in pixels. Default: `300`

**Returns:**
- `String`: Base64-encoded PNG image

**Example:**

```kotlin
// Kotlin
val uri = TotpGuard.generateOtpAuthUri(secret, "user@example.com", "MyApp")
val base64 = TotpGuard.generateQrCodeBase64(uri)

// Use in HTML
val html = """<img src="data:image/png;base64,$base64">"""
```

```java
// Java
String uri = totp.generateOtpAuthUri(secret, "user@example.com", "MyApp");
String base64 = totp.generateQrCodeBase64(uri);

// Use in HTML
String html = "<img src=\"data:image/png;base64," + base64 + "\">";
```

---

### generateOtpAuthUri()

Generates an otpauth:// URI for authenticator apps.

**Signature:**
```kotlin
@JvmOverloads
fun generateOtpAuthUri(
    secret: String,
    accountName: String,
    issuer: String,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): String
```

**Parameters:**
- `secret` (required): Base32-encoded secret
- `accountName` (required): User identifier (e.g., email)
- `issuer` (required): App/service name
- `algorithm` (optional): Hash algorithm. Default: `SHA1`
- `digits` (optional): Number of digits. Default: `6`
- `period` (optional): Period in seconds. Default: `30`

**Returns:**
- `String`: otpauth:// URI

**Example:**

```kotlin
// Kotlin
val uri = TotpGuard.generateOtpAuthUri(
    secret = secret,
    accountName = "user@example.com",
    issuer = "MyCompany"
)
// Result: otpauth://totp/MyCompany:user@example.com?secret=...&issuer=MyCompany&...
```

```java
// Java
String uri = totp.generateOtpAuthUri(
    secret,
    "user@example.com",
    "MyCompany"
);
```

---

## Backup Code Functions

### generateBackupCodes()

Generates backup codes for account recovery.

**Signature:**
```kotlin
@JvmOverloads
fun generateBackupCodes(count: Int = 10, length: Int = 8): BackupCodesResult
```

**Parameters:**
- `count` (optional): Number of codes to generate. Default: `10`
- `length` (optional): Length of each code. Default: `8`

**Returns:**
- `BackupCodesResult`: Contains formatted and hashed codes

**Example:**

```kotlin
// Kotlin
val backupCodes = TotpGuard.generateBackupCodes()

// Show to user (ONCE ONLY!)
backupCodes.formattedCodes.forEach { println(it) }

// Store hashed versions
saveToDatabase(backupCodes.hashedCodes)
```

```java
// Java
BackupCodesResult backupCodes = totp.generateBackupCodes();

// Show to user
for (String code : backupCodes.getFormattedCodes()) {
    System.out.println(code);
}

// Store hashed versions
saveToDatabase(backupCodes.getHashedCodes());
```

---

### verifyBackupCode()

Verifies a backup code.

**Signature:**
```kotlin
fun verifyBackupCode(code: String, hashedCodes: List<String>): BackupCodeVerificationResult
```

**Parameters:**
- `code` (required): User-provided backup code
- `hashedCodes` (required): List of hashed backup codes

**Returns:**
- `BackupCodeVerificationResult`: Contains `isValid` and matched `hash`

**Example:**

```kotlin
// Kotlin
val result = TotpGuard.verifyBackupCode(userCode, storedHashedCodes)
if (result.isValid) {
    // Remove used code
    removeFromDatabase(result.matchedHash!!)
}
```

```java
// Java
BackupCodeVerificationResult result = totp.verifyBackupCode(userCode, storedHashedCodes);
if (result.isValid()) {
    // Remove used code
    removeFromDatabase(result.getMatchedHash());
}
```

---

## Model Classes

### TotpVerificationResult

Result of TOTP code verification.

**Properties:**
```kotlin
data class TotpVerificationResult(
    val isValid: Boolean
)
```

**Usage:**
```kotlin
val result = TotpGuard.verifyTotpCode(secret, code)
if (result.isValid) { /* success */ }
```

---

### EncryptionResult

Result of encryption operation.

**Properties:**
```kotlin
data class EncryptionResult(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val authTag: ByteArray
)
```

**Extension Functions:**
```kotlin
fun EncryptionResult.toBase64String(): String
fun EncryptionResult.Companion.fromBase64String(str: String): EncryptionResult
```

**Usage:**
```kotlin
// Encrypt
val encrypted = TotpGuard.encrypt(secret, key)

// Store as Base64
val base64 = encrypted.toBase64String()
saveToDatabase(base64)

// Load from Base64
val loaded = EncryptionResult.fromBase64String(base64)
val decrypted = TotpGuard.decrypt(loaded, key)
```

---

### BackupCodesResult

Result of backup code generation.

**Properties:**
```kotlin
data class BackupCodesResult(
    val formattedCodes: List<String>,  // Plain text codes (show to user ONCE)
    val hashedCodes: List<String>      // Hashed codes (store in database)
)
```

**Usage:**
```kotlin
val result = TotpGuard.generateBackupCodes()

// Show to user (ONCE!)
println("Save these codes:")
result.formattedCodes.forEach { println(it) }

// Store hashed versions
database.save(result.hashedCodes)
```

---

### BackupCodeVerificationResult

Result of backup code verification.

**Properties:**
```kotlin
data class BackupCodeVerificationResult(
    val isValid: Boolean,
    val matchedHash: String?  // Hash of matched code (if valid)
)
```

**Usage:**
```kotlin
val result = TotpGuard.verifyBackupCode(code, hashedCodes)
if (result.isValid) {
    // Remove used code
    database.remove(result.matchedHash!!)
}
```

---

## Enums

### TotpAlgorithm

Supported hash algorithms for TOTP.

**Values:**
```kotlin
enum class TotpAlgorithm {
    SHA1,    // RFC 6238 standard (most compatible)
    SHA256,  // More secure
    SHA512   // Most secure
}
```

**Usage:**
```kotlin
// Kotlin
val code = TotpGuard.generateTotpCode(
    secret = secret,
    algorithm = TotpAlgorithm.SHA256
)
```

```java
// Java
String code = totp.generateTotpCode(
    secret,
    TotpAlgorithm.SHA256,
    6,
    30
);
```

**Recommendation:**
- Use `SHA1` for maximum compatibility with authenticator apps
- Use `SHA256` or `SHA512` if your app controls both sides

---

## Extension Functions

### EncryptionResult Extensions

```kotlin
// Convert to Base64 string for storage
fun EncryptionResult.toBase64String(): String

// Parse from Base64 string
fun EncryptionResult.Companion.fromBase64String(str: String): EncryptionResult
```

**Example:**
```kotlin
// Save to database
val encrypted = TotpGuard.encrypt(secret, key)
val base64 = encrypted.toBase64String()
database.save("user_secret", base64)

// Load from database
val base64 = database.get("user_secret")
val encrypted = EncryptionResult.fromBase64String(base64)
val secret = TotpGuard.decrypt(encrypted, key)
```

---

## Usage Examples

### Complete TOTP Setup Flow

```kotlin
// 1. Generate secret
val secret = TotpGuard.generateTotpSecret()

// 2. Generate QR code
val uri = TotpGuard.generateOtpAuthUri(secret, "user@example.com", "MyApp")
val qrCodeBase64 = TotpGuard.generateQrCodeBase64(uri)

// 3. Generate backup codes
val backupCodes = TotpGuard.generateBackupCodes()

// 4. Encrypt secret for storage
val key = TotpGuard.generateEncryptionKey()
val encrypted = TotpGuard.encrypt(secret, key)

// 5. Save to database
database.save(
    userId = userId,
    encryptedSecret = encrypted.toBase64String(),
    backupCodes = backupCodes.hashedCodes
)

// 6. Show QR code and backup codes to user
return TotpSetupResponse(
    qrCode = qrCodeBase64,
    backupCodes = backupCodes.formattedCodes
)
```

### Verification Flow

```kotlin
// 1. Load encrypted secret
val encryptedBase64 = database.getSecret(userId)
val encrypted = EncryptionResult.fromBase64String(encryptedBase64)

// 2. Decrypt secret
val secret = TotpGuard.decrypt(encrypted, key)

// 3. Verify code
val result = TotpGuard.verifyTotpCode(secret, userCode)

if (result.isValid) {
    // Grant access
    return AuthResponse(success = true)
} else {
    // Deny access
    return AuthResponse(success = false)
}
```

---

## Next Steps

- See [USAGE_EXAMPLES.md](../USAGE_EXAMPLES.md) for complete integration examples
- Check [SPRING_BOOT_GUIDE.md](SPRING_BOOT_GUIDE.md) for Spring Boot specifics
- Read [README.md](../README.md) for quick start guide

---

Need help? [Open an issue](https://github.com/aribrilliantsyah/totpguard/issues)
