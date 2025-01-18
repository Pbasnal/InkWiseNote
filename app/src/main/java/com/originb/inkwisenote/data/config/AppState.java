package com.originb.inkwisenote.data.config;

import android.util.Log;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.modules.functionalUtils.Try;

import java.util.Optional;

public class AppState {

    public boolean isAzureOcrRunning = false;
    DebugContext debugContext = new DebugContext("AppState");

    public void updateState() {

        Optional<ConfigReader> instance = Try.to(ConfigReader::getInstance, debugContext)
                .get();

        instance.ifPresent(this::loadConfiguredState);

        if (!instance.isPresent()) {
            Log.e(debugContext.getDebugInfo(), "Failed to get configs");
        }
    }

    private void loadConfiguredState(ConfigReader configReader) {
        isAzureOcrRunning = ConfigReader.isAzureOcrEnabled();
    }
}
