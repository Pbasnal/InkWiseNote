package com.originb.inkwisenote2.config

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import java.util.concurrent.Callable
import java.util.function.Consumer

class AppState private constructor() {
    private val logger = Logger("AppState")

    private val isAzureOcrRunning = MutableLiveData<Boolean?>(false)
    private val liveNoteRelationshipMap = MutableLiveData<MutableSet<NoteRelation?>?>(HashSet<NoteRelation?>())

    private fun loadConfiguredState(configReader: ConfigReader?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on the main thread
            isAzureOcrRunning.setValue(ConfigReader.Companion.isAzureOcrEnabled)
        } else {
            // We are on a background thread
            isAzureOcrRunning.postValue(ConfigReader.Companion.isAzureOcrEnabled)
        }
    }

    companion object {
        @get:Synchronized
        var instance: AppState? = null
            get() {
                if (field == null) {
                    field = AppState()
                }
                return field
            }
            private set

        fun updateState() {
            val instance: AppState = instance!!
            val configReaderOptional =
                Try.to<ConfigReader?>(Callable { ConfigReader.Companion.getInstance() }, instance.logger)
                    .get()

            configReaderOptional.ifPresent(Consumer { configReader: ConfigReader? ->
                instance.loadConfiguredState(
                    configReader
                )
            })

            if (!configReaderOptional.isPresent()) {
                instance.logger.error("Failed to get configs")
            }
        }

        fun observeIfAzureOcrRunning(owner: LifecycleOwner, observer: Observer<Boolean?>) {
            instance!!.isAzureOcrRunning.observe(owner, observer)
        }

        fun observeNoteRelationships(owner: LifecycleOwner, observer: Observer<MutableSet<NoteRelation?>?>) {
            instance!!.liveNoteRelationshipMap.observe(owner, observer)
        }

        fun updatedRelatedNotes(relatedNotes: MutableList<NoteRelation?>) {
            updatedRelatedNotes(HashSet<NoteRelation?>(relatedNotes))
        }

        fun updatedRelatedNotes(relatedNotes: MutableSet<NoteRelation?>) {
            val instance: AppState = instance!!
            var noteRelationshipMap = instance.liveNoteRelationshipMap.getValue()
            if (CollectionUtils.isEmpty(noteRelationshipMap)) {
                noteRelationshipMap = HashSet<NoteRelation?>()
            }
            noteRelationshipMap!!.addAll(relatedNotes)
            instance.liveNoteRelationshipMap.setValue(noteRelationshipMap)
        }
    }
}
