package com.originb.inkwisenote.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.sidebar.MenuItemData;

import java.util.List;

public class ExpandableMenuListAdapter extends RecyclerView.Adapter<ExpandableMenuListAdapter.MenuViewHolder> {
    private List<MenuItemData> menuItems;

    public ExpandableMenuListAdapter(List<MenuItemData> menuItems) {
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sidebar_menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItemData menuItem = menuItems.get(position);
        holder.titleTextView.setText(menuItem.getTitle());
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.sidebar_menu_item_title);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                MenuItemData menuItem = menuItems.get(position);
                if (!CollectionUtils.isEmpty(menuItem.getChildItems())) {
                    if (menuItem.isExpanded()) {
                        menuItem.setExpanded(false);
                        removeChildItemsFromMenu(menuItem);
                    } else {
                        menuItem.setExpanded(true);
                        addChildItemsToMenu(menuItem);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        private void addChildItemsToMenu(MenuItemData menuItem) {
            int position = getAdapterPosition();
            for (MenuItemData child : menuItem.getChildItems()) {
                // Add children dynamically
                menuItems.add(position + 1, child);
            }
        }

        private void removeChildItemsFromMenu(MenuItemData menuItem) {
            menuItems.removeAll(menuItem.getChildItems());
        }
    }
}
