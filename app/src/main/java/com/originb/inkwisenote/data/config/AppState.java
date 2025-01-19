package com.originb.inkwisenote.data.config;

import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.backgroundjobs.TextProcessingStage;
import com.originb.inkwisenote.modules.functionalUtils.Try;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AppState {
    private static AppState instance;

    private AppState() {
    }

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    DebugContext debugContext = new DebugContext("AppState");

    private final MutableLiveData<Boolean> isAzureOcrRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Map<Long, TextProcessingStage>> noteState = new MutableLiveData<>(new HashMap<>());

    public void updateState() {
        Optional<ConfigReader> configReaderOptional = Try.to(ConfigReader::getInstance, debugContext)
                .get();

        configReaderOptional.ifPresent(this::loadConfiguredState);

        if (!configReaderOptional.isPresent()) {
            Log.e(debugContext.getDebugInfo(), "Failed to get configs");
        }
    }

    public void setNoteStatus(Long noteId, TextProcessingStage textProcessingStage) {
        Map<Long, TextProcessingStage> noteStateMap = noteState.getValue();
        noteStateMap.put(noteId, textProcessingStage);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on the main thread
            noteState.setValue(noteStateMap);
        } else {
            // We are on a background thread
            noteState.postValue(noteStateMap);
        }
    }

    public void observeNoteStateChange(LifecycleOwner owner, Observer<Map<Long, TextProcessingStage>> observer) {
        noteState.observe(owner, observer);
    }

    public void observeIfAzureOcrRunning(LifecycleOwner owner, Observer<Boolean> observer) {
        isAzureOcrRunning.observe(owner, observer);
    }

    private void loadConfiguredState(ConfigReader configReader) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on the main thread
            isAzureOcrRunning.setValue(ConfigReader.isAzureOcrEnabled());
        } else {
            // We are on a background thread
            isAzureOcrRunning.postValue(ConfigReader.isAzureOcrEnabled());
        }
    }


}
