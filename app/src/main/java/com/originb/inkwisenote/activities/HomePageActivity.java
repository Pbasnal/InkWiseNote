package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.adapters.ExpandableMenuListAdapter;
import com.originb.inkwisenote.adapters.NoteGridAdapter;
import com.originb.inkwisenote.config.Feature;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.modules.repositories.DirectoryContents;
import com.originb.inkwisenote.modules.repositories.FolderItem;
import com.originb.inkwisenote.data.sidebar.MenuItemData;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.io.FolderHierarchyRepository;
import com.originb.inkwisenote.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HomePageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteGridAdapter noteGridAdapter;

    private FolderHierarchyRepository folderHierarchyRepository;

    private DrawerLayout drawerLayout;
    private RecyclerView navigationRecyclerView;
    private ExpandableMenuListAdapter expandableMenuListAdapter;

    private ImageButton noteSearchButton;

    private ConfigReader configReader;

    private ImageButton settingsMenuBtn;

    private LinearLayout fabMenuContainer;
    private FloatingActionButton mainFab;
    private boolean isFabMenuOpen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        configReader = ConfigReader.fromContext(this);

        createSidebarIfEnabled();
        createGridLayoutToShowNotes();
        createNewNoteButton();
        createSettingsBtn();
        createSearchBtn();

        setupFabMenu();
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

        folderHierarchyRepository = new FolderHierarchyRepository(getFilesDir());
        DirectoryContents directoryContents = folderHierarchyRepository.getFilesInDirectory();
        createSidebar(directoryContents.getFolders());
    }

    private void createSettingsBtn() {
        settingsMenuBtn = findViewById(R.id.main_settings_menu_btn);
        settingsMenuBtn.setOnClickListener(v -> MainSettingsActivity.getIntent(this));
    }

    public void createSidebar(List<FolderItem> folders) {
        drawerLayout = findViewById(R.id.sidebar_drawer_view);
        navigationRecyclerView = findViewById(R.id.navigation_sidebar_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button adminButton = findViewById(R.id.admin_button);
        if (adminButton != null) {
            adminButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomePageActivity.this, AdminActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        List<MenuItemData> menuItems = new ArrayList<>();

        folders.forEach(folder -> {
            MenuItemData menuItem = new MenuItemData(folder.getId(), folder.getFolderName());
            menuItems.add(menuItem);
        });

        expandableMenuListAdapter = new ExpandableMenuListAdapter(menuItems);
        navigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        navigationRecyclerView.setAdapter(expandableMenuListAdapter);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.note_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(this, new ArrayList<>());

        recyclerView.setAdapter(noteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void createNewNoteButton() {
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(onNewNoteTapCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the note list on resume
        Repositories.initRepositories();
        List<Long> noteIds = Arrays.stream(Repositories.getInstance().getNoteMetaRepository().getAllNoteIds())
                .collect(Collectors.toList());

        noteGridAdapter.setNoteIds(noteIds);

        noteGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public View.OnClickListener onNewNoteTapCallback = v -> {
        // Start NoteActivity to create a new note
        Routing.NoteActivity.newNoteIntent(this, getFilesDir().getPath());
    };

    private void setupFabMenu() {
        fabMenuContainer = findViewById(R.id.fab_menu_container);
        mainFab = findViewById(R.id.fab_add_note);
        
        // Setup main FAB click
        mainFab.setOnClickListener(v -> toggleFabMenu());
        
        // Setup individual FABs
        FloatingActionButton fabCamera = findViewById(R.id.fab_camera);
        FloatingActionButton fabHandwritten = findViewById(R.id.fab_handwritten);
        FloatingActionButton fabText = findViewById(R.id.fab_text);
        
        fabCamera.setOnClickListener(v -> {
            toggleFabMenu();
            // Handle camera note creation
//            Routing.NoteActivity.newCameraNoteIntent(this, getFilesDir().getPath());
        });
        
        fabHandwritten.setOnClickListener(v -> {
            toggleFabMenu();
            // Handle handwritten note creation
            Routing.NoteActivity.newNoteIntent(this, getFilesDir().getPath());
        });
        
        fabText.setOnClickListener(v -> {
            toggleFabMenu();
            // Handle text note creation
//            Routing.NoteActivity.newTextNoteIntent(this, getFilesDir().getPath());
        });
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
