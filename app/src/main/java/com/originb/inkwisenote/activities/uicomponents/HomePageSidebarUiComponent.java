package com.originb.inkwisenote.activities.uicomponents;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.activities.AdminActivity;
import com.originb.inkwisenote.adapters.ExpandableMenuListAdapter;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.Feature;
import com.originb.inkwisenote.data.sidebar.MenuItemData;
import com.originb.inkwisenote.io.FolderHierarchyRepository;
import com.originb.inkwisenote.modules.repositories.DirectoryContents;
import com.originb.inkwisenote.modules.repositories.FolderItem;

import java.util.ArrayList;
import java.util.List;

public class HomePageSidebarUiComponent {
    private ConfigReader configReader;
    private AppCompatActivity appCompatActivity;

    private FolderHierarchyRepository folderHierarchyRepository;

    private DrawerLayout drawerLayout;
    private RecyclerView navigationRecyclerView;
    private ExpandableMenuListAdapter expandableMenuListAdapter;
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
        configReader.runIfFeatureEnabled(Feature.HOME_PAGE_SIDEBAR_FOLDERS_LIST, this::addFoldersToTheSidebar);

        // not sure what this does.
        // appCompatActivity.setSupportActionBar(toolbar);

        configReader.runIfFeatureEnabled(Feature.ADMIN_VIEW, this::setAdminButton);

        navigationRecyclerView.setLayoutManager(new GridLayoutManager(appCompatActivity, 1));
        navigationRecyclerView.setAdapter(expandableMenuListAdapter);

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

    private void addFoldersToTheSidebar() {
        folderHierarchyRepository = new FolderHierarchyRepository(appCompatActivity.getFilesDir());
        DirectoryContents directoryContents = folderHierarchyRepository.getFilesInDirectory();
        List<FolderItem> folders = directoryContents.getFolders();
        List<MenuItemData> menuItems = new ArrayList<>();
        folders.forEach(folder -> {
            MenuItemData menuItem = new MenuItemData(folder.getId(), folder.getFolderName());
            menuItems.add(menuItem);
        });
        expandableMenuListAdapter = new ExpandableMenuListAdapter(menuItems);

        addNewFolderBtn.setVisibility(View.VISIBLE);
    }
}
