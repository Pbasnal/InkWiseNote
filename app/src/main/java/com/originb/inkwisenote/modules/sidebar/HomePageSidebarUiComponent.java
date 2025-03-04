package com.originb.inkwisenote.modules.sidebar;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.Feature;
import com.originb.inkwisenote.modules.admin.AdminActivity;

public class HomePageSidebarUiComponent {
    private ConfigReader configReader;
    private AppCompatActivity appCompatActivity;

    private DrawerLayout drawerLayout;
    private RecyclerView navigationRecyclerView;

    private Toolbar toolbar;
    private Button addNewFolderBtn;
    private Button adminButton;

    public HomePageSidebarUiComponent(AppCompatActivity appCompatActivity, ConfigReader configReader) {
        this.appCompatActivity = appCompatActivity;
        this.configReader = configReader;

        drawerLayout = appCompatActivity.findViewById(R.id.sidebar_drawer_view);
        navigationRecyclerView = appCompatActivity.findViewById(R.id.navigation_sidebar_view);
        toolbar = appCompatActivity.findViewById(R.id.toolbar);

        addNewFolderBtn = appCompatActivity.findViewById(R.id.btn_new_folder);
        addNewFolderBtn.setVisibility(View.GONE);

        adminButton = appCompatActivity.findViewById(R.id.admin_button);
        adminButton.setVisibility(View.GONE);
    }

    public void createSidebarIfEnabled() {
        configReader.runIfFeatureEnabled(Feature.ADMIN_VIEW, this::setAdminButton);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                appCompatActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setAdminButton() {
        adminButton.setVisibility(View.VISIBLE);
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(appCompatActivity, AdminActivity.class);
            appCompatActivity.startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}
