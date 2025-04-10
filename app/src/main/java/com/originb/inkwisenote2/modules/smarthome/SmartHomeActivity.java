package com.originb.inkwisenote2.modules.smarthome;

import android.os.Bundle;
import android.view.View;
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
import com.originb.inkwisenote2.AppMainActivity;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class SmartHomeActivity extends AppCompatActivity {

    private SmartHomePageViewModel smartHomePageViewModel;

    private RecentNotebooks recentNotebooks;
    private QueriedNotebooks queriedNotebooks;

    private FloatingActionButton addNewNoteButton;

    private TextView createdByUsText;
    private TextView trackedByUserText;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_home);

        AppMainActivity.registerRepos(this);
        AppMainActivity.registerConfigs(this);

        // Initialize ViewModel
        smartHomePageViewModel = new ViewModelProvider(this).get(SmartHomePageViewModel.class);

        if (recentNotebooks == null) {
            recentNotebooks = new RecentNotebooks(this);
        }
        recentNotebooks.onCreate();

        if (queriedNotebooks == null) {
            queriedNotebooks = new QueriedNotebooks(this);
        }
        queriedNotebooks.onCreate();

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


        createdByUsText = findViewById(R.id.created_by_us_text);
        trackedByUserText = findViewById(R.id.queried_notes_text);


        createdByUsText.setVisibility(View.GONE);
        trackedByUserText.setVisibility(View.GONE);

        addNewNoteButton = findViewById(R.id.add_new_note_btn);
        addNewNoteButton.setOnClickListener(v -> {
            Routing.SmartNotebookActivity.newNoteIntent(this, getFilesDir().getPath());
        });

        // Observe notebooks LiveData
        smartHomePageViewModel.getUserNotebooks().observe(this, recentNotebooks::showNotebooks);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_queries) {
                drawerLayout.closeDrawer(GravityCompat.START);
                // Open QueryCreationActivity
                Routing.QueryActivity.openQueryActivity(this);
                return true;
            }
            if (itemId == R.id.admin_button) {
                Routing.AdminActivity.openAdminActivity(this);
            }
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


    class RecentNotebooks {
        private SmartHomeActivity activity;
        private RecyclerView userNotebooksRecyclerView;
        private SmartNoteGridAdapter smartNoteGridAdapter;

        private TextView createdByUserText;

        public RecentNotebooks(SmartHomeActivity activity) {
            this.activity = activity;
        }

        public void onCreate() {
            userNotebooksRecyclerView = findViewById(R.id.user_created_notebooks);
            createdByUserText = findViewById(R.id.created_by_user_text);
            createdByUserText.setVisibility(View.GONE);

            userNotebooksRecyclerView.setLayoutManager(
                    new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));

            smartNoteGridAdapter = new SmartNoteGridAdapter(activity, new ArrayList<>(), true);

//        BackgroundOps.execute(() -> noteRelationDao.getAllNoteRelations(),
//                AppState::updatedRelatedNotes
//        );

            userNotebooksRecyclerView.setAdapter(smartNoteGridAdapter);
        }

        public void showNotebooks(List<SmartNotebook> notebooks) {
            if (CollectionUtils.isEmpty(notebooks)) return;
            smartNoteGridAdapter.setSmartNotebooks(notebooks);
            createdByUserText.setVisibility(View.VISIBLE);
        }
    }

    class QueriedNotebooks {
        private SmartHomeActivity activity;
        private RecyclerView queriedNotebooksRecyclerView;
        private QueryResultsAdapter queryResultsAdapter;

        private TextView queriedNotesText;

        public QueriedNotebooks(SmartHomeActivity activity) {
            this.activity = activity;
        }

        public void onCreate() {
            queriedNotebooksRecyclerView = findViewById(R.id.queried_notes);
            queriedNotesText = findViewById(R.id.queried_notes_text);
            queriedNotesText.setVisibility(View.GONE);

            queriedNotebooksRecyclerView.setLayoutManager(
                    new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

            queryResultsAdapter = new QueryResultsAdapter(activity);
            queriedNotebooksRecyclerView.setAdapter(queryResultsAdapter);

            activity.smartHomePageViewModel.getLiveQueryResults().observe(activity, results -> {
                if (results == null) return;
                queryResultsAdapter.setData(results);
                queriedNotesText.setVisibility(View.VISIBLE);
            });
        }
    }
}


