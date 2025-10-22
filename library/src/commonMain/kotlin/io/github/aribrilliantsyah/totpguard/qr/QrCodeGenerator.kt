package io.github.aribrilliantsyah.totpguard.qr

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Base64

/**
 * Generates QR codes for TOTP setup
 */
class QrCodeGenerator {

    /**
     * Generates a QR code as a byte array containing a PNG image
     *
     * @param otpauthUri The otpauth:// URI to encode
     * @param size The size of the QR code in pixels
     * @return A byte array containing the PNG image
     */
    fun generateQrCode(otpauthUri: String, size: Int = 300): ByteArray {
        val image = generateQrCodeImage(otpauthUri, size, size)
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }

    /**
     * Generates a QR code as a Base64-encoded PNG image
     *
     * @param otpauthUri The otpauth:// URI to encode
     * @param size The size of the QR code in pixels
     * @return A Base64-encoded PNG image
     */
    fun generateQrCodeBase64(otpauthUri: String, size: Int = 300): String {
        val pngBytes = generateQrCode(otpauthUri, size)
        return Base64.getEncoder().encodeToString(pngBytes)
    }

    /**
     * Generates a QR code as a BufferedImage
     *
     * @param otpauthUri The otpauth:// URI to encode
     * @param width The width of the QR code in pixels
     * @param height The height of the QR code in pixels
     * @return A BufferedImage containing the QR code
     */
    private fun generateQrCodeImage(otpauthUri: String, width: Int = 300, height: Int = 300): BufferedImage {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix: BitMatrix = qrCodeWriter.encode(otpauthUri, BarcodeFormat.QR_CODE, width, height)
            MatrixToImageWriter.toBufferedImage(bitMatrix)
        } catch (e: WriterException) {
            throw RuntimeException("Error generating QR Code", e)
        }
    }
}