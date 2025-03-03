package com.originb.inkwisenote.common;

import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class Logger {

    private String EventCode;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Logger(String eventCode) {
        this.EventCode = eventCode;
    }

    public Logger forEvent(String eventCode) {
        return new Logger(eventCode);
    }

    public String getDebugInfo() {
        return EventCode;
    }

    public void debug(String message) {
        Log.d(EventCode, message);
    }

    public void debug(String message, Object obj) {
        try {
            List<Object> debugObjects = new ArrayList<>();
            debugObjects.add(message);
            debugObjects.add(obj);

            Log.d(EventCode, objectMapper.writeValueAsString(debugObjects));
        } catch (JsonProcessingException e) {
            debug(message);
            exception("Failed to serialize input object", e);
        }
    }

    public void error(String message) {
        Log.e(EventCode, message);
    }


    public void error(String message, Object obj) {
        try {
            List<Object> debugObjects = new ArrayList<>();
            debugObjects.add(message);
            debugObjects.add(obj);

            Log.e(EventCode, objectMapper.writeValueAsString(debugObjects));
        } catch (JsonProcessingException e) {
            error(message);
            exception("Failed to serialize input object", e);
        }
    }

    public void exception(String message, Throwable exception) {
        Log.e(EventCode, message, exception);
    }
}
