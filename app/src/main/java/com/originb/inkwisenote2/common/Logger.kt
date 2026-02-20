package com.originb.inkwisenote2.common

import android.util.Log
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

class Logger(val debugInfo: String?) {
    private val objectMapper = ObjectMapper()

    fun forEvent(eventCode: String?): Logger {
        return Logger(eventCode)
    }

    fun debug(message: String?) {
        Log.d(this.debugInfo, message ?: "null")
    }

    fun debug(message: String?, obj: Any?) {
        try {
            val debugObjects: MutableList<Any?> = mutableListOf(message, obj)
            Log.d(this.debugInfo, objectMapper.writeValueAsString(debugObjects))
        } catch (e: JsonProcessingException) {
            debug(message)
            exception("Failed to serialize input object", e)
        }
    }

    fun error(message: String?) {
        Log.e(this.debugInfo, message ?: "null")
    }


    fun error(message: String?, obj: Any?) {
        try {
            val debugObjects: MutableList<Any?> = mutableListOf(message, obj)
            Log.e(this.debugInfo, objectMapper.writeValueAsString(debugObjects))
        } catch (e: JsonProcessingException) {
            error(message)
            exception("Failed to serialize input object", e)
        }
    }

    fun exception(message: String?, exception: Throwable?) {
        Log.e(this.debugInfo, message, exception)
    }
}
