import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // alias(libs.plugins.androidLibrary)  // Uncomment when Android SDK is configured
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.publish)
}

group = "io.github.aribrilliantsyah"
version = project.findProperty("release.version") as String? ?: "0.0.1-beta"

kotlin {
    jvmToolchain(11) // Set Java 11 for all targets
    
    jvm {
        mavenPublication {
            artifactId = "totpguard-jvm"
        }
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    // Android target - uncomment when Android SDK is configured
    // androidTarget {
    //     publishLibraryVariants("release", "debug")
    //     compilations.configureEach {
    //         kotlinOptions {
    //             jvmTarget = "11"
    //         }
    //     }
    // }
    
    // iOS target - uncomment when needed
    // ios()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.bcprov.jdk15to18)
                implementation(libs.zxing.core)
                implementation(libs.zxing.javase)
                implementation(libs.commons.codec)
                implementation(libs.jbcrypt)
                implementation(libs.jackson.databind)
                implementation(libs.jackson.kotlin)
            }
        }
        val jvmTest by getting
        // Android and iOS source sets - uncomment when targets are enabled
        // val androidMain by getting {
        //     dependencies {
        //         implementation(libs.android.security.crypto)
        //         implementation(libs.zxing.core)
        //         implementation(libs.bcrypt.android)
        //         implementation(libs.jackson.databind)
        //         implementation(libs.jackson.kotlin)
        //     }
        // }
        // val androidUnitTest by getting
        // val iosMain by getting
        // val iosTest by getting
    }
}

// Android configuration - uncomment when Android SDK is configured
// android {
//     namespace = "io.github.aribrilliantsyah.totpguard"
//     compileSdk = libs.versions.androidCompileSdk.get().toInt()
//     
//     defaultConfig {
//         minSdk = libs.versions.androidMinSdk.get().toInt()
//         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//     }
//     
//     compileOptions {
//         sourceCompatibility = JavaVersion.VERSION_11
//         targetCompatibility = JavaVersion.VERSION_11
//     }
//     
//     buildFeatures {
//         buildConfig = false
//     }
// }

// Local development repository
publishing {
    publications.withType<MavenPublication> {
        // Set artifactId untuk metadata publication
        if (artifactId == "library") {
            artifactId = "totpguard"
        }
    }
    repositories {
        maven {
            name = "DevRepo"
            url = uri("${rootProject.projectDir}/dev-repo")
        }
    }
}