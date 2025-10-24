# Installation Guide

Complete guide untuk instalasi dan setup TOTP-GUARD library.

## Table of Contents

- [Requirements](#requirements)
- [Method 1: Maven Local (Recommended for Development)](#method-1-maven-local-recommended-for-development)
- [Method 2: Composite Build (Active Development)](#method-2-composite-build-active-development)
- [Method 3: Local Repository](#method-3-local-repository)
- [Maven Central (Coming Soon)](#maven-central-coming-soon)
- [Troubleshooting](#troubleshooting)
- [Verification](#verification)

---

## Requirements

- **JDK**: 11 or higher
- **Kotlin**: 2.2.0 (handled by Gradle)
- **Gradle**: 8.2.1+ (use wrapper)
- **Maven**: 3.6+ (if using Maven)

---

## Method 1: Maven Local (Recommended for Development)

This method publishes the library to your local Maven repository (`~/.m2/repository`).

### Step 1: Clone and Build

```bash
# Clone repository
git clone https://github.com/aribrilliantsyah/totpguard.git
cd totpguard/kotlin-totp-lib

# Build library
./gradlew clean build
```

**Expected output:**
```
BUILD SUCCESSFUL in Xs
```

### Step 2: Publish to Maven Local

```bash
./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

**Expected output:**
```
BUILD SUCCESSFUL in Xs
16 actionable tasks: 10 executed, 6 up-to-date
```

### Step 3: Verify Publication

```bash
ls ~/.m2/repository/io/github/aribrilliantsyah/totpguard-jvm/0.0.1-beta/
```

**Should contain:**
- `totpguard-jvm-0.0.1-beta.jar`
- `totpguard-jvm-0.0.1-beta.pom`
- `totpguard-jvm-0.0.1-beta.module`

### Step 4: Add to Your Project

#### Gradle (Kotlin DSL)

**build.gradle.kts:**
```kotlin
repositories {
    mavenLocal()  // Must be FIRST
    mavenCentral()
    google()
}

dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
}
```

#### Gradle (Groovy)

**build.gradle:**
```groovy
repositories {
    mavenLocal()  // Must be FIRST
    mavenCentral()
    google()
}

dependencies {
    implementation 'io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta'
}
```

#### Maven

**pom.xml:**
```xml
<project>
    <repositories>
        <repository>
            <id>mavenLocal</id>
            <url>file://${user.home}/.m2/repository</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>io.github.aribrilliantsyah</groupId>
            <artifactId>totpguard-jvm</artifactId>
            <version>0.0.1-beta</version>
        </dependency>
    </dependencies>
</project>
```

### Step 5: Sync and Test

**Gradle:**
```bash
./gradlew clean build
```

**Maven:**
```bash
mvn clean install
```

---

## Method 2: Composite Build (Active Development)

Use this when you're actively developing both the library and your application.

### Advantages

- ✅ Changes in library immediately available
- ✅ No need to republish
- ✅ Easy debugging

### Setup

**1. Clone library in separate folder:**

```
~/projects/
├── my-app/           (your application)
└── totpguard/        (library)
    └── kotlin-totp-lib/
```

**2. In your app's `settings.gradle.kts`:**

```kotlin
includeBuild("../totpguard/kotlin-totp-lib") {
    dependencySubstitution {
        substitute(module("io.github.aribrilliantsyah:totpguard-jvm"))
            .using(project(":library"))
    }
}
```

**3. Add dependency normally:**

```kotlin
dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
}
```

### Usage

- Make changes in library
- Rebuild your app
- Changes are automatically included!

---

## Method 3: Local Repository

Useful for sharing library within a team without Maven Central.

### Step 1: Build to Local Repo

```bash
./gradlew :library:publish -PRELEASE_SIGNING_ENABLED=false
```

This creates a `dev-repo` folder in project root.

### Step 2: In Your Project

```kotlin
repositories {
    maven {
        url = uri("/path/to/totpguard/kotlin-totp-lib/dev-repo")
    }
    mavenCentral()
    google()
}

dependencies {
    implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
}
```

### Step 3: Share

- Commit `dev-repo` to git, OR
- Host on network drive, OR
- Sync via cloud storage

---

## Maven Central (Coming Soon)

When published to Maven Central, installation will be simpler:

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aribrilliantsyah:totp-guard-jvm:0.0.1-beta")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.aribrilliantsyah</groupId>
    <artifactId>totp-guard-jvm</artifactId>
    <version>0.0.1-beta</version>
</dependency>
```

**Note:** artifactId will be `totp-guard-jvm` (with hyphen) on Maven Central vs `totpguard-jvm` (no hyphen) for local.

---

## Troubleshooting

### Problem: "Cannot find io.github.aribrilliantsyah:totpguard-jvm"

**Solutions:**

1. Verify publication:
   ```bash
   ls ~/.m2/repository/io/github/aribrilliantsyah/totpguard-jvm/0.0.1-beta/
   ```

2. Ensure `mavenLocal()` is FIRST in repositories list

3. Try clearing cache:
   ```bash
   ./gradlew clean --refresh-dependencies
   # Or
   mvn clean install
   ```

4. Check dependency declaration:
   ```kotlin
   // Correct
   implementation("io.github.aribrilliantsyah:totpguard-jvm:0.0.1-beta")
   
   // Wrong
   implementation("io.github.aribrilliantsyah:totp-guard-jvm:0.0.1-beta")  // Wrong artifactId
   ```

### Problem: "Cannot perform signing task"

**Solution:**

Always add `-PRELEASE_SIGNING_ENABLED=false` flag:

```bash
./gradlew :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

### Problem: Version mismatch

**Solution:**

Check version in `library/build.gradle.kts`:

```kotlin
version = "0.0.1-beta"  // Current version
```

Use same version in your dependency declaration.

### Problem: Changes not picked up after update

**For Maven Local:**

```bash
# Republish library
cd totpguard/kotlin-totp-lib
./gradlew clean :library:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false

# Clear cache in your project
cd your-project
./gradlew clean --refresh-dependencies
```

**For Composite Build:**

Changes should be automatic. If not:

```bash
./gradlew clean build
```

### Problem: "Missing artifact" in Maven

**Solutions:**

1. Verify artifactId: use `totpguard-jvm` (not `totp-guard-jvm`)
2. Add `mavenLocal` repository
3. Reload Maven:
   ```bash
   mvn clean install
   # Or in IDE: Maven > Reload Project
   ```

---

## Verification

Test that installation works:

### Kotlin Test

```kotlin
import io.github.aribrilliantsyah.totpguard.TotpGuard

fun main() {
    val secret = TotpGuard.generateTotpSecret()
    val code = TotpGuard.generateTotpCode(secret)
    val valid = TotpGuard.verifyTotpCode(secret, code).isValid
    
    println("Secret: $secret")
    println("Code: $code")
    println("Valid: $valid")  // Should be true
}
```

### Java Test

```java
import io.github.aribrilliantsyah.totpguard.TotpGuard;

public class Test {
    public static void main(String[] args) {
        TotpGuard totp = TotpGuard.INSTANCE;
        
        String secret = totp.generateTotpSecret();
        String code = totp.generateTotpCode(secret);
        boolean valid = totp.verifyTotpCode(secret, code).isValid();
        
        System.out.println("Secret: " + secret);
        System.out.println("Code: " + code);
        System.out.println("Valid: " + valid);  // Should be true
    }
}
```

**Expected output:**
```
Secret: JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP
Code: 123456
Valid: true
```

---

## Next Steps

After successful installation:

1. Read [Usage Examples](../USAGE_EXAMPLES.md) for complete examples
2. Check [API Reference](API_REFERENCE.md) for all available functions
3. See [Spring Boot Guide](SPRING_BOOT_GUIDE.md) for integration examples

---

## Additional Resources

- [README](../README.md) - Main documentation
- [CHANGELOG](../CHANGELOG.md) - Version history
- [QUICKSTART_MAC](../QUICKSTART_MAC.md) - Mac-specific quick start
- [IOS_BUILD_FIX](../IOS_BUILD_FIX.md) - iOS compilation fixes

---

Need help? [Open an issue](https://github.com/aribrilliantsyah/totpguard/issues)
