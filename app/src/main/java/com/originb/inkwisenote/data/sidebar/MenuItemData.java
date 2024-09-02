package com.originb.inkwisenote.data.sidebar;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MenuItemData {
    private int itemId;
    private String title;
    private List<MenuItemData> childItems;
    private boolean isExpanded;

    public MenuItemData(int itemId, String title) {
        this.itemId = itemId;
        this.title = title;
        childItems = new ArrayList<>();
    }

    public void addChildItem(MenuItemData child) {
        childItems.add(child);
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

}

