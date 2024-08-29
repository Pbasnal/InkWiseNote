package com.originb.inkwisenote.data.sidebar;

import lombok.Getter;


@Getter
public class MenuItemData {
    private int parentMenuId;
    private int groupId;
    private int itemId;
    private int order;
    private String title;
    private int icon;

    public MenuItemData(int parentMenuId, int groupId, int itemId, int order, String title, int icon) {
        this.parentMenuId = parentMenuId;
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.title = title;
        this.icon = icon;
    }
}

