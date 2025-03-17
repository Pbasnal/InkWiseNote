package com.originb.inkwisenote2.modules.ocr.data

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
class Line {
    var content: String? = null
    var boundingBox: ArrayList<Double>? = null
    var spans: ArrayList<Span>? = null
}
