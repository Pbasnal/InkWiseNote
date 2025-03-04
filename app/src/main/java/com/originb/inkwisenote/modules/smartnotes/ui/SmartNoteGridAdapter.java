package com.originb.inkwisenote.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.Routing;
import com.originb.inkwisenote.config.AppState;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class SmartNoteGridAdapter extends RecyclerView.Adapter<GridNoteCardHolder> {
    private final Logger logger = new Logger("SmartNoteGridAdapter");

    private final ComponentActivity parentActivity;

    private List<SmartNotebook> smartNotebooks;

    private final Map<Long, Boolean> bookRelationMap = new HashMap<>();

    private final Map<Long, GridNoteCardHolder> bookCards = new HashMap<>();

    public SmartNoteGridAdapter(ComponentActivity parentActivity, List<SmartNotebook> smartNotebooks) {
        this.parentActivity = parentActivity;
        this.smartNotebooks = smartNotebooks;

        AppState.observeNoteRelationships(parentActivity, this::updateNoteRelations);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteStatusChange(Events.NoteStatus noteStatus) {
        long bookId = noteStatus.smartNotebook.getSmartBook().getBookId();

        if(bookCards.containsKey(bookId)) {
            GridNoteCardHolder holder = bookCards.get(bookId);
            holder.updateNoteStatus(noteStatus);
        }
    }

    public void updateNoteRelations(Set<NoteRelation> updatedNoteRelationMap) {
        logger.debug("Updating note relations", updatedNoteRelationMap);

        Set<Long> relatedBookIds = updatedNoteRelationMap.stream()
                .map(NoteRelation::getBookId)
                .collect(Collectors.toSet());
        relatedBookIds.addAll(updatedNoteRelationMap.stream()
                .map(NoteRelation::getRelatedBookId)
                .collect(Collectors.toSet()));

        for (Long bookId : bookCards.keySet()) {
            boolean isBookRelated = relatedBookIds.contains(bookId);
            GridNoteCardHolder bookHolder = bookCards.get(bookId);
            bookHolder.updateNoteRelation(isBookRelated);
            bookRelationMap.put(bookId, isBookRelated);
        }

        // sets that the book is related even if bookCards haven't been loaded
        for (Long bookId : relatedBookIds) {
            bookRelationMap.put(bookId, true);
        }
    }

    public void setSmartNotebooks(List<SmartNotebook> smartNotebooks) {
        this.smartNotebooks = new ArrayList<>(smartNotebooks);

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
        SmartNotebook smartNotebook = smartNotebooks.get(position);

        logger.debug("Setting book at position: " + position, smartNotebook.getSmartBook());
        gridNoteCardHolder.setNote(smartNotebook);
        bookCards.put(smartNotebook.getSmartBook().getBookId(), gridNoteCardHolder);

        long bookId = smartNotebook.getSmartBook().getBookId();
        if (!bookRelationMap.containsKey(bookId)) {
            logger.debug("Book doesn't have any relations yet. bookId: " + bookId);
            return;
        }

        gridNoteCardHolder.updateNoteRelation(bookRelationMap.get(bookId));
    }

    // Callback when an item is detached (item goes out of view)
    @Override
    public void onViewDetachedFromWindow(@NonNull GridNoteCardHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getAdapterPosition();

        if (position < 0 || smartNotebooks.size() <= position) return;

        SmartNotebook smartNotebook = smartNotebooks.get(holder.getAdapterPosition() - 1);

        bookCards.remove(smartNotebook.getSmartBook().getBookId());
    }

    @Override
    public int getItemCount() {
        return smartNotebooks.size();
    }

    public void removeSmartNotebook(int adapterPosition) {
        smartNotebooks.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }
}
