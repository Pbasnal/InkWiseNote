package org.basnalcorp.shared

import java.io.File

/**
 * Phase 8.3: JVM path utilities for OS-agnostic paths.
 * Use [File] and [separator] so paths work on Windows, Linux, macOS.
 */
object PathUtils {
    /** OS file separator (e.g. \ on Windows, / on Linux/macOS). */
    val separator: String get() = File.separator

    /** Joins path segments using the platform separator. */
    fun join(vararg segments: String): String =
        segments.filter { it.isNotBlank() }.joinToString(separator)
}
