package com.originb.inkwisenote2.modules.sidebar

import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.config.Feature
import com.originb.inkwisenote2.modules.admin.AdminActivity

class HomePageSidebarUiComponent(
    private val appCompatActivity: AppCompatActivity,
    private val configReader: ConfigReader?
) {
    private val drawerLayout: DrawerLayout = appCompatActivity.findViewById(R.id.sidebar_drawer_view)
    private val navigationRecyclerView: RecyclerView = appCompatActivity.findViewById(R.id.navigation_sidebar_view)

    private val toolbar: Toolbar = appCompatActivity.findViewById(R.id.toolbar)
    private val addNewFolderBtn: Button = appCompatActivity.findViewById(R.id.btn_new_folder)
    private val adminButton: Button

    init {
        addNewFolderBtn.visibility = View.GONE

        adminButton = appCompatActivity.findViewById(R.id.admin_button)
        adminButton.visibility = View.GONE
    }

    fun createSidebarIfEnabled() {
        configReader!!.runIfFeatureEnabled(Feature.ADMIN_VIEW) { this.setAdminButton() }

        val toggle = ActionBarDrawerToggle(
            appCompatActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setAdminButton() {
        adminButton.visibility = View.VISIBLE
        adminButton.setOnClickListener { v: View? ->
            val intent = Intent(appCompatActivity, AdminActivity::class.java)
            appCompatActivity.startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
