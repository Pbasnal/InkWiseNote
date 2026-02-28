package org.basnalcorp.shared.debug

import java.net.HttpURLConnection
import java.net.URL

// #region agent log
actual fun reportComposerDebug() {
    val payload = inspectComposer()
    val body = """{"sessionId":"bdbebf","runId":"run1","hypothesisId":"H1-H5","location":"DebugCompose.android.kt","message":"Composer runtime info","data":$payload,"timestamp":${System.currentTimeMillis()}}"""
    try {
        val u = URL("http://10.0.2.2:7690/ingest/da8cae49-dfb0-4706-9c52-645833bece16")
        val conn = u.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("X-Debug-Session-Id", "bdbebf")
        conn.doOutput = true
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        conn.responseCode
        conn.disconnect()
    } catch (_: Exception) {}
}
// #endregion

private fun escapeJson(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")

private fun inspectComposer(): String {
    return try {
        val c = Class.forName("androidx.compose.runtime.Composer")
        val cl = escapeJson(c.classLoader?.toString() ?: "null")
        val cs = escapeJson(c.protectionDomain?.codeSource?.location?.toString() ?: "null")
        val hasMethod = try {
            c.getMethod("shouldExecute", Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            true
        } catch (_: NoSuchMethodException) { false }
        """{"composerClassLoader":"$cl","composerCodeSource":"$cs","hasShouldExecuteBooleanInt":$hasMethod}"""
    } catch (e: Throwable) {
        """{"error":"${escapeJson(e.message ?: "unknown")}"}"""
    }
}
