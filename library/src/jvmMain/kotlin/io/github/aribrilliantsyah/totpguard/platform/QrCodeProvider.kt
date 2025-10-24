package io.github.aribrilliantsyah.totpguard.platform

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

/**
 * JVM implementation of QR code generation using ZXing library
 */
actual class QrCodeProvider {
    
    actual fun generateQrCodePng(data: String, size: Int): ByteArray {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size)
            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            
            val baos = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "png", baos)
            baos.toByteArray()
        } catch (e: WriterException) {
            throw RuntimeException("Error generating QR Code", e)
        }
    }
    
    actual fun generateQrCodeBase64(data: String, size: Int): String {
        val pngBytes = generateQrCodePng(data, size)
        return Base64.getEncoder().encodeToString(pngBytes)
    }
}
