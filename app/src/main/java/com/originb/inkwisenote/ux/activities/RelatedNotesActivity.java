package com.originb.inkwisenote.ux.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.adapters.NoteGridAdapter;
import com.originb.inkwisenote.data.dao.NoteRelationDao;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.entities.notedata.NoteRelation;
import com.originb.inkwisenote.modules.backgroundworkers.WorkManagerBus;
import com.originb.inkwisenote.modules.noteoperations.NoteOperations;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.ux.utils.Routing;

import java.util.*;

public class RelatedNotesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteGridAdapter noteGridAdapter;
    private NoteOperations noteOperations;

    private NoteRepository noteRepository;

    private NoteRelationDao noteRelationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_notes);

        noteRepository = Repositories.getInstance().getNoteRepository();
        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();
        noteOperations = new NoteOperations(this);

        Long rootNoteId = getIntent().getLongExtra("noteId", 0);
        NoteEntity noteEntity = getRootNote(rootNoteId);
        setRootNote(noteEntity);

        createGridLayoutToShowNotes();

        noteRelationDao.getRelatedNotesOf(noteEntity.getNoteId())
                .observe(this, noteRelations -> {

                    Set<Long> relatedNoteIds = new HashSet<>();
                    for (NoteRelation noteRelation : noteRelations) {
                        relatedNoteIds.add(noteRelation.getNoteId());
                        relatedNoteIds.add(noteRelation.getRelatedNoteId());
                    }

                    relatedNoteIds.remove(rootNoteId);
                    noteGridAdapter.setNoteIds(relatedNoteIds);
                });
    }

    private NoteEntity getRootNote(Long rootNoteId) {
        Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(rootNoteId);
        return noteEntityOpt.get();
    }

    private void setRootNote(NoteEntity noteEntity) {
        View includedCard = findViewById(R.id.main_note_card);

        // Then access its child views
        ImageView cardImage = includedCard.findViewById(R.id.card_image);
        TextView cardTitle = includedCard.findViewById(R.id.card_name);
        ImageButton deleteButton = includedCard.findViewById(R.id.btn_dlt_note);


        noteRepository.getThumbnail(noteEntity.getNoteId())
                .ifPresent(cardImage::setImageBitmap);

        String noteTitle = Optional.ofNullable(noteEntity.getNoteMeta().getNoteTitle())
                .filter(title -> !title.trim().isEmpty())
                .orElse(noteEntity.getNoteMeta().getCreateDateTimeString());
        cardTitle.setText(noteTitle);

        cardImage.setOnClickListener(v -> Routing.NoteActivity.openNoteIntent(this, getFilesDir().getPath(), noteEntity.getNoteId()));
        cardTitle.setOnClickListener(v -> Routing.NoteActivity.openNoteIntent(this, getFilesDir().getPath(), noteEntity.getNoteId()));

        deleteButton.setOnClickListener(v -> {
            noteOperations.deleteNote(noteEntity.getNoteId());
            Routing.HomePageActivity.openHomePageAndStartFresh(this);
        });
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.related_note_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(this, new ArrayList<>());

        recyclerView.setAdapter(noteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }
} 