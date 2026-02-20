package com.originb.inkwisenote2.modules.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.originb.inkwisenote2.AppMainActivity.Companion.registerConfigs
import com.originb.inkwisenote2.AppMainActivity.Companion.registerRepos
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.MapsUtils.isEmpty
import com.originb.inkwisenote2.common.Routing.AdminActivity.openAdminActivity
import com.originb.inkwisenote2.common.Routing.NoteSearchActivity.openAllNotebooksPage
import com.originb.inkwisenote2.common.Routing.NoteSearchActivity.openSearchPage
import com.originb.inkwisenote2.common.Routing.QueryActivity.openQueryActivity
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.newNoteIntent
import com.originb.inkwisenote2.modules.fileexplorer.DirectoryExplorerActivity
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import org.koin.android.compat.ViewModelCompat.getViewModel

class SmartHomeActivity : AppCompatActivity() {
    private var smartHomePageViewModel: SmartHomePageViewModel? = null

    private var recentNotebooks: RecentNotebooks? = null
    private var queriedNotebooks: QueriedNotebooks? = null

    private var addNewNoteButton: FloatingActionButton? = null

    private var drawerLayout: DrawerLayout? = null
    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_home)

        registerRepos(this)
        registerConfigs(this)

        // Initialize ViewModel
        smartHomePageViewModel = getViewModel<SmartHomePageViewModel>(this, SmartHomePageViewModel::class.java)

        if (recentNotebooks == null) {
            recentNotebooks = RecentNotebooks(this)
        }
        recentNotebooks!!.onCreate()

        if (queriedNotebooks == null) {
            queriedNotebooks = QueriedNotebooks(this)
        }
        queriedNotebooks!!.onCreate()

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove default title
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        }

        // Add custom view to toolbar
        toolbar.addView(getLayoutInflater().inflate(R.layout.custom_toolbar, null))

        // Setup search button
        val searchButton = findViewById<ImageButton>(R.id.search_button)
        searchButton.setOnClickListener(View.OnClickListener { v: View? -> openSearchPage(this) })

        // Setup drawer layout
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout!!.addDrawerListener(drawerToggle!!)
        drawerToggle!!.syncState()

        addNewNoteButton = findViewById<FloatingActionButton>(R.id.add_new_note_btn)
        addNewNoteButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            newNoteIntent(this, getFilesDir().getPath())
        })

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item: MenuItem? ->
            val itemId = item!!.getItemId()
            if (itemId == R.id.nav_queries) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                // Open QueryCreationActivity
                openQueryActivity(this)
                return@setNavigationItemSelectedListener true
            }
            if (itemId == R.id.admin_button) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                openAdminActivity(this)
                return@setNavigationItemSelectedListener true
            }
            if (itemId == R.id.nav_file_explorer) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                // Open DirectoryExplorerActivity
                val intent = Intent(this, DirectoryExplorerActivity::class.java)
                startActivity(intent)
                return@setNavigationItemSelectedListener true
            }
            false
        })
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    internal inner class RecentNotebooks(private val activity: SmartHomeActivity) {
        private var userNotebooksRecyclerView: RecyclerView? = null
        private var smartNoteGridAdapter: SmartNoteGridAdapter? = null

        private var createdByUserText: TextView? = null

        // Setup open all notebooks button
        private var openAllNotebooksButton: ImageButton? = null
        private var createNotesPrompt: TextView? = null

        fun onCreate() {
            userNotebooksRecyclerView = findViewById<RecyclerView>(R.id.user_created_notebooks)
            createdByUserText = findViewById<TextView>(R.id.created_by_user_text)
            createdByUserText!!.setVisibility(View.GONE)

            // Setup open all notebooks button
            openAllNotebooksButton = findViewById<ImageButton>(R.id.open_all_notebooks)
            openAllNotebooksButton!!.setVisibility(View.GONE)
            openAllNotebooksButton!!.setOnClickListener(View.OnClickListener { v: View? ->
                openAllNotebooksPage(activity)
            })

            createNotesPrompt = findViewById<TextView>(R.id.take_notes_prompt)
            createNotesPrompt!!.setVisibility(View.GONE)

            userNotebooksRecyclerView!!.setLayoutManager(
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            )

            smartNoteGridAdapter = SmartNoteGridAdapter(activity, ArrayList<SmartNotebook?>(), true)

            userNotebooksRecyclerView!!.setAdapter(smartNoteGridAdapter)

            // Observe notebooks LiveData
            smartHomePageViewModel!!.getUserNotebooks().observe(
                activity,
                Observer { notebooks: MutableList<SmartNotebook?>? -> this.showNotebooks(notebooks) })
        }

        fun showNotebooks(notebooks: MutableList<SmartNotebook?>?) {
            if (CollectionUtils.isEmpty(notebooks)) {
                createdByUserText!!.setVisibility(View.GONE)
                createNotesPrompt!!.setVisibility(View.VISIBLE)
                openAllNotebooksButton!!.setVisibility(View.GONE)
                return
            }
            smartNoteGridAdapter!!.setSmartNotebooks(notebooks)
            createdByUserText!!.setVisibility(View.VISIBLE)
            createNotesPrompt!!.setVisibility(View.GONE)
            openAllNotebooksButton!!.setVisibility(View.VISIBLE)
        }
    }

    internal inner class QueriedNotebooks(private val activity: SmartHomeActivity) {
        private var queriedNotebooksRecyclerView: RecyclerView? = null
        private var queryResultsAdapter: QueryResultsAdapter? = null

        private var createNewStandingQueriesMsg: TextView? = null
        private var createNewStandingQueriesBtn: Button? = null

        private var queriedNotesText: TextView? = null

        fun onCreate() {
            queriedNotebooksRecyclerView = findViewById<RecyclerView>(R.id.queried_notes)
            queriedNotesText = findViewById<TextView>(R.id.queried_notes_text)
            queriedNotesText!!.setVisibility(View.GONE)

            createNewStandingQueriesMsg = findViewById<TextView>(R.id.add_standing_queries_msg)
            createNewStandingQueriesMsg!!.setVisibility(View.GONE)

            createNewStandingQueriesBtn = findViewById<Button>(R.id.create_standing_query_btn)
            updateCreateStandingQueryBtnVisibility()
            createNewStandingQueriesBtn!!.setOnClickListener(View.OnClickListener { v: View? ->
                openQueryActivity(activity)
            })

            queriedNotebooksRecyclerView!!.setLayoutManager(
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            )

            queryResultsAdapter = QueryResultsAdapter(activity)
            queriedNotebooksRecyclerView!!.setAdapter(queryResultsAdapter)

            smartHomePageViewModel!!.getLiveQueryResults()
                .observe(activity, Observer { results: MutableMap<String?, MutableSet<QueryNoteResult?>?>? ->
                    queryResultsAdapter!!.setData(results)
                    if (isEmpty(results)) {
                        queriedNotesText!!.setVisibility(View.GONE)
                        updateCreateStandingQueryVisibility()
                    } else {
                        queriedNotesText!!.setVisibility(View.VISIBLE)
                        createNewStandingQueriesMsg!!.setVisibility(View.GONE)
                    }
                    updateCreateStandingQueryBtnVisibility()
                })

            smartHomePageViewModel!!.getShowStandingQueryPrompt().observe(activity, Observer { show: Boolean? ->
                this.createNewStandingQueriesMsg!!.setVisibility(if (show) View.VISIBLE else View.GONE)
            })
        }

        private fun updateCreateStandingQueryBtnVisibility() {
            val userNotebooks = activity.smartHomePageViewModel!!.getUserNotebooks().getValue()

            if (CollectionUtils.isEmpty(userNotebooks)) {
                createNewStandingQueriesBtn!!.setVisibility(View.GONE)
            } else {
                createNewStandingQueriesBtn!!.setVisibility(View.VISIBLE)
            }
        }

        private fun updateCreateStandingQueryVisibility() {
            activity.smartHomePageViewModel!!.refreshDashboardState()
            //            BackgroundOps.execute(() -> activity.smartHomePageViewModel.userHasAnyQuery(),
//                    hasAtLeastOneQuery -> {
//                        List<SmartNotebook> userNotebooks = activity.smartHomePageViewModel
//                                .getUserNotebooks().getValue();
//                        boolean hasSomeNotebooks = !CollectionUtils.isEmpty(userNotebooks);
//                        if (!hasAtLeastOneQuery && hasSomeNotebooks) {
//                            createNewStandingQueriesMsg.setVisibility(View.VISIBLE);
//                        } else {
//                            createNewStandingQueriesMsg.setVisibility(View.GONE);
//                        }
//                    }
//            );
        }
    }
}


