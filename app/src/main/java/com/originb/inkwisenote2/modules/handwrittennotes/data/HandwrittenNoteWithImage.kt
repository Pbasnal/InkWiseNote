package com.originb.inkwisenote2.modules.handwrittennotes.data

import android.graphics.Bitmap
import java.util.*

class HandwrittenNoteWithImage {
    @JvmField
    var handwrittenNoteEntity: HandwrittenNoteEntity? = null
    @JvmField
    var noteImage: Optional<Bitmap?>? = null
}
