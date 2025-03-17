package com.originb.inkwisenote2.modules.admin

import lombok.AllArgsConstructor
import lombok.Getter

@Getter
@AllArgsConstructor
class TermFrequencyEntry {
    private val noteId: Long = 0
    private val term: String? = null
    private val frequency = 0
}