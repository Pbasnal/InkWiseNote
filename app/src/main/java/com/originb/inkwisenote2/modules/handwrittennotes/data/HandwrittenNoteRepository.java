package com.originb.inkwisenote2.modules.handwrittennotes.data;

import android.content.Context;
import android.graphics.Bitmap;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote2.common.*;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.repositories.Repositories;
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
    AtomicNotesDomain atomicNotesDomain;

    // Maps noteId to a lock object for synchronizing file operations per note
    private final Map<Long, Object> noteLocks = new ConcurrentHashMap<>();

    public HandwrittenNoteRepository() {
        this.handwrittenNotesDao = Repositories.getInstance().getNotesDb().handwrittenNotesDao();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
    }

    /**
     * Get or create a lock object for a specific note
     */
    private Object getLockForNote(long noteId) {
        return noteLocks.computeIfAbsent(noteId, k -> new Object());
    }

    public void saveHandwrittenNoteImage(AtomicNoteEntity atomicNote, Bitmap bitmap) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath()) || Objects.isNull(bitmap)) {
            return;
        }
        try {
            String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
            String thumbnailPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + "-t.png";

            Bitmap thumbnail = BitmapFileIoUtils.resizeBitmap(bitmap, BitmapScale.THUMBNAIL.getValue());

            BitmapFileIoUtils.writeDataToDisk(fullPath, bitmap);
            BitmapFileIoUtils.writeDataToDisk(thumbnailPath, thumbnail);
        } catch (Exception ex) {
            logger.exception("Error saving handwritten note for noteId: " + atomicNote.getNoteId(), ex);
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

    public boolean saveHandwrittenNotes(long bookId,
                                        AtomicNoteEntity atomicNote,
                                        Bitmap bitmap,
                                        PageTemplate pageTemplate,
                                        List<Stroke> strokes,
                                        Context context) {
        // Synchronize operations on this specific note
        synchronized (getLockForNote(atomicNote.getNoteId())) {
            // Make a local copy of atomicNote to avoid modifying the original object
            AtomicNoteEntity noteToSave = new AtomicNoteEntity();
            noteToSave.setNoteId(atomicNote.getNoteId());
            noteToSave.setFilepath(atomicNote.getFilepath());
            noteToSave.setFilename(atomicNote.getFilename());
            noteToSave.setNoteType(atomicNote.getNoteType());

            String bitmapHash = getBitmapHash(bitmap);
            String pageTemplateHash = getPageTemplateHash(pageTemplate);

            boolean noteUpdated = false;

            HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNotesDao
                    .getHandwrittenNoteForNote(noteToSave.getNoteId());
            if (handwrittenNoteEntity == null) {
                handwrittenNoteEntity = new HandwrittenNoteEntity();
                handwrittenNoteEntity.setNoteId(noteToSave.getNoteId());
                handwrittenNoteEntity.setBookId(bookId);

                String bitmapFilePath = noteToSave.getFilepath() + "/" + noteToSave.getFilename() + ".png";
                handwrittenNoteEntity.setBitmapFilePath(bitmapFilePath);
                handwrittenNoteEntity.setBitmapHash(bitmapHash);

                handwrittenNoteEntity.setCreatedTimeMillis(System.currentTimeMillis());
                handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
                saveHandwrittenNoteImage(noteToSave, bitmap);
                saveHandwrittenNoteMarkdown(noteToSave, strokes);
                handwrittenNotesDao.insertHandwrittenNote(handwrittenNoteEntity);
                noteUpdated = true;
            } else if (bitmapHash != null && !bitmapHash.equals(handwrittenNoteEntity.getBitmapHash())) {
                handwrittenNoteEntity.setBitmapHash(bitmapHash);
                handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
                saveHandwrittenNoteImage(noteToSave, bitmap);
                saveHandwrittenNoteMarkdown(noteToSave, strokes);
                handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
                noteUpdated = true;
            }

            if (handwrittenNoteEntity.getPageTemplateHash() == null
                    && pageTemplateHash != null) {
                handwrittenNoteEntity.setPageTemplateFilePath(noteToSave.getFilepath() + "/" + noteToSave.getFilename() + ".pt");
                handwrittenNoteEntity.setPageTemplateHash(pageTemplateHash);
                saveHandwrittenNotePageTemplate(noteToSave, pageTemplate);
                handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
            } else if (pageTemplateHash != null && !pageTemplateHash.equals(handwrittenNoteEntity.getPageTemplateHash())) {
                handwrittenNoteEntity.setPageTemplateHash(bitmapHash);
                handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
                saveHandwrittenNotePageTemplate(noteToSave, pageTemplate);
                handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
            }

            if (noteUpdated) {
                // Ensure the original AtomicNoteEntity has the same filepath for consistency
                if (!noteToSave.getFilepath().equals(atomicNote.getFilepath())) {
                    // Update the filepath in the database
                    atomicNotesDomain.updateAtomicNote(noteToSave);
                }

                EventBus.getDefault().post(new Events.HandwrittenNoteSaved(bookId, noteToSave, context));
            }

            return noteUpdated;
        }
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

    private String getBitmapHash(Bitmap bitmap) {
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        return HashUtils.calculateSha256(bitmapStream.toByteArray());
    }

    private String getPageTemplateHash(PageTemplate pageTemplate) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
                objectStream.writeObject(pageTemplate); // Serialize the object
            }
            return HashUtils.calculateSha256(byteStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Saves handwritten note strokes to a markdown file with custom "inkwise" fenced code blocks
     *
     * @param atomicNote The note entity
     * @param strokes    The list of strokes to save
     * @return true if successfully saved, false otherwise
     */
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
                markdown.append(serializeStroke(stroke)).append("\n");
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

    /**
     * Serializes a stroke to a string format that can be stored in markdown
     *
     * @param stroke The stroke to serialize
     * @return String representation of the stroke
     */
    private String serializeStroke(Stroke stroke) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        // Add stroke properties
        builder.append("\"color\":").append(stroke.getColor()).append(",");
        builder.append("\"width\":").append(stroke.getWidth()).append(",");

        // Add points
        builder.append("\"points\":[");
        List<StrokePoint> points = stroke.getPoints();
        for (int i = 0; i < points.size(); i++) {
            StrokePoint point = points.get(i);
            builder.append("{")
                    .append("\"x\":").append(point.getX()).append(",")
                    .append("\"y\":").append(point.getY()).append(",")
                    .append("\"p\":").append(point.getPressure()).append(",")
                    .append("\"t\":").append(point.getTimestamp())
                    .append("}");

            if (i < points.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        builder.append("}");
        return builder.toString();
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

    /**
     * Reads strokes from a markdown file with custom "inkwise" fenced code blocks
     *
     * @param atomicNote The note entity containing file information
     * @return List of strokes or empty list if file doesn't exist or is invalid
     */
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

    public void deleteHandwrittenNoteMarkdown(AtomicNoteEntity atomicNote) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath())) return;
        Path path = Paths.get(atomicNote.getFilepath(), atomicNote.getFilename());
        Path pathWithExtension = Paths.get(path.toString() + ".md");
        try {
            Files.delete(pathWithExtension);
        } catch (Exception e) {
            System.out.println("Failed to delete the file: " + e.getMessage());
        }
    }

    /**
     * Reads strokes from a markdown file containing "inkwise" fenced code blocks
     *
     * @param filePath Path to the markdown file
     * @return List of strokes or empty list if file doesn't exist or parsing fails
     */
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
                        Stroke stroke = deserializeStroke(line);
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

    /**
     * Deserializes a stroke from its string representation
     *
     * @param strokeStr String representation of the stroke
     * @return Deserialized Stroke object
     * @throws JSONException if parsing fails
     */
    private Stroke deserializeStroke(String strokeStr) throws JSONException {
        JSONObject strokeJson = new JSONObject(strokeStr);

        Stroke stroke = new Stroke();
        stroke.setColor(strokeJson.getInt("color"));
        stroke.setWidth((float) strokeJson.getDouble("width"));

        JSONArray pointsArray = strokeJson.getJSONArray("points");
        for (int i = 0; i < pointsArray.length(); i++) {
            JSONObject pointJson = pointsArray.getJSONObject(i);
            float x = (float) pointJson.getDouble("x");
            float y = (float) pointJson.getDouble("y");
            float p = (float) pointJson.getDouble("p");
            long t = pointJson.has("t") ? pointJson.getLong("t") : System.currentTimeMillis();
            StrokePoint point = new StrokePoint(x, y, p, t);
            stroke.addPoint(point);
        }

        return stroke;
    }

    /**
     * Gets strokes for a specific note ID
     *
     * @param noteId The ID of the note to retrieve strokes for
     * @return List of strokes or empty list if none found
     */
    public List<Stroke> getStrokes(long noteId) {
        AtomicNoteEntity note = Repositories.getInstance().getAtomicNotesDomain().getAtomicNote(noteId);
        if (note == null) {
            logger.error("Could not find note with ID: " + noteId);
            return new ArrayList<>();
        }

        return readHandwrittenNoteMarkdown(note);
    }

    /**
     * Gets the parent directory from a filepath
     *
     * @param filepath The full filepath
     * @return The parent directory path
     */
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
