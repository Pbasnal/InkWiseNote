package org.basnalcorp.shared.recognition

/**
 * No-op implementations for platforms that don't support recognition (Phase 9.1 / 9.2).
 */
object NoOpDigitalInkRecognition : DigitalInkRecognition {
    override fun isAvailable(): Boolean = false
    override suspend fun recognize(strokes: List<InkStroke>): Result<String> =
        Result.failure(UnsupportedOperationException("Digital ink recognition not available on this platform"))
}

object NoOpImageOcrService : ImageOcrService {
    override fun isAvailable(): Boolean = false
    override suspend fun recognizeImage(imageBytes: ByteArray): Result<String> =
        Result.failure(UnsupportedOperationException("Image OCR not available on this platform"))
}
