package com.originb.inkwisenote2.modules.queries.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.smarthome.QueryNoteResult;
import com.originb.inkwisenote2.modules.smarthome.SmartHomePageViewModel;

import java.util.*;

public class QueryResultsViewModel extends ViewModel {
    
    private QueryRepository queryRepository;
    private SmartHomePageViewModel smartHomePageViewModel;
    
    private final MutableLiveData<List<QueryEntity>> queries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<QueryNoteResult>> currentQueryResults = new MutableLiveData<>(new HashSet<>());
    
    public QueryResultsViewModel() {
        queryRepository = Repositories.getInstance().getQueryRepository();
        smartHomePageViewModel = new SmartHomePageViewModel();
        
        loadQueries();
    }
    
    private void loadQueries() {
        BackgroundOps.execute(
            () -> queryRepository.getAllQueries(),
            queries::setValue
        );
    }
    
    public void loadQueryResults(String queryName) {
        BackgroundOps.execute(() -> {
            // Use the existing query results map from SmartHomePageViewModel
            Map<String, Set<QueryNoteResult>> allResults =
                smartHomePageViewModel.getLiveQueryResults().getValue();
            
            if (allResults != null && allResults.containsKey(queryName)) {
                return allResults.get(queryName);
            }
            return new HashSet<>();
        }, currentQueryResults::setValue);
    }
    
    public LiveData<List<QueryEntity>> getQueries() {
        return queries;
    }
    
    public LiveData<Set<QueryNoteResult>> getCurrentQueryResults() {
        return currentQueryResults;
    }
} 