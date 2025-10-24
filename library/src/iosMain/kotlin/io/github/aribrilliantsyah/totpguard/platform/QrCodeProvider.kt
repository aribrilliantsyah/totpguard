package io.github.aribrilliantsyah.totpguard.platform

import platform.CoreImage.*
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * iOS implementation of QR code generation using CoreImage
 */
@OptIn(ExperimentalForeignApi::class)
actual class QrCodeProvider {
    
    actual fun generateQrCodePng(data: String, size: Int): ByteArray {
        val nsData = data.toNSData()
        
        // Create QR code filter
        val filter = CIFilter.filterWithName("CIQRCodeGenerator")
            ?: throw RuntimeException("QR Code filter not available")
        
        filter.setValue(nsData, forKey = "inputMessage")
        filter.setValue("H", forKey = "inputCorrectionLevel") // High error correction
        
        val outputImage = filter.outputImage
            ?: throw RuntimeException("Failed to generate QR code")
        
        // Scale the image
        val scaleX = size.toDouble() / outputImage.extent.useContents { this.size.width }
        val scaleY = size.toDouble() / outputImage.extent.useContents { this.size.height }
        
        val scaledImage = outputImage.imageByApplyingTransform(
            CGAffineTransformMakeScale(scaleX, scaleY)
        )
        
        // Convert CIImage to UIImage
        val context = CIContext.contextWithOptions(null)
        val cgImage = context.createCGImage(scaledImage, fromRect = scaledImage.extent)
            ?: throw RuntimeException("Failed to create CG image")
        
        val uiImage = UIImage.imageWithCGImage(cgImage)
        
        // Convert to PNG data
        val pngData = UIImagePNGRepresentation(uiImage)
            ?: throw RuntimeException("Failed to convert image to PNG")
        
        return pngData.toByteArray()
    }
    
    actual fun generateQrCodeBase64(data: String, size: Int): String {
        val pngBytes = generateQrCodePng(data, size)
        val nsData = pngBytes.toNSData()
        return nsData.base64EncodedStringWithOptions(0)
    }
}

// Extension functions for data conversion
private fun String.toNSData(): NSData {
    return this.encodeToByteArray().toNSData()
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.refTo(0).getPointer(MemScope()), length = this.size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, size.toULong())
        }
    }
    return bytes
}
