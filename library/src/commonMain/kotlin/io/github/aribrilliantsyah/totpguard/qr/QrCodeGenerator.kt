package io.github.aribrilliantsyah.totpguard.qr

import io.github.aribrilliantsyah.totpguard.platform.QrCodeProvider

/**
 * Generates QR codes for TOTP setup
 */
class QrCodeGenerator {
    private val qrCodeProvider = QrCodeProvider()

    /**
     * Generates a QR code as a byte array containing a PNG image
     *
     * @param otpauthUri The otpauth:// URI to encode
     * @param size The size of the QR code in pixels
     * @return A byte array containing the PNG image
     */
    fun generateQrCode(otpauthUri: String, size: Int = 300): ByteArray {
        return qrCodeProvider.generateQrCodePng(otpauthUri, size)
    }

    /**
     * Generates a QR code as a Base64-encoded PNG image
     *
     * @param otpauthUri The otpauth:// URI to encode
     * @param size The size of the QR code in pixels
     * @return A Base64-encoded PNG image
     */
    fun generateQrCodeBase64(otpauthUri: String, size: Int = 300): String {
        return qrCodeProvider.generateQrCodeBase64(otpauthUri, size)
    }
}