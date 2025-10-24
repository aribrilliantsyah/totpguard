package io.github.aribrilliantsyah.totpguard.model

import io.github.aribrilliantsyah.totpguard.platform.Base64Provider

/**
 * Extensions for serializing and deserializing EncryptionResult for storage
 */
fun EncryptionResult.toJson(): String {
    val base64Provider = Base64Provider()
    val ciphertextB64 = base64Provider.encode(this.ciphertext)
    val ivB64 = base64Provider.encode(this.iv)
    val authTagB64 = base64Provider.encode(this.authTag)
    
    return """{"ciphertext":"$ciphertextB64","iv":"$ivB64","authTag":"$authTagB64"}"""
}

/**
 * Deserialize EncryptionResult from JSON string
 */
fun fromJson(json: String): EncryptionResult {
    val base64Provider = Base64Provider()
    
    // Simple JSON parsing
    val ciphertextB64 = json.substringAfter("\"ciphertext\":\"").substringBefore("\"")
    val ivB64 = json.substringAfter("\"iv\":\"").substringBefore("\"")
    val authTagB64 = json.substringAfter("\"authTag\":\"").substringBefore("\"")
    
    return EncryptionResult(
        ciphertext = base64Provider.decode(ciphertextB64),
        iv = base64Provider.decode(ivB64),
        authTag = base64Provider.decode(authTagB64)
    )
}