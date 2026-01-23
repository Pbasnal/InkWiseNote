package com.originb.inkwisenote2.modules.admin;

import java.io.File;
import java.util.List;

public abstract class AdminUiState {
    public static class DataList extends AdminUiState {
        public final List<?> data;
        public final String type;

        public DataList(List<?> data, String type) {
            this.data = data;
            this.type = type;
        }
    }

    public static class FilesState extends AdminUiState {
        public final File currentDir;
        public final List<File> files;

        public FilesState(File currentDir, List<File> files) {
            this.currentDir = currentDir;
            this.files = files;
        }
    }
}
