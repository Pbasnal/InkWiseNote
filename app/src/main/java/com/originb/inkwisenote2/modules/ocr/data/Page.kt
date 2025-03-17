package com.originb.inkwisenote2.modules.ocr.data

class Page {
    var height: Double = 0.0
    var width: Double = 0.0
    var angle: Double = 0.0
    var pageNumber: Int = 0
    var words: ArrayList<Word>? = null
    var spans: ArrayList<Span>? = null
    var lines: ArrayList<Line>? = null
}
