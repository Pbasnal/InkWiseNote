package com.originb.inkwisenote2.modules.noterelation.data

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter
import java.io.Serializable

@Getter
@Setter
@AllArgsConstructor
class TextProcessingJobStatus : Serializable {
    private val noteId: Long? = null
    private val stage: String? = null // values are from TextProcessingStage
}
