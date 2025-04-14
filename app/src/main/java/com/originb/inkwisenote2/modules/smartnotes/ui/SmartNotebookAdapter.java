package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.modules.handwrittennotes.ui.HandwrittenNoteHolder;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.textnote.TextNoteHolder;

import java.util.List;

public class SmartNotebookAdapter extends RecyclerView.Adapter<NoteHolder> {
    
    private final ComponentActivity parentActivity;
    private SmartNotebook smartNotebook;
    private List<AtomicNoteEntity> notes;
    private final PageSaveListener pageSaveListener;

    private static final int VIEW_TYPE_INIT = 0;
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_HANDWRITTEN = 2;

    public interface PageSaveListener {
        void onPageSave(AtomicNoteEntity note, int position);
    }
    
    public SmartNotebookAdapter(ComponentActivity parentActivity, SmartNotebook smartNotebook, PageSaveListener listener) {
        this.parentActivity = parentActivity;
        this.smartNotebook = smartNotebook;
        this.pageSaveListener = listener;
        if (smartNotebook != null) {
            this.notes = smartNotebook.getAtomicNotes();
        }
    }
    
    public void setSmartNotebook(SmartNotebook smartNotebook) {
        this.smartNotebook = smartNotebook;
        this.notes = smartNotebook.getAtomicNotes();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(position);
        if (NoteType.NOT_SET.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_INIT;
        } else if (NoteType.TEXT_NOTE.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_TEXT;
        } else if (NoteType.HANDWRITTEN_PNG.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_HANDWRITTEN;
        }
        return VIEW_TYPE_INIT;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_TEXT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_text_layout, parent, false);
                return new TextNoteHolder(itemView, parentActivity, smartNotebookRepository);
            case VIEW_TYPE_HANDWRITTEN:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_drawing_layout, parent, false);
                return new HandwrittenNoteHolder(itemView, parentActivity, smartNotebookRepository);
            case VIEW_TYPE_INIT:
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_init_layout, parent, false);
                return new InitNoteHolder(itemView, parentActivity, smartNotebookRepository, this);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        if (notes != null && position < notes.size()) {
            AtomicNoteEntity note = notes.get(position);
            holder.bind(note);
        }
    }
    
    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }
    
    public void saveNotebookPageAt(int position, AtomicNoteEntity note) {
        if (pageSaveListener != null && position >= 0 && position < getItemCount()) {
            pageSaveListener.onPageSave(note, position);
        }
    }
    
    public void removeNoteCard(long noteId) {
        if (notes != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getNoteId() == noteId) {
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }
    
    public void saveNote(String title) {
        if (smartNotebook != null) {
            smartNotebook.smartBook.setTitle(title);
        }
    }
}
