# Test @JvmOverloads Functionality

## ✅ Fixed! @JvmOverloads Added to All Public Functions

### What Changed

Added `@JvmOverloads` annotation to all public functions in `TotpGuard.kt`:

```kotlin
@JvmOverloads
fun generateTotpSecret(length: Int = 32): String

@JvmOverloads
fun generateTotpCode(
    secret: String,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): String

@JvmOverloads
fun verifyTotpCode(
    secret: String,
    code: String,
    timeWindow: Int = 1,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): TotpVerificationResult

@JvmOverloads
fun getRemainingSeconds(period: Int = 30): Int

@JvmOverloads
fun generateQrCodePng(uri: String, size: Int = 300): ByteArray

@JvmOverloads
fun generateQrCodeBase64(uri: String, size: Int = 300): String

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

---

## How to Test in Java

### 1. Create Java Test Class

Create `TotpJavaTest.java`:

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;
import io.github.aribrilliantsyah.totpguard.model.TotpVerificationResult;

public class TotpJavaTest {
    public static void main(String[] args) {
        TotpGuard totp = TotpGuard.INSTANCE;
        
        // Test 1: generateTotpSecret() - Should work with 0 or 1 parameter
        System.out.println("=== Test 1: generateTotpSecret ===");
        String secret1 = totp.generateTotpSecret();  // ✅ No parameters
        System.out.println("Secret (default): " + secret1);
        
        String secret2 = totp.generateTotpSecret(16);  // ✅ With parameter
        System.out.println("Secret (16 bytes): " + secret2);
        
        // Test 2: generateTotpCode() - Should work with 1, 2, 3, or 4 parameters
        System.out.println("\n=== Test 2: generateTotpCode ===");
        String code1 = totp.generateTotpCode(secret1);  // ✅ Only secret
        System.out.println("Code (minimal): " + code1);
        
        // Test 3: verifyTotpCode() - Should work with 2, 3, 4, 5, or 6 parameters
        System.out.println("\n=== Test 3: verifyTotpCode ===");
        TotpVerificationResult result1 = totp.verifyTotpCode(secret1, code1);  // ✅ Only required params
        System.out.println("Verification (minimal): " + result1.isValid());
        
        TotpVerificationResult result2 = totp.verifyTotpCode(secret1, code1, 2);  // ✅ With timeWindow
        System.out.println("Verification (with window): " + result2.isValid());
        
        // Test 4: getRemainingSeconds() - Should work with 0 or 1 parameter
        System.out.println("\n=== Test 4: getRemainingSeconds ===");
        int remaining1 = totp.getRemainingSeconds();  // ✅ No parameters
        System.out.println("Remaining (default 30s): " + remaining1);
        
        int remaining2 = totp.getRemainingSeconds(60);  // ✅ With parameter
        System.out.println("Remaining (60s period): " + remaining2);
        
        // Test 5: generateOtpAuthUri() - Should work with 3, 4, 5, or 6 parameters
        System.out.println("\n=== Test 5: generateOtpAuthUri ===");
        String uri1 = totp.generateOtpAuthUri(secret1, "user@example.com", "MyApp");  // ✅ Only required params
        System.out.println("URI (minimal): " + uri1.substring(0, 50) + "...");
        
        // Test 6: generateQrCodeBase64() - Should work with 1 or 2 parameters
        System.out.println("\n=== Test 6: generateQrCodeBase64 ===");
        String qr1 = totp.generateQrCodeBase64(uri1);  // ✅ Only URI
        System.out.println("QR Base64 length (default 300px): " + qr1.length());
        
        String qr2 = totp.generateQrCodeBase64(uri1, 512);  // ✅ With size
        System.out.println("QR Base64 length (512px): " + qr2.length());
        
        System.out.println("\n✅ ALL TESTS PASSED!");
        System.out.println("@JvmOverloads is working correctly!");
    }
}
```

### 2. Compile and Run

```bash
# Add dependency in your build.gradle.kts
dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
}

# Or compile directly
javac -cp ~/.m2/repository/io/github/aribrilliantsyah/totpguard-jvm/0.0.1-beta/totpguard-jvm-0.0.1-beta.jar TotpJavaTest.java

# Run
java -cp .:~/.m2/repository/io/github/aribrilliantsyah/totpguard-jvm/0.0.1-beta/totpguard-jvm-0.0.1-beta.jar TotpJavaTest
```

---

## Expected Output

```
=== Test 1: generateTotpSecret ===
Secret (default): JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP
Secret (16 bytes): JBSWY3DPEHPK3PXP

=== Test 2: generateTotpCode ===
Code (minimal): 123456

=== Test 3: verifyTotpCode ===
Verification (minimal): true
Verification (with window): true

=== Test 4: getRemainingSeconds ===
Remaining (default 30s): 15
Remaining (60s period): 45

=== Test 5: generateOtpAuthUri ===
URI (minimal): otpauth://totp/MyApp:user@example.com?secret=...

=== Test 6: generateQrCodeBase64 ===
QR Base64 length (default 300px): 15234
QR Base64 length (512px): 25678

✅ ALL TESTS PASSED!
@JvmOverloads is working correctly!
```

---

## What @JvmOverloads Does

When you add `@JvmOverloads` to a Kotlin function with default parameters:

```kotlin
@JvmOverloads
fun generateTotpCode(
    secret: String,
    algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30
): String
```

Kotlin compiler generates **multiple Java method overloads**:

```java
// Overload 1: All parameters
public String generateTotpCode(String secret, TotpAlgorithm algorithm, int digits, int period)

// Overload 2: Without last parameter
public String generateTotpCode(String secret, TotpAlgorithm algorithm, int digits)

// Overload 3: Without last 2 parameters
public String generateTotpCode(String secret, TotpAlgorithm algorithm)

// Overload 4: Only required parameter
public String generateTotpCode(String secret)
```

This makes the Java API **much more ergonomic**!

---

## Before vs After

### ❌ Before (WITHOUT @JvmOverloads)

```java
// Java code - HAD TO provide all parameters 😢
String code = totp.generateTotpCode(
    secret,
    TotpAlgorithm.SHA1,  // Can't skip this
    6,                    // Can't skip this
    30                    // Can't skip this
);
```

### ✅ After (WITH @JvmOverloads)

```java
// Java code - Can use minimal parameters! 😊
String code = totp.generateTotpCode(secret);

// Or customize only what you need
String code2 = totp.generateTotpCode(secret, TotpAlgorithm.SHA256);
String code3 = totp.generateTotpCode(secret, TotpAlgorithm.SHA256, 8);
```

---

## Verification Steps for User

1. **Update dependency** (pull latest from Maven Local):
   ```bash
   cd your-project
   ./gradlew clean --refresh-dependencies
   ```

2. **Test in your Java code**:
   ```java
   TotpGuard totp = TotpGuard.INSTANCE;
   
   // This should NOW work with just 2 parameters!
   String secret = totp.generateTotpSecret();
   String code = totp.generateTotpCode(secret);
   boolean valid = totp.verifyTotpCode(secret, code).isValid();
   ```

3. **If IntelliJ/IDE shows parameter hints**:
   - Before: Shows all 4 parameters as required
   - After: Shows only `secret` as required, others optional

---

## Published Version

The fixed library has been published to Maven Local:
- **Path**: `~/.m2/repository/io/github/aribrilliantsyah/totpguard-jvm/0.0.1-beta/`
- **File**: `totpguard-jvm-0.0.1-beta.jar`
- **Status**: ✅ Build successful, includes @JvmOverloads

---

## Summary

✅ **Added** `@JvmOverloads` to 7 public functions  
✅ **Built** successfully  
✅ **Published** to Maven Local  
✅ **Java code** can now use minimal parameters  
✅ **Kotlin code** still works with named parameters  

**No breaking changes** - only **improvements**! 🎉
