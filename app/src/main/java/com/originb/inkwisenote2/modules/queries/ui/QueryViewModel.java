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
import java.util.List;

public class QueryViewModel extends AndroidViewModel {
    private final QueryRepository repository;
    private final MutableLiveData<List<String>> wordsToFind = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> wordsToIgnore = new MutableLiveData<>(new ArrayList<>());
    private QueryEntity currentQuery;

    public QueryViewModel(Application application) {
        super(application);
        repository = Repositories.getInstance().getQueryRepository();
    }

    public LiveData<List<QueryEntity>> getAllQueries() {
        return repository.getAllQueries();
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
        List<String> currentList = wordsToFind.getValue();
        if (currentList != null) {
            currentList.remove(word);
            wordsToFind.setValue(currentList);
        }
    }

    public void removeWordToIgnore(String word) {
        List<String> currentList = wordsToIgnore.getValue();
        if (currentList != null) {
            currentList.remove(word);
            wordsToIgnore.setValue(currentList);
        }
    }

    public void saveQuery(String name) {
        BackgroundOps.execute(() -> {
            if (currentQuery == null) {
                repository.saveQuery(name, wordsToFind.getValue(), wordsToIgnore.getValue());
            } else {
                repository.updateQuery(currentQuery, wordsToFind.getValue(), wordsToIgnore.getValue());
            }
        });
    }

    public void loadQuery(QueryEntity query) {
        currentQuery = query;
        wordsToFind.setValue(repository.getWordsToFind(query));
        wordsToIgnore.setValue(repository.getWordsToIgnore(query));
    }

    public LiveData<List<String>> getWordsToFind() {
        return wordsToFind;
    }

    public LiveData<List<String>> getWordsToIgnore() {
        return wordsToIgnore;
    }
} 