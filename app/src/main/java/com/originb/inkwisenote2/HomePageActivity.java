package com.originb.inkwisenote2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.notesearch.NoteSearchActivity;
import com.originb.inkwisenote2.config.AppState;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.sidebar.HomePageSidebarUiComponent;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.config.Feature;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.repositories.Repositories;

import java.util.*;

public class HomePageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartNoteGridAdapter smartNoteGridAdapter;

    private ImageButton noteSearchButton;

    private ConfigReader configReader;

    private LinearLayout fabMenuContainer;
    private FloatingActionButton mainFab;
    private boolean isFabMenuOpen = false;

    private SmartNotebookRepository smartNotebookRepository;
    private NoteRelationDao noteRelationDao;

    private HomePageSidebarUiComponent homePageSidebarUiComponent;

    private Repositories repositories;

    private Logger logger = new Logger("HomePageActivity");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        configReader = ConfigReader.fromContext(this);
        repositories = Repositories.registerRepositories(this);

        smartNotebookRepository = repositories.getSmartNotebookRepository();
        noteRelationDao = repositories.getNotesDb().noteRelationDao();

        createGridLayoutToShowNotes();
        if (configReader.isFeatureEnabled(Feature.MARKDOWN_EDITOR) || configReader.isFeatureEnabled(Feature.CAMERA_NOTE)) {
            setupFabMenu();
        } else {
            createNewNoteButton();
        }

        createSearchBtn();
        createSidebarIfEnabled();
        observeAppState();
    }

    private void observeAppState() {
        ImageButton ocrAzureStatusIndicator = findViewById(R.id.ocr_status);
        AppState.observeIfAzureOcrRunning(this, isAzureOcrRunning -> {
            if (isAzureOcrRunning) {
                ocrAzureStatusIndicator.setColorFilter(null);
            } else {
                ocrAzureStatusIndicator.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            }
        });
        AppState.updateState();
    }

    private void createSearchBtn() {
        noteSearchButton = findViewById(R.id.btn_search_note);
        noteSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, NoteSearchActivity.class);
            startActivity(intent);
        });
    }

    private void createSidebarIfEnabled() {
        if (!configReader.isFeatureEnabled(Feature.HOME_PAGE_NAVIGATION_SIDEBAR)) return;
        homePageSidebarUiComponent = new HomePageSidebarUiComponent(this, configReader);
        homePageSidebarUiComponent.createSidebarIfEnabled();
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.note_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>());

        BackgroundOps.execute(() -> noteRelationDao.getAllNoteRelations(),
                AppState::updatedRelatedNotes
        );

        recyclerView.setAdapter(smartNoteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void createNewNoteButton() {
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v ->
                Routing.SmartNotebookActivity.newNoteIntent(this, getFilesDir().getPath())
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackgroundOps.execute(() -> smartNotebookRepository.getAllSmartNotebooks(),
                smartNotebooks -> {
                    logger.debug("Setting smartNotebooks", smartNotebooks);
                    smartNoteGridAdapter.setSmartNotebooks(smartNotebooks);
                }
        );
    }

    @Override
    public void onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu();
        }
        homePageSidebarUiComponent.onBackPressed();
        super.onBackPressed();
    }

    private void setupFabMenu() {
        fabMenuContainer = findViewById(R.id.fab_menu_container);
        mainFab = findViewById(R.id.new_note_opt);
        FloatingActionButton newNoteFab = findViewById(R.id.fab_add_note);
        newNoteFab.setVisibility(View.GONE);

        mainFab.setVisibility(View.VISIBLE);

        // Setup main FAB click
        mainFab.setOnClickListener(v -> toggleFabMenu());

        // Setup individual FABs
        FloatingActionButton fabCamera = findViewById(R.id.fab_camera);
        FloatingActionButton fabHandwritten = findViewById(R.id.fab_handwritten);
        FloatingActionButton fabText = findViewById(R.id.fab_text);

        fabHandwritten.setOnClickListener(v -> {
            toggleFabMenu();
            // Handle handwritten note creation
            Routing.SmartNotebookActivity.newNoteIntent(this, getFilesDir().getPath());
        });

        if (configReader.isFeatureEnabled(Feature.CAMERA_NOTE)) {
            findViewById(R.id.fab_camera_menu_item).setVisibility(View.VISIBLE);
            fabCamera.setOnClickListener(v -> {
                toggleFabMenu();
                // Handle camera note creation
                Routing.SmartNotebookActivity.newNoteIntent(this, getFilesDir().getPath());
            });
        } else {
            findViewById(R.id.fab_camera_menu_item).setVisibility(View.GONE);
        }


        if (configReader.isFeatureEnabled(Feature.MARKDOWN_EDITOR)) {
            findViewById(R.id.fab_text_menu_item).setVisibility(View.VISIBLE);
            fabText.setOnClickListener(v -> {
                toggleFabMenu();
                // Handle text note creation
                Routing.TextNoteActivity.newNoteIntent(this, getFilesDir().getPath());
            });
        } else {
            findViewById(R.id.fab_text_menu_item).setVisibility(View.INVISIBLE);
        }
    }

    private void toggleFabMenu() {
        if (!isFabMenuOpen) {
            showFabMenu();
        } else {
            closeFabMenu();
        }
    }

    private void showFabMenu() {
        isFabMenuOpen = true;
        fabMenuContainer.setVisibility(View.VISIBLE);
        mainFab.animate().rotation(45f);
        fabMenuContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .start();
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        mainFab.animate().rotation(0f);
        fabMenuContainer.animate()
                .alpha(0f)
                .translationY(fabMenuContainer.getHeight())
                .setInterpolator(new AnticipateInterpolator(1.0f))
                .withEndAction(() -> fabMenuContainer.setVisibility(View.GONE))
                .start();
    }
}
