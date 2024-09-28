package com.originb.inkwisenote;

public class DebugContext {

    private String EventCode;

    public DebugContext(String eventCode) {
        this.EventCode = eventCode;
    }

    public DebugContext forEvent(String eventCode) {
        return new DebugContext(eventCode);
    }

    public String getDebugInfo() {
        return "Debug info";
    }
}
