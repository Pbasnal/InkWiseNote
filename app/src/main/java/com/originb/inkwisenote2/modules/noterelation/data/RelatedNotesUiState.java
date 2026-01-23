package com.originb.inkwisenote2.modules.noterelation.data;

import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;

import java.util.List;
import java.util.Set;

public class RelatedNotesUiState {
    public final SmartNotebook rootNotebook;
    public final HandwrittenNoteWithImage rootImage;
    public final Set<NoteRelation> relations;
    public final List<SmartNotebook> relatedBooks;

    public RelatedNotesUiState(SmartNotebook root, HandwrittenNoteWithImage img,
                               Set<NoteRelation> rels, List<SmartNotebook> books) {
        this.rootNotebook = root;
        this.rootImage = img;
        this.relations = rels;
        this.relatedBooks = books;
    }
}