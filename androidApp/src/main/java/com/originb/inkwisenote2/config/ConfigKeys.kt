package com.originb.inkwisenote2.config

enum class ConfigKeys(configKey: String) {
    NOTES_ROOT_DIRECTORY("NOTES_ROOT_DIRECTORY");

    val configKey: String?

    init {
        this.configKey = configKey
    }
}
