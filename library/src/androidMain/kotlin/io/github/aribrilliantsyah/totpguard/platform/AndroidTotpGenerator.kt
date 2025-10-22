package io.github.aribrilliantsyah.totpguard.platform

import io.github.aribrilliantsyah.totpguard.auth.TotpGenerator
import io.github.aribrilliantsyah.totpguard.model.TotpData

class AndroidTotpGenerator : TotpGenerator {
    override fun generateSecret(): String {
        // Implementation for generating TOTP secret on Android
    }

    override fun generateCode(secret: String, time: Long): String {
        // Implementation for generating TOTP code on Android
    }

    override fun verifyCode(secret: String, code: String, time: Long): Boolean {
        // Implementation for verifying TOTP code on Android
    }

    override fun getRemainingTime(time: Long): Long {
        // Implementation for getting remaining time until TOTP code expiration on Android
    }
}