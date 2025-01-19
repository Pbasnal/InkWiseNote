package com.originb.inkwisenote.data.notedata;

import java.util.List;
import java.util.function.Consumer;

public class PageSettings {
    public int pageLineGap;

    public List<AppSetting> appSettings;

    public PageSettings() {}

    public static class AppSetting {
        public int settingType;
        public Consumer<String> onSettingChange;
    }
}
