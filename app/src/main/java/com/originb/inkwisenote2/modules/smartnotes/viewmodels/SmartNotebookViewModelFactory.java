package com.originb.inkwisenote2.modules.smartnotes.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory for SmartNotebookViewModel to provide it with the application context.
 */
public class SmartNotebookViewModelFactory implements ViewModelProvider.Factory {
    
    private final Application application;

    public SmartNotebookViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SmartNotebookViewModel.class)) {
            return (T) new SmartNotebookViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
} 