package org.basnalcorp.shared.debug

import java.io.File
import java.net.URL

// #region agent log
actual fun reportComposerDebug() {
    val logPath = "/home/pbasnal/coding/Repos/InkWiseNote/.cursor/debug-bdbebf.log"
    val payload = inspectComposer()
    val line = """{"sessionId":"bdbebf","runId":"run1","hypothesisId":"H1-H5","location":"DebugCompose.jvm.kt","message":"Composer runtime info","data":${payload},"timestamp":${System.currentTimeMillis()}}""" + "\n"
    try { File(logPath).appendText(line) } catch (_: Exception) {}
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
