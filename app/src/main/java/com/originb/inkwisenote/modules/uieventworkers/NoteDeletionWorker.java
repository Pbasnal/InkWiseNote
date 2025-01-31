package com.originb.inkwisenote.modules.uieventworkers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import org.jetbrains.annotations.NotNull;

public class NoteDeletionWorker extends Worker {

    private final NoteRepository noteRepository;
    private final DebugContext debugContext = new DebugContext("NoteDeletionWorker");

    public NoteDeletionWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.noteRepository = Repositories.getInstance().getNoteRepository();
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Try.to(() -> getInputData().getLong("note_id", -1), debugContext).get()
                .ifPresent(noteRepository::deleteNote);

        return null;
    }
}
