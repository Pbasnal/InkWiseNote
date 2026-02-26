package org.basnalcorp.shared.recognition

/**
 * Phase 9.1: Digital ink (handwriting) recognition.
 * Android: ML Kit implementation (app sets via setDigitalInkRecognition).
 * Desktop/JVM: no-op (isAvailable() = false, recognize returns failure).
 */
interface DigitalInkRecognition {
    fun isAvailable(): Boolean
    suspend fun recognize(strokes: List<InkStroke>): Result<String>
}

/** Platform-provided instance. Android: set from app; JVM/iOS: no-op. */
expect fun createDigitalInkRecognition(): DigitalInkRecognition
