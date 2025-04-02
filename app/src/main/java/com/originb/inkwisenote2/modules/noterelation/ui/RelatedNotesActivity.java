package com.originb.inkwisenote2.modules.noterelation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.repositories.*;
import com.originb.inkwisenote2.common.Routing;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class RelatedNotesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartNoteGridAdapter smartNoteGridAdapter;

    private SmartNotebookRepository smartNotebookRepository;
    private HandwrittenNoteRepository handwrittenNoteRepository;
    private NoteRelationRepository noteRelationRepository;

    private NoteRelationDao noteRelationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_notes);

        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        noteRelationRepository = Repositories.getInstance().getNoteRelationRepository();
        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

        Long rootBookId = getIntent().getLongExtra("book_id", 0);

        BackgroundOps.execute(() -> {
                    SmartNotebook smartNotebook = getRootBook(rootBookId);
                    Set<Long> noteIds = smartNotebook.atomicNotes.stream().map(AtomicNoteEntity::getNoteId).collect(Collectors.toSet());
                    List<NoteRelation> noteRelations = noteRelationDao.getRelatedNotesOf(noteIds);
                    Set<Long> allBookIds = noteRelations.stream()
                            .map(NoteRelation::getBookId).collect(Collectors.toSet());
                    allBookIds.addAll(noteRelations.stream()
                            .map(NoteRelation::getRelatedBookId).collect(Collectors.toSet()));
                    allBookIds.remove(smartNotebook.getSmartBook().getBookId());
                    List<SmartNotebook> allBooks = allBookIds.stream().map(smartNotebookRepository::getSmartNotebooks)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    AtomicNoteEntity firstNote = smartNotebook.getAtomicNotes().get(0);
                    HandwrittenNoteWithImage handwrittenNoteWithImage = handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL);

                    NotesDataOfFirstNote notesDataOfFirstNote = new NotesDataOfFirstNote();
                    notesDataOfFirstNote.setSmartNotebook(smartNotebook);
                    notesDataOfFirstNote.setHandwrittenNoteWithImage(handwrittenNoteWithImage);
                    notesDataOfFirstNote.setNoteRelations(new HashSet<>(noteRelations));
                    notesDataOfFirstNote.setAllBooksToShow(allBooks);
                    return notesDataOfFirstNote;
                },
                notesDataOfFirstNote -> {
                    setRootNote(notesDataOfFirstNote);
                    smartNoteGridAdapter.updateNoteRelations(notesDataOfFirstNote.getNoteRelations());
                    smartNoteGridAdapter.setSmartNotebooks(notesDataOfFirstNote.getAllBooksToShow());
                });

        createGridLayoutToShowNotes();
    }

    private SmartNotebook getRootBook(Long bookId) {
        Optional<SmartNotebook> noteEntityOpt = smartNotebookRepository.getSmartNotebooks(bookId);
        return noteEntityOpt.get();
    }

    private void setRootNote(NotesDataOfFirstNote notesDataOfFirstNote) {
        View includedCard = findViewById(R.id.main_note_card);

        // Then access its child views
        ImageView cardImage = includedCard.findViewById(R.id.card_image);
        TextView cardTitle = includedCard.findViewById(R.id.card_name);
        ImageButton deleteButton = includedCard.findViewById(R.id.btn_dlt_note);

        notesDataOfFirstNote.getHandwrittenNoteWithImage().noteImage
                .ifPresent(cardImage::setImageBitmap);
        SmartBookEntity smartBook = notesDataOfFirstNote.smartNotebook.getSmartBook();

        String noteTitle = Optional.ofNullable(smartBook.getTitle())
                .filter(title -> !title.trim().isEmpty())
                .orElse(DateTimeUtils.msToDateTime(smartBook.getLastModifiedTimeMillis()));
        cardTitle.setText(noteTitle);

        cardImage.setOnClickListener(v -> Routing.SmartNotebookActivity
                .openNotebookIntent(this, getFilesDir().getPath(), smartBook.getBookId()));
        cardTitle.setOnClickListener(v -> Routing.SmartNotebookActivity
                .openNotebookIntent(this, getFilesDir().getPath(), smartBook.getBookId()));

        deleteButton.setOnClickListener(v -> {
            notesDataOfFirstNote.smartNotebook.atomicNotes.forEach(note -> {
                handwrittenNoteRepository.deleteHandwrittenNote(note);
                noteRelationRepository.deleteNoteRelationData(note);
            });
            smartNotebookRepository.deleteSmartNotebook(notesDataOfFirstNote.smartNotebook);

            Routing.HomePageActivity.openHomePageAndStartFresh(this);
        });
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.related_note_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>(), false);

        recyclerView.setAdapter(smartNoteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    @Getter
    @Setter
    public static class NotesDataOfFirstNote {
        private SmartNotebook smartNotebook;
        private HandwrittenNoteWithImage handwrittenNoteWithImage;
        private Set<NoteRelation> noteRelations;
        private List<SmartNotebook> allBooksToShow;
    }
} 