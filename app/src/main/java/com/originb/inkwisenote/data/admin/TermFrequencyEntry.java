package com.originb.inkwisenote.data.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TermFrequencyEntry {
    private long noteId;
    private String term;
    private int frequency;
} 