package com.originb.inkwisenote2.modules.smarthome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.common.MapsUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.*;
import java.util.stream.Collectors;

public class SmartHomePageViewModel extends ViewModel {
    private SmartNotebookRepository smartNotebookRepository;
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private AtomicNotesDomain atomicNotesDomain;
    private QueryRepository queryRepository;
    private TextNotesDao textNotesDao;
    private NoteOcrTextDao noteOcrTextDao;
    private HandwrittenNoteRepository handwrittenNoteRepository;

    private final MutableLiveData<List<SmartNotebook>> userNotebooks = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Map<String, Set<QueryNoteResult>>> liveQueryResults = new MutableLiveData<>(new HashMap<>());

    public SmartHomePageViewModel() {
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
        atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        queryRepository = Repositories.getInstance().getQueryRepository();

        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();

        EventBus.getDefault().register(this);
        BackgroundOps.execute(this::fetchUserCreatedNotebooks,
                notebooks -> {
                    if (CollectionUtils.isEmpty(notebooks)) return;

                    userNotebooks.setValue(notebooks);
                });
        BackgroundOps.execute(() -> getResultsOfAllQueries(Optional.empty()),
                liveQueryResults::setValue);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSmartNotebookSaved(Events.SmartNotebookSaved smartNotebookSaved) {
        List<SmartNotebook> notebooks = fetchUserCreatedNotebooks();
        if (!CollectionUtils.isEmpty(notebooks)) {
            userNotebooks.postValue(notebooks);
        }
        Optional<Long> bookIdOpt = Optional.of(smartNotebookSaved.smartNotebook.smartBook.getBookId());

        Map<String, Set<QueryNoteResult>> queryResultsMap = getResultsOfAllQueries(bookIdOpt);
        Map<String, Set<QueryNoteResult>> currentQueryResultMap = liveQueryResults.getValue();
        Map<String, Set<QueryNoteResult>> mergedResultMap = MapsUtils.mergeMapsWithSets(
                queryResultsMap,
                currentQueryResultMap);

        liveQueryResults.postValue(mergedResultMap);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmartNotebookDeleted(Events.NotebookDeleted notebookDeleted) {
        List<SmartNotebook> userNotebooks = this.userNotebooks.getValue();
        if (CollectionUtils.isEmpty(userNotebooks) || notebookDeleted.smartNotebook == null) {
            return;
        }
        Long smartBookId = notebookDeleted.smartNotebook.smartBook.getBookId();
        userNotebooks.removeIf(n -> smartBookId.equals(n.smartBook.getBookId()));

        this.userNotebooks.postValue(userNotebooks);

        Map<String, Set<QueryNoteResult>> queryResultsMap = liveQueryResults.getValue();
        if (queryResultsMap == null || queryResultsMap.values() == null) return;

        for (Set<QueryNoteResult> queryNoteResults : queryResultsMap.values()) {
            queryNoteResults.removeIf(q -> smartBookId.equals(q.getBookId()));
        }

        liveQueryResults.setValue(queryResultsMap);
    }

    public Map<String, Set<QueryNoteResult>> getResultsOfAllQueries(Optional<Long> bookIdOpt) {
        List<QueryEntity> queries = queryRepository.getAllQueries();
        Map<String, Set<QueryNoteResult>> queryResultsMap = new HashMap<>();


        for (QueryEntity query : queries) {
            Set<String> wordsToFind = QueriedNotesLogic.splitAndSanitize(query.getWordsToFind());
            Set<String> wordsToIgnore = QueriedNotesLogic.splitAndSanitize(query.getWordsToIgnore());
            // todo: need more optimized approach where the queries will be executed in-memory
            // or only on the updated note.
            List<AtomicNoteEntity> noteResults = processQuery(wordsToFind, wordsToIgnore, bookIdOpt);
            Set<QueryNoteResult> queryResults = noteResults.stream()
                    .map(note -> transform(note, wordsToFind, wordsToIgnore))
                    .collect(Collectors.toSet());

            if (queryResults.isEmpty()) continue;
            if (queryResultsMap.containsKey(query.getName())) {
                queryResultsMap.get(query.getName()).addAll(queryResults);
            } else {
                queryResultsMap.put(query.getName(), queryResults);
            }
        }
        return queryResultsMap;
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onQueryUpdated(Events.QueryUpdated queryUpdate) {

        Set<String> wordsToFind = QueriedNotesLogic.splitAndSanitize(queryUpdate.query.getWordsToFind());
        Set<String> wordsToIgnore = QueriedNotesLogic.splitAndSanitize(queryUpdate.query.getWordsToIgnore());

        List<AtomicNoteEntity> noteEntities = processQuery(wordsToFind, wordsToIgnore, Optional.empty());
        Set<QueryNoteResult> queryResults = noteEntities.stream()
                .map(note -> transform(note, wordsToFind, wordsToIgnore))
                .collect(Collectors.toSet());
        QueryEntity query = queryUpdate.query;

        Map<String, Set<QueryNoteResult>> queryResultsMap = liveQueryResults.getValue();
        queryResultsMap.put(query.getName(), queryResults);
        liveQueryResults.postValue(queryResultsMap);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueryDeleted(Events.QueryDeleted queryDeleted) {
        Map<String, Set<QueryNoteResult>> queryResultsMap = liveQueryResults.getValue();
        queryResultsMap.remove(queryDeleted.query.getName());
        liveQueryResults.postValue(queryResultsMap);
    }

    private QueryNoteResult transform(AtomicNoteEntity atomicNoteEntity,
                                      Set<String> wordsToFind,
                                      Set<String> wordsToIgnore) {
        wordsToFind.removeAll(wordsToIgnore);

        QueryNoteResult queryNoteResult = new QueryNoteResult(atomicNoteEntity);
        wordsToFind.stream().findFirst()
                .ifPresent(queryNoteResult::setQueryWord);

        if (NoteType.TEXT_NOTE.equals(atomicNoteEntity.getNoteType())) {
            queryNoteResult.setNoteType(NoteType.TEXT_NOTE);
            TextNoteEntity textNoteEntity = textNotesDao.getTextNoteForNote(atomicNoteEntity.getNoteId());
            if (textNoteEntity != null) {
                queryNoteResult.setNoteText(textNoteEntity.getNoteText());
            }
        } else {
            queryNoteResult.setNoteType(NoteType.HANDWRITTEN_PNG);
            HandwrittenNoteWithImage noteWithImage = handwrittenNoteRepository.getNoteImage(atomicNoteEntity, BitmapScale.THUMBNAIL);
            if (noteWithImage != null) {
                noteWithImage.noteImage.ifPresent(queryNoteResult::setNoteImage);
            }
            NoteOcrText noteOcrText = noteOcrTextDao.readTextFromDb(atomicNoteEntity.getNoteId());
            if (noteOcrText != null) {
                queryNoteResult.setNoteText(noteOcrText.getExtractedText());
            }
        }
        return queryNoteResult;
    }

    private List<AtomicNoteEntity> processQuery(Set<String> wordsToFind, Set<String> wordsToIgnore, Optional<Long> bookId) {
        int queryScenario = QueriedNotesLogic.getQueryScenario(wordsToFind, wordsToIgnore);
        List<AtomicNoteEntity> atomicNoteEntities = new ArrayList<>();
        switch (queryScenario) {
            case QueriedNotesLogic.ONLY_FIND_WORDS:
                atomicNoteEntities = QueriedNotesLogic.getNotesThatHaveWords(
                        wordsToFind,
                        noteTermFrequencyDao,
                        atomicNotesDomain,
                        bookId
                );
                return atomicNoteEntities;
            case QueriedNotesLogic.ONLY_IGNORE_WORDS:
                atomicNoteEntities = QueriedNotesLogic.getNotesThatDontHaveWords(
                        wordsToIgnore,
                        noteTermFrequencyDao,
                        atomicNotesDomain,
                        bookId
                );
                return atomicNoteEntities;
            case QueriedNotesLogic.FIND_AND_FILTER:
                atomicNoteEntities = QueriedNotesLogic.getNotesWithWordsAndFilter(
                        wordsToFind,
                        wordsToIgnore,
                        noteTermFrequencyDao,
                        atomicNotesDomain,
                        bookId
                );
                return atomicNoteEntities;
            case QueriedNotesLogic.IGNORE:
            default:
                return atomicNoteEntities;
        }
    }

    public List<SmartNotebook> fetchUserCreatedNotebooks() {

        if (smartNotebookRepository == null) ;
        List<SmartNotebook> notebooks = smartNotebookRepository.getAllSmartNotebooks();
        if (notebooks == null) notebooks = new ArrayList<>();

        // Sort notebooks by last updated time in descending order (most recent first)
        notebooks.sort((n1, n2) -> {
            // Assuming getLastUpdatedTime() returns a long timestamp
            // For null safety, put newer items first and null items last
            Long time1 = n1.smartBook.getLastModifiedTimeMillis();
            Long time2 = n2.smartBook.getLastModifiedTimeMillis();

            // Descending order (most recent first)
            return time2.compareTo(time1);
        });


        return notebooks;
    }

    public LiveData<List<SmartNotebook>> getUserNotebooks() {
        return userNotebooks;
    }

    public LiveData<Map<String, Set<QueryNoteResult>>> getLiveQueryResults() {
        return liveQueryResults;
    }
}

