package com.originb.inkwisenote2.modules.admin

import java.io.File

abstract class AdminUiState {
    class DataList(val data: MutableList<*>?, val type: String?) : AdminUiState()

    class FilesState(val currentDir: File?, val files: MutableList<File?>?) : AdminUiState()
}
