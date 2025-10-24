# Gradle Warnings Fix - October 2025

This document records the fixes applied to resolve Gradle warnings and security vulnerabilities.

## Issues Fixed

### 1. Deprecated Property: `kotlin.js.compiler`

**Warning:**
```
w: Deprecated Gradle Property 'kotlin.js.compiler' Used
The `kotlin.js.compiler` deprecated property is used in your build.
Solution: It is unsupported, please stop using it.
```

**Fix:**
- **File:** `gradle.properties`
- **Action:** Removed `kotlin.js.compiler=ir` line
- **Reason:** This property is deprecated and not needed since we're not targeting JavaScript platform yet

**Changes:**
```diff
# Kotlin
kotlin.code.style=official
- kotlin.js.compiler=ir
```

---

### 2. Default Kotlin Hierarchy Template Warning

**Warning:**
```
w: Default Kotlin Hierarchy Template Not Applied Correctly
The Default Kotlin Hierarchy Template was not applied to 'project ':totpguard:library'':
Explicit .dependsOn() edges were configured for the following source sets:
[iosArm64Main, iosMain, iosSimulatorArm64Main, iosX64Main]
```

**Fix:**
- **File:** `gradle.properties`
- **Action:** Added `kotlin.mpp.applyDefaultHierarchyTemplate=false`
- **Reason:** We use custom iOS source set hierarchy with explicit `dependsOn()` configuration

**Changes:**
```diff
# MPP
kotlin.mpp.enableCInteropCommonization=true
kotlin.mpp.stability.nowarn=true
+ kotlin.mpp.applyDefaultHierarchyTemplate=false
```

---

### 3. CVE-2023-51074 - ZXing Security Vulnerability

**Vulnerability:**
```
CVE-2023-51074
Severity: 5.3 (Medium)
Type: Transitive Out-of-bounds Write
Affected: ZXing 3.5.1
```

**Fix:**
- **File:** `gradle/libs.versions.toml`
- **Action:** Updated ZXing from `3.5.1` to `3.5.3`
- **Reason:** Version 3.5.3 patches the security vulnerability

**Changes:**
```diff
[versions]
- zxingCore = "3.5.1"
- zxingJavase = "3.5.1"
+ zxingCore = "3.5.3"
+ zxingJavase = "3.5.3"
```

**References:**
- [CVE-2023-51074](https://nvd.nist.gov/vuln/detail/CVE-2023-51074)
- [ZXing Release 3.5.3](https://github.com/zxing/zxing/releases/tag/zxing-3.5.3)

### 3. CVE-2023-51074 - ZXing Security Vulnerability

**Vulnerability:**
```
CVE-2023-51074
Severity: 5.3 (Medium)
Type: Transitive Out-of-bounds Write
Affected: ZXing 3.5.1
```

**Fix:**
- **File:** `gradle/libs.versions.toml`
- **Action:** Updated ZXing from `3.5.1` to `3.5.3`
- **Reason:** Version 3.5.3 patches the security vulnerability

**Changes:**
```diff
[versions]
- zxingCore = "3.5.1"
- zxingJavase = "3.5.1"
+ zxingCore = "3.5.3"
+ zxingJavase = "3.5.3"
```

**References:**
- [CVE-2023-51074](https://nvd.nist.gov/vuln/detail/CVE-2023-51074)
- [ZXing Release 3.5.3](https://github.com/zxing/zxing/releases/tag/zxing-3.5.3)

---

### 4. Removed Unused Dependencies (CVE Prevention)

**Issue:**
- Jackson dependencies declared but not used
- Vulnerability scanners detect CVEs in unused dependencies:
  - CVE-2022-42004 (Jackson - Deserialization)
  - CVE-2022-42003 (Jackson - Deserialization)
  - CVE-2020-36518 (Jackson - Out-of-bounds Write)

**Fix:**
- **File:** `gradle/libs.versions.toml`
- **Action:** Removed all unused dependency declarations
- **Removed:**
  - `jackson-databind` (2.14.2)
  - `jackson-kotlin` (2.14.2)
  - `commons-codec` (1.15)
  - `jbcrypt` (0.4)
  - `bcrypt-android` (0.10.2)
  - `bcprov-jdk15on` (1.70)
  - `bcprov-jdk15to18` (1.82)
  - `android-security-crypto` (1.1.0-alpha06)

**Reason:** 
- These were declared for future Android support but not used
- JVM uses built-in `javax.crypto` (zero external crypto dependencies)
- Vulnerability scanners flag them even when unused
- Cleaner dependency tree

**Changes:**
```diff
[versions]
kotlin = "2.2.0"
- bcprovJdk15on = "1.70"
- bcprovJdk15to18 = "1.82"
zxingCore = "3.5.3"
zxingJavase = "3.5.3"
- commonsCodec = "1.15"
- jbcrypt = "0.4"
- bcryptAndroid = "0.10.2"
- jacksonDatabind = "2.14.2"
- jacksonKotlin = "2.14.2"
kotlinxSerialization = "1.5.1"

[libraries]
- bcprov-jdk15on = { ... }
- bcprov-jdk15to18 = { ... }
- commons-codec = { ... }
- jbcrypt = { ... }
- jackson-databind = { ... }
- jackson-kotlin = { ... }
- android-security-crypto = { ... }
- bcrypt-android = { ... }
```

**Result:**
- ✅ Zero Jackson/FasterXML dependencies
- ✅ Zero BouncyCastle dependencies  
- ✅ Cleaner dependency tree
- ✅ No CVE warnings from unused libraries
- ✅ Smaller build size

---

### 5. Expect/Actual Classes Beta Warning

**Warning:**
```
w: 'expect'/'actual' classes (including interfaces, objects, annotations, enums, 
and 'actual' typealiases) are in Beta. Consider using the '-Xexpect-actual-classes' 
flag to suppress this warning.
```

**Fix:**
- **File:** `library/build.gradle.kts`
- **Action:** Added compiler flag `-Xexpect-actual-classes`
- **Reason:** Suppresses Beta warning for expect/actual pattern which is stable enough for production use

**Changes:**
```diff
kotlin {
    jvmToolchain(11)
    
+   // Suppress expect/actual classes Beta warning
+   compilerOptions {
+       freeCompilerArgs.add("-Xexpect-actual-classes")
+   }
    
    jvm {
        // ...
    }
}
```

---

### 6. Unused Variable Warnings

**Warning:**
```
w: Variable 'commonTest' is never used
w: Variable 'jvmMain' is never used
w: Variable 'jvmTest' is never used
w: Variable 'iosMain' is never used
```

**Fix:**
- **File:** `library/build.gradle.kts`
- **Action:** Added `@Suppress("UNUSED_VARIABLE")` annotations
- **Reason:** These variables are used implicitly by Gradle but flagged as unused

**Changes:**
```diff
sourceSets {
    val commonMain by getting { ... }
    
+   @Suppress("UNUSED_VARIABLE")
    val commonTest by getting { ... }
    
+   @Suppress("UNUSED_VARIABLE")
    val jvmMain by getting { ... }
    
+   @Suppress("UNUSED_VARIABLE")
    val jvmTest by getting
    
+   @Suppress("UNUSED_VARIABLE")
    val iosMain by creating { ... }
}
```

---

### 7. Beta Interop API Warnings (iOS)

**Warning:**
```
w: This declaration needs opt-in. Its usage should be marked with 
'@kotlinx.cinterop.BetaInteropApi' or '@OptIn(kotlinx.cinterop.BetaInteropApi::class)'
```

**Fix:**
- **Files:** 
  - `library/src/iosMain/.../Base64Provider.kt`
  - `library/src/iosMain/.../QrCodeProvider.kt`
- **Action:** Added `BetaInteropApi` to existing `@OptIn` annotations
- **Reason:** iOS C-interop APIs require opt-in for Beta features

**Changes:**
```diff
- @OptIn(ExperimentalForeignApi::class)
+ @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class Base64Provider {
    // ...
}

- @OptIn(ExperimentalForeignApi::class)
+ @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData { ... }

- @OptIn(ExperimentalForeignApi::class)
+ @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun NSData.toByteArray(): ByteArray { ... }
```

---

## Verification

After applying all fixes, run:

```bash
./gradlew clean build --warning-mode all
```

**Expected Result:**
- ✅ No deprecated property warnings
- ✅ No hierarchy template warnings
- ✅ No CVE vulnerabilities
- ✅ No expect/actual Beta warnings
- ✅ No unused variable warnings
- ✅ No Beta interop API warnings
- ✅ Build successful

**Remaining Warnings (Informational):**
```
w: ⚠️ Disabled Kotlin/Native Targets
The following Kotlin/Native targets cannot be built on this machine and are disabled
```

This is expected when building on Linux/Windows without Mac for iOS targets.

---

## Testing

Verify the library still works correctly:

```bash
# Run tests
./gradlew test

# Publish to Maven Local
./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false

# Check for vulnerabilities
./gradlew dependencyCheckAnalyze
```

---

## Summary

| Issue | Status | Version Before | Version After |
|-------|--------|----------------|---------------|
| `kotlin.js.compiler` deprecated | ✅ Fixed | Present | Removed |
| Hierarchy template warning | ✅ Fixed | Not disabled | Disabled |
| CVE-2023-51074 (ZXing) | ✅ Fixed | 3.5.1 | 3.5.3 |
| Unused dependencies | ✅ Removed | 8+ unused | 0 unused |
| CVE-2022-42004 (Jackson) | ✅ Fixed | Declared | Removed |
| CVE-2022-42003 (Jackson) | ✅ Fixed | Declared | Removed |
| CVE-2020-36518 (Jackson) | ✅ Fixed | Declared | Removed |
| Expect/actual Beta warning | ✅ Fixed | Visible | Suppressed |
| Unused variable warnings | ✅ Fixed | 4 warnings | Suppressed |
| Beta interop API warnings | ✅ Fixed | 3 warnings | Suppressed |

---

## Files Modified

1. **gradle.properties**
   - Removed `kotlin.js.compiler=ir`
   - Added `kotlin.mpp.applyDefaultHierarchyTemplate=false`

2. **gradle/libs.versions.toml**
   - Updated `zxingCore` from `3.5.1` to `3.5.3`
   - Updated `zxingJavase` from `3.5.1` to `3.5.3`
   - Removed 8 unused dependency declarations (Jackson, BouncyCastle, etc.)

3. **library/build.gradle.kts**
   - Added `-Xexpect-actual-classes` compiler flag
   - Added `@Suppress("UNUSED_VARIABLE")` to 4 source set variables

4. **library/src/iosMain/.../Base64Provider.kt**
   - Added `BetaInteropApi` to all `@OptIn` annotations

5. **library/src/iosMain/.../QrCodeProvider.kt**
   - Added `BetaInteropApi` to all `@OptIn` annotations

---

## Impact

### Security
- ✅ Patched CVE-2023-51074 vulnerability
- ✅ Using latest stable ZXing version

### Build Performance
- ✅ Removed deprecated property
- ✅ Proper hierarchy configuration

### Maintenance
- ✅ Cleaner build output
- ✅ No false-positive warnings
- ✅ Better developer experience

---

## Future Recommendations

1. **Dependency Updates:**
   - Monitor ZXing releases for future updates
   - Consider automated dependency scanning (e.g., Dependabot)

2. **Kotlin Version:**
   - Stay on Kotlin 2.2.0+ for best KMP support
   - Monitor expect/actual classes graduation from Beta

3. **Build Configuration:**
   - Keep `kotlin.mpp.applyDefaultHierarchyTemplate=false` while using custom hierarchy
   - Remove `-Xexpect-actual-classes` when feature graduates from Beta

---

## References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Gradle Properties Reference](https://docs.gradle.org/current/userguide/build_environment.html)
- [ZXing Security Updates](https://github.com/zxing/zxing/security)
- [CVE Database](https://nvd.nist.gov/)

---

**Fixed by:** Ari Brilliantsyah  
**Date:** October 24, 2025  
**Build Status:** ✅ All warnings resolved
