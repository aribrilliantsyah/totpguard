plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

// Add OWASP Dependency-Check plugin for a local vulnerability scan
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.owasp:dependency-check-gradle:12.1.0")
    }
}

apply(plugin = "org.owasp.dependencycheck")

// configure dependency-check extension if available
extensions.findByName("dependencyCheck")?.let {
    val ext = it as org.gradle.api.plugins.ExtensionAware
    try {
        // use reflection-safe configuration to avoid compile-time type dependency
        ext.extensions.extraProperties.set("failBuildOnCVSS", 0.0F)
        ext.extensions.extraProperties.set("formats", listOf("HTML", "XML", "JSON"))
        ext.extensions.extraProperties.set("outputDirectory", file("${buildDir}/reports/dependency-check"))
    } catch (_: Throwable) {
        // best-effort; if setting via reflection fails, plugin will use defaults
    }
}

// Custom task to generate SNAPSHOT version with timestamp
tasks.register("devVersion") {
    doLast {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd.HHmmss").format(java.util.Date())
        val snapshotVersion = "1.0.0-dev.$timestamp"
        
        // Write the version to a properties file
        val versionFile = file("$rootDir/version.properties")
        versionFile.writeText("version=$snapshotVersion")
        println("Version set to: $snapshotVersion")
    }
}