package com.originb.inkwisenote2.config;

public enum ConfigKeys {
    NOTES_ROOT_DIRECTORY("NOTES_ROOT_DIRECTORY");

    private final String configKey;

    ConfigKeys(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
