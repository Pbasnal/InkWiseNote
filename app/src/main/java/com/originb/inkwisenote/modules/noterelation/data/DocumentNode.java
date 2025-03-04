package com.originb.inkwisenote.modules.noterelation.data;

import java.util.HashMap;
import java.util.Map;

public class DocumentNode {
    Long documentId;
    Map<String, Integer> termFrequencies; // Term frequencies within this document

    public DocumentNode(Long documentId) {
        this.documentId = documentId;
        this.termFrequencies = new HashMap<>();
    }
}

