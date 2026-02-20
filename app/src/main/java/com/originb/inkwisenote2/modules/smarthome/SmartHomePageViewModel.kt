package com.originb.inkwisenote2.modules.smarthome

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.MapsUtils
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class SmartHomePageViewModel(
    private val smartNotebookRepository: SmartNotebookRepository?,
    private val noteTermFrequencyDao: NoteTermFrequencyDao,
    private val atomicNotesDomain: AtomicNotesDomain,
    private val queryRepository: QueryRepository,
    private val textNotesDao: TextNotesDao,
    private val noteOcrTextDao: NoteOcrTextsDao,
    private val handwrittenNoteRepository: HandwrittenNoteRepository
) : ViewModel() {
    private val _userNotebooks = MutableLiveData<MutableList<SmartNotebook?>?>()
    private val _showStandingQueryPrompt = MutableLiveData<Boolean?>(false)
    private val userNotebooks = MutableLiveData<MutableList<SmartNotebook?>?>(ArrayList<SmartNotebook?>())
    private val liveQueryResults =
        MutableLiveData<MutableMap<String?, MutableSet<QueryNoteResult?>>?>(HashMap<String?, MutableSet<QueryNoteResult?>?>())

    init {
        EventBus.getDefault().register(this)
        execute(
            Runnable { this.fetchUserCreatedNotebooks() },
            Runnable { notebooks ->
                if (CollectionUtils.isEmpty(notebooks)) return@execute
                userNotebooks.setValue(notebooks)
            })
        execute(
            Runnable { getResultsOfAllQueries(Optional.empty<Long?>()) },
            Runnable { liveQueryResults.setValue() })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSmartNotebookSaved(smartNotebookSaved: SmartNotebookSaved) {
        val notebooks = fetchUserCreatedNotebooks()
        if (!CollectionUtils.isEmpty(notebooks)) {
            userNotebooks.postValue(notebooks)
        }
        val bookIdOpt: Optional<Long?> = Optional.of<Long?>(smartNotebookSaved.smartNotebook!!.smartBook!!.bookId)

        val queryResultsMap = getResultsOfAllQueries(bookIdOpt)
        val currentQueryResultMap = liveQueryResults.getValue()
        val mergedResultMap: MutableMap<String?, MutableSet<QueryNoteResult?>?> =
            MapsUtils.mergeMapsWithSets<String?, QueryNoteResult?>(
                queryResultsMap,
                currentQueryResultMap
            )

        liveQueryResults.postValue(mergedResultMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmartNotebookDeleted(notebookDeleted: NotebookDeleted) {
        val userNotebooks = this.userNotebooks.getValue()
        if (CollectionUtils.isEmpty(userNotebooks) || notebookDeleted.smartNotebook == null) {
            return
        }
        val smartBookId = notebookDeleted.smartNotebook!!.smartBook!!.bookId
        userNotebooks!!.removeIf { n: SmartNotebook? -> smartBookId == n!!.smartBook!!.bookId }

        this.userNotebooks.postValue(userNotebooks)

        val queryResultsMap = liveQueryResults.getValue()
        if (queryResultsMap == null || queryResultsMap.values == null) return

        val deletedNotes = notebookDeleted.smartNotebook!!.atomicNotes.stream()
            .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }.collect(Collectors.toList())

        val keysToRemove: MutableList<String?> = ArrayList<String?>()
        for (key in queryResultsMap.keys) {
            val queryNoteResults = queryResultsMap.get(key)
            queryNoteResults!!.removeIf { q: QueryNoteResult? -> deletedNotes.contains(q!!.getNoteId()) }
            if (queryNoteResults.isEmpty()) {
                keysToRemove.add(key)
            }
        }

        keysToRemove.forEach(Consumer { key: String? -> queryResultsMap.remove(key) })

        liveQueryResults.postValue(queryResultsMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteDeleted(noteDeleted: NoteDeleted) {
        val queryResultsMap = liveQueryResults.getValue()
        if (queryResultsMap == null || queryResultsMap.values == null) return

        val noteId = noteDeleted.atomicNote!!.noteId
        for (queryNoteResults in queryResultsMap.values) {
            queryNoteResults.removeIf { q: QueryNoteResult? -> noteId == q!!.getNoteId() }
        }

        liveQueryResults.setValue(queryResultsMap)
    }

    fun getResultsOfAllQueries(bookIdOpt: Optional<Long?>?): MutableMap<String?, MutableSet<QueryNoteResult?>?> {
        val queries: MutableList<QueryEntity>? = queryRepository.allQueries
        val queryResultsMap: MutableMap<String?, MutableSet<QueryNoteResult?>?> =
            HashMap<String?, MutableSet<QueryNoteResult?>?>()


        for (query in queries!!) {
            val wordsToFind = QueriedNotesLogic.splitAndSanitize(query.wordsToFind)
            val wordsToIgnore = QueriedNotesLogic.splitAndSanitize(query.wordsToIgnore)
            // todo: need more optimized approach where the queries will be executed in-memory
            // or only on the updated note.
            val noteResults = processQuery(wordsToFind, wordsToIgnore, bookIdOpt)
            val queryResults = noteResults.stream()
                .map<QueryNoteResult?> { note: AtomicNoteEntity? -> transform(note!!, wordsToFind, wordsToIgnore) }
                .collect(Collectors.toSet())

            if (queryResults.isEmpty()) continue
            if (queryResultsMap.containsKey(query.name)) {
                queryResultsMap.get(query.name)!!.addAll(queryResults)
            } else {
                queryResultsMap.put(query.name, queryResults)
            }
        }
        return queryResultsMap
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onQueryUpdated(queryUpdate: QueryUpdated) {
        val wordsToFind = QueriedNotesLogic.splitAndSanitize(queryUpdate.query!!.wordsToFind)
        val wordsToIgnore = QueriedNotesLogic.splitAndSanitize(queryUpdate.query!!.wordsToIgnore)

        val noteEntities = processQuery(wordsToFind, wordsToIgnore, Optional.empty<Long?>())
        val queryResults = noteEntities.stream()
            .map<QueryNoteResult?> { note: AtomicNoteEntity? -> transform(note!!, wordsToFind, wordsToIgnore) }
            .collect(Collectors.toSet())
        val query: QueryEntity = queryUpdate.query!!

        val queryResultsMap = liveQueryResults.getValue()
        queryResultsMap!!.put(query.name, queryResults)
        liveQueryResults.postValue(queryResultsMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQueryDeleted(queryDeleted: QueryDeleted) {
        val queryResultsMap = liveQueryResults.getValue()
        queryResultsMap!!.remove(queryDeleted.query!!.name)
        liveQueryResults.postValue(queryResultsMap)
    }

    val showStandingQueryPrompt: LiveData<Boolean?>
        get() = _showStandingQueryPrompt

    fun refreshDashboardState() {
        // Move your BackgroundOps logic here!
        execute(Runnable {
            val hasQueries = queryRepository.userHasAnyQuery()
            val notebooks = userNotebooks.getValue()

            // Logic: Show prompt only if they have notes but NO queries
            val showPrompt = !hasQueries && !CollectionUtils.isEmpty(notebooks)

            _showStandingQueryPrompt.postValue(showPrompt)
            _userNotebooks.postValue(notebooks)
        })
    }

    private fun transform(
        atomicNoteEntity: AtomicNoteEntity,
        wordsToFind: MutableSet<String?>,
        wordsToIgnore: MutableSet<String?>
    ): QueryNoteResult {
        wordsToFind.removeAll(wordsToIgnore)

        val queryNoteResult = QueryNoteResult(atomicNoteEntity)
        wordsToFind.stream().findFirst()
            .ifPresent(Consumer { queryWord: String? -> queryNoteResult.setQueryWord(queryWord) })

        if (NoteType.TEXT_NOTE.equals(atomicNoteEntity.noteType)) {
            queryNoteResult.setNoteType(NoteType.TEXT_NOTE)
            val textNoteEntity = textNotesDao.getTextNoteForNote(atomicNoteEntity.noteId)
            if (textNoteEntity != null) {
                queryNoteResult.setNoteText(textNoteEntity.noteText)
            }
        } else {
            queryNoteResult.setNoteType(NoteType.HANDWRITTEN_PNG)
            val noteWithImage = handwrittenNoteRepository.getNoteImage(atomicNoteEntity, BitmapScale.THUMBNAIL)
            if (noteWithImage != null) {
                noteWithImage.noteImage!!.ifPresent(Consumer { noteImage: Bitmap? ->
                    queryNoteResult.setNoteImage(
                        noteImage
                    )
                })
            }
            val noteOcrText = noteOcrTextDao.readTextFromDb(atomicNoteEntity.noteId)
            if (noteOcrText != null) {
                queryNoteResult.setNoteText(noteOcrText.extractedText)
            }
        }
        return queryNoteResult
    }

    private fun processQuery(
        wordsToFind: MutableSet<String?>?,
        wordsToIgnore: MutableSet<String?>?,
        bookId: Optional<Long?>?
    ): MutableList<AtomicNoteEntity?> {
        val queryScenario = QueriedNotesLogic.getQueryScenario(wordsToFind, wordsToIgnore)
        var atomicNoteEntities: MutableList<AtomicNoteEntity?> = ArrayList<AtomicNoteEntity?>()
        when (queryScenario) {
            QueriedNotesLogic.ONLY_FIND_WORDS -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesThatHaveWords(
                    wordsToFind,
                    noteTermFrequencyDao,
                    atomicNotesDomain,
                    bookId
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.ONLY_IGNORE_WORDS -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesThatDontHaveWords(
                    wordsToIgnore,
                    noteTermFrequencyDao,
                    atomicNotesDomain,
                    bookId
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.FIND_AND_FILTER -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesWithWordsAndFilter(
                    wordsToFind,
                    wordsToIgnore,
                    noteTermFrequencyDao,
                    atomicNotesDomain,
                    bookId
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.IGNORE -> return atomicNoteEntities
            else -> return atomicNoteEntities
        }
    }

    fun fetchUserCreatedNotebooks(): MutableList<SmartNotebook?> {
        if (smartNotebookRepository == null) {
            return mutableListOf<SmartNotebook?>()
        }
        val notebooks = smartNotebookRepository.allSmartNotebooks
        if (CollectionUtils.isEmpty(notebooks)) {
            return mutableListOf<SmartNotebook?>()
        }
        // Sort notebooks by last updated time in descending order (most recent first)
        notebooks.sort(Comparator { n1: SmartNotebook?, n2: SmartNotebook? ->
            // Assuming getLastUpdatedTime() returns a long timestamp
            // For null safety, put newer items first and null items last
            val time1 = n1!!.smartBook!!.lastModifiedTimeMillis
            val time2 = n2!!.smartBook!!.lastModifiedTimeMillis
            time2.compareTo(time1)
        })


        return notebooks
    }

    fun getUserNotebooks(): LiveData<MutableList<SmartNotebook?>?> {
        return userNotebooks
    }

    fun getLiveQueryResults(): LiveData<MutableMap<String?, MutableSet<QueryNoteResult?>>?> {
        return liveQueryResults
    }
}

