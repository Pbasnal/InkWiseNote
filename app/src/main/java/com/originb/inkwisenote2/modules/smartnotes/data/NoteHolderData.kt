package com.originb.inkwisenote2.modules.smartnotes.data

import android.graphics.Bitmap
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke
import lombok.AllArgsConstructor

@AllArgsConstructor
class NoteHolderData {
    var noteType: NoteType? = null
    var bitmap: Bitmap? = null
    var pageTemplate: PageTemplate? = null
    var noteText: String? = null
    var strokes: MutableList<Stroke?>? = null

    companion object {
        fun handWrittenNoteData(bitmap: Bitmap?, pageTemplate: PageTemplate?): NoteHolderData {
            return NoteHolderData(NoteType.HANDWRITTEN_PNG, bitmap, pageTemplate, "", null)
        }

        fun handWrittenNoteData(
            bitmap: Bitmap?,
            pageTemplate: PageTemplate?,
            strokes: MutableList<Stroke?>?
        ): NoteHolderData {
            return NoteHolderData(NoteType.HANDWRITTEN_PNG, bitmap, pageTemplate, "", strokes)
        }

        fun textNoteData(textData: String?): NoteHolderData {
            return NoteHolderData(NoteType.TEXT_NOTE, null, null, textData, null)
        }

        fun initNoteData(): NoteHolderData {
            return NoteHolderData(NoteType.NOT_SET, null, null, null, null)
        }
    }
}
