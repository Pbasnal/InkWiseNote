package com.originb.inkwisenote2.modules.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.queries.ui.QueryCreationActivity;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class SmartHomeActivity extends AppCompatActivity {

    private SmartHomePageViewModel smartHomePageViewModel;

    private RecyclerView userNotebooksRecyclerView;
    private SmartNoteGridAdapter smartNoteGridAdapter;

    private FloatingActionButton addNewNoteButton;
    private TextView createdByUserText;
    private TextView createdByUsText;
    private TextView trackedByUserText;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_home);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Remove default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Add custom view to toolbar
        toolbar.addView(getLayoutInflater().inflate(R.layout.custom_toolbar, null));

        // Setup drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
            this, 
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        createdByUserText = findViewById(R.id.created_by_user_text);
        createdByUsText = findViewById(R.id.created_by_us_text);
        trackedByUserText = findViewById(R.id.tracked_by_user_text);

        createdByUserText.setVisibility(View.GONE);
        createdByUsText.setVisibility(View.GONE);
        trackedByUserText.setVisibility(View.GONE);

        // Initialize RecyclerView
        userNotebooksRecyclerView = findViewById(R.id.user_created_notebooks);
        addNewNoteButton = findViewById(R.id.add_new_note_btn);
        addNewNoteButton.setOnClickListener(v -> {
            Routing.SmartNotebookActivity.newNoteIntent(this, getFilesDir().getPath());
        });

        createGridLayoutToShowNotes();
        // Initialize ViewModel
        smartHomePageViewModel = new ViewModelProvider(this).get(SmartHomePageViewModel.class);

        // Observe notebooks LiveData
        smartHomePageViewModel.getUserNotebooks().observe(this, notebooks -> {
            if (CollectionUtils.isEmpty(notebooks)) return;
            smartNoteGridAdapter.setSmartNotebooks(notebooks);
            createdByUserText.setVisibility(View.VISIBLE);
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_queries) {
                // Open QueryCreationActivity
                Intent intent = new Intent(this, QueryCreationActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            // ... handle other menu items ...
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void createGridLayoutToShowNotes() {
        userNotebooksRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>(), true);

//        BackgroundOps.execute(() -> noteRelationDao.getAllNoteRelations(),
//                AppState::updatedRelatedNotes
//        );

        userNotebooksRecyclerView.setAdapter(smartNoteGridAdapter);
    }
}

