# TOTP-GUARD

TOTP-GUARD adalah library Kotlin Multiplatform (KMP) untuk autentikasi TOTP (Time-based One-Time Password), enkripsi, pembuatan kode QR, dan manajemen kode cadangan. Library ini dirancang untuk mudah digunakan dalam aplikasi Spring Boot, Android, dan iOS.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.aribrilliantsyah/totp-guard.svg)](https://search.maven.org/artifact/io.github.aribrilliantsyah/totp-guard)
[![Version](https://img.shields.io/badge/version-0.0.1--beta-orange)](https://github.com/aribrilliantsyah/totpguard/releases)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)

## Fitur Utama

- **TOTP (Time-based One-Time Password)**
  - Pembuatan dan verifikasi kode TOTP sesuai RFC 6238
  - Dukungan algoritma SHA1, SHA256, dan SHA512
  - Toleransi waktu yang dapat dikonfigurasi

- **Enkripsi (AES-256-GCM)**
  - Enkripsi secret TOTP untuk penyimpanan aman
  - Pembangkitan kunci enkripsi 256-bit yang aman
  - Dukungan rotasi kunci

- **Pembangkitan Kode QR**
  - Pembuatan URI otpauth:// sesuai standar
  - Pembuatan kode QR sebagai gambar PNG atau string Base64

- **Kode Cadangan**
  - Pembuatan kode cadangan yang aman untuk pemulihan akun
  - Hash kode cadangan dengan bcrypt
  - Format kode yang mudah dibaca pengguna

## Instalasi

### Gradle (Kotlin DSL)

```kotlin
```kotlin
// Untuk proyek Kotlin Multiplatform
implementation("io.github.aribrilliantsyah:totp-guard:0.0.1-beta")

// Atau untuk proyek JVM/Android saja
implementation("io.github.aribrilliantsyah:totp-guard-jvm:0.0.1-beta") 
implementation("io.github.aribrilliantsyah:totp-guard-android:0.0.1-beta")
```
```

### Maven

```xml
<dependency>
  <groupId>io.github.aribrilliantsyah</groupId>
  <artifactId>totp-guard</artifactId>
  <version>0.0.1-beta</version>
</dependency>
```

## Panduan Penggunaan

### 1. Penggunaan di Spring Boot (Java)

#### Contoh Service untuk Setup TOTP

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;
import io.github.aribrilliantsyah.totpguard.model.BackupCodesResult;
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import io.github.aribrilliantsyah.totpguard.model.TotpVerificationResult;
import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm;
import io.github.aribrilliantsyah.totpguard.model.BackupCodeVerificationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class TotpService {
    private final TotpGuard totpGuard = TotpGuard.INSTANCE;
    private final byte[] encryptionKey;
    
    public TotpService(@Value("${totpguard.encryption.key}") String keyBase64) {
        this.encryptionKey = Base64.getDecoder().decode(keyBase64);
    }
    
    /**
     * Setup autentikasi TOTP untuk pengguna
     */
    public TotpSetupDto setupTotp(String userEmail) {
        // 1. Bangkitkan secret
        String secret = totpGuard.generateTotpSecret(32);
        
        // 2. Enkripsi secret untuk penyimpanan
        EncryptionResult encrypted = totpGuard.encrypt(secret, encryptionKey);
        
        // 3. Bangkitkan kode cadangan
        BackupCodesResult backupCodes = totpGuard.generateBackupCodes(10, 8);
        
        // 4. Bangkitkan kode QR
        String qrBase64 = totpGuard.generateQrCodeBase64(
            totpGuard.generateOtpAuthUri(
                secret, 
                userEmail, 
                "AplikasiSaya"
            ),
            300
        );
        
        // 5. Kembalikan hasil untuk disimpan dan ditampilkan
        return new TotpSetupDto(
            encrypted,
            backupCodes.getHashedCodes(),
            backupCodes.getFormattedCodes(),
            qrBase64
        );
    }
    
    /**
     * Verifikasi kode TOTP yang dimasukkan pengguna
     */
    public boolean verifyTotp(EncryptionResult encryptedSecret, String userCode) {
        // 1. Dekripsi secret
        String secret = totpGuard.decrypt(encryptedSecret, encryptionKey);
        
        // 2. Verifikasi kode
        TotpVerificationResult result = totpGuard.verifyTotpCode(
            secret, 
            userCode, 
            1,  // time window
            TotpAlgorithm.SHA1,
            6,   // digits
            30   // period
        );
        
        return result.isValid();
    }
    
    /**
     * Verifikasi kode cadangan yang dimasukkan pengguna
     */
    public boolean verifyBackupCode(String code, List<String> hashedCodes) {
        BackupCodeVerificationResult result = totpGuard.verifyBackupCode(code, hashedCodes);
        return result.isValid();
    }
    
    /**
     * Rotasi kunci enkripsi
     */
    public EncryptionResult rotateEncryptionKey(EncryptionResult encrypted, byte[] newKey) {
        return totpGuard.rotateKey(encrypted, encryptionKey, newKey);
    }
    
    /**
     * Mendapatkan waktu tersisa kode TOTP saat ini
     */
    public int getRemainingSeconds() {
        return totpGuard.getRemainingSeconds();
    }
}
```

#### Model DTO

```java
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import java.util.List;

public class TotpSetupDto {
    private final EncryptionResult encryptedSecret;
    private final List<String> hashedBackupCodes;
    private final List<String> formattedBackupCodes;
    private final String qrCodeBase64;
    
    public TotpSetupDto(
        EncryptionResult encryptedSecret, 
        List<String> hashedBackupCodes,
        List<String> formattedBackupCodes,
        String qrCodeBase64
    ) {
        this.encryptedSecret = encryptedSecret;
        this.hashedBackupCodes = hashedBackupCodes;
        this.formattedBackupCodes = formattedBackupCodes;
        this.qrCodeBase64 = qrCodeBase64;
    }
    
    // Getters
    public EncryptionResult getEncryptedSecret() {
        return encryptedSecret;
    }
    
    public List<String> getHashedBackupCodes() {
        return hashedBackupCodes;
    }
    
    public List<String> getFormattedBackupCodes() {
        return formattedBackupCodes;
    }
    
    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}
```

#### Controller

```java
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class TotpController {
    private final TotpService totpService;
    private final UserRepository userRepository;
    
    public TotpController(TotpService totpService, UserRepository userRepository) {
        this.totpService = totpService;
        this.userRepository = userRepository;
    }
    
    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(@AuthenticationPrincipal UserDetails user) {
        // Dapatkan user ID (implementasi tergantung pada struktur UserDetails Anda)
        Long userId = ((CustomUserDetails) user).getId();
        
        TotpSetupDto setup = totpService.setupTotp(user.getUsername());
        
        // Simpan data enkripsi dan kode cadangan ke database
        // Catatan: Anda perlu menentukan cara menyimpan objek EncryptionResult
        userRepository.saveTotpDetails(
            userId,
            setup.getEncryptedSecret(),
            setup.getHashedBackupCodes()
        );
        
        // Kembalikan QR code dan kode cadangan ke pengguna
        return ResponseEntity.ok(
            new TotpSetupResponse(
                setup.getQrCodeBase64(),
                setup.getFormattedBackupCodes()
            )
        );
    }
    
    @PostMapping("/totp/verify")
    public ResponseEntity<TotpVerifyResponse> verifyTotp(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody TotpVerifyRequest request
    ) {
        // Dapatkan user ID
        Long userId = ((CustomUserDetails) user).getId();
        
        // Ambil data TOTP dari database
        UserTotpData userData = userRepository.getTotpDetails(userId);
        
        boolean isValid = totpService.verifyTotp(
            userData.getEncryptedSecret(),
            request.getCode()
        );
        
        if (isValid) {
            // Tambahkan otoritas TOTP_VERIFIED ke sesi pengguna
            ((CustomUserDetails) user).addAuthority("TOTP_VERIFIED");
            
            return ResponseEntity.ok(new TotpVerifyResponse(true));
        } else {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new TotpVerifyResponse(false));
        }
    }
    
    @GetMapping("/totp/remaining-time")
    public ResponseEntity<Integer> getRemainingTime() {
        return ResponseEntity.ok(totpService.getRemainingSeconds());
    }
    
    @PostMapping("/totp/backup-code")
    public ResponseEntity<TotpVerifyResponse> verifyBackupCode(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody BackupCodeRequest request
    ) {
        // Dapatkan user ID
        Long userId = ((CustomUserDetails) user).getId();
        
        // Ambil data kode cadangan dari database
        List<String> hashedBackupCodes = userRepository.getBackupCodes(userId);
        
        boolean isValid = totpService.verifyBackupCode(
            request.getCode(),
            hashedBackupCodes
        );
        
        if (isValid) {
            // Tambahkan otoritas TOTP_VERIFIED ke sesi pengguna
            ((CustomUserDetails) user).addAuthority("TOTP_VERIFIED");
            
            // Hapus kode cadangan yang telah digunakan
            // (Dalam implementasi nyata, Anda perlu mencari dan menghapus kode yang cocok)
            
            return ResponseEntity.ok(new TotpVerifyResponse(true));
        } else {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new TotpVerifyResponse(false));
        }
    }
}
```

### 2. Penggunaan di Spring Boot (Kotlin)

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class TotpService(
    @Value("\${totpguard.encryption.key}") keyBase64: String
) {
    private val totpGuard = TotpGuard
    private val encryptionKey = Base64.getDecoder().decode(keyBase64)
    
    fun setupTotp(userEmail: String): TotpSetupDto {
        // 1. Bangkitkan secret TOTP
        val secret = totpGuard.generateTotpSecret()
        
        // 2. Enkripsi secret untuk penyimpanan
        val encrypted = totpGuard.encrypt(secret, encryptionKey)
        
        // 3. Bangkitkan kode cadangan
        val backupCodes = totpGuard.generateBackupCodes()
        
        // 4. Bangkitkan kode QR
        val qrBase64 = totpGuard.generateQrCodeBase64(
            totpGuard.generateOtpAuthUri(
                secret = secret, 
                accountName = userEmail, 
                issuer = "AplikasiSaya"
            ),
            size = 300
        )
        
        return TotpSetupDto(
            encryptedSecret = encrypted,
            hashedBackupCodes = backupCodes.hashedCodes,
            formattedBackupCodes = backupCodes.formattedCodes,
            qrCodeBase64 = qrBase64
        )
    }
    
    fun verifyTotp(encrypted: EncryptionResult, userCode: String): Boolean {
        // 1. Dekripsi secret
        val secret = totpGuard.decrypt(encrypted, encryptionKey)
        
        // 2. Verifikasi kode
        val result = totpGuard.verifyTotpCode(
            secret = secret, 
            code = userCode,
            timeWindow = 1,
            algorithm = TotpAlgorithm.SHA1,
            digits = 6,
            period = 30
        )
        
        return result.isValid
    }
    
    fun getRemainingSeconds(): Int {
        return totpGuard.getRemainingSeconds()
    }
    
    fun verifyBackupCode(code: String, hashedCodes: List<String>): Boolean {
        val result = totpGuard.verifyBackupCode(code, hashedCodes)
        return result.isValid
    }
}
```

### 3. Penggunaan di Java Biasa (Tanpa Framework)

Untuk aplikasi Java standar tanpa framework seperti Spring Boot, Anda dapat menggunakan library ini dengan cara berikut:

#### Contoh Aplikasi Konsol Java

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;
import io.github.aribrilliantsyah.totpguard.model.BackupCodesResult;
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import io.github.aribrilliantsyah.totpguard.model.TotpVerificationResult;
import io.github.aribrilliantsyah.totpguard.model.TotpAlgorithm;
import io.github.aribrilliantsyah.totpguard.model.BackupCodeVerificationResult;
import io.github.aribrilliantsyah.totpguard.model.EncryptionResultExtensionsKt;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TotpExample {
    private static final TotpGuard totpGuard = TotpGuard.INSTANCE;
    private static final String DATA_DIR = "totp_data";
    private static final String SECRET_FILE = DATA_DIR + "/secret.dat";
    private static final String BACKUP_CODES_FILE = DATA_DIR + "/backup_codes.dat";
    private static final String ENCRYPTION_KEY_FILE = DATA_DIR + "/encryption.key";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Buat direktori data jika belum ada
            Files.createDirectories(Paths.get(DATA_DIR));
            
            System.out.println("=== TOTP Authentication Demo ===");
            System.out.println("1. Setup TOTP");
            System.out.println("2. Generate TOTP Code");
            System.out.println("3. Verify TOTP Code");
            System.out.println("4. Verify Backup Code");
            System.out.print("Pilih opsi: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    setupTotp(scanner);
                    break;
                case 2:
                    generateTotpCode();
                    break;
                case 3:
                    verifyTotpCode(scanner);
                    break;
                case 4:
                    verifyBackupCode(scanner);
                    break;
                default:
                    System.out.println("Opsi tidak valid!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void setupTotp(Scanner scanner) throws IOException {
        System.out.print("Masukkan email/username: ");
        String email = scanner.nextLine();
        
        System.out.print("Masukkan nama aplikasi/issuer: ");
        String issuer = scanner.nextLine();
        
        // 1. Generate atau load encryption key
        byte[] encryptionKey = loadOrGenerateEncryptionKey();
        
        // 2. Generate secret TOTP
        String secret = totpGuard.generateTotpSecret(32);
        System.out.println("\n✓ Secret TOTP berhasil dibuat");
        
        // 3. Enkripsi secret untuk penyimpanan
        EncryptionResult encrypted = totpGuard.encrypt(secret, encryptionKey);
        saveEncryptedSecret(encrypted);
        System.out.println("✓ Secret berhasil dienkripsi dan disimpan");
        
        // 4. Generate backup codes
        BackupCodesResult backupCodes = totpGuard.generateBackupCodes(10, 8);
        saveBackupCodes(backupCodes.getHashedCodes());
        System.out.println("✓ Backup codes berhasil dibuat");
        
        // 5. Generate QR Code
        String otpAuthUri = totpGuard.generateOtpAuthUri(secret, email, issuer);
        String qrCodeBase64 = totpGuard.generateQrCodeBase64(otpAuthUri, 300);
        
        // Simpan QR code ke file
        byte[] qrCodeBytes = Base64.getDecoder().decode(qrCodeBase64);
        Files.write(Paths.get(DATA_DIR + "/qrcode.png"), qrCodeBytes);
        System.out.println("✓ QR Code berhasil disimpan ke: " + DATA_DIR + "/qrcode.png");
        
        // Tampilkan backup codes
        System.out.println("\n=== BACKUP CODES (Simpan di tempat aman!) ===");
        for (String code : backupCodes.getFormattedCodes()) {
            System.out.println(code);
        }
        System.out.println("===========================================\n");
        
        // Tampilkan URI untuk manual setup
        System.out.println("URI untuk setup manual:");
        System.out.println(otpAuthUri);
        System.out.println("\nScan QR Code di file qrcode.png dengan aplikasi authenticator Anda.");
    }
    
    private static void generateTotpCode() throws IOException {
        byte[] encryptionKey = loadOrGenerateEncryptionKey();
        EncryptionResult encrypted = loadEncryptedSecret();
        
        if (encrypted == null) {
            System.out.println("Error: Secret belum di-setup. Jalankan setup terlebih dahulu.");
            return;
        }
        
        // Dekripsi secret
        String secret = totpGuard.decrypt(encrypted, encryptionKey);
        
        // Generate kode TOTP saat ini
        String code = totpGuard.generateTotpCode(
            secret,
            TotpAlgorithm.SHA1,
            6,
            30
        );
        
        int remainingSeconds = totpGuard.getRemainingSeconds();
        
        System.out.println("\n=== TOTP Code ===");
        System.out.println("Kode: " + code);
        System.out.println("Valid selama: " + remainingSeconds + " detik");
        System.out.println("=================\n");
    }
    
    private static void verifyTotpCode(Scanner scanner) throws IOException {
        byte[] encryptionKey = loadOrGenerateEncryptionKey();
        EncryptionResult encrypted = loadEncryptedSecret();
        
        if (encrypted == null) {
            System.out.println("Error: Secret belum di-setup. Jalankan setup terlebih dahulu.");
            return;
        }
        
        System.out.print("Masukkan kode TOTP: ");
        String userCode = scanner.nextLine();
        
        // Dekripsi secret
        String secret = totpGuard.decrypt(encrypted, encryptionKey);
        
        // Verifikasi kode
        TotpVerificationResult result = totpGuard.verifyTotpCode(
            secret,
            userCode,
            1,  // time window tolerance
            TotpAlgorithm.SHA1,
            6,
            30
        );
        
        if (result.isValid()) {
            System.out.println("\n✓ Kode TOTP VALID!");
            System.out.println("Time offset: " + result.getTimeOffset());
        } else {
            System.out.println("\n✗ Kode TOTP TIDAK VALID!");
        }
    }
    
    private static void verifyBackupCode(Scanner scanner) throws IOException {
        List<String> hashedCodes = loadBackupCodes();
        
        if (hashedCodes == null || hashedCodes.isEmpty()) {
            System.out.println("Error: Backup codes belum di-setup.");
            return;
        }
        
        System.out.print("Masukkan backup code: ");
        String userCode = scanner.nextLine();
        
        // Verifikasi backup code
        BackupCodeVerificationResult result = totpGuard.verifyBackupCode(userCode, hashedCodes);
        
        if (result.isValid()) {
            System.out.println("\n✓ Backup code VALID!");
            
            // Hapus code yang sudah digunakan
            Integer codeIndex = result.getCodeIndex();
            if (codeIndex != null) {
                hashedCodes.remove(codeIndex.intValue());
                saveBackupCodes(hashedCodes);
                System.out.println("✓ Backup code telah dihapus dari daftar");
                System.out.println("Sisa backup codes: " + hashedCodes.size());
            }
        } else {
            System.out.println("\n✗ Backup code TIDAK VALID!");
        }
    }
    
    // Helper methods untuk penyimpanan data
    
    private static byte[] loadOrGenerateEncryptionKey() throws IOException {
        Path keyPath = Paths.get(ENCRYPTION_KEY_FILE);
        
        if (Files.exists(keyPath)) {
            return Files.readAllBytes(keyPath);
        } else {
            byte[] key = totpGuard.generateEncryptionKey();
            Files.write(keyPath, key);
            return key;
        }
    }
    
    private static void saveEncryptedSecret(EncryptionResult encrypted) throws IOException {
        String json = EncryptionResultExtensionsKt.toJson(encrypted);
        Files.write(Paths.get(SECRET_FILE), json.getBytes());
    }
    
    private static EncryptionResult loadEncryptedSecret() throws IOException {
        Path secretPath = Paths.get(SECRET_FILE);
        
        if (!Files.exists(secretPath)) {
            return null;
        }
        
        String json = new String(Files.readAllBytes(secretPath));
        return EncryptionResultExtensionsKt.fromJson(EncryptionResult.Companion, json);
    }
    
    private static void saveBackupCodes(List<String> hashedCodes) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(BACKUP_CODES_FILE))) {
            oos.writeObject(new ArrayList<>(hashedCodes));
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<String> loadBackupCodes() throws IOException {
        Path backupPath = Paths.get(BACKUP_CODES_FILE);
        
        if (!Files.exists(backupPath)) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(BACKUP_CODES_FILE))) {
            return (List<String>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error loading backup codes", e);
        }
    }
}
```

#### Contoh Penggunaan Sederhana

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import io.github.aribrilliantsyah.totpguard.model.TotpVerificationResult;

import java.util.Base64;

public class SimpleTotpExample {
    public static void main(String[] args) {
        TotpGuard totpGuard = TotpGuard.INSTANCE;
        
        // 1. Generate encryption key (simpan dengan aman!)
        byte[] encryptionKey = totpGuard.generateEncryptionKey();
        System.out.println("Encryption Key (Base64): " + 
            Base64.getEncoder().encodeToString(encryptionKey));
        
        // 2. Generate TOTP secret
        String secret = totpGuard.generateTotpSecret(32);
        System.out.println("TOTP Secret: " + secret);
        
        // 3. Encrypt secret untuk penyimpanan
        EncryptionResult encrypted = totpGuard.encrypt(secret, encryptionKey);
        System.out.println("Secret berhasil dienkripsi");
        
        // 4. Generate QR Code
        String otpAuthUri = totpGuard.generateOtpAuthUri(
            secret, 
            "user@example.com", 
            "MyApp"
        );
        String qrCodeBase64 = totpGuard.generateQrCodeBase64(otpAuthUri, 300);
        System.out.println("QR Code length: " + qrCodeBase64.length() + " characters");
        
        // 5. Generate TOTP code
        String currentCode = totpGuard.generateTotpCode(secret);
        System.out.println("Current TOTP Code: " + currentCode);
        
        // 6. Verify TOTP code
        TotpVerificationResult result = totpGuard.verifyTotpCode(secret, currentCode);
        System.out.println("Verification: " + (result.isValid() ? "VALID" : "INVALID"));
        
        // 7. Decrypt secret (untuk verifikasi atau penggunaan selanjutnya)
        String decryptedSecret = totpGuard.decrypt(encrypted, encryptionKey);
        System.out.println("Decrypted matches original: " + secret.equals(decryptedSecret));
    }
}
```

### 4. Serialisasi dan Penyimpanan Data

#### Serialisasi EncryptionResult

EncryptionResult dapat dikonversi ke JSON untuk disimpan di database:

```kotlin
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import io.github.aribrilliantsyah.totpguard.model.toJson
import io.github.aribrilliantsyah.totpguard.model.fromJson

// Serialisasi ke JSON untuk penyimpanan
val encryptedData: EncryptionResult = totpGuard.encrypt("secretData", encryptionKey)
val jsonForStorage = encryptedData.toJson()

// Deserialisasi dari JSON untuk penggunaan
val retrievedEncryptedData = EncryptionResult.fromJson(jsonFromStorage)
val decryptedData = totpGuard.decrypt(retrievedEncryptedData, encryptionKey)
```

Atau dalam Java:

```java
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import io.github.aribrilliantsyah.totpguard.model.EncryptionResultExtensionsKt;

// Serialisasi ke JSON untuk penyimpanan
EncryptionResult encryptedData = totpGuard.encrypt("secretData", encryptionKey);
String jsonForStorage = EncryptionResultExtensionsKt.toJson(encryptedData);

// Deserialisasi dari JSON untuk penggunaan
EncryptionResult retrievedEncryptedData = EncryptionResultExtensionsKt.fromJson(
    EncryptionResult.Companion, jsonFromStorage);
String decryptedData = totpGuard.decrypt(retrievedEncryptedData, encryptionKey);
```

#### Entity/Model untuk Database

```java
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "user_totp")
public class UserTotpEntity {
    @Id
    private Long userId;
    
    @Column(columnDefinition = "TEXT")
    private String encryptedSecretJson;
    
    @ElementCollection
    @CollectionTable(name = "user_backup_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "hashed_code")
    private List<String> hashedBackupCodes;
    
    @Column
    private boolean enabled;
    
    // Getters and setters
    
    // Helper method to get EncryptionResult
    public EncryptionResult getEncryptedSecret() {
        return EncryptionResultExtensionsKt.fromJson(
            EncryptionResult.Companion, this.encryptedSecretJson);
    }
    
    // Helper method to set EncryptionResult
    public void setEncryptedSecret(EncryptionResult encryptedSecret) {
        this.encryptedSecretJson = EncryptionResultExtensionsKt.toJson(encryptedSecret);
    }
}
```

### 5. Penggunaan di Android

```kotlin
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.aribrilliantsyah.totpguard.TotpGuard
import io.github.aribrilliantsyah.totpguard.model.BackupCodesResult
import io.github.aribrilliantsyah.totpguard.model.EncryptionResult
import io.github.aribrilliantsyah.totpguard.model.toJson
import io.github.aribrilliantsyah.totpguard.model.fromJson

class TotpManager(private val context: Context) {

    private val totpGuard = TotpGuard
    private val encryptionKey: ByteArray
    private val sharedPreferences: SharedPreferences

    init {
        // Setup AndroidKeystore and EncryptedSharedPreferences
        val masterKeySpec = KeyGenParameterSpec.Builder(
            "totp_preferences_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        val masterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(masterKeySpec)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "totp_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Generate or retrieve encryption key
        encryptionKey = if (sharedPreferences.contains("encryption_key")) {
            Base64.decode(sharedPreferences.getString("encryption_key", ""), Base64.DEFAULT)
        } else {
            val newKey = totpGuard.generateEncryptionKey()
            sharedPreferences.edit()
                .putString("encryption_key", Base64.encodeToString(newKey, Base64.DEFAULT))
                .apply()
            newKey
        }
    }

    fun setupTotp(accountName: String): TotpSetupData {
        // Buat secret baru
        val secret = totpGuard.generateTotpSecret()
        
        // Enkripsi secret
        val encrypted = totpGuard.encrypt(secret, encryptionKey)
        
        // Simpan ke SharedPreferences
        sharedPreferences.edit()
            .putString("encrypted_secret", encrypted.toJson())
            .apply()
        
        // Buat kode cadangan
        val backupCodes = totpGuard.generateBackupCodes()
        
        // Simpan kode cadangan ke SharedPreferences
        sharedPreferences.edit()
            .putStringSet("hashed_backup_codes", backupCodes.hashedCodes.toSet())
            .apply()
        
        // Buat URI dan QR Code
        val issuer = "AplikasiSaya"
        val uri = totpGuard.generateOtpAuthUri(secret, accountName, issuer)
        val qrCodeBase64 = totpGuard.generateQrCodeBase64(uri)
        
        // Kembalikan data untuk UI
        return TotpSetupData(
            qrCodeBase64 = qrCodeBase64,
            formattedBackupCodes = backupCodes.formattedCodes
        )
    }

    fun verifyTotp(code: String): Boolean {
        // Ambil secret terenkripsi dari SharedPreferences
        val encryptedJson = sharedPreferences.getString("encrypted_secret", null)
            ?: return false
            
        // Dekripsi secret
        val encrypted = EncryptionResult.fromJson(encryptedJson)
        val secret = totpGuard.decrypt(encrypted, encryptionKey)
        
        // Verifikasi kode
        val result = totpGuard.verifyTotpCode(secret, code)
        return result.isValid
    }

    fun getRemainingSeconds(): Int {
        return totpGuard.getRemainingSeconds()
    }
    
    fun verifyBackupCode(code: String): Boolean {
        // Ambil kode cadangan dari SharedPreferences
        val hashedCodes = sharedPreferences.getStringSet("hashed_backup_codes", emptySet())?.toList()
            ?: return false
            
        // Verifikasi kode
        val result = totpGuard.verifyBackupCode(code, hashedCodes)
        
        if (result.isValid && result.codeIndex != null) {
            // Hapus kode yang sudah digunakan
            val newHashedCodes = hashedCodes.toMutableList()
            newHashedCodes.removeAt(result.codeIndex)
            
            // Update SharedPreferences
            sharedPreferences.edit()
                .putStringSet("hashed_backup_codes", newHashedCodes.toSet())
                .apply()
        }
        
        return result.isValid
    }
    
    data class TotpSetupData(
        val qrCodeBase64: String,
        val formattedBackupCodes: List<String>
    )
}
```

Penggunaan dalam Activity atau Fragment:

```kotlin
class TotpSetupActivity : AppCompatActivity() {

    private lateinit var totpManager: TotpManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_totp_setup)
        
        totpManager = TotpManager(this)
        
        setupTotpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val setupData = totpManager.setupTotp(email)
            
            // Tampilkan QR Code
            val imageBytes = Base64.decode(setupData.qrCodeBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            qrCodeImageView.setImageBitmap(bitmap)
            
            // Tampilkan kode cadangan
            backupCodesTextView.text = setupData.formattedBackupCodes.joinToString("\n")
        }
        
        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString()
            val isValid = totpManager.verifyTotp(code)
            
            if (isValid) {
                Toast.makeText(this, "Kode valid!", Toast.LENGTH_SHORT).show()
                // Lanjutkan dengan autentikasi berhasil
            } else {
                Toast.makeText(this, "Kode tidak valid!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### 6. Penggunaan di iOS

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard
import platform.Foundation.*
import platform.UIKit.*

class TotpManager {
    private val totpGuard = TotpGuard
    private val encryptionKey: ByteArray
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    init {
        // Generate or retrieve encryption key
        val keyData = userDefaults.dataForKey("encryption_key")
        encryptionKey = if (keyData != null) {
            keyData.toByteArray()
        } else {
            val newKey = totpGuard.generateEncryptionKey()
            userDefaults.setData(newKey.toNSData(), "encryption_key")
            newKey
        }
    }
    
    fun setupTotp(accountName: String): Pair<String, List<String>> {
        val secret = totpGuard.generateTotpSecret()
        val encrypted = totpGuard.encrypt(secret, encryptionKey)
        
        // Save encrypted data to NSUserDefaults
        val encryptedData = NSMutableDictionary()
        encryptedData["ciphertext"] = encrypted.ciphertext.toNSData()
        encryptedData["iv"] = encrypted.iv.toNSData()
        encryptedData["authTag"] = encrypted.authTag.toNSData()
        userDefaults.setObject(encryptedData, "encrypted_secret")
        
        // Generate backup codes
        val backupCodes = totpGuard.generateBackupCodes()
        userDefaults.setObject(backupCodes.hashedCodes.toNSArray(), "hashed_backup_codes")
        
        // Generate QR Code
        val uri = totpGuard.generateOtpAuthUri(secret, accountName, "AplikasiSaya")
        val qrCodeBase64 = totpGuard.generateQrCodeBase64(uri)
        
        return Pair(qrCodeBase64, backupCodes.formattedCodes)
    }
    
    fun verifyTotp(code: String): Boolean {
        val encryptedData = userDefaults.dictionaryForKey("encrypted_secret") ?: return false
        
        // Reconstruct EncryptionResult
        val ciphertext = (encryptedData["ciphertext"] as? NSData)?.toByteArray() ?: return false
        val iv = (encryptedData["iv"] as? NSData)?.toByteArray() ?: return false
        val authTag = (encryptedData["authTag"] as? NSData)?.toByteArray() ?: return false
        
        val encrypted = EncryptionResult(ciphertext, iv, authTag)
        val secret = totpGuard.decrypt(encrypted, encryptionKey)
        
        val result = totpGuard.verifyTotpCode(secret, code)
        return result.isValid
    }
    
    private fun ByteArray.toNSData(): NSData {
        return NSData.dataWithBytes(this, size = this.size.toULong())
    }
    
    private fun NSData.toByteArray(): ByteArray {
        val bytes = ByteArray(this.length.toInt())
        this.getBytes(bytes)
        return bytes
    }
    
    private fun List<String>.toNSArray(): NSArray {
        val array = NSMutableArray()
        this.forEach { array.addObject(it) }
        return array
    }
}
```

## Struktur Proyek

Proyek ini menggunakan struktur Kotlin Multiplatform standard dengan 2 file build.gradle.kts:

### 1. Root Project (`build.gradle.kts`)
File ini berada di root proyek dan berfungsi untuk:
- Mendeklarasikan plugins yang digunakan oleh semua module
- Mendefinisikan task-task global untuk seluruh proyek
- Tidak mengandung konfigurasi library secara langsung

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}
```

### 2. Library Module (`library/build.gradle.kts`)
File ini berada di folder `library/` dan berfungsi untuk:
- Konfigurasi lengkap library multiplatform
- Menentukan target platform (JVM, Android, iOS)
- Mengatur dependencies untuk setiap platform
- Konfigurasi publishing ke Maven Central
- Menentukan `group` dan `version` library

```kotlin
group = "io.github.aribrilliantsyah"
version = "0.0.1-beta"

kotlin {
    jvm()
    androidTarget()
    ios()
    // ... sourceSets configuration
}
```

**Catatan Penting:**
- Kedua file ini diperlukan dan memiliki fungsi berbeda
- File root (`build.gradle.kts`) = konfigurasi proyek
- File library (`library/build.gradle.kts`) = konfigurasi module library yang akan dipublish
- Jangan menghapus salah satunya karena keduanya saling melengkapi

## Panduan Keamanan

### Praktik Terbaik

1. **Jangan pernah menyimpan secret TOTP dalam bentuk plain text**
   - Selalu enkripsi secret sebelum disimpan
   - Gunakan mekanisme penyimpanan aman khusus platform

2. **Lindungi kunci enkripsi**
   - Simpan kunci enkripsi di tempat yang aman
   - Jangan hardcode kunci enkripsi dalam kode

3. **Rotasi kunci secara berkala**
   - Ganti kunci enkripsi secara berkala
   - Gunakan fungsi `rotateKey` untuk memperbarui data terenkripsi

## Proses Kontribusi

Kontribusi sangat dihargai! Silakan buat pull request atau laporkan masalah melalui GitHub Issues.

## Lisensi

Library ini dilisensikan di bawah Apache License 2.0 - lihat file [LICENSE](LICENSE) untuk detailnya.