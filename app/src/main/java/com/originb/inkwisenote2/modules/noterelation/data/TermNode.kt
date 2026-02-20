package com.originb.inkwisenote2.modules.noterelation.data

class TermNode(var termId: String?) {
    var documentNodes: MutableSet<String?>? // Document ID to DocumentNode
    var idfScore: Double? = 0.0

    init {
        this.documentNodes = HashSet<String?>()
    }
}



