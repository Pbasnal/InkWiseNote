package com.originb.inkwisenote2.modules.ocr.data;

import java.util.ArrayList;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
public class Line{
    public String content;
    public ArrayList<Double> boundingBox;
    public ArrayList<Span> spans;
}
