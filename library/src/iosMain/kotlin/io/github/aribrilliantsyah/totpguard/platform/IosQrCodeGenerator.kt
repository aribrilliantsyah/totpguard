package io.github.aribrilliantsyah.totpguard.platform

import io.github.aribrilliantsyah.totpguard.qr.QrCodeGenerator

class IosQrCodeGenerator : QrCodeGenerator() {
    override fun generateQrCode(otpauthUri: String): ByteArray {
        // iOS-specific implementation for generating QR code from otpauth URI
        // This is a placeholder for actual QR code generation logic
        return ByteArray(0) // Replace with actual QR code byte array
    }
}