plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.vanniktech.publish) apply false
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