package com.originb.inkwisenote.adapters.smartnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmartNoteGridAdapter extends RecyclerView.Adapter<GridNoteCardHolder> {

    private final ComponentActivity parentActivity;
    private final SmartNotebookRepository smartNotebookRepository;

    private List<SmartNotebook> smartNoteBooks;

    private final Map<Long, GridNoteCardHolder> noteCards = new HashMap<>();

    public SmartNoteGridAdapter(ComponentActivity parentActivity, List<SmartNotebook> smartNotebooks) {
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.parentActivity = parentActivity;
        this.smartNoteBooks = smartNotebooks;
    }

    public void setSmartNoteBooks(List<SmartNotebook> smartNoteBooks) {
        this.smartNoteBooks = new ArrayList<>(smartNoteBooks);

        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public GridNoteCardHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        return new GridNoteCardHolder(this, itemView, parentActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GridNoteCardHolder gridNoteCardHolder, int position) {
        SmartNotebook smartNotebook = smartNoteBooks.get(position);
        gridNoteCardHolder.setNote(smartNotebook);
        noteCards.put(smartNotebook.getSmartBook().getBookId(), gridNoteCardHolder);
    }

    @Override
    public int getItemCount() {
        return smartNoteBooks.size();
    }

    public void removeSmartNotebook(int adapterPosition) {
        smartNoteBooks.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }
}
