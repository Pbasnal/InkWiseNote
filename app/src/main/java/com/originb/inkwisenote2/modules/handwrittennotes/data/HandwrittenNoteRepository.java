package com.originb.inkwisenote2.modules.handwrittennotes.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote2.common.*;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.ui.ThumbnailGenerator;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HandwrittenNoteRepository {
    private final Logger logger = new Logger("HandwrittenNoteRepository");
    private final HandwrittenNotesDao handwrittenNotesDao;
    private final AtomicNotesDomain atomicNotesDomain;

    // Maps noteId to a lock object for synchronizing file operations per note
    private final Map<Long, Object> noteLocks = new ConcurrentHashMap<>();

    public HandwrittenNoteRepository(
            HandwrittenNotesDao dao,
            AtomicNotesDomain domain) {
        this.handwrittenNotesDao = dao;
        this.atomicNotesDomain = domain;
    }

    private Object getLockForNote(long noteId) {
        return noteLocks.computeIfAbsent(noteId, k -> new Object());
    }

    public void saveHandwrittenNoteImage(AtomicNoteEntity note, Bitmap bitmap, List<Stroke> strokes) {
        if (bitmap == null || Strings.isEmptyOrWhitespace(note.getFilepath())) return;

        try {
            Bitmap thumbnail = ThumbnailGenerator.generateThumbnail(bitmap, strokes);
            BitmapFileIoUtils.writeDataToDisk(NoteFileStorage.getImagePath(note), bitmap);
            BitmapFileIoUtils.writeDataToDisk(NoteFileStorage.getThumbnailPath(note), thumbnail);
        } catch (Exception ex) {
            logger.exception("Error saving bitmap for note: " + note.getNoteId(), ex);
        }
    }

    public void saveHandwrittenNotePageTemplate(AtomicNoteEntity atomicNote, PageTemplate pageTemplate) {
        String path = atomicNote.getFilepath();
        if (Objects.isNull(pageTemplate) || Strings.isEmptyOrWhitespace(path)) {
            return;
        }

        String fullPath = path + "/" + atomicNote.getFilename() + ".pt";
        BytesFileIoUtils.writeDataToDisk(fullPath, pageTemplate);
    }

    public boolean saveHandwrittenNotes(long bookId, AtomicNoteEntity atomicNote, Bitmap bitmap,
                                        PageTemplate pageTemplate, List<Stroke> strokes, Context context) {

        AtomicNoteEntity note = atomicNote.clone();
        // Use synchronized on the note ID string intern to be thread-safe across instances
        synchronized (getLockForNote(note.getNoteId())) {
            String bitmapHash = HashUtils.getBitmapHash(bitmap); // Move hashing to a Utility
            HandwrittenNoteEntity entity = handwrittenNotesDao.getHandwrittenNoteForNote(note.getNoteId());

            boolean isNew = (entity == null);
            if (isNew) {
                entity = createNewEntity(bookId, note, bitmapHash);
            }

            boolean updated = false;
            if (isNew || !Objects.equals(bitmapHash, entity.getBitmapHash())) {
                updateVisualData(entity, note, bitmap, strokes, bitmapHash);
                updated = true;
            }

            // Handle Template Hash logic...
            processTemplateUpdate(entity, note, pageTemplate);

            if (updated) {
                handwrittenNotesDao.upsert(entity); // Use a Room @Upsert
                if (!note.getFilepath().equals(atomicNote.getFilepath())) {
                    // Update the filepath in the database
                    atomicNotesDomain.updateAtomicNote(note);
                }
                EventBus.getDefault().post(new Events.HandwrittenNoteSaved(bookId, note, context));
            }
            return updated;
        }
    }

    private HandwrittenNoteEntity createNewEntity(long bookId, AtomicNoteEntity note, String hash) {
        HandwrittenNoteEntity entity = new HandwrittenNoteEntity();
        entity.setNoteId(note.getNoteId());
        entity.setBookId(bookId);
        entity.setBitmapFilePath(NoteFileStorage.getImagePath(note));
        entity.setBitmapHash(hash);
        entity.setCreatedTimeMillis(System.currentTimeMillis());
        entity.setLastModifiedTimeMillis(System.currentTimeMillis());
        return entity;
    }

    private void updateVisualData(HandwrittenNoteEntity entity, AtomicNoteEntity note,
                                  Bitmap bitmap, List<Stroke> strokes, String hash) {
        entity.setBitmapHash(hash);
        entity.setLastModifiedTimeMillis(System.currentTimeMillis());
        saveHandwrittenNoteImage(note, bitmap, strokes);
        saveHandwrittenNoteMarkdown(note, strokes);
    }

    private void processTemplateUpdate(HandwrittenNoteEntity entity,
                                       AtomicNoteEntity note,
                                       PageTemplate pageTemplate) {
        String hash = HashUtils.getPageTemplateHash(pageTemplate);
        if (hash == null) {
            Log.e("HandwrittenNoteRepository", "Failed to generate page template hash");
            return;
        }

        boolean pageTemplateIsSame = hash.equals(entity.getPageTemplateHash());
        if (pageTemplateIsSame) return;

        boolean isNew = entity.getPageTemplateHash() == null;

        if (isNew) {
            entity.setPageTemplateFilePath(NoteFileStorage.getTemplatePath(note));
        }

        entity.setPageTemplateHash(hash);
        entity.setLastModifiedTimeMillis(System.currentTimeMillis());
        saveHandwrittenNotePageTemplate(note, pageTemplate);
        handwrittenNotesDao.updateHandwrittenNote(entity);
    }

    public HandwrittenNoteWithImage getNoteImage(AtomicNoteEntity atomicNote, BitmapScale imageScale) {
        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNotesDao.getHandwrittenNoteForNote(atomicNote.getNoteId());
        HandwrittenNoteWithImage handwrittenNoteWithImage = new HandwrittenNoteWithImage();

        handwrittenNoteWithImage.handwrittenNoteEntity = handwrittenNoteEntity;

        String fullPath;
        if (BitmapScale.FULL_SIZE.equals(imageScale)) {
            fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        } else {
            fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + "-t.png";
        }
        handwrittenNoteWithImage.noteImage = BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.FULL_SIZE);
        return handwrittenNoteWithImage;
    }

    public Optional<PageTemplate> getPageTemplate(AtomicNoteEntity atomicNote) {
        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt";
        return BytesFileIoUtils.readDataFromDisk(fullPath, PageTemplate.class);
    }

    public void deleteHandwrittenNote(AtomicNoteEntity atomicNote) {
        String bitmapPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        BitmapFileIoUtils.deleteBitmap(bitmapPath);
        String thumbnailPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + "-t.png";
        BitmapFileIoUtils.deleteBitmap(thumbnailPath);

        String templPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt";
        File noteFile = new File(templPath);
        noteFile.delete();

        // Delete markdown file
        String markdownPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md";
        File markdownFile = new File(markdownPath);
        markdownFile.delete();

        File notebookDir = new File(atomicNote.getFilepath());
        if (notebookDir.exists() && notebookDir.isDirectory()) {
            // Delete all files in the directory
            File[] files = notebookDir.listFiles();
            if (files == null || files.length == 0) {
                notebookDir.delete();
            }
        }
    }

    public boolean saveHandwrittenNoteMarkdown(AtomicNoteEntity atomicNote, List<Stroke> strokes) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath()) || strokes == null) {
            return false;
        }

        String markdownPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md";
        try (FileWriter writer = new FileWriter(markdownPath)) {
            StringBuilder markdown = new StringBuilder();

            // Add markdown header
            markdown.append("# Handwritten Note\n\n");

            // Begin inkwise code block
            markdown.append("```inkwise\n");

            // Convert strokes to JSON-like format within the code block
            for (Stroke stroke : strokes) {
                markdown.append(NoteFileStorage.serializeStroke(stroke)).append("\n");
            }

            // End inkwise code block
            markdown.append("```\n");

            writer.write(markdown.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getStrokesHash(List<Stroke> strokes) {
        if (strokes == null || strokes.isEmpty()) {
            return null;
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
                objectStream.writeObject(strokes);
            }
            return HashUtils.calculateSha256(byteStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Stroke> readHandwrittenNoteMarkdown(AtomicNoteEntity atomicNote) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath())) {
            return new ArrayList<>();
        }

        String markdownPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md";
        File file = new File(markdownPath);
        if (!file.exists() || !file.isFile()) {
            return new ArrayList<>();
        }

        return readStrokesFromMarkdown(markdownPath);
    }

    private List<Stroke> readStrokesFromMarkdown(String filePath) {
        List<Stroke> strokes = new ArrayList<>();
        boolean inCodeBlock = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check for beginning of inkwise code block
                if (line.trim().equals("```inkwise")) {
                    inCodeBlock = true;
                    continue;
                }

                // Check for end of code block
                if (line.trim().equals("```")) {
                    inCodeBlock = false;
                    continue;
                }

                // Parse stroke data if within code block
                if (inCodeBlock && !line.trim().isEmpty()) {
                    try {
                        Stroke stroke = NoteFileStorage.deserializeStroke(line);
                        if (stroke != null) {
                            strokes.add(stroke);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strokes;
    }

    public List<Stroke> getStrokes(long noteId) {
        // Use the injected AtomicNotesDomain from constructor
        AtomicNoteEntity note = atomicNotesDomain.getAtomicNote(noteId);
        if (note == null) {
            logger.error("Could not find note with ID: " + noteId);
            return new ArrayList<>();
        }

        return readHandwrittenNoteMarkdown(note);
    }

    private String getParentDirectory(String filepath) {
        if (Strings.isEmptyOrWhitespace(filepath)) {
            return "";
        }

        int lastSlashIndex = filepath.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            return filepath.substring(0, lastSlashIndex);
        }
        return filepath;
    }

}
