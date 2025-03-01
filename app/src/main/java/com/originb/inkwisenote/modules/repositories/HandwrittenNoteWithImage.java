package com.originb.inkwisenote.modules.repositories;

import android.graphics.Bitmap;
import com.originb.inkwisenote.data.entities.handwrittennotedata.HandwrittenNoteEntity;

import java.util.Optional;

public class HandwrittenNoteWithImage {
    public HandwrittenNoteEntity handwrittenNoteEntity;
    public Optional<Bitmap> noteImage;
}
