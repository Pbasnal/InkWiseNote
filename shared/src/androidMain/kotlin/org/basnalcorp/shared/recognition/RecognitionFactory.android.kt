package org.basnalcorp.shared.recognition

/**
 * Phase 9.1/9.2: Android holder for recognition implementations.
 * App (androidApp) sets real ML Kit / OCR impl via [setDigitalInkRecognition] / [setImageOcrService]
 * so shared does not depend on ML Kit or tess-two.
 */
private object RecognitionHolder {
    var digitalInkRecognition: DigitalInkRecognition = NoOpDigitalInkRecognition
    var imageOcrService: ImageOcrService = NoOpImageOcrService
}

fun setDigitalInkRecognition(impl: DigitalInkRecognition) {
    RecognitionHolder.digitalInkRecognition = impl
}

fun setImageOcrService(impl: ImageOcrService) {
    RecognitionHolder.imageOcrService = impl
}

actual fun createDigitalInkRecognition(): DigitalInkRecognition = RecognitionHolder.digitalInkRecognition
actual fun createImageOcrService(): ImageOcrService = RecognitionHolder.imageOcrService
