package com.originb.inkwisenote.data.notedata;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageTemplate implements Serializable {
    private long templateId;
    private int lineSpacing;
    private String lineColor;
    private int lineWidth;
}
