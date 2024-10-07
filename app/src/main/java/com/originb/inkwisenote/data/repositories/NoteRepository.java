package com.originb.inkwisenote.data.repositories;

import android.graphics.Bitmap;
import android.util.Log;
import com.originb.inkwisenote.data.NoteEntity;
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.data.config.PageTemplate;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
import com.originb.inkwisenote.modules.Repositories;

import java.util.*;
import java.util.stream.Collectors;

public class NoteRepository {

    private NoteMetaFiles noteMetaFiles;
    private NoteBitmapFiles noteBitmapFiles;
    private PageTemplateFiles pageTemplateFiles;

    public NoteRepository() {
        this.noteMetaFiles = Repositories.getInstance().getNotesRepository();
        this.noteBitmapFiles = Repositories.getInstance().getBitmapRepository();
        this.pageTemplateFiles = Repositories.getInstance().getPageTemplateFiles();
    }

    public void loadAllNotes() {
        noteMetaFiles.loadAll();
        noteBitmapFiles.loadAllAsThumbnails();
        pageTemplateFiles.loadAll();
    }

    public void deleteNoteAtIndex(int position) {
        Long noteId = noteMetaFiles.getNoteAtIndex(position).getNoteId();
        deleteNote(noteId);
    }

    public void deleteNote(Long noteId) {
        noteMetaFiles.deleteNoteFromDisk(noteId);
        noteBitmapFiles.deleteBitmap(noteId);
        pageTemplateFiles.deletePageTemplate(noteId);
    }

    public NoteMeta getNoteAtIndex(int position) {
        return noteMetaFiles.getNoteAtIndex(position);
    }

    public Optional<NoteEntity> getNoteEntity(Long noteId) {
        Optional<NoteMeta> noteMeta = noteMetaFiles.getNote(noteId);
        Optional<Bitmap> bitmap = noteBitmapFiles.getFullBitmap(noteId);
        Optional<PageTemplate> pageTemplate = pageTemplateFiles.getPageTemplate(noteId);

        if (noteMeta.isPresent() && bitmap.isPresent() && pageTemplate.isPresent()) {
            return Optional.of(new NoteEntity(noteId, noteMeta.get(), bitmap.get(), pageTemplate.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Bitmap> getThumbnail(Long noteId) {
        return noteBitmapFiles.getThumbnail(noteId);
    }

    public int numberOfNotes() {
        return noteMetaFiles.numberOfNotes();
    }

    public Optional<NoteEntity> saveNote(String notePath, String noteTitle, Bitmap bitmap, PageTemplate pageTemplate) {
        NoteMeta noteMeta = NoteMetaFiles.createNewNote(noteTitle);

        noteMetaFiles.saveNote(notePath, noteMeta.getNoteId(), noteMeta);
        noteBitmapFiles.saveBitmap(noteMeta.getNoteId(),
                noteMetaFiles.getDirectoryOfNote(noteMeta.getNoteId()),
                noteMeta.getNoteFileName(),
                bitmap);
        pageTemplateFiles.savePageTemplate(noteMeta.getNoteId(),
                noteMetaFiles.getDirectoryOfNote(noteMeta.getNoteId()),
                noteMeta.getNoteFileName(),
                pageTemplate);

        return getNoteEntity(noteMeta.getNoteId());
    }

    public void updateNote(NoteMeta noteMeta, Bitmap bitmap, PageTemplate pageTemplate) {
        noteMetaFiles.updateNoteMeta(noteMeta.getNoteId(), noteMeta);
        noteBitmapFiles.updateBitmap(noteMeta.getNoteId(), bitmap);
        pageTemplateFiles.savePageTemplate(noteMeta.getNoteId(),
                noteMetaFiles.getDirectoryOfNote(noteMeta.getNoteId()),
                noteMeta.getNoteFileName(),
                pageTemplate);
    }

    public void updateNoteMeta(NoteMeta noteMeta) {
        noteMetaFiles.updateNoteMeta(noteMeta.getNoteId(), noteMeta);
    }

    public List<NoteEntity> getNextNote(Long noteId) {
        Set<Long> nextNoteIds = noteMetaFiles.getNote(noteId)
                .map(NoteMeta::getNextNoteIds)
                .filter(nextNotes -> {
                    if (nextNotes.isEmpty()) {
                        Log.i("NoteRepository", "No next notes");
                    }
                    return !nextNotes.isEmpty();
                })
                .orElseGet(HashSet::new);

        return nextNoteIds.stream()
                .map(this::getNoteEntity)
                .map(n -> n.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NoteEntity> getPrevNote(Long noteId) {
        Set<Long> prevNoteIds = noteMetaFiles.getNote(noteId)
                .map(NoteMeta::getPrevNoteIds)
                .filter(prevNotes -> {
                    if (prevNotes.isEmpty()) {
                        Log.i("NoteRepository", "No previous notes");
                    }
                    return !prevNotes.isEmpty();
                })
                .orElseGet(HashSet::new);

        return prevNoteIds.stream()
                .map(this::getNoteEntity)
                .map(n -> n.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
