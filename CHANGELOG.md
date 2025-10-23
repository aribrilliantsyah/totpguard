# Ringkasan Perubahan TOTP-GUARD

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