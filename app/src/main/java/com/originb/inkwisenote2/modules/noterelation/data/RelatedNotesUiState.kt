package com.originb.inkwisenote2.modules.noterelation.data

import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage
import com.originb.inkwisenote2.modules.repositories.SmartNotebook

class RelatedNotesUiState(
    val rootNotebook: SmartNotebook, val rootImage: HandwrittenNoteWithImage?,
    val relations: MutableSet<NoteRelation>, val relatedBooks: MutableList<SmartNotebook>
) 