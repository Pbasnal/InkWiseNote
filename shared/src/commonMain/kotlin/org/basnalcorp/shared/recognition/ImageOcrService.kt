package org.basnalcorp.shared.recognition

/**
 * Phase 9.2: Image-to-text OCR (e.g. Tess-two, Azure Vision).
 * Android: real implementation (app sets via setImageOcrService or provides in Koin).
 * Desktop/JVM: no-op (isAvailable() = false, recognize returns failure).
 */
interface ImageOcrService {
    fun isAvailable(): Boolean
    suspend fun recognizeImage(imageBytes: ByteArray): Result<String>
}

/** Platform-provided instance. Android: set from app; JVM/iOS: no-op. */
expect fun createImageOcrService(): ImageOcrService
