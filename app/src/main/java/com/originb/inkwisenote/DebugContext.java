package com.originb.inkwisenote;

import android.util.Log;

public class DebugContext {

    private String EventCode;

    public DebugContext(String eventCode) {
        this.EventCode = eventCode;
    }

    public DebugContext forEvent(String eventCode) {
        return new DebugContext(eventCode);
    }

    public String getDebugInfo() {
        return EventCode;
    }

    public void logDebug(String message) {
        Log.d(EventCode, message);
    }

    public void logError(String message) {
        Log.e(EventCode, message);
    }
}
