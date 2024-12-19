package com.originb.inkwisenote.ux.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigKeys;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundJobs;
import com.originb.inkwisenote.modules.repositories.Repositories;

public class AppMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);

        registerModules();

        initializeModules();

        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    private void registerModules() {
        ConfigReader.fromContext(this);
        Repositories.registerRepositories(this);
        BackgroundJobs.registerJobs(this);

        String rootNotesDirectory = getFilesDir().getPath();
        ConfigReader.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, rootNotesDirectory);
    }

    private void initializeModules() {
        Repositories.getInstance().getNoteMetaRepository().loadAll();
        Repositories.getInstance().getBitmapRepository().loadAllAsThumbnails();
        BackgroundJobs.scheduleJobs(this);
    }
}
