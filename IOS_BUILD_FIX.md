# iOS Build Fix Guide

## Problem: IosTotpGenerator.kt menggunakan API tidak kompatibel

### Error yang Anda lihat:
```
e: file:///Volumes/External/personal-projects/totpguard/library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform/IosTotpGenerator.kt:5:22 Unresolved reference 'currentTimeMillis'.
```

### Penyebab:
File `IosTotpGenerator.kt` adalah file **legacy** yang masih menggunakan `kotlin.system.currentTimeMillis` yang **TIDAK kompatibel dengan iOS**. File ini harus dihapus.

---

## ✅ Solusi (Di Mac Anda)

### Opsi 1: Menggunakan Script Otomatis

1. **Di Mac Anda, navigate ke project directory:**
   ```bash
   cd /Volumes/External/personal-projects/totpguard
   ```

2. **Jalankan script cleanup:**
   ```bash
   ./remove-ios-legacy-files.sh
   ```

3. **Clean dan rebuild:**
   ```bash
   ./gradlew clean
   ./gradlew :library:build
   ```

### Opsi 2: Manual Delete

1. **Di Mac Anda, hapus file secara manual:**
   ```bash
   cd /Volumes/External/personal-projects/totpguard
   rm -f library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform/IosTotpGenerator.kt
   ```

2. **Verifikasi file yang tersisa:**
   ```bash
   ls -la library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform/
   ```

   **Yang harus ada (4 files):**
   - ✅ `Base64Provider.kt`
   - ✅ `CryptoProvider.kt`
   - ✅ `QrCodeProvider.kt`
   - ✅ `TimeProvider.kt`

   **Yang TIDAK boleh ada:**
   - ❌ `IosTotpGenerator.kt` (DELETE THIS!)
   - ❌ `IosQrCodeGenerator.kt` (should already be deleted)

3. **Clean dan rebuild:**
   ```bash
   ./gradlew clean
   ./gradlew :library:build
   ```

---

## 🔍 Mengapa IosTotpGenerator.kt Harus Dihapus?

### ❌ File Lama (WRONG):
```kotlin
// IosTotpGenerator.kt - TIDAK KOMPATIBEL iOS!
import kotlin.system.currentTimeMillis  // ❌ TIDAK ADA di iOS!

class IosTotpGenerator {
    fun generateTotp(secret: String): String {
        val timeIndex = floor(currentTimeMillis() / 1000.0 / timeStep).toLong()
        // ...
    }
}
```

**Masalah:**
- `kotlin.system.currentTimeMillis` tidak tersedia di iOS/Native
- Menyebabkan compile error: "Unresolved reference 'currentTimeMillis'"

### ✅ Implementasi Yang Benar (CORRECT):

Library sudah punya implementasi yang benar menggunakan **expect/actual pattern**:

```kotlin
// TimeProvider.kt (commonMain) - EXPECT
expect class TimeProvider() {
    fun currentTimeSeconds(): Long
}

// TimeProvider.kt (iosMain) - ACTUAL (CORRECT!)
actual class TimeProvider {
    actual fun currentTimeSeconds(): Long {
        return NSDate().timeIntervalSince1970.toLong()  // ✅ iOS compatible!
    }
}
```

**Keuntungan:**
- ✅ Menggunakan `Foundation.NSDate` yang native iOS
- ✅ Kompatibel dengan semua iOS targets
- ✅ Mengikuti Kotlin Multiplatform best practices

---

## 📋 Checklist Verifikasi

Setelah menghapus `IosTotpGenerator.kt`, pastikan:

- [ ] File `IosTotpGenerator.kt` sudah terhapus
- [ ] Build berhasil tanpa error
- [ ] 4 file platform iOS ada: Base64Provider, CryptoProvider, QrCodeProvider, TimeProvider
- [ ] Tidak ada import `kotlin.system.currentTimeMillis` di iosMain

---

## 🧪 Test Build di Mac

Setelah menghapus file, test build untuk iOS:

```bash
# Clean build
./gradlew clean

# Build semua targets
./gradlew :library:build

# Atau build iOS targets saja
./gradlew :library:compileKotlinIosX64 \
         :library:compileKotlinIosArm64 \
         :library:compileKotlinIosSimulatorArm64
```

**Expected result:**
```
BUILD SUCCESSFUL
```

---

## 🎯 Summary

| File | Status | Action |
|------|--------|--------|
| `IosTotpGenerator.kt` | ❌ Legacy, incompatible | **DELETE** |
| `TimeProvider.kt` | ✅ Correct, iOS compatible | **KEEP** |
| `Base64Provider.kt` | ✅ Fixed, iOS compatible | **KEEP** |
| `CryptoProvider.kt` | ✅ Correct, iOS compatible | **KEEP** |
| `QrCodeProvider.kt` | ✅ Fixed, iOS compatible | **KEEP** |

---

## 💡 Troubleshooting

### Jika masih error setelah delete:

1. **Clean Gradle cache:**
   ```bash
   ./gradlew clean
   rm -rf build/
   rm -rf library/build/
   ```

2. **Clean IDE cache (jika pakai IntelliJ/Android Studio):**
   - File > Invalidate Caches / Restart

3. **Rebuild:**
   ```bash
   ./gradlew :library:build
   ```

### Jika masih ada reference ke IosTotpGenerator di code:

```bash
# Cari semua reference
grep -r "IosTotpGenerator" library/src/
```

Jika ada hasil, hapus/update file-file tersebut.

---

## ✅ Hasil Akhir

Setelah fix, struktur iOS platform Anda akan seperti ini:

```
library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform/
├── Base64Provider.kt      ✅ Uses Foundation.NSData
├── CryptoProvider.kt      ✅ Uses cryptography-kotlin
├── QrCodeProvider.kt      ✅ Uses CoreImage/UIKit
└── TimeProvider.kt        ✅ Uses Foundation.NSDate
```

Semua file menggunakan API iOS native yang kompatibel! 🎉
