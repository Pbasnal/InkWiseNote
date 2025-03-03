package com.originb.inkwisenote.config;

import android.os.Looper;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote.functionalUtils.Try;

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

    private final Logger logger = new Logger("AppState");

    private final MutableLiveData<Boolean> isAzureOcrRunning = new MutableLiveData<>(false);
    private MutableLiveData<Set<NoteRelation>> liveNoteRelationshipMap = new MutableLiveData<>(new HashSet<>());

    public static void updateState() {
        AppState instance = getInstance();
        Optional<ConfigReader> configReaderOptional = Try.to(ConfigReader::getInstance, instance.logger)
                .get();

        configReaderOptional.ifPresent(instance::loadConfiguredState);

        if (!configReaderOptional.isPresent()) {
            instance.logger.error("Failed to get configs");
        }
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

    public static void observeIfAzureOcrRunning(LifecycleOwner owner, Observer<Boolean> observer) {
        getInstance().isAzureOcrRunning.observe(owner, observer);
    }

    public static void observeNoteRelationships(LifecycleOwner owner, Observer<Set<NoteRelation>> observer) {
        getInstance().liveNoteRelationshipMap.observe(owner, observer);
    }

    public static void updatedRelatedNotes(List<NoteRelation> relatedNotes) {
        updatedRelatedNotes(new HashSet<>(relatedNotes));
    }

    public static void updatedRelatedNotes(Set<NoteRelation> relatedNotes) {
        AppState instance = getInstance();
        Set<NoteRelation> noteRelationshipMap = instance.liveNoteRelationshipMap.getValue();
        if (CollectionUtils.isEmpty(noteRelationshipMap)) {
            noteRelationshipMap = new HashSet<>();
        }
        noteRelationshipMap.addAll(relatedNotes);
        instance.liveNoteRelationshipMap.setValue(noteRelationshipMap);
    }
}
