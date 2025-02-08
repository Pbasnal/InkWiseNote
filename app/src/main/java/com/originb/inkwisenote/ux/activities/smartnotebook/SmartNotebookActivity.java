package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.ux.views.DrawingView;
import com.originb.inkwisenote.ux.views.PageBackgroundType;
import com.originb.inkwisenote.ux.views.RuledPageBackground;

import java.util.Optional;

public class SmartNotebookActivity extends AppCompatActivity {

    private DebugContext debugContext;

    private ConfigReader configReader;

    private SmartNotebookRepository smartNotebookRepository;

    private SmartNotebookAdapter smartNotebookAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_note);

        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        configReader = ConfigReader.getInstance();

        recyclerView = findViewById(R.id.smart_note_page_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);


        BackgroundOps.executeOpt(this::getSmartNotebook, smartNotebook -> {
            smartNotebookAdapter = new SmartNotebookAdapter(this, smartNotebook);
            recyclerView.setAdapter(smartNotebookAdapter);
        });
    }


    private Optional<SmartNotebook> getSmartNotebook() {
        Long noteIdToOpen = getIntent().getLongExtra("noteId", -1);
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        String workingNotePath = getIntent().getStringExtra("workingNotePath");

        if (bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebook(bookIdToOpen);
        }
        if (noteIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebookContainingNote(noteIdToOpen);
        }

        return smartNotebookRepository.initializeNewSmartNote("",
                workingNotePath,
                Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888),
                configReader.getAppConfig().getPageTemplates().get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name()));
    }

}

