package com.originb.inkwisenote2.modules.queries.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import com.originb.inkwisenote2.modules.repositories.Repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<String> currentList = wordsToFind.getValue();
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word);
            wordsToFind.setValue(currentList);
        }
    }

    public void addWordToIgnore(String word) {
        List<String> currentList = wordsToIgnore.getValue();
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word);
            wordsToIgnore.setValue(currentList);
        }
    }

    public void removeWordToFind(String word) {
        List<String> currentList = new ArrayList<>(wordsToFind.getValue() != null ?
                wordsToFind.getValue() : new ArrayList<>());
        currentList.remove(word);
        wordsToFind.setValue(currentList);
    }

    public void removeWordToIgnore(String word) {
        List<String> currentList = new ArrayList<>(wordsToIgnore.getValue() != null ?
                wordsToIgnore.getValue() : new ArrayList<>());
        currentList.remove(word);
        wordsToIgnore.setValue(currentList);
    }

    public void saveQuery(String name) {
        BackgroundOps.execute(() -> {
            if (currentQuery == null || !currentQuery.getName().equals(name)) {
                repository.saveQuery(name, wordsToFind.getValue(), wordsToIgnore.getValue());
            } else {
                repository.updateQuery(currentQuery, wordsToFind.getValue(), wordsToIgnore.getValue());
            }
            return repository.getAllQueries();
        }, queries -> {
            Map<String, QueryEntity> allQueriesMap = new HashMap<>();
            for (QueryEntity query : queries) {
                allQueriesMap.put(query.getName(), query);
            }
            allQueries.setValue(allQueriesMap);
            clearCurrentQuery();
        });
    }

    public void loadQuery(QueryEntity query) {
        currentQuery = query;
        currentQueryName.setValue(query.getName());
        wordsToFind.setValue(new ArrayList<>(repository.getWordsToFind(query)));
        wordsToIgnore.setValue(new ArrayList<>(repository.getWordsToIgnore(query)));
    }

    public LiveData<List<String>> getWordsToFind() {
        return wordsToFind;
    }

    public LiveData<List<String>> getWordsToIgnore() {
        return wordsToIgnore;
    }

    public void clearCurrentQuery() {
        currentQuery = null;
        currentQueryName.setValue("");
        wordsToFind.setValue(new ArrayList<>());
        wordsToIgnore.setValue(new ArrayList<>());
    }

    public LiveData<String> getCurrentQueryName() {
        return currentQueryName;
    }
} 