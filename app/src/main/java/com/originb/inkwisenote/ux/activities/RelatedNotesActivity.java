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
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.io.sql.NoteTermFrequencyContract;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.tfidf.NoteTfIdfLogic;
import com.originb.inkwisenote.ux.utils.Routing;

import java.util.*;
import java.util.stream.Collectors;

public class RelatedNotesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteGridAdapter noteGridAdapter;

    private NoteTermFrequencyContract.NoteTermFrequencyDbQueries noteTermFrequencyDbQueries;
    private NoteRepository noteRepository;

    private NoteTfIdfLogic noteTfIdfLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_notes);

        noteRepository = Repositories.getInstance().getNoteRepository();
        noteTermFrequencyDbQueries = Repositories.getInstance().getNoteTermFrequencyDbQueries();

        noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());

        Long rootNoteId = getIntent().getLongExtra("noteId", 0);
        NoteEntity noteEntity = getRootNote(rootNoteId);
        setRootNote(noteEntity);

        createGridLayoutToShowNotes();

        Set<Long> relatedNoteIds = getNoteIdsRelatedByTfIdf(noteEntity);
        relatedNoteIds.addAll(getNotesRelatedByCreation(noteEntity));

        relatedNoteIds.remove(rootNoteId);
        noteGridAdapter.setNoteIds(new ArrayList<>(relatedNoteIds));
    }

    private Set<Long> getNotesRelatedByCreation(NoteEntity noteEntity) {
        Set<Long> connectedNotes = noteEntity.getNoteMeta().getNextNoteIds();
        connectedNotes.addAll(noteEntity.getNoteMeta().getPrevNoteIds());

        return connectedNotes;
    }

    private Set<Long> getNoteIdsRelatedByTfIdf(NoteEntity noteEntity) {
        Map<String, Double> tfIdfScores = noteTfIdfLogic.getTfIdf(noteEntity.getNoteId());
        Set<String> filteredTerms = new HashSet<>();
        for (String key : tfIdfScores.keySet()) {
            if (tfIdfScores.get(key) > 0.1) {
                filteredTerms.add(key);
            }
        }

        Map<String, Set<Long>> termNoteIds = noteTermFrequencyDbQueries.getNoteIdsForTerms(filteredTerms);
        Set<Long> relatedNoteIds = termNoteIds.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        return relatedNoteIds;
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
            noteRepository.deleteNote(noteEntity.getNoteId());
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