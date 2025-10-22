package io.github.aribrilliantsyah.totpguard.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Base64

/**
 * Extensions for serializing and deserializing EncryptionResult for storage
 */
fun EncryptionResult.toJson(): String {
    val mapper = ObjectMapper()
    val data = mapOf(
        "ciphertext" to Base64.getEncoder().encodeToString(this.ciphertext),
        "iv" to Base64.getEncoder().encodeToString(this.iv),
        "authTag" to Base64.getEncoder().encodeToString(this.authTag)
    )
    return mapper.writeValueAsString(data)
}

/**
 * Deserialize EncryptionResult from JSON string
 */
fun fromJson(json: String): EncryptionResult {
    val mapper = ObjectMapper()
    val data: Map<String, String> = mapper.readValue(json)
    
    return EncryptionResult(
        ciphertext = Base64.getDecoder().decode(data["ciphertext"]),
        iv = Base64.getDecoder().decode(data["iv"]),
        authTag = Base64.getDecoder().decode(data["authTag"])
    )
}