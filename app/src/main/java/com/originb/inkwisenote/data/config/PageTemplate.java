package com.originb.inkwisenote.data.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageTemplate implements Serializable {
    private int lineSpacing;
    private String lineColor;
    private int lineWidth;
}
