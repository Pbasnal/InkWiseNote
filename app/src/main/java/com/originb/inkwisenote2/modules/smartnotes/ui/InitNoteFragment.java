package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import org.greenrobot.eventbus.EventBus;


public class InitNoteFragment extends NoteFragment {
    private final Logger logger = new Logger("InitNoteHolder");
    private CardView cardToHandwriting;
    private CardView cardToText;
    private SmartNotebookAdapter adapter;

    private ImageButton deleteNote;

    private SmartNotebookRepository smartNotebookRepository;
    private SmartNotebook smartNotebook;

    public InitNoteFragment(SmartNotebookAdapter adapter) {
        this.adapter = adapter;
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.note_init_fragment, container, false);

        cardToHandwriting = itemView.findViewById(R.id.touch_to_write);
        cardToHandwriting.setOnClickListener(this::createHandwrittenNote);

        cardToText = itemView.findViewById(R.id.tap_to_text);
        cardToText.setOnClickListener(this::createTextNote);

        BackgroundOps.execute(() -> smartNotebookRepository.getSmartNotebooks(bookId),
                smartNotebookOpt -> smartNotebookOpt.ifPresent(smartNotebook -> {
                    this.smartNotebook = smartNotebook;
                    if (smartNotebook.getAtomicNotes().size() <= 1) {
                        deleteNote.setVisibility(View.GONE);
                    } else {
                        deleteNote.setVisibility(View.VISIBLE);
                    }
                }));

        deleteNote = itemView.findViewById(R.id.delete_note);
        deleteNote.setOnClickListener(v ->
                EventBus.getDefault().post(new Events.NoteDeleted(
                        smartNotebook,
                        atomicNote
                )));
        return itemView;
    }

    private void createTextNote(View view) {
        if (atomicNote == null) return;
        adapter.updateNoteType(atomicNote, NoteType.TEXT_NOTE.toString());
    }

    private void createHandwrittenNote(View view) {
        if (atomicNote == null) return;
        adapter.updateNoteType(atomicNote, NoteType.HANDWRITTEN_PNG.toString());
    }

    @Override
    public NoteHolderData getNoteHolderData() {
        return NoteHolderData.initNoteData();
    }
}
