package com.originb.inkwisenote.modules.ocr.data;

import java.util.ArrayList;

public class Word{
    public String content;
    public ArrayList<Double> boundingBox;
    public double confidence;
    public Span span;
}
