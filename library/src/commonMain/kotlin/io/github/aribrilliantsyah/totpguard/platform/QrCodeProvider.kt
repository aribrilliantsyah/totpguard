package io.github.aribrilliantsyah.totpguard.platform

/**
 * Platform-specific QR code generation.
 * Each platform must provide an actual implementation.
 */
expect class QrCodeProvider() {
    
    /**
     * Generates a QR code as PNG image bytes
     * 
     * @param data The data to encode in the QR code
     * @param size The size of the QR code in pixels
     * @return PNG image data as ByteArray
     */
    fun generateQrCodePng(data: String, size: Int): ByteArray
    
    /**
     * Generates a QR code as a Base64-encoded PNG image
     * 
     * @param data The data to encode in the QR code
     * @param size The size of the QR code in pixels
     * @return Base64-encoded PNG image
     */
    fun generateQrCodeBase64(data: String, size: Int): String
}
