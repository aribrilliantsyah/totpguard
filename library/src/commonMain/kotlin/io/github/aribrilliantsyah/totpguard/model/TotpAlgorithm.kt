package io.github.aribrilliantsyah.totpguard.model

/**
 * Supported hash algorithms for TOTP generation
 */
enum class TotpAlgorithm {
    SHA1,
    SHA256,
    SHA512
}