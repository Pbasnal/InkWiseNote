package com.originb.inkwisenote.modules.backgroundjobs.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class TextProcessingJobStatus implements Serializable {
    private Long noteId;
    private String stage; // values are from TextProcessingStage
}
