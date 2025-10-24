# Spring Boot Integration Guide

Complete guide for integrating TOTP-GUARD in Spring Boot applications.

## Table of Contents

- [Setup](#setup)
- [Configuration](#configuration)
- [Service Layer](#service-layer)
- [Controller Layer](#controller-layer)
- [Entity/Model Layer](#entity-model-layer)
- [Repository Layer](#repository-layer)
- [Security Integration](#security-integration)
- [Testing](#testing)
- [Best Practices](#best-practices)

---

## Setup

### 1. Add Dependency

**build.gradle.kts:**
```kotlin
dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Database
    implementation("org.postgresql:postgresql")
}
```

**pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>io.github.aribrilliantsyah</groupId>
        <artifactId>totpguard-jvm</artifactId>
        <version>0.0.1-beta</version>
    </dependency>
    
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

---

## Configuration

### application.yml

```yaml
totpguard:
  encryption:
    # Generate with: TotpGuard.generateEncryptionKey() then Base64 encode
    key: ${TOTP_ENCRYPTION_KEY}
  
  # Optional customization
  settings:
    algorithm: SHA1  # SHA1, SHA256, SHA512
    digits: 6        # 6 or 8
    period: 30       # seconds
    time-window: 1   # for verification tolerance

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

### Configuration Class

**Java:**
```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "totpguard")
public class TotpConfig {
    private Encryption encryption;
    private Settings settings = new Settings();
    
    // Getters and setters
    public Encryption getEncryption() { return encryption; }
    public void setEncryption(Encryption encryption) { this.encryption = encryption; }
    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }
    
    public static class Encryption {
        private String key;
        
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }
    
    public static class Settings {
        private String algorithm = "SHA1";
        private int digits = 6;
        private int period = 30;
        private int timeWindow = 1;
        
        // Getters and setters
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public int getDigits() { return digits; }
        public void setDigits(int digits) { this.digits = digits; }
        public int getPeriod() { return period; }
        public void setPeriod(int period) { this.period = period; }
        public int getTimeWindow() { return timeWindow; }
        public void setTimeWindow(int timeWindow) { this.timeWindow = timeWindow; }
    }
}
```

**Kotlin:**
```kotlin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "totpguard")
data class TotpConfig(
    var encryption: Encryption = Encryption(),
    var settings: Settings = Settings()
) {
    data class Encryption(
        var key: String = ""
    )
    
    data class Settings(
        var algorithm: String = "SHA1",
        var digits: Int = 6,
        var period: Int = 30,
        var timeWindow: Int = 1
    )
}
```

---

## Service Layer

### Java Implementation

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;
import io.github.aribrilliantsyah.totpguard.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

@Service
public class TotpService {
    private final TotpGuard totpGuard = TotpGuard.INSTANCE;
    private final UserRepository userRepository;
    private final byte[] encryptionKey;
    
    public TotpService(UserRepository userRepository, TotpConfig config) {
        this.userRepository = userRepository;
        this.encryptionKey = Base64.getDecoder().decode(config.getEncryption().getKey());
    }
    
    /**
     * Setup TOTP for a user
     */
    @Transactional
    public TotpSetupResponse setupTotp(Long userId, String userEmail) {
        // 1. Generate secret
        String secret = totpGuard.generateTotpSecret(32);
        
        // 2. Encrypt secret
        EncryptionResult encrypted = totpGuard.encrypt(secret, encryptionKey);
        
        // 3. Generate backup codes
        BackupCodesResult backupCodes = totpGuard.generateBackupCodes(10, 8);
        
        // 4. Generate QR code
        String uri = totpGuard.generateOtpAuthUri(
            secret,
            userEmail,
            "MyApplication"
        );
        String qrCodeBase64 = totpGuard.generateQrCodeBase64(uri, 300);
        
        // 5. Save to database
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setTotpEnabled(true);
        user.setTotpSecret(encrypted.toBase64String());
        user.setBackupCodes(backupCodes.getHashedCodes());
        userRepository.save(user);
        
        // 6. Return response (show backup codes ONLY ONCE!)
        return new TotpSetupResponse(
            qrCodeBase64,
            backupCodes.getFormattedCodes()
        );
    }
    
    /**
     * Verify TOTP code
     */
    public boolean verifyTotpCode(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (!user.isTotpEnabled()) {
            throw new TotpNotEnabledException("TOTP not enabled for this user");
        }
        
        // Decrypt secret
        EncryptionResult encrypted = EncryptionResult
            .Companion
            .fromBase64String(user.getTotpSecret());
        String secret = totpGuard.decrypt(encrypted, encryptionKey);
        
        // Verify code (SIMPLE! Just 2 parameters with defaults)
        TotpVerificationResult result = totpGuard.verifyTotpCode(secret, code);
        
        return result.isValid();
    }
    
    /**
     * Verify backup code
     */
    @Transactional
    public boolean verifyBackupCode(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        List<String> hashedCodes = user.getBackupCodes();
        BackupCodeVerificationResult result = totpGuard.verifyBackupCode(code, hashedCodes);
        
        if (result.isValid()) {
            // Remove used backup code
            hashedCodes.remove(result.getMatchedHash());
            user.setBackupCodes(hashedCodes);
            userRepository.save(user);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Disable TOTP for user
     */
    @Transactional
    public void disableTotp(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        user.setBackupCodes(null);
        userRepository.save(user);
    }
    
    /**
     * Rotate encryption key
     */
    @Transactional
    public void rotateEncryptionKey(byte[] newKey) {
        List<User> users = userRepository.findAllWithTotpEnabled();
        
        for (User user : users) {
            EncryptionResult old = EncryptionResult
                .Companion
                .fromBase64String(user.getTotpSecret());
            
            EncryptionResult reencrypted = totpGuard.rotateKey(old, encryptionKey, newKey);
            
            user.setTotpSecret(reencrypted.toBase64String());
        }
        
        userRepository.saveAll(users);
    }
    
    /**
     * Get remaining seconds for current code
     */
    public int getRemainingSeconds() {
        return totpGuard.getRemainingSeconds();
    }
}

// Response DTOs
class TotpSetupResponse {
    private final String qrCodeBase64;
    private final List<String> backupCodes;
    
    public TotpSetupResponse(String qrCodeBase64, List<String> backupCodes) {
        this.qrCodeBase64 = qrCodeBase64;
        this.backupCodes = backupCodes;
    }
    
    public String getQrCodeBase64() { return qrCodeBase64; }
    public List<String> getBackupCodes() { return backupCodes; }
}
```

### Kotlin Implementation

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard
import io.github.aribrilliantsyah.totpguard.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64

@Service
class TotpService(
    private val userRepository: UserRepository,
    config: TotpConfig
) {
    private val totpGuard = TotpGuard
    private val encryptionKey = Base64.getDecoder().decode(config.encryption.key)
    
    @Transactional
    fun setupTotp(userId: Long, userEmail: String): TotpSetupResponse {
        // 1. Generate secret
        val secret = totpGuard.generateTotpSecret()
        
        // 2. Encrypt secret
        val encrypted = totpGuard.encrypt(secret, encryptionKey)
        
        // 3. Generate backup codes
        val backupCodes = totpGuard.generateBackupCodes()
        
        // 4. Generate QR code
        val uri = totpGuard.generateOtpAuthUri(
            secret = secret,
            accountName = userEmail,
            issuer = "MyApplication"
        )
        val qrCodeBase64 = totpGuard.generateQrCodeBase64(uri)
        
        // 5. Save to database
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        
        user.totpEnabled = true
        user.totpSecret = encrypted.toBase64String()
        user.backupCodes = backupCodes.hashedCodes
        userRepository.save(user)
        
        return TotpSetupResponse(
            qrCodeBase64 = qrCodeBase64,
            backupCodes = backupCodes.formattedCodes
        )
    }
    
    fun verifyTotpCode(userId: Long, code: String): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        
        require(user.totpEnabled) { "TOTP not enabled for this user" }
        
        // Decrypt secret
        val encrypted = EncryptionResult.fromBase64String(user.totpSecret!!)
        val secret = totpGuard.decrypt(encrypted, encryptionKey)
        
        // Verify code (SIMPLE! 2 parameters with defaults)
        val result = totpGuard.verifyTotpCode(secret, code)
        
        return result.isValid
    }
    
    @Transactional
    fun verifyBackupCode(userId: Long, code: String): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        
        val hashedCodes = user.backupCodes ?: return false
        val result = totpGuard.verifyBackupCode(code, hashedCodes)
        
        if (result.isValid) {
            // Remove used backup code
            user.backupCodes = hashedCodes - result.matchedHash!!
            userRepository.save(user)
            return true
        }
        
        return false
    }
    
    @Transactional
    fun disableTotp(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        
        user.totpEnabled = false
        user.totpSecret = null
        user.backupCodes = null
        userRepository.save(user)
    }
    
    fun getRemainingSeconds(): Int = totpGuard.getRemainingSeconds()
}

data class TotpSetupResponse(
    val qrCodeBase64: String,
    val backupCodes: List<String>
)
```

---

## Controller Layer

### Java Controller

```java
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/totp")
public class TotpController {
    private final TotpService totpService;
    
    public TotpController(TotpService totpService) {
        this.totpService = totpService;
    }
    
    @PostMapping("/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        TotpSetupResponse response = totpService.setupTotp(
            principal.getId(),
            principal.getEmail()
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verifyCode(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody VerifyRequest request
    ) {
        boolean isValid = totpService.verifyTotpCode(
            principal.getId(),
            request.getCode()
        );
        
        return ResponseEntity.ok(new VerifyResponse(isValid));
    }
    
    @PostMapping("/backup-code/verify")
    public ResponseEntity<VerifyResponse> verifyBackupCode(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody VerifyRequest request
    ) {
        boolean isValid = totpService.verifyBackupCode(
            principal.getId(),
            request.getCode()
        );
        
        return ResponseEntity.ok(new VerifyResponse(isValid));
    }
    
    @DeleteMapping("/disable")
    public ResponseEntity<Void> disableTotp(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        totpService.disableTotp(principal.getId());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/remaining-time")
    public ResponseEntity<TimeResponse> getRemainingTime() {
        int seconds = totpService.getRemainingSeconds();
        return ResponseEntity.ok(new TimeResponse(seconds));
    }
}

// Request/Response DTOs
class VerifyRequest {
    private String code;
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

class VerifyResponse {
    private boolean valid;
    
    public VerifyResponse(boolean valid) { this.valid = valid; }
    public boolean isValid() { return valid; }
}

class TimeResponse {
    private int remainingSeconds;
    
    public TimeResponse(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
    
    public int getRemainingSeconds() { return remainingSeconds; }
}
```

### Kotlin Controller

```kotlin
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth/totp")
class TotpController(
    private val totpService: TotpService
) {
    
    @PostMapping("/setup")
    fun setupTotp(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TotpSetupResponse> {
        val response = totpService.setupTotp(principal.id, principal.email)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/verify")
    fun verifyCode(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: VerifyRequest
    ): ResponseEntity<VerifyResponse> {
        val isValid = totpService.verifyTotpCode(principal.id, request.code)
        return ResponseEntity.ok(VerifyResponse(isValid))
    }
    
    @PostMapping("/backup-code/verify")
    fun verifyBackupCode(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: VerifyRequest
    ): ResponseEntity<VerifyResponse> {
        val isValid = totpService.verifyBackupCode(principal.id, request.code)
        return ResponseEntity.ok(VerifyResponse(isValid))
    }
    
    @DeleteMapping("/disable")
    fun disableTotp(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Void> {
        totpService.disableTotp(principal.id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/remaining-time")
    fun getRemainingTime(): ResponseEntity<TimeResponse> {
        val seconds = totpService.getRemainingSeconds()
        return ResponseEntity.ok(TimeResponse(seconds))
    }
}

data class VerifyRequest(val code: String)
data class VerifyResponse(val valid: Boolean)
data class TimeResponse(val remainingSeconds: Int)
```

---

## Entity/Model Layer

### User Entity (JPA)

**Java:**
```java
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "totp_enabled")
    private boolean totpEnabled = false;
    
    @Column(name = "totp_secret", columnDefinition = "TEXT")
    private String totpSecret;  // Base64-encoded EncryptionResult
    
    @ElementCollection
    @CollectionTable(name = "user_backup_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "hashed_code")
    private List<String> backupCodes = new ArrayList<>();
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isTotpEnabled() { return totpEnabled; }
    public void setTotpEnabled(boolean totpEnabled) { this.totpEnabled = totpEnabled; }
    
    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }
    
    public List<String> getBackupCodes() { return backupCodes; }
    public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }
}
```

**Kotlin:**
```kotlin
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(name = "totp_enabled")
    var totpEnabled: Boolean = false,
    
    @Column(name = "totp_secret", columnDefinition = "TEXT")
    var totpSecret: String? = null,
    
    @ElementCollection
    @CollectionTable(name = "user_backup_codes", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "hashed_code")
    var backupCodes: List<String>? = null
)
```

### Database Migration (Flyway/Liquibase)

**SQL:**
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    totp_enabled BOOLEAN DEFAULT FALSE,
    totp_secret TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_backup_codes (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hashed_code VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, hashed_code)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_totp_enabled ON users(totp_enabled);
```

---

## Repository Layer

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.totpEnabled = true")
    List<User> findAllWithTotpEnabled();
}
```

---

## Security Integration

### Custom Authentication Filter

```kotlin
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class TotpAuthenticationFilter(
    private val totpService: TotpService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication != null && authentication.isAuthenticated) {
            val principal = authentication.principal as? UserPrincipal
            
            if (principal?.requiresTotp() == true) {
                val totpCode = request.getHeader("X-TOTP-Code")
                
                if (totpCode != null) {
                    val isValid = totpService.verifyTotpCode(principal.id, totpCode)
                    
                    if (isValid) {
                        // Grant full access
                        principal.setTotpVerified(true)
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid TOTP code")
                        return
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TOTP code required")
                    return
                }
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
```

---

## Testing

### Service Test

```kotlin
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TotpServiceTest {
    
    private val userRepository = mockk<UserRepository>()
    private val config = mockk<TotpConfig>()
    private val totpService = TotpService(userRepository, config)
    
    @Test
    fun `setupTotp should generate QR code and backup codes`() {
        // Given
        val userId = 1L
        val email = "test@example.com"
        val user = User(id = userId, email = email, password = "hashed")
        
        every { config.encryption.key } returns Base64.getEncoder().encodeToString(ByteArray(32))
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user
        
        // When
        val result = totpService.setupTotp(userId, email)
        
        // Then
        assertNotNull(result.qrCodeBase64)
        assertEquals(10, result.backupCodes.size)
        verify { userRepository.save(any()) }
    }
    
    @Test
    fun `verifyTotpCode should return true for valid code`() {
        // Test implementation
    }
}
```

---

## Best Practices

### 1. Key Management

```kotlin
// ✅ GOOD: Store in environment variable
val key = System.getenv("TOTP_ENCRYPTION_KEY")

// ❌ BAD: Hardcode in source
val key = "my-secret-key-123"
```

### 2. Error Handling

```kotlin
@ControllerAdvice
class TotpExceptionHandler {
    
    @ExceptionHandler(TotpNotEnabledException::class)
    fun handleTotpNotEnabled(ex: TotpNotEnabledException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("TOTP not enabled for this user"))
    }
}
```

### 3. Rate Limiting

```kotlin
@Component
class TotpRateLimiter {
    private val bucket = Bucket.builder()
        .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
        .build()
    
    fun checkRateLimit(): Boolean {
        return bucket.tryConsume(1)
    }
}
```

### 4. Audit Logging

```kotlin
@Aspect
@Component
class TotpAuditAspect {
    
    @AfterReturning("@annotation(AuditTotp)")
    fun logTotpOperation(joinPoint: JoinPoint) {
        logger.info("TOTP operation: ${joinPoint.signature.name}")
    }
}
```

---

## Next Steps

- See [API_REFERENCE.md](API_REFERENCE.md) for complete API documentation
- Check [USAGE_EXAMPLES.md](../USAGE_EXAMPLES.md) for more examples
- Read [README.md](../README.md) for quick start guide

---

Need help? [Open an issue](https://github.com/aribrilliantsyah/totpguard/issues)
