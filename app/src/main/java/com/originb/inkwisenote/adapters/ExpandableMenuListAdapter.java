package com.originb.inkwisenote.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.bind(menuItem);
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
                if (menuItem.getChildItems() != null && !menuItem.getChildItems().isEmpty()) {
                    menuItem.setExpanded(!menuItem.isExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        public void bind(MenuItemData menuItem) {
            titleTextView.setText(menuItem.getTitle());
            if (menuItem.isExpanded() && menuItem.getChildItems() != null) {
                for (MenuItemData child : menuItem.getChildItems()) {
                    // Add children dynamically
                    menuItems.add(getAdapterPosition() + 1, child);
                }
            } else if (!menuItem.isExpanded() && menuItem.getChildItems() != null) {
                menuItems.removeAll(menuItem.getChildItems());
            }
        }
    }
}
