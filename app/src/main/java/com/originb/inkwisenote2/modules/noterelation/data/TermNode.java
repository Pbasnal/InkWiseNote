package com.originb.inkwisenote2.modules.noterelation.data;

import java.util.HashSet;
import java.util.Set;

public class TermNode {
    String termId;
    Set<String> documentNodes; // Document ID to DocumentNode
    Double idfScore;

    public TermNode(String termId) {
        this.termId = termId;
        this.documentNodes = new HashSet<>();
        idfScore = 0.0;
    }
}



