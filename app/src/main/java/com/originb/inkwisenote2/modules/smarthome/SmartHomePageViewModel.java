package com.originb.inkwisenote2.modules.smarthome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.common.MapsUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.*;

public class SmartHomePageViewModel extends ViewModel {
    private SmartNotebookRepository smartNotebookRepository;
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private AtomicNotesDomain atomicNotesDomain;
    private QueryRepository queryRepository;

    private final MutableLiveData<List<SmartNotebook>> userNotebooks = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Map<String, List<AtomicNoteEntity>>> queryResults = new MutableLiveData<>(new HashMap<>());

    public SmartHomePageViewModel() {
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
        atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        queryRepository = Repositories.getInstance().getQueryRepository();

        EventBus.getDefault().register(this);
        BackgroundOps.execute(this::fetchUserCreatedNotebooks,
                notebooks -> {
                    if (CollectionUtils.isEmpty(notebooks)) return;

                    userNotebooks.setValue(notebooks);
                });
        BackgroundOps.execute(() -> getResultsOfAllQueries(Optional.empty()),
                queryResults::setValue);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSmartNotebookSaved(Events.SmartNotebookSaved smartNotebookSaved) {
        List<SmartNotebook> notebooks = fetchUserCreatedNotebooks();
        if (!CollectionUtils.isEmpty(notebooks)) {
            userNotebooks.postValue(notebooks);
        }
        Optional<Long> bookIdOpt = Optional.of(smartNotebookSaved.smartNotebook.smartBook.getBookId());

        Map<String, List<AtomicNoteEntity>> queryResultsMap = getResultsOfAllQueries(bookIdOpt);
        Map<String, List<AtomicNoteEntity>> currentQueryResultMap = queryResults.getValue();
        Map<String, List<AtomicNoteEntity>> mergedResultMap = MapsUtils.mergeMapsWithLists(
                currentQueryResultMap,
                queryResultsMap);

        queryResults.postValue(mergedResultMap);
    }

    public Map<String, List<AtomicNoteEntity>> getResultsOfAllQueries(Optional<Long> bookIdOpt) {
        Map<String, List<AtomicNoteEntity>> newQueryResults = new HashMap<>();
        List<QueryEntity> queries = queryRepository.getAllQueries();
        Map<String, List<AtomicNoteEntity>> queryResultsMap = new HashMap<>();
        for (QueryEntity query : queries) {
            // todo: need more optimized approach where the queries will be executed in-memory
            // or only on the updated note.
            List<AtomicNoteEntity> results = processQuery(query, bookIdOpt);
            newQueryResults.put(query.getName(), results);
            if (queryResultsMap.containsKey(query.getName())) {
                queryResultsMap.get(query.getName()).addAll(results);
            } else {
                queryResultsMap.put(query.getName(), results);
            }
        }
        return queryResultsMap;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onQueryUpdated(Events.QueryUpdated queryUpdate) {
        List<AtomicNoteEntity> atomicNoteEntities = processQuery(queryUpdate.query, Optional.empty());
        QueryEntity query = queryUpdate.query;

        Map<String, List<AtomicNoteEntity>> queryResultsMap = queryResults.getValue();
        queryResultsMap.put(query.getName(), atomicNoteEntities);
        queryResults.postValue(queryResultsMap);
    }

    private List<AtomicNoteEntity> processQuery(QueryEntity query, Optional<Long> bookId) {
        Set<String> wordsToFind = QueriedNotesLogic.splitAndSanitize(query.getWordsToFind());
        Set<String> wordsToIgnore = QueriedNotesLogic.splitAndSanitize(query.getWordsToIgnore());

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

    public LiveData<Map<String, List<AtomicNoteEntity>>> getQueryResults() {
        return queryResults;
    }
}

