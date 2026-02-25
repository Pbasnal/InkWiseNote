package org.basnalcorp.shared

import java.io.File

actual fun appStorageRoot(): String =
    File(System.getProperty("user.home"), ".inkwisenote").absolutePath
