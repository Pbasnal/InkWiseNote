package org.basnalcorp.shared.recognition

actual fun createDigitalInkRecognition(): DigitalInkRecognition = NoOpDigitalInkRecognition
actual fun createImageOcrService(): ImageOcrService = NoOpImageOcrService
