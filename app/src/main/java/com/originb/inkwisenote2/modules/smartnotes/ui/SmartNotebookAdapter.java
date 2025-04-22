package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartNotebookAdapter extends RecyclerView.Adapter<SmartNotebookAdapter.FragmentViewHolder> {

    private final Logger logger = new Logger("SmartNotebookAdapter");
    private final AppCompatActivity parentActivity;
    private final SmartNotebookViewModel viewModel;

    private SmartNotebook smartNotebook;
    private final SmartNotebookRepository smartNotebookRepository;

    // noteId to card mapping
    private final Map<Long, FragmentViewHolder> noteCards = new HashMap<>();
    private Map<Long, NoteFragment> fragments = new HashMap<>();
    private FragmentManager fragmentManager;

    private static final int VIEW_TYPE_INIT = 0;
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_HANDWRITTEN = 2;

    public SmartNotebookAdapter(AppCompatActivity parentActivity,
                                SmartNotebook smartNotebook,
                                SmartNotebookViewModel viewModel) {
        this.parentActivity = parentActivity;
        this.smartNotebook = smartNotebook;
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.fragmentManager = parentActivity.getSupportFragmentManager();
        this.viewModel = viewModel;
    }

    public void setSmartNotebook(SmartNotebook smartNotebook) {
        this.smartNotebook = smartNotebook;
        this.fragments = new HashMap<>();

        List<AtomicNoteEntity> atomicNotes = smartNotebook.getAtomicNotes();

        for (AtomicNoteEntity atomicNote : atomicNotes) {
            NoteFragment fragment = createFragmentByType(atomicNote.getNoteType());
            fragment.setAtomicNote(atomicNote);
            fragment.setBookId(smartNotebook.smartBook.getBookId());
            fragments.put(atomicNote.getNoteId(), fragment);
        }
        notifyDataSetChanged();
    }

    public void setSmartNotebook(SmartNotebook smartNotebook, int indexOfUpdatedNote) {
        this.smartNotebook = smartNotebook;
        if (fragments == null) {
            fragments = new HashMap<>();
        }

        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(indexOfUpdatedNote);
        NoteFragment fragment = createFragmentByType(atomicNote.getNoteType());
        fragment.setAtomicNote(atomicNote);
        fragment.setBookId(smartNotebook.smartBook.getBookId());
        fragments.put(atomicNote.getNoteId(), fragment);

        notifyItemInserted(indexOfUpdatedNote);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @NotNull
    @Override
    public FragmentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note_page, parent, false);

        // Create a truly unique ID for the fragment container
        FragmentViewHolder holder = new FragmentViewHolder(view);
        holder.fragmentContainer.setId(viewType + 1);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FragmentViewHolder holder, int position) {
        // Store the unique container ID for this position
        final int containerId = holder.fragmentContainer.getId();
        List<AtomicNoteEntity> atomicNotes = smartNotebook.getAtomicNotes();

        if (fragments == null || fragments.isEmpty()) return;
        if (position < 0 || position >= atomicNotes.size()) return;

        // Get the fragment for this position
        final Fragment fragment = fragments.get(atomicNotes.get(position).getNoteId());
        final String fragmentTag = "fragment_" + position;

        logger.debug("Binding position " + position + " with container ID " + containerId);

        // Check if this fragment is already attached to this container
        if (fragment.isAdded() && fragment.getId() == containerId) {
            logger.debug("Fragment already attached to this container. Skipping.");
            return;
        }

        // If fragment is already added to a different container, detach it first
        if (fragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .detach(fragment)
                    .commitNow();
        }

        // Wait until the view is properly attached to the window
        holder.itemView.post(() -> {
            if (holder.getAdapterPosition() == position &&
                    holder.itemView.isAttachedToWindow() &&
                    !parentActivity.isFinishing()) {

                try {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    // Remove any existing fragment in this container
                    Fragment existingFragment = fragmentManager.findFragmentById(containerId);
                    if (existingFragment != null && existingFragment != fragment) {
                        transaction.remove(existingFragment);
                    }

                    // Add the fragment to this container
                    if (fragment.isDetached()) {
                        transaction.attach(fragment);
                    } else if (!fragment.isAdded()) {
                        transaction.add(containerId, fragment, fragmentTag);
                    } else {
                        transaction.replace(containerId, fragment, fragmentTag);
                    }

                    transaction.commitNowAllowingStateLoss();
                    logger.debug("Added fragment for position " + position + " to container " + containerId);
                } catch (Exception e) {
                    logger.error("Error adding fragment: " + e.getMessage(), e);
                }
            }
        });
    }

    // should not be needed since we are saving the notes whenever the page is changed
    @Override
    public void onViewRecycled(@NonNull FragmentViewHolder holder) {
        super.onViewRecycled(holder);
        if (smartNotebook == null) return;

//        BackgroundOps.execute(() ->
//            smartNotebookRepository.getSmartNotebooks(smartNotebook.smartBook.getBookId()),
//            existingSmartNotebook -> {
//                if (existingSmartNotebook.isPresent()) {
//                    holder.saveNote();
//                }
//            }
//        );
    }

    public void updateNoteType(AtomicNoteEntity atomicNote, String newNoteType) {
        if (smartNotebook == null) return;

        int position = smartNotebook.getAtomicNotes().indexOf(atomicNote);
        if (position != -1) {
            atomicNote.setNoteType(newNoteType);

            NoteFragment fragment = createFragmentByType(atomicNote.getNoteType());
            fragment.setAtomicNote(atomicNote);
            fragment.setBookId(smartNotebook.smartBook.getBookId());

            fragments.put(atomicNote.getNoteId(), fragment);

            BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(smartNotebook, parentActivity));
            notifyItemChanged(position);
        }
    }

//    No need to save all notes since notes are saved when page changes. Only the current note needs saving
//    public void saveNote(String noteTitle) {
//        if (smartNotebook == null) return;
//
//        BackgroundOps.execute(() -> {
//            for (NoteHolder noteHolder : noteCards.values()) {
//                noteHolder.saveNote();
//            }
//
//            // update title
//            smartNotebook.getSmartBook().setTitle(noteTitle);
//            smartNotebookRepository.updateNotebook(smartNotebook, parentActivity);
//        });
//    }

    public void removeNoteCard(long noteId) {
        if (smartNotebook == null || !noteCards.containsKey(noteId)) return;

        int position = noteCards.get(noteId).getAdapterPosition();
        noteCards.remove(noteId);
        notifyItemRemoved(position);
    }

//    public void saveNotebookPageAt(int currentVisibleItemIndex, AtomicNoteEntity atomicNote) {
//        if (smartNotebook == null || atomicNote == null ||
//            !noteCards.containsKey(atomicNote.getNoteId())) {
//            return;
//        }
//
//        NoteHolder noteHolder = noteCards.get(atomicNote.getNoteId());
//        BackgroundOps.execute(noteHolder::saveNote);
//    }

    @Override
    public int getItemCount() {
        return smartNotebook != null ? smartNotebook.atomicNotes.size() : 0;
    }

    private NoteFragment createFragmentByType(String noteType) {
        if (NoteType.TEXT_NOTE.toString().equals(noteType)) {
            return new TextNoteFragment();
        } else if (NoteType.HANDWRITTEN_PNG.toString().equals(noteType)) {
            return new HandwrittenNoteFragment();
        }
        return new InitNoteFragment(this);
    }

    public NoteHolderData getNoteData(long noteId) {
        return fragments.get(noteId).getNoteHolderData();
    }

    static class FragmentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        FrameLayout fragmentContainer;

        public FragmentViewHolder(@NonNull View itemView) {
            super(itemView);
            fragmentContainer = itemView.findViewById(R.id.note_fragment_container);
        }
    }
}
