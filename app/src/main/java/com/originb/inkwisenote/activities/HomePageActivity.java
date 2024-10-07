package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageButton;
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
import com.originb.inkwisenote.data.repositories.DirectoryContents;
import com.originb.inkwisenote.data.repositories.FolderItem;
import com.originb.inkwisenote.data.sidebar.MenuItemData;
import com.originb.inkwisenote.modules.Repositories;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        registerModules();

        Repositories.getInstance().getNoteMetaRepository().loadAll();
        Repositories.getInstance().getBitmapRepository().loadAllAsThumbnails();

        configReader = ConfigReader.fromContext(this);

        folderHierarchyRepository = new FolderHierarchyRepository(getFilesDir());
        DirectoryContents directoryContents = folderHierarchyRepository.getFilesInDirectory();

        if (configReader.isFeatureEnabled(Feature.HOME_PAGE_NAVIGATION_SIDEBAR)) {
            createSidebar(directoryContents.getFolders());
        }
        createGridLayoutToShowNotes();
        createNewNoteButton();

        noteSearchButton = findViewById(R.id.btn_search_note);
        noteSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, NoteSearchActivity.class);
            startActivity(intent);
        });

    }

    public void createSidebar(List<FolderItem> folders) {
        drawerLayout = findViewById(R.id.sidebar_drawer_view);
        navigationRecyclerView = findViewById(R.id.navigation_sidebar_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public View.OnClickListener onNewNoteTapCallback = v -> {
        // Start NoteActivity to create a new note
        Intent intent = new Intent(HomePageActivity.this, NoteActivity.class);
        NoteActivity.newNoteIntent(intent, getFilesDir().getPath());
        startActivity(intent);
    };

    private void registerModules() {
        Repositories.registerRepositories(this);
    }
}
