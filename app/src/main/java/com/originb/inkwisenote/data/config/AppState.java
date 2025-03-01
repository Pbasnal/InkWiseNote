package com.originb.inkwisenote.data.config;

import android.os.Looper;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.originb.inkwisenote.Logger;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.data.entities.noterelationdata.NoteRelation;
import com.originb.inkwisenote.modules.commonutils.Maps;
import com.originb.inkwisenote.modules.functionalUtils.Try;

import java.util.*;

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

    Logger logger = new Logger("AppState");

    private final MutableLiveData<Boolean> isAzureOcrRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Map<Long, NoteTaskStage>> noteState = new MutableLiveData<>(new HashMap<>());
    private MutableLiveData<Map<Long, List<NoteRelation>>> liveNoteRelationshipMap = new MutableLiveData<>(new HashMap<>());

    public void updateState() {
        Optional<ConfigReader> configReaderOptional = Try.to(ConfigReader::getInstance, logger)
                .get();

        configReaderOptional.ifPresent(this::loadConfiguredState);

        if (!configReaderOptional.isPresent()) {
            logger.error("Failed to get configs");
        }
    }

    public void setNoteStatus(Long noteId, NoteTaskStage noteTaskStage) {
        Map<Long, NoteTaskStage> noteStateMap = noteState.getValue();
        noteStateMap.put(noteId, noteTaskStage);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on the main thread
            noteState.setValue(noteStateMap);
        } else {
            // We are on a background thread
            noteState.postValue(noteStateMap);
        }
    }

    public void observeNoteStateChange(LifecycleOwner owner, Observer<Map<Long, NoteTaskStage>> observer) {
        noteState.observe(owner, observer);
    }

    public void observeIfAzureOcrRunning(LifecycleOwner owner, Observer<Boolean> observer) {
        isAzureOcrRunning.observe(owner, observer);
    }

    public void observeNoteRelationships(LifecycleOwner owner, Observer<Map<Long, List<NoteRelation>>> observer) {
        liveNoteRelationshipMap.observe(owner, observer);
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

    public void updatedRelatedNotes(Long noteId, List<NoteRelation> relatedNotes) {
        Map<Long, List<NoteRelation>> noteRelationshipMap = liveNoteRelationshipMap.getValue();
        if (Maps.isEmpty(noteRelationshipMap)) {
            noteRelationshipMap = new HashMap<>();
        }

        noteRelationshipMap.put(noteId, relatedNotes);
        liveNoteRelationshipMap.setValue(noteRelationshipMap);
    }

    public void updatedRelatedNotes(Map<Long, List<NoteRelation>> relatedNotes) {
        Map<Long, List<NoteRelation>> noteRelationshipMap = liveNoteRelationshipMap.getValue();
        if (Maps.isEmpty(noteRelationshipMap)) {
            noteRelationshipMap = new HashMap<>();
        }
        noteRelationshipMap.putAll(relatedNotes);
        liveNoteRelationshipMap.setValue(noteRelationshipMap);
    }
}
