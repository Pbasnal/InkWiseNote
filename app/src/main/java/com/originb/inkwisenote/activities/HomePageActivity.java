package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.originb.inkwisenote.data.repositories.DirectoryContents;
import com.originb.inkwisenote.data.repositories.FolderItem;
import com.originb.inkwisenote.repositories.FileRepository;
import com.originb.inkwisenote.repositories.NoteRepository;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.adapters.NoteGridAdapter;

import java.util.List;

public class HomePageActivity extends AppCompatActivity {
    private NoteRepository noteRepository;
    private RecyclerView recyclerView;
    private NavigationView navigationView;
    private NoteGridAdapter noteGridAdapter;
    private DrawerLayout drawerLayout;

    private FileRepository fileRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        noteRepository = new NoteRepository(getFilesDir());
        fileRepository = new FileRepository(getFilesDir());
        DirectoryContents directoryContents = fileRepository.getFilesInDirectory();

        createSidebar(directoryContents.getFolders());
        createGridLayoutToShowNotes();
        createNewNoteButton();
    }

    public void createSidebar(List<FolderItem> folders) {
        drawerLayout = findViewById(R.id.sidebar_drawer_view);
        navigationView = findViewById(R.id.home_sidebar_view);
        Menu menu = navigationView.getMenu();


        for (FolderItem folder : folders) {
            menu.add(folder.getId(), 0, Menu.NONE, folder.getFolderName());
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            Toast.makeText(HomePageActivity.this, "Menu item clicked: " + item.getItemId(), Toast.LENGTH_SHORT).show();
            // Todo: instead of folder list, have a menu item map from which we can pick based on menu item id.
            return true;
        });
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.note_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(noteRepository.listNoteNamesInDirectory(),
                this,
                noteRepository);

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
        noteGridAdapter.updateNotes(noteRepository.listNoteNamesInDirectory());
    }

    public View.OnClickListener onNewNoteTapCallback = v -> {
        // Start NoteActivity to create a new note
        Intent intent = new Intent(HomePageActivity.this, NoteActivity.class);
        startActivity(intent);
    };

}
