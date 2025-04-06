package com.originb.inkwisenote2.modules.queries.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.originb.inkwisenote2.common.NotesDatabase;
import com.originb.inkwisenote2.modules.repositories.Repositories;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class QueryRepository {
    private final QueryDao queryDao;

    public QueryRepository(Context context) {
        NotesDatabase db = Repositories.getInstance().getNotesDb();
        queryDao = db.queryDao();
    }

    public LiveData<List<QueryEntity>> getAllQueries() {
        return queryDao.getAllQueries();
    }

    public void saveQuery(String name, List<String> wordsToFind, List<String> wordsToIgnore) {
        QueryEntity query = new QueryEntity();
        query.setName(name);
        query.setWordsToFind(String.join(",", wordsToFind));
        query.setWordsToIgnore(String.join(",", wordsToIgnore));
        query.setCreatedTimeMillis(System.currentTimeMillis());
        queryDao.insertQuery(query);
    }

    public void updateQuery(QueryEntity query, List<String> wordsToFind, List<String> wordsToIgnore) {
        query.setWordsToFind(String.join(",", wordsToFind));
        query.setWordsToIgnore(String.join(",", wordsToIgnore));
        queryDao.updateQuery(query);
    }

    public void deleteQuery(QueryEntity query) {
        queryDao.deleteQuery(query);
    }

    public List<String> getWordsToFind(QueryEntity query) {
        if (query.getWordsToFind() == null || query.getWordsToFind().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(query.getWordsToFind().split(","));
    }

    public List<String> getWordsToIgnore(QueryEntity query) {
        if (query.getWordsToIgnore() == null || query.getWordsToIgnore().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(query.getWordsToIgnore().split(","));
    }
} 