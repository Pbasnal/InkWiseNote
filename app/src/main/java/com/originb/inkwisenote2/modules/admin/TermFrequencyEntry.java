package com.originb.inkwisenote2.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TermFrequencyEntry {
    private long noteId;
    private String term;
    private int frequency;
} 