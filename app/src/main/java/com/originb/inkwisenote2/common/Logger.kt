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
        Log.d(debugInfo, message!!)
    }

    fun debug(message: String?, obj: Any?) {
        try {
            val debugObjects: MutableList<Any?> = ArrayList()
            debugObjects.add(message)
            debugObjects.add(obj)

            Log.d(debugInfo, objectMapper.writeValueAsString(debugObjects))
        } catch (e: JsonProcessingException) {
            debug(message)
            exception("Failed to serialize input object", e)
        }
    }

    fun error(message: String?) {
        Log.e(debugInfo, message!!)
    }


    fun error(message: String?, obj: Any?) {
        try {
            val debugObjects: MutableList<Any?> = ArrayList()
            debugObjects.add(message)
            debugObjects.add(obj)

            Log.e(debugInfo, objectMapper.writeValueAsString(debugObjects))
        } catch (e: JsonProcessingException) {
            error(message)
            exception("Failed to serialize input object", e)
        }
    }

    fun exception(message: String?, exception: Throwable?) {
        Log.e(debugInfo, message, exception)
    }
}
