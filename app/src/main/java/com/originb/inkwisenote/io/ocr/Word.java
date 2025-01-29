package com.originb.inkwisenote.io.ocr;

import java.util.ArrayList;

public class Word{
    public String content;
    public ArrayList<Double> boundingBox;
    public double confidence;
    public Span span;
}
