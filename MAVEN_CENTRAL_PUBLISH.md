# Panduan Publikasi Library TOTP Guard

Panduan ini menjelaskan cara mempublikasikan library TOTP Guard ke Maven Central mengikuti [panduan resmi Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-publish-lib.html).

## Ringkasan Publikasi

Proyek ini menggunakan plugin **Vanniktech Maven Publish** untuk menyederhanakan proses publikasi. Konfigurasi sudah diterapkan di `library/build.gradle.kts`.

## Publikasi ke Maven Central

### 1. Persiapan Awal (Satu Kali)

1. **Buat Akun Sonatype**
   
   Jika menggunakan Group ID `io.github.aribrilliantsyah`:
   
   - Verifikasi kepemilikan dengan membuat repository GitHub dengan nama yang sama
   - Nama repository harus `totpguard` karena sesuai dengan artifact ID

2. **Siapkan GPG Key**

   ```bash
   # Install GPG
   sudo apt-get install gnupg
   
   # Generate key baru
   gpg --gen-key
   
   # Lihat daftar key yang ada
   gpg --list-keys
   
   # Catat key ID (8 karakter terakhir dari ID panjang)
   # Contoh: ABCD1234 dari ID 3AA5C34371567BD2ABCD1234
   
   # Publikasikan key ke server key
   gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
   ```

### 2. Konfigurasi Gradle (Per Mesin Pengembangan)

1. **Buat `local.properties` dengan konfigurasi signing**

   ```properties
   signing.keyId=ABCD1234                        # 8 karakter terakhir dari GPG key ID
   signing.password=your-gpg-key-password        # Password GPG key
   signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg  # Path ke file secring.gpg
   
   mavenCentralUsername=sonatype-username        # Username Sonatype OSSRH
   mavenCentralPassword=sonatype-password        # Password Sonatype OSSRH
   ```

   > **Catatan**: Jika menggunakan GPG 2.1+, Anda mungkin perlu membuat file secring.gpg:
   > ```bash
   > gpg --keyring secring.gpg --export-secret-keys
   > ```

### 3. Publikasi dengan 2 Langkah Sederhana

Publikasi ke Maven Central memerlukan 2 langkah:

1. **Publikasi ke Staging Repository**

   ```bash
   # Versi standar
   ./gradlew library:publishAllPublicationsToSonatypeRepository
   
   # ATAU untuk versi SNAPSHOT
   ./gradlew library:publishAllPublicationsToSonatypeRepository -PSNAPSHOT=true
   ```

2. **Rilis ke Maven Central**
   
   - Buka [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/)
   - Login dengan kredensial Sonatype
   - Klik "Staging Repositories"
   - Pilih repository dengan nama "io.github.aribrilliantsyah-xxxx"
   - Klik "Close" dan tunggu verifikasi selesai
   - Setelah berhasil, klik "Release"

   Library akan tersedia di Maven Central dalam beberapa jam.

## Script Publikasi

Untuk mempermudah proses publikasi, gunakan script berikut:

```bash
# Publikasi versi release
./gradlew library:publishAllPublicationsToSonatypeRepository

# ATAU publikasi snapshot
./gradlew library:publishAllPublicationsToSonatypeRepository -PSNAPSHOT=true
```

## Menggunakan Library

Setelah dipublikasikan, pengguna dapat menambahkan library ini ke proyek mereka:

```kotlin
// build.gradle.kts
dependencies {
    // Untuk proyek JVM
    implementation("io.github.aribrilliantsyah:totp-guard-jvm:1.0.0") 
    
    // Untuk proyek Android
    implementation("io.github.aribrilliantsyah:totp-guard-android:1.0.0") 
    
    // Untuk proyek KMP
    implementation("io.github.aribrilliantsyah:totp-guard:1.0.0")
}
```

## Publikasi di CI/CD

Untuk GitHub Actions atau CI/CD lain, gunakan environment variables:

```yaml
env:
  ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.OSSRH_USERNAME }}
  ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.OSSRH_PASSWORD }}
  ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.GPG_PRIVATE_KEY }}
  ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.GPG_PASSPHRASE }}
```

## Validasi Struktur Proyek

Untuk memastikan struktur proyek sudah benar:

```
totp-guard/            # Root project
├── gradle/
│   └── libs.versions.toml
├── library/           # Library module
│   ├── src/
│   │   ├── commonMain/
│   │   ├── jvmMain/
│   │   ├── androidMain/
│   │   └── iosMain/
│   ├── build.gradle.kts
│   └── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew
```

Gunakan script `cleanup-structure.sh` untuk memvalidasi struktur.
