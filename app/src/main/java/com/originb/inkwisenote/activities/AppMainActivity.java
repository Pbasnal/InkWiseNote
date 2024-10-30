package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.modules.Repositories;

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

    private void initializeModules() {
        Repositories.getInstance().getNoteMetaRepository().loadAll();
        Repositories.getInstance().getBitmapRepository().loadAllAsThumbnails();
    }

    private void registerModules() {
        Repositories.registerRepositories(this);
        ConfigReader.fromContext(this);
    }
}
