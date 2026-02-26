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
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.MapsUtils.isEmpty
import com.originb.inkwisenote2.common.Routing.AdminActivity.openAdminActivity
import com.originb.inkwisenote2.common.Routing.NoteSearchActivity.openAllNotebooksPage
import com.originb.inkwisenote2.common.Routing.NoteSearchActivity.openSearchPage
import com.originb.inkwisenote2.common.Routing.QueryActivity.openQueryActivity
import com.originb.inkwisenote2.common.ComposeRouteExtras
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.newNoteIntent
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

//        registerRepos(this)
//        registerConfigs(this)

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
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        // Add custom view to toolbar
        toolbar.addView(layoutInflater.inflate(R.layout.custom_toolbar, null))

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
        navigationView.setNavigationItemSelectedListener navListener@ { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.nav_queries) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                openQueryActivity(this)
                return@navListener true
            }
            if (itemId == R.id.admin_button) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                openAdminActivity(this)
                return@navListener true
            }
            if (itemId == R.id.nav_file_explorer) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                val intent = Intent(this, com.originb.inkwisenote2.ComposeHostActivity::class.java)
                intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_FILE_EXPLORER)
                startActivity(intent)
                return@navListener true
            }
            return@navListener false
        }
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

            smartNoteGridAdapter = SmartNoteGridAdapter(activity, mutableListOf<SmartNotebook>(), true)

            userNotebooksRecyclerView!!.setAdapter(smartNoteGridAdapter)

            // Observe notebooks LiveData
            smartHomePageViewModel!!.getUserNotebooks().observe(
                activity,
                Observer { notebooks: MutableList<SmartNotebook> -> this.showNotebooks(notebooks) })
        }

        fun showNotebooks(notebooks: MutableList<SmartNotebook>) {
            if (CollectionUtils.isEmpty(notebooks)) {
                createdByUserText!!.visibility = View.GONE
                createNotesPrompt!!.visibility = View.VISIBLE
                openAllNotebooksButton!!.visibility = View.GONE
                return
            }
            smartNoteGridAdapter!!.setSmartNotebooks(notebooks)
            createdByUserText!!.visibility = View.VISIBLE
            createNotesPrompt!!.visibility = View.GONE
            openAllNotebooksButton!!.visibility = View.VISIBLE
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
                .observe(activity, Observer { results: MutableMap<String, MutableSet<QueryNoteResult>> ->
                    queryResultsAdapter!!.setData(results)
                    if (isEmpty(results)) {
                        queriedNotesText!!.visibility = View.GONE
                        updateCreateStandingQueryVisibility()
                    } else {
                        queriedNotesText!!.visibility = View.VISIBLE
                        createNewStandingQueriesMsg!!.visibility = View.GONE
                    }
                    updateCreateStandingQueryBtnVisibility()
                })

            smartHomePageViewModel!!.showStandingQueryPrompt.observe(activity, Observer { show: Boolean? ->
                createNewStandingQueriesMsg!!.visibility = if (show == true) View.VISIBLE else View.GONE
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
        }
    }
}


