package com.originb.inkwisenote2.modules.smartnotes.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.originb.inkwisenote2.modules.handwrittennotes.ui.HandwrittenNoteFragment;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.textnote.TextNoteFragment;

import java.util.HashSet;
import java.util.Set;

/**
 * Adapter for the ViewPager2 that manages fragments for different note types
 */
public class NotePagerAdapter extends FragmentStateAdapter {

    private SmartNotebook smartNotebook;
    private final SmartNotebookViewModel viewModel;

    private Set<Long> changedNotePositions;

    public NotePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.viewModel = new ViewModelProvider(fragmentActivity).get(SmartNotebookViewModel.class);
        changedNotePositions = new HashSet<>();
    }

    public void setSmartNotebook(SmartNotebook smartNotebook) {
        this.smartNotebook = smartNotebook;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (smartNotebook == null || position >= smartNotebook.getAtomicNotes().size()) {
            return InitNoteFragment.newInstance(-1, -1);
        }

        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(position);
        long bookId = smartNotebook.getSmartBook().getBookId();

        if (NoteType.TEXT_NOTE.toString().equals(atomicNote.getNoteType())) {
            return TextNoteFragment.newInstance(atomicNote.getNoteId(), bookId);
        } else if (NoteType.HANDWRITTEN_PNG.toString().equals(atomicNote.getNoteType())) {
            return HandwrittenNoteFragment.newInstance(atomicNote.getNoteId(), bookId);
        } else {
            return InitNoteFragment.newInstance(atomicNote.getNoteId(), bookId);
        }
    }

    @Override
    public boolean containsItem(long position) {
        boolean positionExists = super.containsItem(position);
        boolean shouldUpdateItem = positionExists && changedNotePositions.contains(position);

        if (shouldUpdateItem) changedNotePositions.remove(position);

        // Tell the adapter whether the item exists
        return positionExists && !shouldUpdateItem;
    }

    /**
     * Called when a note type changes, to force recreation of the fragment
     *
     * @param position The position of the note that changed
     */
    public void notifyNoteTypeChanged(int position) {
        changedNotePositions.add((long) position);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return smartNotebook != null ? smartNotebook.getAtomicNotes().size() : 0;
    }
} 