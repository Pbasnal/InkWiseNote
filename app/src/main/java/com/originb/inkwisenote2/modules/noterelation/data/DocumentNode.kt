package com.originb.inkwisenote2.modules.noterelation.data

class DocumentNode(var documentId: Long) {
    var termFrequencies: Map<String, Int> = HashMap() // Term frequencies within this document
}

