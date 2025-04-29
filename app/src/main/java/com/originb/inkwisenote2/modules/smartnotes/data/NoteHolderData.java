package com.originb.inkwisenote2.modules.smartnotes.data;

import android.graphics.Bitmap;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NoteHolderData {
    public NoteType noteType;
    public Bitmap bitmap;
    public PageTemplate pageTemplate;
    public String noteText;
    public List<Stroke> strokes;

    public static NoteHolderData handWrittenNoteData(Bitmap bitmap, PageTemplate pageTemplate) {
        return new NoteHolderData(NoteType.HANDWRITTEN_PNG, bitmap, pageTemplate, "", null);
    }
    
    public static NoteHolderData handWrittenNoteData(Bitmap bitmap, PageTemplate pageTemplate, List<Stroke> strokes) {
        return new NoteHolderData(NoteType.HANDWRITTEN_PNG, bitmap, pageTemplate, "", strokes);
    }

    public static NoteHolderData textNoteData(String textData) {
        return new NoteHolderData(NoteType.TEXT_NOTE, null, null, textData, null);
    }

    public static NoteHolderData initNoteData() {
        return new NoteHolderData(NoteType.NOT_SET, null, null, null, null);
    }
}
