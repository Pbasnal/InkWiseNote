package com.originb.inkwisenote2.modules.ocr.data

class Word {
    var content: String? = null
    var boundingBox: ArrayList<Double>? = null
    var confidence: Double = 0.0
    var span: Span? = null
}
