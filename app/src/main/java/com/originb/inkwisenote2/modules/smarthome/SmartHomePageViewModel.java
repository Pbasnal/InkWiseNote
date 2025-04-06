package com.originb.inkwisenote2.modules.smarthome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SmartHomePageViewModel extends ViewModel {
    private SmartNotebookRepository smartNotebookRepository;
    private final MutableLiveData<List<SmartNotebook>> userNotebooks = new MutableLiveData<>();

    public SmartHomePageViewModel() {
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        EventBus.getDefault().register(this);
        fetchUserCreatedNotebooks();
    }

    public LiveData<List<SmartNotebook>> getUserNotebooks() {
        return userNotebooks;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmartNotebookSaved(Events.SmartNotebookSaved smartNotebookSaved) {
        fetchUserCreatedNotebooks();
    }

    public void fetchUserCreatedNotebooks() {
        BackgroundOps.execute(
                // First lambda: get notebooks and sort them
                () -> {
                    if(smartNotebookRepository == null) return new ArrayList<SmartNotebook>();
                    List<SmartNotebook> notebooks = smartNotebookRepository.getAllSmartNotebooks();
                    if (notebooks != null) {
                        // Sort notebooks by last updated time in descending order (most recent first)
                        notebooks.sort((n1, n2) -> {
                            // Assuming getLastUpdatedTime() returns a long timestamp
                            // For null safety, put newer items first and null items last
                            Long time1 = n1.smartBook.getLastModifiedTimeMillis();
                            Long time2 = n2.smartBook.getLastModifiedTimeMillis();

                            // Descending order (most recent first)
                            return time2.compareTo(time1);
                        });
                    }
                    return notebooks;
                },
                // Second lambda: set the sorted list to LiveData
                notebooks -> {
                    if (!CollectionUtils.isEmpty(notebooks)) {
                        userNotebooks.setValue(notebooks);
                    }
                }
        );
    }
}

