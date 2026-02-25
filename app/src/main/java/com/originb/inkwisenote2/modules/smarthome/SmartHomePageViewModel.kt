package com.originb.inkwisenote2.modules.smarthome

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.MapsUtils
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
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
import java.util.concurrent.Callable

class SmartHomePageViewModel(
    private val smartNotebookRepository: SmartNotebookRepository,
    private val noteTermFrequencyDao: NoteTermFrequencyDao,
    private val atomicNotesDomain: AtomicNotesDomain,
    private val queryRepository: QueryRepository,
    private val textNotesDao: TextNotesDao,
    private val noteOcrTextDao: NoteOcrTextsDao,
    private val handwrittenNoteRepository: HandwrittenNoteRepository
) : ViewModel() {
    private val _userNotebooks = MutableLiveData<MutableList<SmartNotebook>>()
    private val _showStandingQueryPrompt = MutableLiveData<Boolean>(false)
    private val userNotebooks = MutableLiveData<MutableList<SmartNotebook>>(ArrayList())
    private val liveQueryResults =
        MutableLiveData<MutableMap<String, MutableSet<QueryNoteResult>>>(HashMap())

    init {
        EventBus.getDefault().register(this)
        BackgroundOps.execute(
            { fetchUserCreatedNotebooks() },
            { notebooks: MutableList<SmartNotebook>? ->
                if (!CollectionUtils.isEmpty(notebooks)) {
                    userNotebooks.setValue(notebooks)
                }
            }
        )
        BackgroundOps.execute(
            { getResultsOfAllQueries(null) },
            { map: MutableMap<String, MutableSet<QueryNoteResult>>? ->
                if (map != null) liveQueryResults.setValue(map)
            }
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSmartNotebookSaved(smartNotebookSaved: SmartNotebookSaved) {
        val notebooks = fetchUserCreatedNotebooks()
        if (!CollectionUtils.isEmpty(notebooks)) {
            userNotebooks.postValue(notebooks)
        }
        val bookId = smartNotebookSaved.smartNotebook.smartBook.bookId
        val queryResultsMap = getResultsOfAllQueries(bookId)
        val currentQueryResultMap = liveQueryResults.value
        val mergedResultMap = MapsUtils.mergeMapsWithSets<String, QueryNoteResult>(
            queryResultsMap,
            currentQueryResultMap
        )

        liveQueryResults.postValue(mergedResultMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmartNotebookDeleted(notebookDeleted: NotebookDeleted) {
        val notebooks = this.userNotebooks.value ?: return
        val smartBookId = notebookDeleted.smartNotebook!!.smartBook!!.bookId
        notebooks.removeIf { n -> smartBookId == n.smartBook?.bookId }

        this.userNotebooks.postValue(notebooks)

        val queryResultsMap = liveQueryResults.value ?: return

        val deletedNotes = notebookDeleted.smartNotebook!!.atomicNotes.map { it.noteId }

        val keysToRemove = ArrayList<String>()
        for (key in queryResultsMap.keys) {
            val queryNoteResults = queryResultsMap[key] ?: continue
            queryNoteResults.removeIf { q -> deletedNotes.contains(q.noteId) }
            if (queryNoteResults.isEmpty()) keysToRemove.add(key)
        }
        keysToRemove.forEach { queryResultsMap.remove(it) }
        liveQueryResults.postValue(queryResultsMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteDeleted(noteDeleted: NoteDeleted) {
        val queryResultsMap = liveQueryResults.value ?: return
        val noteId = noteDeleted.atomicNote!!.noteId
        for (queryNoteResults in queryResultsMap.values) {
            queryNoteResults.removeIf { q -> noteId == q.noteId }
        }
        liveQueryResults.setValue(queryResultsMap)
    }

    fun getResultsOfAllQueries(bookIdOpt: Long?): MutableMap<String, MutableSet<QueryNoteResult>> {
        val queries = (queryRepository.allQueries ?: emptyList()).filterNotNull()
        val queryResultsMap = HashMap<String, MutableSet<QueryNoteResult>>()
        for (query in queries) {
            val wordsToFind = QueriedNotesLogic.splitAndSanitize(query.wordsToFind)
            val wordsToIgnore = QueriedNotesLogic.splitAndSanitize(query.wordsToIgnore)
            val noteResults = processQuery(wordsToFind, wordsToIgnore, bookIdOpt)
            val queryResults = noteResults.map { note -> note.let { transform(it, wordsToFind, wordsToIgnore) } }.toMutableSet()

            if (queryResults.isEmpty()) continue
            val queryName = query.name
            queryResultsMap.getOrPut(queryName) { mutableSetOf() }.addAll(queryResults)
        }
        return queryResultsMap
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onQueryUpdated(queryUpdate: QueryUpdated) {
        val wordsToFind = QueriedNotesLogic.splitAndSanitize(queryUpdate.query!!.wordsToFind)
        val wordsToIgnore = QueriedNotesLogic.splitAndSanitize(queryUpdate.query!!.wordsToIgnore)

        val noteEntities = processQuery(wordsToFind, wordsToIgnore, null)
        val queryResults = noteEntities.mapNotNull { note -> note?.let { transform(it, wordsToFind, wordsToIgnore) } }.toMutableSet()
        val query: QueryEntity = queryUpdate.query!!

        val queryResultsMap = liveQueryResults.value ?: return
        val queryName = query.name
        queryResultsMap[queryName] = queryResults
        liveQueryResults.postValue(queryResultsMap)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQueryDeleted(queryDeleted: QueryDeleted) {
        val queryResultsMap = liveQueryResults.value ?: return
        queryDeleted.query?.name?.let { queryResultsMap.remove(it) }
        liveQueryResults.postValue(queryResultsMap)
    }

    val showStandingQueryPrompt: LiveData<Boolean>
        get() = _showStandingQueryPrompt

    fun refreshDashboardState() {
        BackgroundOps.execute {
            val hasQueries = queryRepository.userHasAnyQuery()
            val notebooks = userNotebooks.value

            val showPrompt = !hasQueries && !CollectionUtils.isEmpty(notebooks)
            _showStandingQueryPrompt.postValue(showPrompt)
            _userNotebooks.postValue(notebooks)
        }
    }

    private fun transform(
        atomicNoteEntity: AtomicNoteEntity,
        wordsToFind: MutableSet<String>,
        wordsToIgnore: MutableSet<String>
    ): QueryNoteResult {
        wordsToFind.removeAll(wordsToIgnore)

        val queryNoteResult = QueryNoteResult(atomicNoteEntity)
        wordsToFind.firstOrNull()?.let { queryNoteResult.queryWord = it }

        if (NoteType.TEXT_NOTE.toString() == atomicNoteEntity.noteType) {
            queryNoteResult.noteType = NoteType.TEXT_NOTE
            val textNoteEntity = textNotesDao.getTextNoteForNote(atomicNoteEntity.noteId)
            queryNoteResult.noteText = textNoteEntity.noteText
        } else {
            queryNoteResult.noteType = NoteType.HANDWRITTEN_PNG
            val noteWithImage = handwrittenNoteRepository.getNoteImage(atomicNoteEntity, BitmapScale.THUMBNAIL)
            if (noteWithImage.noteImage != null) {
                queryNoteResult.noteImage = noteWithImage.noteImage
            }
            val noteOcrText = noteOcrTextDao.readTextFromDb(atomicNoteEntity.noteId)
            queryNoteResult.noteText = noteOcrText.extractedText
        }
        return queryNoteResult
    }

    private fun processQuery(
        wordsToFind: MutableSet<String>,
        wordsToIgnore: MutableSet<String>,
        bookId: Long?
    ): MutableList<AtomicNoteEntity> {
        val queryScenario = QueriedNotesLogic.getQueryScenario(wordsToFind, wordsToIgnore)
        var atomicNoteEntities: MutableList<AtomicNoteEntity> = ArrayList()
        when (queryScenario) {
            QueriedNotesLogic.ONLY_FIND_WORDS -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesThatHaveWords(
                    wordsToFind,
                    noteTermFrequencyDao,
                    atomicNotesDomain
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.ONLY_IGNORE_WORDS -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesThatDontHaveWords(
                    wordsToIgnore,
                    noteTermFrequencyDao,
                    atomicNotesDomain
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.FIND_AND_FILTER -> {
                atomicNoteEntities = QueriedNotesLogic.getNotesWithWordsAndFilter(
                    wordsToFind,
                    wordsToIgnore,
                    noteTermFrequencyDao,
                    atomicNotesDomain
                )
                return atomicNoteEntities
            }

            QueriedNotesLogic.IGNORE -> return atomicNoteEntities
            else -> return atomicNoteEntities
        }
    }

    fun fetchUserCreatedNotebooks(): MutableList<SmartNotebook> {
        val notebooks = smartNotebookRepository.allSmartNotebooks
        if (CollectionUtils.isEmpty(notebooks)) {
            return mutableListOf()
        }
        notebooks.sortWith(Comparator { n1, n2 ->
            val time1 = n1.smartBook.lastModifiedTimeMillis
            val time2 = n2.smartBook.lastModifiedTimeMillis
            time2.compareTo(time1)
        }
        )
        return notebooks
    }

    fun getUserNotebooks(): LiveData<MutableList<SmartNotebook>> {
        return userNotebooks
    }

    fun getLiveQueryResults(): LiveData<MutableMap<String, MutableSet<QueryNoteResult>>> {
        return liveQueryResults
    }
}

