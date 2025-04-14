package com.originb.inkwisenote2.modules.queries.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.originb.inkwisenote2.common.NotesDatabase;
import com.originb.inkwisenote2.modules.repositories.Repositories;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import android.database.sqlite.SQLiteConstraintException;

public class QueryRepository {
    private final QueryDao queryDao;

    public QueryRepository() {
        NotesDatabase db = Repositories.getInstance().getNotesDb();
        queryDao = db.queryDao();
    }

    public List<QueryEntity> getAllQueries() {
        return queryDao.getAllQueries();
    }

    public QueryEntity getQueryByName(String queryName) {
        return queryDao.getQuery(queryName);
    }

    public void saveQuery(String name, List<String> wordsToFind, List<String> wordsToIgnore) {
        QueryEntity query = new QueryEntity();
        fillEntityWithData(query, name, wordsToFind, wordsToIgnore);
        query.setCreatedTimeMillis(System.currentTimeMillis());
        
        try {
            queryDao.insertQuery(query);
        } catch (SQLiteConstraintException e) {
            // Name already exists, update instead
            queryDao.updateQuery(query);
        }
    }

    public QueryEntity fillEntityWithData(QueryEntity query, String name, List<String> wordsToFind, List<String> wordsToIgnore) {
        query.setName(name);
        query.setWordsToFind(String.join(",", wordsToFind));
        query.setWordsToIgnore(String.join(",", wordsToIgnore));
        return query;
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
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(query.getWordsToFind().split(",")));
    }

    public List<String> getWordsToIgnore(QueryEntity query) {
        if (query.getWordsToIgnore() == null || query.getWordsToIgnore().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(query.getWordsToIgnore().split(",")));
    }
} 