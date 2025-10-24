# Ringkasan Perubahan TOTP-GUARD

## v0.0.1-beta (24 Oktober 2025)

### ✨ Perubahan Terbaru (Update 24 Oktober 2025)

#### Fixed - CRITICAL
- 🚨 **Added @JvmOverloads to all public functions** (MAJOR FIX!):
  - Sebelumnya: Java code HARUS isi semua parameter (sangat ribet!)
  - Sekarang: Java code bisa pakai parameter minimal (simple!)
  - Functions affected: `generateTotpSecret()`, `generateTotpCode()`, `verifyTotpCode()`, `getRemainingSeconds()`, `generateQrCodePng()`, `generateQrCodeBase64()`, `generateOtpAuthUri()`
  - Example: `totp.generateTotpCode(secret)` sekarang WORKS! (dulu harus 4 parameters)
  - See [JVMOVERLOADS_TEST.md](JVMOVERLOADS_TEST.md) for testing guide

- 🐛 **Fixed iOS compilation errors**:
  - Type mismatches (Long → ULong) di Base64Provider dan QrCodeProvider
  - Missing `@OptIn(ExperimentalForeignApi)` annotations
  - Unresolved `memcpy` references (added `import platform.posix.memcpy`)
  - Missing CoreGraphics import untuk `CGAffineTransformMakeScale`
  - Fixed ByteArray.toNSData() implementation dengan proper `usePinned` API
  
- 🐛 **Removed legacy IosTotpGenerator.kt**:
  - File ini menggunakan `kotlin.system.currentTimeMillis` yang tidak kompatibel dengan iOS
  - Sekarang semua platform menggunakan `TimeProvider` expect/actual pattern
  - iOS menggunakan `Foundation.NSDate.timeIntervalSince1970` untuk time operations

- 🐛 **Fixed Java/Spring Boot compatibility**:
  - Removed cryptography-kotlin dependency dari JVM target
  - JVM sekarang menggunakan standard `javax.crypto` (built-in, zero external dependencies)
  - Fixed NoClassDefFoundError saat menggunakan library di Spring Boot
  
#### Changed
- 🔄 **Refactored CryptoProvider ke hybrid approach**:
  - JVM: Menggunakan `javax.crypto.*` (SecureRandom, Mac, Cipher dengan GCM)
  - iOS: Menggunakan `cryptography-kotlin` dengan Apple Security Framework
  - Android (future): Akan menggunakan cryptography-kotlin dengan JDK Provider
  
- ⚡ **Simplified code - removed unnecessary coroutines**:
  - Removed `runBlocking` dari TotpGenerator dan Encryption untuk JVM
  - JVM operations sekarang fully synchronous (better performance)
  - iOS masih menggunakan suspend functions (wrapped in runBlocking)
  
- 📦 **Optimized dependencies**:
  - commonMain: Hanya kotlinx-serialization dan kotlinx-datetime
  - jvmMain: Hanya zxing (crypto is built-in)
  - iosMain: cryptography-kotlin stack + coroutines
  - Library size reduced by ~40%

#### Removed
- ❌ **Backup codes feature removed** (untuk membuat library lebih slim):
  - Deleted BackupCodesManager.kt
  - Deleted BackupCodeVerificationResult.kt
  - Deleted BackupCodesResult.kt
  - Deleted BCryptProvider.kt (all platforms)
  - Removed jbcrypt dependency
  - Updated documentation untuk menghapus semua referensi backup codes

#### Added
- 📚 **Created USAGE_EXAMPLES.md**:
  - Comprehensive examples dengan default parameters
  - Examples dengan custom parameters
  - Platform-specific notes (JVM, iOS, Android)
  - Complete usage examples untuk semua fitur
  - Demonstrates bahwa semua parameter dengan default value bisa tidak diisi

- ✅ **Added comprehensive tests**:
  - Test untuk default parameters functionality
  - Test untuk minimal parameter usage
  - Test untuk custom parameter combinations
  - All tests passing

#### Documentation
- 📝 Updated README.md dengan link ke USAGE_EXAMPLES.md
- 📝 Updated ARCHITECTURE.md (removed backup codes references)
- 📝 Clear documentation tentang parameter optionality

---

## v0.0.1-beta (22 Oktober 2025)

Versi beta pertama dari library TOTP-GUARD.

## 1. Perubahan TotpGuard API

- **Sebelumnya**: 
  - TotpGuard menggunakan referensi ke static methods di TotpGenerator, BackupCodesManager, dll.
  - Tidak konsisten dengan implementasi yang berbasis instance
  - Kurang fleksibel untuk konfigurasi parameter

- **Sekarang**: 
  - TotpGuard sebagai singleton yang menciptakan instance kelas implementasi
  - API yang lebih kaya dan terdokumentasi dengan baik
  - Dukungan untuk semua parameter konfigurasi
  - Return types yang lebih informatif dengan model classes

## 2. Penambahan Model Classes

- **TotpVerificationResult**: Hasil dari verifikasi kode TOTP
- **EncryptionResult**: Hasil dari enkripsi (ciphertext, iv, authTag)
- **BackupCodesResult**: Hasil dari pembuatan kode cadangan
- **BackupCodeVerificationResult**: Hasil dari verifikasi kode cadangan
- **TotpAlgorithm**: Enum untuk algoritma hash yang didukung (SHA1, SHA256, SHA512)

## 3. Implementasi yang Ditingkatkan

- **TotpGenerator**: 
  - Implementasi RFC 6238 yang lebih lengkap 
  - Dukungan untuk algoritma hash yang berbeda
  - Verifikasi dengan time window

- **BackupCodesManager**:
  - Format kode yang lebih aman dan mudah dibaca
  - Hashing dengan bcrypt

- **Encryption**:
  - Menggunakan AES-256-GCM 
  - Pemisahan ciphertext dan authentication tag

- **QrCodeGenerator**:
  - Dukungan untuk output PNG dan Base64
  - Konfigurasi ukuran

## 4. Dokumentasi & Contoh

- **README yang diperbarui**:
  - Contoh untuk Spring Boot (Java & Kotlin)
  - Contoh untuk Android dengan EncryptedSharedPreferences
  - Contoh untuk iOS
  - Panduan serialisasi dan penyimpanan data

## 5. Perubahan pada Build Configuration


## 6. Kesimpulan


## Security fixes

- Bumped Bouncy Castle provider to 1.82 to address several CVEs (CVE-2023-33202, CVE-2024-29857, CVE-2024-30171, CVE-2024-30172). This resolves known issues related to resource exhaustion, infinite loops, and timing leakage.
API baru memberikan pengalaman developer yang lebih baik, konsistensi yang lebih tinggi, dan fleksibilitas yang lebih besar dalam menggunakan library. Semua fitur utama tetap didukung dengan tambahan detail implementasi yang lebih baik.