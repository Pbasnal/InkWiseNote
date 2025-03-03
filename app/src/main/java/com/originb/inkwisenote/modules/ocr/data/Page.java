package com.originb.inkwisenote.modules.ocr.data;

import java.util.ArrayList;

public class Page{
    public double height;
    public double width;
    public double angle;
    public int pageNumber;
    public ArrayList<Word> words;
    public ArrayList<Span> spans;
    public ArrayList<Line> lines;
}
