package com.originb.inkwisenote2.modules.noterelation.data

class DocumentNode(var documentId: Long?) {
    var termFrequencies: MutableMap<String?, Int?>? // Term frequencies within this document

    init {
        this.termFrequencies = HashMap<String?, Int?>()
    }
}

