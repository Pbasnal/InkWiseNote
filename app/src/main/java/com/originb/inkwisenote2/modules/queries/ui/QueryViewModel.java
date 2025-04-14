package com.originb.inkwisenote2.modules.queries.ui;

import android.app.Application;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.*;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class QueryViewModel extends AndroidViewModel {

    private final QueryRepository repository;
    private final MutableLiveData<List<String>> wordsToFind = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> wordsToIgnore = new MutableLiveData<>(new ArrayList<>());
    private QueryEntity currentQuery;
    private final MutableLiveData<String> currentQueryName = new MutableLiveData<>("");

    private MutableLiveData<Map<String, QueryEntity>> allQueries;


    public QueryViewModel(Application application) {
        super(application);

        repository = Repositories.getInstance().getQueryRepository();
        allQueries = new MutableLiveData<>(new HashMap<>());
    }

    public LiveData<Map<String, QueryEntity>> getAllQueries() {
        Map<String, QueryEntity> allQueriesMap = new HashMap<>();

        BackgroundOps.execute(repository::getAllQueries, queries -> {
            for (QueryEntity query : queries) {
                allQueriesMap.put(query.getName(), query);
            }
            allQueries.setValue(allQueriesMap);
        });

        return allQueries;
    }

    public void addWordToFind(String word) {
        if (word.isEmpty()) return;

        List<String> currentList = wordsToFind.getValue();
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word);
            wordsToFind.setValue(currentList);
        }
    }

    public void addWordToIgnore(String word) {
        if (word.isEmpty()) return;

        List<String> currentList = wordsToIgnore.getValue();
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word);
            wordsToIgnore.setValue(currentList);
        }
    }

    public void removeWordToFind(String word) {
        List<String> currentList = wordsToFind.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        currentList.remove(word);
        wordsToFind.setValue(currentList);
    }

    public void removeWordToIgnore(String word) {
        List<String> currentList = wordsToFind.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        currentList.remove(word);
        wordsToIgnore.setValue(currentList);
    }

    public void findQueryWithQueryName(String queryName, Observer<QueryEntity> onQueryFetch) {
        BackgroundOps.execute(() -> repository.getQueryByName(queryName),
                onQueryFetch::onChanged);
    }

    public void saveQuery(String queryName) {
        BackgroundOps.execute(() -> {
            if (isNewQuery(queryName)) {
                repository.saveQuery(queryName, wordsToFind.getValue(), wordsToIgnore.getValue());
            } else {
                repository.updateQuery(currentQuery, wordsToFind.getValue(), wordsToIgnore.getValue());
            }
            return repository.getQueryByName(queryName);
        }, query -> {
            Map<String, QueryEntity> allQueriesMap = allQueries.getValue();
            allQueriesMap.put(queryName, query);

            allQueries.setValue(allQueriesMap);

            clearCurrentQuery();
            EventBus.getDefault().post(new Events.QueryUpdated(allQueriesMap.get(queryName)));
        });
    }

    public void deleteQuery(QueryEntity queryToDelete) {
        BackgroundOps.execute(() -> {
            repository.deleteQuery(queryToDelete);
            return queryToDelete;
        }, queryToRemove -> {
            Map<String, QueryEntity> allQueriesMap = allQueries.getValue();
            allQueriesMap.remove(queryToRemove.getName());

            allQueries.setValue(allQueriesMap);
            EventBus.getDefault().post(new Events.QueryDeleted(queryToRemove));
        });
    }

    private boolean isNewQuery(String queryName) {
        return currentQuery == null || !currentQuery.getName().equals(queryName);
    }

    public void loadQuery(QueryEntity query) {
        currentQuery = query;
        currentQueryName.setValue(query.getName());
        wordsToFind.setValue(repository.getWordsToFind(query));
        wordsToIgnore.setValue(repository.getWordsToIgnore(query));
    }

    public void onWordsToFindChange(LifecycleOwner owner, Observer<List<String>> observer) {
        wordsToFind.observe(owner, observer);
    }

    public void onWordsToIgnoreChange(LifecycleOwner owner, Observer<List<String>> observer) {
        wordsToIgnore.observe(owner, observer);
    }

    public void onQueryNameChange(LifecycleOwner owner, Observer<String> observer) {
        currentQueryName.observe(owner, observer);
    }

    public void clearCurrentQuery() {
        currentQuery = null;
        currentQueryName.setValue("");
        wordsToFind.setValue(new ArrayList<>());
        wordsToIgnore.setValue(new ArrayList<>());
    }


}