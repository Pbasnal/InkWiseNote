package com.originb.inkwisenote2.modules.noterelation.data

class TermNode(var termId: String) {
    var documentNodes: Set<String> = HashSet() // Document ID to DocumentNode
    var idfScore: Double = 0.0
}



